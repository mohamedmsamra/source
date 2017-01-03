package org.seamcat.model.propagation.p528.impl;

import org.seamcat.model.mathematics.Mathematics;

import static org.seamcat.model.mathematics.Mathematics.Qi;

/**
 * Calculates the interpolation for the ITU-R P.528
 *
 * note that for the height h1, the 1.5m is replaced by 1m in order to have only integers. This is taken into account in the rest of the code
 */
public class P528_Interpol {
    public static double PerteP528(double h1, double h2, double distance, double frequence, double Qt)
    {
        int bornes_h1[] ={1, 15,  30,  60,  1000,10000,20000};   //1.5 is replaced by 1 in order to have only integers. This is taken into account in the rest of the code
        int bornes_h2[] = {1000,10000,20000};
        int bornes_Qt[] = {1,5,10,50,95};
        int bornes_f[]  = {125,300,600,1200,2400,5100,9400,15500};
        double Lsup_Qt, Linf_Qt;
        double tmp_h;
        double Qt_tmp,Qinf,Qsup;

        Linf_Qt = 0;


        if (h1 > h2)
        {
            tmp_h = h1;
            h1 = h2;
            h2 = tmp_h;
        }

        Limite Limites_Qt = new Limite();
        Limites_Qt.setLimites((int)Qt, bornes_Qt);
        Limite Limites_f = new Limite();
        Limites_f.setLimites((int)frequence, bornes_f);

        Limite Limites_dist = new Limite();
        Limites_dist.inf = (int) distance;   //suppress decimal part
        if (Limites_dist.inf == distance)    //if distance was an integer
        {
            Limites_dist.action = false;
        }
        else
        {
            Limites_dist.sup=Limites_dist.inf+1;
            Limites_dist.action = true;
        }
        Limite Limites_h2 = new Limite();
        Limites_h2.setLimites((int)h2, bornes_h2);
        Limite Limites_h1 = new Limite();
        Limites_h1.setLimites((int)h1, bornes_h1);

        Linf_Qt=interpol.interpol_f(h2, h1, distance, Limites_dist, Limites_h1,Limites_h2,Limites_f, frequence,Limites_Qt.inf);

        if (Limites_Qt.action == true)
        {
            Lsup_Qt = interpol.interpol_f(h2, h1, distance, Limites_dist, Limites_h1,Limites_h2,Limites_f, frequence,Limites_Qt.sup);
            Qt_tmp=Qi(Qt / 100);
            Qinf=Qi((double) Limites_Qt.inf / 100);
            Qsup=Qi((double) Limites_Qt.sup / 100);
            Linf_Qt=Lsup_Qt*(Qinf-Qt_tmp)/(Qinf-Qsup)+Linf_Qt*(Qt_tmp-Qsup)/(Qinf-Qsup);
        }

        return Linf_Qt;
    }
}


class Limite {
    int inf;
    int sup;
    boolean action;

    void setLimites(int param,int tableau[]) {
        int i;
        int lim_i =tableau.length;

        this.action = true;
        for (i=0;i<lim_i;i++)
        {
            if (param==tableau[i])
            {
                this.inf=tableau[i];
                this.action = false;
                break;
            }
        }
        if (this.action==true)
        {
            for (i=0;i<lim_i;i++)
            {
                if (param<=tableau[i])
                {
                    this.sup=tableau[i];
                    break;
                }
            }
            if (i==0) this.action=false;
            for (i=0;i<lim_i;i++)
            {
                if (param>tableau[i])
                {
                    this.inf=tableau[i];
                }
            }
        }
    }
}

/**
 * In order to avoid a division by 0, the param_inf is set to param_inf=0.00000000000001 (e.g. for distances between 0 and 1 km)
 */
class interpol {
    static double interpol_log(double Linf, double Lsup, double param, double param_inf, double param_sup)
    {
        if (Mathematics.equals(param_inf, 0, 0.01)) {
            param_inf=0.00000000000001; //correction runtime error due to division by 0 if param_inf = 0 (e.g. for distances between 0 and 1 km)
        }
        return ((double)(Linf+(Lsup - Linf) * Math.log10((param / param_inf)) / Math.log10((param_sup / param_inf))));
    }

