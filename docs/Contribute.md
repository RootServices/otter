# Contribute! 

## Ways to Contribute
 - update documentation
 - write features
 - fix bugs
 - suggest new features
 
## Contributing Code
 - Code should be written with SOLID Principles.
 - Please write unit and integration tests.

## Say Hello
 - otter@tokensmith.net

## How to release.
```bash
$ ./gradlew :otter:publish
$ ./gradlew :otter-translatable:publish
```

- Goto [Maven Central staging repositories](https://oss.sonatype.org/#stagingRepositories)
- Search for `Otter` or `tokensmith`.
- Select the repository just uploaded.
- Click `close` it will then prompt to make sure you want to do it.
- Click `release` when its done.
