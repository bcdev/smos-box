package org.esa.beam.smos.visat;

import com.bc.ceres.binio.CompoundMember;
import com.bc.ceres.binio.CompoundType;
import com.jidesoft.grid.TableColumnChooser;
import org.esa.beam.dataio.smos.L1cSmosFile;
import org.esa.beam.framework.ui.product.ProductSceneView;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

public class GridPointBtDataTableToolView extends GridPointBtDataToolView {

    public static final String ID = GridPointBtDataTableToolView.class.getName();

    private JTable table;
    private JButton columnsButton;
    private JButton exportButton;
    private Map<L1cSmosFile, TableColumnModel> columnModels = new WeakHashMap<L1cSmosFile, TableColumnModel>();
    private Map<L1cSmosFile, String[]> columnNames = new WeakHashMap<L1cSmosFile, String[]>();

    private GridPointBtDataTableModel gridPointBtDataTableModel;

    public GridPointBtDataTableToolView() {
        gridPointBtDataTableModel = new GridPointBtDataTableModel();
        table = new JTable(gridPointBtDataTableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    @Override
    protected void updateClientComponent(ProductSceneView smosView) {
        boolean enabled = smosView != null && getSelectedSmosFile() instanceof L1cSmosFile;
        table.setEnabled(enabled);

        columnsButton.setEnabled(enabled);
        exportButton.setEnabled(enabled);
        if (enabled) {
            L1cSmosFile smosFile;
            TableColumnModel columnModel;
            boolean initTableModel = false;
            String[] names;
            synchronized (this) {
                smosFile = (L1cSmosFile) getSelectedSmosFile();
                columnModel = columnModels.get(smosFile);
                names = columnNames.get(smosFile);
                if (columnModel == null) {
                    columnModel = new DefaultTableColumnModel();
                    columnModels.put(smosFile, columnModel);
                    names = getColumnNames(smosFile);
                    columnNames.put(smosFile, names);
                    initTableModel = true;
                }
            }
            gridPointBtDataTableModel.setColumnNames(names);
            table.setColumnModel(columnModel);
            if (initTableModel) {
                table.createDefaultColumnsFromModel();
            }
        }
    }

    private String[] getColumnNames(L1cSmosFile smosFile) {
        final CompoundType btDataType = smosFile.getBtDataType();
        final CompoundMember[] members = btDataType.getMembers();
        String[] names = new String[members.length];
        for (int i = 0; i < members.length; i++) {
            CompoundMember member = members[i];
            names[i] = member.getName();
        }
        return names;
    }

    @Override
    protected JComponent createGridPointComponent() {
        TableColumnChooser.install(table);
        return new JScrollPane(table);
    }

    @Override
    protected JComponent createGridPointComponentOptionsComponent() {
        Action action = TableColumnChooser.getTableColumnChooserButton(table).getAction();
        action.putValue(Action.NAME, "Columns...");
        columnsButton = new JButton(action);

        exportButton = new JButton("Export...");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final TableModelExportRunner modelExportRunner = new TableModelExportRunner(
                        getPaneWindow(), getTitle(), table.getModel(), table.getColumnModel());
                modelExportRunner.run();
            }
        });

        final JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        optionsPanel.add(columnsButton);
        optionsPanel.add(exportButton);
        return optionsPanel;
    }

    @Override
    protected void updateGridPointBtDataComponent(GridPointBtDataset ds) {
        gridPointBtDataTableModel.setGridPointBtDataset(ds);
    }

    @Override
    protected void updateGridPointBtDataComponent(IOException e) {
        gridPointBtDataTableModel.setGridPointBtDataset(null);
    }

    @Override
    protected void clearGridPointBtDataComponent() {
        gridPointBtDataTableModel.setGridPointBtDataset(null);
    }
}