    public static double recherche_val_classe2(double[] tableau, int h1, int h2)
    {
        int no_colonne;
        int val_add;
        double valeur=1;

        switch (h2)   // look for value to add as a function of h2
        {
            case 1000  : val_add=0;break;
            case 10000 : val_add=5;break;
            case 20000 : val_add=11;break;
            default    : val_add=11;
        }

        switch (h1)
        {
            case 1      : no_colonne= val_add;break;//corresponds to 1.5 (truncated to 1 by conversion to integer at method call)
            case 15	    : no_colonne= val_add+1;break;
            case 30 	: no_colonne= val_add+2;break;
            case 60     : no_colonne= val_add+3;break;
            case 1000   : no_colonne= val_add+4;break;
            case 10000  : no_colonne= val_add+5;break;
            case 20000  : no_colonne= val_add+6;break;
            default     : no_colonne= val_add+6;
        }
        valeur = tableau[no_colonne];
        return valeur;
    }

    /**
     * Method that interpolates the distance
     *
     * @param h2
     * @param h1
     * @param distance
     * @param Bornes_distance
     * @param table
     * @return
     */
    static double interpol_distance(double h2, double h1, double distance, Limite Bornes_distance, double[][] table)
    {
        double Linf, Lsup;

        Linf=recherche_val_classe2(table[0], (int) h1, (int) h2);

        if (Bornes_distance.action ==true)
        {
            Lsup=recherche_val_classe2(table[1], (int) h1, (int) h2);
            Linf=interpol_log(Linf, Lsup, distance, Bornes_distance.inf, Bornes_distance.sup);
        }

        return Linf;
    }

    /**
     * Method that interpolates the height h1
     *
     * @param h2
     * @param h1
     * @param distance
     * @param Bornes_distance
     * @param Bornes_h1
     * @param table
     * @return
     */
    static double interpol_h1(double h2, double h1, double distance, Limite Bornes_distance, Limite Bornes_h1, double[][] table)
    {
        double Linf, Lsup;

        Linf =interpol_distance(h2, Bornes_h1.inf, distance, Bornes_distance, table);
        if (Bornes_h1.action == true)
        {
            Lsup=interpol_distance(h2, Bornes_h1.sup, distance, Bornes_distance, table);
            if (Bornes_h1.inf==1)    //patch because Bornes_h1 has been truncated to 1 in order to avoid doubles everywhere
                Linf = interpol_log(Linf,Lsup,h1,1.5f, Bornes_h1.sup);   //
            else
                Linf = interpol_log(Linf,Lsup,h1,Bornes_h1.inf, Bornes_h1.sup);
        }
        return Linf;
    }

    /**
     * Method that interpolates the height h2
     *
     * @param h2
     * @param h1
     * @param distance
     * @param Bornes_distance
     * @param Bornes_h1
     * @param Bornes_h2
     * @param table
     * @return
     */
    static double interpol_h2(double h2, double h1, double distance, Limite Bornes_distance, Limite Bornes_h1,Limite Bornes_h2, double[][]  table)
    {
        double Linf, Lsup;

        Linf=interpol_h1(Bornes_h2.inf, h1, distance, Bornes_distance, Bornes_h1, table);
        if (Bornes_h2.action ==true)
        {
            Lsup=interpol_h1(Bornes_h2.sup, h1, distance, Bornes_distance, Bornes_h1, table);
            Linf=interpol_log(Linf,Lsup,h2,Bornes_h2.inf,Bornes_h2.sup);
        }

        return Linf;
    }

