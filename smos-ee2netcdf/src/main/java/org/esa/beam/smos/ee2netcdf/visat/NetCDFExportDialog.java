package org.esa.beam.smos.ee2netcdf.visat;

import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.ModalDialog;

import javax.swing.*;

public class NetCDFExportDialog extends ModalDialog {

    public NetCDFExportDialog(AppContext appContext, String helpId) {
        super(appContext.getApplicationWindow(), "Convert SMOS EE File to NetCDF 4", ID_OK_CANCEL_HELP, helpId); /* I18N */

        createUi();
    }

    private void createUi() {
        final JPanel mainPanel = new JPanel();
        final BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);

        mainPanel.setLayout(layout);

        setContent(mainPanel);
    }
}
