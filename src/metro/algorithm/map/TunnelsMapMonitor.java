package metro.algorithm.map;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class used to access the map of the tunnels.
 * It uses (x, y) coordinates specified in Coordinates class instances.
 * It allows thread-safe moving of the trains and reading the state of the map.
 */
public class TunnelsMapMonitor {
    /**
     * Instance of a package-private class TunnelsMap
     */
    TunnelsMap mapWrapper = new TunnelsMap();
    /**
     * the map is defined in the TunnelsMap package-private class
     */
    FieldTypes[][] tunnelsMap = mapWrapper.map;

    /**
     * Semaphore used to synchronize travelling trains with the GUI map representation
     */
    Semaphore readSem = new Semaphore(1);
    /**
     * Semaphore used to synchronize travelling trains with the GUI map representation
     */
    Semaphore writeSem = new Semaphore(0);

    /**
     * The lock used for synchronization of the trains
     */
    ReentrantLock lock = new ReentrantLock();

    /**
     * Used to distinguish occupied station sides from the free ones.
     * tactically, these are conditions, but I thought lock is a more appropriate name for its use
     * <p>
     * Uses the same lock as the monitor
     * <p>
     * Necessary for the trains' synchronization *
     */
    StationsLock stationsLock;

    /**
     * A condition used to ensure that only one train is moving at any time
     */
    Condition supervisor = lock.newCondition();
    Queue<FieldTypes> trainQueue;


    /**
     * Constructor of TunnelsMapMonitor class.
     *
     * @param trains   an array of trains.
     *                 Each train is defined by an array of Coordinates values of its wagons.
     * @param stations an array of stations' coordinates
     */
    public TunnelsMapMonitor(Coordinates[][] trains, Coordinates[] stations, Queue<FieldTypes> trainOrder) {
        // adding trains to the map
        markTrain(trains[0], FieldTypes.T1);
        markTrain(trains[1], FieldTypes.T2);
        markTrain(trains[2], FieldTypes.T3);

        // initial order of the trains
        trainQueue = trainOrder;

        // adding station to the map
        TunnelsMap.stations = stations;
        for (Coordinates station : TunnelsMap.stations)
            tunnelsMap[station.getRow()][station.getCol()] = FieldTypes.STATION;

        // initializing the locks for every station's entrance (side)
        stationsLock = new StationsLock(mapWrapper, stations, lock);
    }

    /**
     * Procedure used to move the train of trainType via given route.
     *
     * @param route       array of Coordinates defining the route the train is supposed to take
     * @param wagons      array of Coordinates defining the individual wagons of the train.
     * @param trainType   one of the T1, T2, T3 values of enum FieldTypes
     * @param moveForward boolean value defining whether the train starts from route[0] or the last elem of route
     * @throws InterruptedException may throw during the travel
     */
    public void travel(Coordinates[] route, Coordinates[] wagons, FieldTypes trainType, boolean moveForward)
            throws InterruptedException {
        // first we set start and end point accordingly to the direction
        Coordinates start = moveForward ? route[0] : route[route.length - 1];
        Coordinates end = moveForward ? route[route.length - 1] : route[0];

        try {
            beginCourse(end, trainType);
            moveToNextStation(route, wagons, trainType, moveForward);
        } finally {
            endCourse(start, trainType);
        }
    }

    /**
     * Procedure used to acquire the locks needed to achieve thread safety.
     *
     * @param destination coordinate of the route's end point. It's used to lock the station lock of a given coordinate
     * @param trainType   enum value defining the current train
     * @throws InterruptedException the thread can wait on a Condition
     */
    private void beginCourse(Coordinates destination, FieldTypes trainType) throws InterruptedException {
        lock.lock();

        // if the queue is empty, it means all the trains are blocking each other
        if (trainQueue.isEmpty()) {
            System.out.println(Thread.currentThread().getName() + ": no possible move");
            return;
        }

        while (trainQueue.peek() != trainType) {
            // if it's not this train's turn, we have to signal the next waiting train
            // so it checks if it's maybe its turn
            supervisor.signal();
            supervisor.await();
        }
        Thread.sleep(1);
        lock.lock();

        // once it's this train's turn, we remove it from the queue
        trainQueue.poll();
        stationsLock.lockDestination(destination, supervisor);
    }


