/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.rec.eb;

import java.util.TreeMap;
import org.jlab.data.func.F1D;

/**
 *
 * @author gavalian
 */
public class ParticleID {
    
    private TreeMap<Integer,F1D>  betaLogFunctions = new TreeMap<Integer,F1D>();
    private TreeMap<Integer,F1D>  pcalLogFunctions = new TreeMap<Integer,F1D>();
    private TreeMap<Integer,F1D>  ecinLogFunctions = new TreeMap<Integer,F1D>();
    private TreeMap<Integer,F1D>  eoutLogFunctions = new TreeMap<Integer,F1D>();
            
    public static double getBeta(double momentum, double mass){
        double energy = Math.sqrt(momentum*momentum + mass*mass);
        if(energy==0.0) return 0.0;
        return momentum/energy;
    }
    
    public static void determinePID(RecParticle particle){
        double beta = particle.beta;
        double betadiff = 100.0;
        double[] masses = {0.005,0.135,0.497,0.938};
        int[]    pid    = {-11,211,321,2212};
        int[]    pidn   = {11,-211,-321,-2212};
        for(int loop = 0; loop < masses.length; loop++){
            if(Math.abs(beta - ParticleID.getBeta(particle.vector.mag(), masses[loop]))
                    <betadiff){
                betadiff = Math.abs(beta - ParticleID.getBeta(particle.vector.mag(), masses[loop]));
                if(particle.charge<0){
                    particle.pid = pidn[loop];
                } else {
                    particle.pid = pid[loop];
                }
            }
        }
        //System.err.println(" check the pid electron ");
        if(particle.getHitStore().hasHit(2, 0, 0)){
            //System.err.println(" hit found check the pid electron ");
            RecDetectorHit hit = particle.getHitStore().getHit(2, 0, 0);
            if(hit.energy>0.0){
                double ratio = hit.energy/particle.vector.mag();
                //System.err.println(" HIT found  PID = " + particle.pid +  " ratio = " + ratio);
                if(ratio>50.0 && particle.charge<0){
                    //System.err.println("Found electron current id is = " + particle.pid);
                    particle.pid = 11;
                }
                if(ratio<50.0 && particle.charge<0){
                    //System.err.println("Found electron current id is = " + particle.pid);
                    particle.pid = -211;
                }
            }
        }
    }
    
    public void initFunctions(){
        
        this.betaLogFunctions.put(   11, new F1D("gaus",0.0,2.0));
        this.betaLogFunctions.put(  211, new F1D("gaus",0.0,2.0));
        this.betaLogFunctions.put(  321, new F1D("gaus",0.0,2.0));
        this.betaLogFunctions.put( 2212, new F1D("gaus",0.0,2.0));
        
    }
}
