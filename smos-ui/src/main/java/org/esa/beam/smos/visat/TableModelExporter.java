package org.esa.beam.smos.visat;

import javax.swing.table.TableModel;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * A class which can export a given {@link TableModel table model} to an output stream
 * in a CSV format.
 *
 * @author Marco Peters
 * @version $Revision: $ $Date: $
 */
class TableModelExporter {

    private char separator;
    private final TableModel tableModel;
    private ColumnFilter columnFilter;

    /**
     * Creates an CSV exporter for the given {@link TableModel table model}.
     * As default separator ',' is used.
     *
     * @param tableModel the table model to export.
     */
    TableModelExporter(TableModel tableModel) {
        this.tableModel = tableModel;
        this.separator = ',';
        columnFilter = ColumnFilter.NULL;
    }

    /**
     * Gets the currently used separator character.
     *
     * @return the separator character.
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * Sets the separtor character.
     *
     * @param separator the separtor character.
     */
    public void setSeparator(char separator) {
        this.separator = separator;
    }

    /**
     * Sets the column filter used during export. The filter defines whether a
     * column shall be exported or not.
     *
     * <p>The {@link ColumnFilter#NULL default filter} does not filter at all.<p>
     *
     * @param columnFilter The column filter.
     */
    public void setColumnFilter(ColumnFilter columnFilter) {
        this.columnFilter = columnFilter;
    }

    /**
     * Exports the  associated table model to the given output stream
     *
     * @param out the stream to export the table model to.
     */
    public void export(OutputStream out) {
        final PrintWriter writer = new PrintWriter(out);
        try {
            writeHeaderRow(writer);
            writeDataRows(writer);
        } finally {
            writer.close();
        }
    }

    private void writeHeaderRow(final PrintWriter out) {
        final int columnCount = tableModel.getColumnCount();
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            if (columnFilter.exportColumn(columnIndex)) {
                out.print(tableModel.getColumnName(columnIndex));
                if (columnIndex < columnCount - 1) {
                    out.print(getSeparator());
                }
            }
        }
        out.println();
    }

    private void writeDataRows(final PrintWriter out) {
        final int columnCount = tableModel.getColumnCount();
        final int rowCount = tableModel.getRowCount();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                if (columnFilter.exportColumn(columnIndex)) {
                    out.print(tableModel.getValueAt(rowIndex, columnIndex));
                    if (columnIndex < columnCount - 1) {
                        out.print(getSeparator());
                    }
                }
            }
            out.println();
        }
    }

    public interface ColumnFilter {
        ColumnFilter NULL = new ColumnFilter() {
            public boolean exportColumn(int columnIndex) {
                return true;
            }
        };

        boolean exportColumn(int columnIndex);

    }
}
