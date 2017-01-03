package org.seamcat.model.scenariocheck;

import org.seamcat.function.DiscreteFunction;
import org.seamcat.model.distributions.UserDefinedDistributionImpl;
import org.seamcat.model.distributions.DiscreteUniformDistributionImpl;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.scenariocheck.ScenarioCheckResult.Outcome;

import java.util.Collections;

abstract class AbstractCheck implements ScenarioCheck {

    protected ScenarioCheckResult result = new ScenarioCheckResult();

    public void addErrorMsg(String msg) {
        if (result.getOutcome() != Outcome.FAILED) {
            result.setOutcome(Outcome.FAILED);
        }
        result.addMessage(msg);
    }

    public void checkDistribution(Distribution dist, String linkName,
                                     String fieldName) {
        if ( dist instanceof UserDefinedDistributionImpl) {
            double rMin = ((UserDefinedDistributionImpl) dist).getCdf().evaluateMin();
            double rMax = ((UserDefinedDistributionImpl) dist).getCdf().evaluateMax();
            if (rMin != 0.0 || rMax != 1.0) {
                addErrorMsg("User-defined distribution doesn't include 0 and/or 1 value(s) for parameter "
                        + fieldName + " in " + linkName);
            }else{
                Collections.sort(((DiscreteFunction)((UserDefinedDistributionImpl) dist).getCdf()).points(), Point2D.POINTY_COMPARATOR);
            }
        } else if ( dist instanceof DiscreteUniformDistributionImpl) {
            DiscreteUniformDistributionImpl dud = (DiscreteUniformDistributionImpl) dist;
            double rRatio = (dud.getMax() - dud.getMin()) / dud.getStep();

            // Check if the range of the distribution is multiple of the
            // step
            if ((rRatio - Math.rint(rRatio)) / rRatio > 0.01) {
                addErrorMsg("Range is not multiple of the step in the discrete uniform distribution used for parameter "
                        + fieldName + " in " + linkName);
            }
        }
    }
}