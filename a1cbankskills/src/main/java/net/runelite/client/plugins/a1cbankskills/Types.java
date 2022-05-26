package net.runelite.client.plugins.a1cbankskills;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.widgets.WidgetInfo;
import org.checkerframework.checker.nullness.Opt;

import java.util.Optional;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor
public class Types
{
    public enum Productuse14on14{
        Custom(-1, -1, -1, -1, null),


        PRAYER_POTION(139, 99, 231, 1, null),
        SUPER_ATTACK(145, 101, 221, 1, null),
        SUPER_STRENGTH(157, 105, 225, 1, null),
        SUPER_DEFENCE(163, 107, 239, 1, null),
        SARA_BREW(6687, 3002, 6693, 1, null),
        SUPER_RESTORE(3026, 3004, 223, 1, null),
        RANGE_POTION(169, 109, 245, 1, null),
        MAGIC_POTION(3042, 2483, 3138, 1, null),
        ENERGY_POTION(3018, 103, 2970, 1, null),
        STAMINA_4_dose(12625, 12640, 3016, 1, null),


        YEW_LONGBOW(855, 1777, 66, 1, null),
        MAGIC_LONGBOW(859, 1777, 70, 1, null),


        JUG_OF_WINE(1995, 1937, 1987, 1, null);

        public int productid;
        public int ingredientid1;
        public int ingredientid2;
        public int craftOpt;
        public WidgetInfo spellname;

        Productuse14on14(int productid, int ingredientid1, int ingredientid2, int craftOpt, WidgetInfo spellname) {
            this.productid = productid;
            this.ingredientid1 = ingredientid1;
            this.ingredientid2 = ingredientid2;
            this.craftOpt = craftOpt;
            this.spellname = spellname;
        }
    }

    public enum Productuse1on27 {
        Custom(-1, -1, -1, -1, null),


        MAPLE_LONGBOW_u(62, 946, 1517, 3, null),
        YEW_LONGBOW_u(66, 946, 1515, 3, null),
        MAGIC_LONGBOW_u(70, 946, 1513, 3, null),


        UNPOWERED_ORB(567, 1785, 1775, 6, null),
        LANTERN_LENS(4542, 1785, 1775, 7, null),
        EMPTY_LIGHT_ORB(10980, 1785, 1775, 8, null);

        public int productid;
        public int ingredientid1;
        public int ingredientid2;
        public int craftOpt;
        public WidgetInfo spellname;

        Productuse1on27(int productid, int ingredientid1, int ingredientid2, int craftOpt, WidgetInfo spellname) {
            this.productid = productid;
            this.ingredientid1 = ingredientid1;
            this.ingredientid2 = ingredientid2;
            this.craftOpt = craftOpt;
            this.spellname = spellname;
        }
    }

    public enum Productcastspell {
        HUMIDIFY(1937, -1, 1935, -1, WidgetInfo.SPELL_HUMIDIFY),
        SUPERGLASSMAKE(1775, 21504, 1783, -1, WidgetInfo.SPELL_SUPERGLASS_MAKE);

        public int productid;
        public int ingredientid1;
        public int ingredientid2;
        public int craftOpt;
        public WidgetInfo spellname;

        Productcastspell(int productid, int ingredientid1, int ingredientid2, int craftOpt, WidgetInfo spellname) {
            this.productid = productid;
            this.ingredientid1 = ingredientid1;
            this.ingredientid2 = ingredientid2;
            this.craftOpt = craftOpt;
            this.spellname = spellname;
        }
    }

    public enum Banks
    {
        Custom(-1,"Chest"),


        Seers(25808, "Booth"),
        Edgeville(10355,"Booth"),
        CWars(4483, "Chest");
        public final int ID;
        public final String Type;
        Banks(int ID, String Type)
        {
            this.ID = ID; this.Type = Type;
        }
    }

    public enum BankType
    {
        NPC("NPC"),
        BOOTH("Booth"),
        CHEST("Chest");
        public final String Type;
        BankType(String Type)
        {
            this.Type = Type;
        }
    }

    public enum Skill
    {
        Custom,
        Use14on14,
        Use1on27,
        CastSpell
    }
}
