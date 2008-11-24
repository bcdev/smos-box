package org.esa.beam.dataio.smos;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import java.nio.ByteOrder;

/**
 * Created by IntelliJ IDEA.
 * User: Norman
 * Date: 05.05.2008
 * Time: 10:27:53
 * To change this template use File | Settings | File Templates.
 */ //  <Data_Set>
//    <DS_Name>Swath_Snapshot_List</DS_Name>
//    <DS_Type>M</DS_Type>
//    <DS_Size>0000052756</DS_Size>
//    <DS_Offset>0000000000</DS_Offset>
//    <Ref_Filename></Ref_Filename>
//    <Num_DSR>0000000336</Num_DSR>
//    <DSR_Size>00000157</DSR_Size>
//    <Byte_Order>0123</Byte_Order>
//  </Data_Set>
@XStreamAlias("Data_Set")
public class SmosDsDescriptor {
    @XStreamAlias("DS_Name")
    String dsName;
    @XStreamAlias("DS_Type")
    char dsType;
    @XStreamAlias("DS_Size")
    @XStreamConverter(EeLongConverter.class)
    long dsSize;
    @XStreamAlias("DS_Offset")
    @XStreamConverter(EeLongConverter.class)
    long dsOffset;
    @XStreamAlias("Ref_Filename")
    String refFilename;
    @XStreamAlias("Num_DSR")
    @XStreamConverter(EeIntConverter.class)
    int dsrCount;
    @XStreamAlias("DSR_Size")
    @XStreamConverter(EeIntConverter.class)
    int dsrSize;
    @XStreamAlias("Byte_Order")
    @XStreamConverter(EeByteOrderConverter.class)
    ByteOrder byteOrder;

    public String getDsName() {
        return dsName;
    }

    public char getDsType() {
        return dsType;
    }

    public long getDsSize() {
        return dsSize;
    }

    public long getDsOffset() {
        return dsOffset;
    }

    public String getRefFilename() {
        return refFilename;
    }

    public int getDsrCount() {
        return dsrCount;
    }

    public int getDsrSize() {
        return dsrSize;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    void afterPropertiesSet() {
        if (dsName != null) {
            dsName = dsName.trim();
        }
        if (refFilename != null) {
            refFilename = refFilename.trim();
        }
    }
}
