# Publishing Next Steps for zenmanage-java

This guide outlines practical next steps to publish the Java SDK to major public package repositories.

## 1. Repository Readiness Checklist

Before publishing anywhere:

1. Ensure legal/project metadata is complete in `pom.xml`:
   - `groupId`, `artifactId`, versioning
   - license
   - SCM URLs
   - developer/contact information
2. Ensure all CI checks are green:
   - `mvn clean verify`
   - code coverage gates
3. Tag a release candidate in Git:
   - `git tag v0.1.0`
   - `git push origin v0.1.0`
4. Prepare release notes/changelog.

## 2. Publish to Maven Central (Sonatype)

Maven Central is the primary distribution channel for Java libraries.

### One-time setup

1. Create Sonatype Central account: https://central.sonatype.com/
2. Verify namespace ownership for your chosen group:
   - domain-based (recommended): e.g. `com.zenmanage`
   - or GitHub namespace if required by Sonatype
3. Generate and securely store GPG keypair for signing artifacts.
4. Configure Maven settings in `~/.m2/settings.xml`:
   - Sonatype credentials
   - GPG passphrase (or use env vars)

### Project updates

1. Add `maven-gpg-plugin` for signing.
2. Add `nexus-staging-maven-plugin` (or Sonatype Central-compatible publishing plugin) if needed.
3. Ensure source and javadoc JARs are attached (already configured).

### Release command

```bash
mvn -P release clean deploy
```

Use a dedicated `release` profile for signing and deploying.

## 3. Publish to GitHub Packages

Use GitHub Packages as a secondary distribution channel.

### Steps

1. Add `distributionManagement` in `pom.xml` pointing to GitHub Maven registry:

```xml
<distributionManagement>
  <repository>
    <id>github</id>
    <name>GitHub Packages</name>
    <url>https://maven.pkg.github.com/zenmanage/zenmanage-java</url>
  </repository>
</distributionManagement>
```

2. Add credentials in `~/.m2/settings.xml`:
   - username: GitHub username
   - password: GitHub PAT with `write:packages`
3. Publish:

```bash
mvn clean deploy
```

## 4. Publish via JitPack

JitPack builds directly from Git tags.

### Steps

1. Ensure repository is public and build is reproducible with `mvn -q test`.
2. Create and push a version tag:

```bash
git tag v0.1.0
git push origin v0.1.0
```

3. Visit `https://jitpack.io/#zenmanage/zenmanage-java/v0.1.0` to trigger and verify the build.
4. Add JitPack badge and dependency snippet to README.

## 5. CI/CD Automation Recommendations

Automate release quality and deployment with GitHub Actions:

1. `ci.yml`: build/test/lint/coverage on every PR.
2. `release.yml`: publish on semver tag pushes.
3. Store secrets in repository/org secrets:
   - `SONATYPE_USERNAME`
   - `SONATYPE_PASSWORD`
   - `GPG_PRIVATE_KEY`
   - `GPG_PASSPHRASE`
   - `GITHUB_TOKEN` or PAT for package publishing

## 6. Post-Publish Validation

After each release:

1. Validate Maven Central metadata and Javadocs are visible.
2. Verify install in a clean sample app.
3. Confirm checksums/signatures and dependency resolution.
4. Announce release with migration notes and compatibility details.

## 7. Versioning Policy

Recommended approach:

1. Follow SemVer strictly.
2. Maintain `CHANGELOG.md` with Keep a Changelog format.
3. For breaking changes, provide upgrade guidance in `UPGRADING.md`.
