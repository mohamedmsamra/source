package org.seamcat.model.propagation;

import junit.framework.Assert;
import org.junit.Test;
import org.seamcat.model.mathematics.Mathematics;

public class HataSE21Test {

    @Test
    public void testHataInternal() {
        HataSE21PropagationModel model = new HataSE21PropagationModel();

        double loss = model.calculateMedianLoss(900, 10, 30, 1.5);
        Assert.assertTrue(Mathematics.equals(161.7963, loss, 0.001));
    }

}
