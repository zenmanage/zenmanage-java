package com.zenmanage.sdk.flags;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenmanage.sdk.api.ApiClient;
import com.zenmanage.sdk.cache.Cache;
import com.zenmanage.sdk.config.Logger;
import com.zenmanage.sdk.context.Context;
import com.zenmanage.sdk.errors.EvaluationException;
import com.zenmanage.sdk.rollout.RolloutBucketer;
import com.zenmanage.sdk.rules.RuleEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Loads, caches, and evaluates flags against context/default values.
 */
public final class FlagManager {
    private static final String CACHE_KEY = "zenmanage_rules";

    private final ApiClient apiClient;
    private final Cache cache;
    private final RuleEngine ruleEngine;
    private final int cacheTtlSeconds;
    private final Logger logger;
    private final SharedState sharedState;
    private final Context context;
    private final DefaultsCollection defaults;
    private final ObjectMapper objectMapper;

    public FlagManager(
        ApiClient apiClient,
        Cache cache,
        RuleEngine ruleEngine,
        int cacheTtlSeconds,
        Logger logger
    ) {
        this(apiClient, cache, ruleEngine, cacheTtlSeconds, logger,
            new SharedState(), new Context("anonymous"), new DefaultsCollection(), new ObjectMapper());
    }

    private FlagManager(
        ApiClient apiClient,
        Cache cache,
        RuleEngine ruleEngine,
        int cacheTtlSeconds,
        Logger logger,
        SharedState sharedState,
        Context context,
        DefaultsCollection defaults,
        ObjectMapper objectMapper
    ) {
        this.apiClient = apiClient;
        this.cache = cache;
        this.ruleEngine = ruleEngine;
        this.cacheTtlSeconds = cacheTtlSeconds;
        this.logger = logger;
        this.sharedState = sharedState;
        this.context = context;
        this.defaults = defaults;
        this.objectMapper = objectMapper;
    }

    public List<Flag> all() {
        ensureRulesLoaded();

        List<Flag> results = new ArrayList<>();
        for (Flag flag : sharedState.flags) {
            results.add(evaluateFlag(flag));
        }
        return results;
    }

    public Flag single(String key) {
        return single(key, null);
    }

    public Flag single(String key, Object defaultValue) {
        List<Flag> flags = loadFlagsOrFallBackToDefaults();

        for (Flag flag : flags) {
            if (flag.getKey().equals(key) && flag.getType() != FlagType.UNKNOWN) {
                reportUsage(key, usageContext(), resolveEffectiveDefault(key, defaultValue));
                return evaluateFlag(flag);
            }
        }

        if (defaultValue != null) {
            reportUsage(key, usageContext(), defaultValue);
            return createFlagFromDefault(key, defaultValue);
        }

        if (defaults.has(key)) {
            Object fallbackDefault = defaults.get(key);
            reportUsage(key, usageContext(), fallbackDefault);
            return createFlagFromDefault(key, fallbackDefault);
        }

        throw new EvaluationException("Flag not found: " + key);
    }

    /**
     * Load the current flag set, falling back to an empty list (so callers fall
     * through to their own default handling) if rule-loading fails outright — e.g.
     * an unreachable API, an invalid/unauthorized environment key, or an unexpected
     * bug in the loading/parsing path. Deliberately catches every {@link Exception},
     * not just the SDK's own {@code ZenmanageException} hierarchy: this SDK's core
     * value proposition is that a broken or unreachable API never crashes the host
     * application, so any failure here — expected or not — degrades to configured
     * defaults rather than propagating.
     */
    private List<Flag> loadFlagsOrFallBackToDefaults() {
        try {
            ensureRulesLoaded();
            return sharedState.flags == null ? List.of() : sharedState.flags;
        } catch (Exception exception) {
            logger.warn("Failed to load rules, falling back to configured defaults: " + exception.getMessage());
            return List.of();
        }
    }

    public FlagManager withContext(Context newContext) {
        return new FlagManager(apiClient, cache, ruleEngine, cacheTtlSeconds, logger, sharedState, newContext, defaults, objectMapper);
    }

    public FlagManager withDefaults(DefaultsCollection newDefaults) {
        return new FlagManager(apiClient, cache, ruleEngine, cacheTtlSeconds, logger, sharedState, context, newDefaults, objectMapper);
    }

    public void reportUsage(String key, Context usageContext) {
        apiClient.reportUsage(key, usageContext);
    }

    public void reportUsage(String key, Context usageContext, Object defaultValue) {
        apiClient.reportUsage(key, usageContext, defaultValue);
    }

    public void refreshRules() {
        logger.info("Refreshing rules from API");
        loadRulesFromApi();
    }

    private Object resolveEffectiveDefault(String key, Object defaultValue) {
        if (defaultValue != null) {
            return defaultValue;
        }

        return defaults.has(key) ? defaults.get(key) : null;
    }

    private Context usageContext() {
        if ("anonymous".equals(context.getType())
            && context.getName() == null
            && context.getIdentifier() == null
            && context.getAttributes().isEmpty()) {
            return null;
        }
        return context;
    }

