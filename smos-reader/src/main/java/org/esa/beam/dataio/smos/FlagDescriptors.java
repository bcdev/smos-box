/* 
 * Copyright (C) 2002-2008 by Brockmann Consult
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.dataio.smos;

import java.awt.Color;

/**
 * Descriptor for flags.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 * @since SMOS-Box 1.0
 */
@Deprecated
public final class FlagDescriptors {

    @SuppressWarnings({"PointlessBitwiseExpression"})
    public static final FlagDescriptors[] L2_SM_CONFIDENCE_FLAGS = {
            new FlagDescriptors("FL_RFI_PRONE_H", 1 << 1, "DGG Current RFI for H pol above threshold"),
            new FlagDescriptors("FL_RFI_PRONE_V", 1 << 2, "DGG Current RFI for V pol above threshold"),
            new FlagDescriptors("FL_NO_PROD", 1 << 4, "No products are generated"),
            new FlagDescriptors("FL_RANGE", 1 << 5, "Retrieval values outside range"),
            new FlagDescriptors("FL_DQX", 1 << 6, "High retrieval DQX"),
            new FlagDescriptors("FL_CHI2_P", 1 << 7, "Poor fit quality"),
    };

    @SuppressWarnings({"PointlessBitwiseExpression"})
    public static final FlagDescriptors[] L2_SM_SCIENCE_FLAGS = {
            new FlagDescriptors("FL_NON_NOM", 1 << 0, "Presence of other than nominal soil"),
            new FlagDescriptors("FL_SCENE_T", 1 << 1, "True (1) if any of scene flags is set (1)"),
            new FlagDescriptors("FL_BARREN", 1 << 2, "Scene flag indicating presence of rocks"),
            new FlagDescriptors("FL_TOPO_S", 1 << 3, "Scene flag indicating presence of strong topography"),
            new FlagDescriptors("FL_TOPO_M", 1 << 4, "Scene flag indicating presence of moderate topography"),
            new FlagDescriptors("FL_OW", 1 << 5, "Scene flag indicating presence of open water"),
            new FlagDescriptors("FL_SNOW_MIX", 1 << 6, "Scene flag indicating presence of mixed snow"),
            new FlagDescriptors("FL_SNOW_WET", 1 << 7, "Scene flag indicating presence of wet snow"),
            new FlagDescriptors("FL_SNOW_DRY", 1 << 8, "Scene flag indicating presence of significant dry snow"),
            new FlagDescriptors("FL_FOREST", 1 << 9, "Scene flag indicating presence of forest"),
            new FlagDescriptors("FL_NOMINAL", 1 << 10, "Scene flag indicating presence of nominal soil"),
            new FlagDescriptors("FL_FROST", 1 << 11, "Scene flag indicating presence of frost"),
            new FlagDescriptors("FL_ICE", 1 << 12, "Scene flag indicating presence of permanent ice/snow"),
            new FlagDescriptors("FL_WETLANDS", 1 << 13, "Scene flag indicating presence of wetlands"),
            new FlagDescriptors("FL_FLOOD_PROB", 1 << 14, "Scene flag indicating probable flooding risk"),
            new FlagDescriptors("FL_URBAN_LOW", 1 << 15, "Scene flag indicating presence of limited urban area"),
            new FlagDescriptors("FL_URBAN_HIGH", 1 << 16, "Scene flag indicating presence of large urban area"),
            new FlagDescriptors("FL_SAND", 1 << 17, "Scene flag indicating presence of high sand fraction"),
            new FlagDescriptors("FL_SEA_ICE", 1 << 18, "Scene flag indicating presence of sea ice"),
            new FlagDescriptors("FL_COAST", 1 << 19, "Scene flag indicating presence of large tidal flag"),
            new FlagDescriptors("FL_OCCUR_T", 1 << 20, "True (1) if any of occur flags is set (1)"),
            new FlagDescriptors("FL_LITTER", 1 << 21, "Occur flag indicating litter suspected"),
            new FlagDescriptors("FL_PR", 1 << 22, "Occur flag indicating interception suspected (Pol ratio)"),
            new FlagDescriptors("FL_INTERCEP", 1 << 23, "Occur flag - ECMWF indicates interception"),
            new FlagDescriptors("FL_EXTERNAL", 1 << 24,
                               "Any of the external flags on, or N_SKY counter not equal to zero"),
            new FlagDescriptors("FL_RAIN", 1 << 25, "External flag indicating heavy rain suspected"),
            new FlagDescriptors("FL_TEC", 1 << 26, "External flag indicating high ionospheric contributions"),
            new FlagDescriptors("FL_TAU_FO", 1 << 27, "Scene flag indicating presence of thick forest"),
            new FlagDescriptors("FL_TAU_FO", 1 << 27, "Scene flag indicating presence of thick forest"),
            new FlagDescriptors("FL_WINTER_FOREST", 1 << 28,
                               "Flag indicating that the winter forest case has been selected by the decision tree"),
            new FlagDescriptors("FL_DUAL_RETR_FNO_FFO", 1 << 29,
                               "Flag indicating dual retrieval is performed on the FNO and FFO fractions"),
    };

