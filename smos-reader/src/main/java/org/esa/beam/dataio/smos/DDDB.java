package org.esa.beam.dataio.smos;

import org.esa.beam.util.io.CsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DDDB {

    private static final Charset CHARSET = Charset.forName("US-ASCII");
    private static final char[] SEPARATORS = new char[]{'|'};

    private final ConcurrentMap<String, BandDescriptors> bandDescriptorMap;
    private final ConcurrentMap<String, FlagDescriptors> flagDescriptorMap;

    private DDDB() {
        bandDescriptorMap = new ConcurrentHashMap<String, BandDescriptors>(17);
        flagDescriptorMap = new ConcurrentHashMap<String, FlagDescriptors>(17);
    }

    public static DDDB getInstance() {
        return Holder.INSTANCE;
    }

    public Family<BandDescriptor> getBandDescriptors(String identifier) {
        if (!bandDescriptorMap.containsKey(identifier)) {
            final InputStream inputStream = getBandDescriptorResource(identifier);

            if (inputStream != null) {
                final BandDescriptors descriptors;

                try {
                    descriptors = readBandDescriptors(inputStream);
                } catch (Throwable e) {
                    throw new IllegalStateException(MessageFormat.format(
                            "Band descriptors resource for identifier ''{0}'': {1}", identifier, e.getMessage()));
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }

                bandDescriptorMap.putIfAbsent(identifier, descriptors);
            }
        }

        return bandDescriptorMap.get(identifier);
    }

    public Family<FlagDescriptor> getFlagDescriptors(String identifier) {
        if (!flagDescriptorMap.containsKey(identifier)) {
            final InputStream inputStream = getFlagDescriptorResource(identifier);

            if (inputStream != null) {
                final FlagDescriptors descriptors;

                try {
                    descriptors = readFlagDescriptors(inputStream);
                } catch (Throwable e) {
                    throw new IllegalStateException(MessageFormat.format(
                            "Flag descriptor resource for identifier ''{0}'': {1}", identifier, e.getMessage()));
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }

                flagDescriptorMap.putIfAbsent(identifier, descriptors);
            }
        }

        return flagDescriptorMap.get(identifier);
    }

    private static BandDescriptors readBandDescriptors(InputStream inputStream) throws IOException {
        final CsvReader reader = new CsvReader(new InputStreamReader(inputStream, CHARSET), SEPARATORS, true, "#");
        final List<String[]> recordList = reader.readStringRecords();

        return new BandDescriptors(recordList);
    }

    private static FlagDescriptors readFlagDescriptors(InputStream inputStream) throws IOException {
        final CsvReader reader = new CsvReader(new InputStreamReader(inputStream, CHARSET), SEPARATORS, true, "#");
        final List<String[]> recordList = reader.readStringRecords();

        return new FlagDescriptors(recordList);
    }

    private static InputStream getBandDescriptorResource(String identifier) {
        // Reference: SO-MA-IDR-GS-0004, SMOS DPGS, XML Schema Guidelines
        if (identifier == null || !identifier.matches("DBL_\\w{2}_\\w{4}_\\w{10}_\\d{4}")) {
            return null;
        }

        final String fc = identifier.substring(12, 16);
        final String sd = identifier.substring(16, 22);

        final StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append("bands/").append(fc).append("/").append(sd).append("/").append(identifier);
        pathBuilder.append(".csv");

        return SmosFormats.class.getResourceAsStream(pathBuilder.toString());
    }

    private static InputStream getFlagDescriptorResource(String identifier) {
        // Reference: SO-MA-IDR-GS-0004, SMOS DPGS, XML Schema Guidelines
        if (identifier == null || !identifier.matches("DBL_\\w{2}_\\w{4}_\\w{10}_\\d{4}_.*")) {
            return null;
        }

        final String fc = identifier.substring(12, 16);
        final String sd = identifier.substring(16, 22);

        final StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append("flags/").append(fc).append("/").append(sd).append("/").append(identifier);
        pathBuilder.append(".csv");

        return SmosFormats.class.getResourceAsStream(pathBuilder.toString());
    }

    // Initialization on demand holder idiom
    private static class Holder {

        private static final DDDB INSTANCE = new DDDB();
    }

    private static class BandDescriptors implements Family<BandDescriptor> {

        private final List<BandDescriptor> descriptorList;
        private final Map<String, BandDescriptor> descriptorMap;

        private BandDescriptors(List<String[]> recordList) {
            descriptorList = new ArrayList<BandDescriptor>(recordList.size());
            descriptorMap = new HashMap<String, BandDescriptor>(recordList.size());

            for (final String[] tokens : recordList) {
                final BandDescriptorImpl descriptor = new BandDescriptorImpl(tokens);
                descriptorList.add(descriptor);
                descriptorMap.put(descriptor.getBandName(), descriptor);
            }
        }

        @Override
        public final List<BandDescriptor> asList() {
            return Collections.unmodifiableList(descriptorList);
        }

        @Override
        public final BandDescriptor getMember(String bandName) {
            return descriptorMap.get(bandName);
        }
    }

    private static class FlagDescriptors implements Family<FlagDescriptor> {

        private final List<FlagDescriptor> descriptorList;
        private final Map<String, FlagDescriptor> descriptorMap;

        private FlagDescriptors(List<String[]> recordList) {
            descriptorList = new ArrayList<FlagDescriptor>(recordList.size());
            descriptorMap = new HashMap<String, FlagDescriptor>(recordList.size());

            for (final String[] tokens : recordList) {
                final FlagDescriptor record = new FlagDescriptor(tokens);
                descriptorList.add(record);
                descriptorMap.put(record.getFlagName(), record);
            }
        }

        @Override
        public final List<FlagDescriptor> asList() {
            return Collections.unmodifiableList(descriptorList);
        }

        @Override
        public final FlagDescriptor getMember(String flagName) {
            return descriptorMap.get(flagName);
        }
    }

    private static class BandDescriptorImpl implements BandDescriptor {

        private final String bandName;
        private final String memberName;
        private final int sampleModel;
        private final double scalingOffset;
        private final double scalingFactor;
        private final double typicalMin;
        private final double typicalMax;
        private final boolean cyclic;
        private final double fillValue;
        private final String validPixelExpression;
        private final String unit;
        private final String description;
        private final String flagCodingName;
        private final Family<FlagDescriptor> flagDescriptors;

        private BandDescriptorImpl(String[] tokens) {
            bandName = getString(tokens[1]);
            memberName = getString(tokens[2], bandName);
            sampleModel = getInt(tokens[3], 0);

            scalingOffset = getDouble(tokens[4], 0.0);
            scalingFactor = getDouble(tokens[5], 1.0);

            typicalMin = getDouble(tokens[6], Double.NEGATIVE_INFINITY);
            typicalMax = getDouble(tokens[7], Double.POSITIVE_INFINITY);
            cyclic = getBoolean(tokens[8], false);

            fillValue = getDouble(tokens[9], Double.NaN);
            validPixelExpression = getString(tokens[10], "").replaceAll("x", bandName);

            unit = getString(tokens[11], "");
            description = getString(tokens[12], "");
            flagCodingName = getString(tokens[13], "");
            flagDescriptors = getFlagDescriptors(tokens[14]);
        }

        private Family<FlagDescriptor> getFlagDescriptors(String token) {
            if (flagCodingName.isEmpty()) {
                return null;
            }
            return getInstance().getFlagDescriptors(getString(token));
        }

        @Override
        public final String getBandName() {
            return bandName;
        }

        @Override
        public final String getMemberName() {
            return memberName;
        }

        @Override
        public final int getSampleModel() {
            return sampleModel;
        }

        @Override
        public final double getScalingOffset() {
            return scalingOffset;
        }

        @Override
        public final double getScalingFactor() {
            return scalingFactor;
        }

        @Override
        public final boolean hasTypicalMin() {
            return !Double.isInfinite(typicalMin);
        }

        @Override
        public final boolean hasTypicalMax() {
            return !Double.isInfinite(typicalMax);
        }

        @Override
        public final boolean hasFillValue() {
            return !Double.isNaN(fillValue);
        }

        @Override
        public final double getTypicalMin() {
            return typicalMin;
        }

        @Override
        public final double getTypicalMax() {
            return typicalMax;
        }

        @Override
        public final double getFillValue() {
            return fillValue;
        }

        @Override
        public final String getValidPixelExpression() {
            return validPixelExpression;
        }

        @Override
        public final String getUnit() {
            return unit;
        }

        @Override
        public final boolean isCyclic() {
            return cyclic;
        }

        @Override
        public final String getDescription() {
            return description;
        }

        @Override
        public final String getFlagCodingName() {
            return flagCodingName;
        }

        @Override
        public final Family<FlagDescriptor> getFlagDescriptors() {
            return flagDescriptors;
        }

        @SuppressWarnings({"SimplifiableIfStatement"})
        private static boolean getBoolean(String token, boolean defaultValue) {
            if ("*".equals(token.trim())) {
                return defaultValue;
            }
            return Boolean.parseBoolean(token);
        }

        private static double getDouble(String token, double defaultValue) {
            if ("*".equals(token.trim())) {
                return defaultValue;
            }
            return Double.parseDouble(token);
        }

        private static int getInt(String token, int defaultValue) {
            if ("*".equals(token.trim())) {
                return defaultValue;
            }
            return Integer.parseInt(token);
        }

        private static String getString(String token) {
            return token.trim();
        }

        private static String getString(String token, String defaultValue) {
            if ("*".equals(token.trim())) {
                return defaultValue;
            }
            return token.trim();
        }
    }
}
