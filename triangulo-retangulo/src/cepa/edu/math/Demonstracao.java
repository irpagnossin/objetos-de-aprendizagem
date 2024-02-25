package cepa.edu.math;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;

import cepa.edu.util.LogoPanel;
import cepa.edu.util.Util;
import cepa.edu.util.svg.SVGViewBox;
import java.awt.Color;
import java.awt.geom.Point2D;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.batik.script.Window;
import org.apache.batik.swing.svg.SVGLoadEventDispatcherAdapter;
import org.apache.batik.swing.svg.SVGLoadEventDispatcherEvent;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.svg.SVGElement;

/**
 *
 * @author irpagnossin
 */
public class Demonstracao extends javax.swing.JFrame {

    /** Creates new form Demonstracao */
    public Demonstracao() {
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

        Element A = document.getElementById("A"); // NOI18N
        Element B = document.getElementById("B"); // NOI18N
        Element C = document.getElementById("C"); // NOI18N
        Element D = document.getElementById("D"); // NOI18N

        Point2D.Double pA = new Point2D.Double(Double.parseDouble(A.getAttribute("cx")),Double.parseDouble(A.getAttribute("cy"))); // NOI18N
        Point2D.Double pB = new Point2D.Double(Double.parseDouble(B.getAttribute("cx")),Double.parseDouble(B.getAttribute("cy"))); // NOI18N
        Point2D.Double pC = new Point2D.Double(Double.parseDouble(C.getAttribute("cx")),Double.parseDouble(C.getAttribute("cy"))); // NOI18N
        Point2D.Double pD = new Point2D.Double(Double.parseDouble(D.getAttribute("cx")),Double.parseDouble(D.getAttribute("cy"))); // NOI18N

        double b = Util.distance(pA,pC);
        double c = Util.distance(pA,pB);
        double d = Math.abs(pD.getY()-pC.getY());
        double a = Math.sqrt(b*b+c*c);
        double h = b*c/a;
        double m = c*c/a;
        double n = b*b/a;

        rAngle = new double[]{Math.toDegrees(Math.atan2(b,c)),-Math.toDegrees(Math.atan2(c,b))};

        baricenter[0] = new Point2D.Double(pA.getX()-m/3,pB.getY()-h/3);
        baricenter[1] = new Point2D.Double(pA.getX()+n/3,pB.getY()-h/3);

        originalBaricenter[0] = new Point2D.Double(baricenter[0].getX(),baricenter[0].getY());
        originalBaricenter[1] = new Point2D.Double(baricenter[1].getX(),baricenter[1].getY());

        originalLabelPos[0][0] = new Point2D.Double(-m/6,h/3+d);
        originalLabelPos[0][1] = new Point2D.Double(-(c/6+d)*c/a,-(c/6+d)*b/a);
        originalLabelPos[0][2] = new Point2D.Double(m/3+d,-h/6);
        originalLabelPos[1][0] = new Point2D.Double(n/6,h/3+d);
        originalLabelPos[1][1] = new Point2D.Double((b/6+d)*b/a,-(b/6+d)*c/a);
        originalLabelPos[1][2] = new Point2D.Double(-n/3-d,-h/6);

        for (short i = 0; i < 2; i++)
            for (short j = 0; j < 3; j++)
                labelPos[i][j] = new Point2D.Double(originalLabelPos[i][j].getX(),originalLabelPos[i][j].getY());

        for (short i = 0; i < 2; i++) {
            triangle[i] = document.getElementById(triangleId[i]);
            triangle[i].setAttribute("transform", // NOI18N
                "translate(" + baricenter[i].getX() + "," + baricenter[i].getY() + ") " + // NOI18N
                "scale(1," + actualYScale[i] + ")" + // NOI18N
                "rotate(" + actualAngle[i] + ") " // NOI18N
                );

            triangleBkg[i] = document.getElementById(triangleBkgId[i]);
        }

        Element e = document.getElementById("label.fixed.a"); // NOI18N
        e.setAttribute("transform", "translate(" + pD.getX() + "," + pD.getY() + ")"); // NOI18N

        e = document.getElementById("label.fixed.b"); // NOI18N
        e.setAttribute("transform", "translate(" + (originalLabelPos[1][1].getX() + baricenter[1].getX()) + "," + (originalLabelPos[1][1].getY() + baricenter[1].getY()) + ")"); // NOI18N

        e = document.getElementById("label.fixed.c"); // NOI18N
        e.setAttribute("transform", "translate(" + (originalLabelPos[0][1].getX() + baricenter[0].getX()) + "," + (originalLabelPos[0][1].getY() + baricenter[0].getY()) + ")"); // NOI18N

        for (short i = 0; i < 2; i++) {
            for (short j = 0; j < 3; j++) {
                label[i][j] = document.getElementById(labelId[i][j]);
                label[i][j].setAttribute("transform", "translate(" + (originalLabelPos[i][j].getX() + baricenter[i].getX()) + "," + (originalLabelPos[i][j].getY() + baricenter[i].getY()) + ")"); // NOI18N
                label[i][j].setAttribute("opacity", "0"); // NOI18N
            }
        }

        // Referência para o elemento raiz da cena SVG.
        root = (SVGElement) ((SVGDocument)document).getRootElement();

        // Lê do arquivo SVG as coordenadas e dimensões da view-box.
        viewbox = new SVGViewBox(root.getAttribute("viewBox")); // NOI18N

        background = document.getElementById("background"); // NOI18N
        standardColor = background.getAttribute("fill"); // NOI18N

        // Referências para as (áreas sensíveis das) relações métricas.
        for (short i = 0; i < 4; i++) {
            equation[i] = document.getElementById(equationId[i]);
            eqSlice[i] = document.getElementById(eqSliceId[i]);
        }

        // Referências para os 9 lados dos três triângulos.
        for (short i = 0; i < 9; i++) {
            side[i] = document.getElementById(sideId[i]);
            side[i].setAttribute("stroke", "black"); // NOI18N
        }

        // Referência para a aba que cobre as equações.
        tab = document.getElementById("tab"); // NOI18N
        tabColor = document.getElementById("tab.color"); // NOI18N
        Color bc = this.getContentPane().getBackground();
        tabColor.setAttribute("fill", "rgb(" +
            bc.getRed() + "," +
            bc.getGreen() + "," +
            bc.getBlue() + ")"
        );

        e = document.getElementById("tab.ref"); // NOI18N
        tabPosLimit[0] = Double.parseDouble(e.getAttribute("cy")); // NOI18N
        tabPosLimit[1] = viewbox.getX() + viewbox.getHeight();

        // Referência para a manete de movimentação da aba (para cima).
        tabHandleMoveUp = document.getElementById("tab.handle.move-up"); // NOI18N
        tabHandleMoveUp.setAttribute("opacity", "0"); // NOI18N

        // Referência para a manete de movimentação da aba (para baixo).
        tabHandleMoveDown = document.getElementById("tab.handle.move-down"); // NOI18N
    }

