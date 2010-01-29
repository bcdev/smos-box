package org.esa.beam.dataio.smos.dddb;

import java.awt.Color;

public interface FlagDescriptor {

    String getFlagName();

    int getMask();

    boolean isVisible();

    Color getColor();

    double getTransparency();

    String getDescription();
}
