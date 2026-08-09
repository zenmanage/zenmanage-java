# Upgrading Guide

This document tracks upgrade steps between released versions of the Zenmanage Java SDK.

## Upgrading to 0.1.0

Initial release. No migration required.

## Future upgrade template

When creating a new release with breaking changes, use this checklist:

1. Identify API changes
- List renamed classes/methods.
- List removed classes/methods.
- List behavior changes in flag evaluation/cache/rules.

2. Provide before/after examples
- Show old code snippet.
- Show replacement code snippet.

3. Highlight operational changes
- Required JDK version changes.
- Required config/env var changes.
- Caching/reporting behavior differences.

4. Add rollback guidance
- Pin previous stable version in Maven dependency.
- Reference known incompatibilities.
