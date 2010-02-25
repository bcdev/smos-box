package org.esa.beam.smos.visat;

import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.framework.help.HelpSys;
import org.esa.beam.framework.ui.UIUtils;
import org.esa.beam.framework.ui.application.PageComponent;
import org.esa.beam.framework.ui.application.support.AbstractToolView;
import org.esa.beam.framework.ui.application.support.PageComponentListenerAdapter;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.framework.ui.tool.ToolButtonFactory;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.net.URL;

public abstract class SmosToolView extends AbstractToolView {

    private JPanel panel;
    private JLabel defaultComponent;
    private JComponent clientComponent;
    private SmosToolView.SVSL svsl;

    protected SmosToolView() {
    }

    protected final SceneViewSelectionService getSmosViewSelectionService() {
        return SmosBox.getInstance().getSmosViewSelectionService();
    }

    protected final SnapshotSelectionService getSnapshotSelectionService() {
        return SmosBox.getInstance().getSnapshotSelectionService();
    }

    protected final ProductSceneView getSelectedSmosView() {
        return getSmosViewSelectionService().getSelectedSceneView();
    }

    protected final Product getSelectedSmosProduct() {
        return getSmosViewSelectionService().getSelectedSmosProduct();
    }

    protected final SmosFile getSelectedSmosFile() {
        return getSmosViewSelectionService().getSelectedSmosFile();
    }

    protected final long getSelectedSnapshotId(RasterDataNode raster) {
        return getSnapshotSelectionService().getSelectedSnapshotId(raster);
    }

    protected final long getSelectedSnapshotId(ProductSceneView view) {
        final RasterDataNode raster;
        if (view != null) {
            raster = view.getRaster();
        } else {
            raster = null;
        }
        return getSnapshotSelectionService().getSelectedSnapshotId(raster);
    }

    protected final void setSelectedSnapshotId(RasterDataNode raster, long id) {
        getSnapshotSelectionService().setSelectedSnapshotId(raster, id);
    }

    @Override
    protected JComponent createControl() {
        panel = new JPanel(new BorderLayout());
        URL resource = SmosToolView.class.getResource("SmosIcon.png");
        if (resource != null) {
            defaultComponent = new JLabel(new ImageIcon(resource));
        } else {
            defaultComponent = new JLabel();
        }
        defaultComponent.setIconTextGap(10);
        defaultComponent.setText("No SMOS image selected.");
        panel.add(defaultComponent);

        HelpSys.enableHelpKey(getPaneControl(), getDescriptor().getHelpId());

        super.getContext().getPage().addPageComponentListener(new PageComponentListenerAdapter() {
            @Override
            public void componentOpened(PageComponent component) {
                super.componentOpened(component);
            }

            @Override
            public void componentClosed(PageComponent component) {
                super.componentClosed(component);
            }

            @Override
            public void componentShown(PageComponent component) {
                super.componentShown(component);
            }

            @Override
            public void componentHidden(PageComponent component) {
                super.componentHidden(component);
            }
        });

        return panel;
    }

    @Override
    public void componentOpened() {
        svsl = new SVSL();
        getSmosViewSelectionService().addSceneViewSelectionListener(svsl);
        realizeSmosView(getSelectedSmosView());
    }

    @Override
    public void componentClosed() {
        getSmosViewSelectionService().removeSceneViewSelectionListener(svsl);
        realizeSmosView(null);
    }

    @Override
    public void componentShown() {
        realizeSmosView(getSelectedSmosView());
    }

    @Override
    public void componentHidden() {
        realizeSmosView(null);
    }

    protected void realizeSmosView(ProductSceneView newView) {
        if (clientComponent == null) {
            clientComponent = createClientComponent();
        }
        if (newView != null) {
            setToolViewComponent(clientComponent);
            updateClientComponent(newView);
        } else {
            setToolViewComponent(defaultComponent);
        }
    }

    protected final JComponent getClientComponent() {
        return clientComponent;
    }

    protected abstract JComponent createClientComponent();

    protected abstract void updateClientComponent(ProductSceneView smosView);

    protected final void setToolViewComponent(JComponent comp) {
        panel.removeAll();
        panel.add(comp, BorderLayout.CENTER);
        panel.invalidate();
        panel.validate();
        panel.updateUI();
    }

    private class SVSL implements SceneViewSelectionService.SelectionListener {

        @Override
        public void handleSceneViewSelectionChanged(ProductSceneView oldView, ProductSceneView newView) {
            realizeSmosView(newView);
        }
    }

    protected static AbstractButton createHelpButton() {
        final ImageIcon icon = UIUtils.loadImageIcon("icons/Help24.gif");
        final AbstractButton button = ToolButtonFactory.createButton(icon, false);
        button.setToolTipText("Help."); /*I18N*/
        button.setName("helpButton");

        return button;
    }
}