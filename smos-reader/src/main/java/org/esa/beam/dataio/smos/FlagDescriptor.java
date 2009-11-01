package org.esa.beam.dataio.smos;

import java.awt.Color;

public interface FlagDescriptor {

    String getFlagName();

    int getMask();

    boolean isVisible();

    Color getColor();

    double getTransparency();

    String getDescription();
}
