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

    private final int preferredWidth = 856;
    private final int preferredHeight = 652;

    /**
     * Specifies the width of the column with labels of rows
     */
    private final int rowLabelWidth = 20;
    /**
     * Specifies the height of the row with labels of columns
     */
    private final int colLabelHeight = 20;

    private int tileWidth;
    private int tileHeight;

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

    /**
     * Reference to the tunnel's map monitor the mapPanel will draw
     *
     * @see TunnelsMapMonitor
     */
    TunnelsMapMonitor tunnelsMapMonitor;

    /**
     * @param monitor Reference to the tunnel's map monitor the mapPanel will draw
     */
    public MapPanel(TunnelsMapMonitor monitor) {
        super();
        tunnelsMapMonitor = monitor;
    }

    /**
     * Sets the monitor and repaints the JPanel
     *
     * @param monitor Reference to the tunnel's map monitor the mapPanel will draw
     */
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

        tileWidth = (width - rowLabelWidth) / tileCols;
        tileHeight = (height - colLabelHeight) / tileRows;

        drawLabels(g);

        for (int i = 0; i < tileCols; i++) {
            for (int j = 0; j < tileRows; j++) {
                FieldTypes field = tunnelsMapMonitor.getField(new Coordinates(j, i));
                setTileColor(g, field);
                g.fillRect(spacing + i * tileWidth + rowLabelWidth, spacing + j * tileHeight + colLabelHeight, tileWidth - spacing * 2, tileHeight - spacing * 2);
            }
        }
    }

    /**
     * Draws the row and column indexes
     */
    private void drawLabels(Graphics g) {
        g.setColor(Color.WHITE);
        // drawing row labels
        for (int i = 1; i <= tileRows; i++)
            g.drawString("" + (i - 1), 1, i * tileHeight - 2 * spacing + colLabelHeight);
        // drawing column labels
        g.setColor(Color.WHITE);
        for (int i = 1; i <= tileCols; i++)
            g.drawString("" + (i - 1), i * tileWidth - spacing - rowLabelWidth, colLabelHeight);
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
