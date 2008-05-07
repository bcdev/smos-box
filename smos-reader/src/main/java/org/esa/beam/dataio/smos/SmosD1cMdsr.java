package org.esa.beam.dataio.smos;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;


public final class SmosD1cMdsr {

    int flags;
    float btValueReal;
    float btValueImag;
    float pixelRadiometricAccuracy;
    float incidenceAngle;
    float azimuthAngle;
    float faradayRotationAngle;
    float geometricRotationAngle;
    int snapshotId;
    float footprintAxis1;
    float footprintAxis2;

    public SmosD1cMdsr() {
    }

    void readFrom(ImageInputStream iis) throws IOException {
//        iis.skipBytes(28);
//        return;


//        final long pos0 = iis.getStreamPosition();

        flags = iis.readShort() & 0xFFFF;
        btValueReal = iis.readFloat();
        btValueImag = iis.readFloat();
        pixelRadiometricAccuracy = readScaledUShort(iis, 50.0F); // todo - use Radiometric_Accuracy_Scale from SPH
        incidenceAngle = readScaledUShort(iis, 90.0F);
        azimuthAngle = readScaledUShort(iis, 360.0F);
        faradayRotationAngle = readScaledUShort(iis, 360.0F);
        geometricRotationAngle = readScaledUShort(iis, 360.0F);
        snapshotId = iis.readInt();
        footprintAxis1 = readScaledUShort(iis, 100.0F); // todo - use Pixel_Footprint_Scale from SPH
        footprintAxis2 = readScaledUShort(iis, 100.0F); // todo - use Pixel_Footprint_Scale from SPH

//        final long dsrSize = iis.getStreamPosition() - pos0;
//        System.out.println("SmosD1cMdsr.dsrSize = " + dsrSize);
    }

    private float readScaledUShort(ImageInputStream iis, float scale) throws IOException {
//        final int us = iis.readShort() & 0xFFFF;
//        final float c = 1 << 16;
//        return scale * (us / c);
        iis.skipBytes(2);
        return 0;
    }
}
