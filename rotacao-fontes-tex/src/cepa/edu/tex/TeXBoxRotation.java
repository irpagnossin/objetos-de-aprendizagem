package cepa.edu.tex;

import cepa.edu.util.LogoPanel;
import cepa.edu.util.Util;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.ResourceBundle;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.batik.dom.events.DOMMouseEvent;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
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

/**
 *
 * @author irpagnossin
 */
public class TeXBoxRotation extends javax.swing.JFrame {

    public TeXBoxRotation() {

        InputStream stream = getClass().getClassLoader().getResourceAsStream("app.properties"); // NOI18N
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } catch (IOException ex) { Logger.getLogger(TeXBoxRotation.class.getName()).log(Level.SEVERE, null, ex); System.exit(-1);    }

        startIcon = Util.getIcon(properties.getProperty("start.icon")); // NOI18N
        pauseIcon = Util.getIcon(properties.getProperty("pause.icon")); // NOI18N
        stopIcon  = Util.getIcon(properties.getProperty("stop.icon")); // NOI18N

        initComponents();
        jMenuBar1.add(new JPanel());
        jMenuBar1.add(languageMenu);

        // Define look & feel
        try{
        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        	SwingUtilities.updateComponentTreeUI(this);
        }
        catch( Exception e ){/*Nada*/}

        /***********************************************************************
         Carrega o arquivo SVG para o JPanel.
         **********************************************************************/        
		try {

		    String parser = XMLResourceDescriptor.getXMLParserClassName();
		    SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);

            ClassLoader classLoader = this.getClass().getClassLoader();
            URL url = classLoader.getResource(properties.getProperty("scene.file")); // NOI18N
            String uri = url.toURI().toString();

