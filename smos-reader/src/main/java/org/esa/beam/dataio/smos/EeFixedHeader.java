package org.esa.beam.dataio.smos;

import com.thoughtworks.xstream.annotations.XStreamAlias;

//<Fixed_Header>
//    <File_Name>SM_TEST_MIR_SCLD1C_20070223T061024_20070223T070437_141_000_0</File_Name>
//    <File_Description>Level 1C Dual Polarization Land Science measurements product</File_Description>
//    <Notes>x</Notes>
//    <Mission>SMOS</Mission>
//    <File_Class>TEST</File_Class>
//    <File_Type>MIR_SCLD1C</File_Type>
//    <Validity_Period>
//        <Validity_Start>UTC=2007-02-23T06:10:24</Validity_Start>
//        <Validity_Stop>UTC=2007-02-23T07:04:37</Validity_Stop>
//    </Validity_Period>
//    <File_Version>0001</File_Version>
//    <Source>
//        <System>DGPS</System>
//        <Creator>L1PP</Creator>
//        <Creator_Version>141</Creator_Version>
//        <Creation_Date>UTC=2008-02-13T21:39:12</Creation_Date>
//    </Source>

//</Fixed_Header>
@XStreamAlias("Fixed_Header")
public class EeFixedHeader {
    @XStreamAlias("File_Name")
    private String fileName;
    @XStreamAlias("File_Description")
    private String fileDescription;

    @XStreamAlias("Notes")
    private String notes;
    @XStreamAlias("Mission")
    private String mission;
    @XStreamAlias("File_Class")
    private String fileClass;
    @XStreamAlias("File_Type")
    private String fileType;
    @XStreamAlias("File_Version")
    private String fileVersion;

    @XStreamAlias("Validity_Period")
    private ValidityPeriod validityPeriod;

    @XStreamAlias("Source")
    private Source source;

    public String getFileName() {
        return fileName;
    }

    public String getFileDescription() {
        return fileDescription;
    }

    public String getNotes() {
        return notes;
    }

    public String getMission() {
        return mission;
    }

    public String getFileClass() {
        return fileClass;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFileVersion() {
        return fileVersion;
    }

    public ValidityPeriod getValidityPeriod() {
        return validityPeriod;
    }

    public Source getSource() {
        return source;
    }

    @XStreamAlias("Validity_Period")
    public static class ValidityPeriod {
        @XStreamAlias("Validity_Start")
        private String validityStart;
        @XStreamAlias("Validity_Stop")
        private String validityStop;
    }

    @XStreamAlias("Source")
    public static class Source {
        @XStreamAlias("System")
        private String system;
        @XStreamAlias("Creator")
        private String creator;
        @XStreamAlias("Creator_Version")
        private String creatorVersion;
        @XStreamAlias("Creation_Date")
        private String creationDate;

        public String getSystem() {
            return system;
        }

        public String getCreator() {
            return creator;
        }

        public String getCreatorVersion() {
            return creatorVersion;
        }

        public String getCreationDate() {
            return creationDate;
        }
    }
}