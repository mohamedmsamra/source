package org.seamcat.cdma;

import org.apache.log4j.Logger;
import org.seamcat.function.MutableLibraryItem;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;
import org.seamcat.presentation.systems.cdma.CDMAEditModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.table.TableModel;
import java.util.*;

public class CDMALinkLevelData extends MutableLibraryItem {

	public enum LinkType {
		DOWNLINK, UPLINK
	}

	public enum TargetERType {
		BLER, FER
	}

	public static final Comparator<CDMALinkLevelDataPoint> DATAPOINT_COMPARATOR_ASCENDING_GEOMETRY = new Comparator<CDMALinkLevelDataPoint>() {

		public int compare(CDMALinkLevelDataPoint p1, CDMALinkLevelDataPoint p2) {
			return -DATAPOINT_COMPARATOR_DESCENDING_GEOMETRY.compare(p1, p2);
		}
	};

	public static final Comparator<CDMALinkLevelDataPoint> DATAPOINT_COMPARATOR_DESCENDING_GEOMETRY = new Comparator<CDMALinkLevelDataPoint>() {

		public int compare(CDMALinkLevelDataPoint p1, CDMALinkLevelDataPoint p2) {
			double result;
			if (p1.getPath() != p2.getPath()) {
				result = p2.getPath() - p1.getPath();
			} else if (p1.getGeometry() != p2.getGeometry()) {
				result = p2.getGeometry() > p1.getGeometry() ? 1 : -1;
			} else if ((int) p1.getSpeed() != (int) p2.getSpeed()) {
				result = (int) (p2.getSpeed() - p1.getSpeed());
			} else {
				result = 0;
			}
			return (int) Math.rint(result);
		}
	};

	private static final Logger LOG = Logger.getLogger(CDMALinkLevelData.class);

	// The maximum Ec/Ior requirement (requires half of received signals to be traffic)
	public static final double MAX_EC_IOR = 0;
	private static final int MAX_PATH_VALUE = 2;


	public static final double[] SPEED_VALUES = new double[] { 0.0, 3.0, 30.0,100.0 };
	private int currentPath = 1;
	// defined points corresponds to the function definition for the link level data
	private List<CDMALinkLevelDataPoint> definedPoints = new ArrayList<CDMALinkLevelDataPoint>();
	// data points corresponds to the defined points and additional calculated points
	private List<CDMALinkLevelDataPoint> dataPoints = new ArrayList<CDMALinkLevelDataPoint>();
	private String description;
	private double frequency;
	private double initialMinGeometry;
	private double initialMaxGeometry;

	private LinkType linkType = LinkType.DOWNLINK;
	private String name;
	private String[] pathDescription = new String[MAX_PATH_VALUE + 1];

	private boolean sorted = false;

	private String source;

	private String system;

	private String targetERpct;

	private TargetERType targetERType;

	/** Creates a new instance of CDMALinkLevelData */
	public CDMALinkLevelData() {
		setPathDescription(1, "");
		setPathDescription(2, "");
		setFrequency(0.0);
		setSystem("");
		setSource("");
		setTargetERpct("1");
		setTargetERType(TargetERType.FER);
        initialMinGeometry = -6;
        initialMaxGeometry = 12;
		sortAll();
	}

	public CDMALinkLevelData(CDMALinkLevelData tmp) {
		this();
		if (tmp != null) {
			this.currentPath = tmp.currentPath;
			this.description = tmp.description;
			this.frequency = tmp.frequency;
			this.name = tmp.name;
			this.source = tmp.source;
			this.system = tmp.system;
			this.targetERpct = tmp.targetERpct;
			this.targetERType = tmp.targetERType;
			this.linkType = tmp.linkType;

			System.arraycopy(tmp.pathDescription, 1, pathDescription, 1, MAX_PATH_VALUE);
	
			for (CDMALinkLevelDataPoint point : tmp.definedPoints) {
				initialMinGeometry = Math.min( initialMinGeometry, point.getGeometry() );
				initialMaxGeometry = Math.max( initialMaxGeometry, point.getGeometry() );
				definedPoints.add(new CDMALinkLevelDataPoint(point));
				dataPoints.add(new CDMALinkLevelDataPoint(point));
			}
			sortAll();
		} 
	}

