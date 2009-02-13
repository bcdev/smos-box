package org.esa.beam.smos.visat;

import javax.swing.table.AbstractTableModel;


class GridPointBtDataTableModel extends AbstractTableModel {
    private String[] columnNames;
    private GridPointBtDataset ds;

    @Override
    public int getRowCount() {
        return ds == null ? 0 :ds.data.length;
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
            return ds.data[rowIndex][columnIndex - 1];
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
                return ds.columnClasses[columnIndex - 1];
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
