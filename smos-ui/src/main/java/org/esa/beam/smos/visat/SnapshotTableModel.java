package org.esa.beam.smos.visat;

import javax.swing.table.AbstractTableModel;


class SnapshotTableModel extends AbstractTableModel {
    final Object[][] objects;

    public SnapshotTableModel(Object[][] objects) {
        this.objects = objects;
    }

    @Override
    public int getRowCount() {
        return objects.length;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int column) {
        return column == 0 ? "Name" : "Value";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return objects[rowIndex][columnIndex];
    }
}
