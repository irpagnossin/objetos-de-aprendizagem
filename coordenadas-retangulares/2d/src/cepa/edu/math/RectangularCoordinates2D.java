package cepa.edu.math;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.batik.dom.events.DOMMouseEvent;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.XMLResourceDescriptor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;

/**
 *
 * @author irpagnossin
 */
public class RectangularCoordinates2D extends javax.swing.JFrame {

    @SuppressWarnings("static-access") // NOI18N
    public RectangularCoordinates2D() {
        setSize(500,500);

        initComponents();

        // Define look & feel
        try{
        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        	SwingUtilities.updateComponentTreeUI(this);
        }
        catch( Exception e ){/*Nada*/}

        numberFormat = (new DecimalFormat("###,##0.0")).getInstance(new Locale("en","US")); // NOI18N
        numberFormat.setMaximumFractionDigits(1);

        ResourceBundle resources = ResourceBundle.getBundle("resources"); // NOI18N
        /***********************************************************************
         Carrega o arquivo SVG para o JPanel.
         **********************************************************************/
		try {
		    String parser = XMLResourceDescriptor.getXMLParserClassName();
		    SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
    
            ClassLoader classLoader = this.getClass().getClassLoader();
            URL url = classLoader.getResource(resources.getString("scene-file")); // NOI18N
            String uri = url.toURI().toString();
            
		    document = (SVGDocument) f.createDocument(uri);
		}

		catch (IOException ex) { Logger.getLogger(RectangularCoordinates2D.class.getName()).log(Level.SEVERE, null, ex); }        catch (URISyntaxException ex) { Logger.getLogger(RectangularCoordinates2D.class.getName()).log(Level.SEVERE, null, ex); }

		canvas.setDocumentState (JSVGCanvas.ALWAYS_DYNAMIC); // Torna dinâmico o canvas.
		canvas.setDocument (document); // Associa a cena SVG (propriedade scene-file) ao canvas.
		canvas.setEnableImageZoomInteractor(false); // TODO: desabilita o quê?
		canvas.setEnablePanInteractor(false); // Desabilita a opção de arrastar a cena SVG.
		canvas.setEnableRotateInteractor(false); // Desabilita a opção de rotacionar a cena SVG.
		canvas.setEnableZoomInteractor(false); // Desabilita a opção de ampliar/reduzir a cena SVG.        
        
        SVGPanel.setLayout(new BorderLayout());
        SVGPanel.add(canvas,BorderLayout.CENTER);


        /***********************************************************************
         Configuração dos elementos da cena SVG.
         **********************************************************************/

        // Carrega os elementos SVG relevantes.
        SVGElement root = (SVGElement) ((SVGDocument)document).getRootElement();
		referenceFrame = document.getElementById("rframe"); // NOI18N
        ptoA = document.getElementById("ptoA"); // NOI8N
        ptoE = document.getElementById("ptoE"); // NOI8N
        xyAxis = document.getElementById("frame"); // NOI18N
        ptoC = document.getElementById("ptoC"); // NOI18N
        coordinates[X] = document.getElementById("x"); // NOI18N
        coordinates[Y] = document.getElementById("y"); // NOI18N
        xyPair = document.getElementById("xy-label"); // NOI18N
        hLine = document.getElementById("hLine"); // NOI18N
        vLine = document.getElementById("vLine"); // NOI18N
        ptoATip = document.getElementById("ptoA-tip");
        ptoETip = document.getElementById("ptoE-tip");

        // Lê da imagem SVG as dimensões da cena SVG e define o viewBox com base nelas.
        SVGViewBoxSize[X] = Double.parseDouble(root.getAttribute("width")); // NOI18N
        SVGViewBoxSize[Y] = Double.parseDouble(root.getAttribute("height")); // NOI18N
        root.setAttribute( "viewBox", ulc[X] + " " + ulc[Y] + " " + SVGViewBoxSize[X] + " " + SVGViewBoxSize[Y] ); // NOI18N

        // Lê da imagem SVG a posição inicial do ponto A (manete de translação).
        ptoAPos[X] = Double.parseDouble(ptoA.getAttribute("x")); // NOI18N
        ptoAPos[Y] = Double.parseDouble(ptoA.getAttribute("y")); // NOI18N

        // Lê da imagem SVG a posição inicial do ponto E (manete de rotação).
        double[] ptoEPos = new double[2];
        ptoEPos[X] = Double.parseDouble(ptoE.getAttribute("x")); // NOI18N
        ptoEPos[Y] = Double.parseDouble(ptoE.getAttribute("y")); // NOI18N

        // Calcula a distância entre as manetes de translação e rotação.
        radius = Math.sqrt(Math.pow(ptoEPos[X]-ptoAPos[X],2) + Math.pow(ptoEPos[Y]-ptoAPos[Y],2));

        // Calcula o ângulo de rotação inicial do sistema de referência.
        theta = Math.atan2(ptoEPos[Y]-ptoAPos[Y],ptoEPos[X]-ptoAPos[X]);

        // Lê da imagem SVG a posição inicial do ponto C (amostra).
        ptoCPos[X] = Double.parseDouble(ptoC.getAttribute("x")); // NOI18N
        ptoCPos[Y] = Double.parseDouble(ptoC.getAttribute("y")); // NOI18N

        // Calcula as coordenadas dos pontos B e D.
        double distAC = Math.sqrt(Math.pow(ptoCPos[X]-ptoAPos[X],2)+Math.pow(ptoCPos[Y]-ptoAPos[Y],2));
        double phi = Math.atan2(ptoCPos[Y]-ptoAPos[Y],ptoCPos[X]-ptoAPos[X]) - theta;

        ptoBPos[X] = ptoAPos[X] - distAC * Math.sin(phi) * Math.sin(theta);
        ptoBPos[Y] = ptoAPos[Y] + distAC * Math.sin(phi) * Math.cos(theta);

        ptoDPos[X] = ptoAPos[X] + distAC * Math.cos(phi) * Math.cos(theta);
        ptoDPos[Y] = ptoAPos[Y] + distAC * Math.cos(phi) * Math.sin(theta);

        // Cria a linha paralela ao eixo x que liga a amostra ao eixo y. Isto é,
        // o segmento de reta que liga os pontos B e C.
        hLine.setAttribute("x1", "" + ptoBPos[X]); // NOI18N
        hLine.setAttribute("x2", "" + ptoCPos[X]); // NOI18N
        hLine.setAttribute("y1", "" + ptoBPos[Y]); // NOI18N
        hLine.setAttribute("y2", "" + ptoCPos[Y]); // NOI18N

        // Cria a linha paralela ao eixo y que liga a amostra ao eixo x. Isto é,
        // o segmento de reta que liga os pontos D e C.
        vLine.setAttribute("x1", "" + ptoDPos[X]); // NOI18N
        vLine.setAttribute("x2", "" + ptoCPos[X]); // NOI18N
        vLine.setAttribute("y1", "" + ptoDPos[Y]); // NOI18N
        vLine.setAttribute("y2", "" + ptoCPos[Y]); // NOI18N

        // Cria o rótulo da coordenada x da amostra.
        coordinates[X].setAttribute("x", "" + (ptoDPos[X] + xCoordOffset[X])); // NOI18N
        coordinates[X].setAttribute("y", "" + (ptoDPos[Y] + xCoordOffset[Y])); // NOI18N
        double x = distAC*Math.cos(phi);
        coordinates[X].setTextContent(numberFormat.format(x));

        // Cria o rótulo da coordenada y da amostra.
        coordinates[Y].setAttribute("x", "" + (ptoBPos[X] + yCoordOffset[X])); // NOI18N
        coordinates[Y].setAttribute("y", "" + (ptoBPos[Y] + yCoordOffset[Y])); // NOI18N
        double y = distAC*Math.sin(phi);
        coordinates[Y].setTextContent(numberFormat.format(-y));

        Element tmp = document.getElementById("pC"); // NOI18N
        ptoCLabelOffset[X] = Double.parseDouble(tmp.getAttribute("width")); // NOI18N
        ptoCLabelOffset[Y] = Double.parseDouble(tmp.getAttribute("height")); // NOI18N

        xyPair.setAttribute("x", "" + (ptoCPos[X]+ptoCLabelOffset[X]/2)); // NOI18N
        xyPair.setAttribute("y", "" + (ptoCPos[Y]-ptoCLabelOffset[Y]/2)); // NOI18N
        xyPair.setTextContent("(" + numberFormat.format(x) + " , " + numberFormat.format(-y) + ")"); // NOI18N

        /***********************************************************************
         Configuração dos observadores de eventos.
         **********************************************************************/

        // Configura os observadores dos eventos do mouse no ponto A (manete de translação).
        EventTarget ptoAEventTarget = (EventTarget) ptoA;
        PtoAEventListener ptoAEventListener = new PtoAEventListener();
        ptoAEventTarget.addEventListener("click", ptoAEventListener, false); // NOI18N
        ptoAEventTarget.addEventListener("mouseover", new EventListener(){ // NOI8N

            public void handleEvent(Event evt) {
                ptoATip.setAttribute("opacity", "1"); // NOI8N
            }
        }, false);
        ptoAEventTarget.addEventListener("mouseout", new EventListener(){ // NOI8N

            public void handleEvent(Event evt) {
                ptoATip.setAttribute("opacity", "0"); // NOI8N
            }
        }, false);

        // Configura os observadores dos eventos do mouse no ponto C (amostra).
        EventTarget ptoCEventTarget = (EventTarget) ptoC;
        PtoCEventListener ptoCEventListener = new PtoCEventListener();
        ptoCEventTarget.addEventListener("click", ptoCEventListener, false); // NOI18N

        // Configura os observadores dos eventos do mouse no ponto E (manete de rotação).
        EventTarget ptoEEventTarget = (EventTarget) ptoE;
        PtoEEventListener ptoEEventListener = new PtoEEventListener();
        ptoEEventTarget.addEventListener("click", ptoEEventListener, false); // NOI18N
        ptoEEventTarget.addEventListener("mouseover", new EventListener(){ // NOI8N

            public void handleEvent(Event evt) {
                ptoETip.setAttribute("opacity", "1"); // NOI8N
            }
        }, false);
        ptoEEventTarget.addEventListener("mouseout", new EventListener(){ // NOI8N

            public void handleEvent(Event evt) {
                ptoETip.setAttribute("opacity", "0"); // NOI8N
            }
        }, false);

        // Configura os observadores dos eventos do mouse na raiz do documento SVG.
        EventTarget rootEventTarget = (EventTarget) root;
        rootEventTarget.addEventListener("mousemove", new EventListener(){
            @Override
            public void handleEvent(Event evt) {

                Dimension d = SVGPanel.getSize();
				double margin = (d.getWidth() - d.getHeight()) / 2;

                DOMMouseEvent mouseEvent = (DOMMouseEvent) evt;

                mousePos[X] = ulc[X] + (mouseEvent.getClientX() - Math.max(0,margin)) * SVGViewBoxSize[X] / Math.min(d.getHeight(), d.getWidth());
                mousePos[Y] = ulc[Y] + (mouseEvent.getClientY() + Math.min(0,margin)) * SVGViewBoxSize[Y] / Math.min(d.getHeight(), d.getWidth());

                if (ptoASelected) {

                    ptoAPos[X] = mousePos[X];
                    ptoAPos[Y] = mousePos[Y];

                    double rX = mousePos[X]-ptoAPos[X];
                    double rY = mousePos[Y]-ptoAPos[Y];
                    double xLinha = rX * Math.cos(theta) - rY * Math.sin(theta) + ptoAPos[X];
                    double yLinha = rX * Math.sin(theta) + rY * Math.cos(theta) + ptoAPos[Y];

                    ptoA.setAttribute("x", "" + xLinha);
                    ptoA.setAttribute("y", "" + yLinha);

                    ptoE.setAttribute("x", "" + xLinha);
                    ptoE.setAttribute("y", "" + yLinha);

                    xyAxis.setAttribute("x", "" + xLinha);
                    xyAxis.setAttribute("y", "" + yLinha);
                }
                else if (ptoESelected) {
                    theta = Math.atan2(mousePos[Y]-ptoAPos[Y], mousePos[X]-ptoAPos[X]);
                }
                else if (ptoCSelected) {
                    ptoCPos[X] = mousePos[X];
                    ptoCPos[Y] = mousePos[Y];

                    ptoC.setAttribute("x", "" + ptoCPos[X]);
                    ptoC.setAttribute("y", "" + ptoCPos[Y]);
                }

                // Atualiza a rotação do sistema de coordenadas. Esta operação deve ser feita sempre que o ângulo
                // da rotação ou as coordenadas do eixo de rotação mudar. Isto é, sempre que o usuário arrastar as
                // manetes de rotação ou translação.
                referenceFrame.setAttribute("transform", "rotate(" + Math.toDegrees(theta) + "," + ptoAPos[X] + "," + ptoAPos[Y] + ")");

                // Atualiza as coordenadas dos pontos B e D.
                double distAC = Math.sqrt(Math.pow(ptoCPos[X]-ptoAPos[X],2)+Math.pow(ptoCPos[Y]-ptoAPos[Y],2));
                double phi = Math.atan2(ptoCPos[Y]-ptoAPos[Y],ptoCPos[X]-ptoAPos[X]) - theta;

                ptoBPos[X] = ptoAPos[X] - distAC * Math.sin(phi) * Math.sin(theta);
                ptoBPos[Y] = ptoAPos[Y] + distAC * Math.sin(phi) * Math.cos(theta);

                ptoDPos[X] = ptoAPos[X] + distAC * Math.cos(phi) * Math.cos(theta);
                ptoDPos[Y] = ptoAPos[Y] + distAC * Math.cos(phi) * Math.sin(theta);

                // Atualiza a linha paralela ao eixo x que liga a amostra ao eixo y.
                hLine.setAttribute("x1", "" + ptoBPos[X]);
                hLine.setAttribute("x2", "" + ptoCPos[X]);
                hLine.setAttribute("y1", "" + ptoBPos[Y]);
                hLine.setAttribute("y2", "" + ptoCPos[Y]);

                // Atualiza a linha paralela ao eixo y que liga a amostra ao eixo x.
                vLine.setAttribute("x1", "" + ptoDPos[X]);
                vLine.setAttribute("x2", "" + ptoCPos[X]);
                vLine.setAttribute("y1", "" + ptoDPos[Y]);
                vLine.setAttribute("y2", "" + ptoCPos[Y]);

                // Atualiza o rótulo da coordenada x da amostra.
                double x = distAC*Math.cos(phi);
                coordinates[X].setAttribute("x", "" + (x + ptoAPos[X]));
                coordinates[X].setAttribute("y", "" + (ptoAPos[Y]+offset[Y]));
                coordinates[X].setTextContent(numberFormat.format(x));

                // Atualiza o rótulo da coordenada y da amostra.
                double y = distAC*Math.sin(phi);
                coordinates[Y].setAttribute("x", "" + (ptoAPos[X] + offset[X]));
                coordinates[Y].setAttribute("y", "" + (y + ptoAPos[Y]));
                coordinates[Y].setTextContent(numberFormat.format(-y));

                xyPair.setAttribute("x", "" + (ptoCPos[X]+ptoCLabelOffset[X]/2));
                xyPair.setAttribute("y", "" + (ptoCPos[Y]-ptoCLabelOffset[Y]/2));
                xyPair.setTextContent("(" + numberFormat.format(x) + " , " + numberFormat.format(-y) + ")");
            }
        }, false); // NOI18N

        setGlassPane(new LogoPanel(resources.getString("logo-file"))); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked") // NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        SVGPanel = new javax.swing.JPanel(new BorderLayout());
        jMenuBar1 = new javax.swing.JMenuBar();
        aboutMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        SVGPanel.setBackground(new java.awt.Color(255, 255, 255));
        SVGPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        SVGPanel.setPreferredSize(new java.awt.Dimension(400, 400));

        javax.swing.GroupLayout SVGPanelLayout = new javax.swing.GroupLayout(SVGPanel);
        SVGPanel.setLayout(SVGPanelLayout);
        SVGPanelLayout.setHorizontalGroup(
            SVGPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 396, Short.MAX_VALUE)
        );
        SVGPanelLayout.setVerticalGroup(
            SVGPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 396, Short.MAX_VALUE)
        );

        aboutMenu.setText("Sobre/About");

        jMenuItem1.setText("aboutOption");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        aboutMenu.add(jMenuItem1);

        jMenuBar1.add(aboutMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SVGPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SVGPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        getGlassPane().setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RectangularCoordinates2D().setVisible(true);
            }
        });
    }

    // Observador de eventos do ponto A (manete de rotação).
    private class PtoAEventListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            ptoASelected = !ptoASelected;
            ptoCSelected = false;
            ptoESelected = false;

            if (ptoASelected){
                ptoA.setAttribute("fill", "red"); // NOI18N
                ptoATip.setAttribute("opacity", "0"); // NOI18N
            }
            else{
                ptoA.setAttribute("fill", "black"); // NOI18N
            }
        }
    }

    // Observador de eventos do ponto C (amostra).
    private class PtoCEventListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            ptoASelected = false;
            ptoCSelected = !ptoCSelected;
            ptoESelected = false;

            if (ptoCSelected) ptoC.setAttribute("opacity", "0.5"); // NOI18N
            else ptoC.setAttribute("opacity", "1.0"); // NOI18N
        }
    }

    // Observador de eventos do ponto E (manete de rotação).
    private class PtoEEventListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            ptoASelected = false;
            ptoCSelected = false;
            ptoESelected = !ptoESelected;

            if (ptoESelected){
                ptoE.setAttribute("fill", "red"); // NOI18N
                ptoETip.setAttribute("opacity","1"); // NOI18N
            }
            else{
                ptoE.setAttribute("fill", "black"); // NOI18N
            }
        }
    }

    private class LogoPanel extends JComponent implements MouseListener{

		BufferedImage logo = null;

        public LogoPanel( String path ){
            super();

            try{
                ClassLoader classLoader = this.getClass().getClassLoader();
                URL url = classLoader.getResource(path);
                if ( url != null ) logo = ImageIO.read(url);
            }
            catch( IOException e ){}

            addMouseListener(this);
        }
        
        @Override
        protected void paintComponent( Graphics g ){
            if ( logo != null ) g.drawImage( logo, (getWidth() - logo.getWidth() >> 1), (getHeight() - logo.getHeight() >> 1), null );
        }

        public void mouseClicked(MouseEvent e) {}

        public void mousePressed(MouseEvent e) {
            setVisible(false);
        }

        public void mouseReleased(MouseEvent e) {
            mousePressed(e);
        }

        public void mouseEntered(MouseEvent e) {
            requestFocus();
        }

        public void mouseExited(MouseEvent e) {}
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel SVGPanel;
    private javax.swing.JMenu aboutMenu;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    // End of variables declaration//GEN-END:variables

	private static final short X = 0, Y = 1;

	private JSVGCanvas canvas = new JSVGCanvas ();
	private Document document; // The SVG document

    private final double[] ulc = {0,0};

    private Element ptoA, ptoC, ptoE, referenceFrame, xyAxis, hLine, vLine, xyPair, ptoATip, ptoETip;
    private boolean ptoASelected = false,
                    ptoCSelected = false,
                    ptoESelected = false;

    private double[] mousePos = {0,0};

    private double[] SVGViewBoxSize = {40,40};
    private double radius = 0;
    private double theta = 0;

    private double[] ptoAPos = {0,0}, ptoBPos = {0,0}, ptoCPos = {0,0}, ptoDPos = {0,0};
    private Element[] coordinates = new Element[2];
    private final double[] offset = {-1,1};

    private final double[] xCoordOffset = {-1,1};
    private final double[] yCoordOffset = {-1,1};

    private NumberFormat numberFormat;

    private double[] ptoCLabelOffset = {0,0};
}
