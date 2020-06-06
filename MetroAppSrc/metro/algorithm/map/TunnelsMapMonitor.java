package metro.algorithm.map;

import java.util.*;
import java.util.concurrent.locks.*;

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

    CrossingsLock crossingsLock;


    /**
     * Constructor of TunnelsMapMonitor class.
     *
     * @param trains   an array of trains.
     *                 Each train is defined by an array of Coordinates values of its wagons.
     * @param stations an array of stations' coordinates
     */
    public TunnelsMapMonitor(Coordinates[][] trains, Coordinates[] stations, Coordinates[][] crossings) {
        // adding trains to the map
        markTrain(trains[0], FieldTypes.T1);
        markTrain(trains[1], FieldTypes.T2);
        markTrain(trains[2], FieldTypes.T3);

        // adding station to the map
        TunnelsMap.stations = stations;
        for (Coordinates station : TunnelsMap.stations)
            tunnelsMap[station.getRow()][station.getCol()] = FieldTypes.STATION;

        Set<Coordinates> crossingsSet = new HashSet<>();
        for (Coordinates[] trainCrossings : crossings)
            crossingsSet.addAll(Arrays.asList(trainCrossings));
        crossingsLock = new CrossingsLock(crossingsSet.toArray(new Coordinates[0]));
    }


    /**
     * Procedure used to acquire the locks needed to achieve thread safety.
     */
    public void beginCourse(Coordinates start) {
       crossingsLock.lockCrossing(start);
    }


    /**
     * Procedure moving the train via the route to its destination
     *
     * @param start     coordinates of current crossing
     * @param end       coordinates of destination crossing
     * @param wagons    array of Coordinates defining the individual wagons of the train.
     * @param trainType one of the T1, T2, T3 values of enum FieldTypes
     * @throws InterruptedException this method uses sleep to visualize the transition in GUI
     */
    public void moveToNextCrossing(Coordinates start, Coordinates end, Coordinates[] wagons, FieldTypes trainType)
            throws InterruptedException {
        boolean horizontal = start.getRow() == end.getRow();

        if (horizontal) {
            if (start.getCol() < end.getCol()) {
                // left to right
                for (int i = start.getCol(); i < end.getCol(); i++) {
                    moveTrain(wagons, new Coordinates(start.getRow(), i), trainType);
                    Thread.sleep(200);
                }
            } else {
                // right to left
                for (int i = start.getCol(); i > end.getCol(); i--) {
                    moveTrain(wagons, new Coordinates(start.getRow(), i), trainType);
                    Thread.sleep(200);
                }
            }
        } else {
            if (start.getRow() < end.getRow()) {
                // top to bottom
                for (int i = start.getRow(); i < end.getRow(); i++) {
                    moveTrain(wagons, new Coordinates(i, start.getCol()), trainType);
                    Thread.sleep(200);
                }
            } else {
                // bottom to top
                for (int i = start.getRow(); i > end.getRow(); i--) {
                    moveTrain(wagons, new Coordinates(i, start.getCol()), trainType);
                    Thread.sleep(200);
                }
            }
        }
    }


    /**
     * Procedure used to release the locks and the starting point of the train.
     * Should be called after the beginCourse function
     * Adds the train that reached its destination to the end of the queue.
     *
     * @param start     coordinates of the starting point to unlock
     */
    public void endCourse(Coordinates start) {
        crossingsLock.unlockCrossing(start);
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

    /**
     * Returns list of coordinates of entrances to all stations
     *
     * @return list of coordinates defining the entrances to the stations
     */
    public List<Coordinates> getStationsEntrances() {
        return mapWrapper.getStationsEntrances();
    }
}
