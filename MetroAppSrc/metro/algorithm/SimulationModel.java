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
     * Array of enums representing the trains
     */
    FieldTypes[] trainTypes = {FieldTypes.T1, FieldTypes.T2, FieldTypes.T3};

    /**
     * Initializes routes to the default values.
     */
    public SimulationModel() {
        // here we specify the parameters of the simulation
        modelParams = new ModelParameters();
        monitor = new TunnelsMapMonitor(modelParams.trains, modelParams.crossings);

        for (int i = 0; i < trains.length; i++)
            trains[i] = new Train(monitor, trainTypes[i], modelParams.trains[i], modelParams.crossings[i]);

        for (Thread t : trains)
            t.start();

        // the simulation starts as paused (that way the startButton implementation is simpler
        pause();
    }

    /**
     * Initializes routes to the default values.
     *
     * @param routes an array of Coordinates pairs, specifying each route's start and end
     *               e.g. For train 1 route routes[0][0] = start, route[0][1] = end
     *               For train 2 route routes[1][0] = start, route[1][1] = end
     */
    public SimulationModel(Coordinates[][] routes) {
        // here we specify the parameters of the simulation
        modelParams = new ModelParameters(routes);
        monitor = new TunnelsMapMonitor(modelParams.trains, modelParams.crossings);

        for (int i = 0; i < trains.length; i++)
            trains[i] = new Train(monitor, trainTypes[i], modelParams.trains[i], modelParams.crossings[i]);

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
        for (int i = 0; i < starts.length; i++)
            starts[i] = modelParams.crossings[i][0];
        return starts;
    }


    public Coordinates[] getRouteEnds() {
        Coordinates[] ends = new Coordinates[ModelParameters.NUMBER_OF_TRAINS];
        for (int i = 0; i < ends.length; i++)
            ends[i] = modelParams.crossings[i][modelParams.crossings[i].length - 1];
        return ends;
    }

    /**
     * Set the time a train waits after moving to the next tile
     *
     * @param train     enum value identifying the train
     * @param sleepTime time in ms
     */
    public void setSleepTime(FieldTypes train, int sleepTime) {
        switch (train) {
            case T1 -> trains[0].setSleepTime(sleepTime);
            case T2 -> trains[1].setSleepTime(sleepTime);
            case T3 -> trains[2].setSleepTime(sleepTime);
        }
    }
}
