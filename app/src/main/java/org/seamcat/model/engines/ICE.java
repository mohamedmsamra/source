package org.seamcat.model.engines;

import org.apache.log4j.Logger;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.ICEProbabilityResultEvent;
import org.seamcat.model.RadioSystem;
import org.seamcat.model.Scenario;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.generic.GenericReceiver;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.generic.GenericTransmitter;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.simulation.result.SimulationResult;
import org.seamcat.model.types.result.NamedVectorResult;
import org.seamcat.model.types.result.ResultTypes;
import org.seamcat.model.types.result.VectorGroupResultType;
import org.seamcat.model.types.result.VectorResultType;
import org.seamcat.presentation.MainWindow;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static java.lang.String.format;

/**
 * Interference Calculation Engine
 */
public class ICE implements Runnable {

	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

	private static final String[] ERRORS = new String[] { STRINGLIST.getString("ICE_ERROR") };
	private static final Logger LOG = Logger.getLogger(ICE.class);

	private static final String THREAD_NAME = STRINGLIST.getString("ICE_THREAD");
	private static final String[] WARNINGS = new String[] { STRINGLIST.getString("ICE_WARNING_PROB") };

	private ICEConfiguration iceconf;
	private final List<InterferenceCalculationListener> iceListeners = new ArrayList<InterferenceCalculationListener>();

    private boolean stopped;
	private Scenario scenario;
    private SimulationResult result;

    private static String DRSS = "dRSS";
    private static String UNWANTED = "iRSS Unwanted";
    private static String BLOCKING = "iRSS Blocking";
    private static String INTERMOD = "iRSS Intermodulation";
    private static String OVERLOAD = "Delta overloading";

	public ICE(Scenario scenario, SimulationResult result) {
		this.scenario = scenario;
        this.result = result;
    }

	public void addIceListener(InterferenceCalculationListener iceListener) {
		iceListeners.add(iceListener);
	}

