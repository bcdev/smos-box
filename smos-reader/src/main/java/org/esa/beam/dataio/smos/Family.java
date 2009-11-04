package org.esa.beam.dataio.smos;

import java.util.List;

public interface Family<T> {

    List<T> asList();

    T getMember(String name);
}
