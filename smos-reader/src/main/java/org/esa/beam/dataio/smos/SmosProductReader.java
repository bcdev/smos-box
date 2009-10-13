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

import com.bc.ceres.binio.*;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import com.bc.jexp.ParseException;
import org.esa.beam.framework.dataio.AbstractProductReader;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.dataop.barithm.BandArithmetic;
import org.esa.beam.framework.dataop.maptransf.Datum;
import org.esa.beam.framework.dataop.maptransf.IdentityTransformDescriptor;
import org.esa.beam.framework.dataop.maptransf.MapInfo;
import org.esa.beam.framework.dataop.maptransf.MapProjectionRegistry;
import org.esa.beam.smos.dgg.SmosDgg;
import org.esa.beam.util.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import javax.media.jai.JAI;
import java.awt.*;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Random;

public class SmosProductReader extends AbstractProductReader {

    private static MultiLevelImage dggridMultiLevelImage;

    private SmosFile smosFile;

    SmosProductReader(final SmosProductReaderPlugIn productReaderPlugIn) {
        super(productReaderPlugIn);
    }

    public SmosFile getSmosFile() {
        return smosFile;
    }

    @Override
    protected synchronized Product readProductNodesImpl() throws IOException {
        dggridMultiLevelImage = SmosDgg.getDggridMultiLevelImage();

        final File inputFile = getInputFile();
        final File hdrFile = FileUtils.exchangeExtension(inputFile, ".HDR");
        final File dblFile = FileUtils.exchangeExtension(inputFile, ".DBL");

        final DataFormat format = SmosFormats.getFormat(hdrFile);
        if (format == null) {
            throw new IOException(MessageFormat.format("File ''{0}'': Unknown SMOS data format", inputFile));
        }

        return createProduct(hdrFile, dblFile, format);
    }

    @Override
    protected synchronized void readBandRasterDataImpl(int sourceOffsetX,
                                                       int sourceOffsetY,
                                                       int sourceWidth,
                                                       int sourceHeight,
                                                       int sourceStepX,
                                                       int sourceStepY,
                                                       Band destBand,
                                                       int destOffsetX,
                                                       int destOffsetY,
                                                       int destWidth,
                                                       int destHeight,
                                                       ProductData destBuffer,
                                                       ProgressMonitor pm) throws IOException {
        final RenderedImage image = destBand.getSourceImage();
        final Raster data = image.getData(new Rectangle(destOffsetX, destOffsetY, destWidth, destHeight));
        data.getDataElements(destOffsetX, destOffsetY, destWidth, destHeight, destBuffer.getElems());
    }

    @Override
    public void close() throws IOException {
        smosFile.close();
        super.close();
    }

    private Product createProduct(File hdrFile, File dblFile, DataFormat format) throws IOException {
        final int sceneWidth = dggridMultiLevelImage.getWidth();
        final int sceneHeight = dggridMultiLevelImage.getHeight();

        final String productName = FileUtils.getFilenameWithoutExtension(hdrFile);
        final String productType = format.getName().substring(12, 22);
        final Product product = new Product(productName, productType, sceneWidth, sceneHeight);

        addMetadata(product.getMetadataRoot(), hdrFile);
        product.setPreferredTileSize(512, 512);
        product.setFileLocation(dblFile);
        product.setGeoCoding(createGeoCoding(product));

        final String formatName = format.getName();

        addGridPointSequentialNumberBand(product);
        if (formatName.contains("MIR_BWLD1C")
                || formatName.contains("MIR_BWND1C")
                || formatName.contains("MIR_BWSD1C")) {
            addL1cFlagCoding(product);
            smosFile = new L1cBrowseSmosFile(dblFile, format);
            addDualPolBrowseBands(product, ((L1cSmosFile) smosFile).getBtDataType());
        } else if (formatName.contains("MIR_BWLF1C")
                || formatName.contains("MIR_BWNF1C")
                || formatName.contains("MIR_BWSF1C")) {
            addL1cFlagCoding(product);
            smosFile = new L1cBrowseSmosFile(dblFile, format);
            addFullPolBrowseBands(product, ((L1cSmosFile) smosFile).getBtDataType());
        } else if (formatName.contains("MIR_SCLD1C")
                || formatName.contains("MIR_SCSD1C")) {
            addL1cFlagCoding(product);
            final L1cScienceSmosFile scienceSmosFile = new L1cScienceSmosFile(dblFile, format, false);
            scienceSmosFile.startBackgroundInit();
            smosFile = scienceSmosFile;
            addDualPolScienceBands(product, ((L1cSmosFile) smosFile).getBtDataType());
        } else if (formatName.contains("MIR_SCLF1C")
                || formatName.contains("MIR_SCSF1C")) {
            addL1cFlagCoding(product);
            final L1cScienceSmosFile scienceSmosFile = new L1cScienceSmosFile(dblFile, format, true);
            scienceSmosFile.startBackgroundInit();
            smosFile = scienceSmosFile;
            addFullPolScienceBands(product, ((L1cSmosFile) smosFile).getBtDataType());
        } else if (formatName.contains("MIR_OSUDP2")) {
            addL2OsFlagCodings(product);
            smosFile = new SmosFile(dblFile, format);
            addSmosL2OsBandsFromCompound(product, smosFile.getGridPointType());
        } else if (formatName.contains("MIR_SMUDP2")) {
            addL2SmFlagCodings(product);
            smosFile = new SmosFile(dblFile, format);
            addSmosL2SmBandsFromCompound(product, smosFile.getGridPointType());
        } else if (formatName.contains("MIR_SMDAP2")) {
            // no flag codings in analysis product
            smosFile = new SmosDAFile(dblFile, format);
            final CompoundType type = smosFile.getGridPointType();
            System.out.println("count = " + type.getMemberCount());
        } else {
            throw new IllegalStateException("Illegal SMOS format: " + formatName);
        }

        // set quicklook band name to first BT band
        for (Band band : product.getBands()) {
            if ("K".equals(band.getUnit())) {
                product.setQuicklookBandName(band.getName());
                break;
            }
        }

        return product;
    }

