package org.esa.beam.dataio.smos;

/**
 * Created by IntelliJ IDEA.
* User: Norman
* Date: 06.05.2008
* Time: 14:44:20
* To change this template use File | Settings | File Templates.
*/
public class EeLongConverter extends EeNumberConverter {
    public EeLongConverter() {
        super(Long.class);
    }

    @Override
    protected Number parse(String value) {
        return Long.valueOf(value);
    }
}
