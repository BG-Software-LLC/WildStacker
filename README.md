# WildStacker

WildStacker 3 - The first ever multi-threaded stacking solution!

## Compiling

You can compile the project using gradlew.<br>
Run `gradlew shadowJar build` in console to build the project.<br>
You can find already compiled jars on our [Jenkins](https://hub.bg-software.com/) hub!<br>
You must add yourself all the private jars or purchase access to our private repository.

##### Private Jars:
- Boss (latest & v3.9.7) by kangarko [[link]](https://www.spigotmc.org/resources/46497/)
- Citizens by fullwall [[link]](https://www.spigotmc.org/resources/13811/)
- CustomBosses by AMinecraftDev
- EpicBosses by Songoda [[link]](https://www.spigotmc.org/resources/34159/)
- EpicSpawners (v5 & v6) by Songoda [[link]](https://songoda.com/marketplace/product/13)
- FabledSkyblock by Songoda [[link]](https://songoda.com/marketplace/product/17)
- mcMMO (v1 & v2) by nossr50 [[link]](https://www.spigotmc.org/resources/64348/)
- PlotSquared (legacy, v4 & v5) by IntellectualSites [[link]](https://www.spigotmc.org/resources/77506/)
- ShopGUIPlus (v1.18 & v1.20) by brcdev [[link]](https://www.spigotmc.org/resources/6515/)

## API

You can hook into the plugin by using the built-in API module.<br>
The API module is safe to be used, its methods will not be renamed or changed, and will not have methods removed 
without any further warning.<br>
You can add the API as a dependency using Maven or Gradle:<br>

#### Maven
```
<repository>
    <id>bg-repo</id>
    <url>https://repo.bg-software.com/repository/api/</url>
</repository>

<dependency>
    <groupId>com.bgsoftware</groupId>
    <artifactId>WildStackerAPI</artifactId>
    <version>latest</version>
</dependency>
```

#### Gradle
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
