/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.VirtualDir;
import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.MultiLevelModel;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.AbstractMultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.beam.dataio.netcdf.util.DataTypeUtils;
import org.esa.beam.dataio.netcdf.util.MetadataUtils;
import org.esa.beam.dataio.smos.dddb.BandDescriptor;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.framework.dataio.AbstractProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.jai.ImageManager;
import org.esa.beam.jai.ResolutionLevel;
import org.esa.beam.jai.SingleBandedOpImage;
import org.esa.beam.smos.SmosUtils;
import org.esa.beam.smos.dgg.SmosDgg;
import org.esa.beam.smos.lsmask.SmosLsMask;
import org.esa.beam.util.io.FileUtils;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Sequence;
import ucar.nc2.Variable;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

public class SmosProductReader extends AbstractProductReader {

    private static final String LSMASK_SCHEMA_NAME = "DBL_SM_XXXX_AUX_LSMASK_0200";

    private ProductFile productFile;
    private VirtualDir virtualDir;

    public static boolean isDualPolScienceFormat(String formatName) {
        return formatName.contains("MIR_SCLD1C")
               || formatName.contains("MIR_SCSD1C")
               || formatName.contains("MIR_SCND1C");
    }

    public static boolean isFullPolBrowseFormat(String formatName) {
        return formatName.contains("MIR_BWLF1C")
               || formatName.contains("MIR_BWSF1C")
               || formatName.contains("MIR_BWNF1C");
    }

    public static boolean isFullPolScienceFormat(String formatName) {
        return formatName.contains("MIR_SCLF1C")
               || formatName.contains("MIR_SCSF1C")
               || formatName.contains("MIR_SCNF1C");
    }

    public static boolean isDffLaiFormat(String formatName) {
        return formatName.contains("AUX_DFFLAI");
    }

    public static boolean isVTecFormat(String formatName) {
        return formatName.contains("AUX_VTEC_C")
               || formatName.contains("AUX_VTEC_P");
    }

    public static boolean isLsMaskFormat(String formatName) {
        return formatName.contains("AUX_LSMASK");
    }

    public ProductFile getProductFile() {
        return productFile;
    }

    public static ProductFile createProductFile(File file) throws IOException {
        if (file.isDirectory()) {
            final File[] files = file.listFiles(new ExplorerFilenameFilter());
            if (files != null && files.length == 2) {
                file = files[0];
            }
        }

        final ProductFile productFile = createProductFile2(file);
        if (productFile == null) {
            throw new IOException(MessageFormat.format("File ''{0}'': unknown/unsupported SMOS data format.", file));
        }
        return productFile;
    }

    private static ProductFile createProductFile(VirtualDir virtualDir) throws IOException {
        String listPath = "";
        String[] list = virtualDir.list(listPath);
        if (list.length == 1) {
            listPath = list[0] + "/";
        }
        list = virtualDir.list(listPath);

        String fileName = null;
        for (String listEntry : list) {
            if (listEntry.contains(".hdr") || listEntry.contains(".HDR")) {
                fileName = listEntry;
                break;
            }
        }

        if (fileName == null) {
            return null;
        }

        final File hdrFile = virtualDir.getFile(listPath + fileName);
        File dblFile = FileUtils.exchangeExtension(hdrFile, ".DBL");
        dblFile = virtualDir.getFile(listPath + dblFile.getName());

        return createProductFile2(dblFile);
    }

    SmosProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    @Override
    protected final Product readProductNodesImpl() throws IOException {
        synchronized (this) {
            final File inputFile = getInputFile();
            final String inputFileName = inputFile.getName();
            if (SmosUtils.isDblFileName(inputFileName) || SmosUtils.isLightBufrType(inputFileName)) {
                productFile = createProductFile(inputFile);
            } else {
                productFile = createProductFile(getInputVirtualDir());
            }
            if (productFile == null) {
                throw new IOException(
                        MessageFormat.format("File ''{0}'': unknown/unsupported SMOS data format.", inputFile));
            }
            final Product product = productFile.createProduct();
            if (virtualDir != null && virtualDir.isCompressed()) {
                final String path = virtualDir.getBasePath();
                product.setFileLocation(new File(path));
            } else {
                product.setFileLocation(productFile.getFile());
            }
            if (productFile instanceof SmosFile) {
                addLandSeaMask(product);
            }
            return product;
        }
    }


