package metro.algorithm.map;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
     * Lock used to synchronize travelling trains with the GUI map representation
     */
    ReadWriteLock ioLock = new ReentrantReadWriteLock();

    /**
     * Used for storing and synchronization of shared route segments
     */
    SegmentLock segmentLock;

    private int t1SleepTime = 100;
    private int t2SleepTime = 200;
    private int t3SleepTime = 300;


    /**
     * Constructor of TunnelsMapMonitor class.
     *
     * @param trains an array of trains.
     *               Each train is defined by an array of Coordinates values of its wagons.
     */
    public TunnelsMapMonitor(Coordinates[][] trains, Coordinates[][] trainRoutes) {
        // adding trains to the map
        markTrain(trains[0], FieldTypes.T1);
        markTrain(trains[1], FieldTypes.T2);
        markTrain(trains[2], FieldTypes.T3);

        segmentLock = new SegmentLock(createSharedSegments(trainRoutes));
        System.out.println("Shared segments: " + segmentLock);
        System.out.println();
    }


    /**
     * Procedure moving the train from crossing start to crossing end
     *
     * @param start       coordinates of current crossing
     * @param end         coordinates of destination crossing
     * @param wagons      array of Coordinates defining the individual wagons of the train.
     * @param trainType   one of the T1, T2, T3 values of enum FieldTypes
     * @param moveForward boolean value specifying the direction the train is heading
     * @throws InterruptedException this method uses sleep to visualize the transition in GUI
     */
    public void moveToNextCrossing(Coordinates start, Coordinates end, Coordinates[] wagons, FieldTypes trainType, boolean moveForward)
            throws InterruptedException {
        boolean horizontal = start.getRow() == end.getRow();
        boolean previousSegmentReleased = false;

        segmentLock.lockTrainSegments(trainType, start, moveForward);

        if (horizontal) {
            if (start.getCol() < end.getCol()) {
                // left to right
                for (int i = start.getCol(); i < end.getCol(); i++) {
                    moveTrain(wagons, new Coordinates(start.getRow(), i), trainType);
                    if (!previousSegmentReleased && !trainIsOnCrossing(start, wagons)) {
                        segmentLock.unlockTrainSegments(trainType, start, moveForward);
                        previousSegmentReleased = true;
                    }
                    wait(trainType);
                }
            } else {
                // right to left
                for (int i = start.getCol(); i > end.getCol(); i--) {
                    moveTrain(wagons, new Coordinates(start.getRow(), i), trainType);
                    if (!previousSegmentReleased && !trainIsOnCrossing(start, wagons)) {
                        segmentLock.unlockTrainSegments(trainType, start, moveForward);
                        previousSegmentReleased = true;
                    }
                    wait(trainType);
                }
            }
        } else {
            if (start.getRow() < end.getRow()) {
                // top to bottom
                for (int i = start.getRow(); i < end.getRow(); i++) {
                    moveTrain(wagons, new Coordinates(i, start.getCol()), trainType);
                    if (!previousSegmentReleased && !trainIsOnCrossing(start, wagons)) {
                        segmentLock.unlockTrainSegments(trainType, start, moveForward);
                        previousSegmentReleased = true;
                    }
                    wait(trainType);
                }
            } else {
                // bottom to top
                for (int i = start.getRow(); i > end.getRow(); i--) {
                    moveTrain(wagons, new Coordinates(i, start.getCol()), trainType);
                    if (!previousSegmentReleased && !trainIsOnCrossing(start, wagons)) {
                        segmentLock.unlockTrainSegments(trainType, start, moveForward);
                        previousSegmentReleased = true;
                    }
                    wait(trainType);
                }
            }
        }
    }

    private void wait(FieldTypes train) throws InterruptedException{
        switch (train) {
            case T1 -> Thread.sleep(t1SleepTime);
            case T2 -> Thread.sleep(t2SleepTime);
            case T3 -> Thread.sleep(t3SleepTime);
            default -> Thread.sleep(200);
        }
    }


    /**
     * Checks if the one of the wagons is on the crossing.
     *
     * @param crossing coordinates of the crossing
     * @param wagons   coordinates of the train's wagons
     * @return true if a wagon's coordinates are equal to crossing's
     * false otherwise
     */
    private boolean trainIsOnCrossing(Coordinates crossing, Coordinates[] wagons) {
        for (Coordinates wagon : wagons) {
            if (wagon.equals(crossing))
                return true;
        }
        return false;
    }

    /**
     * Analyzes the trains' routes and creates shared segments based on them.
     * Every train has its own copy of a segment.
     *
     * @param trainRoutes array of Coordinates arrays of train routes
     * @return array of shared segments
     */
    private Segment[] createSharedSegments(Coordinates[][] trainRoutes) {
        List<Segment> segments = new LinkedList<>();
        FieldTypes[] trains = {FieldTypes.T1, FieldTypes.T2, FieldTypes.T3};

        for (int i = 0; i < trainRoutes.length; i++) {
            for (int j = 0; j < trainRoutes.length; j++) {
                if (i == j)
                    continue;
                Segment[] sharedSegments = getSharedSegmentsForTwoTrains(trainRoutes[i], trainRoutes[j], trains[i], trains[j]);
                segments.addAll(Arrays.asList(sharedSegments));
            }
        }
        return segments.toArray(new Segment[0]);
    }


    /**
     * Analyzes the routes of two trains and creates shared segments based on them.
     * Every train has its own copy of a segment.
     *
     * @param t1Route array of coordinates specifying the route of the first train
     * @param t2Route array of coordinates specifying the route of the second train
     * @param t1      enum value used for segment owner identification
     * @param t2      enum value used for segment owner identification
     * @return array of shared segments
     */
    private Segment[] getSharedSegmentsForTwoTrains(Coordinates[] t1Route, Coordinates[] t2Route, FieldTypes t1, FieldTypes t2) {
        LinkedList<Segment> segments = new LinkedList<>();
        Set<Coordinates> otherRoute = new HashSet<>(Arrays.asList(t2Route));
        Coordinates actStart = t1Route[0];
        for (int i = 0; i < t1Route.length - 1; i++) {
            if (otherRoute.contains(t1Route[i]) && !otherRoute.contains(t1Route[i + 1])) {
                segments.add(new Segment(actStart, t1Route[i], t1));
                actStart = t1Route[i];
            }
            if (!otherRoute.contains(t1Route[i]) && otherRoute.contains(t1Route[i + 1])) {
                actStart = t1Route[i + 1];
            }
        }
        if (otherRoute.contains(t1Route[t1Route.length - 2]) && otherRoute.contains(t1Route[t1Route.length - 1]))
            segments.add(new Segment(actStart, t1Route[t1Route.length - 1], t1));
        return segments.toArray(new Segment[0]);
    }


    /**
     * Procedure moving the train to the given tile (should be the next tile of the train's route)
     * Works in sync with the paintMap method, so the GUI is updated after every move.
     *
     * @param wagons           array of Coordinates defining the individual wagons of the train.
     * @param nextHeadPosition coordinates the 0th elem of wagons array will move to
     * @param trainType        one of the T1, T2, T3 values of enum FieldTypes
     */
    private void moveTrain(Coordinates[] wagons, Coordinates nextHeadPosition, FieldTypes trainType) {
        ioLock.writeLock().lock();

        try {
            Coordinates nextPosition = nextHeadPosition;
            Coordinates oldPosition;

            eraseTrain(wagons);
            // we shift every wagons position by one
            for (Coordinates wagon : wagons) {
                oldPosition = new Coordinates(wagon);
                wagon.moveTo(nextPosition);
                nextPosition = oldPosition;
            }
            markTrain(wagons, trainType);
        } finally {
            ioLock.writeLock().unlock();
        }
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
     * Acquires the synchronization tools.
     * After reading you should invoke endPainting().
     */
    public void beginPainting() {
        ioLock.readLock().lock();
    }

    /**
     * Releases the synchronization tools.
     * Should be invoked after beginPainting(), once the reading is done.
     */
    public void endPainting() {
        ioLock.readLock().unlock();
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

    public void setSleepTime(int sleepTime, FieldTypes train) {
        switch (train) {
            case T1 -> t1SleepTime = sleepTime;
            case T2 -> t2SleepTime = sleepTime;
            case T3 -> t3SleepTime = sleepTime;
        }
    }

    /**
     * Returns list of coordinates of entrances to all stations
     *
     * @return list of coordinates defining the entrances to the stations
     */
    public List<Coordinates> getStationsEntrances() {
        return mapWrapper.getStationsEntrances();
    }
}