    // Registra os observadores (ou "ouvidores") de eventos.
    private void registerListeners () {

		canvas.addSVGLoadEventDispatcherListener (
			new SVGLoadEventDispatcherAdapter () {
                @Override
		        public void svgLoadEventDispatchStarted (SVGLoadEventDispatcherEvent e) {
                    window = canvas.getUpdateManager().getScriptingEnvironment().createWindow ();
		       }
		    }
		);
        
        // Observador responsável por iniciar a animação da aba.
        ((EventTarget)tab).addEventListener("click", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                tabAnimationOn = true;
            }
        }, false);

        // Observador responsável por animar a aba.
        ((EventTarget)tab).addEventListener("SVGLoad", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                window.setInterval (new Runnable(){

                    private double dy = 1, tabPos = 0;

                    public void run() {
                        if (!tabAnimationOn) return;

                        // Define a posição inicial e o sentido do movimento (para cima ou para baixo).
                        if (tabState == TabState.UP) {
                            tabState = TabState.MOVING;
                            tabPos = tabPosLimit[0];
                            dy = +2;
                        }
                        else if (tabState == TabState.DOWN) {
                            tabState = TabState.MOVING;
                            tabPos = tabPosLimit[1];
                            dy = -2;
                        }

                        // Atualiza a posição da aba.
                        tabPos += dy;

                        // Limita o movimento da aba.
                        if (tabPos > tabPosLimit[1]) {
                            tabState = TabState.DOWN;
                            tabPos = tabPosLimit[1];
                            tabHandleMoveUp.setAttribute("opacity", "1"); // NOI18N
                            tabHandleMoveDown.setAttribute("opacity", "0"); // NOI18N
                            tabAnimationOn = false;
                        }
                        else if (tabPos < tabPosLimit[0]) {
                            tabState = TabState.UP;
                            tabPos = tabPosLimit[0];
                            tabHandleMoveUp.setAttribute("opacity", "0"); // NOI18N
                            tabHandleMoveDown.setAttribute("opacity", "1"); // NOI18N
                            tabAnimationOn = false;
                        }

                        // Aplica a transformação (translação em y) à aba.
                        tab.setAttribute("transform", "translate(0," + (tabPos-tabPosLimit[0]) + ")"); // NOI18N
                    }
                }, dt);
            }
        }, false);

        // Observadores responsáveis por enfatizar os lados dos triângulos e as relações-métricas.
        for (short i = 0; i < eqSlice.length; i++) {
            final short eq = i;

            ((EventTarget) eqSlice[i]).addEventListener("click", new EventListener(){

                public void handleEvent(Event evt) {
                    emphasizeEquation(eq);
                }
            }, false);
        }

        for (short index = 0; index < 2; index++) {

            final short t = index;

            ((EventTarget)triangle[t]).addEventListener("mousedown", new EventListener(){ // NOI18N

                public void handleEvent(Event evt) {

                    pressed[0] = selected[0] = pressed[1] = selected[1] = false;
                    pressed[t] = selected[t] = true;

                    try {
                        offset.setLocation(
                            getMousePos(X) - baricenter[t].getX(),
                            getMousePos(Y) - baricenter[t].getY()
                        );
                    } catch (Exception ex) {
                        offset.setLocation(0,0);
                    }

                    setTriangleEmphasis(t,true);
                }
            }, false);

            ((EventTarget)triangle[t]).addEventListener("mousemove", new EventListener(){ // NOI18N

                public void handleEvent(Event evt) {
                    onMouseMove();
                }
            }, false);

            ((EventTarget)triangle[t]).addEventListener("mouseup", new EventListener(){ // NOI18N

                public void handleEvent(Event evt) {
                    pressed[0] = pressed[1] = false;                    
                }
            }, false);

            ((EventTarget)triangle[t]).addEventListener("click", new EventListener(){ // NOI18N

                public void handleEvent(Event evt) {
                    selected[0] = selected[1] = false;
                    selected[t] = !selected[t];
                }
            }, false);

            animatedRotation[t] = new AnimatedRotation(t);

            ((EventTarget)root).addEventListener("SVGLoad", new EventListener(){ // NOI18N

                public void handleEvent(Event evt) {
                    window.setInterval (animatedRotation[t], dt);
                }
            }, false);

            animatedReflection[t] = new AnimatedReflection(t);

            ((EventTarget)root).addEventListener("SVGLoad", new EventListener(){ // NOI18N

                public void handleEvent(Event evt) {
                    window.setInterval (animatedReflection[t], dt);
                }
            }, false);

        }

        ((EventTarget)background).addEventListener("click", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                pressed[0] = selected[0] = pressed[1] = selected[1] = false;
                setTriangleEmphasis(0,false);
                setTriangleEmphasis(1,false);
                emphasizeEquation(-1);
            }
        }, false);

       ((EventTarget)background).addEventListener("mousedown", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                pressed[0] = selected[0] = pressed[1] = selected[1] = false;
                setTriangleEmphasis(0,false);
                setTriangleEmphasis(1,false);
            }
        }, false);

        ((EventTarget)background).addEventListener("mouseup", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                pressed[0] = selected[0] = pressed[1] = selected[1] = false;
                setTriangleEmphasis(0,false);
                setTriangleEmphasis(1,false);
            }
        }, false);
    }

    private void emphasizeEquation (final int eq) {
        if (state[0] != State.FINAL || state[1] != State.FINAL) return;

        // Retorna a cor de TODOS os lados para preto.
        for (short i = 0; i < side.length; i++) side[i].setAttribute("stroke", "black"); // NOI18N

        // Retorna a cor de TODAS as equações para preto.
        for (short i = 0; i < equation.length; i++) equation[i].setAttribute("fill", "black"); // NOI18N

        if (eq != -1) {
            equation[eq].setAttribute("fill", "red");

            for (short sideIdx = 0; sideIdx < relationToSidesFlags[eq].length; sideIdx++) {
                if (relationToSidesFlags[eq][sideIdx]) {
                   side[sideIdx].setAttribute("stroke", "red"); // NOI18N
                }
            }
        }
    }

    private void setTriangleEmphasis (final int t1, final boolean emphasis) {

        int t2 = (t1 == 0 ? 1 : 0);

        if (emphasis) {
            triangleBkg[t1].setAttribute("fill", selectionColor); // NOI18N
            triangleBkg[t2].setAttribute("fill", standardColor); // NOI18N
        }
        else triangleBkg[t1].setAttribute("fill", standardColor); // NOI18N
    }

    // Atualiza a posição dos triângulos durante o arraste.
    private void onMouseMove () {
        for (short tt = 0; tt < 2; tt++) {
            if (pressed[tt]) {
                try {
                    baricenter[tt].setLocation( getMousePos(X) - offset.getX(), getMousePos(Y) - offset.getY() );
                } catch (Exception ex) {
                    return;
                }

                if (state[tt] == State.INITIAL || state[tt] == State.TRANSLATED) {
                    if (Util.distance(baricenter[tt],originalBaricenter[tt]) < snapDistance) {
                        baricenter[tt].setLocation(originalBaricenter[tt]);
                        state[tt] = State.INITIAL;
                    } else {
                        state[tt] = State.TRANSLATED;
                    }
                }

                triangle[tt].setAttribute("transform", // NOI18N
                    "translate(" + baricenter[tt].getX() + "," + baricenter[tt].getY() + ") " + // NOI18N
                    "scale(1," + actualYScale[tt] + ")" + // NOI18N
                    "rotate(" + actualAngle[tt] + ") " // NOI18N
                    );

                double opacity = Math.min(1, Util.distance(baricenter[tt],originalBaricenter[tt])/fullOpacityDistance);

                for (short i = 0; i < 3; i++) {
                    label[tt][i].setAttribute("transform", "translate(" + (labelPos[tt][i].getX() + baricenter[tt].getX()) + "," + (labelPos[tt][i].getY() + baricenter[tt].getY()) + ")"); // NOI18N
                    if (state[tt] == State.TRANSLATED || state[tt] == State.INITIAL) label[tt][i].setAttribute("opacity", String.valueOf(opacity)); // NOI18N
                }
            }
        }
    }

    private class AnimatedReflection implements Runnable {

        private double scaleSpeed = 0.3;
        private int t;
        private double currentScale;
        private double endingScale;
        private boolean scaling = false;
        private boolean increasing = true;
        private double dScale;

        public AnimatedReflection ( final int t ) {
            this.t = t;
        }

        public void animate () {

            this.currentScale = actualYScale[t];
            this.endingScale = -1 * actualYScale[t];
            this.scaling = true;

            if (currentScale < endingScale) {
                increasing = true;
                dScale = +scaleSpeed;
            }
            else {
                increasing = false;
                dScale = -scaleSpeed;
            }
        }

        public void run() {
            if (!scaling) return;

            currentScale += dScale;

            if (currentScale > endingScale == increasing) {
                dScale = endingScale - currentScale;
                currentScale = endingScale;
                scaling = false;
            }

            triangle[t].setAttribute("transform", // NOI18N
                "translate(" + baricenter[t].getX() + "," + baricenter[t].getY() + ") " + // NOI18N
                "scale(1," + currentScale + ")" + // NOI18N
                "rotate(" + actualAngle[t] + ") " // NOI18N
                );

            for (short i = 0; i < 3; i++) {
                labelPos[t][i] = rotate(originalLabelPos[t][i],-actualAngle[t]);
                labelPos[t][i] = scale(labelPos[t][i],1,currentScale);
                label[t][i].setAttribute("transform", "translate(" + (labelPos[t][i].getX() + baricenter[t].getX()) + "," + (labelPos[t][i].getY() + baricenter[t].getY()) + ")"); // NOI18N
            }
        }
    }

    // Anima a rotação dos triângulos.
    private class AnimatedRotation implements Runnable {

        private double angularSpeed = 10;
        private double dAngle = 1;
        private double currentAngle = 0;
        private boolean increasing = true;

        private double endingAngle = 0;
        private short t = 0;
        private boolean rotating = false;

        public AnimatedRotation ( final short index ) {
            t = index;
        }

        public void animate (final double startingAngle, final double endingAngle) {
            this.currentAngle = startingAngle;
            this.endingAngle = endingAngle;            
            this.rotating = true;


            if (startingAngle < endingAngle) {
                increasing = true;
                dAngle = +angularSpeed;
            }
            else {
                increasing = false;
                dAngle = -angularSpeed;
            }
        }

        public void run() {
            if (!rotating) return;

            currentAngle += dAngle;

            if (currentAngle > endingAngle == increasing) {
                dAngle = endingAngle - currentAngle;
                currentAngle = endingAngle;
                rotating = false;
            }
            
            triangle[t].setAttribute("transform", // NOI18N
                "translate(" + baricenter[t].getX() + "," + baricenter[t].getY() + ") " + // NOI18N
                "scale(1," + actualYScale[t] + ")" + // NOI18N
                "rotate(" + currentAngle + ") " // NOI18N
                );

            for (short i = 0; i < 3; i++) {
                double angle = actualYScale[t] > 0 ? -currentAngle : currentAngle;
                labelPos[t][i] = scale(originalLabelPos[t][i],1,actualYScale[t]);
                labelPos[t][i] = rotate(labelPos[t][i],angle);
                label[t][i].setAttribute("transform", "translate(" + (labelPos[t][i].getX() + baricenter[t].getX()) + "," + (labelPos[t][i].getY() + baricenter[t].getY()) + ")"); // NOI18N
            }
        }
    }

    // Rotaciona o vetor r[] ao redor do eixo axis[] de um ângulo tAngle (em graus).
    private Point2D.Double rotate ( final Point2D.Double r, final double angle ) {
        return new Point2D.Double(
            + r.getX() * Math.cos(Math.toRadians(angle)) + r.getY() * Math.sin(Math.toRadians(angle)),
            - r.getX() * Math.sin(Math.toRadians(angle)) + r.getY() * Math.cos(Math.toRadians(angle))
        );
    }

    // Reflete o vetor r[] segundo os fatores (xScale,yScale).
    private Point2D.Double scale ( Point2D.Double r, final double xScale, final double yScale ) {
        return new Point2D.Double(xScale*r.getX(), yScale*r.getY());
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

    private Window window;
    private JSVGCanvas canvas; // O canvas SVG
    private Document document; // O documento SVG
    private SVGElement root; // O elemento raiz do arquivo SVG.

    private Element background, // O backrgound da cena.
                    tab, // A aba que cobre as relações-métricas.
                    tabColor, // O elemento do grupo tab ao qual posso atribuir a cor.
                    tabHandleMoveUp, // A manete de movimentação da aba (para cima).
                    tabHandleMoveDown; // A manete de movimentação da aba (para baixo).

    private Element[] triangle = new Element[2]; // Os dois triângulos móveis.
    private Element[] eqSlice = new Element[4]; // Os slices das quatro relações métricas.
    private Element[] equation = new Element[4]; // As quatro relações métricas.
    private Element[] triangleBkg = new Element[2]; // O interior dos triângulos móveis.
    private Element[] side = new Element[9]; // Os nove lados dos três triângulos.
    private Element[][] label = new Element[2][3]; // Os 6 rotulos móveis (dos dois triângulos móveis).
    
    private Point2D.Double offset = new Point2D.Double(0,0); // A distância entre o baricentro e o mouse no momento do toque (mousedown).
    private Point2D.Double[] baricenter = new Point2D.Double[2]; // As coordenadas (atuais) dos baricentros dos dois triângulos móveis.
    private Point2D.Double[] originalBaricenter = new Point2D.Double[2]; // As coordenadas originais (iniciais) dos baricentros.
    private Point2D.Double[][] labelPos = new Point2D.Double[2][3]; // As coordenadas (atuais) dos rótulos dos 6 lados (dos 2 triângulos móveis).
    private Point2D.Double[][] originalLabelPos = new Point2D.Double[2][3]; // As coordenadas originais dos rótulos dos 6 lados (dos 2 triângulos móveis).

    private SVGViewBox viewbox; // Encapsula os parâmetros da view-box (ou view-port).
    private AnimatedRotation[] animatedRotation = new AnimatedRotation[2]; // Thread responsável pela animação da rotação.
    private AnimatedReflection[] animatedReflection = new AnimatedReflection[2]; // Thread responsável pela animação da rotação.

    private boolean[] pressed = {false,false}; // Indica se o botão do mouse está pressionado sobre um dos triângulos móveis.
    private boolean[] selected = {false,false}; // Indica se um dos triângulos móveis estão selecionados (suscetíveis à rotação e reflexão).
    private boolean tabAnimationOn = false; // Indica se a animação da aba está em andamento ou não.

    private int[] actualYScale = {1,1}; // A escala atual (em y) dos dois triângulos móveis.
    private double[] actualAngle = {0,0}; // O ângulo de rotação atual dos dois triângulos móveis.
    private double[] rAngle; // Os ângulos alpha (elemento 0) e beta (1).
    private double[] tabPosLimit = new double[2]; // Os limites do movimento da aba.

    private String standardColor; // A cor-padrão dos triângulos (definida pela cor do background).
        
    private enum TabState { UP, MOVING, DOWN } // Os possíveis estados da aba.
    private TabState tabState = TabState.UP; // O estado atual da aba.

    private enum State { INITIAL, TRANSLATED, MIDDLE1, MIDDLE2, FINAL } // Os possíveis estados dos triângulos.
    private State[] state = {State.INITIAL,State.INITIAL}; // O estado atual dos triângulos.

    private final short X = 0, Y = 1;
    private final long dt = 30; // Intervalo entre dois quadros (frames) sucessivos, em milissegundos.
    private final double snapDistance = 3; // A distância que aciona/desaciona a atração (snap) dos triângulos.
    private final double fullOpacityDistance = 20; // A distância na qual a transparência do rótulos é 0 (totalmente opaco).

    // Relação entre a equação e os lados dos triângulos utilizados para deduzí-la.
    private final boolean[][] relationToSidesFlags = {
        {true,true,false,true,false,false,false,false,true}, // Os lados usados (true) na dedução da equação 0 e os não usados (false).
        {true,false,true,false,true,false,false,true,false}, // Os lados usados (true) na dedução da equação 1 e os não usados (false).
        {false,false,false,false,false,true,true,true,true}, // Os lados usados (true) na dedução da equação 2 e os não usados (false).
        {true,true,false,false,true,true,false,false,false}  // Os lados usados (true) na dedução da equação 3 e os não usados (false).
    };

    private final String[] sideId = {"fixed.side.a","fixed.side.b","fixed.side.c","side.b","side.c","side.h.0","side.h.1","side.m","side.n"}; // NOI18N
    private final String[] triangleId = {"triangle.0","triangle.1"}; // NOI18N
    private final String[] triangleBkgId = {"triangle.0.bkg","triangle.1.bkg"}; // NOI18N
    private final String[] equationId = {"relation.1","relation.2","relation.3","relation.4"}; // NOI18N
    private final String[] eqSliceId = {"relation.1.slice","relation.2.slice","relation.3.slice","relation.4.slice"}; // NOI18N
    private final String[][] labelId = {
        {"label.m", "label.c", "label.h.1"}, // Os IDs dos rótulos do triângulo 0. // NOI18N
        {"label.n", "label.b", "label.h.2"}  // Os IDs dos rótulos do triângulo 1. // NOI18N
    };

    private final String selectionColor = "rgb(0,146,63)"; // A cor do triângulo selecionado. // NOI18N
    private final String SVG_FILE = new String("resources/demonstracao.svg"); // Arquivo SVG. // NOI18N
    private final String LOGO_FILE = new String("resources/cepa.jpg"); // Logotipo do CEPA. // NOI18N

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        svgPanel = new javax.swing.JPanel();
        rotationButton = new javax.swing.JButton();
        reflectionButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        aboutMenu = new javax.swing.JMenu();
        aboutOption = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        svgPanel.setBackground(new java.awt.Color(255, 255, 255));
        svgPanel.setPreferredSize(new java.awt.Dimension(500, 500));

        javax.swing.GroupLayout svgPanelLayout = new javax.swing.GroupLayout(svgPanel);
        svgPanel.setLayout(svgPanelLayout);
        svgPanelLayout.setHorizontalGroup(
            svgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );
        svgPanelLayout.setVerticalGroup(
            svgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );

        rotationButton.setText("Girar");
        rotationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotationButtonActionPerformed(evt);
            }
        });

        reflectionButton.setText("Refletir");
        reflectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reflectionButtonActionPerformed(evt);
            }
        });

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
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(svgPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rotationButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(reflectionButton)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(svgPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotationButton)
                    .addComponent(reflectionButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Efetua a rotação dos triângulos.
    private void rotationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotationButtonActionPerformed
        canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(new Runnable(){

            public void run() {
                
                int t = 0;
                if (selected[0]) t = 0;
                else if (selected[1]) t = 1;
                else return;

                switch(state[t]) {
                    //------------------------------------------------------------------
                    case INITIAL:
                        return;
                    //------------------------------------------------------------------
                    case TRANSLATED:
                        animatedRotation[t].animate(0, rAngle[t]);
                        state[t] = State.MIDDLE1;
                        actualAngle[t] = rAngle[t];
                        break;

                    //------------------------------------------------------------------
                    case MIDDLE1:
                        animatedRotation[t].animate(rAngle[t], 0);
                        state[t] = State.TRANSLATED;
                        actualAngle[t] = 0;
                        break;

                    //------------------------------------------------------------------
                    case MIDDLE2:                        
                        animatedRotation[t].animate(0, rAngle[t]);
                        state[t] = State.FINAL;
                        actualAngle[t] = rAngle[t];
                        break;

                    //------------------------------------------------------------------
                    case FINAL:
                        emphasizeEquation(-1);
                        animatedRotation[t].animate(rAngle[t], 0);
                        state[t] = State.MIDDLE2;
                        actualAngle[t] = 0;                        
                        break;
                }
            }
        });
    }//GEN-LAST:event_rotationButtonActionPerformed

    // Efetua a reflexão dos triângulos.
    private void reflectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reflectionButtonActionPerformed
        canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(new Runnable(){

            public void run() {

                int t = 0;
                if (selected[0]) t = 0;
                else if (selected[1]) t = 1;
                else return;

                switch(state[t]) {
                    //------------------------------------------------------------------
                    case INITIAL:
                        return;
                    //------------------------------------------------------------------
                    case TRANSLATED:
                        state[t] = State.MIDDLE2;
                        animatedReflection[t].animate();
                        actualYScale[t] = -1;
                        break;

                    //------------------------------------------------------------------
                    case MIDDLE1:
                        state[t] = State.FINAL;
                        animatedReflection[t].animate();
                        actualYScale[t] = -1;
                        break;

                    //------------------------------------------------------------------
                    case MIDDLE2:
                        state[t] = State.TRANSLATED;
                        animatedReflection[t].animate();
                        actualYScale[t] = +1;
                        break;

                    //------------------------------------------------------------------
                    case FINAL:
                        emphasizeEquation(-1);
                        state[t] = State.MIDDLE1;
                        animatedReflection[t].animate();
                        actualYScale[t] = +1;                        
                        break;
                }

                // Aplica a transformação ao triângulo.                
//                triangle[t].setAttribute("transform", // NOI18N
//                    "translate(" + baricenter[t].getX() + "," + baricenter[t].getY() + ") " + // NOI18N
//                    "scale(1," + actualYScale[t] + ")" + // NOI18N
//                    "rotate(" + actualAngle[t] + ") " // NOI18N
//                    );

                // Atualiza a posição dos rótulos com base nas transformações aplicadas.                
//                for (short i = 0; i < 3; i++) {
//                    labelPos[t][i] = scale(originalLabelPos[t][i],1,actualYScale[t]);
//                    double tmp = state[t] == State.MIDDLE1 ? -actualAngle[t] : actualAngle[t]; // TODO: obtido empiricamente. Não entendi o porquê!
//                    labelPos[t][i] = rotate(labelPos[t][i],tmp);
//                    label[t][i].setAttribute("transform", "translate(" + (labelPos[t][i].getX() + baricenter[t].getX()) + "," + (labelPos[t][i].getY() + baricenter[t].getY()) + ")"); // NOI18N
//                }
            }
        });
    }//GEN-LAST:event_reflectionButtonActionPerformed

    // Habilita a visualização do logotipo do CEPA.
    private void aboutOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutOptionActionPerformed
        this.getGlassPane().setVisible(true);
    }//GEN-LAST:event_aboutOptionActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Demonstracao().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu aboutMenu;
    private javax.swing.JMenuItem aboutOption;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JButton reflectionButton;
    private javax.swing.JButton rotationButton;
    private javax.swing.JPanel svgPanel;
    // End of variables declaration//GEN-END:variables
}
