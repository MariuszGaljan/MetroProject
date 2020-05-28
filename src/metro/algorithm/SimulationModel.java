package metro.algorithm;

import metro.ModelParameters;
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
    Train[] trains = new Train[ModelParameters.numberOfTrains];


    public SimulationModel() {
        // here we specify the parameters of the simulation
        modelParams = new ModelParameters();
        monitor = new TunnelsMapMonitor(modelParams.trains, modelParams.stations);

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
     * */
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


}
