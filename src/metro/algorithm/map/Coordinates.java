package metro.algorithm.map;

import java.util.Objects;

/**
 * Used to define (x, y) coordinates of a point on the tunnel's map
 */
public class Coordinates {
    private int x, y;

    /**
     * @param x row value of this point
     * @param y column value of this point
     */
    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Can be used to create a copy of a Coordinates object.
     *
     * @param coordinates another Coordinates point that is copied to this object
     */
    public Coordinates(Coordinates coordinates) {
        x = coordinates.getX();
        y = coordinates.getY();
    }

    /**
     * Returns the x coordinate (row) of this point
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y coordinate (column) of this point
     */
    public int getY() {
        return y;
    }

    /**
     * Moves this point to the given position.
     *
     * @param newCoordinate position to move to.
     */
    public void moveTo(Coordinates newCoordinate) {
        x += newCoordinate.getX() - x;
        y += newCoordinate.getY() - y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return x == that.getX() &&
                y == that.getY();
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
