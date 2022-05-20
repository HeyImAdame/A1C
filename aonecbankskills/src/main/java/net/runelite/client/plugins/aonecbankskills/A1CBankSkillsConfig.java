package net.runelite.client.plugins.aonecbankskills;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("aonecbankskills")
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
