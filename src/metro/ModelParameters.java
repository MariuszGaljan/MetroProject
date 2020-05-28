package metro;

import metro.algorithm.map.Coordinates;
import metro.algorithm.map.FieldTypes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class
ModelParameters {
    public static final int numberOfTrains = 3;

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
        t1Route = createT1Route();
        t2Route = createT2Route();
        t3Route = createT3Route();
        this.trainOrder = setTrainOrder(trainOrder);
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
     * */
    public Queue<FieldTypes> setTrainOrder(FieldTypes[] trains) {
        Queue<FieldTypes> queue = new ConcurrentLinkedQueue<>();
        Collections.addAll(queue, trains);
        return queue;
    }
}
