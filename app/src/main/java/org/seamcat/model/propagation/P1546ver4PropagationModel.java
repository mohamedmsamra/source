package org.seamcat.model.propagation;

import org.seamcat.model.factory.Factory;
import org.seamcat.model.Scenario;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.plugin.OptionalDoubleValue;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.propagation.P1546ver4Input;
import org.seamcat.model.plugin.propagation.PropagationModelPlugin;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.seamcat.model.mathematics.Mathematics.Qi;

/**
 * <p>Title: ITU-R P.1546-3</p>
 * <p>Copyright: Copyright (c) 2010</p>
 * <p>Company: National Institute of Telecommunications</p>
 * @author Dariusz Wypior
 * @version 3.0
 */
public class P1546ver4PropagationModel implements PropagationModelPlugin<P1546ver4Input> {

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, P1546ver4Input input, Validator<P1546ver4Input> validator) {
        Distribution frequency = HataSE21PropagationModel.findFrequency(scenario, path);
        if ( frequency != null ) {
            Bounds bounds = frequency.getBounds();
            if (bounds.getMin() < 30 || bounds.getMax() > 3000) {
                validator.error("Frequencies below 30 MHz or above 3000 MHz are not supported by the ITU-R P.1546-4 Recommendation." +
                PluginCheckUtilsToBeRemoved.getExceptionHint());
            }
        }
        if (path.size() > 0) {
            double R = 10;
            if (input.localClutter().isRelevant()) {
                R = input.localClutter().getValue();
            } else {
                String selected = input.generalEnvironment();
                if (selected.equals("Suburban")) R = 10;
                else if (selected.equals("Urban")) R = 20;
                else if (selected.equals("Dense Urban")) R = 30;
            }
            if (PluginCheckUtilsToBeRemoved.getAntennaHeightRXmin(path) < R
                    && PluginCheckUtilsToBeRemoved.getAntennaHeightTXmin(path) < R) {
                validator.error("<HtMl>if both terminals are below the levels of clutter in their respective vicinities, <br/>" +
                        "then the P1546-4 Recommendation will not give accurate predictions to the problem " +
                        "at hand. <br/>Users should seek guidance from other, more appropriate, ITU-R " +
                        " Recommendations. (See Annex 5-1.1 a of ITU-R P.1546-4))" + PluginCheckUtilsToBeRemoved.getExceptionHint());
            }

            if (PluginCheckUtilsToBeRemoved.getMaxDistance(scenario,path) > 1000)
                validator.error("Distances above 1000 km are not supported by the ITU-R P.1546-4 Recommendation." +
                PluginCheckUtilsToBeRemoved.getExceptionHint());

            if (PluginCheckUtilsToBeRemoved.getAntennaHeightTXmax(path) > 3000 ||
                    PluginCheckUtilsToBeRemoved.getAntennaHeightRXmax(path) > 3000)
                validator.error("Antenna height higher than 3000 m are not supported by the ITU-R P.1546-4 Recommendation." +
                PluginCheckUtilsToBeRemoved.getExceptionHint());
        }

        Distribution timePercentage = input.timePercentage();
        if (timePercentage.getBounds().getMin() < 1 || timePercentage.getBounds().getMax() > 50){
            validator.error("The ITU-R P.1546-4 Recommendation is not valid for field strengths exceeded for percentage times outside the range from 1% to 50%."
                    + PluginCheckUtilsToBeRemoved.getExceptionHint());
        }

    }

    public static double stdDeviation=0;
    public static Set<String> urban = new HashSet<String>(Arrays.asList("Urban", "Dense Urban", "Suburban"));

    private OptionalDoubleValue userSpecifiedStdDev;
    private boolean isBuildingOfUniformHeightSelected = true ;

    @Override
    public Description description() {
        return new DescriptionImpl("ITU-R P.1546-4 land","<html>" +
                "Version 3.0. Copyright (c) 2010\n" +
                "Company: National Institute of Telecommunications\n" +
                "Dariusz Wypior<body><b><u>Frequency range:</u></b><br>30 MHz - 3 GHz<br><b><u>Distance range:</u></b><br>1-1000 km<br><b><u>Typical application area:</u></b><br>Broadcasting and other terrestrial services, <br>typically considered in cases with high <br> mounted transmitter antenna (e.g. above<br> 50-60 m).<br><b><u>Information:</u></b><br>Note that the P.1546-4 model assumes that <br>the specified height of transmitting antenna <br>is height above local clutter (effective height<br> of antenna). <br>The receiver antenna is above ground and <br>the correction for local clutter will be applied<br> by the model.</body></html>");
    }
    @Override
    public double evaluate(LinkResult linkResult, boolean variations, P1546ver4Input input) {
        double rFreq = linkResult.getFrequency();
        double rDist = linkResult.getTxRxDistance();
        double rHTx = linkResult.txAntenna().getHeight();
        double rHRx = linkResult.rxAntenna().getHeight();
        double gain = linkResult.txAntenna().getGain();

        double rPt=0.;
        double rh1=0.;
        double rh2=0.;
        double rha=0;
        double R = 10;
        try {
            Distribution timePercentage = input.timePercentage();
            userSpecifiedStdDev = input.stdDev();
            String locationArea = input.area();
            isBuildingOfUniformHeightSelected = input.uniformBuildingHeight();

            // Trial on time percentage
            rPt=timePercentage.trial();

            if (input.localClutter().isRelevant()) {
                R = input.localClutter().getValue();
            } else {
                String selected = input.generalEnvironment();
                if ( selected.equals( "Suburban")) R=10;
                else if ( selected.equals("Urban")) R=20;
                else if ( selected.equals("Dense Urban")) R=30;
            }

            if (input.terminalDesignations()) {
                if(rHTx < R && rHRx < R){ // implementation of P1546-4 Annex 5 �1.1 a)
                    throw new RuntimeException("if both terminals are below the levels of clutter in their respective vicinities, \n"+
                            "then the P1546-4 Recommendation will not give accurate predictions to the problem \n"+
                            "at hand. Users should seek guidance from other, more appropriate, ITU-R \n"+
                            "Recommendations. (See Annex 5-1.1 a))\n");
                }
                else if (rHTx > R && rHRx <= R){ //implementation of P1546-4 Annex 5 �1.1 b)
                    rha = rHTx;
                    rh2 = rHRx;
                }
                else if (rHTx <= R && rHRx > R){
                    rha = rHRx;
                    rh2 = rHTx;
                }
                else if (rHTx >= R && rHRx >= R){//implementation of P1546-4 Annex 5 �1.1 c)
                    if(rHTx > rHRx){
                        rha = rHTx;
                        rh2 = rHRx;
                    }
                    else{
                        rha = rHRx;
                        rh2 = rHTx;
                    }
                }
                else{
                    throw new RuntimeException("The terminal designation of ITU-R P.1546-4 Annex 5 � 1.1 does not consider that case.");
                }
            }else{
                rha = rHTx;
                rh2 = rHRx;
            }

            double rheff = rha - R; //between 3 and 15km in the direction of the receiving antenna

            if (rDist <= 3) { //(4)
                rh1 = rha;
            } else if ((rDist > 3) && (rDist < 15)) { //(5)
                rh1 = rha + ((rheff - rha) * (rDist  - 3)/12);
            } else if (rDist >= 15) { //(7)
                rh1 = rheff;
            }
            double rE = compute_e(rPt, rFreq, rh1, rDist, gain);
            double corr = clutterCorrection(rDist, rFreq, rh1, rh2, rha, R, input.generalEnvironment(), input.system(),locationArea);
            rE = rE+corr;

            double MaximuFieldStrengthLimit = 106.9 - 20*Math.log10(rDist); //(2)
            if (rE > MaximuFieldStrengthLimit){
                rE = MaximuFieldStrengthLimit;
            }

            double loss = 139.3 - rE + 20*Math.log10(rFreq);

            if (variations) {
                double std = Factory.distributionFactory().getGaussianDistribution(0, stdDeviation).trial();
                loss = loss + std;
            }

            if (Double.isInfinite(loss)) {
                loss = 20 * Math.log10(rFreq) - 100;//represents a symbolic distance of about 0.3 mm
            }
            return loss;

        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public double clutterCorrection(double d, double f, double h1, double h2,double ha, double R,String environment, String system, String locationArea) {
        double Rp, corr=0, v, theta_clut, h_dif, K_nu, K_h2;
        try {
            if (h1 < (6.5*d + R)){
                Rp = R;
            }
            else{
                Rp = (1000*d*R-15*h1)/(1000*d-15); //(26)
            }

            K_nu = 0.0108*Math.sqrt(f);//(27g)
            K_h2 = 3.2+6.2*Math.log10(f);//(27f)
            h_dif = Rp - h2;//(27d)
            theta_clut = Math.atan(h_dif/27);//(27e)
            theta_clut *= (180/Math.PI);
            v = K_nu*Math.sqrt(h_dif*theta_clut);//(27c)

            if (urban.contains(environment)) {
                if (Rp < 1) {
                    Rp = 1;
                }
                if (h2 < Rp) {
                    corr = 6.03 - j(v);//(27a)
                } else {
                    corr = K_h2*Math.log10(h2/Rp);
                }
                if (Rp < 10) {
                    corr = corr - K_h2*Math.log10(10/Rp);
                }
            } else if (environment.equals("Rural")) {
                Rp = 10;
                corr = K_h2*Math.log10(h2/Rp);
            }

            if (urban.contains(environment) && (d < 15.0) && ((h1-R) < 150) && (h1 > R) && (isBuildingOfUniformHeightSelected)) {
                corr += -3.3 * (Math.log10(f)) * (1 - 0.85 * Math.log10(d)) * (1 - 0.46* Math.log10(1 + ha - R)); //(29)
            }
            stdDeviation = stdDev(environment, system, locationArea,R, h2, f);
            return corr;
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public double stdDev(String environment, String system, String locationArea,double R, double h2, double f) {
        double Dev=0, K=0;
        try {
            if (userSpecifiedStdDev.isRelevant()) {
                Dev = userSpecifiedStdDev.getValue();
            } else {
                if(system.equals("Mobile")){
                    if (urban.contains(environment)){
                        if (h2 < R) {
                            K = 1.2;
                        } else {
                            K = 1.0;
                        }
                    } else {//rural
                        K = 0.5;
                    }
                    Dev = K + 1.3*Math.log10(f);
                    if (locationArea.equals("< 2 km radius")){
                        Dev = Dev + 4;
                    }else if (locationArea.equals("< 50 km radius")){
                        Dev = Dev + 8;
                    }
                }else{
                    if (system.equals("Broadcasting digital")){
                        Dev = 5.5;
                    }else if (system.equals("Broadcasting analogue")){
                        K = 5.1;
                        Dev = K + 1.6*Math.log10(f);
                    }
                }
            }
            return Dev;
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public double less1km(double d, double t, double f, double h, double gain)  {
        double rE=0;
        double dnf = 0.01, e01=0, e1km=0;

        dnf = Math.pow(10,(0.1*gain))/(10 * f); //(35d)
        if(dnf > 0.1){
            dnf = 0.1;
        }

        try {
            if (d <= dnf) {
                rE = 106.9 - 20*Math.log10(dnf);
            } else if ((dnf < d) && (d <= 0.1)) {
                rE = 106.9 - 20*Math.log10(d);
            } else if ((0.1 < d) && (d < 1.0)) {
                e01 = rE = 106.9 - 20*Math.log10(0.1);
                e1km = compute_e(t, f, h, 1, gain);
                rE = e01 + (e1km-e01)*Math.log10(d/0.1);
            }
            return rE;
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public double j(double v) {
        double rJ=0;
        try {
            rJ=6.9+20*Math.log10(Math.sqrt(((v-0.1)*(v-0.1))+1) + v - 0.1);
            return rJ;
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public double compute_e(double t, double f, double h, double d, double gain) {
        int t_inf_index=0, t_sup_index=0, f_inf_index=0, f_sup_index=0, h_inf_index=0, h_sup_index=0, d_inf_index=0, d_sup_index=0;
        double t_inf=0, t_sup=0, f_inf=0, f_sup=0, d_inf=0, d_sup=0, h_inf=0, h_sup=0;
        double E_comp, rE=0, e1_h=0, e2_h=0, e1_f=0, e2_f=0, e1_t=0, e2_t=0, e_inf, e_sup;
        int t_x, f_x, h_x;
        int flag_A = 1, flag_B = 1, flag_C=1;

        try {
            //Time select
            if ((t <= 50) && (t >= 1)){
                if (t == 50) {
                    t_inf = 50; t_sup = 50;
                    t_inf_index = 2; t_sup_index = 2;
                } else {
                    for (int i = 0; i<(tabIndex[0].length-1); i++){
                        if ((tabIndex[0][i] <= t) && (t < tabIndex[0][i+1])) {
                            t_inf = tabIndex[0][i]; t_sup = tabIndex[0][i+1];
                            t_inf_index = i; t_sup_index = (i+1); break;
                        }
                    }
                }

            } else {
                throw new RuntimeException("The ITU-R P.1546-4 Recommendation is not valid for field strengths exceeded for percentage times outside the range from 1% to 50%.");
            }
            // Frequency select
            if ((f >= 30) && (f <= 3000)) {
                if (f<tabIndex[1][1] ) {
                    f_inf = 100; f_sup = 600;
                    f_inf_index = 0; f_sup_index = 1;
                } else if (f == tabIndex[1][2]) {
                    f_inf = 2000; f_sup = 2000;
                    f_inf_index = 2; f_sup_index = 2;
                }
                else {
                    f_inf = 600; f_sup = 2000;
                    f_inf_index = 1; f_sup_index = 2;
                }
            } else {
                throw new RuntimeException("Frequencies below 30 MHz or above 3000 MHz are not supported by the ITU-R P.1546-4 Recommendation ");
            }
            //distance select
            if ((d >= 0) && (d <= 1000)) {
                for (int j = 0; j<(tabIndex[2].length-1); j++){
                    if((tabIndex[2][j] <= d) && (tabIndex[2][j+1] > d)) {
                        d_inf = tabIndex[2][j]; d_sup = tabIndex[2][j+1];
                        d_inf_index = j; d_sup_index = (j+1);break;
                    }
                }
                if (d == 1000) {
                    d_inf = 1000; d_sup = 1000;
                    d_inf_index = 77; d_sup_index = 77;
                }
            } else {
                throw new RuntimeException("Distances above 1000 km are not supported by the ITU-R P.1546-4 Recommendation ");
            }
            //height select
            if ((h >= 0) && (h <= 3000)) {
                if (h > 1200) {
                    h_inf = 600; h_sup = 1200;
                    h_inf_index = 6; h_sup_index = 7;
                } else if (h == 1200) {
                    h_inf = 1200; h_sup = 1200;
                    h_inf_index = 7; h_sup_index = 7;
                } else if (h<1200){
                    for (int k = 0; k<(tabIndex[3].length-1); k++) {
                        if ((tabIndex[3][k] <= h) && (tabIndex[3][k+1] > h)) {
                            h_inf = tabIndex[3][k]; h_sup = tabIndex[3][k+1];
                            h_inf_index = k; h_sup_index = (k+1); break;
                        }
                    }
                }
            } else {
                throw new RuntimeException("Antenna height higher than 3000 m are not supported by the ITU-R P.1546-4 Recommendation ");
            }

            // Computing path loss;
            t_x = t_inf_index;
            f_x = f_inf_index;
            h_x = h_inf_index;
            if (d >= 1){
                while (flag_C == 1) {
                    while (flag_B == 1) {
                        while (flag_A == 1){
                            if(d == d_inf) {
                                rE = tabData[t_x][f_x][d_inf_index][h_x];
                                d_sup_index = d_inf_index;
                                // d_sup_index was increased in line 315
                                //which points to the next distance of the table
                            } else {
                                e_inf = tabData[t_x][f_x][d_inf_index][h_x];
                                e_sup = tabData[t_x][f_x][d_sup_index][h_x];
                                rE = e_inf + (e_sup - e_inf)*Math.log10((d/d_inf))/Math.log10((d_sup/d_inf));
                            }

                            if (h == h_inf) {
                                flag_A = 0;
                            } else if (h<10) {
                                double e_zero, e10, e20, e10_inf, e10_sup, e20_inf, e20_sup, C1020, C_h1neg10, v, K_v=0, theta_eff2;

                                if(d == d_inf) {
                                    e10 = tabData[t_x][f_x][d_inf_index][0];
                                } else {
                                    e10_inf = tabData[t_x][f_x][d_inf_index][h_x];
                                    e10_sup = tabData[t_x][f_x][d_sup_index][h_x];
                                    e10 = e10_inf + (e10_sup - e10_inf)*Math.log10((d/d_inf))/Math.log10((d_sup/d_inf));
                                }
                                if(d == d_inf) {
                                    e20 = tabData[t_x][f_x][d_inf_index][1];
                                } else {
                                    e20_inf = tabData[t_x][f_x][d_inf_index][h_x];
                                    e20_sup = tabData[t_x][f_x][d_sup_index][h_x];
                                    e20 = e20_inf + (e20_sup - e20_inf)*Math.log10((d/d_inf))/Math.log10((d_sup/d_inf));
                                }

                                switch (f_x) {
                                    case 0:  K_v = 1.35; break;
                                    case 1:  K_v = 3.31; break;
                                    case 2:  K_v = 6.00; break;
                                }
                                theta_eff2 = Math.atan((double)10/9000);
                                theta_eff2 *= (180/Math.PI);
                                v = K_v*theta_eff2;
                                C_h1neg10 = 6.03 - j(v);
                                C1020 = e10-e20;
                                e_zero = e10 + 0.5*(C1020+C_h1neg10);
                                rE = e_zero + 0.1*h*(e10-e_zero);

                                flag_A = 0;

                            } else {
                                if (h_x == h_sup_index) {
                                    e2_h = rE;
                                    h_x = h_inf_index;
                                    e_inf = e1_h;
                                    e_sup = e2_h;
                                    rE = e_inf + (e_sup - e_inf)*Math.log10((h/h_inf))/Math.log10((h_sup/h_inf));
                                    if (h>1200){
                                        if (rE>(106.9-20*Math.log10(d)))
                                            rE = 106.9-20*Math.log10(d);
                                    }
                                    flag_A = 0;
                                } else {
                                    h_x = h_sup_index;
                                    e1_h = rE;
                                }
                            }
                        }
                        flag_A = 1;

                        if (f == f_inf) {
                            flag_B = 0;
                        } else {
                            if (f_x == f_sup_index) {
                                e2_f = rE;
                                f_x = f_inf_index;
                                e_inf = e1_f;
                                e_sup = e2_f;
                                rE = e_inf + (e_sup - e_inf)*Math.log10((f/f_inf))/Math.log10((f_sup/f_inf));
                                if (f>2000){
                                    if (rE>(106.9-20*Math.log10(d)))
                                        rE = 106.9-20*Math.log10(d);
                                }
                                flag_B = 0;
                            } else {
                                f_x = f_sup_index;
                                e1_f = rE;
                            }
                        }
                    }
                    flag_B = 1;

                    if (t == t_inf) {
                        flag_C = 0;
                    } else {
                        if (t_x == t_sup_index) {
                            e2_t = rE;
                            t_x = t_inf_index;
                            e_inf = e1_t;
                            e_sup = e2_t;
                            rE = e_sup*(Qi(t_inf/100)-Qi(t/100))/(Qi(t_inf/100)-Qi(t_sup/100)) + e_inf*(Qi(t/100)-Qi(t_sup/100))/(Qi(t_inf/100)-Qi(t_sup/100));
                            flag_C = 0;
                        } else {
                            t_x = t_sup_index;
                            e1_t = rE;
                        }
                    }
                }
                E_comp = rE;
                //check max power
                if (E_comp>tabData[t_x][f_x][d_sup_index][8]) {
                    E_comp = tabData[t_x][f_x][d_sup_index][8];
                }

            } else {
                rE = less1km(d, t, f, h, gain);
            }

            E_comp = rE;
            return E_comp;
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static double[][] tabIndex = new double[][] {
            {1,10,50},
            {100,600,2000},
            {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190, 200, 225, 250, 275, 300, 325, 350, 375, 400, 425, 450, 475, 500, 525, 550, 575, 600, 625, 650, 675, 700, 725, 750, 775, 800, 825, 850, 875, 900, 925, 950, 975, 1000,},
            {10, 20, 37.5, 75, 150, 300, 600, 1200},
    };

    public static double[][][][] tabData = new double[][][][] {
            {
                    {// 100 MHz 1%
                            {89.976, 92.181, 94.636, 97.385, 100.318, 103.121, 105.243, 106.357, 106.900},
                            {80.275, 83.091, 86.080, 89.407, 92.913, 96.331, 98.981, 100.326, 100.879},
                            {74.166, 77.530, 80.898, 84.623, 88.495, 92.298, 95.296, 96.797, 97.358},
                            {69.518, 73.355, 77.049, 81.110, 85.279, 89.384, 92.660, 94.289, 94.859},
                            {65.699, 69.921, 73.942, 78.287, 82.714, 87.077, 90.596, 92.340, 92.921},
                            {62.436, 66.958, 71.318, 75.901, 80.555, 85.147, 88.888, 90.743, 91.337},
                            {59.648, 64.332, 69.040, 73.818, 78.672, 83.473, 87.424, 89.390, 89.998},
                            {57.462, 62.166, 67.024, 71.961, 76.990, 81.983, 86.135, 88.212, 88.838},
                            {55.541, 60.276, 65.216, 70.280, 75.462, 80.630, 84.977, 87.170, 87.815},
                            {53.831, 58.580, 63.577, 68.742, 74.055, 79.383, 83.920, 86.232, 86.900},
                            {52.292, 57.043, 62.078, 67.322, 72.747, 78.221, 82.942, 85.380, 86.072},
                            {50.899, 55.641, 60.699, 66.002, 71.522, 77.128, 82.029, 84.597, 85.316},
                            {49.627, 54.353, 59.421, 64.768, 70.367, 76.093, 81.168, 83.871, 84.621},
                            {48.461, 53.165, 58.232, 63.609, 69.273, 75.106, 80.350, 83.195, 83.977},
                            {47.388, 52.062, 57.120, 62.516, 68.232, 74.161, 79.568, 82.560, 83.378},
                            {46.396, 51.037, 56.077, 61.482, 67.238, 73.252, 78.817, 81.962, 82.818},
                            {45.476, 50.078, 55.096, 60.500, 66.286, 72.376, 78.093, 81.394, 82.291},
                            {44.620, 49.181, 54.169, 59.565, 65.373, 71.528, 77.391, 80.854, 81.795},
                            {43.824, 48.338, 53.292, 58.674, 64.494, 70.706, 76.708, 80.339, 81.325},
                            {43.080, 47.545, 52.461, 57.821, 63.646, 69.907, 76.042, 79.844, 80.879},
                            {40.004, 44.183, 48.853, 54.039, 59.802, 66.201, 72.907, 77.620, 78.941},
                            {37.729, 41.577, 45.946, 50.878, 56.472, 62.874, 69.995, 75.675, 77.358},
                            {36.004, 39.504, 43.545, 48.183, 53.544, 59.850, 67.239, 73.887, 76.019},
                            {34.667, 37.822, 41.534, 45.863, 50.957, 57.096, 64.619, 72.176, 74.859},
                            {33.609, 36.437, 39.833, 43.857, 48.669, 54.596, 62.136, 70.489, 73.836},
                            {32.751, 35.278, 38.381, 42.117, 46.649, 52.335, 59.801, 68.796, 72.921},
                            {32.037, 34.294, 37.133, 40.602, 44.866, 50.299, 57.618, 67.087, 72.093},
                            {31.426, 33.444, 36.047, 39.275, 43.290, 48.468, 55.592, 65.366, 71.337},
                            {30.888, 32.695, 35.092, 38.106, 41.892, 46.823, 53.718, 63.647, 70.642},
                            {30.399, 32.023, 34.240, 37.065, 40.643, 45.341, 51.990, 61.947, 69.998},
                            {29.944, 31.408, 33.470, 36.128, 39.521, 44.000, 50.396, 60.281, 69.399},
                            {29.509, 30.836, 32.762, 35.274, 38.503, 42.780, 48.925, 58.662, 68.838},
                            {29.085, 30.294, 32.103, 34.488, 37.570, 41.663, 47.565, 57.098, 68.312},
                            {28.666, 29.773, 31.482, 33.755, 36.706, 40.633, 46.302, 55.595, 67.815},
                            {28.247, 29.267, 30.888, 33.063, 35.899, 39.675, 45.125, 54.155, 67.346},
                            {27.826, 28.770, 30.315, 32.404, 35.138, 38.778, 44.023, 52.777, 66.900},
                            {26.967, 27.790, 29.209, 31.156, 33.719, 37.126, 42.005, 50.201, 66.072},
                            {26.083, 26.814, 28.135, 29.970, 32.396, 35.615, 40.184, 47.843, 65.316},
                            {25.173, 25.831, 27.073, 28.818, 31.137, 34.202, 38.509, 45.674, 64.621},
                            {24.238, 24.838, 26.015, 27.688, 29.918, 32.858, 36.945, 43.666, 63.977},
                            {23.282, 23.834, 24.955, 26.569, 28.727, 31.564, 35.467, 41.794, 63.378},
                            {22.307, 22.818, 23.893, 25.457, 27.556, 30.307, 34.055, 40.036, 62.818},
                            {21.316, 21.793, 22.828, 24.351, 26.401, 29.080, 32.697, 38.374, 62.291},
                            {20.312, 20.761, 21.761, 23.249, 25.258, 27.878, 31.383, 36.794, 61.795},
                            {19.299, 19.724, 20.695, 22.153, 24.127, 26.696, 30.106, 35.285, 61.325},
                            {18.279, 18.684, 19.630, 21.063, 23.008, 25.534, 28.861, 33.837, 60.879},
                            {15.719, 16.089, 16.987, 18.372, 20.260, 22.703, 25.871, 30.439, 59.856},
                            {13.173, 13.522, 14.386, 15.738, 17.586, 19.971, 23.026, 27.296, 58.941},
                            {10.667, 11.000, 11.841, 13.170, 14.990, 17.332, 20.304, 24.352, 58.113},
                            {8.211, 8.533, 9.358, 10.669, 12.469, 14.780, 17.690, 21.568, 57.358},
                            {5.809, 6.122, 6.935, 8.234, 10.017, 12.304, 15.167, 18.915, 56.662},
                            {3.457, 3.763, 4.567, 5.856, 7.627, 9.896, 12.722, 16.367, 56.019},
                            {1.150, 1.449, 2.247, 3.528, 5.290, 7.544, 10.341, 13.904, 55.419},
                            {-1.122, -0.827, -0.035, 1.240, 2.994, 5.237, 8.010, 11.508, 54.859},
                            {-3.366, -3.076, -2.288, -1.018, 0.730, 2.963, 5.718, 9.161, 54.332},
                            {-5.593, -5.306, -4.521, -3.255, -1.512, 0.713, 3.452, 6.851, 53.836},
                            {-7.810, -7.526, -6.744, -5.482, -3.743, -1.524, 1.202, 4.563, 53.366},
                            {-10.026, -9.744, -8.965, -7.705, -5.970, -3.757, -1.041, 2.289, 52.921},
                            {-12.248, -11.967, -11.190, -9.933, -8.201, -5.992, -3.286, 0.018, 52.497},
                            {-14.480, -14.202, -13.426, -12.171, -10.441, -8.236, -5.538, -2.257, 52.093},
                            {-16.728, -16.451, -15.677, -14.423, -12.695, -10.494, -7.803, -4.541, 51.707},
                            {-18.993, -18.717, -17.945, -16.693, -14.967, -12.768, -10.083, -6.838, 51.337},
                            {-21.278, -21.004, -20.232, -18.981, -17.257, -15.061, -12.381, -9.150, 50.982},
                            {-23.582, -23.309, -22.538, -21.288, -19.566, -17.372, -14.696, -11.479, 50.642},
                            {-25.904, -25.631, -24.861, -23.612, -21.891, -19.699, -17.027, -13.821, 50.314},
                            {-28.239, -27.967, -27.198, -25.950, -24.230, -22.040, -19.371, -16.175, 49.998},
                            {-30.584, -30.313, -29.545, -28.297, -26.578, -24.389, -21.724, -18.536, 49.693},
                            {-32.933, -32.662, -31.894, -30.648, -28.929, -26.742, -24.079, -20.899, 49.399},
                            {-35.277, -35.007, -34.240, -32.994, -31.276, -29.090, -26.430, -23.257, 49.114},
                            {-37.610, -37.340, -36.573, -35.328, -33.611, -31.426, -28.768, -25.602, 48.838},
                            {-39.922, -39.652, -38.886, -37.641, -35.925, -33.740, -31.084, -27.924, 48.571},
                            {-42.203, -41.933, -41.168, -39.923, -38.207, -36.024, -33.370, -30.215, 48.312},
                            {-44.443, -44.174, -43.409, -42.165, -40.450, -38.267, -35.614, -32.464, 48.060},
                            {-46.633, -46.365, -45.600, -44.356, -42.641, -40.459, -37.808, -34.662, 47.815},
                            {-48.763, -48.494, -47.730, -46.486, -44.772, -42.591, -39.941, -36.798, 47.577},
                            {-50.822, -50.554, -49.790, -48.547, -46.833, -44.652, -42.003, -38.864, 47.346},
                            {-52.803, -52.535, -51.771, -50.528, -48.815, -46.635, -43.987, -40.851, 47.120},
                            {-54.698, -54.430, -53.666, -52.424, -50.710, -48.531, -45.884, -42.751, 46.900},
                    },
                    {// 600 MHz 1%
                            {92.788, 94.892, 97.076, 99.699, 102.345, 104.591, 106.007, 106.629, 106.900},
                            {82.390, 85.130, 87.816, 91.033, 94.401, 97.503, 99.622, 100.542, 100.879},
                            {76.031, 79.199, 82.230, 85.816, 89.602, 93.221, 95.829, 96.974, 97.358},
                            {71.287, 74.801, 78.119, 82.004, 86.105, 90.104, 93.101, 94.437, 94.859},
                            {67.459, 71.245, 74.808, 78.950, 83.315, 87.625, 90.956, 92.465, 92.921},
                            {64.233, 68.233, 72.001, 76.367, 80.963, 85.543, 89.173, 90.848, 91.337},
                            {61.442, 65.608, 69.545, 74.107, 78.907, 83.727, 87.637, 89.475, 89.998},
                            {58.981, 63.277, 67.353, 72.083, 77.064, 82.101, 86.274, 88.279, 88.838},
                            {56.781, 61.178, 65.368, 70.241, 75.382, 80.614, 85.040, 87.217, 87.815},
                            {54.794, 59.270, 63.553, 68.547, 73.827, 79.235, 83.902, 86.259, 86.900},
                            {52.982, 57.520, 61.879, 66.974, 72.376, 77.942, 82.838, 85.382, 86.072},
                            {51.320, 55.907, 60.326, 65.506, 71.013, 76.718, 81.832, 84.572, 85.316},
                            {49.785, 54.410, 58.878, 64.129, 69.726, 75.555, 80.872, 83.816, 84.621},
                            {48.361, 53.016, 57.524, 62.833, 68.505, 74.443, 79.950, 83.103, 83.977},
                            {47.035, 51.712, 56.251, 61.608, 67.345, 73.377, 79.059, 82.428, 83.378},
                            {45.794, 50.488, 55.052, 60.449, 66.240, 72.354, 78.196, 81.783, 82.818},
                            {44.630, 49.336, 53.919, 59.348, 65.184, 71.369, 77.358, 81.193, 82.291},
                            {43.535, 48.249, 52.846, 58.300, 64.174, 70.420, 76.542, 80.637, 81.795},
                            {42.502, 47.220, 51.828, 57.302, 63.207, 69.506, 75.747, 80.104, 81.325},
                            {41.527, 46.246, 50.860, 56.349, 62.279, 68.623, 74.987, 79.591, 80.879},
                            {37.346, 42.033, 46.640, 52.150, 58.144, 64.629, 71.455, 77.239, 78.941},
                            {34.052, 38.661, 43.212, 48.682, 54.670, 61.210, 68.237, 75.108, 77.358},
                            {31.393, 35.890, 40.350, 45.739, 51.677, 58.225, 65.349, 73.200, 76.019},
                            {29.207, 33.564, 37.907, 43.185, 49.043, 55.568, 62.793, 71.296, 74.859},
                            {27.383, 31.579, 35.785, 40.927, 46.680, 53.158, 60.456, 69.318, 73.836},
                            {25.843, 29.860, 33.912, 38.900, 44.527, 50.940, 58.287, 67.213, 72.921},
                            {24.524, 28.352, 32.239, 37.057, 42.543, 48.871, 56.248, 64.966, 72.093},
                            {23.382, 27.014, 30.728, 35.366, 40.697, 46.924, 54.311, 62.835, 71.337},
                            {22.380, 25.814, 29.351, 33.803, 38.970, 45.081, 52.456, 61.203, 70.642},
                            {21.491, 24.729, 28.089, 32.352, 37.348, 43.329, 50.672, 59.607, 69.998},
                            {20.692, 23.738, 26.924, 30.999, 35.819, 41.661, 48.950, 58.041, 69.399},
                            {19.965, 22.826, 25.843, 29.733, 34.377, 40.071, 47.286, 56.497, 68.838},
                            {19.295, 21.981, 24.836, 28.546, 33.016, 38.554, 45.677, 54.975, 68.312},
                            {18.671, 21.192, 23.893, 27.430, 31.728, 37.108, 44.122, 53.471, 67.815},
                            {18.085, 20.450, 23.005, 26.378, 30.510, 35.730, 42.620, 51.988, 67.346},
                            {17.527, 19.748, 22.167, 25.384, 29.356, 34.416, 41.172, 50.527, 66.900},
                            {16.476, 18.440, 20.613, 23.546, 27.220, 31.969, 38.431, 47.675, 66.072},
                            {15.482, 17.228, 19.189, 21.876, 25.284, 29.739, 35.891, 44.932, 65.316},
                            {14.522, 16.084, 17.865, 20.338, 23.512, 27.699, 33.539, 42.310, 64.621},
                            {13.581, 14.987, 16.614, 18.906, 21.876, 25.820, 31.358, 39.815, 63.977},
                            {12.649, 13.922, 15.419, 17.555, 20.349, 24.076, 29.331, 37.449, 63.378},
                            {11.721, 12.880, 14.265, 16.269, 18.911, 22.447, 27.438, 35.210, 62.818},
                            {10.792, 11.853, 13.143, 15.033, 17.544, 20.911, 25.663, 33.091, 62.291},
                            {9.862, 10.837, 12.045, 13.837, 16.235, 19.455, 23.989, 31.086, 61.795},
                            {8.930, 9.828, 10.965, 12.674, 14.974, 18.065, 22.404, 29.185, 61.325},
                            {7.994, 8.825, 9.901, 11.538, 13.753, 16.731, 20.895, 27.380, 60.879},
                            {5.644, 6.338, 7.292, 8.788, 10.837, 13.589, 17.392, 23.230, 59.856},
                            {3.288, 3.878, 4.744, 6.139, 8.069, 10.656, 14.188, 19.503, 58.941},
                            {0.937, 1.448, 2.250, 3.572, 5.415, 7.880, 11.207, 16.105, 58.113},
                            {-1.396, -0.946, -0.192, 1.075, 2.853, 5.227, 8.396, 12.965, 57.358},
                            {-3.705, -3.301, -2.583, -1.357, 0.372, 2.676, 5.723, 10.029, 56.662},
                            {-5.985, -5.616, -4.926, -3.731, -2.040, 0.209, 3.160, 7.256, 56.019},
                            {-8.235, -7.892, -7.224, -6.054, -4.393, -2.187, 0.688, 4.614, 55.419},
                            {-10.456, -10.134, -9.483, -8.332, -6.695, -4.523, -1.709, 2.079, 54.859},
                            {-12.651, -12.345, -11.708, -10.572, -8.954, -6.810, -4.046, -0.372, 54.332},
                            {-14.825, -14.532, -13.905, -12.782, -11.180, -9.059, -6.335, -2.755, 53.836},
                            {-16.983, -16.700, -16.082, -14.970, -13.380, -11.278, -8.588, -5.086, 53.366},
                            {-19.130, -18.856, -18.245, -17.141, -15.562, -13.476, -10.814, -7.378, 52.921},
                            {-21.273, -21.005, -20.400, -19.304, -17.734, -15.660, -13.022, -9.642, 52.497},
                            {-23.415, -23.153, -22.553, -21.463, -19.900, -17.838, -15.220, -11.888, 52.093},
                            {-25.562, -25.304, -24.708, -23.623, -22.068, -20.015, -17.414, -14.122, 51.707},
                            {-27.716, -27.463, -26.871, -25.790, -24.240, -22.195, -19.610, -16.353, 51.337},
                            {-29.882, -29.632, -29.043, -27.966, -26.420, -24.383, -21.810, -18.584, 50.982},
                            {-32.059, -31.812, -31.226, -30.152, -28.611, -26.579, -24.018, -20.818, 50.642},
                            {-34.249, -34.004, -33.420, -32.350, -30.812, -28.786, -26.234, -23.057, 50.314},
                            {-36.450, -36.208, -35.626, -34.558, -33.023, -31.002, -28.459, -25.302, 49.998},
                            {-38.661, -38.420, -37.840, -36.774, -35.243, -33.225, -30.690, -27.551, 49.693},
                            {-40.877, -40.638, -40.059, -38.995, -37.466, -35.452, -32.924, -29.801, 49.399},
                            {-43.093, -42.856, -42.279, -41.217, -39.690, -37.679, -35.157, -32.049, 49.114},
                            {-45.305, -45.069, -44.493, -43.433, -41.908, -39.900, -37.383, -34.287, 48.838},
                            {-47.505, -47.270, -46.695, -45.636, -44.113, -42.108, -39.595, -36.511, 48.571},
                            {-49.684, -49.450, -48.877, -47.819, -46.297, -44.294, -41.786, -38.712, 48.312},
                            {-51.835, -51.602, -51.029, -49.973, -48.452, -46.452, -43.947, -40.883, 48.060},
                            {-53.948, -53.716, -53.144, -52.089, -50.570, -48.571, -46.070, -43.014, 47.815},
                            {-56.015, -55.784, -55.213, -54.158, -52.640, -50.643, -48.145, -45.097, 47.577},
                            {-58.026, -57.795, -57.225, -56.171, -54.654, -52.658, -50.163, -47.122, 47.346},
                            {-59.971, -59.742, -59.172, -58.119, -56.603, -54.609, -52.116, -49.081, 47.120},
                            {-61.844, -61.615, -61.045, -59.993, -58.478, -56.485, -53.995, -50.966, 46.900},
                    },
                    {// 2000 MHz 1%
                            {94.233, 96.509, 98.662, 101.148, 103.509, 105.319, 106.328, 106.732, 106.900},
                            {82.711, 86.063, 88.943, 92.187, 95.445, 98.251, 99.972, 100.647, 100.879},
                            {75.466, 79.573, 82.996, 86.732, 90.502, 93.925, 96.182, 97.077, 97.358},
                            {70.027, 74.676, 78.565, 82.726, 86.888, 90.763, 93.451, 94.539, 94.859},
                            {65.657, 70.683, 74.957, 79.501, 84.003, 88.248, 91.304, 92.565, 92.921},
                            {62.006, 67.294, 71.875, 76.760, 81.573, 86.141, 89.526, 90.949, 91.337},
                            {58.874, 64.345, 69.167, 74.349, 79.451, 84.314, 88.001, 89.580, 89.998},
                            {56.136, 61.735, 66.743, 72.180, 77.547, 82.686, 86.660, 88.391, 88.838},
                            {53.705, 59.397, 64.548, 70.199, 75.807, 81.206, 85.457, 87.339, 87.815},
                            {51.522, 57.280, 62.541, 68.369, 74.194, 79.839, 84.359, 86.395, 86.900},
                            {49.543, 55.348, 60.693, 66.667, 72.684, 78.558, 83.344, 85.538, 86.072},
                            {47.736, 53.573, 58.982, 65.075, 71.260, 77.348, 82.394, 84.751, 85.316},
                            {46.073, 51.933, 57.390, 63.580, 69.909, 76.194, 81.495, 84.023, 84.621},
                            {44.536, 50.409, 55.903, 62.170, 68.623, 75.088, 80.638, 83.345, 83.977},
                            {43.109, 48.989, 54.508, 60.836, 67.396, 74.022, 79.815, 82.708, 83.378},
                            {41.777, 47.658, 53.196, 59.572, 66.220, 72.993, 79.018, 82.107, 82.818},
                            {40.531, 46.409, 51.957, 58.371, 65.094, 71.996, 78.243, 81.536, 82.291},
                            {39.362, 45.232, 50.786, 57.227, 64.011, 71.028, 77.487, 80.991, 81.795},
                            {38.261, 44.120, 49.675, 56.136, 62.971, 70.088, 76.745, 80.469, 81.325},
                            {37.223, 43.068, 48.619, 55.093, 61.969, 69.173, 76.017, 79.966, 80.879},
                            {32.791, 38.529, 44.017, 50.483, 57.457, 64.947, 72.530, 77.659, 78.941},
                            {29.313, 34.900, 40.271, 46.649, 53.606, 61.212, 69.260, 75.604, 77.358},
                            {26.509, 31.910, 37.130, 43.372, 50.246, 57.871, 66.197, 73.633, 76.019},
                            {24.203, 29.394, 34.437, 40.508, 47.258, 54.843, 63.329, 71.641, 74.859},
                            {22.275, 27.239, 32.087, 37.965, 44.561, 52.066, 60.636, 69.571, 73.836},
                            {20.641, 25.367, 30.009, 35.677, 42.098, 49.494, 58.096, 67.447, 72.921},
                            {19.239, 23.722, 28.150, 33.599, 39.830, 47.095, 55.688, 65.425, 72.093},
                            {18.021, 22.260, 26.473, 31.696, 37.727, 44.844, 53.398, 63.427, 71.337},
                            {16.951, 20.949, 24.948, 29.945, 35.771, 42.726, 51.214, 61.454, 70.642},
                            {16.000, 19.764, 23.554, 28.327, 33.944, 40.727, 49.126, 59.511, 69.998},
                            {15.145, 18.684, 22.271, 26.825, 32.235, 38.839, 47.129, 57.605, 69.399},
                            {14.368, 17.692, 21.086, 25.427, 30.632, 37.054, 45.218, 55.738, 68.838},
                            {13.655, 16.776, 19.985, 24.122, 29.128, 35.364, 43.389, 53.914, 68.312},
                            {12.994, 15.924, 18.957, 22.901, 27.713, 33.764, 41.639, 52.135, 67.815},
                            {12.375, 15.126, 17.995, 21.754, 26.380, 32.249, 39.964, 50.402, 67.346},
                            {11.791, 14.375, 17.089, 20.675, 25.123, 30.812, 38.362, 48.717, 66.900},
                            {10.700, 12.985, 15.421, 18.690, 22.808, 28.153, 35.364, 45.491, 66.072},
                            {9.683, 11.711, 13.906, 16.898, 20.722, 25.747, 32.617, 42.457, 65.316},
                            {8.715, 10.523, 12.510, 15.262, 18.825, 23.557, 30.096, 39.612, 64.621},
                            {7.777, 9.397, 11.205, 13.748, 17.082, 21.550, 27.774, 36.945, 63.977},
                            {6.859, 8.317, 9.971, 12.333, 15.466, 19.698, 25.627, 34.447, 63.378},
                            {5.951, 7.270, 8.792, 10.997, 13.955, 17.974, 23.632, 32.104, 62.818},
                            {5.051, 6.249, 7.656, 9.725, 12.529, 16.360, 21.768, 29.903, 62.291},
                            {4.153, 5.247, 6.554, 8.505, 11.173, 14.837, 20.017, 27.831, 61.795},
                            {3.257, 4.260, 5.480, 7.328, 9.877, 13.392, 18.366, 25.876, 61.325},
                            {2.362, 3.284, 4.428, 6.186, 8.631, 12.013, 16.800, 24.025, 60.879},
                            {0.125, 0.885, 1.876, 3.452, 5.686, 8.798, 13.191, 19.788, 59.856},
                            {-2.108, -1.470, -0.591, 0.851, 2.928, 5.835, 9.922, 16.003, 58.941},
                            {-4.330, -3.785, -2.991, -1.650, 0.309, 3.058, 6.905, 12.567, 58.113},
                            {-6.534, -6.061, -5.332, -4.069, -2.202, 0.425, 4.084, 9.403, 57.358},
                            {-8.715, -8.299, -7.621, -6.417, -4.622, -2.092, 1.415, 6.456, 56.662},
                            {-10.870, -10.499, -9.861, -8.705, -6.967, -4.514, -1.129, 3.683, 56.019},
                            {-12.999, -12.663, -12.058, -10.941, -9.248, -6.858, -3.572, 1.050, 55.419},
                            {-15.104, -14.797, -14.217, -13.131, -11.476, -9.137, -5.933, -1.470, 54.859},
                            {-17.187, -16.903, -16.345, -15.285, -13.661, -11.364, -8.229, -3.897, 54.332},
                            {-19.253, -18.989, -18.449, -17.410, -15.811, -13.549, -10.471, -6.252, 53.836},
                            {-21.307, -21.060, -20.534, -19.513, -17.936, -15.704, -12.675, -8.551, 53.366},
                            {-23.356, -23.122, -22.609, -21.603, -20.045, -17.838, -14.850, -10.807, 52.921},
                            {-25.405, -25.183, -24.680, -23.687, -22.144, -19.959, -17.006, -13.034, 52.497},
                            {-27.461, -27.248, -26.754, -25.772, -24.243, -22.077, -19.154, -15.243, 52.093},
                            {-29.527, -29.323, -28.837, -27.865, -26.347, -24.197, -21.301, -17.443, 51.707},
                            {-31.610, -31.413, -30.934, -29.970, -28.463, -26.327, -23.454, -19.642, 51.337},
                            {-33.714, -33.523, -33.050, -32.093, -30.595, -28.471, -25.618, -21.848, 50.982},
                            {-35.840, -35.654, -35.187, -34.237, -32.746, -30.633, -27.798, -24.064, 50.642},
                            {-37.990, -37.810, -37.347, -36.402, -34.919, -32.815, -29.996, -26.294, 50.314},
                            {-40.165, -39.989, -39.530, -38.591, -37.113, -35.018, -32.213, -28.540, 49.998},
                            {-42.363, -42.190, -41.735, -40.800, -39.328, -37.241, -34.449, -30.801, 49.693},
                            {-44.579, -44.410, -43.959, -43.027, -41.560, -39.480, -36.699, -33.074, 49.399},
                            {-46.810, -46.644, -46.195, -45.268, -43.805, -41.731, -38.960, -35.356, 49.114},
                            {-49.047, -48.884, -48.438, -47.514, -46.055, -43.986, -41.225, -37.639, 48.838},
                            {-51.283, -51.122, -50.679, -49.758, -48.302, -46.239, -43.485, -39.917, 48.571},
                            {-53.507, -53.349, -52.907, -51.989, -50.537, -48.478, -45.732, -42.179, 48.312},
                            {-55.708, -55.552, -55.113, -54.196, -52.748, -50.693, -47.954, -44.414, 48.060},
                            {-57.874, -57.720, -57.282, -56.368, -54.922, -52.871, -50.138, -46.612, 47.815},
                            {-59.992, -59.839, -59.403, -58.491, -57.048, -55.000, -52.273, -48.758, 47.577},
                            {-62.049, -61.898, -61.463, -60.553, -59.112, -57.067, -54.345, -50.841, 47.346},
                            {-64.033, -63.883, -63.450, -62.541, -61.102, -59.060, -56.343, -52.849, 47.120},
                            {-65.931, -65.783, -65.351, -64.444, -63.007, -60.968, -58.255, -54.770, 46.900},
                    },
            },
            {
                    {// 100 MHz 10%
                            {89.976, 92.181, 94.636, 97.385, 100.318, 103.121, 105.243, 106.357, 106.900},
                            {80.275, 83.091, 86.001, 89.208, 92.674, 96.120, 98.858, 100.285, 100.879},
                            {74.166, 77.530, 80.823, 84.350, 88.143, 91.969, 95.096, 96.731, 97.358},
                            {69.518, 73.355, 77.015, 80.831, 84.885, 88.993, 92.412, 94.208, 94.859},
                            {65.699, 69.921, 73.925, 78.021, 82.314, 86.660, 90.320, 92.250, 92.921},
                            {62.436, 66.958, 71.272, 75.641, 80.164, 84.727, 88.600, 90.649, 91.337},
                            {59.580, 64.332, 68.916, 73.542, 78.292, 83.063, 87.135, 89.294, 89.998},
                            {57.041, 61.967, 66.778, 71.642, 76.613, 81.589, 85.853, 88.119, 88.838},
                            {54.756, 59.814, 64.813, 69.890, 75.073, 80.252, 84.707, 87.080, 87.815},
                            {52.680, 57.838, 62.990, 68.255, 73.638, 79.018, 83.666, 86.148, 86.900},
                            {50.778, 56.013, 61.289, 66.716, 72.284, 77.859, 82.704, 85.301, 86.072},
                            {49.180, 54.364, 59.746, 65.286, 70.996, 76.760, 81.805, 84.525, 85.316},
                            {47.759, 52.947, 58.367, 63.988, 69.789, 75.706, 80.954, 83.807, 84.621},
                            {46.447, 51.630, 57.074, 62.759, 68.664, 74.690, 80.141, 83.136, 83.977},
                            {45.231, 50.401, 55.857, 61.591, 67.584, 73.727, 79.358, 82.506, 83.378},
                            {44.100, 49.251, 54.708, 60.477, 66.544, 72.807, 78.597, 81.910, 82.818},
                            {43.044, 48.170, 53.621, 59.412, 65.541, 71.912, 77.861, 81.343, 82.291},
                            {42.057, 47.152, 52.589, 58.393, 64.569, 71.038, 77.159, 80.801, 81.795},
                            {41.130, 46.191, 51.608, 57.415, 63.628, 70.183, 76.469, 80.289, 81.325},
                            {40.259, 45.283, 50.673, 56.475, 62.715, 69.345, 75.791, 79.796, 80.879},
                            {36.594, 41.383, 46.584, 52.272, 58.520, 65.373, 72.496, 77.546, 78.941},
                            {33.803, 38.310, 43.255, 48.733, 54.852, 61.735, 69.304, 75.494, 77.358},
                            {31.635, 35.836, 40.493, 45.712, 51.624, 58.418, 66.224, 73.502, 76.019},
                            {29.923, 33.812, 38.169, 43.107, 48.774, 55.405, 63.332, 71.501, 74.859},
                            {28.551, 32.133, 36.192, 40.844, 46.248, 52.676, 60.747, 69.469, 73.836},
                            {27.432, 30.721, 34.494, 38.865, 44.002, 50.206, 58.301, 67.414, 72.921},
                            {26.500, 29.516, 33.018, 37.122, 41.999, 47.967, 55.958, 65.360, 72.093},
                            {25.707, 28.470, 31.722, 35.575, 40.203, 45.934, 53.684, 63.382, 71.337},
                            {25.015, 27.547, 30.570, 34.191, 38.584, 44.083, 51.458, 61.540, 70.642},
                            {24.394, 26.718, 29.533, 32.941, 37.115, 42.390, 49.441, 59.705, 69.998},
                            {23.823, 25.960, 28.587, 31.800, 35.773, 40.835, 47.670, 57.866, 69.399},
                            {23.285, 25.255, 27.712, 30.749, 34.537, 39.398, 46.017, 56.017, 68.838},
                            {22.768, 24.588, 26.892, 29.771, 33.389, 38.062, 44.469, 54.155, 68.312},
                            {22.263, 23.949, 26.116, 28.851, 32.314, 36.812, 43.014, 52.391, 67.815},
                            {21.762, 23.329, 25.373, 27.978, 31.300, 35.637, 41.641, 50.805, 67.346},
                            {21.262, 22.721, 24.655, 27.142, 30.337, 34.524, 40.340, 49.282, 66.900},
                            {20.249, 21.527, 23.270, 25.555, 28.527, 32.450, 37.919, 46.410, 66.072},
                            {19.211, 20.340, 21.927, 24.046, 26.834, 30.533, 35.696, 43.745, 65.316},
                            {18.142, 19.149, 20.605, 22.587, 25.222, 28.732, 33.627, 41.260, 64.621},
                            {17.044, 17.947, 19.295, 21.162, 23.668, 27.019, 31.680, 38.929, 63.977},
                            {15.919, 16.734, 17.991, 19.762, 22.160, 25.375, 29.834, 36.732, 63.378},
                            {14.773, 15.513, 16.691, 18.381, 20.689, 23.788, 28.070, 34.653, 62.818},
                            {13.611, 14.286, 15.398, 17.019, 19.249, 22.249, 26.378, 32.677, 62.291},
                            {12.437, 13.057, 14.112, 15.674, 17.838, 20.752, 24.748, 30.792, 61.795},
                            {11.259, 11.831, 12.836, 14.347, 16.455, 19.295, 23.174, 28.991, 61.325},
                            {10.079, 10.609, 11.572, 13.040, 15.098, 17.874, 21.651, 27.264, 60.879},
                            {7.150, 7.599, 8.478, 9.861, 11.822, 14.470, 18.041, 23.232, 59.856},
                            {4.285, 4.676, 5.496, 6.817, 8.708, 11.263, 14.680, 19.550, 58.941},
                            {1.511, 1.861, 2.637, 3.912, 5.751, 8.236, 11.537, 16.156, 58.113},
                            {-1.162, -0.844, -0.101, 1.140, 2.939, 5.370, 8.581, 13.004, 57.358},
                            {-3.736, -3.442, -2.724, -1.510, 0.258, 2.647, 5.787, 10.054, 56.662},
                            {-6.218, -5.944, -5.246, -4.053, -2.310, 0.046, 3.130, 7.271, 56.019},
                            {-8.622, -8.363, -7.681, -6.505, -4.782, -2.452, 0.586, 4.624, 55.419},
                            {-10.961, -10.714, -10.045, -8.882, -7.175, -4.868, -1.867, 2.086, 54.859},
                            {-13.249, -13.012, -12.353, -11.202, -9.509, -7.220, -4.250, -0.367, 54.332},
                            {-15.500, -15.271, -14.622, -13.480, -11.798, -9.524, -6.580, -2.757, 53.836},
                            {-17.726, -17.505, -16.863, -15.729, -14.056, -11.795, -8.873, -5.101, 53.366},
                            {-19.941, -19.726, -19.090, -17.963, -16.298, -14.048, -11.144, -7.415, 52.921},
                            {-22.152, -21.942, -21.312, -20.191, -18.533, -16.292, -13.404, -9.712, 52.497},
                            {-24.369, -24.164, -23.538, -22.422, -20.769, -18.537, -15.663, -12.003, 52.093},
                            {-26.597, -26.396, -25.774, -24.662, -23.015, -20.789, -17.927, -14.295, 51.707},
                            {-28.840, -28.642, -28.024, -26.916, -25.273, -23.053, -20.202, -16.595, 51.337},
                            {-31.100, -30.905, -30.290, -29.185, -27.546, -25.332, -22.490, -18.904, 50.982},
                            {-33.377, -33.185, -32.572, -31.471, -29.835, -27.626, -24.792, -21.225, 50.642},
                            {-35.669, -35.479, -34.869, -33.770, -32.137, -29.932, -27.106, -23.555, 50.314},
                            {-37.971, -37.783, -37.175, -36.078, -34.448, -32.247, -29.427, -25.892, 49.998},
                            {-40.277, -40.091, -39.485, -38.390, -36.763, -34.565, -31.751, -28.229, 49.693},
                            {-42.580, -42.395, -41.791, -40.698, -39.073, -36.878, -34.069, -30.560, 49.399},
                            {-44.870, -44.687, -44.084, -42.993, -41.370, -39.178, -36.374, -32.875, 49.114},
                            {-47.137, -46.956, -46.355, -45.265, -43.644, -41.454, -38.654, -35.166, 48.838},
                            {-49.371, -49.191, -48.591, -47.503, -45.883, -43.695, -40.899, -37.420, 48.571},
                            {-51.560, -51.380, -50.782, -49.695, -48.077, -45.891, -43.098, -39.628, 48.312},
                            {-53.691, -53.513, -52.915, -51.829, -50.212, -48.029, -45.239, -41.776, 48.060},
                            {-55.754, -55.577, -54.980, -53.895, -52.280, -50.097, -47.311, -43.855, 47.815},
                            {-57.738, -57.561, -56.965, -55.882, -54.267, -52.087, -49.303, -45.853, 47.577},
                            {-59.632, -59.457, -58.862, -57.779, -56.165, -53.986, -51.205, -47.761, 47.346},
                            {-61.430, -61.255, -60.660, -59.578, -57.966, -55.788, -53.009, -49.570, 47.120},
                            {-63.123, -62.948, -62.355, -61.274, -59.662, -57.485, -54.709, -51.275, 46.900},
                    },
                    {// 600 MHz 10%
                            {92.788, 94.892, 97.076, 99.699, 102.345, 104.591, 106.007, 106.629, 106.900},
                            {81.956, 84.747, 87.449, 90.672, 94.076, 97.267, 99.511, 100.511, 100.879},
                            {74.848, 78.446, 81.617, 85.246, 89.076, 92.812, 95.623, 96.917, 97.358},
                            {69.340, 73.650, 77.292, 81.294, 85.451, 89.574, 92.819, 94.359, 94.859},
                            {64.860, 69.686, 73.762, 78.128, 82.577, 87.011, 90.613, 92.369, 92.921},
                            {61.111, 66.285, 70.727, 75.443, 80.171, 84.877, 88.786, 90.738, 91.337},
                            {57.905, 63.306, 68.041, 73.080, 78.076, 83.033, 87.219, 89.356, 89.998},
                            {55.112, 60.663, 65.622, 70.947, 76.202, 81.398, 85.842, 88.154, 88.838},
                            {52.644, 58.294, 63.421, 68.991, 74.491, 79.917, 84.607, 87.090, 87.815},
                            {50.438, 56.151, 61.403, 67.177, 72.904, 78.553, 83.481, 86.134, 86.900},
                            {48.448, 54.200, 59.543, 65.484, 71.416, 77.279, 82.440, 85.265, 86.072},
                            {46.638, 52.411, 57.819, 63.895, 70.010, 76.077, 81.467, 84.466, 85.316},
                            {44.982, 50.764, 56.216, 62.397, 68.673, 74.932, 80.549, 83.727, 84.621},
                            {43.459, 49.238, 54.719, 60.982, 67.395, 73.835, 79.675, 83.037, 83.977},
                            {42.051, 47.820, 53.316, 59.642, 66.172, 72.778, 78.835, 82.388, 83.378},
                            {40.746, 46.497, 51.998, 58.369, 64.997, 71.755, 78.025, 81.775, 82.818},
                            {39.531, 45.259, 50.757, 57.158, 63.866, 70.762, 77.239, 81.193, 82.291},
                            {38.398, 44.096, 49.583, 56.003, 62.777, 69.796, 76.473, 80.637, 81.795},
                            {37.338, 43.002, 48.472, 54.899, 61.725, 68.854, 75.723, 80.104, 81.325},
                            {36.344, 41.970, 47.417, 53.843, 60.708, 67.934, 74.987, 79.591, 80.879},
                            {32.186, 37.563, 42.829, 49.148, 56.071, 63.622, 71.455, 77.239, 78.941},
                            {29.036, 34.091, 39.096, 45.192, 52.015, 59.690, 68.237, 75.108, 77.358},
                            {26.584, 31.269, 35.962, 41.762, 48.386, 56.051, 65.125, 73.200, 76.019},
                            {24.632, 28.922, 33.274, 38.735, 45.095, 52.651, 61.999, 71.296, 74.859},
                            {23.045, 26.935, 30.938, 36.040, 42.094, 49.468, 58.862, 69.318, 73.836},
                            {21.725, 25.230, 28.891, 33.629, 39.356, 46.495, 55.739, 67.213, 72.921},
                            {20.605, 23.747, 27.083, 31.469, 36.864, 43.729, 52.782, 64.966, 72.093},
                            {19.631, 22.442, 25.478, 29.532, 34.600, 41.168, 50.059, 62.591, 71.337},
                            {18.766, 21.279, 24.043, 27.791, 32.545, 38.808, 47.476, 60.122, 70.642},
                            {17.980, 20.228, 22.749, 26.219, 30.680, 36.638, 45.042, 57.601, 69.998},
                            {17.252, 19.267, 21.572, 24.792, 28.984, 34.645, 42.757, 55.065, 69.399},
                            {16.564, 18.376, 20.490, 23.489, 27.434, 32.814, 40.618, 52.611, 68.838},
                            {15.905, 17.539, 19.487, 22.288, 26.012, 31.127, 38.619, 50.394, 68.312},
                            {15.265, 16.745, 18.546, 21.174, 24.698, 29.567, 36.751, 48.253, 67.815},
                            {14.638, 15.982, 17.656, 20.130, 23.476, 28.120, 35.005, 46.196, 67.346},
                            {14.017, 15.244, 16.806, 19.145, 22.332, 26.771, 33.370, 44.226, 66.900},
                            {12.784, 13.818, 15.196, 17.311, 20.231, 24.313, 30.390, 40.542, 66.072},
                            {11.547, 12.434, 13.669, 15.609, 18.317, 22.107, 27.731, 37.185, 65.316},
                            {10.300, 11.070, 12.194, 13.997, 16.535, 20.088, 25.326, 34.123, 64.621},
                            {9.039, 9.719, 10.754, 12.448, 14.851, 18.210, 23.122, 31.320, 63.977},
                            {7.768, 8.374, 9.339, 10.945, 13.239, 16.440, 21.077, 28.741, 63.378},
                            {6.488, 7.036, 7.943, 9.478, 11.683, 14.753, 19.161, 26.352, 62.818},
                            {5.204, 5.704, 6.564, 8.041, 10.173, 13.136, 17.349, 24.127, 62.291},
                            {3.919, 4.380, 5.201, 6.631, 8.703, 11.575, 15.624, 22.042, 61.795},
                            {2.638, 3.066, 3.855, 5.246, 7.267, 10.064, 13.974, 20.078, 61.325},
                            {1.364, 1.764, 2.527, 3.884, 5.863, 8.597, 12.388, 18.218, 60.879},
                            {-1.774, -1.424, -0.712, 0.583, 2.483, 5.096, 8.660, 13.949, 59.856},
                            {-4.816, -4.500, -3.822, -2.569, -0.723, 1.806, 5.211, 10.111, 58.941},
                            {-7.743, -7.452, -6.797, -5.575, -3.767, -1.296, 1.995, 6.610, 58.113},
                            {-10.550, -10.276, -9.638, -8.437, -6.657, -4.230, -1.021, 3.380, 57.358},
                            {-13.237, -12.976, -12.351, -11.166, -9.407, -7.011, -3.865, 0.375, 56.662},
                            {-15.812, -15.561, -14.945, -13.772, -12.029, -9.657, -6.560, -2.444, 56.019},
                            {-18.284, -18.041, -17.433, -16.269, -14.538, -12.186, -9.125, -5.108, 55.419},
                            {-20.668, -20.431, -19.828, -18.672, -16.950, -14.613, -11.582, -7.642, 54.859},
                            {-22.976, -22.743, -22.146, -20.995, -19.281, -16.956, -13.949, -10.072, 54.332},
                            {-25.222, -24.993, -24.400, -23.254, -21.546, -19.231, -16.243, -12.416, 53.836},
                            {-27.420, -27.194, -26.604, -25.462, -23.759, -21.452, -18.479, -14.695, 53.366},
                            {-29.583, -29.360, -28.772, -27.633, -25.935, -23.634, -20.675, -16.925, 52.921},
                            {-31.723, -31.502, -30.916, -29.780, -28.085, -25.790, -22.841, -19.121, 52.497},
                            {-33.851, -33.632, -33.048, -31.914, -30.222, -27.931, -24.992, -21.296, 52.093},
                            {-35.977, -35.760, -35.177, -34.045, -32.356, -30.069, -27.138, -23.463, 51.707},
                            {-38.110, -37.894, -37.313, -36.183, -34.495, -32.212, -29.287, -25.630, 51.337},
                            {-40.256, -40.041, -39.461, -38.333, -36.647, -34.367, -31.448, -27.806, 50.982},
                            {-42.421, -42.207, -41.628, -40.501, -38.817, -36.539, -33.625, -29.997, 50.642},
                            {-44.608, -44.395, -43.817, -42.691, -41.008, -38.733, -35.823, -32.207, 50.314},
                            {-46.819, -46.607, -46.029, -44.904, -43.223, -40.949, -38.043, -34.437, 49.998},
                            {-49.052, -48.840, -48.263, -47.139, -45.459, -43.186, -40.284, -36.687, 49.693},
                            {-51.305, -51.094, -50.517, -49.394, -47.714, -45.444, -42.544, -38.955, 49.399},
                            {-53.572, -53.362, -52.786, -51.663, -49.985, -47.715, -44.818, -41.236, 49.114},
                            {-55.848, -55.638, -55.063, -53.940, -52.262, -49.994, -47.100, -43.524, 48.838},
                            {-58.123, -57.913, -57.338, -56.216, -54.539, -52.272, -49.380, -45.810, 48.571},
                            {-60.386, -60.177, -59.603, -58.481, -56.805, -54.538, -51.648, -48.083, 48.312},
                            {-62.627, -62.418, -61.844, -60.723, -59.047, -56.781, -53.893, -50.332, 48.060},
                            {-64.832, -64.623, -64.049, -62.928, -61.253, -58.988, -56.101, -52.545, 47.815},
                            {-66.987, -66.779, -66.205, -65.085, -63.410, -61.146, -58.260, -54.707, 47.577},
                            {-69.080, -68.872, -68.298, -67.178, -65.504, -63.240, -60.356, -56.806, 47.346},
                            {-71.097, -70.889, -70.316, -69.196, -67.522, -65.259, -62.375, -58.829, 47.120},
                            {-73.026, -72.818, -72.245, -71.125, -69.452, -67.189, -64.307, -60.763, 46.900},
                    },
                    {// 2000 MHz 10%
                            {94.233, 96.509, 98.662, 101.148, 103.509, 105.319, 106.328, 106.732, 106.900},
                            {82.427, 85.910, 88.762, 92.000, 95.276, 98.138, 99.926, 100.635, 100.879},
                            {74.501, 79.135, 82.671, 86.423, 90.219, 93.718, 96.089, 97.054, 97.358},
                            {68.368, 73.847, 78.078, 82.310, 86.522, 90.481, 93.317, 94.505, 94.859},
                            {63.440, 69.412, 74.253, 79.006, 83.569, 87.906, 91.132, 92.522, 92.921},
                            {59.326, 65.580, 70.908, 76.172, 81.072, 85.751, 89.321, 90.898, 91.337},
                            {55.804, 62.216, 67.909, 73.643, 78.912, 83.880, 87.767, 89.521, 89.998},
                            {52.734, 59.227, 65.186, 71.329, 76.970, 82.211, 86.400, 88.324, 88.838},
                            {50.019, 56.544, 62.696, 69.181, 75.181, 80.707, 85.173, 87.266, 87.815},
                            {47.590, 54.149, 60.406, 67.169, 73.507, 79.331, 84.052, 86.316, 86.900},
                            {45.398, 51.989, 58.291, 65.277, 71.922, 78.043, 83.015, 85.452, 86.072},
                            {43.403, 50.015, 56.329, 63.490, 70.408, 76.822, 82.061, 84.660, 85.316},
                            {41.577, 48.200, 54.502, 61.800, 68.956, 75.653, 81.165, 83.927, 84.621},
                            {39.897, 46.523, 52.816, 60.198, 67.558, 74.524, 80.313, 83.244, 83.977},
                            {38.345, 44.967, 51.276, 58.677, 66.210, 73.429, 79.497, 82.608, 83.378},
                            {36.904, 43.519, 49.836, 57.231, 64.909, 72.361, 78.708, 82.009, 82.818},
                            {35.564, 42.166, 48.484, 55.853, 63.652, 71.316, 77.942, 81.442, 82.291},
                            {34.312, 40.898, 47.212, 54.557, 62.437, 70.293, 77.192, 80.903, 81.795},
                            {33.142, 39.706, 46.011, 53.367, 61.260, 69.289, 76.455, 80.387, 81.325},
                            {32.044, 38.584, 44.874, 52.235, 60.121, 68.303, 75.728, 79.893, 80.879},
                            {27.448, 33.810, 39.972, 47.266, 55.121, 63.628, 72.179, 77.642, 78.941},
                            {23.966, 30.064, 36.014, 43.134, 50.933, 59.475, 68.706, 75.604, 77.358},
                            {21.259, 27.018, 32.683, 39.544, 47.191, 55.784, 65.291, 73.633, 76.019},
                            {19.114, 24.470, 29.794, 36.325, 43.743, 52.298, 62.125, 71.641, 74.859},
                            {17.385, 22.298, 27.238, 33.389, 40.514, 48.952, 59.021, 69.571, 73.836},
                            {15.967, 20.421, 24.957, 30.694, 37.476, 45.725, 55.936, 67.447, 72.921},
                            {14.787, 18.784, 22.916, 28.226, 34.630, 42.624, 52.874, 65.223, 72.093},
                            {13.788, 17.350, 21.090, 25.978, 31.988, 39.673, 49.858, 62.869, 71.337},
                            {12.927, 16.087, 19.461, 23.944, 29.558, 36.896, 46.919, 60.403, 70.642},
                            {12.171, 14.968, 18.007, 22.113, 27.343, 34.310, 44.088, 57.862, 69.998},
                            {11.495, 13.969, 16.708, 20.471, 25.337, 31.926, 41.391, 55.285, 69.399},
                            {10.878, 13.069, 15.543, 18.997, 23.527, 29.743, 38.847, 52.709, 68.838},
                            {10.305, 12.251, 14.493, 17.673, 21.895, 27.753, 36.467, 50.169, 68.312},
                            {9.764, 11.497, 13.537, 16.476, 20.422, 25.942, 34.254, 47.690, 67.815},
                            {9.245, 10.796, 12.661, 15.389, 19.087, 24.294, 32.204, 45.293, 67.346},
                            {8.741, 10.134, 11.848, 14.392, 17.871, 22.792, 30.310, 42.991, 66.900},
                            {7.755, 8.897, 10.367, 12.611, 15.726, 20.153, 26.943, 38.707, 66.072},
                            {6.775, 7.731, 9.018, 11.035, 13.867, 17.896, 24.054, 34.861, 65.316},
                            {5.784, 6.599, 7.748, 9.591, 12.205, 15.917, 21.542, 31.431, 64.621},
                            {4.772, 5.480, 6.524, 8.234, 10.677, 14.137, 19.320, 28.371, 63.977},
                            {3.738, 4.362, 5.323, 6.930, 9.240, 12.499, 17.319, 25.629, 63.378},
                            {2.679, 3.238, 4.135, 5.660, 7.865, 10.963, 15.485, 23.151, 62.818},
                            {1.599, 2.104, 2.950, 4.410, 6.532, 9.500, 13.778, 20.890, 62.291},
                            {0.499, 0.961, 1.765, 3.174, 5.228, 8.092, 12.168, 18.808, 61.795},
                            {-0.618, -0.191, 0.580, 1.946, 3.946, 6.724, 10.634, 16.872, 61.325},
                            {-1.747, -1.350, -0.607, 0.726, 2.680, 5.388, 9.160, 15.056, 60.879},
                            {-4.605, -4.264, -3.571, -2.301, -0.427, 2.152, 5.669, 10.915, 59.856},
                            {-7.477, -7.174, -6.513, -5.283, -3.462, -0.966, 2.383, 7.187, 58.941},
                            {-10.327, -10.049, -9.410, -8.207, -6.421, -3.982, -0.747, 3.749, 58.113},
                            {-13.128, -12.868, -12.245, -11.060, -9.299, -6.899, -3.744, 0.533, 57.358},
                            {-15.865, -15.617, -15.005, -13.834, -12.090, -9.718, -6.621, -2.503, 56.662},
                            {-18.529, -18.290, -17.686, -16.524, -14.793, -12.442, -9.387, -5.387, 56.019},
                            {-21.116, -20.884, -20.286, -19.131, -17.410, -15.074, -12.051, -8.140, 55.419},
                            {-23.629, -23.402, -22.808, -21.659, -19.945, -17.621, -14.622, -10.779, 54.859},
                            {-26.072, -25.848, -25.257, -24.113, -22.405, -20.090, -17.110, -13.321, 54.332},
                            {-28.451, -28.230, -27.642, -26.501, -24.797, -22.490, -19.525, -15.778, 53.836},
                            {-30.775, -30.557, -29.971, -28.832, -27.132, -24.830, -21.878, -18.164, 53.366},
                            {-33.054, -32.837, -32.253, -31.116, -29.419, -27.122, -24.179, -20.493, 52.921},
                            {-35.296, -35.081, -34.498, -33.364, -31.669, -29.375, -26.440, -22.776, 52.497},
                            {-37.513, -37.299, -36.718, -35.584, -33.891, -31.601, -28.673, -25.027, 52.093},
                            {-39.714, -39.501, -38.920, -37.788, -36.097, -33.809, -30.886, -27.256, 51.707},
                            {-41.907, -41.695, -41.115, -39.984, -38.294, -36.009, -33.090, -29.473, 51.337},
                            {-44.102, -43.891, -43.312, -42.181, -40.492, -38.209, -35.294, -31.688, 50.982},
                            {-46.305, -46.095, -45.516, -44.386, -42.698, -40.416, -37.505, -33.907, 50.642},
                            {-48.522, -48.312, -47.733, -46.604, -44.917, -42.636, -39.728, -36.138, 50.314},
                            {-50.755, -50.546, -49.968, -48.839, -47.153, -44.873, -41.967, -38.384, 49.998},
                            {-53.007, -52.798, -52.220, -51.092, -49.406, -47.128, -44.223, -40.647, 49.693},
                            {-55.275, -55.066, -54.489, -53.361, -51.676, -49.399, -46.496, -42.924, 49.399},
                            {-57.557, -57.348, -56.771, -55.644, -53.959, -51.682, -48.781, -45.214, 49.114},
                            {-59.844, -59.636, -59.059, -57.932, -56.248, -53.971, -51.072, -47.508, 48.838},
                            {-62.128, -61.920, -61.343, -60.217, -58.532, -56.257, -53.358, -49.799, 48.571},
                            {-64.396, -64.188, -63.612, -62.485, -60.802, -58.527, -55.629, -52.073, 48.312},
                            {-66.635, -66.427, -65.851, -64.725, -63.041, -60.766, -57.870, -54.316, 48.060},
                            {-68.828, -68.620, -68.044, -66.918, -65.235, -62.960, -60.065, -56.513, 47.815},
                            {-70.958, -70.750, -70.174, -69.048, -67.365, -65.091, -62.197, -58.647, 47.577},
                            {-73.008, -72.801, -72.225, -71.099, -69.417, -67.143, -64.249, -60.701, 47.346},
                            {-74.964, -74.756, -74.180, -73.055, -71.372, -69.099, -66.205, -62.660, 47.120},
                            {-76.809, -76.602, -76.026, -74.901, -73.218, -70.945, -68.052, -64.508, 46.900},
                    },
            },
            {
                    {// 100 MHz 50%
                            {89.975852, 92.181157, 94.635547, 97.384505, 100.318067, 103.120502, 105.242609, 106.356584, 106.900},
                            {80.275128, 83.090807, 86.001380, 89.207574, 92.674184, 96.119692, 98.857707, 100.284591, 100.879},
                            {74.166239, 77.529572, 80.823450, 84.350391, 88.142741, 91.968629, 95.095801, 96.730553, 97.358},
                            {69.518418, 73.354816, 77.014863, 80.831220, 84.885018, 88.993402, 92.412470, 94.207699, 94.859},
                            {65.699420, 69.920619, 73.924833, 78.021358, 82.313726, 86.660124, 90.320287, 92.249770, 92.921},
                            {62.435852, 66.957760, 71.272338, 75.640654, 80.163501, 84.727135, 88.600472, 90.648882, 91.337},
                            {59.580317, 64.332176, 68.916118, 73.542303, 78.291523, 83.063318, 87.135157, 89.293970, 89.998},
                            {57.041171, 61.967283, 66.778309, 71.642351, 76.612716, 81.589073, 85.853051, 88.118573, 88.838},
                            {54.755982, 59.813965, 64.812656, 69.890311, 75.073307, 80.252365, 84.707392, 87.079662, 87.815},
                            {52.679639, 57.837704, 62.989553, 68.254774, 73.638200, 79.017504, 83.665614, 86.147696, 86.900},
                            {50.778224, 56.012601, 61.288551, 66.715560, 72.284151, 77.859317, 82.703995, 85.301451, 86.072},
                            {49.025504, 54.318341, 59.694488, 65.259188, 70.995694, 76.759834, 81.804680, 84.525108, 85.316},
                            {47.400748, 52.738521, 58.195448, 63.876181, 69.762520, 75.706242, 80.953922, 83.806508, 84.621},
                            {45.887292, 51.259635, 56.781648, 62.559472, 68.577707, 74.689515, 80.141009, 83.136070, 83.977},
                            {44.471549, 49.870406, 55.444826, 61.303469, 67.436528, 73.703427, 79.357567, 82.506076, 83.378},
                            {43.142302, 48.561319, 54.177862, 60.103501, 66.335633, 72.743795, 78.597090, 81.910200, 82.818},
                            {41.890198, 47.324273, 52.974551, 58.955517, 65.272514, 71.807899, 77.854603, 81.343177, 82.291},
                            {40.707358, 46.152327, 51.829432, 57.855898, 64.245156, 70.894024, 77.126392, 80.800578, 81.795},
                            {39.587089, 45.039488, 50.737681, 56.801363, 63.251832, 70.001117, 76.409783, 80.278643, 81.325},
                            {38.523662, 43.980553, 49.695009, 55.788902, 62.290968, 69.128528, 75.702949, 79.774160, 80.879},
                            {33.906877, 39.353167, 45.096999, 51.267648, 57.923133, 65.057416, 72.290194, 77.430187, 78.941},
                            {30.181101, 35.575326, 41.290077, 47.459335, 54.161053, 61.436134, 69.082123, 75.247050, 77.358},
                            {27.102165, 32.409272, 38.052688, 44.171197, 50.859376, 58.194286, 66.099112, 73.137574, 76.019},
                            {24.517820, 29.703944, 35.239021, 41.267845, 47.901729, 55.251096, 63.331892, 71.082292, 74.859},
                            {22.324232, 27.356109, 32.748142, 38.652583, 45.198990, 52.532544, 60.746502, 69.082851, 73.836},
                            {20.445670, 25.291689, 30.508235, 36.256256, 42.685292, 49.978308, 58.301307, 67.139023, 72.921},
                            {18.824162, 23.455968, 28.467890, 34.030371, 40.314208, 47.543206, 55.957510, 65.243052, 72.093},
                            {17.413824, 21.807891, 26.590669, 31.942327, 38.055323, 45.196392, 53.683883, 63.381760, 71.337},
                            {16.177532, 20.316355, 24.851189, 29.971576, 35.890970, 42.919531, 51.458278, 61.540308, 70.642},
                            {15.084811, 18.957532, 23.231999, 28.106245, 33.812984, 40.704406, 49.267439, 59.705222, 69.998},
                            {14.110428, 17.712841, 21.721003, 26.340144, 31.819589, 38.550265, 47.105869, 57.866225, 69.399},
                            {13.233379, 16.567400, 20.309404, 24.670258, 29.912603, 36.461160, 44.974219, 56.017025, 68.838},
                            {12.436137, 15.508884, 18.990163, 23.094821, 28.095169, 34.443562, 42.877491, 54.155361, 68.312},
                            {11.704060, 14.526731, 17.756983, 21.612009, 26.370112, 32.504418, 40.823265, 52.282561, 67.815},
                            {11.024921, 13.611629, 16.603707, 20.219207, 24.738938, 30.649757, 38.820156, 50.402856, 67.346},
                            {10.388525, 12.755210, 15.524069, 18.912717, 23.201374, 28.883852, 36.876559, 48.522573, 66.900},
                            {9.211460, 11.188785, 13.559993, 16.538857, 20.397099, 25.624759, 33.195430, 44.791371, 66.072},
                            {8.120958, 9.775007, 11.813599, 14.444151, 17.923485, 22.720205, 29.817323, 41.153416, 65.316},
                            {7.081758, 8.471760, 10.237193, 12.577955, 15.732972, 20.138921, 26.750860, 37.666046, 64.621},
                            {6.070394, 7.246432, 8.789752, 10.892439, 13.774902, 17.837208, 23.981533, 34.370263, 63.977},
                            {5.071714, 6.074722, 7.438162, 9.346499, 12.002404, 15.768665, 21.480733, 31.288462, 63.378},
                            {4.076377, 4.939190, 6.156958, 7.906945, 10.375587, 13.890122, 19.213831, 28.426623, 62.818},
                            {3.079057, 3.827778, 4.927268, 6.548108, 8.862259, 12.164385, 17.145787, 25.778524, 62.291},
                            {2.077166, 2.732491, 3.735540, 5.250735, 7.437347, 10.560916, 15.244355, 23.330171, 61.795},
                            {1.069944, 1.648310, 2.572312, 4.000724, 6.081793, 9.055424, 13.481508, 21.063518, 61.325},
                            {0.057808, 0.572330, 1.431171, 2.787940, 4.781360, 7.628982, 11.833768, 18.959170, 60.879},
                            {-2.485571, -2.088893, -1.348877, -0.122912, 1.709288, 4.321613, 8.100969, 14.287235, 59.856},
                            {-5.026417, -4.707595, -4.044637, -2.903540, -1.176759, 1.279258, 4.767244, 10.263072, 58.941},
                            {-7.538997, -7.273236, -6.661949, -5.577762, -3.922033, -1.572460, 1.713116, 6.706292, 58.113},
                            {-10.003377, -9.774718, -9.199125, -8.154263, -6.547809, -4.272572, -1.130267, 3.495549, 57.358},
                            {-12.406876, -12.204732, -11.654455, -10.637506, -9.066119, -6.844046, -3.805172, 0.549411, 56.662},
                            {-14.743468, -14.560645, -14.028746, -13.032086, -11.486241, -9.303027, -6.340266, -2.188532, 56.019},
                            {-17.012525, -16.844031, -16.325754, -15.344155, -13.817302, -11.663058, -8.757319, -4.759376, 55.419},
                            {-19.217485, -19.059792, -18.551801, -17.581593, -16.069123, -13.936864, -11.074556, -7.194799, 54.859},
                            {-21.364677, -21.215247, -20.715154, -19.753705, -18.252309, -16.137000, -13.308275, -9.520527, 54.332},
                            {-23.462348, -23.319328, -22.825390, -21.870779, -20.378035, -18.275985, -15.473584, -11.758346, 53.836},
                            {-25.519898, -25.381915, -24.892837, -23.943635, -22.457741, -20.366198, -17.584693, -13.927251, 53.366},
                            {-27.547270, -27.413291, -26.928099, -25.983228, -24.502821, -22.419702, -19.654974, -16.044088, 52.921},
                            {-29.554470, -29.423708, -28.941657, -28.000291, -26.524328, -24.448036, -21.696918, -18.123902, 52.497},
                            {-31.551176, -31.423023, -30.943536, -30.005036, -28.532708, -26.462001, -23.722031, -20.180098, 52.093},
                            {-33.546410, -33.420393, -32.943019, -32.006883, -30.537555, -28.471460, -25.740699, -22.224492, 51.707},
                            {-35.548264, -35.424010, -34.948391, -34.014221, -32.547389, -30.485133, -27.762042, -24.267293, 51.337},
                            {-37.563650, -37.440862, -36.966712, -36.034190, -34.569451, -32.510413, -29.793758, -26.317035, 50.982},
                            {-39.598079, -39.476519, -39.003609, -38.072478, -36.609505, -34.553186, -31.841967, -28.380489, 50.642},
                            {-41.655471, -41.534947, -41.063088, -40.133139, -38.671667, -36.617659, -33.911062, -30.462556, 50.314},
                            {-43.737982, -43.618337, -43.147376, -42.218437, -40.758248, -38.706215, -36.003571, -32.566167, 49.998},
                            {-45.845868, -45.726974, -45.256785, -44.328714, -42.869628, -40.819293, -38.120048, -34.692198, 49.693},
                            {-47.977393, -47.859144, -47.389621, -46.462300, -45.004167, -42.955299, -40.258993, -36.839404, 49.399},
                            {-50.128777, -50.011086, -49.542140, -48.615470, -47.158165, -45.110571, -42.416816, -39.004406, 49.114},
                            {-52.294211, -52.177004, -51.708563, -50.782459, -49.325876, -47.279393, -44.587865, -41.181720, 48.838},
                            {-54.465939, -54.349154, -53.881154, -52.955548, -51.499597, -49.454087, -46.764510, -43.363856, 48.571},
                            {-56.634413, -56.517998, -56.050386, -55.125217, -53.669822, -51.625169, -48.937307, -45.541484, 48.312},
                            {-58.788536, -58.672447, -58.205177, -57.280394, -55.825489, -53.781592, -51.095244, -47.703689, 48.060},
                            {-60.915978, -60.800177, -60.333211, -59.408770, -57.954300, -55.911072, -53.226066, -49.838292, 47.815},
                            {-63.003580, -62.888035, -62.421338, -61.497200, -60.043117, -58.000484, -55.316672, -51.932259, 47.577},
                            {-65.037809, -64.922493, -64.456036, -63.532169, -62.078430, -60.036329, -57.353580, -53.972166, 47.346},
                            {-67.005279, -66.890166, -66.423924, -65.500300, -64.046869, -62.005242, -59.323444, -55.944713, 47.120},
                            {-68.893276, -68.778346, -68.312296, -67.388890, -65.935735, -63.894534, -61.213590, -57.837266, 46.900},
                    },
                    {// 600 MHz 50%
                            {92.681, 94.868, 97.072, 99.699, 102.345, 104.591, 106.007, 106.629, 106.900},
                            {81.108, 84.291, 87.092, 90.356, 93.803, 97.071, 99.417, 100.484, 100.879},
                            {73.480, 77.690, 81.046, 84.741, 88.624, 92.462, 95.443, 96.866, 97.358},
                            {67.693, 72.675, 76.575, 80.667, 84.877, 89.107, 92.562, 94.285, 94.859},
                            {63.064, 68.556, 72.942, 77.421, 81.920, 86.457, 90.290, 92.275, 92.921},
                            {59.229, 65.047, 69.834, 74.687, 79.459, 84.256, 88.406, 90.626, 91.337},
                            {55.965, 61.992, 67.096, 72.296, 77.333, 82.365, 86.792, 89.227, 89.998},
                            {53.130, 59.293, 64.640, 70.152, 75.447, 80.700, 85.376, 88.010, 88.838},
                            {50.628, 56.879, 62.410, 68.195, 73.739, 79.204, 84.110, 86.933, 87.815},
                            {48.393, 54.701, 60.370, 66.387, 72.167, 77.839, 82.961, 85.965, 86.900},
                            {46.377, 52.719, 58.489, 64.702, 70.703, 76.576, 81.907, 85.085, 86.072},
                            {44.542, 50.904, 56.748, 63.122, 69.327, 75.396, 80.928, 84.279, 85.316},
                            {42.862, 49.230, 55.127, 61.633, 68.022, 74.282, 80.013, 83.533, 84.621},
                            {41.315, 47.680, 53.613, 60.224, 66.780, 73.223, 79.148, 82.838, 83.977},
                            {39.883, 46.238, 52.192, 58.888, 65.590, 72.209, 78.327, 82.187, 83.378},
                            {38.553, 44.890, 50.856, 57.617, 64.447, 71.233, 77.541, 81.574, 82.818},
                            {37.312, 43.626, 49.594, 56.404, 63.345, 70.289, 76.786, 80.993, 82.291},
                            {36.151, 42.437, 48.399, 55.244, 62.280, 69.373, 76.056, 80.441, 81.795},
                            {35.062, 41.315, 47.265, 54.133, 61.250, 68.480, 75.346, 79.914, 81.325},
                            {34.038, 40.254, 46.185, 53.066, 60.250, 67.607, 74.655, 79.408, 80.879},
                            {29.704, 35.679, 41.448, 48.276, 55.634, 63.479, 71.375, 77.129, 78.941},
                            {26.339, 31.999, 37.521, 44.162, 51.501, 59.617, 68.237, 75.108, 77.358},
                            {23.638, 28.930, 34.148, 40.517, 47.713, 55.935, 65.125, 73.200, 76.019},
                            {21.411, 26.304, 31.182, 37.224, 44.194, 52.395, 61.999, 71.296, 74.859},
                            {19.531, 24.013, 28.535, 34.219, 40.906, 48.992, 58.862, 69.318, 73.836},
                            {17.910, 21.986, 26.151, 31.464, 37.834, 45.734, 55.739, 67.213, 72.921},
                            {16.485, 20.173, 23.991, 28.936, 34.972, 42.632, 52.661, 64.966, 72.093},
                            {15.211, 18.536, 22.027, 26.616, 32.314, 39.698, 49.656, 62.591, 71.337},
                            {14.051, 17.044, 20.233, 24.486, 29.852, 36.938, 46.748, 60.122, 70.642},
                            {12.982, 15.675, 18.588, 22.530, 27.578, 34.354, 43.955, 57.601, 69.998},
                            {11.982, 14.407, 17.071, 20.730, 25.477, 31.941, 41.287, 55.065, 69.399},
                            {11.037, 13.223, 15.666, 19.068, 23.536, 29.694, 38.752, 52.542, 68.838},
                            {10.136, 12.111, 14.357, 17.527, 21.739, 27.602, 36.351, 50.056, 68.312},
                            {9.269, 11.059, 13.129, 16.093, 20.070, 25.654, 34.083, 47.624, 67.815},
                            {8.429, 10.056, 11.972, 14.751, 18.515, 23.837, 31.944, 45.257, 67.346},
                            {7.612, 9.095, 10.874, 13.489, 17.061, 22.138, 29.928, 42.964, 66.900},
                            {6.030, 7.273, 8.825, 11.164, 14.407, 19.050, 26.235, 38.617, 66.072},
                            {4.498, 5.556, 6.929, 9.049, 12.026, 16.304, 22.941, 34.601, 65.316},
                            {3.004, 3.915, 5.147, 7.093, 9.855, 13.830, 19.982, 30.910, 64.621},
                            {1.541, 2.336, 3.455, 5.261, 7.848, 11.571, 17.302, 27.523, 63.977},
                            {0.103, 0.805, 1.834, 3.528, 5.972, 9.484, 14.854, 24.413, 63.378},
                            {-1.311, -0.684, 0.272, 1.873, 4.200, 7.538, 12.597, 21.550, 62.818},
                            {-2.702, -2.137, -1.241, 0.285, 2.516, 5.707, 10.500, 18.905, 62.291},
                            {-4.070, -3.557, -2.710, -1.246, 0.904, 3.972, 8.537, 16.452, 61.795},
                            {-5.417, -4.945, -4.140, -2.728, -0.646, 2.319, 6.689, 14.166, 61.325},
                            {-6.741, -6.305, -5.534, -4.166, -2.141, 0.736, 4.938, 12.027, 60.879},
                            {-9.955, -9.585, -8.880, -7.594, -5.677, -2.969, 0.905, 7.208, 59.856},
                            {-13.033, -12.709, -12.047, -10.819, -8.976, -6.385, -2.743, 2.977, 58.941},
                            {-15.981, -15.689, -15.059, -13.871, -12.081, -9.573, -6.099, -0.816, 58.113},
                            {-18.809, -18.541, -17.934, -16.774, -15.023, -12.577, -9.227, -4.275, 57.358},
                            {-21.529, -21.277, -20.688, -19.550, -17.827, -15.427, -12.172, -7.473, 56.662},
                            {-24.151, -23.913, -23.336, -22.214, -20.514, -18.150, -14.966, -10.466, 56.019},
                            {-26.687, -26.459, -25.893, -24.784, -23.101, -20.764, -17.637, -13.294, 55.419},
                            {-29.150, -28.930, -28.371, -27.273, -25.603, -23.288, -20.207, -15.988, 54.859},
                            {-31.550, -31.336, -30.784, -29.694, -28.035, -25.737, -22.692, -18.575, 54.332},
                            {-33.896, -33.688, -33.141, -32.057, -30.407, -28.124, -25.108, -21.074, 53.836},
                            {-36.198, -35.995, -35.452, -34.374, -32.730, -30.459, -27.467, -23.501, 53.366},
                            {-38.464, -38.264, -37.724, -36.651, -35.013, -32.752, -29.780, -25.872, 52.921},
                            {-40.700, -40.503, -39.966, -38.896, -37.264, -35.010, -32.055, -28.195, 52.497},
                            {-42.911, -42.717, -42.183, -41.116, -39.488, -37.241, -34.301, -30.481, 52.093},
                            {-45.104, -44.912, -44.379, -43.315, -41.691, -39.450, -36.522, -32.737, 51.707},
                            {-47.281, -47.090, -46.560, -45.498, -43.877, -41.641, -38.723, -34.968, 51.337},
                            {-49.445, -49.256, -48.727, -47.667, -46.049, -43.817, -40.908, -37.178, 50.982},
                            {-51.598, -51.411, -50.883, -49.825, -48.209, -45.981, -43.080, -39.373, 50.642},
                            {-53.743, -53.556, -53.030, -51.974, -50.359, -48.135, -45.240, -41.552, 50.314},
                            {-55.878, -55.693, -55.168, -54.113, -52.500, -50.278, -47.390, -43.719, 49.998},
                            {-58.005, -57.821, -57.297, -56.243, -54.632, -52.412, -49.529, -45.873, 49.693},
                            {-60.123, -59.939, -59.416, -58.363, -56.753, -54.536, -51.657, -48.015, 49.399},
                            {-62.230, -62.047, -61.524, -60.472, -58.864, -56.649, -53.774, -50.144, 49.114},
                            {-64.325, -64.143, -63.620, -62.569, -60.962, -58.748, -55.877, -52.258, 48.838},
                            {-66.405, -66.224, -65.702, -64.652, -63.045, -60.834, -57.966, -54.355, 48.571},
                            {-68.469, -68.288, -67.767, -66.717, -65.112, -62.901, -60.037, -56.435, 48.312},
                            {-70.514, -70.334, -69.813, -68.764, -67.159, -64.950, -62.088, -58.493, 48.060},
                            {-72.537, -72.356, -71.836, -70.787, -69.183, -66.975, -64.116, -60.528, 47.815},
                            {-74.534, -74.354, -73.834, -72.786, -71.182, -68.975, -66.118, -62.537, 47.577},
                            {-76.502, -76.323, -75.803, -74.755, -73.152, -70.946, -68.091, -64.515, 47.346},
                            {-78.439, -78.259, -77.740, -76.693, -75.090, -72.885, -70.031, -66.461, 47.120},
                            {-80.340, -80.161, -79.642, -78.595, -76.993, -74.789, -71.937, -68.371, 46.900},
                    },
                    {// 2000 MHz 50%
                            {94.233, 96.509, 98.662, 101.148, 103.509, 105.319, 106.328, 106.732, 106.900},
                            {82.427, 85.910, 88.758, 91.971, 95.244, 98.116, 99.916, 100.632, 100.879},
                            {74.501, 79.135, 82.671, 86.395, 90.171, 93.677, 96.070, 97.049, 97.358},
                            {68.368, 73.847, 78.078, 82.308, 86.474, 90.429, 93.289, 94.498, 94.859},
                            {63.385, 69.412, 74.253, 79.006, 83.536, 87.851, 91.099, 92.513, 92.921},
                            {59.209, 65.580, 70.908, 76.172, 81.068, 85.701, 89.284, 90.887, 91.337},
                            {55.628, 62.216, 67.909, 73.643, 78.912, 83.845, 87.729, 89.509, 89.998},
                            {52.499, 59.227, 65.186, 71.329, 76.970, 82.198, 86.365, 88.312, 88.838},
                            {49.725, 56.544, 62.696, 69.181, 75.181, 80.707, 85.144, 87.253, 87.815},
                            {47.236, 54.116, 60.406, 67.169, 73.507, 79.331, 84.035, 86.304, 86.900},
                            {44.981, 51.901, 58.291, 65.277, 71.922, 78.043, 83.013, 85.442, 86.072},
                            {42.922, 49.867, 56.329, 63.490, 70.408, 76.822, 82.061, 84.652, 85.316},
                            {41.029, 47.989, 54.502, 61.800, 68.956, 75.653, 81.165, 83.922, 84.621},
                            {39.279, 46.245, 52.794, 60.198, 67.558, 74.524, 80.313, 83.243, 83.977},
                            {37.653, 44.619, 51.191, 58.677, 66.210, 73.429, 79.497, 82.608, 83.378},
                            {36.137, 43.096, 49.682, 57.231, 64.909, 72.361, 78.708, 82.009, 82.818},
                            {34.717, 41.665, 48.257, 55.853, 63.652, 71.316, 77.942, 81.442, 82.291},
                            {33.384, 40.316, 46.908, 54.537, 62.437, 70.293, 77.192, 80.903, 81.795},
                            {32.129, 39.041, 45.627, 53.278, 61.260, 69.289, 76.455, 80.387, 81.325},
                            {30.945, 37.832, 44.407, 52.072, 60.121, 68.303, 75.728, 79.893, 80.879},
                            {25.889, 32.596, 39.051, 46.684, 54.906, 63.628, 72.179, 77.642, 78.941},
                            {21.921, 28.355, 34.599, 42.081, 50.306, 59.317, 68.706, 75.604, 77.358},
                            {18.729, 24.802, 30.756, 37.994, 46.119, 55.280, 65.291, 73.633, 76.019},
                            {16.114, 21.754, 27.352, 34.269, 42.210, 51.427, 61.921, 71.641, 74.859},
                            {13.939, 19.099, 24.292, 30.826, 38.510, 47.703, 58.580, 69.571, 73.836},
                            {12.108, 16.766, 21.526, 27.634, 34.999, 44.085, 55.252, 67.388, 72.921},
                            {10.548, 14.707, 19.028, 24.685, 31.681, 40.581, 51.937, 65.076, 72.093},
                            {9.201, 12.884, 16.779, 21.982, 28.576, 37.214, 48.650, 62.636, 71.337},
                            {8.025, 11.268, 14.761, 19.525, 25.699, 34.012, 45.413, 60.085, 70.642},
                            {6.984, 9.831, 12.957, 17.305, 23.060, 31.001, 42.256, 57.448, 69.998},
                            {6.050, 8.546, 11.343, 15.308, 20.658, 28.196, 39.207, 54.753, 69.399},
                            {5.200, 7.390, 9.896, 13.516, 18.482, 25.607, 36.292, 52.029, 68.838},
                            {4.416, 6.342, 8.594, 11.905, 16.516, 23.230, 33.529, 49.305, 68.312},
                            {3.683, 5.381, 7.413, 10.452, 14.740, 21.057, 30.929, 46.606, 67.815},
                            {2.990, 4.493, 6.334, 9.135, 13.131, 19.074, 28.496, 43.955, 67.346},
                            {2.327, 3.662, 5.339, 7.933, 11.669, 17.262, 26.230, 41.369, 66.900},
                            {1.061, 2.131, 3.544, 5.800, 9.102, 14.082, 22.170, 36.454, 66.072},
                            {-0.158, 0.716, 1.934, 3.934, 6.899, 11.377, 18.668, 31.936, 65.316},
                            {-1.356, -0.628, 0.443, 2.249, 4.953, 9.027, 15.628, 27.838, 64.621},
                            {-2.550, -1.932, -0.972, 0.686, 3.187, 6.939, 12.958, 24.145, 63.977},
                            {-3.746, -3.213, -2.338, -0.794, 1.548, 5.042, 10.577, 20.821, 63.378},
                            {-4.950, -4.482, -3.674, -2.219, -0.002, 3.287, 8.421, 17.823, 62.818},
                            {-6.161, -5.745, -4.989, -3.604, -1.486, 1.637, 6.440, 15.102, 62.291},
                            {-7.380, -7.004, -6.290, -4.962, -2.923, 0.066, 4.597, 12.617, 61.795},
                            {-8.604, -8.261, -7.581, -6.297, -4.322, -1.442, 2.863, 10.329, 61.325},
                            {-9.831, -9.515, -8.861, -7.614, -5.692, -2.901, 1.216, 8.208, 60.879},
                            {-12.895, -12.626, -12.020, -10.839, -9.011, -6.382, -2.610, 3.469, 59.856},
                            {-15.923, -15.685, -15.110, -13.970, -12.202, -9.678, -6.132, -0.679, 58.941},
                            {-18.888, -18.670, -18.115, -17.003, -15.275, -12.821, -9.429, -4.412, 58.113},
                            {-21.771, -21.567, -21.026, -19.933, -18.232, -15.826, -12.542, -7.834, 57.358},
                            {-24.562, -24.368, -23.837, -22.758, -21.077, -18.705, -15.497, -11.013, 56.662},
                            {-27.260, -27.073, -26.549, -25.479, -23.813, -21.466, -18.314, -13.996, 56.019},
                            {-29.866, -29.684, -29.166, -28.103, -26.447, -24.120, -21.009, -16.816, 55.419},
                            {-32.387, -32.209, -31.695, -30.638, -28.989, -26.676, -23.597, -19.500, 54.859},
                            {-34.830, -34.656, -34.144, -33.092, -31.450, -29.147, -26.093, -22.070, 54.332},
                            {-37.206, -37.034, -36.525, -35.476, -33.839, -31.545, -28.510, -24.545, 53.836},
                            {-39.525, -39.355, -38.848, -37.801, -36.168, -33.881, -30.861, -26.943, 53.366},
                            {-41.798, -41.629, -41.123, -40.079, -38.449, -36.167, -33.160, -29.279, 52.921},
                            {-44.034, -43.866, -43.362, -42.319, -40.691, -38.414, -35.417, -31.566, 52.497},
                            {-46.243, -46.077, -45.574, -44.532, -42.906, -40.633, -37.644, -33.818, 52.093},
                            {-48.435, -48.269, -47.767, -46.727, -45.103, -42.832, -39.850, -36.045, 51.707},
                            {-50.617, -50.452, -49.951, -48.911, -47.289, -45.021, -42.044, -38.257, 51.337},
                            {-52.796, -52.631, -52.131, -51.092, -49.471, -47.205, -44.233, -40.460, 50.982},
                            {-54.976, -54.812, -54.312, -53.274, -51.654, -49.389, -46.422, -42.661, 50.642},
                            {-57.162, -56.998, -56.498, -55.461, -53.841, -51.579, -48.614, -44.864, 50.314},
                            {-59.354, -59.190, -58.691, -57.654, -56.035, -53.774, -50.812, -47.072, 49.998},
                            {-61.552, -61.389, -60.890, -59.854, -58.236, -55.975, -53.016, -49.284, 49.693},
                            {-63.756, -63.593, -63.094, -62.058, -60.440, -58.181, -55.224, -51.498, 49.399},
                            {-65.960, -65.797, -65.298, -64.263, -62.646, -60.387, -57.433, -53.712, 49.114},
                            {-68.159, -67.997, -67.498, -66.463, -64.846, -62.589, -59.636, -55.920, 48.838},
                            {-70.347, -70.184, -69.686, -68.651, -67.035, -64.778, -61.826, -58.115, 48.571},
                            {-72.513, -72.351, -71.853, -70.818, -69.202, -66.946, -63.995, -60.289, 48.312},
                            {-74.649, -74.487, -73.989, -72.954, -71.339, -69.083, -66.134, -62.431, 48.060},
                            {-76.744, -76.582, -76.084, -75.049, -73.434, -71.179, -68.230, -64.530, 47.815},
                            {-78.785, -78.624, -78.126, -77.092, -75.476, -73.221, -70.274, -66.577, 47.577},
                            {-80.763, -80.602, -80.104, -79.070, -77.455, -75.200, -72.254, -68.559, 47.346},
                            {-82.667, -82.505, -82.008, -80.973, -79.359, -77.104, -74.159, -70.466, 47.120},
                            {-84.485, -84.324, -83.826, -82.792, -81.178, -78.924, -75.979, -72.288, 46.900},
                    },

            },
    };
}
