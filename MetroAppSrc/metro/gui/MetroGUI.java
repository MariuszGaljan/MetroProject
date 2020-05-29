package metro.gui;

import metro.algorithm.SimulationModel;
import metro.algorithm.map.FieldTypes;
import metro.algorithm.map.TunnelsMapMonitor;

import javax.swing.*;
import java.util.Arrays;
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
    private JButton resetButton;

    private JComboBox<FieldTypes> train1Select;
    private JComboBox<FieldTypes> train2Select;
    private JComboBox<FieldTypes> train3Select;
    private JPanel trainSelection;

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

    /**
     * Creates and paints the GUI.
     */
    public MetroGUI() {
        setTitle("Metro App");
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

        if (metro == null)
            metro = new SimulationModel(initialTrainOrder);
        if (tunnelsMapMonitor == null)
            tunnelsMapMonitor = metro.getMonitor();
        mapPanelType = new MapPanel(tunnelsMapMonitor);
        mapPanel = mapPanelType;


        train1Select = new JComboBox<>(initialTrainOrder);
        train1Select.setSelectedIndex(0);
        train2Select = new JComboBox<>(initialTrainOrder);
        train2Select.setSelectedIndex(1);
        train3Select = new JComboBox<>(initialTrainOrder);
        train3Select.setSelectedIndex(2);
    }

    private void addListeners() {
        startPauseButton.addActionListener(e -> {
            if (startPauseButton.getText().equals("Launch simulation")) {
                createNewSimulation();
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

        resetButton.addActionListener(e -> {
            // first we stop the model
            metro.end();
            // then we stop the thread that's updating the map
            isRunning.set(false);
            createNewSimulation();

            startPauseButton.setText("Launch simulation");
            startPauseButton.setToolTipText("Starts the new simulation");

            revalidate();
            repaint();
        });
    }

    /**
     * Creates new SimulationModel object based on current parameters
     */
    private void createNewSimulation() {
        metro = new SimulationModel(getTrainOrder());
        tunnelsMapMonitor = metro.getMonitor();
        mapPanelType.setTunnelsMapMonitor(tunnelsMapMonitor);
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
        mapPanel.revalidate();
        mapPanel.repaint();
        tunnelsMapMonitor.endPainting();
    }
}
