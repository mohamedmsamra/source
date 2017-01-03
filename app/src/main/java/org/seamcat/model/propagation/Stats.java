package org.seamcat.model.propagation;

public class Stats {

	private static double rC0 = 2.515517;
	private static double rC1 = 0.802853;
	private static double rC2 = 0.010328;
	private static double rD1 = 1.432788;
	private static double rD2 = 0.189269;
	private static double rD3 = 0.001308;
	private static long seed0 = 1;

	private static double dZeta(double rX) {
		double rDzeta = 0;
		double rT = 0;
		rT = Stats.t(rX);
		rDzeta = ((rC2 * rT + rC1) * rT + rC0)
		      / (((rD3 * rT + rD2) * rT + rD1) * rT + 1);
		return rDzeta;
	}

	public static final long getSeed0() {
		seed0++;
		return seed0;
	}

	public static void init() {
		seed0 = 1;
	}

	public static double qi(double rX) {
		double rQ = 0;
		if (rX < 0.5) {
			rQ = Stats.t(rX) - Stats.dZeta(rX);
		} else {
			rQ = -(Stats.t(1 - rX) - Stats.dZeta(1 - rX));
		}
		return rQ;
	}

	private static double t(double rX) {
		double rT = 0;
		rT = Math.sqrt(-2 * Math.log(rX));
		return rT;
	}

	public Stats() {
	}

}
