package org.seamcat.presentation;

import org.jfree.data.xy.XYSeries;
import org.seamcat.model.functions.Point2D;

import java.util.*;

public class ExtendableXYSeries extends XYSeries {
	private String type;
	private PointToArgumentMap argMap = new PointToArgumentMap();

	public ExtendableXYSeries(Comparable key) {
		super(key);
	}

	public ExtendableXYSeries() {
		super("");
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void add(double x, double y, Argument... args) {
		addToMap(x, y, args);
	}

	public List<Argument> getArgsForPoint(Point2D point) {
		return argMap.get(point);
	}

	public List<Argument> getArgsForPoint(double x, double y) {

		return getArgsForPoint(new Point2D(x, y));
	}

	public List<Argument> getArgsForPoint(Number x, Number y) {
		return getArgsForPoint(x.doubleValue(), y.doubleValue());
	}

	private void addToMap(double x, double y, Argument... args) {
		if (x == 0 && y == 0) {
			// For some reason this gets called a lot.
			args = null;
		}
		// Add x,y data
		super.add(x, y);

		// Add arguments
		if (args != null && args.length > 0 && args[0] != null) {
			Point2D point = new Point2D(x, y);
			argMap.put(point, Arrays.asList(args));
		}
	}

	private class PointToArgumentMap {
		Map<Object, ArrayList<Argument>> map;

		public PointToArgumentMap() {
			map = new HashMap<Object, ArrayList<Argument>>();
		}

		/**
		 * Adds value to key. If key already had value assigned, it is added to a
		 * list rather than overwriting old value
		 * 
		 * @param key
		 * @param args
		 */
		public void put(Object key, List<Argument> args) {
			if (!map.containsKey(key)) {
				ArrayList<Argument> list = new ArrayList<Argument>();
				list.addAll(args);
				map.put(key, list);
			} else {
				map.get( key ).clear();
                map.get(key).addAll(args);
			}
		}

		public ArrayList<Argument> get(Object key) {
			return map.get(key);

		}

	}
}
