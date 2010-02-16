package org.esa.beam.smos.visat;

import org.esa.beam.dataio.smos.L1cSmosFile;
import org.esa.beam.dataio.smos.L1cScienceSmosFile;
import org.esa.beam.dataio.smos.SmosConstants;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.RectangleInsets;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class GridPointBtDataChartToolView extends GridPointBtDataToolView {
    public static final String ID = GridPointBtDataChartToolView.class.getName();

    private JFreeChart chart;
    private YIntervalSeriesCollection coPolDataset;
    private YIntervalSeriesCollection crossPolDataset;
    private XYPlot plot;
    private JCheckBox[] modeCheckers;

    @Override
    protected JComponent createGridPointComponent() {
        coPolDataset = new YIntervalSeriesCollection();
        crossPolDataset = new YIntervalSeriesCollection();
        chart = ChartFactory.createXYLineChart(null,
                                               null,
                                               null,
                                               coPolDataset,
                                               PlotOrientation.VERTICAL,
                                               true, // Legend?
                                               true,
                                               false);
        
        plot = chart.getXYPlot();
        plot.setNoDataMessage("No data");
        plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));

        final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setLabel("Incidence Angle (deg)");
        xAxis.setRange(0, 70);
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setLabel("Co-Pol BT(K)");
        yAxis.setRange(50, 350);
        
        final NumberAxis yAxis2 = new NumberAxis("Cross-Pol BT(K)");
        yAxis2.setRange(-25, 25);
        plot.setRangeAxis(1, yAxis2);
        plot.setDataset(1, crossPolDataset);
        plot.mapDatasetToRangeAxis(1, 1);
        
        DeviationRenderer coPolRenderer = new DeviationRenderer(true, false);
        coPolRenderer.setSeriesFillPaint(0, new Color(255, 127, 127));
        coPolRenderer.setSeriesFillPaint(1, new Color(127, 127, 255));
        DeviationRenderer crossPolRenderer = new DeviationRenderer(true, false);
        crossPolRenderer.setSeriesFillPaint(0, new Color(127, 255, 127));
        crossPolRenderer.setSeriesFillPaint(1, new Color(255, 255, 127));
        plot.setRenderer(0, coPolRenderer);
        plot.setRenderer(1, crossPolRenderer);

        return new ChartPanel(chart);
    }

    @Override
    protected void updateClientComponent(ProductSceneView smosView) {
        L1cSmosFile l1cSmosFile = getL1cSmosFile();
        if (l1cSmosFile != null && l1cSmosFile instanceof L1cScienceSmosFile) {
            final L1cScienceSmosFile smosFile = (L1cScienceSmosFile) l1cSmosFile;
            modeCheckers[0].setEnabled(true);
            modeCheckers[1].setEnabled(true);
            modeCheckers[2].setEnabled(SmosProductReader.isFullPolScienceFormat(smosFile.getDataFormat().getName()));
        }
    }

    @Override
    protected JComponent createGridPointComponentOptionsComponent() {
        modeCheckers = new JCheckBox[]{
                new JCheckBox("X", true),
                new JCheckBox("Y", true),
                new JCheckBox("XY", true),
        };
        final JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        for (JCheckBox modeChecker : modeCheckers) {
            modeChecker.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateGridPointBtDataComponent();
                }
            });
            optionsPanel.add(modeChecker);
        }
        return optionsPanel;
    }

    @Override
    protected void updateGridPointBtDataComponent(GridPointBtDataset ds) {
        coPolDataset.removeAllSeries();
        crossPolDataset.removeAllSeries();

        int ix = ds.getColumnIndex("Incidence_Angle");
        int iq = ds.getColumnIndex("Flags");
        int id = ds.getColumnIndex("Pixel_Radiometric_Accuracy");
        // todo: calculate and display H/V/HV BT values instead of X/Y/XY (rq-200100121)
        if (ix != -1 && iq != -1 && id != -1) {
            int iy1 = ds.getColumnIndex("BT_Value");
            if (iy1 != -1) {
                YIntervalSeries series1 = new YIntervalSeries("X");
                YIntervalSeries series2 = new YIntervalSeries("Y");
                boolean m1 = modeCheckers[0].isSelected();
                boolean m2 = modeCheckers[1].isSelected();
                int length = ds.data.length;
                for (int i = 0; i < length; i++) {
                    int polMode = ds.data[i][iq].intValue() & SmosConstants.L1C_POL_MODE_FLAGS_MASK;
                    double x = ds.data[i][ix].doubleValue();
                    double y = ds.data[i][iy1].doubleValue();
                    double dev = ds.data[i][id].doubleValue();
                     if (m1 && polMode == SmosConstants.L1C_POL_MODE_X) {
                        series1.add(x, y, y - dev, y + dev);
                    } else if (m2 && polMode == SmosConstants.L1C_POL_MODE_Y) {
                        series2.add(x, y, y - dev, y + dev);
                    }
                }
                coPolDataset.addSeries(series1);
                coPolDataset.addSeries(series2);
            } else {
                int iy2;
                iy1 = ds.getColumnIndex("BT_Value_Real");
                iy2 = ds.getColumnIndex("BT_Value_Imag");
                if (iy1 != -1 && iy2 != -1) {
                    YIntervalSeries series1 = new YIntervalSeries("X");
                    YIntervalSeries series2 = new YIntervalSeries("Y");
                    YIntervalSeries series3 = new YIntervalSeries("XY_Real");
                    YIntervalSeries series4 = new YIntervalSeries("XY_Imag");
                    boolean m1 = modeCheckers[0].isSelected();
                    boolean m2 = modeCheckers[1].isSelected();
                    boolean m3 = modeCheckers[2].isSelected();
                    int length = ds.data.length;
                    for (int i = 0; i < length; i++) {
                        int polMode = ds.data[i][iq].intValue() & SmosConstants.L1C_POL_MODE_FLAGS_MASK;
                        double dev = ds.data[i][id].doubleValue();
                        double x = ds.data[i][ix].doubleValue();
                        double y1 = ds.data[i][iy1].doubleValue();
                        if (m1 && polMode == SmosConstants.L1C_POL_MODE_X) {
                            series1.add(x, y1, y1 - dev, y1 + dev);
                        } else if (m2 && polMode == SmosConstants.L1C_POL_MODE_Y) {
                            series2.add(x, y1, y1 - dev, y1 + dev);
                        } else if (m3 && (polMode == SmosConstants.L1C_POL_MODE_XY1 || polMode == SmosConstants.L1C_POL_MODE_XY2)) {
                            double y2 = ds.data[i][iy2].doubleValue();
                            series3.add(x, y1, y1 - dev, y1 + dev);
                            series4.add(x, y2, y2 - dev, y2 + dev);
                        }
                    }
                    coPolDataset.addSeries(series1);
                    coPolDataset.addSeries(series2);
                    crossPolDataset.addSeries(series3);
                    crossPolDataset.addSeries(series4);
                }
            }
        } else {
            plot.setNoDataMessage("Not a SMOS D1C/F1C pixel.");
        }
        chart.fireChartChanged();
    }

    @Override
    protected void updateGridPointBtDataComponent(IOException e) {
        coPolDataset.removeAllSeries();
        crossPolDataset.removeAllSeries();
        plot.setNoDataMessage("I/O error");
    }

    @Override
    protected void clearGridPointBtDataComponent() {
        coPolDataset.removeAllSeries();
        crossPolDataset.removeAllSeries();
        plot.setNoDataMessage("No data");
    }
}
