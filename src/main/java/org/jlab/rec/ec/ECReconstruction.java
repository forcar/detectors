/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.rec.ec;

import org.jlab.clasrec.main.DetectorReconstruction;
import org.jlab.clasrec.utils.ServiceConfiguration;
import org.jlab.evio.clas12.EvioDataBank;
import org.jlab.evio.clas12.EvioDataEvent;



/**
 *
 * @author gavalian
 */
public class ECReconstruction extends DetectorReconstruction {

    public ECReconstruction() {
        super("EC", "gavalian", "1.0");
    }

    @Override
    public void processEvent(EvioDataEvent event) {
        try {
            ECStore store = new ECStore();
            store.initECHits(event, this.getGeometry("EC"));
            //store.showHits();
            store.initECPeaks();
            //store.showPeaks();
            store.initECPeakClusters();
            //store.showClusters();
            this.writeOutput(event, store);
        } catch (Exception e) {
            System.err.println("[EC-REC] >>>>>> something went wrong with this event");
            e.printStackTrace();
        }
    }
   
    
    public void writeOutput(EvioDataEvent event, ECStore store){
        EvioDataBank bankHits  = store.getBankHits(event);
        EvioDataBank bankPeaks = store.getBankPeaks(event);
        EvioDataBank bankClusters = store.getBankClusters(event);
        if(bankHits.rows()>0&&bankPeaks.rows()>0&&bankClusters.rows()>0)
            event.appendBanks(bankHits,bankPeaks,bankClusters);
    }
    
    @Override
    public void init() {
        this.requireGeometry("EC");
    }

    @Override
    public void configure(ServiceConfiguration c) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static void main(String[] args){        
    }

    
}
