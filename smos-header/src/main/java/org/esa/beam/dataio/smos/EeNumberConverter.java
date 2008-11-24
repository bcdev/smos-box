package org.esa.beam.dataio.smos;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Created by IntelliJ IDEA.
* User: Norman
* Date: 06.05.2008
* Time: 14:44:03
* To change this template use File | Settings | File Templates.
*/
public abstract class EeNumberConverter implements Converter {
    private final Class<? extends Number> type;

    protected EeNumberConverter(Class<? extends Number> type) {
        this.type = type;
    }

    public boolean canConvert(Class aClass) {
        return aClass == this.type;
    }

    public Object unmarshal(HierarchicalStreamReader hierarchicalStreamReader, UnmarshallingContext unmarshallingContext) {
        String value = hierarchicalStreamReader.getValue();
        if (value.startsWith("+")) {
            return parse(eatZeros(value.substring(1)));
        } else if (value.startsWith("-")) {
            return parse("-" + eatZeros(value.substring(1)));
        } else {
            return parse(eatZeros(value));
        }
    }

    private static String eatZeros(String value) {
        while (value.length() > 1 && value.startsWith("0")) {
            value = value.substring(1);
        }
        return value;
    }

    protected abstract Number parse(String value);

    public void marshal(Object o, HierarchicalStreamWriter hierarchicalStreamWriter, MarshallingContext marshallingContext) {
        hierarchicalStreamWriter.setValue(o.toString());
    }
}
