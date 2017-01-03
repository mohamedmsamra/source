package org.seamcat.model.propagation;

/* LongleyRice Init class modified by THALES/Béatrice MARTIN : correction of bugs in "initPlugin"
                                                                + restore code which had been deleted in V5.0.0 (the deletion of this part code
                                                                produced severe bugs and strong instability of results for large distances)
                                                                + deletion of redundant code for testing input frequency
                                                                + deletion of redundant application of standard deviation (done twice instead of once +
                                                                  standard deviation is not part of core Longley Rice model but is a Seamcat specific
                                                                  parameter)
 * LongleyRice class modified by Karl Koch
 * LongleyRice plugin created by Dariusz Wypi�r
 */

import org.seamcat.model.Scenario;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.propagation.LongleyRice_modInput;
import org.seamcat.model.plugin.propagation.PropagationModelPlugin;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;

import java.util.List;

/**
 * @author <strong>Dariusz Wypior</strong>
 * @author Karl Koch (modification for adaption)
 * @author THALES/Béatrice MARTIN (correction of bugs introduced with adaptation to SEAMCAT V5.0.0)
 * @version Correction of bugs + code cleaning
 */
public class LongleyRice_mod implements PropagationModelPlugin<LongleyRice_modInput> {

    public static LongleyRice_modInput input;
    public static int polar;
    public static int sitingCriteria;
    public static int radio_climate;
    public static int mdvar;

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, LongleyRice_modInput input, Validator<LongleyRice_modInput> validator) {
        if (path.size() > 0){
            if (PluginCheckUtilsToBeRemoved.getMaxDistance(scenario,path) < 1.){
                validator.error("Results with distances below 1 km are not guaranteed <br/>by the Longley Rice propagation model" + PluginCheckUtilsToBeRemoved.getExceptionHint());
            }
            Distribution frequencies = HataSE21PropagationModel.findFrequency(scenario,path);
            if (frequencies.getBounds().getMin() < 20 || frequencies.getBounds().getMax() > 20000)
                validator.error("Frequencies below 20 MHz or above 20 GHz are not supported by the Longley Rice propagation model" + PluginCheckUtilsToBeRemoved.getExceptionHint());

            if (PluginCheckUtilsToBeRemoved.getAntennaHeightTXmax(path) > 1000)
                validator.error("Antenna Height above 1 000 m are not supported by the Longley Rice propagation model" +
                PluginCheckUtilsToBeRemoved.getExceptionHint());

        }
        LongleyRice_mod.input = input;
    }

    @Override
    public double evaluate(LinkResult linkResult, boolean variation, LongleyRice_modInput input) {
        double frequency = linkResult.getFrequency();
        double distance = linkResult.getTxRxDistance();
        double txHeight = linkResult.txAntenna().getHeight();
        double rxHeight = linkResult.rxAntenna().getHeight();

        if (frequency < 20. || frequency > 20000. )
            throw new RuntimeException("Frequencies below 20 MHz or above 20 GHz are not supported by the Longley Rice propagation model");
        if (txHeight>1000. || rxHeight > 1000.)
            throw new RuntimeException("Antenna Height above 1 000 m are not supported by the Longley Rice propagation model");
        if (distance < 1.)  // Longley Rice is in principle not applicable for d < 1 km, but some people sometime use it anyway so this runtime exception can be removed
            throw new RuntimeException("Results with distances below 1 km are not guaranteed the Longley Rice propagation model");


        Param param = new Param();
        Param_att param_att = new Param_att();
        Param_tmp param_tmp = new Param_tmp();
        param.setPar2(txHeight, rxHeight);
        Param2 param2 = new Param2();
        initPlugin(param);

        Complex wyznaczanie = Wyznaczanie_param.wyznaczanie_param(linkResult, param2);
        Licz_geometrie.licz_geometrie(param, param2);
        Strefa2 strefa2 = new Strefa2();
        strefa2.licz_Aed(wyznaczanie, param, param2, param_att, param_tmp);
        Strefa1 strefa1 = new Strefa1();
        strefa1.strefa1(wyznaczanie, param_att,param2, param_tmp);
        Strefa3 strefa3 = new Strefa3();
        strefa3.strefa3(param_att, param2, param_tmp);
        double aref = Att.att(linkResult, param_att, param_tmp, param2);

        double loss = Stat.stat(linkResult, aref, param, param2);
        if (variation) {
            loss += input.stdDev() * Factory.distributionFactory().getGaussianDistribution(0, 1).trial();
        }

        return loss;
    }


    private void initPlugin(Param param) {
        if (input.polarisation().matches("Horizontal"))
            setPolar( 0);
        else
            setPolar( 1); // Vertical

        if (input.siteCriteria().contains("Random"))
            setSitingCriteria( 0);
        else if (input.siteCriteria().contains("Very Careful"))
            setSitingCriteria( 2);
        else
            setSitingCriteria( 1); // Careful

        if (input.radioClimate().contains("Equatorial"))
            setRadio_climate( 0);
        else if (input.radioClimate().contains("Continental Subtropical"))
            setRadio_climate( 1);
        else if (input.radioClimate().contains("Maritime Subtropical"))
            setRadio_climate( 2);
        else if (input.radioClimate().contains("Desert"))
            setRadio_climate( 3);
        else if (input.radioClimate().contains("Continental Temperate"))
            setRadio_climate( 4);
        else if (input.radioClimate().contains("Maritime Temperate over land"))
            setRadio_climate( 5);
        else
            setRadio_climate( 6); // Maritime Temperate over sea

        param.pctTime = input.timePercentage().trial() / 100.;
        param.pctLoc = input.locationPercentage().trial() / 100.;
        param.pctConf = input.confidentPercent().trial() / 100.;

        if (input.variability().contains("Single"))
            setMdvar( 0);
        else if (input.variability().contains("Individual"))
            setMdvar( 1);
        else if (input.variability().contains("Mobile"))
            setMdvar( 2);
        else
            setMdvar( 3); // Broadcast

    }

    public static void setPolar(int _polar) {
        polar = _polar;
    }

    public static void setSitingCriteria( int _sitingCriteria) {
        sitingCriteria = _sitingCriteria;
    }

    public static void setRadio_climate( int _radio_climate) {
        radio_climate = _radio_climate;
    }

    public static void setMdvar( int _mdvar) {
        mdvar = _mdvar;
    }

    @Override
    public Description description() {
        return new DescriptionImpl("Longley Rice", "adapted to SEAMCAT 5");
    }
}

