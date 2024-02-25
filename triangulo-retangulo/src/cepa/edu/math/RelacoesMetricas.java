/*
 * Author: Ivan R. Pagnossin (irpagnossin@usp.br) at CEPA (Centro de Ensino e Pesquisa Aplicada).
 *
 * This material, by I. R. Pagnossin and CEPA (cepa.if.usp.br), is licensed under Creative Commons
 * 2.5 Brazil (attribution: noncommercial share alike 2.5 Brazil).
 *
 */
package cepa.edu.math;

import cepa.edu.util.LogoPanel;
import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;

/**
 * @author I. R. Pagnossin - (irpagnossin@usp.br)
 */
public class RelacoesMetricas extends javax.swing.JFrame {

    public RelacoesMetricas() {

        targets = new Element[N_TARGETS];
        lineTriggers = new EventTarget[N_LINE_TRIGGERS];
        equationTriggers = new EventTarget[N_EQUATION_TRIGGERS];
        angleTriggers = new EventTarget[N_ANGLE_TRIGGERS];

        setupGUI();
        setupSVG();
        loadSVGElements();
        registerListeners();
    }

    // Configura a interface do usuário (GUI)
    private void setupGUI () {

        // Define look & feel
        try{
        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        	SwingUtilities.updateComponentTreeUI(this);
        }
        catch( Exception e ){/*Nada*/}

        // Configura os componentes criados automaticamente pelo Matisse.
        initComponents();

        // Insere o logotipo do CEPA no glass pane (invisível).
        // ---------------------------------------------------------------------
        setGlassPane(new LogoPanel(LOGO_FILE)); // NOI18N
    }

    // Carrega o arquivo SVG, configura a cena e adiciona-a ao svgPanel.
    private void setupSVG () {

		try {
		    String parser = XMLResourceDescriptor.getXMLParserClassName();
		    SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);

            ClassLoader classLoader = this.getClass().getClassLoader();
            URL url = classLoader.getResource(SVG_FILE);
            String uri = url.toURI().toString();

		    document = (SVGDocument) factory.createDocument(uri);
		}

		catch (IOException ex) {
            Logger.getLogger(RelacoesMetricas.class.getName()).log(Level.SEVERE, "Não foi possível ler o arquivo " + SVG_FILE, ex);
            System.exit(-1);
        }        catch (URISyntaxException ex) {
            Logger.getLogger(RelacoesMetricas.class.getName()).log(Level.SEVERE, "Não foi possível ler o arquivo " + SVG_FILE, ex);
            System.exit(-1);
        }

        canvas = new JSVGCanvas();
		canvas.setDocumentState (JSVGCanvas.ALWAYS_DYNAMIC); // Torna dinâmico o canvas.
		canvas.setDocument (document); // Associa a cena SVG (propriedade scene.file) ao canvas.
		canvas.setEnableImageZoomInteractor(false);
		canvas.setEnablePanInteractor(false); // Desabilita a opção de arrastar a cena SVG.
		canvas.setEnableRotateInteractor(false); // Desabilita a opção de rotacionar a cena SVG.
		canvas.setEnableZoomInteractor(false); // Desabilita a opção de ampliar/reduzir a cena SVG.

