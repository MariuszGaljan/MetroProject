package metro.algorithm;

import metro.algorithm.map.Coordinates;
import metro.algorithm.map.FieldTypes;
import metro.algorithm.map.TunnelsMapMonitor;

/**
 * A class defining an app simulating the concurrently working metro
 * Use start() method to launch the GUI of the simulation.
 */
public class SimulationModel {
    /**
     * Contains trains' routes and wagons
     */
    ModelParameters modelParams;

    /**
     * Thread-safe tunnel map
     */
    TunnelsMapMonitor monitor;

    /**
     * Thread representing a train
     */
    Train[] trains = new Train[ModelParameters.NUMBER_OF_TRAINS];


    /**
     * Initializes routes to the default values.
     */
    public SimulationModel() {
        // here we specify the parameters of the simulation
        modelParams = new ModelParameters();
        monitor = new TunnelsMapMonitor(modelParams.trains, modelParams.crossings);

        trains[0] = new Train(monitor, FieldTypes.T1, modelParams.trains[0], modelParams.t1Crossings);
        trains[1] = new Train(monitor, FieldTypes.T2, modelParams.trains[1], modelParams.t2Crossings);
        trains[2] = new Train(monitor, FieldTypes.T3, modelParams.trains[2], modelParams.t3Crossings);

        for (Thread t : trains)
            t.start();

        // the simulation starts as paused (that way the startButton implementation is simpler
        pause();
    }

    /**
     * Initializes routes to the default values.
     *
     * @param routes     an array of Coordinates pairs, specifying each route's start and end
     *                   e.g. For train 1 route routes[0][0] = start, route[0][1] = end
     *                   For train 2 route routes[1][0] = start, route[1][1] = end
     */
    public SimulationModel(Coordinates[][] routes) {
        // here we specify the parameters of the simulation
        modelParams = new ModelParameters(routes);
        monitor = new TunnelsMapMonitor(modelParams.trains, modelParams.crossings);

        trains[0] = new Train(monitor, FieldTypes.T1, modelParams.trains[0], modelParams.t1Crossings);
        trains[1] = new Train(monitor, FieldTypes.T2, modelParams.trains[1], modelParams.t2Crossings);
        trains[2] = new Train(monitor, FieldTypes.T3, modelParams.trains[2], modelParams.t3Crossings);

        for (Thread t : trains)
            t.start();

        // the simulation starts as paused (that way the startButton implementation is simpler
        pause();
    }

    /**
     * Ends the simulation by interrupting its thread.
     */
    public void end() {
        for (Thread t : trains)
            if (t != null)
                t.interrupt();
    }

    /**
     * Pauses the execution after the current train arrives at the station
     */
    public void pause() {
        for (Train t : trains) {
            t.doPause();
        }
    }

    /**
     * Restarts the simulation
     */
    public void restart() {
        for (Train t : trains)
            t.doRestart();
    }

    /**
     * Returns the monitor attached to this model.
     *
     * @return Monitor of the tunnel's map.
     */
    public TunnelsMapMonitor getMonitor() {
        return monitor;
    }

    /**
     * Returns number of trains in the simulation.
     *
     * @return constant value specifying the number of trains in the simulation
     */
    public int getNumberOfTrains() {
        return ModelParameters.NUMBER_OF_TRAINS;
    }


    public Coordinates[] getRouteStarts() {
        Coordinates[] starts = new Coordinates[ModelParameters.NUMBER_OF_TRAINS];
        starts[0] = modelParams.t1Route[0];
        starts[1] = modelParams.t2Route[0];
        starts[2] = modelParams.t3Route[0];
        return starts;
    }


    public Coordinates[] getRouteEnds() {
        Coordinates[] ends = new Coordinates[ModelParameters.NUMBER_OF_TRAINS];
        ends[0] = modelParams.t1Route[modelParams.t1Route.length - 1];
        ends[1] = modelParams.t2Route[modelParams.t2Route.length - 1];
        ends[2] = modelParams.t3Route[modelParams.t3Route.length - 1];
        return ends;
    }

    public void setSleepTime(FieldTypes train, int sleepTime) {
        switch (train) {
            case T1 -> trains[0].setSleepTime(sleepTime);
            case T2 -> trains[1].setSleepTime(sleepTime);
            case T3 -> trains[2].setSleepTime(sleepTime);
        }
    }
}