class Param {
    double hg1;
    double hg2;
    double pctTime;
    double pctLoc;
    double pctConf;

    public void setPar2(double txh, double rxh) {
        hg1 = txh;
        hg2 = rxh;
    }
}

class Param2 {
    double k;
    double a;
    double he1, he2;
    double dl1, dl2, dl;
    double teta_e1, teta_e2, teta_e;
    double dls1, dls2, dls;
    double Xae;
    int lvar = 5;
    double sgc;
}

class Complex {

    public double re, im;

    public static final Complex I = new Complex(0.0, 1.0);

    public Complex(double x, double y) {
        re = x;
        im = y;
    }

    public Complex(double x) {
        re = x;
        im = 0.0;
    }

    public Complex() {
        re = 0.0;
        im = 0.0;
    }

    public static Complex complex(double x) {
        return new Complex(x);
    }

    public static Complex complex() {
        return new Complex();
    }

    public String getName() {
        if (Mathematics.equals(im,0,0.0001)) return re + "";
        if (Mathematics.equals(re,0,0.0001)) return im + "i";
        if (im < 0) return re + " - " + (-im) + "i";
        return re + " + " + im + "i";
    }

    public boolean equals(Complex x) {
        return (Mathematics.equals(re,x.re,0.0001) && Mathematics.equals(im,x.im,0.0001));
    }

    public static boolean equals(Complex x, Complex y) {
        return (Mathematics.equals(x.re,y.re,0.0001) && Mathematics.equals(x.im,y.im,0.0001));
    }

    public static double real(Complex x) {
        return x.re;
    }

    public static double imag(Complex x) {
        return x.im;
    }

    public static double abs(Complex x) {
        return Math.hypot(x.re, x.im);
    }

    public static double arg(Complex x) {
        if (x.equals(complex()))
            return 0.0;
        else
            return Math.atan2(x.im, x.re);
    }

    public static Complex add(Complex x, Complex y) {
        Complex z = new Complex(x.re, x.im);
        z.re += y.re;
        z.im += y.im;
        return z;
    }

    public static Complex add(Complex x, double y) {
        return add(x, complex(y));
    }

    public static Complex add(double x, Complex y) {
        return add(complex(x), y);
    }

    public static Complex sub(Complex x, Complex y) {
        Complex z = new Complex(x.re, x.im);
        z.re -= y.re;
        z.im -= y.im;
        return z;
    }

    public static Complex sub(Complex x, double y) {
        return sub(x, complex(y));
    }

    public static Complex sub(double x, Complex y) {
        return sub(complex(x), y);
    }

    public static Complex mult(Complex x, Complex y) {
        Complex z = new Complex();
        z.re = x.re * y.re - x.im * y.im;
        z.im = x.re * y.im + x.im * y.re;
        return z;
    }

    public static Complex mult(Complex x, double y) {
        return mult(x, complex(y));
    }

    public static Complex div(Complex x, Complex y) {
        Complex z = new Complex();
        double denom = Math.pow(y.re, 2) + Math.pow(y.im, 2);
        z.re = (x.re * y.re + x.im * y.im) / denom;
        z.im = (x.im * y.re - x.re * y.im) / denom;
        return z;
    }


