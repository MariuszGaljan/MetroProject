package metro.algorithm.map;

import java.util.LinkedList;
import java.util.List;

/**
 * Class holding the 2D array of the tunnel's map.
 * <p>
 * It doesn't have any methods as it was created
 * only to separate the 2D map array from the monitor (for clarity)
 * <p>
 * You can access the map via the public map variable.
 */
class TunnelsMap {
    /**
     * Width of the map
     */
    public static final int WIDTH = 11;
    /**
     * Height of the map
     */
    public static final int HEIGHT = 17;

    /**
     * Used to shorten the enum values and make the map more
     * appealing to the human eye.
     */
    public static FieldTypes E = FieldTypes.EMPTY, W = FieldTypes.WALL,
            S = FieldTypes.STATION;

    /**
     * Array of coordinates of stations on the map
     */
    public Coordinates[] stations = {
            new Coordinates(0, 0),
            new Coordinates(0, 10),
            new Coordinates(16, 0),
            new Coordinates(16, 10)
    };

    /**
     * The 2D array representing the tunnel's map.
     * <p>
     * Each tile is defined by one of the enum FieldTypes values.
     */
    public FieldTypes[][] map = {
            {S, E, E, E, E, E, E, E, E, E, S},
            {E, W, W, W, W, E, W, W, W, W, E},
            {E, W, W, W, W, E, W, W, W, W, E},
            {E, W, W, W, W, E, W, W, W, W, E},
            {E, W, W, W, W, E, W, W, W, W, E},
            {E, W, W, W, W, E, W, W, W, W, E},
            {E, W, W, W, W, E, W, W, W, W, E},
            {E, W, W, W, W, E, W, W, W, W, E},
            {E, E, E, E, E, E, E, E, E, E, E},
            {E, W, W, W, W, E, W, W, W, W, E},
            {E, W, W, W, W, E, W, W, W, W, E},
            {E, W, W, W, W, E, W, W, W, W, E},
            {E, W, W, W, W, E, W, W, W, W, E},
            {E, W, W, W, W, E, W, W, W, W, E},
            {E, W, W, W, W, E, W, W, W, W, E},
            {E, W, W, W, W, E, W, W, W, W, E},
            {S, E, E, E, E, E, E, E, E, E, S},
    };

    /**
     * Creates an array of tiles marked as empty adjacent to the all stations
     *
     * @return coordinates of tiles adjacent to all stations
     */
    public List<Coordinates> getStationsEntrances() {
        List<Coordinates> stationsEntraces = new LinkedList<>();

        for (Coordinates station : stations)
            stationsEntraces.addAll(getStationEntrances(station));

        return stationsEntraces;
    }

    /**
     * Creates an array of tiles marked as empty adjacent to the given station (station's entrance)
     *
     * @param station coordinates of a station.
     * @return coordinates of tiles adjacent to one station
     */
    private List<Coordinates> getStationEntrances(Coordinates station) {
        List<Coordinates> stationEntrances = new LinkedList<>();
        int[] possibleVectors = {-1, 0, 1};
        int row, col;

        for (int vectorRow : possibleVectors) {
            for (int vectorCol : possibleVectors) {
                if (vectorRow != 0 || vectorCol != 0) {
                    row = station.getRow() + vectorRow;
                    col = station.getCol() + vectorCol;

                    if (row >= 0 && row < TunnelsMap.HEIGHT)
                        if (col >= 0 && col < TunnelsMap.WIDTH)
                            if (map[row][col] != FieldTypes.WALL) {
                                stationEntrances.add(new Coordinates(row, col));
                            }
                }
            }
        }
        return stationEntrances;
    }
}
