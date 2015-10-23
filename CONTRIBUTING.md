# Contributing to Sparkplug

## Rules

1. **PR everything**. Commits made directly to master are prohibited, except under specific circumstances
1. **Use feature branches**. Create an issue for every single feature or bug and tag it. If you are a core contributor, create a branch named feature/[issue #] to resolve the issue. If you are not, fork and branch.
1. Use github "Fixes #[issue]" syntax on your PRs to indicate which issues you are attempting to resolve
1. **Keep sparkplug-core small**. If some piece of functionality *can* fit in a separate sparkplug-* module, then it probably *should*
1. Code coverage is strictly enforced at 100%, so don't worry if your build fails prior to PRing
1. Please try to follow the Scala coding style exemplified by existing source files

## Developing and Testing

Since testing Sparkplug requires a Spark cluster, a containerized development/test environment is included via [Docker](https://www.docker.com/). If you have docker installed, you can build and test Sparkplug within that environment:

```bash
$ docker build -t uncharted/sparkplug-test .
$ docker run --rm uncharted/sparkplug-test
```

The above commands trigger a one-off build and test of Sparkplug. If you want to interactively test Sparkplug while developing (without having to re-run the container), use the following commands:

```bash
$ docker build -t uncharted/sparkplug-test .
$ docker run -v $(pwd):/opt/sparkplug -it uncharted/sparkplug-test bash
# then, inside the running container
$ ./gradlew
```

This will mount the code directory into the container as a volume, allowing you to make code changes on your host machine and test them on-the-fly.

## Deploying to Sonatype Central Repository

Staging and deployment to the Sonatype Central Repository is restricted to core contributors.

You will need the Sparkplug signing key in your GPG keyring. Then, create a gradle.properties file in the root project directory (`.gitignored` for security reasons), which contains the following contents:

```ini
signing.keyId=[Sparkplug signing key ID]
signing.password=[Sparkplug signing key password]
signing.secretKeyRingFile=[/path/to/your/.gnupg/secring.gpg]
```

Finally, building a Nexus-compatible bundle JAR for [manual staging/deployment](http://central.sonatype.org/pages/manual-staging-bundle-creation-and-deployment.html) can be achieved as follows:

```bash
$ ./gradlew nexus
```
