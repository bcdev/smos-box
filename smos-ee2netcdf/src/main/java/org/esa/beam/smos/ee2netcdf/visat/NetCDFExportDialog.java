package org.esa.beam.smos.ee2netcdf.visat;

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.BindingContext;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.ModalDialog;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

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
        final JPanel mainPanel = new JPanel();
        final BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);

        mainPanel.setLayout(layout);

        mainPanel.add(createSourceProductsPanel());
        setContent(mainPanel);
    }

    private JComponent createSourceProductsPanel() {
        final JRadioButton useSelectedProductButton = new JRadioButton("Use selected SMOS product");
        final JRadioButton useAllProductsInDirectoryButton = new JRadioButton("Use all SMOS products in directory:");
        final Map<AbstractButton, Object> buttonGroupValueSet = new HashMap<AbstractButton, Object>();
        buttonGroupValueSet.put(useSelectedProductButton, true);
        buttonGroupValueSet.put(useAllProductsInDirectoryButton, false);

        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(useSelectedProductButton);
        buttonGroup.add(useAllProductsInDirectoryButton);
        bindingContext.bind("useSelectedProduct", buttonGroup, buttonGroupValueSet);

        setEnabledStateForProductSelection(useSelectedProductButton);

        final TableLayout layout = new TableLayout(1);
        layout.setTableAnchor(TableLayout.Anchor.WEST);
        layout.setTableFill(TableLayout.Fill.HORIZONTAL);
        layout.setTablePadding(3, 3);
        layout.setTableWeightX(1.0);

        final JPanel sourceProductPanel = new JPanel(layout);
        sourceProductPanel.setBorder(BorderFactory.createTitledBorder("Source Products"));
        sourceProductPanel.add(useSelectedProductButton);
        sourceProductPanel.add(useAllProductsInDirectoryButton);
//        layout.setCellPadding(2, 0, new Insets(0, 24, 3, 3));
//        sourceProductPanel.add(fileEditor);
//        layout.setCellPadding(3, 0, new Insets(0, 24, 3, 3));
//        sourceProductPanel.add(checkBox);

        return sourceProductPanel;
    }

    private void setEnabledStateForProductSelection(JRadioButton useSelectedProductButton) {
        final Product selectedProduct = appContext.getSelectedProduct();
        if (selectedProduct != null) {
            final String productType = selectedProduct.getProductType();
            if (!isSupportedType(productType)) {
                useSelectedProductButton.setSelected(false);
                useSelectedProductButton.setEnabled(false);
            }
        } else {
            useSelectedProductButton.setSelected(false);
            useSelectedProductButton.setEnabled(false);
        }
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
}
