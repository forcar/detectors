/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.rec.eb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.clas.physics.Vector3;
import org.jlab.evio.clas12.EvioDataBank;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.base.Detector;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.eb.RecParticle;

/**
 *
 * @author gavalian
 */
public class SEBEventStore {
    
    private ArrayList<RecParticle>  particles = new ArrayList<RecParticle>();
    private RecDetectorHitStore     recDetectorHits = new RecDetectorHitStore();
    private Integer                 sebDebugMode    = 0;
    
    public SEBEventStore(){
        
    }
    
    public void setDebugMode(int mode){
        this.sebDebugMode = mode;
    }
    
    public int getDebugMode(){
        return this.sebDebugMode;
    }
    /**
     * Fill the array of reconstructed particle candidates
     * @param event 
     */
    public void init(EvioDataEvent event){
        if(event.hasBank("TimeBasedTrkg::TBTracks")==true){ 
        //EvioDataBank bank = (EvioDataBank) event.getBank("HitBasedTrkg::HBTracks");
        //System.out.println("------------------>  FOUND A TRAKING BANK");
        EvioDataBank bank = (EvioDataBank) event.getBank("TimeBasedTrkg::TBTracks");
        int nrows = bank.rows();
        for(int loop = 0; loop < nrows; loop++){
            RecParticle part = new RecParticle();
            part.trackID = loop;
            part.trackCross.set(
                    bank.getDouble("c3_x", loop), 
                    bank.getDouble("c3_y", loop),
                    bank.getDouble("c3_z", loop)
            );
            
            part.trackCrossDir.setXYZ(
                    bank.getDouble("c3_ux", loop), 
                    bank.getDouble("c3_uy", loop),
                    bank.getDouble("c3_uz", loop)
            );
            
            part.charge = bank.getInt("q", loop);
            part.vector.setXYZ(
                    bank.getDouble("p0_x",loop),
                    bank.getDouble("p0_y",loop),
                    bank.getDouble("p0_z",loop)
            );
            part.vertex.setXYZ(
                    bank.getDouble("Vtx0_x",loop),
                    bank.getDouble("Vtx0_y",loop),
                    bank.getDouble("Vtx0_z",loop)
            );
            part.trackCrossPath = bank.getDouble("pathlength", loop);
            particles.add(part);
        }
        }
        this.initCentralParticles(event);
        
    }
    
    private void initCentralParticles(EvioDataEvent event){
        //System.out.println(" CENTRAL DETECTORS : " + event.hasBank("BSTRec::Tracks"));
        if(event.hasBank("BSTRec::Tracks")==true){
            EvioDataBank bankSVT = (EvioDataBank) event.getBank("BSTRec::Tracks");
            int nrows = bankSVT.rows();
            for(int loop = 0; loop < nrows; loop++){
                RecParticle part = new RecParticle();
                part.trackID = this.particles.size();
                part.trackCross.set(
                    bankSVT.getDouble("c_x", loop), 
                    bankSVT.getDouble("c_y", loop),
                    bankSVT.getDouble("c_z", loop)
                );
                part.trackCrossDir.setXYZ(
                    bankSVT.getDouble("c_ux", loop), 
                    bankSVT.getDouble("c_uy", loop),
                    bankSVT.getDouble("c_uz", loop)
                );
                part.charge = bankSVT.getInt("q", loop);
                double tandip = bankSVT.getDouble("tandip", loop);
                double phi    = bankSVT.getDouble("phi0", loop);
                double pt     = bankSVT.getDouble("pt", loop);
                double p      = bankSVT.getDouble("p" , loop);
                //double sinth  = tandip/Math.sqrt(1+tandip*tandip);
                double pz     = pt*tandip;
                double px     = pt*Math.cos(phi);
                double py     = pt*Math.sin(phi);
                part.vector.setXYZ(
                        px,py,pz
                );
                part.vertex.setXYZ(0.0,0.0,
                        bankSVT.getDouble("z0", loop)
                );
                part.trackCrossPath = bankSVT.getDouble("pathlength", loop);
                part.status         = 100;
                this.particles.add(part);
            }
        }
    }
    
