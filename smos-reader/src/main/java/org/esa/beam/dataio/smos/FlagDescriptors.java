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
