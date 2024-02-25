package cepa.edu.mechanics;

import java.applet.AudioClip;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.batik.dom.events.DOMMouseEvent;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.script.Window;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGLoadEventDispatcherAdapter;
import org.apache.batik.swing.svg.SVGLoadEventDispatcherEvent;
import org.apache.batik.util.XMLResourceDescriptor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;
	
public class ClassicalDoppler extends JApplet {
	
	private static final long serialVersionUID = 1L;
	
	private static final short X = 0, Y = 1; 
	private double[] sourcePos = {50,50};
	private double[] detectorPos = {175,175};

	private final double[] ulc = {0,0};
	
	private double speed = 40; // m/s
	private JSVGCanvas canvas = new JSVGCanvas ();
	private Document document; // The SVG document
	private Window window; // The 'window' object
	
	private Element source,detector;
	  
	private boolean sourceSelected = false, detectorSelected = false;
	  
	private final short n = 15;
	private Element[] waves = new Element[n];
	private double[] r = new double[n];
	  

	Dimension svgViewBoxSize = new Dimension(300,300);
	  
	private double d = Math.sqrt(Math.pow(svgViewBoxSize.getWidth(),2) + Math.pow(svgViewBoxSize.getHeight(),2));
	  
	private final Translation transformThread = new Translation();
	  
	protected String filename;
	private long dt = 30;
	  
	private final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
	  
	  
//	private boolean isSourceEmphsized = false;
	private boolean isDetectorEmphsized = false;
	  
	  
	private AudioClip beep;
  
  
	public void destroy() {
		canvas.dispose();
	}
  
	public void stop() {
		canvas.setDocument(null);
	}
	  
	public void start() {
		
		// Define look & feel
        try{
        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        	SwingUtilities.updateComponentTreeUI(this);
        }
        catch( Exception e ){/*Nada*/}
        
		// Torna dinâmico o canvas.
		canvas.setDocumentState (JSVGCanvas.ALWAYS_DYNAMIC);
		
		// Associa a cena SVG (propriedade scene-file) ao canvas.
		canvas.setDocument (document);        
	}
	
	public void init() {
		
		// Inicia o vetor r[].
		for ( short i = 0; i < n; i++ ) r[i] = 0;		
		
		//final ResourceBundle resources = ResourceBundle.getBundle("resources"); //NOI18N
				
		//beep = getAudioClip(this.getCodeBase(),resources.getString("bleep-file")); // NOI8N
		beep = getAudioClip(this.getCodeBase(),getParameter("bleep-file")); // NOI8N
		
		canvas.setMySize (svgViewBoxSize);
				
		// Obtain the Window reference when it becomes available
		canvas.addSVGLoadEventDispatcherListener (
			new SVGLoadEventDispatcherAdapter () {
		        public void svgLoadEventDispatchStarted (
		               SVGLoadEventDispatcherEvent e) {
		
		        window = canvas.getUpdateManager ().
		           getScriptingEnvironment ().createWindow ();
		       }
		    }
		);
		
		try {
		    String parser = XMLResourceDescriptor.getXMLParserClassName();
		    SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
		
		    //URL url = new URL(getCodeBase(), resources.getString("scene-file"));
		    URL url = new URL(getCodeBase(), getParameter("scene-file"));		    
            document = f.createDocument(url.toString());
            
            // No applet viewer isso funciona. No applet de verdade não!
//		    File file = new File(this.getParameter("scene-file"));
//		    String uri = file.toURI().toString();			
//		    document = (SVGDocument) f.createDocument(uri);
            
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}    
    
		SVGElement root = (SVGElement) ((SVGDocument)document).getRootElement();
		
		root.setAttributeNS(null,"height",""+svgViewBoxSize.getHeight());
		root.setAttributeNS(null,"width",""+svgViewBoxSize.getWidth());
		root.setAttributeNS(null, "viewBox", ulc[X] + " " + ulc[Y] + " " + svgViewBoxSize.getWidth()+" "+svgViewBoxSize.getHeight());
							
		detector = document.getElementById("detector");
		detector.setAttributeNS (null, "transform", "translate(" + detectorPos[X] + "," + detectorPos[Y] + ")" );
		root.appendChild(detector);
		
		source = document.getElementById("source");
		source.setAttributeNS (null, "transform", "translate(" + sourcePos[X] + "," + sourcePos[Y] + ")" );
		root.appendChild(source);
				
		for ( short i = 0; i < n; i++ ){
		
			r[i] = i*d/n;
			
			waves[i] = document.createElementNS(svgNS,"circle");
			waves[i].setAttributeNS(null,"fill","none");
			waves[i].setAttributeNS(null,"stroke","gray");
			waves[i].setAttributeNS(null,"stroke-width","0.5");
			waves[i].setAttributeNS(null,"cx",""+sourcePos[X]);
			waves[i].setAttributeNS(null,"cy",""+sourcePos[Y]);
			waves[i].setAttributeNS(null,"r",""+r[i]);
			
			root.appendChild(waves[i]);
		}
	
	
		EventTarget sourceTarget = (EventTarget) source;
		sourceTarget.addEventListener ("click", new EventListener(){
			@Override
			public void handleEvent (Event evt) { 
	   			sourceSelected = !sourceSelected;
	    	}}, false);
		
		EventTarget detectorTarget = (EventTarget) detector;
		detectorTarget.addEventListener("click", new EventListener(){
			@Override
			public void handleEvent (Event evt) {
	   			detectorSelected = !detectorSelected;
	    	}}, false);
		
		EventTarget eventTarget = (EventTarget) root;
		eventTarget.addEventListener("SVGLoad", new EventListener () {
			
			@Override
	    	public void handleEvent (Event evt) {		    		
	    		window.setInterval (transformThread, dt);	    		
	    	}},false);
		eventTarget.addEventListener("mousemove", new EventListener() {

			@Override
			public void handleEvent(Event arg0) {				
				DOMMouseEvent me = (DOMMouseEvent) arg0;
				
				Dimension d = canvas.getSize();
				double margin = (d.getWidth() - d.getHeight()) / 2;	
				
				if ( sourceSelected ) {
									
					sourcePos[X] = ulc[X] + (me.getClientX() - Math.max(0,margin)) * svgViewBoxSize.getWidth() / Math.min(d.getHeight(), d.getWidth());
					sourcePos[Y] = ulc[Y] + (me.getClientY() + Math.min(0,margin)) * svgViewBoxSize.getHeight() / Math.min(d.getHeight(), d.getWidth());
					
					source.setAttribute ( "transform",
						"translate(" + sourcePos[X] + "," + sourcePos[Y] + ")" );	
									
				} else if ( detectorSelected ) {
										
					detectorPos[X] = ulc[X] + (me.getClientX() - Math.max(0,margin)) * svgViewBoxSize.getWidth() / Math.min(d.getHeight(), d.getWidth());
					detectorPos[Y] = ulc[Y] + (me.getClientY() + Math.min(0,margin)) * svgViewBoxSize.getHeight() / Math.min(d.getHeight(), d.getWidth());
					
					detector.setAttribute ( "transform",
						"translate(" + detectorPos[X] + "," + detectorPos[Y] + ")" );	
				}				
			}}, false);
		
		
		getContentPane().add(canvas);
		
		// Remove os objetos Interactor do canvas, para evitar que o
		// usuário possa arrastar, rotacionar ou dar zoom na cena SVG.
		canvas.setEnableImageZoomInteractor(false);
		canvas.setEnablePanInteractor(false);
		canvas.setEnableRotateInteractor(false);
		canvas.setEnableZoomInteractor(false);		
	}
  