    public RecDetectorHitStore getReconstructedHits(){
        return recDetectorHits;
    }
    
    public void initReconstructedHits(EvioDataEvent event){
        if(event.hasBank("FTOFRec::ftofhits")==true){
            EvioDataBank bank = (EvioDataBank) event.getBank("FTOFRec::ftofhits");
            int nrows = bank.rows();
            for(int loop = 0; loop < nrows; loop++){
                RecDetectorHit hit = new RecDetectorHit();
                hit.detector   = 1;
                hit.superlayer = bank.getInt("panel_id", loop);
                hit.sector     = bank.getInt("sector", loop);
                hit.hitPosition.set(
                        bank.getFloat("x", loop), 
                        bank.getFloat("y", loop), 
                        bank.getFloat("z", loop)
                );
                hit.time   = bank.getFloat("time", loop);
                hit.energy = bank.getFloat("energy", loop);
                recDetectorHits.addHit(hit);
            }
        }
        
        if(event.hasBank("ECRec::clusters")==true){
            EvioDataBank bankEC = (EvioDataBank) event.getBank("ECRec::clusters");
            int nrowsEC = bankEC.rows();
            for (int loop = 0; loop < nrowsEC; loop++){
                RecDetectorHit hit = new RecDetectorHit();
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
                recDetectorHits.addHit(hit);
            }
        }
            //System.out.println("ADDING CTOF HITS");
        if(event.hasBank("CTOFRec::ctofhits")==true){
            EvioDataBank bankCTOF = (EvioDataBank) event.getBank("CTOFRec::ctofhits");
            int nrowsCTOF = bankCTOF.rows();
            for(int loop = 0; loop < nrowsCTOF; loop++){
                //System.out.println("ADDING CTOF HITS rows = " + nrowsCTOF);
                RecDetectorHit hit = new RecDetectorHit();
                hit.detector   = 3;
                hit.sector     = bankCTOF.getInt("sector",loop);
                hit.superlayer = 0;//bankCTOF.getInt("paddle", loop);
                hit.layer      = 0;
                hit.hitPosition.set(bankCTOF.getFloat("x", loop), 
                        bankCTOF.getFloat("y", loop), 
                        bankCTOF.getFloat("z", loop));
                hit.time   = bankCTOF.getFloat("time", loop);
                hit.energy = bankCTOF.getFloat("energy", loop);
                recDetectorHits.addHit(hit);
            }
        }
    }
        
    /**
     * Matching the particles to the FTOF detector
     * @param det 
     */
    public void initMatchingFTOF(Detector det){
        int icounter = 0;
        for(RecParticle part : particles){
            Path3D path = new Path3D();
            path.addPoint(
                    part.trackCross.x(),
                    part.trackCross.y(),
                    part.trackCross.z()
            );
            
            path.addPoint(
                    part.trackCross.x() + 1500.0*part.trackCrossDir.x(),
                    part.trackCross.y() + 1500.0*part.trackCrossDir.y(),
                    part.trackCross.z() + 1500.0*part.trackCrossDir.z()                    
            );
            
            List<DetectorHit>  dhits = det.getLayerHits(path);
            for(DetectorHit hit : dhits){
                RecDetectorHit recHit = new RecDetectorHit();
                recHit.pindex = icounter;
                recHit.detector = 1;
                recHit.hitPosition.set(
                        hit.getPosition().x(),
                        hit.getPosition().y(),
                        hit.getPosition().z()
                        );
                recHit.trackPath = part.trackCrossPath + 
                        part.trackCross.distance(hit.getPosition());
                recHit.sector = hit.getSectorId();
                recHit.superlayer = hit.getSuperlayerId();
                recHit.layer      = hit.getLayerId();
                part.getHitStore().addHit(recHit);
            }
            
            icounter++;
        }
    }
    
