package org.esa.beam.dataio.smos;

import javax.imageio.stream.ImageInputStream;
import java.awt.image.DataBuffer;
import java.io.IOException;

public class SmosL1cFieldDescriptor {
    final String name;
    final int dataType;
    final int offset;
    final int size;

    public SmosL1cFieldDescriptor(String name, int dataType, int offset) {
        this.name = name;
        this.dataType = dataType;
        this.offset = offset;
        this.size = DataBuffer.getDataTypeSize(dataType) / 8;
    }

    public SmosL1cField createField(int length) {
        final SmosL1cField field;
        if (dataType == DataBuffer.TYPE_BYTE) {
            field = new ByteSmosL1cField(this, length);
        } else if (dataType == DataBuffer.TYPE_SHORT || dataType == DataBuffer.TYPE_USHORT) {
            field = new ShortSmosL1cField(this, length);
        } else if (dataType == DataBuffer.TYPE_INT) {
            field = new IntSmosL1cField(this, length);
        } else if (dataType == DataBuffer.TYPE_FLOAT) {
            field = new FloatSmosL1cField(this, length);
        } else if (dataType == DataBuffer.TYPE_DOUBLE) {
            field = new DoubleSmosL1cField(this, length);
        } else {
            throw new IllegalArgumentException("dataType");
        }
        return field;
    }

    private static class ByteSmosL1cField extends SmosL1cField {
        private byte[] array;

        private ByteSmosL1cField(SmosL1cFieldDescriptor descriptor, int length) {
            super(descriptor);
            array = new byte[length];
        }

        public Object getData() {
            return array;
        }

        public void readDataElement(ImageInputStream iis, int index) throws IOException {
            array[index] = iis.readByte();
        }
    }

    private static class ShortSmosL1cField extends SmosL1cField {
        private short[] array;

        private ShortSmosL1cField(SmosL1cFieldDescriptor descriptor, int length) {
            super(descriptor);
            array = new short[length];
        }

        public Object getData() {
            return array;
        }

        public void readDataElement(ImageInputStream iis, int index) throws IOException {
            array[index] = iis.readShort();
        }
    }

    private static class IntSmosL1cField extends SmosL1cField {
        private int[] array;

        private IntSmosL1cField(SmosL1cFieldDescriptor descriptor, int length) {
            super(descriptor);
            array = new int[length];
        }

        public Object getData() {
            return array;
        }

        public void readDataElement(ImageInputStream iis, int index) throws IOException {
            array[index] = iis.readInt();
        }
    }

    private static class FloatSmosL1cField extends SmosL1cField {
        private float[] array;

        private FloatSmosL1cField(SmosL1cFieldDescriptor descriptor, int length) {
            super(descriptor);
            array = new float[length];
        }

        public Object getData() {
            return array;
        }

        public void readDataElement(ImageInputStream iis, int index) throws IOException {
            array[index] = iis.readFloat();
        }
    }

    private static class DoubleSmosL1cField extends SmosL1cField {
        private double [] array;

        private DoubleSmosL1cField(SmosL1cFieldDescriptor descriptor, int length) {
            super(descriptor);
            array = new double[length];
        }

        public Object getData() {
            return array;
        }

        public void readDataElement(ImageInputStream iis, int index) throws IOException {
            array[index] = iis.readDouble();
        }
    }
}
