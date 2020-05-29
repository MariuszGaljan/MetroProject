package metro.algorithm.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class used to ensure that trains only move when the station's entrance
 * they're headed to is not occupied.
 * <p>
 * The assumption is that a train reaches a station when one of its wagons is on a tile adjacent to the station.
 * There can be many trains at one station as long as there's max one on each tile adjacent to this station.
 * <p>
 * It creates and uses a list of Conditions from a given lock,
 * one for each station's entrance
 */
public class StationsLock {
    /**
     * Instance of a package-private class TunnelsMap
     * */
    TunnelsMap mapWrapper;
    /**
     * the map is defined in the TunnelsMap package-private class
     */
    FieldTypes[][] tunnelsMap;

    /**
     * List of station's entrances coordinates
     */
    List<Coordinates> coordinates;
    /**
     * List of condition of the same size as coordinates
     */
    List<Condition> conditions;
    /**
     * Logical values used with conditions
     */
    boolean[] entranceOccupied;

    /**
     * A lock passed through a constructor used to create the conditions.
     */
    ReentrantLock lock;

    /**
     * @param stations an array of stations' coordinates
     * @param lock     a lock used in the synchronization process
     */
    public StationsLock(TunnelsMap mapWrapper, Coordinates[] stations, ReentrantLock lock) {
        this.mapWrapper = mapWrapper;
        this.tunnelsMap = mapWrapper.map;
        this.lock = lock;
        coordinates = new LinkedList<>();
        conditions = new ArrayList<>();
        for (Coordinates station : stations) {
            createStationConditions(station);
        }
        // we initialize all boolean occupied values to false...
        entranceOccupied = new boolean[conditions.size()];
        Arrays.fill(entranceOccupied, false);
        // ... except for the starting entrances of the trains
        markInitialTrainEntrances();

        // printing condition addresses
//        for (int i = 0; i < coordinates.size(); i++) {
//            System.out.println(coordinates.get(i) + ", " + entranceOccupied[i] + ", " + conditions.get(i));
//        }
    }

    /**
     * Locks the lock and waits until the destination entrance is not occupied.
     * Marks the destination as occupied.
     *
     * @param destination coordinates of the destination to occupy
     * @param supervisor  a condition that is signaled if the destination is locked.
     *                    Used to launch the next train in case this one can't move.
     */
    public void lockDestination(Coordinates destination, Condition supervisor) throws InterruptedException {
        lock.lock();
        int i = getIndex(destination);


        while (entranceOccupied[i]) {
            System.out.print(Thread.currentThread().getName() + ": Entrance " + coordinates.get(i) + " is taken. ");
            System.out.println("Signaling next train");
            supervisor.signal();
            conditions.get(i).await();
        }

        entranceOccupied[i] = true;
    }

    /**
     * Marks the starting point as not occupied and signals the trains waiting for it to be released.
     * The lock should be released manually
     */
    public void signalStartingPoint(Coordinates startingPoint) {
        int i = getIndex(startingPoint);
        entranceOccupied[i] = false;
        conditions.get(i).signal();
//        lock.unlock();
    }

    /**
     * Return the index of coordinates in the coordinates list.
     *
     * @param coordinates coordinates to look for in the list.
     */
    private int getIndex(Coordinates coordinates) {
        return this.coordinates.indexOf(coordinates);
    }

    /**
     * Creates a condition for every tile marked as empty adjacent to the given station (station's entrance)
     *
     * @param station coordinates of a station.
     */
    private void createStationConditions(Coordinates station) {
        coordinates = mapWrapper.getStationsEntrances();

        for (Coordinates c : coordinates)
            conditions.add(lock.newCondition());

//        int[] possibleVectors = {-1, 0, 1};
//        int row, col;
//
//        for (int vectorRow : possibleVectors) {
//            for (int vectorCol : possibleVectors) {
//                if (vectorRow != 0 || vectorCol != 0) {
//                    row = station.getRow() + vectorRow;
//                    col = station.getCol() + vectorCol;
//
//                    if (row >= 0 && row < TunnelsMap.HEIGHT)
//                        if (col >= 0 && col < TunnelsMap.WIDTH)
//                            if (tunnelsMap[row][col] != FieldTypes.WALL) {
//                                coordinates.add(new Coordinates(row, col));
//                                conditions.add(lock.newCondition());
//                            }
//                }
//            }
//        }
    }

    /**
     * Marks the stations' entrances that the trains are on at the start of the program.
     */
    private void markInitialTrainEntrances() {
        int i;
        for (Coordinates c : coordinates) {
            if (tunnelsMap[c.getRow()][c.getCol()] != FieldTypes.EMPTY) {
                i = coordinates.indexOf(c);
                entranceOccupied[i] = true;
            }
        }
    }
}
