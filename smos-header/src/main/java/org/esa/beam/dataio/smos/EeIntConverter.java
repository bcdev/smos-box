package org.esa.beam.dataio.smos;

/**
 * Created by IntelliJ IDEA.
* User: Norman
* Date: 06.05.2008
* Time: 14:44:10
* To change this template use File | Settings | File Templates.
*/
public class EeIntConverter extends EeNumberConverter {
    public EeIntConverter() {
        super(Integer.class);
    }

    @Override
    protected Number parse(String value) {
        return Integer.valueOf(value);
    }
}
