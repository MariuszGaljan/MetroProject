package metro.gui;

import metro.algorithm.SimulationModel;
import metro.algorithm.map.Coordinates;
import metro.algorithm.map.FieldTypes;
import metro.algorithm.map.TunnelsMapMonitor;

import javax.swing.*;
import java.util.*;
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
    /**
     * combo box used for the initial train order selection
     */
    private JComboBox<FieldTypes> train1Select, train2Select, train3Select;
    private JPanel trainOrderSelection;
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

    private FieldTypes[] trainOrder;
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
        FieldTypes[] initialTrainOrder = {FieldTypes.T1, FieldTypes.T2, FieldTypes.T3};

        if (metro == null) {
            metro = new SimulationModel(initialTrainOrder);
        }
        if (tunnelsMapMonitor == null) {
            tunnelsMapMonitor = metro.getMonitor();
        }

        mapPanelType = new MapPanel(tunnelsMapMonitor);
        mapPanel = mapPanelType;

        train1Select = new JComboBox<>(initialTrainOrder);
        train1Select.setSelectedIndex(0);
        train2Select = new JComboBox<>(initialTrainOrder);
        train2Select.setSelectedIndex(1);
        train3Select = new JComboBox<>(initialTrainOrder);
        train3Select.setSelectedIndex(2);

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

        trainOrder = initialTrainOrder;
        routes = getRoutes();
    }


    private void addListeners() {
        startPauseButton.addActionListener(e -> {
            if (startPauseButton.getText().equals("Launch simulation")) {
//                createNewSimulation();
                System.out.println("Train order: " + Arrays.toString(getTrainOrder()));
                metro.restart();
                isRunning.set(true);
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        while (isRunning.get()) {
                            updateGUI();
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
    }

    /**
     * Creates new SimulationModel object based on current parameters
     * Updates attributes of the gui.
     */
    private void createNewSimulation() {
        trainOrder = getTrainOrder();
        routes = getRoutes();
        metro = new SimulationModel(trainOrder, routes);
        tunnelsMapMonitor = metro.getMonitor();
        mapPanelType.setTunnelsMapMonitor(tunnelsMapMonitor);
    }

    /**
     * Resets current simulation to its initial state.
     */
    private void resetSimulation() {
        metro = new SimulationModel(trainOrder, routes);
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
        // first we check if every train has its own starting entrance
        Set<Coordinates> trainStartsSet = new HashSet<>();
        trainStartsSet.add((Coordinates) train1Start.getSelectedItem());
        trainStartsSet.add((Coordinates) train2Start.getSelectedItem());
        trainStartsSet.add((Coordinates) train3Start.getSelectedItem());

        if (trainStartsSet.size() != metro.getNumberOfTrains()) {
            JOptionPane.showMessageDialog(this, "Every train has to have a different starting point");
            return false;
        }

        Coordinates[] trainStarts = trainStartsSet.toArray(new Coordinates[0]);

        List<Coordinates> trainEndsList = new LinkedList<>();
        trainEndsList.add((Coordinates) train1End.getSelectedItem());
        trainEndsList.add((Coordinates) train2End.getSelectedItem());
        trainEndsList.add((Coordinates) train3End.getSelectedItem());
        Coordinates[] trainEnds = trainEndsList.toArray(new Coordinates[0]);

        for (int i = 0; i < trainStarts.length; i++) {
            Coordinates start = trainStarts[i];
            Coordinates end = trainEnds[i];
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
     * Reads the initial order of the trains and returns it as an array
     *
     * @return array of FieldTypes specifying order of the trains
     */
    private FieldTypes[] getTrainOrder() {
        FieldTypes[] trainOrder = new FieldTypes[metro.getNumberOfTrains()];
        trainOrder[0] = (FieldTypes) train1Select.getSelectedItem();
        trainOrder[1] = (FieldTypes) train2Select.getSelectedItem();
        trainOrder[2] = (FieldTypes) train3Select.getSelectedItem();
        return trainOrder;
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
