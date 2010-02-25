/*
 * $Id: $
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Command line tool for grid cell export.
 *
 * @author Marco Zuehlke
 * @version $Revision$ $Date$
 * @since SMOS-Box 2.0
 */
public class GridPointExporter {

    public static void main(String[] args) {
        final Arguments arguments = new Arguments(args);
        final PrintWriter printWriter;

        if (arguments.outputfile != null) {
            try {
                printWriter = new PrintWriter(arguments.outputfile);
            } catch (FileNotFoundException e) {
                System.err.println(MessageFormat.format(
                        "Could not write to file: ''{0}''.", arguments.outputfile.getName()));
                e.printStackTrace();
                return;
            }
        } else {
            printWriter = new PrintWriter(System.out);
        }

        final CsvExportStream csvExportStream = new CsvExportStream(printWriter, ";");
        final GridPointFilterStreamHandler streamHandler = new GridPointFilterStreamHandler(csvExportStream,
                                                                                            arguments.area);
        final ProductReader smosProductReader = ProductIO.getProductReader("SMOS");
        final ProductReaderPlugIn readerPlugIn = smosProductReader.getReaderPlugIn();
        final Set<File> handledProductFiles = new HashSet<File>();

        try {
            for (final File sourceFile : arguments.sourceFiles) {
                final DecodeQualification qualification = readerPlugIn.getDecodeQualification(sourceFile);
                if (qualification.equals(DecodeQualification.INTENDED)) {
                    Product product = null;
                    try {
                        product = smosProductReader.readProductNodes(sourceFile, null);
                        final File productFile = product.getFileLocation();
                        if (!handledProductFiles.contains(productFile)) {
                            streamHandler.processProduct(product, ProgressMonitor.NULL);
                            handledProductFiles.add(productFile);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getMessage());
                    } finally {
                        if (product != null) {
                            product.dispose();
                        }
                    }
                } else {
                    System.err.println("Not a SMOS product: " + sourceFile.getAbsolutePath());
                }
            }
        } finally {
            printWriter.close();
        }
    }


    private static class Arguments {

        Area area;
        File outputfile;
        File[] sourceFiles;

        public Arguments(String[] args) {
            try {
                parse(args);
            } catch (Exception e) {
                printHelpAndExit();
            }
            if (area == null) {
                area = getBoxArea(-180, 180, -90, 90);
            }
        }

        private void parse(String[] args) {
            if (args.length == 0) {
                printHelpAndExit();
            }
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-box")) {
                    if (args.length < i + 6) {
                        printHelpAndExit();
                    }
                    if (area != null) {
                        System.err.println("Either the parameter '-box' or the parameter '-point' can be specified.");
                        printHelpAndExit();
                    }
                    final double lon1 = Double.valueOf(args[i + 1]);
                    rangeCheck(lon1, "lon1", -180, 180);
                    final double lon2 = Double.valueOf(args[i + 2]);
                    rangeCheck(lon2, "lon2", -180, 180);
                    if (lon2 <= lon1) {
                        System.err.println("The value of 'lon2' must exceed the value of 'lon1'.");
                        System.err.println("lon1 =" + lon1);
                        System.err.println("lon2 =" + lon2);
                        printHelpAndExit();
                    }
                    final double lat1 = Double.valueOf(args[i + 3]);
                    rangeCheck(lat1, "lat1", -90, 90);
                    final double lat2 = Double.valueOf(args[i + 4]);
                    rangeCheck(lat2, "lat2", -90, 90);
                    if (lat2 <= lat1) {
                        System.err.println("The value of 'lat2' must exceed the value of 'lat1'.");
                        System.err.println("lat1=" + lat1);
                        System.err.println("lat2=" + lat2);
                        printHelpAndExit();
                    }
                    area = getBoxArea(lon1, lon2, lat1, lat2);
                    i += 4;
                } else if (args[i].equals("-point")) {
                    if (args.length < i + 4) {
                        printHelpAndExit();
                    }
                    if (area != null) {
                        System.err.println("Either the parameter '-box' or the parameter '-point' can be specified.");
                        printHelpAndExit();
                    }
                    final double lon = Double.valueOf(args[i + 1]);
                    rangeCheck(lon, "lon", -180, 180);
                    final double lat = Double.valueOf(args[i + 2]);
                    rangeCheck(lat, "lat", -90, 90);
                    area = getPointArea(lon, lat);
                    i += 2;
                } else if (args[i].equals("-o")) {
                    if (args.length < i + 3) {
                        printHelpAndExit();
                    }
                    outputfile = new File(args[i + 1]);
                    i += 1;
                } else if (args[i].startsWith("-")) {
                    printHelpAndExit();
                } else {
                    final List<File> sourceFileList = new ArrayList<File>();
                    for (int j = i; j < args.length; j++) {
                        final File file = new File(args[j]);
                        sourceFileList.add(file);
                    }
                    sourceFiles = sourceFileList.toArray(new File[sourceFileList.size()]);
                    i = args.length - 1;
                }
            }
        }

        private static void rangeCheck(double value, String name, double min, double max) {
            if (value < min || value > max) {
                System.err.println(MessageFormat.format("The value of ''{0}'' must be between ''{1}'' and ''{2}''.",
                                                        name, min, max));
                System.err.println(MessageFormat.format("The actual value is: {0}", value));
                printHelpAndExit();
            }
        }

        private static void printHelpAndExit() {
            System.out.println(
                    "Usage: [-box <lon1> <lon2> <lat1> <lat2>]|[-point lon lat] [-o output] smosProducts...");
            System.exit(1);
        }

        private static Area getBoxArea(double lon1, double lon2, double lat1, double lat2) {
            return new Area(new Rectangle2D.Double(lon1, lat1, lon2 - lon1, lat2 - lat1));
        }

        private static Area getPointArea(double lon, double lat) {
            final double x = lon - 0.08;
            final double y = lat - 0.08;
            final double w = 0.16;
            final double h = 0.16;

            return new Area(new Rectangle2D.Double(x, y, w, h));
        }
    }
}
