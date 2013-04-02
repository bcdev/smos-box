package org.esa.beam.smos.ee2netcdf.visat;

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.BindingContext;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.smos.gui.GuiHelper;

import javax.swing.*;

public class NetCDFExportDialog extends ModalDialog {

    private final ExportParameter exportParameter;
    private final PropertyContainer propertyContainer;
    private final AppContext appContext;
    private final BindingContext bindingContext;

    public NetCDFExportDialog(AppContext appContext, String helpId) {
        super(appContext.getApplicationWindow(), "Convert SMOS EE File to NetCDF 4", ID_OK_CANCEL_HELP, helpId); /* I18N */

        this.appContext = appContext;
        exportParameter = new ExportParameter();
        propertyContainer = PropertyContainer.createObjectBacked(exportParameter);
        bindingContext = new BindingContext(propertyContainer);

        createUi();
    }

    private void createUi() {
        final JPanel mainPanel = GuiHelper.createPanelWithBoxLayout();

        mainPanel.add(createSourceProductsPanel());
        setContent(mainPanel);
    }

    private JComponent createSourceProductsPanel() {
        final TableLayout layout = GuiHelper.createWeightedTablelayout(1);
        final JPanel sourceProductPanel = new JPanel(layout);
        sourceProductPanel.setBorder(BorderFactory.createTitledBorder("Source Products"));
        final boolean canProductSelectionBeEnabled = canProductSelectionBeEnabled(appContext);

        GuiHelper.addSourceProductsButtons(sourceProductPanel, canProductSelectionBeEnabled, bindingContext);

//        layout.setCellPadding(2, 0, new Insets(0, 24, 3, 3));
//        sourceProductPanel.add(fileEditor);
//        layout.setCellPadding(3, 0, new Insets(0, 24, 3, 3));
//        sourceProductPanel.add(checkBox);

        return sourceProductPanel;
    }

    // package access for testing only tb 2013-03-26
    static boolean isSupportedType(String productType) {
        return "MIR_BWLF1C".equalsIgnoreCase(productType) ||
                "MIR_BWSF1C".equalsIgnoreCase(productType) ||
                "MIR_BWLD1C".equalsIgnoreCase(productType) ||
                "MIR_BWSD1C".equalsIgnoreCase(productType) ||
                "MIR_SCSD1C".equalsIgnoreCase(productType) ||
                "MIR_SCLD1C".equalsIgnoreCase(productType) ||
                "MIR_SCSF1C".equalsIgnoreCase(productType) ||
                "MIR_SCLF1C".equalsIgnoreCase(productType) ||
                "MIR_OSUDP2".equalsIgnoreCase(productType) ||
                "MIR_SMUDP2".equalsIgnoreCase(productType);
    }

    // package access for testing only tb 2013-04-02
    static boolean canProductSelectionBeEnabled(AppContext appContext) {
        final Product selectedProduct = appContext.getSelectedProduct();
        if (selectedProduct != null) {
            return isSupportedType(selectedProduct.getProductType());
        }
        return false;
    }
}