    public void initMatchingEC(Detector det){
        int icounter = 0;
        for(RecParticle part : particles){
            Path3D path = new Path3D();
            path.addPoint(
                    part.trackCross.x(),
                    part.trackCross.y(),
                    part.trackCross.z()
            );
            
            path.addPoint(
                    part.trackCross.x() + 1500.0*part.trackCrossDir.x(),
                    part.trackCross.y() + 1500.0*part.trackCrossDir.y(),
                    part.trackCross.z() + 1500.0*part.trackCrossDir.z()                    
            );
            
            List<DetectorHit>  dhits = det.getLayerHits(path);
            for(DetectorHit hit : dhits){
                RecDetectorHit recHit = new RecDetectorHit();
                recHit.pindex = icounter;
                recHit.detector = 2;
                recHit.sector   = hit.getSectorId();
                recHit.superlayer = hit.getSuperlayerId();
                recHit.layer      = 0;
                recHit.hitPosition.set(
                        hit.getPosition().x(),
                        hit.getPosition().y(),
                        hit.getPosition().z()
                        );
                recHit.trackPath = part.trackCrossPath + 
                        part.trackCross.distance(hit.getPosition());
                recHit.sector = hit.getSectorId();
                recHit.superlayer = hit.getSuperlayerId();
                recHit.layer      = hit.getLayerId();
                if(hit.getLayerId()==0)
                    part.getHitStore().addHit(recHit);
            }
            
            icounter++;
        }
    }
    
    public void doParticleID(){
        for(RecParticle part : particles){
            part.doMass();
            ParticleID.determinePID(part);
        }
        Collections.sort(particles);
    }
    
    public void doMatchingEC(EvioDataEvent event){
        //System.out.println("\t\t ***** >>>>>> DO MATCHING EC  " + particles.size());
        for(RecParticle part : particles){            
            
            RecDetectorHit  hitPCAL  = part.getHitStore().getHit(2, 0, 0);
            RecDetectorHit  hitECIN  = part.getHitStore().getHit(2, 1, 0);
            RecDetectorHit  hitECOUT = part.getHitStore().getHit(2, 2, 0);
            
            int index_pcal  = -1; 
            int index_ecin  = -1;
            int index_ecout = -1;
            
            if(hitPCAL!=null){
                index_pcal  = this.recDetectorHits.matchDetectorHit(hitPCAL);
                if(index_pcal>=0) this.recDetectorHits.removeHit(index_pcal);
            }
            
            if(hitECIN!=null){
                index_ecin  = this.recDetectorHits.matchDetectorHit(hitECIN);
                if(index_ecin>=0) this.recDetectorHits.removeHit(index_ecin);
            }
            
            if(hitECOUT!=null){                
                index_ecout = this.recDetectorHits.matchDetectorHit(hitECOUT);
                if(index_ecout>=0) this.recDetectorHits.removeHit(index_ecout);
            }
            
            //System.out.println("   EC MATCHING RESULTS = " + index_pcal
            //+ "  " + index_ecin + "   " + index_ecout);
            //System.out.println("\t\t ***** >>>>>> Some thing Distance = " + distance
            //+ " index = " + index);
            
        }
    }
    /*
    public void doMatchingEC(EvioDataEvent event){
        EvioDataBank bank = (EvioDataBank) event.getBank("ECRec::clusters");
        int counter = 0;
        //System.out.println("============>>>>> SHOW Reconstructed hits");
        //recDetectorHits.show();
        
        int nrows = bank.rows();
        for(RecParticle part : particles){            
            
            for(int loop = 0; loop < nrows; loop++){
                Point3D hitPoint = new Point3D(
                        bank.getDouble("X", loop),
                        bank.getDouble("Y", loop),
                        bank.getDouble("Z", loop)
                );
                int sector = bank.getInt("sector", loop);
                int superlayer = bank.getInt("superlayer", loop);
                int layer = bank.getInt("layer", loop);
                RecDetectorHit detHit = part.getHitStore().getHit(2, superlayer, layer);
                //System.out.println("---- GET HIT --- " + sector + "  " + superlayer);
                if(detHit!=null){
                    //System.out.println("---- GET HIT --->>> " + detHit.toString() );
                    if(hitPoint.distance(detHit.hitPosition)<
                            detHit.hitPosition.distance(detHit.matchPosition)){
                        detHit.matchPosition = hitPoint;
                        detHit.time = bank.getDouble("time", loop);
                        detHit.trackPath = part.trackCrossPath + part.trackCross.distance(hitPoint);
                        //part.beta = detHit.trackPath/detHit.time/30.0;
                        detHit.index = loop;
                        detHit.energy = bank.getDouble("energy", loop);
                    }
                }
            }
            counter++;
        }                
    }
    */
    public void doNeutralMatching(){
        int ecIndex = -1;
        ArrayList<RecParticle> neutral = new ArrayList<RecParticle>();
        
        ArrayList<RecDetectorHit> dHit = recDetectorHits.getDetectorHits(2, 0, 0);
        
        for(int loop = 0; loop < dHit.size(); loop++){
            RecParticle particle = new RecParticle();
            Vector3D         vec = dHit.get(loop).hitPosition.toVector3D();
            Vector3D unitVec = new Vector3D();
                unitVec.copy(vec);
                unitVec.unit();
                particle.beta   = 1.0;
                particle.charge = 0; 
                particle.vector.setXYZ(unitVec.x(), unitVec.y(), unitVec.z());
                particle.vertex.setXYZ(0.0, 0.0, 0.0);
                particle.pid = 22;
                particle.mass = 0.0;
                particle.trackID = -1;
                particle.status  = 100;
                this.particles.add(particle);
        }
        
        for(RecParticle part : neutral){
            this.particles.add(part);
        }
    }
    
