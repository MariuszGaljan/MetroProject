package metro.algorithm.map;

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
    public static int width = 11;
    /**
     * Height of the map
     */
    public static int height = 17;

    /**
     * Used to shorten the enum values and make the map more
     * appealing to the human eye.
     */
    public static FieldTypes E = FieldTypes.EMPTY, W = FieldTypes.WALL,
            S = FieldTypes.STATION;

    /**
     * Array of coordinates of the stations on the map.
     */
    public static Coordinates[] stations;

    /**
     * The 2D array representing the tunnel's map.
     * <p>
     * Each tile is defined by one of the enum FieldTypes values.
     */
    public FieldTypes[][] map = {
            {E, E, E, E, E, E, E, E, E, E, E},
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
            {E, E, E, E, E, E, E, E, E, E, E},
    };
}
