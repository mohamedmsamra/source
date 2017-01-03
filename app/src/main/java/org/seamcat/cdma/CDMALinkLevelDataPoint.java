package org.seamcat.cdma;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CDMALinkLevelDataPoint {

	private static int geometryPrecission = 1;
	private double ecior;
	private double frequency;
	private double geometry;
	private int path;

	private double speed;

	public CDMALinkLevelDataPoint(CDMALinkLevelDataPoint pt) {
		this.frequency = pt.frequency;
		this.path = pt.path;
		this.geometry = pt.geometry;
		this.speed = pt.speed;
		this.ecior = pt.ecior;
	}

	public CDMALinkLevelDataPoint(double frequency, int path, double geometry,
	      double speed, double ecior) {
		this.frequency = frequency;
		this.path = path;
		this.geometry = geometry;
		this.speed = speed;
		this.ecior = ecior;
	}

	public CDMALinkLevelDataPoint(Element element, double frequency, int path) {
		try {
			geometry = Double.parseDouble(element.getAttribute("geo"));
		} catch (NumberFormatException e) {
		}
		try {
			speed = Double.parseDouble(element.getAttribute("speed"));
		} catch (NumberFormatException e) {
		}

		try {
			ecior = Double.parseDouble(element.getAttribute("ecior"));
		} catch (NumberFormatException e) {
		}

		this.frequency = frequency;
		this.path = path;

	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CDMALinkLevelDataPoint)) {
			return false;
		}
		CDMALinkLevelDataPoint p = (CDMALinkLevelDataPoint) obj;
		return getPath() == p.getPath()
		      && (int) getFrequency() == (int) p.getFrequency()
		      && (int) getSpeed() == (int) p.getSpeed()
		      && getGeometry() == p.getGeometry();
	}

	/** Uplink scenario */
	public double getEbNo() {
		// Returns same value as getEcIor()
		return ecior;
	}

	/** Downlink scenario */
	public double getEcIor() {
		return ecior;
	}

	public double getFrequency() {
		return frequency;
	}

	public double getGeometry() {
		double geo = Math.rint(geometry * geometryPrecission)
		      / geometryPrecission;
		return geo;
	}

	public int getPath() {
		return path;
	}

	public double getSpeed() {
		return speed;
	}

	public void setEcior(double ecior) {
		this.ecior = ecior;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

	public void setGeometry(double geometry) {
		this.geometry = geometry;
	}

	public void setPath(int path) {
		this.path = path;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public Element toElement(Document doc) {
		Element element = doc.createElement("point");
		element.setAttribute("geo", String.valueOf(geometry));
		element.setAttribute("speed", String.valueOf(speed));
		element.setAttribute("ecior", Double.toString(ecior));
		return element;
	}

	@Override
	public String toString() {
		return frequency + "MHz, " + path + "-path, " + (int) geometry
		      + "dB Geometry, " + speed + "km/h";
	}

}
