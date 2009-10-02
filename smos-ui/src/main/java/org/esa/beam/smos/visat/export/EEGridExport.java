package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.*;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosFormats;
import org.esa.beam.util.io.FileUtils;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.io.IOException;

public class EEGridExport implements GridPointFilterStream {

    private final File outputDir;
    private static final int ONE_KB = 1024;
    private File inHdr;
    private SequenceData gridPointListOut;
    private int counter;
    private long offSet;
    private int size;
    private DataContext ouputContext;

    public EEGridExport(File outputDir) {
        this.outputDir = outputDir;
    }

    @Override
    public void startFile(SmosFile smosfile) throws IOException {
        final String dblName = smosfile.getFile().getName();
        inHdr = FileUtils.exchangeExtension(smosfile.getFile(), ".HDR");

        // @todo 1 tb/tb adapt file name according to file specs and subset ....
        final File outDbl = new File(outputDir, dblName);

        // @todo 2 tb/tb check if successful - handle if not
        outDbl.createNewFile();

        final SequenceData inData = smosfile.getDataBlock().getSequence(SmosFormats.GRID_POINT_LIST_NAME);
        final SequenceType sequenceType = inData.getSequenceType();
        sequenceType.getElementType();

        final DataFormat dataFormat = smosfile.getFormat();
        System.out.println("dataFormat.getType() = " + dataFormat.getType());
        ouputContext = dataFormat.createContext(outDbl, "rw");
//        final CompoundData data = ouputContext.getData();
//        data.setInt(0, -1);
//        gridPointListOut = data.getSequence(SmosFormats.GRID_POINT_LIST_NAME);
//        System.out.println("size = " + gridPointListOut.getSize());

        counter = 0;
        offSet = 4;
    }

    @Override
    public void stopFile(SmosFile smosfile) throws IOException {
        // @todo 1 tb/tb patch relevant fields
        final File outHdr = new File(outputDir, inHdr.getName());
        copy(inHdr, outHdr);
        final CompoundData data = ouputContext.createData(
                TypeBuilder.COMPOUND("foo", TypeBuilder.MEMBER("int", SimpleType.INT)), 0L);
        data.setInt(0, counter);
        data.flush();

    }

    @Override
    public void handleGridPoint(int id, CompoundData gridPointDataIn) throws IOException {
//        final String s = gridPointDataIn.getCompoundType().getName();
//        if (counter == 0) {
//            final DataPrinter dataPrinter = new DataPrinter();
//            dataPrinter.print(gridPointDataIn);
//        }
        final CompoundType compoundType = gridPointDataIn.getCompoundType();
        final CompoundData data = ouputContext.createData(compoundType, offSet);
        size = 0;
        copyCompound(gridPointDataIn, data);
        data.flush();
        final long gridPointSize = gridPointDataIn.getSize();
        System.out.println("gridPointSize = " + size);
        offSet += size;
//      System.out.println("size = " + gridPointListOut.getSize());
        counter++;
        System.out.println("counter = " + counter);
    }

    private void copyCompound(CompoundData in, CompoundData out) throws IOException {
        CompoundType inCompoundType = in.getCompoundType();
        CompoundType outCompoundType = out.getCompoundType();
        if (!inCompoundType.equals(outCompoundType)) {
            throw new RuntimeException("boo");
        }
        final int memberCount = inCompoundType.getMemberCount();
        for (int i = 0; i < memberCount; i++) {
            CompoundMember member = inCompoundType.getMember(i);
            Type type = member.getType();
            if (type.isCompoundType()) {
                copyCompound(in.getCompound(i), out.getCompound(i));
            } else if (type.isSequenceType()) {
                copySequence(in.getSequence(i), out.getSequence(i));
            } else if (type.isSimpleType()) {
                size += type.getSize();
                out.setDouble(i, in.getDouble(i));
            }
        }
    }

    private void copySequence(SequenceData in, SequenceData out) throws IOException {
        SequenceType inSequenceType = in.getSequenceType();
        SequenceType outSequenceType = out.getSequenceType();
//        if (!inSequenceType.equals(outSequenceType)) {
//            System.out.println("inSequenceType = " + inSequenceType);
//            System.out.println("outSequenceType = " + outSequenceType);
//            throw new RuntimeException("boo");
//        }
        Type type = inSequenceType.getElementType();
        final int elementCount = inSequenceType.getElementCount();
        for (int i = 0; i < elementCount; i++) {
            if (type.isCompoundType()) {
                copyCompound(in.getCompound(i), out.getCompound(i));
            } else if (type.isSequenceType()) {
                copySequence(in.getSequence(i), out.getSequence(i));
            } else if (type.isSimpleType()) {
                size += type.getSize();
                out.setDouble(i, in.getDouble(i));
            }
        }
    }


    @Override
    public void close() throws IOException {
        // todo - implement, if necessary (rq-20091002)
    }

    public static void copy(File source, File target) throws IOException {
        final byte[] buffer = new byte[ONE_KB * ONE_KB];
        int bytesRead;
        FileImageInputStream sourceStream = null;
        FileImageOutputStream targetStream = null;

        try {
            final File targetDir = target.getParentFile();
            if (!targetDir.isDirectory()) {
                if (!targetDir.mkdirs()) {
                    throw new IOException("failed to create target directory: " + targetDir.getAbsolutePath());
                }
            }
            target.createNewFile();

            sourceStream = new FileImageInputStream(source);
            targetStream = new FileImageOutputStream(target);

            while ((bytesRead = sourceStream.read(buffer)) >= 0) {
                targetStream.write(buffer, 0, bytesRead);
            }
        } finally {
            if (sourceStream != null) {
                sourceStream.close();
            }
            if (targetStream != null) {
                targetStream.flush();
                targetStream.close();
            }
        }
    }
}
