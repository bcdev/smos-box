package org.esa.beam.smos.visat;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.SimpleType;
import com.bc.ceres.binio.Type;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.application.PageComponent;
import org.esa.beam.framework.ui.application.support.AbstractToolView;
import org.esa.beam.framework.ui.application.support.PageComponentListenerAdapter;
import org.esa.beam.framework.ui.product.ProductSceneView;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.io.IOException;
import java.math.BigInteger;
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

    protected final long getSelectedSnapshotId(ProductSceneView view) {
        return getSnapshotSelectionService().getSelectedSnapshotId(view);
    }

    protected final void setSelectedSnapshotId(ProductSceneView view, long id) {
        getSnapshotSelectionService().setSelectedSnapshotId(view, id);
    }

    @Override
    protected JComponent createControl() {
        panel = new JPanel(new BorderLayout());
        URL resource = getClass().getResource("smos-icon.png");
        if (resource != null) {
            defaultComponent = new JLabel(new ImageIcon(resource));
        } else {
            defaultComponent = new JLabel();
        }
        defaultComponent.setIconTextGap(10);
        defaultComponent.setText("No SMOS image selected.");
        panel.add(defaultComponent);

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

    public static Number getNumbericMember(CompoundData compoundData, int memberIndex) throws IOException {
        final Type memberType = compoundData.getCompoundType().getMemberType(memberIndex);

        if (memberType == SimpleType.DOUBLE) {
            return compoundData.getDouble(memberIndex);
        }
        if (memberType == SimpleType.FLOAT) {
            return compoundData.getFloat(memberIndex);
        }
        if (memberType == SimpleType.ULONG) {
            // This mask is used to obtain the value of an int as if it were unsigned.
            // todo - write a test; according to the BigInteger API doc this cannot work as intended (rq-20090205)
            final BigInteger mask = BigInteger.valueOf(0xffffffffffffffffL);
            final BigInteger bi = BigInteger.valueOf(compoundData.getLong(memberIndex));
            return bi.and(mask);
        }
        if (memberType == SimpleType.LONG || memberType == SimpleType.UINT) {
            return compoundData.getDouble(memberIndex);
        }
        if (memberType == SimpleType.INT || memberType == SimpleType.USHORT) {
            return compoundData.getDouble(memberIndex);
        }
        if (memberType == SimpleType.SHORT || memberType == SimpleType.UBYTE) {
            return compoundData.getDouble(memberIndex);
        }
        if (memberType == SimpleType.BYTE) {
            return compoundData.getDouble(memberIndex);
        }

        return null;
    }

    public static Class<? extends Number> getNumbericMemberType(CompoundType compoundData, int memberIndex) {
        final Type memberType = compoundData.getMemberType(memberIndex);

        if (memberType == SimpleType.DOUBLE) {
            return Double.class;
        }
        if (memberType == SimpleType.FLOAT) {
            return Float.class;
        }
        if (memberType == SimpleType.ULONG) {
            return BigInteger.class;
        }
        if (memberType == SimpleType.LONG || memberType == SimpleType.UINT) {
            return Long.class;
        }
        if (memberType == SimpleType.INT || memberType == SimpleType.USHORT) {
            return Integer.class;
        }
        if (memberType == SimpleType.SHORT || memberType == SimpleType.UBYTE) {
            return Short.class;
        }
        if (memberType == SimpleType.BYTE) {
            return Byte.class;
        }
        
        return null;
    }

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
}