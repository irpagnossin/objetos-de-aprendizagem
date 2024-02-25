/*
 * Author: Ivan R. Pagnossin (irpagnossin@usp.br) at CEPA (Centro de Ensino e Pesquisa Aplicada).
 *
 * This material, by I. R. Pagnossin and CEPA (cepa.if.usp.br), is licensed under Creative Commons
 * 2.5 Brazil (attribution: noncommercial share alike 2.5 Brazil).
 *
 */
// TODO: repensar o arraste das células. Quando estão uma sobre as outras pode ficar ruim a interação.
package cepa.edu.math;

import cepa.edu.util.LogoPanel;
import cepa.edu.util.svg.SVGViewBox;
import java.awt.BorderLayout;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.ext.awt.geom.Polygon2D;
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
 * @author I. R. Pagnossin - irpagnossin (at) usp (dot) br
 */
public class Pitagoras extends javax.swing.JFrame {

    public Pitagoras() {

        cell = new Element[N];
        cellSelected = new boolean[N];
        cellEventTarget = new EventTarget[N];
        pt = new Point2D.Double[2*N];

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
            Logger.getLogger(Pitagoras.class.getName()).log(Level.SEVERE, "Não foi possível ler o arquivo " + SVG_FILE, ex);
            System.exit(-1);
        }
        catch (URISyntaxException ex) {
            Logger.getLogger(Pitagoras.class.getName()).log(Level.SEVERE, "Não foi possível ler o arquivo " + SVG_FILE, ex);
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

        // Referências para as células arrastáveis da cena SVG.
        for (short i = 0; i < N; i++) {
            cell[i] = document.getElementById(CELL_BASENAME + (i+1));
        }

        // Lê do arquivo SVG as coordenadas dos centros das células-da-grade.
        for (short i = 0; i < 2*N; i++) {
            Element e = document.getElementById(CELLPOS_BASENAME + (i+1));
            pt[i] = new Point2D.Double(Double.parseDouble(e.getAttribute("x")),Double.parseDouble(e.getAttribute("y"))); // NOI18N
        }

        // Referência para a grade de orientação (áreas associadas aos lados do triângulo)
        grid = document.getElementById("grid");

        // Referência para o conjunto de células arrastáveis.
        cellsGroup = document.getElementById("cells.group");

        //square5x5 = document.getElementById("square5x5"); // NOI18N

        // Referência para o elemento raiz da cena SVG.
        root = (SVGElement) ((SVGDocument)document).getRootElement();

        // Referência para a camada suporte das células (transparente; ajuda no tratamento dos eventos do mouse).
        background = document.getElementById("background");

        // Lê do arquivo SVG as coordenadas e dimensões da view-box.
        viewbox = new SVGViewBox(root.getAttribute("viewBox")); // NOI18N

        float[] xpoints = new float[]{6,9,5,2};
        float[] ypoints = new float[]{9.5f,5.5f,2.5f,6.5f};
        polygon = new Polygon2D(xpoints,ypoints,4);
    }

    // Registra os observadores (ou "ouvidores") de eventos.
    private void registerListeners () {

        MouseDownListener mouseDownListener = new MouseDownListener();
        MouseUpListener mouseUpListener = new MouseUpListener();
        MouseMoveListener mouseMoveListener = new MouseMoveListener();

        ((EventTarget) background).addEventListener("mousemove", mouseMoveListener, false);
        ((EventTarget) root).addEventListener("mouseup", mouseUpListener, false);

        for (short i = 0; i < N; i++) {
            cellEventTarget[i] = (EventTarget) cell[i];
            cellEventTarget[i].addEventListener("mousedown", mouseDownListener, false);
            cellEventTarget[i].addEventListener("mouseup", mouseUpListener, false);
            cellEventTarget[i].addEventListener("mousemove", mouseMoveListener, false);
        }
    }

    // Controla os eventos associados a pressionar o botão esquerdo do mouse sobre uma célula.
    private class MouseDownListener implements EventListener {

        public void handleEvent(Event evt) {
            if (!cellsOption.isSelected()) return;

            for (short i = 0; i < N; i++) {
                if (evt.getCurrentTarget() == cellEventTarget[i]) {
                    cellSelected[i] = true;

                    try {
                        dx = getMousePos(X) - Double.parseDouble(cell[i].getAttribute("x"));
                        dy = getMousePos(Y) - Double.parseDouble(cell[i].getAttribute("y"));
                    } catch (Exception ex) {
                        dx = dy = 0;
                    }
                }
            }
        }
    }

    // Controla os eventos associados a liberar o botão esquerdo do mouse.
    private class MouseUpListener implements EventListener {
        public void handleEvent(Event evt) {
            if (!cellsOption.isSelected()) return;

            for (short i = 0; i < N; i++) cellSelected[i] = false;
        }
    }

    // Controla os eventos associados ao arrastar de uma célula.
    private class MouseMoveListener implements EventListener {

        public void handleEvent(Event evt) {
            if (!cellsOption.isSelected()) return;

            // Obtém a posição do mouse na cena.
            Point2D.Double cellPos = null;
            try {
                cellPos = new Point2D.Double(getMousePos(X)-dx,getMousePos(Y)-dy);
            } catch (Exception ex) {
                return;
            }


            for (short index = 0; index < N; index++ ) {
                if (cellSelected[index]) {

                    // Produz o efeito de atração (snap).
                    for (short i = 0; i < 2*N; i++) {
                        if (distance(cellPos, pt[i]) < SNAP_DISTANCE) {
                            cellPos = (Point2D.Double) pt[i].clone();
                            break;
                        }
                    }

                    cell[index].setAttribute("x",String.valueOf(cellPos.getX()));
                    cell[index].setAttribute("y",String.valueOf(cellPos.getY()));

                    if (polygon.contains(cellPos)) cell[index].setAttribute("transform",
                        "rotate(" + -ANGLE + "," + cell[index].getAttribute("x") + "," + cell[index].getAttribute("y") + ")");
                    else cell[index].removeAttribute("transform");
                }
            }
        }
    }

