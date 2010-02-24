/*
 * $Id: $
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
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
package org.esa.beam.smos.visat.export;

import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.visat.actions.AbstractVisatAction;

/**
 * @author Ralf Quast
 * @author Marco Zuehlke
 * @version $Revision$ $Date$
 * @since SMOS 2.0
 */
public class GridPointExportAction extends AbstractVisatAction {

    @Override
    public void actionPerformed(CommandEvent event) {
        new GridPointExportDialog(getAppContext(), event.getCommand().getHelpId()).show();
    }
}
