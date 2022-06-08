package net.runelite.client.plugins.a1cconstruction;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Config;

@ConfigGroup("a1cplankmake")
public interface A1CConstructionConfig extends Config
{//
    @ConfigItem(
            position = 0,
            keyName = "stopmisclicks",
            name = "Stop Misclicks",
            description = "Stops misclicking"
    )
    default boolean consumeMisclicks()
    {
        return true;
    }
    @ConfigItem(
            position = 10,
            keyName = "Build",
            name = "Build option",
            description = "Choose"
    )
    default Types.Build build()
    {
        return Types.Build.MAHOGANY_TABLE;
    }
}
