/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.mon.eb;

import java.util.ArrayList;
import java.util.TreeMap;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas.physics.Vector3;
import org.jlab.clas12.physics.GenericKinematicFitter;
import org.jlab.evio.clas12.EvioDataEvent;
import org.root.group.TDirectory;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

/**
 *
 * @author gavalian
 */
public class ParticleMonitoring {
    
    private TDirectory  dir = new TDirectory("Particles");
    private ArrayList<TDirectory> directories = new ArrayList<TDirectory>();
    private Integer     particleID = 11;
    private GenericKinematicFitter  fitter = new GenericKinematicFitter(11.0,"11");
    private String                  generatedDirectory = null;
    private String                  reconstructedDirectory = null;
    private String                  efficiencyDirectory = null;
    private String                  resolutionsDirectory = null;
    private TreeMap<Integer,String>  particleNames = new TreeMap<Integer,String>();
    private TreeMap<Integer,Integer> particleColors = new TreeMap<Integer,Integer>();
    private Integer                  histLineWidth  = 2;
    private Double                   particleCosCut = 0.998;
    
    public ParticleMonitoring(){
        this.particleID = 11;
        this.initMap();
    }
    
    public ParticleMonitoring(int pid){
        this.particleID = pid;
        this.initMap();
    }
    
    private void initMap(){
        this.particleNames.put(   11, "e-");
        this.particleNames.put( 2212, "p");
        this.particleNames.put(  211, "pi+");
        this.particleNames.put( -211, "pi-");
        this.particleNames.put(  321, "K+");
        this.particleNames.put( -321, "K-");
        
        this.particleColors.put(   11, 2);
        this.particleColors.put( 2212, 3);
        this.particleColors.put(  211, 4);
        this.particleColors.put( -211, 5);
        this.particleColors.put(  321, 6);
        this.particleColors.put( -321, 7);
        
    }
    
    public void process(EvioDataEvent event){
        
        PhysicsEvent genEvent = this.fitter.getGeneratedEvent(event);
        PhysicsEvent recEvent = this.fitter.getPhysicsEvent(event);
        
        //System.out.println(genEvent.toLundString());
        int nparticles = genEvent.countByPid(this.particleID);
        for(int loop = 0; loop < nparticles; loop++){
            
            Particle refPart = genEvent.getParticleByPid(this.particleID, loop);
            H1D mom = ((H1D) this.directories.get(0).getObject("momentum"));
            mom.fill(refPart.p());
            //((H1D) this.directories.get(0).getObject("momentum")).fill(refPart.p());
            //System.out.println(" filling " + refPart.p());
            ((H1D) this.directories.get(0).getObject("theta")).fill(Math.toDegrees(refPart.theta()));
            ((H1D) this.directories.get(0).getObject("phi")).fill(Math.toDegrees(refPart.phi()));
            
            Particle recPart = recEvent.closestParticle(refPart);
            
            Vector3 vec = recPart.vector().vect().cross(refPart.vector().vect());
            
            double delta_the = Math.toDegrees(refPart.theta()-recPart.theta());
            double delta_phi = Math.toDegrees(refPart.phi()-recPart.phi());
            if(vec.z()<0) delta_the = -delta_the;
            //if(vec.z()<0) delta_phi = -delta_phi;
            
            H1D resThe = (H1D) this.directories.get(3).getObject("theta");
            H1D resPhi = (H1D) this.directories.get(3).getObject("phi");
            
            resThe.fill(delta_the);
            resPhi.fill(delta_phi);
            //System.out.println(recPart.cosTheta(refPart));
            if(recPart.cosTheta(refPart)>this.particleCosCut){                
                ((H1D) this.directories.get(1).getObject("momentum")).fill(recPart.p());
                ((H1D) this.directories.get(1).getObject("theta")).fill(Math.toDegrees(recPart.theta()));
                ((H1D) this.directories.get(1).getObject("phi")).fill(Math.toDegrees(recPart.phi()));
                
                ((H1D) this.directories.get(2).getObject("momentum")).fill(refPart.p());
                ((H1D) this.directories.get(2).getObject("theta")).fill(Math.toDegrees(refPart.theta()));
                ((H1D) this.directories.get(2).getObject("phi")).fill(Math.toDegrees(refPart.phi()));
                
                H1D resMom = (H1D) this.directories.get(3).getObject("momentum");
                H2D resMomVSth  = (H2D) this.directories.get(3).getObject("momentumVStheta");
                H2D resMomVSmom = (H2D) this.directories.get(3).getObject("momentumVSmomentum");
                H2D resMomVSphi = (H2D) this.directories.get(3).getObject("momentumVSphi");
                H2D resThVSmom = (H2D) this.directories.get(3).getObject("thetaVSmomentum");
                H2D resPhiVSmom = (H2D) this.directories.get(3).getObject("phiVSmomentum");
                
                if(refPart.p()!=0){
                    double res = (recPart.p()-refPart.p())/refPart.p();
                    resMom.fill(res);
                    resMomVSth.fill(Math.toDegrees(refPart.theta()),res);
                    resMomVSphi.fill(Math.toDegrees(refPart.phi()),res);
                    resMomVSmom.fill(refPart.p(),res);
                    resThVSmom.fill(refPart.p(),delta_the);
                    resPhiVSmom.fill(refPart.p(),delta_phi);
                }
            }
        }
    }
    
    public ArrayList<TDirectory>  getDirectiries(){
        return this.directories;
    }
    