    private void addDualPolBrowseBands(Product product, CompoundType compoundDataType) {
        final CompoundMember[] members = compoundDataType.getMembers();
        final HashMap<String, GridPointValueProvider> valueProviderMap = new HashMap<String, GridPointValueProvider>();

        for (int fieldIndex = 0; fieldIndex < members.length; fieldIndex++) {
            final CompoundMember member = members[fieldIndex];
            final String memberName = member.getName();
            final BandInfo bandInfo = BandInfoRegistry.getInstance().getBandInfo(memberName);

            if (bandInfo != null) {
                if ("Flags".equals(memberName)) {
                    // flags do not depend on polarisation mode, so there is a single flag band only
                    addL1cBand(product, memberName,
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_X, valueProviderMap);
                } else {
                    addL1cBand(product, memberName + "_X",
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_X, valueProviderMap);
                    addL1cBand(product, memberName + "_Y",
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_Y, valueProviderMap);
                }
            }
        }
    }

    private void addFullPolBrowseBands(Product product, CompoundType compoundDataType) {
        final CompoundMember[] members = compoundDataType.getMembers();
        final HashMap<String, GridPointValueProvider> valueProviderMap = new HashMap<String, GridPointValueProvider>();

        for (int fieldIndex = 0; fieldIndex < members.length; fieldIndex++) {
            final CompoundMember member = members[fieldIndex];
            final String memberName = member.getName();
            final BandInfo bandInfo = BandInfoRegistry.getInstance().getBandInfo(memberName);

            if (bandInfo != null) {
                if ("Flags".equals(memberName)) {
                    // flags do not depend on polarisation mode, so there is a single flag band only
                    addL1cBand(product, memberName,
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_X, valueProviderMap);
                } else if ("BT_Value".equals(memberName)) {
                    addL1cBand(product, memberName + "_X",
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_X, valueProviderMap);
                    addL1cBand(product, memberName + "_Y",
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_Y, valueProviderMap);
                    final BandInfo bandInfoCrossPol = new BandInfo(bandInfo.getName(),
                            bandInfo.getUnit(),
                            bandInfo.getScaleOffset(),
                            bandInfo.getScaleFactor(),
                            bandInfo.getNoDataValue(),
                            -10.0, 10.0,
                            bandInfo.getDescription());
                    addL1cBand(product, memberName + "_XY_Real",
                            memberTypeToBandType(member.getType()), bandInfoCrossPol, fieldIndex,
                            SmosFormats.L1C_POL_MODE_XY1, valueProviderMap);
                    addL1cBand(product, memberName + "_XY_Imag",
                            memberTypeToBandType(member.getType()), bandInfoCrossPol, fieldIndex,
                            SmosFormats.L1C_POL_MODE_XY2, valueProviderMap);
                } else {
                    addL1cBand(product, memberName + "_X",
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_X, valueProviderMap);
                    addL1cBand(product, memberName + "_Y",
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_Y, valueProviderMap);
                    addL1cBand(product, memberName + "_XY",
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_XY1, valueProviderMap);
                }
            }
        }
    }