    // sharedState.flags is volatile, so this fast path (the common case once rules are
    // loaded) never touches the lock at all — cheap for every flag lookup. withContext()/
    // withDefaults() hand out sibling FlagManager instances that all share the same
    // SharedState, so synchronized blocks below lock on sharedState itself, not the
    // FlagManager instance monitor ("this") — otherwise two sibling instances could race
    // on sharedState.flags with no real mutual exclusion between them.
    //
    // Neither this method nor loadRulesFromApi() holds the lock during the cache read or
    // the network call: a slow/unresponsive API must never block concurrent flag lookups
    // (on this thread or any other FlagManager instance sharing this SharedState) for as
    // long as that call takes. The lock only ever guards the final assignment. The
    // tradeoff is that concurrent first-loads on a cold cache may each fetch once (a
    // bounded, self-resolving "thundering herd") rather than serializing behind one
    // caller's round-trip.
    private void ensureRulesLoaded() {
        if (sharedState.flags != null) {
            return;
        }

        Optional<String> cached = cache.get(CACHE_KEY);
        if (cached.isPresent()) {
            try {
                RulesResponse cachedResponse = objectMapper.readValue(cached.get(), RulesResponse.class);
                if (cachedResponse != null && cachedResponse.getFlags() != null) {
                    setFlagsIfAbsent(toFlags(cachedResponse.getFlags()));
                    return;
                }
            } catch (JsonProcessingException exception) {
                logger.warn("Failed to parse cached rules");
            }
        }

        if (sharedState.flags == null) {
            loadRulesFromApi();
        }
    }

    // apiClient.getRules() guarantees a non-null response with a non-null getFlags()
    // list on every normal return — it throws InvalidRulesException itself otherwise —
    // so no redundant null-check is needed here.
    private void loadRulesFromApi() {
        RulesResponse response = apiClient.getRules();
        List<Flag> flags = toFlags(response.getFlags());

        synchronized (sharedState) {
            sharedState.flags = flags;
        }

        try {
            cache.set(CACHE_KEY, objectMapper.writeValueAsString(response), cacheTtlSeconds);
        } catch (JsonProcessingException exception) {
            logger.warn("Failed to serialize rules for cache");
        }
    }

    private void setFlagsIfAbsent(List<Flag> flags) {
        synchronized (sharedState) {
            if (sharedState.flags == null) {
                sharedState.flags = flags;
            }
        }
    }

    private List<Flag> toFlags(List<FlagData> flagDataList) {
        List<Flag> result = new ArrayList<>();
        for (FlagData data : flagDataList) {
            if (data.getType() == FlagType.UNKNOWN) {
                logger.warn("Flag \"" + data.getKey() + "\" has an unrecognized type and will be"
                    + " treated as missing; upgrade this SDK to evaluate it. Lookups will fall back"
                    + " to the caller-provided default.");
            }
            result.add(Flag.fromData(data));
        }
        return result;
    }

    private Flag evaluateFlag(Flag flag) {
        RolloutData rollout = flag.getRollout();
        FlagTarget target = flag.getTarget();
        List<Rule> rules = flag.getRules();

        if (rollout != null) {
            boolean inBucket = RolloutBucketer.isInBucket(
                rollout.getSalt(),
                context.getIdentifier(),
                rollout.getPercentage()
            );

            if (inBucket) {
                target = rollout.getTarget();
                rules = rollout.getRules() == null ? List.of() : rollout.getRules();
            }
        }

        if (rules.isEmpty()) {
            return new Flag(flag.getVersion(), flag.getType(), flag.getKey(), flag.getName(), target, rules, flag.getRollout());
        }

        Rule matchedRule = ruleEngine.evaluate(rules, context);
        if (matchedRule != null && matchedRule.getValue() != null) {
            FlagTarget mappedTarget = mapRuleValueOntoTarget(target, matchedRule.getValue());
            return new Flag(flag.getVersion(), flag.getType(), flag.getKey(), flag.getName(), mappedTarget, rules, flag.getRollout());
        }

        return new Flag(flag.getVersion(), flag.getType(), flag.getKey(), flag.getName(), target, rules, flag.getRollout());
    }

    private FlagTarget mapRuleValueOntoTarget(FlagTarget target, RuleValue ruleValue) {
        FlagTarget remapped = new FlagTarget();
        remapped.setVersion(target.getVersion());
        remapped.setExpiredAt(target.getExpiredAt());
        remapped.setPublishedAt(target.getPublishedAt());
        remapped.setScheduledAt(target.getScheduledAt());

        TargetValue value = new TargetValue();
        value.setVersion(ruleValue.getVersion());
        value.setValue(ruleValue.getValue());
        remapped.setValue(value);
        return remapped;
    }

    private Flag createFlagFromDefault(String key, Object defaultValue) {
        FlagType type;
        if (defaultValue instanceof Boolean) {
            type = FlagType.BOOLEAN;
        } else if (defaultValue instanceof Number) {
            type = FlagType.NUMBER;
        } else if (defaultValue instanceof Map || defaultValue instanceof List || defaultValue instanceof JsonNode) {
            type = FlagType.JSON;
        } else {
            type = FlagType.STRING;
        }

        RawFlagValue rawValue = RawFlagValue.fromJavaValue(defaultValue);
        TargetValue targetValue = new TargetValue();
        targetValue.setValue(rawValue);
        FlagTarget target = new FlagTarget();
        target.setValue(targetValue);

        return new Flag("1", type, key, key, target, List.of(), null);
    }

    private static final class SharedState {
        private volatile List<Flag> flags;
    }
}