	public CDMALinkLevelData(Element element) {
		this();

		String linkType = element.getAttribute("system-type");
		if (linkType.equals("uplink")) {
			this.linkType = LinkType.UPLINK;
		} else if (linkType.equals("downlink")) {
			this.linkType = LinkType.DOWNLINK;
		} else {
			throw new IllegalArgumentException(
			      "system-type must be uplink or downlink (" + linkType + ")");
		}

		source = element.getAttribute("source");
		system = element.getAttribute("system");
		frequency = Double.parseDouble(element.getAttribute("frequency"));
		targetERpct = element.getAttribute("targetPct");

		String targetERType = element.getAttribute("targetType");
		if (targetERType.equals(TargetERType.BLER.toString())) {
			this.targetERType = TargetERType.BLER;
		} else if (targetERType.equals(TargetERType.FER.toString())) {
			this.targetERType = TargetERType.FER;
		} else {
			throw new IllegalArgumentException(
			      "targetERType must be FER or BLER (" + this.targetERType + ")");
		}

		NodeList paths = element.getElementsByTagName("path");
		for (int i = 0, stop = paths.getLength(); i < stop; i++) {
			Element path = (Element) paths.item(i);
			int pathNo = Integer.parseInt(path.getAttribute("no"));
			setPathDescription(pathNo, path.getAttribute("caption"));
			NodeList points = path.getElementsByTagName("point");
			for (int j = 0, _stop = points.getLength(); j < _stop; j++) {
				CDMALinkLevelDataPoint point = new CDMALinkLevelDataPoint(
				      (Element) points.item(j), frequency, pathNo);
				dataPoints.add( point );
				definedPoints.add( point );
			}
		}
		sortAll();
	}

	private void sortAll() {
		Collections.sort(dataPoints, DATAPOINT_COMPARATOR_DESCENDING_GEOMETRY);
		Collections.sort(definedPoints, DATAPOINT_COMPARATOR_DESCENDING_GEOMETRY);
		sorted = true;
	}
	
	@Override
	public boolean equals(Object o) {
		return o != null && o instanceof CDMALinkLevelData && toString().equals(
                o.toString());
	}

	public int getCurrentPath() {
		return currentPath;
	}

	public List<CDMALinkLevelDataPoint> getDataPoints() {
		return getDataPoints(currentPath);
	}

	public List<CDMALinkLevelDataPoint> getDataPoints(int pathNo) {
		List<CDMALinkLevelDataPoint> points = new ArrayList<CDMALinkLevelDataPoint>();
		for (CDMALinkLevelDataPoint point : definedPoints) {
			if (point.getPath() == pathNo) {
				points.add(point);
			}
		}
		return points;
	}

	public CDMALinkLevelDataPoint[] getDataSet(CDMALinkLevelDataPoint key) {
		List<CDMALinkLevelDataPoint> t = new ArrayList<CDMALinkLevelDataPoint>();
		for (CDMALinkLevelDataPoint pt : dataPoints) {
			if ((int) pt.getSpeed() == (int) key.getSpeed()
					  && pt.getPath() == key.getPath()) {
				t.add(pt);
			}
		}
		return t.toArray(new CDMALinkLevelDataPoint[t.size()]);
	}

	public double getFrequency() {
		return frequency;
	}