        svgPanel.setLayout(new BorderLayout());
        svgPanel.add(canvas,BorderLayout.CENTER);
    }

    // Carrega os elementos relevantes da cena SVG e define alguns parâmetros com base neles.
    private void loadSVGElements () {

        // Lê do arquivo SVG os triggers de eventos das linhas ou lados do triângulo (EventTarget)
        for (short i = 0; i < N_LINE_TRIGGERS; i++) {
            lineTriggers[i] = (EventTarget) document.getElementById(LINE_TRIGGERS_ID[i]);
        }

        // Lê do arquivo SVG os triggers de eventos dos ângulos do triângulo (EventTarget)
        for (short i = 0; i < N_ANGLE_TRIGGERS; i++) {
            angleTriggers[i] = (EventTarget) document.getElementById(ANGLE_TRIGGERS_ID[i]);
        }

        // Lê do arquivo SVG os triggers de eventos das relações (equações) do triângulo (EventTarget)
        for (short i = 0; i < N_EQUATION_TRIGGERS; i++) {
            equationTriggers[i] = (EventTarget) document.getElementById(EQUATION_TRIGGERS_ID[i]);
        }

        // Lê do arquivo SVG os alvos das ações do triggers (Element).
        for (short i = 0; i < N_TARGETS; i++) {
            targets[i] = document.getElementById(TARGETS_ID[i]);
            targets[i].setAttribute("opacity", "0");
        }
    }

    // Registra os observadores (ou "ouvidores") de eventos.
    private void registerListeners () {

        // Os ouvidores dos ângulos.
        //----------------------------------------------------------------------
        final Element[][] angleTargets = {
            {targets[ANGLE_B]},
            {targets[ANGLE_C]},
            {targets[ANGLE_D_LEFT]},
            {targets[ANGLE_D_RIGHT]},
        };

        for (short i = 0; i < angleTargets.length; i++) {

            final MouseOverListener mouseOverListener = new MouseOverListener();
            final MouseOutListener mouseOutListener = new MouseOutListener();

            angleTriggers[i].addEventListener("mouseover", mouseOverListener, false);
            angleTriggers[i].addEventListener("mouseout", mouseOutListener, false);

            for (short j = 0; j < angleTargets[i].length; j++ ) {

                mouseOverListener.addElement(angleTargets[i][j]);
                mouseOutListener.addElement(angleTargets[i][j]);

//                final int I = i, J = j;
//                angleTriggers[i].addEventListener("click", new EventListener(){
//
//                    public void handleEvent(Event evt) {
//                        mouseOverListener.toggleLock(angleTargets[I][J]);
//                        mouseOutListener.toggleLock(angleTargets[I][J]);
//                    }
//                }, false);
            }
        }

        // Os ouvidores dos segmentos de retas (linhas).
        //----------------------------------------------------------------------
        final Element[][] lineTargets = {
            {targets[SIDE_B]}, // Alvos do lado B
            {targets[SIDE_C]}, // Alvos do lado C
            {targets[SIDE_H]}, // Alvos do lado H
            {targets[SIDE_M],targets[SIDE_A]}, // Alvos do lado M
            {targets[SIDE_N],targets[SIDE_A]}, // Alvos do lado N
        };


        for (short i = 0; i < lineTargets.length; i++) {

            final MouseOverListener mouseOverListener = new MouseOverListener();
            final MouseOutListener mouseOutListener = new MouseOutListener();

            lineTriggers[i].addEventListener("mouseover", mouseOverListener, false);
            lineTriggers[i].addEventListener("mouseout", mouseOutListener, false);

            for (short j = 0; j < lineTargets[i].length; j++ ) {
                mouseOverListener.addElement(lineTargets[i][j]);
                mouseOutListener.addElement(lineTargets[i][j]);

//                final int I = i, J = j;
//                lineTriggers[i].addEventListener("click", new EventListener(){
//
//                    public void handleEvent(Event evt) {
//                        mouseOverListener.toggleLock(lineTargets[I][J]);
//                        mouseOutListener.toggleLock(lineTargets[I][J]);
//                    }
//                }, false);
            }
        }

        // Os ouvidores das equações (relações métricas).
        //----------------------------------------------------------------------
        final Element[][] equationTargets = {
            {targets[SIDE_A],targets[SIDE_B],targets[SIDE_C]}, // Alvos da equação 1.
            {targets[SIDE_A],targets[SIDE_B],targets[SIDE_N]}, // Alvos da equação 2.
            {targets[SIDE_A],targets[SIDE_C],targets[SIDE_M]}, // Alvos da equação 3.
            {targets[SIDE_H],targets[SIDE_M],targets[SIDE_N]}, // Alvos da equação 4.
            {targets[SIDE_A],targets[SIDE_B],targets[SIDE_C],targets[SIDE_H]} // Alvos da equação 5.
        };

        for (short i = 0; i < equationTargets.length; i++) {

            final MouseOverListener mouseOverListener = new MouseOverListener();
            final MouseOutListener mouseOutListener = new MouseOutListener();

            equationTriggers[i].addEventListener("mouseover", mouseOverListener, false);
            equationTriggers[i].addEventListener("mouseout", mouseOutListener, false);

            for (short j = 0; j < equationTargets[i].length; j++) {
                mouseOverListener.addElement(equationTargets[i][j]);
                mouseOutListener.addElement(equationTargets[i][j]);

//                final int I = i, J = j;
//                equationTriggers[i].addEventListener("click", new EventListener(){
//
//                    public void handleEvent(Event evt) {
//                        hH.toggleLock(equationsTargets[I][J]);
//                    }
//                }, false);
            }
        }
    }

    
    private JSVGCanvas canvas; // O canvas SVG
    private Document document; // O documento SVG
    
    private EventTarget[] angleTriggers;
    private final int N_ANGLE_TRIGGERS = 4;
    private final String[] ANGLE_TRIGGERS_ID = {
        "angle.B",
        "angle.C",
        "angle.D.left",
        "angle.D.right"
    };

    private EventTarget[] equationTriggers;
    private final int N_EQUATION_TRIGGERS = 5;
    private final String[] EQUATION_TRIGGERS_ID = {
        "relation.1.sensitive.area",
        "relation.2.sensitive.area",
        "relation.3.sensitive.area",
        "relation.4.sensitive.area",
        "relation.5.sensitive.area",
    };

    private EventTarget[] lineTriggers;
    private final int N_LINE_TRIGGERS = 5;
    private final String[] LINE_TRIGGERS_ID = {
        "side.b",
        "side.c",
        "side.h",
        "side.m",
        "side.n"
    };

    private final int N_TARGETS = 14;
    private Element[] targets;
    private final String[] TARGETS_ID = {
        "label.a",
        "label.b",
        "label.c",
        "label.h",
        "label.m",
        "label.n",
        "angle.B",
        "angle.C",
        "angle.D.left",
        "angle.D.right",
        "label.A",
        "label.B",
        "label.C",
        "label.D"
    };

    private final int
        SIDE_A = 0,
        SIDE_B = 1,
        SIDE_C = 2,
        SIDE_H = 3,
        SIDE_M = 4,
        SIDE_N = 5,
        ANGLE_B = 6,
        ANGLE_C = 7,
        ANGLE_D_LEFT = 8,
        ANGLE_D_RIGHT = 9,
        VERTEX_A = 10,
        VERTEX_B = 11,
        VERTEX_C = 12,
        VERTEX_D = 13;

    private final String SVG_FILE = new String("resources/triangulo-retangulo.svg"); // Arquivo SVG.
    private final String LOGO_FILE = new String("resources/cepa.jpg"); // Logotipo do CEPA.

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        svgPanel = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        aboutMenu = new javax.swing.JMenu();
        aboutOption = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Relações métricas do triângulo-retângulo");
        setBackground(new java.awt.Color(117, 197, 240));
        setForeground(new java.awt.Color(117, 197, 240));
        setResizable(false);

        svgPanel.setBackground(new java.awt.Color(255, 255, 255));
        svgPanel.setPreferredSize(new java.awt.Dimension(700, 329));

        javax.swing.GroupLayout svgPanelLayout = new javax.swing.GroupLayout(svgPanel);
        svgPanel.setLayout(svgPanelLayout);
        svgPanelLayout.setHorizontalGroup(
            svgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 700, Short.MAX_VALUE)
        );
        svgPanelLayout.setVerticalGroup(
            svgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 329, Short.MAX_VALUE)
        );

        aboutMenu.setText("Sobre");

        aboutOption.setText("Sobre");
        aboutOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutOptionActionPerformed(evt);
            }
        });
        aboutMenu.add(aboutOption);

        jMenuBar1.add(aboutMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(svgPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(svgPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void aboutOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutOptionActionPerformed
        this.getGlassPane().setVisible(true);
    }//GEN-LAST:event_aboutOptionActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RelacoesMetricas().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu aboutMenu;
    private javax.swing.JMenuItem aboutOption;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel svgPanel;
    // End of variables declaration//GEN-END:variables
}
