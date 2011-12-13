package org.esa.beam.smos.dto;

public class Source implements Comparable<Source> {

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreatorVersion() {
        return creatorVersion;
    }

    public void setCreatorVersion(String creatorVersion) {
        this.creatorVersion = creatorVersion;
    }

    //package local for testing
    static int compareString_nullFirst(String lhs, String rhs) {
        //check first if the references are equal, so if both are null, 0 is returned instead of returning -1 in the next check
        if( lhs == rhs )
            return 0;

        //null is less than non-null according to this method
        if( lhs == null )
            return -1;
        if( rhs == null )
            return 1;

        return lhs.compareTo(rhs);

    }

    public int compareTo(Source o) {
        int ret = compareString_nullFirst( getId(), o.getId());
        if( ret != 0 )
            return ret;
        ret = compareString_nullFirst( getSystem(), o.getSystem() );
        if( ret != 0 )
            return ret;
        ret = compareString_nullFirst( getCreator(), o.getCreator() );
        if( ret != 0 )
            return ret;
        ret = compareString_nullFirst( getCreatorVersion(), o.getCreatorVersion() );
        return ret;
    }

    @Override
    public boolean equals(Object source) {
        return compareTo((Source)source) == 0;
    }

    ////////////////////////////////////////////////////////////////////////////////
    /////// END OF PUBLIC
    ////////////////////////////////////////////////////////////////////////////////

    private String id;
    private String system;
    private String creator;
    private String creatorVersion;

}
