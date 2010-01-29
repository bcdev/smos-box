package org.esa.beam.dataio.smos.dddb;

import java.util.List;

public interface Family<T> {

    List<T> asList();

    T getMember(String name);
}
