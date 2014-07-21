package org.esa.beam.smos.ee2netcdf.visat;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.smos.ee2netcdf.EEToNetCDFExporterOp;
import org.esa.beam.smos.ee2netcdf.ExportParameter;
import org.esa.beam.smos.gui.BindingConstants;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

class ConverterSwingWorker extends ProgressMonitorSwingWorker<List<Exception>, File> {

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

        final ArrayList<Exception> exceptions = new ArrayList<>();

        final HashMap<String, Object> parameterMap = createParameterMap(exportParameter);

        try {
            if (exportParameter.isUseSelectedProduct()) {
                final Product selectedProduct = appContext.getSelectedProduct();
                GPF.createProduct(EEToNetCDFExporterOp.ALIAS, parameterMap, new Product[]{selectedProduct});
            } else {
                GPF.createProduct(EEToNetCDFExporterOp.ALIAS, parameterMap);
            }
        } catch (Exception e) {
            exceptions.add(e);
        }

        pm.done();

        return exceptions;
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
        final HashMap<String, Object> parameterMap = new HashMap<>();

        final File targetDirectory = exportParameter.getTargetDirectory();
        if (targetDirectory != null) {
            parameterMap.put("targetDirectory", targetDirectory);
        }

        final File sourceDirectory = exportParameter.getSourceDirectory();
        if (sourceDirectory != null && !exportParameter.isUseSelectedProduct()) {
            final String sourcePath = createInputPathWildcards(sourceDirectory);
            parameterMap.put("sourceProductPaths", sourcePath);
        }

        final int roiType = exportParameter.getRoiType();
        if (roiType == BindingConstants.ROI_TYPE_AREA) {
            parameterMap.put("region", exportParameter.toAreaWKT());
        } else if (roiType == BindingConstants.ROI_TYPE_GEOMETRY) {
            // @todo 1 tb/tb write test 2013-04-08
            addSelectedProductGeometry(exportParameter.getRegion(), parameterMap);
        } else if (roiType == BindingConstants.ROI_TYPE_PRODUCT) {
            parameterMap.remove("region");
        }

        if (exportParameter.isOverwriteTarget()) {
            parameterMap.put("overwriteTarget", "true");
        } else {
            parameterMap.put("overwriteTarget", "false");
        }

        return parameterMap;
    }

    // package access for testing only tb 2013-04-10
    static String createInputPathWildcards(File sourceDirectory) {
        final StringBuilder sourcePath = new StringBuilder();
        final String absolutePath = sourceDirectory.getAbsolutePath();
        sourcePath.append(absolutePath);
        sourcePath.append(File.separator);
        sourcePath.append("*.zip,");
        sourcePath.append(absolutePath);
        sourcePath.append(File.separator);
        sourcePath.append("*.dbl");
        return sourcePath.toString();
    }

    static void addSelectedProductGeometry(Geometry geometry, HashMap<String, Object> parameterMap) {
        if (geometry instanceof Polygon) {
            final WKTWriter wktWriter = new WKTWriter();
            final String multiPolygonWkt = wktWriter.write(geometry);
            parameterMap.put("region", multiPolygonWkt);
        }
    }
}
