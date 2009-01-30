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
package org.esa.beam.dataio.smos;

/**
 * Point filter.
 *
 * @author Ralf Quast
 * @version $Revision: 3988 $ $Date: 2008-12-18 09:40:14 +0100 (Do, 18 Dez 2008) $
 * @since BEAM 4.6
 */
interface PointFilter {

    PointFilter NULL = new PointFilter() {
        @Override
        public boolean accept(double x, double y) {
            return true;
        }
    };

    boolean accept(double x, double y);
}