    public static Complex exp(Complex x) {
        Complex z = new Complex();
        z.re = Math.exp(x.re) * Math.cos(x.im);
        z.im = Math.exp(x.re) * Math.sin(x.im);
        return z;
    }

    public static Complex log(Complex x) {
        Complex z = new Complex(abs(x), arg(x));
        z.re = Math.log(z.re);
        return z;
    }

    public static Complex sqrt(Complex x) {
        Complex z = new Complex();
        z.re = Math.sqrt(abs(x)) * Math.cos(arg(x) / 2.0);
        z.im = Math.sqrt(abs(x)) * Math.sin(arg(x) / 2.0);
        return z;
    }


    public static Complex pow(Complex x, Complex y) {
        if (x.equals(complex(0.0)) && y.re > 0.0)
            return complex();
        else
            return exp(mult(y, log(x)));
    }

    public static Complex pow(Complex x, double y) {
        Complex z = new Complex();
        z.re = Math.pow(abs(x), y) * Math.cos(y * arg(x));
        z.im = Math.pow(abs(x), y) * Math.sin(y * arg(x));
        return z;
    }

    public static Complex pow(double x, Complex y) {
        return pow(complex(x), y);
    }
}

class Param_att {
    double Aed;
    double md;
    double Aref1;
    double Aref2;
    double Aref3;
    double Ael;
    double Aes;
    double dx;
    double ms;
}

class Param_tmp {
    double qk;
    double xht;
    double aht;
    double wd1;
    double xd1;
    double afo;
    double wls;
    double h0s;
    double ad;
    double rr;
    double etq;
    double ak2;
    double ak1;
}

class Wyznaczanie_param {
    static Complex wyznaczanie_param(LinkResult link, Param2 param2){
        double gma;
        gma = 157e-9;
        double a=gma*(1-0.04665*Math.exp(LongleyRice_mod.input.meanSurface()/179.3));
        param2.a = a;
        double k1 = link.getFrequency()/47.70;
        param2.k = k1;
        double Z0 = 376.62;
        double k = param2.k;
        double sigma = LongleyRice_mod.input.conductivity();
        double ePrimTmp;
        ePrimTmp = (Z0*sigma)/k;
        Complex ePrim = new Complex(LongleyRice_mod.input.relPermit(), ePrimTmp);
        if (LongleyRice_mod.polar == 0) {
            ePrim = Complex.sub(ePrim, 1);
            ePrim = Complex.sqrt(ePrim);
        } else if (LongleyRice_mod.polar == 1) {
            ePrim = Complex.sub(ePrim, 1);
            ePrim = Complex.div((Complex.sqrt(ePrim)), ePrim);
        }
        return ePrim;
    }
}

class Licz_geometrie {
    static void licz_geometrie(Param param, Param2 param2) {
        int b = 0;
        double bPrim1;
        double bPrim2;
        if (LongleyRice_mod.sitingCriteria == 0) {
            param2.he1 = param.hg1;
            param2.he2 = param.hg2;
        } else if ((LongleyRice_mod.sitingCriteria == 1) || (LongleyRice_mod.sitingCriteria == 2)) {
            b = 5;
            if (LongleyRice_mod.sitingCriteria == 2)
                b += 5;
            bPrim1 = ((b-1)*Math.sin(((Math.PI)/2)*Math.min((param.hg1/5),1)))+1;
            bPrim2 = ((b-1)*Math.sin(((Math.PI)/2)*Math.min((param.hg2/5),1)))+1;
            param2.he1 = param.hg1 + (bPrim1*Math.exp((-2*param.hg1)/LongleyRice_mod.input.terrainIrregular()));
            param2.he2 = param.hg2 + (bPrim2*Math.exp((-2*param.hg2)/LongleyRice_mod.input.terrainIrregular()));
        }
        param2.dls1 = Math.sqrt((2*param2.he1)/param2.a);
        param2.dls2 = Math.sqrt((2*param2.he2)/param2.a);
        param2.dls = param2.dls1 + param2.dls2;

        param2.dl1 = param2.dls1*Math.exp(-0.07*Math.sqrt(LongleyRice_mod.input.terrainIrregular()/(Math.max(param2.he1, 5))));
        param2.dl2 = param2.dls2*Math.exp(-0.07*Math.sqrt(LongleyRice_mod.input.terrainIrregular()/(Math.max(param2.he2, 5))));
        param2.dl = param2.dl1 + param2.dl2;

        param2.teta_e1 = (0.65*LongleyRice_mod.input.terrainIrregular()*(param2.dls1/param2.dl1 - 1) - 2*param2.he1)/param2.dls1;
        param2.teta_e2 = (0.65*LongleyRice_mod.input.terrainIrregular()*(param2.dls2/param2.dl2 - 1) - 2*param2.he2)/param2.dls2;
        param2.teta_e = Math.max((param2.teta_e1 + param2.teta_e2), -(param2.dl/param2.a));

    }
}