    @SuppressWarnings({"PointlessBitwiseExpression"})
    public static final FlagDescriptors[] L2_SM_PROCESSING_FLAGS = {
            new FlagDescriptors("FL_R4", 1 << 0, "It will be set to True if attempted regardless of success"),
            new FlagDescriptors("FL_R3", 1 << 1, "It will be set to True if attempted regardless of success"),
            new FlagDescriptors("FL_R2", 1 << 2, "It will be set to True if attempted regardless of success"),
            new FlagDescriptors("FL_MD_A", 1 << 3, "True if MDa failed"),
    };

    @SuppressWarnings({"PointlessBitwiseExpression"})
    public static final FlagDescriptors[] L2_SM_DGG_CURRENT_FLAGS = {
            new FlagDescriptors("FL_CURRENT_TAU_NADIR_LV", 1 << 0,
                               "Flag driving request for updating the DGG_Current_Tau_Nadir_LV map after processing. 1 means update to the map"),
            new FlagDescriptors("FL_CURRENT_TAU_NADIR_FO", 1 << 1,
                               "Flag driving request for updating the DGG_Current_Tau_Nadir_FO map after processing. 1 means update to the map"),
            new FlagDescriptors("FL_CURRENT_HR", 1 << 2,
                               "Flag driving request for updating the DGG_Current_HR map after processing. 1 means update to the map"),
            new FlagDescriptors("FL_CURRENT_RFI ", 1 << 3,
                               "Flag driving request for updating the DGG_Current_RFI map after processing. 1 means update to the map"),
            new FlagDescriptors("FL_CURRENT_FLOOD", 1 << 4,
                               "Flag driving request for updating the DGG_Current_Flood map after processing. It is a place holder. No Algorithm has been defined yet. 1 means update to the map"),
    };

