package org.esa.beam.smos.visat;

import com.bc.ceres.binio.CompoundMember;
import com.bc.ceres.binio.CompoundType;
import com.jidesoft.swing.CheckBoxList;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.framework.ui.SelectExportMethodDialog;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.dataio.smos.L1cSmosFile;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.util.SystemUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.MessageFormat;

public class GridPointBtDataTableToolView extends GridPointBtDataToolView {

    public static final String ID = GridPointBtDataTableToolView.class.getName();

    private JTable table;
    private DefaultTableModel nullModel;
    private JButton columnsButton;
    private JButton exportButton;

    public GridPointBtDataTableToolView() {
        nullModel = new DefaultTableModel();
    }

    @Override
    protected void updateClientComponent(ProductSceneView smosView) {
        boolean enabled = smosView != null && getSelectedSmosFile() instanceof L1cSmosFile;
        table.setEnabled(enabled);
        columnsButton.setEnabled(enabled);
        exportButton.setEnabled(enabled);
    }


    @Override
    protected JComponent createGridPointComponent() {
        table = new JTable();
        return new JScrollPane(table);
    }

    @Override
    protected JComponent createGridPointComponentOptionsComponent() {
        columnsButton = new JButton("Columns...");
        columnsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final CompoundType btDataType = ((L1cSmosFile) getSelectedSmosFile()).getBtDataType();
                final CompoundMember[] members = btDataType.getMembers();
                String[] names = new String[members.length];
                for (int i = 0; i < members.length; i++) {
                    CompoundMember member = members[i];
                    names[i] = member.getName();
                }
                CheckBoxList checkBoxList = new CheckBoxList(names);

                final ModalDialog dialog = new ModalDialog(SwingUtilities.windowForComponent(columnsButton),
                                                           "Select Columns",
                                                           new JScrollPane(checkBoxList),
                                                           ModalDialog.ID_OK_CANCEL, null);
                final int i = dialog.show();
                if (i == ModalDialog.ID_OK) {
                    // todo - filter columns (nf,20081208)
                }

            }
        });

        exportButton = new JButton("Export...");
        exportButton.addActionListener(new ExportTableActionListener());

        final JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        optionsPanel.add(columnsButton);
        optionsPanel.add(exportButton);
        return optionsPanel;
    }


    @Override
    protected void updateGridPointBtDataComponent(GridPointBtDataset ds) {
        table.setModel(new GridPointBtDataTableModel(ds));
    }

    @Override
    protected void updateGridPointBtDataComponent(IOException e) {
        table.setModel(nullModel);
    }

    @Override
    protected void clearGridPointBtDataComponent() {
        table.setModel(nullModel);
    }

    private class ExportTableActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            final VisatApp app = VisatApp.getApp();
            if (table.getModel().getRowCount() == 0) {
                app.showInfoDialog("The table is empty!", null);
            }
            // Get export method from user
            final GridPointBtDataTableToolView tableToolView = GridPointBtDataTableToolView.this;
            final int method = SelectExportMethodDialog.run(tableToolView.getPaneWindow(), tableToolView.getTitle(),
                                                            "How do you want to export the table?", "");
            if (method == SelectExportMethodDialog.EXPORT_CANCELED) {
                return;
            }

            if (method == SelectExportMethodDialog.EXPORT_TO_FILE) {
                final File outFile = promptForFile(app, tableToolView.getTitle());
                if (outFile != null) {
                    OutputStream outputStream = null;
                    try {
                        outputStream = new BufferedOutputStream(new FileOutputStream(outFile));
                        exportTable(outputStream);
                    } catch (FileNotFoundException fnfe) {
                        fnfe.printStackTrace();
                        final String message = MessageFormat.format("The table could not be exported!\nReason: {0}",
                                                                    fnfe.getMessage());
                        app.showErrorDialog(message);
                    } finally {
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException ignore) {
                            }
                        }
                    }
                }
            } else if (method == SelectExportMethodDialog.EXPORT_TO_CLIPBOARD) {
                OutputStream outputStream = null;
                try {
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    outputStream = new BufferedOutputStream(out);
                    exportTable(outputStream);
                    outputStream.flush();
                    SystemUtils.copyToClipboard(out.toString());
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    final String message = MessageFormat.format("The table could not be exported!\nReason: {0}",
                                                                ioe.getMessage());
                    app.showErrorDialog(message);
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException ignore) {
                        }
                    }
                }
            }
        }

        private void exportTable(OutputStream outputStream) {
            final TableModelExporter exporter = new TableModelExporter(table.getModel());
            // todo - use filter defined by the column action (mp - 02.02.2009)
            // exporter.setColumnFilter();
            exporter.export(outputStream);
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
}