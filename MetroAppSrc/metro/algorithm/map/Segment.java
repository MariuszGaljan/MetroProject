package metro.algorithm.map;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a map segment that is shared by two or more trains, specified  by it's start and end.
 * Each train should have its own copy of a given segment, with different trainCrossing values.
 */
class Segment {
    private final Coordinates start;
    private final Coordinates end;
    private ReentrantLock lock = new ReentrantLock();
    /**
     * Specifies to which train this segment belongs
     */
    private final FieldTypes trainCrossing;

    /**
     * @param start         Coordinates of the starting point of this segment
     *                      from the perspective of the trainCrossing train
     * @param end           Coordinates of the ending point of this segment
     *                      from the perspective of the trainCrossing train
     * @param trainCrossing enum defining the train this segment belongs to
     */
    public Segment(Coordinates start, Coordinates end, FieldTypes trainCrossing) {
        this.start = start;
        this.end = end;
        this.trainCrossing = trainCrossing;
    }

    /**
     * Locks this segment.
     * To unlock use unlockSegment.
     */
    public void lockSegment() {
        lock.lock();
    }

    /**
     * Unlocks this segment.
     * To lock use lockSegment.
     */
    public void unlockSegment() {
        lock.unlock();
    }

    public boolean isTrainCrossing(FieldTypes train) {
        return trainCrossing == train;
    }

    public Coordinates getStart() {
        return start;
    }

    public Coordinates getEnd() {
        return end;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public void setLock(ReentrantLock lock) {
        this.lock = lock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Segment segment = (Segment) o;
        return (start == segment.getStart() && end == segment.getEnd());
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return "[" + trainCrossing + ": " + start + ", " + end + ", " + lock + "]";
    }
}
