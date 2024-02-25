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
public class PolarCoordinates2D extends javax.swing.JFrame {

    @SuppressWarnings("static-access") // NOI18N
    public PolarCoordinates2D() {
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

		catch (IOException ex) { Logger.getLogger(PolarCoordinates2D.class.getName()).log(Level.SEVERE, null, ex); }        catch (URISyntaxException ex) { Logger.getLogger(PolarCoordinates2D.class.getName()).log(Level.SEVERE, null, ex); }

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
        rLabel = document.getElementById("x"); // NOI18N
        phiLabel = document.getElementById("y"); // NOI18N
        xyPair = document.getElementById("xy-label"); // NOI18N
        rLine = document.getElementById("rLine"); // NOI18N
        //vLine = document.getElementById("vLine"); // NOI18N
        arc = document.getElementById("arc"); // NOI18N
        ptoATip = document.getElementById("ptoA-tip"); // NOI18N
        ptoETip = document.getElementById("ptoE-tip"); // NOI18N

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
        
        // Calcula o ângulo de rotação inicial do sistema de referência.
        theta = Math.atan2(ptoEPos[Y]-ptoAPos[Y],ptoEPos[X]-ptoAPos[X]);
        if ( theta < 0 ) theta += 2*Math.PI; // Converte [-\pi,\pi] -> [0,2\pi]

        // Lê da imagem SVG a posição inicial do ponto C (amostra).
        ptoCPos[X] = Double.parseDouble(ptoC.getAttribute("x")); // NOI18N
        ptoCPos[Y] = Double.parseDouble(ptoC.getAttribute("y")); // NOI18N

        // Calcula a distância da amostra ao ponto (0,0): a coordenada raio.
        double distAC = Math.sqrt(Math.pow(ptoCPos[X]-ptoAPos[X],2)+Math.pow(ptoCPos[Y]-ptoAPos[Y],2));

        // Calcula o ângulo entre o eixo r e o raio que liga (0,0) à amostra.
        double phi = Math.atan2(ptoCPos[Y]-ptoAPos[Y],ptoCPos[X]-ptoAPos[X]);
        if ( phi < 0 ) phi += 2*Math.PI; // Converte [-\pi,\pi] -> [0,2\pi]
        phi = phi - theta;

        // Calcula as coordenadas dos pontos B e D.
        ptoBPos[X] = ptoAPos[X] + arcRadius * Math.cos(phi+theta);
        ptoBPos[Y] = ptoAPos[Y] + arcRadius * Math.sin(phi+theta);

        ptoDPos[X] = ptoAPos[X] + arcRadius * Math.cos(theta);
        ptoDPos[Y] = ptoAPos[Y] + arcRadius * Math.sin(theta);

        // O parâmetro fAfS vem de "large Arc Flag/Sweep Flag". É um par de parâmetros
        // utilizado na tag <path/> que desenha o arco do ângulo \phi. Veja a especificação
        // da tag em http://www.w3.org/TR/SVG11/paths.html (comando "A", de arc).
        String fAfS = new String("0,0");        
        if ((phi > 0 && phi < Math.PI) || (phi > -2*Math.PI && phi < -Math.PI)) fAfS = "1,0";

        arc.setAttribute( "d", "M" + ptoDPos[X] + "," + ptoDPos[Y] + " " +
            "A" + arcRadius + "," + arcRadius + ",0," + fAfS + "," + ptoBPos[X] + "," + ptoBPos[Y] );

        // Cria a linha paralela ao eixo x que liga a amostra ao eixo y. Isto é,
        // o segmento de reta que liga os pontos B e C.
        rLine.setAttribute("x1", "" + ptoAPos[X]); // NOI18N
        rLine.setAttribute("x2", "" + ptoCPos[X]); // NOI18N
        rLine.setAttribute("y1", "" + ptoAPos[Y]); // NOI18N
        rLine.setAttribute("y2", "" + ptoCPos[Y]); // NOI18N

        // Atualiza o rótulo da coordenada r da amostra.
        rLabel.setAttribute("x", "" + (distAC*Math.cos(phi+theta)/2 + ptoAPos[X]));
        rLabel.setAttribute("y", "" + (distAC*Math.sin(phi+theta)/2 + ptoAPos[Y]));
        rLabel.setTextContent(numberFormat.format(distAC));

        // Atualiza o rótulo da coordenada phi da amostra. A orientação do ângulo na tela é
        // inversa àquela que estamos acostumados. Por isso a conversão.
        double tmp = Math.toDegrees( phi > 0 ? 2*Math.PI-phi : -phi );
        String phiLabelText = numberFormat.format(tmp);

        // Calcula a posição angular do rótulo da coordenada ângulo da amostra.
        double phiLabelAngularPos = ( phi < 0 ? theta + phi + Math.abs(phi)/2 : theta - (2*Math.PI - phi)/2 );

        // Atualiza o rótulo da coordenada phi da amostra.
        phiLabel.setAttribute("x", "" + (ptoAPos[X] + phiLabelRadius * Math.cos(phiLabelAngularPos)));
        phiLabel.setAttribute("y", "" + (ptoAPos[Y] + phiLabelRadius * Math.sin(phiLabelAngularPos)));
        phiLabel.setTextContent(phiLabelText + "º ");

        // Calcula o offset para o posicionamento do rótulo par ordenado (r,phi) da amostra.
        Element aux = document.getElementById("pC"); // NOI18N
        ptoCLabelOffset[X] = Double.parseDouble(aux.getAttribute("width")); // NOI18N
        ptoCLabelOffset[Y] = Double.parseDouble(aux.getAttribute("height")); // NOI18N

        // Atualiza o par coordenado (r,phi) da amostra.
        xyPair.setAttribute("x", "" + (ptoCPos[X]+ptoCLabelOffset[X]/2));
        xyPair.setAttribute("y", "" + (ptoCPos[Y]-ptoCLabelOffset[Y]/2));
        xyPair.setTextContent("(" + numberFormat.format(distAC) + " , " + phiLabelText + "º )");
        
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
                    if ( theta < 0 ) theta += 2*Math.PI; // Converte [-\pi,\pi] -> [0,2\pi]
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

                // Atualiza a distância da amostra ao ponto (0,0): a coordenada raio.
                double distAC = Math.sqrt(Math.pow(ptoCPos[X]-ptoAPos[X],2)+Math.pow(ptoCPos[Y]-ptoAPos[Y],2));

                // Atualiza o ângulo entre o eixo r e o raio que liga (0,0) à amostra.
                double phi = Math.atan2(ptoCPos[Y]-ptoAPos[Y],ptoCPos[X]-ptoAPos[X]);
                if ( phi < 0 ) phi += 2*Math.PI; // Converte [-\pi,\pi] -> [0,2\pi]
                phi = phi - theta;

                // Atualiza as coordenadas dos pontos B e D.
                ptoBPos[X] = ptoAPos[X] + arcRadius * Math.cos(phi+theta);
                ptoBPos[Y] = ptoAPos[Y] + arcRadius * Math.sin(phi+theta);

                ptoDPos[X] = ptoAPos[X] + arcRadius * Math.cos(theta);
                ptoDPos[Y] = ptoAPos[Y] + arcRadius * Math.sin(theta);

                // O parâmetro fAfS vem de "large Arc Flag/Sweep Flag". É um par de parâmetros
                // utilizado na tag <path/> que desenha o arco do ângulo \phi. Veja a especificação
                // da tag em http://www.w3.org/TR/SVG11/paths.html (comando "A", de arc).
                String fAfS = new String("0,0");
                if ((phi > 0 && phi < Math.PI) || (phi > -2*Math.PI && phi < -Math.PI)) fAfS = "1,0";

                // Atualiza o arco do ângulo na cena.
                arc.setAttribute( "d", "M" + ptoDPos[X] + "," + ptoDPos[Y] + " " +
                    "A" + arcRadius + "," + arcRadius + ",0," + fAfS + "," + ptoBPos[X] + "," + ptoBPos[Y] );

                // Atualiza a linha que liga a amostra à origem do sistema de referências.
                rLine.setAttribute("x1", "" + ptoAPos[X]); // NOI18N
                rLine.setAttribute("x2", "" + ptoCPos[X]); // NOI18N
                rLine.setAttribute("y1", "" + ptoAPos[Y]); // NOI18N
                rLine.setAttribute("y2", "" + ptoCPos[Y]); // NOI18N

                // Atualiza o rótulo da coordenada r da amostra.
                rLabel.setAttribute("x", "" + (distAC*Math.cos(phi+theta)/2 + ptoAPos[X]));
                rLabel.setAttribute("y", "" + (distAC*Math.sin(phi+theta)/2 + ptoAPos[Y]));
                rLabel.setTextContent(numberFormat.format(distAC));

                // Atualiza o rótulo da coordenada phi da amostra. A orientação do ângulo na tela é
                // inversa àquela que estamos acostumados. Por isso a conversão.
                double tmp = Math.toDegrees( phi > 0 ? 2*Math.PI-phi : -phi );
                String phiLabelText = numberFormat.format(tmp);

                // Calcula a posição angular do rótulo da coordenada ângulo da amostra.
                double phiLabelAngularPos = ( phi < 0 ? theta + phi + Math.abs(phi)/2 : theta - (2*Math.PI - phi)/2 );

                // Atualiza o rótulo da coordenada phi da amostra.
                phiLabel.setAttribute("x", "" + (ptoAPos[X] + phiLabelRadius * Math.cos(phiLabelAngularPos)));
                phiLabel.setAttribute("y", "" + (ptoAPos[Y] + phiLabelRadius * Math.sin(phiLabelAngularPos)));
                phiLabel.setTextContent(phiLabelText + "º ");

                // Atualiza o par coordenado (r,phi) da amostra.
                xyPair.setAttribute("x", "" + (ptoCPos[X]+ptoCLabelOffset[X]/2));
                xyPair.setAttribute("y", "" + (ptoCPos[Y]-ptoCLabelOffset[Y]/2));
                xyPair.setTextContent("(" + numberFormat.format(distAC) + " , " + phiLabelText + "º )");
            }
        }, false);

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
        setResizable(false);

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
                new PolarCoordinates2D().setVisible(true);
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

    private Element ptoA, ptoC, ptoE, referenceFrame, xyAxis, xyPair, ptoATip, ptoETip, arc, rLine, rLabel, phiLabel;
    private boolean ptoASelected = false,
                    ptoCSelected = false,
                    ptoESelected = false;

    private double[] mousePos = {0,0};

    private double[] SVGViewBoxSize = {40,40};
    private double arcRadius = 2;
    private double phiLabelRadius = 3.2;
    private double theta = 0;

    private double[] ptoAPos = {0,0}, ptoBPos = {0,0}, ptoCPos = {0,0}, ptoDPos = {0,0};
    private NumberFormat numberFormat;

    private double[] ptoCLabelOffset = {0,0};
}