class Strefa2 {

    void licz_Aed(Complex wyznaczanie, Param param, Param2 param2, Param_att param_att, Param_tmp param_tmp) {
        double Xae = Math.cbrt(param2.k * param2.a * param2.a);
        Xae = 1/Xae;
        param2.Xae = Xae;
        double q=Adiff(0, wyznaczanie, param, param2, param_tmp);
        double d3 = Math.max(param2.dls, (param2.dl + (1.3787 * Xae)));
        double d4 = d3 + (2.7574*Xae);
        double A3 = Adiff(d3, wyznaczanie, param, param2, param_tmp);
        double A4 = Adiff(d4, wyznaczanie, param, param2, param_tmp);
        param_att.md = (A4-A3)/(d4-d3);
        param_att.Aed = A3 - (param_att.md*d3);

    }

    public static double modul(Complex x) {
        double modul, im, re;
        im = Complex.imag(x);
        re = Complex.real(x);
        im = Math.pow(im, 2);
        re = Math.pow(re, 2);
        modul = Math.sqrt(im+re);
        return modul;
    }

    public static double aknfe(double x)
    { double a;
        if(x<5.76)
            a=6.02+9.11*Math.sqrt(x)-1.27*x;
        else
            a=12.953+4.343*Math.log(x);
        return a;
    }

    public static double fht(double x, double pk)
    { double w, fhtv;
        if(x<200.0)
        { w=-Math.log(pk);
            if( pk < 1e-5 || x*Math.pow(w,3.0) > 5495.0 )
            { fhtv=-117.0;
                if(x>1.0)
                    fhtv=17.372*Math.log(x)+fhtv;
            }
            else
                fhtv=2.5e-5*x*x/pk-8.686*w-15.0;
        }
        else
        { fhtv=0.05751*x-4.343*Math.log(x);
            if(x<2000.0)
            { w=0.0134*x*Math.exp(-0.005*x);
                fhtv=(1.0-w)*fhtv+w*(17.372*Math.log(x)-117.0);
            }
        }
        return fhtv;
    }

    public static double Adiff(double s, Complex wyznaczanie, Param param, Param2 param2, Param_tmp param_tmp) {
        double adiffv;
        double th, ds, q, a, wa, pk, qk, ar, wd;
        double qbis,pkbis;
        if(Mathematics.equals(s,0.0,0.0001)) {
            q=param.hg1*param.hg2;
            qk=param2.he1*param2.he2-q;
            param_tmp.wd1 = Math.sqrt(1+qk/q);
            param_tmp.xd1=param2.dl+param2.teta_e/param2.a;
            q=(1.0-0.8*Math.exp(-param2.dls/50e3))*LongleyRice_mod.input.terrainIrregular();
            q*=0.78*Math.exp(-Math.pow(q/16.0,0.25));
            double x_tmp;
            x_tmp = 1.0+4.77e-4*param.hg1*param.hg2 *param2.k*q;
            param_tmp.afo=Math.min(15.0,2.171*Math.log(x_tmp));
            param_tmp.qk=1.0/modul(wyznaczanie);
            a=0.5*Math.pow(param2.dl1,2.0)/param2.he1;
            wa=Math.cbrt(a*param2.k);
            pk=param_tmp.qk/wa;
            q=(1.607-pk)*151.0*wa*param2.dl1/a;
            param_tmp.xht=q;
            param_tmp.aht=20+fht(q,pk);
            a=0.5*Math.pow(param2.dl2,2.0)/param2.he2;
            wa=Math.cbrt(a*param2.k);
            pkbis=param_tmp.qk/wa;
            qbis=(1.607-pk)*151.0*wa*param2.dl2/a;
            param_tmp.xht=q+qbis;
            param_tmp.aht=20+fht(q,pk)+fht(qbis,pkbis);
            adiffv=0.0;
        } else {
            th=param2.teta_e+s*param2.a;
            ds=s-param2.dl;
            q=0.0795775*param2.k*ds*Math.pow(th,2);
            adiffv=aknfe(q*param2.dl1/(ds+param2.dl1))+aknfe(q*param2.dl2/(ds+param2.dl2));
            a=ds/th;
            wa=Math.cbrt(a*param2.k);
            pk=param_tmp.qk/wa;
            q=(1.607-pk)*151.0*wa*th+param_tmp.xht;
            ar=0.05751*q-4.343*Math.log(q)-param_tmp.aht;
            q=(param_tmp.wd1+param_tmp.xd1/s)*Math.min(((1.0-0.8*Math.exp(-s/50e3))*LongleyRice_mod.input.terrainIrregular()*param2.k),6283.2);
            wd=25.1/(25.1+Math.sqrt(q));
            adiffv=ar*wd+(1.0-wd)*adiffv+param_tmp.afo;
        }
        return adiffv;
    }
}

