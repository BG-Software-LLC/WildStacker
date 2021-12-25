<p align="center">
<img src="https://bg-software.com/imgs/wildstacker-logo.png" />
<h2 align="center">The first ever multi-threaded stacking solution!</h2>
</p>
<br>
<p align="center">
<a href="https://bg-software.com/discord/"><img src="https://img.shields.io/discord/293212540723396608?color=7289DA&label=Discord&logo=discord&logoColor=7289DA&link=https://bg-software.com/discord/"></a>
<a href="https://bg-software.com/patreon/"><img src="https://img.shields.io/badge/-Support_on_Patreon-F96854.svg?logo=patreon&style=flat&logoColor=white&link=https://bg-software.com/patreon/"></a><br>
<a href=""><img src="https://img.shields.io/maintenance/yes/2020"></a>
</p>

## Compiling

You can compile the project using gradlew.<br>
Run `gradlew build` in console to build the project.<br>
You can find already compiled jars on our [Jenkins](https://hub.bg-software.com/) hub!<br>

When compiling you will receive errors about missing dependencies.<br>
These dependencies are premium plugins that cannot be published on a public repository.<br>
You can do either of the followings in order to solve it:
- Add manually all the jar files of the premium plugins.
- Purchase access to our private repository.
- Disabling compiling of the modules of these dependencies in the `gradle.properties` file.

<br>

##### Private Jars:
- Boss (latest & v3.9.7) by kangarko [[link]](https://www.mc-market.org/resources/21619/)
- Citizens by fullwall [[link]](https://www.spigotmc.org/resources/13811/)
- CustomBosses by AMinecraftDev
- EpicBosses by Songoda [[link]](https://www.spigotmc.org/resources/34159/)
- EpicSpawners (v5 & v6) by Songoda [[link]](https://songoda.com/marketplace/product/13)
- FabledSkyblock by Songoda [[link]](https://songoda.com/marketplace/product/17)
- mcMMO (v1 & v2) by nossr50 [[link]](https://www.spigotmc.org/resources/64348/)
- PlotSquared (legacy, v4 & v5) by IntellectualSites [[link]](https://www.spigotmc.org/resources/77506/)
- ShopGUIPlus (v1.18 & v1.20) by brcdev [[link]](https://www.spigotmc.org/resources/6515/)

## API

The plugin is packed with a rich API for interacting with entities, items and more. When hooking into the plugin, it's highly recommended to only use the API and not the compiled plugin, as the API methods are not only commented, but also will not get removed or changed unless they are marked as deprecated. This means that by using the API, you won't have to do any additional changes to your code between updates.

##### Maven
```
<repositories>
    <repository>
        <id>bg-repo</id>
        <url>https://repo.bg-software.com/repository/api/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.bgsoftware</groupId>
        <artifactId>WildStackerAPI</artifactId>
        <version>latest</version>
    </dependency>
</dependencies>
```
##### Gradle
```
repositories {
    maven { url 'https://repo.bg-software.com/repository/api/' }
}

dependencies {
    compileOnly 'com.bgsoftware:WildStackerAPI:latest'
}
```
## Updates

This plugin is provided "as is", which means no updates or new features are guaranteed. We will do our best to keep 
updating and pushing new updates, and you are more than welcome to contribute your time as well and make pull requests
for bug fixes. 

## License

This plugin is licensed under GNU GPL v3.0
