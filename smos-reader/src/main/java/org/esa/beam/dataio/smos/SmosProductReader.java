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
import org.esa.beam.dataio.smos.dddb.BandDescriptor;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.framework.dataio.AbstractProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.smos.SmosUtils;
import org.esa.beam.smos.lsmask.SmosLsMask;
import org.esa.beam.util.io.FileUtils;

import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

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
                product.setFileLocation(productFile.getDataFile());
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

}
