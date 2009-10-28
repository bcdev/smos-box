package org.esa.beam.dataio.smos;

import org.esa.beam.util.io.CsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BandDescriptorRegistry {

    private static final Charset CHARSET = Charset.forName("US-ASCII");
    private static final char[] SEPARATORS = new char[]{'|'};

    private static final BandDescriptorRegistry uniqueInstance = new BandDescriptorRegistry();
    private final ConcurrentMap<String, BandDescriptors> map;

    private BandDescriptorRegistry() {
        map = new ConcurrentHashMap<String, BandDescriptors>(17);
    }

    public static BandDescriptorRegistry getInstance() {
        return uniqueInstance;
    }

    public BandDescriptors getDescriptors(String formatName) {
        if (!map.containsKey(formatName)) {
            final InputStream inputStream = getBandDescriptorResource(formatName);

            if (inputStream != null) {
                final BandDescriptors descriptors;

                try {
                    descriptors = readDescriptors(inputStream);
                } catch (Throwable e) {
                    throw new IllegalStateException(MessageFormat.format(
                            "Band descriptor resource for format ''{0}'': {1}", formatName, e.getMessage()));
                }

                map.putIfAbsent(formatName, descriptors);
            }
        }

        return map.get(formatName);
    }

    private static BandDescriptors readDescriptors(InputStream inputStream) throws IOException {
        final CsvReader reader = new CsvReader(new InputStreamReader(inputStream, CHARSET), SEPARATORS, true, "#");
        final List<String[]> recordList = reader.readStringRecords();

        return new BandDescriptors(recordList);
    }

    private static InputStream getBandDescriptorResource(String formatName) {
        // Reference: SO-MA-IDR-GS-0004, SMOS DPGS, XML Schema Guidelines
        if (formatName == null || !formatName.matches("DBL_\\w{2}_\\w{4}_\\w{10}_\\d{4}")) {
            return null;
        }

        final String fc = formatName.substring(12, 16);
        final String sd = formatName.substring(16, 22);

        final StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append("bands/").append(fc).append("/").append(sd).append("/").append(formatName);
        pathBuilder.append(".txt");

        return SmosFormats.class.getResourceAsStream(pathBuilder.toString());
    }
}
