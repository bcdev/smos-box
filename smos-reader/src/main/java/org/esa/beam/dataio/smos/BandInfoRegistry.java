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
 * @since BEAM 4.6
 */
public class BandInfoRegistry {

    private static final BandInfoRegistry uniqueInstance = new BandInfoRegistry();

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
        // TODO: no-data values
        registerBandInfo("Grid_Point_ID", "", 0.0, 1.0, 0, 1, 2621442,
                         "Unique identifier of Earth fixed grid point.");
        registerBandInfo("Grid_Point_Latitude", "deg", 0.0, 1.0, Double.NaN, -90.0, 90.0,
                         "Latitude of DGG point.");
        registerBandInfo("Grid_Point_Longitude", "deg", 0.0, 1.0, Double.NaN, -180.0, 180.0,
                         "Longitude of DGG point.");
        registerBandInfo("Grid_Point_Altitude", "mm", 0.0, 1.0, Double.NaN, -10.0E6, 10.0E6,
                         "Altitude of DGG point.");
        // TODO: <unsignedByte-8 varName="Water_Fraction"/>

        // TODO: no-data values & typical ranges
        registerBandInfo("Flags", "", 0.0, 1.0, null, 0, 1 << 16 - 1,
                         "L1c flags applicable to the pixel for this " +
                         "particular integration time.");
        registerBandInfo("BT_Value", "K", 0.0, 1.0, Double.NaN, 50.0, 350.0,
                         "Brightness temperature measurement over current " +
                         "Earth fixed grid point, obtained by DFT " +
                         "interpolation from L1b data.");
        registerBandInfo("BT_Value_Real", "K", 0.0, 1.0, Double.NaN, 50.0, 350.0,
                         "Real component of HH, HV or VV polarisation brightness " +
                         "temperature measurement over current " +
                         "Earth fixed grid point, obtained by DFT " +
                         "interpolation from L1b data.");
        registerBandInfo("BT_Value_Imag", "K", 0.0, 1.0, Double.NaN, -10.0, 10.0,
                         "Imaginary component of HH, HV or VV polarisation brightness " +
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

        /*
         * Soil moisture retrieval results.
         */
        // TODO: Mean_Acq_Time
        registerBandInfo("Soil_Moisture", "m3m3", 0.0, 1.0, -999.0, 0.0, 0.6,
                         "Retrieved soil moisture value.");
        registerBandInfo("Soil_Moisture_DQX", "m3m3", 0.0, 1.0, -999.0, 0.0, 1.2,
                         "DQX for soil moisture.");
        registerBandInfo("Optical_Thickness_Nad", "Np", 0.0, 1.0, -999.0, 0.0, 3.0,
                         "Nadir optical thickness estimate for vegetation layer.");
        registerBandInfo("Optical_Thickness_Nad_DQX", "Np", 0.0, 1.0, -999.0, 0.0, 3.0,
                         "DQX for nadir optical thickness.");
        registerBandInfo("Surface_Temperature", "K", 0.0, 1.0, -999.0, 230.0, 350.0,
                         "Surface temperature - may be a retrieved value " +
                         "or from an external source.");
        registerBandInfo("Surface_Temperature_DQX", "K", 0.0, 1.0, -999.0, 0.0, 50.0,
                         "DQX for surface temperature.");
        registerBandInfo("TTH", "", 0.0, 1.0, -999.0, 0.1, 15.0,
                         "Optical thickness coefficient for polarisation H.");
        registerBandInfo("TTH_DQX", "", 0.0, 1.0, -999.0, 0.0, 30.0,
                         "DQX for optical thickness coefficient for polarisation H.");
        registerBandInfo("RTT", "", 0.0, 1.0, -999.0, 0.05, 20.0,
                         "Ratio of optical thickness coefficients TTH/TTV.");
        registerBandInfo("RTT_DQX", "", 0.0, 1.0, -999.0, 0.0, 40.0,
                         "DQX for atio of optical thickness coefficients TTH/TTV.");
        registerBandInfo("Scattering_Albedo_H", "", 0.0, 1.0, -999.0, 0.0, 0.2,
                         "Scattering albedo for horizontal polarisation.");
        registerBandInfo("Scattering_Albedo_H_DQX", "", 0.0, 1.0, -999.0, 0.0, 1.0,
                         "DQX for scattering albedo for horizontal polarisation.");
        registerBandInfo("DIFF_Albedos", "", 0.0, 1.0, -999.0, -0.2, 0.2,
                         "Difference of albedos H-V.");
        registerBandInfo("DIFF_Albedos_DQX", "", 0.0, 1.0, -999.0, 0.0, 1.0,
                         "DQX for difference of albedos H-V.");
        registerBandInfo("Roughness_Param", "K", 0.0, 1.0, -999.0, 0.0, 5.0,
                         "Roughness parameter estimate.");
        registerBandInfo("Roughness_Param_DQX", "K", 0.0, 1.0, -999.0, 0.0, 10.0,
                         "DQX for roughness parameter estimate.");
        registerBandInfo("Dielect_Const_MD_RE", "Fm-1", 0.0, 1.0, -999.0, 0.0, 160.0,
                         "Real part of the dielectric constant from MD " +
                         "retrieval.");
        registerBandInfo("Dielect_Const_MD_RE_DQX", "Fm-1", 0.0, 1.0, -999.0, 0.0, 160.0,
                         "DQX for real part of the dielectric constant from MD " +
                         "retrieval.");
        registerBandInfo("Dielect_Const_MD_IM", "Fm-1", 0.0, 1.0, -999.0, -50.0, 50.0,
                         "Imaginary part of the dielectric constant from MD " +
                         "retrieval.");
        registerBandInfo("Dielect_Const_MD_IM_DQX", "Fm-1", 0.0, 1.0, -999.0, 0.0, 100.0,
                         "DQX for imaginary part of the dielectric constant from MD " +
                         "retrieval.");
        registerBandInfo("Dielect_Const_Non_MD_RE", "Fm-1", 0.0, 1.0, -999.0, 0.0, 160.0,
                         "Real part of dielectric constant from retrieval " +
                         "models other than MD.");
        registerBandInfo("Dielect_Const_Non_MD_RE_DQX", "Fm-1", 0.0, 1.0, -999.0, 0.0, 160.0,
                         "DQX for real part of dielectric constant from retrieval " +
                         "models other than MD.");
        registerBandInfo("Dielect_Const_Non_MD_IM", "Fm-1", 0.0, 1.0, -999.0, -50.0, 50.0,
                         "Imaginary part of dielectric constant from retrieval " +
                         "models other than MD.");
        registerBandInfo("Dielect_Const_Non_MD_IM_DQX", "Fm-1", 0.0, 1.0, -999.0, 0.0, 100.0,
                         "DQX for imaginary part of dielectric constant from retrieval " +
                         "models other than MD.");
        registerBandInfo("TB_ASL_Theta_B_H", "K", 0.0, 1.0, -999.0, 50.0, 350.0,
                         "Surface level TB (corrected from sky/atmosphere " +
                         "contribution) computed from forward model with " +
                         "a specific incidence angle of 42.5°, and for H " +
                         "polarisation.");
        registerBandInfo("TB_ASL_Theta_B_H_DQX", "K", 0.0, 1.0, -999.0, 0.0, 50.0,
                         "DQX for surface level TB for H polarisation.");
        registerBandInfo("TB_ASL_Theta_B_V", "K", 0.0, 1.0, -999.0, 50.0, 350.0,
                         "Surface level TB (corrected from sky/atmosphere " +
                         "contribution) computed from forward model with " +
                         "a specific incidence angle of 42.5°, and for V " +
                         "polarisation.");
        registerBandInfo("TB_ASL_Theta_B_V_DQX", "K", 0.0, 1.0, -999.0, 0.0, 50.0,
                         "DQX for surface level TB for V polarisation.");
        registerBandInfo("TB_TOA_Theta_B_H", "K", 0.0, 1.0, -999.0, 50.0, 350.0,
                         "Top of the atmosphere TB computed from " +
                         "forward model at specific incidence angle of " +
                         "42.5°, for H polarisation.");
        registerBandInfo("TB_TOA_Theta_B_H_DQX", "K", 0.0, 1.0, -999.0, 0.0, 50.0,
                         "DQX for top of the atmosphere TB for H polarisation.");
        registerBandInfo("TB_TOA_Theta_B_V", "K", 0.0, 1.0, -999.0, 50.0, 350.0,
                         "Top of the atmosphere TB computed from " +
                         "forward model at specific incidence angle of " +
                         "42.5°, for V polarisation.");
        registerBandInfo("TB_TOA_Theta_B_V_DQX", "K", 0.0, 1.0, -999.0, 0.0, 50.0,
                         "DQX for top of the atmosphere TB for V polarisation.");
        /*
         * Soil moisture confidence descriptors.
         */
        registerBandInfo("Confidence_Flags", "", 0.0, 1.0, null, 0, 65535,
                         "Confidence flags.");
        registerBandInfo("GQX", "", 0.0, 1.0, 0, 0, 200,
                         "Global Quality Index.");
        registerBandInfo("Chi_2", "", 0.0, 1.0, 0, 0, 20,
                         "Retrieval fit quality index.");
        registerBandInfo("Chi_2_P", "", 0.0, 1.0 / ((1 << 8) - 1), 0.0, 0.0, 1.0,
                         "Chi square high value acceptability probability.");
        registerBandInfo("N_Wild", "", 0.0, 1.0, 0, 0, 255,
                         "Number of times that wild data occurred.");
        registerBandInfo("M_AVA0", "", 0.0, 1.0, 0, 0, 255,
                         "Initial number of TB measurements available in L1c.");
        registerBandInfo("M_AVA", "", 0.0, 1.0, 0, 0, 255,
                         "Preprocessing - number of TB measurements " +
                         "available for retrieval.");
        registerBandInfo("AFP", "Km", 0.0, 1.0, 0.0, 0.0, 55.0,
                         "Mean Surface of the antenna Footprint ellipses " +
                         "on Earth.");
        registerBandInfo("N_AF_FOV", "", 0.0, 0.0, 0, 0, 255,
                         "Counter view - number of views that flag is set off " +
                         "to AF_FOV_flag.");
        registerBandInfo("N_Sun_Tails", "", 0.0, 1.0, 0, 0, 255,
                         "Counter view - number of views that flag is set on " +
                         "to Sun_Tails flag.");
        registerBandInfo("N_Sun_Glint_Area", "", 0.0, 1.0, 0, 0, 255,
                         "Counter view - number of views that flag is set on " +
                         "to Sun_Glint_Area flag.");
        registerBandInfo("N_Sun_FOV", "", 0.0, 1.0, 0, 0, 255,
                         "Counter view - number of views that flag is set on " +
                         "Sun_FOV flag.");
        registerBandInfo("N_Instrument_Error", "", 0.0, 1.0, 0, 0, 255,
                         "This counts the number of TBs that pass the " +
                         "initial TB filtering and have the L1c " +
                         "Instrument_Error_Flag ON");
        registerBandInfo("N_Software_Error", "", 0.0, 1.0, 0, 0, 255,
                         "This counts the number of TBs that pass the " +
                         "initial TB filtering and have the L1c " +
                         "Software_Error_flag ON.");
        registerBandInfo("N_ADF_Error", "", 0.0, 1.0, 0, 0, 255,
                         "This counts the number of TBs that pass the " +
                         "initial TB filtering and have the L1c " +
                         "ADF_Error_flag on.");
        registerBandInfo("N_Calibration_Error", "", 0.0, 1.0, 0, 0, 255,
                         "This counts the number of TBs that pass the " +
                         "initial TB filtering and have the L1c " +
                         "Calibration_Error_flag ON");
        registerBandInfo("N_X_Band", "", 0.0, 1.0, 0, 0, 255,
                         "This counts the number of TBs that pass the " +
                         "initial TB fitering and have L1c X-Band ON.");
        /*
        * Soil moisture science descriptors.
        */
        registerBandInfo("Science_Flags", "", 0.0, 1.0, null, 0, 4294967295L,
                         "Science flags.");
        registerBandInfo("N_Sky", "", 0.0, 1.0, 0, 0, 255,
                         "Strong Galactic Source.");
        /*
         * Soil moisture processing descriptors.
         */
        registerBandInfo("Processing_Flags", "", 0.0, 1.0, null, 0, 65535,
                         "Processing flags.");
        registerBandInfo("S_Tree_1", "", 0.0, 1.0, 0, 0, 20,
                         "Branches of decision tree stage 1.");
        registerBandInfo("S_Tree_2", "", 0.0, 1.0, 0, 0, 255,
                         "Retrieval R2, R3 or R4.");

        /*
         * Soil moisture DGG current data.
         */
        registerBandInfo("DGG_Current_Flags", "", 0.0, 1.0, 0, 0, 255,
                         "DGG current flags.");
        registerBandInfo("Tau_Cur_DQX", "", 0.0, 1.0, -999.0, 0.0, 20.0,
                         "This is a special tau DQX value computed using " +
                         "a special sigma corresponding to the case where " +
                         "tau nad is completely free. This sigma is the " +
                         "parameter Current_TAU_NADIR_ASTD in the " +
                         "L2SM Configuration Parameters Products.");
        registerBandInfo("HR_Cur_DQX", "", 0.0, 1.0, -999.0, 0.0, 20.0,
                         "This is a special HR DQX value computed using " +
                         "a special sigma corresponding to the case where " +
                         "HR is completely free. This sigma is the " +
                         "parameter Current_HR_ASTD in the L2SM " +
                         "Configuration Parameters Product.");
        registerBandInfo("N_RFI_X", "", 0.0, 1.0, 0, 0, 255,
                         "Count of deleted TBs due to suspected RFI.");
        registerBandInfo("N_RFI_Y", "", 0.0, 1.0, 0, 0, 255,
                         "Count of deleted TBs due to suspected RFI.");

        /*
         * Ocean salinity geophysical parameters.
         */
        registerBandInfo("Equiv_ftprt_diam", "m", 0.0, 1.0, -9999.0, 30.0, 90.0,
                         "Equivalent Footprint diameter.");
        registerBandInfo("Mean_acq_time", "dd", 0.0, 1.0, -9999.0, 2610.0, 2615.0,
                         "Mean acquisition time.");
        registerBandInfo("SSS1", "psu", 0.0, 1.0, -9999.0, 0.0, 40.0,
                         "Sea surface salinity using roughness model 1.");
        registerBandInfo("Sigma_SSS1", "psu", 0.0, 1.0, -9999.0, 0.1, 1.5,
                         "Theoretical uncertainty computed for SSS1.");
        registerBandInfo("SSS2", "psu", 0.0, 1.0, -9999.0, 0.0, 40.0,
                         "Sea surface salinity using roughness model 2.");
        registerBandInfo("Sigma_SSS2", "psu", 0.0, 1.0, -9999.0, 0.1, 1.5,
                         "Theoretical uncertainty computed for SSS2.");
        registerBandInfo("SSS3", "psu", 0.0, 1.0, -9999.0, 0.0, 40.0,
                         "Sea surface salinity using roughness model 3.");
        registerBandInfo("Sigma_SSS3", "psu", 0.0, 1.0, -9999.0, 0.1, 1.5,
                         "Theoretical uncertainty computed for SSS3.");
        registerBandInfo("A_card", "", 0.0, 1.0, -9999.0, 0.1, 70.0,
                         "Effective_Acard retrieved with minimalist model.");
        registerBandInfo("Sigma_Acard", "", 0.0, 1.0, -9999.0, 0.1, 1.0,
                         "Theoretical uncertainty computed for Acard.");
        registerBandInfo("WS", "m s-1", 0.0, 1.0, -9999.0, 0.0, 30.0,
                         "Equivalent  neutral  wind  speed  as  derived from " +
                         "ECMWF.");
        registerBandInfo("Sigma_WS", "m s-1", 0.0, 1.0, -9999.0, 0.1, 1.0,
                         "Theoretical uncertainty associated with WS.");
        registerBandInfo("SST", "K", 0.0, 1.0, -9999.0, 273.0, 293.0,
                         "Sea  Surface  Temperature  as  derived  from ECMWF.");
        registerBandInfo("Sigma_SST", "K", 0.0, 1.0, -9999.0, 0.1, 1.0,
                         "Theoretical uncertainty associated with SST.");
        registerBandInfo("Tb_42.5H", "K", 0.0, 1.0, -9999.0, 70.0, 130.0,
                         "Brightness Temperature at surface level derived " +
                         "with default forward model and retrieved " +
                         "geophysical parameters, H polarisation direction.");
        registerBandInfo("Sigma_Tb_42.5H", "K", 0.0, 1.0, -9999.0, 0.1, 0.8,
                         "Theoretical uncertainty computed for Tb42.5H.");
        registerBandInfo("Tb_42.5V", "K", 0.0, 1.0, -9999.0, 70.0, 130.0,
                         "Brightness Temperature at surface level derived " +
                         "with default forward model and retrieved " +
                         "geophysical parameters, V polarisation direction.");
        registerBandInfo("Sigma_Tb_42.5V", "K", 0.0, 1.0, -9999.0, 0.1, 0.8,
                         "Theoretical uncertainty computed for Tb42.5V.");
        registerBandInfo("Tb_42.5X", "K", 0.0, 1.0, -9999.0, 70.0, 130.0,
                         "Brightness Temperature at antenna level derived with " +
                         "default forward model and retrieved geophysical " +
                         "parameters, X polarisation direction.");
        registerBandInfo("Sigma_Tb_42.5X", "K", 0.0, 1.0, -9999.0, 0.1, 0.8,
                         "Theoretical uncertainty computed for Tb42.5X.");
        registerBandInfo("Tb_42.5Y", "K", 0.0, 1.0, -9999.0, 70.0, 130.0,
                         "Brightness Temperature at antenna level derived with " +
                         "default forward model and retrieved geophysical " +
                         "parameters, Y polarisation direction.");
        registerBandInfo("Sigma_Tb_42.5Y", "K", 0.0, 1.0, -9999.0, 0.1, 0.8,
                         "Theoretical uncertainty computed for Tb42.5Y.");

        registerBandInfo("Control_Flags_1", "", 0.0, 1.0, 2147483647, 0, 2 << 23 - 1,
                         "Control Flags for SSS retrieval with forward model 1.");
        registerBandInfo("Control_Flags_2", "", 0.0, 1.0, 2147483647, 0, 2 << 23 - 1,
                         "Control Flags for SSS retrieval with forward model 2.");
        registerBandInfo("Control_Flags_3", "", 0.0, 1.0, 2147483647, 0, 2 << 23 - 1,
                         "Control Flags for SSS retrieval with forward model 3.");
        registerBandInfo("Control_Flags_4", "", 0.0, 1.0, 2147483647, 0, 2 << 23 - 1,
                         "Control Flags for SSS retrieval with forward model 4.");

        /*
         * Ocean salinity confidence descriptors.
         */
        registerBandInfo("Dg_chi2_1", "", 0.0, 1.0, 0.0, 1.0, 1000.0,
                         "Retrieval fit quality index with forward model 1.");
        registerBandInfo("Dg_chi2_2", "", 0.0, 1.0, 0.0, 1.0, 1000.0,
                         "Retrieval fit quality index with forward model 2.");
        registerBandInfo("Dg_chi2_3", "", 0.0, 1.0, 0.0, 1.0, 1000.0,
                         "Retrieval fit quality index with forward model 3.");
        registerBandInfo("Dg_chi2_Acard", "", 0.0, 1.0, 0.0, 1.0, 1000.0,
                         "Retrieval fit quality index with cardioid model.");
        registerBandInfo("Dg_chi2_P_1", "", 0.0, 1.0, 0.0, 1.0, 1000.0,
                         "Normalised chi2 high value acceptability probability " +
                         "with forward model 1, scaled by multiplying by 1000.");
        registerBandInfo("Dg_chi2_P_2", "", 0.0, 1.0, 0.0, 1.0, 1000.0,
                         "Normalised chi2 high value acceptability probability " +
                         "with forward model 2, scaled by multiplying by 1000.");
        registerBandInfo("Dg_chi2_P_3", "", 0.0, 1.0, 0.0, 1.0, 1000.0,
                         "Normalised chi2 high value acceptability probability " +
                         "with forward model 3, scaled by multiplying by 1000.");
        registerBandInfo("Dg_chi2_P_Acard", "", 0.0, 1.0, 0.0, 1.0, 1000.0,
                         "Normalised chi2 high value acceptability probability " +
                         "with cardioid, scaled by multiplying by 1000.");
        registerBandInfo("Dg_quality_SSS_1", "", 0.0, 1.0, 0.0, 20.0, 15000.0,
                         "Quality index for SSS1.");
        registerBandInfo("Dg_quality_SSS_2", "", 0.0, 1.0, 0.0, 20.0, 15000.0,
                         "Quality index for SSS2.");
        registerBandInfo("Dg_quality_SSS_3", "", 0.0, 1.0, 0.0, 20.0, 15000.0,
                         "Quality index for SSS3.");
        registerBandInfo("Dg_quality_Acard", "", 0.0, 1.0, 0.0, 20.0, 15000.0,
                         "Quality Index for Acard.");
        registerBandInfo("Dg_num_iter_1", "", 0.0, 1.0, 0.0, 1.0, 21.0,
                         "Number of iterations for the retrieval of SSS with " +
                         "forward model 1.");
        registerBandInfo("Dg_num_iter_2", "", 0.0, 1.0, 0.0, 1.0, 21.0,
                         "Number of iterations for the retrieval of SSS with " +
                         "forward model 2.");
        registerBandInfo("Dg_num_iter_3", "", 0.0, 1.0, 0.0, 1.0, 21.0,
                         "Number of iterations for the retrieval of SSS with " +
                         "forward model 3.");
        registerBandInfo("Dg_num_iter_4", "", 0.0, 1.0, 0.0, 1.0, 21.0,
                         "Number of iterations for the retrieval of SSS with " +
                         "cardioid model.");
        registerBandInfo("Dg_num_meas_l1c", "", 0.0, 1.0, 0.0, 1.0, 300.0,
                         "Number of  measurements available in  L1c product");
        registerBandInfo("Dg_num_meas_valid", "", 0.0, 1.0, 0.0, 1.0, 300.0,
                         "Number of valid measurement available for SSS " +
                         "retrieval.");
        registerBandInfo("Dg_border_fov", "", 0.0, 1.0, 0.0, 1.0, 300.0,
                         "Number of valid measurements with BORDER_FOV " +
                         "flag raised.");
        registerBandInfo("Dg_eaf_fov", "", 0.0, 1.0, 0.0, 1.0, 300.0,
                         "Number of valid measurements with EAF_FOV flag " +
                         "raised.");
        registerBandInfo("Dg_af_fov", "", 0.0, 1.0, 0.0, 1.0, 300.0,
                         "Number of valid measurements with AF_FOV flag " +
                         "raised.");
        registerBandInfo("Dg_sun_tails", "", 0.0, 1.0, 0.0, 1.0, 300.0,
                         "Number of measurements with SUN_TAILS flag " +
                         "raised.");
        registerBandInfo("Dg_sun_glint_area", "", 0.0, 1.0, 0.0, 1.0, 300.0,
                         "Number of measurements with SUN_GLINT_AREA " +
                         "flag raised.");
        registerBandInfo("Dg_sun_glint_fov", "", 0.0, 1.0, 0.0, 1.0, 300.0,
                         "Number of measurements with SUN_GLINT_FOV flag " +
                         "raised.");
        registerBandInfo("Dg_sun_fov", "", 0.0, 1.0, 0.0, 1.0, 300.0,
                         "Number of measurements with SUN_FOV flag raised.");
        registerBandInfo("Dg_sun_glint_L2", "", 0.0, 1.0, 0.0, 1.0, 300.0,
                         "Number of  measurements with  L2 sunglint flag " +
                         "raised.");
        registerBandInfo("Dg_Suspect_ice", "", 0.0, 1.0, 0.0, 1.0, 300.0,
                         "Number of suspected ice contaminated measurements.");
        registerBandInfo("Dg_galactic_Noise_Error", "", 0.0, 1.0, 0.0, 1.0, 300.0,
                         "Number of measurements discarded due to errors in \n" +
                         "galactic noise.");
        registerBandInfo("Dg_galactic_Noise_Pol", "", 0.0, 1.0, 0.0, 1.0, 300.0,
                         "Number of measurements with Fm_gal_noise_pol flag " +
                         "raised.");
        registerBandInfo("Dg_moonglint", "", 0.0, 1.0, 0.0, 1.0, 300.0,
                         "Number of measurements with L2 moonglint raised.");

        registerBandInfo("Science_Flags_1", "", 0.0, 1.0, 0.0, 0, 2097152,
                         "Science flags for SSS retrieval with forward model 1.");
        registerBandInfo("Science_Flags_2", "", 0.0, 1.0, 0.0, 0, 2097152,
                         "Science flags for SSS retrieval with forward model 12.");
        registerBandInfo("Science_Flags_3", "", 0.0, 1.0, 0.0, 0, 2097152,
                         "Science flags for SSS retrieval with forward model 3.");
        registerBandInfo("Science_Flags_4", "", 0.0, 1.0, 0.0, 0, 2097152,
                         "Science flags for SSS retrieval with cardioid model.");

        registerBandInfo("Dg_sky", "", 0.0, 1.0, 0.0, 1.0, 300.0,
                         "Count measurements with specular direction toward a " +
                         "strong galactic source.");
    }

    public static BandInfoRegistry getInstance() {
        return uniqueInstance;
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
}
