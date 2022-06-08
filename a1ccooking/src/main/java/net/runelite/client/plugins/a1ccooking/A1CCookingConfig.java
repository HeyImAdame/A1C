package net.runelite.client.plugins.a1ccooking;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("a1ccooking")
public interface A1CCookingConfig extends Config {
    @ConfigItem(position = 0, keyName = "fish", name = "Cook option", description = "Choose")
    default Types.Fish fish()
    {
        return Types.Fish.Shark;
    }

    @ConfigItem(position = 11, keyName = "customfish", name = "Custom item ID", description = "Choose", hidden = true, unhide = "fish", unhideValue = "Custom")
    default int customfish()
    {
        return 6969;
    }

    @ConfigItem(position = 20, keyName = "consumeMisclicks", name = "Stop Misclicks", description = "Allows you to spam left click")
    default boolean consumeMisclicks()
    {
        return true;
    }

    @ConfigItem(position = 30, keyName = "cooklocation", name = "Cooking Location", description = "Choose")
    default Types.cookArea cookarea()
    {
        return Types.cookArea.Hosidius;
    }

    @ConfigItem(position = 31, keyName = "objid", name = "Object ID", description = "Choose", hidden = true, unhide = "cooklocation", unhideValue = "Custom")
    default int objid()
    {
        return 6969;
    }
}
