package metro.algorithm.map;

import java.util.Objects;

/**
 * Used to define (row, column) coordinates of a point on the tunnel's map
 */
public class Coordinates {
    private int row, col;

    /**
     * @param row row value of this point
     * @param col column value of this point
     */
    public Coordinates(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Can be used to create a copy of a Coordinates object.
     *
     * @param coordinates another Coordinates point that is copied to this object
     */
    public Coordinates(Coordinates coordinates) {
        row = coordinates.getRow();
        col = coordinates.getCol();
    }

    /**
     * Returns the row coordinate of this point
     *
     * @return value of the row index
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the column coordinate of this point
     *
     * @return value of the column index
     */
    public int getCol() {
        return col;
    }

    /**
     * Moves this point to the given position.
     *
     * @param newCoordinate position to move to.
     */
    public void moveTo(Coordinates newCoordinate) {
        row += newCoordinate.getRow() - row;
        col += newCoordinate.getCol() - col;
    }

    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return row == that.getRow() &&
                col == that.getCol();
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
