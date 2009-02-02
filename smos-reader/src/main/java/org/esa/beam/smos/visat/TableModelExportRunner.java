package org.esa.beam.smos.visat;

import org.esa.beam.visat.VisatApp;
import org.esa.beam.framework.ui.SelectExportMethodDialog;
import org.esa.beam.util.SystemUtils;

import java.awt.Component;
import java.io.*;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;

import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import com.bc.ceres.core.ProgressMonitor;

import javax.swing.JOptionPane;
import javax.swing.table.TableModel;

/**
 * todo - add API doc
 *
 * @author Marco Peters
 * @version $Revision: $ $Date: $
 * @since BEAM 4.6
 */
class TableModelExportRunner {

    private final Component parentComponent;
    private String title;
    private final TableModel model;

    TableModelExportRunner(Component parentComponent, String title, TableModel model) {
        this.parentComponent = parentComponent;
        this.title = title;
        this.model = model;
    }

    public void run() {
        final VisatApp app = VisatApp.getApp();
        if (model.getRowCount() == 0) {
            app.showInfoDialog("The table is empty!", null);
        }
        // Get export method from user
        final int method = SelectExportMethodDialog.run(parentComponent, title,
                                                        "How do you want to export the table?", "");
        if (method == SelectExportMethodDialog.EXPORT_CANCELED) {
            return;
        }

        if (method == SelectExportMethodDialog.EXPORT_TO_FILE) {
            final File outFile = promptForFile(app, title);
            if (outFile != null) {
                final TableModelExporter exporter = new TableModelExporter(model);
                // todo - use filter defined by the column action (mp - 02.02.2009)
                // exporter.setColumnFilter();
                exportToFile(outFile, exporter);
            }
        } else if (method == SelectExportMethodDialog.EXPORT_TO_CLIPBOARD) {
            final TableModelExporter exporter = new TableModelExporter(model);
            // todo - use filter defined by the column action (mp - 02.02.2009)
            // exporter.setColumnFilter();
            exportToClipboard(exporter);

        }
    }

    private void exportToFile(final File outFile, final TableModelExporter exporter) {
        final ProgressMonitorSwingWorker worker = new ProgressMonitorSwingWorker(parentComponent,
                                                                                 "Table Model Export") {
            @Override
            protected Object doInBackground(ProgressMonitor pm) throws Exception {
                pm.beginTask("Exporting table model...", 1);
                final OutputStream stream = new BufferedOutputStream(new FileOutputStream(outFile));
                try {
                    exporter.export(stream);
                } finally {
                    stream.close();
                    pm.done();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException ignore) {
                } catch (ExecutionException e) {
                    e.getCause().printStackTrace();
                        final String message = MessageFormat.format(
                                "The table could not be exported!\nReason: {0}",
                                e.getCause().getMessage());
                        VisatApp.getApp().showErrorDialog(message);
                }

            }
        };
        worker.execute();
    }

    private void exportToClipboard(final TableModelExporter exporter) {
        final ProgressMonitorSwingWorker worker = new ProgressMonitorSwingWorker(parentComponent,
                                                                                 "Table Model Export") {
            @Override
            protected Object doInBackground(ProgressMonitor pm) throws Exception {
                pm.beginTask("Exporting table model...", 1);
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                final OutputStream stream = new BufferedOutputStream(out);
                try {
                    exporter.export(stream);
                } finally {
                    stream.close();
                    pm.done();
                }
                return out.toString();
            }

            @Override
            protected void done() {
                try {
                    final Object result = get();
                    if (result instanceof String) {
                        SystemUtils.copyToClipboard((String) result);
                    }
                } catch (InterruptedException ignore) {
                } catch (ExecutionException exex) {
                    final Throwable cause = exex.getCause();
                    cause.printStackTrace();
                    final String message = MessageFormat.format("The table could not be exported!\nReason: {0}",
                                                                cause.getMessage());
                    VisatApp.getApp().showErrorDialog(message);
                }
            }
        };
        worker.execute();
    }

    /**
     * Opens a modal file chooser dialog that prompts the user to select the output file name.
     *
     * @param visatApp        An instance of the VISAT application.
     * @param defaultFileName The default file name.
     *
     * @return The selected file, <code>null</code> means "Cancel".
     */
    private File promptForFile(final VisatApp visatApp, String defaultFileName) {
        // Loop while the user does not want to overwrite a selected, existing file
        // or if the user presses "Cancel"
        //
        final String dlgTitle = "Export Table";
        File file = null;
        while (file == null) {
            file = visatApp.showFileSaveDialog(dlgTitle,
                                               false,
                                               null,
                                               ".txt",
                                               defaultFileName,
                                               "exportSmosTable.lastDir");
            if (file == null) {
                return null; // Cancel
            } else if (file.exists()) {
                int status = JOptionPane.showConfirmDialog(visatApp.getMainFrame(),
                                                           "The file '" + file + "' already exists.\n" + /*I18N*/
                                                           "Overwrite it?",
                                                           MessageFormat.format("{0} - {1}", visatApp.getAppName(),
                                                                                dlgTitle),
                                                           JOptionPane.YES_NO_CANCEL_OPTION,
                                                           JOptionPane.WARNING_MESSAGE);
                if (status == JOptionPane.CANCEL_OPTION) {
                    return null; // Cancel
                } else if (status == JOptionPane.NO_OPTION) {
                    file = null; // No, do not overwrite, let user select other file
                }
            }
        }
        return file;
    }

}