    private void addDualPolScienceBands(Product product, CompoundType compoundDataType) {
        final CompoundMember[] members = compoundDataType.getMembers();
        final HashMap<String, GridPointValueProvider> valueProviderMap = new HashMap<String, GridPointValueProvider>();

        for (int fieldIndex = 0; fieldIndex < members.length; fieldIndex++) {
            final CompoundMember member = members[fieldIndex];
            final String memberName = member.getName();
            final BandInfo bandInfo = BandInfoRegistry.getInstance().getBandInfo(memberName);

            if (bandInfo != null) {
                if ("Flags".equals(memberName)) {
                    // flags do not depend on polarisation mode, so there is a single flag band only
                    addL1cBand(product, memberName,
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_ANY, valueProviderMap);
                } else {
                    addL1cBand(product, memberName + "_X",
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_X, valueProviderMap);
                    addL1cBand(product, memberName + "_Y",
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_Y, valueProviderMap);
                }
            }
        }

        addRotatedDualPolBands(product, valueProviderMap);
    }

    private void addFullPolScienceBands(Product product, CompoundType compoundDataType) {
        final CompoundMember[] members = compoundDataType.getMembers();
        final HashMap<String, GridPointValueProvider> valueProviderMap = new HashMap<String, GridPointValueProvider>();

        for (int fieldIndex = 0; fieldIndex < members.length; fieldIndex++) {
            final CompoundMember member = members[fieldIndex];
            final String memberName = member.getName();
            final BandInfo bandInfo = BandInfoRegistry.getInstance().getBandInfo(memberName);

            if (bandInfo != null) {
                if ("Flags".equals(memberName)) {
                    // flags do not depend on polarisation mode, so there is a single flag band only
                    addL1cBand(product, memberName,
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_ANY, valueProviderMap);
                } else if ("BT_Value_Real".equals(memberName)) {
                    addL1cBand(product, "BT_Value_X",
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_X, valueProviderMap);
                    addL1cBand(product, "BT_Value_Y",
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_Y, valueProviderMap);
                    final BandInfo bandInfoCrossPol = new BandInfo(bandInfo.getName(),
                            bandInfo.getUnit(),
                            bandInfo.getScaleOffset(),
                            bandInfo.getScaleFactor(),
                            bandInfo.getNoDataValue(), -10.0, 10.0,
                            bandInfo.getDescription());
                    addL1cBand(product, "BT_Value_XY_Real",
                            memberTypeToBandType(member.getType()), bandInfoCrossPol, fieldIndex,
                            SmosFormats.L1C_POL_MODE_XY1, valueProviderMap);
                } else if ("BT_Value_Imag".equals(memberName)) {
                    addL1cBand(product, "BT_Value_XY_Imag",
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_XY1, valueProviderMap);
                } else {
                    addL1cBand(product, memberName + "_X",
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_X, valueProviderMap);
                    addL1cBand(product, memberName + "_Y",
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_Y, valueProviderMap);
                    addL1cBand(product, memberName + "_XY",
                            memberTypeToBandType(member.getType()), bandInfo, fieldIndex,
                            SmosFormats.L1C_POL_MODE_XY1, valueProviderMap);
                }
            }
        }

        addRotatedFullPolBands(product, valueProviderMap);
    }

    private void addRotatedDualPolBands(Product product, HashMap<String, GridPointValueProvider> valueProviderMap) {
        DpGPVP provider;
        BandInfo bandInfo;

        provider = new DphGPVP(product, valueProviderMap, false);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("BT_Value");
        addBand(product, "BT_Value_H", ProductData.TYPE_FLOAT32, bandInfo, provider);

        provider = new DpvGPVP(product, valueProviderMap, false);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("BT_Value");
        addBand(product, "BT_Value_V", ProductData.TYPE_FLOAT32, bandInfo, provider);

        provider = new DphGPVP(product, valueProviderMap, false);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("Pixel_Radiometric_Accuracy");
        addBand(product, "Pixel_Radiometric_Accuracy_H", ProductData.TYPE_FLOAT32, bandInfo, provider);

        provider = new DpvGPVP(product, valueProviderMap, false);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("Pixel_Radiometric_Accuracy");
        addBand(product, "Pixel_Radiometric_Accuracy_V", ProductData.TYPE_FLOAT32, bandInfo, provider);

        addVirtualBand(product, "Stokes_1", "(BT_Value_H + BT_Value_V) / 2.0");
        addVirtualBand(product, "Stokes_2", "(BT_Value_H - BT_Value_V) / 2.0");
    }

