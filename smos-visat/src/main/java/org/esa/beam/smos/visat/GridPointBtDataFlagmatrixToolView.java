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

import org.esa.beam.dataio.smos.L1cSmosFile;
import org.esa.beam.dataio.smos.SmosConstants;
import org.esa.beam.dataio.smos.dddb.BandDescriptor;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.dataio.smos.dddb.Family;
import org.esa.beam.dataio.smos.dddb.FlagDescriptor;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.labels.XYZToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleInsets;

import javax.swing.JComponent;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GridPointBtDataFlagmatrixToolView extends GridPointBtDataToolView {

    public static final String ID = GridPointBtDataFlagmatrixToolView.class.getName();
    private static final String SERIES_KEY = "Flags";

    private static final String DEFAULT_FLAG_DESCRIPTOR_IDENTIFIER = "DBL_SM_XXXX_MIR_XXXF1C_0400_flags";

    private JFreeChart chart;
    private DefaultXYZDataset dataset;
    private XYPlot plot;
    private ChartPanel chartPanel;
    private String[] flagNames;

    @Override
    protected JComponent createGridPointComponent() {
        dataset = new DefaultXYZDataset();

        final NumberAxis xAxis = new NumberAxis("Record #");
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        xAxis.setAutoRangeIncludesZero(false);
        xAxis.setLowerMargin(0.0);
        xAxis.setUpperMargin(0.0);

        flagNames = createFlagNames(Dddb.getInstance().getFlagDescriptors(DEFAULT_FLAG_DESCRIPTOR_IDENTIFIER));
        final NumberAxis yAxis = createRangeAxis(flagNames);

        final LookupPaintScale paintScale = new LookupPaintScale(0.0, 4.0, Color.WHITE);
        paintScale.add(0.0, Color.BLACK);
        paintScale.add(1.0, Color.RED);
        paintScale.add(2.0, Color.GREEN);
        paintScale.add(3.0, Color.BLUE);
        paintScale.add(4.0, Color.YELLOW);

        final XYBlockRenderer renderer = new XYBlockRenderer();
        renderer.setPaintScale(paintScale);
        renderer.setBaseToolTipGenerator(new FlagToolTipGenerator(flagNames));

        plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);
        plot.setForegroundAlpha(0.5f);
        plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
        plot.setNoDataMessage("No data");

        chart = new JFreeChart(null, plot);
        chart.removeLegend();
        chartPanel = new ChartPanel(chart);

        return chartPanel;
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
        chartPanel.setEnabled(enabled);
        if (enabled) {
            final Family<BandDescriptor> bandDescriptors = Dddb.getInstance().getBandDescriptors(
                    smosFile.getDataFormat().getName());
            final BandDescriptor flagsBandDescriptor = bandDescriptors.getMember(SmosConstants.BT_FLAGS_NAME);
            final Family<FlagDescriptor> flagDescriptors = flagsBandDescriptor.getFlagDescriptors();
            flagNames = createFlagNames(flagDescriptors);
            final NumberAxis rangeAxis = createRangeAxis(flagNames);
            plot.setRangeAxis(rangeAxis);
        }
    }

    @Override
    protected void updateGridPointBtDataComponent(GridPointBtDataset ds) {
        dataset.removeSeries(SERIES_KEY);

        int iq = ds.getColumnIndex(SmosConstants.BT_FLAGS_NAME);
        if (iq != -1) {
            final Number[][] dsData = ds.getData();
            final int m = dsData.length;
            final int n = flagNames.length;
            double[][] data = new double[3][n * m];
            for (int x = 0; x < m; x++) {
                final int flags = dsData[x][iq].intValue();
                for (int y = 0; y < n; y++) {
                    data[0][y * m + x] = (1 + x);
                    data[1][y * m + x] = y;
                    data[2][y * m + x] = ((flags & (1 << y)) != 0) ? (1 + y % 3) : 0.0;
                }
            }
            dataset.addSeries(SERIES_KEY, data);
        } else {
            plot.setNoDataMessage("Not a SMOS D1C/F1C pixel.");
        }
        chart.fireChartChanged();
    }

    @Override
    protected void updateGridPointBtDataComponent(IOException e) {
        dataset.removeSeries(SERIES_KEY);
        plot.setNoDataMessage("I/O error");
    }

    @Override
    protected void clearGridPointBtDataComponent() {
        dataset.removeSeries(SERIES_KEY);
        plot.setNoDataMessage("No data");
    }

    private String[] createFlagNames(Family<FlagDescriptor> flagDescriptors) {
        final List<FlagDescriptor> flagDescriptorsList = flagDescriptors.asList();
        final List<String> flagNames = new ArrayList<>(flagDescriptorsList.size());
        for (final FlagDescriptor d : flagDescriptorsList) {
            flagNames.add(d.getFlagName());
        }

        return flagNames.toArray(new String[flagNames.size()]);
    }

    private NumberAxis createRangeAxis(String[] flagNames) {
        final NumberAxis axis = new SymbolAxis(null, flagNames);
        axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        axis.setAutoRangeIncludesZero(false);
        axis.setLowerMargin(0.0);
        axis.setUpperMargin(0.0);
        axis.setInverted(true);

        return axis;
    }


    private class FlagToolTipGenerator implements XYZToolTipGenerator {

        private String[] flagNames;

        private FlagToolTipGenerator(String[] flagNames) {
            this.flagNames = flagNames;
        }

        @Override
        public String generateToolTip(XYDataset xyDataset, int series, int item) {
            return generateToolTip((XYZDataset) xyDataset, series, item);
        }

        @Override
        public String generateToolTip(XYZDataset xyzDataset, int series, int item) {
            int recordIndex = dataset.getX(series, item).intValue();
            int flagIndex = dataset.getY(series, item).intValue();
            boolean flagSet = dataset.getZ(series, item).intValue() != 0;
            String flagName = "?";
            if (flagIndex >= 0 && flagIndex < flagNames.length) {
                flagName = flagNames[flagIndex];
            }
            return flagName + "(" + recordIndex + "): " + (flagSet ? "ON" : "OFF");
        }
    }
}