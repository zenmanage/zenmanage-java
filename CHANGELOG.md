# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog and this project follows Semantic Versioning.

## [Unreleased]

### Added
- Java SDK initial package scaffold
- Config builder, API client, rule engine, rollouts, and cache backends
- Example scripts matching PHP/JavaScript SDK example set
- Test suite and JaCoCo reporting
- CI and release workflows for public publishing
- `json` flag type support: `FlagType.JSON`, a `json` value wrapper on `RawFlagValue`, and a `Flag.asJson()` accessor returning a Jackson `JsonNode` (both JSON objects and JSON arrays decode correctly; `MissingNode.getInstance()` is returned as the safe fallback for non-`json` flags). `FlagManager.createFlagFromDefault()` now types a `Map`/`List`/`JsonNode` default value as `json` instead of coercing it to a string.

### Changed
- Renamed `X-API-Key`, `X-ZENMANAGE-CONTEXT`, and `X-DEFAULT-VALUE` headers to `X-ZEN-API-KEY`, `X-ZEN-CONTEXT`, and `X-ZEN-DEFAULT-VALUE` for consistency with the JavaScript/PHP SDKs

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- Filesystem cache serialization stability for tests
- `FlagManager.single()` now reports the effective default value (inline parameter, falling back to a `DefaultsCollection` entry) on every usage report, including when the flag is found and evaluated normally, not just on fallback paths
- `FlagType.fromWireValue()` no longer throws on an unrecognized wire value (e.g. a future flag type such as `json`). It now resolves to an `UNKNOWN` sentinel that `FlagManager` treats like a missing flag, falling back to the caller-provided default instead of throwing or dropping the entire rules payload. A warning is logged once per rules load when this occurs.

### Security
- N/A

## [0.1.0] - 2026-05-07

### Added
- Initial public release of `com.zenmanage:zenmanage-java`