    private void addRotatedFullPolBands(Product product, HashMap<String, GridPointValueProvider> valueProviderMap) {
        FpGPVP provider;
        BandInfo bandInfo;

        provider = new FphGPVP(product, valueProviderMap, false);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("BT_Value");
        addBand(product, "BT_Value_H", ProductData.TYPE_FLOAT32, bandInfo, provider);

        provider = new FpvGPVP(product, valueProviderMap, false);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("BT_Value");
        addBand(product, "BT_Value_V", ProductData.TYPE_FLOAT32, bandInfo, provider);

        provider = new FprGPVP(product, valueProviderMap, false);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("BT_Value_Real");
        addBand(product, "BT_Value_HV_Real", ProductData.TYPE_FLOAT32, bandInfo, provider);

        provider = new FpiGPVP(product, valueProviderMap, false);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("BT_Value_Imag");
        addBand(product, "BT_Value_HV_Imag", ProductData.TYPE_FLOAT32, bandInfo, provider);

        provider = new FphGPVP(product, valueProviderMap, false);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("Pixel_Radiometric_Accuracy");
        addBand(product, "Pixel_Radiometric_Accuracy_H", ProductData.TYPE_FLOAT32, bandInfo, provider);

        provider = new FpvGPVP(product, valueProviderMap, false);
        bandInfo = BandInfoRegistry.getInstance().getBandInfo("Pixel_Radiometric_Accuracy");
        addBand(product, "Pixel_Radiometric_Accuracy_V", ProductData.TYPE_FLOAT32, bandInfo, provider);

        provider = new FprGPVP(product, valueProviderMap, false);
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

    private void addSmosL2OsBandsFromCompound(Product product, CompoundType compoundDataType) {
        final CompoundMember[] members = compoundDataType.getMembers();

        for (int fieldIndex = 0; fieldIndex < members.length; fieldIndex++) {
            final CompoundMember member = members[fieldIndex];
            if (member.getType().isSimpleType()) {
                final String memberName = member.getName();
                final BandInfo bandInfo = BandInfoRegistry.getInstance().getBandInfo(memberName);
                if (bandInfo != null) {
                    addL2OsBand(product, memberName, memberTypeToBandType(member.getType()), bandInfo, fieldIndex);
                } else {
                    System.out.println("No band info available for memberName: " + memberName);
                }
            }
        }
    }

    private void addSmosL2SmBandsFromCompound(Product product, CompoundType compoundDataType) {
        final CompoundMember[] members = compoundDataType.getMembers();

        for (int fieldIndex = 0; fieldIndex < members.length; fieldIndex++) {
            final CompoundMember member = members[fieldIndex];
            if (member.getType().isSimpleType()) {
                final String memberName = member.getName();
                final BandInfo bandInfo = BandInfoRegistry.getInstance().getBandInfo(memberName);
                if (bandInfo != null) {
                    addL2SmBand(product, memberName, memberTypeToBandType(member.getType()), bandInfo, fieldIndex);
                } else {
                    System.out.println("No band info available for memberName: " + memberName);
                }
            }
        }
    }

    private void addL1cBand(Product product, String bandName, int bandType, BandInfo bandInfo, int fieldIndex,
                            int polMode, HashMap<String, GridPointValueProvider> valueProviderMap) {
        final GridPointValueProvider valueProvider =
                new L1cFieldValueProvider((L1cSmosFile) smosFile, fieldIndex, polMode);
        final Band band = addBand(product, bandName, bandType, bandInfo, valueProvider);

        if (bandName.equals("Flags")) {
            final Random random = new Random(5489);
            addFlagCodingAndBitmaskDefs(band, product, product.getFlagCodingGroup().get(0), random);
        }

        valueProviderMap.put(bandName, valueProvider);
    }

    private void addL2OsBand(Product product, String bandName, int bandType, BandInfo bandInfo, int fieldIndex) {
        final Band band = addBand(product, bandName, bandType, bandInfo,
                new L2FieldValueProvider(smosFile, fieldIndex));

        final Random random = new Random(5489);
        if (bandName.startsWith("Control_Flags")) {
            addFlagCodingAndBitmaskDefs(band, product, product.getFlagCodingGroup().get(0), random);
        } else if (bandName.startsWith("Science_Flags")) {
            addFlagCodingAndBitmaskDefs(band, product, product.getFlagCodingGroup().get(1), random);
        }
    }

    private void addL2SmBand(Product product, String bandName, int bandType, BandInfo bandInfo, int fieldIndex) {
        final Band band = addBand(product, bandName, bandType, bandInfo,
                new L2FieldValueProvider(smosFile, fieldIndex));

        final Random random = new Random(5489);
        if (bandName.equals("Confidence_Flags")) {
            addFlagCodingAndBitmaskDefs(band, product, product.getFlagCodingGroup().get(0), random);
        } else if (bandName.equals("Science_Flags")) {
            addFlagCodingAndBitmaskDefs(band, product, product.getFlagCodingGroup().get(1), random);
        } else if (bandName.equals("Processing_Flags")) {
            addFlagCodingAndBitmaskDefs(band, product, product.getFlagCodingGroup().get(2), random);
        } else if (bandName.equals("DGG_Current_Flags")) {
            addFlagCodingAndBitmaskDefs(band, product, product.getFlagCodingGroup().get(3), random);
        }
    }

    private File getInputFile() {
        final Object input = getInput();

        if (input instanceof String) {
            return new File((String) input);
        }
        if (input instanceof File) {
            return (File) input;
        }

        throw new IllegalArgumentException(MessageFormat.format("Unsupported input: {0}", input));
    }

    private static GeoCoding createGeoCoding(Product product) {
        final MapInfo mapInfo = new MapInfo(MapProjectionRegistry.getProjection(IdentityTransformDescriptor.NAME),
                0.0f, 0.0f,
                -180.0f, +90.0f,
                360.0f / product.getSceneRasterWidth(),
                180.0f / product.getSceneRasterHeight(),
                Datum.WGS_84);
        mapInfo.setSceneWidth(product.getSceneRasterWidth());
        mapInfo.setSceneHeight(product.getSceneRasterHeight());

        return new MapGeoCoding(mapInfo);
    }

    private static void addL1cFlagCoding(Product product) {
        final FlagCoding flagCoding = new FlagCoding("SMOS_L1C");

        for (final FlagDescriptor descriptor : SmosFormats.L1C_FLAGS) {
            // skip polarisation flags since they are not meaningful
            if ((descriptor.getMask() & SmosFormats.L1C_POL_FLAGS_MASK) == 0) {
                flagCoding.addFlag(descriptor.getName(), descriptor.getMask(), descriptor.getDescription());
            }
        }

        product.getFlagCodingGroup().add(flagCoding);
    }

    private static void addL2SmFlagCodings(Product product) {
        final FlagCoding confidenceFlagCoding = new FlagCoding("SMOS_L2_SM_CONFIDENCE");
        for (final FlagDescriptor descriptor : SmosFormats.L2_SM_CONFIDENCE_FLAGS) {
            confidenceFlagCoding.addFlag(descriptor.getName(), descriptor.getMask(), descriptor.getDescription());
        }

        final FlagCoding scienceFlagCoding = new FlagCoding("SMOS_L2_SM_SCIENCE");
        for (final FlagDescriptor descriptor : SmosFormats.L2_SM_SCIENCE_FLAGS) {
            scienceFlagCoding.addFlag(descriptor.getName(), descriptor.getMask(), descriptor.getDescription());
        }

        final FlagCoding processingFlagCoding = new FlagCoding("SMOS_L2_SM_PROCESSING");
        for (final FlagDescriptor descriptor : SmosFormats.L2_SM_PROCESSING_FLAGS) {
            processingFlagCoding.addFlag(descriptor.getName(), descriptor.getMask(), descriptor.getDescription());
        }

        final FlagCoding dggCurrentFlagCoding = new FlagCoding("SMOS_L2_SM_DGG_CURRENT");
        for (final FlagDescriptor descriptor : SmosFormats.L2_SM_DGG_CURRENT_FLAGS) {
            dggCurrentFlagCoding.addFlag(descriptor.getName(), descriptor.getMask(), descriptor.getDescription());
        }

        product.getFlagCodingGroup().add(confidenceFlagCoding);
        product.getFlagCodingGroup().add(scienceFlagCoding);
        product.getFlagCodingGroup().add(processingFlagCoding);
        product.getFlagCodingGroup().add(dggCurrentFlagCoding);
    }

    private static void addL2OsFlagCodings(Product product) {
        final FlagCoding controlFlagCoding = new FlagCoding("SMOS_L2_OS_CONTROL");
        for (final FlagDescriptor descriptor : SmosFormats.L2_OS_CONTROL_FLAGS) {
            controlFlagCoding.addFlag(descriptor.getName(), descriptor.getMask(), descriptor.getDescription());
        }

        final FlagCoding scienceFlagCoding = new FlagCoding("SMOS_L2_OS_SCIENCE");
        for (final FlagDescriptor descriptor : SmosFormats.L2_OS_SCIENCE_FLAGS) {
            scienceFlagCoding.addFlag(descriptor.getName(), descriptor.getMask(), descriptor.getDescription());
        }

        product.getFlagCodingGroup().add(controlFlagCoding);
        product.getFlagCodingGroup().add(scienceFlagCoding);
    }

    private static void addGridPointSequentialNumberBand(Product product) {
        final BandInfo bandInfo = new BandInfo("Grid_Point_Sequential_Number", "", 0.0, 1.0, -999, 0, 1L << 31,
                "Unique identifier for Earth fixed grid point (ISEA4H9 DGG).");
        final Band band = product.addBand(bandInfo.getName(), ProductData.TYPE_UINT32);
        band.setDescription(bandInfo.getDescription());

        band.setSourceImage(dggridMultiLevelImage);
    }

    private Band addBand(Product product, String bandName, int bandType, BandInfo bandInfo,
                         GridPointValueProvider valueProvider) {
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

    private static int memberTypeToBandType(Type type) {
        int bandType;

        if (type.equals(SimpleType.BYTE)) {
            bandType = ProductData.TYPE_INT8;
        } else if (type.equals(SimpleType.UBYTE)) {
            bandType = ProductData.TYPE_UINT8;
        } else if (type.equals(SimpleType.SHORT)) {
            bandType = ProductData.TYPE_INT16;
        } else if (type.equals(SimpleType.USHORT)) {
            bandType = ProductData.TYPE_UINT16;
        } else if (type.equals(SimpleType.INT)) {
            bandType = ProductData.TYPE_INT32;
        } else if (type.equals(SimpleType.UINT)) {
            bandType = ProductData.TYPE_UINT32;
        } else if (type.equals(SimpleType.FLOAT)) {
            bandType = ProductData.TYPE_FLOAT32;
        } else if (type.equals(SimpleType.DOUBLE)) {
            bandType = ProductData.TYPE_FLOAT64;
        } else {
            throw new IllegalStateException("type = " + type);
        }

        return bandType;
    }

    private MultiLevelImage createSourceImage(GridPointValueProvider valueProvider, Band band) {
        return new DefaultMultiLevelImage(new SmosMultiLevelSource(valueProvider, dggridMultiLevelImage, band));
    }

    private static ImageInfo createDefaultImageInfo(BandInfo bandInfo) {
        final Color[] colors;
        if (bandInfo.isTopologyCircular()) {
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

    private static void addMetadata(MetadataElement metadataElement, File hdrFile) throws IOException {
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

        addMetadata(metadataElement, document.getRootElement(), namespace);
    }

    private static void addMetadata(MetadataElement metadataElement, Element xmlElement, Namespace namespace) {
        for (final Object o : xmlElement.getChildren()) {
            final Element xmlChild = (Element) o;

            if (xmlChild.getChildren(null, namespace).size() == 0) {
                final String s = xmlChild.getTextNormalize();
                if (s != null && !s.isEmpty()) {
                    metadataElement.addAttribute(
                            new MetadataAttribute(xmlChild.getName(), ProductData.createInstance(s), true));
                }
            } else {
                MetadataElement mdChild = new MetadataElement(xmlChild.getName());
                metadataElement.addElement(mdChild);
                addMetadata(mdChild, xmlChild, namespace);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        JAI.getDefaultInstance().getTileCache().setMemoryCapacity(512 * (1024 * 1024));
        SmosProductReader smosProductReader = new SmosProductReader(new SmosProductReaderPlugIn());
        final File dir = new File(args[0]);
        final File file = new File(dir, dir.getName() + ".DBL");
        Product product = smosProductReader.readProductNodes(file, null);
        ProductIO.writeProduct(product, "smosproduct_2.dim", null);
    }
}
