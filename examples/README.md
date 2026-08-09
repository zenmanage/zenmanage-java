# Zenmanage Java SDK Examples

This folder contains self-contained examples mirroring the PHP and JavaScript SDK example set.

## Examples

- `simple_flags.java`: Basic flag retrieval and typed access
- `context_based_flags.java`: Context-aware targeting with attributes
- `defaults.java`: Inline and collection defaults
- `caching.java`: Memory, filesystem, and null cache options
- `ab_testing.java`: Experiment variants and segmentation
- `percentage_rollouts.java`: Deterministic rollout behavior

## Run Locally

1. Export your environment token:

```bash
export ZENMANAGE_ENVIRONMENT_TOKEN="srv_your_server_key_here"
```

2. Build and test the SDK:

```bash
mvn clean verify
```

3. Copy example snippets into your application and adapt flag keys to your dashboard setup.
