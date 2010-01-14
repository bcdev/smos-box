package org.esa.beam.dataio.smos;

public class SmosConstants {

    private SmosConstants() {
    }

    /**
     * The name of any SMOS grid point data sequence is mapped to this name.
     */
    public static final String GRID_POINT_LIST_NAME = "Grid_Point_List";

    public static final String GRID_POINT_COUNTER_NAME = "Grid_Point_Counter";
    public static final String GRID_POINT_ID_NAME = "Grid_Point_ID";
    public static final String GRID_POINT_LAT_NAME = "Latitude";
    public static final String GRID_POINT_LON_NAME = "Longitude";

    public static final String SNAPSHOT_LIST_NAME = "Swath_Snapshot_List";
    public static final String SNAPSHOT_ID_NAME = "Snapshot_ID";

    public static final String BT_DATA_LIST_NAME = "BT_Data";
    public static final String BT_FLAGS_NAME = "Flags";

    /**
     * Mask for flags indicating polarization mode.
     */
    public static final int L1C_POL_MODE_FLAGS_MASK = 3;

    /**
     * Flag value indicating X polarisation mode.
     */
    public static final int L1C_POL_MODE_X = 0;
    /**
     * Flag value indicating Y polarisation mode.
     */
    public static final int L1C_POL_MODE_Y = 1;
    /**
     * Flag value indicating cross-polarisation mode.
     */
    public static final int L1C_POL_MODE_XY1 = 2;
    /**
     * Flag value indicating cross-polarisation mode.
     */
    public static final int L1C_POL_MODE_XY2 = 3;
}
