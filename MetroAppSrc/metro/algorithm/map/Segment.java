package metro.algorithm.map;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a shared map segment, specified  by it's start and end
 */
class Segment {
    private final Coordinates start;
    private final Coordinates end;
    private Lock lock = new ReentrantLock();
    private FieldTypes trainCrossing;

    public Segment(Coordinates start, Coordinates end, FieldTypes trainCrossing) {
        this.start = start;
        this.end = end;
        this.trainCrossing = trainCrossing;
    }

    public void lockSegment() {
        lock.lock();
    }


    public void unlockSegment() {
        lock.unlock();
    }

    public void setTrain(FieldTypes train) {
        trainCrossing = train;
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

    public Lock getLock() {
        return lock;
    }

    public void setLock(Lock lock) {
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
