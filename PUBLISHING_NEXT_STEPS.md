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

## 2. Publish to Maven Central (Central Publisher Portal)

Maven Central is the primary distribution channel for Java libraries. Sonatype
retired the legacy OSSRH host (`s01.oss.sonatype.org`) and the
`nexus-staging-maven-plugin` deploy flow — all new and existing publishing now
goes through the **Central Publisher Portal** at central.sonatype.com, using
the `central-publishing-maven-plugin` (already configured in this repo's `pom.xml`
`release` profile).

### One-time setup

1. Create a Central Portal account: https://central.sonatype.com/
2. Verify namespace ownership for `com.zenmanage` (domain-based, via a DNS TXT
   record on zenmanage.com) in the Portal's Namespaces section.
3. Generate a **user token** (Portal account → View Account → Generate User Token)
   — this is a username/password-shaped token pair, not your Portal login
   credentials. It's what the `central` server entry below actually holds.
4. Generate and securely store a GPG keypair for signing artifacts (Central still
   requires every deployed artifact to be GPG-signed):
   ```bash
   gpg --gen-key
   gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
   ```
5. Configure Maven settings in `~/.m2/settings.xml`:
   ```xml
   <settings>
     <servers>
       <server>
         <id>central</id>
         <username>YOUR_USER_TOKEN_USERNAME</username>
         <password>YOUR_USER_TOKEN_PASSWORD</password>
       </server>
     </servers>
   </settings>
   ```
   GPG passphrase can be supplied via `-Dgpg.passphrase=...` or a `GPG_PASSPHRASE`
   env var, as the release workflow already does.

### Project updates

1. `maven-gpg-plugin` for signing — already configured.
2. `central-publishing-maven-plugin` (`org.sonatype.central:central-publishing-maven-plugin`)
   with `<publishingServerId>central</publishingServerId>` and `<autoPublish>true</autoPublish>`
   — already configured in the `release` profile; this plugin bundles and uploads
   directly through the Portal's publishing API, so there's no `distributionManagement`
   `<repository>` for releases (only snapshots still deploy that way).
3. Ensure source and javadoc JARs are attached (already configured).

### Release command

```bash
mvn -P release clean deploy
```

With `autoPublish=true`, this uploads, validates, and releases the deployment
bundle to Maven Central in one step — no separate manual "close and release"
step in the Portal UI. Drop `autoPublish` (or set it to `false`) if you'd
rather review the bundle in the Portal before releasing it manually.

## 3. Publish to GitHub Packages

Use GitHub Packages as a secondary distribution channel.

### Steps

1. The `distributionManagement` pointing to the GitHub Maven registry is already
   configured behind the `github-packages` profile in `pom.xml` — no pom changes needed.
2. Add credentials in `~/.m2/settings.xml`:
   - username: GitHub username
   - password: GitHub PAT with `write:packages`
3. Publish:

```bash
mvn -P github-packages clean deploy
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
   - `CENTRAL_PORTAL_USERNAME` / `CENTRAL_PORTAL_PASSWORD` (Central Portal user token pair)
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
