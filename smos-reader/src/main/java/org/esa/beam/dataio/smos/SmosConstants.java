package org.esa.beam.dataio.smos;

public class SmosConstants {

    private SmosConstants() {
    }

    public static final String TAG_DATABLOCK_SCHEMA = "Datablock_Schema";

    // Reference: SO-MA-IDR-GS-0004, SMOS DPGS, XML Schema Guidelines
    public static final String SCHEMA_NAMING_CONVENTION = "DBL_\\w{2}_\\w{4}_\\w{10}_\\d{4}";

    // the name of any grid point data sequence is mapped to this name
    public static final String GRID_POINT_LIST_NAME = "Grid_Point_List";

    public static final String GRID_POINT_COUNTER_NAME = "Grid_Point_Counter";
    public static final String GRID_POINT_ID_NAME = "Grid_Point_ID";
    public static final String GRID_POINT_LAT_NAME = "Latitude";
    public static final String GRID_POINT_LON_NAME = "Longitude";

    // used for reading L1C data
    public static final String SNAPSHOT_LIST_NAME = "Swath_Snapshot_List";
    public static final String SNAPSHOT_ID_NAME = "Snapshot_ID";

    public static final String BT_DATA_LIST_NAME = "BT_Data";
    public static final String BT_FLAGS_NAME = "Flags";
    public static final String BT_INCIDENCE_ANGLE_NAME = "Incidence_Angle";
    public static final String BT_SNAPSHOT_ID_OF_PIXEL_NAME = "Snapshot_ID_of_Pixel";

    public static final int L1C_POL_FLAGS_MASK = 3;
    public static final int L1C_POL_MODE_X = 0;
    public static final int L1C_POL_MODE_Y = 1;
    public static final int L1C_POL_MODE_XY1 = 2;
    public static final int L1C_POL_MODE_XY2 = 3;

    // used for reading VTEC data
    public static final String TAG_IONEX_DESCRIPTOR = "IONEX_Descriptor";
    public static final String TAG_LATITUDE_VECTOR = "Latitude_Vector";
    public static final String TAG_LATITUDE_VECTOR_1ST = "Latitude_Vector_1st";
    public static final String TAG_LATITUDE_VECTOR_2ND = "Latitude_Vector_2nd";
    public static final String TAG_LATITUDE_VECTOR_INCREMENT = "Latitude_Vector_Increment";
    public static final String TAG_LONGITUDE_VECTOR = "Longitude_Vector";
    public static final String TAG_LONGITUDE_VECTOR_1ST = "Longitude_Vector_1st";
    public static final String TAG_LONGITUDE_VECTOR_2ND = "Longitude_Vector_2nd";
    public static final String TAG_LONGITUDE_VECTOR_INCREMENT = "Longitude_Vector_Increment";
    public static final String TAG_SCALING_FACTOR_EXPONENT = "Scale_Factor";

    public static final String VTEC_INFO_NAME = "VTEC_Info";
    public static final String VTEC_RECORD_NAME = "VTEC_Record";
    public static final String VTEC_DATA_NAME = "VTEC_Data";
    public static final String VTEC_VALUE_NAME = "VTEC_value";
}