    public void doMatchingCTOF(){
        //this.recDetectorHits.show();
        for(RecParticle particle : this.particles){
            if(particle.status==100){
                
                int index = this.recDetectorHits.matchDetectorHit(
                        particle.trackCross, particle.trackCrossDir, 10.0, 3,0,0);
                if(index>=0){
                    RecDetectorHit  hit = this.recDetectorHits.get(index);
                    hit.matchPosition.copy(hit.hitPosition);
                    particle.getHitStore().addHit(hit);                    
                    this.recDetectorHits.removeHit(index);
                    particle.doMass();
                }
                //System.out.println(particle.toString());
                //System.out.println("  MATCHING CENTRAL PARTICLE  INDEX = " + index);
            }
        }
    }
    
    public void doMatchingFTOF(EvioDataEvent event){
        if(event.hasBank("FTOFRec::ftofhits")==false) return;
        EvioDataBank bank = (EvioDataBank) event.getBank("FTOFRec::ftofhits");
        int counter = 0;
        int nrows = bank.rows();
        for(RecParticle part : particles){
            RecDetectorHit detHit = part.getHitStore().getHit(1, 1, 0);
            if(detHit!=null){
                for(int loop = 0; loop < nrows; loop++){
                    Point3D hitPoint = new Point3D(
                            bank.getFloat("x", loop),
                            bank.getFloat("y", loop),
                            bank.getFloat("z", loop)
                    );
                    
                    //System.err.println("---> TOF HITS = PART " + counter + " " + 
                    //        hitPoint.distance(detHit.hitPosition) + "  "
                    //+ detHit.hitPosition.distance(detHit.matchPosition)+ " hit point = "
                    //+ hitPoint.toString());
                    if(hitPoint.distance(detHit.hitPosition)<
                            detHit.hitPosition.distance(detHit.matchPosition)){
                        detHit.matchPosition = hitPoint;
                        detHit.time = bank.getFloat("time", loop);
                        detHit.trackPath = part.trackCrossPath + part.trackCross.distance(hitPoint);
                        part.beta = detHit.trackPath/detHit.time/30.0;
                        detHit.index = loop;
                        detHit.energy = bank.getFloat("energy", loop);
                    }
                }                   
            } else {
                //System.err.println("-----------------> no assiciated hit in TOF for particle " + counter);
            }
            counter++;
        }
    }
    
