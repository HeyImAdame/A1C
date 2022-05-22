package net.runelite.client.plugins.a1cbankskills;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.widgets.WidgetInfo;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor
public class Types
{
    public enum Product {
        Custom(-1,-1,-1,-1),


        PRAYER_POTION(139,99,231,1),
        SUPER_ATTACK(145,101,221,1),
        SUPER_STRENGTH(157,105,225,1),
        SUPER_DEFENCE(163,107,239,1),
        SARA_BREW(6687,3002,6693,1),
        SUPER_RESTORE(3026,3004,223,1),
        RANGE_POTION(169,109,245,1),
        MAGIC_POTION(3042,2483,3138,1),
        ENERGY_POTION(3018,103,2970,1),
        STAMINA_4_dose(12625,12640,3016,1),


        MAPLE_LONGBOW_u(62,946,1517,3),
        YEW_LONGBOW_u(66,946,1515,3),
        MAGIC_LONGBOW_u(70,946,1513,3),
        YEW_LONGBOW(855,1777,66,1),
        MAGIC_LONGBOW(859,1777,70,1),
        UNPOWERED_ORB(567,1785,1775,6),
        LANTERN_LENS(4542,1785,1775,7),
        EMPTY_LIGHT_ORB(10980,1785,1775,8),
        HUMIDIFY(1937,9075,1935,-1),
        JUG_OF_WINE(1995,1937,1987,1);

        public final int productid;
        public final int ingredientid1;
        public final int ingredientid2;
        public final int craftOpt;
        Product(int productid, int ingredientid1, int ingredientid2, int craftOpt)
        {
            this.productid = productid;
            this.ingredientid1 = ingredientid1;
            this.ingredientid2 = ingredientid2;
            this.craftOpt = craftOpt;
        }
    }
    public enum spellType
    {
        SPELL_HUMIDIFY;
    }
    public enum Banks
    {
    NPC,
    BOOTH,
    CHEST,
    }

    public enum Skill
    {
    Use14on14,
    Use1on27,
    CastSpell
    }
}