class Strefa1 {
    public static double abq_alos (Complex r) {
        double modul, im, re;
        im = Complex.imag(r) * Complex.imag(r);
        re = Complex.real(r) * Complex.real(r);
        modul = re + im;
        return modul;
    }

    public static double Alos(double d, Complex wyznaczanie, Param_att param_att, Param2 param2, Param_tmp param_tmp) {
        Complex r = new Complex(0, 0);
        double s, sps, q;
        double alosv;
        if(Mathematics.equals(d,0.0,0.0001)){
            param_tmp.wls=0.021/(0.021+param2.k*LongleyRice_mod.input.terrainIrregular()/Math.max(10e3,param2.dls));
            alosv=0.0;
        }
        else {
            q=(1.0-0.8*Math.exp(-d/50e3))*LongleyRice_mod.input.terrainIrregular();
            s=0.78*q*Math.exp(-Math.pow(q/16.0,0.25));
            q=param2.he1+param2.he2;
            sps=q/Math.sqrt(d*d+q*q);
            r = Complex.sub(sps, wyznaczanie);
            r = Complex.div(r, (Complex.add(sps, wyznaczanie)));
            r = Complex.mult(r, (Math.exp(-Math.min(10, param2.k*s*sps))));
            q=abq_alos(r);
            if(q<0.25 || q<sps)
                r = Complex.mult(r, (Math.sqrt(sps/q)));
            alosv=param_att.md*d+param_att.Aed;
            q=param2.k*param2.he1*param2.he2*2.0/d;
            if(q>1.57)
                q=3.14-2.4649/q;
            Complex r_tmp = new Complex(Math.cos(q), -Math.sin(q));
            r_tmp = Complex.add(r_tmp, r);
            alosv=(-4.343*Math.log(abq_alos(r_tmp))-alosv) * param_tmp.wls+alosv;
        }

        return alosv;
    }

    void strefa1(Complex wyznaczanie, Param_att param_att, Param2 param2, Param_tmp param_tmp){
        double d0, d1, d2;
        double A0, A1, A2, q;
        boolean wq;

        q=Alos(0.0, wyznaczanie, param_att, param2, param_tmp);
        d2=param2.dls;
        A2=param_att.Aed+d2*param_att.md;
        d0=1.908*param2.k*param2.he1*param2.he2;
        if(param_att.Aed>=0.0){
            d0=Math.min(d0,0.5*param2.dl);
            d1=d0+0.25*(param2.dl-d0);
        }
        else
            d1=Math.max(-param_att.Aed/param_att.md,0.25*param2.dl);
        A1=Alos(d1,wyznaczanie, param_att, param2, param_tmp);
        wq=false;
        if(d0<d1){
            A0=Alos(d0,wyznaczanie, param_att, param2, param_tmp);
            q=Math.log(d2/d0);
            param_tmp.ak2=Math.max(0.0,((d2-d0)*(A1-A0)-(d1-d0)*(A2-A0))/((d2-d0)*Math.log(d1/d0)-(d1-d0)*q));
            wq=param_att.Aed>=0 || param_tmp.ak2>0.0;
            if(wq){
                param_tmp.ak1=(A2-A0-param_tmp.ak2*q)/(d2-d0);
                if(param_tmp.ak1<0.0){
                    param_tmp.ak1=0.0;
                    param_tmp.ak2=Strefa3.FORTRAN_DIM(A2,A0)/q;
                    if(Mathematics.equals(param_tmp.ak2,0.0,0.0001))
                        param_tmp.ak1=param_att.md;
                }
            }
        }
        if(!wq)	{
            param_tmp.ak1=Strefa3.FORTRAN_DIM(A2,A1)/(d2-d1);
            param_tmp.ak2=0.0;
            if(Mathematics.equals(param_tmp.ak1,0.0,0.0001))
                param_tmp.ak1=param_att.md;
        }
        param_att.Ael=A2-param_tmp.ak1*d2-param_tmp.ak2*Math.log(d2);

    }
}

class Strefa3 {

    public static double FORTRAN_DIM(double x, double y) {
        if(x>y)
            return x-y;
        else
            return 0.0;
    }

    public static double ahd(double td)
    { int i;
        double [] a = {   133.4,    104.6,     71.8};
        double [] b = {0.332e-3, 0.212e-3, 0.157e-3};
        double [] c = {  -4.343,   -1.086,    2.171};
        if(td<=10e3)
            i=0;
        else if(td<=70e3)
            i=1;
        else
            i=2;
        return a[i]+b[i]*td+c[i]*Math.log(td);
    }

