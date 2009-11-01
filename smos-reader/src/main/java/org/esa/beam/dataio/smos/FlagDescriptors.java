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
    public static final FlagDescriptor[] L2_SM_CONFIDENCE_FLAGS = {
            new FlagDescriptorI("FL_RFI_PRONE_H", 1 << 1, "DGG Current RFI for H pol above threshold"),
            new FlagDescriptorI("FL_RFI_PRONE_V", 1 << 2, "DGG Current RFI for V pol above threshold"),
            new FlagDescriptorI("FL_NO_PROD", 1 << 4, "No products are generated"),
            new FlagDescriptorI("FL_RANGE", 1 << 5, "Retrieval values outside range"),
            new FlagDescriptorI("FL_DQX", 1 << 6, "High retrieval DQX"),
            new FlagDescriptorI("FL_CHI2_P", 1 << 7, "Poor fit quality"),
    };

    @SuppressWarnings({"PointlessBitwiseExpression"})
    public static final FlagDescriptor[] L2_SM_SCIENCE_FLAGS = {
            new FlagDescriptorI("FL_NON_NOM", 1 << 0, "Presence of other than nominal soil"),
            new FlagDescriptorI("FL_SCENE_T", 1 << 1, "True (1) if any of scene flags is set (1)"),
            new FlagDescriptorI("FL_BARREN", 1 << 2, "Scene flag indicating presence of rocks"),
            new FlagDescriptorI("FL_TOPO_S", 1 << 3, "Scene flag indicating presence of strong topography"),
            new FlagDescriptorI("FL_TOPO_M", 1 << 4, "Scene flag indicating presence of moderate topography"),
            new FlagDescriptorI("FL_OW", 1 << 5, "Scene flag indicating presence of open water"),
            new FlagDescriptorI("FL_SNOW_MIX", 1 << 6, "Scene flag indicating presence of mixed snow"),
            new FlagDescriptorI("FL_SNOW_WET", 1 << 7, "Scene flag indicating presence of wet snow"),
            new FlagDescriptorI("FL_SNOW_DRY", 1 << 8, "Scene flag indicating presence of significant dry snow"),
            new FlagDescriptorI("FL_FOREST", 1 << 9, "Scene flag indicating presence of forest"),
            new FlagDescriptorI("FL_NOMINAL", 1 << 10, "Scene flag indicating presence of nominal soil"),
            new FlagDescriptorI("FL_FROST", 1 << 11, "Scene flag indicating presence of frost"),
            new FlagDescriptorI("FL_ICE", 1 << 12, "Scene flag indicating presence of permanent ice/snow"),
            new FlagDescriptorI("FL_WETLANDS", 1 << 13, "Scene flag indicating presence of wetlands"),
            new FlagDescriptorI("FL_FLOOD_PROB", 1 << 14, "Scene flag indicating probable flooding risk"),
            new FlagDescriptorI("FL_URBAN_LOW", 1 << 15, "Scene flag indicating presence of limited urban area"),
            new FlagDescriptorI("FL_URBAN_HIGH", 1 << 16, "Scene flag indicating presence of large urban area"),
            new FlagDescriptorI("FL_SAND", 1 << 17, "Scene flag indicating presence of high sand fraction"),
            new FlagDescriptorI("FL_SEA_ICE", 1 << 18, "Scene flag indicating presence of sea ice"),
            new FlagDescriptorI("FL_COAST", 1 << 19, "Scene flag indicating presence of large tidal flag"),
            new FlagDescriptorI("FL_OCCUR_T", 1 << 20, "True (1) if any of occur flags is set (1)"),
            new FlagDescriptorI("FL_LITTER", 1 << 21, "Occur flag indicating litter suspected"),
            new FlagDescriptorI("FL_PR", 1 << 22, "Occur flag indicating interception suspected (Pol ratio)"),
            new FlagDescriptorI("FL_INTERCEP", 1 << 23, "Occur flag - ECMWF indicates interception"),
            new FlagDescriptorI("FL_EXTERNAL", 1 << 24,
                                "Any of the external flags on, or N_SKY counter not equal to zero"),
            new FlagDescriptorI("FL_RAIN", 1 << 25, "External flag indicating heavy rain suspected"),
            new FlagDescriptorI("FL_TEC", 1 << 26, "External flag indicating high ionospheric contributions"),
            new FlagDescriptorI("FL_TAU_FO", 1 << 27, "Scene flag indicating presence of thick forest"),
            new FlagDescriptorI("FL_TAU_FO", 1 << 27, "Scene flag indicating presence of thick forest"),
            new FlagDescriptorI("FL_WINTER_FOREST", 1 << 28,
                                "Flag indicating that the winter forest case has been selected by the decision tree"),
            new FlagDescriptorI("FL_DUAL_RETR_FNO_FFO", 1 << 29,
                                "Flag indicating dual retrieval is performed on the FNO and FFO fractions"),
    };

    @SuppressWarnings({"PointlessBitwiseExpression"})
    public static final FlagDescriptor[] L2_SM_PROCESSING_FLAGS = {
            new FlagDescriptorI("FL_R4", 1 << 0, "It will be set to True if attempted regardless of success"),
            new FlagDescriptorI("FL_R3", 1 << 1, "It will be set to True if attempted regardless of success"),
            new FlagDescriptorI("FL_R2", 1 << 2, "It will be set to True if attempted regardless of success"),
            new FlagDescriptorI("FL_MD_A", 1 << 3, "True if MDa failed"),
    };

    @SuppressWarnings({"PointlessBitwiseExpression"})
    public static final FlagDescriptor[] L2_SM_DGG_CURRENT_FLAGS = {
            new FlagDescriptorI("FL_CURRENT_TAU_NADIR_LV", 1 << 0,
                                "Flag driving request for updating the DGG_Current_Tau_Nadir_LV map after processing. 1 means update to the map"),
            new FlagDescriptorI("FL_CURRENT_TAU_NADIR_FO", 1 << 1,
                                "Flag driving request for updating the DGG_Current_Tau_Nadir_FO map after processing. 1 means update to the map"),
            new FlagDescriptorI("FL_CURRENT_HR", 1 << 2,
                                "Flag driving request for updating the DGG_Current_HR map after processing. 1 means update to the map"),
            new FlagDescriptorI("FL_CURRENT_RFI ", 1 << 3,
                                "Flag driving request for updating the DGG_Current_RFI map after processing. 1 means update to the map"),
            new FlagDescriptorI("FL_CURRENT_FLOOD", 1 << 4,
                                "Flag driving request for updating the DGG_Current_Flood map after processing. It is a place holder. No Algorithm has been defined yet. 1 means update to the map"),
    };

    @SuppressWarnings({"PointlessBitwiseExpression"})
    public static final FlagDescriptor[] L1C_FLAGS = {
            new FlagDescriptorI("POL_FLAG_1", 1 << 0, ""),
            new FlagDescriptorI("POL_FLAG_2", 1 << 1, ""),
            new FlagDescriptorI("SUN_FOV", 1 << 2,
                                "Direct  Sun  correction  has  been  performed  during  image reconstruction of this pixel"),
            new FlagDescriptorI("SUN_GLINT_FOV", 1 << 3,
                                "Reflected  Sun  correction  has  been  performed  during  image reconstruction of this pixel"),
            new FlagDescriptorI("MOON_GLINT_FOV", 1 << 4,
                                "Direct  Moon  correction  has  been  performed  during  image reconstruction of this pixel"),
            new FlagDescriptorI("SINGLE_SNAPSHOT", 1 << 5, ""),
            new FlagDescriptorI("FTT", 1 << 6,
                                "Flat Target  Transformation  has  been  performed  during  image reconstruction of this pixel"),
            new FlagDescriptorI("SUN_POINT", 1 << 7,
                                "Pixel is located in a zone where a Sun alias was reconstructed (after Sun removal, measurement may be degraded)"),
            new FlagDescriptorI("SUN_GLINT_AREA", 1 << 8,
                                "Pixel is located in a zone where Sun reflection has been detected"),
            new FlagDescriptorI("MOON_POINT", 1 << 9,
                                "Pixel  is  located  in a zone where a Moon alias was reconstructed (after Moon removal, measurement may be degraded)"),
            new FlagDescriptorI("AF_FOV", 1 << 10,
                                "Pixel is inside the exclusive zone of Alias free (delimited by the six aliased unit circles)"),
            new FlagDescriptorI("EAF_FOV", 1 << 11,
                                "Pixel is inside the Extended Alias free zone (obtained after removing sky aliases)"),
            new FlagDescriptorI("BORDER_FOV", 1 << 12,
                                "Pixel is close to the border delimiting the Extended Alias free zone"),
            new FlagDescriptorI("SUN_TAILS", 1 << 13,
                                "Pixel is located in the hexagonal alias directions centred on a Sun alias (if Sun is not removed, measurement may be degraded in these directions)"),
            new FlagDescriptorI("RFI", 1 << 14,
                                "Pixel is affected by RFI effects (as identified in static ADF file)"),
    };

    private static class FlagDescriptorI implements FlagDescriptor {

        private final String flagName;
        private final int mask;
        private final String description;

        private FlagDescriptorI(String flagName, int mask, String description) {
            this.flagName = flagName;
            this.mask = mask;
            this.description = description;
        }

        @Override
        public final String getFlagName() {
            return flagName;
        }

        @Override
        public final int getMask() {
            return mask;
        }

        @Override
        public boolean isVisible() {
            return false;
        }

        @Override
        public final Color getColor() {
            return null;
        }

        @Override
        public final double getTransparency() {
            return 0.5;
        }

        @Override
        public final String getDescription() {
            return description;
        }
    }
}
