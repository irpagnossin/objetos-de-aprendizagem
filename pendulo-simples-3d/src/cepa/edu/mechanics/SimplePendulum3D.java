package cepa.edu.mechanics;

import cepa.edu.ode.ODE;
import cepa.edu.ode.RungeKuttaCashKarpSolver;
import cepa.edu.util.AnimationTimer;
import cepa.edu.util.CEPA;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import com.microcrowd.loader.java3d.max3ds.Loader3DS;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.NumberFormat;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.PointLight;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

/**
 *
 * @author Ivan Ramos Pagnossin
 * @date 2007.04.15
 */
public class SimplePendulum3D extends JApplet {
  
	private static final long serialVersionUID = 1L;

	// Define Look & Feel.
    @Override
    public void start(){        
        super.start();
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);                        
        }
        catch( Exception e ){/*Nada*/}
        
        //propertiesFile = getParameter("propertiesFile");
    } 
        
    // Configurações iniciais (não-GUI).
    @Override
    public void init() {
        
        font = new Font("Verdana", Font.PLAIN, 11);
        
        angle = new double[2];
        
        numberFormat = new DecimalFormat[3];            
        numberFormat[0] = new DecimalFormat("###,##0");
        numberFormat[1] = new DecimalFormat("###,##0.000");        
        numberFormat[2] = new DecimalFormat("###,##0.0");

        
        nFrames = tF * frameRate + 1; // A quantidade de frames necessária.
        dt = (double) tF / (nFrames - 1); // Intervalo de tempo entre dois quadros (frames).
        
        // Atualiza a cena tridimensional de tempos em tempos.
        timer = new AnimationTimer( (int) (1000*dt/timeCompression), new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                                
                if ( tabbedPane.getSelectedIndex() == 0 ){                    

                    trueTime = (System.currentTimeMillis()-tStart) / 1000d; // Instante correto do quadro.
                    
                    n = (int) Math.floor(trueTime/dt); // Calcula (corrige: obs. no final do arquivo) o ângulo do pêndulo.
                                        
                    if ( n > nFrames - 1 ) stopAnimation();
                    else{
                        angle[SIMPLE_PENDULUM] = x[n][0] + (x[n+1][0]-x[n][0])/(t[n+1]-t[n])*(trueTime-t[n]); // Calcula (interpola) o ângulo do pêndulo.
                        angle[HARMONIC_OSCILATOR] = A * Math.cos(omega*trueTime); // Calcula o ângulo do oscilador harmônico (aproximação do pêndulo)

                        jPanel3.update(angle[SIMPLE_PENDULUM],angle[HARMONIC_OSCILATOR]);
                    }
                }
            }
        });
        
        t = new double[MAX_FRAMES];            // Variável independente (tempo). SOMENTE SOLUÇÃO OBTIDA COM ÊXITO.
        x = new double[MAX_FRAMES][ODE_ORDER]; // Variavel dependente (solução da EDO). SOMENTE SOLUÇÃO OBTIDA COM ÊXITO.

        tmp_t = new double[MAX_FRAMES];            // Variável independente (tempo).
        tmp_x = new double[MAX_FRAMES][ODE_ORDER]; // Variavel dependente (solução da EDO).            

        for ( int i = 0; i < MAX_FRAMES; i++ ){
            t[i] = 0;
            for ( int j = 0; j < ODE_ORDER; j++ ) x[i][j] = 0;
        }
        
        //O objeto solver resolve a equação diferencial definida na classe que estende ODE.
        solver = new RungeKuttaCashKarpSolver(new ODE( ODE_ORDER ){

            @Override
            public double F(double x, double[] y) {
                return -g/L * Math.sin(y[0]);
            }
        });        

        // Condições iniciais.
        t[0]    = 0;
        x[0][0] = A;
        x[0][1] = 0;

        // Resolve a EDO para os parâmetros-padrão.
        try{
            for ( int i = 1; i < nFrames; i++ ){
                t[i] = t[i-1] + dt; // O próximo valor da variável independente.
                solver.evolve( t[i], x[i], t[i-1], x[i-1] ); // Reitera a solução da EDO (evolui um passo).
                CEPA.sleep(0); // Retorna o controle do processamento ao sistema operacional.
            }   

             // Carrega os components GUI.
            SwingUtilities.invokeAndWait(new Runnable(){

                public void run() {
                    initComponents();
                }
            });
            
            jPanel3.update(A,A); // Coloca os pêndulos na posição inicial.
        }
        catch( Exception e ){
            e.printStackTrace();
            destroy();
        }
    }
    
    // Configura os componentes GUI.
    private void initComponents() {

        InputStream stream = getClass().getClassLoader().getResourceAsStream("applet.properties"); // NOI18N
        Properties prop = new Properties();
        try{
            prop.load(stream);
        }
        catch( IOException e ) {
            e.printStackTrace();
            destroy();
        }        

        URL url = this.getClass().getClassLoader().getResource(prop.getProperty("model.1"));
        
        tabbedPane = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new Pendulum3D(url);
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jSpinner2 = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        jSpinner3 = new javax.swing.JSpinner();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        languageMenu = new javax.swing.JMenu();
        
        
        
        
        // ------------------------------------------------
        // ----- Início da configuração do menu de idiomas.
        idiom = ResourceBundle.getBundle( prop.getProperty("language.bundle"), new Locale("en", "US")); // NOI18N        

        languageMenu.setIcon(getIcon(prop.getProperty("language.menu.icon")));
        availableLocales = CEPA.getLocales(prop.getProperty("available.locales"));

        radioButton = new JRadioButtonMenuItem[availableLocales.length];
        ButtonGroup group = new ButtonGroup();

        for (int i = 0; i < availableLocales.length; i++) {
            final int index = i;

            radioButton[i] = new JRadioButtonMenuItem();
            radioButton[i].setText(availableLocales[i].getDisplayLanguage(availableLocales[i]) + " (" + availableLocales[i].getCountry() + ")");
            radioButton[i].addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    idiom = ResourceBundle.getBundle("LanguageBundle", availableLocales[index]);
                    setI18NProperties();
                }
            });

            // Se o item (Locale) for igual ao padrão, utiliza-o.
            if (availableLocales[i].equals(Locale.getDefault())) {
                idiom = ResourceBundle.getBundle(prop.getProperty("language.bundle"), availableLocales[i]);
                radioButton[i].setSelected(true);
            }

            group.add(radioButton[i]);
            languageMenu.add(radioButton[i]);
        }
        // ----- Fim da configuração do menu de idiomas.
        // ---------------------------------------------        
         
        numberFormat[2] = (DecimalFormat) NumberFormat.getInstance(idiom.getLocale());
        
        border = new TitledBorder[2];
        for ( int i = 0; i < 2; i++ ) border[i] = BorderFactory.createTitledBorder(null, null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font);
                


        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(49, 106, 197)));
        jPanel4.setBorder(border[0]);
        jPanel5.setBorder(border[1]);                
        jLabel1.setFont(font);
        jLabel2.setFont(font);
        jLabel3.setFont(font);        
        jLabel4.setFont(font);
        jLabel5.setFont(font);
        jLabel8.setFont(font);
        
        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(30, 10, 60, 1));
        jSpinner2.setModel(new javax.swing.SpinnerNumberModel(30, 10, 60, 1));
        jSpinner3.setModel(new javax.swing.SpinnerNumberModel(1,1,4,1)); 
        
        Dimension d = new Dimension(40,20);
        jTextField1.setPreferredSize(d);
        jTextField1.setMinimumSize(d);
        jTextField1.setMaximumSize(d);
        jTextField2.setPreferredSize(d);
        jTextField2.setMinimumSize(d);
        jTextField2.setMaximumSize(d);
        jTextField3.setPreferredSize(d);
        jTextField3.setMinimumSize(d);
        jTextField3.setMaximumSize(d);
        jSpinner1.setPreferredSize(d);
        jSpinner1.setMinimumSize(d);
        jSpinner1.setMaximumSize(d);
        jSpinner2.setPreferredSize(d);
        jSpinner2.setMinimumSize(d);
        jSpinner2.setMaximumSize(d);
        jSpinner3.setPreferredSize(d);
        jSpinner3.setMinimumSize(d);
        jSpinner3.setMaximumSize(d);
        
        jTextField1.setHorizontalAlignment(JTextField.RIGHT);
        jTextField2.setHorizontalAlignment(JTextField.RIGHT);
        jTextField3.setHorizontalAlignment(JTextField.RIGHT);
        
        textArea.setFont(font);
        textArea.setColumns(20);
        textArea.setRows(5);
        jScrollPane1.setViewportView(textArea);        
        
        tabbedPane.add(jPanel1);        
        tabbedPane.add(jPanel2);
        
        jMenuBar1.add(new JPanel());
        jMenuBar1.add(languageMenu);
        setJMenuBar(jMenuBar1); 
        
        
        // ------------------------------------------------
        // ----- Carrega os ícones dos botões da animação.
        play = getIcon(prop.getProperty("start.button.icon"));
        if (play != null) jButton1.setIcon(play);
        
        stop = getIcon(prop.getProperty("stop.button.icon"));
        if (stop != null) jButton2.setIcon(stop);
        
        pause = getIcon(prop.getProperty("pause.button.icon"));        

        
        
        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 453, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 332, Short.MAX_VALUE)
        );

 


        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 217, Short.MAX_VALUE)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1)
                        .addComponent(jButton2))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)))
                .addContainerGap())
        );




       


        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 265, Short.MAX_VALUE)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 204, Short.MAX_VALUE)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 254, Short.MAX_VALUE)
                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );




        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 237, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSpinner3)
                    .addComponent(jSpinner2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jSpinner3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        
        


        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4)
                    .addComponent(jButton5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                .addContainerGap())
        );



        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                .addContainerGap())
        );
       
        setI18NProperties(); // Define as propriedades internacionalizadas.
        
        setParameters(); // Atribui aos campos de texto e spinners os valores padrão dos parâmetros.
        
        // Inicia/pausa a animação.
        jButton1.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                if ( timer.isRunning() ) pauseAnimation();
                else startAnimation();
            }
        });

        // Pára (stop) a animação.
        jButton2.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                stopAnimation();
                jPanel3.update(A,A);
            }
        });        
        
        // Aciona a resolução da equação diferencial.
        jButton4.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                
                prev_A = A;
                prev_L = L;
                prev_g = g;
                prev_tF = tF;
                prev_frameRate = frameRate;
                prev_timeCompression = timeCompression;


                try {
                    getParameters();
                } catch (NumberFormatException exception) {
                    textArea.append(CEPA.getCurrentTime() + "\t" + idiom.getString("invalid.parameterS") + "\n");
                    restoreParameters();
                    return;
                } catch (ParseException exception) {
                    textArea.append(CEPA.getCurrentTime() + "\t" + idiom.getString("invalid.parameterS") + "\n");
                    restoreParameters();
                    return;
                }
                
                if ( timer.isRunning() ) stopAnimation();

                textArea.append(CEPA.getCurrentTime() + "\t" + idiom.getString("solving.ODE") + "... ");

                omega = Math.sqrt(g/L);
                
                // Calcula quantos quadros serão necessários e o intervalo de tempo entre eles.
                nFrames = tF * frameRate + 1;
                dt = (double) tF / (nFrames - 1);
                timer.setDelay((int) (1000*dt/timeCompression));
                
                for (int i = 0; i < MAX_FRAMES; i++) {
                    tmp_t[i] = 0;
                    for (int j = 0; j < ODE_ORDER; j++) {
                        tmp_x[i][j] = 0;
                    }
                }

                tmp_t[0] = 0;
                tmp_x[0][0] = A;
                tmp_x[0][1] = 0;

                //double dA = 2*A/nFrames;
                int i = 0;
                try {
                    
                    for (i = 1; i < nFrames; i++) {

                        tmp_t[i] = tmp_t[i - 1] + dt;
                        solver.evolve(tmp_t[i], tmp_x[i], tmp_t[i - 1], tmp_x[i - 1]);
                                                
                        CEPA.sleep(0);
                    }

                    // Copia a solução (obtida com sucesso) para os vetores t[] e x[][].
                    t = tmp_t.clone();
                    x = tmp_x.clone();

                    textArea.append(idiom.getString("ok") + "\n");
                    
                    // Leva o usuário diretamente para a aba de animação e inicia-a.
                    tabbedPane.setSelectedIndex(0);
                    
                    startAnimation();
                } catch (Exception exception) {

                    if (t[i] < 1) {
                        textArea.append(idiom.getString("failed.at") + " t = " + numberFormat[0].format(t[i]) + " ms.\n");
                    } else {
                        textArea.append(idiom.getString("failed.at") + " t = " + numberFormat[1].format(t[i]) + " s.\n");
                    }
                    textArea.append(CEPA.getCurrentTime() + "\t" + idiom.getString("choose.other.parameters") + "\n");

                    restoreParameters();

                }

                tabbedPane.setEnabledAt(0, true);
            }
        });
        
        // Botão "desfazer" (undo).
        jButton5.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                restoreParameters();
            }
        });
    }
     
    // Inicia a animação.
    private void startAnimation(){        
        tStart = System.currentTimeMillis();
        timer.start();
        jButton1.setIcon(pause);
    }
    
    // Pausa a animação.
    private void pauseAnimation(){
        timer.pause();
        jButton1.setIcon(play);
    }
    
    // Pára (stop) a animação.
    private void stopAnimation(){
        timer.stop();
        jButton1.setIcon(play);
        jPanel3.update(A,A);
    }    
        
    // Atribui às variáveis o conteúdo dos componentes GUI.
    private void getParameters() throws NumberFormatException, ParseException{
        
        A = numberFormat[2].parse(jTextField1.getText()).doubleValue();               
        A = A % 360;                
        A = Math.toRadians(A);
        
        L = numberFormat[2].parse(jTextField2.getText()).doubleValue();        
        if ( L < 0 ){
            textArea.append(CEPA.getCurrentTime() + "\t" + idiom.getString("L.smaller.than.0") + "\n");            
            throw new NumberFormatException();
        }
        
        g = numberFormat[2].parse(jTextField3.getText()).doubleValue();        
        if ( g < 0 ){
            textArea.append(CEPA.getCurrentTime() + "\t" + idiom.getString("g.smaller.than.0") + "\n");            
            throw new NumberFormatException();
        }        
        
        tF = (Integer) jSpinner1.getValue();
        frameRate = (Integer) jSpinner2.getValue();
        timeCompression = (Integer) jSpinner3.getValue();
    }        
    
    // Atribui as variáveis aos componentes GUI.
    private void setParameters(){
        try{             
            SwingUtilities.invokeLater(new Runnable(){

                public void run() {
                    jTextField1.setText(numberFormat[2].format(Math.toDegrees(A)));
                    jTextField2.setText(numberFormat[2].format(L));
                    jTextField3.setText(numberFormat[2].format(g));

                    jSpinner1.setValue(tF);
                    jSpinner2.setValue(frameRate);
                    jSpinner3.setValue(timeCompression);
                }
            });
        }
        catch( Exception e ){
            e.printStackTrace();
            destroy();
        }
    }
    
    // Atribui as variaveis anteriores à resolução da equação diferencial aos componentes GUI (undo).
    private void restoreParameters(){
        A = prev_A;
        L = prev_L;
        g = prev_g;
        tF = prev_tF;
        frameRate = prev_frameRate;
        timeCompression = prev_timeCompression;
        
        setParameters();
    }

    // (Re)define todas as propriedades internacionalizadas.
    private void setI18NProperties(){
        tabbedPane.setTitleAt(0, idiom.getString("animation.pane.title"));
        tabbedPane.setTitleAt(1, idiom.getString("parameters.pane.title"));
        
        border[0].setTitle(idiom.getString("ode.parameters"));
        border[1].setTitle(idiom.getString("animation.parameters"));
        
        jButton1.setToolTipText(idiom.getString("play/pause.button.tooltip"));
        jButton2.setToolTipText(idiom.getString("stop.button.tooltip"));
        jButton4.setToolTipText(idiom.getString("ok.button.tooltip"));
        jButton5.setToolTipText(idiom.getString("undo.button.tooltip"));

        jLabel1.setText(idiom.getString("starting.angle"));
        jLabel2.setText(idiom.getString("rod.length"));        
        jLabel3.setText(idiom.getString("gravity"));               
        jLabel4.setText(idiom.getString("animation.duration"));
        jLabel5.setText(idiom.getString("frame.rate"));
        jLabel8.setText(idiom.getString("time.compression"));
        
        jButton4.setText(idiom.getString("ok.button.label"));
        jButton5.setText(idiom.getString("undo.button.label"));                 
                        
        Locale locale = idiom.getLocale();
        numberFormat[2] = NumberFormat.getNumberInstance(locale);
        textArea.append( CEPA.getCurrentTime() + "\t" + idiom.getString("selected.language") + ": " + locale.getDisplayLanguage(locale) + " (" + locale.getCountry() + ")\n" );
        
        setParameters();
    }
        
    // Retorna o ícone, objeto de ImageIcon, associado ao arquivo de nome filename.
    private ImageIcon getIcon( String filename ){
                
        URL url = null;
        
        ClassLoader classLoader = this.getClass().getClassLoader();
        url = classLoader.getResource(filename);
        
        if ( url == null ) return null;
        else return new ImageIcon(url);
    }    
        
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JMenu languageMenu;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private Pendulum3D jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JSpinner jSpinner3;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTextArea textArea;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private JRadioButtonMenuItem[] radioButton;

    private AnimationTimer timer;
    private ImageIcon play, pause, stop;
    
    private double A = Math.toRadians(45);
    private double L = 3;
    private double g = 9.8;
    private double omega = Math.sqrt(g/L);
    private int tF = 30;
    private int frameRate = 30;
    private int timeCompression = 1;
    
    private double prev_A = A;
    private double prev_L = L;
    private double prev_g = g;
    private int prev_tF = tF;
    private int prev_frameRate = frameRate;
    private int prev_timeCompression = timeCompression;

    private final int ODE_ORDER = 2;
    
    private RungeKuttaCashKarpSolver solver;
    private int nFrames;
    
    private double[] t, tmp_t;
    private double[][] x, tmp_x;    
    
    private final int MAX_FRAMES = 3601; // Quantidade máxima de frames = duração máxima * frame-rate máximo + 1.    
    private Font font;
    private NumberFormat[] numberFormat;
        
    private ResourceBundle idiom;
    private Locale[] availableLocales;
    
    private TitledBorder[] border;
    
        
    
    private double dt;
    private long tStart;
    
    private static final int SIMPLE_PENDULUM = 0, HARMONIC_OSCILATOR = 1;
    double[] angle;
    
    double trueTime;
    
    int n;
       
    //String propertiesFile;
    
    private class Pendulum3D extends JPanel{

		private static final long serialVersionUID = 1L;
		
		private URL url;
        
        public Pendulum3D( URL url ) {

            this.url = url;            
            
            System.out.println("url = " + url);
            
            pendulumGroup = new TransformGroup[2];
            transform = new Transform3D();
            transform.setScale(scale);

            // Cria o Canvas3D com as configurações-padrão do SimpleUniverse.
            canvas3D = new Canvas3D(SimpleUniverse.getPreferredConfiguration());

            // Cria o SimpleUniverse no Canvas3D e adiciona a ele os objetos da cena.
            SimpleUniverse universe = new SimpleUniverse(canvas3D);
            BranchGroup scene = createSceneGraph();
            universe.addBranchGraph(scene);

            // Adiciona o comportamento de órbita à câmera.
            OrbitBehavior orbit = new OrbitBehavior(canvas3D, OrbitBehavior.REVERSE_TRANSLATE | OrbitBehavior.REVERSE_ROTATE  );
            BoundingSphere bounds = new BoundingSphere(new Point3d(10.0, 10.0, 10.0), 100.0);
            orbit.setSchedulingBounds(bounds);            
            ViewingPlatform viewingPlatform = universe.getViewingPlatform();            
            viewingPlatform.setViewPlatformBehavior(orbit);       

            // Reposiciona a câmera.
            TransformGroup tGroup = viewingPlatform.getViewPlatformTransform();
            Transform3D t = new Transform3D();
            tGroup.getTransform(t);
            t.lookAt(new Point3d(6,2,4), new Point3d(0,0,1.5), new Vector3d(0,0,1));
            t.invert();        
            tGroup.setTransform(t);         

            // Adiciona o Canvas3D ao JPanel.
            setLayout(new BorderLayout());
            setOpaque(false);
            add(canvas3D,BorderLayout.CENTER);
        }      

        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            Dimension d = getSize();
            canvas3D.setSize(d);
        }

        private BranchGroup createSceneGraph(){
            BranchGroup rootObj = new BranchGroup();

            // Carrega o modelo.
            Loader3DS loader = new Loader3DS();
            loader.setFlags(Loader3DS.LOAD_ALL);

            Scene[] pendulum = new Scene[2];
            try {
                pendulum[0] = loader.load(url);
                pendulum[1] = loader.load(url);                
            }
            catch (FileNotFoundException e) {
                System.err.println(e);
                destroy();
            }
            catch (ParsingErrorException e) {
                System.err.println(e);
                destroy();
            }
            catch (IncorrectFormatException e) {
                System.err.println(e);
                destroy();
            }
            
            // Coloca o pêndulo 1 na sua posição inicial.        
            transform.setTranslation(new Vector3d(dx,0,dz));
            pendulumGroup[0] = new TransformGroup(transform);
            pendulumGroup[0].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            pendulumGroup[0].addChild(pendulum[0].getSceneGroup());
            rootObj.addChild(pendulumGroup[0]);

            // Coloca o pêndulo 2 na sua posição inicial.
            transform.setTranslation(new Vector3d(-dx,0,dz));
            pendulumGroup[1] = new TransformGroup(transform);
            pendulumGroup[1].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            pendulumGroup[1].addChild(pendulum[1].getSceneGroup());
            rootObj.addChild(pendulumGroup[1]);

            // Define o fundo da cena (background).
            BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
            Background bg = new Background(new Color3f(0.05f, 0.05f, 0.2f));
            bg.setApplicationBounds(bounds);
            rootObj.addChild(bg);        


            // Luzes
            AmbientLight aLgt = new AmbientLight(new Color3f(0.3f, 0.3f, 0.3f));
            aLgt.setInfluencingBounds(bounds);
            rootObj.addChild(aLgt);

            Point3f[] lightPosition = new Point3f[]{
                new Point3f(-1f,-1f,2f),
                new Point3f(.5f,0,0)
            };

            Color3f[] lightColor = new Color3f[]{
                new Color3f(1.0f, 0.0f, 0.0f),
                new Color3f(0.0f, 1.0f, 0.0f)
            };


            Point3f attenuation = new Point3f(0.8f,0f,0f);

            PointLight[] light = new PointLight[2];
            for ( int i = 0; i < 2; i++ ){
                light[i] = new PointLight(lightColor[i],lightPosition[i],attenuation);
                light[i].setInfluencingBounds(bounds);
                rootObj.addChild(light[i]);
            }        

            Color3f[] axisColor = {
                new Color3f(1f,0f,0f),
                new Color3f(0f,1f,0f),
                new Color3f(0f,0f,1f)
            };
            
            rootObj.addChild(new ReferenceFrame(1f,axisColor));

            rootObj.compile();
            return rootObj;
        }

        public void update( final double angle1, final double angle2 ){
            // Reposiciona o pêndulo 1.
            transform.setTranslation(new Vector3d(dx,0,dz));
            transform.setRotation(new AxisAngle4d(1,0,0,angle1));
            pendulumGroup[0].setTransform(transform);        
            
            // Reposiciona o pêndulo 2.
            transform.setTranslation(new Vector3d(-dx,0,dz));
            transform.setRotation(new AxisAngle4d(1,0,0,angle2));
            pendulumGroup[1].setTransform(transform);
        }

        // Parêmetros de organização dos modelos (determinados empiricamente).
        private final double dx = 0.2, dz = 3.0;
        private final double scale = 0.02;

        private Canvas3D canvas3D;
        private Transform3D transform;
        private TransformGroup[] pendulumGroup;
    }
}

/*
 * obs.1: observei que o período do pêndulo, para pequenos ângulos, apresentava
 * um desvio do valor esperado que era proporcional ao frame-rate e inversamente
 * proporcional ao comprimento da haste.
 *   Ocorre que a execução do método actionPerformed ocorria algumas dezenas de
 * milisegundos após a ocorrência do triger. Esta defasagem se somava e dava origem
 * a erros grandes. Por exemplo, para a situação inicial, com L = 3, g = 9,8 e 
 * frame-rate = 30, o período observado era de 5 segundos, quando deveria ser
 * de apenas 3,5s!
 *   Para resolver isso utilizei de interpolação: dentro do método actionPerformed
 * eu calculo do tempo decorrido do início da animação (variável trueTime) e, com
 * base nele, interpolo linearmente entre os ângulos do pêndulo associados aos frames
 * anterior e posterior a trueTime para obter uma aproximação do ângulo associado
 * a trueTime. Para o oscilador harmônico basta utilizar trueTime na expressão.
 */
