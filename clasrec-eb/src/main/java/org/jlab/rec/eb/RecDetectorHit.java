/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.rec.eb;

import org.jlab.geom.prim.Point3D;

/**
 *
 * @author gavalian
 */
public class RecDetectorHit {
    
    int  associatedParticle = -1;
    int  pindex  = -1;
    int  index   = -1;
    int  detector = -1;
    int  sector   = -1;
    int  superlayer = -1;
    int  layer      = -1;
    Point3D  matchPosition = new Point3D();
    Point3D  hitPosition   = new Point3D();
    double   trackPath = 0.0;
    double   time      = 0.0;
    double   energy    = 0.0;
    
    public RecDetectorHit(){
        
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("[%3d %3d %3d %3d ] [%3d %3d ] %8.3f %8.3f ", 
                detector,sector,superlayer,layer,pindex,index,time,energy));
        str.append(String.format("(%8.3f %8.3f %8.3f) (%8.3f %8.3f %8.3f)", 
                matchPosition.x(),
                matchPosition.y(),
                matchPosition.z(),
                hitPosition.x(),
                hitPosition.y(),
                hitPosition.z()
                ));
        return str.toString();
    }
}
