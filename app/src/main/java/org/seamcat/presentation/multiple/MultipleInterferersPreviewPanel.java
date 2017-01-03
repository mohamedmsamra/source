package org.seamcat.presentation.multiple;

import org.apache.log4j.Logger;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;

import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static java.lang.String.format;

public class MultipleInterferersPreviewPanel extends JPanel {

	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);
	private static final Logger LOG = Logger.getLogger(MultipleInterferersPreviewPanel.class);
	
	public static enum LayoutType {
		Circular, Hexagon3GPP
	}
	
	private LayoutType layoutType;
	private double intersiteDistance;
	private int numberOfTiers;
	private double displacementAngle;
	private double offsetAngle;
	private int sizeOfFirstTier;
	
	private double centerX;
	private double centerY;
	
	private int width;
	private int height;
	private double scaleFactor;
	
	private double translateX;
	private double translateY;
	
	private boolean plottingGenerated;
	
	private Map<String, Point2D> victimReceivers;
	private Map<String, Point2D> interferingTransmitters;
	
	private int selectedItemZoomFactor = 0;
	private boolean victimIsDma;
	private double victimInterSiteDistance;
	private boolean victimIsUplink;
	private boolean generateRelativeToVictim = false;
	private Point2D vrCenter;
	
	private static final Color GREY = new Color(10, 10, 10, 25);
	
   private static final long serialVersionUID = 1L;
   
   private final Image it;
   private final Image wt;
   private final Image vr;
   
   private boolean forcePaint = false;
   
   private static final int MAX_POINTS_GRID_X = 250;
   private static final int MAX_POINTS_GRID_Y = 100;
   
   public MultipleInterferersPreviewPanel() {
   	super();
   	victimReceivers = new HashMap<String, Point2D>();
   	interferingTransmitters = new HashMap<String, Point2D>();
   	
   	it = getImage("/org/seamcat/presentation/resources/it.png");
   	wt = getImage("/org/seamcat/presentation/resources/wt.png");
   	vr = getImage("/org/seamcat/presentation/resources/vr.png");
   }
   
   private Image getImage(String file) {
   	byte[] bytes = null;
   	try {
      	InputStream iStream = getClass().getResourceAsStream(file);
      	bytes = new byte[iStream.available()];
      	iStream.read(bytes);
      	iStream.close();
   	}
   	catch (IOException e) { }
   	
   	return new ImageIcon(bytes).getImage();
   }
   
   @Override
   public void repaint() {
   	forcePaint = true;
   	super.repaint();
   }
   
	@Override
   protected void paintComponent(Graphics g) {
	   if (forcePaint) {
	   	forcePaint = false;
		   super.paintComponent(g);
	   	Graphics2D gr = (Graphics2D) g;
		   gr.setBackground(Color.WHITE);
		   prepare(gr);

		   drawCoordinateSystem(gr);
		   drawLegend(gr);
		   
		   drawExistingElements(gr);
		   if (isPlottingGenerated()) {
		   	drawGeneratedElements(gr);
		   }
	   }
   }

   private void drawGeneratedElements(Graphics2D gr) {
   	gr.setColor(Color.RED);
   	if (layoutType == LayoutType.Circular) {
   		drawCircularElements(gr);
   	} else if (layoutType == LayoutType.Hexagon3GPP) {
   		double x = getCenterX();
   		double y = -getCenterY();
   		double D = getIntersiteDistance();
   		
   		if (generateRelativeToVictim && vrCenter != null) {
   			x =  vrCenter.getX() + getCenterX();
   			y = -vrCenter.getY() - getCenterY();
   		}
   		
   		draw3GPPLayoutElements(gr,x,y,D, true);
   	}
   }


	private void draw3GPPLayoutElements(Graphics2D gr, double x, double y, double D, boolean animate) {
		drawImage(gr, it, x + 1.5*D/Math.sqrt(3), y + D / 2);
		drawImage(gr, it, x, y + D);
		drawImage(gr, it, x - 1.5*D / Math.sqrt(3),y + D / 2);
		drawImage(gr, it, x - 1.5*D / Math.sqrt(3),y - D / 2);
		drawImage(gr, it, x,y - D);
		drawImage(gr, it, x + 1.5*D/Math.sqrt(3),y - D / 2);
		if (getNumberOfTiers() > 1) {
			drawImage(gr, it, x + 3*D / Math.sqrt(3),y);
			drawImage(gr, it, x + 3*D / Math.sqrt(3),y + D);
			drawImage(gr, it, x + 1.5*D / Math.sqrt(3),y + 1.5*D);
			drawImage(gr, it, x, y + 2*D);
			drawImage(gr, it, x - 1.5*D / Math.sqrt(3),y + 1.5*D);
			drawImage(gr, it, x - 3*D / Math.sqrt(3),y + D);
			drawImage(gr, it, x - 3*D / Math.sqrt(3),y);
			drawImage(gr, it, x - 3*D / Math.sqrt(3),y - D);
			drawImage(gr, it, x - 1.5*D / Math.sqrt(3),y - 1.5*D);
			drawImage(gr, it, x,y - 2*D);
			drawImage(gr, it, x + 1.5*D / Math.sqrt(3),y - 1.5*D);
			drawImage(gr, it, x + 3*D / Math.sqrt(3),y - D);
		}
   }

	private void drawCircularElements(Graphics2D gr) {
	   drawRecursiveTiers(gr, 1);
   }


	private void drawRecursiveTiers(Graphics2D gr, int tierid) {
			if (tierid > getNumberOfTiers()) {
				//End recursive loop
				return;
			}
			
			double angleFromCenter = getDisplacementAngle() / tierid;			
			int numberOfSitesInThisTier = tierid * getSizeOfFirstTier();
	
			double drawCenterX = getCenterX();
   		double drawCenterY = -getCenterY();
   		
   		if (generateRelativeToVictim && vrCenter != null) {
   			drawCenterX =  vrCenter.getX() + getCenterX();
   			drawCenterY = -vrCenter.getY() - getCenterY();
   		}
			
			for (int i = 0; i < numberOfSitesInThisTier; i++) {
				double x = drawCenterX + (Mathematics.cosD(i * angleFromCenter + getOffsetAngle()) * getIntersiteDistance() * tierid);
				double y = drawCenterY - (Mathematics.sinD(i * angleFromCenter + getOffsetAngle()) * getIntersiteDistance() * tierid);
				
				drawImage(gr, it, x, y);
			}
			drawRecursiveTiers(gr, tierid + 1);
   }

	private void drawExistingElements(Graphics2D gr) {
		gr.setColor(Color.BLUE);
		
		if (victimIsDma) {
			if (!victimIsUplink) {
				gr.setColor(Color.GREEN);
			} 
			draw3GPPLayoutElements(gr, 0, 0, victimInterSiteDistance, false);
		} else {
			drawImage(gr, wt, translateX, translateY, false, true);
			
			if (!victimReceivers.isEmpty()) {
				gr.setColor(Color.GREEN);
				for (String name : victimReceivers.keySet()) {
					Point2D point = victimReceivers.get(name);
					
					double x = point.getX() * scaleFactor;
					double y = -point.getY() * scaleFactor;
					
					x += translateX;
					y += translateY;
					
					drawImage(gr, vr, x, y, false, true);
				}
			}
		}
		
		gr.setColor(Color.RED);
		
		double drawCenterX = getCenterX();
		double drawCenterY = -getCenterY();
		
		if (generateRelativeToVictim && vrCenter != null) {
			drawCenterX =  vrCenter.getX() + getCenterX();
			drawCenterY = -vrCenter.getY() - getCenterY();
		}
		
		double x = drawCenterX * scaleFactor;
		double y = drawCenterY * scaleFactor;
		
		x += translateX;
		y += translateY;
		
		drawImage(gr, it, x, y, false, true);
   }

	private void drawLegend(Graphics2D gr) {
		gr.setColor(Color.BLACK);
		double unitLen = scaleFactor * getIntersiteDistance();
		int unitXPos = (int)(width / 2 - width / 2 / unitLen * unitLen);
		gr.drawLine(unitXPos, height / 20, unitXPos + (int)unitLen, height / 20);
		gr.drawLine(unitXPos + (int)unitLen, height / 20 - 5, unitXPos + (int)unitLen, height / 20 + 5);
		gr.drawLine(unitXPos, height / 20 - 5, unitXPos, height / 20 + 5);
		gr.drawString("D = " + Mathematics.round(getIntersiteDistance()) + " km", (int) unitXPos + 30, (int) (height / 20) + 20);

		// Interfering Link Transmitter legend
		final int legendYText = height - 25;
		final int legendYImage = legendYText - 8;
		final int legendSpace = 5;
		int legendX = 25;
		final FontMetrics metrics = gr.getFontMetrics();
		final String itLabel = STRINGLIST.getString("MULTIPLE_LABEL_ILT");
		final String wtLabel = STRINGLIST.getString("MULTIPLE_LABEL_VLT");
		final String vrLabel = STRINGLIST.getString("MULTIPLE_LABEL_VLR");

		gr.drawString(STRINGLIST.getString("MULTIPLE_LABEL_ILT"), legendX, legendYText);
		legendX += metrics.stringWidth(itLabel) + legendSpace;
		drawImage(gr, it, legendX, legendYImage, false, false);
		legendX += it.getWidth(null) + legendSpace * 2;

		// Victim Link Transmitter legend
		gr.drawString(STRINGLIST.getString("MULTIPLE_LABEL_VLT"), legendX, legendYText);
		legendX += metrics.stringWidth(wtLabel) + legendSpace;
		drawImage(gr, wt, legendX, legendYImage, false, false);
		legendX += wt.getWidth(null) + legendSpace * 2;

		// Victim Link Receiver legend
		gr.drawString(STRINGLIST.getString("MULTIPLE_LABEL_VLR"), legendX, legendYText);
		legendX += metrics.stringWidth(vrLabel) + legendSpace;
		drawImage(gr, vr, legendX, legendYImage, false, false);
	}

	private void drawCoordinateSystem(Graphics2D gr) {
	   gr.setColor(Color.BLACK);

	   int xCenter = width / 2;
	   int yCenter = height / 2;

	   // Y axis
	   gr.drawLine(xCenter, 0, xCenter, height);

	   // X axis
	   gr.drawLine(0, yCenter, width, yCenter);

	   // Grid X
	   final int gridPixels = 3;
	   final int gridLen = 2;
	   final int gridSpace = 1;
	   final double displacePixels = scaleFactor * getIntersiteDistance();
	   int yNegative = yCenter - gridPixels;
	   int yPositive = yCenter + gridPixels;
	   
	   double xFactor = -1d;
	   if (xCenter/displacePixels > MAX_POINTS_GRID_X) {
	   	xFactor = xCenter/displacePixels / MAX_POINTS_GRID_X;
	   }
	   
	   int skippedX = 0;
	   for (double xDisplace = displacePixels, nextXdisplace = displacePixels; xDisplace < xCenter; xDisplace += displacePixels) {
	   	// Skip this line (for performance)
	   	if (xFactor != -1d) {
		   	if (xDisplace < nextXdisplace) {
		   		skippedX++;
		   		continue;
		   	}
		   	else {
		   		nextXdisplace = xDisplace + (xFactor * displacePixels);
		   	}
	   	}

	   	// Grid
	   	int xNegative = (int)(xCenter - xDisplace);
	   	int xPositive = (int)(xCenter + xDisplace);

	   	gr.setColor(GREY);
	   	for (int y = 0, z = gridLen + gridSpace + 1; y < height; y += z) {
	   		int yLen = y + gridLen;
	   		gr.drawLine(xNegative, y, xNegative, yLen);
		   	gr.drawLine(xPositive, y, xPositive, yLen);
	   	}

	   	// Center
	   	gr.setColor(Color.BLACK);
	   	gr.drawLine(xNegative, yNegative, xNegative, yPositive);
	   	gr.drawLine(xPositive, yNegative, xPositive, yPositive);
	   }

        if (LOG.isDebugEnabled()) {
            LOG.debug(format("Skipped %d X points", skippedX));
        }
	   
	   // Grid Y
	   int xNegative = xCenter - gridPixels;
	   int xPositive = xCenter + gridPixels;

	   double yFactor = -1d;
	   if (yCenter/displacePixels > MAX_POINTS_GRID_Y) {
	   	yFactor = yCenter/displacePixels / MAX_POINTS_GRID_Y;
	   }

	   int skippedY = 0;
	   for (double yDisplace = displacePixels, nextYdisplace = displacePixels; yDisplace < yCenter; yDisplace += displacePixels) {
	   	// Skip this line (for performance)
	   	if (yFactor != -1d) {
		   	if (yDisplace < nextYdisplace) {
		   		skippedY++;
		   		continue;
		   	}
		   	else {
		   		nextYdisplace = yDisplace + (yFactor * displacePixels);
		   	}
	   	}

	   	// Grid
	   	yNegative = (int)(yCenter - yDisplace);
	   	yPositive = (int)(yCenter + yDisplace);

	   	gr.setColor(GREY);
	   	for (int x = 0, z = gridLen + gridSpace + 1; x < width; x += z) {
	   		int xLen = x + gridLen;
		   	gr.drawLine(x, yNegative, xLen, yNegative);
		   	gr.drawLine(x, yPositive, xLen, yPositive);
	   	}

	   	// Center
	   	gr.setColor(Color.BLACK);
	   	gr.drawLine(xNegative, yNegative, xPositive, yNegative);
	   	gr.drawLine(xNegative, yPositive, xPositive, yPositive);
	   }

        if (LOG.isDebugEnabled()) {
            LOG.debug(format("Skipped %d Y points", skippedY));
        }

	}

	private void prepare(Graphics2D gr) {
		gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Dimension dim = this.getSize();
		
		height = (int) dim.getHeight();
		width = (int) dim.getWidth();

		double distance = Math.sqrt((centerX*centerX) + (centerY*centerY));
		
		double vrDist = 0;
		
		for (Point2D p : victimReceivers.values()) {
			vrDist = Math.max(Mathematics.distance(new Point2D(0,0), new Point2D(p.getX(),p.getY())), vrDist);
		}
		
		double d = Math.max(((1.5 * (getNumberOfTiers() + 2)) * getIntersiteDistance()), 2*vrDist);
		
		double scaleFactorH = height / (d + (2*distance));
		double scaleFactorW = width / (d + (2*distance));

		scaleFactor = Math.min(scaleFactorH, scaleFactorW);
		
		translateX = (width / 2);
		translateY = (height / 2);

		gr.clearRect(0, 0, width, height);
		
   }

	private void drawImage(Graphics2D gr, Image i, double x, double y) {
		drawImage(gr, i, x, y, true, true);
	}
	private void drawImage(Graphics2D gr, Image i, double x, double y, boolean scaleTranslate, boolean center) {
		if (scaleTranslate) {
			x *= scaleFactor;
			y *= scaleFactor;
			
			x += translateX;
			y += translateY;
		}
		if (center) {
			x -= i.getWidth(null) / 2;
			y -= i.getHeight(null) / 2;
		}

		gr.drawImage(i, (int)x, (int)y, (ImageObserver)null);
	}
	
	public void addVictimReceiver(String name, double x, double y) {
		victimReceivers.put(name, new Point2D(x,y));
		vrCenter = new Point2D(x,y);
	}
	
	public void addInterferingTransmitter(String name, double x, double y) {
		interferingTransmitters.put(name, new Point2D(x,y));
	}
	
	
	public void reset() {
		victimReceivers.clear();
		interferingTransmitters.clear();
	}
	
	public LayoutType getLayoutType() {
   	return layoutType;
   }

	
   public void setLayoutType(LayoutType layoutType) {
   	this.layoutType = layoutType;
   	forcePaint = true;
   }

	
   public double getIntersiteDistance() {
   	return intersiteDistance;
   }

	
   public void setIntersiteDistance(double intersiteDistance) {
   	this.intersiteDistance = intersiteDistance;
   	forcePaint = true;
   }

	
   public int getNumberOfTiers() {
   	return numberOfTiers;
   }

	
   public void setNumberOfTiers(int numberOfTiers) {
   	this.numberOfTiers = numberOfTiers;
   	forcePaint = true;
   }

	
   public double getDisplacementAngle() {
   	return displacementAngle;
   }

	
   public void setDisplacementAngle(double displacementAngle) {
   	this.displacementAngle = displacementAngle;
   	forcePaint = true;
   }

	
   public double getOffsetAngle() {
   	return offsetAngle;
   }

	
   public void setOffsetAngle(double offsetAngle) {
   	this.offsetAngle = offsetAngle;
   	forcePaint = true;
   }

	
   public int getSizeOfFirstTier() {
   	return sizeOfFirstTier;
   }

	
   public void setSizeOfFirstTier(int sizeOfFirstTier) {
   	this.sizeOfFirstTier = sizeOfFirstTier;
   	forcePaint = true;
   }


	
   public double getCenterX() {
   	return centerX;
   }


	
   public void setCenterX(double centerX) {
   	this.centerX = centerX;
   	forcePaint = true;
   }


	
   public double getCenterY() {
   	return centerY;
   }


	
   public void setCenterY(double centerY) {
   	this.centerY = centerY;
   	forcePaint = true;
   }


	
   public boolean isPlottingGenerated() {
   	return plottingGenerated;
   }


	
   public void setPlotGenerated(boolean plottingGenerated) {
   	this.plottingGenerated = plottingGenerated;
   }

	
	public void setDmaVictim(boolean isDma, boolean isUplink, double interCellDistance) {
	   victimIsDma = isDma;
	   if (isDma) {
	   	victimIsUplink = isUplink;
	   	victimInterSiteDistance = interCellDistance;
	   }
   	forcePaint = true;
   }

   public void setGenerateRelativeToVictim(boolean generateRelativeToVictim) {
   	this.generateRelativeToVictim = generateRelativeToVictim;
   	forcePaint = true;
   }

}
