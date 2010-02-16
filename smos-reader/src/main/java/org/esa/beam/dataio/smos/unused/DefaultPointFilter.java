/* 
 * Copyright (C) 2002-2008 by Brockmann Consult
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.dataio.smos.unused;

/**
 * Default point filter, rejecting any point (x, y) where x or y is
 * infinite or not a number.
 *
 * @author Ralf Quast
 * @version $Revision: 3871 $ $Date: 2008-12-04 19:42:05 +0100 (Do, 04 Dez 2008) $
 * @since SMOS-Box 1.0
 */
public class DefaultPointFilter implements PointFilter {

    @Override
    public boolean accept(double x, double y) {
        return !(Double.isNaN(x) || Double.isNaN(y) || Double.isInfinite(x) || Double.isInfinite(y));
    }
}
