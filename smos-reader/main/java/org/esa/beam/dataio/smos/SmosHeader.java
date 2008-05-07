package org.esa.beam.dataio.smos;

import org.jdom.JDOMException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.JDomReader;

// todo - header should use a general EE MPH unmarshaller and should be configured with sensor specific SPH unmarshaller

public class SmosHeader {
    private EeFixedHeader fixedHeader;
    private SmosDsDescriptor[] dsDescriptors;

    public SmosHeader(InputStream headerInputStream) throws IOException {
        SAXBuilder builder = new SAXBuilder();
        final Document document;
        try {
            document = builder.build(headerInputStream);
        } catch (JDOMException e) {
            throw new IOException(e);
        }
        fixedHeader = unmarshalFixedHeader(document.getRootElement());
        dsDescriptors = unmarshalDataSetDescriptors(document.getRootElement());
    }

    public EeFixedHeader getFixedHeader() {
        return fixedHeader;
    }

    public SmosDsDescriptor[] getDsDescriptors() {
        return dsDescriptors.clone();
    }

    public SmosDsDescriptor getDsDescriptor(final String dsName) {
        for (SmosDsDescriptor dsDescriptor : dsDescriptors) {
            if (dsName.equalsIgnoreCase(dsDescriptor.getDsName())) {
                return dsDescriptor;
            }
        }
        return null;
    }

    private static EeFixedHeader unmarshalFixedHeader(Element rootElement) {
        final Element fixedHeaderElement = rootElement.getChild("Fixed_Header", rootElement.getNamespace());
        final XStream xStream = new XStream();
        xStream.autodetectAnnotations(true);
        xStream.alias("Fixed_Header", EeFixedHeader.class);
        final EeFixedHeader fixedHeader = new EeFixedHeader();
        xStream.unmarshal(new JDomReader(fixedHeaderElement), fixedHeader);
        return fixedHeader;
    }

    private static SmosDsDescriptor[] unmarshalDataSetDescriptors(Element rootElement) {
        Element[] dataSetElements = getChildren(rootElement, "Variable_Header/Specific_Product_Header/List_of_Data_Sets", "Data_Set");
        final SmosDsDescriptor[] dsDescriptors = new SmosDsDescriptor[dataSetElements.length];
        final XStream xStream = new XStream();
        xStream.autodetectAnnotations(true);
        xStream.alias("Data_Set", SmosDsDescriptor.class);
        for (int i = 0; i < dataSetElements.length; i++) {
            Element dataSetElement = dataSetElements[i];
            final SmosDsDescriptor dsDescriptor = new SmosDsDescriptor();
            xStream.unmarshal(new JDomReader(dataSetElement), dsDescriptor);
            dsDescriptor.afterPropertiesSet();
            dsDescriptors[i] = dsDescriptor;
        }
        return dsDescriptors;
    }

    private static Element[] getChildren(Element element, String path, String name) {
        Element childElement = element;
        String childPath = path;
        while (childPath != null) {
            final int index = childPath.indexOf('/');
            final String childName;
            if (index > 0) {
                childName = childPath.substring(0, index);
                childPath = childPath.substring(index + 1);
            } else {
                childName = childPath;
                childPath = null;
            }
            childElement = childElement.getChild(childName, childElement.getNamespace());
            if (childElement == null) {
                return null;
            }
        }
        final List<?> children = childElement.getChildren(name, childElement.getNamespace());
        return children.toArray(new Element[children.size()]);
    }

}
