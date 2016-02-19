/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.mon.eb;

import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.pdg.PDGParticle;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas12.physics.GenericKinematicFitter;
import org.jlab.clasrec.main.DetectorMonitoring;
import org.jlab.clasrec.rec.CLASMonitoring;
import org.jlab.clasrec.utils.ServiceConfiguration;
import org.jlab.evio.clas12.EvioDataBank;
import org.jlab.evio.clas12.EvioDataEvent;
import org.root.group.PlotGroup;
import org.root.histogram.H1D;
import org.root.histogram.H2D;
import org.root.pad.DirectoryViewer;


/**
 *
 * @author gavalian
 */
public class CLASMonitoringSEB extends DetectorMonitoring {
    private GenericKinematicFitter fitter = new GenericKinematicFitter(11.0,"11");
    public CLASMonitoringSEB(){
        super("SEBMON","1.0","gavalian");
    }
    
    @Override
    public void processEvent(EvioDataEvent event) {
        //PDGDatabase.show();
        try {
            this.particleHistograms(event);
        } catch (Exception e) {
            System.err.println(" Something went wrong with filling the particle hists.");
        }
        if(event.hasBank("GenPart::true")){
            PhysicsEvent pgens = this.getPhysicsEvent((EvioDataBank) event.getBank("GenPart::true"));
            for(int gloop = 0; gloop<pgens.count();gloop++){
                int pid = pgens.getParticle(gloop).pid();
                PDGParticle particle = PDGDatabase.getParticleById(pid);
                if(particle!=null) {
                    this.fill("PIDEventBuilder", "GenPID", (double) particle.gid());
                    //System.err.println(particle);
                }                
            }
        }
        
        if(event.hasBank("GenPart::true") && event.hasBank("EVENTHB::particle")){
            PhysicsEvent pgens = this.getPhysicsEvent((EvioDataBank) event.getBank("GenPart::true"));
            PhysicsEvent precs = this.getPhysicsEvent((EvioDataBank) event.getBank("EVENTHB::particle"));
            
            //System.out.println(pgens.toLundString());
            //System.out.println(precs.toLundString());                        
            
            for(int ploop = 0; ploop<precs.count();ploop++){
                int pid = pgens.getParticle(ploop).pid();
                PDGParticle particle = PDGDatabase.getParticleById(pid);
                if(particle!=null) {
                    this.fill("PIDEventBuilder", "RecPID", (double) particle.gid());
                }
            }
            //System.out.println(pgens.toLundString());
            //System.out.println(precs.toLundString());
            
            EvioDataBank evntBank = (EvioDataBank) event.getBank("EVENTHB::particle");
            for(int nev = 0; nev < evntBank.rows();nev++){
                double px = evntBank.getFloat("px", nev);
                double py = evntBank.getFloat("py", nev);
                double pz = evntBank.getFloat("pz", nev);
                double mom = Math.sqrt(px*px+py*py+pz*pz);
                this.fill("ParticleID", "Mass", evntBank.getFloat("mass", nev));
                this.fill("ParticleID", "Beta", evntBank.getFloat("beta", nev));
                this.fill("ParticleID", "BetaVsMom", mom,
                        evntBank.getFloat("beta", nev));
                this.fill("ParticleID", "MassVsMom", mom,
                        evntBank.getFloat("mass", nev));
            }
            if(pgens.countByPid(2212)==1&&precs.countByPid(2212)==1){
                Particle proton_gen = pgens.getParticleByPid(2212, 0);
                Particle proton_rec = precs.getParticleByPid(2212, 0);
                double res   = (proton_gen.p() - proton_rec.p())/proton_gen.p();
                double thres = Math.toDegrees(proton_gen.theta()-proton_rec.theta())/(proton_gen.theta());
                this.fill("OutBending", "H1D_P_RESOLUTION", res );
                this.fill("OutBending", "H1D_TH_RESOLUTION", thres );
                this.fill("OutBending", "H2D_P_RESOLUTION_P", proton_gen.p(),res);
                this.fill("OutBending", "H2D_P_RESOLUTION_TH", Math.toDegrees(proton_gen.theta()),res);                
            }
            
            if(pgens.countByPid(11)==1&&precs.countByPid(11)==1){
                Particle proton_gen = pgens.getParticleByPid(11, 0);
                Particle proton_rec = precs.getParticleByPid(11, 0);
                double res   = (proton_gen.p() - proton_rec.p())/proton_gen.p();
                double thres = Math.toDegrees(proton_gen.theta()-proton_rec.theta())/(proton_gen.theta());
                this.fill("InBending", "H1D_P_RESOLUTION", res );
                this.fill("InBending", "H1D_TH_RESOLUTION", thres );
                this.fill("InBending", "H2D_P_RESOLUTION_P", proton_gen.p(),res);
                this.fill("InBending", "H2D_P_RESOLUTION_TH", Math.toDegrees(proton_gen.theta()),res);                
            }
            
        }
        
        if(event.hasBank("EVENTHB::detector")){
            EvioDataBank  detBank = (EvioDataBank) event.getBank("EVENTHB::detector");
            //detBank.show();
            int rows = detBank.rows();
            for(int loop = 0; loop < rows; loop++){
                if(detBank.getInt("detector",loop)==1&&detBank.getFloat("energy",loop)>0.00001){
                    this.fill("SEB_FTOF_Matching", "FTOFMatch_X", 
                            detBank.getFloat("X", loop)-detBank.getFloat("hX", loop));
                    this.fill("SEB_FTOF_Matching", "FTOFMatch_Y", 
                            detBank.getFloat("Y", loop)-detBank.getFloat("hY", loop));
                    this.fill("SEB_FTOF_Matching", "FTOFMatch_Z", 
                            detBank.getFloat("Z", loop)-detBank.getFloat("hZ", loop));
                    this.fill("SEB_FTOF_Matching", "FTOFMatch_HITS_XY",detBank.getFloat("X",loop),
                            detBank.getFloat("Y",loop));
                }
                /*
                * PCAL Matching plots
                */
                if(detBank.getInt("detector",loop)==2&&detBank.getInt("superlayer",loop)==0&&
                        detBank.getFloat("energy",loop)>0.00001){
                    this.fill("SEB_PCAL_Matching", "PCALMatch_X", 
                            detBank.getFloat("X", loop)-detBank.getFloat("hX", loop));
                    this.fill("SEB_PCAL_Matching", "PCALMatch_Y", 
                            detBank.getFloat("Y", loop)-detBank.getFloat("hY", loop));
                    this.fill("SEB_PCAL_Matching", "PCALMatch_Z", 
                            detBank.getFloat("Z", loop)-detBank.getFloat("hZ", loop));
                    this.fill("SEB_PCAL_Matching", "PCALMatch_HITS_XY",detBank.getFloat("X",loop),
                            detBank.getFloat("Y",loop));
                }
                /*
                * EC Inner Matching plots
                */
                if(detBank.getInt("detector",loop)==2&&detBank.getInt("superlayer",loop)==1&&
                        detBank.getFloat("energy",loop)>0.00001){
                    this.fill("SEB_ECIN_Matching", "ECINMatch_X", 
                            detBank.getFloat("X", loop)-detBank.getFloat("hX", loop));
                    this.fill("SEB_ECIN_Matching", "ECINMatch_Y", 
                            detBank.getFloat("Y", loop)-detBank.getFloat("hY", loop));
                    this.fill("SEB_ECIN_Matching", "ECINMatch_Z", 
                            detBank.getFloat("Z", loop)-detBank.getFloat("hZ", loop));
                    this.fill("SEB_ECIN_Matching", "ECINMatch_HITS_XY",detBank.getFloat("X",loop),
                            detBank.getFloat("Y",loop));
                }
                
            }
        }
        
        if(event.hasBank("EVENTHB::detector")&&event.hasBank("EVENTHB::particle")){
            EvioDataBank  evntBank = (EvioDataBank) event.getBank("EVENTHB::particle");
            EvioDataBank  detBank  = (EvioDataBank) event.getBank("EVENTHB::detector");
            
            int prows = evntBank.rows();
            for(int part = 0; part < prows; part++){
                double px          = evntBank.getFloat("px", part);
                double py          = evntBank.getFloat("py", part);
                double pz          = evntBank.getFloat("pz", part);
                double mom         = Math.sqrt(px*px+py*py+pz*pz);
                double beta        = evntBank.getFloat("beta",part);
                double hypKaon     = mom/Math.sqrt(0.4937*0.4937+mom*mom);
                double hypProt     = mom/Math.sqrt(0.9383*0.9383+mom*mom);
                double hypPion     = mom/Math.sqrt(0.1396*0.1396+mom*mom);
                double hypElec     = mom/Math.sqrt(0.0005*0.0005+mom*mom);
                this.fill("PIDTimeOfFlight", "HypKaon", beta/hypKaon);
                this.fill("PIDTimeOfFlight", "HypProton", beta/hypProt);
                this.fill("PIDTimeOfFlight", "HypPion", beta/hypPion);
                this.fill("PIDTimeOfFlight", "HypElectron", beta/hypElec);
            }
            
            int nrows = detBank.rows();
            for(int loop = 0; loop < nrows; loop++){
                int detector   = detBank.getInt("detector", loop);
                int superlayer = detBank.getInt("superlayer", loop);
                if(detector==2){
                    double energy = detBank.getFloat("energy", loop);
                    int    pindex      = detBank.getInt("pindex", loop);
                    double px          = evntBank.getFloat("px", pindex);
                    double py          = evntBank.getFloat("py", pindex);
                    double pz          = evntBank.getFloat("pz", pindex);
                    double mom         = Math.sqrt(px*px+py*py+pz*pz);
                    if(superlayer==0){
                        this.fill("PIDCalorimeter", "PCALEnergyVsMom", energy/mom);
                        //System.out.println(" filling ---- " + energy + "  " + mom + " " + pindex + " " + px);
                    }
                    if(superlayer==1){
                        this.fill("PIDCalorimeter", "ECINEnergyVsMom", energy/mom);
                    }
                    if(superlayer==2){
                        this.fill("PIDCalorimeter", "ECOUTEnergyVsMom", energy/mom);
                    }
                }
            }
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    private void particleHistograms(EvioDataEvent event){
        PhysicsEvent genEvent = this.fitter.getGeneratedEvent(event);
        PhysicsEvent recEvent = this.fitter.getPhysicsEvent(event);
        for(int loop = 0; loop < genEvent.count(); loop++){
            
            Integer genPid = genEvent.getParticle(loop).pid();
            Particle genParticle = genEvent.getParticle(loop);
            String namemom = "H1D_GEN_MOM" + genPid.toString();
            String namethe = "H1D_GEN_THETA" + genPid.toString();
            String namephi = "H1D_GEN_PHI" + genPid.toString();
            this.fill("GeneratedMomenta", namemom, genParticle.p());
            this.fill("GeneratedTheta", namethe, genParticle.theta());
            this.fill("GeneratedPhi", namephi, genParticle.phi());
            
            namemom = "H1D_REC_MOM" + genPid.toString();
            namethe = "H1D_REC_THETA" + genPid.toString();
            namephi = "H1D_REC_PHI" + genPid.toString();
            
            Particle recParticle = recEvent.closestParticle(genParticle);
            double resolution = (recParticle.p()-genParticle.p())/(genParticle.p());
            if(recParticle.cosTheta(genParticle)>0.99&&resolution<0.015){
                this.fill("ReconstructedMomenta", namemom, genParticle.p());
                this.fill("ReconstructedTheta", namethe, genParticle.theta());
                this.fill("ReconstructedPhi", namephi, genParticle.phi());
            }
        }
    }
    
    @Override 
    public void init() {
        
        PlotGroup inbending = new PlotGroup("InBending",2,2);
        inbending.add("H1D_P_RESOLUTION", new H1D("H1D_P_RESOLUTION",300,-0.2,0.2));
        inbending.add("H1D_TH_RESOLUTION", new H1D("H1D_TH_RESOLUTION",300,-0.5,0.5));
        inbending.add("H2D_P_RESOLUTION_P", new H2D("H2D_P_RESOLUTION_P",160,0.0,6.0,160,-0.2,0.2));
        inbending.add("H2D_P_RESOLUTION_TH", new H2D("H2D_P_RESOLUTION_TH",160,0.0,45.0,160,-0.2,0.2));
        
        inbending.addDescriptor(0, "H1D_P_RESOLUTION");
        inbending.addDescriptor(1, "H1D_TH_RESOLUTION");
        inbending.addDescriptor(2, "H2D_P_RESOLUTION_P");
        inbending.addDescriptor(3, "H2D_P_RESOLUTION_TH");
        
        PlotGroup outbending = new PlotGroup("OutBending",2,2);
        outbending.add("H1D_P_RESOLUTION", new H1D("H1D_P_RESOLUTION",300,-0.2,0.2));
        outbending.add("H1D_TH_RESOLUTION", new H1D("H1D_TH_RESOLUTION",300,-0.5,0.5));
        outbending.add("H2D_P_RESOLUTION_P", new H2D("H2D_P_RESOLUTION_P",160,0.0,6.0,160,-0.2,0.2));
        outbending.add("H2D_P_RESOLUTION_TH", new H2D("H2D_P_RESOLUTION_TH",160,0.0,45.0,160,-0.2,0.2));
        
        outbending.addDescriptor(0, "H1D_P_RESOLUTION");
        outbending.addDescriptor(1, "H1D_TH_RESOLUTION");
        outbending.addDescriptor(2, "H2D_P_RESOLUTION_P");
        outbending.addDescriptor(3, "H2D_P_RESOLUTION_TH");
        
        this.addGroup(inbending);
        this.addGroup(outbending);
        
        PlotGroup  ftofMatching = new PlotGroup("SEB_FTOF_Matching",2,2);
        ftofMatching.add("FTOFMatch_X", new H1D("FTOFMatch_X",300,-15.0,15.0));
        ftofMatching.add("FTOFMatch_Y", new H1D("FTOFMatch_Y",300,-15.0,15.0));
        ftofMatching.add("FTOFMatch_Z", new H1D("FTOFMatch_Z",300,-15.0,15.0));
        ftofMatching.add("FTOFMatch_HITS_XY", new H2D("FTOFMatch_HITS_XY",200,-375.0,375.0, 
                200, -375.0, 375.0));
        
        ftofMatching.addDescriptor(0, "FTOFMatch_X");
        ftofMatching.addDescriptor(1, "FTOFMatch_Y");
        ftofMatching.addDescriptor(2, "FTOFMatch_Z");
        ftofMatching.addDescriptor(3, "FTOFMatch_HITS_XY");
        
        this.addGroup(ftofMatching);
        
        PlotGroup  pcalMatching = new PlotGroup("SEB_PCAL_Matching",2,2);
        pcalMatching.add("PCALMatch_X", new H1D("PCALMatch_X",300,-35.0,35.0));
        pcalMatching.add("PCALMatch_Y", new H1D("PCALMatch_Y",300,-35.0,35.0));
        pcalMatching.add("PCALMatch_Z", new H1D("PCALMatch_Z",300,-35.0,35.0));
        pcalMatching.add("PCALMatch_HITS_XY", new H2D("PCALMatch_HITS_XY",200,-375.0,375.0, 
                200, -375.0, 375.0));
        
        pcalMatching.addDescriptor(0, "PCALMatch_X");
        pcalMatching.addDescriptor(1, "PCALMatch_Y");
        pcalMatching.addDescriptor(2, "PCALMatch_Z");
        pcalMatching.addDescriptor(3, "PCALMatch_HITS_XY");
        
        this.addGroup(pcalMatching);
        
        PlotGroup  ecinMatching = new PlotGroup("SEB_ECIN_Matching",2,2);
        ecinMatching.add("ECINMatch_X", new H1D("ECINMatch_X",100,-35.0,35.0));
        ecinMatching.add("ECINMatch_Y", new H1D("ECINMatch_Y",100,-35.0,35.0));
        ecinMatching.add("ECINMatch_Z", new H1D("ECINMatch_Z",100,-35.0,35.0));
        ecinMatching.add("ECINMatch_HITS_XY", new H2D("ECINMatch_HITS_XY",200,-375.0,375.0, 
                200, -375.0, 375.0));
        
        ecinMatching.addDescriptor(0, "ECINMatch_X");
        ecinMatching.addDescriptor(1, "ECINMatch_Y");
        ecinMatching.addDescriptor(2, "ECINMatch_Z");
        ecinMatching.addDescriptor(3, "ECINMatch_HITS_XY");
        
        this.addGroup(ecinMatching);
        
        PlotGroup  particleID = new PlotGroup("ParticleID",2,2);
        particleID.add("Mass", new H1D("Mass",200,-0.2,1.2));
        particleID.add("Beta", new H1D("Beta",200,0.4,1.2));
        particleID.add("MassVsMom", new H2D("MassVsMom",200,0.0,6.0,200,-0.2,1.2));
        particleID.add("BetaVsMom", new H2D("BetaVsMom",200,0.0,6.0,200,0.4,1.2));
        particleID.addDescriptor(0, "Mass");
        particleID.addDescriptor(1, "MassVsMom");
        particleID.addDescriptor(2, "Beta");
        particleID.addDescriptor(3, "BetaVsMom");
        
        this.addGroup(particleID);
        
        PlotGroup  pidCalo = new PlotGroup("PIDCalorimeter",2,3);
        
        pidCalo.add("PCALEnergyVsMom", new H1D("PCALEnergyVsMom",200,0.0,200.0));
        pidCalo.add("ECINEnergyVsMom", new H1D("ECINEnergyVsMom",200,0.0,200.0));
        pidCalo.add("ECOUTEnergyVsMom", new H1D("ECOUTEnergyVsMom",200,0.0,200.0));
        pidCalo.add("TOTALEnergyVsMom", new H1D("TOTALEnergyVsMom",200,0.0,200.0));
        pidCalo.add("ECIN_vs_ECOUT", new H2D("ECIN_vs_ECOUT",200,0.0,200.0,200,0.0,200.0));
        pidCalo.add("ECIN_vs_PCAL", new H2D("ECIN_vs_PCAL",200,0.0,200.0,200,0.0,200.0));
        
        pidCalo.addDescriptor(0, "PCALEnergyVsMom");
        pidCalo.addDescriptor(1, "ECINEnergyVsMom");
        pidCalo.addDescriptor(2, "ECOUTEnergyVsMom");
        //pidCalo.addDescriptor(3, "TOTALEnergyVsMom");
        //pidCalo.addDescriptor(4, "ECIN_vs_ECOUT");
        //pidCalo.addDescriptor(5, "ECIN_vs_PCAL");
        
        this.addGroup(pidCalo);
        
        PlotGroup  pidFTOF = new PlotGroup("PIDTimeOfFlight",2,2);
        pidFTOF.add("HypKaon", new H1D("HypKaon",200,0.9,1.05));
        pidFTOF.add("HypProton", new H1D("HypProton",200,0.9,1.05));
        pidFTOF.add("HypPion", new H1D("HypPion",200,0.9,1.05));
        pidFTOF.add("HypElectron", new H1D("HypElectron",200,0.9,1.05));
        
        pidFTOF.addDescriptor(0, "HypKaon");
        pidFTOF.addDescriptor(1, "HypProton");
        pidFTOF.addDescriptor(2, "HypPion");
        pidFTOF.addDescriptor(3, "HypElectron");
        
        this.addGroup(pidFTOF);
        
        PlotGroup  pidSEB = new PlotGroup("PIDEventBuilder",1,2);
        pidSEB.add("GenPID", new H1D("GenPID",16,-0.5,15.5));
        pidSEB.add("RecPID", new H1D("RecPID",16,-0.5,15.5));
        
        pidSEB.addDescriptor(0, "GenPID");
        //pidSEB.addDescriptor(0, "RecPID");
        pidSEB.addDescriptor(1, "RecPID");
        
        this.addGroup(pidSEB);
        int[] pid = new int[]{11,211,-211,2212,321,-321};
        PlotGroup genParticlesMom = new PlotGroup("GeneratedMomenta",2,3);        
        for(int loop = 0; loop < 6; loop++){
            Integer ID = pid[loop];
            String name = "H1D_GEN_MOM" + ID.toString();
            genParticlesMom.add(name, new H1D(name,100,0.0,8.0));
            genParticlesMom.addDescriptor(loop, name);
        }
        
        PlotGroup genParticlesTheta = new PlotGroup("GeneratedTheta",2,3);
        for(int loop = 0; loop < 6; loop++){
            Integer ID = pid[loop];
            String name = "H1D_GEN_THETA" + ID.toString();
            genParticlesTheta.add(name, new H1D(name,100,0.0,3.14*0.5));
            genParticlesTheta.addDescriptor(loop, name);
        }
        
        PlotGroup genParticlesPhi   = new PlotGroup("GeneratedPhi",2,3);
        for(int loop = 0; loop < 6; loop++){
            Integer ID = pid[loop];
            String name = "H1D_GEN_PHI" + ID.toString();
            genParticlesPhi.add(name, new H1D(name,100,-3.16,3.16));
            genParticlesPhi.addDescriptor(loop, name);
        }
        
        this.addGroup(genParticlesMom);
        this.addGroup(genParticlesTheta);
        this.addGroup(genParticlesPhi);
        PlotGroup recParticlesMom = new PlotGroup("ReconstructedMomenta",2,3);        
        for(int loop = 0; loop < 6; loop++){
            Integer ID = pid[loop];
            String name = "H1D_REC_MOM" + ID.toString();
            recParticlesMom.add(name, new H1D(name,100,0.0,8.0));
            recParticlesMom.addDescriptor(loop, name);
        }
        
        PlotGroup recParticlesTheta = new PlotGroup("ReconstructedTheta",2,3);
        for(int loop = 0; loop < 6; loop++){
            Integer ID = pid[loop];
            String name = "H1D_REC_THETA" + ID.toString();
            recParticlesTheta.add(name, new H1D(name,100,0.0,3.14*0.5));
            recParticlesTheta.addDescriptor(loop, name);
        }
        
        PlotGroup recParticlesPhi   = new PlotGroup("ReconstructedPhi",2,3);
        for(int loop = 0; loop < 6; loop++){
            Integer ID = pid[loop];
            String name = "H1D_REC_PHI" + ID.toString();
            recParticlesPhi.add(name, new H1D(name,100,-3.16,3.16));
            recParticlesPhi.addDescriptor(loop, name);
        }
        
        this.addGroup(recParticlesMom);
        this.addGroup(recParticlesTheta);
        this.addGroup(recParticlesPhi);
        
        this.getDirectory().list();
    }
    
    @Override
    public void configure(ServiceConfiguration c) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void analyze() {
        int[] pid = new int[]{11,211,-211,2212,321,-321};
        PlotGroup effParticlesMom = new PlotGroup("EfficiencyMomenta",2,3);        
        for(int loop = 0; loop < 6; loop++){
            Integer ID = pid[loop];
            String nameGen = "H1D_GEN_MOM" + ID.toString();
            String nameRec = "H1D_REC_MOM" + ID.toString();
            String nameEff = "H1D_EFF_MOM" + ID.toString();
            H1D gen = (H1D) this.getDirectory().getGroup("GeneratedMomenta").getObjects().get(nameGen);
            H1D rec = (H1D) this.getDirectory().getGroup("ReconstructedMomenta").getObjects().get(nameRec);
            H1D eff = rec.histClone(nameEff);
            eff.divide(gen);
            effParticlesMom.add(nameEff,eff);
            effParticlesMom.addDescriptor(loop, nameEff);
        }
        
        PlotGroup effParticlesTheta = new PlotGroup("EfficiencyTheta",2,3);        
        for(int loop = 0; loop < 6; loop++){
            Integer ID = pid[loop];
            String nameGen = "H1D_GEN_THETA" + ID.toString();
            String nameRec = "H1D_REC_THETA" + ID.toString();
            String nameEff = "H1D_EFF_THETA" + ID.toString();
            H1D gen = (H1D) this.getDirectory().getGroup("GeneratedTheta").getObjects().get(nameGen);
            H1D rec = (H1D) this.getDirectory().getGroup("ReconstructedTheta").getObjects().get(nameRec);
            H1D eff = rec.histClone(nameEff);
            eff.divide(gen);
            effParticlesTheta.add(nameEff,eff);
            effParticlesTheta.addDescriptor(loop, nameEff);
        }
        
        PlotGroup effParticlesPhi = new PlotGroup("EfficiencyPhi",2,3);        
        for(int loop = 0; loop < 6; loop++){
            Integer ID = pid[loop];
            String nameGen = "H1D_GEN_PHI" + ID.toString();
            String nameRec = "H1D_REC_PHI" + ID.toString();
            String nameEff = "H1D_EFF_PHI" + ID.toString();
            H1D gen = (H1D) this.getDirectory().getGroup("GeneratedPhi").getObjects().get(nameGen);
            H1D rec = (H1D) this.getDirectory().getGroup("ReconstructedPhi").getObjects().get(nameRec);
            H1D eff = rec.histClone(nameEff);
            eff.divide(gen);
            effParticlesPhi.add(nameEff,eff);
            effParticlesPhi.addDescriptor(loop, nameEff);
        }
        
        
        this.addGroup(effParticlesMom);
        this.addGroup(effParticlesTheta);
        this.addGroup(effParticlesPhi);
        //this.addGroup(recParticlesTheta);
        //this.addGroup(recParticlesPhi);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private PhysicsEvent getPhysicsEvent(EvioDataBank dbank){
        PhysicsEvent physEvent = new PhysicsEvent();

        double GeV=1;
        if(dbank.getDescriptor().getName().equals("GenPart::true")){
            GeV=0.001;
            int nrows=dbank.rows();
            for(int irow=0; irow<nrows; irow++){
            Particle part = new Particle(dbank.getInt("pid",irow),
                dbank.getDouble("px",irow)*GeV, dbank.getDouble("py",irow)*GeV, dbank.getDouble("pz",irow)*GeV,
                dbank.getDouble("vx",irow)*GeV, dbank.getDouble("vy",irow)*GeV, dbank.getDouble("vz",irow)*GeV);
            physEvent.addParticle(part);
            }
            return physEvent;
        }

        int nrows=dbank.rows();
        for(int irow=0; irow<nrows; irow++){
            Particle part = new Particle(dbank.getInt("pid",irow),
                dbank.getFloat("px",irow)*GeV, dbank.getFloat("py",irow)*GeV, dbank.getFloat("pz",irow)*GeV,
                dbank.getFloat("vx",irow)*GeV, dbank.getFloat("vy",irow)*GeV, dbank.getFloat("vz",irow)*GeV);
            physEvent.addParticle(part);
        }
        return physEvent;
    }

    public static void main(String[] args){
        String inputFile = args[0];
        System.err.println(" \n[PROCESSING FILE] : " + inputFile);
        CLASMonitoringSEB  dcrecMonitor = new CLASMonitoringSEB();
        dcrecMonitor.init();
        CLASMonitoring  monitor = new CLASMonitoring(inputFile, dcrecMonitor);
        monitor.process();
        dcrecMonitor.analyze();
        DirectoryViewer browser = new DirectoryViewer(dcrecMonitor.getDirectory());
    }
}
