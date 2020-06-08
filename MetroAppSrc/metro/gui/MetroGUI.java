package metro.gui;

import metro.algorithm.SimulationModel;
import metro.algorithm.map.Coordinates;
import metro.algorithm.map.FieldTypes;
import metro.algorithm.map.TunnelsMapMonitor;

import javax.swing.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The main class of this app's GUI.
 */
public class MetroGUI extends JFrame {
    private JPanel MainPanel;
    /**
     * The panel showing the current state of the tunnel's map.
     */
    private JPanel mapPanel;
    /**
     * Allows usage of the MapPanel methods of mapPanel.
     * <p>
     * Added due to some problem with adding non palette component in my IDE
     */
    private MapPanel mapPanelType;
    private JButton startPauseButton;

    private JPanel newSimulationParameters;
    private JButton newSimButton;

    private JPanel trainRouteSelection;
    private JPanel train1Route;
    /**
     * Box used to specify one of the ends of the route of train T1
     */
    private JComboBox<Coordinates> train1Start, train1End;
    private JPanel train2Route;
    /**
     * Box used to specify one of the ends of the route of train T2
     */
    private JComboBox<Coordinates> train2Start, train2End;
    private JPanel train3Route;
    /**
     * Box used to specify one of the ends of the route of train T3
     */
    private JComboBox<Coordinates> train3Start, train3End;
    /**
     * Button used to reset the simulation
     */
    private JButton resetButton;
    private JPanel trainSpeedSelection;
    private JSlider t1Slider;
    private JSlider t2Slider;
    private JSlider t3Slider;

    /**
     * Speed of the trains defined in sleep time per tile in seconds
     */
    private static final int SLEEP_MAX = 1000;
    private static final int SLEEP_MIN = 100;

    private int actT1Speed = 100;
    private int actT2Speed = 200;
    private int actT3Speed = 300;

    /**
     * The simulation model of the metro
     */
    private SimulationModel metro;

    /**
     * The monitor of the tunnel's map.
     */
    private TunnelsMapMonitor tunnelsMapMonitor;

    /**
     * Used to manage the execution of the updateGUI thread.
     */
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private Coordinates[][] routes;

    /**
     * Creates and paints the GUI.
     */
    public MetroGUI() {
        super();
        setTheme();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(MainPanel);
        addListeners();
        pack();
        setVisible(true);

    }

    /**
     * Custom creation of chosen elements.
     */
    private void createUIComponents() {
        if (metro == null) {
            metro = new SimulationModel();
        }
        if (tunnelsMapMonitor == null) {
            tunnelsMapMonitor = metro.getMonitor();
        }

        mapPanelType = new MapPanel(tunnelsMapMonitor);
        mapPanel = mapPanelType;

        t1Slider = new JSlider(SLEEP_MIN, SLEEP_MAX, actT1Speed);
        t2Slider = new JSlider(SLEEP_MIN, SLEEP_MAX, actT2Speed);
        t3Slider = new JSlider(SLEEP_MIN, SLEEP_MAX, actT3Speed);

        Coordinates[] stationsEntrances = tunnelsMapMonitor.getStationsEntrances().toArray(new Coordinates[0]);
        Coordinates[] starts = metro.getRouteStarts();
        Coordinates[] ends = metro.getRouteEnds();

        train1Start = new JComboBox<>(stationsEntrances);
        train1Start.setSelectedItem(starts[0]);
        train1End = new JComboBox<>(stationsEntrances);
        train1End.setSelectedItem(ends[0]);

        train2Start = new JComboBox<>(stationsEntrances);
        train2Start.setSelectedItem(starts[1]);
        train2End = new JComboBox<>(stationsEntrances);
        train2End.setSelectedItem(ends[1]);

        train3Start = new JComboBox<>(stationsEntrances);
        train3Start.setSelectedItem(starts[2]);
        train3End = new JComboBox<>(stationsEntrances);
        train3End.setSelectedItem(ends[2]);

        routes = getRoutes();
    }