    // Mapeia a posição do mouse na tela para a cena SVG.
    private double getMousePos ( final short axis ) throws Exception {
        // A razão entre as dimensões do JPanel e do view-box, necessário para mapear a posição do mouse na tela para g cena SVG.
        double ratio = Math.min(canvas.getHeight()/viewbox.getHeight(),canvas.getWidth()/viewbox.getWidth());

        Point2D point = canvas.getMousePosition();
        if (point == null) throw new Exception("Mouse is outside canvas.");

        double ans = 0;
        if (axis == X) ans = viewbox.getX() + point.getX()/ratio;
        else if (axis == Y) ans = viewbox.getY() + point.getY()/ratio;

        return ans;
    }

    // Calcula a distância entre dois pontos na cena.
    private double distance (Point2D.Double pt1, Point2D.Double pt2) {
        return Math.sqrt(Math.pow(pt1.getX()-pt2.getX(),2)+Math.pow(pt1.getY()-pt2.getY(),2));
    }

    
    private SVGViewBox viewbox; // Encapsula os parâmetros da view-box (ou view-port).

    private JSVGCanvas canvas; // O canvas SVG
    private Document document; // O documento SVG
    private Element root, // Elemento-raiz do arquivo SVG.
                    background, // "Tabuleiro" (transparente) sobre o qual são "apoiadas" as células.
                    grid, // A grade
                    cellsGroup, // O conjunto das 25 células.
                    square5x5; // A área associada à hipotenusa.
    private Element[] cell; // As 25 células arrastáveis.
    private EventTarget[] cellEventTarget; // Alvos dos eventos do mouse.

    private Polygon2D polygon; // Define a região interior à área associada à hipotenusa.
    private Point2D.Double[] pt; // As coordenadas do centro das células-da-grade.

    private boolean[] cellSelected; // Indica se uma dada célula está selecionada (true) ou não (false).

    private double dx = 0, // A distância x entre onde o centro da célula e a posição (nela) onde o mouse foi clicado
                   dy = 0; // A distância y entre onde o centro da célula e a posição (nela) onde o mouse foi clicado

    private final double SNAP_DISTANCE = 0.25; // Distância de atração.
    private final double ANGLE = Math.toDegrees(Math.atan2(4,3)); // Ângulo de inclinação da área associada à hipotenusa.
    private final int N = 25; // Quantidade de células (área associada à hipotenusa).
    private final short X = 0, Y = 1; // Enumeração simples para facilitar a visualização.
    private final String SVG_FILE = new String("resources/pitagoras.svg"); // Arquivo SVG.
    private final String LOGO_FILE = new String("resources/cepa.jpg"); // Logotipo do CEPA.
    private final String CELL_BASENAME = new String("cell"); // Prefixo dos IDs das células no arquivo SVG.
    private final String CELLPOS_BASENAME = new String("cellPos"); // Prefixo dos IDs das coordenadas das células no arquivo SVG.
    
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
        viewMenu = new javax.swing.JMenu();
        areaOption = new javax.swing.JCheckBoxMenuItem();
        cellsOption = new javax.swing.JCheckBoxMenuItem();
        aboutMenu = new javax.swing.JMenu();
        aboutOption = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Teorema de Pitágoras");
        setBackground(new java.awt.Color(117, 197, 240));
        setForeground(new java.awt.Color(117, 197, 240));
        setResizable(false);

        svgPanel.setBackground(new java.awt.Color(255, 255, 255));
        svgPanel.setPreferredSize(new java.awt.Dimension(700, 700));

        javax.swing.GroupLayout svgPanelLayout = new javax.swing.GroupLayout(svgPanel);
        svgPanel.setLayout(svgPanelLayout);
        svgPanelLayout.setHorizontalGroup(
            svgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 700, Short.MAX_VALUE)
        );
        svgPanelLayout.setVerticalGroup(
            svgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 700, Short.MAX_VALUE)
        );

        viewMenu.setText("Ver");

        areaOption.setText("Áreas");
        areaOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                areaOptionActionPerformed(evt);
            }
        });
        viewMenu.add(areaOption);

        cellsOption.setText("Ladrilhos");
        cellsOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cellsOptionActionPerformed(evt);
            }
        });
        viewMenu.add(cellsOption);

        jMenuBar1.add(viewMenu);

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

    private void areaOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_areaOptionActionPerformed
        if (areaOption.isSelected()) grid.setAttribute("opacity", "1");
        else grid.setAttribute("opacity", "0");
    }//GEN-LAST:event_areaOptionActionPerformed

    private void cellsOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cellsOptionActionPerformed
        if (cellsOption.isSelected()) cellsGroup.setAttribute("opacity", "1");
        else cellsGroup.setAttribute("opacity", "0");
    }//GEN-LAST:event_cellsOptionActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Pitagoras().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu aboutMenu;
    private javax.swing.JMenuItem aboutOption;
    private javax.swing.JCheckBoxMenuItem areaOption;
    private javax.swing.JCheckBoxMenuItem cellsOption;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel svgPanel;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables
}