    public void init(){
        
        String pidString = this.particleNames.get(this.particleID);
        this.generatedDirectory = "Particles/Generated/"
                + this.particleNames.get(this.particleID);
        this.reconstructedDirectory = "Particles/Reconstructed/" 
                + this.particleNames.get(this.particleID);
        this.efficiencyDirectory = "Particles/Efficiency/"
                + this.particleNames.get(this.particleID);
        this.resolutionsDirectory = "Particles/Resolution/"
                + this.particleNames.get(this.particleID);
        
        TDirectory dirGen = new TDirectory(this.generatedDirectory);
        TDirectory dirRec = new TDirectory(this.reconstructedDirectory);
        TDirectory dirEff = new TDirectory(this.efficiencyDirectory);
        TDirectory dirRes = new TDirectory(this.resolutionsDirectory);
        
        //dir.addDirectory(new TDirectory(this.generatedDirectory));
        //dir.addDirectory(new TDirectory(this.reconstructedDirectory));
        
        H1D H100 = new H1D("momentum","P (" + pidString +  ") [GeV]","",100,0.0,10.0);
        H1D H101 = new H1D("theta","Theta (" + pidString +  ") [Deg]","",100,0.0,45.0);
        H1D H102 = new H1D("phi","Phi (" + pidString +  ") [Deg]","",120,-185.0,185.0);
        H100.setLineColor(this.particleColors.get(this.particleID));
        H101.setLineColor(this.particleColors.get(this.particleID));
        H102.setLineColor(this.particleColors.get(this.particleID));
        
        H100.setLineWidth(this.histLineWidth);
        H101.setLineWidth(this.histLineWidth);
        H102.setLineWidth(this.histLineWidth);
        
        dirGen.add(H100);
        dirGen.add(H101);
        dirGen.add(H102);
        
        H1D H200 = new H1D("momentum","P (" + pidString +  ") [GeV]","",100,0.0,10.0);
        H1D H201 = new H1D("theta","Theta (" + pidString +  ") [Deg]","",100,0.0,45.0);
        H1D H202 = new H1D("phi","Phi (" + pidString +  ") [Deg]","",120,-185.0,185.0);
        
        H200.setLineColor(this.particleColors.get(this.particleID));
        H201.setLineColor(this.particleColors.get(this.particleID));
        H202.setLineColor(this.particleColors.get(this.particleID));
        
        H200.setLineWidth(this.histLineWidth);
        H201.setLineWidth(this.histLineWidth);
        H202.setLineWidth(this.histLineWidth);
        
        dirRec.add(H200);
        dirRec.add(H201);
        dirRec.add(H202);
        
        H1D H300 = new H1D("momentum","P (" + pidString +  ") [GeV]","efficiency",100,0.0,10.0);
        H1D H301 = new H1D("theta","Theta (" + pidString +  ") [Deg]","efficiency",100,0.0,45.0);
        H1D H302 = new H1D("phi","Phi (" + pidString +  ") [Deg]","efficiency",120,-185.0,185.0);
        H300.setLineColor(this.particleColors.get(this.particleID));
        H301.setLineColor(this.particleColors.get(this.particleID));
        H302.setLineColor(this.particleColors.get(this.particleID));
        
        H300.setLineWidth(this.histLineWidth);
        H301.setLineWidth(this.histLineWidth);
        H302.setLineWidth(this.histLineWidth);
        
        dirEff.add(H300);
        dirEff.add(H301);
        dirEff.add(H302);
        
        H1D H400 = new H1D("momentum","delta P/P (" + pidString +  ") [GeV]","",240,-0.3,0.3);
        H1D H401 = new H1D("theta","delta Theta (" + pidString +  ") [Deg]","",320,-2.0,2.0);
        H1D H402 = new H1D("phi","delta Phi (" + pidString +  ") [Deg]","",320,-2.0,2.0);

        H2D H403 = new H2D("momentumVSmomentum",40,0.0,10.0,220,-0.3,0.3);
        H2D H404 = new H2D("momentumVStheta"   ,40,0.0,50.0,120,-0.3,0.3);
        H2D H405 = new H2D("momentumVSphi"     ,40,-185.0,185.0,120,-0.3,-0.3);
        
        H2D H406 = new H2D("thetaVSmomentum",40,0.0,10.0,120,-2.0,2.0);
        H2D H407 = new H2D("phiVSmomentum",40,0.0,10.0,120,-5.0,5.0);
        
        H400.setLineColor(this.particleColors.get(this.particleID));
        H401.setLineColor(this.particleColors.get(this.particleID));
        H402.setLineColor(this.particleColors.get(this.particleID));
        
        H400.setLineWidth(this.histLineWidth);
        H401.setLineWidth(this.histLineWidth);
        H402.setLineWidth(this.histLineWidth);
        dirRes.add(H400);
        dirRes.add(H401);
        dirRes.add(H402);
        dirRes.add(H403);
        dirRes.add(H404);
        dirRes.add(H405);
        dirRes.add(H406);
        dirRes.add(H407);
        this.directories.add(dirGen);
        this.directories.add(dirRec);
        this.directories.add(dirEff);
        this.directories.add(dirRes);
        
        //dir.getDirectory(this.generatedDirectory).add(H100.name(),H100);
        //dir.getDirectory(this.generatedDirectory).add(H101.name(),H101);
        //dir.getDirectory(this.generatedDirectory).add(H102.name(),H102);
    }
    
    public void analyze(){
        H1D hme = (H1D) this.directories.get(2).getObject("momentum");
        H1D hmg = (H1D) this.directories.get(0).getObject("momentum");
        hme.divide(hmg);
        
        H1D hte = (H1D) this.directories.get(2).getObject("theta");
        H1D htg = (H1D) this.directories.get(0).getObject("theta");
        hte.divide(htg);
        
        H1D hpe = (H1D) this.directories.get(2).getObject("phi");
        H1D hpg = (H1D) this.directories.get(0).getObject("phi");
        hpe.divide(hpg);
        
    }
    
}
