/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.eb;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas12.physics.DetectorEvent;
import org.jlab.clas12.physics.DetectorParticle;
import org.jlab.data.io.DataEvent;
import org.jlab.evio.clas12.EvioDataBank;
import org.jlab.geom.prim.Path3D;

/**
 *
 * @author gavalian
 */
public class EventBuilder {
    
    /**
     * Read the event and if the Tracking bank is present, make a list of tracks 
     * and make a List of DetectorParticle classes. DetectorParticle contains
     * the momentum of the particle, the cross, direction.
     * @param event
     * @return list of detector particles
     */
    public static List<DetectorParticle>  getTracks(DataEvent event){
        List<DetectorParticle>   trackPaths = new ArrayList<DetectorParticle>();
        if(event.hasBank("TimeBasedTrkg::TBTracks")==true){
                        
            EvioDataBank bank = (EvioDataBank) event.getBank("TimeBasedTrkg::TBTracks");
            int nrows = bank.rows();
            
            for(int loop = 0; loop < nrows; loop++){
                
                DetectorParticle   particle = new DetectorParticle();
                particle.setPid(0);
                particle.setStatus(100);
                
                particle.setPath(bank.getDouble("pathlength", loop));
                
                particle.setCharge(bank.getInt("q", loop));
                
                particle.setCross(
                        bank.getDouble("c3_x", loop), 
                        bank.getDouble("c3_y", loop),
                        bank.getDouble("c3_z", loop),
                        bank.getDouble("c3_ux", loop), 
                        bank.getDouble("c3_uy", loop),
                        bank.getDouble("c3_uz", loop)
                );
                //part.charge = bank.getInt("q", loop);
                particle.vector().setXYZ(
                        bank.getDouble("p0_x",loop),
                        bank.getDouble("p0_y",loop),
                        bank.getDouble("p0_z",loop)
                );
                particle.vertex().setXYZ(
                        bank.getDouble("Vtx0_x",loop),
                        bank.getDouble("Vtx0_y",loop),
                        bank.getDouble("Vtx0_z",loop)
                );
                
                trackPaths.add(particle);
            }
        }
        return trackPaths;
    }
    
    public static DetectorEvent  getDetectorEvent(DataEvent event){
        List<DetectorParticle>  particles = EventBuilder.getTracks(event);
        DetectorEvent detEvent = new DetectorEvent();
        for(DetectorParticle p : particles){
            detEvent.addParticle(p);
        }
        return detEvent;
    }
}