    public static double h0f(double r, double et){
        double [] a = {25.0, 80.0, 177.0, 395.0, 705.0};
        double [] b ={24.0, 45.0,  68.0,  80.0, 105.0};
        double q, x;
        int it;
        double h0fv;
        it=(int)et;
        if(it<=0){
            it=1;
            q=0.0;
        } else if (it>=5) {
            it=5;
            q=0.0;
        } else
            q=et-it;
        x=Math.pow(1/r,2.0);
        h0fv=4.343*Math.log((a[it-1]*x+b[it-1])*x+1.0);
        if(q!=0.0)
            h0fv=(1.0-q)*h0fv+q*4.343*Math.log((a[it]*x+b[it])*x+1.0);
        return h0fv;
    }

    public static double Acsat(double d, Param_tmp param_tmp, Param2 param2) {
        double h0, r1, r2, z0, ss, et, ett, th, q;
        double ascatv;
        if(Mathematics.equals(d,0.0,0.0001)) {
            param_tmp.ad=param2.dl1-param2.dl2;
            param_tmp.rr=param2.he2/param2.he1;
            if(param_tmp.ad<0.0){
                param_tmp.ad=-param_tmp.ad;
                param_tmp.rr=1.0/param_tmp.rr;
            }
            param_tmp.etq=(5.67e-6*LongleyRice_mod.input.meanSurface()-2.32e-3)*LongleyRice_mod.input.meanSurface()+0.031;
            param_tmp.h0s=-15.0;
            ascatv=0.0;
        } else {
            if(param_tmp.h0s>15.0)
                h0=param_tmp.h0s;
            else {
                th=param2.teta_e1+param2.teta_e2+d*param2.a;
                r2=2.0*param2.k*th;
                r1=r2*param2.he1;
                r2*=param2.he2;
                if(r1<0.2 && r2<0.2)
                    return 1001.0;  // <==== early return
                ss=(d-param_tmp.ad)/(d+param_tmp.ad);
                q=param_tmp.rr/ss;
                ss=Math.max(0.1,ss);
                q=Math.min(Math.max(0.1,q),10.0);
                z0=(d-param_tmp.ad)*(d+param_tmp.ad)*th*0.25/d;
                et=(param_tmp.etq*Math.exp(-Math.pow(Math.min(1.7,z0/8.0e3),6.0))+1.0)*z0/1.7556e3;
                ett=Math.max(et,1.0);
                h0=(h0f(r1,ett)+h0f(r2,ett))*0.5;
                h0+=Math.min(h0,(1.38-Math.log(ett))*Math.log(ss)*Math.log(q)*0.49);
                h0=FORTRAN_DIM(h0,0.0);
                if(et<1.0)
                    h0=et*h0+(1.0-et)*4.343*Math.log(Math.pow((1.0+1.4142/r1)*(1.0+1.4142/r2),2.0)*(r1+r2)/(r1+r2+2.8284));
                if(h0>15.0 && param_tmp.h0s>=0.0)
                    h0=param_tmp.h0s;
            }
            param_tmp.h0s=h0;
            th=param2.teta_e+d*param2.a;
            ascatv=ahd(th*d)+4.343*Math.log(47.7*param2.k*Math.pow(th,4.0))-0.1 *(LongleyRice_mod.input.meanSurface()-301.0)*Math.exp(-th*d/40e3)+h0;
        }
        return ascatv;
    }

    void strefa3(Param_att param_att, Param2 param2, Param_tmp param_tmp) {
        double d5,d6, A5, A6, q;
        d5 = param2.dl + 200e3;
        d6 = d5+200e3;
        q=Acsat(0.0, param_tmp, param2);
        A5 = Acsat(d5, param_tmp, param2);
        A6 = Acsat(d6, param_tmp, param2);
        if(A5<1000.0)
        { param_att.ms=(A6-A5)/200e3;
            param_att.dx=Math.max(param2.dls,Math.max(param2.dl+0.3*param2.Xae*Math.log(47.7*param2.k),(A5-param_att.Aed-param_att.ms*d5)/(param_att.md-param_att.ms)));
            param_att.Aes=(param_att.md-param_att.ms)*param_att.dx+param_att.Aed;

        }
        else
        { param_att.ms=param_att.md;
            param_att.Aes=param_att.Aed;
            param_att.dx=10.e6;
        }
    }
}

class Stat {
    public static double querfi (double q) {
        double x, t, v;
        double c0  = 2.515516698;
        double c1  = 0.802853;
        double c2  = 0.010328;
        double d1  = 1.432788;
        double d2  = 0.189269;
        double d3  = 0.001308;

        x = 0.5 - q;
        t = Math.max(0.5 - Math.abs(x), 0.000001);
        t = Math.sqrt(-2.0 * Math.log(t));
        v = t - ((c2 * t + c1) * t + c0) / (((d3 * t + d2) * t + d1) * t + 1.0);
        if (x < 0.0) v = -v;
        return v;
    }

