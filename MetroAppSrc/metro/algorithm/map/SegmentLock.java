package metro.algorithm.map;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Used for storing and synchronization of shared segments
 */
public class SegmentLock {
    Segment[] segments;

    /**
     * @param segments array of shared segments in the map
     */
    public SegmentLock(Segment[] segments) {
        this.segments = segments;
        initLocks();
    }

    /**
     * Every train has its own copy of a shared segment. Therefore, we have to make sure
     * that every copy of the same segment uses the same lock.
     */
    private void initLocks() {
        for (int i = 0; i < segments.length; i++) {
            ReentrantLock segLock = segments[i].getLock();
            for (int j = i + 1; j < segments.length; j++) {
                if ((segments[i].getStart().equals(segments[j].getEnd()) && segments[i].getEnd().equals(segments[j].getStart()))
                        || (segments[i].getStart().equals(segments[j].getStart()) && segments[i].getEnd().equals(segments[j].getEnd()))) {
                    segments[j].setLock(segLock);
                }
            }
        }
    }

    /**
     * Locks every train segment starting with start that the train is entering.
     * If moveForward is false, locks every segment with s.end == start
     *
     * @param train       enum defining current train
     * @param start       next crossing the train will going through
     * @param moveForward boolean value specifying the direction the train is heading
     */
    public void lockTrainSegments(FieldTypes train, Coordinates start, boolean moveForward) {
        for (Segment s : segments) {
            if (moveForward) {
                if (s.isTrainCrossing(train) && s.getStart().equals(start)) {
                    if (!s.getLock().isHeldByCurrentThread())
                        s.lockSegment();
                }
            } else {
                if (s.isTrainCrossing(train) && s.getEnd().equals(start)) {
                    if (!s.getLock().isHeldByCurrentThread())
                        s.lockSegment();
                }
            }
        }
    }


    /**
     * Unlocks every train segment ending with end that the train is entering.
     * If moveForward is false, locks every segment with s.start == end
     *
     * @param train       enum defining current train
     * @param end         previous crossing the train just left
     * @param moveForward boolean value specifying the direction the train is heading
     */
    public void unlockTrainSegments(FieldTypes train, Coordinates end, boolean moveForward) {
        for (Segment s : segments) {
            if (moveForward) {
                if (s.isTrainCrossing(train) && s.getEnd().equals(end)) {
                    s.unlockSegment();
                }
            } else {
                if (s.isTrainCrossing(train) && s.getStart().equals(end)) {
                    s.unlockSegment();
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
