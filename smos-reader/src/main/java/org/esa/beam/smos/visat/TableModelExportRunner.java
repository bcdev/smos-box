package org.esa.beam.smos.visat;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import org.esa.beam.framework.ui.SelectExportMethodDialog;
import org.esa.beam.util.SystemUtils;
import org.esa.beam.visat.VisatApp;

import javax.swing.JOptionPane;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;

/**
 * A class to export a table model.
 *
 * @author Marco Peters
 * @version $Revision: $ $Date: $
 * @since BEAM 4.6
 */
class TableModelExportRunner {

    private final Component parentComponent;
    private String title;
    private final TableModel model;

    /**
     * Creates an instance of this class to export a table model.
     *
     * @param parentComponent The parent component to align displayed dialogs.
     * @param title The title of displayed dialogs.
     * @param model the model to export.
     */
    TableModelExportRunner(Component parentComponent, String title, TableModel model) {
        this.parentComponent = parentComponent;
        this.title = title;
        this.model = model;
    }

    /**
     * Starts the export process.
     */
    public void run() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(parentComponent,
                                  "The table is empty!",
                                  title,
                                  JOptionPane.INFORMATION_MESSAGE);
        }
        // Get export method from user
        final int method = SelectExportMethodDialog.run(parentComponent, title,
                                                        "How do you want to export the table?", "");
        if (method == SelectExportMethodDialog.EXPORT_CANCELED) {
            return;
        }

        if (method == SelectExportMethodDialog.EXPORT_TO_FILE) {
            final File outFile = promptForFile(title);
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
                    JOptionPane.showMessageDialog(parentComponent,
                                          message,
                                          title,
                                          JOptionPane.ERROR_MESSAGE);
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
                    JOptionPane.showMessageDialog(parentComponent,
                                          message,
                                          title,
                                          JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * Opens a modal file chooser dialog that prompts the user to select the output file name.
     *
     * @param defaultFileName The default file name.
     *
     * @return The selected file, <code>null</code> means "Cancel".
     */
    private static File promptForFile(String defaultFileName) {
        // Loop while the user does not want to overwrite a selected, existing file
        // or if the user presses "Cancel"
        //
        final String dlgTitle = "Export Table";
        File file = null;
        while (file == null) {
            final VisatApp app = VisatApp.getApp();
            file = app.showFileSaveDialog(dlgTitle,
                                               false,
                                               null,
                                               ".txt",
                                               defaultFileName,
                                               "exportSmosTable.lastDir");
            if (file == null) {
                return null; // Cancel
            } else if (file.exists()) {
                final String message = MessageFormat.format("The file ''{0}'' already exists.\nOverwrite it?", file);
                int status = JOptionPane.showConfirmDialog(app.getMainFrame(),
                                                           message,
                                                           MessageFormat.format("{0} - {1}", app.getAppName(),
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