  	private double dist( final double[] p1, final double[] p2 ) {
  		return Math.sqrt(Math.pow(p1[X]-p2[X],2)+Math.pow(p1[Y]-p2[Y],2));
  	}
  	  	
	public class Translation implements Runnable {
    
		private double tA = 0, tB = 0, tC = 0;
		private double dr = 0;
		private double opacity = 1.0;  
	
		public Translation () {
			tA = System.currentTimeMillis()/1000d;
		}
	
	    public void run () {
	    	
	    	// Des-destaca a fonte um quadro (frame) após ter sido destacada.
//	    	if ( isSourceEmphsized ) {
//	    		
//				isSourceEmphsized = !isSourceEmphsized;
//				
//				source.setAttribute("transform",
//					"translate(" + sourcePos[X] + "," + sourcePos[Y] + ") scale(1.0)" );				
//			}
	    	
	    	// Des-destaca o detector um quadro (frame) após ter sido destacado.
	    	if ( isDetectorEmphsized ) {
	    		
	    		isDetectorEmphsized = !isDetectorEmphsized;
	    		
    			detector.setAttribute("transform",
    	    		"translate(" + detectorPos[X] + "," + detectorPos[Y] + ") scale(1.0)" );	
	    	}
	    	
	    	tC = System.currentTimeMillis()/1000d - tA;
	    	
	    	dr = speed * (tC-tB);
	    	
	    	for ( short i = 0; i < n; i++ ) {   		
				r[i] += dr;
				if ( r[i] > d ){
					
					waves[i].setAttribute("cx", "" + sourcePos[X]);
					waves[i].setAttribute("cy", "" + sourcePos[Y]);
					
					r[i] = r[i] - d;
					
					// Destaca a fonte quando uma nova frente de onda é emitida.
//					if (!isSourceEmphsized){					
//						isSourceEmphsized = !isSourceEmphsized;
//						source.setAttribute("transform",
//							"translate(" + sourcePos[X] + "," + sourcePos[Y] + ") scale(1.5)" );			
//					}
				}
				
				opacity = 1-r[i]*0.9/d;
				
				waves[i].setAttribute( "r", ""+r[i] );				
	    		waves[i].setAttribute( "stroke-opacity", ""+opacity );
	    		
	    		// Verifica se o detector está sobre uma frente de onda
	    		double[] center = new double[2];
	    		center[X] = Double.parseDouble(waves[i].getAttribute("cx"));
	    		center[Y] = Double.parseDouble(waves[i].getAttribute("cy"));
	    		
	    		if (Math.abs(dist(center,detectorPos)-r[i]) < 2){
	
	    			isDetectorEmphsized = !isDetectorEmphsized;
	    			beep.play();
	    			
	    			detector.setAttribute("transform",
	    				"translate(" + detectorPos[X] + "," + detectorPos[Y] + ") scale(1.5)");
	    		}
	    	}
	
	    	tB = tC;
	    }
	} 	
}

