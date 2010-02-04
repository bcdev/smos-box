package org.esa.beam.dataio.smos;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable index of SMOS L1c Science snapshot IDs.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 * @since SMOS-Box 2.0
 */
public class SnapshotInfo {

    private final Map<Long, Integer> snapshotIndexMap;
    private final List<Long> snapshotIds;
    private final List<Long> snapshotIdsX;
    private final List<Long> snapshotIdsY;
    private final List<Long> snapshotIdsXY;
    private final Map<Long, Area> snapshotAreaMap;

    public SnapshotInfo(Map<Long, Integer> snapshotIndexMap,
                        Collection<Long> snapshotIds,
                        Collection<Long> snapshotIdsX,
                        Collection<Long> snapshotIdsY,
                        Collection<Long> snapshotIdsXY,
                        Map<Long, Rectangle2D> snapshotAreaMap) {
        this.snapshotIndexMap = Collections.unmodifiableMap(snapshotIndexMap);
        this.snapshotIds = Collections.unmodifiableList(new ArrayList<Long>(snapshotIds));
        this.snapshotIdsX = Collections.unmodifiableList(new ArrayList<Long>(snapshotIdsX));
        this.snapshotIdsY = Collections.unmodifiableList(new ArrayList<Long>(snapshotIdsY));
        this.snapshotIdsXY = Collections.unmodifiableList(new ArrayList<Long>(snapshotIdsXY));

        final HashMap<Long, Area> map = new HashMap<Long, Area>();
        for (Map.Entry<Long, Rectangle2D> entry : snapshotAreaMap.entrySet()) {
            map.put(entry.getKey(), new Area(entry.getValue()));
        }
        this.snapshotAreaMap = Collections.unmodifiableMap(map);
    }

    public int getSnapshotIndex(long snapshotId) {
        if (!snapshotIndexMap.containsKey(snapshotId)) {
            return -1;
        }
        return snapshotIndexMap.get(snapshotId);
    }

    public List<Long> getSnapshotIds() {
        return snapshotIds;
    }

    public List<Long> getSnapshotIdsX() {
        return snapshotIdsX;
    }

    public List<Long> getSnapshotIdsY() {
        return snapshotIdsY;
    }

    public List<Long> getSnapshotIdsXY() {
        return snapshotIdsXY;
    }

    public Area getArea(long snapshotId) {
        return snapshotAreaMap.get(snapshotId);
    }
}
