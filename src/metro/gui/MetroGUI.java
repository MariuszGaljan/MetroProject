package metro.gui;

import metro.algorithm.SimulationModel;
import metro.algorithm.map.TunnelsMapMonitor;

import javax.swing.*;
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
    private AtomicBoolean isRunning = new AtomicBoolean(false);

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
        if (metro == null)
            metro = new SimulationModel();
        if (tunnelsMapMonitor == null)
            tunnelsMapMonitor = metro.getMonitor();
        mapPanelType = new MapPanel(tunnelsMapMonitor);
        mapPanel = mapPanelType;
    }

    private void addListeners() {
        startPauseButton.addActionListener(e -> {
            if (startPauseButton.getText().equals("Start simulation")) {
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

                startPauseButton.setText("Stop after train arrives");
                startPauseButton.setToolTipText("Stops after train arrives at the station.");
            } else {
                metro.pause();
                startPauseButton.setText("Start simulation");
                startPauseButton.setToolTipText("Starts the simulation");
            }
        });

        resetButton.addActionListener(e -> {
            // first we stop the model
            metro.end();
            // then we stop the thread that's updating the map
            isRunning.set(false);
            // then we create a new model
            metro = new SimulationModel();
            tunnelsMapMonitor = metro.getMonitor();
            mapPanelType.setTunnelsMapMonitor(tunnelsMapMonitor);

            startPauseButton.setText("Start simulation");
            startPauseButton.setToolTipText("Starts the simulation");

            revalidate();
            repaint();
        });
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
