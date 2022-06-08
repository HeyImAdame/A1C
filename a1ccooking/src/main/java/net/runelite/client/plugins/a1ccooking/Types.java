package net.runelite.client.plugins.a1ccooking;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.widgets.WidgetInfo;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor
public class Types
{
    public enum Fish {
        Custom(-1, -1),


        Shark(-1,-1);

        public int cookedID;
        public int uncookedID;

        Fish(int cookedID, int uncookedID) {
            this.cookedID = cookedID;
            this.uncookedID = uncookedID;
        }
    }

    public enum cookArea
    {
        Custom(-1,-1),


        Hosidius(-1,-1);
        public final int cookID;
        public final int bankID;
        cookArea(int cookID, int bankID)
        {
            this.cookID = cookID; this.bankID = bankID;
        }
    }
}
