package org.esa.beam.smos.dto;

import com.bc.util.geom.Geometry;

import java.util.Date;

// todo se/tb 1 ... rename to ExplorerFile
//                  This dto does not contain any smos specific properties
public class SmosFile {

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * Path and file name with extension.
     * @return path and file name with extension of dbl file
     */
    public String getPath_DBL() {
        return path_DBL;
    }

    /**
     * Path and file name with extension.
     * @param path path and file name with extension of dbl file
     */
    public void setPath_DBL(String path) {
        this.path_DBL = path;
    }

    public String getPath_HDR() {
        return path_HDR;
    }

    public void setPath_HDR(String path) {
        this.path_HDR = path;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setOfflineDate(Date offlineDate) {
        this.offlineDate = offlineDate;
    }

    public Date getOfflineDate() {
        return offlineDate;
    }

    public String getHashValue_DBL() {
        return hashValue_DBL;
    }

    public void setHashValue_DBL(String hashValue_DBL) {
        this.hashValue_DBL = hashValue_DBL;
    }

    public String getHashValue_HDR() {
        return hashValue_HDR;
    }

    public void setHashValue_HDR(String hashValue_HDR) {
        this.hashValue_HDR = hashValue_HDR;
    }

    public void setGeoBounds(Geometry geoBounds) {
        this.geoBounds = geoBounds;
    }

    public Geometry getGeoBounds() {
        return geoBounds;
    }

    public Date getSensingStart() {
        return sensingStart;
    }

    /**
     * @param sensingStart the new sensingStart value. It can be null, which is automatically converted to the epoch
     */
    public void setSensingStart(Date sensingStart) {
        if (sensingStart == null) {
            sensingStart = epoch;
        }
        this.sensingStart = sensingStart;
    }

    public Date getSensingStop() {
        return sensingStop;
    }

    /**
     * @param sensingStop the new sensingStop value. It can be null, which is automatically converted to the epoch
     */
    public void setSensingStop(Date sensingStop) {
        if (sensingStop == null) {
            sensingStart = epoch;
        }
        this.sensingStop = sensingStop;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Integer getAbsOrbit() {
        return absOrbit;
    }

    public void setAbsOrbit(Integer absOrbit) {
        this.absOrbit = absOrbit;
    }

    public Integer getRelOrbit() {
        return relOrbit;
    }

    public void setRelOrbit(Integer relOrbit) {
        this.relOrbit = relOrbit;
    }

    public String getProcessingCentre() {
        return processingCentre;
    }

    public void setProcessingCentre(String processingCentre) {
        this.processingCentre = processingCentre;
    }

    public String getLogicalProcessingCentre() {
        return logicalProcessingCentre;
    }

    public void setLogicalProcessingCentre(String logicalProcessingCentre) {
        this.logicalProcessingCentre = logicalProcessingCentre;
    }

    //============================================================================
    // Calculated properties
    //============================================================================

    /**
     * This property refers to the presense of sensingStart and sensingStop being present
     *
     * @return true if sensingStop is present and will return a valid Date, false if it will return either null or the
     *         special value indicating the sensingStop is not present (special value is the epoch).
     */
    public boolean isValidityStartPresent() {
        return getSensingStart() != null && getSensingStart().getTime() != 0;
    }

    /**
     * This property refers to the presense of sensingStart and sensingStop being present
     *
     * @return true if sensingStop is present and will return a valid Date, false if it will return either null or the
     *         special value indicating the sensingStop is not present (special value is the epoch).
     */
    public boolean isValidityStopPresent() {
        return getSensingStop() != null && getSensingStop().getTime() != 0;
    }

    ////////////////////////////////////////////////////////////////////////////////
    /////// END OF PUBLIC
    ////////////////////////////////////////////////////////////////////////////////

    private static final Date epoch = new Date(0);

    private String name;
    private String id;
    private String path_DBL;
    private String path_HDR;
    private ProductType productType;
    private long sizeInBytes;
    private Date deliveryDate;
    private Date publicationDate;
    private Date offlineDate;
    private String hashValue_DBL;
    private String hashValue_HDR;
    private Geometry geoBounds;
    private Date sensingStart = epoch;
    private Date sensingStop = epoch;
    private Source source;
    //Abs_Orbit from HDR xml
    private Integer absOrbit;
    //Rel_Orbit from HDR xml
    private Integer relOrbit;
    private String processingCentre;
    private String logicalProcessingCentre;
}
