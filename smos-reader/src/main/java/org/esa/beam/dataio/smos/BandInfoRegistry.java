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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Band info registry.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 * @since SMOS-Box 1.0
 */
@Deprecated
public class BandInfoRegistry {

    private final ConcurrentMap<String, BandInfo> bandInfoMap;

    /* For registering a new band, call:
     *
     * registerBandInfo(String description,
     *                  String unit,
     *                  double scaleOffset,
     *                  double scaleFactor,
     *                  double noDataValue,
     *                  double typicalMinimumValue,
     *                  double typicalMaximumValue);
     */
    private BandInfoRegistry() {
        bandInfoMap = new ConcurrentHashMap<String, BandInfo>(17);

        /*
         * Level 1C
         */
        registerBandInfo("Grid_Point_ID", "", 0.0, 1.0, 0, 0, 9262145,
                         "Unique identifier of Earth fixed grid point.");
        registerBandInfo("Grid_Point_Latitude", "deg", 0.0, 1.0, -999.0, -90.0, 90.0,
                         "Latitude of DGG point.");
        registerBandInfo("Grid_Point_Longitude", "deg", 0.0, 1.0, -999.0, -180.0, 180.0,
                         "Longitude of DGG point.");
        registerBandInfo("Grid_Point_Altitude", "m", 0.0, 1.0, -999.0, -300.0, 9000.0,
                         "Altitude of DGG point.");
        // TODO: <unsignedByte-8 varName="Water_Fraction"/>

        // TODO: no-data values & typical ranges
        registerBandInfo("Flags", "", 0.0, 1.0, 0, 0, 65535,
                         "L1c flags applicable to the pixel for this " +
                         "particular integration time.");
        registerBandInfo("BT_Value", "K", 0.0, 1.0, -999.0, 0.0, 350.0,
                         "Brightness temperature measurement over current " +
                         "Earth fixed grid point, obtained by DFT " +
                         "interpolation from L1b data.");
        registerBandInfo("BT_Value_Real", "K", 0.0, 1.0, -999.0, 0.0, 350.0,
                         "Real component of XX, XY or YY polarisation brightness " +
                         "temperature measurement over current " +
                         "Earth fixed grid point, obtained by DFT " +
                         "interpolation from L1b data.");
        registerBandInfo("BT_Value_Imag", "K", 0.0, 1.0, -999.0, -20.0, 20.0,
                         "Imaginary component of XX, XY or YY polarisation brightness " +
                         "temperature measurement over current " +
                         "Earth fixed grid point, obtained by DFT " +
                         "interpolation from L1b data.");
        registerBandInfo("Pixel_Radiometric_Accuracy", "K", 0.0, 50.0 / (1 << 16), 0, 0.0, 5.0,
                         "Error accuracy measurement in the Brightness " +
                         "Temperature presented in the previous field, " +
                         "extracted in the direction of the pixel.");
        registerBandInfo("Incidence_Angle", "deg", 0.0, 90.0 / (1 << 16), 0, 0.0, 90.0,
                         "Incidence angle value corresponding to the " +
                         "measured BT value over current Earth fixed " +
                         "grid point. Measured as angle from pixel to " +
                         "S/C with respect to the pixel local normal (0° " +
                         "if vertical)");
        registerBandInfo("Azimuth_Angle", "deg", 0.0, 360.0 / (1 << 16), 0, 0.0, 360.0,
                         "Azimuth angle value corresponding to the " +
                         "measured BT value over current Earth fixed " +
                         "grid point. Measured as angle in pixel local " +
                         "tangent plane from projected pixel to S/C " +
                         "direction with respect to the local North (0° if" +
                         "local North)", true);
        registerBandInfo("Faraday_Rotation_Angle", "deg", 0.0, 360.0 / (1 << 16), 0, 0.0, 360.0,
                         "Faraday rotation angle value corresponding " +
                         "to the measured BT value over current Earth " +
                         "fixed grid point. It is computed as the rotation " +
                         "from antenna to surface (i.e. inverse angle)", true);
        registerBandInfo("Geometric_Rotation_Angle", "deg", 0.0, 360.0 / (1 << 16), 0, 0.0, 360.0,
                         "Geometric rotation angle value " +
                         "corresponding to the measured BT value " +
                         "over current Earth fixed grid point. It is " +
                         "computed as the rotation from surface to " +
                         "antenna (i.e. direct angle).", true);
        registerBandInfo("Footprint_Axis1", "km", 0.0, 100.0 / (1 << 16), 0, 20.0, 35.0,
                         "Elliptical footprint major semi-axis value.");
        registerBandInfo("Footprint_Axis2", "km", 0.0, 100.0 / (1 << 16), 0, 20.0, 35.0,
                         "Elliptical footprint minor semi-axis value.");
    }

    public static BandInfoRegistry getInstance() {
        return Holder.instance;
    }

    public BandInfo getBandInfo(String name) {
        return bandInfoMap.get(name);
    }

    private void registerBandInfo(String name, String unit,
                                  double scaleOffset,
                                  double scaleFactor,
                                  Number noDataValue,
                                  double min,
                                  double max,
                                  String description) {
        registerBandInfo(name, unit, scaleOffset, scaleFactor, noDataValue, min, max, description, false);
    }

    private void registerBandInfo(String name, String unit,
                                  double scaleOffset,
                                  double scaleFactor,
                                  Number noDataValue,
                                  double min,
                                  double max,
                                  String description,
                                  boolean topologyCircular) {
        registerBandInfo(name, new BandInfo(name, unit, scaleOffset, scaleFactor, noDataValue, min, max, description,
                                            topologyCircular));
    }

    private void registerBandInfo(String name, BandInfo bandInfo) {
        bandInfoMap.putIfAbsent(name, bandInfo);
    }

    // Initialization on demand holder idiom
    private static class Holder {

        private static final BandInfoRegistry instance = new BandInfoRegistry();
    }
}
