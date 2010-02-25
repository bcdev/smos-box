package org.esa.beam.smos.visat.export;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import com.vividsolutions.jts.geom.Geometry;
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

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;

class ExportSwingWorker extends ProgressMonitorSwingWorker<Void, Void> {

    private final AppContext appContext;

    @Parameter(alias = GridPointExportDialog.ALIAS_USE_SELECTED_PRODUCT)
    private boolean useSelectedProduct;

    @Parameter(alias = GridPointExportDialog.ALIAS_SOURCE_DIRECTORY)
    private File sourceDirectory;

    @Parameter(alias = GridPointExportDialog.ALIAS_RECURSIVE, defaultValue = "true")
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

    ExportSwingWorker(AppContext appContext) {
        super(appContext.getApplicationWindow(), "Exporting grid points");
        this.appContext = appContext;
    }

    @Override
    protected Void doInBackground(ProgressMonitor pm) throws Exception {
        GridPointFilterStream exportStream = null;
        try {
            final PrintWriter printWriter = new PrintWriter(targetFile);
            exportStream = new CsvExportStream(printWriter, ";");

            final Area area = getArea();
            final GridPointFilterStreamHandler handler = new GridPointFilterStreamHandler(exportStream, area);

            if (useSelectedProduct) {
                handler.processProduct(appContext.getSelectedProduct(), pm);
            } else {
                handler.processDirectory(sourceDirectory, recursive, pm);
            }
        } finally {
            if (exportStream != null) {
                exportStream.close();
            }
        }
        return null;
    }

    @Override
    protected void done() {
        try {
            get();
        } catch (InterruptedException e) {
            appContext.handleError(MessageFormat.format(
                    "An error occurred: {0}", e.getMessage()), e);
        } catch (ExecutionException e) {
            appContext.handleError(MessageFormat.format(
                    "An error occurred: {0}", e.getCause().getMessage()), e.getCause());
        }
    }

    private Area getArea() {
        switch (roiType) {
        case 0: {
            final Area area = new Area();
            final FeatureIterator<SimpleFeature> featureIterator = geometry.getFeatureCollection().features();

            while (featureIterator.hasNext()) {
                final Area featureArea;
                final SimpleFeature feature = featureIterator.next();
                if (feature.getFeatureType() == Placemark.getFeatureType()) {
                    featureArea = getAreaForPlacemarkFeature(feature);
                } else {
                    featureArea = getArea(feature);
                }
                if (featureArea != null && !featureArea.isEmpty()) {
                    area.add(featureArea);
                }
            }
            return area;
        }
        case 1: {
            final Area area = new Area();
            final PlacemarkGroup pinGroup = appContext.getSelectedProduct().getPinGroup();

            for (Placemark pin : pinGroup.toArray(new Placemark[pinGroup.getNodeCount()])) {
                area.add(getAreaForPlacemarkFeature(pin.getFeature()));
            }
            return area;
        }
        case 2: {
            return new Area(new Rectangle2D.Double(west, south, east - west, north - south));
        }
        default:
            // cannot happen
            throw new IllegalStateException(MessageFormat.format("Illegal ROI type: {0}", roiType));
        }
    }

    private Area getArea(SimpleFeature feature) {
        Shape shape = null;
        try {
            final Object geometry = feature.getDefaultGeometry();
            if (geometry instanceof Geometry) {
                shape = new LiteShape2((Geometry) geometry, null, null, true);
            }
        } catch (TransformException e) {
            // ignore
        } catch (FactoryException e) {
            // ignore
        }
        return shape != null ? new Area(shape) : null;
    }

    private Area getAreaForPlacemarkFeature(SimpleFeature feature) {
        final Point geometry = (Point) feature.getDefaultGeometry();
        double lon = geometry.getX();
        double lat = geometry.getY();

        final double x = lon - 0.08;
        final double y = lat - 0.08;
        final double w = 0.16;
        final double h = 0.16;

        return new Area(new Rectangle2D.Double(x, y, w, h));
    }
}