    /**
     * Procedure moving the train via the route to its destination
     *
     * @param route       array of Coordinates defining the route the train is supposed to take
     * @param wagons      array of Coordinates defining the individual wagons of the train.
     * @param trainType   one of the T1, T2, T3 values of enum FieldTypes
     * @param moveForward boolean value defining whether the train starts from route[0] or the last elem of route
     */
    private void moveToNextStation(Coordinates[] route, Coordinates[] wagons, FieldTypes trainType, boolean moveForward)
            throws InterruptedException {
        if (moveForward) {
            for (Coordinates nextStep : route) {
                moveTrain(wagons, nextStep, trainType);
                // I used sleep here to make the transition visible in the GUI
                Thread.sleep(200);
            }
        } else {
            Coordinates nextStep;
            for (int i = route.length - 1; i >= 0; i--) {
                nextStep = route[i];
                moveTrain(wagons, nextStep, trainType);
                Thread.sleep(200);
            }
        }
    }


    /**
     * Procedure used to release the locks and the starting point of the train.
     * Should be called after the beginCourse function
     * Adds the train that reached its destination to the end of the queue.
     *
     * @param start     coordinates of the starting point to unlock
     * @param trainType one of the T1, T2, T3 values of enum FieldTypesain
     */
    private void endCourse(Coordinates start, FieldTypes trainType) {
        trainQueue.add(trainType);
        stationsLock.signalStartingPoint(start);
        lock.unlock();
        supervisor.signal();
    }


    /**
     * Procedure moving the train to the given tile (should be the next tile of the train's route)
     * Works in sync with the paintMap method, so the GUI is updated after every move.
     *
     * @param wagons           array of Coordinates defining the individual wagons of the train.
     * @param nextHeadPosition coordinates the 0th elem of wagons array will move to
     * @param trainType        one of the T1, T2, T3 values of enum FieldTypes
     */
    private void moveTrain(Coordinates[] wagons, Coordinates nextHeadPosition, FieldTypes trainType)
            throws InterruptedException {
        writeSem.acquire();

        Coordinates nextPosition = nextHeadPosition;
        Coordinates oldPosition;

        eraseTrain(wagons);
        for (Coordinates wagon : wagons) {
            // we move every wagon one by one to the next position
            oldPosition = new Coordinates(wagon);
            wagon.moveTo(nextPosition);
            nextPosition = oldPosition;
        }
        markTrain(wagons, trainType);

        readSem.release();
    }

    /**
     * Marks the train on the map.
     *
     * @param wagons    array of Coordinates defining the individual wagons of the train.
     * @param trainType one of the T1, T2, T3 values of enum FieldTypes
     */
    private void markTrain(Coordinates[] wagons, FieldTypes trainType) {
        for (Coordinates actWagon : wagons)
            tunnelsMap[actWagon.getRow()][actWagon.getCol()] = trainType;
    }

    /**
     * Removes the train from the map.
     *
     * @param wagons array of Coordinates defining the individual wagons of the train.
     */
    private void eraseTrain(Coordinates[] wagons) {
        for (Coordinates actWagon : wagons)
            tunnelsMap[actWagon.getRow()][actWagon.getCol()] = FieldTypes.EMPTY;
    }

    /**
     * Prints the tunnel's map.
     */
    public void printMap() {
        beginPainting();
        System.out.println();
        for (FieldTypes[] row : tunnelsMap) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println();
        endPainting();
    }


    /**
     * Acquires the synchronization tools
     */
    public void beginPainting() {
        try {
            readSem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void endPainting() {
        writeSem.release();
    }

    /**
     * Function used to get the value of a given field
     *
     * @param field coordinates of a given field
     * @return the FieldTypes value of the field
     */
    public FieldTypes getField(Coordinates field) {
        return tunnelsMap[field.getRow()][field.getCol()];
    }

    public static int getWidth() {
        return TunnelsMap.WIDTH;
    }

    public static int getHeight() {
        return TunnelsMap.HEIGHT;
    }
}
