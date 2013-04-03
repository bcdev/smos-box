package org.esa.beam.smos.ee2netcdf.visat;

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.BindingContext;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.VectorDataNode;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.smos.gui.BindingConstants;
import org.esa.beam.smos.gui.DefaultChooserFactory;
import org.esa.beam.smos.gui.GuiHelper;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

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
        try {
            init(propertyContainer);
        } catch (ValidationException e) {
            throw new IllegalStateException(e.getMessage());
        }

        bindingContext = new BindingContext(propertyContainer);

        createUi();
    }

    private void init(PropertyContainer propertyContainer) throws ValidationException {
        propertyContainer.setDefaultValues();

        final File defaultSourceDirectory = GuiHelper.getDefaultSourceDirectory(appContext);
        propertyContainer.setValue(BindingConstants.SOURCE_DIRECTORY, defaultSourceDirectory);

        final Product selectedSmosProduct = DialogHelper.getSelectedSmosProduct(appContext);
        if (selectedSmosProduct != null) {
            final List<VectorDataNode> geometryNodeList = GuiHelper.getGeometries(selectedSmosProduct);
            if (!geometryNodeList.isEmpty()) {
                GuiHelper.bindGeometries(geometryNodeList, propertyContainer);
            }
        }
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
        final boolean canProductSelectionBeEnabled = DialogHelper.canProductSelectionBeEnabled(appContext);

        GuiHelper.addSourceProductsButtons(sourceProductPanel, canProductSelectionBeEnabled, bindingContext);

        final PropertyDescriptor sourceDirectoryDescriptor = propertyContainer.getDescriptor(BindingConstants.SOURCE_DIRECTORY);
        final JComponent fileEditor = GuiHelper.createFileEditorComponent(sourceDirectoryDescriptor, new DefaultChooserFactory(), bindingContext);

        layout.setCellPadding(2, 0, new Insets(0, 24, 3, 3));
        sourceProductPanel.add(fileEditor);

        return sourceProductPanel;
    }

}
