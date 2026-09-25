# Zenmanage Java SDK

[![Maven Central](https://img.shields.io/maven-central/v/com.zenmanage/zenmanage-java.svg)](https://search.maven.org/search?q=g:com.zenmanage%20AND%20a:zenmanage-java)

Add feature flags to your Java application in minutes. Control feature rollouts, run A/B tests, and manage runtime configuration without redeploying.

## Why Zenmanage?

- Fast evaluations with local caching
- Context-aware targeting for users and organizations
- Deterministic percentage rollouts (CRC32 bucketing)
- Graceful defaults and error handling
- Designed for testability and observability

## Requirements

- Java 11+
- Maven 3.9+ (for build/test)

For local build/test with JaCoCo in this repository, Java 21 is recommended.

## Installation

```xml
<dependency>
  <groupId>com.zenmanage</groupId>
  <artifactId>zenmanage-java</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Get Started

```java
import com.zenmanage.sdk.Zenmanage;
import com.zenmanage.sdk.config.Config;
import com.zenmanage.sdk.config.ConfigBuilder;

Config config = ConfigBuilder.create()
    .withEnvironmentToken("srv_your_server_key_here")
    .build();

Zenmanage zenmanage = new Zenmanage(config);

boolean enabled = zenmanage.flags()
    .single("new-dashboard", false)
    .isEnabled();
```

## Common Use Cases

### Simple Flags

```java
boolean enabled = zenmanage.flags().single("feature-x", false).isEnabled();
String variant = zenmanage.flags().single("checkout-flow", "control").asString();
double timeout = zenmanage.flags().single("api-timeout", 5000).asNumber();
```

### JSON Configuration

```java
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

// Structured configuration values (JSON objects and JSON arrays both decode
// to a Jackson JsonNode — ObjectNode/ArrayNode respectively)
JsonNode theme = zenmanage.flags()
    .single("theme-config", Map.of("mode", "light", "accent", "#4f46e5"))
    .asJson();

JsonNode rolloutPlan = zenmanage.flags()
    .single("rollout-plan", List.of())
    .asJson();
```

### Context-Based Evaluation

```java
import com.zenmanage.sdk.context.Attribute;
import com.zenmanage.sdk.context.Context;

Context context = Context.single("user", "user-123", "Alice")
    .addAttribute(new Attribute("country").addValue("US"));

boolean premium = zenmanage.flags()
    .withContext(context)
    .single("premium-feature", false)
    .isEnabled();
```

### Defaults Collection

```java
import com.zenmanage.sdk.flags.DefaultsCollection;

DefaultsCollection defaults = new DefaultsCollection()
    .set("new-ui", true)
    .set("api-version", "v2")
    .set("max-items", 100);

String version = zenmanage.flags()
    .withDefaults(defaults)
    .single("api-version")
    .asString();
```

### Percentage Rollouts

```java
Context context = Context.single("user", "user-999");

boolean inRollout = zenmanage.flags()
    .withContext(context)
    .single("new-checkout-flow", false)
    .isEnabled();
```

### Spring Boot Auto-Configuration

If your application uses Spring Boot, the SDK can auto-configure itself from application properties.

```properties
zenmanage.environment-token=srv_your_server_key_here
zenmanage.cache-ttl-seconds=3600
zenmanage.cache-backend=memory
zenmanage.usage-reporting=true
zenmanage.api-endpoint=https://api.zenmanage.com
```

Then inject the SDK anywhere:

```java
import com.zenmanage.sdk.Zenmanage;

@Service
public final class CheckoutService {
    private final Zenmanage zenmanage;

    public CheckoutService(Zenmanage zenmanage) {
        this.zenmanage = zenmanage;
    }
}
```

Auto-configured Spring beans:

- `Zenmanage`
- `Config`
- `FlagManager`

You can override the cache by defining your own Spring `Cache` bean of type `com.zenmanage.sdk.cache.Cache`.

## Configuration

```java
Config config = ConfigBuilder.create()
    .withEnvironmentToken("srv_your_server_key_here")
    .withCacheBackend("memory")
    .withCacheTtlSeconds(3600)
    .withUsageReporting(true)
    .withApiEndpoint("https://api.zenmanage.com")
    .build();
```

Environment-based setup:

```java
Config config = ConfigBuilder.fromEnvironment().build();
```

Supported environment variables:

- `ZENMANAGE_ENVIRONMENT_TOKEN`
- `ZENMANAGE_CACHE_TTL`
- `ZENMANAGE_CACHE_BACKEND`
- `ZENMANAGE_CACHE_DIR`
- `ZENMANAGE_ENABLE_USAGE_REPORTING`
- `ZENMANAGE_API_ENDPOINT`

## Value Types & Cross-Type Coercion

A flag's `type` is one of `boolean`, `string`, `number`, or `json`. Each type has a matching accessor (`asBool()`, `asString()`, `asNumber()`, `asJson()`), plus `isEnabled()` for boolean flags specifically.

**`asJson()`** returns the decoded value as a Jackson `JsonNode`. Both JSON objects (`{"a": 1}`) and JSON arrays (`[1, 2, 3]`) decode to their natural `JsonNode` subtype (`ObjectNode`/`ArrayNode` respectively) — the SDK never forces one shape onto the other. Calling it on a non-`json` flag, or on a `json` flag whose value is missing or malformed, returns `MissingNode.getInstance()` rather than `null` or a thrown exception, so it is always safe to call further `JsonNode` methods (`.isObject()`, `.isArray()`, `.get(...)`, `.size()`, etc.) on the result.

**`asJson()` only recognizes the flag's own `{"json": …}` value wrapper.** For every other flag type it falls back to the safe `MissingNode.getInstance()` sentinel instead of attempting a conversion — it never guesses at turning a boolean/string/number into JSON. `asBool()`, `asString()`, and `asNumber()` predate `json` support and take a different, best-effort approach for a "foreign" type: they parse or stringify the underlying value rather than returning a fixed placeholder. The full picture, calling each accessor against each flag type:

| Called on →<br>Flag type ↓ | `asBool()` | `asString()` | `asNumber()` | `asJson()` |
|---|---|---|---|---|
| `boolean` | the bool | stringified bool (`"true"`/`"false"`) | `1` or `0` | `MissingNode.getInstance()` |
| `string` | `true` only if the string equals `"true"` (case-insensitive), else `false` | the string | the string parsed as a number, or `0` if it isn't numeric | `MissingNode.getInstance()` |
| `number` | `true` unless the number is `0` | stringified number | the number | `MissingNode.getInstance()` |
| `json` | `false` | `""` | `0` | the decoded `JsonNode` |

**Default values** passed to `single(key, default)` or `DefaultsCollection` are typed from the Java value itself: a `Map<String, Object>` or `List<?>` default becomes a `json`-typed synthesized flag (not a stringified fallback), so `asJson()` on a missing flag with a `Map`/`List` default returns that structure decoded back into a `JsonNode`. A `com.fasterxml.jackson.databind.JsonNode` passed directly as a default is also accepted and typed as `json`.

## Caching Backends

- `memory`: in-process cache (default)
- `filesystem`: cache persisted to disk
- `null`: disables caching
- custom: provide your own `Cache` implementation

## Examples

See [examples/README.md](examples/README.md) for runnable sample programs:

- `simple_flags.java`
- `context_based_flags.java`
- `defaults.java`
- `caching.java`
- `ab_testing.java`
- `percentage_rollouts.java`

## Quality and Coverage

Run tests and coverage report:

```bash
mvn clean verify
```

Coverage report output:

- `target/site/jacoco/index.html`

## Publishing

See [PUBLISHING_NEXT_STEPS.md](PUBLISHING_NEXT_STEPS.md) for a step-by-step checklist to publish to Maven Central, GitHub Packages, and JitPack.

## CI/CD

- CI workflow: [.github/workflows/ci.yml](.github/workflows/ci.yml)
- Release workflow (tag-triggered): [.github/workflows/release.yml](.github/workflows/release.yml)

Release workflow expects repository secrets:

- `SONATYPE_USERNAME`
- `SONATYPE_PASSWORD`
- `GPG_PRIVATE_KEY`
- `GPG_PASSPHRASE`
