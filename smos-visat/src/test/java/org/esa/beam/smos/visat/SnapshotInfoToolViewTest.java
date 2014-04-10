package org.esa.beam.smos.visat;

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.ProductData;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SnapshotInfoToolViewTest {

    @Test
    public void testIsXPolarized() {
        Band band = createband("BT_Value_X");
        assertTrue(SnapshotInfoToolView.isXPolarized(band));

        band = createband("Faraday_Rotation_Angle_X");
        assertTrue(SnapshotInfoToolView.isXPolarized(band));

        band = createband("Faraday_Rotation_Angle_Y");
        assertFalse(SnapshotInfoToolView.isXPolarized(band));

        band = createband("BT_Value_V");
        assertFalse(SnapshotInfoToolView.isXPolarized(band));
    }

    @Test
    public void testIsYPolarized() {
        Band band = createband("Geometric_Rotation_Angle_Y");
        assertTrue(SnapshotInfoToolView.isYPolarized(band));

        band = createband("Footprint_Axis_1_Y");
        assertTrue(SnapshotInfoToolView.isYPolarized(band));

        band = createband("Footprint_Axis_2_XY");
        assertFalse(SnapshotInfoToolView.isYPolarized(band));

        band = createband("BT_Value_HV_Real");
        assertFalse(SnapshotInfoToolView.isYPolarized(band));
    }

    @Test
    public void testIsXYPolarized() {
        Band band = createband("BT_Value_XY_Real");
        assertTrue(SnapshotInfoToolView.isXYPolarized(band));

        band = createband("Pixel_Radiometric_accuracy_XY");
        assertTrue(SnapshotInfoToolView.isXYPolarized(band));

        band = createband("Azimuth_Angle_X");
        assertFalse(SnapshotInfoToolView.isXYPolarized(band));

        band = createband("BT_Value_H");
        assertFalse(SnapshotInfoToolView.isXYPolarized(band));
    }

    private Band createband(String name) {
        return new Band(name, ProductData.TYPE_INT8, 4, 4);
    }
}