    public static double curve (double c1, double c2, double x1,double x2, double x3, double de){
        return (c1+c2/(1.0+Math.pow((de-x2)/x3,2.0)))*Math.pow(de/x1,2.0) / (1.0+Math.pow(de/x1,2.0));
    }


    public static double avar(double zzt, double zzl, double zzc, LinkResult link, double aref, Param2 param2){
        int kdv;
        double dexa, de, vmd, vs0, sgl, sgtm, sgtp, sgtd, tgtd,
                gm, gp, cv1, cv2, yv1, yv2, yv3, csm1, csm2, ysm1, ysm2,
                ysm3, csp1, csp2, ysp1, ysp2, ysp3, csd1, zd, cfm1, cfm2,
                cfm3, cfp1, cfp2, cfp3;
        double [] bv1 = {-9.67,-0.62,1.26,-9.21,-0.62,-0.39,3.15};
        double [] bv2 ={12.7,9.19,15.5,9.05,9.19,2.86,857.9};
        double [] xv1 ={144.9e3,228.9e3,262.6e3,84.1e3,228.9e3,141.7e3,2222.e3};
        double [] xv2 ={190.3e3,205.2e3,185.2e3,101.1e3,205.2e3,315.9e3,164.8e3};
        double [] xv3 ={133.8e3,143.6e3,99.8e3,98.6e3,143.6e3,167.4e3,116.3e3};
        double [] bsm1={2.13,2.66,6.11,1.98,2.68,6.86,8.51};
        double [] bsm2 ={159.5,7.67,6.65,13.11,7.16,10.38,169.8};
        double [] xsm1={762.2e3,100.4e3,138.2e3,139.1e3,93.7e3,187.8e3,609.8e3};
        double [] xsm2 ={123.6e3,172.5e3,242.2e3,132.7e3,186.8e3,169.6e3,119.9e3};
        double [] xsm3 ={94.5e3,136.4e3,178.6e3,193.5e3,133.5e3,108.9e3,106.6e3};
        double [] bsp1 ={2.11,6.87,10.08,3.68,4.75,8.58,8.43};
        double [] bsp2 ={102.3,15.53,9.60,159.3,8.12,13.97,8.19};
        double [] xsp1 ={636.9e3,138.7e3,165.3e3,464.4e3,93.2e3,216.0e3,136.2e3};
        double [] xsp2 ={134.8e3,143.7e3,225.7e3,93.1e3,135.9e3,152.0e3,188.5e3};
        double [] xsp3 ={95.6e3,98.6e3,129.7e3,94.2e3,113.4e3,122.7e3,122.9e3};
        double [] bsd1={1.224,0.801,1.380,1.000,1.224,1.518,1.518};
        double [] bzd1={1.282,2.161,1.282,20.,1.282,1.282,1.282};
        double [] bfm1={1.0,1.0,1.0,1.0,0.92,1.0,1.0};
        double [] bfm2={0.0,0.0,0.0,0.0,0.25,0.0,0.0};
        double [] bfm3={0.0,0.0,0.0,0.0,1.77,0.0,0.0};
        double [] bfp1={1.0,0.93,1.0,0.93,0.93,1.0,1.0};
        double [] bfp2={0.0,0.31,0.0,0.19,0.31,0.0,0.0};
        double [] bfp3={0.0,2.00,0.0,1.79,2.00,0.0,0.0};
        boolean ws, w1;
        double rt=7.8, rl=24.0, avarv, q, vs, zt, zl, zc;
        double sgt, yr;
        int temp_radio_2 = LongleyRice_mod.radio_climate;

        cv1 = bv1[temp_radio_2];
        cv2 = bv2[temp_radio_2];
        yv1 = xv1[temp_radio_2];
        yv2 = xv2[temp_radio_2];
        yv3 = xv3[temp_radio_2];
        csm1=bsm1[temp_radio_2];
        csm2=bsm2[temp_radio_2];
        ysm1=xsm1[temp_radio_2];
        ysm2=xsm2[temp_radio_2];
        ysm3=xsm3[temp_radio_2];
        csp1=bsp1[temp_radio_2];
        csp2=bsp2[temp_radio_2];
        ysp1=xsp1[temp_radio_2];
        ysp2=xsp2[temp_radio_2];
        ysp3=xsp3[temp_radio_2];
        csd1=bsd1[temp_radio_2];
        zd  =bzd1[temp_radio_2];
        cfm1=bfm1[temp_radio_2];
        cfm2=bfm2[temp_radio_2];
        cfm3=bfm3[temp_radio_2];
        cfp1=bfp1[temp_radio_2];
        cfp2=bfp2[temp_radio_2];
        cfp3=bfp3[temp_radio_2];

        kdv = LongleyRice_mod.mdvar;
        ws = kdv>=20;
        w1 = kdv>=10;
        if(w1)
            kdv-=10;
        if(kdv<0 || kdv>3){
            kdv=0;
        }
        q=Math.log(0.133*param2.k);
        gm=cfm1+cfm2/(Math.pow(cfm3*q,2.0)+1.0);
        gp=cfp1+cfp2/(Math.pow(cfp3*q,2.0)+1.0);
        dexa=Math.sqrt(18e6*param2.he1)+Math.sqrt(18e6*param2.he2) + Math.cbrt(575.7e12/param2.k);
        if((link.getTxRxDistance()*1000)<dexa)
            de=130e3*(link.getTxRxDistance()*1000)/dexa;
        else
            de=130e3+(link.getTxRxDistance()*1000)-dexa;

        vmd=curve(cv1,cv2,yv1,yv2,yv3,de);
        sgtm=curve(csm1,csm2,ysm1,ysm2,ysm3,de) * gm;
        sgtp=curve(csp1,csp2,ysp1,ysp2,ysp3,de) * gp;
        sgtd=sgtp*csd1;
        tgtd=(sgtp-sgtd)*zd;
        if(w1)
            sgl=0.0;
        else {
            q=(1.0-0.8*Math.exp(-(link.getTxRxDistance()*1000)/50e3))*LongleyRice_mod.input.terrainIrregular()*param2.k;
            sgl=10.0*q/(q+13.0);
        }
        if(ws)
            vs0=0.0;
        else
            vs0=Math.pow(5.0+3.0*Math.exp(-de/100e3),2.0);
        param2.lvar=0;

        zt=zzt;
        zl=zzl;
        zc=zzc;
        switch(kdv){
            case 0:
                zt=zc;
                zl=zc;
                break;
            case 1:
                zl=zc;
                break;
            case 2:
                zl=zt;
        }
        if(zt<0.0)
            sgt=sgtm;
        else if(zt<=zd)
            sgt=sgtp;
        else
            sgt=sgtd+tgtd/zt;
        vs=vs0+Math.pow(sgt*zt,2.0)/(rt+zc*zc)+Math.pow(sgl*zl,2.0)/(rl+zc*zc);
        if(kdv==0){
            yr=0.0;
            param2.sgc=Math.sqrt(sgt*sgt+sgl*sgl+vs);
        }
        else if(kdv==1){
            yr=sgt*zt;
            param2.sgc=Math.sqrt(sgl*sgl+vs);
        }
        else if(kdv==2){
            yr=Math.sqrt(sgt*sgt+sgl*sgl)*zt;
            param2.sgc=Math.sqrt(vs);
        }
        else {
            yr=sgt*zt+sgl*zl;
            param2.sgc=Math.sqrt(vs);
        }
        avarv=aref-vmd-yr-param2.sgc*zc;
        if(avarv<0.0)
            avarv=avarv*(29.0-avarv)/(29.0-10.0*avarv);
        return avarv;
    }

