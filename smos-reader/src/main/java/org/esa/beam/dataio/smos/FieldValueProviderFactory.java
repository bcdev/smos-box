package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.SimpleType;
import com.bc.ceres.binio.Type;

import java.awt.geom.Area;
import java.io.IOException;

class FieldValueProviderFactory {

    static FieldValueProvider createProvider(SmosFile smosFile, int fieldIndex, Type type) {
        if (type.equals(SimpleType.BYTE)) {
            return new DefaultValueProvider(smosFile, fieldIndex);
        }
        if (type.equals(SimpleType.UBYTE)) {
            return new DefaultValueProvider(smosFile, fieldIndex);
        }
        if (type.equals(SimpleType.SHORT)) {
            return new DefaultValueProvider(smosFile, fieldIndex);
        }
        if (type.equals(SimpleType.USHORT)) {
            return new DefaultValueProvider(smosFile, fieldIndex);
        }
        if (type.equals(SimpleType.INT)) {
            return new DefaultValueProvider(smosFile, fieldIndex);
        }
        if (type.equals(SimpleType.UINT)) {
            return new DefaultValueProvider(smosFile, fieldIndex);
        }
        if (type.equals(SimpleType.FLOAT)) {
            return new DefaultValueProvider(smosFile, fieldIndex);
        }
        if (type.equals(SimpleType.DOUBLE)) {
            return new DoubleValueProvider(smosFile, fieldIndex);
        }
        if (type.equals(SimpleType.LONG)) {
            return new LongValueProvider(smosFile, fieldIndex);
        }
        if (type.equals(SimpleType.ULONG)) {
            return new LongValueProvider(smosFile, fieldIndex);
        }

        throw new IllegalArgumentException("type = " + type);
    }

    static FieldValueProvider createProviderForLoDWordOfLongField(final SmosFile smosFile, final int fieldIndex) {
        return new LongValueProvider(smosFile, fieldIndex);
    }

    static FieldValueProvider createProviderForHiDWordOfLongField(final SmosFile smosFile, final int fieldIndex) {
        return new DefaultValueProvider(smosFile, fieldIndex) {
            @Override
            public int getValue(int gridPointIndex, int noDataValue) {
                try {
                    final long value = getSmosFile().getGridPointData(gridPointIndex).getLong(getFieldIndex());
                    return (int) (value >>> 32);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private static class ValueProviderByte extends DefaultValueProvider {

        ValueProviderByte(SmosFile smosFile, int fieldIndex) {
            super(smosFile, fieldIndex);
        }

        @Override
        public byte getValue(int gridPointIndex, byte noDataValue) {
            try {
                return getSmosFile().getGridPointData(gridPointIndex).getByte(getFieldIndex());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class ValueProviderShort extends DefaultValueProvider {

        ValueProviderShort(SmosFile smosFile, int fieldIndex) {
            super(smosFile, fieldIndex);
        }

        @Override
        public short getValue(int gridPointIndex, short noDataValue) {
            try {
                return getSmosFile().getGridPointData(gridPointIndex).getShort(getFieldIndex());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class ValueProviderInt extends DefaultValueProvider {

        ValueProviderInt(SmosFile smosFile, int fieldIndex) {
            super(smosFile, fieldIndex);
        }

        @Override
        public int getValue(int gridPointIndex, int noDataValue) {
            try {
                return getSmosFile().getGridPointData(gridPointIndex).getInt(getFieldIndex());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class ValueProviderFloat extends DefaultValueProvider {

        ValueProviderFloat(SmosFile smosFile, int fieldIndex) {
            super(smosFile, fieldIndex);
        }

        @Override
        public float getValue(int gridPointIndex, float noDataValue) {
            try {
                return getSmosFile().getGridPointData(gridPointIndex).getFloat(getFieldIndex());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class DoubleValueProvider extends DefaultValueProvider {

        DoubleValueProvider(SmosFile smosFile, int fieldIndex) {
            super(smosFile, fieldIndex);
        }

        @Override
        public byte getValue(int gridPointIndex, byte noDataValue) {
            return 0;
        }

        @Override
        public short getValue(int gridPointIndex, short noDataValue) {
            return 0;
        }

        @Override
        public int getValue(int gridPointIndex, int noDataValue) {
            return 0;
        }

        @Override
        public float getValue(int gridPointIndex, float noDataValue) {
            try {
                return (float) getSmosFile().getGridPointData(gridPointIndex).getDouble(getFieldIndex());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class LongValueProvider extends DefaultValueProvider {

        LongValueProvider(SmosFile smosFile, int fieldIndex) {
            super(smosFile, fieldIndex);
        }

        @Override
        public int getValue(int gridPointIndex, int noDataValue) {
            try {
                return (int) getSmosFile().getGridPointData(gridPointIndex).getLong(getFieldIndex());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class DefaultValueProvider implements FieldValueProvider {

        private final SmosFile smosFile;
        private final int fieldIndex;

        protected DefaultValueProvider(SmosFile smosFile, int fieldIndex) {
            this.smosFile = smosFile;
            this.fieldIndex = fieldIndex;
        }

        public final SmosFile getSmosFile() {
            return smosFile;
        }

        public final int getFieldIndex() {
            return fieldIndex;
        }

        @Override
        public final Area getRegion() {
            return smosFile.getRegion();
        }

        @Override
        public final int getGridPointIndex(int seqnum) {
            return smosFile.getGridPointIndex(seqnum);
        }

        @Override
        public byte getValue(int gridPointIndex, byte noDataValue) {
            try {
                return smosFile.getGridPointData(gridPointIndex).getByte(fieldIndex);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public short getValue(int gridPointIndex, short noDataValue) {
            try {
                return smosFile.getGridPointData(gridPointIndex).getShort(fieldIndex);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int getValue(int gridPointIndex, int noDataValue) {
            try {
                return smosFile.getGridPointData(gridPointIndex).getInt(fieldIndex);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public float getValue(int gridPointIndex, float noDataValue) {
            try {
                return smosFile.getGridPointData(gridPointIndex).getFloat(fieldIndex);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
