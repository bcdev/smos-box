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

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command line tool for grid point export.
 *
 * @author Ralf Quast
 * @author Marco Zuehlke
 * @version $Revision$ $Date$
 * @since SMOS-Box 2.0
 */
public class GridPointExporter {

    private static Logger logger;


    static {
        logger = Logger.getLogger("org.esa.beam.smos");

        try {
            final FileHandler fileHandler = new FileHandler("export.log");
            fileHandler.setLevel(Level.ALL);
            logger.addHandler(fileHandler);

            final ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            logger.addHandler(consoleHandler);

            // handlers are set, so
            logger.setUseParentHandlers(false);
            logger.setLevel(Level.INFO);
        } catch (Exception e) {
            // ignore
        }
    }

    public static void main(String[] args) {
        final Arguments arguments = new Arguments(args, new ErrorHandler() {
            @Override
            public void warning(final Throwable t) {
            }

            @Override
            public void error(final Throwable t) {
                printUsage();
                System.exit(1);
            }
        });

        execute(arguments, new ErrorHandler() {
            @Override
            public void warning(final Throwable t) {
                logger.warning(t.getMessage());
            }

            @Override
            public void error(final Throwable t) {
                logger.severe(t.getMessage());
                t.printStackTrace(System.err);
                System.exit(1);
            }
        });
    }

    private static void execute(Arguments arguments, ErrorHandler errorHandler) {
        logger.info(MessageFormat.format("targetFile = {0}", arguments.targetFile));
        logger.info(MessageFormat.format("ROI = {0}", arguments.roi.getBounds2D()));

        PrintWriter printWriter = null;
        try {
            if (arguments.targetFile != null) {
                try {
                    printWriter = new PrintWriter(arguments.targetFile);
                } catch (FileNotFoundException e) {
                    errorHandler.error(e);
                }
            }
            if (printWriter == null) {
                printWriter = new PrintWriter(System.out);
            }
            final CsvExportStream csvExportStream = new CsvExportStream(printWriter, ";");
            final GridPointFilterStreamHandler streamHandler =
                    new GridPointFilterStreamHandler(csvExportStream, arguments.roi);

            for (final File sourceFile : arguments.sourceFiles) {
                try {
                    logger.info(MessageFormat.format("Exporting source file ''{0}''.", sourceFile.getPath()));
                    streamHandler.processDirectory(sourceFile, false, ProgressMonitor.NULL);
                } catch (IOException e) {
                    errorHandler.warning(e);
                }
            }
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }

    private static void printUsage() {
        System.out.println("SMOS-Box Grid Point Export command line tool, version 2.0");
        System.out.println("February 25, 2010");
        System.out.println();
        System.out.println("usage : export-grid-points [ROI] [-o targetFile] [sourceProduct ...]");
        System.out.println();
        System.out.println();
        System.out.println("ROI\n" +
                           "\n" +
                           "    [-box lon1 lon2 lat1 lat2] | [-point lon lat]\n" +
                           "        a region-of-interest either defined by a latitude-longitude\n" +
                           "        box or the coordinates of a DGG grid point");
        System.out.println();
        System.out.println("Note that each source product must be specified by the path name of\n" +
                           "the directory, which contains the SMOS '.HDR' and '.DBL' files.");
        System.out.println();
        System.out.println("When no target file is specified, the output is written to standard\n" +
                           "output, which usually is the console.");
        System.out.println();
        System.out.println();
    }

    private static class Arguments {

        Area roi;
        File targetFile;
        File[] sourceFiles;

        public Arguments(String[] args, ErrorHandler errorHandler) {
            try {
                parse(args);
            } catch (IllegalArgumentException e) {
                errorHandler.error(e);
            }
            if (roi == null) {
                roi = createBoxedArea(-180, 180, -90, 90);
            }
        }

        private void parse(String[] args) {
            if (args.length == 0) {
                throw new IllegalArgumentException("No arguments specified.");
            }
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-box")) {
                    if (args.length < i + 6) {
                        throw new IllegalArgumentException("Not enough arguments specified.");
                    }
                    if (roi != null) {
                        throw new IllegalArgumentException("A ROI may either be defined by '-box' or '-point'.");
                    }
                    final double lon1 = Double.valueOf(args[i + 1]);
                    ensureRange("lon1", lon1, -180, 180);
                    final double lon2 = Double.valueOf(args[i + 2]);
                    ensureRange("lon2", lon2, -180, 180);
                    if (lon2 <= lon1) {
                        throw new IllegalArgumentException("The value of 'lon2' must exceed the value of 'lon1'.");
                    }
                    final double lat1 = Double.valueOf(args[i + 3]);
                    ensureRange("lat1", lat1, -90, 90);
                    final double lat2 = Double.valueOf(args[i + 4]);
                    ensureRange("lat2", lat2, -90, 90);
                    if (lat2 <= lat1) {
                        throw new IllegalArgumentException("The value of 'lat2' must exceed the value of 'lat1'.");
                    }
                    roi = createBoxedArea(lon1, lon2, lat1, lat2);
                    i += 4;
                } else if (args[i].equals("-point")) {
                    if (args.length < i + 4) {
                        throw new IllegalArgumentException("Not enough arguments specified.");
                    }
                    if (roi != null) {
                        throw new IllegalArgumentException("A ROI may either be defined by '-box' or '-point'.");
                    }
                    final double lon = Double.valueOf(args[i + 1]);
                    ensureRange("lon", lon, -180, 180);
                    final double lat = Double.valueOf(args[i + 2]);
                    ensureRange("lat", lat, -90, 90);
                    roi = createPointArea(lon, lat);
                    i += 2;
                } else if (args[i].equals("-o")) {
                    if (args.length < i + 3) {
                        throw new IllegalArgumentException("Not enough arguments specified.");
                    }
                    targetFile = new File(args[i + 1]);
                    i += 1;
                } else if (args[i].startsWith("-")) {
                    throw new IllegalArgumentException("Illegal option.");
                } else {
                    final List<File> sourceFileList = new ArrayList<File>();
                    for (int j = i; j < args.length; j++) {
                        final File sourceFile = new File(args[j]);
                        if (sourceFile.isDirectory()) {
                            sourceFileList.add(sourceFile);
                        }
                    }
                    sourceFiles = sourceFileList.toArray(new File[sourceFileList.size()]);
                    i = args.length - 1;
                }
            }
        }

        private static void ensureRange(String name, double value, double min, double max) {
            if (value < min || value > max) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "The value of ''{0}'' = ''{1}'' is out of range [''{2}'', ''{3}''].", name, value, min, max));
            }
        }

        private static Area createBoxedArea(double lon1, double lon2, double lat1, double lat2) {
            return new Area(new Rectangle2D.Double(lon1, lat1, lon2 - lon1, lat2 - lat1));
        }

        private static Area createPointArea(double lon, double lat) {
            final double x = lon - 0.08;
            final double y = lat - 0.08;
            final double w = 0.16;
            final double h = 0.16;

            return new Area(new Rectangle2D.Double(x, y, w, h));
        }
    }

    private interface ErrorHandler {

        public void warning(final Throwable t);

        public void error(final Throwable t);
    }
}