    static double stat(LinkResult link, double aref, Param param, Param2 param2) {
        double zt, zc, zl, fs, dbloss;

        zt = querfi(param.pctTime);
        zl = querfi(param.pctLoc);
        zc = querfi(param.pctConf);

        fs = 32.45 + 20.0 * Math.log10(link.getFrequency()) + 20.0 * Math.log10(link.getTxRxDistance());
        dbloss = fs + avar(zt, zl, zc, link, aref, param2);
        return dbloss;
    }
}


class Att {
    public static double Att_2(double d, Param_att param_att) {
        return param_att.Aed + param_att.md*d*1000;
    }

    public static double Att_1(double d, Param_att param_att, Param_tmp param_tmp) {
        return param_att.Ael + param_tmp.ak1*d*1000 + param_tmp.ak2*Math.log(d*1000);
    }

    public static double Att_3(double d, Param_att param_att) {
        return param_att.Aes + param_att.ms*d*1000;
    }

    static double att(LinkResult link, Param_att param_att, Param_tmp param_tmp, Param2 param2) {
        param_att.Aref2 = Att_2(link.getTxRxDistance(), param_att);
        param_att.Aref1 = Att_1(link.getTxRxDistance(), param_att, param_tmp);
        param_att.Aref3 = Att_3(link.getTxRxDistance(), param_att);
        if ((link.getTxRxDistance()*1000) <= param2.dls) {
            return param_att.Aref1;
        } else if ((param2.dls < (link.getTxRxDistance()*1000)) && ((link.getTxRxDistance()*1000) <= param_att.dx)){
            return param_att.Aref2;
        } else {
            return param_att.Aref3;
        }
    }
}