    @SuppressWarnings({"PointlessBitwiseExpression"})
    public static final FlagDescriptors[] L2_OS_CONTROL_FLAGS = {
            new FlagDescriptors("FG_CTRL_SEL_GP", 1 << 0,
                               "Grid point selected according to land sea mask. Least significant bit"),
            new FlagDescriptors("FG_CTRL_RANGE", 1 << 1, "Retrieved values outside range. Least significant Bit"),
            new FlagDescriptors("FG_CTRL_SIGMA", 1 << 2, "High retrieval sigma"),
            new FlagDescriptors("FG_CTRL_CHI2", 1 << 3, "Poor fit quality"),
            new FlagDescriptors("FG_CTRL_CHI2_P", 1 << 4, "Poor fit quality"),
            new FlagDescriptors("FG_CTRL_QUALITY_SSS", 1 << 5,
                               "At least one critical flag was raised during SSS1 retrieval"),
            new FlagDescriptors("FG_CTRL_SUNGLINT", 1 << 6,
                               "Grid point with number of measurements flagged for sunglint above threshold"),
            new FlagDescriptors("FG_CTRL_MOONGLINT", 1 << 7,
                               "Grid point with number of measurements flagged for moonglint above threshold"),
            new FlagDescriptors("FG_CTRL_GAL_NOISE", 1 << 8,
                               "Grid point with number of measurements flagged for galactic noise above threshold"),
            new FlagDescriptors("FG_CTRL_GAL_NOISE_POL", 1 << 9,
                               "Grid point with number of measurements flagged for polarised galactic noise above threshold"),
            new FlagDescriptors("FG_CTRL_REACH_MAXITER", 1 << 10,
                               "Maximum number of iteration reached before convergence"),
            new FlagDescriptors("FG_CTRL_NUM_MEAS_MIN", 1 << 11, "Not processed due to too few valid measurements"),
            new FlagDescriptors("FG_CTRL_NUM_MEAS_LOW", 1 << 12,
                               "Number of valid measurements used for retrieval is less than Tg_num_meas_valid"),
            new FlagDescriptors("FG_CTRL_MANY_OUTLIERS", 1 << 13,
                               "If number of outliers Dg_num_outliers > Tg_num_outliers_max"),
            new FlagDescriptors("FG_CTRL_MARQ", 1 << 14,
                               "Iterative loop ends because Marquardt increment is greather than lambdaMax"),
            new FlagDescriptors("FG_CTRL_ROUGHNESS", 1 << 15, "Roughness correction applied"),
            new FlagDescriptors("FG_CTRL_FOAM", 1 << 16,
                               "Wind speed is less than Tg_WS_foam and foam contribution and foam fraction are set to zero"),
            new FlagDescriptors("FG_CTRL_ECMWF", 1 << 17,
                               "Flag set to false if one or more ECMWF data is missing for the different models. Most significant Bit"),
            new FlagDescriptors("FG_CTRL_VALID", 1 << 18,
                               "Flags raised if grid points pass grid point measurement discrimination tests"),
            new FlagDescriptors("FG_CTRL_NO_SURFACE", 1 << 19,
                               "Flags raised if the 42.5° angle is not included in the dwell line for grid points"),
            new FlagDescriptors("FG_CTRL_RANGE_ACARD", 1 << 20, "Flags raised if retrieved Acard is outside range"),
            new FlagDescriptors("FG_CTRL_SIGMA_ACARD", 1 << 21, "Flags raised if retrieved Acard sigma is too high"),
            new FlagDescriptors("FG_CTRL_QUALITY_ACARD", 1 << 22,
                               "Flags raised if at least one critical flag was raised during Acard retrieval. Most significant Bit"),
    };

    @SuppressWarnings({"PointlessBitwiseExpression"})
    public static final FlagDescriptors[] L2_OS_SCIENCE_FLAGS = {
            new FlagDescriptors("FG_SC_LAND_SEA_COAST1", 1 << 0,
                               "Distance from coast to gridpoint is less than threshold Max1 in file AUX_DISTAN"),
            new FlagDescriptors("FG_SC_LAND_SEA_COAST2", 1 << 1,
                               "Distance from coast to gridpoint is less than threshold Max2 in file AUX_DISTAN"),
            new FlagDescriptors("FG_SC_TEC_GRADIENT", 1 << 2, "High TEC gradient along dwell for a grid point"),
            new FlagDescriptors("FG_SC_IN_CLIM_ICE", 1 << 3,
                               "Gridpoint with maximum extend of sea ice accordy to monthly climatology"),
            new FlagDescriptors("FG_SC_ICE", 1 << 4,
                               "Ice concentration at gridpoint is above threshold Tg_ice_concentration"),
            new FlagDescriptors("FG_SC_SUSPECT_ICE", 1 << 5, "Suspect ice on gridpoint"),
            new FlagDescriptors("FG_SC_RAIN", 1 << 6,
                               "Heavy rain suspected on gridpoint. Rain rate is above threshold Tg_max_rainfall"),
            new FlagDescriptors("FG_SC_HIGH_WIND", 1 << 7, "High wind"),
            new FlagDescriptors("FG_SC_LOW_WIND", 1 << 8, "Low wind"),
            new FlagDescriptors("FG_SC_HIGHT_SST", 1 << 9, "High SST"),
            new FlagDescriptors("FG_SC_LOW_SST", 1 << 10, "Low SST"),
            new FlagDescriptors("FG_SC_HIGH_SSS", 1 << 11, "High SSS"),
            new FlagDescriptors("FG_SC_LOW_SSS", 1 << 12, "Low SSS"),
            new FlagDescriptors("FG_SC_SEA_STATE_1", 1 << 13, "Sea state class 1"),
            new FlagDescriptors("FG_SC_SEA_STATE_2", 1 << 14, "Sea state class 2"),
            new FlagDescriptors("FG_SC_SEA_STATE_3", 1 << 15, "Sea state class 3"),
            new FlagDescriptors("FG_SC_SEA_STATE_4", 1 << 16, "Sea state class 4"),
            new FlagDescriptors("FG_SC_SEA_STATE_5", 1 << 17, "Sea state class 5"),
            new FlagDescriptors("FG_SC_SEA_STATE_6", 1 << 18, "Sea state class 6"),
            new FlagDescriptors("FG_SC_SST_FRONT", 1 << 19, "Not implemented yet"),
            new FlagDescriptors("FG_SC_SSS_FRONT", 1 << 20, "Not implemented yet"),
            new FlagDescriptors("FG_SC_ICE_ACARD", 1 << 21, "Ice flag from cardioid"),
    };

