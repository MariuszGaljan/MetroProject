
package metro.algorithm;

import metro.algorithm.map.Coordinates;
import metro.algorithm.map.FieldTypes;
import metro.algorithm.map.TunnelsMapMonitor;

import java.util.Arrays;

/**
 * Class representing a concurrent train in the metro
 */
public class Train extends Thread {
    private volatile boolean isPaused = true;

    /**
     * Object used to invoke wait and notify methods on this thread
     */
    private final Object startPauseMonitor = new Object();

    /**
     * A monitor of the tunnels' map
     */
    private final TunnelsMapMonitor tunnelsMap;

    /**
     * Array of coordinates of this train's wagons.
     * Each wagon is defined by its coordinate on the map
     */
    private final Coordinates[] wagons;
    /**
     * The route of this train.
     * The train moves forward and backward along this road.
     */
    private final Coordinates[] route;
    /**
     * A variable defining current direction the train is headed to.
     */
    private boolean moveForward = true;

    /**
     * One of the FieldTypes enum values T1, T2, T3
     */
    private final FieldTypes trainType;

    /**
     * @param monitor   Monitor of the tunnel's map.
     * @param trainType one of the T1, T2, T3 values of enum FieldTypes
     * @param wagons    array of Coordinates defining the individual wagons of the train.
     * @param route     array of Coordinates defining the route the train is supposed to take
     */
    public Train(TunnelsMapMonitor monitor, FieldTypes trainType, Coordinates[] wagons, Coordinates[] route) {
        super(trainType.toString());
        this.wagons = wagons;
        this.tunnelsMap = monitor;
        this.trainType = trainType;
        this.route = route;

    }

    @Override
    public void run() {
        int i = 0;
        while (true) {
            try {
                // if the program is paused, the train has to wait
                synchronized (startPauseMonitor) {
                    if (isPaused) {
                        System.out.println(Thread.currentThread().getName() + ": Thread paused");
                        startPauseMonitor.wait();
                    }
                }
                tunnelsMap.travel(route, wagons, trainType, moveForward);

                i++;
                // after getting to the destination, the train turns around and goes back
                moveForward = !moveForward;
            } catch (InterruptedException e) {
                System.out.println(getName() + ": Interrupted");
                return;
            }
        }
    }

    /**
     * Pauses the execution of the program
     */
    public void doPause() {
        isPaused = true;
    }

    /**
     * Continues the execution of the thread.
     * If the thread is already running, nothing happens
     */
    public void doRestart() {
        synchronized (startPauseMonitor) {
            isPaused = false;
            startPauseMonitor.notify();
        }
    }

    public Coordinates getRouteEnd() {
        return route[route.length - 1];
    }

    public Coordinates getRouteStart() {
        return route[0];
    }
}
