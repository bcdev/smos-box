package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.binx.BinX;
import com.bc.ceres.binio.binx.BinXException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteOrder;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Registry for all supported SMOS product formats.
 */
public class SmosFormats {

    public static final String GRID_POINT_LIST_NAME = "Grid_Point_List";
    public static final String GRID_POINT_ID_NAME = "Grid_Point_ID";
    public static final String GRID_POINT_LATITUDE_NAME = "Grid_Point_Latitude";
    public static final String GRID_POINT_LONGITUDE_NAME = "Grid_Point_Longitude";

    public static final String SNAPSHOT_LIST_NAME = "Snapshot_List";
    public static final String SNAPSHOT_ID_NAME = "Snapshot_ID";

    public static final String BT_DATA_LIST_NAME = "BT_Data_List";
    public static final String BT_FLAGS_NAME = "Flags";
    public static final String BT_INCIDENCE_ANGLE_NAME = "Incidence_Angle";
    public static final String BT_SNAPSHOT_ID_OF_PIXEL_NAME = "Snapshot_ID_of_Pixel";

    public static final int L1C_POL_FLAGS_MASK = 3;
    public static final int L1C_POL_MODE_X = 0;
    public static final int L1C_POL_MODE_Y = 1;
    public static final int L1C_POL_MODE_XY1 = 2;
    public static final int L1C_POL_MODE_XY2 = 3;

    @SuppressWarnings({"PointlessBitwiseExpression"})
    public static final FlagDescriptor[] L1C_FLAGS = {
//            new FlagDescriptor(1 << 0, "POL_FLAG_1", ""),
//            new FlagDescriptor(1 << 1, "POL_FLAG_2", ""),
            new FlagDescriptor(1 << 2, "SUN_FOV", "Direct  Sun  correction  has  been  performed  during  image reconstruction of this pixel"),
            new FlagDescriptor(1 << 3, "SUN_GLINT_FOV", "Reflected  Sun  correction  has  been  performed  during  image reconstruction of this pixel"),
            new FlagDescriptor(1 << 4, "MOON_GLINT_FOV", "Direct  Moon  correction  has  been  performed  during  image reconstruction of this pixel"),
            new FlagDescriptor(1 << 5, "SINGLE_SNAPSHOT", ""),
            new FlagDescriptor(1 << 6, "FTT", "Flat Target  Transformation  has  been  performed  during  image reconstruction of this pixel"),
            new FlagDescriptor(1 << 7, "SUN_POINT", "Pixel is located in a zone where a Sun alias was reconstructed (after Sun removal, measurement may be degraded)"),
            new FlagDescriptor(1 << 8, "SUN_GLINT_AREA", "Pixel is located in a zone where Sun reflection has been detected"),
            new FlagDescriptor(1 << 9, "MOON_POINT", "Pixel  is  located  in a zone where a Moon alias was reconstructed (after Moon removal, measurement may be degraded)"),
            new FlagDescriptor(1 << 10, "AF_FOV", "Pixel is inside the exclusive zone of Alias free (delimited by the six aliased unit circles)"),
            new FlagDescriptor(1 << 11, "EAF_FOV", "Pixel is inside the Extended Alias free zone (obtained after removing sky aliases)"),
            new FlagDescriptor(1 << 12, "BORDER_FOV", "Pixel is close to the border delimiting the Extended Alias free zone"),
            new FlagDescriptor(1 << 13, "SUN_TAILS", "Pixel is located in the hexagonal alias directions centred on a Sun alias (if Sun is not removed, measurement may be degraded in these directions)"),
            new FlagDescriptor(1 << 14, "RFI", "Pixel is affected by RFI effects (as identified in static ADF file)"),
    };

    @SuppressWarnings({"PointlessBitwiseExpression"})
    public static final FlagDescriptor[] L2_SM_CONFIDENCE_FLAGS = {
            new FlagDescriptor(1 << 1, "FL_RFI_PRONE_H", "DGG Current RFI for H pol above threshold"),
            new FlagDescriptor(1 << 2, "FL_RFI_PRONE_V", "DGG Current RFI for V pol above threshold"),
            new FlagDescriptor(1 << 4, "FL_NO_PROD", "No products are generated"),
            new FlagDescriptor(1 << 5, "FL_RANGE", "Retrieval values outside range"),
            new FlagDescriptor(1 << 6, "FL_DQX", "High retrieval DQX"),
            new FlagDescriptor(1 << 7, "FL_CHI2_P", "Poor fit quality"),
    };

