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
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import org.esa.beam.framework.datamodel.Placemark;
import org.esa.beam.framework.datamodel.PlacemarkGroup;
import org.esa.beam.framework.datamodel.VectorDataNode;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.ui.AppContext;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.LiteShape2;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

class GridPointExportSwingWorker extends ProgressMonitorSwingWorker<List<Exception>, File> {

    private final AppContext appContext;

    @Parameter(alias = GridPointExportDialog.ALIAS_USE_SELECTED_PRODUCT)
    private boolean useSelectedProduct;

    @Parameter(alias = GridPointExportDialog.ALIAS_SOURCE_DIRECTORY)
    private File sourceDirectory;

    @Parameter(alias = GridPointExportDialog.ALIAS_RECURSIVE, defaultValue = "false")
    private boolean recursive;

    @Parameter(alias = GridPointExportDialog.ALIAS_ROI_TYPE, defaultValue = "2", valueSet = {"0", "1", "2"})
    private int roiType;

    @Parameter(alias = GridPointExportDialog.ALIAS_GEOMETRY)
    private VectorDataNode geometry;

    @Parameter(alias = GridPointExportDialog.ALIAS_NORTH, defaultValue = "90.0", interval = "[-90.0, 90.0]")
    private double north;

    @Parameter(alias = GridPointExportDialog.ALIAS_SOUTH, defaultValue = "-90.0", interval = "[-90.0, 90.0]")
    private double south;

    @Parameter(alias = GridPointExportDialog.ALIAS_EAST, defaultValue = "180.0", interval = "[-180.0, 180.0]")
    private double east;

    @Parameter(alias = GridPointExportDialog.ALIAS_WEST, defaultValue = "-180.0", interval = "[-180.0, 180.0]")
    private double west;

    @Parameter(alias = GridPointExportDialog.ALIAS_TARGET_FILE, notNull = true, notEmpty = true)
    private File targetFile;

    @Parameter(alias = GridPointExportDialog.ALIAS_EXPORT_FORMAT, defaultValue = GridPointExportDialog.NAME_CSV,
               valueSet = {GridPointExportDialog.NAME_CSV, GridPointExportDialog.NAME_EEF})
    private String exportFormat;

    GridPointExportSwingWorker(AppContext appContext) {
        super(appContext.getApplicationWindow(), "Exporting grid points");
        this.appContext = appContext;
    }

    @Override
    protected List<Exception> doInBackground(ProgressMonitor pm) throws Exception {
        final List<Exception> problemList = new ArrayList<Exception>();

        GridPointFilterStream filterStream = null;
        try {
            if (GridPointExportDialog.NAME_CSV.equals(exportFormat)) {
                filterStream = new CsvExportStream(new PrintWriter(targetFile), ";");
            } else {
                filterStream = new EEExportStream(targetFile);
            }
            final GridPointFilter gridPointFilter = getGridPointFilter();
            final GridPointFilterStreamHandler handler = new GridPointFilterStreamHandler(filterStream,
                                                                                          gridPointFilter);
            if (useSelectedProduct) {
                handler.processProduct(appContext.getSelectedProduct(), pm);
            } else {
                handler.processDirectory(sourceDirectory, recursive, pm, problemList);
            }
        } finally {
            if (filterStream != null) {
                filterStream.close();
            }
        }
        return problemList;
    }

    @Override
    protected void done() {
        try {
            final List<Exception> problemList = get();
            if (!problemList.isEmpty()) {
                final StringBuilder message = new StringBuilder();
                message.append("The following problem(s) have occurred:\n");
                for (final Exception problem : problemList) {
                    problem.printStackTrace();
                    message.append("  ");
                    message.append(problem.getMessage());
                    message.append("\n");
                }
                appContext.handleError(message.toString(), null);
            }
        } catch (InterruptedException e) {
            appContext.handleError(MessageFormat.format(
                    "An error occurred: {0}", e.getMessage()), e);
        } catch (ExecutionException e) {
            appContext.handleError(MessageFormat.format(
                    "An error occurred: {0}", e.getCause().getMessage()), e.getCause());
        }
    }

    private GridPointFilter getGridPointFilter() {
        switch (roiType) {
            case 0: {
                final MultiFilter multiFilter = new MultiFilter();
                if (geometry != null) {
                    final FeatureIterator<SimpleFeature> featureIterator = geometry.getFeatureCollection().features();

                    while (featureIterator.hasNext()) {
                        final Shape featureShape;
                        final SimpleFeature feature = featureIterator.next();
                        if (feature.getDefaultGeometry() instanceof Point) {
                            featureShape = getPointShape(feature);
                        } else {
                            featureShape = getAreaShape(feature);
                        }
                        if (featureShape != null) {
                            multiFilter.add(new RegionFilter(featureShape));
                        }
                    }
                }
                return multiFilter;
            }
            case 1: {
                final MultiFilter multiFilter = new MultiFilter();
                final PlacemarkGroup pinGroup = appContext.getSelectedProduct().getPinGroup();
                for (Placemark pin : pinGroup.toArray(new Placemark[pinGroup.getNodeCount()])) {
                    multiFilter.add(new RegionFilter(getPointShape(pin.getFeature())));
                }
                return multiFilter;
            }
            case 2: {
                return new RegionFilter(new Rectangle2D.Double(west, south, east - west, north - south));
            }
            default:
                // cannot happen
                throw new IllegalStateException(MessageFormat.format("Illegal ROI type: {0}", roiType));
        }
    }

    private Shape getAreaShape(SimpleFeature feature) {
        try {
            final Object geometry = feature.getDefaultGeometry();
            if (geometry instanceof Geometry) {
                return new LiteShape2((Geometry) geometry, null, null, true);
            }
        } catch (TransformException e) {
            // ignore
        } catch (FactoryException e) {
            // ignore
        }
        return null;
    }

    private Shape getPointShape(SimpleFeature feature) {
        final Point point = (Point) feature.getDefaultGeometry();
        final double lon = point.getX();
        final double lat = point.getY();

        return new Rectangle2D.Double(lon, lat, 0.0, 0.0);
    }
}
