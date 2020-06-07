package metro.algorithm.map;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;

/**
 * Used for synchronization of segments
 */
public class SegmentLock {
    Segment[] segments;

    public SegmentLock(Segment[] segments) {
        this.segments = segments;
        initLocks();
    }

    /**
     * Every segment is doubled for every train
     */
    private void initLocks() {
        for (int i = 0; i < segments.length; i++) {
            Lock segLock = segments[i].getLock();
            for (int j = i + 1; j < segments.length; j++) {
                if (segments[i].getStart().equals(segments[j].getEnd()) && segments[i].getEnd().equals(segments[j].getStart())) {
                    segments[j].setLock(segLock);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "SegmentLock{" +
                "segments=" + Arrays.toString(segments) +
                '}';
    }
}
