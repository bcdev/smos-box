package org.esa.beam.smos.ee2netcdf.visat;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.smos.ee2netcdf.ConverterOp;
import org.esa.beam.smos.gui.BindingConstants;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ConverterSwingWorker extends ProgressMonitorSwingWorker<List<Exception>, File> {

    private AppContext appContext;
    private ExportParameter exportParameter;

    ConverterSwingWorker(AppContext appContext, ExportParameter exportParameter) {
        super(appContext.getApplicationWindow(), "Converting EE to NetCDF");
        this.appContext = appContext;
        this.exportParameter = exportParameter;
    }

    @Override
    protected List<Exception> doInBackground(ProgressMonitor pm) throws Exception {
        pm.beginTask("Converting product(s)", ProgressMonitor.UNKNOWN);
        final List<Exception> problemList = new ArrayList<Exception>();

        final HashMap<String, Object> parameterMap = createParameterMap(exportParameter);

        if (exportParameter.isUseSelectedProduct()) {
            final Product selectedProduct = appContext.getSelectedProduct();
            GPF.createProduct(ConverterOp.ALIAS, parameterMap, new Product[]{selectedProduct});
        } else {
            GPF.createProduct(ConverterOp.ALIAS, parameterMap);
        }

        pm.done();

        return problemList;
    }

    @Override
    protected void done() {
        // @todo 3 tb/tb duplicated code - extract common swing worker for SMOS stuff tb 2013-04-05
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

    // package access for testing only tb 2013-04-05
    static HashMap<String, Object> createParameterMap(ExportParameter exportParameter) {
        final HashMap<String, Object> parameterMap = new HashMap<String, Object>();

        final File targetDirectory = exportParameter.getTargetDirectory();
        if (targetDirectory != null) {
            parameterMap.put("targetDirectory", targetDirectory);
        }

        final File sourceDirectory = exportParameter.getSourceDirectory();
        if (sourceDirectory != null && !exportParameter.isUseSelectedProduct()) {
            final StringBuilder sourcePath = new StringBuilder();
            sourcePath.append(sourceDirectory.getAbsolutePath());
            sourcePath.append(File.separator);
            sourcePath.append("*");
            parameterMap.put("sourceProductPaths", sourcePath.toString());
        }

        final int roiType = exportParameter.getRoiType();
        if (roiType == BindingConstants.ROI_TYPE_AREA) {
            parameterMap.put("region", exportParameter.toAreaWKT());
        } else if (roiType == BindingConstants.ROI_TYPE_GEOMETRY) {
            // @todo 1 tb/tb write test 2013-04-08
            addSelectedProductGeometry(exportParameter.getGeometry(), parameterMap);
        } else if (roiType == BindingConstants.ROI_TYPE_PRODUCT) {
            parameterMap.remove("region");
        }

        return parameterMap;
    }

    private static void addSelectedProductGeometry(Geometry geometry, HashMap<String, Object> parameterMap) {
        if (geometry instanceof Polygon) {
            final WKTWriter wktWriter = new WKTWriter();
            final String multiPolygonWkt = wktWriter.write(geometry);
            parameterMap.put("region", multiPolygonWkt);
        }
    }
}
