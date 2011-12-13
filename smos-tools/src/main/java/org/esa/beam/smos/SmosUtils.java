package org.esa.beam.smos;

import com.bc.util.file.ZipUtils;
import com.bc.util.string.StringUtils;
import org.esa.beam.util.io.FileUtils;
import org.esa.beam.smos.dto.SmosFile;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: 12.12.11
 * Time: 16:17
 * To change this template use File | Settings | File Templates.
 */
public class SmosUtils {


    private static final Logger logger = Logger.getLogger(SmosUtils.class.getName());

    public static File assertUnknownFileFolder(SmosConfig config, Logger logger) {
        return createDirectory(config, new Date(), "Unknown", logger);
    }

    public static File assertTargetFolder(SmosConfig config, Date date, String fileType, Logger logger) {
        return createDirectory(config, date, fileType, logger);
    }

    public static File findDblFile(File[] zipFiles) {
        File dblFile = null;
        if (zipFiles.length == 1) {
            dblFile = zipFiles[0];
        } else {
            for (File zipFile : zipFiles) {
                if (zipFile.getName().matches(".+\\.[dD][bB][lL]")) {
                    dblFile = zipFile;
                    break;
                }
            }
        }
        return dblFile;
    }

    /**
     * @param fileName The filename of the product, from which the start day will be read.
     * @return a Date with the year, month, and day field set from the filename, and the rest left default (today's time)
     */
    public static Date getSensingStartDayFromFilename(String fileName) {
        final String yearString = fileName.substring(19, 23);
        final String monthString = fileName.substring(23, 25);
        final String dayString = fileName.substring(25, 27);

        final int year = Integer.parseInt(yearString);
        final int month = Integer.parseInt(monthString);
        final int day = Integer.parseInt(dayString);

        // @todo 1 pm/* review this code... it doesn't set a time zone, so if some other code sets time zone to UTC, then the day can change. Better to just use a different class than Date for the return value.
        final Calendar cal = GregorianCalendar.getInstance();
        cal.set(year, month - 1, day);
        return cal.getTime();
    }

