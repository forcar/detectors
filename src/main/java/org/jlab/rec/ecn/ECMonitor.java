/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.ecn;

import static cnuphys.splot.pdata.DataSetType.H1D;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.detector.DetectorDescriptor;
import org.jlab.clas.detector.DetectorType;
import org.jlab.clas12.basic.IDetectorModule;
import org.jlab.clas12.basic.IDetectorProcessor;
import org.jlab.clas12.calib.DetectorShape2D;
import org.jlab.clas12.calib.DetectorShapeTabView;
import org.jlab.clas12.calib.DetectorShapeView2D;
import org.jlab.clas12.calib.IDetectorListener;
import org.jlab.clas12.detector.DetectorCounter;
import org.jlab.clas12.detector.EventDecoder;
import org.jlab.clasrec.main.DetectorEventProcessorPane;
import org.jlab.clasrec.utils.CLASGeometryLoader;
import org.jlab.data.io.DataEvent;
import org.jlab.evio.clas12.EvioDataEvent;
import org.root.histogram.H1D;
import org.root.pad.EmbeddedCanvas;

/**
 *
 * @author gavalian
 */
public class ECMonitor implements IDetectorModule, IDetectorProcessor,IDetectorListener {
    
    JPanel    detectorPanel = null;
    DetectorEventProcessorPane evPane = new DetectorEventProcessorPane();
    DetectorShapeTabView  view   = new DetectorShapeTabView();
    DetectorShapeTabView  viewPC   = new DetectorShapeTabView();
    DetectorShapeTabView  viewEI   = new DetectorShapeTabView();
    DetectorShapeTabView  viewEO   = new DetectorShapeTabView();

    EventDecoder          decoder = new EventDecoder();
    ECDetectorReconstruction  ecRec = new ECDetectorReconstruction();
    EmbeddedCanvas            canvas = new EmbeddedCanvas();
    DetectorCollection<H1D>   viewH1 = new DetectorCollection<H1D>();
    
    public ECMonitor(){
        this.detectorPanel = new JPanel();
        this.detectorPanel.setLayout(new BorderLayout());
        JPanel viewPane = new JPanel();
        viewPane.setLayout(new FlowLayout());
        viewPane.add(viewPC);
        viewPane.add(viewEI);
        viewPane.add(viewEO);
        JSplitPane split = new JSplitPane();
        split.setOrientation(JSplitPane.VERTICAL_SPLIT);
        //split.setLeftComponent(view);
        //split.setRightComponent(canvas);
        split.setTopComponent(viewPane);
        split.setBottomComponent(canvas);
        this.detectorPanel.add(split,BorderLayout.CENTER);
        this.detectorPanel.add(evPane,BorderLayout.PAGE_END);
        this.evPane.addProcessor(this);
        //view.addDetectorLayer(view2D);
        this.initDetector();
        ecRec.init();
    }
    
    public void processEvent(DataEvent de) {
        /*
        ecRec.processEvent( (EvioDataEvent) de);
        
        List<ECStrip>  ecStrips = ecRec.getStrips();
        
        viewH1.clear();
        
        for(ECStrip strip : ecStrips){
            viewH1.add(
                    strip.getDescriptor().getSector(), 
                    strip.getDescriptor().getLayer(),
                    strip.getDescriptor().getComponent(),
                    new H1D("STRIP",100,0.0,65));
        }
        //this.viewH1.show();
        this.viewPC.repaint();
        this.viewEI.repaint();
        this.viewEO.repaint();
        
        this.ecRec.getH().setFillColor(4);
        canvas.divide(1, 1);
        canvas.cd(0);
        canvas.draw(this.ecRec.getH());
        */
        /*
        List<H1D>  h1List = this.viewH1.getList();
        for(H1D h1 : h1List){
            h1.reset();
        }        
       
        this.view.repaint();
        canvas.divide(1, 3);
        for(int loop = 0; loop < 3; loop++){
            canvas.cd(loop);
            H1D h1 = this.viewH1.get(2, loop+1, 0);
            h1.setFillColor(4);
            canvas.draw(h1);
        }*/
    }

    public void initDetector(){
        DetectorShapeView2D   viewPCAL  = new DetectorShapeView2D("PCAL");
        DetectorShapeView2D   viewECIN  = new DetectorShapeView2D("ECIN");
        DetectorShapeView2D   viewECOUT = new DetectorShapeView2D("ECOUT");

        for(int sector = 0; sector < 6; sector++){
            List<DetectorShape2D>  shapesPCAL  = CLASGeometryLoader.getDetectorShape2D(DetectorType.EC, sector, 0);
            List<DetectorShape2D>  shapesECIN  = CLASGeometryLoader.getDetectorShape2D(DetectorType.EC, sector, 1);
            List<DetectorShape2D>  shapesECOUT = CLASGeometryLoader.getDetectorShape2D(DetectorType.EC, sector, 2);
            
            for(DetectorShape2D shape : shapesPCAL) viewPCAL.addShape(shape);
            for(DetectorShape2D shape : shapesECIN) viewECIN.addShape(shape);
            for(DetectorShape2D shape : shapesECOUT) viewECOUT.addShape(shape);
        }
        
        this.viewPC.addDetectorLayer(viewPCAL);
        this.viewEI.addDetectorLayer(viewECIN);
        this.viewEO.addDetectorLayer(viewECOUT);
        
        this.viewPC.addDetectorListener(this);
        this.viewEI.addDetectorListener(this);
        this.viewEO.addDetectorListener(this);
    }
    
    public String getName() {
        return "ECMonitor";
    }

    public String getAuthor() {
        return "gavalian";
    }

    public String getDescription() {
        return "Monitors EC reconstruction";
    }

    public DetectorType getType() {
        return DetectorType.EC;
    }

    public JPanel getDetectorPanel() {
        return this.detectorPanel;
    }
    
    public static void main(String[] args){
        ECMonitor module = new ECMonitor();
        JFrame frame = new JFrame();
        frame.add(module.getDetectorPanel());
        frame.pack();
        frame.setVisible(true);
    }

    public void detectorSelected(DetectorDescriptor desc) {
        System.out.println(" SELECTED = " + desc);
        this.canvas.divide(1, 1);
        this.canvas.cd(0);
        //this.canvas.draw(this.ecRec.getH());
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void update(DetectorShape2D shape) {
        int sector = shape.getDescriptor().getSector();
        int layer  = shape.getDescriptor().getLayer();
        int comp   = shape.getDescriptor().getComponent();
        //System.out.println("UPDATING COLORS FOR " + shape.getDescriptor());
        if(this.viewH1.hasEntry(sector, layer, comp)==true){
            shape.setColor(255, 0, 0, 255);
            //System.out.println(" UPDATING COLOR FOR " + shape.getDescriptor());
        } else {
            shape.setColor(180, 180, 245, 100);
        }
    }
    
}