    @SuppressWarnings({"PointlessBitwiseExpression"})
    public static final FlagDescriptor[] L2_SM_SCIENCE_FLAGS = {
            new FlagDescriptor(1 << 0, "FL_NON_NOM", "Presence of other than nominal soil"),
            new FlagDescriptor(1 << 1, "FL_SCENE_T", "True (1) if any of scene flags is set (1)"),
            new FlagDescriptor(1 << 2, "FL_BARREN", "Scene flag indicating presence of rocks"),
            new FlagDescriptor(1 << 3, "FL_TOPO_S", "Scene flag indicating presence of strong topography"),
            new FlagDescriptor(1 << 4, "FL_TOPO_M", "Scene flag indicating presence of moderate topography"),
            new FlagDescriptor(1 << 5, "FL_OW", "Scene flag indicating presence of open water"),
            new FlagDescriptor(1 << 6, "FL_SNOW_MIX", "Scene flag indicating presence of mixed snow"),
            new FlagDescriptor(1 << 7, "FL_SNOW_WET", "Scene flag indicating presence of wet snow"),
            new FlagDescriptor(1 << 8, "FL_SNOW_DRY", "Scene flag indicating presence of significant dry snow"),
            new FlagDescriptor(1 << 9, "FL_FOREST", "Scene flag indicating presence of forest"),
            new FlagDescriptor(1 << 10, "FL_NOMINAL", "Scene flag indicating presence of nominal soil"),
            new FlagDescriptor(1 << 11, "FL_FROST", "Scene flag indicating presence of frost"),
            new FlagDescriptor(1 << 12, "FL_ICE", "Scene flag indicating presence of permanent ice/snow"),
            new FlagDescriptor(1 << 13, "FL_WETLANDS", "Scene flag indicating presence of wetlands"),
            new FlagDescriptor(1 << 14, "FL_FLOOD_PROB", "Scene flag indicating probable flooding risk"),
            new FlagDescriptor(1 << 15, "FL_URBAN_LOW", "Scene flag indicating presence of limited urban area"),
            new FlagDescriptor(1 << 16, "FL_URBAN_HIGH", "Scene flag indicating presence of large urban area"),
            new FlagDescriptor(1 << 17, "FL_SAND", "Scene flag indicating presence of high sand fraction"),
            new FlagDescriptor(1 << 18, "FL_SEA_ICE", "Scene flag indicating presence of sea ice"),
            new FlagDescriptor(1 << 19, "FL_COAST", "Scene flag indicating presence of large tidal flag"),
            new FlagDescriptor(1 << 20, "FL_OCCUR_T", "True (1) if any of occur flags is set (1)"),
            new FlagDescriptor(1 << 21, "FL_LITTER", "Occur flag indicating litter suspected"),
            new FlagDescriptor(1 << 22, "FL_PR", "Occur flag indicating interception suspected (Pol ratio)"),
            new FlagDescriptor(1 << 23, "FL_INTERCEP", "Occur flag – ECMWF indicates interception"),
            new FlagDescriptor(1 << 24, "FL_EXTERNAL", "Any of the external flags on, or N_SKY counter not equal to zero"),
            new FlagDescriptor(1 << 25, "FL_RAIN", "External flag indicating heavy rain suspected"),
            new FlagDescriptor(1 << 26, "FL_TEC", "External flag indicating high ionospheric contributions"),
            new FlagDescriptor(1 << 27, "FL_TAU_FO", "Scene flag indicating presence of thick forest"),
            new FlagDescriptor(1 << 27, "FL_TAU_FO", "Scene flag indicating presence of thick forest"),
            new FlagDescriptor(1 << 28, "FL_WINTER_FOREST", "Flag indicating that the winter forest case has been selected by the decision tree"),
            new FlagDescriptor(1 << 29, "FL_DUAL_RETR_FNO_FFO", "Flag indicating dual retrieval is performed on the FNO and FFO fractions"),
    };

