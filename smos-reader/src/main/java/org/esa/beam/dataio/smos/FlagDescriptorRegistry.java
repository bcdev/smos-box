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

public class FlagDescriptorRegistry {

    private static final Charset CHARSET = Charset.forName("US-ASCII");
    private static final char[] SEPARATORS = new char[]{'|'};

    private final ConcurrentMap<String, FlagDescriptors> map;

    private FlagDescriptorRegistry() {
        map = new ConcurrentHashMap<String, FlagDescriptors>(17);
    }

    public static FlagDescriptorRegistry getInstance() {
        return Holder.instance;
    }

    public FlagDescriptors getDescriptors(String name) {
        if (!map.containsKey(name)) {
            final InputStream inputStream = getFlagDescriptorResource(name);

            if (inputStream != null) {
                final FlagDescriptors descriptors;

                try {
                    descriptors = readDescriptors(inputStream);
                } catch (Throwable e) {
                    throw new IllegalStateException(MessageFormat.format(
                            "Band descriptor resource for format ''{0}'': {1}", name, e.getMessage()));
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }

                map.putIfAbsent(name, descriptors);
            }
        }

        return map.get(name);
    }

    private static FlagDescriptors readDescriptors(InputStream inputStream) throws IOException {
        final CsvReader reader = new CsvReader(new InputStreamReader(inputStream, CHARSET), SEPARATORS, true, "#");
        final List<String[]> recordList = reader.readStringRecords();

        return new FlagDescriptors(recordList);
    }

    private static InputStream getFlagDescriptorResource(String name) {
        // Reference: SO-MA-IDR-GS-0004, SMOS DPGS, XML Schema Guidelines
        if (name == null || !name.matches("DBL_\\w{2}_\\w{4}_\\w{10}_\\d{4}_.*")) {
            return null;
        }

        final String fc = name.substring(12, 16);
        final String sd = name.substring(16, 22);

        final StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append("flags/").append(fc).append("/").append(sd).append("/").append(name);

        return SmosFormats.class.getResourceAsStream(pathBuilder.toString());
    }

    // Initialization on demand holder idiom
    private static class Holder {

        private static final FlagDescriptorRegistry instance = new FlagDescriptorRegistry();
    }

}
