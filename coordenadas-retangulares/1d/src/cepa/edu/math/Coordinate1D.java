package cepa.edu.math;

import cepa.edu.util.LogoPanel;
import cepa.edu.util.Util;
import java.awt.BorderLayout;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
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
public class Coordinate1D extends javax.swing.JFrame {

    /** Creates new form Coordinate1D */
    public Coordinate1D() {

        InputStream stream = getClass().getClassLoader().getResourceAsStream("app.properties"); // NOI18N
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } catch (IOException ex) {
            Logger.getLogger(Coordinate1D.class.getName()).log(Level.SEVERE, null, ex); System.exit(-1);
        }

        numberFormat = new DecimalFormat("###,##0.0"); // NOI18N
        numberFormat.setMaximumFractionDigits(1);
        numberFormat.setMinimumFractionDigits(0);

        /***********************************************************************
         Configura a GUI
         **********************************************************************/
        // Define look & feel
        try{
        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        	SwingUtilities.updateComponentTreeUI(this);
        }
        catch( Exception e ){/*Nada*/}

        initComponents();
        menuBar.add(new JPanel());
        menuBar.add(languageMenu);

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
                    Coordinate1D.this.setLocale(idiom.getLocale());
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

        setGlassPane(new LogoPanel(properties.getProperty("logo.file"))); // NOI18N

        /***********************************************************************
         Carrega o arquivo SVG para o JPanel.
         **********************************************************************/
		try {

		    String parser = XMLResourceDescriptor.getXMLParserClassName();
		    SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);

            ClassLoader classLoader = this.getClass().getClassLoader();
            URL url = classLoader.getResource(properties.getProperty("scene.file")); // NOI18N
            String uri = url.toURI().toString();

