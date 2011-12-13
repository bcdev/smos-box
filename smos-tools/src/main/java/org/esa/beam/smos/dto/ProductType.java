package org.esa.beam.smos.dto;

public class ProductType {

    public static final String TYPE_UNKNOWN = "UNKNOWN";

    public static ProductType createUnknownType() {
        final ProductType result = new ProductType();
        result.setTypeString(TYPE_UNKNOWN);
        return result;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setTypeString(String typeString) {
        this.typeString = typeString;
    }

    public String getTypeString() {
        return typeString;
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    /////// END OF PUBLIC
    ////////////////////////////////////////////////////////////////////////////////

    private String id;
    private String typeString;
}
