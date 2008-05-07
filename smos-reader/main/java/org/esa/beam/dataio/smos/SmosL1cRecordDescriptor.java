package org.esa.beam.dataio.smos;

import java.awt.image.DataBuffer;

public class SmosL1cRecordDescriptor {
    private final static SmosL1cFieldDescriptor[] D1C_FIELD_DESCRIPTORS = new SmosL1cFieldDescriptor[]{
            /*00*/ new SmosL1cFieldDescriptor("Flags", DataBuffer.TYPE_USHORT, 0),
            /*01*/ new SmosL1cFieldDescriptor("BT_Value_Real", DataBuffer.TYPE_FLOAT, 2),
            /*02*/ new SmosL1cFieldDescriptor("Radiometric_Accuracy_of_Pixel", DataBuffer.TYPE_USHORT, 6),
            /*03*/ new SmosL1cFieldDescriptor("Incidence_Angle", DataBuffer.TYPE_USHORT, 8),
            /*04*/ new SmosL1cFieldDescriptor("Azimuth_Angle", DataBuffer.TYPE_USHORT, 10),
            /*05*/ new SmosL1cFieldDescriptor("Faraday_Rotation_Angle", DataBuffer.TYPE_USHORT, 12),
            /*06*/ new SmosL1cFieldDescriptor("Geometric_Rotation_Angle", DataBuffer.TYPE_USHORT, 14),
            /*07*/ new SmosL1cFieldDescriptor("Snapshot_ID_of_Pixel", DataBuffer.TYPE_INT, 18),
            /*08*/ new SmosL1cFieldDescriptor("Footprint_Axis1", DataBuffer.TYPE_USHORT, 20),
            /*09*/ new SmosL1cFieldDescriptor("Footprint_Axis2", DataBuffer.TYPE_USHORT, 22),
            // --> size is 24 bytes total
    };

    private final static SmosL1cFieldDescriptor[] F1C_FIELD_DESCRIPTORS = new SmosL1cFieldDescriptor[]{
            /*00*/ new SmosL1cFieldDescriptor("Flags", DataBuffer.TYPE_USHORT, 0),
            /*01*/ new SmosL1cFieldDescriptor("BT_Value_Real", DataBuffer.TYPE_FLOAT, 2),
            /*02*/ new SmosL1cFieldDescriptor("BT_Value_Imag", DataBuffer.TYPE_FLOAT, 6),
            /*03*/ new SmosL1cFieldDescriptor("Radiometric_Accuracy_of_Pixel", DataBuffer.TYPE_USHORT, 10),
            /*04*/ new SmosL1cFieldDescriptor("Incidence_Angle", DataBuffer.TYPE_USHORT, 12),
            /*05*/ new SmosL1cFieldDescriptor("Azimuth_Angle", DataBuffer.TYPE_USHORT, 14),
            /*06*/ new SmosL1cFieldDescriptor("Faraday_Rotation_Angle", DataBuffer.TYPE_USHORT, 16),
            /*07*/ new SmosL1cFieldDescriptor("Geometric_Rotation_Angle", DataBuffer.TYPE_USHORT, 18),
            /*08*/ new SmosL1cFieldDescriptor("Snapshot_ID_of_Pixel", DataBuffer.TYPE_INT, 20),
            /*09*/ new SmosL1cFieldDescriptor("Footprint_Axis1", DataBuffer.TYPE_USHORT, 24),
            /*10*/ new SmosL1cFieldDescriptor("Footprint_Axis2", DataBuffer.TYPE_USHORT, 26),
            // --> size is 28 bytes total
    };

    private final static SmosL1cFieldDescriptor[] BW1C_FIELD_DESCRIPTORS = new SmosL1cFieldDescriptor[]{
            /*00*/ new SmosL1cFieldDescriptor("Flags", DataBuffer.TYPE_USHORT, 0),
            /*01*/ new SmosL1cFieldDescriptor("BT_Value", DataBuffer.TYPE_FLOAT, 2),
            /*02*/ new SmosL1cFieldDescriptor("Radiometric_Accuracy_of_Pixel", DataBuffer.TYPE_USHORT, 6),
            /*03*/ new SmosL1cFieldDescriptor("Azimuth_Angle", DataBuffer.TYPE_USHORT, 8),
            /*04*/ new SmosL1cFieldDescriptor("Footprint_Axis1", DataBuffer.TYPE_USHORT, 10),
            /*05*/ new SmosL1cFieldDescriptor("Footprint_Axis2", DataBuffer.TYPE_USHORT, 12),
            // --> size is 14 bytes total
    };

    private final SmosL1cFieldDescriptor[] fieldDescriptors;
    private final int dsrSize;

    public static SmosL1cRecordDescriptor create(String fileType) {
        if (fileType.endsWith("SCSD1C") || fileType.endsWith("SCLD1C")) {
            return new SmosL1cRecordDescriptor(D1C_FIELD_DESCRIPTORS, 24);
        }
        if (fileType.endsWith("SCSF1C") || fileType.endsWith("SCLF1C")) {
            return new SmosL1cRecordDescriptor(F1C_FIELD_DESCRIPTORS, 28);
        }
        if (fileType.endsWith("BWSD1C") || fileType.endsWith("BWLD1C") ||
                fileType.endsWith("BWSF1C") || fileType.endsWith("BWLF1C")) {
            return new SmosL1cRecordDescriptor(BW1C_FIELD_DESCRIPTORS, 14);
        }
        return null;
    }

    public SmosL1cFieldDescriptor[] getFieldDescriptors() {
        return fieldDescriptors.clone();
    }

    public int getFieldCount() {
        return fieldDescriptors.length;
    }

    public SmosL1cFieldDescriptor getFieldDescriptor(int i) {
        return fieldDescriptors[i];
    }

    public SmosL1cFieldDescriptor[] getFieldDescriptors(int[] fieldIndexes) {
        SmosL1cFieldDescriptor[] fieldDescriptors = new SmosL1cFieldDescriptor[fieldIndexes.length];
        for (int i = 0; i < fieldDescriptors.length; i++) {
            fieldDescriptors[i] = getFieldDescriptor(fieldIndexes[i]);            
        }
        return fieldDescriptors;
    }


    public int getRecordSize() {
        return dsrSize;
    }

    private SmosL1cRecordDescriptor(SmosL1cFieldDescriptor[] fieldDescriptors, int dsrSize) {
        this.fieldDescriptors = fieldDescriptors;
        this.dsrSize = dsrSize;
    }
}
