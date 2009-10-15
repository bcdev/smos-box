package org.esa.beam.smos.visat.export;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FileNamePatcher {
    private String newStartDate;
    private String oldStartDate;
    private String newStopDate;
    private String oldStopDate;
    private String prefix;
    private String suffix;
    private SimpleDateFormat dateFormat;

    public FileNamePatcher(String originalName) {
        prefix = originalName.substring(0, 19);
        oldStartDate = originalName.substring(19, 34);
        oldStopDate = originalName.substring(35, 50);
        suffix = originalName.substring(50, originalName.length());
        dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
    }

    public void setStartDate(Date startDate) {
        newStartDate = dateFormat.format(startDate);
    }

    public void setStopDate(Date stopDate) {
        newStopDate = dateFormat.format(stopDate);
    }

    public String getHdrFileName() {
        final StringBuffer buffer = getFileNameWithoutExtension();
        buffer.append(".HDR");
        return buffer.toString();
    }

    public String getDblFileName() {
        final StringBuffer buffer = getFileNameWithoutExtension();
        buffer.append(".DBL");
        return buffer.toString();
    }

    private StringBuffer getFileNameWithoutExtension() {
        final StringBuffer buffer = new StringBuffer(256);
        buffer.append(prefix);
        if (newStartDate != null) {
            buffer.append(newStartDate);
        } else {
            buffer.append(oldStartDate);
        }
        buffer.append("_");

        if (newStopDate != null) {
            buffer.append(newStopDate);
        } else {
            buffer.append(oldStopDate);
        }

        buffer.append(suffix);
        return buffer;
    }
}