    @SuppressWarnings({"PointlessBitwiseExpression"})
    public static final FlagDescriptors[] L1C_FLAGS = {
            new FlagDescriptors("POL_FLAG_1", 1 << 0, ""),
            new FlagDescriptors("POL_FLAG_2", 1 << 1, ""),
            new FlagDescriptors("SUN_FOV", 1 << 2,
                               "Direct  Sun  correction  has  been  performed  during  image reconstruction of this pixel"),
            new FlagDescriptors("SUN_GLINT_FOV", 1 << 3,
                               "Reflected  Sun  correction  has  been  performed  during  image reconstruction of this pixel"),
            new FlagDescriptors("MOON_GLINT_FOV", 1 << 4,
                               "Direct  Moon  correction  has  been  performed  during  image reconstruction of this pixel"),
            new FlagDescriptors("SINGLE_SNAPSHOT", 1 << 5, ""),
            new FlagDescriptors("FTT", 1 << 6,
                               "Flat Target  Transformation  has  been  performed  during  image reconstruction of this pixel"),
            new FlagDescriptors("SUN_POINT", 1 << 7,
                               "Pixel is located in a zone where a Sun alias was reconstructed (after Sun removal, measurement may be degraded)"),
            new FlagDescriptors("SUN_GLINT_AREA", 1 << 8,
                               "Pixel is located in a zone where Sun reflection has been detected"),
            new FlagDescriptors("MOON_POINT", 1 << 9,
                               "Pixel  is  located  in a zone where a Moon alias was reconstructed (after Moon removal, measurement may be degraded)"),
            new FlagDescriptors("AF_FOV", 1 << 10,
                               "Pixel is inside the exclusive zone of Alias free (delimited by the six aliased unit circles)"),
            new FlagDescriptors("EAF_FOV", 1 << 11,
                               "Pixel is inside the Extended Alias free zone (obtained after removing sky aliases)"),
            new FlagDescriptors("BORDER_FOV", 1 << 12,
                               "Pixel is close to the border delimiting the Extended Alias free zone"),
            new FlagDescriptors("SUN_TAILS", 1 << 13,
                               "Pixel is located in the hexagonal alias directions centred on a Sun alias (if Sun is not removed, measurement may be degraded in these directions)"),
            new FlagDescriptors("RFI", 1 << 14, "Pixel is affected by RFI effects (as identified in static ADF file)"),
    };

    private final String flagName;
    private final int mask;
    private final Color color;
    private final double transparency;
    private final String description;


    private FlagDescriptors(String flagName, int mask, String description) {
        this.flagName = flagName;
        this.mask = mask;
        this.color = null;
        this.transparency = 0.5;
        this.description = description;
    }

    public final String getFlagName() {
        return flagName;
    }

    public final int getMask() {
        return mask;
    }

    public final Color getColor() {
        return color;
    }

    public final double getTransparency() {
        return transparency;
    }

    public final String getDescription() {
        return description;
    }

    private static Color getColor(String token, Color defaultColor) {
        if ("*".equals(token.trim())) {
            return defaultColor;
        }

        return new Color(Integer.parseInt(token, 16));
    }

    private static double getDouble(String token, double defaultValue) {
        if ("*".equals(token.trim())) {
            return defaultValue;
        }
        return Double.parseDouble(token);
    }

    private static int getHex(String token, int defaultValue) {
        if ("*".equals(token.trim())) {
            return defaultValue;
        }
        return Integer.parseInt(token, 16);
    }

    private static String getString(String token) {
        return token.trim();
    }

    private static String getString(String token, String defaultValue) {
        if ("*".equals(token.trim())) {
            return defaultValue;
        }
        return token.trim();
    }
}