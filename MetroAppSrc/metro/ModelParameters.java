package metro;

import metro.algorithm.map.Coordinates;
import metro.algorithm.map.FieldTypes;
import metro.algorithm.map.TunnelsMapMonitor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class
ModelParameters {
    public static final int NUMBER_OF_TRAINS = 3;
    public static final int TRAIN_LENGTH = 3;

    /**
     * A constant variable specifying the position of the crossing on the tunnel's map
     */
    private static final Coordinates middle
            = new Coordinates(TunnelsMapMonitor.getHeight() / 2, TunnelsMapMonitor.getWidth() / 2);

    /**
     * Array of coordinates of stations on the map
     */
    public Coordinates[] stations = {
            new Coordinates(0, 0),
            new Coordinates(0, 10),
            new Coordinates(16, 0),
            new Coordinates(16, 10)
    };


    public Coordinates[] t1Wagons, t2Wagons, t3Wagons;

    /**
     * Array of trains.
     * Each train is an array of coordinates of its wagons.
     */
    public Coordinates[][] trains;

    /**
     * An array of coordinates specifying the route of the train.
     */
    public Coordinates[] t1Route, t2Route, t3Route;

    /**
     * Specifying the initial order of the trains
     */
    public Queue<FieldTypes> trainOrder;

    /**
     * Initializes routes to the default values.
     *
     * @param trainOrder an array of FieldTypes enum values T1, T2, T3,
     *                   specifying the initial order of the trains
     */
    public ModelParameters(FieldTypes[] trainOrder) {
        t1Route = generateRoute(new Coordinates(0, 1), new Coordinates(16, 9));
        t2Route = generateRoute(new Coordinates(16, 1), new Coordinates(1, 0));
        t3Route = generateRoute(new Coordinates(15, 10), new Coordinates(0, 1));

        t1Wagons = generateTrainFromRoute(t1Route);
        t2Wagons = generateTrainFromRoute(t2Route);
        t3Wagons = generateTrainFromRoute(t3Route);

        trains = new Coordinates[NUMBER_OF_TRAINS][];
        trains[0] = t1Wagons;
        trains[1] = t2Wagons;
        trains[2] = t3Wagons;

        this.trainOrder = setTrainOrder(trainOrder);
    }

    /**
     * @param trainOrder an array of FieldTypes enum values T1, T2, T3,
     *                   specifying the initial order of the trains
     * @param routes     an array of Coordinates pairs, specifying each route's start and end
     *                   e.g. For train 1 route routes[0][0] = start, route[0][1] = end
     *                   For train 2 route routes[1][0] = start, route[1][1] = end
     */
    public ModelParameters(FieldTypes[] trainOrder, Coordinates[][] routes) {
        t1Route = generateRoute(routes[0][0], routes[0][1]);
        t2Route = generateRoute(routes[1][0], routes[1][1]);
        t3Route = generateRoute(routes[2][0], routes[2][1]);

        t1Wagons = generateTrainFromRoute(t1Route);
        t2Wagons = generateTrainFromRoute(t2Route);
        t3Wagons = generateTrainFromRoute(t3Route);

        trains = new Coordinates[NUMBER_OF_TRAINS][];
        trains[0] = t1Wagons;
        trains[1] = t2Wagons;
        trains[2] = t3Wagons;

        this.trainOrder = setTrainOrder(trainOrder);
    }


    /**
     * Generates a route from [start, end] (inclusive both ways).
     *
     * @param start coordinates of the starting point of the route
     * @param end   coordinates of the end point of the route
     * @return route from start to the end given by a list of coordinates
     */
    private Coordinates[] generateRoute(Coordinates start, Coordinates end) {
        List<Coordinates> route;

        // first, we have to check if start and end are station entrances opposite to each other in the same line
        // because then we don't have to go into the passage
        if (start.getRow() == end.getRow() && Math.abs(end.getCol() - start.getCol()) == TunnelsMapMonitor.getWidth() - 3) {
            route = new LinkedList<>();
            addLine(route, start, end, true);
            route.add(end);
            return route.toArray(new Coordinates[0]);
        }
        if (start.getCol() == end.getCol() && Math.abs(end.getRow() - start.getRow()) == TunnelsMapMonitor.getHeight() - 3) {
            route = new LinkedList<>();
            addLine(route, start, end, false);
            route.add(end);
            return route.toArray(new Coordinates[0]);
        }

        // if not, we create a route through the passage by connecting two routes:
        //  - from start the middle
        //  - a reversed route from end to the middle
        route = getRouteToTheMiddle(start);
        route.add(middle);

        List<Coordinates> routeToMiddleFromEnd = getRouteToTheMiddle(end);
        Collections.reverse(routeToMiddleFromEnd);
        route.addAll(routeToMiddleFromEnd);

        return route.toArray(new Coordinates[0]);
    }

    /**
     * Generates a list of coordinates specifying a route from start to the middle of the map
     *
     * @param start Coordinates of the starting point
     * @return route from start to the middle given by a list of coordinates
     */
    private List<Coordinates> getRouteToTheMiddle(Coordinates start) {
        List<Coordinates> route = new LinkedList<>();
        Coordinates actStart = start;
        Coordinates actEnd;
        boolean actHorizontal;

        // first we add a line to the crossing
        if (actStart.getCol() == 1 || actStart.getCol() == TunnelsMapMonitor.getWidth() - 2) {
            actHorizontal = true;
            actEnd = new Coordinates(actStart.getRow(), TunnelsMapMonitor.getWidth() / 2);
        } else {
            actHorizontal = false;
            actEnd = new Coordinates(TunnelsMapMonitor.getHeight() / 2, actStart.getCol());
        }
        addLine(route, actStart, actEnd, actHorizontal);
        actStart = actEnd;

        // then we add a line to the middle
        actHorizontal = (actStart.getRow() == middle.getRow());
        addLine(route, actStart, middle, actHorizontal);

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
     * Generates a queue from a given array, specifying the initial order of the trains
     */
    public Queue<FieldTypes> setTrainOrder(FieldTypes[] trains) {
        Queue<FieldTypes> queue = new ConcurrentLinkedQueue<>();
        Collections.addAll(queue, trains);
        return queue;
    }


    /**
     * Creates a new train by making a copy of the first TRAIN_LENGTH elements
     * from the route array.
     *
     * @param route array of coordinates specifying the route of the train
     * @return an array of coordinates specifying the wagons of the train
     */
    private Coordinates[] generateTrainFromRoute(Coordinates[] route) {
        Coordinates[] train = new Coordinates[TRAIN_LENGTH];
        for (int i = 0; i < TRAIN_LENGTH && i < route.length; i++)
            train[i] = new Coordinates(route[i]);
        return train;
    }
}