    @SuppressWarnings({"PointlessBitwiseExpression"})
    public static final FlagDescriptor[] L2_SM_PROCESSING_FLAGS = {
            new FlagDescriptor(1 << 0, "FL_R4", "It will be set to True if attempted regardless of success"),
            new FlagDescriptor(1 << 1, "FL_R3", "It will be set to True if attempted regardless of success"),
            new FlagDescriptor(1 << 2, "FL_R2", "It will be set to True if attempted regardless of success"),
            new FlagDescriptor(1 << 3, "FL_MD_A", "True if MDa failed"),
    };

    @SuppressWarnings({"PointlessBitwiseExpression"})
    public static final FlagDescriptor[] L2_SM_DGG_CURRENT_FLAGS = {
            new FlagDescriptor(1 << 0, "FL_CURRENT_TAU_NADIR_LV", "Flag driving request for updating the DGG_Current_Tau_Nadir_LV map after processing. 1 means update to the map"),
            new FlagDescriptor(1 << 1, "FL_CURRENT_TAU_NADIR_FO", "Flag driving request for updating the DGG_Current_Tau_Nadir_FO map after processing. 1 means update to the map"),
            new FlagDescriptor(1 << 2, "FL_CURRENT_HR", "Flag driving request for updating the DGG_Current_HR map after processing. 1 means update to the map"),
            new FlagDescriptor(1 << 3, "FL_CURRENT_RFI ", "Flag driving request for updating the DGG_Current_RFI map after processing. 1 means update to the map"),
            new FlagDescriptor(1 << 4, "FL_CURRENT_FLOOD", "Flag driving request for updating the DGG_Current_Flood map after processing. It is a place holder. No Algorithm has been defined yet. 1 means update to the map"),
    };

    @SuppressWarnings({"PointlessBitwiseExpression"})
    public static final FlagDescriptor[] L2_OS_CONTROL_FLAGS = {
            new FlagDescriptor(1 << 0, "FG_CTRL_SEL_GP", "Grid point selected according to land sea mask. Least significant bit"),
            new FlagDescriptor(1 << 1, "FG_CTRL_RANGE", "Retrieved values outside range. Least significant Bit"),
            new FlagDescriptor(1 << 2, "FG_CTRL_SIGMA", "High retrieval sigma"),
            new FlagDescriptor(1 << 3, "FG_CTRL_CHI2", "Poor fit quality"),
            new FlagDescriptor(1 << 4, "FG_CTRL_CHI2_P", "Poor fit quality"),
            new FlagDescriptor(1 << 5, "FG_CTRL_QUALITY_SSS", "At least one critical flag was raised during SSS1 retrieval"),
            new FlagDescriptor(1 << 6, "FG_CTRL_SUNGLINT", "Grid point with number of measurements flagged for sunglint above threshold"),
            new FlagDescriptor(1 << 7, "FG_CTRL_MOONGLINT", "Grid point with number of measurements flagged for moonglint above threshold"),
            new FlagDescriptor(1 << 8, "FG_CTRL_GAL_NOISE", "Grid point with number of measurements flagged for galactic noise above threshold"),
            new FlagDescriptor(1 << 9, "FG_CTRL_GAL_NOISE_POL", "Grid point with number of measurements flagged for polarised galactic noise above threshold"),
            new FlagDescriptor(1 << 10, "FG_CTRL_REACH_MAXITER", "Maximum number of iteration reached before convergence"),
            new FlagDescriptor(1 << 11, "FG_CTRL_NUM_MEAS_MIN", "Not processed due to too few valid measurements"),
            new FlagDescriptor(1 << 12, "FG_CTRL_NUM_MEAS_LOW", "Number of valid measurements used for retrieval is less than Tg_num_meas_valid"),
            new FlagDescriptor(1 << 13, "FG_CTRL_MANY_OUTLIERS", "If number of outliers Dg_num_outliers > Tg_num_outliers_max"),
            new FlagDescriptor(1 << 14, "FG_CTRL_MARQ", "Iterative loop ends because Marquardt increment is greather than lambdaMax"),
            new FlagDescriptor(1 << 15, "FG_CTRL_ROUGHNESS", "Roughness correction applied"),
            new FlagDescriptor(1 << 16, "FG_CTRL_FOAM", "Wind speed is less than Tg_WS_foam and foam contribution and foam fraction are set to zero"),
            new FlagDescriptor(1 << 17, "FG_CTRL_ECMWF", "Flag set to false if one or more ECMWF data is missing for the different models. Most significant Bit"),
            new FlagDescriptor(1 << 18, "FG_CTRL_VALID", "Flags raised if grid points pass grid point measurement discrimination tests"),
            new FlagDescriptor(1 << 19, "FG_CTRL_NO_SURFACE", "Flags raised if the 42.5º angle is not included in the dwell line for grid points"),
            new FlagDescriptor(1 << 20, "FG_CTRL_RANGE_ACARD", "Flags raised if retrieved Acard is outside range"),
            new FlagDescriptor(1 << 21, "FG_CTRL_SIGMA_ACARD", "Flags raised if retrieved Acard sigma is too high"),
            new FlagDescriptor(1 << 22, "FG_CTRL_QUALITY_ACARD", "Flags raised if at least one critical flag was raised during Acard retrieval. Most significant Bit"),
    };

