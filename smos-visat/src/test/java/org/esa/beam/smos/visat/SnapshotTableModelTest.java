package org.esa.beam.smos.visat;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SnapshotTableModelTest {

    private Object[][] content;
    private SnapshotTableModel tableModel;

    @Before
    public void setUp() {
        content = new Object[2][2];
        tableModel = new SnapshotTableModel(content);
    }

    @Test
    public void testRowCount() {
        assertEquals(2, tableModel.getRowCount());
    }

    @Test
    public void testColumnCount() {
        assertEquals(2, tableModel.getColumnCount());
    }

    @Test
    public void testGetColumnName() {
        assertEquals("Name", tableModel.getColumnName(0));
        assertEquals("Value", tableModel.getColumnName(1));
    }

    @Test
    public void testGetColumnClass() {
        assertEquals(String.class, tableModel.getColumnClass(0));
        assertEquals(String.class, tableModel.getColumnClass(1));
    }

    @Test
    public void testGetValueAt() {
        content[0][1] = "Schneck";
        content[1][1] = "bird";

        assertEquals("Schneck", tableModel.getValueAt(0, 1));
        assertEquals("bird", tableModel.getValueAt(1, 1));
    }
}
