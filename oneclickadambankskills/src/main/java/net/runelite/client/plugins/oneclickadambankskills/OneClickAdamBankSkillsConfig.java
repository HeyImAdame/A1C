package net.runelite.client.plugins.oneclickadambankskills;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("oneclickadambankskills")
public interface OneClickAdamBankSkillsConfig extends Config
{

    @ConfigSection(
            name = "Crafting Settings",
            description = "Configure settings for crafting",
            position = 0,
            keyName = "craftsection"
    )
    String craftsection = "Crafting Details";
    @ConfigItem(
            position = 10,
            keyName = "skill",
            name = "Skilling option",
            description = "Choose",
            section = craftsection
    )
    default Types.Skill skill()
    {
        return Types.Skill.Use14on14;
    }

    @ConfigItem(
            position = 20,
            keyName = "consumeMisclicks",
            name = "Stop Misclicks",
            description = "Allows you to spam left click",
            section = craftsection
    )
    default boolean consumeMisclicks()
    {
        return true;
    }

    @ConfigItem(
            position = 30,
            keyName = "first14on14",
            name = "Use this 14 (ID)",
            description = "The left click use item ID",
            hidden = true,
            unhide = "skill",
            unhideValue = "Use14on14",
            section = craftsection
    )
    default int use14on14id1()
    {
        return 2970;
    }

    @ConfigItem(
            position = 40,
            keyName = "second14on14",
            name = "On this 14 (ID)",
            description = "The item ID that is used on",
            hidden = true,
            unhide = "skill",
            unhideValue = "Use14on14",
            section = craftsection
    )
    default int use14on14id2()
    {
        return 103;
    }

    @ConfigItem(
            position = 30,
            keyName = "first1on27",
            name = "Use this 1  (ID)",
            description = "The left click use item ID",
            hidden = true,
            unhide = "skill",
            unhideValue = "Use1on27",
            section = craftsection
    )
    default int use1on27id1()
    {
        return 2970;
    }

    @ConfigItem(
            position = 40,
            keyName = "second1on27",
            name = "On this 27  (ID)",
            description = "The item ID that is used on",
            hidden = true,
            unhide = "skill",
            unhideValue = "Use1on27",
            section = craftsection
    )
    default int use1on27id2()
    {
        return 103;
    }

    @ConfigItem(
            position = 45,
            keyName = "humidify1",
            name = "Single rune/rune pouch",
            description = "ID of rune or rune pouch",
            hidden = true,
            unhide = "skill",
            unhideValue = "Humidify",
            section = craftsection
    )
    default int humidifyid1()
    {
        return 9075;
    }

    @ConfigItem(
            position = 46,
            keyName = "humidify2",
            name = "Vessel to fill with water",
            description = "ID for vessel",
            hidden = true,
            unhide = "skill",
            unhideValue = "Humidify",
            section = craftsection
    )
    default int humidifyid2()
    {
        return 1935;
    }

    @ConfigItem(
            position = 50,
            keyName = "craftmenunum14on14",
            name = "Menu Craft Number",
            description = "Enter craft item number",
            hidden = true,
            unhide = "skill",
            unhideValue = "Use14on14",
            section = craftsection
    )
    default int craftNum14on14()
    {
        return 1;
    }

    @ConfigItem(
            position = 50,
            keyName = "craftmenunum1on27",
            name = "Menu Craft Number",
            description = "Enter craft item number",
            hidden = true,
            unhide = "skill",
            unhideValue = "Use1on27",
            section = craftsection
    )
    default int craftNum1on27()
    {
        return 8;
    }

    @ConfigSection(
            name = "Bank Settings",
            description = "Configure settings for banking",
            position = 55,
            keyName = "banksettings"
    )
    String banksettings = "Bank Details";

    @ConfigItem(
            position = 60,
            keyName = "bankType",
            name = "Bank Type",
            description = "Choose",
            section = banksettings
    )
    default Types.Banks bankType()
    {
        return Types.Banks.CHEST;
    }

    @ConfigItem(
            position = 70,
            keyName = "bankID",
            name = "Bank ID",
            description = "Input bank ID, supports chests/NPCs/Booths",
            section = banksettings
    )
    default int bankID()
    {
        return 30796;
    }
}
