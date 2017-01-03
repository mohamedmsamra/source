package org.seamcat.presentation.systems;

import org.seamcat.model.cellular.CellularLayout;
import org.seamcat.model.core.GridPositionCalculator;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import static org.seamcat.mathematics.Constants.SQRT3;
import static org.seamcat.model.cellular.CellularLayout.SectorSetup.*;
import static org.seamcat.model.cellular.CellularLayout.SystemLayout.LeftHandSideOfNetworkEdge;
import static org.seamcat.model.cellular.CellularLayout.SystemLayout.RightHandSideOfNetworkEdge;

public class ReferenceCellSelector extends JPanel implements
        MouseMotionListener, MouseListener {

    private Color activeClusterBackgroundColor = new Color(125, 183, 255);
    private Stroke activeClusterStroke = new BasicStroke(2);

    private CellularPositionHolder model;

    private double center_translateX;
    private double center_translateY;

    private Color defaultCellColor = Color.BLUE;
    private CellUI mouseOverCell;

    private Color mouseOverColor = new Color(107, 232, 57);

    private Color outsideNetworkColor = new Color(145, 145, 145);
    private boolean plotCenterCross = false;
    private boolean plotWrapAround = true;
    private double scaleFactor;
    private Color selectedCellColor = new Color(255, 53, 53);
    private Color textColor = Color.BLACK;

    private double translateX;
    private double translateY;

    private Stroke wrapAroundStroke = new BasicStroke(1);

    public ReferenceCellSelector() {
        addMouseMotionListener(this);
        addMouseListener(this);
    }

    public void mouseClicked(MouseEvent e) {
        if (model == null) {
            return;
        }

        double x = (e.getX() - center_translateX) / scaleFactor;
        double y = (e.getY() - center_translateY) / scaleFactor * -1;
        for (int i = 0, stop = cells.length; i < stop; i++) {
            CellUI c = cells[i][0];
            if (c.isInside(new Point2D(x, y))) {
                int referenceCell = i;
                int refSector = 0;
                if (model.getCellularPosition().sectorType().equals(TriSector3GPP2)) {
                    double angle = Mathematics.calculateKartesianAngle(new Point2D(x, y), c.getPosition());
                    if (angle <= 120 && angle >= 0) {
                        // firstSector
                    } else if (angle <= 240 && angle >= 120) {
                        // second sector
                        refSector = 1;
                    } else if (angle <= 360 && angle >= 240) {
                        // third sector
                        refSector = 2;
                    }
                } else if (model.getCellularPosition().sectorType().equals(TriSector3GPP)) {
                    double angle = Mathematics.calculateKartesianAngle(new Point2D(x, y), c.getPosition());
                    if (angle <= 60 && angle >= 300) {
                        // firstSector
                    } else if (angle <= 180 && angle >= 60) {
                        // second sector
                        refSector = 1;
                    } else if (angle <= 300 && angle >= 180) {
                        // third sector
                        refSector = 2;
                    }
                }
                CellularPosition prototype = Factory.prototype(CellularPosition.class, model.getCellularPosition());
                Factory.when(prototype.referenceCellId()  ).thenReturn(referenceCell);
                Factory.when(prototype.referenceSector() ).thenReturn(refSector);
                model.setCellularPosition( Factory.build(prototype) );
                repaint();
                return;
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        if (model == null) {
            return;
        }
        double x = (e.getX() - center_translateX) / scaleFactor;
        double y = (e.getY() - center_translateY) / scaleFactor * -1;
        if (cells != null) {
            for (CellUI[] cell : cells) {
                CellUI c = cell[0];
                if (c.isInside(new Point2D(x, y))) {
                    if (model.getCellularPosition().sectorType().equals(TriSector3GPP2)) {
                        double angle = Mathematics.calculateKartesianAngle(new Point2D(x, y), c.getPosition());
                        if (angle <= 120 && angle >= 0) {
                            // firstSector
                            mouseOverCell = cell[0];
                        } else if (angle <= 240 && angle >= 120) {
                            // second sector
                            mouseOverCell = cell[1];
                        } else if (angle <= 360 && angle >= 240) {
                            // third sector
                            mouseOverCell = cell[2];
                        }
                    } else if (model.getCellularPosition().sectorType().equals(TriSector3GPP)) {
                        double angle = Mathematics.calculateKartesianAngle(new Point2D(x, y), c.getPosition());
                        if (angle <= 60 && angle >= 300) {
                            // firstSector
                            mouseOverCell = cell[0];
                        } else if (angle <= 180 && angle >= 60) {
                            // second sector
                            mouseOverCell = cell[1];
                        } else if (angle <= 300 && angle >= 180) {
                            // third sector
                            mouseOverCell = cell[2];
                        }
                    } else {
                        mouseOverCell = c;
                    }
                    repaint();
                    return;
                }
            }
        }
        mouseOverCell = null;
        repaint();
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseDragged(MouseEvent arg0) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public static double getInterCellDistance(CellularPosition model) {
        double interCellDistance = 0.0;

        if (!model.sectorType().equals(TriSector3GPP)){
            interCellDistance = model.cellRadius() * SQRT3;
        }else{
            interCellDistance = model.cellRadius() * 3;
        }
        return interCellDistance;
    }

    @Override
    public void paintComponent(Graphics _gr) {
        super.paintComponent(_gr);
        Graphics2D gr = (Graphics2D) _gr;
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        Dimension dim = this.getSize();
        double scaleFactorH, scaleFactorW;

        if (model == null) {
            model = new CellularPositionHolder();
        }

        double dist = getInterCellDistance(model.getCellularPosition());
        if (plotWrapAround) {
            scaleFactorH = dim.getHeight() / (15 * dist);
            scaleFactorW = dim.getWidth() / (6 * dist);
        } else {
            scaleFactorH = dim.getHeight() / (6 * dist);
            scaleFactorW = dim.getWidth() / (6 * dist);
        }

        scaleFactor = Math.min(scaleFactorH, scaleFactorW);

        double lx = 0;
        double ly = 0;

        translateX = dim.getWidth() / 2 - lx * scaleFactor;
        translateY = dim.getHeight() / 2 + ly * scaleFactor;

        center_translateX = dim.getWidth() / 2 - lx * scaleFactor;
        center_translateY = dim.getHeight() / 2 + ly * scaleFactor;

        CellularLayout.SystemLayout layout = model.getCellularPosition().layout();
        double radius = scaleFactor * model.getCellularPosition().cellRadius();

        double angle = 360 / 6;
        double d = 0.0;
        if(model.getCellularPosition().sectorType().equals(TriSector3GPP)){
            d = radius * 3;
        }else{
            d = radius * SQRT3;
        }


        int systemsToPlot = 1;
        if (plotWrapAround) {
            systemsToPlot = 7;
        }

        if (plotCenterCross) {
            gr.setColor(Color.BLACK);
            gr.drawLine(0, (int) center_translateY, (int) dim.getWidth(), (int) center_translateY);
            gr.drawLine((int) center_translateX, 0, (int) center_translateX, (int) dim.getHeight());
        }

        gr.translate(translateX, translateY);

        Color cellColor = defaultCellColor;
        if(!model.getCellularPosition().sectorType().equals(TriSector3GPP)){
            for (int systemID = systemsToPlot - 1; systemID >= 0; systemID--) {
                translateX = 0;
                translateY = 0;

                cellColor = defaultCellColor;

                switch (systemID) {
                    case 0: {
                        // Default case - center of system is center of component.
                        break;
                    }
                    case 1: {
                        if (layout == RightHandSideOfNetworkEdge) {
                            cellColor = outsideNetworkColor;
                        }
                        translateX += 4.5 * d / SQRT3;
                        translateY -= -3.5 * d ;
                        break;
                    }
                    case 2: {
                        if (layout == RightHandSideOfNetworkEdge) {
                            cellColor = outsideNetworkColor;
                        }
                        translateX += 3 * d / SQRT3 * 2 + 1.5 * d / SQRT3;
                        translateY -= d / 2;
                        break;
                    }
                    case 3: {
                        if (layout == RightHandSideOfNetworkEdge) {
                            cellColor = outsideNetworkColor;
                        }
                        translateX += 3 * d / SQRT3;
                        translateY -= 4 * d;
                        break;
                    }
                    case 4: {
                        if (layout == LeftHandSideOfNetworkEdge) {
                            cellColor = outsideNetworkColor;
                        }
                        translateX += -1.5 * d / SQRT3 + -3 * d / SQRT3;
                        translateY -= 2 * d + 1.5 * d;
                        break;
                    }
                    case 5: {
                        if (layout == LeftHandSideOfNetworkEdge) {
                            cellColor = outsideNetworkColor;
                        }
                        translateX += -1.5 * d / SQRT3 + -3 * d / SQRT3 * 2;
                        translateY -= -d / 2;
                        break;
                    }
                    case 6: {
                        if (layout == LeftHandSideOfNetworkEdge) {
                            cellColor = outsideNetworkColor;
                        }
                        translateX += -3 * d / SQRT3;
                        translateY -= -4 * d;
                        break;
                    }
                }
                if (cells != null) {

                    for (int i = 0; i < cells.length; i++) {
                        for (int k = 0; k < cells[i].length; k++) {
                            double cellX = cells[i][k].getPosition().getX();
                            double cellY = cells[i][k].getPosition().getY() * -1;

                            cellX *= scaleFactor;
                            cellY *= scaleFactor;

                            cellX += translateX;
                            cellY += translateY;

                            int[] xPoints = new int[6];
                            int[] yPoints = new int[6];
                            for (int j = 0; j < 6; j++) {
                                xPoints[j] = (int) (Mathematics.cosD((j * angle)) * radius + cellX);
                                yPoints[j] = (int) (Mathematics.sinD((j * angle)) * radius + cellY);
                            }
                            gr.setColor(cellColor);
                            if (systemID == 0) {
                                gr.setStroke(activeClusterStroke);
                            } else {
                                gr.setStroke(wrapAroundStroke);
                            }

                            for (int j = 0; j < 6; j++) {
                                gr.drawLine(xPoints[j], yPoints[j], (int) (Mathematics
                                        .cosD(((j + 1) * angle))
                                        * radius + cellX), (int) (Mathematics
                                        .sinD(((j + 1) * angle))
                                        * radius + cellY));
                                if (!(model.getCellularPosition().sectorType().equals(SingleSector)) && j % 2 == 0) {
                                    gr.drawLine((int) (Mathematics.cosD((j * angle))
                                            * radius + cellX), (int) (Mathematics
                                            .sinD((j * angle))
                                            * radius + cellY), (int) cellX, (int) cellY);
                                }
                            }
                            if (systemID == 0 && k == 0 && plotWrapAround) {
                                gr.setColor(activeClusterBackgroundColor);
                                gr.fillPolygon(xPoints, yPoints, xPoints.length);
                            }

                            if (systemID == 0 && cells[i][k] == mouseOverCell) {
                                gr.setColor(mouseOverColor);
                                if (!model.getCellularPosition().sectorType().equals(SingleSector)) {
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
                                } else {
                                    gr.fillPolygon(xPoints, yPoints, xPoints.length);
                                }
                            }

                            if (systemID == 0 && i == model.getCellularPosition().referenceCellId() && k == model.getCellularPosition().referenceSector()) {
                                gr.setColor(selectedCellColor);
                                if (!model.getCellularPosition().sectorType().equals(SingleSector)) {
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
                                } else {
                                    gr.fillPolygon(xPoints, yPoints, xPoints.length);
                                }
                            }
                            gr.setColor(textColor);
                            if (systemID == 0 && !plotWrapAround) {
                                gr.drawString("#" + i, (int) cellX, (int) cellY - 1);
                            }
                        }
                    }
                }
            }
        }else{ //3GPP grid
            for (int systemID = systemsToPlot - 1; systemID >= 0; systemID--) {
                translateX = 0;
                translateY = 0;

                cellColor = defaultCellColor;
                switch (systemID) {
                    case 0: {
                        // Default case - center of system is center of component.
                        break;
                    }
                    case 1: {
                        if (layout == RightHandSideOfNetworkEdge) {
                            cellColor = outsideNetworkColor;
                        }
                        translateX += 3.5*d ;
                        translateY -= 3 * d * SQRT3/2;
                        break;
                    }
                    case 2: {
                        if (layout == LeftHandSideOfNetworkEdge) {
                            cellColor = outsideNetworkColor;
                        }
                        translateX += -d/2;
                        translateY -= 5 * d * SQRT3/2;
                        break;
                    }
                    case 3: {
                        if (layout == LeftHandSideOfNetworkEdge) {
                            cellColor = outsideNetworkColor;
                        }
                        translateX -= 4*d ;
                        translateY -= d * SQRT3;
                        break;
                    }
                    case 4: {
                        if (layout == LeftHandSideOfNetworkEdge) {
                            cellColor = outsideNetworkColor;
                        }
                        translateX -= 3.5*d ;
                        translateY += d * 3 * SQRT3 /2 ;
                        break;
                    }
                    case 5: {
                        if (layout == RightHandSideOfNetworkEdge) {
                            cellColor = outsideNetworkColor;
                        }
                        translateX += d/2;
                        translateY += 5 * d * SQRT3/2;
                        break;
                    }
                    case 6: {
                        if (layout == RightHandSideOfNetworkEdge) {
                            cellColor = outsideNetworkColor;
                        }
                        translateX += 4*d ;
                        translateY += d * SQRT3 ;
                        break;
                    }
                }
                if (cells != null) {
                    double [] shiftX;
                    shiftX = new double[3];
                    shiftX[0] = radius;
                    shiftX[1] = -radius * Mathematics.cosD(angle);
                    shiftX[2] = -radius * Mathematics.cosD(angle);

                    double [] shiftY;
                    shiftY = new double[3];
                    shiftY[0] = 0;
                    shiftY[1] = -radius * Mathematics.sinD(angle);
                    shiftY[2] = radius * Mathematics.sinD(angle);

                    for (int i = 0; i < cells.length; i++) {
                        for (int k = 0; k < cells[i].length; k++) {
                            double cellX = cells[i][k].getPosition().getX();
                            double cellY = cells[i][k].getPosition().getY() * -1;

                            cellX *= scaleFactor;
                            cellY *= scaleFactor;

                            cellX += translateX;
                            cellY += translateY;

                            int[] xPoints = new int[6];
                            int[] yPoints = new int[6];
                            for (int j = 0; j < 6; j++) {
                                xPoints[j] = (int)((Mathematics.cosD((j * angle)) * radius) + cellX + shiftX[k]);
                                yPoints[j] = (int)((Mathematics.sinD((j * angle)) * radius) + cellY + shiftY[k]);
                            }
                            gr.setColor(cellColor);
                            if (systemID == 0) {
                                gr.setStroke(activeClusterStroke);
                            } else {
                                gr.setStroke(wrapAroundStroke);
                            }

                            for (int j = 0; j < 6; j++) {
                                int X = (int)((Mathematics.cosD(((j+1) * angle)) * radius) + cellX + shiftX[k]);
                                int Y = (int)((Mathematics.sinD(((j+1) * angle)) * radius) + cellY + shiftY[k]);
                                gr.drawLine(xPoints[j], yPoints[j],X,Y);
                            }
                            if (systemID == 0 && plotWrapAround) {
                                gr.setColor(activeClusterBackgroundColor);
                                gr.fillPolygon(xPoints, yPoints, xPoints.length);
                            }

                            if (systemID == 0 && cells[i][k] == mouseOverCell) {
                                gr.setColor(mouseOverColor);
                                gr.fillPolygon(xPoints, yPoints, xPoints.length);
                            }

                            if (systemID == 0 && i == model.getCellularPosition().referenceCellId() && k == model.getCellularPosition().referenceSector()) {
                                gr.setColor(selectedCellColor);
                                gr.fillPolygon(xPoints, yPoints, xPoints.length);
                            }
                            gr.setColor(textColor);
                            if (systemID == 0 && !plotWrapAround) {
                                gr.drawString("#" + i, (int) cellX, (int) cellY - 1);
                            }
                        }
                    }
                }
            }
        }
    }


    public void setPlotWrapAround(boolean plotWrapAround) {
        this.plotWrapAround = plotWrapAround;
        repaint();
    }

    public void generateCells() {
        cells = new CellUI[getNumberOfCellSitesInPowerControlCluster()][getNumberOfCellsPerSite()];
        for (int i1 = 0; i1 < cells.length; i1++) {
            for (int i = 0; i < cells[i1].length; i++) {
                cells[i1][i] = new CellUI();
            }
        }

        double dist = getInterCellDistance(model.getCellularPosition());
        Point2D p = new Point2D();
        for ( int j=0; j<cells.length; j++) {
            for (int i = 0; i < cells[0].length; i++) {
                if ( model.getCellularPosition().sectorType().equals(TriSector3GPP) ) {
                    cells[j][i].setPosition(GridPositionCalculator.standard(false, j, p, dist), dist);
                } else {
                    cells[j][i].setPosition(GridPositionCalculator.ppg2(false, j, p, dist), dist);
                }
            }
        }
    }

    private int getNumberOfCellsPerSite() {
        if ( model.getCellularPosition().sectorType().equals(SingleSector) ) return 1;
        return 3;
    }

    private int getNumberOfCellSitesInPowerControlCluster() {
        switch ( model.getCellularPosition().tiers()) {
            case 0: return 1;
            case 1: return 7;
            default: return 19;
        }
    }

    private CellUI[][] cells;

    public CellularPositionHolder getModel() {
        return model;
    }

    public void setModel(CellularPositionHolder model) {
        this.model = model;
    }
}