    private void addListeners() {
        startPauseButton.addActionListener(e -> {
            if (startPauseButton.getText().equals("Launch simulation")) {
//                createNewSimulation();
                metro.restart();
                isRunning.set(true);
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        while (isRunning.get()) {
                            updateGUI();
                            try {
                                Thread.sleep(30);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                            }
                        }
                        return null;
                    }
                }.execute();

                startPauseButton.setText("Pause after train arrives");
                startPauseButton.setToolTipText("Stops after train arrives at the station.");
            } else {
                if (startPauseButton.getText().equals("Pause after train arrives")) {
                    metro.pause();
                    startPauseButton.setText("Resume simulation");
                    startPauseButton.setToolTipText("Restarts current simulation");
                } else {
                    // resume simulation
                    metro.restart();
                    startPauseButton.setText("Pause after train arrives");
                    startPauseButton.setToolTipText("Stops after train arrives at the station.");
                }
            }
        });

        newSimButton.addActionListener(e -> {
            if (routesAreValid()) {
                // first we stop the model
                metro.end();
                // then we stop the thread that's updating the map
                isRunning.set(false);
                createNewSimulation();

                startPauseButton.setText("Launch simulation");
                startPauseButton.setToolTipText("Starts the new simulation");

                revalidate();
                repaint();

                JOptionPane.showMessageDialog(this, "New simulation created successfully");
            }

        });

        resetButton.addActionListener(e -> {
            metro.end();
            isRunning.set(false);
            resetSimulation();
            startPauseButton.setText("Launch simulation");
            startPauseButton.setToolTipText("Starts the new simulation");

            revalidate();
            repaint();
        });

        t1Slider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                int speed = source.getValue();
                metro.setSleepTime(FieldTypes.T1, speed);
                actT1Speed = speed;
            }
        });

        t2Slider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                int speed = source.getValue();
                metro.setSleepTime(FieldTypes.T2, speed);
                actT2Speed = speed;
            }
        });

        t3Slider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                int speed = source.getValue();
                metro.setSleepTime(FieldTypes.T3, speed);
                actT3Speed = speed;
            }
        });
    }

    /**
     * Creates new SimulationModel object based on current parameters
     * Updates attributes of the gui.
     */
    private void createNewSimulation() {
        routes = getRoutes();
        metro = new SimulationModel(routes);
        tunnelsMapMonitor = metro.getMonitor();
        mapPanelType.setTunnelsMapMonitor(tunnelsMapMonitor);
        metro.setSleepTime(FieldTypes.T1, actT1Speed);
        metro.setSleepTime(FieldTypes.T2, actT2Speed);
        metro.setSleepTime(FieldTypes.T3, actT3Speed);
    }

    /**
     * Resets current simulation to its initial state.
     */
    private void resetSimulation() {
        metro = new SimulationModel(routes);
        tunnelsMapMonitor = metro.getMonitor();
        mapPanelType.setTunnelsMapMonitor(tunnelsMapMonitor);
    }

    /**
     * Checks if any two trains start from the same station entrance
     * or if trains are blocking each other by having same, but opposite routes
     *
     * @return true if there are no duplicates, false otherwise
     */
    private boolean routesAreValid() {
        List<Coordinates> trainStartsList = new LinkedList<>();
        trainStartsList.add((Coordinates) train1Start.getSelectedItem());
        trainStartsList.add((Coordinates) train2Start.getSelectedItem());
        trainStartsList.add((Coordinates) train3Start.getSelectedItem());
        Coordinates[] trainStarts = trainStartsList.toArray(new Coordinates[0]);

        // first we check if every train has its own starting entrance
        Set<Coordinates> trainStartsSet = new HashSet<>(trainStartsList);

        if (trainStartsSet.size() != metro.getNumberOfTrains()) {
            JOptionPane.showMessageDialog(this, "Every train has to have a different starting point");
            return false;
        }

        List<Coordinates> trainEndsList = new LinkedList<>();
        trainEndsList.add((Coordinates) train1End.getSelectedItem());
        trainEndsList.add((Coordinates) train2End.getSelectedItem());
        trainEndsList.add((Coordinates) train3End.getSelectedItem());
        Coordinates[] trainEnds = trainEndsList.toArray(new Coordinates[0]);

        for (int i = 0; i < trainStarts.length; i++) {
            Coordinates start = trainStarts[i];
            Coordinates end = trainEnds[i];

            if (start.equals(end)) {
                JOptionPane.showMessageDialog(this, "Start and end have to be different");
                return false;
            }

            for (int j = i + 1; j < trainStarts.length; j++) {
                if (start == trainEnds[j] && trainStarts[j] == end) {
                    JOptionPane.showMessageDialog(this, "Trains can't block each other");
                    return false;
                }
            }
        }
        return true;
    }


    private Coordinates[][] getRoutes() {
        Coordinates[][] routes = new Coordinates[metro.getNumberOfTrains()][2];
        routes[0][0] = (Coordinates) train1Start.getSelectedItem();
        routes[0][1] = (Coordinates) train1End.getSelectedItem();
        routes[1][0] = (Coordinates) train2Start.getSelectedItem();
        routes[1][1] = (Coordinates) train2End.getSelectedItem();
        routes[2][0] = (Coordinates) train3Start.getSelectedItem();
        routes[2][1] = (Coordinates) train3End.getSelectedItem();

        return routes;
    }


    /**
     * Thread-safely reads the map from the monitor and paints it in the GUI
     */
    public void updateGUI() {
        tunnelsMapMonitor.beginPainting();
        try {
            mapPanel.revalidate();
            mapPanel.repaint();
        } finally {
            tunnelsMapMonitor.endPainting();
        }
    }

    private void setTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }
}
