package org.esa.beam.smos.gui;


import org.esa.beam.util.io.FileChooserFactory;

import javax.swing.*;
import java.io.File;

public class DirectoryChooserFactory implements ChooserFactory{

    @Override
    public JFileChooser createChooser(File file) {
        return FileChooserFactory.getInstance().createDirChooser(file);
    }
}
