import ProjectVersions.rlVersion


version = "1337.10.20"

project.extra["PluginName"] = "A1C Bloods Morytania" // This is the name that is used in the external plugin manager panel
project.extra["PluginDescription"] = "Active One Click Bloods Runecrafting at the new altar" // This is the description that is used in the external plugin manager panel

dependencies {
    annotationProcessor(Libraries.lombok)
    annotationProcessor(Libraries.pf4j)

    compileOnly("com.openosrs:runelite-api:$rlVersion+")
    compileOnly("com.openosrs:runelite-client:$rlVersion+")

    compileOnly(Libraries.guice)
    //compileOnly(Libraries.javax)
    compileOnly(Libraries.lombok)
    compileOnly(Libraries.pf4j)
}

tasks {
    jar {
        manifest {
            attributes(mapOf(
                    "Plugin-Version" to project.version,
                    "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                    "Plugin-Provider" to project.extra["PluginProvider"],
                    "Plugin-Description" to project.extra["PluginDescription"],
                    "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}