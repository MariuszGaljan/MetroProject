package metro.algorithm;

import metro.algorithm.map.Coordinates;
import metro.algorithm.map.FieldTypes;
import metro.algorithm.map.TunnelsMapMonitor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Class used to specify parameters of the metro simulation:
 * - trains' wagons
 * - trains' routes
 * - initial order
 * - stations
 *
 * @see Coordinates
 * @see FieldTypes
 */
public class ModelParameters {
    public static final int NUMBER_OF_TRAINS = 3;
    /**
     * Number of wagons in each train
     */
    public static final int TRAIN_LENGTH = 3;

    /**
     * A constant variable specifying the position of the crossing on the tunnel's map
     */
    private static final Coordinates middle
            = new Coordinates(TunnelsMapMonitor.getHeight() / 2, TunnelsMapMonitor.getWidth() / 2);

    /**
     * Array of trains.
     * Each train is an array of coordinates of its wagons.
     */
    public Coordinates[][] trains = new Coordinates[NUMBER_OF_TRAINS][];

    /**
     * Specifies the routes of trains
     */
    public Coordinates[][] crossings = new Coordinates[NUMBER_OF_TRAINS][];

    /**
     * Specifies start and end for each train's route
     */
    private Coordinates[][] initialPoints = {
            {new Coordinates(0, 1), new Coordinates(16, 9)},
            {new Coordinates(16, 1), new Coordinates(1, 0)},
            {new Coordinates(15, 10), new Coordinates(0, 1)}
    };

    /**
     * Initializes routes to the default values.
     */
    public ModelParameters() {
        for (int i = 0; i < trains.length; i++) {
            trains[i] = generateTrain(initialPoints[i][0]);
            crossings[i] = generateCrossings(initialPoints[i][0], initialPoints[i][1]);
        }
    }

    /**
     * @param routes an array of Coordinates pairs, specifying each route's start and end
     *               e.g. For train 1 route routes[0][0] = start, route[0][1] = end
     *               For train 2 route routes[1][0] = start, route[1][1] = end
     */
    public ModelParameters(Coordinates[][] routes) {
        initialPoints = routes;
        for (int i = 0; i < trains.length; i++) {
            trains[i] = generateTrain(initialPoints[i][0]);
            crossings[i] = generateCrossings(initialPoints[i][0], initialPoints[i][1]);
        }
    }


    /**
     * Generates array of coordinates specifying the route from start to the end via crossings on the tunnel's map
     *
     * @param start coordinates of the starting point
     * @param end   coordinates of the ending point
     * @return array of coordinates of crossings the route goes through
     */
    private Coordinates[] generateCrossings(Coordinates start, Coordinates end) {
        List<Coordinates> route = new LinkedList<>();

        // first, we have to check if start and end are station entrances opposite to each other in the same line
        // because then we don't have to go into the passage
        if (start.getRow() == end.getRow() && Math.abs(end.getCol() - start.getCol()) == TunnelsMapMonitor.getWidth() - 3) {
            route.add(start);
            route.add(new Coordinates(start.getRow(), TunnelsMapMonitor.getWidth() / 2));
            route.add(end);
            return route.toArray(new Coordinates[0]);
        }
        if (start.getCol() == end.getCol() && Math.abs(end.getRow() - start.getRow()) == TunnelsMapMonitor.getHeight() - 3) {
            route.add(start);
            route.add(new Coordinates(TunnelsMapMonitor.getHeight() / 2, start.getCol()));
            route.add(end);
            return route.toArray(new Coordinates[0]);
        }

        // if not, we create a route through the passage by connecting two routes:
        //  - from start the middle
        //  - a reversed route from end to the middle
        route = getCrossingsToMiddle(start);
        route.add(middle);

        List<Coordinates> routeToMiddleFromEnd = getCrossingsToMiddle(end);
        Collections.reverse(routeToMiddleFromEnd);
        route.addAll(routeToMiddleFromEnd);

        return route.toArray(new Coordinates[0]);
    }


    /**
     * Generates list of crossings to pass through when going from start to the middle of the map,
     * middle exclusive
     *
     * @param start coordinates of the starting point
     * @return list of crossing the train passes from the starting point to the middle
     * in range [start, middle)
     */
    private List<Coordinates> getCrossingsToMiddle(Coordinates start) {
        List<Coordinates> route = new LinkedList<>();
        Coordinates actEnd;

        // to get to the middle every train has to pass one crossing in between two stations
        if (start.getCol() == 1 || start.getCol() == TunnelsMapMonitor.getWidth() - 2) {
            actEnd = new Coordinates(start.getRow(), TunnelsMapMonitor.getWidth() / 2);
        } else {
            actEnd = new Coordinates(TunnelsMapMonitor.getHeight() / 2, start.getCol());
        }
        route.add(start);
        route.add(actEnd);

        return route;
    }


    /**
     * Appends a line represented by a list of coordinates to the route.
     * The line includes points in range [start, end).
     * A line can be horizontal or vertical.
     *
     * @param route      list of coordinates the line is supposed to be added to
     * @param start      coordinates of the starting point
     * @param end        coordinates of the end point
     * @param horizontal defines whether the line is horizontal or vertical
     */
    private void addLine(List<Coordinates> route, Coordinates start, Coordinates end, boolean horizontal) {
        if (horizontal) {
            if (start.getCol() < end.getCol()) {
                // left to right
                for (int i = start.getCol(); i < end.getCol(); i++)
                    route.add(new Coordinates(start.getRow(), i));
            } else {
                // right to left
                for (int i = start.getCol(); i > end.getCol(); i--)
                    route.add(new Coordinates(start.getRow(), i));
            }
        } else {
            if (start.getRow() < end.getRow()) {
                // bottom to top
                for (int i = start.getRow(); i < end.getRow(); i++)
                    route.add(new Coordinates(i, start.getCol()));
            } else {
                // top to bottom
                for (int i = start.getRow(); i > end.getRow(); i--)
                    route.add(new Coordinates(i, start.getCol()));
            }
        }
    }


    /**
     * Generates an array of coordinates specifying train's wagons, based on its starting point
     *
     * @param start coordinates of the train's starting point
     * @return array of coordinates of train's wagons of length TRAIN_LENGTH
     */
    private Coordinates[] generateTrain(Coordinates start) {
        List<Coordinates> train = new LinkedList<>();

        if (start.equals(new Coordinates(0, 1))
                || start.equals(new Coordinates(0, 9))
                || start.equals(new Coordinates(16, 1))
                || start.equals(new Coordinates(16, 9))) {
            addLine(train, start, new Coordinates(start.getRow(), TunnelsMapMonitor.getWidth() / 2), true);
        } else {
            addLine(train, start, new Coordinates(TunnelsMapMonitor.getHeight() / 2, start.getCol()), false);
        }

        return train.subList(0, TRAIN_LENGTH).toArray(new Coordinates[0]);
    }
}
