package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.DataFormat;
import static junit.framework.Assert.*;
import org.esa.beam.util.io.CsvReader;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BandDescriptorRegistryTest {

    @Test
    public void getDescriptorsFor_DBL_SM_XXXX_AUX_ECMWF__0200() {
        final DataFormat dataFormat = SmosFormats.getInstance().getFormat("DBL_SM_XXXX_AUX_ECMWF__0200");
        assertNotNull(dataFormat);
        final String formatName = dataFormat.getName();
        assertEquals("DBL_SM_XXXX_AUX_ECMWF__0200", formatName);

        final BandDescriptors descriptors = BandDescriptorRegistry.getInstance().getDescriptors(formatName);
        assertEquals(53, descriptors.getDescriptorList().size());

        final BandDescriptor descriptor = descriptors.getDescriptor("RR");
        assertNotNull(descriptor);
        assertEquals("RR", descriptor.getBandName());
        assertEquals("Rain_Rate", descriptor.getMemberName());
        assertTrue(descriptor.hasTypicalMin());
        assertEquals(0.0, descriptor.getTypicalMin(), 0.0);
        assertTrue(descriptor.hasTypicalMax());
        assertEquals(500.0, descriptor.getTypicalMax(), 0.0);
    }

    public static class BandDescriptorRegistry {

        private static BandDescriptorRegistry uniqueInstance = new BandDescriptorRegistry();
        private final ConcurrentMap<String, BandDescriptors> map;

        private BandDescriptorRegistry() {
            map = new ConcurrentHashMap<String, BandDescriptors>(17);
        }

        public static BandDescriptorRegistry getInstance() {
            return uniqueInstance;
        }

        public BandDescriptors getDescriptors(String formatName) {
            if (!map.containsKey(formatName)) {
                final URL resourceUrl = getBandDescriptorResource(formatName);

                if (resourceUrl != null) {
                    try {
                        final CsvReader csvReader = createCsvReader(resourceUrl.toURI());
                        final List<String[]> recordList = csvReader.readStringRecords();
                        map.putIfAbsent(formatName, new BandDescriptors(recordList));
                    } catch (IOException e) {
                        throw new IllegalStateException(
                                MessageFormat.format("Schema resource ''{0}'': {1}", resourceUrl, e.getMessage()));
                    } catch (URISyntaxException e) {
                        throw new IllegalStateException(
                                MessageFormat.format("Schema resource ''{0}'': {1}", resourceUrl, e.getMessage()));
                    }
                }
            }

            return map.get(formatName);
        }

        private static CsvReader createCsvReader(URI uri) throws FileNotFoundException {
            final InputStream inputStream = new BufferedInputStream(new FileInputStream(new File(uri)));
            final Reader reader = new InputStreamReader(inputStream, Charset.forName("US-ASCII"));

            return new CsvReader(reader, new char[]{'|'}, false, "#");
        }

        private static URL getBandDescriptorResource(String formatName) {
            // Reference: SO-MA-IDR-GS-0004, SMOS DPGS, XML Schema Guidelines
            if (formatName == null || !formatName.matches("DBL_\\w{2}_\\w{4}_\\w{10}_\\d{4}")) {
                return null;
            }

            final String fc = formatName.substring(12, 16);
            final String sd = formatName.substring(16, 22);

            final StringBuilder pathBuilder = new StringBuilder();
            pathBuilder.append("bands/").append(fc).append("/").append(sd).append("/").append(formatName);
            pathBuilder.append(".txt");

            return SmosFormats.class.getResource(pathBuilder.toString());
        }
    }

    public static class BandDescriptors {

        private final List<BandDescriptor> recordList;
        private final Map<String, BandDescriptor> recordMap;

        BandDescriptors(List<String[]> stringRecordList) {
            recordList = new ArrayList<BandDescriptor>(stringRecordList.size());
            recordMap = new HashMap<String, BandDescriptor>(stringRecordList.size());

            for (String[] strings : stringRecordList) {
                final BandDescriptor record = new BandDescriptor(strings);
                recordList.add(record);
                recordMap.put(record.bandName, record);
            }
        }

        public List<BandDescriptor> getDescriptorList() {
            return Collections.unmodifiableList(recordList);
        }

        public BandDescriptor getDescriptor(String bandName) {
            return recordMap.get(bandName);
        }
    }

    public static class BandDescriptor {

        final String bandName;
        final String memberName;
        final int sampleModel;
        final int dataType;
        final double scalingOffset;
        final double scalingFactor;
        final double typicalMin;
        final double typicalMax;
        final double noDataValue;
        final String validPixelExpression;
        final String unit;
        final boolean cyclicColorLegend;
        final String description;

        BandDescriptor(String[] tokens) {
            bandName = getString(tokens[0]);
            memberName = getString(tokens[1], bandName);

            sampleModel = getInt(tokens[2], 0);
            dataType = getInt(tokens[3], 0);

            scalingOffset = getDouble(tokens[4], 0.0);
            scalingFactor = getDouble(tokens[5], 1.0);

            typicalMin = getDouble(tokens[6], Double.NaN);
            typicalMax = getDouble(tokens[7], Double.NaN);

            noDataValue = getDouble(tokens[8], Double.NaN);
            validPixelExpression = getString(tokens[9], "");

            unit = getString(tokens[10], "");
            cyclicColorLegend = getBoolean(tokens[11], false);
            description = getString(tokens[12], "");
        }

        public String getBandName() {
            return bandName;
        }

        public String getMemberName() {
            return memberName;
        }

        public int getSampleModel() {
            return sampleModel;
        }

        public int getDataType() {
            return dataType;
        }

        public double getScalingOffset() {
            return scalingOffset;
        }

        public double getScalingFactor() {
            return scalingFactor;
        }

        public boolean hasTypicalMin() {
            return !Double.isNaN(typicalMin);
        }

        public boolean hasTypicalMax() {
            return !Double.isNaN(typicalMax);
        }

        public double getTypicalMin() {
            return typicalMin;
        }

        public double getTypicalMax() {
            return typicalMax;
        }

        public boolean hasNoDataValue() {
            return !Double.isNaN(noDataValue);
        }

        public double getNoDataValue() {
            return noDataValue;
        }

        public boolean hasValidPixelExpression() {
            return !validPixelExpression.isEmpty();
        }

        public String getValidPixelExpression() {
            return validPixelExpression;
        }

        public boolean hasUnit() {
            return !unit.isEmpty();
        }

        public String getUnit() {
            return unit;
        }

        public boolean isCyclicColorLegend() {
            return cyclicColorLegend;
        }

        public boolean hasDescription() {
            return !description.isEmpty();
        }

        public String getDescription() {
            return description;
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
    }

}