		    document = (SVGDocument) factory.createDocument(uri);
		}
		catch (IOException ex) { Logger.getLogger(Coordinate1D.class.getName()).log(Level.SEVERE, null, ex); System.exit(-1); }
        catch (URISyntaxException ex) { Logger.getLogger(Coordinate1D.class.getName()).log(Level.SEVERE, null, ex); System.exit(-1);}

        canvas = new JSVGCanvas();
		canvas.setDocumentState (JSVGCanvas.ALWAYS_DYNAMIC); // Torna dinâmico o canvas.
		canvas.setDocument (document); // Associa a cena SVG (propriedade scene.file) ao canvas.
		canvas.setEnableImageZoomInteractor(false); // TODO: desabilita o quê?
		canvas.setEnablePanInteractor(false); // Desabilita a opção de arrastar a cena SVG.
		canvas.setEnableRotateInteractor(false); // Desabilita a opção de rotacionar a cena SVG.
		canvas.setEnableZoomInteractor(false); // Desabilita a opção de ampliar/reduzir a cena SVG.


        svgPanel.setLayout(new BorderLayout());
        svgPanel.add(canvas,BorderLayout.CENTER);

        loadSVGElements();
        registerListeners();
    }

    // Lê os elementos relevantes da cena SVG e obtém as propriedades importantes.
    private void loadSVGElements () {

        // Lê do arquivo SVG os elementos da cena
        SVGElement root = (SVGElement) ((SVGDocument)document).getRootElement(); // O elemento raiz da cena SVG.
        bikeGroup = document.getElementById("bike.group"); // NOI18N
        coordinateLabel = document.getElementById("coordinate.label"); // NOI18N
        background = document.getElementById("background"); // NOI18N
        Element rule = document.getElementById("rule"); // NOI18N
        Element aPositiveLabel = document.getElementById("positive.label.sample"); // NOI18N
        Element aNegativeLabel = document.getElementById("negative.label.sample"); // NOI18N

        // Define o tamanho da view-port (ou view-box)
        String[] aux = Pattern.compile(" ").split(root.getAttribute("viewBox")); // NOI18N

        ulc[X] = Double.parseDouble(aux[0]);
        ulc[Y] = Double.parseDouble(aux[1]);
        viewBoxSize[X] = Double.parseDouble(aux[2]);
        viewBoxSize[Y] = Double.parseDouble(aux[3]);

        // Define o ponto médio (em x) da cena.
        meanX = (ulc[X]+viewBoxSize[X])/2;

        // Posiciona a régua e a bicicleta no centro da cena
        bikeGroup.setAttribute("x", String.valueOf(meanX)); // NOI18N
        rule.setAttribute("x", String.valueOf(meanX)); // NOI18N

        // Escreve a coordenada inicial da bicicleta: 0.
        coordinateLabel.setTextContent("0"); // NOI18N

        // Lê os estilos utilizados nos números positivos e negativos. Eles são
        // utilizados para trocar o estilo de exibição do rótulo da coordenada,
        // conforme este número é positivo ou negativo.
        textClassOfPositiveLabel = aPositiveLabel.getAttribute("class"); // NOI18N
        textClassOfNegativeLabel = aNegativeLabel.getAttribute("class"); // NOI18N
    }

    // Registra os observadores (ou "ouvidores") de eventos.
    private void registerListeners () {

        EventTarget backgroundEventTarget = (EventTarget) background;
        backgroundEventTarget.addEventListener("mousemove", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                updateBikeGroupPos();
            }
        }, false);

        EventTarget bikeGroupEventTarget = (EventTarget) bikeGroup;
        bikeGroupEventTarget.addEventListener("mousedown", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                bikeGroupSelected = true;
            }
        }, false);
        bikeGroupEventTarget.addEventListener("mouseup", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                bikeGroupSelected = false;
            }
        }, false);
        bikeGroupEventTarget.addEventListener("mousemove", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                updateBikeGroupPos();
            }
        }, false);
    }

    // Atualiza a posição da bicicleta
    private void updateBikeGroupPos () {
        if (bikeGroupSelected) {
            Dimension d = svgPanel.getSize();

            Point pt = canvas.getMousePosition();
            if (pt == null) return;
            double x = pt.getX() * viewBoxSize[X] / d.getWidth();

            bikeGroup.setAttribute("x", String.valueOf(x)); // NOI18N
            currentX = (x-meanX)/2;
            coordinateLabel.setTextContent(numberFormat.format(currentX));

            if ( currentX < 0 ) coordinateLabel.setAttribute("class", textClassOfNegativeLabel); // NOI18N
            else coordinateLabel.setAttribute("class", textClassOfPositiveLabel); // NOI18N
        }
    }

    // Observador de mudança de língua.
    private class LanguageObserver implements Observer {

        @SuppressWarnings("static-access") // NOI18N
        public void update(Observable o, Object arg) {
            aboutMenu.setText(idiom.getString("about.menu.label")); // NOI18N
            aboutOption.setText(idiom.getString("about.option.label")); // NOI18N;
            numberFormat = new DecimalFormat("###,##0.0").getInstance(idiom.getLocale()); // NOI18N
            numberFormat.setMaximumFractionDigits(1);
            numberFormat.setMinimumFractionDigits(0);

            canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(new Runnable(){

                public void run() {
                    coordinateLabel.setTextContent(numberFormat.format(currentX));
                }
            });
        }
    }
    private class LanguageObservable extends Observable {
        @Override
        public void notifyObservers(){
            setChanged();
            super.notifyObservers();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked") // NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        svgPanel = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        aboutMenu = new javax.swing.JMenu();
        aboutOption = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("LanguageBundle_pt_BR"); // NOI18N
        setTitle(bundle.getString("frame.title")); // NOI18N
        setName(""); // NOI18N
        setResizable(false);

        svgPanel.setBackground(new java.awt.Color(255, 255, 255));
        svgPanel.setPreferredSize(new java.awt.Dimension(699, 100));

        javax.swing.GroupLayout svgPanelLayout = new javax.swing.GroupLayout(svgPanel);
        svgPanel.setLayout(svgPanelLayout);
        svgPanelLayout.setHorizontalGroup(
            svgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 669, Short.MAX_VALUE)
        );
        svgPanelLayout.setVerticalGroup(
            svgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 131, Short.MAX_VALUE)
        );

        menuBar.setName("teste"); // NOI18N

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
                .addComponent(svgPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 669, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(svgPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Habilita/desabilita a exibição do logotipo do CEPA
    private void aboutOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutOptionActionPerformed
        this.getGlassPane().setVisible(true);
    }//GEN-LAST:event_aboutOptionActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Coordinate1D().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu aboutMenu;
    private javax.swing.JMenuItem aboutOption;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JPanel svgPanel;
    // End of variables declaration//GEN-END:variables

    private JMenu languageMenu = new JMenu(); // Menu de idiomas
    private JRadioButtonMenuItem[] radioButton; // Opções do menu de idiomas
    private JSVGCanvas canvas; // O canvas SVG

    private Document document; // O documento SVG
    private Element bikeGroup, // O grupo de elementos da bicicleta (na cena SVG), composto pela própria bicicleta, pelo rótulo da coordenada e pelo cursor (bug).
                    coordinateLabel, // Rótulo da coordenada da bicicleta. Faz parte do grupo "bikeGroup", mas preciso de uma referência para poder modificar o conteúdo da tag.
                    background; // O fundo da cena, utilizado para melhorar a resposta aos eventos do mouse.

    private final short X = 0, Y = 1; // Índices dos vetores ulc e viewBoxSize; apenas para torná-los mais inteligíveis.

    private String textClassOfNegativeLabel, // Classe (ou estilo) da fonte usada para exibir os números NEGATIVOS.
                   textClassOfPositiveLabel; // Classe (ou estilo) da fonte usada para exibir os números POSITIVOS.

    private double meanX = 0, // Ponto médio (em x) da cena SVG
                   currentX = 0; // Coordenada atual da bicicleta
    private double[] ulc = new double[2], // Coordenada do canto superior esquerdo (upper-left-corner: ulc) da cena SVG.
                     viewBoxSize = new double[2]; // Dimensões da view-port (ou view-box).
    
    private boolean bikeGroupSelected = false; // Indica se o grupo de bicicleta está selecionado ou não.

    private LanguageObservable languageObservable; // Objeto observável que representa a língua utilizada na interface gráfica
    private ResourceBundle idiom; // ResourceBundle que contém as configurações de língua
    private Locale[] availableLocales; // Locales para os quais existe tradução deste aplicativo
    private NumberFormat numberFormat; // Formato de exibição do rótulo da coordenada da bicicleta
}
