package net.runelite.client.plugins.aonecbankskills;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor
public class Types
{
    public enum Product {
        Custom(-1,-1,-1),
        PRAYER_POTION(139,99,231),
        SUPER_ATTACK(145,101,221),
        SUPER_STRENGTH(157,105,225),
        SUPER_DEFENCE(163,107,239),
        SARA_BREW(6687,3002,6693),
        SUPER_RESTORE(3026,3004,223),
        RANGE_POTION(169,109,245),
        MAGIC_POTION(3042,2483,3138),
        ENERGY_POTION(3018,103,2970),
        STAMINA_4(12625,12640,3016),
        MAPLE_LONGBOW_u(62,946,1517),
        YEW_LONGBOW_u(66,946,1515),
        MAGIC_LONGBOW_u(70,946,1513),
        YEW_LONGBOW(855,1777,66),
        MAGIC_LONGBOW(859,1777,70),
        UNPOWERED_ORB(567,1785,1775),
        LANTERN_LENS(4542,1785,1775),
        EMPTY_LIGHT_ORB(10980,1785,1775),
        HUMIDIFY(1937,9075,1935),
        JUG_OF_WINE(1993,1937,1987),
        ;

        public final int id;
        public final int ingredientid1;
        public final int ingredientid2;
        Product(int id, int ingredientid1, int ingredientid2)
        {
            this.id = id;
            this.ingredientid1 = ingredientid1;
            this.ingredientid2 = ingredientid2;
        }
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
Humidify
}
}
