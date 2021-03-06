package net.runelite.client.plugins.a1cplankmake;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor
public class Types
{

    public enum Bank
    {

        CWars(4483, "Chest");
//        Seers(25808, "Booth"),
//        Crafting_Guild(14886, "Chest"),
//        Camelot_pvp(10777, "Chest");

        public final int ID;
        public final String Type;
        Bank(int ID, String Type)
        {
            this.ID = ID; this.Type = Type;
        }
    }
    public enum Plank
    {
        Teak(6333, 8780),
        Mahogany(6332, 8782);

        public final int ID1;
        public final int ID2;
        Plank(int ID1, int ID2)
        {
            this.ID1 = ID1;
            this.ID2 = ID2;
        }
    }
}
