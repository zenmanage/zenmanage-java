package com.zenmanage.sdk.flags;

import com.fasterxml.jackson.core.JsonProcessingException;
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
        ensureRulesLoaded();

        for (Flag flag : sharedState.flags) {
            if (flag.getKey().equals(key)) {
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

    private synchronized void ensureRulesLoaded() {
        if (sharedState.flags != null) {
            return;
        }

        Optional<String> cached = cache.get(CACHE_KEY);
        if (cached.isPresent()) {
            try {
                RulesResponse cachedResponse = objectMapper.readValue(cached.get(), RulesResponse.class);
                if (cachedResponse != null && cachedResponse.getFlags() != null) {
                    sharedState.flags = toFlags(cachedResponse.getFlags());
                    return;
                }
            } catch (JsonProcessingException exception) {
                logger.warn("Failed to parse cached rules");
            }
        }

        loadRulesFromApi();
    }

    private void loadRulesFromApi() {
        RulesResponse response = apiClient.getRules();
        sharedState.flags = toFlags(response.getFlags());

        try {
            cache.set(CACHE_KEY, objectMapper.writeValueAsString(response), cacheTtlSeconds);
        } catch (JsonProcessingException exception) {
            logger.warn("Failed to serialize rules for cache");
        }
    }

    private List<Flag> toFlags(List<FlagData> flagDataList) {
        List<Flag> result = new ArrayList<>();
        for (FlagData data : flagDataList) {
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
        private List<Flag> flags;
    }
}
