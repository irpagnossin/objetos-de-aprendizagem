package cepa.edu.mechanics.kinematics;

import cepa.edu.util.Cronometer;
import cepa.edu.util.DynamicAverage;
import cepa.edu.util.LanguageObservable;
import cepa.edu.util.LogoPanel;
import cepa.edu.util.Util;
import cepa.edu.util.svg.SVGViewBox;
import java.awt.BorderLayout;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
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

/**
 *
 * @author irpagnossin
 */
public class UniformAcceleratedMovementVertical extends javax.swing.JFrame implements Observer {

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new UniformAcceleratedMovementVertical().setVisible(true);
            }
        });
    }

    // Construtor da classe.
    public UniformAcceleratedMovementVertical() {

        // Define look & feel da GUI.
        // ---------------------------------------------------------------------
        try{
        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        	SwingUtilities.updateComponentTreeUI(this);
        }
        catch( Exception e ){/* Nada: não tem problema se falhar. */}

        // Obtém uma referência para o logger do sistema.
        // ---------------------------------------------------------------------
        logger = Logger.getLogger(UniformAcceleratedMovementVertical.class.getName());

        // Carrega as propriedades do aplicativo.
        // ---------------------------------------------------------------------
        InputStream stream = getClass().getClassLoader().getResourceAsStream(propertiesFile); // NOI18N
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "File " + propertiesFile + " not found: the application cannot run.", ex); // NOI18N
            System.exit(-1);
        }

        animationCron = new Cronometer();
        averageMouseSpeed = new DynamicAverage(N);

        setupGUI(properties);
        setupSVGScene(properties);
        getSVGElements();
        registerObservers();
    }

    // Traduz os elementos sensíveis ao locale.
    public void update(Observable o, Object arg) {
        UniformAcceleratedMovementVertical.this.setTitle(idiom.getString("frame.title"));
        aboutMenu.setText(idiom.getString("about.menu.label"));
        aboutOption.setText(idiom.getString("about.option.label"));
    }

    // Configura os elementos GUI do aplicativo.
    private void setupGUI ( final Properties properties ) {
        initComponents(); // Configura g GUI (g parte gerada pelo Matisse).

        // Configuração do menu de idiomas.
        // ---------------------------------------------------------------------
        menuBar.add(new JPanel());
        languageMenu = new JMenu();
        menuBar.add(languageMenu);

        idiomObservable = new LanguageObservable();
        idiomObservable.addObserver(this);

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
                    idiomObservable.notifyObservers();
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

        // Insere o logotipo do CEPA no glass pane (invisível).
        // ---------------------------------------------------------------------
        setGlassPane(new LogoPanel(properties.getProperty("logo.file"))); // NOI18N
    }

    // Carrega o arquivo SVG e configura a cena.
    private void setupSVGScene ( final Properties properties ) {

		try {
		    String parser = XMLResourceDescriptor.getXMLParserClassName();
		    SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);

            ClassLoader classLoader = this.getClass().getClassLoader();
            URL url = classLoader.getResource(properties.getProperty("scene.file")); // NOI18N
            String uri = url.toURI().toString();

		    document = (SVGDocument) factory.createDocument(uri);
		}
		catch (IOException ex) {
            logger.log(Level.SEVERE, "Check file " + properties.getProperty("scene.file"), ex); // NOI18N
            System.exit(-1);
        }
        catch (URISyntaxException ex) {
            logger.log(Level.SEVERE, "Check file " + properties.getProperty("scene.file"), ex); // NOI18N
            System.exit(-1);
        }

        canvas = new JSVGCanvas();
		canvas.setDocumentState (JSVGCanvas.ALWAYS_DYNAMIC); // Torna dinâmico o canvas.
		canvas.setDocument (document); // Associa a cena SVG (propriedade scene.file) ao canvas.
		canvas.setEnableImageZoomInteractor(false);
		canvas.setEnablePanInteractor(false); // Desabilita g opção de arrastar g cena SVG.
		canvas.setEnableRotateInteractor(false); // Desabilita g opção de rotacionar g cena SVG.
		canvas.setEnableZoomInteractor(false); // Desabilita g opção de ampliar/reduzir g cena SVG.

        svgPanel.setLayout(new BorderLayout());
        svgPanel.add(canvas,BorderLayout.CENTER);
    }

    // Cria as referências para os elementos relevantes da cena SVG e lê propriedades importantes deles.
    private void getSVGElements () {

        root = (SVGElement) ((SVGDocument)document).getRootElement();
        ball = document.getElementById("ball");
        ballGroup = document.getElementById("ball.group");
        shadow = document.getElementById("shadow");
        velocityVector = document.getElementById("v.vector");
        background = document.getElementById("background");
        velocityGroup = document.getElementById("v.group");

        // Lê do arquivo SVG as coordenadas e dimensões da view-box.
        viewbox = new SVGViewBox(root.getAttribute("viewBox"));
        
        // Lê do arquivo SVG o raio da bola (meia-altura da figura png).
        ballRadius = Double.parseDouble(document.getElementById("ball.def").getAttribute("height"))/2;

        // Lê do arquivo SVG g posição inicial da bola.
        double[] aux = getTranslateCoordinates(ballGroup.getAttribute("transform"));
        xPos = aux[0];
        ballPos = aux[1];
        lastBallPos = ballPos;

        // Lê do arquivo SVG g posição da sombra (utilizada apenas para limitar o movimento da bola)
        shadowPos = Double.parseDouble(shadow.getAttribute("cy"));
        shadowRadius = Double.parseDouble(shadow.getAttribute("rx"));

        // Cria as marcas do tempo de queda-livre.
        markGroup = new Element[nMarks];
        label = new Element[nMarks];

        for (short i = 0; i < nMarks; i++) {
            markGroup[i] = document.createElementNS(svgNS, "g"); // O grupo serve para aplicar a translação (posicionamento) da marca e do rótulo

            label[i] = document.createElementNS(svgNS, "text"); // O rótulo (segundos de queda) e suas propriedades.
            label[i].setAttribute("style", "font-weight:normal;font-size:1.5;font-family:'Times New Roman';fill:black");
            label[i].setAttribute("text-anchor", "end");
            label[i].setAttribute("transform", "translate(-1,0)");

            Element mark = document.createElementNS(svgNS,"circle"); // A marca do tempo de queda.
            mark.setAttribute("r", "0.2");
            mark.setAttribute("fill", "black");

            markGroup[i].appendChild(label[i]);
            markGroup[i].appendChild(mark);
            root.appendChild(markGroup[i]);
        }
    }

    // Registra os observadores de eventos dos elementos relevantes da cena SVG
    private void registerObservers () {

        // Registra os observadores de eventos do fundo da cena.
        // ---------------------------------------------------------------------
//        ((EventTarget) background).addEventListener("mousemove", new EventListener(){
//
//            public void handleEvent(Event evt) {
//                onMouseMove();
//            }
//        }, false);

        // Registra os observadores de eventos da bola.
        // ---------------------------------------------------------------------
        ((EventTarget) ballGroup).addEventListener("mousedown", new EventListener(){

            public void handleEvent(Event evt) {

                ballSelected = true;
                animationCron.stop();

                dy = getMousePos(Y) - ballPos;
                nextMark = 0;

                for ( int i = 0; i < nMarks; i++ ){
                    markGroup[i].setAttribute("transform", "translate(" + xPos + "," + (viewbox.getY() + 2*viewbox.getHeight()) + ")");
                }
            }
        }, false);
        ((EventTarget) ballGroup).addEventListener("mouseup", new EventListener(){

            public void handleEvent(Event evt) {
                if (ballSelected) {
                    ballSelected = false;
                    animationCron.start();

                    dy = getMousePos(Y) - ballPos;

                    freeFallHandler.setStartingY(ballPos);
                    freeFallHandler.setStartingV(averageMouseSpeed.getMean());

                    markGroup[nextMark%nMarks].setAttribute("transform", "translate(" + xPos + "," + (ballPos-ballRadius) + ")");
                    label[nextMark%nMarks].setTextContent(nextMark + " s");
                    ++nextMark;
                }
            }
        }, false);
        ((EventTarget) ballGroup).addEventListener("mouseout", new EventListener(){

            public void handleEvent(Event evt) {
                if (ballSelected) {
                    ballSelected = false;
                    animationCron.start();

                    dy = getMousePos(Y) - ballPos;

                    freeFallHandler.setStartingY(ballPos);
                    freeFallHandler.setStartingV(averageMouseSpeed.getMean());
                }
            }
        }, false);
        ((EventTarget) ballGroup).addEventListener("mousemove", new EventListener(){

            public void handleEvent(Event evt) {
                onMouseMove();
            }
        }, false);

        // Registra a thread responsável pela animação da queda-livre.
        // ---------------------------------------------------------------------
		canvas.addSVGLoadEventDispatcherListener (
			new SVGLoadEventDispatcherAdapter () {
                @Override
		        public void svgLoadEventDispatchStarted (SVGLoadEventDispatcherEvent e) {
                    window = canvas.getUpdateManager().getScriptingEnvironment().createWindow ();
		       }
		    }
		);

        freeFallHandler = new AnimationHandler();

        ((EventTarget) root).addEventListener("SVGLoad", new EventListener(){

            public void handleEvent(Event evt) {
                window.setInterval(freeFallHandler, dt);
            }
        }, false);
    }

    // Arrasta a bola.
    private void onMouseMove () {
        if (ballSelected) {
            
            // Define a posição da bola e limita-a, acima pela borda da cena, e abaixo pela posição da sombra (chão).
            ballPos = Math.min(
                Math.max(
                    getMousePos(Y) - dy,
                    viewbox.getY()+2*ballRadius),
                shadowPos
            );

            // Atualiza a posição da bola durante o arraste do mouse.
            ballGroup.setAttribute("transform", "translate(" + xPos + "," + ballPos + ")");
        }
    }

    // Mapeia a posição do mouse na tela para a cena SVG.
    private double getMousePos ( final short axis ) {
        // A razão entre as dimensões do JPanel e do view-box, necessário para mapear a posição do mouse na tela para g cena SVG.
        double ratio = Math.min(canvas.getHeight()/viewbox.getHeight(),canvas.getWidth()/viewbox.getWidth());

        double ans = 0;
        if (axis == X) ans = viewbox.getX() + canvas.getMousePosition().getX()/ratio;
        else if (axis == Y) ans = viewbox.getY() + canvas.getMousePosition().getY()/ratio;

        return ans;
    }

    // Retorna as coordenadas SVG do elemento "translate" do atributo "transform" (passado como argumento).
    private double[] getTranslateCoordinates ( String transformation ) {

        String regex1 = "\\s*translate[(]\\s*\\d+\\s*,\\s*\\d+\\s*[)]\\s*";
        String regex2 = "\\d+,\\d+";

        Pattern pattern1 = Pattern.compile(regex1);
        Pattern pattern2 = Pattern.compile(regex2);
        Matcher matcher1 = pattern1.matcher(transformation);

        String[] translate = {"0","0"};

        if (matcher1.find()) {
            Matcher matcher2 = pattern2.matcher(matcher1.group());

            if (matcher2.find()) {
                translate = Pattern.compile(",").split(matcher2.group());
            }
        }

        return new double[]{Double.parseDouble(translate[0]),Double.parseDouble(translate[1])};
    }

    // Fator de deformação da bola (em função do frame).
    private double scale ( final short f ) {
        double ans = 1;
        if ( f > 0 && f < n ) ans = 4 * (1-fMin) * (Math.pow((double)f/n,2) - (double)f/n) + 1;
        System.out.println("scale(" + f + ") = " + ans);
        return ans;
    }

    // Lida com a animação da queda-livre e da deformação da bola.
    private class AnimationHandler implements Runnable {

        private double yI = 0, // Altura inicial da bola
                       vI = 0, // Velocidade inicial da bola
                       v  = 0, // Velocidade no instante t.
                       g  = 2.5; // Aceleração da gravidade (ajustado de modo a melhorar a animação na tela)

        private double factor = 1, // Fator de redução da sombra
                       d = 35; // A distância bola-sombra na qual o raio da sombra é zero.

        private double t = 0; // O tempo t
        private short frame = 0; // Contagem de quadros (frame) na fase de deformação da bola.

        public void run() {
            
            if (animationCron.isRunning()) {

                // Fase de queda-livre da animação.
                // - - - - - - - - - - - - - - - -
                if (falling) {
                    t = animationCron.read()/1000d;
                    ballPos = yI + vI * t + 0.5 * g * Math.pow(t,2);

                    if (ballPos > shadowPos) {
                        ballPos = shadowPos;
                        falling = false;
                        frame = 0;
                        velocityVector.setAttribute("transform", "scale(1,0)");
                    }

                    // Atualiza a posição da bola durante a animação.
                    ballGroup.setAttribute("transform", "translate(" + xPos + "," + ballPos + ")");

                    // Atualiza a marca de tempo de queda.
                    if (Math.abs(t - nextMark) < dt/1000d) {
                        markGroup[nextMark%nMarks].setAttribute("transform", "translate(" + xPos + "," + (ballPos-ballRadius) + ")");
                        label[nextMark%nMarks].setTextContent(nextMark + " s");
                        ++nextMark;
                    }

                    v = vI + g * t;
                    velocityVector.setAttribute("transform", "scale(1," + -v*ballSpeedToArrowScale + ")");

                }
                // Fase de deformação da bola (após a queda).
                // - - - - - - - - - - - - - - - - - - - - -
                else {                    
                    ball.setAttribute("transform", "scale(1," + scale(++frame) + ")");
                    
                    if (frame == n) {
                        falling = true;
                        animationCron.stop();
                    }

                    velocityVector.setAttribute("transform", "scale(1,0)");
                    averageMouseSpeed.reset();
                }                
            }
            else if (ballSelected) {

                // Calcula a velocidade média da bola com base na velocidade do mouse.
                averageMouseSpeed.put((ballPos-lastBallPos)/(dt/1000d)*mouseSpeedToBallSpeedFactor);

                // Atualiza o tamanho da flecha que representa a velocidade da bola.
                velocityVector.setAttribute("transform", "scale(1," + -averageMouseSpeed.getMean()*ballSpeedToArrowScale + ")");
            }
            
            if (ballSelected || animationCron.isRunning()) {

                // Posiciona a flecha que representa a velocidade da bola sobre ela.
                velocityGroup.setAttribute("transform", "translate(" + xPos + "," + (ballPos-ballRadius) + ")");

                // Modifica g sombra com base na altura da bola.
                factor = Math.max(0,1 - Math.abs(shadowPos - ballPos) / d);
                shadow.setAttribute("rx", String.valueOf(factor*shadowRadius));
                shadow.setAttribute("ry", String.valueOf(factor*shadowRadius/2));

                // Salva em lastBallPos g posição vertical da bola no quadro (frame) atual.
                lastBallPos = ballPos;
            }

            background.setAttribute("x", String.valueOf(3*Math.random()));
        }

        // Define a altura inicial da bola.
        public void setStartingY ( final double y ){
            yI = y;
        }

        // Define a velocidade inicial da bola.
        public void setStartingV ( final double v ){
            vI = v;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        svgPanel = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        aboutMenu = new javax.swing.JMenu();
        aboutOption = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("LanguageBundle_pt_BR"); // NOI18N
        setTitle(bundle.getString("frame.title")); // NOI18N
        setResizable(false);

        svgPanel.setBackground(new java.awt.Color(255, 255, 255));
        svgPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        svgPanel.setPreferredSize(new java.awt.Dimension(200, 450));

        javax.swing.GroupLayout svgPanelLayout = new javax.swing.GroupLayout(svgPanel);
        svgPanel.setLayout(svgPanelLayout);
        svgPanelLayout.setHorizontalGroup(
            svgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 196, Short.MAX_VALUE)
        );
        svgPanelLayout.setVerticalGroup(
            svgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 446, Short.MAX_VALUE)
        );

        aboutMenu.setText(bundle.getString("about.menu.label")); // NOI18N

        aboutOption.setText(bundle.getString("about.option.label")); // NOI18N
        aboutOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutOptionActionPerformed(evt);
            }
        });
        aboutMenu.add(aboutOption);

        menuBar.add(aboutMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(svgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(svgPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Habilita g visualização do logotipo do CEPA (torna visível o glass pane).
    private void aboutOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutOptionActionPerformed
        this.getGlassPane().setVisible(true);
    }//GEN-LAST:event_aboutOptionActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu aboutMenu;
    private javax.swing.JMenuItem aboutOption;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JPanel svgPanel;
    // End of variables declaration//GEN-END:variables

    private Document document; // The SVG document
    private JSVGCanvas canvas; // Cena SVG
    private JMenu languageMenu; // Menu de idiomas
    private JRadioButtonMenuItem[] radioButton; // Opções de idiomas

    private Logger logger; // Referência (ponteiro) para o logger do sistema.

    private ResourceBundle idiom; // Referência para os arquivos LanguageBundle_??_??.properties
    private Observable idiomObservable; // Alvo de observação do(s) observador(es) de mudança de idioma.
    private Locale[] availableLocales; // Locales para os quais há tradução

    private SVGElement root;
    private Window window;

    private Element ball, // Referência para a bola na cena SVG (para aplicar a deformação ao chocar-se com o chão).
                    ballGroup, // Referência para o grupo da bola na cena SVG (para aplicar a movimentação).
                    shadow, // Referência para a sombra na cena SVG.
                    velocityVector, // Referência para a flecha que representa a velocidade na cena SVG (para aplicar a deformação, proporcional à velocidade).
                    background, // Referência para o fundo da cena.
                    velocityGroup; // Referência para o grupo da flecha que representa a velocidade na cena SVG (para aplicar a movimentação).

    private Element[] label, // Referência para os rótulos das marcas de queda-livre ("1s", "2s" etc)
                      markGroup; // Referência para o grupo de objetos que compõem a marca de queda livre: marca + rótulo (label).

    private int nMarks = 6; // Quantidade de marcas de tempo de queda-livre.
    private int nextMark = 0; // Indica o próximo instante para o qual se deve inserir uma marca de queda: 1s, 2s, 3s etc.

    private boolean ballSelected = false; // Identifica se a bola está selecionda (true) ou não (false)
    private boolean falling = true; // Identifica se animação está na fase de queda (true) ou de deformação da bola (false).

    private double ballPos, // A altura da bola
                   lastBallPos, // A altura da bola no quadro (frame) anterior.
                   ballRadius, // O raio da bola (meia-altura da figura).
                   shadowPos = 0, // A altura da sombra (fixa)
                   shadowRadius = 0; // O raio da sombra

    private double xPos = 0, // Posição horizontal (x) da bola. É a mesma para a bola, a sombra e as marcas de tempo de queda.
                   dy = 0; // Distância do mouse até a base da bola (usado apenas para que a bola não "pule" ao ser clicada fora da base).

    private SVGViewBox viewbox; // Representa o atributo viewbox da tag <svg/>
    private AnimationHandler freeFallHandler; // Thread que cuida da animação
    private Cronometer animationCron; // Cronômetro utilizado durante a queda-livre
    private DynamicAverage averageMouseSpeed; // Calcula a velocidade média da bola durante o arraste pelo mouse

    private final short X = 0, Y = 1;
    private final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI; // Namespace do documento SVG
    private final String propertiesFile = new String("app.properties"); // Nome do arquivo de propriedades // NOI18N
    private final long dt = 30; // Intervalo de tempo entre dois quadros (frames) sucessivos (em mili-segundos)
    private final double ballSpeedToArrowScale = 0.1; // Fator de escala para converter a velocidade no tamanho da flecha que a representa na cena SVG.
    private final double mouseSpeedToBallSpeedFactor = 0.4; // Fator de conversão de velocidade do mouse para velocidade da bola.
    private final int N = 5; // Quantidade de quadros (frames) utilizados para calcular a velocidade média de arraste da bola.
    private final short n = 10; // A quantidade de frames usada para animar g deformação da bola ao chocar-se com o chão.
    private final double fMin = 0.7; // Fator máximo de deformação vertical da bola ao chocar-se com o chão.
}
