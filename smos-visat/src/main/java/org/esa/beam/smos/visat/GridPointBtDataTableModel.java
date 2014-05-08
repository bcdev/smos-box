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

import javax.swing.table.AbstractTableModel;


class GridPointBtDataTableModel extends AbstractTableModel {
    private String[] columnNames;
    private GridPointBtDataset ds;

    @Override
    public int getRowCount() {
        return ds == null ? 0 :ds.getData().length;
    }

    @Override
    public int getColumnCount() {
        return columnNames == null ? 0 : columnNames.length + 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return 1 + rowIndex;
        } else {
            return ds.getData()[rowIndex][columnIndex - 1];
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return "Rec#";
        } else {
            return columnNames == null ? "" : columnNames[columnIndex - 1];
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Integer.class;
        } else {
            if (ds == null) {
                return Number.class;
            } else {
                return ds.getColumnClasses()[columnIndex - 1];
            }
        }
    }

    public void setGridPointBtDataset(GridPointBtDataset ds) {
        this.ds = ds;
        fireTableDataChanged();
    }
    
    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }
}