    @SuppressWarnings({"PointlessBitwiseExpression"})
    public static final FlagDescriptor[] L2_OS_SCIENCE_FLAGS = {
            new FlagDescriptor(1 << 0, "FG_SC_LAND_SEA_COAST1", "Distance from coast to gridpoint is less than threshold Max1 in file AUX_DISTAN"),
            new FlagDescriptor(1 << 1, "FG_SC_LAND_SEA_COAST2", "Distance from coast to gridpoint is less than threshold Max2 in file AUX_DISTAN"),
            new FlagDescriptor(1 << 2, "FG_SC_TEC_GRADIENT", "High TEC gradient along dwell for a grid point"),
            new FlagDescriptor(1 << 3, "FG_SC_IN_CLIM_ICE", "Gridpoint with maximum extend of sea ice accordy to monthly climatology"),
            new FlagDescriptor(1 << 4, "FG_SC_ICE", "Ice concentration at gridpoint is above threshold Tg_ice_concentration"),
            new FlagDescriptor(1 << 5, "FG_SC_SUSPECT_ICE", "Suspect ice on gridpoint"),
            new FlagDescriptor(1 << 6, "FG_SC_RAIN", "Heavy rain suspected on gridpoint. Rain rate is above threshold Tg_max_rainfall"),
            new FlagDescriptor(1 << 7, "FG_SC_HIGH_WIND", "High wind"),
            new FlagDescriptor(1 << 8, "FG_SC_LOW_WIND", "Low wind"),
            new FlagDescriptor(1 << 9, "FG_SC_HIGHT_SST", "High SST"),
            new FlagDescriptor(1 << 10, "FG_SC_LOW_SST", "Low SST"),
            new FlagDescriptor(1 << 11, "FG_SC_HIGH_SSS", "High SSS"),
            new FlagDescriptor(1 << 12, "FG_SC_LOW_SSS", "Low SSS"),
            new FlagDescriptor(1 << 13, "FG_SC_SEA_STATE_1", "Sea state class 1"),
            new FlagDescriptor(1 << 14, "FG_SC_SEA_STATE_2", "Sea state class 2"),
            new FlagDescriptor(1 << 15, "FG_SC_SEA_STATE_3", "Sea state class 3"),
            new FlagDescriptor(1 << 16, "FG_SC_SEA_STATE_4", "Sea state class 4"),
            new FlagDescriptor(1 << 17, "FG_SC_SEA_STATE_5", "Sea state class 5"),
            new FlagDescriptor(1 << 18, "FG_SC_SEA_STATE_6", "Sea state class 6"),
            new FlagDescriptor(1 << 19, "FG_SC_SST_FRONT", "Not implemented yet"),
            new FlagDescriptor(1 << 20, "FG_SC_SSS_FRONT", "Not implemented yet"),
            new FlagDescriptor(1 << 21, "FG_SC_ICE_ACARD", "Ice flag from cardioid"),
    };

    private static final SmosFormats INSTANCE = new SmosFormats();

