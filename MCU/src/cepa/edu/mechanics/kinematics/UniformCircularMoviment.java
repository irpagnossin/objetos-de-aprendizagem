// TODO: na próxima versão, adicionar os vetores de posição, velocidade e aceleração na base móvel (arrastável)
package cepa.edu.mechanics.kinematics;

import cepa.edu.util.LogoPanel;
import cepa.edu.util.Util;
import java.awt.BorderLayout;
import java.awt.Dimension;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

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
public class UniformCircularMoviment extends javax.swing.JFrame {

    /** Creates new form UniformCircularMoviment */
    public UniformCircularMoviment() {

        InputStream stream = getClass().getClassLoader().getResourceAsStream("app.properties"); // NOI18N
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } catch (IOException ex) {
            Logger.getLogger(UniformCircularMoviment.class.getName()).log(Level.SEVERE, null, ex); System.exit(-1);
        }

        // Define look & feel
        try{
        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        	SwingUtilities.updateComponentTreeUI(this);
        }
        catch( Exception e ){/*Nada*/}

        numberFormat = new DecimalFormat("###,##0");

        startIcon = Util.getIcon(properties.getProperty("start.icon")); // NOI18N
        pauseIcon = Util.getIcon(properties.getProperty("pause.icon")); // NOI18N
        stopIcon  = Util.getIcon(properties.getProperty("stop.icon")); // NOI18N

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
		catch (IOException ex) { Logger.getLogger(UniformCircularMoviment.class.getName()).log(Level.SEVERE, null, ex); System.exit(-1); }
        catch (URISyntaxException ex) { Logger.getLogger(UniformCircularMoviment.class.getName()).log(Level.SEVERE, null, ex); System.exit(-1);}

		canvas.setDocumentState (JSVGCanvas.ALWAYS_DYNAMIC); // Torna dinâmico o canvas.
		canvas.setDocument (document); // Associa a cena SVG (propriedade scene.file) ao canvas.
		canvas.setEnableImageZoomInteractor(false); // TODO: desabilita o quê?
		canvas.setEnablePanInteractor(false); // Desabilita a opção de arrastar a cena SVG.
		canvas.setEnableRotateInteractor(false); // Desabilita a opção de rotacionar a cena SVG.
		canvas.setEnableZoomInteractor(false); // Desabilita a opção de ampliar/reduzir a cena SVG.

        SVGPanel.setLayout(new BorderLayout());
        SVGPanel.add(canvas,BorderLayout.CENTER);

        rotationHandler = new RotationHandler();

        loadSVGElements();
        updateVectorsVAndA();
        applyRotation();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked") // NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        SVGPanel = new javax.swing.JPanel();
        stopButton = new javax.swing.JButton();
        startButton = new javax.swing.JButton();
        dPhiSlider = new javax.swing.JSlider();
        sliderLabel = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        viewMenu = new javax.swing.JMenu();
        showVectorROption = new javax.swing.JCheckBoxMenuItem();
        showVectorVOption = new javax.swing.JCheckBoxMenuItem();
        showVectorAOption = new javax.swing.JCheckBoxMenuItem();
        showAngleOption = new javax.swing.JCheckBoxMenuItem();
        aboutMenu = new javax.swing.JMenu();
        aboutOption = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        SVGPanel.setBackground(new java.awt.Color(255, 255, 255));
        SVGPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        SVGPanel.setPreferredSize(new java.awt.Dimension(500, 500));

