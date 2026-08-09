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

### Changed
- Renamed `X-API-Key`, `X-ZENMANAGE-CONTEXT`, and `X-DEFAULT-VALUE` headers to `X-ZEN-API-KEY`, `X-ZEN-CONTEXT`, and `X-ZEN-DEFAULT-VALUE` for consistency with the JavaScript/PHP SDKs

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- Filesystem cache serialization stability for tests

### Security
- N/A

## [0.1.0] - 2026-05-07

### Added
- Initial public release of `com.zenmanage:zenmanage-java`
