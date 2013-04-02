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

import org.junit.Assert;
import org.junit.Test;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.io.ByteArrayOutputStream;

public class TableModelExporterTest {

    @Test
    public void testUninitializedModel() {
        final DefaultTableModel tableModel = new DefaultTableModel(2, 3);
        final TableModelExporter exporter = new TableModelExporter(tableModel);
        exporter.setSeparator('\t');
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        exporter.export(stream);
        final String actual = stream.toString();
        Assert.assertTrue(actual.contains("A\tB\tC"));
        Assert.assertTrue(actual.contains("null\tnull\tnull"));
    }


    @Test
    public void testSimpleModel() {
        final TableModel tableModel = createTableModel();
        final TableModelExporter exporter = new TableModelExporter(tableModel);
        exporter.setSeparator('\t');
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        exporter.export(stream);
        final String actual = stream.toString();
        assertTableModel(actual);
    }

    @Test
    public void testSimpleModelWithDifferentColumnVisibility() {
        final TableModel tableModel = createTableModel();
        final TableModelExporter exporter = new TableModelExporter(tableModel);
        exporter.setSeparator('\t');
        exporter.setColumnFilter(new TableModelExporter.ColumnFilter() {
            public boolean exportColumn(int columnIndex) {
                return columnIndex != 1;
            }
        });
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        exporter.export(stream);
        final String actual = stream.toString();
        Assert.assertTrue(actual.contains("Bibo\tSamson"));
        Assert.assertTrue(actual.contains("12\t45.456"));
        Assert.assertTrue(actual.contains("11\t129.5678"));
        Assert.assertTrue(actual.contains("2\t0.1"));

    }

    @Test
    public void testSimpleModelNoColumnVisibility() {
        final TableModel tableModel = createTableModel();
        final TableModelExporter exporter = new TableModelExporter(tableModel);
        exporter.setSeparator('\t');
        exporter.setColumnFilter(new TableModelExporter.ColumnFilter() {
            public boolean exportColumn(int columnIndex) {
                return false;
            }
        });
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        exporter.export(stream);
        Assert.assertTrue(stream.toString().trim().isEmpty());
    }

    private void assertTableModel(String actual) {
        Assert.assertTrue(actual.contains("Bibo\tTiffy\tSamson"));
        Assert.assertTrue(actual.contains("12\tCat\t45.456"));
        Assert.assertTrue(actual.contains("11\tMouse\t129.5678"));
        Assert.assertTrue(actual.contains("2\tDog\t0.1"));
    }

    private TableModel createTableModel() {
        final String[] columnNames = {"Bibo", "Tiffy", "Samson"};
        final Object[][] tableData = {
                {12, "Cat", 45.456},
                {11, "Mouse", 129.5678},
                {2, "Dog", 0.1}
        };
        final DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setDataVector(tableData, columnNames);
        return tableModel;
    }
}
