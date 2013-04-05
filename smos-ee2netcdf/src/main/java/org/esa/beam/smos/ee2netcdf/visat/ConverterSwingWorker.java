package org.esa.beam.smos.ee2netcdf.visat;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.ui.AppContext;

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
            GPF.createProduct("SmosEE2NetCDF", parameterMap, new Product[]{selectedProduct});
        } else {
            // extract input path
            // run GPF
        }

        //GPF.createProduct("SmosEE2NetCDF", );

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
            parameterMap.put("sourceProductPaths", sourceDirectory.getAbsolutePath());
        }

        return parameterMap;
    }
}
