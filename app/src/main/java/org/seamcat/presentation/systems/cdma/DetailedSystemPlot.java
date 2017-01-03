package org.seamcat.presentation.systems.cdma;

import org.apache.log4j.Logger;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.Animator.RepeatBehavior;
import org.jdesktop.animation.timing.interpolation.PropertySetter;
import org.seamcat.dmasystems.AbstractDmaBaseStation;
import org.seamcat.dmasystems.AbstractDmaLink;
import org.seamcat.dmasystems.AbstractDmaMobile;
import org.seamcat.model.cellular.CellularLayout;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.plugin.OptionalFunction;
import org.seamcat.model.plugin.antenna.HorizontalVerticalInput;
import org.seamcat.model.simulation.result.Interferer;
import org.seamcat.ofdma.DownlinkOfdmaMobile;
import org.seamcat.ofdma.OfdmaExternalInterferer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetailedSystemPlot extends JComponent {

    private static String[][] fixedLocationsStrings;

    private static final Logger LOG = Logger.getLogger(DetailedSystemPlot.class);

    static {
        fixedLocationsStrings = new String[19][2];
        // ////
        fixedLocationsStrings[0][0] = "x";
        fixedLocationsStrings[0][1] = "y";

        fixedLocationsStrings[1][0] = "x + 1.5*D/Math.sqrt(3)";
        fixedLocationsStrings[1][1] = "y + D / 2";

        fixedLocationsStrings[2][0] = "x";
        fixedLocationsStrings[2][1] = "y + D";

        fixedLocationsStrings[3][0] = "x - 1.5*D / Math.sqrt(3)";
        fixedLocationsStrings[3][1] = "y + D / 2";

        fixedLocationsStrings[4][0] = "x - 1.5*D / Math.sqrt(3)";
        fixedLocationsStrings[4][1] = "y - D / 2";

        fixedLocationsStrings[5][0] = "x";
        fixedLocationsStrings[5][1] = "y - D";

        fixedLocationsStrings[6][0] = "x + 1.5*D/Math.sqrt(3)";
        fixedLocationsStrings[6][1] = "y - d / 2";

        fixedLocationsStrings[7][0] = "x + 3*D / Math.sqrt(3)";
        fixedLocationsStrings[7][1] = "y";

        fixedLocationsStrings[8][0] = "x + 3*D / Math.sqrt(3)";
        fixedLocationsStrings[8][1] = "y + D";

        fixedLocationsStrings[9][0] = "x + 1.5*D / Math.sqrt(3)";
        fixedLocationsStrings[9][1] = "y + 1.5*D";

        fixedLocationsStrings[10][0] = "x";
        fixedLocationsStrings[10][1] = "y + 2*D";

        fixedLocationsStrings[11][0] = "x - 1.5*D / Math.sqrt(3)";
        fixedLocationsStrings[11][1] = "y + 1.5*D";

        fixedLocationsStrings[12][0] = "x - 3*D / Math.sqrt(3)";
        fixedLocationsStrings[12][1] = "y + D";

        fixedLocationsStrings[13][0] = "x - 3*D / Math.sqrt(3)";
        fixedLocationsStrings[13][1] = "y";

        fixedLocationsStrings[14][0] = "x - 3*D / Math.sqrt(3)";
        fixedLocationsStrings[14][1] = "y - D";

        fixedLocationsStrings[15][0] = "x - 1.5*D / Math.sqrt(3)";
        fixedLocationsStrings[15][1] = "y - 1.5*D";

        fixedLocationsStrings[16][0] = "x";
        fixedLocationsStrings[16][1] = "y - 2*D";

        fixedLocationsStrings[17][0] = "x + 1.5*D / Math.sqrt(3)";
        fixedLocationsStrings[17][1] = "y - 1.5*D";

        fixedLocationsStrings[18][0] = "x + 3*D / Math.sqrt(3)";
        fixedLocationsStrings[18][1] = "y - D";

    }

    protected Animator animator;

    private int focusShiftX = 0;

    private int focusShiftY = 0;

    private Color patternColor = new Color(240, 193, 193);

    private boolean plotActivelistSize = false;

    private boolean plotAntennaPattern = false;

    private boolean plotCellBackground = false;

    private boolean plotCellCenter = true;

    private boolean plotCellid = false;

    private boolean plotConnectionLines = false;

    private boolean plotDroppedUsers = true;

    private boolean plotExternalInterferers = true;

    private boolean plotFixedLocations = false;

    private boolean plotHelp = true;

    private boolean plotLegend = true;

    private boolean plotScale = true;

    private boolean plotTxStats = false;

    private boolean plotUsers = false;

    protected PropertySetter prop;

    private double scaleFactor;

    private AbstractDmaBaseStation selectedCell;

    private Interferer selectedInterferer;

    private int selectedItemZoomFactor = 0;

    private AbstractDmaLink selectedLink;

    private AbstractDmaMobile selectedUser;

    private String tooltip;

    private Point tooltipDestination;

    private double translateX;

    private double translateY;

    private double transX;
    private double transY;

    private double zoomFactor = 1;
    private CDMAPlotModel model;

    public DetailedSystemPlot() {
        super();
        prop = new PropertySetter(this, "selectedItemZoomFactor", 0, 4);

    }

    public void adjustFocusShiftX(int _focusShiftX) {
        this.focusShiftX += _focusShiftX;
    }

    public void adjustFocusShiftY(int _focusShiftY) {
        this.focusShiftY += _focusShiftY;
    }

    public void adjustZoom(double adjustment) {

        double adjust = adjustment / 100;
        zoomFactor += adjust;

        if (zoomFactor < 0) {
            zoomFactor = 0;
        }

    }

    protected void drawAnimatedOval(Graphics2D gr, Object elementBeingDrawed, Object selectedElement, double x, double y, int diameter) {
        if (elementBeingDrawed == selectedElement) {
            gr.fillOval((int) x - (diameter / 2 + selectedItemZoomFactor), (int) y
                    - (diameter / 2 + selectedItemZoomFactor), diameter
                    + selectedItemZoomFactor * 2, diameter + selectedItemZoomFactor
                    * 2);
        } else {
            gr.fillOval((int) x - diameter / 2, (int) y - diameter / 2, diameter, 6);
        }
    }

    public CDMAPlotModel getModel() {
        return model;
    }

    public int getFocusShiftX() {
        return focusShiftX;
    }

    public int getFocusShiftY() {
        return focusShiftY;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public AbstractDmaBaseStation getSelectedCell() {
        return selectedCell;
    }

    public int getSelectedItemZoomFactor() {
        return selectedItemZoomFactor;
    }

    public AbstractDmaLink getSelectedLink() {
        return selectedLink;
    }

    public AbstractDmaMobile getSelectedUser() {
        return selectedUser;
    }

    public double getTranslateX() {
        return translateX;
    }

    public double getTranslateY() {
        return translateY;
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public void hideToolTip() {
        tooltip = null;
    }

    public boolean isPlotAntennaPattern() {
        return plotAntennaPattern;
    }

    public boolean isPlotCellBackground() {
        return plotCellBackground;
    }

    public boolean isPlotCellCenter() {
        return plotCellCenter;
    }

    public boolean isPlotCellid() {
        return plotCellid;
    }

    public boolean isPlotConnectionLines() {
        return plotConnectionLines;
    }

    public boolean isPlotDroppedUsers() {
        return plotDroppedUsers;
    }

    /**
     * @return Returns the plotExternalInterferers.
     */
    public boolean isPlotExternalInterferers() {
        return plotExternalInterferers;
    }

    public boolean isPlotFixedLocations() {
        return plotFixedLocations;
    }

    public boolean isPlotHelp() {
        return plotHelp;
    }

    public boolean isPlotLegend() {
        return plotLegend;
    }

    public boolean isPlotScale() {
        return plotScale;
    }

    public boolean isPlotSizeOfActiveList() {
        return plotActivelistSize;
    }

    public boolean isPlotTxStats() {
        return plotTxStats;
    }

    public boolean isPlotUsers() {
        return plotUsers;
    }

    public void moveToolTipToRelativeDestination(Point destination) {
        tooltipDestination = destination;
    }

    @Override
    public void paintComponent(Graphics _gr) {
        Graphics2D gr = (Graphics2D) _gr;
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setBackground(Color.WHITE);

        Dimension dim = this.getSize();

        if (model != null) {

            CellularSystem system = model.getCellularSystem();
            CellularLayout.TierSetup setup = system.getLayout().getTierSetup();
            int tiers;
            if ( setup == CellularLayout.TierSetup.SingleCell ) {
                tiers = 0;
            } else {
                tiers = setup == CellularLayout.TierSetup.OneTier ? 1 : 2;
            }
            double intercellDistance = model.intercellDistance;
            double scaleFactorH = dim.getHeight() / ((2 * tiers + 2) * intercellDistance);
            double scaleFactorW = dim.getWidth() / ((2 * tiers + 2) * intercellDistance);

            scaleFactor = Math.min(scaleFactorH, scaleFactorW);

            scaleFactor *= zoomFactor;

            gr.clearRect(0, 0, (int) dim.getWidth(), (int) dim.getHeight());
            if (plotScale) {
                gr.drawLine((int) dim.getWidth() / 20, (int) dim.getHeight() / 20,(int) (dim.getWidth() / 20 + scaleFactor* intercellDistance), (int) dim.getHeight() / 20);
                gr.drawLine((int) (dim.getWidth() / 20 + scaleFactor* intercellDistance),(int) dim.getHeight() / 20 - 5,(int) (dim.getWidth() / 20 + scaleFactor* intercellDistance), (int) dim.getHeight() / 20 + 5);
                gr.drawLine((int) dim.getWidth() / 20,(int) dim.getHeight() / 20 - 5, (int) dim.getWidth() / 20,(int) dim.getHeight() / 20 + 5);
                gr.drawString("D = " + Mathematics.round(intercellDistance)+ " km", (int) (dim.getWidth() / 20) + 30, (int) (dim.getHeight() / 20) + 20);
            }
            if (plotLegend) {
                int line = 1;

                gr.setColor(Color.RED);
                gr.fillOval((int) (dim.getWidth() / 20) + 30, (int) (dim.getHeight() / 20 + 14 + line * gr.getFont().getSize() * 1.5), 4, 4);
                gr.drawString(" = voice active user",(int) (dim.getWidth() / 20) + 35,(int) (dim.getHeight() / 20 + 20 + line * gr.getFont().getSize() * 1.5));

                if (system.getCDMASettings() != null) {
                    line++;
                    gr.setColor(Color.ORANGE);
                    gr.fillOval((int) (dim.getWidth() / 20) + 30, (int) (dim.getHeight() / 20 + 14 + line * gr.getFont().getSize() * 1.5), 4, 4);
                    gr.drawString(" = voice active user in softhandover", (int) (dim.getWidth() / 20) + 35, (int) (dim.getHeight() / 20 + 20 + line * gr.getFont().getSize() * 1.5));
                }

                line++;
                gr.setColor(Color.GRAY);
                gr.fillOval((int) (dim.getWidth() / 20) + 30, (int) (dim.getHeight() / 20 + 14 + line * gr.getFont().getSize() * 1.5),4, 4);
                gr.drawString(" = dropped user",(int) (dim.getWidth() / 20) + 35, (int) (dim.getHeight() / 20 + 20 + line * gr.getFont().getSize() * 1.5));

                line++;
                gr.setColor(Color.MAGENTA);
                gr.fillOval((int) (dim.getWidth() / 20) + 30, (int) (dim.getHeight() / 20 + 14 + line * gr.getFont().getSize() * 1.5),4, 4);
                gr.drawString(" = external interferer",(int) (dim.getWidth() / 20) + 35, (int) (dim.getHeight() / 20 + 20 + line * gr.getFont().getSize() * 1.5));
            }

            if (plotHelp) {
                gr.setColor(Color.BLUE);
                int textOffset = 0;
                gr.fillOval((int) (dim.getWidth() / 20) + 30, (int) (dim.getHeight() - (dim.getHeight() / 20 + textOffset + gr.getFont().getSize() * 1.5)), 4, 4);
                gr.drawString("Click on element to see details", (int) (dim.getWidth() / 20) + 38, (int) (dim.getHeight() - (dim.getHeight()/ 20 + textOffset - 6 + 5 * gr.getFont().getSize() * 1.5)));

                gr.fillOval((int) (dim.getWidth() / 20) + 30, (int) (dim.getHeight() - (dim.getHeight() / 20 + textOffset + 2 * gr.getFont().getSize() * 1.5)), 4, 4);
                gr.drawString("Zoom using mousewheel or slider", (int) (dim.getWidth() / 20) + 38, (int) (dim.getHeight() - (dim.getHeight()/ 20 + textOffset - 6 + 4 * gr.getFont().getSize() * 1.5)));

                gr.fillOval((int) (dim.getWidth() / 20) + 30, (int) (dim.getHeight() - (dim.getHeight() / 20 + textOffset + 3 * gr.getFont().getSize() * 1.5)), 4, 4);
                gr.drawString("Grab and drag to recenter",(int) (dim.getWidth() / 20) + 38,(int) (dim.getHeight() - (dim.getHeight() / 20 + textOffset- 6 + 3 * gr.getFont().getSize() * 1.5)));

                gr.fillOval((int) (dim.getWidth() / 20) + 30, (int) (dim.getHeight() - (dim.getHeight() / 20 + textOffset + 4 * gr.getFont().getSize() * 1.5)), 4, 4);
                gr.drawString("Double Right click to reset to 100% zoom",(int) (dim.getWidth() / 20) + 38,(int) (dim.getHeight() - (dim.getHeight() / 20 + textOffset- 6 + 2 * gr.getFont().getSize() * 1.5)));

                gr.fillOval((int) (dim.getWidth() / 20) + 30, (int) (dim.getHeight() - (dim.getHeight() / 20 + textOffset + 5 * gr.getFont().getSize() * 1.5)), 4, 4);
                gr.drawString("Select user and Ctrl-click any BS to see link data",(int) (dim.getWidth() / 20) + 38,(int) (dim.getHeight() - (dim.getHeight() / 20 + textOffset- 6 + gr.getFont().getSize() * 1.5)));
            }

            double radius = scaleFactor * system.getLayout().getCellRadius();

            Point2D location = model.getLocation();
            double lx = location.getX();
            double ly = location.getY();

            transX = dim.getWidth() / 2 - lx * scaleFactor;
            transY = dim.getHeight() / 2 + ly * scaleFactor;

            translateX = transX;
            translateY = transY;

            gr.translate(translateX, translateY);

            AbstractDmaBaseStation[][] cells = model.getBaseStations();
            double angle = 360 / 6;

            List<?> users = model.getActiveUsers();

            if (system.getLayout().getSectorSetup() == CellularLayout.SectorSetup.TriSector3GPP2){
                double cellX = model.getReferenceCell().getPosition().getX();
                double cellY = -model.getReferenceCell().getPosition().getY();

                cellX *= scaleFactor;
                cellY *= scaleFactor;

                cellX += focusShiftX;
                cellY += focusShiftY;

                int[] xPoints = new int[6];
                int[] yPoints = new int[6];

                for (int j = 0; j < 6; j++) {
                    xPoints[j] = (int) (Mathematics.cosD((j * angle)) * radius + cellX);
                    yPoints[j] = (int) (Mathematics.sinD((j * angle)) * radius + cellY);
                }

                gr.setColor(Color.YELLOW);

                int k = model.getReferenceCell().getSectorId()-1;
                int[] xP = new int[4];
                int[] yP = new int[4];
                xP[0] = (int) cellX;
                yP[0] = (int) cellY;
                switch (k) {
                    case 2: {
                        for (int zz = 0; zz < 3; zz++) {
                            xP[zz + 1] = xPoints[zz];
                            yP[zz + 1] = yPoints[zz];
                        }
                        break;
                    }
                    case 1: {
                        for (int zz = 2; zz < 5; zz++) {
                            xP[zz - 1] = xPoints[zz];
                            yP[zz - 1] = yPoints[zz];
                        }
                        break;
                    }
                    case 0: {
                        for (int zz = 4; zz < 6; zz++) {
                            xP[zz - 3] = xPoints[zz];
                            yP[zz - 3] = yPoints[zz];
                        }
                        xP[3] = xPoints[0];
                        yP[3] = yPoints[0];
                        break;
                    }
                }
                gr.fillPolygon(xP, yP, xP.length);

            } else if (system.getLayout().getSectorSetup() == CellularLayout.SectorSetup.SingleSector){
                double cellX = model.getReferenceCell().getPosition().getX();
                double cellY = -model.getReferenceCell().getPosition().getY();

                cellX *= scaleFactor;
                cellY *= scaleFactor;

                cellX += focusShiftX;
                cellY += focusShiftY;

                int[] xPoints = new int[6];
                int[] yPoints = new int[6];

                for (int j = 0; j < 6; j++) {
                    xPoints[j] = (int) (Mathematics.cosD((j * angle)) * radius + cellX );
                    yPoints[j] = (int) (Mathematics.sinD((j * angle)) * radius + cellY );
                }

                gr.setColor(Color.YELLOW);

                gr.fillPolygon(xPoints, yPoints, xPoints.length);
            } else {
                double cellX = model.getReferenceCell().getPosition().getX();
                double cellY = -model.getReferenceCell().getPosition().getY();

                cellX *= scaleFactor;
                cellY *= scaleFactor;

                cellX += focusShiftX;
                cellY += focusShiftY;

                int[] xPoints = new int[6];
                int[] yPoints = new int[6];

                int sector = model.getReferenceCell().getSectorId()-1;
                double [] shiftX;
                shiftX = new double[3];
                shiftX[0] = radius;
                shiftX[1] = -radius * Mathematics.cosD(60);
                shiftX[2] = -radius * Mathematics.cosD(60);

                double [] shiftY;
                shiftY = new double[3];
                shiftY[0] = 0;
                shiftY[1] = -radius * Mathematics.sinD(60);
                shiftY[2] = radius * Mathematics.sinD(60);

                for (int j = 0; j < 6; j++) {
                    xPoints[j] = (int) (Mathematics.cosD((j * angle)) * radius + cellX + shiftX[sector]);
                    yPoints[j] = (int) (Mathematics.sinD((j * angle)) * radius + cellY + shiftY[sector]);
                }

                gr.setColor(Color.YELLOW);

                gr.fillPolygon(xPoints, yPoints, xPoints.length);

            }


            if (selectedCell != null) {
                double cellX = selectedCell.getPosition().getX();
                double cellY = -selectedCell.getPosition().getY();

                cellX *= scaleFactor;
                cellY *= scaleFactor;

                cellX += focusShiftX;
                cellY += focusShiftY;

                if (plotAntennaPattern) {
                    if (selectedCell.getAntennaGain() != null ) {
                        Object conf = selectedCell.getAntennaGain().getConfiguration();
                        if ( conf instanceof HorizontalVerticalInput ) {
                            drawHorizontal(true, gr, (HorizontalVerticalInput) conf, radius, cellX, cellY);
                        }
                    }
                }
            }

            if (plotExternalInterferers) {
                List<Interferer> ext = model.getExternalInterferers();
                gr.setColor(Color.MAGENTA);
                for (int i = 0, stop = ext.size(); i < stop; i++) {
                    Interferer e = ext.get(i);
                    double extX = e.getPoint().getX();
                    double extY = -e.getPoint().getY();

                    extX *= scaleFactor;
                    extY *= scaleFactor;

                    extX += focusShiftX;
                    extY += focusShiftY;

                    drawAnimatedOval(gr, e, selectedInterferer, extX, extY, 6);

                    if (e == selectedInterferer) {

                        if (plotAntennaPattern) {
                            if (e.getAntennaGain() != null ) {
                                Object conf = e.getAntennaGain().getConfiguration();
                                if ( conf instanceof HorizontalVerticalInput ) {
                                    drawHorizontal(false, gr, (HorizontalVerticalInput) conf, radius, extX, extY);
                                }
                            }
                        }
                        if (selectedUser != null && !system.isUpLink()) {
                            gr.setColor(Color.RED);

                            double userX = selectedUser.getPosition().getX();
                            double userY = -selectedUser.getPosition().getY();

                            userX *= scaleFactor;
                            userY *= scaleFactor;

                            userX += focusShiftX;
                            userY += focusShiftY;

                            double cX = e.getPoint().getX();
                            double cY = -e.getPoint().getY();

                            cX *= scaleFactor;
                            cY *= scaleFactor;

                            cX += focusShiftX;
                            cY += focusShiftY;

                            gr.drawLine((int) userX, (int) userY, (int) cX, (int) cY);
                        } else if (system.isUpLink() && selectedCell != null) {
                            gr.setColor(Color.RED);

                            double cellX = selectedCell.getPosition().getX();
                            double cellY = -selectedCell.getPosition().getY();

                            cellX *= scaleFactor;
                            cellY *= scaleFactor;

                            cellX += focusShiftX;
                            cellY += focusShiftY;

                            double cX = e.getPoint().getX();
                            double cY = -e.getPoint().getY();

                            cX *= scaleFactor;
                            cY *= scaleFactor;

                            cX += focusShiftX;
                            cY += focusShiftY;

                            gr.drawLine((int) cellX, (int) cellY, (int) cX, (int) cY);

                        }
                        gr.setColor(Color.MAGENTA);
                    }
                }
            }

            for (int i = 0; (i < users.size()) && plotUsers; i++) {
                AbstractDmaMobile user = (AbstractDmaMobile) users.get(i);
                if (user == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("User is null");
                    }
                    continue;
                }

                if (user.isInSoftHandover() && system.getCDMASettings() != null) {
                    gr.setColor(Color.ORANGE);
                } else {
                    gr.setColor(Color.RED);
                }

                double userX = user.getPosition().getX();
                double userY = -user.getPosition().getY();

                userX *= scaleFactor;
                userY *= scaleFactor;

                userX += focusShiftX;
                userY += focusShiftY;

                gr.fillOval((int) userX - 2, (int) userY - 2, 4, 4);
                if (plotActivelistSize) {
                    gr.drawString(Integer.toString(user.getActiveList().size()), (int) userX + 6, (int) userY);
                }

                for (int j = 0; (j < user.getActiveList().size())
                        && plotConnectionLines; j++) {

                    gr.setColor(Color.LIGHT_GRAY);
                    if (user.isInSoftHandover() && system.isUpLink()) {
                        if ((user.getActiveList().get(j) == user.getServingLink()) || user.isInSofterHandover()) {
                            gr.setColor(Color.DARK_GRAY);
                        }
                    }

                    double cellX = ((AbstractDmaLink) user.getActiveList().get(j)).getBaseStation().getPosition().getX();
                    double cellY = -((AbstractDmaLink) user.getActiveList().get(j)).getBaseStation().getPosition().getY();

                    cellX *= scaleFactor;
                    cellY *= scaleFactor;

                    cellX += focusShiftX;
                    cellY += focusShiftY;

                    gr.drawLine((int) userX, (int) userY, (int) cellX, (int) cellY);
                }

            }
            if (selectedUser != null) {
                gr.setColor(Color.BLACK);

                double userX = selectedUser.getPosition().getX();
                double userY = -selectedUser.getPosition().getY();

                userX *= scaleFactor;
                userY *= scaleFactor;

                userX += focusShiftX;
                userY += focusShiftY;
                // gr.drawOval((int) userX - 7, (int) userY - 7, 13, 13);
                gr.drawString("#" + selectedUser.getUserId() + " (" + selectedUser.getActiveList().size() + ")", (int) userX + 6, (int) userY);
                gr.setColor(Color.BLUE);
                drawAnimatedOval(gr, selectedUser, selectedUser, userX, userY, 4);
                gr.setColor(Color.LIGHT_GRAY);
                for (int j = 0; j < selectedUser.getActiveList().size(); j++) {
                    gr.setColor(Color.LIGHT_GRAY);
                    if (selectedUser.getActiveList().get(j) == selectedUser.getServingLink()) {
                        gr.setColor(Color.DARK_GRAY);
                    }

                    double cellX = ((AbstractDmaLink) selectedUser.getActiveList().get(j)).getBaseStation().getPosition().getX();
                    double cellY = -((AbstractDmaLink) selectedUser.getActiveList().get(j)).getBaseStation().getPosition().getY();

                    cellX *= scaleFactor;
                    cellY *= scaleFactor;

                    cellX += focusShiftX;
                    cellY += focusShiftY;

                    gr.drawLine((int) userX, (int) userY, (int) cellX, (int) cellY);
                }

                if (isPlotExternalInterferers()&& selectedUser instanceof DownlinkOfdmaMobile) {
                    gr.setColor(Color.RED);

                    DownlinkOfdmaMobile mobile = (DownlinkOfdmaMobile) selectedUser;

                    for (OfdmaExternalInterferer ext : mobile.getExternalInterferers()) {
                        double cX = ext.getInterferer().getPoint().getX();
                        double cY = -ext.getInterferer().getPoint().getY();

                        cX *= scaleFactor;
                        cY *= scaleFactor;

                        cX += focusShiftX;
                        cY += focusShiftY;

                        gr.drawLine((int) userX, (int) userY, (int) cX, (int) cY);
                    }
                }

            }
            if (selectedCell != null) {
                java.util.List<AbstractDmaLink> connections = selectedCell.getOldTypeActiveConnections();
                double cellX = selectedCell.getPosition().getX();
                double cellY = -selectedCell.getPosition().getY();

                cellX *= scaleFactor;
                cellY *= scaleFactor;

                cellX += focusShiftX;
                cellY += focusShiftY;

                gr.setColor(Color.BLACK);
                // gr.drawOval((int) cellX - 9, (int) cellY - 9, 16, 16);

                for (int i = 0; i < connections.size(); i++) {

                    AbstractDmaMobile user = connections.get(i).getUserTerminal();

                    if (user == null) {
                        continue;
                    }

                    if (user == selectedUser) {
                        gr.setColor(Color.BLUE);
                    } else {
                        gr.setColor(Color.RED);
                    }

                    double userX = user.getPosition().getX();
                    double userY = -user.getPosition().getY();

                    userX *= scaleFactor;
                    userY *= scaleFactor;

                    userX += focusShiftX;
                    userY += focusShiftY;

                    gr.fillOval((int) userX - 2, (int) userY - 2, 4, 4);
                    if (plotActivelistSize) {
                        gr.drawString(Integer.toString(user.getActiveList().size()),(int) userX + 6, (int) userY);
                    }

                    gr.setColor(Color.LIGHT_GRAY);
                    for (int j = 0; j < user.getActiveList().size(); j++) {
                        gr.setColor(Color.LIGHT_GRAY);
                        if (user.isInSoftHandover() && system.isUpLink()) {
                            if ((user.getActiveList().get(j) == user.getServingLink()) || user.isInSofterHandover()) {
                                gr.setColor(Color.DARK_GRAY);
                            }
                        }

                        double cX = user.getActiveList().get(j).getBaseStation().getPosition().getX();
                        double cY = -user.getActiveList().get(j).getBaseStation().getPosition().getY();

                        cX *= scaleFactor;
                        cY *= scaleFactor;

                        cX += focusShiftX;
                        cY += focusShiftY;

                        gr.drawLine((int) userX, (int) userY, (int) cX, (int) cY);
                    }

                    gr.drawLine((int) userX, (int) userY, (int) cellX, (int) cellY);
                }

                for (AbstractDmaLink user : selectedCell.getDroppedUsers()) {
                    gr.setColor(Color.GRAY);

                    double userX = user.getUserTerminal().getPosition().getX();
                    double userY = -user.getUserTerminal().getPosition().getY();

                    userX *= scaleFactor;
                    userY *= scaleFactor;

                    userX += focusShiftX;
                    userY += focusShiftY;
                    if (plotActivelistSize) {
                        gr.drawString(Integer.toString(user.getUserTerminal().getActiveList().size()), (int) userX + 6, (int) userY);
                    }
                    gr.fillOval((int) userX - 2, (int) userY - 2, 4, 4);
                }
            }

            for (int i = 0; (i < cells.length) && plotDroppedUsers; i++) {
                for (int j = 0; j < cells[i].length; j++) {
                    for (AbstractDmaLink user : cells[i][j].getDroppedUsers()) {
                        if ((user.getUserTerminal().getDropReason() != null) && user.getUserTerminal().getDropReason().equals("Inside Power Balance")) {
                            gr.setColor(Color.LIGHT_GRAY);
                        } else {
                            gr.setColor(Color.GRAY);
                        }
                        double userX = user.getUserTerminal().getPosition().getX();
                        double userY = -user.getUserTerminal().getPosition().getY();

                        userX *= scaleFactor;
                        userY *= scaleFactor;

                        userX += focusShiftX;
                        userY += focusShiftY;
                        if (plotActivelistSize) {
                            gr.drawString(Integer.toString(user.getUserTerminal().getActiveList().size()), (int) userX + 6,(int) userY);
                        }

                        int diameter = 4;

                        gr.fillOval((int) userX - diameter / 2, (int) userY - diameter / 2, diameter, diameter);
                    }
                }
            }

            if (selectedLink != null) {
                AbstractDmaMobile user = selectedLink.getUserTerminal();

                gr.setColor(Color.BLUE);

                double userX = user.getPosition().getX();
                double userY = -user.getPosition().getY();

                userX *= scaleFactor;
                userY *= scaleFactor;

                userX += focusShiftX;
                userY += focusShiftY;

                drawAnimatedOval(gr, user, user, userX, userY, 4);

                gr.setColor(Color.GREEN);
                double cX = selectedLink.getBaseStation().getPosition().getX();
                double cY = -selectedLink.getBaseStation().getPosition().getY();

                cX *= scaleFactor;
                cY *= scaleFactor;

                cX += focusShiftX;
                cY += focusShiftY;

                gr.drawLine((int) userX, (int) userY, (int) cX, (int) cY);
            }

            if (selectedLink != null) {
                AbstractDmaMobile user = selectedLink.getUserTerminal();

                gr.setColor(Color.BLUE);

                double userX = user.getPosition().getX();
                double userY = -user.getPosition().getY();

                userX *= scaleFactor;
                userY *= scaleFactor;

                userX += focusShiftX;
                userY += focusShiftY;

                drawAnimatedOval(gr, user, user, userX, userY, 4);

                gr.setColor(Color.GREEN);
                double cX = selectedLink.getBaseStation().getPosition().getX();
                double cY = -selectedLink.getBaseStation().getPosition().getY();

                cX *= scaleFactor;
                cY *= scaleFactor;

                cX += focusShiftX;
                cY += focusShiftY;

                gr.drawLine((int) userX, (int) userY, (int) cX, (int) cY);
            }

            double [] shiftX;
            shiftX = new double[3];
            shiftX[0] = radius;
            shiftX[1] = -radius * Mathematics.cosD(60);
            shiftX[2] = -radius * Mathematics.cosD(60);

            double [] shiftY;
            shiftY = new double[3];
            shiftY[0] = 0;
            shiftY[1] = -radius * Mathematics.sinD(60);
            shiftY[2] = radius * Mathematics.sinD(60);

            for (int i = 0; i < cells.length; i++) {
                int k = 0;

                double cellX = cells[i][k].getPosition().getX();
                double cellY = -cells[i][k].getPosition().getY();

                cellX *= scaleFactor;
                cellY *= scaleFactor;

                cellX += focusShiftX;
                cellY += focusShiftY;

                gr.setColor(Color.BLUE);
                int[] xPoints = new int[6];
                int[] yPoints = new int[6];

                for (int j = 0; j < 6; j++) {
                    int x = 0;
                    int y = 0;
                    if(system.getLayout().getSectorSetup() == CellularLayout.SectorSetup.TriSector3GPP2){
                        xPoints[j] = (int) (Mathematics.cosD((j * angle)) * radius + cellX);
                        yPoints[j] = (int) (Mathematics.sinD((j * angle)) * radius + cellY);

                        x = (int) (Mathematics.cosD(((j + 1) * angle)) * radius + cellX);
                        y = (int) (Mathematics.sinD(((j + 1) * angle)) * radius + cellY);

                        gr.drawLine(xPoints[j], yPoints[j], x, y);

                        if (j % 2 == 0) {
                            gr.drawLine((int) (Mathematics.cosD((j * angle)) * radius + cellX),(int) (Mathematics.sinD((j * angle)) * radius + cellY),(int) cellX, (int) cellY);
                        }
                    }else{
                        if (system.getLayout().getSectorSetup() != CellularLayout.SectorSetup.SingleSector) {
                            for (int sector = 0 ; sector < 3 ; sector++){
                                xPoints[j] = (int) (Mathematics.cosD((j * angle)) * radius + cellX + shiftX[sector]);
                                yPoints[j] = (int) (Mathematics.sinD((j * angle)) * radius + cellY + shiftY[sector]);

                                x = (int) (Mathematics.cosD(((j + 1) * angle)) * radius + cellX + shiftX[sector]);
                                y = (int) (Mathematics.sinD(((j + 1) * angle)) * radius + cellY + shiftY[sector]);

                                gr.drawLine(xPoints[j], yPoints[j], x, y);
                            }
                        }
                    }

                }

                gr.setColor(Color.GREEN);
                if (plotCellCenter) {
                    drawAnimatedOval(gr, cells[i][k], selectedCell, cellX, cellY, 6);
                }

                gr.setColor(Color.BLACK);
                if (plotCellid) {
                    if (system.getLayout().getSectorSetup() != CellularLayout.SectorSetup.SingleSector) {
                        gr.drawString("BS #" + cells[i][0].getCellid() + ":",(int) (cellX + radius / 5), (int) (cellY - gr.getFont().getSize() * 1.5));
                        gr.drawString("BS #" + cells[i][1].getCellid() + ":",(int) (cellX - 2 * radius / 3), (int) cellY);gr.drawString("BS #" + cells[i][2].getCellid() + ":",(int) (cellX + radius / 5), (int) (cellY + gr.getFont().getSize() * 1.5));

                    } else {
                        gr.drawString("Cell #" + cells[i][0].getCellid() + ":",(int) (cellX - 2 * radius / 3), (int) (cellY - gr.getFont().getSize() * 1.5));
                    }
                }
                if (plotTxStats) {
                    if (system.isUpLink()) {
                        gr.drawString("Itotal: "+ Mathematics.round(cells[i][0].getTotalInterference())+ " dBm", (int) (cellX - 2 * radius / 3), (int) cellY);
                        if ( system.getCDMASettings() != null ) {
                            gr.drawString("NoiseRise: "+ Math.rint(cells[i][0].calculateNoiseRiseOverThermalNoise_dB() * 1000)/ 1000 + " dB", (int) (cellX - 2 * radius / 3),(int) (cellY + gr.getFont().getSize() * 1.5));
                        }

                    } else {
                        gr.drawString("TX: "+ Math.rint(cells[i][0].getCurrentTransmitPower_dBm() * 1000)/ 1000 + " dBm", (int) (cellX - 2 * radius / 3),(int) cellY);

                    }
                    gr.drawString("Users: " + cells[i][0].countServedUsers(),(int) (cellX - 2 * radius / 3), (int) (cellY + gr.getFont().getSize() * 3));
                }
                if (plotFixedLocations) {
                    gr.drawString("X: " + fixedLocationsStrings[i][0], (int) cellX,(int) (cellY - 1.5 * gr.getFont().getSize()));
                    gr.drawString("Y: " + fixedLocationsStrings[i][1], (int) cellX,(int) cellY);
                }
            }

            if ((tooltip != null) && (tooltipDestination != null)) {
                double toolX = tooltipDestination.getX();
                double toolY = -tooltipDestination.getY();

                toolX *= scaleFactor;
                toolY *= scaleFactor;

                toolX += focusShiftX;
                toolY += focusShiftY;

                gr.drawString(tooltip, (int) toolX, (int) toolY);
            }

        } else {
            gr.setFont(getFont().deriveFont(20f));
            String msg = "Run a simulation to display the cellular grid";
            gr.drawString(msg, (int) (dim.getWidth() / 2 - gr.getFontMetrics().getStringBounds(msg, gr).getWidth() / 2), (int) (dim.getHeight() / 2));
        }
    }

    private void drawHorizontal(boolean main, Graphics2D gr, HorizontalVerticalInput input, double radius, double extX, double extY) {
        OptionalFunction horizontal = input.horizontal();
        if ( horizontal.isRelevant() ) {
            int[] xPatternPoints;
            int[] yPatternPoints;
            java.util.List<Point2D> points = new ArrayList<>(horizontal.getFunction().getPoints());

            double antennadirection = 0; //e.getAntennaDirectionToZeroDegReference();
            double antennaAzimuth = 0; //-e.getAntennaAzimuth();

            Collections.sort(points, Point2D.POINTY_COMPARATOR);

            double min = points.get(0).getY();
            double max = points.get(points.size() - 1).getY();
            Collections.sort(points, Point2D.POINTX_COMPARATOR);
            double diff = Math.abs(max - min);

            xPatternPoints = new int[points.size()];
            yPatternPoints = new int[points.size()];

            gr.setColor(Color.RED);

            for (int z = 0; z < points.size(); z++) {
                Point2D po1 = points.get(z);

                double distFactor = radius * (radius - radius / diff * Math.abs(po1.getY())) / radius;

                int p1 = (int) (Mathematics.cosD(points.get(z).getX() + antennaAzimuth + antennadirection) * distFactor + extX);
                int p2 = (int) (-(Mathematics.sinD(points.get(z).getX() + antennaAzimuth + antennadirection) * distFactor) + extY);

                xPatternPoints[z] = p1;
                yPatternPoints[z] = p2;
            }

            if ( main ) {
                gr.setPaint(new RadialGradientPaint(new Point2D(extX, extY), new Float(radius).floatValue(), new float[]{0.0f, 1.0f}, new Color[]{Color.RED, patternColor.brighter()}, MultipleGradientPaint.CycleMethod.NO_CYCLE));
                gr.fillPolygon(xPatternPoints, yPatternPoints,xPatternPoints.length);
                gr.setColor(Color.RED);
                gr.drawPolygon(xPatternPoints, yPatternPoints,xPatternPoints.length);
            } else {
                gr.setColor(patternColor);
                gr.fillPolygon(xPatternPoints, yPatternPoints,xPatternPoints.length);
                gr.setColor(Color.RED);
                gr.drawPolygon(xPatternPoints, yPatternPoints, xPatternPoints.length);
            }
        }
    }

    public void resetView() {
        PropertySetter.createAnimator(2000, this, "focusShiftX",getFocusShiftX(), 0).start();
        PropertySetter.createAnimator(2000, this, "focusShiftY",getFocusShiftY(), 0).start();
        PropertySetter.createAnimator(2000, this, "zoomFactor", getZoomFactor(), 1.0).start();

    }

    public void setModel(CDMAPlotModel model) {
        this.model = model;
    }

    public void setFocusShiftX(int focusShiftX) {
        this.focusShiftX = focusShiftX;
        repaint();
    }

    public void setFocusShiftY(int focusShiftY) {
        this.focusShiftY = focusShiftY;
        repaint();
    }

    public void setPlotAntennaPattern(boolean plotAntennaPattern) {
        this.plotAntennaPattern = plotAntennaPattern;
        repaint();
    }

    public void setPlotCellBackground(boolean plotCellBackground) {
        this.plotCellBackground = plotCellBackground;
        repaint();
    }

    public void setPlotCellCenter(boolean plotCellCenter) {
        this.plotCellCenter = plotCellCenter;
        repaint();
    }

    public void setPlotCellid(boolean plotCellid) {
        this.plotCellid = plotCellid;
        repaint();
    }

    public void setPlotConnectionLines(boolean plotConnectionLines) {
        this.plotConnectionLines = plotConnectionLines;
        repaint();
    }

    public void setPlotDroppedUsers(boolean plotDroppedUsers) {
        this.plotDroppedUsers = plotDroppedUsers;
        repaint();
    }

    public void setPlotExternalInterferers(boolean plotExternalInterferers) {
        this.plotExternalInterferers = plotExternalInterferers;
        repaint();
    }

    public void setPlotFixedLocations(boolean plotFixedLocations) {
        this.plotFixedLocations = plotFixedLocations;
        repaint();
    }

    public void setPlotHelp(boolean plotHelp) {
        this.plotHelp = plotHelp;
        repaint();
    }

    public void setPlotLegend(boolean plotLegend) {
        this.plotLegend = plotLegend;
        repaint();
    }

    public void setPlotScale(boolean plotScale) {
        this.plotScale = plotScale;
    }

    public void setPlotSizeOfActiveList(boolean plotUserID) {
        this.plotActivelistSize = plotUserID;
        repaint();
    }

    public void setPlotTxStats(boolean plotTxStats) {
        this.plotTxStats = plotTxStats;
        repaint();
    }

    public void setPlotUsers(boolean plotUsers) {
        this.plotUsers = plotUsers;
        repaint();
    }

    public void setSelectedCell(AbstractDmaBaseStation selectedCell) {
        this.selectedCell = selectedCell;
        if (this.selectedCell != null) {
            if (animator != null) {
                animator.cancel();
            } else {
                animator = new Animator(200, Animator.INFINITE,
                        RepeatBehavior.REVERSE, prop);
            }
            animator.start();
        }
    }

    public void setSelectedInterferer(Interferer inter) {
        this.selectedInterferer = inter;
        if (this.selectedInterferer != null) {
            if (animator != null) {
                animator.cancel();
            } else {
                animator = new Animator(200, Animator.INFINITE,
                        RepeatBehavior.REVERSE, prop);
            }
            animator.start();
        }
    }

    public void setSelectedItemZoomFactor(int selectedItemZoomFactor) {
        this.selectedItemZoomFactor = selectedItemZoomFactor;
        repaint();
    }

    public void setSelectedLink(AbstractDmaLink selectedLink) {
        this.selectedLink = selectedLink;
    }

    public void setSelectedUser( AbstractDmaMobile selectedUser) {
        this.selectedUser = selectedUser;
        if (this.selectedUser != null) {
            if (animator != null) {
                animator.cancel();
            } else {
                animator = new Animator(200, Animator.INFINITE,
                        RepeatBehavior.REVERSE, prop);
            }
            animator.start();
        }
    }

    @Override
    public void setToolTipText(String text) {
        tooltip = text;
    }

    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
        repaint();
    }
}