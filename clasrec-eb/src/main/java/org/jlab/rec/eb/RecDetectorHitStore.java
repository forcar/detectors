/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.rec.eb;

import java.util.ArrayList;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author gavalian
 */
public class RecDetectorHitStore {
    
    private final ArrayList<RecDetectorHit>  hitStore = new ArrayList<RecDetectorHit>();
    public RecDetectorHitStore(){
        
    }
    
    public int size(){
        return hitStore.size();
    }
    
    public RecDetectorHit get(int i){
        return hitStore.get(i);
    }
    
    public boolean hasHit(int det, int superlayer, int layer){
        for(RecDetectorHit hit : hitStore){
            if(hit.detector==det && hit.superlayer == superlayer
                    && hit.layer==layer) return true;
        }
        return false;
    }
    
    public RecDetectorHit getHit(int det, int superlayer, int layer){
        for(RecDetectorHit hit : hitStore){
            if(hit.detector==det && hit.superlayer == superlayer
                    && hit.layer==layer) return hit;
        }
        return null;
    }
    
    public void removeHit(int det, int superlayer, int layer){
        for(RecDetectorHit hit : hitStore){
            if(hit.detector==det && hit.superlayer == superlayer
                    && hit.layer==layer){
                hitStore.remove(hit);
            }
        }
    }
    
    public void removeHit(int index){
        this.hitStore.remove(index);
    }
    
    public void addHit(RecDetectorHit hit){
        hitStore.add(hit);
    }
    
    public ArrayList<RecDetectorHit>  getDetectorHits(int det, int sup, int lay){
        ArrayList<RecDetectorHit> list = new ArrayList<RecDetectorHit>();
        for(RecDetectorHit hit : this.hitStore){
            if(hit.detector==det&&hit.superlayer==sup&&hit.layer==lay){
                list.add(hit);
            }
        }
        return list;
    }
    
    public int matchDetectorHit(Point3D origin, 
            Vector3D vec, 
            double tolerance, int det, int superlayer, int layer){
        int index = -1;
        double distance = 1000.0;
        Line3D  vecLine = new Line3D(origin.x(),origin.y(),origin.z(),
                vec.x()*1500.0,vec.y()*1500.0,vec.z()*1500.0);
        
        for(int loop = 0; loop < this.hitStore.size(); loop++){
            RecDetectorHit detHit = this.hitStore.get(loop);
            //System.out.println("  FOUNT DET = " + detHit.detector + 
            //        "  " + detHit.superlayer + " ");
            if(detHit.detector == det
                    && detHit.superlayer == superlayer
                    &&  detHit.layer == layer){

                double hdist = vecLine.distance(detHit.hitPosition).length();
                if(hdist<distance&&
                        hdist<tolerance){
                    distance = hdist;                    
                    index = loop;
                }
            }
        }
        return index;
    }
    
    public int matchDetectorHit(RecDetectorHit hit){
        int index = -1;
        double distance = 1000.0;
        for(int loop = 0; loop < this.hitStore.size(); loop++){
            RecDetectorHit detHit = this.hitStore.get(loop);
            if(hit.detector == detHit.detector 
                    && hit.superlayer == detHit.superlayer
                    && hit.layer == detHit.layer){
                double hdist = detHit.hitPosition.distance(hit.hitPosition);
                if(detHit.hitPosition.distance(hit.hitPosition)<distance&&
                        hdist<20.0){
                    distance = detHit.hitPosition.distance(hit.hitPosition);
                    hit.matchPosition.copy(detHit.hitPosition);
                    index = loop;
                }
            }
        }
        return index;
    }
    
    public void show(){
        System.out.println("-----> DETECTOR HITS....");
        for(RecDetectorHit hit : hitStore){            
            System.err.println(hit.toString());
        }
    }
}