		    document = (SVGDocument) f.createDocument(uri);
		}
		catch (IOException ex) { Logger.getLogger(TeXBoxRotation.class.getName()).log(Level.SEVERE, null, ex); System.exit(-1); }
        catch (URISyntaxException ex) { Logger.getLogger(TeXBoxRotation.class.getName()).log(Level.SEVERE, null, ex); System.exit(-1);}

		canvas.setDocumentState (JSVGCanvas.ALWAYS_DYNAMIC); // Torna dinâmico o canvas.
		canvas.setDocument (document); // Associa a cena SVG (propriedade scene.file) ao canvas.
		canvas.setEnableImageZoomInteractor(false); // TODO: desabilita o quê?
		canvas.setEnablePanInteractor(false); // Desabilita a opção de arrastar a cena SVG.
		canvas.setEnableRotateInteractor(false); // Desabilita a opção de rotacionar a cena SVG.
		canvas.setEnableZoomInteractor(false); // Desabilita a opção de ampliar/reduzir a cena SVG.

        SVGPanel.setLayout(new BorderLayout());
        SVGPanel.add(canvas,BorderLayout.CENTER);

        setGlassPane(new LogoPanel(properties.getProperty("logo.file"))); // NOI18N

        // ------------------------------------------------
        // ----- Início da configuração do menu de idiomas.
        languageObservable = new LanguageObservable();
        languageObservable.addObserver(new LanguageObserver());

        idiom = ResourceBundle.getBundle( properties.getProperty("language.bundle"), new Locale("en", "US")); // NOI18N

        languageMenu.setIcon(Util.getIcon(properties.getProperty("language.menu.icon"))); // NOI18N
        availableLocales = Util.getLocales(properties.getProperty("available.locales")); // NOI18N

        radioButton = new JRadioButtonMenuItem[availableLocales.length];
        ButtonGroup group = new ButtonGroup();

        for (int i = 0; i < availableLocales.length; i++) {
            final int index = i;

            radioButton[i] = new JRadioButtonMenuItem();
            radioButton[i].setText(availableLocales[i].getDisplayLanguage(availableLocales[i]) + " (" + availableLocales[i].getCountry() + ")"); // NOI18N
            radioButton[i].addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    idiom = ResourceBundle.getBundle("LanguageBundle", availableLocales[index]); // NOI18N
                    languageObservable.notifyObservers();
                }
            });

            // Se o item (Locale) for igual ao padrão, utiliza-o.
            if (availableLocales[i].equals(Locale.getDefault())) {
                idiom = ResourceBundle.getBundle(properties.getProperty("language.bundle"), availableLocales[i]); // NOI18N
                radioButton[i].setSelected(true);
            }

            group.add(radioButton[i]);
            languageMenu.add(radioButton[i]);
        }
        // ----- Fim da configuração do menu de idiomas.
        // ---------------------------------------------

        /***********************************************************************
         Configuração dos elementos da cena SVG.
         **********************************************************************/

        // Carrega os elementos SVG relevantes.
        SVGElement root = (SVGElement) ((SVGDocument)document).getRootElement();
		box[0]   = document.getElementById("box.0");  // NOI18N
        box[1]   = document.getElementById("box.1");   // NOI18N
        baseLine = document.getElementById("base.line");     // NOI18N
        axis     = document.getElementById("rotation.axis"); // NOI18N
        refPt[0] = document.getElementById("ref.pt.0"); // NOI18N
        refPt[1] = document.getElementById("ref.pt.1"); // NOI18N
        boxGroup = document.getElementById("box.0.group"); // NOI18N

        baseLineLabel = document.getElementById("baseline.label"); // NOI18N
        refPtLabelText = document.getElementById("ref.pt.label"); // NOI18N
        rotationAxisLabelText = document.getElementById("rotation.axis.label.text"); // NOI18N

        baseLineLabel.setTextContent(idiom.getString("baseline.label")); // NOI18N

        // Lê da imagem SVG as dimensões da cena SVG e define o viewBox com base nelas.
        SVGViewBoxSize[X] = Double.parseDouble(root.getAttribute("width")); // NOI18N
        SVGViewBoxSize[Y] = Double.parseDouble(root.getAttribute("height")); // NOI18N
        root.setAttribute( "viewBox", ulc[X] + " " + ulc[Y] + " " + SVGViewBoxSize[X] + " " + SVGViewBoxSize[Y] ); // NOI18N
        
        // Determina as coordenadas dos vértices da caixa original (necessário para calcular os vértices
        // rotacionados, necessários para calcular a caixa circunscrita).
        originalCorners[0][X] = Double.parseDouble(box[0].getAttribute("x")); // NOI18N
        originalCorners[0][Y] = Double.parseDouble(box[0].getAttribute("y")); // NOI18N

        double boxWidth  = Double.parseDouble(box[0].getAttribute("width")), // NOI18N
               boxHeight = Double.parseDouble(box[0].getAttribute("height")); // NOI18N

        originalCorners[1][X] = originalCorners[0][X];
        originalCorners[1][Y] = originalCorners[0][Y] + boxHeight;
        originalCorners[2][X] = originalCorners[0][X] + boxWidth;
        originalCorners[2][Y] = originalCorners[0][Y] + boxHeight;
        originalCorners[3][X] = originalCorners[0][X] + boxWidth;
        originalCorners[3][Y] = originalCorners[0][Y];

        // Inicialmente, coloca a caixa rotacionada sobre a original.
        box[1].setAttribute("x", box[0].getAttribute("x")); // NOI18N
        box[1].setAttribute("y", box[0].getAttribute("y")); // NOI18N
        box[1].setAttribute("width", String.valueOf(boxWidth)); // NOI18N
        box[1].setAttribute("height", String.valueOf(boxHeight)); // NOI18N


        baseLinePos = Double.parseDouble(baseLine.getAttribute("y1")); // NOI18N

        refPt[1].setAttribute("cx", box[1].getAttribute("x")); // NOI18N
        refPt[1].setAttribute("cy", String.valueOf(baseLinePos)); // NOI18N


        //----------------------------------------------------------------------
        EventTarget rotationAxisTarget = (EventTarget) axis;
        rotationAxisTarget.addEventListener("click", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                if (!animationOn) rotationAxisSelected = !rotationAxisSelected;
            }
        }, false);
        rotationAxisTarget.addEventListener("mouseover", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                rotationAxisLabelText.setTextContent(idiom.getString("rotation.axis.label")); // NOI18N
            }
        }, false);
        rotationAxisTarget.addEventListener("mouseout", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                rotationAxisLabelText.setTextContent(""); // NOI18N
            }
        }, false);
        //----------------------------------------------------------------------
        EventTarget refPt0EventTarget = (EventTarget) refPt[0];
        refPt0EventTarget.addEventListener("mouseover", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                refPtLabelText.setTextContent(idiom.getString("reference.point.label")); // NOI18N
            }
        }, false);
        refPt0EventTarget.addEventListener("mouseout", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                refPtLabelText.setTextContent(""); // NOI18N
            }
        }, false);
        //----------------------------------------------------------------------
        EventTarget refPt1EventTarget = (EventTarget) refPt[1];
        refPt1EventTarget.addEventListener("mouseover", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                refPtLabelText.setTextContent(idiom.getString("reference.point.label")); // NOI18N
            }
        }, false);
        refPt1EventTarget.addEventListener("mouseout", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                refPtLabelText.setTextContent(""); // NOI18N
            }
        }, false);
        //----------------------------------------------------------------------
        rootEventTarget = (EventTarget) root;
        rootEventTarget.addEventListener("mousemove", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {

                Dimension d = SVGPanel.getSize();
				double margin = (d.getWidth() - d.getHeight()) / 2;

                DOMMouseEvent mouseEvent = (DOMMouseEvent) evt;

                mousePos[X] = ulc[X] + (mouseEvent.getClientX() - Math.max(0,margin)) * SVGViewBoxSize[X] / Math.min(d.getHeight(), d.getWidth());
                mousePos[Y] = ulc[Y] + (mouseEvent.getClientY() + Math.min(0,margin)) * SVGViewBoxSize[Y] / Math.min(d.getHeight(), d.getWidth());

                if (rotationAxisSelected) {
                    if (rotationHandler.getAngle() != 0) angleSpinner.setValue(0);
                    axis.setAttribute("x", String.valueOf(mousePos[X])); // NOI18N
                    axis.setAttribute("y", String.valueOf(mousePos[Y])); // NOI18N
                }
            }
        }, false);


        // Obtain the Window reference when it becomes available
		canvas.addSVGLoadEventDispatcherListener (
			new SVGLoadEventDispatcherAdapter () {            
                @Override
		        public void svgLoadEventDispatchStarted (
		               SVGLoadEventDispatcherEvent e) {

		        window = canvas.getUpdateManager ().
		           getScriptingEnvironment ().createWindow ();
		       }
		    }
		);

        rotationHandler = new RotationHandler();
        rootEventTarget.addEventListener("SVGLoad", rotationHandler, false); // NOI18N
        //----------------------------------------------------------------------
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        SVGPanel = new JPanel();
        startButton = new JButton();
        angleSpinner = new JSpinner();
        angleLabel = new JLabel();
        stopButton = new JButton();
        jMenuBar1 = new JMenuBar();
        aboutMenu = new JMenu();
        aboutOption = new JMenuItem();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        SVGPanel.setBackground(new Color(255, 255, 255));
        SVGPanel.setBorder(BorderFactory.createEtchedBorder());
        SVGPanel.setPreferredSize(new Dimension(400, 250));

        GroupLayout SVGPanelLayout = new GroupLayout(SVGPanel);
        SVGPanel.setLayout(SVGPanelLayout);
        SVGPanelLayout.setHorizontalGroup(
            SVGPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
        );
        SVGPanelLayout.setVerticalGroup(
            SVGPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
        );

        startButton.setIcon(startIcon);
        startButton.setPreferredSize(new Dimension(35, 25));
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        angleSpinner.setMinimumSize(new Dimension(50, 20));
        angleSpinner.setPreferredSize(new Dimension(50, 20));
        angleSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                angleSpinnerStateChanged(evt);
            }
        });

        angleLabel.setText("Ângulo (º):");

        stopButton.setIcon(stopIcon);
        stopButton.setPreferredSize(new Dimension(35, 25));
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        aboutMenu.setText("Sobre");

        aboutOption.setText("Sobre");
        aboutOption.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                aboutOptionActionPerformed(evt);
            }
        });
        aboutMenu.add(aboutOption);

        jMenuBar1.add(aboutMenu);

        setJMenuBar(jMenuBar1);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
                    .addComponent(SVGPanel, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(stopButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(startButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(angleLabel)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(angleSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SVGPanel, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(stopButton, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                    .addComponent(startButton, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(angleSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(angleLabel)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void angleSpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_angleSpinnerStateChanged
        canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(new Runnable(){

            public void run() {
                int angle = (Integer) angleSpinner.getValue() % 360;
                
                angleSpinner.setValue(angle);
                rotate(angle);
                rotationHandler.setAngle(angle);
            }
        });
}//GEN-LAST:event_angleSpinnerStateChanged

    private void startButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        animationOn = !animationOn;
        if (animationOn) startButton.setIcon(pauseIcon);
        else startButton.setIcon(startIcon);
}//GEN-LAST:event_startButtonActionPerformed

    private void stopButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        animationOn = false;
        rotationHandler.setAngle(0);
        rotate(0);
        angleSpinner.setValue(0);
        startButton.setIcon(startIcon);
}//GEN-LAST:event_stopButtonActionPerformed

    private void aboutOptionActionPerformed(ActionEvent evt) {//GEN-FIRST:event_aboutOptionActionPerformed
        this.getGlassPane().setVisible(true);
}//GEN-LAST:event_aboutOptionActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TeXBoxRotation().setVisible(true);
            }
        });
    }

    private void rotate ( final double angle ) {

        double[] center = {
            Double.parseDouble(axis.getAttribute("x")), // NOI18N
            Double.parseDouble(axis.getAttribute("y")) // NOI18N
        };

        boxGroup.setAttribute("transform", "rotate(" + -angle + "," + center[X] + "," + center[Y] + ")"); // NOI18N

        double xMin = 0, xMax = 0, yMin = 0, yMax = 0;

        for (short i = 0; i < 4; i++) {
            rotatedCorners[i] = R(originalCorners[i],angle,center);

            // Determina a menor e a maior coordenada x e y dos vértices da caixa em rotação.
            if (i == 0) {
                xMin = xMax = rotatedCorners[i][X];
                yMin = yMax = rotatedCorners[i][Y];
            } else {
                xMin = Math.min(xMin, rotatedCorners[i][X]);
                yMin = Math.min(yMin, rotatedCorners[i][Y]);
                yMin = Math.min(yMin, baseLinePos);

                xMax = Math.max(xMax, rotatedCorners[i][X]);
                yMax = Math.max(yMax, rotatedCorners[i][Y]);
                yMax = Math.max(yMax, baseLinePos);
            }
        }

        box[1].setAttribute("x", String.valueOf(xMin)); // NOI18N
        box[1].setAttribute("y", String.valueOf(yMin)); // NOI18N
        box[1].setAttribute("width", String.valueOf(xMax-xMin)); // NOI18N
        box[1].setAttribute("height", String.valueOf(yMax-yMin)); // NOI18N

        refPt[1].setAttribute("cx", box[1].getAttribute("x")); // NOI18N
        refPt[1].setAttribute("cy", baseLine.getAttribute("y1")); // NOI18N

    }

    // Retorna as coordenadas (x',y'), obtidas da rotação de (x,y) por um dado ângulo.
    private double[] R (final double[] r, final double angle, final double[] center) {
        return new double[]{
            + (r[X] - center[X]) * Math.cos(Math.toRadians(angle)) + (r[Y] - center[Y]) * Math.sin(Math.toRadians(angle)) + center[X],
            - (r[X] - center[X]) * Math.sin(Math.toRadians(angle)) + (r[Y] - center[Y]) * Math.cos(Math.toRadians(angle)) + center[Y]
        };
    }

    private class RotationHandler implements EventListener {

        private int A = 0, dA = 1;

        public void handleEvent(Event evt) {
            window.setInterval (new Runnable() {

                public void run() {
                    if (!animationOn) return;

                    A = (A + dA) % 360;
                    
                    angleSpinner.setValue(A);
                }
            }, dt);
        }

        public void setAngle (final int angle) {
            A = angle % 360;
        }

        public int getAngle () { return A; }
    }

    private class LanguageObserver implements Observer {
        public void update(Observable o, Object arg) {
            aboutMenu.setText(idiom.getString("about.menu.title")); // NOI18N
            aboutOption.setText(idiom.getString("about.option.label")); // NOI18N
            angleLabel.setText(idiom.getString("angle.spinner.label")); // NOI18N
            baseLineLabel.setTextContent(idiom.getString("baseline.label")); // NOI18N
        }
    }

    private class LanguageObservable extends Observable {
        @Override
        public void notifyObservers(){
            setChanged();
            super.notifyObservers();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel SVGPanel;
    private JMenu aboutMenu;
    private JMenuItem aboutOption;
    private JLabel angleLabel;
    private JSpinner angleSpinner;
    private JMenuBar jMenuBar1;
    private JButton startButton;
    private JButton stopButton;
    // End of variables declaration//GEN-END:variables

    private JMenu languageMenu = new JMenu();
    private JRadioButtonMenuItem[] radioButton;

    private JSVGCanvas canvas = new JSVGCanvas();
    private Document document; // The SVG document
    private Window window;

    private Observable languageObservable;
    private Locale[] availableLocales;
    private ResourceBundle idiom;
    private double[] SVGViewBoxSize = {30,30}, ulc = { 0, 0};;

    private Element baseLine, baseLineLabel, axis, boxGroup, refPtLabelText, rotationAxisLabelText;
    private Element[] box = new Element[2], refPt = new Element[2];
    private static final short X = 0, Y = 1;
    private boolean rotationAxisSelected = false;
    private double[] mousePos = {0,0};

    private double[][] originalCorners = new double[4][2], rotatedCorners = new double[4][2];

    private double baseLinePos = 0;
    private static long dt = 30;

    private boolean animationOn = false;

    private RotationHandler rotationHandler;
    private EventTarget rootEventTarget;

    private ImageIcon startIcon, pauseIcon, stopIcon;
}
