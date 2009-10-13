package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.*;

import java.awt.geom.Area;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

public class SmosDAFile extends SmosFile {

    private SequenceData swathAnalysisList;

    public SmosDAFile(File file, DataFormat format) throws IOException {
        dataContext = format.createContext(file, "r");
        dataBlock = dataContext.getData();

        gridPointList = dataBlock.getSequence(SmosFormats.DA_GRID_POINT_LIST_NAME);        
        if (gridPointList == null) {
            throw new IllegalStateException(MessageFormat.format("SMOS File ''{0}'': Missing grid point list.", file.getPath()));
        }

        gridPointType = (CompoundType) gridPointList.getType().getElementType();
        gridPointIdIndex = gridPointType.getMemberIndex(SmosFormats.GRID_POINT_ID_NAME);
        gridPointIndexes = createGridPointIndexes();
    }

    public Area getRegion() {
        return null;
    }

    public int getGridPointIndex(int seqnum) {
        return 0;
    }

    public CompoundType getGridPointType() {
        return null;
    }

    public CompoundData getGridPointData(int gridPointIndex) throws IOException {
        return null;
    }
}
