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

import com.bc.ceres.binio.CompoundMember;
import com.bc.ceres.binio.CompoundType;
import com.jidesoft.grid.AutoResizePopupMenuCustomizer;
import com.jidesoft.grid.TableColumnChooser;
import com.jidesoft.grid.TableColumnChooserPopupMenuCustomizer;
import com.jidesoft.grid.TableHeaderPopupMenuInstaller;
import org.esa.beam.dataio.smos.L1cSmosFile;
import org.esa.beam.framework.ui.product.ProductSceneView;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class GridPointBtDataTableToolView extends GridPointBtDataToolView {

    public static final String ID = GridPointBtDataTableToolView.class.getName();

    private JTable table;
    private JButton columnsButton;
    private JButton exportButton;

    private GridPointBtDataTableModel gridPointBtDataTableModel;

    public GridPointBtDataTableToolView() {
        gridPointBtDataTableModel = new GridPointBtDataTableModel();
        table = new JTable(gridPointBtDataTableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    @Override
    protected void updateClientComponent(ProductSceneView smosView) {
        boolean enabled = smosView != null;
        L1cSmosFile smosFile = null;
        if (enabled) {
            smosFile = getL1cSmosFile();
            if (smosFile == null) {
                enabled = false;
            }
        }
        table.setEnabled(enabled);
        columnsButton.setEnabled(enabled);
        exportButton.setEnabled(enabled);
        if (enabled) {
            String[] names = getColumnNames(smosFile);
            TableColumnModel columnModel = new DefaultTableColumnModel();
            gridPointBtDataTableModel.setColumnNames(names);
            table.setColumnModel(columnModel);
            table.createDefaultColumnsFromModel();
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
        final TableHeaderPopupMenuInstaller installer = new TableHeaderPopupMenuInstaller(table);
        installer.addTableHeaderPopupMenuCustomizer(new AutoResizePopupMenuCustomizer());
        installer.addTableHeaderPopupMenuCustomizer(new TableColumnChooserPopupMenuCustomizer());

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