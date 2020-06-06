package metro.algorithm.map;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Crossing {
    Coordinates coordinates;
    Lock lock = new ReentrantLock();

    public Crossing(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public void lockCrossing() {
        lock.lock();
    }

    public void unlockCrossing() {
        lock.unlock();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Crossing crossing = (Crossing) o;
        return Objects.equals(coordinates, crossing.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinates);
    }

    @Override
    public String toString() {
        return coordinates.toString();
    }
}
