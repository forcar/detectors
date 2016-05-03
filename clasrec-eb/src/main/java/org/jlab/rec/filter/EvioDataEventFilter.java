/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.filter;

import org.jlab.clasrec.main.DetectorReconstruction;
import org.jlab.clasrec.utils.ServiceConfiguration;
import org.jlab.evio.clas12.EvioDataBank;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.evio.clas12.EvioFactory;


/**
 *
 * @author gavalian
 */
public class EvioDataEventFilter extends DetectorReconstruction {

    public EvioDataEventFilter() {
        super("FILTER", "gavalian", "1.0");
    }

    @Override
    public void processEvent(EvioDataEvent ede) {
        EvioDataEvent  event = EvioFactory.createEvioEvent();
        if(ede.hasBank("GenPart::true")==true){
            EvioDataBank genBank = (EvioDataBank) ede.getBank("GenPart::true");
            //genBank.show();
            event.appendGeneratedBank(genBank);
        }
        if(ede.hasBank("EVENTHB::particle")&&ede.hasBank("EVENTHB::detector")){
            EvioDataBank evBank = (EvioDataBank) ede.getBank("EVENTHB::particle");
            EvioDataBank dtBank = (EvioDataBank) ede.getBank("EVENTHB::detector");
            event.appendBanks(evBank,dtBank);            
        } else {
        
            if(ede.hasBank("EVENTHB::particle")){
                EvioDataBank evBank = (EvioDataBank) ede.getBank("EVENTHB::particle");       
                event.appendBanks(evBank);
            }
        }
        //ede. event;
        ede.copyEvent(event);
    }

    @Override
    public void init() {
        
    }

    @Override
    public void configure(ServiceConfiguration sc) {
        
    }
    
}
