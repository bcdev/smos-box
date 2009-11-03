/*
 * $Id: $
 *
 * Copyright (C) 2008 by Brockmann Consult (info@brockmann-consult.de)
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

import com.bc.ceres.binio.CompoundMember;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.SimpleType;
import com.bc.ceres.binio.Type;
import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import com.bc.jexp.ParseException;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.BitmaskDef;
import org.esa.beam.framework.datamodel.ColorPaletteDef;
import org.esa.beam.framework.datamodel.FlagCoding;
import org.esa.beam.framework.datamodel.ImageInfo;
import org.esa.beam.framework.datamodel.MapGeoCoding;
import org.esa.beam.framework.datamodel.MetadataAttribute;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.datamodel.VirtualBand;
import org.esa.beam.framework.dataop.barithm.BandArithmetic;
import org.esa.beam.framework.dataop.maptransf.MapInfo;
import org.esa.beam.smos.dgg.SmosDgg;
import org.esa.beam.util.io.FileUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

@Deprecated
public class ProductFactory {

    protected void addBands(Product product, ExplorerFile smosFile) {
        final String formatName = smosFile.getFormat().getName();

        addGridPointSequentialNumberBand(product);
        if (formatName.contains("MIR_BWLD1C")
            || formatName.contains("MIR_BWND1C")
            || formatName.contains("MIR_BWSD1C")) {
            addL1cFlagCoding(product);
            addDualPolBrowseBands(product, ((L1cSmosFile) smosFile).getBtDataType(), smosFile);
        } else if (formatName.contains("MIR_BWLF1C")
                   || formatName.contains("MIR_BWNF1C")
                   || formatName.contains("MIR_BWSF1C")) {
            addL1cFlagCoding(product);
            addFullPolBrowseBands(product, ((L1cSmosFile) smosFile).getBtDataType(), (L1cSmosFile) smosFile);
        } else if (formatName.contains("MIR_SCLD1C")
                   || formatName.contains("MIR_SCSD1C")) {
            addL1cFlagCoding(product);
            addDualPolScienceBands(product, ((L1cSmosFile) smosFile).getBtDataType(), (L1cScienceSmosFile) smosFile);
        } else if (formatName.contains("MIR_SCLF1C")
                   || formatName.contains("MIR_SCSF1C")) {
            addL1cFlagCoding(product);
            addFullPolScienceBands(product, ((L1cSmosFile) smosFile).getBtDataType(), (L1cScienceSmosFile) smosFile);
        } else if (formatName.contains("MIR_OSDAP2")) {
            // todo: rq/rq flag codings
            final CompoundType type = ((SmosFile) smosFile).getGridPointType();
            // todo: rq/rq add bands
            System.out.println("count = " + type.getMemberCount());
        } else if (formatName.contains("MIR_SMDAP2")) {
            // todo: rq/rq flag codings
            final CompoundType type = ((SmosFile) smosFile).getGridPointType();
            // todo: rq/rq add bands
            System.out.println("count = " + type.getMemberCount());
        } else {
            throw new IllegalStateException("Illegal SMOS format: " + formatName);
        }
    }

    protected void setQuicklookBandName(Product product) {
        // set quicklook band name to first BT band
        for (Band band : product.getBands()) {
            if ("K".equals(band.getUnit())) {
                product.setQuicklookBandName(band.getName());
                break;
            }
        }
    }

    public static boolean is_OSUDP_File(String formatName) {
        return formatName.contains("MIR_OSUDP2");
    }

    public static boolean is_SMUDP_File(String formatName) {
        return formatName.contains("MIR_SMUDP2");
    }

    public static boolean is_L2_User_File(String formatName) {
        return is_OSUDP_File(formatName) || is_SMUDP_File(formatName);
    }

    private void addDualPolBrowseBands(Product product, CompoundType compoundDataType, ExplorerFile smosFile) {
        final CompoundMember[] members = compoundDataType.getMembers();
        final HashMap<String, ValueProvider> valueProviderMap = new HashMap<String, ValueProvider>();

        for (int fieldIndex = 0; fieldIndex < members.length; fieldIndex++) {
            final CompoundMember member = members[fieldIndex];
            final String memberName = member.getName();
            final BandInfo bandInfo = BandInfoRegistry.getInstance().getBandInfo(memberName);

            if (bandInfo != null) {
                if ("Flags".equals(memberName)) {
                    // flags do not depend on polarisation mode, so there is a single flag band only
                    addL1cBand(product, memberName,
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_X, valueProviderMap, smosFile);
                } else {
                    addL1cBand(product, memberName + "_X",
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_X, valueProviderMap, smosFile);
                    addL1cBand(product, memberName + "_Y",
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_Y, valueProviderMap, smosFile);
                }
            }
        }
    }

    private void addFullPolBrowseBands(Product product, CompoundType compoundDataType, L1cSmosFile smosFile) {
        final CompoundMember[] members = compoundDataType.getMembers();
        final HashMap<String, ValueProvider> valueProviderMap = new HashMap<String, ValueProvider>();

        for (int fieldIndex = 0; fieldIndex < members.length; fieldIndex++) {
            final CompoundMember member = members[fieldIndex];
            final String memberName = member.getName();
            final BandInfo bandInfo = BandInfoRegistry.getInstance().getBandInfo(memberName);

            if (bandInfo != null) {
                if ("Flags".equals(memberName)) {
                    // flags do not depend on polarisation mode, so there is a single flag band only
                    addL1cBand(product, memberName,
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_X, valueProviderMap, smosFile);
                } else if ("BT_Value".equals(memberName)) {
                    addL1cBand(product, memberName + "_X",
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_X, valueProviderMap, smosFile);
                    addL1cBand(product, memberName + "_Y",
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_Y, valueProviderMap, smosFile);
                    final BandInfo bandInfoCrossPol = new BandInfo(bandInfo.getName(),
                                                                   bandInfo.getUnit(),
                                                                   bandInfo.getScaleOffset(),
                                                                   bandInfo.getScaleFactor(),
                                                                   bandInfo.getNoDataValue(),
                                                                   -10.0, 10.0,
                                                                   bandInfo.getDescription());
                    addL1cBand(product, memberName + "_XY_Real",
                               memberTypeToBandType(member.getType()), bandInfoCrossPol, fieldIndex,
                               SmosConstants.L1C_POL_MODE_XY1, valueProviderMap, smosFile);
                    addL1cBand(product, memberName + "_XY_Imag",
                               memberTypeToBandType(member.getType()), bandInfoCrossPol, fieldIndex,
                               SmosConstants.L1C_POL_MODE_XY2, valueProviderMap, smosFile);
                } else {
                    addL1cBand(product, memberName + "_X",
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_X, valueProviderMap, smosFile);
                    addL1cBand(product, memberName + "_Y",
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_Y, valueProviderMap, smosFile);
                    addL1cBand(product, memberName + "_XY",
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_XY1, valueProviderMap, smosFile);
                }
            }
        }
    }

    private void addDualPolScienceBands(Product product, CompoundType compoundDataType, L1cScienceSmosFile smosFile) {
        final CompoundMember[] members = compoundDataType.getMembers();
        final HashMap<String, ValueProvider> valueProviderMap = new HashMap<String, ValueProvider>();

        for (int fieldIndex = 0; fieldIndex < members.length; fieldIndex++) {
            final CompoundMember member = members[fieldIndex];
            final String memberName = member.getName();
            final BandInfo bandInfo = BandInfoRegistry.getInstance().getBandInfo(memberName);

            if (bandInfo != null) {
                if ("Flags".equals(memberName)) {
                    // flags do not depend on polarisation mode, so there is a single flag band only
                    addL1cBand(product, memberName,
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_ANY, valueProviderMap, smosFile);
                } else {
                    addL1cBand(product, memberName + "_X",
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_X, valueProviderMap, smosFile);
                    addL1cBand(product, memberName + "_Y",
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_Y, valueProviderMap, smosFile);
                }
            }
        }

        addRotatedDualPolBands(product, valueProviderMap);
    }

    private void addFullPolScienceBands(Product product, CompoundType compoundDataType, L1cScienceSmosFile smosFile) {
        final CompoundMember[] members = compoundDataType.getMembers();
        final HashMap<String, ValueProvider> valueProviderMap = new HashMap<String, ValueProvider>();

        for (int fieldIndex = 0; fieldIndex < members.length; fieldIndex++) {
            final CompoundMember member = members[fieldIndex];
            final String memberName = member.getName();
            final BandInfo bandInfo = BandInfoRegistry.getInstance().getBandInfo(memberName);

            if (bandInfo != null) {
                if ("Flags".equals(memberName)) {
                    // flags do not depend on polarisation mode, so there is a single flag band only
                    addL1cBand(product, memberName,
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_ANY, valueProviderMap, smosFile);
                } else if ("BT_Value_Real".equals(memberName)) {
                    addL1cBand(product, "BT_Value_X",
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_X, valueProviderMap, smosFile);
                    addL1cBand(product, "BT_Value_Y",
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_Y, valueProviderMap, smosFile);
                    final BandInfo bandInfoCrossPol = new BandInfo(bandInfo.getName(),
                                                                   bandInfo.getUnit(),
                                                                   bandInfo.getScaleOffset(),
                                                                   bandInfo.getScaleFactor(),
                                                                   bandInfo.getNoDataValue(), -10.0, 10.0,
                                                                   bandInfo.getDescription());
                    addL1cBand(product, "BT_Value_XY_Real",
                               memberTypeToBandType(member.getType()), bandInfoCrossPol, fieldIndex,
                               SmosConstants.L1C_POL_MODE_XY1, valueProviderMap, smosFile);
                } else if ("BT_Value_Imag".equals(memberName)) {
                    addL1cBand(product, "BT_Value_XY_Imag",
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_XY1, valueProviderMap, smosFile);
                } else {
                    addL1cBand(product, memberName + "_X",
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_X, valueProviderMap, smosFile);
                    addL1cBand(product, memberName + "_Y",
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_Y, valueProviderMap, smosFile);
                    addL1cBand(product, memberName + "_XY",
                               memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                               SmosConstants.L1C_POL_MODE_XY1, valueProviderMap, smosFile);
                }
            }
        }

        addRotatedFullPolBands(product, valueProviderMap);
    }

    private void addRotatedDualPolBands(Product product, HashMap<String, ValueProvider> valueProviderMap) {
        DP provider;
        BandInfo bandInfo;

        provider = new DPH(product, valueProviderMap, false);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("BT_Value");
        addBand(product, "BT_Value_H", ProductData.TYPE_FLOAT32, bandInfo, provider);

        provider = new DPV(product, valueProviderMap, false);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("BT_Value");
        addBand(product, "BT_Value_V", ProductData.TYPE_FLOAT32, bandInfo, provider);

        provider = new DPH(product, valueProviderMap, true);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("Pixel_Radiometric_Accuracy");
        addBand(product, "Pixel_Radiometric_Accuracy_H", ProductData.TYPE_FLOAT32, bandInfo, provider);

        provider = new DPV(product, valueProviderMap, true);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("Pixel_Radiometric_Accuracy");
        addBand(product, "Pixel_Radiometric_Accuracy_V", ProductData.TYPE_FLOAT32, bandInfo, provider);

        addVirtualBand(product, "Stokes_1", "(BT_Value_H + BT_Value_V) / 2.0");
        addVirtualBand(product, "Stokes_2", "(BT_Value_H - BT_Value_V) / 2.0");
    }

    private void addRotatedFullPolBands(Product product, HashMap<String, ValueProvider> valueProviderMap) {
        FP provider;
        BandInfo bandInfo;

        provider = new FPH(product, valueProviderMap, false);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("BT_Value");
        addBand(product, "BT_Value_H", ProductData.TYPE_FLOAT32, bandInfo, provider);

        provider = new FPV(product, valueProviderMap, false);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("BT_Value");
        addBand(product, "BT_Value_V", ProductData.TYPE_FLOAT32, bandInfo, provider);

        provider = new FPR(product, valueProviderMap, false);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("BT_Value_Real");
        addBand(product, "BT_Value_HV_Real", ProductData.TYPE_FLOAT32, bandInfo, provider);

        provider = new FPI(product, valueProviderMap, false);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("BT_Value_Imag");
        addBand(product, "BT_Value_HV_Imag", ProductData.TYPE_FLOAT32, bandInfo, provider);

        provider = new FPH(product, valueProviderMap, true);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("Pixel_Radiometric_Accuracy");
        addBand(product, "Pixel_Radiometric_Accuracy_H", ProductData.TYPE_FLOAT32, bandInfo, provider);

        provider = new FPV(product, valueProviderMap, true);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("Pixel_Radiometric_Accuracy");
        addBand(product, "Pixel_Radiometric_Accuracy_V", ProductData.TYPE_FLOAT32, bandInfo, provider);

        provider = new FPR(product, valueProviderMap, true);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("Pixel_Radiometric_Accuracy");
        addBand(product, "Pixel_Radiometric_Accuracy_HV", ProductData.TYPE_FLOAT32, bandInfo, provider);

        addVirtualBand(product, "Stokes_1", "(BT_Value_H + BT_Value_V) / 2.0");
        addVirtualBand(product, "Stokes_2", "(BT_Value_H - BT_Value_V) / 2.0");
        addVirtualBand(product, "Stokes_3", "BT_Value_HV_Real");
        addVirtualBand(product, "Stokes_4", "BT_Value_HV_Imag");
    }

    private static void addVirtualBand(Product product, String name, String expression) {
        final VirtualBand band = new VirtualBand(name, ProductData.TYPE_FLOAT32,
                                                 product.getSceneRasterWidth(),
                                                 product.getSceneRasterHeight(),
                                                 expression);

        band.setValidPixelExpression(createValidPixelExpression(product, expression));
        product.addBand(band);
    }

    private static String createValidPixelExpression(Product product, String expression) {
        try {
            return BandArithmetic.getValidMaskExpression(expression, new Product[]{product}, 0, null);
        } catch (ParseException e) {
            return null;
        }
    }

    private void addL1cBand(Product product, String bandName, int bandType, BandInfo bandInfo, int fieldIndex,
                            int polMode, HashMap<String, ValueProvider> valueProviderMap, ExplorerFile smosFile) {
        final ValueProvider valueProvider = new BtDataValueProvider((L1cSmosFile) smosFile, fieldIndex, polMode);
        final Band band = addBand(product, bandName, bandType, bandInfo, valueProvider);

        if (bandName.equals("Flags")) {
            final Random random = new Random(5489);
            addFlagCodingAndBitmaskDefs(band, product, product.getFlagCodingGroup().get(0), random);
        }

        valueProviderMap.put(bandName, valueProvider);
    }

    private static void addL1cFlagCoding(Product product) {
        final FlagCoding flagCoding = new FlagCoding("SMOS_L1C");

        for (final FlagDescriptor descriptor : FlagDescriptors.L1C_FLAGS) {
            // skip polarisation flags since they are not meaningful
            if ((descriptor.getMask() & SmosConstants.L1C_POL_FLAGS_MASK) == 0) {
                flagCoding.addFlag(descriptor.getFlagName(), descriptor.getMask(), descriptor.getDescription());
            }
        }

        product.getFlagCodingGroup().add(flagCoding);
    }

    private static void addGridPointSequentialNumberBand(Product product) {
        final BandInfo bandInfo = new BandInfo("Grid_Point_Sequential_Number", "", 0.0, 1.0, -999, 0, 1L << 31,
                                               "Unique identifier for Earth fixed grid point (ISEA4H9 DGG).");
        final Band band = product.addBand(bandInfo.getName(), ProductData.TYPE_UINT32);
        band.setDescription(bandInfo.getDescription());
        band.setSourceImage(SmosDgg.getInstance().getDggMultiLevelImage());
    }

    private Band addBand(Product product, String bandName, int bandType, BandInfo bandInfo,
                         ValueProvider valueProvider) {
        final Band band = product.addBand(bandName, bandType);

        if (bandInfo != null) {
            band.setScalingFactor(bandInfo.getScaleFactor());
            band.setScalingOffset(bandInfo.getScaleOffset());
            band.setUnit(bandInfo.getUnit());
            band.setDescription(bandInfo.getDescription());

            if (bandInfo.getNoDataValue() != null) {
                band.setNoDataValueUsed(true);
                band.setNoDataValue(bandInfo.getNoDataValue().doubleValue());
            }
        }
        band.setSourceImage(createSourceImage(valueProvider, band));
        if (bandInfo != null) {
            band.setImageInfo(createDefaultImageInfo(bandInfo));
        }

        return band;
    }

    private static void addFlagCodingAndBitmaskDefs(Band band, Product product, FlagCoding flagCoding, Random random) {
        band.setSampleCoding(flagCoding);

        final String bandName = band.getName();

        for (final MetadataAttribute flag : flagCoding.getAttributes()) {
            final String name = bandName + "_" + flag.getName();
            final String expr = bandName + " != " + band.getNoDataValue() + " && " + bandName + "." + flag.getName();
            final Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            final BitmaskDef bitmaskDef = new BitmaskDef(name, flag.getDescription(), expr, color, 0.5f);

            product.addBitmaskDef(bitmaskDef);
        }
    }

    private static int memberTypeToBandType(Type memberType) {
        int bandType;

        if (memberType.equals(SimpleType.BYTE)) {
            bandType = ProductData.TYPE_INT8;
        } else if (memberType.equals(SimpleType.UBYTE)) {
            bandType = ProductData.TYPE_UINT8;
        } else if (memberType.equals(SimpleType.SHORT)) {
            bandType = ProductData.TYPE_INT16;
        } else if (memberType.equals(SimpleType.USHORT)) {
            bandType = ProductData.TYPE_UINT16;
        } else if (memberType.equals(SimpleType.INT)) {
            bandType = ProductData.TYPE_INT32;
        } else if (memberType.equals(SimpleType.UINT)) {
            bandType = ProductData.TYPE_UINT32;
        } else if (memberType.equals(SimpleType.FLOAT)) {
            bandType = ProductData.TYPE_FLOAT32;
        } else if (memberType.equals(SimpleType.DOUBLE)) {
            bandType = ProductData.TYPE_FLOAT64;
        } else if (memberType.equals(SimpleType.LONG)) {
            bandType = ProductData.TYPE_INT32;
        } else if (memberType.equals(SimpleType.ULONG)) {
            bandType = ProductData.TYPE_INT32;
        } else {
            throw new IllegalStateException("type = " + memberType);
        }

        return bandType;
    }

    private MultiLevelImage createSourceImage(ValueProvider valueProvider, Band band) {
        return new DefaultMultiLevelImage(new SmosMultiLevelSource(band, valueProvider));
    }

    private static ImageInfo createDefaultImageInfo(BandInfo bandInfo) {
        final Color[] colors;
        if (bandInfo.isCircular()) {
            colors = new Color[]{
                    new Color(0, 0, 0),
                    new Color(85, 0, 136),
                    new Color(0, 0, 255),
                    new Color(0, 255, 255),
                    new Color(0, 255, 0),
                    new Color(255, 255, 0),
                    new Color(255, 140, 0),
                    new Color(255, 0, 0),
                    new Color(255, 140, 0),
                    new Color(255, 255, 0),
                    new Color(0, 255, 0),
                    new Color(0, 255, 255),
                    new Color(0, 0, 255),
                    new Color(85, 0, 136),
                    new Color(0, 0, 0)
            };
        } else {
            colors = new Color[]{
                    new Color(0, 0, 0),
                    new Color(85, 0, 136),
                    new Color(0, 0, 255),
                    new Color(0, 255, 255),
                    new Color(0, 255, 0),
                    new Color(255, 255, 0),
                    new Color(255, 140, 0),
                    new Color(255, 0, 0)
            };
        }
        double min = bandInfo.getMin();
        double max = bandInfo.getMax();

        final ColorPaletteDef.Point[] points = new ColorPaletteDef.Point[colors.length];
        for (int i = 0; i < colors.length; i++) {
            final double sample = min + ((max - min) * i / (colors.length - 1));
            points[i] = new ColorPaletteDef.Point(sample, colors[i]);
        }

        return new ImageInfo(new ColorPaletteDef(points));
    }

    public final Product createProduct(ExplorerFile explorerFile) throws IOException {
        final String productName = FileUtils.getFilenameWithoutExtension(explorerFile.getHdrFile());
        final String productType = explorerFile.getFormat().getName().substring(12, 22);
        final Dimension dimension = ProductHelper.getSceneRasterDimension();
        final Product product = new Product(productName, productType, dimension.width, dimension.height);

        product.setFileLocation(explorerFile.getDblFile());
        product.setPreferredTileSize(512, 512);
        ProductHelper.addMetadata(product.getMetadataRoot(), explorerFile);

        final MapInfo mapInfo = ProductHelper.createMapInfo(ProductHelper.getSceneRasterDimension());
        product.setGeoCoding(new MapGeoCoding(mapInfo));

        addBands(product, explorerFile);
        setQuicklookBandName(product);

        return product;
    }
}
