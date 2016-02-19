/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.rec.eb;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author gavalian
 */
public class RecParticle implements Comparable {
    
    private final RecDetectorHitStore  hitStore = new RecDetectorHitStore();

    public  int       trackID = -1;
    public  Point3D   trackCross = new Point3D();
    public  double    trackCrossPath = 0;
    public  Vector3D  trackCrossDir = new Vector3D();
    public  int       status  = 0;
    public  Vector3D  vector  = new Vector3D();
    public  Vector3D  vertex  = new Vector3D();
    public  int       charge  = 0;
    public  int       pid     = 0;
    public  double    mass    = 0.0;
    public  double    beta    = 0.0;
    public  double    chi2pid = 0.0;
    
    public RecParticle(){
        
    }
    
    public RecDetectorHitStore getHitStore(){
        return hitStore;
    }
    
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("===> %4d %5d %3d %8.5f %8.5f ",status, pid,charge,beta,mass));
        str.append(String.format(" %8.3f ( %8.3f %8.3f %8.3f) \n", vector.mag(),
                vector.x(),vector.y(),vector.z()));
        for(int loop =0; loop < hitStore.size(); loop++){
            str.append(hitStore.get(loop).toString());
            str.append("\n");
        }
        return str.toString();
    }

    @Override
    public int compareTo(Object o) {
        RecParticle obj = (RecParticle) o;
        if(this.pid==11){
            if(obj.pid!=11){
                return 1;
            } else {
                if(this.chi2pid>obj.chi2pid){
                    return 1;
                } else return -1;
            }
        } else {
            if(this.pid>obj.pid) return 1;
        }
        return 0;
    }
    
    public void doMass(){
        //
        if(this.status==100){
            RecDetectorHit hit = hitStore.getHit(3, 0, 0);
            if(hit!=null){
                double path = this.trackCrossPath + this.trackCross.distance(hit.matchPosition);
                this.beta   = path/hit.time/30.0;
                double beta2 = this.beta*this.beta;
                this.mass    = this.vector.mag2()*(1.0-beta2)/beta2;
            }
            return;
        }
        
        if(hitStore.hasHit(1, 1, 0)==true){
            RecDetectorHit hit = hitStore.getHit(1, 1, 0);
            this.beta = hit.trackPath/hit.time/30.0;
            double beta2 = this.beta*this.beta;
            this.mass = this.vector.mag2()*(1.0-beta2)/(beta2);
        }
    }
}
