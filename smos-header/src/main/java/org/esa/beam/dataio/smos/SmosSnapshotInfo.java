package org.esa.beam.dataio.smos;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;


public class SmosSnapshotInfo {
    int[] snapshotTime = new int[3];
    int snapshotId;
    long snapshotObet;
    double xPosition;
    double yPosition;
    double zPosition;
    double xVelocity;
    double yVelocity;
    double zVelocity;
    int vectorSource;
    double q1;
    double q2;
    double q3;
    double q4;
    double tec;
    double geomagF;
    double geomagD;
    double geomagI;
    float sunRa;
    float sunDec;
    float sunBt;
    float accuracy;
    float[] radiometricAccuracy = new float[2];

    void readFrom(ImageInputStream iis) throws IOException {
//        final long pos0 = iis.getStreamPosition();

        iis.readFully(snapshotTime, 0, 3);
        snapshotId = iis.readInt();
        snapshotObet = iis.readLong();
        xPosition = iis.readDouble();
        yPosition = iis.readDouble();
        zPosition = iis.readDouble();
        xVelocity = iis.readDouble();
        yVelocity = iis.readDouble();
        zVelocity = iis.readDouble();
        vectorSource = iis.readByte() & 0xFF;
        q1 = iis.readDouble();
        q2 = iis.readDouble();
        q3 = iis.readDouble();
        q4 = iis.readDouble();
        tec = iis.readDouble();
        geomagF = iis.readDouble();
        geomagD = iis.readDouble();
        geomagI = iis.readDouble();
        sunRa = iis.readFloat();
        sunDec = iis.readFloat();
        sunBt = iis.readFloat();
        accuracy = iis.readFloat();
        radiometricAccuracy[0] = iis.readFloat();
        radiometricAccuracy[1] = iis.readFloat();

//        long dsdSize = iis.getStreamPosition() - pos0;
//        System.out.println("SmosSnapshotInfo.dsdSize = " + dsdSize);
    }
}