    public void writeOutput(EvioDataEvent event){
        int nparticles = particles.size();
        if(nparticles<1) return;
        int ndets      = 0;
        EvioDataBank bankPart = (EvioDataBank) event.getDictionary().createBank("EVENTHB::particle", nparticles);
        int loop = 0;
        for(RecParticle part : particles){
            bankPart.setInt  ( "status" , loop, (int)  part.status);
            bankPart.setInt  ( "charge" , loop, (int)  part.charge);
            bankPart.setInt  ( "pid"    , loop, (int)   part.pid);
            bankPart.setFloat( "px"     , loop, (float) part.vector.x());
            bankPart.setFloat( "py"     , loop, (float) part.vector.y());
            bankPart.setFloat( "pz"     , loop, (float) part.vector.z());
            bankPart.setFloat( "vx"     , loop, (float) part.vertex.x());
            bankPart.setFloat( "vy"     , loop, (float) part.vertex.y());
            bankPart.setFloat( "vz"     , loop, (float) part.vertex.z());
            bankPart.setFloat( "beta"   , loop, (float) part.beta);
            bankPart.setFloat( "mass"   , loop, (float) part.mass);
            ndets += part.getHitStore().size();
            loop++;
        }
        
        EvioDataBank bankDet = (EvioDataBank) event.getDictionary().createBank("EVENTHB::detector", ndets);
        loop = 0;
        int  pindex = 0;
        for(RecParticle part : particles){
            int npsize = part.getHitStore().size();
            for(int i = 0; i < npsize; i++){
                RecDetectorHit hit = part.getHitStore().get(i);
                bankDet.setInt  ("pindex",   loop,(int) pindex);
                bankDet.setInt  ("index",    loop, (int) hit.index);
                bankDet.setInt  ("detector", loop, (int) hit.detector);
                bankDet.setInt  ("sector",    loop, (int) hit.sector);
                bankDet.setInt  ("superlayer", loop, (int) hit.superlayer);
                bankDet.setFloat("X",        loop, (float) hit.matchPosition.x());
                bankDet.setFloat("Y",        loop, (float) hit.matchPosition.y());
                bankDet.setFloat("Z",        loop, (float) hit.matchPosition.z());
                bankDet.setFloat("hX",       loop, (float) hit.hitPosition.x());
                bankDet.setFloat("hY",       loop, (float) hit.hitPosition.y());
                bankDet.setFloat("hZ",       loop, (float) hit.hitPosition.z());
                bankDet.setFloat("path",     loop, (float) hit.trackPath);
                bankDet.setFloat("time",     loop, (float) hit.time);
                bankDet.setFloat("energy",   loop, (float) hit.energy);
                //bankDet.setFloat();
                loop++;
            }
            pindex++;
        }
        if(ndets<1){
            event.appendBanks(bankPart);
        } else {
            event.appendBanks(bankPart,bankDet);
        }
    }
    
    public void show(){
        
        if(particles.size()>0){
            //if(particles.get(0).pid==11){
                System.err.println("=============> SHOWING EVENT");
                for(RecParticle part : particles){
                    System.err.println(part.toString());
                }
            //}
        }
        
    }
    
    
    public void initForwardParticles(EvioDataEvent event){
        if(event.hasBank("FTRec::tracks")==true){
            EvioDataBank bank = (EvioDataBank) event.getBank("FTRec::tracks");
            int nrows = bank.rows();
            for(int loop = 0; loop < nrows; loop++){
                double cx = bank.getDouble("Cx", loop);
                double cy = bank.getDouble("Cy", loop);
                double cz = bank.getDouble("Cz", loop);
                double energy =  bank.getDouble("Energy", loop);
                int    charge = bank.getInt("Charge", loop);
                RecParticle particle = new RecParticle();
                particle.mass = 0.0;
                particle.pid  = 11;
                if(charge==0){
                    particle.pid = 22;
                }
                //System.out.println(" ----------> energy = " + energy );
                particle.status = 200;
                particle.chi2pid = 1.0;
                particle.vertex.setXYZ(0.0, 0.0, 0.0);
                particle.vector.setXYZ(energy*cx, energy*cy, energy*cz);
                this.particles.add(particle);
            }
        }
    }
}
