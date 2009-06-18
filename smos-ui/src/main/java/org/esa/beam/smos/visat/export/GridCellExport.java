/*
 * $Id: $
 *
 * Copyright (C) 2009 by Brockmann Consult (info@brockmann-consult.de)
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
package org.esa.beam.smos.visat.export;

import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.framework.datamodel.Product;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Command line toll for grid cell export
 *
 * @author Marco Zuehlke
 * @version $Revision$ $Date$
 * @since BEAM 4.2
 */
public class GridCellExport {
    
    public static void main(String[] args) {
        Arguments arguments = new Arguments(args);
        Area area = computeArea(arguments);
        
        PrintWriter printWriter = new PrintWriter(System.out);
        CsvGridExport csvGridExport = new CsvGridExport(printWriter);
        GridPointFilterStreamHandler streamHandler = new GridPointFilterStreamHandler(csvGridExport, area);
        
        ProductReader smosProductReader = ProductIO.getProductReader("SMOS");
        ProductReaderPlugIn readerPlugIn = smosProductReader.getReaderPlugIn();
        for (File file : arguments.inputFiles) {
            DecodeQualification qualification = readerPlugIn.getDecodeQualification(file);
            if (qualification.equals(DecodeQualification.INTENDED)) {
                try {
                    Product product = smosProductReader.readProductNodes(file, null);
                  streamHandler.processProduct(product);
                } catch (Exception e) {
                    // ignore
                }
            } else {
                System.err.println("Not a SMOS product: "+file.getAbsolutePath());
            }
        }
    }
    
    private static Area computeArea(Arguments arguments) {
        final double x = arguments.west;
        final double y = arguments.south;
        final double w = arguments.east - arguments.west;
        final double h = arguments.north - arguments.south;
        return new Area(new Rectangle2D.Double(x, y, w, h));
    }
    
    private static class Arguments {

        private double north = 90;
        private double south = -90;
        private double east = 180;
        private double west = -180;
        
        private File[] inputFiles;
        
        public Arguments(String[] args) {
            try {
                parse(args);
            } catch (Exception e) {
                printHelp();
            }
        }
        
        private void printHelp() {
            System.err.println("Usage: [-roi <north> <south> <east> <west>] smosProducts...");
            System.exit(1);
        }
        
        private void parse(String[] args) {
            if (args.length == 0) {
                printHelp();
            }
            int productStartIndex = 0;
            if (args[0].startsWith("-roi")) {
                if (args.length < 6) {
                    printHelp();
                }
                north = Double.valueOf(args[1]);
                south = Double.valueOf(args[2]);
                if (north <= south) {
                    System.err.println("Value for north can not be smaller than value for south.");
                    System.err.println("north="+north);
                    System.err.println("south="+south);
                    printHelp();
                }
                east = Double.valueOf(args[3]);
                west = Double.valueOf(args[4]);
                if (east <= west) {
                    System.err.println("Value for east can not be smaller than value for west.");
                    System.err.println("east="+east);
                    System.err.println("west="+west);
                    printHelp();
                }
                productStartIndex = 5;
            }
            List<File> files = new ArrayList<File>();
            for (int i = productStartIndex; i < args.length; i++) {
                File file = new File(args[i]);
                files.add(file);
            }
            inputFiles = files.toArray(new File[files.size()]);
        }
    }

}
