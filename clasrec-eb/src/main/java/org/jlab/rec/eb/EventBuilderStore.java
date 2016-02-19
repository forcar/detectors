/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.eb;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas12.physics.DetectorEvent;
import org.jlab.clas12.physics.DetectorParticle;
import org.jlab.clas12.physics.DetectorResponse;
import org.jlab.evio.clas12.EvioDataBank;
import org.jlab.evio.clas12.EvioDataEvent;


/**
 *
 * @author gavalian
 */
public class EventBuilderStore {
    
    private DetectorEvent   detectorEvent = new DetectorEvent();
    private List<DetectorResponse>  detectorResponses = new ArrayList<DetectorResponse>();
    
    public EventBuilderStore(){
        
    }
    /**
     * Initialize particle information from the Time based tracking.
     * @param event 
     */
    public void initForwardParticles(EvioDataEvent event){
        
        
        //if(event.hasBank("HitBasedTrkg::HBTracks")==true){ 
        if(event.hasBank("TimeBasedTrkg::TBTracks")==true){ 
            //System.out.println("  FOUND TBTRACKS ");
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
                
                this.detectorEvent.addParticle(particle);
            }
            
        }
    }
    
    
    public void initCentralParticles(EvioDataEvent event){
        if(event.hasBank("BSTRec::Tracks")==true){
            EvioDataBank bankSVT = (EvioDataBank) event.getBank("BSTRec::Tracks");
            int nrows = bankSVT.rows();
            for(int loop = 0; loop < nrows; loop++){
                
                DetectorParticle  particle = new DetectorParticle();

                particle.setCross(
                    bankSVT.getDouble("c_x", loop), 
                    bankSVT.getDouble("c_y", loop),
                    bankSVT.getDouble("c_z", loop),
                    bankSVT.getDouble("c_ux", loop), 
                    bankSVT.getDouble("c_uy", loop),
                    bankSVT.getDouble("c_uz", loop)
                );

                particle.setCharge( bankSVT.getInt("q", loop));
                
                double tandip = bankSVT.getDouble("tandip", loop);
                double phi    = bankSVT.getDouble("phi0", loop);
                double pt     = bankSVT.getDouble("pt", loop);
                double p      = bankSVT.getDouble("p" , loop);
                //double sinth  = tandip/Math.sqrt(1+tandip*tandip);
                double pz     = pt*tandip;
                double px     = pt*Math.cos(phi);
                double py     = pt*Math.sin(phi);
                particle.vector().setXYZ(
                        px,py,pz
                );
                particle.vertex().setXYZ(0.0,0.0,
                        bankSVT.getDouble("z0", loop)
                );
                particle.setPath(bankSVT.getDouble("pathlength", loop));
                particle.setStatus(200);
                this.detectorEvent.addParticle(particle);
            }
        }
    }
    
    public void initDetectorResponses_EC(EvioDataEvent event){
        if(event.hasBank("ECRec::clusters")==true){
            EvioDataBank bankEC = (EvioDataBank) event.getBank("ECRec::clusters");
            int nrowsEC = bankEC.rows();
            for (int loop = 0; loop < nrowsEC; loop++){
                RecDetectorHit hit = new RecDetectorHit();
                
                DetectorResponse  response = new DetectorResponse();
                response.getDescriptor().setType(org.jlab.clas.detector.DetectorType.EC);
                response.getDescriptor().setSectorLayerComponent(
                        bankEC.getInt("sector",loop),
                        bankEC.getInt("superlayer", loop)
                        , -1);
                
                response.setPosition(
                        bankEC.getDouble("X", loop), 
                        bankEC.getDouble("Y", loop), 
                        bankEC.getDouble("Z", loop)
                );
                
                response.setEnergy(bankEC.getDouble("energy", loop));
                response.setTime(bankEC.getDouble("time", loop));
                
                hit.detector   = 2;
                hit.sector     = bankEC.getInt("sector",loop);
                hit.superlayer = bankEC.getInt("superlayer", loop);
                hit.layer      = 0;
                hit.hitPosition.set(
                        bankEC.getDouble("X", loop), 
                        bankEC.getDouble("Y", loop), 
                        bankEC.getDouble("Z", loop)
                );
                hit.time   = bankEC.getDouble("time", loop);
                hit.energy = bankEC.getDouble("energy", loop);
                //recDetectorHits.addHit(hit);
                this.detectorResponses.add(response);
            }
        }
    }
    
    public void initDetectorResponses_FTOF(EvioDataEvent event){
        
        //System.out.println(" BANK FTOF = " + event.hasBank("FTOFRec::ftofhits"));
        if(event.hasBank("FTOFRec::ftofhits")==false) return;
        EvioDataBank bank = (EvioDataBank) event.getBank("FTOFRec::ftofhits");
        int counter = 0;
        int nrows = bank.rows();
 
        for(int loop = 0; loop < nrows; loop++){
            DetectorResponse  response = new DetectorResponse();
            response.getDescriptor().setType(org.jlab.clas.detector.DetectorType.FTOF);
            response.getDescriptor().setSectorLayerComponent(
                    bank.getInt("sector", loop),
                    bank.getInt("panel_id", loop),
                    bank.getInt("paddle_id", loop)
                    );
            response.getPosition().setXYZ( bank.getFloat("x", loop),
                            bank.getFloat("y", loop),
                            bank.getFloat("z", loop)
            );
            
            response.setTime(bank.getFloat("time", loop));
            response.setEnergy(bank.getFloat("energy", loop)); 
            this.detectorResponses.add(response);
        }
    }
    
    public void initDetectorResponses(EvioDataEvent event){
        this.initDetectorResponses_FTOF(event);
        this.initDetectorResponses_EC(event);
    }
    
    public void doDetectorMatching(){
        for(int loop = 0; loop < this.detectorEvent.getParticles().size(); loop++){
            DetectorParticle  particle = this.detectorEvent.getParticles().get(loop);
            int index = particle.getDetectorHit(detectorResponses, 
                    org.jlab.clas.detector.DetectorType.FTOF, 1, 20.0);
            //System.out.println(" Detector matching index = " + index);
            if(index>=0){
                particle.addResponse(this.detectorResponses.get(index));
            }
        }
        
        for(int loop = 0; loop < this.detectorEvent.getParticles().size(); loop++){
            DetectorParticle  particle = this.detectorEvent.getParticles().get(loop);
            int index = particle.getDetectorHit(detectorResponses, 
                    org.jlab.clas.detector.DetectorType.EC, 0, 30.0);            
            //System.out.println(" Detector matching index = " + index);
            if(index>=0){
                particle.addResponse(this.detectorResponses.get(index));
            }
            index = particle.getDetectorHit(detectorResponses, 
                    org.jlab.clas.detector.DetectorType.EC, 1, 30.0); 
            //System.out.println(" Detector matching index = " + index);
            if(index>=0){
                particle.addResponse(this.detectorResponses.get(index));
            }
            index = particle.getDetectorHit(detectorResponses, 
                    org.jlab.clas.detector.DetectorType.EC, 2, 30.0); 
            //System.out.println(" Detector matching index = " + index);
            if(index>=0){
                particle.addResponse(this.detectorResponses.get(index));
            }
            
        }
        
    }
    
    public DetectorEvent getDetectorEvent(){
        return this.detectorEvent;
    }
    
    
    public EvioDataBank  getParticleBank(EvioDataEvent event){
        int nrows = this.detectorEvent.getParticles().size();
        EvioDataBank  bank = (EvioDataBank) event.getDictionary().createBank("EVENTHB::particle", nrows);
        for(int loop = 0; loop < nrows; loop++){
            DetectorParticle part = this.detectorEvent.getParticles().get(loop);
            bank.setInt  ( "status" , loop, (int)  part.getStatus());
            bank.setInt  ( "charge" , loop, (int)  part.getCharge());
            bank.setInt  ( "pid"    , loop, (int)   part.getPid());
            bank.setFloat( "px"     , loop, (float) part.vector().x());
            bank.setFloat( "py"     , loop, (float) part.vector().y());
            bank.setFloat( "pz"     , loop, (float) part.vector().z());
            bank.setFloat( "vx"     , loop, (float) part.vertex().x());
            bank.setFloat( "vy"     , loop, (float) part.vertex().y());
            bank.setFloat( "vz"     , loop, (float) part.vertex().z());
            bank.setFloat( "beta"   , loop, (float) part.getBeta());
            bank.setFloat( "mass"   , loop, (float) part.getMass());
        }
        return bank;
    }
    
    public void show(){
        System.out.println("---------------------->  DETECTOR RESPONSES ");
        for(DetectorResponse resp : this.detectorResponses){
            System.out.println(resp);
        }
        System.out.println("---------------------->  DONE DETECTOR RESPONSES ");
    }
}
