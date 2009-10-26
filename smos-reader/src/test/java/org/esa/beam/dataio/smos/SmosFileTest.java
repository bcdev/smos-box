package org.esa.beam.dataio.smos;

import static junit.framework.Assert.assertEquals;
import org.junit.Test;
import org.esa.beam.util.io.BeamFileChooser;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.Icon;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileView;
import java.util.Date;
import java.awt.Component;
import java.io.File;

public class SmosFileTest {

    @Test
    public void testCfiToUtc() {
        Date utc = SmosFile.getCfiDateInUtc(3456, 2267, 778734);
        assertEquals(1245285467778L, utc.getTime());

        utc = SmosFile.getCfiDateInUtc(3457, 2267, 778734);
        assertEquals(1245371867778L, utc.getTime());

        utc = SmosFile.getCfiDateInUtc(3456, 2268, 778734);
        assertEquals(1245285468778L, utc.getTime());

        utc = SmosFile.getCfiDateInUtc(3456, 2267, 878734);
        assertEquals(1245285467878L, utc.getTime());
    }

}
