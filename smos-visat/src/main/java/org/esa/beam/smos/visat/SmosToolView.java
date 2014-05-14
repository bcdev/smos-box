/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

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

    protected void realizeSmosView(ProductSceneView view) {
        if (clientComponent == null) {
            clientComponent = createClientComponent();
        }
        if (view != null) {
            setToolViewComponent(clientComponent);
            updateClientComponent(view);
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