    /**
     * Method that interpolates the frequency
     *
     * @param h2
     * @param h1
     * @param distance
     * @param Bornes_distance
     * @param Bornes_h1
     * @param Bornes_h2
     * @param Bornes_f
     * @param freq
     * @param Qt
     * @return
     */
    static double interpol_f(double h2, double h1, double distance, Limite Bornes_distance, Limite Bornes_h1,Limite Bornes_h2,Limite Bornes_f, double freq,int Qt)
    {
        double Linf, Lsup;
        double[][] table;
        //look for which table to load

        table=findTableName(Qt,Bornes_f.inf,distance);
        Linf=interpol_h2(h2,h1,distance,Bornes_distance,Bornes_h1,Bornes_h2,table);
        if (Bornes_f.action==true)
        {
            table=findTableName(Qt,Bornes_f.sup,(int)distance);
            Lsup=interpol_h2(h2,h1,distance,Bornes_distance,Bornes_h1,Bornes_h2,table);
            Linf=interpol_log(Linf,Lsup,freq,Bornes_f.inf,Bornes_f.sup);
        }

        return Linf;
    }

    /**
     * Method that select the table
     *
     * tests are stopped before 400 (i.e. <400 but not <=400) because the modulo 400 would get the value to come bas to
     * tables are limited to 400 values because of compilation problems when the tables are too large
     *
     * @param Qt
     * @param f
     * @param distance
     * @return
     */
    static double[][] findTableName(int Qt, int f, double distance){
        double[][] tableauTrouve;
        double [][] tableau2lignes = new double[2][18];

        switch(Qt)
        {
            case 1 :
                switch(f)
                {
                    case 125:
                        if (distance <400)  tableauTrouve= T125_01_0_400.t;
                        else if (distance <800)  tableauTrouve=T125_01_400_800.t;
                        else if (distance <1200)  tableauTrouve=T125_01_800_1200.t;
                        else if (distance <1600)  tableauTrouve=T125_01_1200_1600.t;
                        else  tableauTrouve=T125_01_1600_1800.t;
                        break;
                    case 300:
                        if (distance <400)  tableauTrouve= T300_01_0_400.t;
                        else if (distance <800)  tableauTrouve= T300_01_400_800.t;
                        else if (distance <1200)  tableauTrouve= T300_01_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T300_01_1200_1600.t;
                        else  tableauTrouve= T300_01_1600_1800.t;
                        break;
                    case 600:
                        if (distance <400)  tableauTrouve= T600_01_0_400.t;
                        else if (distance <800)  tableauTrouve= T600_01_400_800.t;
                        else if (distance <1200)  tableauTrouve= T600_01_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T600_01_1200_1600.t;
                        else  tableauTrouve= T600_01_1600_1800.t;
                        break;
                    case 1200:
                        if (distance <400)  tableauTrouve= T1200_01_0_400.t;
                        else if (distance <800)  tableauTrouve= T1200_01_400_800.t;
                        else if (distance <1200)  tableauTrouve= T1200_01_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T1200_01_1200_1600.t;
                        else  tableauTrouve= T1200_01_1600_1800.t;
                        break;
                    case 2400:
                        if (distance <400)  tableauTrouve= T2400_01_0_400.t;
                        else if (distance <800)  tableauTrouve= T2400_01_400_800.t;
                        else if (distance <1200)  tableauTrouve= T2400_01_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T2400_01_1200_1600.t;
                        else  tableauTrouve= T2400_01_1600_1800.t;
                        break;
                    case 5100:
                        if (distance <400)  tableauTrouve= T5100_01_0_400.t;
                        else if (distance <800)  tableauTrouve= T5100_01_400_800.t;
                        else if (distance <1200)  tableauTrouve= T5100_01_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T5100_01_1200_1600.t;
                        else  tableauTrouve= T5100_01_1600_1800.t;
                        break;
                    case 9400:
                        if (distance <400)  tableauTrouve= T9400_01_0_400.t;
                        else if (distance <800)  tableauTrouve= T9400_01_400_800.t;
                        else if (distance <1200)  tableauTrouve= T9400_01_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T9400_01_1200_1600.t;
                        else  tableauTrouve= T9400_01_1600_1800.t;
                        break;
                    default://include the case 15500
                        if (distance <400)  tableauTrouve= T15500_01_0_400.t;
                        else if (distance <800)  tableauTrouve= T15500_01_400_800.t;
                        else if (distance <1200)  tableauTrouve= T15500_01_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T15500_01_1200_1600.t;
                        else  tableauTrouve= T15500_01_1600_1800.t;
                        break;
                }
                break;
            case 5 :
                switch(f)
                {
                    case 125:
                        if (distance <400)  tableauTrouve= T125_05_0_400.t;
                        else if (distance <800)  tableauTrouve=T125_05_400_800.t;
                        else if (distance <1200)  tableauTrouve=T125_05_800_1200.t;
                        else if (distance <1600)  tableauTrouve=T125_05_1200_1600.t;
                        else  tableauTrouve=T125_05_1600_1800.t;
                        break;
                    case 300:
                        if (distance <400)  tableauTrouve= T300_05_0_400.t;
                        else if (distance <800)  tableauTrouve= T300_05_400_800.t;
                        else if (distance <1200)  tableauTrouve= T300_05_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T300_05_1200_1600.t;
                        else  tableauTrouve= T300_05_1600_1800.t;
                        break;
                    case 600:
                        if (distance <400)  tableauTrouve= T600_05_0_400.t;
                        else if (distance <800)  tableauTrouve= T600_05_400_800.t;
                        else if (distance <1200)  tableauTrouve= T600_05_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T600_05_1200_1600.t;
                        else  tableauTrouve= T600_05_1600_1800.t;
                        break;
                    case 1200:
                        if (distance <400)  tableauTrouve= T1200_05_0_400.t;
                        else if (distance <800)  tableauTrouve= T1200_05_400_800.t;
                        else if (distance <1200)  tableauTrouve= T1200_05_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T1200_05_1200_1600.t;
                        else  tableauTrouve= T1200_05_1600_1800.t;
                        break;
                    case 2400:
                        if (distance <400)  tableauTrouve= T2400_05_0_400.t;
                        else if (distance <800)  tableauTrouve= T2400_05_400_800.t;
                        else if (distance <1200)  tableauTrouve= T2400_05_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T2400_05_1200_1600.t;
                        else  tableauTrouve= T2400_05_1600_1800.t;
                        break;
                    case 5100:
                        if (distance <400)  tableauTrouve= T5100_05_0_400.t;
                        else if (distance <800)  tableauTrouve= T5100_05_400_800.t;
                        else if (distance <1200)  tableauTrouve= T5100_05_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T5100_05_1200_1600.t;
                        else  tableauTrouve= T5100_05_1600_1800.t;
                        break;
                    case 9400:
                        if (distance <400)  tableauTrouve= T9400_05_0_400.t;
                        else if (distance <800)  tableauTrouve= T9400_05_400_800.t;
                        else if (distance <1200)  tableauTrouve= T9400_05_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T9400_05_1200_1600.t;
                        else  tableauTrouve= T9400_05_1600_1800.t;
                        break;
                    default://inclut le case 15500
                        if (distance <400)  tableauTrouve= T15500_05_0_400.t;
                        else if (distance <800)  tableauTrouve= T15500_05_400_800.t;
                        else if (distance <1200)  tableauTrouve= T15500_05_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T15500_05_1200_1600.t;
                        else  tableauTrouve= T15500_05_1600_1800.t;
                        break;
                }
                break;
            case 10 :
                switch(f)
                {
                    case 125:
                        if (distance <400)  tableauTrouve= T125_10_0_400.t;
                        else if (distance <800)  tableauTrouve=T125_10_400_800.t;
                        else if (distance <1200)  tableauTrouve=T125_10_800_1200.t;
                        else if (distance <1600)  tableauTrouve=T125_10_1200_1600.t;
                        else  tableauTrouve=T125_10_1600_1800.t;
                        break;
                    case 300:
                        if (distance <400)  tableauTrouve= T300_10_0_400.t;
                        else if (distance <800)  tableauTrouve= T300_10_400_800.t;
                        else if (distance <1200)  tableauTrouve= T300_10_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T300_10_1200_1600.t;
                        else  tableauTrouve= T300_10_1600_1800.t;
                        break;
                    case 600:
                        if (distance <400)  tableauTrouve= T600_10_0_400.t;
                        else if (distance <800)  tableauTrouve= T600_10_400_800.t;
                        else if (distance <1200)  tableauTrouve= T600_10_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T600_10_1200_1600.t;
                        else  tableauTrouve= T600_10_1600_1800.t;
                        break;
                    case 1200:
                        if (distance <400)  tableauTrouve= T1200_10_0_400.t;
                        else if (distance <800)  tableauTrouve= T1200_10_400_800.t;
                        else if (distance <1200)  tableauTrouve= T1200_10_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T1200_10_1200_1600.t;
                        else  tableauTrouve= T1200_10_1600_1800.t;
                        break;
                    case 2400:
                        if (distance <400)  tableauTrouve= T2400_10_0_400.t;
                        else if (distance <800)  tableauTrouve= T2400_10_400_800.t;
                        else if (distance <1200)  tableauTrouve= T2400_10_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T2400_10_1200_1600.t;
                        else  tableauTrouve= T2400_10_1600_1800.t;
                        break;
                    case 5100:
                        if (distance <400)  tableauTrouve= T5100_10_0_400.t;
                        else if (distance <800)  tableauTrouve= T5100_10_400_800.t;
                        else if (distance <1200)  tableauTrouve= T5100_10_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T5100_10_1200_1600.t;
                        else  tableauTrouve= T5100_10_1600_1800.t;
                        break;
                    case 9400:
                        if (distance <400)  tableauTrouve= T9400_10_0_400.t;
                        else if (distance <800)  tableauTrouve= T9400_10_400_800.t;
                        else if (distance <1200)  tableauTrouve= T9400_10_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T9400_10_1200_1600.t;
                        else  tableauTrouve= T9400_10_1600_1800.t;
                        break;
                    default://inclut le case 15500
                        if (distance <400)  tableauTrouve= T15500_10_0_400.t;
                        else if (distance <800)  tableauTrouve= T15500_10_400_800.t;
                        else if (distance <1200)  tableauTrouve= T15500_10_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T15500_10_1200_1600.t;
                        else  tableauTrouve= T15500_10_1600_1800.t;
                        break;
                }
                break;
            case 50 :
                switch(f)
                {
                    case 125:
                        if (distance <400)  tableauTrouve= T125_50_0_400.t;
                        else if (distance <800)  tableauTrouve=T125_50_400_800.t;
                        else if (distance <1200)  tableauTrouve=T125_50_800_1200.t;
                        else if (distance <1600)  tableauTrouve=T125_50_1200_1600.t;
                        else  tableauTrouve=T125_50_1600_1800.t;
                        break;
                    case 300:
                        if (distance <400)  tableauTrouve= T300_50_0_400.t;
                        else if (distance <800)  tableauTrouve= T300_50_400_800.t;
                        else if (distance <1200)  tableauTrouve= T300_50_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T300_50_1200_1600.t;
                        else  tableauTrouve= T300_50_1600_1800.t;
                        break;
                    case 600:
                        if (distance <400)  tableauTrouve= T600_50_0_400.t;
                        else if (distance <800)  tableauTrouve= T600_50_400_800.t;
                        else if (distance <1200)  tableauTrouve= T600_50_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T600_50_1200_1600.t;
                        else  tableauTrouve= T600_50_1600_1800.t;
                        break;
                    case 1200:
                        if (distance <400)  tableauTrouve= T1200_50_0_400.t;
                        else if (distance <800)  tableauTrouve= T1200_50_400_800.t;
                        else if (distance <1200)  tableauTrouve= T1200_50_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T1200_50_1200_1600.t;
                        else  tableauTrouve= T1200_50_1600_1800.t;
                        break;
                    case 2400:
                        if (distance <400)  tableauTrouve= T2400_50_0_400.t;
                        else if (distance <800)  tableauTrouve= T2400_50_400_800.t;
                        else if (distance <1200)  tableauTrouve= T2400_50_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T2400_50_1200_1600.t;
                        else  tableauTrouve= T2400_50_1600_1800.t;
                        break;
                    case 5100:
                        if (distance <400)  tableauTrouve= T5100_50_0_400.t;
                        else if (distance <800)  tableauTrouve= T5100_50_400_800.t;
                        else if (distance <1200)  tableauTrouve= T5100_50_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T5100_50_1200_1600.t;
                        else  tableauTrouve= T5100_50_1600_1800.t;
                        break;
                    case 9400:
                        if (distance <400)  tableauTrouve= T9400_50_0_400.t;
                        else if (distance <800)  tableauTrouve= T9400_50_400_800.t;
                        else if (distance <1200)  tableauTrouve= T9400_50_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T9400_50_1200_1600.t;
                        else  tableauTrouve= T9400_50_1600_1800.t;
                        break;
                    default:   //includes case 15500
                        if (distance <400)  tableauTrouve= T15500_50_0_400.t;
                        else if (distance <800)  tableauTrouve= T15500_50_400_800.t;
                        else if (distance <1200)  tableauTrouve= T15500_50_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T15500_50_1200_1600.t;
                        else  tableauTrouve= T15500_50_1600_1800.t;
                        break;
                }
                break;
            default:
                //default=case 95 : corresponds to 95%
                switch(f)
                {
                    case 125:
                        if (distance <400)  	tableauTrouve= T125_95_0_400.t;
                        else if (distance <800)  tableauTrouve= T125_95_400_800.t;
                        else if (distance <1200)  tableauTrouve= T125_95_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T125_95_1200_1600.t;
                        else  tableauTrouve= T125_95_1600_1800.t;
                        break;
                    case 300:
                        if (distance <400)  tableauTrouve= T300_95_0_400.t;
                        else if (distance <800)  tableauTrouve= T300_95_400_800.t;
                        else if (distance <1200)  tableauTrouve= T300_95_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T300_95_1200_1600.t;
                        else  tableauTrouve= T300_95_1600_1800.t;
                        break;
                    case 600:
                        if (distance <400)  tableauTrouve= T600_95_0_400.t;
                        else if (distance <800)  tableauTrouve= T600_95_400_800.t;
                        else if (distance <1200)  tableauTrouve= T600_95_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T600_95_1200_1600.t;
                        else  tableauTrouve= T600_95_1600_1800.t;
                        break;
                    case 1200:
                        if (distance <400)  tableauTrouve= T1200_95_0_400.t;
                        else if (distance <800)  tableauTrouve= T1200_95_400_800.t;
                        else if (distance <1200)  tableauTrouve= T1200_95_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T1200_95_1200_1600.t;
                        else  tableauTrouve= T1200_95_1600_1800.t;
                        break;
                    case 2400:
                        if (distance <400)  tableauTrouve= T2400_95_0_400.t;
                        else if (distance <800)  tableauTrouve= T2400_95_400_800.t;
                        else if (distance <1200)  tableauTrouve= T2400_95_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T2400_95_1200_1600.t;
                        else  tableauTrouve= T2400_95_1600_1800.t;
                        break;
                    case 5100:
                        if (distance <400)  tableauTrouve= T5100_95_0_400.t;
                        else if (distance <800)  tableauTrouve= T5100_95_400_800.t;
                        else if (distance <1200)  tableauTrouve= T5100_95_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T5100_95_1200_1600.t;
                        else  tableauTrouve= T5100_95_1600_1800.t;
                        break;
                    case 9400:
                        if (distance <400)  tableauTrouve= T9400_95_0_400.t;
                        else if (distance <800)  tableauTrouve= T9400_95_400_800.t;
                        else if (distance <1200)  tableauTrouve= T9400_95_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T9400_95_1200_1600.t;
                        else  tableauTrouve= T9400_95_1600_1800.t;
                        break;
                    default://inclut le case 15500
                        if (distance <400)  tableauTrouve= T15500_95_0_400.t;
                        else if (distance <800)  tableauTrouve= T15500_95_400_800.t;
                        else if (distance <1200)  tableauTrouve= T15500_95_800_1200.t;
                        else if (distance <1600)  tableauTrouve= T15500_95_1200_1600.t;
                        else  tableauTrouve= T15500_95_1600_1800.t;
                        break;
                }
        }

        distance=distance%400;
        tableau2lignes[0]=tableauTrouve[(int)distance];
        tableau2lignes[1]=tableauTrouve[(int)distance+1];

        return tableau2lignes;
    }
}