    private final ConcurrentMap<String, DataFormat> formatMap;

    private SmosFormats() {
        formatMap = new ConcurrentHashMap<String, DataFormat>(17);
    }

    public static SmosFormats getInstance() {
        return INSTANCE;
    }

    public String[] getFormatNames() {
        final Set<String> names = formatMap.keySet();
        return names.toArray(new String[names.size()]);
    }

    public DataFormat getFormat(String name) {
        if (!formatMap.containsKey(name)) {
            final URL schemaUrl = getSchemaResource(name);

            if (schemaUrl != null) {
                final BinX binX = createBinX(name);

                try {
                    final DataFormat format = binX.readDataFormat(schemaUrl.toURI(), name);
                    format.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                    formatMap.putIfAbsent(name, format);
                } catch (BinXException e) {
                    throw new IllegalStateException(
                            MessageFormat.format("Schema resource ''{0}'': {1}", schemaUrl, e.getMessage()));
                } catch (IOException e) {
                    throw new IllegalStateException(
                            MessageFormat.format("Schema resource ''{0}'': {1}", schemaUrl, e.getMessage()));
                } catch (URISyntaxException e) {
                    throw new IllegalStateException(
                            MessageFormat.format("Schema resource ''{0}'': {1}", schemaUrl, e.getMessage()));
                }
            }
        }

        return formatMap.get(name);
    }

    static DataFormat getFormat(File hdrFile) throws IOException {
        final Document document;

        try {
            document = new SAXBuilder().build(hdrFile);
        } catch (JDOMException e) {
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': Invalid document", hdrFile.getPath()), e);
        }

        final Namespace namespace = document.getRootElement().getNamespace();
        if (namespace == null) {
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': Missing namespace", hdrFile.getPath()));
        }

        final Element variableHeader = document.getRootElement().getChild("Variable_Header", namespace);
        if (variableHeader == null) {
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': Missing variable header", hdrFile.getPath()));
        }

        final Element specificProductHeader = variableHeader.getChild("Specific_Product_Header", namespace);
        if (specificProductHeader == null) {
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': Missing specific product header", hdrFile.getPath()));
        }

        final Element mainInfo = specificProductHeader.getChild("Main_Info", namespace);
        if (mainInfo == null) {
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': Missing main info.", hdrFile.getPath()));
        }

        final String schema = mainInfo.getChildText("Datablock_Schema", namespace);
        if (schema == null) {
            throw new IOException(
                    MessageFormat.format("File ''{0}'': Missing datablock schema''", hdrFile.getPath()));
        }

        return getInstance().getFormat(schema);
    }

    static URL getSchemaResource(String name) {
        // Reference: SO-MA-IDR-GS-0004, SMOS DPGS, XML Schema Guidelines
        if (name == null || !name.matches("DBL_\\w{2}_\\w{4}_\\w{10}_\\d{4}(\\.binXschema\\.xml)?")) {
            return null;
        }

        final String fc = name.substring(12, 16);
        final String sd = name.substring(16, 22);

        final StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append("schemas/").append(fc).append("/").append(sd).append("/").append(name);
        if (!name.endsWith(".binXschema.xml")) {
            nameBuilder.append(".binXschema.xml");
        }

        return SmosFormats.class.getResource(nameBuilder.toString());
    }

    private static BinX createBinX(String name) {
        final BinX binX = new BinX();
        binX.setSingleDatasetStructInlined(true);
        binX.setArrayVariableInlined(true);

        try {
            binX.setVarNameMappings(getResourceAsProperties("binx_var_name_mappings.properties"));

            if (name.contains("MIR_OSUDP2")) {
                binX.setTypeMembersInlined(getResourceAsProperties("binx_inlined_structs_MIR_OSUDP2.properties"));
            }
            if (name.contains("MIR_SMUDP2")) {
                binX.setTypeMembersInlined(getResourceAsProperties("binx_inlined_structs_MIR_SMUDP2.properties"));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }

        return binX;
    }

    private static Properties getResourceAsProperties(String name) throws IOException {
        final Properties properties = new Properties();
        final InputStream is = SmosFormats.class.getResourceAsStream(name);

        if (is != null) {
            properties.load(is);
        }

        return properties;
    }
}