        javax.swing.GroupLayout SVGPanelLayout = new javax.swing.GroupLayout(SVGPanel);
        SVGPanel.setLayout(SVGPanelLayout);
        SVGPanelLayout.setHorizontalGroup(
            SVGPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 496, Short.MAX_VALUE)
        );
        SVGPanelLayout.setVerticalGroup(
            SVGPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 496, Short.MAX_VALUE)
        );

        stopButton.setIcon(stopIcon);
        stopButton.setPreferredSize(new java.awt.Dimension(35, 25));
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        startButton.setIcon(startIcon);
        startButton.setPreferredSize(new java.awt.Dimension(35, 25));
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        dPhiSlider.setMinimum(-100);
        dPhiSlider.setValue(100);
        dPhiSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                dPhiSliderStateChanged(evt);
            }
        });

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("LanguageBundle_pt_BR"); // NOI18N
        sliderLabel.setText(bundle.getString("slider.label")); // NOI18N

        viewMenu.setText("Visualizar");

        showVectorROption.setSelected(true);
        showVectorROption.setText(bundle.getString("view.r.vec.option.label")); // NOI18N
        showVectorROption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showVectorROptionActionPerformed(evt);
            }
        });
        viewMenu.add(showVectorROption);

        showVectorVOption.setSelected(true);
        showVectorVOption.setText(bundle.getString("view.v.vec.option.label")); // NOI18N
        showVectorVOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showVectorVOptionActionPerformed(evt);
            }
        });
        viewMenu.add(showVectorVOption);

        showVectorAOption.setSelected(true);
        showVectorAOption.setText(bundle.getString("view.a.vec.option.label")); // NOI18N
        showVectorAOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showVectorAOptionActionPerformed(evt);
            }
        });
        viewMenu.add(showVectorAOption);

        showAngleOption.setSelected(true);
        showAngleOption.setText(bundle.getString("view.angle.option.label")); // NOI18N
        showAngleOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAngleOptionActionPerformed(evt);
            }
        });
        viewMenu.add(showAngleOption);

        menuBar.add(viewMenu);

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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(SVGPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(stopButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(startButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(sliderLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dPhiSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SVGPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(stopButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(startButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dPhiSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(sliderLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Inicia/pausa a animação
    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        animationOn = !animationOn;
        if (animationOn) startButton.setIcon(pauseIcon);
        else startButton.setIcon(startIcon);
    }//GEN-LAST:event_startButtonActionPerformed

    // Pára a animação
    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        animationOn = false;
        rotationHandler.setPhi(0);
        startButton.setIcon(startIcon);
        applyRotation();
    }//GEN-LAST:event_stopButtonActionPerformed

    // Altera o comportamento da animação na cena SVG com base no JSlider
    private void dPhiSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_dPhiSliderStateChanged

        canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(new Runnable(){

            public void run() {
                
                double dPhi = ((double) dPhiSlider.getValue()) * topDPhi / 100;
                rotationHandler.setDPhi(dPhi);

                updateVectorsVAndA();
            }
        });
    }//GEN-LAST:event_dPhiSliderStateChanged

    // Habilita/desabilita a visualização do vetor de posição.
    private void showVectorROptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showVectorROptionActionPerformed
        canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(new Runnable(){

            public void run() {
                if (showVectorROption.isSelected()) {
                    vectorR.setAttribute("opacity","1"); // NOI18N
                    label[VEC_R].setAttribute("opacity","1"); // NOI18N
                } else {
                    vectorR.setAttribute("opacity","0"); // NOI18N
                    label[VEC_R].setAttribute("opacity","0"); // NOI18N
                }
            }
        });
    }//GEN-LAST:event_showVectorROptionActionPerformed

    // Habilita/desabilita a visualização do vetor de velocidade.
    private void showVectorVOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showVectorVOptionActionPerformed
        canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(new Runnable(){

            public void run() {
                if (showVectorVOption.isSelected()) {
                    vectorV.setAttribute("opacity","1"); // NOI18N
                    label[VEC_V].setAttribute("opacity","1"); // NOI18N
                } else {
                    vectorV.setAttribute("opacity","0"); // NOI18N
                    label[VEC_V].setAttribute("opacity","0"); // NOI18N
                }
            }
        });
}//GEN-LAST:event_showVectorVOptionActionPerformed

    // Habilita/desabilita a visualização do vetor de aceleração.
    private void showVectorAOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showVectorAOptionActionPerformed
        canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(new Runnable(){

            public void run() {
                if (showVectorAOption.isSelected()) {
                    vectorA.setAttribute("opacity","1"); // NOI18N
                    label[VEC_A].setAttribute("opacity","1"); // NOI18N
                } else {
                    vectorA.setAttribute("opacity","0"); // NOI18N
                    label[VEC_A].setAttribute("opacity","0"); // NOI18N
                }
            }
        });
    }//GEN-LAST:event_showVectorAOptionActionPerformed

    // Habilita/desabilita a visualização do logotipo do CEPA.
    private void aboutOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutOptionActionPerformed
        this.getGlassPane().setVisible(true);
}//GEN-LAST:event_aboutOptionActionPerformed

    // Habilita/desabilita a visualização do arco/ângulo.
    private void showAngleOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAngleOptionActionPerformed
        canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(new Runnable(){

            public void run() {
                if (showAngleOption.isSelected()) {
                    arc.setAttribute("opacity","1"); // NOI18N
                    arcLabel.setAttribute("opacity","1"); // NOI18N
                    arcReferenceAxis.setAttribute("opacity","1"); // NOI18N
                } else {
                    arc.setAttribute("opacity","0"); // NOI18N
                    arcLabel.setAttribute("opacity","0"); // NOI18N
                    arcReferenceAxis.setAttribute("opacity","0"); // NOI18N
                }
            }
        });
    }//GEN-LAST:event_showAngleOptionActionPerformed

    // Habilita/desabilita a visualização do arco e do ângulo.
    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new UniformCircularMoviment().setVisible(true);
            }
        });
    }

    private void updateVectorsVAndA () {

        // Reduz ou amplia o vetor de velocidade.
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        String transform = vectorV.getAttribute("transform"); // NOI18N
        String translate = new String();
        int a = transform.indexOf("translate"); // NOI18N
        if ( a >= 0 ) {
            int b = transform.indexOf(")", a); // NOI18N
            translate = transform.substring(a, ++b);
        }

        // Define a escala de ampliação ou redução do vetor de velocidade
        // com base na velocidade definida pelo JSlider.
        scale = 2 * rotationHandler.getDPhi();
        if (scale != 0) {
            scale = 1/Math.tanh(scale) - 1/scale;
        }

        vectorV.setAttribute("transform", translate + " scale(" + scale + ")"); // NOI18N

        // Troca o rótulo da velocidade, dependendo dela ser positiva ou negativa.
        if (scale < 0) {
            label[VEC_V] = negativeVLabel;
            positiveVLabel.setAttribute("x", String.valueOf(ulc[X] + 2*viewBoxSize[X])); // NOI18N
            positiveVLabel.setAttribute("y", String.valueOf(ulc[Y] + 2*viewBoxSize[Y])); // NOI18N
        } else {
            label[VEC_V] = positiveVLabel;
            negativeVLabel.setAttribute("x", String.valueOf(ulc[X] + 2*viewBoxSize[X])); // NOI18N
            negativeVLabel.setAttribute("y", String.valueOf(ulc[Y] + 2*viewBoxSize[Y])); // NOI18N
        }

        updateLabelOfVectorV();

        // Reduz ou amplia o vetor de aceleração centrípeta.
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        transform = vectorA.getAttribute("transform"); // NOI18N
        translate = new String();
        a = transform.indexOf("translate"); // NOI18N
        if ( a >= 0 ) {
            int b = transform.indexOf(")", a); // NOI18N
            translate = transform.substring(a, ++b);
        }

        // Define a escala de ampliação ou redução do vetor de aceleração centrípeta
        // com base na velocidade definida pelo JSlider.
        scale = 2 * rotationHandler.getDPhi();
        if (scale != 0) {
            scale = 0.5 + 0.5*(1/Math.tanh(Math.abs(scale)) - 1/Math.abs(scale));
        }

        vectorA.setAttribute("transform", translate + " scale(" + scale + ")"); // NOI18N

        updateLabelOfVectorA();

        if (!animationOn) applyRotation();
    }

    private void loadSVGElements () {

        Element element;

        // Determina a posição dos rótulos \hat r e \hat \phi da base fixa
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        element = document.getElementById("r.hat.label.pos"); // NOI18N
        labelPos[FIXED_HAT_R][X] = Double.parseDouble(element.getAttribute("x")); // NOI18N
        labelPos[FIXED_HAT_R][Y] = Double.parseDouble(element.getAttribute("y")); // NOI18N
        
        element = document.getElementById("p.hat.label.pos"); // NOI18N
        labelPos[FIXED_HAT_PHI][X] = Double.parseDouble(element.getAttribute("x")); // NOI18N
        labelPos[FIXED_HAT_PHI][Y] = Double.parseDouble(element.getAttribute("y")); // NOI18N
        
        fixedBase = document.getElementById("fixed.base"); // NOI18N
        labelPos[FIXED_HAT_R][X] += Double.parseDouble(fixedBase.getAttribute("x")); // NOI18N
        labelPos[FIXED_HAT_R][Y] += Double.parseDouble(fixedBase.getAttribute("y")); // NOI18N
        labelPos[FIXED_HAT_PHI][X] += Double.parseDouble(fixedBase.getAttribute("x")); // NOI18N
        labelPos[FIXED_HAT_PHI][Y] += Double.parseDouble(fixedBase.getAttribute("y")); // NOI18N

        // Determina a posição dos rótulos \hat r e \hat \phi da base móvel
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        freeBase = document.getElementById("free.base"); // NOI18N

        labelPos[FREE_HAT_R][X] = labelPos[FIXED_HAT_R][X];
        labelPos[FREE_HAT_R][Y] = labelPos[FIXED_HAT_R][Y];

        labelPos[FREE_HAT_PHI][X] = labelPos[FIXED_HAT_PHI][X];
        labelPos[FREE_HAT_PHI][Y] = labelPos[FIXED_HAT_PHI][Y];

        freeBasePos[X] = Double.parseDouble(freeBase.getAttribute("x")); // NOI18N
        freeBasePos[Y] = Double.parseDouble(freeBase.getAttribute("y")); // NOI18N

        freeBase.setAttribute("transform", "rotate(" + -rotationHandler.getPhi() + "," + freeBasePos[X] + "," + freeBasePos[Y] + ")"); // NOI18N
        
        // Determina a posição relativa do rótulo \vec r
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        element = document.getElementById("r.label.pos"); // NOI18N
        labelPos[VEC_R][X] = Double.parseDouble(element.getAttribute("x")); // NOI18N
        labelPos[VEC_R][Y] = Double.parseDouble(element.getAttribute("y")); // NOI18N
        vectorR = document.getElementById("r.vec"); // NOI18N
        labelPos[VEC_R][X] += Double.parseDouble(vectorR.getAttribute("x")); // NOI18N
        labelPos[VEC_R][Y] += Double.parseDouble(vectorR.getAttribute("y")); // NOI18N

        // Determina a posição relativa do rótulo \vec v
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        element = document.getElementById("v.label.pos"); // NOI18N
        labelPos[VEC_V][X] = Double.parseDouble(element.getAttribute("x")); // NOI18N
        labelPos[VEC_V][Y] = Double.parseDouble(element.getAttribute("y")); // NOI18N

        vectorV = document.getElementById("v.vec"); // NOI18N
        labelPos[VEC_V][X] += Double.parseDouble(vectorV.getAttribute("x")); // NOI18N
        labelPos[VEC_V][Y] += Double.parseDouble(vectorV.getAttribute("y")); // NOI18N

        double[] translate = getTranslateCoordinates(vectorV.getAttribute("transform")); // NOI18N
        labelPos[VEC_V][X] += translate[X];
        labelPos[VEC_V][Y] += translate[Y];

        // Determina a posição relativa do rótulo \vec a
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        element = document.getElementById("a.label.pos"); // NOI18N
        labelPos[VEC_A][X] = Double.parseDouble(element.getAttribute("x")); // NOI18N
        labelPos[VEC_A][Y] = Double.parseDouble(element.getAttribute("y")); // NOI18N

        vectorA = document.getElementById("a.vec"); // NOI18N
        labelPos[VEC_A][X] += Double.parseDouble(vectorA.getAttribute("x")); // NOI18N
        labelPos[VEC_A][Y] += Double.parseDouble(vectorA.getAttribute("y")); // NOI18N

        // Determina a posição do rótulo da amostra (= a própria amostra)
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        element = document.getElementById("sample.label.pos"); // NOI18N
        labelPos[SAMPLE][X] = Double.parseDouble(element.getAttribute("x")); // NOI18N
        labelPos[SAMPLE][Y] = Double.parseDouble(element.getAttribute("y")); // NOI18N

        // Posiciona o grupo de translação (veja o arquivo SVG) no centro da cena
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        SVGElement root = (SVGElement) ((SVGDocument)document).getRootElement();

        String[] aux = Pattern.compile(" ").split(root.getAttribute("viewBox")); // NOI18N
        
        ulc[X] = Double.parseDouble(aux[0]);
        ulc[Y] = Double.parseDouble(aux[1]);
        viewBoxSize[X] = Double.parseDouble(aux[2]);
        viewBoxSize[Y] = Double.parseDouble(aux[3]);

        fixedBasePos[X] = (ulc[X] + viewBoxSize[X]) / 2; // Calcula a coordenada x do centro da cena.
        fixedBasePos[Y] = (ulc[Y] + viewBoxSize[Y]) / 2; // Calcula a coordenada y do centro da cena.

        element = document.getElementById("translation.group"); // NOI18N
        element.setAttribute("transform", "translate(" + fixedBasePos[X] + "," + fixedBasePos[Y] + ")"); // NOI18N

        // Rotaciona o grupo de rotação (veja o arquivo SVG)
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        rotationGroup = document.getElementById("rotation.group"); // NOI18N
        rotationGroup.setAttribute("transform", "rotate(" + -rotationHandler.getPhi() + ")"); // NOI18N

        arc = document.getElementById("arc");
        arcLabel = document.getElementById("arc.label");
        arcReferenceAxis = document.getElementById("arc.reference.axis");

        // Cria referências para os rótulos (labels) da cena
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        label[FIXED_HAT_R] = document.getElementById("r.hat.label.fixed.base"); // NOI18N
        label[FIXED_HAT_PHI] = document.getElementById("p.hat.label.fixed.base"); // NOI18N
        label[VEC_R] = document.getElementById("r.vec.label"); // NOI18N
        label[VEC_V] = document.getElementById("v.vec.label"); // NOI18N
        label[VEC_A] = document.getElementById("a.vec.label"); // NOI18N
        label[SAMPLE] = document.getElementById("sample.label"); // NOI18N
        label[FREE_HAT_R] = document.getElementById("r.hat.label.free.base"); // NOI18N
        label[FREE_HAT_PHI] = document.getElementById("p.hat.label.free.base"); // NOI18N
        positiveVLabel = document.getElementById("v.vec.label"); // NOI18N
        negativeVLabel = document.getElementById("-v.vec.label"); // NOI18N

        // Inicialmente, coloca o rótulo $\vec v = -v\hat \phi$ fora da view port (ou view box)
        negativeVLabel.setAttribute("x", String.valueOf(ulc[X] + 2 * viewBoxSize[X])); // NOI18N
        negativeVLabel.setAttribute("y", String.valueOf(ulc[Y] + 2 * viewBoxSize[Y])); // NOI18N

        // Registrando os observadores de eventos
        //----------------------------------------------------------------------
		canvas.addSVGLoadEventDispatcherListener (
			new SVGLoadEventDispatcherAdapter () {            
                @Override
		        public void svgLoadEventDispatchStarted (SVGLoadEventDispatcherEvent e) {
                    window = canvas.getUpdateManager().getScriptingEnvironment().createWindow ();
		       }
		    }
		);

        // Observador(es) de eventos do elemento raiz da cena SVG
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        EventTarget rootEventTarget = (EventTarget) root;
        rootEventTarget.addEventListener("SVGLoad", rotationHandler, false); // NOI18N
        rootEventTarget.addEventListener("mousemove", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                if (freeBaseSelected) {
                    Dimension d = SVGPanel.getSize();
                    double margin = (d.getWidth() - d.getHeight()) / 2;

                    DOMMouseEvent mouseEvent = (DOMMouseEvent) evt;

                    freeBasePos[X] = ulc[X] + (mouseEvent.getClientX() - Math.max(0,margin)) * viewBoxSize[X] / Math.min(d.getHeight(), d.getWidth());
                    freeBasePos[Y] = ulc[Y] + (mouseEvent.getClientY() + Math.min(0,margin)) * viewBoxSize[Y] / Math.min(d.getHeight(), d.getWidth());

                    if (Util.distance(freeBasePos,fixedBasePos) < snapDistance) {
                        freeBasePos[X] = fixedBasePos[X];
                        freeBasePos[Y] = fixedBasePos[Y];

                        fixedBase.setAttribute("opacity", "0"); // NOI18N
                        label[FIXED_HAT_R].setAttribute("opacity", "0"); // NOI18N
                        label[FIXED_HAT_PHI].setAttribute("opacity", "0"); // NOI18N
                    } else {
                        fixedBase.setAttribute("opacity", "0.3"); // NOI18N
                        label[FIXED_HAT_R].setAttribute("opacity", "0.3"); // NOI18N
                        label[FIXED_HAT_PHI].setAttribute("opacity", "0.3"); // NOI18N
                    }

                    freeBase.setAttribute("x", String.valueOf(freeBasePos[X])); // NOI18N
                    freeBase.setAttribute("y", String.valueOf(freeBasePos[Y])); // NOI18N

                    if (!animationOn) applyRotation();
                }
            }
        }, false);

        // Observador(es) de eventos da base móvel (arrastável)
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        EventTarget freeBaseEventTarget = (EventTarget) freeBase;
        freeBaseEventTarget.addEventListener("click", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                freeBaseSelected = !freeBaseSelected;
            }
        }, false);
    }

    // Rotaciona o vetor r[] ao redor do eixo axis[] de um ângulo angle (em radianos).
    private double[] R ( final double[] r, final double angle, final double[] axis ) {
        return new double[]{
            + r[X] * Math.cos(angle) + r[Y] * Math.sin(angle) + axis[X],
            - r[X] * Math.sin(angle) + r[Y] * Math.cos(angle) + axis[Y]
        };
    }

    // Aplica a rotação atual ao grupo de rotação, à base móvel (arrastável) e
    // redefine (e reatribui) as posições dos rótulos com base nesta rotação.
    private void applyRotation () {

        double phi = rotationHandler.getPhi();
        double[] rotated = {0,0};
        
        rotationGroup.setAttribute("transform", "rotate(" + -phi + ")"); // NOI18N
        freeBase.setAttribute("transform", " rotate(" + -phi + "," + freeBasePos[X] + "," + freeBasePos[Y] + ")"); // NOI18N
        
        for ( short i = 0; i < 8; i++ ) {

            if ( i == FREE_HAT_R || i == FREE_HAT_PHI ) {
                rotated = R(labelPos[i],Math.toRadians(phi),freeBasePos);
            } else {
                rotated = R(labelPos[i],Math.toRadians(phi),fixedBasePos);
            }

            label[i].setAttribute("x", String.valueOf(rotated[X])); // NOI18N
            label[i].setAttribute("y", String.valueOf(rotated[Y])); // NOI18N
        }

        updateArc();
    }

    // Retorna as coordenadas SVG do elemento "translate" do atributo "transform"
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

    private void updateLabelOfVectorV () {
        Element element = document.getElementById("v.label.pos"); // NOI18N
        labelPos[VEC_V][X] = scale * Double.parseDouble(element.getAttribute("x")); // NOI18N
        labelPos[VEC_V][Y] = scale * Double.parseDouble(element.getAttribute("y")); // NOI18N
        element = document.getElementById("v.vec"); // NOI18N

        labelPos[VEC_V][X] += Double.parseDouble(element.getAttribute("x")); // NOI18N
        labelPos[VEC_V][Y] += Double.parseDouble(element.getAttribute("y")); // NOI18N

        double[] translate = getTranslateCoordinates(element.getAttribute("transform")); // NOI18N
        labelPos[VEC_V][X] += translate[X];
        labelPos[VEC_V][Y] += translate[Y];
    }

    private void updateLabelOfVectorA () {
        Element element = document.getElementById("a.label.pos"); // NOI18N
        labelPos[VEC_A][X] = scale * Double.parseDouble(element.getAttribute("x")); // NOI18N
        labelPos[VEC_A][Y] = scale * Double.parseDouble(element.getAttribute("y")); // NOI18N
        element = document.getElementById("a.vec"); // NOI18N

        labelPos[VEC_A][X] += Double.parseDouble(element.getAttribute("x")); // NOI18N
        labelPos[VEC_A][Y] += Double.parseDouble(element.getAttribute("y")); // NOI18N

        double[] translate = getTranslateCoordinates(element.getAttribute("transform")); // NOI18N
        labelPos[VEC_A][X] += translate[X];
        labelPos[VEC_A][Y] += translate[Y];
    }

    private void updateArc () {

        double phi = -Math.toRadians(rotationHandler.getPhi());

        // O parâmetro fAfS vem de "large Arc Flag/Sweep Flag". É um par de parâmetros
        // utilizado na tag <path/> que desenha o arco do ângulo \phi. Veja a especificação
        // da tag em http://www.w3.org/TR/SVG11/paths.html (comando "A", de arc).
        String fAfS = new String("0,0");
        if ((phi > 0 && phi < Math.PI) || (phi > -2*Math.PI && phi < -Math.PI)) fAfS = "1,0";

        arc.setAttribute( "d", "M" + (fixedBasePos[X]+arcRadius) + "," + fixedBasePos[Y] + " " +
            "A" + arcRadius + "," + arcRadius + ",0," + fAfS + "," + (fixedBasePos[X]+arcRadius*Math.cos(phi)) + "," + (fixedBasePos[Y]+arcRadius*Math.sin(phi)) );

        // Atualiza o rótulo da coordenada phi da amostra. A orientação do ângulo na tela é
        // inversa àquela que estamos acostumados. Por isso a conversão.
        double tmp = Math.toDegrees( phi > 0 ? 2*Math.PI-phi : -phi );
        String phiLabelText = numberFormat.format(tmp); 
        
        // Calcula a posição angular do rótulo da coordenada ângulo da amostra.
        double phiLabelAngularPos = ( phi < 0 ? phi + Math.abs(phi)/2 : -(2*Math.PI - phi)/2 );

        // Atualiza o rótulo da coordenada phi da amostra.
        arcLabel.setAttribute("x", "" + (fixedBasePos[X] + arcLabelRadius * Math.cos(phiLabelAngularPos)));
        arcLabel.setAttribute("y", "" + (fixedBasePos[Y] + arcLabelRadius * Math.sin(phiLabelAngularPos)));
        arcLabel.setTextContent(phiLabelText + "º");
        if (tmp < 10) arcLabel.setAttribute("opacity", "0");
        else arcLabel.setAttribute("opacity", "1");
    }

    private class RotationHandler implements EventListener {

        private double phi = 0, dPhi = 1;

        public void handleEvent(Event evt) {
            window.setInterval ( new Runnable() {

                public void run() {
                    if (!animationOn) return;
                    phi = (phi + dPhi) % 360;
                    applyRotation();
                }
            }, dt);
        }

        public synchronized void setPhi ( double phi ) {
            this.phi = phi;
        }

        /**
         * @return the phi
         */
        public double getPhi() {
            return phi;
        }

        /**
         * @return the dPhi
         */
        public double getDPhi() {
            return dPhi / ((double) dt / 1000);
        }

        /**
         * @param dPhi the dPhi to set
         */
        public synchronized void setDPhi(double dPhi) {
            this.dPhi = dPhi * ((double) dt / 1000);
        }
    }

    // Observador de mudança de idioma
    private class LanguageObserver implements Observer {
        public void update(Observable o, Object arg) {
            viewMenu.setText(idiom.getString("view.menu.label")); // NOI18N
            showVectorROption.setText(idiom.getString("view.r.vec.option.label")); // NOI18N
            showVectorVOption.setText(idiom.getString("view.v.vec.option.label")); // NOI18N
            showVectorAOption.setText(idiom.getString("view.a.vec.option.label")); // NOI18N
            showAngleOption.setText(idiom.getString("view.angle.option.label")); // NOI18N

            aboutMenu.setText(idiom.getString("about.menu.label")); // NOI18N
            aboutOption.setText(idiom.getString("about.option.label")); // NOI18N

            sliderLabel.setText(idiom.getString("slider.label")); // NOI18N
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
    private javax.swing.JPanel SVGPanel;
    private javax.swing.JMenu aboutMenu;
    private javax.swing.JMenuItem aboutOption;
    private javax.swing.JSlider dPhiSlider;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JCheckBoxMenuItem showAngleOption;
    private javax.swing.JCheckBoxMenuItem showVectorAOption;
    private javax.swing.JCheckBoxMenuItem showVectorROption;
    private javax.swing.JCheckBoxMenuItem showVectorVOption;
    private javax.swing.JLabel sliderLabel;
    private javax.swing.JButton startButton;
    private javax.swing.JButton stopButton;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables

    private JMenu languageMenu = new JMenu();
    private JRadioButtonMenuItem[] radioButton;

    private JSVGCanvas canvas = new JSVGCanvas();
    private Document document; // The SVG document
    private Window window;

    private final short FIXED_HAT_R = 0, FIXED_HAT_PHI = 1, VEC_R = 2, VEC_V = 3, VEC_A = 4, SAMPLE = 5, FREE_HAT_R = 6, FREE_HAT_PHI = 7;
    private final short X = 0, Y = 1;
    private final long dt = 30; // Intervalo entre dois frames (quadros), em mili-segundos
    private double labelPos[][] = new double[8][2];
    private double[] fixedBasePos = new double[2], freeBasePos = new double[2], ulc = new double[2], viewBoxSize = new double[2];
    
    // Os oito rótulos relevantes: \hat r (um da base fixa e outro da arrastável),
    // \hat \phi (um da base fixa e outro da arrastável), \vec r, +\vec v, -\vec v, \vec a,
    private Element[] label = new Element[8];

    private Element vectorR, // O vetor de posição
                    vectorV, // O vetor de velocidade
                    vectorA, // O vetor de aceleração (centrípeta)
                    rotationGroup, // O grupo de elementos sensíveis à rotação
                    freeBase, // O grupo de elementos que compõem a base móvel (arrastável)
                    fixedBase, // O grupo de elementos que compõem a base fixa (no centro da cena)
                    positiveVLabel, // Referência para o rótulo $+v \hat v$
                    negativeVLabel, // Referência para o rótulo $-v \hat v$
                    arc, // O arco do ângulo
                    arcLabel, // O rótulo do arco (ângulo)
                    arcReferenceAxis; // O eixo de referência da medida do ângulo.

    private boolean animationOn = false;
    private boolean freeBaseSelected = false;
    private ImageIcon startIcon, pauseIcon, stopIcon;
    private RotationHandler rotationHandler;

    private double snapDistance = 0.3; // A distância de atração entre as bases móvel e fixa
    private double scale = 1; // Escala inicial dos vetores vectorV e vectorA
    private double topDPhi = 20; // Velocidade angular máxima (º/s), associada ao fundo de escala do JSlider

    private Observable languageObservable;
    private Locale[] availableLocales;
    private ResourceBundle idiom;

    private double arcRadius = 2, arcLabelRadius = 2.7;
    private NumberFormat numberFormat;
}

