package org.esa.beam.smos.gui;


import javax.swing.*;
import java.io.File;

public interface ChooserFactory {

    JFileChooser createChooser(File file);
}
