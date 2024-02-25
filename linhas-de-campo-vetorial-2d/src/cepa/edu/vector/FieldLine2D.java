package cepa.edu.vector;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import util.Arrow;
import util.GraphCanvas;
import util.ParametricCurve2D;
import util.VectorField2D;

public class FieldLine2D extends JApplet {

	private static final long serialVersionUID = 1L;
	
	// Define Look & Feel.
    @Override
    public void start(){        
        super.start();
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
            SwingUtilities.updateComponentTreeUI(jPopup[0]);
            SwingUtilities.updateComponentTreeUI(jPopup[1]);
        }
        catch( Exception e ){/*Nada*/}        
    }
        
    // Carrega configurações, inicia os componentes e as variáveis.
    @Override
    public void init() {
        super.init();

        // Carrega o Locale padrão.
        Locale locale = Locale.getDefault();
        try{
            languageBundle = ResourceBundle.getBundle("LanguageBundle", locale); // NOI18N         
        }
        catch( MissingResourceException e ){ /* Nada */ }
        
        if ( languageBundle == null ) languageBundle = ResourceBundle.getBundle("LanguageBundle", new Locale("en", "US")); // NOI18N 
        
        tF                            = 3f;                           // Tempo final das linhas de campo.
        maxStrength                   = 0;                            // Máxima intensidade do campo vetorial na área do gráfico.
        nGrid                         = 20;                           // Quantidade de pontos da grade.
        fF                            = 20;                           // Brilho do campo vetorial, em porcentagem.
        catched                       = -1;                           // Identifica se o ponto de prova foi capturado pela linha de campo (snap).
        show_clickedPt                = true;                         // Marca se o ponto clicado deve ser desenhado ou não.
        selectedPt                    = new int[]{-1,-1};             // Índice do ponto selecionado.
        clickedPt                     = new float[]{0f,0f};           // Coordenadas do ponto clicado.
        contourPt                     = new Vector<float[]>();        // As condições iniciais.
        proofPt                       = new Vector<float[]>();        // Os pontos de prova.
        isAnimatedProofPtOutsideGraph = new Vector<Boolean>();        // Flag que identifica se um ponto de prova animado saiu da área do gráfico.
        buildingSL                    = new Vector<Boolean>();        // Flag que identifica se a linha de campo está em construção ou não.
        sLtimer                       = new Vector<AnimationTimer>(); // Timer da linha de campo        
        
        mainPanel    = new JPanel();      // O JPanel principal contém os dois seguintes e está contido no JApplet.
        fieldPanel   = new FieldPanel();  // JPanel onde o campo vetorial é apresentado.
        controlPanel = new JPanel();      // JPanel com alguns controles (brilho do campo vetorial, animação, etc.).
        tFSpinner    = new JSpinner();    // JSpinner que controla o tempo final das linhas de campo.        
        tFLabel      = new JLabel();      // JLabel do JSpinner acima.
        gridSlider   = new JSlider();     // JSlider de controle do tamanho da grade do campo vetorial.     
        fadeSlider   = new JSlider();     // JSlider de controle do brilho do campo vetorial.        
        fieldMenu    = new JMenu();       // Menu de campos vetoriais.        
        languageMenu = new JMenu();       // Menu de línguas.
        jButton      = new JButton[4];    // Os quatro botões utilizados na animação: rewind, backward, start/pause e forward.        
        jPopup       = new JPopupMenu[2]; // Menus popups (permitem adicionar e remover pontos do gráfico (fieldPanel).        
        jMenuItem    = new JMenuItem[6];  // Opções do menu popup de índice 0. O de índice 1 é apenas uma mensagem.
        
        JMenuBar menuBar = new JMenuBar();  // Barra de menu.        

        //------------------------------------------------------------------------
        // Configura o JPanel principal.
        //------------------------------------------------------------------------        
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new Dimension(700, 600));
        mainPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

        //------------------------------------------------------------------------
        // Configura o JPanel de exibição do campo vetorial.
        //------------------------------------------------------------------------
        fieldPanel.setBackground(Color.WHITE);
        fieldPanel.setName("fieldPanel"); // NOI18N
        
        FieldPanelListener fieldPanelListener = new FieldPanelListener();
        fieldPanel.addMouseListener(fieldPanelListener);
        fieldPanel.addMouseMotionListener(fieldPanelListener);
       
        GroupLayout fieldPanelLayout = new GroupLayout(fieldPanel);
        fieldPanel.setLayout(fieldPanelLayout);
        fieldPanelLayout.setHorizontalGroup(
            fieldPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 643, Short.MAX_VALUE)
        );
        fieldPanelLayout.setVerticalGroup(
            fieldPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 371, Short.MAX_VALUE)
        );

        //------------------------------------------------------------------------
        // Configura o JPanel de controle.
        //------------------------------------------------------------------------        
        controlPanel.setName("controlPanel"); // NOI18N       

        //------------------------------------------------------------------------
        // Configura os componentes de controle do tempo final das linhas de campo.
        //------------------------------------------------------------------------        
        tFLabel.setText(languageBundle.getString("tFLabel.text"));
        tFLabel.setName("tFLabel"); // NOI18N

        tFSpinner.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e) {
                tF = new Float(tFSpinner.getValue().toString());
                fieldPanel.repaint();
            }
            
        });
        tFSpinner.setModel(new javax.swing.SpinnerNumberModel((int)tF, 1, 10, 1));
        tFSpinner.setToolTipText(languageBundle.getString("tFSpinner.toolTipText"));
        tFSpinner.setName("tFSpinner"); // NOI18N
        
        //------------------------------------------------------------------------
        // Configura o JPanel de controle do brilho do campo vetorial.
        //------------------------------------------------------------------------        
        fadeSlider.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e) {
                fF = fadeSlider.getValue();
                fieldPanel.repaint();
            }
        });
        fadeSlider.setOrientation(javax.swing.JSlider.HORIZONTAL);
        fadeSlider.setToolTipText(languageBundle.getString("fadeSlider.toolTipText"));
        fadeSlider.setName("fadeSlider"); // NOI18N        
        fadeSlider.setValue(fF);
        fadeSlider.setMaximum(100);
        fadeSlider.setMinimum(0);
        
        fadeSliderBorder = new TitledBorder(null, languageBundle.getString("fadeSliderBorder.text"), TitledBorder.CENTER, TitledBorder.TOP );        
        fadeSliderBorder.setTitleColor( Color.BLACK );        
        fadeSlider.setBorder(fadeSliderBorder);     
        
        //------------------------------------------------------------------------
        // Configura o JSlider de controle da grade do campo vetorial.
        //------------------------------------------------------------------------        
        gridSlider.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e) {
                nGrid = gridSlider.getValue();
                fieldPanel.repaint();
            }
        });
        gridSlider.setOrientation(javax.swing.JSlider.HORIZONTAL);
        gridSlider.setToolTipText(languageBundle.getString("gridSlider.toolTipText"));
        gridSlider.setName("gridSlider"); // NOI18N
        gridSlider.setValue(nGrid);
        gridSlider.setMaximum(30);
        gridSlider.setMinimum(10);

        gridSliderBorder = new TitledBorder(null, languageBundle.getString("gridSliderBorder.text"), TitledBorder.CENTER, TitledBorder.TOP );
        gridSliderBorder.setTitleColor( Color.BLACK );        
        gridSlider.setBorder(gridSliderBorder);        
        
        //------------------------------------------------------------------------
        // Configura a barra de menus (JMenuBar).
        //------------------------------------------------------------------------                
        menuBar.setName("menuBar"); // NOI18N

        //------------------------------------------------------------------------
        // Configura o menu de campo vetorial.
        //------------------------------------------------------------------------                
        fieldMenu.setName("fieldMenu"); // NOI18N
        fieldMenu.setText(languageBundle.getString("fieldMenu.text"));
                
        ButtonGroup fieldGroup = new ButtonGroup();
        final ResourceBundle fieldBundle = ResourceBundle.getBundle("FieldBundle"); //NOI18N        
        int nField = new Integer(fieldBundle.getString("field.available"));
        for ( int i = 1; i <= nField; i++ ){
            
            final String ref = new String( "field." + i );
            
            final VectorField2D vF = new VectorField2D(
                fieldBundle.getString( ref + ".Fx" ),            // Componente x do campo vetorial.
                fieldBundle.getString( ref + ".Fy" )             // Componente y do campo vetorial.
            );

            String scalar = fieldBundle.getString( ref + ".scalar" );
            if ( !scalar.isEmpty() ) vF.setConservative(scalar);
                        
            ParametricCurve2D stream = new ParametricCurve2D(
                new String[]{
                    fieldBundle.getString( ref + ".stream.x" ),  // Componente x da linha de campo.
                    fieldBundle.getString( ref + ".stream.y" )   // Componente y da linha de campo.
                },
                new String[]{"(x0)","(y0)"}, // NOI18N           // String que representa a condição inicial (derivada zero) em x e y.
                new String[]{"(dx0)","(dy0)"} // NOI18N          // String que representa a condição inicial (derivada um) em x e y.
            );

            vF.addFieldLine(stream);                                
            
            JRadioButtonMenuItem item = new JRadioButtonMenuItem();
            item.addActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent e) {
                    vectorField = vF;
                    fieldPanel.repaint();
                }
            });
            
            String description = fieldBundle.getString( ref + ".description" );
            if ( !description.isEmpty() ) vF.setDescription(description);
            
            item.setText(vF.getDescription());
            
            fieldMenu.add(item);
            fieldGroup.add(item);
            
            if ( i == 1 ){
                vectorField = vF;
                item.setSelected(true);
            }
            
            sleep();
        }
        
        //------------------------------------------------------------------------
        // Configura o menu de línguas.
        //------------------------------------------------------------------------        
        StringTokenizer[] tokenizer = new StringTokenizer[2];
        
        tokenizer[0] = new StringTokenizer(languageBundle.getString("locale.available"));
        int nTokens = tokenizer[0].countTokens();
        int nLocales = 0;
        
        if ( nTokens > 1 ){
            
            languageMenu.setName("languageMenu"); // NOI18N

            ImageIcon languageMenuIcon = getIcon(languageBundle.getString("languageMenu.icon"));            
            if ( languageMenuIcon != null ) languageMenu.setIcon(languageMenuIcon);
            else languageMenu.setText(languageBundle.getString("languageMenu.text"));

            ButtonGroup languageGroup = new ButtonGroup();
                        
            ResourceBundle[] resourceBundle = new ResourceBundle[nTokens];            
            
            for ( int i = 0; i < nTokens; i++ ){

                tokenizer[1] = new StringTokenizer( tokenizer[0].nextToken(), "_" );
                if ( tokenizer[1].countTokens() < 2 ) continue;
                
                String language = tokenizer[1].nextToken();
                String country  = tokenizer[1].nextToken();
                
                try{
                    resourceBundle[i] = ResourceBundle.getBundle( "LanguageBundle", new Locale(language,country) ); // NOI18N
                }
                catch( Exception e ){
                    continue;
                }
                            
                JRadioButtonMenuItem item = new JRadioButtonMenuItem();            
                item.addActionListener(new LanguageMenuListener(resourceBundle[i]));

                ImageIcon languageItemIcon = getIcon(getCodeBase() + resourceBundle[i].getString( "menuItem.icon" ));
                if ( languageItemIcon != null ) item.setIcon(languageItemIcon);
                else item.setText(resourceBundle[i].getString( "menuItem.text" ));
                                
                languageMenu.add(item);
                languageGroup.add(item);
                
                if ( resourceBundle[i] == languageBundle ) item.setSelected(true);
                
                sleep();
                
                ++nLocales;
            }
        }

        //------------------------------------------------------------------------
        // Configura os botões de controle da animação.
        //------------------------------------------------------------------------
        AnimationButtonListener animationButtonListener = new AnimationButtonListener();
        for ( int i = 0; i <= 3; i++ ){
            
            String ref = new String( "jButton" + (i+1) ); // NOI18N
                        
            jButton[i] = new JButton();
            jButton[i].addActionListener(animationButtonListener);
            
            ImageIcon jButtonIcon = getIcon(languageBundle.getString(ref + ".icon"));
            if ( jButtonIcon != null ) jButton[i].setIcon(jButtonIcon);
            else jButton[i].setText(languageBundle.getString(ref + ".text"));
            
            jButton[i].setToolTipText(languageBundle.getString(ref + ".toolTipText"));
            jButton[i].setName(ref);
            
            sleep();
        }

        // Situação inicial dos botões: ativos ou inativos.
        jButton[REWIND].setEnabled(false);
        jButton[BACKWARD].setEnabled(false);
        jButton[START].setEnabled(false);
        jButton[FORWARD].setEnabled(false);
        
        // Carrega os ícones de start e pause, que serão intercambiados em tempo de execução.
        icon = new ImageIcon[2];
        
        icon[0] = getIcon(languageBundle.getString("jButton3.icon"));
        icon[1] = getIcon(languageBundle.getString("jButton3.icon.2"));
        
        //------------------------------------------------------------------------
        // Monta o menu jPopup.
        //------------------------------------------------------------------------                
        for ( int i = 0; i < 2; i++ ){
            jPopup[i] = new JPopupMenu();
            jPopup[i].setName("popup"+(i+1)); // NOI18N
        }
        
        PopupListener popupListener = new PopupListener();
        for ( int i = 0; i < 6; i++ ){
            jMenuItem[i] = new JMenuItem();
            jMenuItem[i].addActionListener(popupListener);
            jMenuItem[i].setText(languageBundle.getString("jMenuItem" + (i+1) + ".text"));
            
            jPopup[0].add(jMenuItem[i]);
            if ( i == 2 ) jPopup[0].addSeparator();
            
            sleep();
        }
        
        popupHint = new JLabel();
        popupHint.setText(languageBundle.getString("jPopup2.text"));        
        jPopup[1].add(popupHint);        
        
        //------------------------------------------------------------------------
        // Organiza os componentes; NÃO ALTERAR! (obs.: gerado pelo NetBeans)
        //------------------------------------------------------------------------        
        javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fadeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 100, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                //.addComponent(gridSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(gridSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 100, Short.MAX_VALUE)
                //.addGap(169, 169, 169)
                .addGap(100, 100, 100)
                .addComponent(jButton[0], javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton[1], javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton[2], javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton[3], javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(115, 115, 115)
                .addComponent(tFLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tFSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlPanelLayout.createSequentialGroup()
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tFLabel)
                        .addComponent(tFSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jButton[0], javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jButton[1], javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton[2], javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton[3], javax.swing.GroupLayout.Alignment.LEADING))))
                .addGap(12, 12, 12))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(fadeSlider, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                .addComponent(gridSlider, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fieldPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(controlPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fieldPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        //------------------------------------------------------------------------
        // Reúne os componentes.
        //------------------------------------------------------------------------      
        menuBar.add(fieldMenu);
        if ( nLocales > 1 ){
            menuBar.add(new JPanel());
            menuBar.add(languageMenu);
        }
        setJMenuBar(menuBar);

        add(mainPanel);

        //------------------------------------------------------------------------
        // Inicializa as ferramentas de exibição e interação.
        //------------------------------------------------------------------------        
        proofPtTimer = new AnimationTimer(ANIMATION_DELAY, new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                fieldPanel.repaint();                     
            }
        });        
        animationTimeout = new Timer(ANIMATION_TIMEOUT, new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                proofPtTimer.stop();
                animationTimeout.stop();
                               
                jButton[REWIND].setEnabled(proofPtTimer.getElapsedTime() > 0);
                jButton[BACKWARD].setEnabled(!proofPtTimer.isRunning());
                jButton[FORWARD].setEnabled(!proofPtTimer.isRunning());
                
                jButton[START].setIcon(icon[0]);
                
                for ( int i = 0; i < isAnimatedProofPtOutsideGraph.size(); i++ ) {
                    isAnimatedProofPtOutsideGraph.set(i,false);
                    sleep();
                }
                
                fieldPanel.repaint();
            }            
        });   
        
        map = new GraphCanvas(
            new float[]{X_RANGE[0],X_RANGE[1],Y_RANGE[0],Y_RANGE[1]},
            new int[]{CANVAS_SIZE[0],CANVAS_SIZE[1]},
            new int[]{CANVAS_MARGIN,CANVAS_MARGIN}
        );        
                
        R = Math.min(Math.abs(Y_RANGE[1]-Y_RANGE[0]),Math.abs(X_RANGE[1]-X_RANGE[0]))/100;
        
        setGrid();
        fieldArrow = new Arrow( 1f*Math.min(dx,dy), 0.5f );        
        proofPtArrow = new Arrow( 0.7f*Math.min(dx,dy), 0f );
    }
     
    private ImageIcon getIcon( String filename ){
                
        URL url = null;
        
        ClassLoader classLoader = this.getClass().getClassLoader();
        url = classLoader.getResource(filename);//ClassLoader.getSystemResource(filename);
        
        if ( url == null ) return null;
        else return new ImageIcon(url);
    }
    
    /*
    private URL getURL( String filename ){
        URL url = null;
                
        ClassLoader cl = FieldLine2D.class.getClassLoader();
        
        url = getClass().getResource(filename);
        if ( url == null ) url = cl.getResource(filename);//ClassLoader.getSystemResource(filename);

        return url;
    } */   
    
    private float dist( float[] p1, float[] p2 ){
        return (float)Math.sqrt( Math.pow(p1[0]-p2[0],2) + Math.pow(p1[1]-p2[1],2) );
    }    
    private void sleep(){
        try{
            Thread.sleep(0);
        }
        catch( InterruptedException e ) {/* Nada */}
    }    
    private void setGrid(){
        dx = (X_RANGE[1] - X_RANGE[0])/(nGrid - 1);
        dy = (Y_RANGE[1] - Y_RANGE[0])/(nGrid - 1);
    }    
    private void setMaxFieldStrength(){

        float x = X_RANGE[0], y = Y_RANGE[0];		

        maxStrength = 0;
        for( int i = 0; i <= nGrid-1; i++){
            x = X_RANGE[0] + i * dx;

            for( int j = 0; j <= nGrid-1; j++ ){								
                y = Y_RANGE[0] + i * dy;

                float strength = vectorField.getModulus(x,y);                    
                if ( Float.isNaN(strength) ) continue;
                maxStrength = Math.max(maxStrength,strength);

                sleep();
            }
            y = Y_RANGE[0];
        }		            
    }
    
    private class FieldPanel extends JPanel{
     
		private static final long serialVersionUID = 1L;
		
		private Graphics2D graphics;
                
        @Override
        public void paintComponent( Graphics g ){
            super.paintComponent(g);
                        
            this.graphics = (Graphics2D) g;
                        
            // Cor de fundo da área de exibição do campo vetorial: branca.
            setBackground(Color.WHITE);

            setGrid();
            setMaxFieldStrength();
            fieldArrow.setLength(1f*Math.min(dx,dy));
            
            //////////////////////////////////////////////////////////////////////////
            // Desenha o campo vetorial.
            //////////////////////////////////////////////////////////////////////////            		
            for( int i = 0; i <= nGrid-1; i++){
                
                float x = X_RANGE[0],
                      y = Y_RANGE[0];
                
                x = X_RANGE[0] + i * dx;
                for( int j = 0; j <= nGrid-1; j++ ){

                    y = Y_RANGE[0] + j * dy;
                    
                    graphics.setColor( vectorColor( vectorField.getModulus(x,y)/maxStrength, true ) );                        
                    drawArrow(fieldArrow, new float[]{x,y}, vectorField.getOrientation(x,y));
                    
                    sleep();
                }
            }
                        
            //////////////////////////////////////////////////////////////////////////
            // Desenha o ponto clicado.
            //////////////////////////////////////////////////////////////////////////              
            if ( show_clickedPt ){
                graphics.setColor( Color.BLACK );
                
                float p1[] = new float[]{clickedPt[0]-0.05f,clickedPt[1]-0.05f},
                      p2[] = new float[]{clickedPt[0]+0.05f,clickedPt[1]+0.05f},
                      p3[] = new float[]{clickedPt[0]-0.05f,clickedPt[1]+0.05f},
                      p4[] = new float[]{clickedPt[0]+0.05f,clickedPt[1]-0.05f};
                
                drawLine(p1,p2);
                drawLine(p3,p4);
            }            

            //////////////////////////////////////////////////////////////////////////
            // Desenha as linhas de campo analíticas (nas duas orelhas).
            //////////////////////////////////////////////////////////////////////////            
            graphics.setColor( Color.BLACK );
            for ( int i = 0; i < contourPt.size(); i++ ){
              
                float[] p1 = null;
                float[] p2 = null;
                float   dt = 0.01f;                    
                
                // Instante do frame (em segundos).
                float elapsed = sLtimer.elementAt(i).getElapsedTime()/1000f;
                
                // Verifica se a linha de campo já está completa: tempo do frame > tempo final configurado.
                if ( buildingSL.elementAt(i) ){
                    if ( elapsed > tF ){
                        sLtimer.elementAt(i).pause();
                        buildingSL.set(i,false);
                    }                    
                }
                
                // Se a linha de campo NÃO estiver sendo construída, o tempo final do frame será sempre "tF".
                if ( !buildingSL.elementAt(i) ) elapsed = tF;
                
                // Define a condição inicial/de contorno (ponto "p1" e o campo vetorial neste ponto).
                p1 = contourPt.elementAt(i);                
                vectorField.getFieldLine().setContour(p1, vectorField.getField(p1));
                                
                // Desenha a linha de campo até o instante do frame, dado pela variável "elapsed".
                for ( float t = 0; t < elapsed; t += dt ){

                    p1 = vectorField.getFieldLine().at(t);
                    p2 = vectorField.getFieldLine().at(t+dt);
                    
                    if ( !map.has(p1) || !map.has(p2) ) continue;

                    drawLine(p1,p2); 
                    
                    sleep();
                }                    
                
                // Se a linha de campo estiver em construção, desenha um ponto diretor.
                if ( buildingSL.elementAt(i) ){

                    p1 = vectorField.getFieldLine().at(elapsed);
                    
                    if ( !map.has(p1) ) continue;

                    float size = Math.max(0.2f,vectorField.getModulus(p1)/maxStrength);
                    proofPtArrow.setLength(size);

                    graphics.setStroke(new BasicStroke(2));
                    graphics.setColor( Color.BLUE );
                    drawArrow(proofPtArrow,p1,vectorField.getOrientation(p1));

                    graphics.setStroke( new BasicStroke(0) );
                    graphics.setColor( Color.BLACK );
                    drawCircle(p1,true); 
                }
            }
            
            //////////////////////////////////////////////////////////////////////////
            // Desenha as condições iniciais.
            //////////////////////////////////////////////////////////////////////////            
            graphics.setColor( Color.GRAY );
            for ( float[] p : contourPt ){                
                drawCircle(p,true);
                sleep();
            } 

            //////////////////////////////////////////////////////////////////////////
            // Desenha os pontos de prova.
            //////////////////////////////////////////////////////////////////////////                                
            if ( proofPtTimer.getElapsedTime() == 0 ){                
                for ( float[] p : proofPt ){

                    float size = Math.max(0.2f,vectorField.getModulus(p)/maxStrength);
                    proofPtArrow.setLength(size);

                    graphics.setStroke(new BasicStroke(2));
                    graphics.setColor( Color.BLUE );
                    drawArrow(proofPtArrow,p,vectorField.getOrientation(p));

                    graphics.setStroke( new BasicStroke(0) );
                    graphics.setColor( Color.WHITE );
                    drawCircle(p,true);                                        

                    graphics.setColor( Color.BLUE );
                    drawCircle(p,false); 
                    
                    sleep();
                }
            }
            else{
                for ( int i = 0; i < proofPt.size(); i++ ){

                    float[] p = proofPt.elementAt(i);

                    vectorField.getFieldLine().setContour(p, vectorField.getField(p));

                    float[] nextPt = vectorField.getFieldLine().at(proofPtTimer.getElapsedTime()/1000f);

                    Boolean test = new Boolean(!map.has(nextPt));
                    isAnimatedProofPtOutsideGraph.set(i, test);
                    if ( test ) continue;

                    float size = Math.max(0.2f,vectorField.getModulus(nextPt)/maxStrength);
                    proofPtArrow.setLength(size);

                    graphics.setStroke(new BasicStroke(2));
                    graphics.setColor( Color.BLUE );
                    drawArrow(proofPtArrow,nextPt,vectorField.getOrientation(nextPt));

                    graphics.setStroke( new BasicStroke(0) );
                    graphics.setColor( Color.WHITE );
                    drawCircle(nextPt,true);                                        

                    graphics.setColor( Color.BLUE );
                    drawCircle(nextPt,false);                    
                    
                    sleep();
                }

                boolean start = true;
                for ( Boolean test : isAnimatedProofPtOutsideGraph ){
                    start = start && test;
                    sleep();
                }
                if ( start ) animationTimeout.start();
                else animationTimeout.stop();
            }
            
            //////////////////////////////////////////////////////////////////////////
            // Destaca o ponto selecionado, se houver um.
            //////////////////////////////////////////////////////////////////////////                
            if ( selectedPt[CONTOUR_PT] != -1 ){
                graphics.setColor( Color.BLACK );
                drawCircle( contourPt.elementAt(selectedPt[CONTOUR_PT]), true );
            }
            else if ( selectedPt[PROOF_PT] != -1 && proofPtTimer.getElapsedTime() == 0 ){
                graphics.setColor( Color.BLUE );
                drawCircle( proofPt.elementAt(selectedPt[PROOF_PT]), true );
            }
        }
        
        private Color vectorColor( float t, boolean fade ){

            // Mantém t no intervalo [0,1].
            t = Math.max( 0, Math.min(1,t) );
            
            float red   = 0.1f * t + 0.9f,
                  green = ( fade ? ((1f-fF/100f) - 0.9f) * t + 0.9f : -0.9f * t + 0.9f ), 
                  blue  = green;
            
            return new Color(red,green,blue);            
        }  
        
        private void drawCircle( float[] p, boolean filled ){
            int[] pp = map.translate(p);
            int radius = map.translateX(R);

            if ( filled ) graphics.fillOval( pp[0]-radius, pp[1]-radius, 2*radius, 2*radius );
            else graphics.drawOval( pp[0]-radius, pp[1]-radius, 2*radius, 2*radius );
        }        
        private void drawLine( float[] p1, float[] p2 ){
            int[] pp1 = map.translate(p1);
            int[] pp2 = map.translate(p2);

            graphics.drawLine( pp1[0], pp1[1], pp2[0], pp2[1] );
        }   
        private void drawArrow( Arrow arrow, float[] p0, float angle ){

            Vector<float[]> arrowPoints = arrow.getArrow(angle);
            Polygon polygon = new Polygon();
            
            float[] pA = arrowPoints.elementAt(0).clone();
            pA[0] += p0[0]; pA[1] += p0[1];
            
            float[] pB = arrowPoints.elementAt(1).clone();
            pB[0] += p0[0]; pB[1] += p0[1];
            
            for ( int i = 1; i < arrowPoints.size(); i++ ){
                float[] p = arrowPoints.elementAt(i);
                p[0] += p0[0]; p[1] += p0[1];
                
                int[] pp = map.translate(p);
                
                polygon.addPoint( pp[0], pp[1] );
                sleep();
            }
            
            drawLine(pA,pB);
            graphics.fillPolygon(polygon);            
        }
        
    }
    private class AnimationTimer extends Timer{
       
		private static final long serialVersionUID = 1L;
		
		private int t = 0;        
        
        public AnimationTimer( final int dt, ActionListener listener ){
            super(dt,listener);
            super.addActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent e) {
                    t += dt;
                }
                
            });
        }
                
        @Override
        public void stop(){
            super.stop();
            t = 0;
        }
        
        public void pause(){
            super.stop();
        }
        
        public void set( int t ){
            this.t = t;
        }
        
        public int getElapsedTime(){
            return t;
        }
    }    

    private class LanguageMenuListener implements ActionListener{

        private ResourceBundle resourceBundle;
        
        public LanguageMenuListener( ResourceBundle resourceBundle ){
            this.resourceBundle = resourceBundle;
        }
        
        
        public void actionPerformed(ActionEvent e) {
            languageBundle = resourceBundle;
                        
            tFLabel.setText(languageBundle.getString("tFLabel.text"));
            fieldMenu.setText(languageBundle.getString("fieldMenu.text"));
            
            if (languageMenu.getIcon() == null ) languageMenu.setText(languageBundle.getString("languageMenu.text"));
            
            tFSpinner.setToolTipText(languageBundle.getString("tFSpinner.toolTipText"));
            fadeSlider.setToolTipText(languageBundle.getString("fadeSlider.toolTipText"));
            gridSlider.setToolTipText(languageBundle.getString("gridSlider.toolTipText"));
            
            fadeSliderBorder.setTitle(languageBundle.getString("fadeSliderBorder.text"));            
            fadeSlider.repaint();
            gridSliderBorder.setTitle(languageBundle.getString("gridSliderBorder.text"));
            gridSlider.repaint();
            
            for ( int i = 0; i < 3; i++ ){
                String ref = new String( "jButton" + (i+1) ); // NOI18N

                if ( jButton[i].getIcon() == null ) jButton[i].setText(languageBundle.getString(ref + ".text"));
                jButton[i].setToolTipText(languageBundle.getString(ref + ".toolTipText"));
                jButton[i].setName(ref);
                
                sleep();
            }
            
            for ( int i = 0; i < 6; i++ ){
                jMenuItem[i].setText(languageBundle.getString("jMenuItem" + (i+1) + ".text"));
                sleep();
            }            
            
            popupHint.setText(languageBundle.getString("jPopup2.text"));
            
        }
    }    
    private class FieldPanelListener implements MouseListener, MouseMotionListener{

        public void mouseClicked(MouseEvent e) {
            // Nada.
        }

        public void mousePressed(MouseEvent e) {
            fieldPanel.requestFocus();
            
            clickedPt = map.translate( e.getX(), e.getY() );            
            boolean isInnerPt = map.has(clickedPt);
                        
            // Mostrar o ponto clicado?
            show_clickedPt = isInnerPt;
            
            // Determina se há um ponto selecionado.            
            selectedPt[CONTOUR_PT] = -1;
            for ( int i = 0; i < contourPt.size(); i++ ){
                if ( dist(clickedPt,contourPt.elementAt(i)) < R ){
                    selectedPt[CONTOUR_PT] = i;
                    show_clickedPt = false;
                    break;
                }
                sleep();
            }
            if ( selectedPt[CONTOUR_PT] == -1 ){
                selectedPt[PROOF_PT] = -1;                
                for ( int i = 0; i < proofPt.size(); i++ ){
                    if ( dist(clickedPt,proofPt.elementAt(i)) < R ){
                        selectedPt[PROOF_PT] = i;
                        show_clickedPt = false;
                        break;
                    }
                    sleep();
                }                
            }
                
            if ( isInnerPt && e.isPopupTrigger() ){
                
                if ( proofPtTimer.getElapsedTime() == 0){
                    jMenuItem[ADD_CONTOUR_PT].setEnabled(contourPt.isEmpty() || (!contourPt.isEmpty() && selectedPt[CONTOUR_PT] == -1));
                    jMenuItem[DEL_CONTOUR_PT].setEnabled(!contourPt.isEmpty() && selectedPt[CONTOUR_PT] >= 0);
                    jMenuItem[CLEAR_CONTOUR_PT].setEnabled(!contourPt.isEmpty());
                    jMenuItem[ADD_PROOF_PT].setEnabled(proofPt.isEmpty() || (!proofPt.isEmpty() && selectedPt[PROOF_PT] == -1));
                    jMenuItem[DEL_PROOF_PT].setEnabled(!proofPt.isEmpty() && selectedPt[PROOF_PT] >= 0);
                    jMenuItem[CLEAR_PROOF_PT].setEnabled(!proofPt.isEmpty());

                    jPopup[0].show(fieldPanel,e.getX(),e.getY());
                }
                else{
                    jPopup[1].show(fieldPanel,e.getX(),e.getY());
                }                
            }
                                    
            fieldPanel.repaint();
        }

        public void mouseReleased(MouseEvent e) {
            mousePressed(e);
        }

        public void mouseEntered(MouseEvent e) {
            fieldPanel.requestFocus();
        }

        public void mouseExited(MouseEvent e) {
            // Nada.
        }

        public void mouseDragged(MouseEvent e) {
            clickedPt = map.translate( e.getX(), e.getY() );
            
            // Limite o ponto arrastado à área do gráfico.
            clickedPt[0] = Math.max( X_RANGE[0], Math.min( X_RANGE[1], clickedPt[0] ) );
            clickedPt[1] = Math.max( Y_RANGE[0], Math.min( Y_RANGE[1], clickedPt[1] ) );
            
            if ( selectedPt[CONTOUR_PT] != -1 ){
                contourPt.set(selectedPt[CONTOUR_PT], clickedPt);
            }
            else if ( selectedPt[PROOF_PT] != -1 ){
                
                //float[] p1 = proofPt.elementAt(selectedPt[PROOF_PT]);
                               
                if ( catched == -1 ){
                    for ( int i = 0; i < contourPt.size(); i++ ){
                        
                        float[] contour = contourPt.elementAt(i);                            
                        vectorField.getFieldLine().setContour(contour, vectorField.getField(contour));
                        
                        float dt = 0.01f;
                        for ( float t = 0; t < tF; t += dt ){
                
                            float[] p2 = vectorField.getFieldLine().at(t);
                            
                            if ( dist(clickedPt,p2) < 1.5f*R ){
                                clickedPt = p2;
                                catched = i;
                                break;
                            }
                            else catched = -1;
                            
                            sleep();
                        }

                        if ( catched != -1 ) break;
                    }                    
                }
                else{
                    
                    int catchedProofPt = catched;
                    
                    float[] contour = contourPt.elementAt(catched);                            
                    vectorField.getFieldLine().setContour(contour, vectorField.getField(contour));
                                        
                    float dt = 0.01f;
                    for ( float t = 0; t < tF; t += dt ){
                                                
                        float[] p2 = vectorField.getFieldLine().at(t);
                        
                        if ( dist(clickedPt,p2) < 1.5f*R ){
                            clickedPt = p2.clone();
                            catched = catchedProofPt;
                            break;
                        }
                        else catched = -1;
                        
                        sleep();
                    }
                    
                }
                
                proofPt.set(selectedPt[PROOF_PT], clickedPt);
            }
                        
            fieldPanel.repaint();
        }

        public void mouseMoved(MouseEvent e) { /* Nada */ }
        
    }
    private class AnimationButtonListener implements ActionListener{

        public void actionPerformed(ActionEvent e) {
            
            // Objeto de origem do evento.
            JButton source = (JButton)e.getSource();
                        
            //------------------------------------------------------------------------
            // Botão REWIND (|<<)
            //------------------------------------------------------------------------
            if ( source == jButton[REWIND] ){
                proofPtTimer.stop();
                jButton[START].setIcon(icon[0]);
            }            
            //------------------------------------------------------------------------
            // Botão BACKWARD (|<)
            //------------------------------------------------------------------------            
            else if ( source == jButton[BACKWARD] ){
                proofPtTimer.set(Math.max(0,proofPtTimer.getElapsedTime()-proofPtTimer.getDelay()));
            }
            //------------------------------------------------------------------------
            // Botão START (>)
            //------------------------------------------------------------------------            
            else if ( source == jButton[START] ){
                if ( !proofPtTimer.isRunning() ){
                    proofPtTimer.start();
                    jButton[START].setIcon(icon[1]);
                }
                else{
                    proofPtTimer.pause();
                    jButton[START].setIcon(icon[0]);
                }
            }     
            //------------------------------------------------------------------------
            // Botão FORWARD (>|)
            //------------------------------------------------------------------------            
            else if ( source == jButton[FORWARD] ){                
                proofPtTimer.set(proofPtTimer.getElapsedTime()+proofPtTimer.getDelay());
            }
            
            jButton[REWIND].setEnabled(proofPtTimer.getElapsedTime() > 0 || proofPtTimer.isRunning());
            jButton[BACKWARD].setEnabled(!proofPtTimer.isRunning() && proofPtTimer.getElapsedTime() > 0);
            jButton[FORWARD].setEnabled(!proofPtTimer.isRunning());            
            
            fieldPanel.repaint();
        }
        
    }    
    private class PopupListener implements ActionListener{

        public void actionPerformed(ActionEvent e) {
            
            JMenuItem source = (JMenuItem)e.getSource();
            
            //----------------------------------------------
            // Adiciona um ponto de condição inicial.
            //----------------------------------------------
            if ( source == jMenuItem[ADD_CONTOUR_PT] ){
                if ( map.has(clickedPt) ){

                     contourPt.add(clickedPt);
                     buildingSL.add(true);
                     
                     show_clickedPt = false;
                     selectedPt[CONTOUR_PT] = contourPt.size()-1;
                     
                     sLtimer.add(new AnimationTimer(ANIMATION_DELAY, new ActionListener(){

                        public void actionPerformed(ActionEvent e) {
                            fieldPanel.repaint();
                        }
                         
                     }));
                     sLtimer.elementAt(selectedPt[CONTOUR_PT]).start();
                }                
            }
            //----------------------------------------------
            // Apaga um ponto de condição inicial.
            //----------------------------------------------
            else if ( source == jMenuItem[DEL_CONTOUR_PT] ){
                if ( selectedPt[CONTOUR_PT] != -1 ){
                    contourPt.remove(selectedPt[CONTOUR_PT]);
                    sLtimer.remove(selectedPt[CONTOUR_PT]);
                    buildingSL.remove(selectedPt[CONTOUR_PT]);
                    
                    selectedPt[CONTOUR_PT] = -1;
                }              
            }
            //----------------------------------------------
            // Apaga todos os pontos de condição inicial.
            //----------------------------------------------
            else if ( source == jMenuItem[CLEAR_CONTOUR_PT] ){
                if ( !contourPt.isEmpty() ){
                    contourPt.clear();
                    sLtimer.clear();
                    buildingSL.clear();
                    
                    selectedPt[CONTOUR_PT] = -1;
                }                
            }
            //----------------------------------------------
            // Adiciona um ponto de prova.
            //----------------------------------------------
            else if ( source == jMenuItem[ADD_PROOF_PT] ){
                if ( map.has(clickedPt) ){

                     proofPt.add(clickedPt);
                     isAnimatedProofPtOutsideGraph.add(false);

                     show_clickedPt = false;
                     
                     selectedPt[PROOF_PT] = proofPt.size()-1;
                }                
            }            
            //----------------------------------------------
            // Apaga um ponto de prova.
            //----------------------------------------------
            else if ( source == jMenuItem[DEL_PROOF_PT] ){
                if ( selectedPt[PROOF_PT] != -1 ){
                    proofPt.remove(selectedPt[PROOF_PT]);
                    isAnimatedProofPtOutsideGraph.remove(selectedPt[PROOF_PT]);
                    
                    selectedPt[PROOF_PT] = -1;
                }                
            }   
            //----------------------------------------------
            // Apaga todos os pontos de prova.
            //----------------------------------------------
            else if ( source == jMenuItem[CLEAR_PROOF_PT] ){
                if ( !proofPt.isEmpty() ){
                    proofPt.clear();
                    isAnimatedProofPtOutsideGraph.clear();
                    
                    selectedPt[PROOF_PT]   = -1;
                }                
            }            
            
            jButton[REWIND].setEnabled(   !proofPt.isEmpty() && proofPtTimer.getElapsedTime() > 0 );
            jButton[BACKWARD].setEnabled( !proofPt.isEmpty() && proofPtTimer.getElapsedTime() > 0 && !proofPtTimer.isRunning() );
            jButton[START].setEnabled(    !proofPt.isEmpty() );
            jButton[FORWARD].setEnabled(  !proofPt.isEmpty() && !proofPtTimer.isRunning() );
            
            fieldPanel.repaint();
        }
    }    
    
    private JPanel         controlPanel;
    private JSlider        fadeSlider;
    private FieldPanel     fieldPanel;
    private JSlider        gridSlider;
    private JPanel         mainPanel;
    private JLabel         tFLabel;
    private JLabel         popupHint;
    private JSpinner       tFSpinner;
    private ResourceBundle languageBundle;
    private VectorField2D  vectorField;
    private JMenu          languageMenu;
    private JMenu          fieldMenu;
    private JButton[]      jButton;
    private JMenuItem[]    jMenuItem;
    private ImageIcon[]    icon;
    private JPopupMenu[]   jPopup;
    
    TitledBorder fadeSliderBorder, gridSliderBorder;
        
    private GraphCanvas map; 
    private AnimationTimer proofPtTimer;
    private Timer animationTimeout;

    private float R;
    private float tF;
    private float dx, dy;
    private float maxStrength;    
    private float[] clickedPt;
    
    private int nGrid;
    private int fF;
    private int catched;
    private int[] selectedPt;

    private boolean show_clickedPt;
    
    private Vector<float[]>        contourPt;
    private Vector<float[]>        proofPt;
    private Vector<Boolean>        isAnimatedProofPtOutsideGraph;
    private Vector<Boolean>        buildingSL;    
    private Vector<AnimationTimer> sLtimer;
    
	private Arrow fieldArrow;
    private Arrow proofPtArrow;    
    
    //--------------------------------------------------------------------------
    // Membros constantes.
    //--------------------------------------------------------------------------
    private final int ANIMATION_TIMEOUT = 5000; // milisegundos
    private final int ANIMATION_DELAY = 50;     // milisegundos
    
    private final int CANVAS_MARGIN = 50; // Pixels
    private final int[] CANVAS_SIZE = new int[]{700,500}; // Pixels em x e em y
    
    private float[] X_RANGE = new float[]{ -1, +5 };
    private float[] Y_RANGE = new float[]{ -1, +3 };
    
    private static final int ADD_CONTOUR_PT   = 0,
                             DEL_CONTOUR_PT   = 1,
                             CLEAR_CONTOUR_PT = 2,
                             ADD_PROOF_PT     = 3,
                             DEL_PROOF_PT     = 4,
                             CLEAR_PROOF_PT   = 5;
    private static final int REWIND   = 0,
                             BACKWARD = 1,
                             START    = 2,
                             FORWARD  = 3;
    private static final int CONTOUR_PT = 0,
                             PROOF_PT   = 1;
}