	public void calculateInterference(ICEConfiguration iceconf) {
		this.iceconf = iceconf;
		stopped = false;
		iceconf.setNumberOfSamples(scenario.numberOfEvents());

		int validationResult = iceconf.validate();
		if (validationResult == ICEConfiguration.EVERYTHING_OK) {
            Thread iceThread = new Thread(this, THREAD_NAME);
			iceThread.start();
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.error("configuration validation not OK - notify listeners");
			}
			notifyListenersWarningMessage(ICEConfiguration.ERROR[validationResult]);
		}
	}

	public double[] cfcCompositeIRSSVector(boolean unwant, boolean blocking, boolean intermod) {
		List<VectorResultType> iRssVectors = new ArrayList<VectorResultType>();
        // size of the DRSS vector will determine the size of all other vectors
        int iSize = getVectors(DRSS).get(0).getValue().size();
        double[] pResult = new double[iSize];
        // vector 0 always holds the summation of all the sub link vectors
        if ( unwant ) {
            iRssVectors.add(getVectors(UNWANTED).get(0));
        }
        if ( blocking ) {
            iRssVectors.add(getVectors(BLOCKING).get(0));
        }
        if ( intermod ) {
            iRssVectors.add(getVectors(INTERMOD).get(0));
        }

		for (int j = 0; j < iSize && !stopped; j++) {
			double rSum = 0;
            for (VectorResultType iRssVector : iRssVectors) {
                rSum += Mathematics.dB2Linear(iRssVector.getValue().get(j));
            }
            pResult[j] = Mathematics.linear2dB(rSum);
		}

		return pResult;
	}

    private List<VectorResultType> getVectors( String name ) {
        ResultTypes types = result.getSeamcatResult(name).getResultTypes();
        if ( name.equals( UNWANTED) || name.equals(BLOCKING)) {
            List<VectorResultType> result = new ArrayList<>();
            // add summation as first
            result.add( types.getVectorResultTypes().get(0));

            for (VectorGroupResultType type : types.getVectorGroupResultTypes()) {
                for (NamedVectorResult vector : type.getVectorGroup()) {
                    result.add( new VectorResultType(type.getName(), type.getUnit(), vector.getVector().asArray()));
                }
            }
            return result;
        }
        return result.getSeamcatResult(name).getResultTypes().getVectorResultTypes();
    }

	public double[] cfcCompositeIRSSVectorDerivation(boolean unwant,boolean blocking, boolean intermod, double ref, int choice) {
		int iCount = scenario.getInterferenceLinks().size();
        Function pBlockingResponse = scenario.getVictimSystem().getReceiver().getBlockingMask();
        Function pIntermod = ((GenericSystem)scenario.getVictimSystem()).getReceiver().getIntermodulationRejection();
        int iSize = getVectors(DRSS).get(0).getValue().size();
        double[] pResult = new double[iSize];
		// Calculation of iRSS Composite in the case of iRSS Vectors and
		// derivation mode
		for (int j = 0; j < iSize; j++) {
			double rSum = 0;
			switch (choice) {
				// Intermodulation choice
				case 1: {
					double rRefInit = pIntermod.evaluate(0);

					if (intermod) {
                        List<VectorResultType> vectors = getVectors(INTERMOD);
                        for (int i = 1; i <= iCount; i++) {
							double riRSS = vectors.get(i).getValue().get(j);
							double rValue = riRSS - 3 * (ref - rRefInit);

                            rSum += Math.pow(10, rValue / 10);
						}
					}

					if (unwant) {
                        List<VectorResultType> vectors = getVectors(UNWANTED);
                        for (int i = 1; i <= iCount; i++) {
							double rValue = vectors.get(i).getValue().get(j);

                            rSum += Math.pow(10, rValue / 10);
						}
					}

					if (blocking) {
                        List<VectorResultType> vectors = getVectors(BLOCKING);
                        for (int i = 1; i <= iCount; i++) {
							double rValue = vectors.get(i).getValue().get(j);
                            rSum += Math.pow(10, rValue / 10);
						}
					}
					break;
				}
				// Blocking choice
				case 0: {
					double rRefInit = pBlockingResponse.evaluate(0);

					if (blocking) {
                        List<VectorResultType> vectors = getVectors(BLOCKING);
						for (int i = 1; i <= iCount; i++) {
							double riRSS = vectors.get(i).getValue().get(j);
							double rValue = riRSS - (ref - rRefInit);
                            rSum += Math.pow(10, rValue / 10);
						}
					}

					if (unwant) {
                        List<VectorResultType> vectors = getVectors(UNWANTED);
                        for (int i = 1; i <= iCount; i++) {
							double rValue = vectors.get(i).getValue().get(j);
                            rSum += Math.pow(10, rValue / 10);
						}
					}

					if (intermod) {
                        List<VectorResultType> vectors = getVectors(INTERMOD);
                        for (int i = 0; i < iCount * (iCount - 1); i++) {
							double rValue = vectors.get(i).getValue().get(j);
                            rSum += Math.pow(10, rValue / 10);
						}
					}
					break;
				}
				// Power choice
				default: {
                    RadioSystem radioSystem = scenario.getInterferenceLinks().get(choice - 2).getInterferingSystem();
                    Distribution pUnwantedPower;
                    if ( radioSystem instanceof GenericSystem) {
                        pUnwantedPower = ((GenericTransmitter)radioSystem.getTransmitter()).getPower();
                    } else {
                        pUnwantedPower = Factory.distributionFactory().getConstantDistribution(0.0);
                    }
					double rUnwantedPower = pUnwantedPower.trial();
					double rRefInit = rUnwantedPower;

					if (unwant && !blocking) {
                        List<VectorResultType> vectors = getVectors(UNWANTED);
						double riRSS = vectors.get(choice -1 ).getValue().get(j);
						double rValue = riRSS + ref - rRefInit;
						rSum = Math.pow(10, rValue / 10);

						for (int i = 1; i <= iCount; i++) {
							if (i == (choice - 1)){
								// do nothing because (choice-1) has been processed before the loop
							}else{
								rValue = vectors.get(i).getValue().get(j);
                                rSum += Math.pow(10, rValue / 10);
							}
						}
					}
					if (!unwant && blocking) {
                        List<VectorResultType> vectors = getVectors(BLOCKING);
						double riRSS = vectors.get(choice - 1).getValue().get(j);
						double rValue = riRSS + ref - rRefInit;
						rSum = Math.pow(10, rValue / 10);

						for (int i = 1; i <= iCount; i++) {
							if (i == (choice - 1)){
								// do nothing because (choice-1) has been processed before the loop
							}else{
								rValue = vectors.get(i).getValue().get(j);
                                rSum += Math.pow(10, rValue / 10);
							}
						}
					}
					if (unwant && blocking) {
                        if (unwant) {
                            List<VectorResultType> vectors = getVectors(UNWANTED);
                            double riRSS = vectors.get(choice -1 ).getValue().get(j);
                            double rValue = riRSS + ref - rRefInit;
                            rSum = Math.pow(10, rValue / 10);

                            for (int i = 1; i <= iCount; i++) {
                                if (i == (choice - 1)){
                                    // do nothing because (choice-1) has been processed before the loop
                                }else{
                                    rValue = vectors.get(i).getValue().get(j);
                                    rSum += Math.pow(10, rValue / 10);
                                }
                            }
                        }
                        if (blocking) {
                            List<VectorResultType> vectors = getVectors(BLOCKING);
							double riRSS = vectors.get(choice-1).getValue().get(j);
							double rValue = riRSS + ref - rRefInit;
							rSum += Math.pow(10, rValue / 10);

							for (int i = 1; i <= iCount; i++) {
								if (i == (choice - 1)){
									// do nothing because (choice-1) has been processed before the loop
								}else{
									rValue = vectors.get(i).getValue().get(j);
                                    rSum += Math.pow(10, rValue / 10);
								}
							}
						}
					}

					if (intermod) {
                        List<VectorResultType> vectors = getVectors(INTERMOD);
						for (int i = 0; i < iCount * (iCount - 1); i++) {
							double rValue = vectors.get(i).getValue().get(j);
                            rSum += Math.pow(10, rValue / 10);
						}
					}
				}
			}

            pResult[j] = Mathematics.linear2dB(rSum);
		}
		return pResult;
	}

	public double[] cfcCritVector(double[] dRSS, double[] iRSSComp, double[] NoiseFloor,
                                     InterferenceCriterionType InterferenceCriterion) {
		double rdRSS, riRSS, rNoiseFloor;
        int iSize = dRSS.length;
        double[] pResult = new double[iSize];
		// Calculation of Criterion Vector
		switch (InterferenceCriterion.getInterferenceCriterionType()) {
			case InterferenceCriterionType.CI: {
				for (int i = 0; i < iSize; i++) {
					rdRSS = dRSS[i];
					riRSS = iRSSComp[i];

                    pResult[i] = rdRSS - riRSS;
				}

				return pResult;
			}
			case InterferenceCriterionType.CNI: {
				for (int i = 0; i < iSize; i++) {
					rdRSS = dRSS[i];
					riRSS = iRSSComp[i];
					rNoiseFloor = NoiseFloor[i];

                    double rA = Math.pow(10, rdRSS / 10);
                    double rB = Math.pow(10, riRSS / 10);
                    double rC = Math.pow(10, rNoiseFloor / 10);

                    pResult[i] = Mathematics.linear2dB(rA / (rB + rC));
				}

				return pResult;
			}
			case InterferenceCriterionType.INI: {
				for (int i = 0; i < iSize; i++) {
					riRSS = iRSSComp[i];
					rNoiseFloor = NoiseFloor[i];

                    double rB = Math.pow(10, riRSS / 10);
                    double rC = Math.pow(10, rNoiseFloor / 10);

                    pResult[i] = Mathematics.linear2dB((rC + rB) / rC);
				}

				return pResult;
			}
			case InterferenceCriterionType.IN: {
				for (int i = 0; i < iSize; i++) {
					riRSS = iRSSComp[i];
					rNoiseFloor = NoiseFloor[i];
                    pResult[i] = riRSS - rNoiseFloor;
				}

				return pResult;
			}
			default: {
				throw new IllegalArgumentException(
				      "Invalid Interference Criterion: "
				            + InterferenceCriterion.getInterferenceCriterionType());
			}
		}
	}

	private double iceComplete1Compatibility(boolean bUnwant, boolean bBlocking,
	      boolean bIntermod, boolean bOverloading, int iInterferenceCriterion, int iNbEvents) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(format("ICEComplete1Compatibility(bUnwant: %f, bBlocking: %f, bIntermod: %f, bOverloading: %f ,iInterferenceCriterion: %f, iNbEvents: %f)",
					bUnwant, bBlocking, bIntermod, bOverloading, iInterferenceCriterion, iNbEvents
			));
		}
		notifyListenersSetCurrentProcessCompletionPercentage(0);
		double rProbD = 0;
		double rProb = 0;

        double[] dRSS = getVectors(DRSS).get(0).getValue().asArray();
        int iSize = dRSS.length;
        double rSensVr = ((GenericSystem)scenario.getVictimSystem()).getReceiver().getSensitivity();

        double[] critVector;
		double[] iRSSVectorComposite = null;
		double[] noiseFloor = new double[iSize];
		Distribution pNoiseFloor = ((GenericSystem)scenario.getVictimSystem()).getReceiver().getNoiseFloor();
		for ( int i=0; i<iSize; i++) {
            noiseFloor[i] = pNoiseFloor.trial();
        }
		int rProbN = 0;

		// iRSS Composite
		iRSSVectorComposite = cfcCompositeIRSSVector(bUnwant, bBlocking, bIntermod);
		int numberOfEvents = 0;
		if (!isStopped()) {
			critVector = cfcCritVector(dRSS, iRSSVectorComposite, noiseFloor, iceconf);
			// Probability calculation
			int i = 0;
			for (i = 0; i < iNbEvents && i < dRSS.length;  i++) {
				boolean bResultTest = true;

				// dRSS Trial
				double rdRSSTrial = getVectors(DRSS).get(0).getValue().get(i);
				// Test dRSS > Victim Receiver sensitivity
				if (rdRSSTrial > rSensVr) {
					rProbD++;

					if (bOverloading) {
						if (getVectors(OVERLOAD).get(0).getValue().get(i) >= 0) {
							bResultTest = false;
						}
					}

					if (bResultTest && (bUnwant || bBlocking || bIntermod)) {
						// Criterion Trial
						double rCrit = critVector[i];
						bResultTest = testProbComplete(rdRSSTrial, rCrit, iceconf);
					}

					if (bResultTest) {
						rProbN++;
					}
				}
			}
			numberOfEvents = i;
		}

		double probResult;

		// Probability calculation
		if (rProbD == 0) {
			rProb = 0;
			notifyListenersWarningMessage(WARNINGS[0]);
		} else {
			rProb = rProbN / rProbD;
		}
		probResult = 1.0 - rProb;

        iceconf.setProbabilityResult( probResult );
        iceconf.setHasBeenCalculated(true);
        EventBusFactory.getEventBus().publish(new ICEProbabilityResultEvent(iceconf));
        notifyListenersParameters(numberOfEvents, rProbD);

		return probResult;
	}

	private void iceComplete1Derivation(boolean bUnwant, boolean bBlocking,
	      boolean bIntermod, int iInterferenceCriterion, int iNbEvents) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("ICEComplete1Derivation(bUnwant: " + bUnwant
			      + ", bBlocking: " + bBlocking + ", bIntermod: " + bIntermod
			      + ",iInterferenceCriterion: " + iInterferenceCriterion
			      + ", iNbEvents: " + iNbEvents + ")");
		}
		double rProbD = 0;
		int iChoice = iceconf.getTranslationParameter();
		int iSize = getVectors(DRSS).get(0).getValue().size();
		double rSensVr = ((GenericSystem)scenario.getVictimSystem()).getReceiver().getSensitivity();
		double rRefMin = iceconf.getTranslationMin();
		double rRefMax = iceconf.getTranslationMax();
		int iRefNbr = (int) iceconf.getTranslationPoints();
		double rRefStep = (rRefMax - rRefMin) / iRefNbr;
		double[] rProduct = new double[iRefNbr + 1];

		double[] dRSS = getVectors(DRSS).get(0).getValue().asArray();

		int k = 0;
		double rRef = rRefMin;
		Distribution pNoiseFloor = ((GenericSystem)scenario.getVictimSystem()).getReceiver().getNoiseFloor();

		int numberOfEventsTranslation = 0;
		for (int j = 0; j < iRefNbr && !isStopped(); j++, rRef += rRefStep) {
			rProbD = 0;

			// iRSS Composite
			double[] iRSSVectorComposite = cfcCompositeIRSSVectorDerivation(bUnwant, bBlocking, bIntermod, rRef, iChoice);

			// Criterion Vector
			double[] critVector;
			double[] noiseFloor = new double[iSize];
            for ( int i=0; i<iSize; i++) {
                noiseFloor[i] = pNoiseFloor.trial();
            }
			critVector = cfcCritVector(dRSS, iRSSVectorComposite, noiseFloor, iceconf);

			// Probabilities calculation
			int i;
			for (i = 0; i < iNbEvents; i++) {
				double rdRSSTrial = dRSS[i];

				// Test dRSS > Victim Receiver sensitivity
				if (rdRSSTrial > rSensVr) {
					rProbD++;
				}

				double rCrit = critVector[i];

				boolean bResultTest = testProbComplete(rdRSSTrial, rCrit, iceconf);

				if (bResultTest == true) {
					rProduct[j]++;
				}
			}
			if (rProbD == 0) {
				rProduct[j] = 0;
				k++;
			} else {
				rProduct[j] = rProduct[j] / rProbD;
			}

			notifyListenersIncrementCurrentProcessCompletionPercentage((j * 100 / iRefNbr));

			numberOfEventsTranslation = numberOfEventsTranslation + i;
		}

		if (k == iRefNbr) {
			notifyListenersWarningMessage(WARNINGS[0]);
		}

		notifyListenersParameters(numberOfEventsTranslation,rProbD * 100);

		rRef = rRefMin;
		for (int j = 0; j < iRefNbr; rRef += rRefStep, j++) {
			Point2D pt = new Point2D(rRef, (1 - rProduct[j]) * 100);
			notifyListenersTranslationResult(pt);
		}
	}

	public boolean isStopped() {
		return stopped;
	}

	private void notifyListenersCalculationComplete() {
		for (InterferenceCalculationListener l : iceListeners) {
			l.calculationComplete();
		}
	}

	// ---------------------------------------------------------------------------

	private void notifyListenersCalculationStarted() {
		for (InterferenceCalculationListener l : iceListeners) {
			l.calculationStarted();
		}
	}

	// ---------------------------------------------------------------------------

	// //////////////////////////////////////////////////////////

	void notifyListenersIncrementCurrentProcessCompletionPercentage(int value) {
		for (InterferenceCalculationListener l : iceListeners) {
			l.incrementCurrentProcessCompletionPercentage(value);
		}
	}

    void notifyListenersParameters(int numberTotalEvents, double probabilityTotalN) {
		GenericReceiver vr = ((GenericSystem)scenario.getVictimSystem()).getReceiver();
		double rCI = vr.getProtectionRatio();
		double rCNI = vr.getExtendedProtectionRatio();
		double rINI = vr.getNoiseAugmentation();
		double rIN = vr.getInterferenceToNoiseRatio();
		double sensitivity = vr.getSensitivity();

		for (InterferenceCalculationListener l : iceListeners) {
			l.parameters(numberTotalEvents, probabilityTotalN, rCI, rCNI, rINI, rIN, sensitivity);
		}
	}

	void notifyListenersSetCurrentProcessCompletionPercentage(int value) {
		for (InterferenceCalculationListener l : iceListeners) {
			l.setCurrentProcessCompletionPercentage(value);
		}
	}

	void notifyListenersTranslationResult(Point2D value) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("TranslationResult: x=" + value.getX() + " y="
			      + value.getY());
		}
		for (InterferenceCalculationListener l : iceListeners) {
			l.addTranslationResult(value);
		}
	}

	void notifyListenersWarningMessage(String warning) {
		for (InterferenceCalculationListener l : iceListeners) {
			l.warningMessage(warning);
		}
	}

	public void run() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Begin ICE run()");
		}
		notifyListenersCalculationStarted();

		try{
			MainWindow.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			if (iceconf == null) {
				LOG.error("Ice configuration is null -> throws IllegalStateException");
				throw new IllegalStateException(
				"Unable to start ICE when no ICEConfiguration is specified!");
			} else {
				if (!iceconf.calculationModeIsTranslation()) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("ALGORITHM_COMPLETE1 && !Translation");
					}
					try {
						iceComplete1Compatibility(iceconf.isUnwanted(), iceconf
								.isBlocking(), iceconf.isIntermodulation(), iceconf.isOverloading(),
								iceconf.getInterferenceCriterionType(), iceconf
								.getNumberOfSamples());
					} catch (RuntimeException e) {
						LOG.error("Exception - notify listeners", e);
						notifyListenersWarningMessage("Interference calculation failed");
					}
				}
				// if Complete 1 Algorithm with derivation mode (correlated case)
				// (dRSS distribution and iRSS vectors OR dRSS vectors and iRSS
				// vectors)
				else if (iceconf.calculationModeIsTranslation()) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("ALGORITHM_COMPLETE1 && Translation");
					}
					try {
						iceComplete1Derivation(iceconf.isUnwanted(), iceconf
								.isBlocking(), iceconf.isIntermodulation(), iceconf
								.getInterferenceCriterionType(), iceconf
								.getNumberOfSamples());
					} catch (Exception e) {
						LOG.error("Exception - notify listeners", e);
						notifyListenersWarningMessage(ERRORS[0]);
					}
				}
			}
			iceconf.setHasBeenCalculated(true);
			notifyListenersCalculationComplete();

		} catch (Exception ex){
			LOG.error("Something went wrong in the ICE calculation", ex);
			throw new IllegalStateException("Something went wrong in the ICE calculation", ex);
		} finally{
			MainWindow.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("End ICE run()");
		}
	}

	public void stop() {
		stopped = true;
	}

	public boolean testProbComplete(double dRSS, double crit, InterferenceCriterionType interferenceCriterion) {
		GenericReceiver vr = ((GenericSystem)scenario.getVictimSystem()).getReceiver();
		double rSens = vr.getSensitivity();
		double rCI = vr.getProtectionRatio();
		double rCNI = vr.getExtendedProtectionRatio();
		double rINI = vr.getNoiseAugmentation();
		double rIN = vr.getInterferenceToNoiseRatio();

		boolean test;
		switch (interferenceCriterion.getInterferenceCriterionType()) {
			case InterferenceCriterionType.CI: {
				test = crit > rCI && dRSS > rSens;
				break;
			}
			case InterferenceCriterionType.CNI: {
				test = crit > rCNI && dRSS > rSens;
				break;
			}
			case InterferenceCriterionType.INI: {
				test = crit < rINI && dRSS > rSens;
				break;
			}
			case InterferenceCriterionType.IN: {
				test = crit < rIN && dRSS > rSens;
				break;
			}
			default: {
				test = false;
				break;
			}
		}
		return test;
	}
}