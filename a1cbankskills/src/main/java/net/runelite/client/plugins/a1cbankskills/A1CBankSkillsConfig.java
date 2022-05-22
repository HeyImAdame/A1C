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
            position = 29,
            keyName = "spellType",
            name = "Spell Type",
            description = "Select spell",
            hidden = true,
            unhide = "skill",
            unhideValue = "Castspell",
            section = craftsection
    )
    default WidgetInfo spelltype()
    {
        return SPELL_HUMIDIFY;
    }

    @ConfigItem(
            position = 30,
            keyName = "product",
            name = "Craft",
            description = "The item to craft",
            section = craftsection
    )
 default Types.Product product()    {
        return Types.Product.SUPER_ATTACK;
    }

    @ConfigItem(
            position = 31,
            keyName = "productID",
            name = "Product (ID)",
            description = "ID of crafted item",
            hidden = true,
            unhide = "product",
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
            unhide = "product",
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
            unhide = "product",
            unhideValue = "Custom",
            section = craftsection
    )
    default int customingredientID2()
    {
        return 103;
    }

    @ConfigItem(
            position = 50,
            keyName = "craftnum14on14",
            name = "Menu Craft Number",
            description = "Enter craft item number",
            hidden = true,
            unhide = "product",
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
