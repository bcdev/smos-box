package org.esa.beam.dataio.smos;

public class SmosConstants {

    // Reference: SO-MA-IDR-GS-0004, SMOS DPGS, XML Schema Guidelines
    public static final String SCHEMA_NAMING_CONVENTION = "DBL_\\w{2}_\\w{4}_\\w{10}_\\d{4}";

    // the name of any grid point data sequence is mapped to this name
    public static final String GRID_POINT_LIST_NAME = "Grid_Point_List";

    public static final String GRID_POINT_COUNTER_NAME = "Grid_Point_Counter";
    public static final String GRID_POINT_ID_NAME = "Grid_Point_ID";
    public static final String GRID_POINT_LAT_NAME = "Latitude";
    public static final String GRID_POINT_LON_NAME = "Longitude";

    public static final String SNAPSHOT_LIST_NAME = "Swath_Snapshot_List";
    public static final String SNAPSHOT_ID_NAME = "Snapshot_ID";

    // from L!C schemas
    public static final String BT_DATA_LIST_NAME = "BT_Data";
    public static final String BT_FLAGS_NAME = "Flags";
    public static final String BT_INCIDENCE_ANGLE_NAME = "Incidence_Angle";
    public static final String BT_SNAPSHOT_ID_OF_PIXEL_NAME = "Snapshot_ID_of_Pixel";

    public static final int L1C_POL_FLAGS_MASK = 3;
    public static final int L1C_POL_MODE_X = 0;
    public static final int L1C_POL_MODE_Y = 1;
    public static final int L1C_POL_MODE_XY1 = 2;
    public static final int L1C_POL_MODE_XY2 = 3;
    public static final int L1C_POL_MODE_ANY = 4;
}
