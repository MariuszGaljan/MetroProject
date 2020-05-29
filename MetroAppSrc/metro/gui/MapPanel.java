package metro.gui;

import metro.algorithm.map.Coordinates;
import metro.algorithm.map.FieldTypes;
import metro.algorithm.map.TunnelsMapMonitor;

import javax.swing.*;
import java.awt.*;

/**
 * Class used to show the tunnel's map state in the GUI.
 */
public class MapPanel extends JPanel {
    /**
     * Rows in the tunnel's map
     */
    private final int tileRows = 17;
    /**
     * Columns in the tunnel's map
     */
    private final int tileCols = 11;

    private final int preferredWidth = 816;
    private final int preferredHeight = 605;

    /**
     * Width of the panel
     */
    private int width = preferredWidth;
    /**
     * Height of the panel
     */
    private int height = preferredHeight;
    /**
     * Defines the distance between the tiles
     */
    private final int spacing = 5;

    TunnelsMapMonitor tunnelsMapMonitor;

    public MapPanel(TunnelsMapMonitor monitor) {
        super();
        tunnelsMapMonitor = monitor;
    }

    /**
     * Sets the monitor and repaints the JPanel
     * */
    public void setTunnelsMapMonitor(TunnelsMapMonitor monitor) {
        this.tunnelsMapMonitor = monitor;
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(preferredWidth, preferredHeight);
    }

    /**
     * Overrides the default method.
     * Paints the tiles, filling them with a color specified by field's type.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        width = getWidth();
        height = getHeight();
        this.setBackground(Color.DARK_GRAY);
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, width, height);

        int tileWidth = width / tileCols;
        int tileHeight = height / tileRows;

        for (int i = 0; i < tileCols; i++) {
            for (int j = 0; j < tileRows; j++) {
                FieldTypes field = tunnelsMapMonitor.getField(new Coordinates(j, i));
                setTileColor(g, field);
                g.fillRect(spacing + i * tileWidth, spacing + j * tileHeight, tileWidth - spacing * 2, tileHeight - spacing * 2);
            }
        }
    }

    /**
     * Sets the g's color accordingly to the field's type
     *
     * @param g     object to set the color
     * @param field evaluated field's type
     */
    private void setTileColor(Graphics g, FieldTypes field) {
        switch (field) {
            case WALL -> g.setColor(Color.GRAY);
            case STATION -> g.setColor(new Color(128, 0, 0)); //brown
            case T1 -> g.setColor(Color.CYAN);
            case T2 -> g.setColor(new Color(255, 204, 153));
            case T3 -> g.setColor(Color.YELLOW);
            default -> g.setColor(Color.WHITE);
        }
    }
}
