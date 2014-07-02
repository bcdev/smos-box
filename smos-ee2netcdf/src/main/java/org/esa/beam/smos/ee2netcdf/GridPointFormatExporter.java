package org.esa.beam.smos.ee2netcdf;

import com.bc.ceres.binio.DataFormat;
import org.esa.beam.dataio.netcdf.nc.N4FileWriteable;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.framework.datamodel.Product;

import java.io.File;
import java.io.IOException;

public class GridPointFormatExporter {

    public void write(Product product, File outputFile) throws IOException {
        final SmosFile smosFile = getSmosFile(product);
        final DataFormat dataFormat = smosFile.getDataFormat();
        final Dddb dddb = Dddb.getInstance();
//        final Family<BandDescriptor> bandDescriptors = dddb.getBandDescriptors(dataFormat.getName());
//        final List<BandDescriptor> bandDescriptorList = bandDescriptors.asList();
//        for (BandDescriptor aBandDescriptorList : bandDescriptorList) {
//            System.out.println(aBandDescriptorList.getMemberName());
//        }
//        System.out.println("000000000000000000000000000000000000000000000000000000000000000000000000000");

        final FormatExporter exporter = FormatExporterFactory.create(product.getFileLocation().getName());
        exporter.initialize(product);

        final NFileWriteable nFileWriteable = N4FileWriteable.create(outputFile.getPath());

        exporter.addGlobalAttributes(nFileWriteable, product.getMetadataRoot());
        exporter.addDimensions(nFileWriteable);
        exporter.addVariables(nFileWriteable);

        nFileWriteable.create();

        exporter.writeData(nFileWriteable);

        nFileWriteable.close();
    }

    public static SmosFile getSmosFile(Product product) {
        final SmosProductReader smosReader = (SmosProductReader) product.getProductReader();
        return (SmosFile) smosReader.getProductFile();
    }
}
