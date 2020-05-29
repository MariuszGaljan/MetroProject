package metro;

import metro.algorithm.map.Coordinates;
import metro.algorithm.map.FieldTypes;
import metro.algorithm.map.TunnelsMapMonitor;

import java.awt.image.MemoryImageSource;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class
ModelParameters {
    public static final int numberOfTrains = 3;
    private final Coordinates middle
            = new Coordinates(TunnelsMapMonitor.getHeight() / 2, TunnelsMapMonitor.getWidth() / 2);

    public Coordinates[] stations = {
            new Coordinates(0, 0),
            new Coordinates(16, 0),
            new Coordinates(16, 10),
    };


    public Coordinates[] t1Wagons = {
            new Coordinates(0, 1),
            new Coordinates(0, 2),
            new Coordinates(0, 3)
    };

    public Coordinates[] t2Wagons = {
            new Coordinates(16, 1),
            new Coordinates(16, 2),
            new Coordinates(16, 3)
    };

    public Coordinates[] t3Wagons = {
            new Coordinates(15, 10),
            new Coordinates(14, 10),
            new Coordinates(13, 10)
    };

    public Coordinates[][] trains = {t1Wagons, t2Wagons, t3Wagons};

    public Coordinates[] t1Route, t2Route, t3Route;

    public Queue<FieldTypes> trainOrder;

    public ModelParameters(FieldTypes[] trainOrder) {
        t1Route = generateRoute(new Coordinates(0, 1), new Coordinates(16, 9));
        t2Route = generateRoute(new Coordinates(16, 1), new Coordinates(1, 0));
        t3Route = generateRoute(new Coordinates(15, 10), new Coordinates(0, 1));
        this.trainOrder = setTrainOrder(trainOrder);
    }

    private Coordinates[] generateRoute(Coordinates start, Coordinates end) {
        List<Coordinates> route = getRouteToTheMiddle(start);
        route.add(middle);

        List<Coordinates> routeToMiddleFromEnd = getRouteToTheMiddle(end);
        Collections.reverse(routeToMiddleFromEnd);
        route.addAll(routeToMiddleFromEnd);

        return route.toArray(new Coordinates[0]);
    }

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
     * Generates an array of coordinates specifying the route of train 1
     */
    private Coordinates[] createT1Route() {
        List<Coordinates> route = new LinkedList<>();
        for (int i = 1; i < 6; i++)
            route.add(new Coordinates(0, i));
        for (int i = 1; i < 17; i++)
            route.add(new Coordinates(i, 5));
        for (int i = 6; i < 10; i++)
            route.add(new Coordinates(16, i));

        return route.toArray(new Coordinates[0]);
    }

    /**
     * Generates an array of coordinates specifying the route of train 2
     */
    private Coordinates[] createT2Route() {
        List<Coordinates> route = new LinkedList<>();
        for (int i = 1; i < 9; i++)
            route.add(new Coordinates(i, 0));
        for (int i = 1; i < 6; i++)
            route.add(new Coordinates(8, i));
        for (int i = 9; i < 17; i++)
            route.add(new Coordinates(i, 5));
        for (int i = 4; i > 0; i--)
            route.add(new Coordinates(16, i));

        Collections.reverse(route);

        return route.toArray(new Coordinates[0]);
    }

    /**
     * Generates an array of coordinates specifying the route of train 3
     */
    private Coordinates[] createT3Route() {
        List<Coordinates> route = new LinkedList<>();
        for (int i = 15; i >= 8; i--)
            route.add(new Coordinates(i, 10));
        for (int i = 9; i >= 5; i--)
            route.add(new Coordinates(8, i));
        for (int i = 7; i >= 0; i--)
            route.add(new Coordinates(i, 5));
        for (int i = 4; i >= 1; i--)
            route.add(new Coordinates(0, i));

        return route.toArray(new Coordinates[0]);
    }

    /**
     * Generates a queue from a given array, specifying the initial order of the trains
     */
    public Queue<FieldTypes> setTrainOrder(FieldTypes[] trains) {
        Queue<FieldTypes> queue = new ConcurrentLinkedQueue<>();
        Collections.addAll(queue, trains);
        return queue;
    }
}
