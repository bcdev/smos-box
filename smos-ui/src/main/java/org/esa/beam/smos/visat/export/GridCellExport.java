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

import com.bc.ceres.core.ProgressMonitor;

import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.framework.datamodel.Product;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Command line toll for grid cell export
 *
 * @author Marco Zuehlke
 * @version $Revision$ $Date$
 * @since SMOS-Box 2.0
 */
public class GridCellExport {
    
    public static void main(String[] args) {
        Arguments arguments = new Arguments(args);
        PrintWriter printWriter;
        if (arguments.outputfile != null) {
            try {
                printWriter = new PrintWriter(arguments.outputfile);
            } catch (FileNotFoundException e) {
                System.err.println("Could not print to file: "+arguments.outputfile.getName());
                e.printStackTrace();
                return;
            }
        } else {
            printWriter = new PrintWriter(System.out);
        }
        CsvExport csvExport = new CsvExport(printWriter, ";");
        GridPointFilterStreamHandler streamHandler = new GridPointFilterStreamHandler(csvExport, arguments.area);
        
        ProductReader smosProductReader = ProductIO.getProductReader("SMOS");
        ProductReaderPlugIn readerPlugIn = smosProductReader.getReaderPlugIn();
        Set<String> handledProductNames = new HashSet<String>();
        for (File file : arguments.inputFiles) {
            DecodeQualification qualification = readerPlugIn.getDecodeQualification(file);
            if (qualification.equals(DecodeQualification.INTENDED)) {
                try {
                    Product product = smosProductReader.readProductNodes(file, null);
                    String productName = product.getName();
                    if (!handledProductNames.contains(productName)) {
                        streamHandler.processProduct(product, ProgressMonitor.NULL);
                        handledProductNames.add(productName);
                    }
                } catch (Exception e) {
                    // ignore
                }
            } else {
                System.err.println("Not a SMOS product: "+file.getAbsolutePath());
            }
        }
    }
    
    
    
    private static class Arguments {

        Area area;
        File outputfile;
        File[] inputFiles;
        
        public Arguments(String[] args) {
            try {
                parse(args);
            } catch (Exception e) {
                printHelp();
            }
            if (area == null) {
                area = computeBoxArea(-180, 180, -90, 90);
            }
        }
        
        private void printHelp() {
            System.err.println("Usage: [-box <lon1> <lon2> <lat1> <lat2>|-point lon lat][-o output] smosProducts...");
            System.exit(1);
        }
        
        private void parse(String[] args) {
            if (args.length == 0) {
                printHelp();
            }
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-box")) {
                    if (args.length < i+6) {
                        printHelp();
                    }
                    if (area != null) {
                        System.err.println("Only one of the parameter '-box' or '-point' can specified.");
                        printHelp();
                    }
                    double lon1 = Double.valueOf(args[i+1]);
                    rangeCheck(lon1, "lon1", -180, 180);
                    double lon2 = Double.valueOf(args[i+2]);
                    rangeCheck(lon2, "lon2", -180, 180);
                    if (lon2 <= lon1) {
                        System.err.println("The specified value for lon2 can not be smaller than value for lon1.");
                        System.err.println("lon2="+lon2);
                        System.err.println("lon1="+lon1);
                        printHelp();
                    }
                    double lat1 = Double.valueOf(args[i+3]);
                    rangeCheck(lat1, "lat1", -90, 90);
                    double lat2 = Double.valueOf(args[i+4]);
                    rangeCheck(lat2, "lat2", -90, 90);
                    if (lat2 <= lat1) {
                        System.err.println("The specified value for lat2 can not be smaller than value for lat1.");
                        System.err.println("lat2="+lat2);
                        System.err.println("lat1="+lat1);
                        printHelp();
                    }
                    area = computeBoxArea(lon1, lon2, lat1, lat2);
                    i += 4;
                } else if (args[i].equals("-point")) {
                    if (args.length < i+4) {
                        printHelp();
                    }
                    if (area != null) {
                        System.err.println("Only one of the parameter '-box' or '-point' can specified.");
                        printHelp();
                    }
                    double lon = Double.valueOf(args[i+1]);
                    rangeCheck(lon, "lon", -180, 180);
                    double lat = Double.valueOf(args[i+2]);
                    rangeCheck(lat, "lat", -90, 90);
                    area = computePointArea(lon, lat);
                    i += 2;
                } else if (args[i].equals("-o")) {
                    if (args.length < i+3) {
                        printHelp();
                    }
                    outputfile = new File(args[i+1]);
                    i += 1;
                } else if (args[i].startsWith("-")) {
                    printHelp();
                } else {
                    List<File> files = new ArrayList<File>();
                    for (int j = i; j < args.length; j++) {
                        File file = new File(args[j]);
                        files.add(file);
                    }
                    inputFiles = files.toArray(new File[files.size()]);
                    i = args.length - 1;
                }
            }
        }
        
        private void rangeCheck(double value, String name, double min, double max) {
            if (value < min || value > max) {
                System.err.println("The value for '"+name+"' must be between '"+min+"' and '"+max+"'.");
                System.err.println("Was: "+value);
                printHelp();
            }
        }
        
        private static Area computeBoxArea(double lon1, double lon2, double lat1, double lat2) {
            final double x = lon1;
            final double y = lat1;
            final double w = lon2 - lon1;
            final double h = lat2 - lat1;
            return new Area(new Rectangle2D.Double(x, y, w, h));
        }
        
        private static Area computePointArea(double lon, double lat) {
            final double hw = 0.08;
            final double hh = 0.08;
            
            final double x = lon - hw;
            final double y = lat - hh;
            final double w = 0.16;
            final double h = 0.16;

            return new Area(new Rectangle2D.Double(x, y, w, h));
        }
    }

}
