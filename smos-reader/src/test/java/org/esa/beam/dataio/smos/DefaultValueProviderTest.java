package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.CompoundData;
import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Area;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class DefaultValueProviderTest {

    private DggFile dggFile;
    private DefaultValueProvider defaultValueProvider;
    private CompoundData compoundData;

    @Before
    public void setUp() {
        dggFile = mock(DggFile.class);
        compoundData = mock(CompoundData.class);
        defaultValueProvider = new DefaultValueProvider(dggFile, 12);
    }

    @Test
    public void testGetArea() {
        when(dggFile.getArea()).thenReturn(new Area());

        defaultValueProvider.getArea();

        verify(dggFile, times(1)).getArea();
    }

    @Test
    public void testGetGridPointIndex() {
        when(dggFile.getGridPointIndex(anyInt())).thenReturn(34);

        defaultValueProvider.getGridPointIndex(45);

        verify(dggFile, times(1)).getGridPointIndex(anyInt());
    }

    @Test
    public void testGetByte() throws IOException {
        when(dggFile.getGridPointData(anyInt())).thenReturn(compoundData);
        when(compoundData.getByte(anyInt())).thenReturn(new Byte("1"));

        defaultValueProvider.getByte(45);

        verify(dggFile, times(1)).getGridPointData(45);
        verify(compoundData, times(1)).getByte(anyInt());
    }

    @Test
    public void testGetShort() throws IOException {
        when(dggFile.getGridPointData(anyInt())).thenReturn(compoundData);
        when(compoundData.getShort(anyInt())).thenReturn(new Short("4"));

        defaultValueProvider.getShort(46);

        verify(dggFile, times(1)).getGridPointData(46);
        verify(compoundData, times(1)).getShort(anyInt());
    }

    @Test
    public void testGetInt() throws IOException {
        when(dggFile.getGridPointData(anyInt())).thenReturn(compoundData);
        when(compoundData.getInt(anyInt())).thenReturn(5);

        defaultValueProvider.getInt(47);

        verify(dggFile, times(1)).getGridPointData(47);
        verify(compoundData, times(1)).getInt(anyInt());
    }

    @Test
    public void testGetLong() throws IOException {
        when(dggFile.getGridPointData(anyInt())).thenReturn(compoundData);
        when(compoundData.getLong(anyInt())).thenReturn(6L);

        defaultValueProvider.getLong(48);

        verify(dggFile, times(1)).getGridPointData(48);
        verify(compoundData, times(1)).getLong(anyInt());
    }

    @Test
    public void testGetFloat() throws IOException {
        when(dggFile.getGridPointData(anyInt())).thenReturn(compoundData);
        when(compoundData.getFloat(anyInt())).thenReturn(7.f);

        defaultValueProvider.getFloat(49);

        verify(dggFile, times(1)).getGridPointData(49);
        verify(compoundData, times(1)).getFloat(anyInt());
    }
}
