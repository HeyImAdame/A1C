package net.runelite.client.plugins.a1cconstruction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor
public class Types
{
    public enum Build
    {
        MAHOGANY_TABLE(ItemID.MAHOGANY_PLANK, ObjectID.MAHOGANY_TABLE, -1, ObjectID.TABLE_SPACE, -1),
        MAHOGANY_BENCH(ItemID.MAHOGANY_PLANK, ObjectID.MAHOGANY_BENCH_26215, ObjectID.MAHOGANY_BENCH_26241, ObjectID.SEATING_SPACE_29139, ObjectID.SEATING_SPACE_29136),
        TEAK_BENCH(ItemID.TEAK_PLANK, ObjectID.TEAK_BENCH_29270, ObjectID.TEAK_BENCH_29271, ObjectID.SEATING_SPACE_29139, ObjectID.SEATING_SPACE_29136);

        public final int plankID;
        public final int builtID;
        public final int builtID2;
        public final int unbuiltID;
        public final int unbuiltID2;

        Build (int plankID, int builtID, int builtID2, int unbuiltID, int unbuiltID2)
        {
            this.plankID = plankID;
            this.builtID = builtID;
            this.builtID2 = builtID2;
            this.unbuiltID = unbuiltID;
            this.unbuiltID2 = unbuiltID2;
        }
    }
}
