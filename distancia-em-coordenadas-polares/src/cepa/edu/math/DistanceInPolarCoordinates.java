package cepa.edu.math;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
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

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
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
public class DistanceInPolarCoordinates extends javax.swing.JFrame {


    @SuppressWarnings("static-access") // NOI18N
    public DistanceInPolarCoordinates() {
        setSize(500,500);

        initComponents();
        jMenuBar1.add(new JPanel());
        jMenuBar1.add(languageMenu);

        // Define look & feel
        try{
        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        	SwingUtilities.updateComponentTreeUI(this);
        }
        catch( Exception e ){/*Nada*/}

        numberFormat = (new DecimalFormat("###,##0.0")).getInstance(new Locale("en","US")); // NOI18N
        radiansFormat = (new DecimalFormat("###,##0.00")).getInstance(new Locale("en","US")); // NOI18N
        numberFormat.setMaximumFractionDigits(1);

        InputStream stream = getClass().getClassLoader().getResourceAsStream("app.properties"); // NOI18N
        Properties properties = new Properties();

        /***********************************************************************
         Carrega o arquivo SVG para o JPanel.
         **********************************************************************/
		try {
            properties.load(stream);

		    String parser = XMLResourceDescriptor.getXMLParserClassName();
		    SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
    
            ClassLoader classLoader = this.getClass().getClassLoader();
            URL url = classLoader.getResource(properties.getProperty("scene.file")); // NOI18N
            String uri = url.toURI().toString();
            
		    document = (SVGDocument) f.createDocument(uri);
		}

		catch (IOException ex) { Logger.getLogger(DistanceInPolarCoordinates.class.getName()).log(Level.SEVERE, null, ex); }        catch (URISyntaxException ex) { Logger.getLogger(DistanceInPolarCoordinates.class.getName()).log(Level.SEVERE, null, ex); }

		canvas.setDocumentState (JSVGCanvas.ALWAYS_DYNAMIC); // Torna dinâmico o canvas.
		canvas.setDocument (document); // Associa a cena SVG (propriedade scene.file) ao canvas.
		canvas.setEnableImageZoomInteractor(false); // TODO: desabilita o quê?
		canvas.setEnablePanInteractor(false); // Desabilita a opção de arrastar a cena SVG.
		canvas.setEnableRotateInteractor(false); // Desabilita a opção de rotacionar a cena SVG.
		canvas.setEnableZoomInteractor(false); // Desabilita a opção de ampliar/reduzir a cena SVG.        
        
        SVGPanel.setLayout(new BorderLayout());
        SVGPanel.add(canvas,BorderLayout.CENTER);

        // ------------------------------------------------
        // ----- Início da configuração do menu de idiomas.
        languageObservable = new LanguageObservable();
        languageObservable.addObserver(new LanguageObserver());

        idiom = ResourceBundle.getBundle( properties.getProperty("language.bundle"), new Locale("en", "US")); // NOI18N

        languageMenu.setIcon(getIcon(properties.getProperty("language.menu.icon"))); // NOI18N
        availableLocales = getLocales(properties.getProperty("available.locales")); // NOI18N

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
		referenceFrame = document.getElementById("rframe"); // NOI18N
        ptoA = document.getElementById("ptoA"); // NOI8N
        ptoE = document.getElementById("ptoE"); // NOI8N
        xyAxis = document.getElementById("frame"); // NOI18N
        ptoC1 = document.getElementById("ptoC1"); // NOI18N
        ptoC2 = document.getElementById("ptoC2"); // NOI18N
        xyPair1 = document.getElementById("xyLabel1"); // NOI18N
        xyPair2 = document.getElementById("xyLabel2"); // NOI18N
        rLine1 = document.getElementById("rLine1"); // NOI18N
        arc1 = document.getElementById("arc1"); // NOI18N
        rLine2 = document.getElementById("rLine2"); // NOI18N
        arc2 = document.getElementById("arc2"); // NOI18N
        ptoATip = document.getElementById("ptoA-tip"); // NOI18N
        ptoETip = document.getElementById("ptoE-tip"); // NOI18N
        dLine = document.getElementById("dLine"); // NOI18N
        dLabel = document.getElementById("dLabel"); // NOI18N
        samplesTip = document.getElementById("samples-tip"); // NOI18N
        background = document.getElementById("background"); // NOI18N

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

        updateCanvas();

        /***********************************************************************
         Configuração dos observadores de eventos.
         **********************************************************************/

        EventTarget backgroundTarget = (EventTarget) background;
        backgroundTarget.addEventListener("click", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                if (ptoESelected) {
                    dragging = !dragging;
                    ptoESelected = !ptoESelected;

                    if (ptoESelected) ptoE.setAttribute("fill", "red"); // NOI18N
                    else ptoE.setAttribute("fill", "blue"); // NOI18N
                }
            }
        }, false);

        // Configura os observadores dos eventos do mouse no ponto A (manete de translação).
        EventTarget ptoAEventTarget = (EventTarget) ptoA;
        PtoAEventListener ptoAEventListener = new PtoAEventListener();
        ptoAEventTarget.addEventListener("click", ptoAEventListener, false); // NOI18N
        ptoAEventTarget.addEventListener("mouseover", new EventListener(){ // NOI8N

            public void handleEvent(Event evt) {
                if (!dragging) {
                    ptoATip.setTextContent(idiom.getString("ptoA.tip")); // NOI8N
                    samplesTip.setTextContent(""); // NOI18N
                    ptoETip.setTextContent(""); // NOI18N
                }
            }
        }, false);
        ptoAEventTarget.addEventListener("mouseout", new EventListener(){ // NOI8N

            public void handleEvent(Event evt) {
                ptoATip.setTextContent(""); // NOI8N
            }
        }, false);

        // Configura os observadores dos eventos do mouse no ponto C1 (amostra 1).
        EventTarget ptoC1EventTarget = (EventTarget) ptoC1;
        PtoC1EventListener ptoC1EventListener = new PtoC1EventListener();
        ptoC1EventTarget.addEventListener("click", ptoC1EventListener, false); // NOI18N
        ptoC1EventTarget.addEventListener("mouseover", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                if (!dragging) {
                    ptoATip.setTextContent(""); // NOI8N
                    samplesTip.setTextContent(idiom.getString("samples.tip")); // NOI18N
                    ptoETip.setTextContent(""); // NOI18N
                }
            }
        }, false);
        ptoC1EventTarget.addEventListener("mouseout", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                samplesTip.setTextContent(""); // NOI18N
            }
        }, false);

        // Configura os observadores dos eventos do mouse no ponto C2 (amostra 2).
        EventTarget ptoC2EventTarget = (EventTarget) ptoC2;
        PtoC2EventListener ptoC2EventListener = new PtoC2EventListener();
        ptoC2EventTarget.addEventListener("click", ptoC2EventListener, false); // NOI18N
        ptoC2EventTarget.addEventListener("mouseover", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                if (!dragging) {
                    ptoATip.setTextContent(""); // NOI8N                    
                    samplesTip.setTextContent(idiom.getString("samples.tip")); // NOI8N
                    ptoETip.setTextContent(""); // NOI18N
                }
            }
        }, false);
        ptoC2EventTarget.addEventListener("mouseout", new EventListener(){ // NOI18N

            public void handleEvent(Event evt) {
                samplesTip.setTextContent(""); // NOI18N
            }
        }, false);

        // Configura os observadores dos eventos do mouse no ponto E (manete de rotação).
        EventTarget ptoEEventTarget = (EventTarget) ptoE;
        PtoEEventListener ptoEEventListener = new PtoEEventListener();
        ptoEEventTarget.addEventListener("click", ptoEEventListener, false); // NOI18N
        ptoEEventTarget.addEventListener("mouseover", new EventListener(){ // NOI8N

            public void handleEvent(Event evt) {
                if (!dragging) {
                    ptoATip.setTextContent(""); // NOI8N
                    samplesTip.setTextContent(""); // NOI8N
                    ptoETip.setTextContent(idiom.getString("ptoE.tip")); // NOI8N
                }
            }
        }, false);
        ptoEEventTarget.addEventListener("mouseout", new EventListener(){ // NOI8N

            public void handleEvent(Event evt) {
                ptoETip.setTextContent(""); // NOI8N
            }
        }, false);

        // Configura os observadores dos eventos do mouse na raiz do documento SVG.
        EventTarget rootEventTarget = (EventTarget) root;
        rootEventTarget.addEventListener("mousemove", new EventListener(){ // NOI18N

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

                    ptoA.setAttribute("x", "" + xLinha); // NOI18N
                    ptoA.setAttribute("y", "" + yLinha); // NOI18N

                    ptoE.setAttribute("x", "" + xLinha); // NOI18N
                    ptoE.setAttribute("y", "" + yLinha); // NOI18N

                    xyAxis.setAttribute("x", "" + xLinha); // NOI18N
                    xyAxis.setAttribute("y", "" + yLinha); // NOI18N
                }
                else if (ptoESelected) {
                    theta = Math.atan2(mousePos[Y]-ptoAPos[Y], mousePos[X]-ptoAPos[X]);
                    if ( theta < 0 ) theta += 2*Math.PI; // Converte [-\pi,\pi] -> [0,2\pi]
                }
                else if (ptoC1Selected) {
                    ptoC1Pos[X] = mousePos[X];
                    ptoC1Pos[Y] = mousePos[Y];

                    ptoC1.setAttribute("x", "" + ptoC1Pos[X]); // NOI18N
                    ptoC1.setAttribute("y", "" + ptoC1Pos[Y]); // NOI18N
                }
                else if (ptoC2Selected) {
                    ptoC2Pos[X] = mousePos[X];
                    ptoC2Pos[Y] = mousePos[Y];

                    ptoC2.setAttribute("x", "" + ptoC2Pos[X]); // NOI18N
                    ptoC2.setAttribute("y", "" + ptoC2Pos[Y]); // NOI18N
                }

                // Atualiza a rotação do sistema de coordenadas. Esta operação deve ser feita sempre que o ângulo
                // da rotação ou as coordenadas do eixo de rotação mudar. Isto é, sempre que o usuário arrastar as
                // manetes de rotação ou translação.
                referenceFrame.setAttribute("transform", "rotate(" + Math.toDegrees(theta) + "," + ptoAPos[X] + "," + ptoAPos[Y] + ")"); // NOI18N

                // Atualiza os elementos dependentes.
                updateCanvas();
            }
        }, false);

        setGlassPane(new LogoPanel(properties.getProperty("logo.file"))); // NOI18N
    }


    private void updateCanvas () {
        /*----------------------------------------------------------------------
         Atualizando as coordenadas dos pontos B1 e D1.
         ---------------------------------------------------------------------*/

        // Lê da imagem SVG a posição inicial do ponto C1 (amostra 1).
        ptoC1Pos[X] = Double.parseDouble(ptoC1.getAttribute("x")); // NOI18N
        ptoC1Pos[Y] = Double.parseDouble(ptoC1.getAttribute("y")); // NOI18N

        double distAC1 = distance(ptoC1Pos,ptoAPos);
        double phi1 = Math.atan2(ptoC1Pos[Y]-ptoAPos[Y],ptoC1Pos[X]-ptoAPos[X]);
        if ( phi1 < 0 ) phi1 += 2*Math.PI; // Converte [-\pi,\pi] -> [0,2\pi]
        phi1 = phi1 - theta;

        // Calcula as coordenadas dos pontos B1 e D1.
        ptoB1Pos[X] = ptoAPos[X] + arc1Radius * Math.cos(phi1+theta);
        ptoB1Pos[Y] = ptoAPos[Y] + arc1Radius * Math.sin(phi1+theta);

        ptoD1Pos[X] = ptoAPos[X] + arc1Radius * Math.cos(theta);
        ptoD1Pos[Y] = ptoAPos[Y] + arc1Radius * Math.sin(theta);

        // O parâmetro fAfS vem de "large Arc Flag/Sweep Flag". É um par de parâmetros
        // utilizado na tag <path/> que desenha o arco do ângulo \phi. Veja a especificação
        // da tag em http://www.w3.org/TR/SVG11/paths.html (comando "A", de arc).
        String fAfS = new String("0,0"); // NOI18N
        if ((phi1 > 0 && phi1 < Math.PI) || (phi1 > -2*Math.PI && phi1 < -Math.PI)) fAfS = "1,0"; // NOI18N

        arc1.setAttribute( "d", "M" + ptoD1Pos[X] + "," + ptoD1Pos[Y] + " " + // NOI18N
            "A" + arc1Radius + "," + arc1Radius + ",0," + fAfS + "," + ptoB1Pos[X] + "," + ptoB1Pos[Y] ); // NOI18N

        // Atualiza a linha radial que liga (0,0) à amostra 1 (ponto C1)
        rLine1.setAttribute("x1", "" + ptoAPos[X]); // NOI18N
        rLine1.setAttribute("x2", "" + ptoC1Pos[X]); // NOI18N
        rLine1.setAttribute("y1", "" + ptoAPos[Y]); // NOI18N
        rLine1.setAttribute("y2", "" + ptoC1Pos[Y]); // NOI18N

        // Atualiza o rótulo da coordenada phi da amostra. A orientação do ângulo na tela é
        // inversa àquela que estamos acostumados. Por isso a conversão.
        double tmp = phi1 > 0 ? 2*Math.PI-phi1 : -phi1 ;
        String phiLabelText = null;
        if (!anglesGivenInRadians) {
            tmp = Math.toDegrees(tmp);
            phiLabelText = numberFormat.format(tmp);
        } else {
            phiLabelText = radiansFormat.format(tmp);
        }

        // Calcula o offset para o posicionamento do rótulo par ordenado (r,phi) da amostra.
        Element aux = document.getElementById("pC1"); // NOI18N
        ptoC1LabelOffset[X] = Double.parseDouble(aux.getAttribute("width")); // NOI18N
        ptoC1LabelOffset[Y] = Double.parseDouble(aux.getAttribute("height")); // NOI18N

        // Atualiza o par coordenado (r,phi) da amostra.
        xyPair1.setAttribute("x", "" + (ptoC1Pos[X]+ptoC1LabelOffset[X]/2)); // NOI18N
        xyPair1.setAttribute("y", "" + (ptoC1Pos[Y]-ptoC1LabelOffset[Y]/2)); // NOI18N
        xyPair1.setTextContent("(" + numberFormat.format(distAC1) + " , " + phiLabelText + ( anglesGivenInRadians ? " rad)" : "º )") ); // NOI18N

        /*----------------------------------------------------------------------
         Atualizando as coordenadas dos pontos B2 e D2.
         ---------------------------------------------------------------------*/

        // Lê da imagem SVG a posição inicial do ponto C1 (amostra 1).
        ptoC2Pos[X] = Double.parseDouble(ptoC2.getAttribute("x")); // NOI18N
        ptoC2Pos[Y] = Double.parseDouble(ptoC2.getAttribute("y")); // NOI18N

        double distAC2 = distance(ptoC2Pos,ptoAPos);
        double phi2 = Math.atan2(ptoC2Pos[Y]-ptoAPos[Y],ptoC2Pos[X]-ptoAPos[X]);
        if ( phi2 < 0 ) phi2 += 2*Math.PI; // Converte [-\pi,\pi] -> [0,2\pi]
        phi2 = phi2 - theta;

        // Calcula as coordenadas dos pontos B2 e D2.
        ptoB2Pos[X] = ptoAPos[X] + arc2Radius * Math.cos(phi2+theta);
        ptoB2Pos[Y] = ptoAPos[Y] + arc2Radius * Math.sin(phi2+theta);

        ptoD2Pos[X] = ptoAPos[X] + arc2Radius * Math.cos(theta);
        ptoD2Pos[Y] = ptoAPos[Y] + arc2Radius * Math.sin(theta);

        // O parâmetro fAfS vem de "large Arc Flag/Sweep Flag". É um par de parâmetros
        // utilizado na tag <path/> que desenha o arco do ângulo \phi. Veja a especificação
        // da tag em http://www.w3.org/TR/SVG11/paths.html (comando "A", de arc).
        fAfS = new String("0,0"); // NOI18N
        if ((phi2 > 0 && phi2 < Math.PI) || (phi2 > -2*Math.PI && phi2 < -Math.PI)) fAfS = "1,0"; // NOI18N

        arc2.setAttribute( "d", "M" + ptoD2Pos[X] + "," + ptoD2Pos[Y] + " " + // NOI18N
            "A" + arc2Radius + "," + arc2Radius + ",0," + fAfS + "," + ptoB2Pos[X] + "," + ptoB2Pos[Y] ); // NOI18N

        // Atualiza a linha radial que liga (0,0) à amostra 1 (ponto C1)
        rLine2.setAttribute("x1", "" + ptoAPos[X]); // NOI18N
        rLine2.setAttribute("x2", "" + ptoC2Pos[X]); // NOI18N
        rLine2.setAttribute("y1", "" + ptoAPos[Y]); // NOI18N
        rLine2.setAttribute("y2", "" + ptoC2Pos[Y]); // NOI18N

        // Atualiza o rótulo da coordenada phi da amostra. A orientação do ângulo na tela é
        // inversa àquela que estamos acostumados. Por isso a conversão.
        tmp = phi2 > 0 ? 2*Math.PI-phi2 : -phi2 ;
        if (!anglesGivenInRadians) {
            tmp = Math.toDegrees(tmp);
            phiLabelText = numberFormat.format(tmp);
        } else {
            phiLabelText = radiansFormat.format(tmp);
        }

        // Calcula o offset para o posicionamento do rótulo par ordenado (r,phi) da amostra.
        aux = document.getElementById("pC2"); // NOI18N
        ptoC2LabelOffset[X] = Double.parseDouble(aux.getAttribute("width")); // NOI18N
        ptoC2LabelOffset[Y] = Double.parseDouble(aux.getAttribute("height")); // NOI18N

        // Atualiza o par coordenado (r,phi) da amostra.
        xyPair2.setAttribute("x", "" + (ptoC2Pos[X]+ptoC2LabelOffset[X]/2)); // NOI18N
        xyPair2.setAttribute("y", "" + (ptoC2Pos[Y]-ptoC2LabelOffset[Y]/2)); // NOI18N
        xyPair2.setTextContent("(" + numberFormat.format(distAC2) + " , " + phiLabelText + ( anglesGivenInRadians ? " rad)" : "º )")); // NOI18N

        /*----------------------------------------------------------------------
         Atualizando a reta que liga as duas amostras.
         ---------------------------------------------------------------------*/

        dLine.setAttribute("x1", "" + ptoC1Pos[X]); // NOI18N
        dLine.setAttribute("x2", "" + ptoC2Pos[X]); // NOI18N
        dLine.setAttribute("y1", "" + ptoC1Pos[Y]); // NOI18N
        dLine.setAttribute("y2", "" + ptoC2Pos[Y]); // NOI18N

        dLabel.setAttribute("x", "" + (ptoC1Pos[X] + ptoC2Pos[X])/2 ); // NOI18N
        dLabel.setAttribute("y", "" + (ptoC1Pos[Y] + ptoC2Pos[Y])/2 ); // NOI18N
        dLabel.setTextContent(numberFormat.format(distance(ptoC1Pos,ptoC2Pos)));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked") // NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        SVGPanel = new javax.swing.JPanel(new BorderLayout());
        jPanel1 = new javax.swing.JPanel();
        degreeRadioButton = new javax.swing.JRadioButton();
        degreeRadioButton.setSelected(true);
        radiansRadioButton = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        aboutMenu = new javax.swing.JMenu();
        aboutOption = new javax.swing.JMenuItem();

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        buttonGroup1.add(degreeRadioButton);
        degreeRadioButton.setText("graus");
        degreeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                degreeRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(radiansRadioButton);
        radiansRadioButton.setText("radianos");
        radiansRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radiansRadioButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Ângulos em");

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("LanguageBundle_pt_BR"); // NOI18N
        aboutMenu.setActionCommand(bundle.getString("about.menu.title")); // NOI18N
        aboutMenu.setLabel(bundle.getString("about.menu.title")); // NOI18N

        aboutOption.setText(bundle.getString("about.option.title")); // NOI18N
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(SVGPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(degreeRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radiansRadioButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SVGPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(degreeRadioButton)
                    .addComponent(radiansRadioButton)
                    .addComponent(jLabel1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void aboutOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutOptionActionPerformed
        getGlassPane().setVisible(true);
}//GEN-LAST:event_aboutOptionActionPerformed

    // Aqui o método updateCanvas() deve ser chamado de dentro da thread UpdateManager.
    private void degreeRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_degreeRadioButtonActionPerformed
        anglesGivenInRadians = false;
        canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(new Runnable(){

            public void run() {
                updateCanvas();
            }
        });
        
}//GEN-LAST:event_degreeRadioButtonActionPerformed

    // Aqui o método updateCanvas() deve ser chamado de dentro da thread UpdateManager.
    private void radiansRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radiansRadioButtonActionPerformed
        anglesGivenInRadians = true;
       canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(new Runnable(){

            public void run() {
                updateCanvas();
            }
        });
    }//GEN-LAST:event_radiansRadioButtonActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DistanceInPolarCoordinates().setVisible(true);
            }
        });
    }

    // Observador de eventos do ponto A (manete de rotação).
    private class PtoAEventListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            if ( !ptoC1Selected & !ptoC2Selected & !ptoESelected ) {
                dragging = !dragging;
                ptoASelected = !ptoASelected;

                if (ptoASelected) ptoA.setAttribute("fill", "red"); // NOI18N
                else ptoA.setAttribute("fill", "blue"); // NOI18N
            }
        }
    }

    // Observador de eventos do ponto C1 (amostra 1).
    private class PtoC1EventListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            if ( !ptoASelected & !ptoC2Selected & !ptoESelected ) {
                dragging = !dragging;
                ptoC1Selected = !ptoC1Selected;

                if (ptoC1Selected) ptoC1.setAttribute("opacity", "0.5"); // NOI18N
                else ptoC1.setAttribute("opacity", "1.0"); // NOI18N
            }
        }
    }

    // Observador de eventos do ponto C2 (amostra 2).
    private class PtoC2EventListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            if ( !ptoASelected & !ptoC1Selected & !ptoESelected ) {
                dragging = !dragging;
                ptoC2Selected = !ptoC2Selected;

                if (ptoC2Selected) ptoC2.setAttribute("opacity", "0.5"); // NOI18N
                else ptoC2.setAttribute("opacity", "1.0"); // NOI18N
            }
        }
    }

    // Observador de eventos do ponto E (manete de rotação).
    private class PtoEEventListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            if ( !ptoASelected & !ptoC1Selected & !ptoC2Selected ) {
                dragging = !dragging;
                ptoESelected = !ptoESelected;

                if (ptoESelected) ptoE.setAttribute("fill", "red"); // NOI18N
                else ptoE.setAttribute("fill", "blue"); // NOI18N
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


    /*
     * Retorna a distância entre dois pontos (x,y), representados por vetores.
     */
    private double distance( final double[] pointA, final double[] pointB ) {
        return Math.sqrt(Math.pow(pointA[X]-pointB[X],2)+Math.pow(pointA[Y]-pointB[Y],2));
    }

    /*
     * Retorna o ícone, objeto de ImageIcon, associado ao arquivo de nome filename.
     */
    private ImageIcon getIcon( final String filename ){

        URL url = null;

        ClassLoader classLoader = this.getClass().getClassLoader();
        url = classLoader.getResource(filename);

        if ( url == null ) return null;
        else return new ImageIcon(url);
    }

    /*
     * getLocales("pt_BR en_US it_IT") = { pt_BR, en_US, it_IT } (os elementos da matriz são objetos da classe Locale).
     */
    private Locale[] getLocales( String list ){

        Vector<Locale> vector = new Vector<Locale>();

        String[] locales = Pattern.compile(" ").split(list); // NOI18N

        for ( int i = 0; i < locales.length; i++ ){
            String[] codes = Pattern.compile("_").split(locales[i]); // NOI18N
            if ( codes.length == 2 ) vector.add( new Locale(codes[0],codes[1]) );
        }

        return vector.toArray(new Locale[locales.length]);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel SVGPanel;
    private javax.swing.JMenu aboutMenu;
    private javax.swing.JMenuItem aboutOption;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton degreeRadioButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton radiansRadioButton;
    // End of variables declaration//GEN-END:variables

	private static final short X = 0, Y = 1;

	private JSVGCanvas canvas = new JSVGCanvas ();
	private Document document; // The SVG document

    private Element
        ptoA,
        ptoC1,
        ptoC2,
        ptoE,
        referenceFrame,
        xyAxis,
        rLine1,
        rLine2,
        arc1,
        arc2,
        xyPair1,
        xyPair2,
        ptoATip,
        ptoETip,
        samplesTip,
        dLine,
        dLabel,
        background;

    private boolean ptoASelected = false,
                    ptoC1Selected = false,
                    ptoESelected = false,
                    ptoC2Selected = false,
                    dragging = false,
                    anglesGivenInRadians = false;

    private double theta = 0;

    private double[]
        ptoAPos  = {0,0},
        ptoB1Pos = {0,0},
        ptoB2Pos = {0,0},
        ptoC1Pos = {0,0},
        ptoC2Pos = {0,0},
        ptoD1Pos = {0,0},
        ptoD2Pos = {0,0},
        mousePos = {0,0};

    private double[]
        SVGViewBoxSize   = {40,40},
        ptoC1LabelOffset = { 0, 0},
        ptoC2LabelOffset = { 0, 0},
        ulc              = { 0, 0};

    private NumberFormat numberFormat, radiansFormat;


    private double arc1Radius = 2, arc2Radius = 4;

    private ResourceBundle idiom;

    private JMenu languageMenu = new JMenu();
    private JRadioButtonMenuItem[] radioButton;

    private Locale[] availableLocales;

    private class LanguageObserver implements Observer {

        public void update(Observable o, Object arg) {
            aboutMenu.setText(idiom.getString("about.menu.title")); // NOI18N
            aboutOption.setText(idiom.getString("about.option.title")); // NOI18N;
            degreeRadioButton.setText(idiom.getString("degrees.radio.button.label")); // NOI18N;
            radiansRadioButton.setText(idiom.getString("radians.radio.button.label")); // NOI18N;
            jLabel1.setText(idiom.getString("radio.button.group.label")); // NOI18N;
        }
    }
    private class LanguageObservable extends Observable {
        @Override
        public void notifyObservers(){
            setChanged();
            super.notifyObservers();
        }
    }

    LanguageObservable languageObservable = new LanguageObservable();
}
