package net.runelite.client.plugins.a1cplankmake;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Config;

@ConfigGroup("a1cplankmake")
public interface A1CPlankMakeConfig extends Config
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
            keyName = "plank",
            name = "Plank option",
            description = "Choose"
    )
    default Types.Plank plank()
    {
        return Types.Plank.Mahogany;
    }
    @ConfigItem(
            position = 20,
            keyName = "bank",
            name = "Banking option",
            description = "Choose"
    )
    default Types.Bank bank()
    {
        return Types.Bank.CWars;
    }
}
