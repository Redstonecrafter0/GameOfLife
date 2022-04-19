# Module GameOfLife-OpenGL
This is the OpenGL abstraction part of this project.
It is also intended for use in other projects such as [Amber](https://github.com/Redstonecrafter0/Amber).
It's released on GitHub Packages and so requires authentication to use as a maven dependency.

### pom.xml
```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Redstonecrafter0/GameOfLife</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>net.redstonecraft.gameoflife</groupId>
        <artifactId>opengl</artifactId>
        <version>${opengl.version}</version>
    </dependency>
</dependencies>
```

### ~/.m2/settings.xml
See [GitHub's docs](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-with-a-personal-access-token).
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
    <servers>
        <server>
            <id>github</id>
            <username>yourUsername</username>
            <password>yourGithubToken</password>
        </server>
    </servers>
</settings>
```
