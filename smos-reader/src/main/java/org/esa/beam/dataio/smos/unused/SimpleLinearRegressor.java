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

package org.esa.beam.dataio.smos.unused;

import java.awt.geom.Point2D;

/**
 * Class performing the function of calculating simple linear regressions. See
 * Mendenhall & Sincich (1995, Statistics for Engineering and the Sciences).
 *
 * @author Ralf Quast
 * @version $Revision: 3988 $ $Date: 2008-12-18 09:40:14 +0100 (Do, 18 Dez 2008) $
 * @since SMOS-Box 1.0
 */
public class SimpleLinearRegressor {

    final PointFilter pointFilter;

    private int count;
    private double sx;

    private double sy;
    private double sxx;

    private double sxy;

    public SimpleLinearRegressor() {
        this(PointFilter.NULL);
    }

    public SimpleLinearRegressor(PointFilter pointFilter) {
        if (pointFilter == null) {
            pointFilter = PointFilter.NULL;
        }
        this.pointFilter = pointFilter;
    }

    /**
     * Adds a point (x, y) to the regression, if accepted.
     *
     * @param x the x-coordinate of the point to be added.
     * @param y the y-coordinate of the point to be added.
     *
     * @return {@code true} if the point was accepted and added to the regression,
     *         otherwise {@code false}.
     */
    public boolean add(double x, double y) {
        final boolean accepted = pointFilter.accept(x, y);

        if (accepted) {
            sx += x;
            sy += y;
            sxx += x * x;
            sxy += x * y;
            ++count;
        }

        return accepted;
    }

    /**
     * Returns the number of valid ({@code x[i]}, {@code y[i]}) pairs.
     *
     * @return the number of valid pairs.
     */
    public final int getPointCount() {
        return count;
    }

    /**
     * Returns the regression for the current points.
     *
     * @return a point (x, y) where x is the slope of the regression line,
     *         and y is the intercept with the y-axis.
     */
    public final Point2D getRegression() {
        final double a = (count * sxy - sx * sy) / (count * sxx - sx * sx);
        final double b = (sy - a * sx) / count;

        return new Point2D.Double(a, b);
    }
}
