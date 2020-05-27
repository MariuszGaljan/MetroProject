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
    private JButton startPauseButton;
    private JButton resetButton;

    /**
     * The monitor of the tunnel's map.
     */
    private TunnelsMapMonitor tunnelsMapMonitor;

    private SimulationModel metro;

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
        mapPanel = new MapPanel(tunnelsMapMonitor);
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
                            System.out.println(isRunning.get());

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

//        resetButton.addActionListener(e -> {
//            metro.end();
//            isRunning.set(false);
//            metro = new SimulationModel();
//            tunnelsMapMonitor = metro.getMonitor();
//            startPauseButton.setText("Start simulation");
//            startPauseButton.setToolTipText("Starts the simulation");
//            revalidate();
//            repaint();
//        });
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
