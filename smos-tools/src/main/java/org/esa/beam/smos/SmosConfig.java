package org.esa.beam.smos;

public class SmosConfig {
    private String ftpBaseDir;
    private String inputDirectory;
    private String targetDirectory;
    private int userFileAvailability;
    private String[] globalSiteNames;
    private int ecmwfSiteMargin;
    private String linkInFtpHomeToAllData;

    public SmosConfig() {
        ecmwfSiteMargin = 120;
    }

    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public String getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public void setFtpBaseDir(String ftpBaseDir) {
        this.ftpBaseDir = ftpBaseDir;
    }

    public String getFtpBaseDir() {
        return ftpBaseDir;
    }

    public void setUserFileAvailability(int userFileAvailability) {
        this.userFileAvailability = userFileAvailability;
    }

    public int getUserFileAvailability() {
        return userFileAvailability;
    }

    public void setGlobalSiteNames(String[] globalSiteName) {
        this.globalSiteNames = globalSiteName;
    }

    public String[] getGlobalSiteNames() {
        return globalSiteNames;
    }

    public void setEcmwfSiteMargin(int ecmwfSiteMargin) {
        this.ecmwfSiteMargin = ecmwfSiteMargin;
    }

    public int getEcmwfSiteMargin() {
        return ecmwfSiteMargin;
    }

    public void setLinkInFtpHomeToAllData(String linkInFtpHomeToAllData) {
        this.linkInFtpHomeToAllData = linkInFtpHomeToAllData;
    }

    public String getLinkInFtpHomeToAllData() {
        return linkInFtpHomeToAllData;
    }
}
