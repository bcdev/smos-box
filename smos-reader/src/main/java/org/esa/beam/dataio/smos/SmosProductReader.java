package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.dataio.smos.dddb.BandDescriptor;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.framework.dataio.AbstractProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
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

    private ExplorerFile explorerFile;

    public static boolean isDualPolBrowseFormat(String formatName) {
        return formatName.contains("MIR_BWLD1C")
               || formatName.contains("MIR_BWSD1C")
               || formatName.contains("MIR_BWND1C");
    }

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

    public static boolean isOsAnalysisFormat(String formatName) {
        return formatName.contains("MIR_OSDAP2");
    }

    public static boolean isOsUserFormat(String formatName) {
        return formatName.contains("MIR_OSUDP2");
    }

    public static boolean isSmAnalysisFormat(String formatName) {
        return formatName.contains("MIR_SMDAP2");
    }

    public static boolean isSmUserFormat(String formatName) {
        return formatName.contains("MIR_SMUDP2");
    }

    public static boolean isEcmwfFormat(String formatName) {
        return formatName.contains("AUX_ECMWF_");
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

    public static ExplorerFile createExplorerFile(File file) throws IOException {
        final File hdrFile = FileUtils.exchangeExtension(file, ".HDR");
        final File dblFile = FileUtils.exchangeExtension(file, ".DBL");

        final DataFormat format = Dddb.getInstance().getDataFormat(hdrFile);
        if (format == null) {
            throw new IOException(MessageFormat.format("File ''{0}'': unknown SMOS data format.", file));
        }
        final String formatName = format.getName();
        if (isDualPolBrowseFormat(formatName)) {
            return new L1cBrowseSmosFile(hdrFile, dblFile, format);
        } else if (isFullPolBrowseFormat(formatName)) {
            return new L1cBrowseSmosFile(hdrFile, dblFile, format);
        } else if (isDualPolScienceFormat(formatName)) {
            return new L1cScienceSmosFile(hdrFile, dblFile, format);
        } else if (isFullPolScienceFormat(formatName)) {
            return new L1cScienceSmosFile(hdrFile, dblFile, format);
        } else if (isOsUserFormat(formatName)) {
            return new SmosFile(hdrFile, dblFile, format);
        } else if (isSmUserFormat(formatName)) {
            return new SmosFile(hdrFile, dblFile, format);
        } else if (isOsAnalysisFormat(formatName)) {
            return new SmosFile(hdrFile, dblFile, format);
        } else if (isSmAnalysisFormat(formatName)) {
            return new SmosFile(hdrFile, dblFile, format);
        } else if (isEcmwfFormat(formatName)) {
            return new SmosFile(hdrFile, dblFile, format);
        } else if (isDffLaiFormat(formatName)) {
            return new LaiFile(hdrFile, dblFile, format);
        } else if (isVTecFormat(formatName)) {
            return new VTecFile(hdrFile, dblFile, format);
        } else if (isLsMaskFormat(formatName)) {
            return new LsMaskFile(hdrFile, dblFile, format);
        } else {
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': unsupported SMOS data format ''{1}''.", file, formatName));
        }
    }

    SmosProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    public ExplorerFile getExplorerFile() {
        synchronized (this) {
            return explorerFile;
        }
    }

    @Override
    protected final Product readProductNodesImpl() throws IOException {
        synchronized (this) {
            final File inputFile = getInputFile();
            explorerFile = createExplorerFile(inputFile);

            final Product product = explorerFile.createProduct();
            if (explorerFile instanceof SmosFile) {
                addLandSeaMask(product);
            }

            product.setFileLocation(explorerFile.getDblFile());
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
            explorerFile.close();
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
}
