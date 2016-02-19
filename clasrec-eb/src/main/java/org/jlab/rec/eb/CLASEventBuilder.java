/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.rec.eb;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas12.physics.GenericKinematicFitter;
import org.jlab.clasrec.main.DetectorReconstruction;
import org.jlab.clasrec.utils.ServiceConfiguration;
import org.jlab.evio.clas12.EvioDataBank;
import org.jlab.evio.clas12.EvioDataEvent;

/**
 *
 * @author gavalian
 */
public class CLASEventBuilder extends DetectorReconstruction {
    private GenericKinematicFitter fitter = new GenericKinematicFitter(11.0,"11:X+:X-:Xn");
    private Integer debugMode = 0;
    public CLASEventBuilder() {
        super("EB", "gavalian", "1.0");
    }

    @Override
    public void processEvent(EvioDataEvent event) {
        
        EventBuilderStore store = new EventBuilderStore();
        /*
        if(event.hasBank("DC::dgtz")==true){
            System.out.println(" found DC::dgtz");
        }
        if(event.hasBank("TimeBasedTrkg::TBTracks")==true){ 
            System.out.println(" found TRACKING banks");
        }*/
        
        
        store.initForwardParticles(event);
        store.initCentralParticles(event);
        store.initDetectorResponses(event);
        store.doDetectorMatching();
        
        if(this.debugMode>0){
            System.out.println("******************>>>>>> DETECTOR EVENT ");
            System.out.println(store.getDetectorEvent().toString());
        }
        
        if(this.debugMode>1){
            System.out.println("******************>>>>>> DETECTOR RESPONSE STORE ");
            store.show();
        }
        /*
        PhysicsEvent  genEvent = this.fitter.getGeneratedEvent(event);        
        System.out.println(genEvent.toLundString());
        System.out.println(store.getDetectorEvent());
        store.show();
        */
        EvioDataBank bank = store.getParticleBank(event);
        event.appendBanks(bank);
        /*
        SEBEventStore store = new SEBEventStore();
        store.setDebugMode(debugMode);
        
        store.init(event);
        store.initReconstructedHits(event);
        
        //System.out.println(" processing event ");
        //store.in
        
        if(store.getDebugMode()>0){
            System.out.println("***********************************************");
            System.out.println("*    SEB DEBUG MODE ");
            System.out.println("***********************************************");
        }
        
        if(store.getDebugMode()>1){
            System.out.println("=====>  DETECTOR Reconstructed HITS");
            store.getReconstructedHits().show();
            System.out.println("=====> END REC HITS");
        }
        
        store.initMatchingFTOF(this.getGeometry("FTOF"));
        store.doMatchingFTOF(event);
        
        store.initMatchingEC(this.getGeometry("EC"));
        store.doMatchingEC(event);
        store.doMatchingCTOF();
        store.doParticleID();
        store.doNeutralMatching();
        store.initForwardParticles(event);
        //System.err.println(store.toString());
        //store.show();
        //store.getReconstructedHits().show();
        store.writeOutput(event);
        //System.out.println("debug mode ---> " + store.getDebugMode());
        
        if(store.getDebugMode()>3){
            PhysicsEvent genEvent = fitter.getGeneratedEvent(event);
            System.out.println("------> GENERATED EVENT ");
            System.out.println(genEvent.toString());
            System.out.println("------>");
        }
        
        if(store.getDebugMode()>0){
            System.out.println("------> RECONSTRUCTED PARTICLES");
            store.show();
        }
        
        if(store.getDebugMode()>1){            
            store.getReconstructedHits().show();
        }*/
    }

    @Override
    public void init() {
        this.requireGeometry("EC");
        this.requireGeometry("FTOF");        
    }

    @Override
    public void configure(ServiceConfiguration c) {
        if(c.hasItem("SEB","debug")==true){
            this.debugMode = Integer.parseInt(c.asString("SEB", "debug"));
            
            System.out.println("[SEB] Initializing debugging mode = " 
                    + this.debugMode + "  string " + c.asString("SEB", "debug")
            );
        }
    }

    
}