	public CDMALinkLevelDataPoint getLinkLevelDataPoint(
	      final CDMALinkLevelDataPoint keypoint) {
		if (dataPoints.size() < 1) {
			throw new IllegalStateException("No Link Level Data Points found");
		}
		sort();

		// CHP 18/05-06: No need to search for match as long as we use unrounded geometry
		int index = Collections.binarySearch(dataPoints, keypoint,
		      DATAPOINT_COMPARATOR_DESCENDING_GEOMETRY);
		if (index > 0 && index < dataPoints.size()) {
			return dataPoints.get(index);
		}
		final CDMALinkLevelDataPoint[] data = getDataSet(keypoint);

		CDMALinkLevelDataPoint p1 = null;
		CDMALinkLevelDataPoint p2 = null;

		if (data.length == 0) { // No matching data points found
			if (LOG.isDebugEnabled()) {
				LOG.debug("No matching data was found for keypoint: " + keypoint);
			}

			throw new IllegalStateException(
			      "No matching data was found for keypoint = " + keypoint);
		} else if (data.length == 1) { // Only one mathing data point found -
			// no interpolation possible
			if (LOG.isDebugEnabled()) {
				LOG
				      .debug("Only one mathing data point found - no extrapolation possible for: "
				            + keypoint);
			}

			return data[0];
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Extrapolating new value for: " + keypoint);
			}
			Arrays.sort(data, DATAPOINT_COMPARATOR_ASCENDING_GEOMETRY);
			if (keypoint.getGeometry() > data[data.length - 1].getGeometry()) {
				// keypoint is larger than max of set
				p1 = data[data.length - 2];
				p2 = data[data.length - 1];
			} else if (keypoint.getGeometry() < data[0].getGeometry()) {
				// keypoint is lower than min of set
				p1 = data[0];
				p2 = data[1];
			} else {
				for (int i = 0, stop = data.length - 1; i < stop; i++) {
					p1 = data[i];
					p2 = data[i + 1];
					if (keypoint.getGeometry() > p1.getGeometry()
							&& keypoint.getGeometry() < p2.getGeometry()) {
						break;
					}
				}
			}
			double a = (p2.getEcIor() - p1.getEcIor())
			      / (p2.getGeometry() - p1.getGeometry());
			double b = -(a * p1.getGeometry()) + p1.getEcIor();
			keypoint.setEcior((a * keypoint.getGeometry() + b));

			// Cache result
			if (Double.isNaN(keypoint.getEcIor())) {
				if (LOG.isDebugEnabled()) {
					LOG.error("P1: " + p1);
					LOG.error("P2: " + p2);
					LOG.error("Keypoint: " + keypoint);
				}
				throw new IllegalStateException(
				      "Invalid extrapolation for keypoint = " + keypoint);
			}

			// cache data point
			dataPoints.add(new CDMALinkLevelDataPoint(keypoint));
			sorted = false;
			return keypoint;

		}
	}

	public LinkType getLinkType() {
		return linkType;
	}

	public double getInitialMaximumGeometry() {
		return initialMaxGeometry;
	}
	
	public double getInitialMinimumGeometry() {
		return initialMinGeometry;
	}

	public String getName() {
		return name;
	}

	public String getPathDescription(int path) {
		return pathDescription[path];
	}

	public String getSource() {
		return source;
	}

	public String getSystem() {
		return system;
	}

	public TableModel getTableModel(int pathNo) {
		CDMAEditModel model = new CDMAEditModel();
		model.setData(loadDataFromSource(pathNo));
		return model;
	}

	public String getTargetERpct() {
		return targetERpct;
	}

	public TargetERType getTargetERType() {
		return targetERType;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	private Vector<Vector<Double>> loadDataFromSource(int pathNo) {
		setCurrentPath(pathNo);
		Vector<Vector<Double>> result = new Vector<Vector<Double>>();

		// Count unique geometry values to determine number of rows
		double currentGeom = Integer.MIN_VALUE;
		Vector<Double> tmp = null;
		for (CDMALinkLevelDataPoint point : getDataPoints(pathNo)) {
			// New row
			double geom = point.getGeometry();
			if (geom != currentGeom) {
				currentGeom = geom;
				tmp = new Vector<Double>(5);
				tmp.add( geom );
				for (int x = 1; x < 5; x++) {
					tmp.add(null);
				}
				result.add( tmp );
			}

			switch ((int) point.getSpeed()) {
				case 0: {
					tmp.set(1, point.getEcIor());
					break;
				}
				case 3: {
					tmp.set(2, point.getEcIor());
					break;
				}
				case 30: {
					tmp.set(3, point.getEcIor());
					break;
				}
				case 100: {
					tmp.set(4, point.getEcIor());
					break;
				}
			}
		}
		return result;
	}

	public void setCurrentPath(int path) {
		if (path >= 1 && path <= MAX_PATH_VALUE) {
			this.currentPath = path;
		} else {
			throw new IllegalArgumentException("Path must be 0 or 1 (" + path
			      + ")");
		}
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

	public void setLinkType(LinkType linkType) {
		if (linkType == null) {
			throw new IllegalArgumentException("LinkType cannot be null");
		}
		this.linkType = linkType;
	}

	public void setPathDescription(int path, String desc) {
		pathDescription[path] = desc;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public void setTargetERpct(String targetERpct) {
		this.targetERpct = targetERpct;
	}

	public void setTargetERType(TargetERType targetERType) {
		this.targetERType = targetERType;
	}

	private void sort() {
		if (!sorted) {
			Collections.sort(dataPoints, DATAPOINT_COMPARATOR_DESCENDING_GEOMETRY);
			sorted = true;
		}
	}

	public Element toElement(Document doc) {
		Element rootElem = doc.createElement("CDMA-Link-level-data");
		rootElem.setAttribute("system-type",
		      linkType == LinkType.UPLINK ? "uplink" : "downlink");
		rootElem.setAttribute("source", source);
		rootElem.setAttribute("system", system);
		rootElem.setAttribute("frequency", Double.toString(frequency));
		rootElem.setAttribute("targetPct", targetERpct);
		rootElem.setAttribute("targetType", targetERType.toString());

		// Paths
		for (int pathNo = 1; pathNo <= MAX_PATH_VALUE; pathNo++) {
			Element pathElem = doc.createElement("path");
			pathElem.setAttribute("no", Integer.toString(pathNo));
			pathElem.setAttribute("caption", getPathDescription(pathNo));

			// Data points
			for (CDMALinkLevelDataPoint point : definedPoints ) {
				// Only store data point if pathno is matches current path iteration
				if (point.getPath() == pathNo) {
					pathElem.appendChild(point.toElement(doc));
				}
			}
			rootElem.appendChild(pathElem);
		}

		return rootElem;

	}

    @Override
    public Description description() {
        return new DescriptionImpl(shortText(), "");
    }

    @Override
    public void setDescription(Description description) {
        this.system = description.name();
    }

    @Override
	public String toString() {
		return shortText();
	}
	
	public String fullText() {
		StringBuilder sb = new StringBuilder();
		sb.append(system != null ? system : "").append(" : ");
		sb.append(source != null ? source : "").append(" : ");
		sb.append(frequency).append(" MHz : ");
		sb.append(linkType == LinkType.UPLINK ? "uplink" : "downlink").append(" : ").append(targetERpct);
		sb.append("% ").append(targetERType.toString());
		return sb.toString();		
	}

	public String shortText() {
		StringBuilder sb = new StringBuilder();
		sb.append(system != null ? system : "").append(" : ").append(frequency).append(" MHz : ");
		return sb.toString();		
	}

	public void updatePath(int currentPath2, List<CDMALinkLevelDataPoint> values) {
	   definedPoints.removeAll( getDataPoints() );
	   definedPoints.addAll( values );
	   dataPoints.clear();
	   dataPoints.addAll( definedPoints );
	   sortAll();
   }

}