package net.runelite.client.plugins.a1cbankskills;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigSection;

import static net.runelite.api.widgets.WidgetInfo.SPELL_HUMIDIFY;

@ConfigGroup("a1cbankskills")
public interface A1CBankSkillsConfig extends Config
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
    default Types.Skill skill() { return Types.Skill.Use14on14; }
    @ConfigItem(
        position = 11,
        keyName = "customskill",
        name = "Custom option",
        description = "Choose",
        hidden = true,
        unhide = "skill",
        unhideValue = "Custom",
        section = craftsection
)
default Types.Skill customskill() { return Types.Skill.Use14on14; }

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
            position = 28,
            keyName = "14on14product",
            name = "Craft",
            description = "The item to craft",
            hidden = true,
            unhide = "skill",
            unhideValue = "Use14on14",
            section = craftsection
    )
    default Types.Productuse14on14 product14on14() { return Types.Productuse14on14.SUPER_ATTACK; }
    @ConfigItem(
            position = 29,
            keyName = "1on27product",
            name = "Craft",
            description = "The item to craft",
            hidden = true,
            unhide = "skill",
            unhideValue = "Use1on27",
            section = craftsection
    )
    default Types.Productuse1on27 product1on27()    {
        return Types.Productuse1on27.EMPTY_LIGHT_ORB;
    }
    @ConfigItem(
            position = 30,
            keyName = "productcastspell",
            name = "Craft",
            description = "The item to craft",
            hidden = true,
            unhide = "skill",
            unhideValue = "CastSpell",
            section = craftsection
    )
 default Types.Productcastspell productcastspell()    {return Types.Productcastspell.SUPERGLASSMAKE;}
    @ConfigItem(
            position = 31,
            keyName = "productID",
            name = "Product (ID)",
            description = "ID of crafted item",
            hidden = true,
            unhide = "skill",
            unhideValue = "Custom",
            section = craftsection
    )
    default int customproductID()
    {
        return 2970;
    }

    @ConfigItem(
            position = 32,
            keyName = "ingredient1",
            name = "Ingredient 1 ID",
            description = "ID of ingredient 1",
            hidden = true,
            unhide = "skill",
            unhideValue = "Custom",
            section = craftsection
    )
    default int customingredientID1()
    {
        return 2970;
    }

    @ConfigItem(
            position = 33,
            keyName = "ingredient2",
            name = "Ingredient 2 ID",
            description = "ID of ingredient 2",
            hidden = true,
            unhide = "skill",
            unhideValue = "Custom",
            section = craftsection
    )
    default int customingredientID2()
    {
        return 103;
    }

    @ConfigItem(
            position = 50,
            keyName = "craftnum",
            name = "Menu Craft Number",
            description = "Enter craft item number",
            hidden = true,
            unhide = "skill",
            unhideValue = "Custom",
            section = craftsection
    )
    default int craftNum()
    {
        return 1;
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
            keyName = "bankname",
            name = "Bank Type",
            description = "Choose",
            section = banksettings
    )
    default Types.Banks bank()
    {
        return Types.Banks.CWars;
    }

    @ConfigItem(
            position = 61,
            keyName = "bankID",
            name = "Bank ID",
            description = "Choose",
            hidden = true,
            unhide = "bankname",
            unhideValue = "Custom",
            section = banksettings
    )
    default int bankid()
    {
        return 4483;
    }

    @ConfigItem(
            position = 62,
            keyName = "bankType",
            name = "Bank Type",
            description = "Choose",
            hidden = true,
            unhide = "bankname",
            unhideValue = "Custom",
            section = banksettings
    )
    default Types.BankType banktype() { return Types.BankType.CHEST; }
/*    @ConfigItem(
            position = 70,
            keyName = "bankID",
            name = "Bank ID",
            description = "Input bank ID, supports chests/NPCs/Booths",
            section = banksettings
    )
    default int bankID()
    {
        return 30796;
    }*/
}
