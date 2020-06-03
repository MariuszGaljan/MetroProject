package metro;

import metro.gui.MetroGUI;

/**
 * Application creating a simulation of a metro.
 * There are 3 trains: T1, T2, T3.
 * Each train has an assigned route, going from one station's entrance to another.
 * Each train moves forward and backward along its route.
 * <p>
 * The app launches a gui that lets the user change the parameters.
 * The gui also visualizes the algorithm.
 * <p>
 * Invoke its static start() method to initialize
 *
 * @author Mariusz Galjan
 */
public class MetroApplication {
    public static void start() {
        new MetroGUI();
    }

    public static void main(String[] args) {
        MetroApplication.start();
    }
}
