/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.mon.eb;

import java.util.ArrayList;
import org.jlab.clasrec.main.DetectorMonitoring;
import org.jlab.clasrec.rec.CLASMonitoring;
import org.jlab.clasrec.utils.ServiceConfiguration;
import org.jlab.evio.clas12.EvioDataEvent;
import org.root.group.TBrowser;
import org.root.group.TDirectory;
import org.root.pad.DirectoryViewer;

/**
 *
 * @author gavalian
 */
public class EventBuilderMonitoring extends DetectorMonitoring {
    private ParticleMonitoring  monElectron = new ParticleMonitoring();
    private ArrayList<ParticleMonitoring> particlesMon = new ArrayList<ParticleMonitoring>();
    private DetectorMatching  detectorMatching = new DetectorMatching();
    
    public EventBuilderMonitoring(){
        super("EBMON","1.0","gavalian");
    }
    
    @Override
    public void processEvent(EvioDataEvent event) {
        monElectron.process(event);
        for(ParticleMonitoring mon : this.particlesMon){
            mon.process(event);            
        }
        
        this.detectorMatching.process(event);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void init() {
        this.particlesMon.add(new ParticleMonitoring(   11));
        this.particlesMon.add(new ParticleMonitoring(  211));
        this.particlesMon.add(new ParticleMonitoring( -211));
        this.particlesMon.add(new ParticleMonitoring(  321));
        this.particlesMon.add(new ParticleMonitoring( -321));
        this.particlesMon.add(new ParticleMonitoring( 2212));
        
        
        for(ParticleMonitoring mon : this.particlesMon){
            mon.init();
        }
        
        this.detectorMatching.init();
        this.monElectron.init();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void configure(ServiceConfiguration c) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void analyze() {
        /*
        ArrayList<TDirectory> electronDirs = this.monElectron.getDirectiries();
        for(TDirectory dir : electronDirs){
            this.getDir().addDirectory(dir);
        }*/
        for(ParticleMonitoring mon : this.particlesMon){
            mon.analyze();
            for(TDirectory dir : mon.getDirectiries()){
                this.getDir().addDirectory(dir);
            }
        }
        this.detectorMatching.analyze();
        for(TDirectory dir : this.detectorMatching.getDirectories()){
            this.getDir().addDirectory(dir);
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static void main(String[] args){
        String inputFile = args[0];
        System.err.println(" \n[PROCESSING FILE] : " + inputFile);
        EventBuilderMonitoring  ebMonitor = new EventBuilderMonitoring();
        ebMonitor.init();
        CLASMonitoring  monitor = new CLASMonitoring(inputFile, ebMonitor);
        monitor.process();
        //ebMonitor.analyze();
        ebMonitor.getDir().write("monitoring.evio");
        TBrowser browser = new TBrowser(ebMonitor.getDir());
    }
}
