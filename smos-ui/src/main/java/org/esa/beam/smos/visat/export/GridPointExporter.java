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
package org.esa.beam.smos.visat.export;

import com.bc.ceres.core.ProgressMonitor;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
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
                System.err.println(t.getMessage());
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
                System.err.println(t.getMessage());
                logger.severe(t.getMessage());
                final StackTraceElement[] stackTraceElements = t.getStackTrace();
                for (final StackTraceElement stackTraceElement : stackTraceElements) {
                    logger.severe(stackTraceElement.toString());
                }
                System.exit(1);
            }
        });
    }

    private static void execute(Arguments arguments, ErrorHandler errorHandler) {
        logger.info(MessageFormat.format("targetFile = {0}", arguments.targetFile));
        logger.info(MessageFormat.format("ROI = {0}", arguments.roi.getBounds2D()));

        GridPointFilterStream filterStream = null;
        try {
            filterStream = createGridPointFilterStream(arguments);
            final GridPointFilterStreamHandler streamHandler = new GridPointFilterStreamHandler(filterStream,
                                                                                                arguments.roi);
            for (final File sourceFile : arguments.sourceFiles) {
                try {
                    logger.info(MessageFormat.format("Exporting source file ''{0}''.", sourceFile.getPath()));
                    streamHandler.processDirectory(sourceFile, false, ProgressMonitor.NULL);
                } catch (IOException e) {
                    errorHandler.warning(e);
                }
            }
        } catch (Exception e) {
            errorHandler.error(e);
        } finally {
            if (filterStream != null) {
                try {
                    filterStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private static GridPointFilterStream createGridPointFilterStream(Arguments arguments) throws IOException {
        if (arguments.targetFile != null) {
            if (arguments.targetFile.isDirectory()) {
                return new EEExportStream(arguments.targetFile);
            } else {
                return new CsvExportStream(new PrintWriter(arguments.targetFile), ";");
            }
        }
        return new CsvExportStream(new PrintWriter(System.out), ";");
    }

    private static void printUsage() {
        System.out.println("SMOS-Box Grid Point Export command line tool, version 2.1");
        System.out.println("July 16, 2010");
        System.out.println();
        System.out.println("usage : export-grid-points [ROI] [-o targetFile] [sourceProduct ...]");
        System.out.println();
        System.out.println("ROI\n" +
                           "\n" +
                           "    [-box minLon maxLon minLat maxLat] | [-point lon lat]\n" +
                           "        a region-of-interest either defined by a latitude-longitude\n" +
                           "        box or the coordinates of a DGG grid point");
        System.out.println();
        System.out.println("Note that each source product must be specified by the path name of\n" +
                           "the directory, which contains the SMOS '.HDR' and '.DBL' files.");
        System.out.println();
        System.out.println("If the target file is a directory, the grid point data are exported" +
                           "into of EE formatted files, which reside in the target directory.\n" +
                           "If the target file is a normal file, the grid point data are stored" +
                           "into the target file in form of a CSV table.\n");
        System.out.println("If no target file is specified,  the grid point data are printed to" +
                           "the console (in CSV format).");
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
                    final double minLon = Double.valueOf(args[i + 1]);
                    ensureRange("minLon", minLon, -180, 180);
                    final double maxLon = Double.valueOf(args[i + 2]);
                    ensureRange("maxLon", maxLon, -180, 180);
                    if (maxLon <= minLon) {
                        throw new IllegalArgumentException("The value of 'maxLon' must exceed the value of 'minLon'.");
                    }
                    final double minLat = Double.valueOf(args[i + 3]);
                    ensureRange("minLat", minLat, -90, 90);
                    final double maxLat = Double.valueOf(args[i + 4]);
                    ensureRange("maxLat", maxLat, -90, 90);
                    if (maxLat <= minLat) {
                        throw new IllegalArgumentException("The value of 'maxLat' must exceed the value of 'minLat'.");
                    }
                    roi = createBoxedArea(minLon, maxLon, minLat, maxLat);
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
