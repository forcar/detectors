/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.mon.eb;

import java.util.ArrayList;
import org.jlab.evio.clas12.EvioDataEvent;
import org.root.group.TDirectory;
import org.root.histogram.H1D;

/**
 *
 * @author gavalian
 */
public class DetectorMatching {
    private ArrayList<TDirectory>  dirs = new ArrayList<TDirectory>();
    private String[] detectorList = new String[]{"ftof1a","ftof1b","pcal","ecin","ecout"};
    
    public DetectorMatching(){
        
    }
    
    public void process(EvioDataEvent event){
        
    }
    
    public void init(){
        
        for(int loop = 0; loop < detectorList.length; loop++){
            String dirname = "Detectors/Matching/"+detectorList[loop];
            dirs.add(new TDirectory(dirname));
        }
        
        for(int loop = 0; loop < dirs.size(); loop++){
            H1D mX = new H1D("matchingX","X matching","",100,-12.0,12.0);
            H1D mY = new H1D("matchingY","Y matching","",100,-12.0,12.0);
            H1D mZ = new H1D("matchingZ","Z matching","",100,-12.0,12.0);
            dirs.get(loop).add(mX);
            dirs.get(loop).add(mY);
            dirs.get(loop).add(mZ);
        }
    }
    
    public void analyze(){
        
    }
    public ArrayList<TDirectory> getDirectories(){
        return this.dirs;
    }
}
