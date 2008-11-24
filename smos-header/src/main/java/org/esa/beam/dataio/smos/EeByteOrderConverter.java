package org.esa.beam.dataio.smos;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.nio.ByteOrder;

/**
 * Created by IntelliJ IDEA.
* User: Norman
* Date: 06.05.2008
* Time: 14:43:54
* To change this template use File | Settings | File Templates.
*/
public class EeByteOrderConverter implements Converter {
    public boolean canConvert(Class aClass) {
        return aClass == ByteOrder.class;
    }

    public Object unmarshal(HierarchicalStreamReader hierarchicalStreamReader, UnmarshallingContext unmarshallingContext) {
        final String value = hierarchicalStreamReader.getValue();
        return "0123".equals(value) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
    }

    public void marshal(Object o, HierarchicalStreamWriter hierarchicalStreamWriter, MarshallingContext marshallingContext) {
        final ByteOrder order = (ByteOrder) o;
        hierarchicalStreamWriter.setValue(ByteOrder.LITTLE_ENDIAN.equals(order) ? "0123" : "3210");
    }
}