    @Override
    protected final void readBandRasterDataImpl(int sourceOffsetX,
                                                int sourceOffsetY,
                                                int sourceWidth,
                                                int sourceHeight,
                                                int sourceStepX,
                                                int sourceStepY,
                                                Band targetBand,
                                                int targetOffsetX,
                                                int targetOffsetY,
                                                int targetWidth,
                                                int targetHeight,
                                                ProductData targetBuffer,
                                                ProgressMonitor pm) {
        synchronized (this) {
            final RenderedImage image = targetBand.getSourceImage();
            final Raster data = image.getData(new Rectangle(targetOffsetX, targetOffsetY, targetWidth, targetHeight));

            data.getDataElements(targetOffsetX, targetOffsetY, targetWidth, targetHeight, targetBuffer.getElems());
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (this) {
            productFile.close();
            if (virtualDir != null) {
                virtualDir.close();
            }
            super.close();
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

        throw new IllegalArgumentException(MessageFormat.format("Illegal input: {0}", input));
    }

    private VirtualDir getInputVirtualDir() {
        File inputFile = getInputFile();

        if (!SmosUtils.isCompressedFile(inputFile)) {
            inputFile = inputFile.getParentFile();
        }

        virtualDir = VirtualDir.create(inputFile);
        if (virtualDir == null) {
            throw new IllegalArgumentException(MessageFormat.format("Illegal input: {0}", inputFile));
        }

        return virtualDir;
    }

    private void addLandSeaMask(Product product) {
        final BandDescriptor descriptor = Dddb.getInstance().getBandDescriptors(
                LSMASK_SCHEMA_NAME).getMember(SmosConstants.LAND_SEA_MASK_NAME);

        final Band band = product.addBand(descriptor.getBandName(), ProductData.TYPE_UINT8);

        band.setScalingOffset(descriptor.getScalingOffset());
        band.setScalingFactor(descriptor.getScalingFactor());
        if (descriptor.hasFillValue()) {
            band.setNoDataValueUsed(true);
            band.setNoDataValue(descriptor.getFillValue());
        }
        if (!descriptor.getValidPixelExpression().isEmpty()) {
            band.setValidPixelExpression(descriptor.getValidPixelExpression());
        }
        if (!descriptor.getUnit().isEmpty()) {
            band.setUnit(descriptor.getUnit());
        }
        if (!descriptor.getDescription().isEmpty()) {
            band.setDescription(descriptor.getDescription());
        }
        if (descriptor.getFlagDescriptors() != null) {
            ProductHelper.addFlagsAndMasks(product, band, descriptor.getFlagCodingName(),
                                           descriptor.getFlagDescriptors());
        }

        band.setSourceImage(SmosLsMask.getInstance().getMultiLevelImage());
        band.setImageInfo(ProductHelper.createImageInfo(band, descriptor));
    }

    private static boolean isDggFloFormat(String formatName) {
        return formatName.contains("AUX_DGGFLO");
    }

    private static boolean isDggRfiFormat(String formatName) {
        return formatName.contains("AUX_DGGRFI");
    }

    private static boolean isDggRouFormat(String formatName) {
        return formatName.contains("AUX_DGGROU");
    }

    private static boolean isDggTfoFormat(String formatName) {
        return formatName.contains("AUX_DGGTFO");
    }

    private static boolean isDggTlvFormat(String formatName) {
        return formatName.contains("AUX_DGGTLV");
    }

    private static ProductFile createProductFile2(File file) throws IOException {
        if (SmosUtils.isLightBufrType(file.getName())) {
            return new LightBufrFile(file);
        }

        final File hdrFile = FileUtils.exchangeExtension(file, ".HDR");
        final File dblFile = FileUtils.exchangeExtension(file, ".DBL");

        final DataFormat format = Dddb.getInstance().getDataFormat(hdrFile);
        if (format == null) {
            return null;
        }

        final String formatName = format.getName();
        if (SmosUtils.isDualPolBrowseFormat(formatName)) {
            return new L1cBrowseSmosFile(hdrFile, dblFile, format);
        } else if (isFullPolBrowseFormat(formatName)) {
            return new L1cBrowseSmosFile(hdrFile, dblFile, format);
        } else if (isDualPolScienceFormat(formatName) ||
                   isFullPolScienceFormat(formatName)) {
            return new L1cScienceSmosFile(hdrFile, dblFile, format);
        } else if (SmosUtils.isOsUserFormat(formatName)) {
            return new SmosFile(hdrFile, dblFile, format);
        } else if (SmosUtils.isSmUserFormat(formatName)) {
            return new SmUserSmosFile(hdrFile, dblFile, format);
        } else if (SmosUtils.isOsAnalysisFormat(formatName)) {
            return new SmosFile(hdrFile, dblFile, format);
        } else if (SmosUtils.isSmAnalysisFormat(formatName)) {
            return new SmosFile(hdrFile, dblFile, format);
        } else if (SmosUtils.isAuxECMWFType(formatName)) {
            return new SmosFile(hdrFile, dblFile, format);
        } else if (isDffLaiFormat(formatName)) {
            return new LaiFile(hdrFile, dblFile, format);
        } else if (isVTecFormat(formatName)) {
            return new VTecFile(hdrFile, dblFile, format);
        } else if (isLsMaskFormat(formatName)) {
            return new GlobalSmosFile(hdrFile, dblFile, format);
        } else if (isDggFloFormat(formatName)) {
            return new AuxiliaryFile(hdrFile, dblFile, format);
        } else if (isDggRfiFormat(formatName)) {
            return new AuxiliaryFile(hdrFile, dblFile, format);
        } else if (isDggRouFormat(formatName)) {
            return new AuxiliaryFile(hdrFile, dblFile, format);
        } else if (isDggTfoFormat(formatName)) {
            return new AuxiliaryFile(hdrFile, dblFile, format);
        } else if (isDggTlvFormat(formatName)) {
            return new AuxiliaryFile(hdrFile, dblFile, format);
        }

        return null;
    }

    private static class LightBufrFile implements ProductFile {

        private final NetcdfFile ncfile;

        public LightBufrFile(File file) throws IOException {
            ncfile = NetcdfFile.open(file.getPath());
        }

        @Override
        public void close() throws IOException {
            ncfile.close();
        }

        @Override
        public Product createProduct() throws IOException {
            final Sequence sequence = getObservationSequence();
            final Variable lonVariable = sequence.findVariable("Longitude_high_accuracy");
            final Variable latVariable = sequence.findVariable("Latitude_high_accuracy");
            final Array lonData = lonVariable.read();
            final Array latData = latVariable.read();
            final int[] seqnums = new int[lonVariable.getShape(0)];
            final double lonScalingOffset = getAttributeValue(lonVariable, "add_offset", 0.0);
            final double latScalingOffset = getAttributeValue(latVariable, "add_offset", 0.0);
            final double lonScalingFactor = getAttributeValue(lonVariable, "scale_factor", 1.0);
            final double latScalingFactor = getAttributeValue(latVariable, "scale_factor", 1.0);
            final double lonMissingValue = getAttributeValue(lonVariable, "missing_value");
            final double latMissingValue = getAttributeValue(latVariable, "missing_value");

            // TODO - establish mapping from (lon, lat, snapshot) to index

            final String productName = FileUtils.getFilenameWithoutExtension(getFile());
            final String productType = "W_ES-ESA-ESAC,SMOS,N256";
            final Dimension dimension = ProductHelper.getSceneRasterDimension();
            final Product product = new Product(productName, productType, dimension.width, dimension.height);

            product.setFileLocation(getFile());
            product.setPreferredTileSize(512, 512);
            final List<Attribute> globalAttributes = ncfile.getGlobalAttributes();
            product.getMetadataRoot().addElement(
                    MetadataUtils.readAttributeList(globalAttributes, "Global_Attributes"));
            final List<Variable> variables = sequence.getVariables();
            product.getMetadataRoot().addElement(
                    MetadataUtils.readVariableDescriptions(variables, "Variable_Attributes", 100));
            product.setGeoCoding(ProductHelper.createGeoCoding(dimension));


            addBands(product);
            //setTimes(product);

            return product;
        }

        private static double getAttributeValue(Variable lonVariable, String attributeName, double defaultValue) {
            final Attribute attribute = lonVariable.findAttribute(attributeName);
            if (attribute == null) {
                return defaultValue;
            }
            return attribute.getNumericValue().doubleValue();
        }

        private static double getAttributeValue(Variable lonVariable, String attributeName) {
            final Attribute attribute = lonVariable.findAttribute(attributeName);
            if (attribute == null) {
                throw new IllegalArgumentException(
                        MessageFormat.format("Variable ''{0}'' does not exhibit requested attribute ''{1}''.",
                                             lonVariable.getShortName(), attributeName)
                );
            }
            return attribute.getNumericValue().doubleValue();
        }

        private void addBands(Product product) {
            final Sequence sequence = getObservationSequence();
            final List<Variable> variables = sequence.getVariables();
            for (final Variable v : variables) {
                if (v.getDataType().isEnum()) {
                    final int dataType = ProductData.TYPE_UINT8;
                    addBand(product, v, dataType);
                } else {
                    final int dataType = DataTypeUtils.getRasterDataType(v);
                    if (dataType != -1) {
                        addBand(product, v, dataType);
                    }
                }
            }
        }

        private void addBand(Product product, Variable v, int dataType) {
            final Band band = product.addBand(v.getShortName(), dataType);
            final Attribute units = v.findAttribute("units");
            if (units != null) {
                band.setUnit(units.getStringValue());
            }
            final Attribute addOffset = v.findAttribute("add_offset");
            if (addOffset != null) {
                band.setScalingOffset(addOffset.getNumericValue().doubleValue());
            }
            final Attribute scaleFactor = v.findAttribute("scale_factor");
            if (scaleFactor != null) {
                band.setScalingFactor(scaleFactor.getNumericValue().doubleValue());
            }
            final Attribute missingValue = v.findAttribute("missing_value");
            if (missingValue != null) {
                band.setNoDataValue(missingValue.getNumericValue().doubleValue());
                band.setNoDataValueUsed(true);
            }
            final LightBufrValueProvider valueProvider = createValueProvider(v);
            band.setSourceImage(createSourceImage(band, valueProvider));
        }

        private MultiLevelImage createSourceImage(final Band band, final LightBufrValueProvider valueProvider) {
            return new DefaultMultiLevelImage(createMultiLevelSource(band, valueProvider));
        }

        private MultiLevelSource createMultiLevelSource(final Band band, final LightBufrValueProvider valueProvider) {
            return new AbstractMultiLevelSource(SmosDgg.getInstance().getMultiLevelImage().getModel()) {
                @Override
                protected RenderedImage createImage(int level) {
                    return new LightBufrOpImage(valueProvider, band, getModel(),
                                                ResolutionLevel.create(getModel(), level));
                }
            };
        }

        private LightBufrValueProvider createValueProvider(Variable v) {
            return null;
        }

        @Override
        public File getFile() {
            return new File(ncfile.getLocation());
        }

        private Sequence getObservationSequence() {
            return (Sequence) ncfile.findVariable("obs");
        }

    }

    private static class LightBufrValueProvider {

    }

    private static class LightBufrOpImage extends SingleBandedOpImage {

        public LightBufrOpImage(LightBufrValueProvider valueProvider, Band band, MultiLevelModel model,
                                ResolutionLevel level) {
            super(ImageManager.getDataBufferType(band.getDataType()),
                  band.getSceneRasterWidth(),
                  band.getSceneRasterHeight(),
                  band.getProduct().getPreferredTileSize(),
                  null, // no configuration
                  level);
        }
    }

}
