package metro.algorithm;

import metro.ModelParameters;
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
     *
     * @param trainOrder an array of FieldTypes enum values T1, T2, T3,
     *                   specifying the initial order of the trains
     */
    public SimulationModel(FieldTypes[] trainOrder) {
        // here we specify the parameters of the simulation
        modelParams = new ModelParameters(trainOrder);
        monitor = new TunnelsMapMonitor(modelParams.trains, modelParams.stations, modelParams.trainOrder);

        trains[0] = new Train(monitor, FieldTypes.T1, modelParams.t1Wagons, modelParams.t1Route);
        trains[1] = new Train(monitor, FieldTypes.T2, modelParams.t2Wagons, modelParams.t2Route);
        trains[2] = new Train(monitor, FieldTypes.T3, modelParams.t3Wagons, modelParams.t3Route);

        for (Thread t : trains)
            t.start();

        // the simulation starts as paused (that way the startButton implementation is simpler
        pause();
    }

    /**
     * Initializes routes to the default values.
     *
     * @param trainOrder an array of FieldTypes enum values T1, T2, T3,
     *                   specifying the initial order of the trains
     * @param routes     an array of Coordinates pairs, specifying each route's start and end
     *                   e.g. For train 1 route routes[0][0] = start, route[0][1] = end
     *                        For train 2 route routes[1][0] = start, route[1][1] = end
     */
    public SimulationModel(FieldTypes[] trainOrder, Coordinates[][] routes) {
        // here we specify the parameters of the simulation
        modelParams = new ModelParameters(trainOrder, routes);
        monitor = new TunnelsMapMonitor(modelParams.trains, modelParams.stations, modelParams.trainOrder);

        trains[0] = new Train(monitor, FieldTypes.T1, modelParams.t1Wagons, modelParams.t1Route);
        trains[1] = new Train(monitor, FieldTypes.T2, modelParams.t2Wagons, modelParams.t2Route);
        trains[2] = new Train(monitor, FieldTypes.T3, modelParams.t3Wagons, modelParams.t3Route);

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
     */
    public TunnelsMapMonitor getMonitor() {
        return monitor;
    }


    public void setTrainOrder(FieldTypes[] trainOrder) {
        modelParams.setTrainOrder(trainOrder);
    }

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
}