    /**
     * @param sensingTime The sensing time from the filename, which looks like ISO-8601 timespec=second with colons and hyphens removed.
     * @return a Date representing the whole sensing time
     */
    private static Date getSensingTime(String sensingTime) {
        DateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return df.parse(sensingTime);
        } catch (ParseException e) {
            //unexpected; assuming file name filtering was done before
            logger.log(Level.WARNING, "Exception while parsing sensing time from string: \"" + sensingTime + "\"", e);
            return null;
        }
    }

    public static Date getSensingStartTimeFromFilename(String fileName) {
        if (fileName.length() < 34)
            return null;
        return getSensingTime(fileName.substring(19, 34));
    }

    public static Date getSensingStopTimeFromFilename(String fileName) {
        if (fileName.length() < 50)
            return null;
        return getSensingTime(fileName.substring(35, 50));
    }

    /**
     * @param fileName the filename which may contain the path, and must contain the extension
     * @return the type string (which may not exactly equal a substring in the filename, depending on special conditions)
     */
    public static String getProductType(String fileName) {
        //format the name to remove path
        fileName = new File(fileName).getName();

        final String fileType;
        if (isAuxFileType(fileName)) {
            fileType = "AUX_DATA";
        } else if (isQualityControlType(fileName)) {
            // This value should never end up as a directory name. QualityFileHandler uses
            // getTargetForQCFiles(...) instead of getProductType(...) to find the directory name.
            fileType = "RQC_RQD";
        } else {
            fileType = getProductTypeFromFilename(fileName);
        }
        return fileType;
    }

    public static boolean isDblFileName(String fileName) {
        final String extension = FileUtils.getExtension(fileName);
        return ".dbl".equalsIgnoreCase(extension);
    }

    public static boolean isKnownFileType(String fileName) {
        return isL2Type(fileName) ||
                isL1aType(fileName) ||
                isL1bType(fileName) ||
                isL1cType(fileName) ||
                isL0Type(fileName) ||
                isAuxFileType(fileName) ||
                isQualityControlType(fileName) ||
                isMirasPlanType(fileName);
    }

    public static boolean isL0Type(String fileName) {
        return fileName.matches("SM_.{4}_MIR_SC_[DF]0__.{45}") ||
                fileName.matches("SM_.{4}_MIR_TAR[DF]0__.{45}") ||
                fileName.matches("SM_.{4}_MIR_UNC[DNU]0__.{45}") ||
                fileName.matches("SM_.{4}_MIR_COR[DNU]0__.{45}") ||
                fileName.matches("SM_.{4}_MIR_TEST0__.{45}") ||
                fileName.matches("SM_.{4}_TLM_MIRA0__.{45}");
    }

    public static boolean isL1aType(String fileName) {
        return fileName.matches("SM_.{4}_MIR_SC_[DF]1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_TAR[DF]1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_AFW[DU]1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_ANIR1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_UNC[DNU]1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_CORN1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_CRS[DU]1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_UAV[DU]1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_NIR_1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_FWAS1[Aa]_.{45}") ||
                fileName.matches("SM_.{4}_TLM_MIRA1[Aa]_.{45}");
    }

    public static boolean isL1bType(String fileName) {
        return fileName.matches("SM_.{4}_MIR_SC_[DF]1[Bb]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_TAR[DF]1[Bb]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_FTT[DF]___.{45}") ||
                fileName.matches("SM_.{4}_MIR_[GJ]MAT[DU]__.{45}");
    }

    public static boolean isL1cType(String fileName) {
        return fileName.matches("SM_.{4}_MIR_SC[LS][DF]1[Cc]_.{45}") ||
                fileName.matches("SM_.{4}_MIR_BW[LS][DF]1[Cc]_.{45}");
    }

    public static boolean isL2Type(String fileName) {
        return fileName.matches("SO_.{4}_MIR_TSM_2__.{45}") ||
                fileName.matches("SO_.{4}_MIR_TOS_2__.{45}") ||
                fileName.matches("SO_.{4}_MIR_SM__2__.{45}") ||
                fileName.matches("SM_.{4}_MIR_(SM|OS)UDP2_.{45}") ||
                fileName.matches("SM_.{4}_MIR_(SM|OS)DAP2_.{45}") ||
                fileName.matches("SO_.{4}_MIR_OS__2__.{45}");
    }

    public static boolean isAuxFileType(String fileName) {
        return fileName.matches("SM_.{4}_AUX_APOD01_.{44}") ||
                fileName.matches("SM_.{4}_AUX_AGDPT__.{45}") ||
                fileName.matches("SM_.{4}_AUX_APD[LS](00|__)_.{45}") ||
                fileName.matches("SM_.{4}_AUX_ATMOS__.{45}") ||
                fileName.matches("SM_.{4}_AUX_BFP____.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_BNDLST_.{45}") ||
                fileName.matches("SM_.{4}_AUX_BSCAT__.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_BWGHT__.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_CNF(L0P|L1P|SMD|SMF|OSD|OSF)_.{45}") ||
                fileName.matches("SM_.{4}_AUX_DFFFRA_.{45}") ||
                fileName.matches("SM_.{4}_AUX_D(FF|GG)XYZ_.{45}") ||
                fileName.matches("SM_.{4}_AUX_DFFL(AI|MX)_.{45}") ||
                fileName.matches("SM_.{4}_AUX_DGG____.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_DGGT(LV|FO)_.{45}") ||
                fileName.matches("SM_.{4}_AUX_DGGR(OU|FI)_.{45}") ||
                fileName.matches("SM_.{4}_AUX_DGGFLO_.{45}") ||
                fileName.matches("SM_.{4}_AUX_DISTAN_.{45}") ||
                fileName.matches("SM_.{4}_AUX_ECOLAI_.{45}") ||
                fileName.matches("SM_.{4}_AUX_FAIL___.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_FLATT__.{44}") ||
                fileName.matches("SM_.{4}_AUX_FLTSEA_.{45}") ||
                fileName.matches("SM_.{4}_AUX_FOAM___.{45}") ||
                fileName.matches("SM_.{4}_AUX_GALAXY_.{45}") ||
                fileName.matches("SM_.{4}_AUX_GALNIR_.{45}") ||
                fileName.matches("SM_.{4}_AUX_GAL(_|2)(SM|OS)_.{45}") ||
                fileName.matches("SM_.{4}_AUX_GLXY___.{45}") ||
                fileName.matches("SM_.{4}_AUX_IGRF___.{45}") ||
                fileName.matches("SM_.{4}_AUX_LCF____.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_MASK___.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_MISP___.{45}") ||
                fileName.matches("SM_.{4}_AUX_LANDCL_.{45}") ||
                fileName.matches("SM_.{4}_AUX_LSMASK_.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_MN_WEF_.{45}") ||
                fileName.matches("SM_.{4}_AUX_MOONT__.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_NIR____.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_ORBPRE_.{45}") ||
                fileName.matches("SM_.{4}_AUX_ORBRES_.{45}") ||
                fileName.matches("SM_.{4}_AUX_PATT[0-9_][0-9_]_.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_PLM____.{45,46}") ||
                fileName.matches("SM_.{4}_AUX_PMS____.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_RFI____.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_RGHNS[1-3]_.{45}") ||
                fileName.matches("SM_.{4}_AUX_SOIL_P_.{45}") ||
                fileName.matches("SM_.{4}_AUX_SPAR___.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_SSS____.{45}") ||
                fileName.matches("SM_.{4}_AUX_VTEC___.{45}") ||
                fileName.matches("SM_.{4}_AUX_SUNT___.{44,45}") ||
                fileName.matches("SM_.{4}_AUX_SGLINT_.{45}") ||
                fileName.matches("SM_.{4}_AUX_TIME___.{45}") ||
                fileName.matches("SM_.{4}_AUX_VTEC_[CPR]_.{45}") ||
                fileName.matches("SM_.{4}_AUX_WEF____.{45}") ||
                fileName.matches("SM_.{4}_AUX_CNFFAR_.{45}") ||
                fileName.matches("SM_.{4}_AUX_BULL_B_.{45}") ||
                fileName.matches("SM_.{4}_AUX_OTT(1|2|3)(D|F)__.{45}") ||
                fileName.matches("SM_.{4}_AUX_ECMWF__.{45}");
    }

    public static boolean isQualityControlType(String fileName) {
        return fileName.matches("SM_.{4}_RQ(C|D)_.{49}(EEF|eef)");
    }

    public static boolean isMirasPlanType(String fileName) {
        return fileName.matches("SM_.{4}_MPL_ORBSCT_.{44,45}") ||
                fileName.matches("SM_.{4}_MPL_XBDOWN_.{44,45}") ||
                fileName.matches("SM_.{4}_MPL_APIDPL_.{44,45}") ||
                fileName.matches("SM_.{4}_MPL_HLPLAN_.{44,45}") ||
                fileName.matches("SM_.{4}_MPL_XBDPRE_.{44,45}") ||
                fileName.matches("SM_.{4}_MPL_PROTEV_.{44,45}");
    }

    public static boolean isAuxECMWFType(String fileName) {
        return fileName.indexOf("_AUX_ECMWF_") > 0;
    }

    /**
     * Validates pathToDirectory and throws exceptions if the parameter is blank or the directory does not exist
     *
     * @param pathToDirectory the path to the directory
     * @param directoryName   friendly name, only used in exception messages
     * @return the File reprensenting the path to the directory
     */
    public static File getDirectorySave(String pathToDirectory, String directoryName) {
        if (StringUtils.isEmpty(pathToDirectory)) {
            throw new IllegalArgumentException(directoryName + " not set.");
        }
        final File result = new File(pathToDirectory);
        if (!result.isDirectory()) {
            throw new IllegalArgumentException(directoryName + " does not exist: " + pathToDirectory);
        }
        return result;
    }

    // @todo 3 tb/tb add test for this
    public static File[] removeDirectories(File[] inputFiles) {
        final ArrayList<File> resultList = new ArrayList<File>();

        for (int i = 0; i < inputFiles.length; i++) {
            final File currentFile = inputFiles[i];
            if (currentFile.isDirectory()) {
                Logger.getLogger("com.bc.calval.smos").warning("Directory found - will NOT be handled: " + currentFile);
                continue;
            }

            resultList.add(currentFile);
        }

        return resultList.toArray(new File[resultList.size()]);
    }

    public static SmosFile[] getSmosFiles(File[] files) {
        final HashMap<String, SmosFile> resultMap = new HashMap<String, SmosFile>();

        for (int i = 0; i < files.length; i++) {
            final File currentFile = files[i];
            String currentFileName = currentFile.getName();
            String currentFilePath = currentFile.getAbsolutePath();

            final String key = FileUtils.getFilenameWithoutExtension(currentFileName);
            if (resultMap.containsKey(key)) {
                addFileToEntry(currentFilePath, resultMap.get(key));
            } else {
                final SmosFile newEntry = new SmosFile();
                if (ZipUtils.isCompressedFileName(currentFileName)) {
                    newEntry.setPath_DBL(currentFilePath);
                } else {
                    addFileToEntry(currentFilePath, newEntry);
                }
                newEntry.setName(key);
                resultMap.put(key, newEntry);
            }
        }

        final Iterator<Map.Entry<String, SmosFile>> resultIterator = resultMap.entrySet().iterator();
        final SmosFile[] resultArray = new SmosFile[resultMap.size()];
        int i = 0;
        while (resultIterator.hasNext()) {
            resultArray[i] = resultIterator.next().getValue();
            ++i;
        }
        return resultArray;
    }

    public static String calculateFileHash(File inputFile) throws IOException, NoSuchAlgorithmException {
        com.bc.util.encoder.MD5Encoder md5Encoder = new com.bc.util.encoder.MD5Encoder();
        return md5Encoder.encode(inputFile);
    }

    public static String getTargetForQCFiles(String fileName) {
        final String typeString = getProductTypeFromFilename(fileName);
        if (typeString.indexOf("SC_D0_") >= 0 ||
                typeString.indexOf("SC_F0_") >= 0 ||
                typeString.indexOf("TARD0_") >= 0 ||
                typeString.indexOf("TARF0_") >= 0 ||
                typeString.indexOf("UNCD0_") >= 0 ||
                typeString.indexOf("UNCN0_") >= 0 ||
                typeString.indexOf("UNCU0_") >= 0 ||
                typeString.indexOf("CORD0_") >= 0 ||
                typeString.indexOf("CORN0_") >= 0 ||
                typeString.indexOf("CORU0_") >= 0 ||
                typeString.indexOf("TEST0_") >= 0 ||
                typeString.indexOf("CRSU1A") >= 0 ||
                typeString.indexOf("CRSD1A") >= 0 ||
                typeString.indexOf("AFWU1A") >= 0 ||
                typeString.indexOf("AFWD1A") >= 0 ||
                typeString.indexOf("ANIR1A") >= 0 ||
                typeString.indexOf("UAVU1A") >= 0 ||
                typeString.indexOf("UAVD1A") >= 0 ||
                typeString.indexOf("GMATU_") >= 0 ||
                typeString.indexOf("GMATD_") >= 0 ||
                typeString.indexOf("JMATU_") >= 0 ||
                typeString.indexOf("JMATD_") >= 0 ||
                typeString.indexOf("TARD1A") >= 0 ||
                typeString.indexOf("TARF1A") >= 0 ||
                typeString.indexOf("SC_D1A") >= 0 ||
                typeString.indexOf("SC_F1A") >= 0 ||
                typeString.indexOf("TARD1B") >= 0 ||
                typeString.indexOf("TARF1B") >= 0 ||
                typeString.indexOf("SC_D1B") >= 0 ||
                typeString.indexOf("SC_F1B") >= 0 ||
                typeString.indexOf("FTTD__") >= 0 ||
                typeString.indexOf("FTTF__") >= 0 ||
                typeString.indexOf("SCSD1C") >= 0 ||
                typeString.indexOf("SCSF1C") >= 0 ||
                typeString.indexOf("SCLD1C") >= 0 ||
                typeString.indexOf("SCLF1C") >= 0 ||
                typeString.indexOf("BWND1C") >= 0 ||
                typeString.indexOf("BWSD1C") >= 0 ||
                typeString.indexOf("BWLD1C") >= 0 ||
                typeString.indexOf("BWSF1C") >= 0 ||
                typeString.indexOf("BWLF1C") >= 0 ||
                typeString.indexOf("BWNF1C") >= 0 ||
                typeString.indexOf("SMUDP2") >= 0 ||
                typeString.indexOf("SMDAP2") >= 0 ||
                typeString.indexOf("OSUDP2") >= 0 ||
                typeString.indexOf("OSDAP2") >= 0) {
            return "MIR".concat(typeString.substring(3, typeString.length()));
        } else if (typeString.indexOf("MIRA0_") >= 0 ||
                typeString.indexOf("MIRA1A") >= 0) {
            return "TLM".concat(typeString.substring(3, typeString.length()));
        }
        return "AUX_DATA";
    }

    public static boolean hasGeolocation(String fileName) {
        final String upperCase = fileName.toUpperCase();
        return upperCase.indexOf("MIR_BWND1C") > 0 ||
                upperCase.indexOf("MIR_BWSD1C") > 0 ||
                upperCase.indexOf("MIR_BWLF1C") > 0 ||
                upperCase.indexOf("MIR_BWNF1C") > 0 ||
                upperCase.indexOf("MIR_BWSF1C") > 0 ||
                upperCase.indexOf("MIR_SCLD1C") > 0 ||
                upperCase.indexOf("MIR_SCLF1C") > 0 ||
                upperCase.indexOf("MIR_SCSD1C") > 0 ||
                upperCase.indexOf("MIR_SCSF1C") > 0 ||
                upperCase.indexOf("MIR_OSUDP2") > 0 ||
                upperCase.indexOf("MIR_OSDAP2") > 0 ||
                upperCase.indexOf("MIR_SMUDP2") > 0 ||
                upperCase.indexOf("MIR_SMDAP2") > 0 ||
                upperCase.indexOf("MIR_BWLD1C") > 0 ||
                upperCase.indexOf("AUX_ECMWF") > 0;
    }

    public static String createChildProductFileName(String smosFileName, String siteId) {

        String newFileName = patchFileClass(smosFileName);
        int siteIndex = 999;
        try {
            siteIndex = Integer.parseInt(siteId);
        } catch (NumberFormatException e) {
            // could not parse site id, should not happen
        }
        newFileName = patchFileIndex(siteIndex, newFileName);
        return newFileName;
    }

    ////////////////////////////////////////////////////////////////////////////////
    /////// END OF PUBLIC
    ////////////////////////////////////////////////////////////////////////////////

    static String getProductTypeFromFilename(String fileName) {
        //in case fileName includes path, remove it
        fileName = new File(fileName).getName();

        final String typeString = fileName.substring(8, 18);
        return typeString.toUpperCase();
    }


    static boolean isBrowseType(String fileName) {
        return fileName.matches("SM_.{4}_MIR_BW.{4}_.{45}");
    }

    static String patchFileIndex(int newIndex, String fileName) {
        final int dotIndex = fileName.lastIndexOf(".");
        final String newIndexString = decFormat_threeDigits.format(newIndex); //this line might not be thread safe because it uses a static decimalformat
        final StringBuffer result = new StringBuffer(64);
        result.append(fileName.substring(0, dotIndex - 5));
        result.append(newIndexString);
        result.append(fileName.substring(dotIndex - 2, fileName.length()));
        return result.toString();
    }

    static String patchFileClass(String fileName) {
        final StringBuffer result = new StringBuffer(64);
        result.append(fileName.substring(0, 6));
        result.append('B');
        result.append(fileName.substring(7, fileName.length()));
        return result.toString();
    }


    public static File renameFile(File original, String newName) throws IOException {
        final String path = original.getParent();
        final File newFile = new File(path, newName);
        if (!original.renameTo(newFile)) {
            throw new IOException("failed to rename file: " + original.getAbsolutePath());
        }

        return newFile;
    }

    ////////////////////////////////////////////////////////////////////////////////
    /////// END OF PACKAGE
    ////////////////////////////////////////////////////////////////////////////////

    private static final DecimalFormat decFormat = new DecimalFormat("00");
    private static final DecimalFormat decFormat_threeDigits = new DecimalFormat("000");

    private static File createDirectory(SmosConfig config, Date date, String directoryName, Logger logger) {
        final Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH) + 1;
        final int day = cal.get(Calendar.DAY_OF_MONTH);
        final File outputDir = getDirectorySave(config.getTargetDirectory(), "Target directory");
        final File outDir = new File(outputDir, Integer.toString(year)
                + File.separatorChar +
                decFormat.format(month) + File.separatorChar +
                decFormat.format(day) + File.separatorChar +
                directoryName);
        if (!outDir.isDirectory()) {
            if (!outDir.mkdirs()) {
                logger.severe("Failed to create directory: " + outDir.getAbsolutePath());
            } else {
                logger.fine("Created directory: " + outDir.getAbsolutePath());
            }
        }
        return outDir;
    }

    private static void addFileToEntry(String currentFilePath, SmosFile smosFile) {
        final String extension = FileUtils.getExtension(currentFilePath);
        if (".dbl".equalsIgnoreCase(extension) || ".eef".equalsIgnoreCase(extension)) {
            smosFile.setPath_DBL(currentFilePath);
        } else if (".hdr".equalsIgnoreCase(extension)) {
            smosFile.setPath_HDR(currentFilePath);
        } else {
            smosFile.setPath_DBL(currentFilePath);
        }
    }


}
