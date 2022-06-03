package net.runelite.client.plugins.a1cblastfurnace;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("a1cblastfurnace")
public interface A1CBlastFurnaceConfig extends Config {

    @ConfigItem(
            keyName = "bartype",
            name = "Bar Type",
            description = "Choose which bar to smelt",
            position = 0
    )
    default A1CBlastFurnaceTypes barType() {
        return A1CBlastFurnaceTypes.STEEL;
    }

    @ConfigItem(
            keyName = "upperrun",
            name = "Below run %",
            description = "Run % to drink stamina below",
            position = 10
    )
    default int drinkbelow() {
        return 60;
    }

}