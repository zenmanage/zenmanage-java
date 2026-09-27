# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog and this project follows Semantic Versioning.

## [Unreleased]

### Added
- N/A

### Changed
- N/A

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- N/A

### Security
- N/A

## [1.0.0] - 2026-09-27

### Added
- Java SDK initial package scaffold
- Config builder, API client, rule engine, rollouts, and cache backends
- Example scripts matching PHP/JavaScript SDK example set
- Test suite and JaCoCo reporting
- CI and release workflows for public publishing
- `json` flag type support: `FlagType.JSON`, a `json` value wrapper on `RawFlagValue`, and a `Flag.asJson()` accessor returning a Jackson `JsonNode` (both JSON objects and JSON arrays decode correctly; `MissingNode.getInstance()` is returned as the safe fallback for non-`json` flags). `FlagManager.createFlagFromDefault()` now types a `Map`/`List`/`JsonNode` default value as `json` instead of coercing it to a string.

### Changed
- Renamed `X-API-Key`, `X-ZENMANAGE-CONTEXT`, and `X-DEFAULT-VALUE` headers to `X-ZEN-API-KEY`, `X-ZEN-CONTEXT`, and `X-ZEN-DEFAULT-VALUE` for consistency with the JavaScript/PHP SDKs
- CI now builds/tests across a Java 11/17/21 matrix instead of only 21, actually verifying the Java 11+ support this SDK advertises
- Maven Central publishing switched from the retired Sonatype OSSRH host/`nexus-staging-maven-plugin` to the Central Publisher Portal via `central-publishing-maven-plugin`

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- Filesystem cache serialization stability for tests
- `FlagManager.single()` now reports the effective default value (inline parameter, falling back to a `DefaultsCollection` entry) on every usage report, including when the flag is found and evaluated normally, not just on fallback paths
- `FlagType.fromWireValue()` no longer throws on an unrecognized wire value (e.g. a future flag type such as `json`). It now resolves to an `UNKNOWN` sentinel that `FlagManager` treats like a missing flag, falling back to the caller-provided default instead of throwing or dropping the entire rules payload. A warning is logged once per rules load when this occurs.
- (ZEN-1755) `Flag.asBool()` now returns `true` for every non-boolean-typed flag regardless of the underlying value (a `json` flag, a `number` flag set to `0`, or a `string` flag set to `""`/`"false"`), matching the documented cross-SDK coercion contract. Previously it fell through to `Boolean.parseBoolean(String.valueOf(value))` (or a numeric zero-check for `number` flags), which returned `false` in these cases.
- (ZEN-1757) `FlagManager.single()` now falls back to the caller-provided default (inline parameter or `DefaultsCollection` entry) when the rules fetch fails outright (e.g. an unreachable API or an invalid/unauthorized environment key), instead of letting the fetch exception propagate out of `single()` even when a default was supplied.
- (ZEN-406) `Flag.asString()`/`asNumber()` now only recognize their own value wrapper and fall back to the safe zero value (`""`/`0`) for every other type, instead of best-effort stringifying/parsing whatever wrapper was present — e.g. `asString()` on a boolean flag now returns `""` instead of `"true"`, matching the documented cross-SDK coercion contract.
- (ZEN-406) `RuleEngine` no longer matches negated operators (`notequal`/`notin`/`notcontains`/`notstartswith`/`notendswith`) or `isnull` when an attribute is entirely absent from the context, matching the reference PHP SDK's behavior exactly.
- (ZEN-406) Fixed a thread-safety bug where `FlagManager` instances created via `withContext()`/`withDefaults()` shared rules state but synchronized on separate instance monitors, giving no real mutual exclusion between them.
- (ZEN-406) Fixed a file-descriptor leak in `FileSystemCache.clear()`.

### Security
- N/A

## [0.1.0] - 2026-05-07

### Added
- Initial public release of `com.zenmanage:zenmanage-java`
