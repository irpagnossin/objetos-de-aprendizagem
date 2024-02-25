/*
 * TODO:
 * Implementar o cálculo da EDO em thread separada, dando a opção de criar um botão "cancelar" 
 * Barra de rolagem deve rolar automaticamente, mostrando sempre a última informação impressa 
 * Colocar escala nos eixos de x e dx/dt
 * Incluir uma fase no sinal modulador (driving signal)
 * Flechas nos eixos coordenados
 * -> Escrever um testador da EDO.
 */
package cepa.edu.mechanics;

import cepa.edu.ode.ODE;
import cepa.edu.ode.RungeKuttaCashKarpSolver;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Time;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author  ivan.pagnossin
 */
public class VanderPolOscillator extends JApplet {
        
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
    }
    
    @Override
    public void init() {
        System.out.println(getLocale());
        try {   
            font = new Font("Verdana", Font.PLAIN, 11);
            
            caption = new String[3];
            
            t = new double[MAX_FRAMES];            // Variável independente (tempo). SOMENTE SOLUÇÃO OBTIDA COM ÊXITO.
            x = new double[MAX_FRAMES][ODE_ORDER]; // Variavel dependente (solução da EDO). SOMENTE SOLUÇÃO OBTIDA COM ÊXITO.
            x_hO = new double[MAX_FRAMES][ODE_ORDER];
            
            tmp_t = new double[MAX_FRAMES];            // Variável independente (tempo).
            tmp_x = new double[MAX_FRAMES][ODE_ORDER]; // Variavel dependente (solução da EDO).

            for ( int i = 0; i < MAX_FRAMES; i++ ){
                t[i] = 0;
                for ( int j = 0; j < ODE_ORDER; j++ ) x[i][j] = x_hO[i][j] = 0;
            }

            // Inicialmente, define os parâmetros atuais e anteriores como iguais aos defaults.
            tI        = prev_tI        = STD_TI;
            tF        = prev_tF        = STD_TF;
            x0        = prev_x0        = STD_X0;
            dx0       = prev_x0        = STD_DX0;
            epsilon   = prev_epsilon   = STD_EPSILON;
            F         = prev_F         = STD_F;
            f         = prev_f         = STD_FREQUENCY;
            frameRate = prev_frameRate = STD_FRAME_RATE;
            trail     = prev_trail     = STD_TRAIL;

            numberFormat = new DecimalFormat[2];            
            numberFormat[0] = new DecimalFormat("###,##0");
            numberFormat[1] = new DecimalFormat("###,##0.000");

            // Resolve a EDO para os parâmetros iniciais.
            nFrames = (tF - tI) * frameRate;

            // Intervalo de tempo entre dois quadros (frames).
            dt = (double)(tF - tI)/(nFrames - 1);
            
            //O objeto vanderPol resolve a equação diferencial definida na classe que estende ODE.
            vanderPol = new RungeKuttaCashKarpSolver(new ODE( ODE_ORDER ){

                @Override
                public double F(double x, double[] y) {
                    return -y[0] - epsilon * ( y[0]*y[0] - 1.0 )* y[1] + F*Math.sin(2*Math.PI*f*x);                
                }
            });

            // O objeto timer repinta o animationPanel aproximadamente frameRate vezes por segundo.
            timer = new Timer( (int)(1000*dt), new ActionListener(){

                public void actionPerformed(ActionEvent e) {

                    if ( tabbedPane.getSelectedIndex() == 0 ){
                        trueTime = (System.currentTimeMillis()-tStart) / 1000d; // Instante correto do quadro.
                        frame = (int) Math.floor(trueTime/dt); // Determina qual o quadro mais próximo deste instante.
                        
                        if ( frame > nFrames - 1 ) stopAnimation();
                        else animationPanel.repaint();
                    }
                    
//                    // Embora a animação funcione, desta maneira (3 instruções seguintes) o período da oscilação fica incorreto.
//                    if ( frame < nFrames - 1 ) ++frame;
//                    else stopAnimation();
//                    animationPanel.repaint();
                    
                }
            } );
                 
            t[0]    = tI;
            x[0][0] = x_hO[0][0] = x0;
            x[0][1] = x_hO[0][1] = dx0;

            spPhase = -Math.atan2(x[0][1], x[0][0]); // Fase do oscilador harmônico (para as mesmas condições iniciais).
            spAmplitude = Math.sqrt(x[0][0] * x[0][0] + x[0][1] * x[0][1]); // Amplitude do oscilador harmônico (para as mesmas condições iniciais).
            
            for ( int i = 1; i < nFrames; i++ ){
                t[i] = t[i-1] + dt;
                
                vanderPol.evolve( t[i], x[i], t[i-1], x[i-1] ); // Reitera a solução da EDO (evolui um passo).
                
                x_hO[i][0] = + spAmplitude * Math.cos( t[i] + spPhase );
                x_hO[i][1] = - spAmplitude * Math.sin( t[i] + spPhase );
            }
            
            // Determinando os valores máximos de x (X) e dx/dt (Y).
            maxX = maxY = 0;
            for ( int i = 0; i < nFrames; i++ ){
                maxX = Math.max(maxX,Math.abs(x[i][0]));
                maxY = Math.max(maxY,Math.abs(x[i][1]));                
                
                maxX = Math.max(maxX,Math.abs(x_hO[i][0]));
                maxY = Math.max(maxY,Math.abs(x_hO[i][1]));                
            }
            
            // Carrega os components GUI.
            SwingUtilities.invokeAndWait(new Runnable(){

                public void run() {
                    initComponents();
                }
            });
        }
        catch( Exception e ){
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private void initComponents() {

        
        try {

            InputStream stream = getClass().getClassLoader().getResourceAsStream("applet.properties"); 
            Properties prop = new Properties();
            prop.load(stream);

            label = new JLabel[8];
            for (int i = 0; i < 8; i++) {
                label[i] = new JLabel();
                label[i].setFont(font);
            }

            panel = new JPanel[5];
            for (int i = 0; i < 5; i++) {
                panel[i] = new JPanel();
                //panel[i].setFont(font);
            }

            spinner = new JSpinner[4];
            for (int i = 0; i < 4; i++) {
                spinner[i] = new JSpinner();
            }
            textField = new JTextField[4];
            for (int i = 0; i < 4; i++) {
                textField[i] = new JTextField();
            }
            button = new JButton[4];
            for (int i = 0; i < 4; i++) {
                button[i] = new JButton();
            }
            tabbedPane = new javax.swing.JTabbedPane();

            animationPanel = new AnimationPanel();
            scrollPane = new javax.swing.JScrollPane();
            textArea = new javax.swing.JTextArea();
            menuBar = new javax.swing.JMenuBar();
            aboutMenu = new JMenu();
            aboutOption = new JMenuItem();
            languageMenu = new javax.swing.JMenu();

            
            
            //----------------------------------------------------------------------


            tabbedPane.setFont(font);
            scrollPane.setAutoscrolls(true);

            animationPanel.setBackground(Color.WHITE);
            animationPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(0, 70, 213)));

            Dimension dimension = new Dimension(40, 20);

            textField[0].setHorizontalAlignment(javax.swing.JTextField.RIGHT);
            textField[0].setPreferredSize(dimension);
            textField[0].setText(Double.toString(x0));

            textField[1].setHorizontalAlignment(javax.swing.JTextField.RIGHT);
            textField[1].setPreferredSize(dimension);
            textField[1].setText(Double.toString(dx0));

            textField[2].setHorizontalAlignment(javax.swing.JTextField.RIGHT);
            textField[2].setPreferredSize(dimension);
            textField[2].setText(Double.toString(epsilon));

            textField[3].setHorizontalAlignment(javax.swing.JTextField.RIGHT);
            textField[3].setPreferredSize(dimension);
            textField[3].setText(Double.toString(F));

            spinner[0].setModel(new javax.swing.SpinnerNumberModel(0, 0, 100, 1));
            spinner[0].setPreferredSize(dimension);

            spinner[1].setModel(new javax.swing.SpinnerNumberModel(30, 30, 120, 1));
            spinner[1].setPreferredSize(dimension);

            spinner[2].setModel(new javax.swing.SpinnerNumberModel(30, 25, 50, 1));
            spinner[2].setPreferredSize(dimension);

            spinner[3].setModel(new javax.swing.SpinnerNumberModel(5, 0, 10, 1));
            spinner[3].setPreferredSize(dimension);

            textArea.setColumns(20);
            textArea.setEditable(false);
            textArea.setRows(5);
            textArea.setFont(font);
            textArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            textArea.setTabSize(2);
            scrollPane.setViewportView(textArea);

            tabbedPane.add(panel[0]);
            tabbedPane.add(panel[1]);




            
            startIcon = getIcon(prop.getProperty("start.button.icon"));
            if (startIcon != null) {
                button[0].setIcon(startIcon);
            }
            stopIcon = getIcon(prop.getProperty("stop.button.icon"));


            setGlassPane(new LogoPanel(prop.getProperty("logo.path")));



            aboutOption.setFont(font);
            aboutMenu.setFont(font);
            aboutMenu.add(aboutOption);

            menuBar.add(aboutMenu);
            menuBar.add(new JPanel());
            menuBar.add(languageMenu);
            setJMenuBar(menuBar);

            // ------------------------------------------------
            // ----- Início da configuração do menu de idiomas.
            
            idiom = ResourceBundle.getBundle( prop.getProperty("language.bundle"), new Locale("en", "US")); // NOI18N
            
            languageMenu.setIcon(getIcon(prop.getProperty("language.menu.icon")));
            availableLocales = getLocales(prop.getProperty("available.locales"));

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

            setI18NProperties(); // Efetua as configurações dependentes do Locale.
            aboutOption.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    getGlassPane().setVisible(true);
                }
            });

            button[0].addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (timer.isRunning()) {
                        stopAnimation();
                    } else {
                        startAnimation();
                    }
                }
            });
            button[3].addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    restoreDefaultParameters();
                }
            });
            button[2].addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    restorePreviousParameters();
                }
            });
            button[1].addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    prev_tI = tI;
                    prev_tF = tF;
                    prev_x0 = x0;
                    prev_dx0 = dx0;
                    prev_epsilon = epsilon;
                    prev_F = F;
                    prev_f = f;
                    prev_frameRate = frameRate;
                    prev_trail = trail;

                    try {
                        getParameters();
                    } catch (NumberFormatException exception) {
                        textArea.append(getCurrentTime() + "\t" + idiom.getString("invalid.parameterS") + "\n");
                        setParameters();
                        return;
                    }

                    textArea.append(getCurrentTime() + "\t" + idiom.getString("solving.ODE") + "... ");

                    tabbedPane.setEnabledAt(0, false);
                    for (int i = 0; i < 4; i++) {
                        spinner[i].setEnabled(false);
                    }
                    for (int i = 0; i < 4; i++) {
                        textField[i].setEnabled(false);
                    }
                    nFrames = (tF - tI) * frameRate;
                    dt = (double) (tF - tI) / (nFrames - 1);
                    timer.setDelay((int) (1000*dt));

                    maxX = maxY = 0;

                    for (int i = 0; i < MAX_FRAMES; i++) {
                        tmp_t[i] = 0;
                        for (int j = 0; j < ODE_ORDER; j++) {
                            tmp_x[i][j] = 0;
                        }
                    }

                    tmp_t[0] = tI;
                    tmp_x[0][0] = x0;
                    tmp_x[0][1] = dx0;

                    int i = 0;
                    try {
                        for (i = 1; i < nFrames; i++) {

                            tmp_t[i] = tmp_t[i - 1] + dt;
                            vanderPol.evolve(tmp_t[i], tmp_x[i], tmp_t[i - 1], tmp_x[i - 1]); 
                            sleep(0);
                            //throw new Exception();
                        }

                        // Copia a solução (obtida com sucesso) para os vetores t[] e x[][].
                        t = tmp_t.clone();
                        x = tmp_x.clone();
                        
                        spPhase = -Math.atan2(x[0][1], x[0][0]); // Fase do oscilador harmônico (para as mesmas condições iniciais).
                        spAmplitude = Math.sqrt(x[0][0] * x[0][0] + x[0][1] * x[0][1]); // Amplitude do oscilador harmônico (para as mesmas condições iniciais).
            
                        // Determinando os valores máximos de x (X) e dx/dt (Y).
                        maxX = maxY = 0;
                        for ( i = 0; i < nFrames; i++ ){
                            x_hO[i][0] = + spAmplitude * Math.cos( t[i] + spPhase );
                            x_hO[i][1] = - spAmplitude * Math.sin( t[i] + spPhase );
                            
                            maxX = Math.max(maxX,Math.abs(x[i][0]));
                            maxY = Math.max(maxY,Math.abs(x[i][1]));                

                            maxX = Math.max(maxX,Math.abs(x_hO[i][0]));
                            maxY = Math.max(maxY,Math.abs(x_hO[i][1]));                            
                        }

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
                        textArea.append(getCurrentTime() + "\t" + idiom.getString("choose.other.parameters") + "\n");

                        restorePreviousParameters();

                        for (i = 0; i < 4; i++) {
                            spinner[i].setEnabled(true);
                        }
                        for (i = 0; i < 4; i++) {
                            textField[i].setEnabled(true);
                        }
                    }

                    tabbedPane.setEnabledAt(0, true);
                }
            });

            textField[0].addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        x0 = Double.parseDouble(textField[0].getText());
                    } catch (NumberFormatException exception) {

                        textField[0].setText(Double.toString(x0));
                        textArea.append(getCurrentTime() + "\t" + idiom.getString("invalid.parameter") + "\n");
                    }
                }
            });
            textField[1].addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        dx0 = Double.parseDouble(textField[1].getText());
                    } catch (NumberFormatException exception) {

                        textField[1].setText(Double.toString(dx0));
                        textArea.append(getCurrentTime() + "\t" + idiom.getString("invalid.parameter") + "\n");
                    }
                }
            });
            textField[2].addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        epsilon = Double.parseDouble(textField[2].getText());
                    } catch (NumberFormatException exception) {
                        textField[2].setText(Double.toString(epsilon));
                        textArea.append(getCurrentTime() + "\t" + idiom.getString("invalid.parameter") + "\n");
                    }
                }
            });
            textField[3].addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        F = Double.parseDouble(textField[3].getText());
                    } catch (NumberFormatException exception) {
                        textField[3].setText(Double.toString(F));
                        textArea.append(getCurrentTime() + "\t" + idiom.getString("invalid.parameter") + "\n");
                    }
                }
            });

            tabbedPane.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    if (tabbedPane.getSelectedIndex() == 1 && timer.isRunning()) {
                        textArea.append(getCurrentTime() + "\t" + idiom.getString("stop.animation.to.change.parameters") + "\n");
                    }
                }
            });

            //----------------------------------------------------------------------
            javax.swing.GroupLayout animationPanelLayout = new javax.swing.GroupLayout(animationPanel);
            animationPanel.setLayout(animationPanelLayout);
            animationPanelLayout.setHorizontalGroup(animationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 392, Short.MAX_VALUE));
            animationPanelLayout.setVerticalGroup(animationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 399, Short.MAX_VALUE));

            //----------------------------------------------------------------------
            javax.swing.GroupLayout panel0Layout = new javax.swing.GroupLayout(panel[0]);
            panel[0].setLayout(panel0Layout);
            panel0Layout.setHorizontalGroup(panel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(panel0Layout.createSequentialGroup().addContainerGap().addGroup(panel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(animationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(button[0], javax.swing.GroupLayout.Alignment.TRAILING)).addContainerGap()));
            panel0Layout.setVerticalGroup(panel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel0Layout.createSequentialGroup().addContainerGap().addComponent(animationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(button[0]).addContainerGap()));

            //----------------------------------------------------------------------
            javax.swing.GroupLayout panel2Layout = new javax.swing.GroupLayout(panel[2]);
            panel[2].setLayout(panel2Layout);
            panel2Layout.setHorizontalGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(panel2Layout.createSequentialGroup().addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(label[0]).addComponent(label[1])).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 195, Short.MAX_VALUE).addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(textField[0], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(textField[1], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))));
            panel2Layout.setVerticalGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(panel2Layout.createSequentialGroup().addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(label[0]).addComponent(textField[0], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(label[1]).addComponent(textField[1], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))));

            //----------------------------------------------------------------------
            javax.swing.GroupLayout panel3Layout = new javax.swing.GroupLayout(panel[3]);
            panel[3].setLayout(panel3Layout);
            panel3Layout.setHorizontalGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel3Layout.createSequentialGroup().addComponent(label[2]).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 160, Short.MAX_VALUE).addComponent(textField[2], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel3Layout.createSequentialGroup().addComponent(label[3]).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 95, Short.MAX_VALUE).addComponent(textField[3], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel3Layout.createSequentialGroup().addComponent(label[4]).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 182, Short.MAX_VALUE).addComponent(spinner[0], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));
            panel3Layout.setVerticalGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(panel3Layout.createSequentialGroup().addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(textField[2], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(label[2])).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(textField[3], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(label[3])).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(spinner[0], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(label[4]))));

            //----------------------------------------------------------------------
            javax.swing.GroupLayout panel4Layout = new javax.swing.GroupLayout(panel[4]);
            panel[4].setLayout(panel4Layout);
            panel4Layout.setHorizontalGroup(panel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel4Layout.createSequentialGroup().addComponent(label[5]).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 282, Short.MAX_VALUE).addComponent(spinner[1], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel4Layout.createSequentialGroup().addComponent(label[6]).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 225, Short.MAX_VALUE).addComponent(spinner[2], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel4Layout.createSequentialGroup().addComponent(label[7]).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 256, Short.MAX_VALUE).addComponent(spinner[3], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));
            panel4Layout.setVerticalGroup(panel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(panel4Layout.createSequentialGroup().addGroup(panel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(spinner[1], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(label[5])).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(panel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(spinner[2], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(label[6])).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(panel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(spinner[3], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(label[7]))));

            //----------------------------------------------------------------------
            javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel[1]);
            panel[1].setLayout(panel1Layout);
            panel1Layout.setHorizontalGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(panel1Layout.createSequentialGroup().addContainerGap().addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(scrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE).addComponent(panel[4], javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(panel[3], javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(panel[2], javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup().addComponent(button[3]).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(button[2]).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(button[1]))).addContainerGap()));
            panel1Layout.setVerticalGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(panel1Layout.createSequentialGroup().addContainerGap().addComponent(panel[2], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(panel[3], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(panel[4], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(button[1]).addComponent(button[2]).addComponent(button[3])).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE).addContainerGap()));

            //----------------------------------------------------------------------
            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE).addContainerGap()));
            layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE).addContainerGap()));
            
        }
        catch( IOException e ) {
            e.printStackTrace();
            System.exit(1);
        }
    }
        
    /**
     * Efetua as configurações internacionalizadas; aquelas que são dependentes do Locale.
     */
    private void setI18NProperties(){        
                
        tabbedPane.setTitleAt(0, idiom.getString("animation.pane.title"));
        tabbedPane.setTitleAt(1, idiom.getString("parameters.pane.title"));
        
        TitledBorder[] border = new TitledBorder[3];        
        border[0] = BorderFactory.createTitledBorder(idiom.getString("initial.condition"));
        border[1] = BorderFactory.createTitledBorder(idiom.getString("ode"));
        border[2] = BorderFactory.createTitledBorder(idiom.getString("animation.parameters.group.title"));
        for ( int i = 0; i < 3; i++ ) border[i].setTitleFont(font);
        
        panel[2].setBorder(border[0]);
        panel[3].setBorder(border[1]);
        panel[4].setBorder(border[2]);
        
        label[0].setText(idiom.getString("starting.x"));
        label[1].setText(idiom.getString("starting.dx/dt"));        
        label[2].setText(idiom.getString("damping.parameter"));
        label[3].setText(idiom.getString("driving.signal.intensity"));
        label[4].setText(idiom.getString("driving.signal.frequency"));
        label[5].setText(idiom.getString("duration(seconds)"));
        label[6].setText(idiom.getString("frame.rate"));
        label[7].setText(idiom.getString("tail(frames)"));
                      
        button[1].setText(idiom.getString("ok.button"));
        button[2].setText(idiom.getString("cancel.actual.parameters"));
        button[3].setText(idiom.getString("default.parameters"));
                
        aboutOption.setText(idiom.getString("about"));
        aboutMenu.setText(idiom.getString("about"));
                
        for ( int i = 0; i < 3; i++ ) caption[i] = new String(idiom.getString("caption." + i));
        
        IC = new String(idiom.getString("IC"));
        
        Locale locale = idiom.getLocale();
        textArea.append( getCurrentTime() + "\t" + idiom.getString("selected.language") + ": " + locale.getDisplayLanguage(locale) + " (" + locale.getCountry() + ")\n" );
        
        // Atualiza o painel de animações se a animação não estiver ocorrendo (neste caso o repaint é feito pelo Timer).
        if ( !timer.isRunning() ) animationPanel.repaint();
    }    
    
    /*
     * getLocales("pt_BR en_US it_IT") = { pt_BR, en_US, it_IT } (os elementos da matriz são objetos da classe Locale).
     */
    private Locale[] getLocales( String list ){
        
        Vector<Locale> vector = new Vector<Locale>(); 
        
        String[] locales = Pattern.compile(" ").split(list);        
                
        for ( int i = 0; i < locales.length; i++ ){
            String[] codes = Pattern.compile("_").split(locales[i]);            
            if ( codes.length == 2 ) vector.add( new Locale(codes[0],codes[1]) );
        }        
        
        return vector.toArray(new Locale[locales.length]);
    }
    
    /*
     * Retorna o ícone, objeto de ImageIcon, associado ao arquivo de nome filename.
     */
    private ImageIcon getIcon( String filename ){
                
        URL url = null;
        
        ClassLoader classLoader = this.getClass().getClassLoader();
        url = classLoader.getResource(filename);
        
        if ( url == null ) return null;
        else return new ImageIcon(url);
    }    
    
    private void startAnimation(){
        tStart = System.currentTimeMillis();
        frame = 0;
        timer.start();
        button[0].setIcon(stopIcon);
        
        for ( int i = 1; i < 4; i++ ) button[i].setEnabled(false);
        for ( int i = 0; i < 4; i++ ) textField[i].setEnabled(false);
        for ( int i = 0; i < 4; i++ ) spinner[i].setEnabled(false);
    }
    
    private void stopAnimation(){
        timer.stop();
        button[0].setIcon(startIcon);       
        
        for ( int i = 1; i < 4; i++ ) button[i].setEnabled(true);
        for ( int i = 0; i < 4; i++ ) textField[i].setEnabled(true);
        for ( int i = 0; i < 4; i++ ) spinner[i].setEnabled(true);        
    }    
    
    private synchronized void setParameters(){
        
        textField[0].setText(Double.toString(x0));
        textField[1].setText(Double.toString(dx0));
        textField[2].setText(Double.toString(epsilon));
        textField[3].setText(Double.toString(F));

        spinner[0].setValue(f);
        spinner[1].setValue(tF-tI);
        spinner[2].setValue(frameRate);
        spinner[3].setValue(trail);
    }
    
    private synchronized void getParameters() throws NumberFormatException{
        
        x0 = Double.parseDouble(textField[0].getText());
        dx0 = Double.parseDouble(textField[1].getText());
        epsilon = Double.parseDouble(textField[2].getText());
        F = Double.parseDouble(textField[3].getText());

        f = (Integer) spinner[0].getValue();
        tF = tI + (Integer) spinner[1].getValue();
        frameRate = (Integer) spinner[2].getValue();
        trail = (Integer) spinner[3].getValue();
    }
    
    private void restorePreviousParameters(){
        tI        = prev_tI;
        tF        = prev_tF;
        x0        = prev_x0;
        dx0       = prev_dx0;
        epsilon   = prev_epsilon;
        F         = prev_F;
        f         = prev_f;
        frameRate = prev_frameRate;
        trail     = prev_trail;
        
        setParameters();      
    }    
    
    private void restoreDefaultParameters(){
        tI        = STD_TI;
        tF        = STD_TF;
        x0        = STD_X0;
        dx0       = STD_DX0;
        epsilon   = STD_EPSILON;
        F         = STD_F;
        f         = STD_FREQUENCY;
        frameRate = STD_FRAME_RATE;
        trail     = STD_TRAIL;       
        
        setParameters();
   }
   
    private void sleep( int time ){
        try{
            Thread.sleep(time);
        }
        catch( InterruptedException e ) {/* Nada */}
    }
        
    private String getCurrentTime(){
        return (new Time(System.currentTimeMillis())).toString();
    }
    
    private class AnimationPanel extends JPanel{
    
		private static final long serialVersionUID = 1L;

		private final int X = 0, Y = 1;
                
        private int L;   // Tamanho da haste do pêndulo.
        private int M;   // Margem do painel.
        private int R;   // Raio da esfera oscilante.
        private int GAP; // Espaço-padrão na legenda.
        
        private int[] center; // Coordenadas do centro do painel.
        
        @Override
        public void paintComponent( Graphics graphics ){
            super.paintComponent(graphics);
            
            Graphics2D g = (Graphics2D) graphics;   
            g.setFont(font);
            FontMetrics metrics = g.getFontMetrics(font);
            
            int smallerDimension = Math.min(getSize().height,getSize().width);
            center = new int[]{ getSize().width >> 1, getSize().height >> 1 };
            R = smallerDimension/30;
            M = smallerDimension/30;
            L = (smallerDimension - 2 * (M + R)) >> 1;
            GAP = M>>1;
            
            int xPos = 0, yPos = 0, prevPosX = 0, prevPosY = 0, d = 0;
                                   
            // Desenha os eixos coordenados.
            g.setColor(Color.BLACK);
            g.drawLine( center[X] - L, center[Y], center[X] + L, center[Y] );
            g.drawLine( center[X], center[Y] - L, center[X], center[Y] + L );
                        
            /*
             * Desenha o digrama de fases do oscilador harmônico.
             */            
            prevPosX = center[X] + (int)(x[0][0]/maxX * (L-R));
            prevPosY = center[Y] + (int)(x[0][1]/maxY * (L-R));

            g.setColor(new Color(0.5f,0.5f,0.5f));
            for ( int i = 1; i < frame; i++ ){
                                
                d = frame - i;
                                
                xPos = center[X] + (int)( x_hO[i][0]/maxX * (L-R));
                yPos = center[Y] + (int)( x_hO[i][1]/maxY * (L-R));
            
                g.drawLine(prevPosX,getSize().height-prevPosY,xPos,getSize().height-yPos);
                
                prevPosX = xPos;
                prevPosY = yPos;
                                
                if ( d == 1 ){
                    g.setColor(Color.BLACK);
                    g.fillOval(xPos-(R>>1),getSize().height-yPos-(R>>1),R,R);
                }
                else if ( d <= trail + 1 ){
                    float component = 0.5f*(float)d/(trail+1);
                    g.setColor(new Color(component,component,component));
                    g.fillOval(xPos-(R>>1),getSize().height-yPos-(R>>1),R,R);
                }
            }           
            
            /*
             * Desenha o digrama de fases do oscilador de Van-der Pol.
             */            
            prevPosX = center[X] + (int)(x[0][0]/maxX * (L-R));
            prevPosY = center[Y] + (int)(x[0][1]/maxY * (L-R));

            g.setColor(Color.BLUE);
            for ( int i = 1; i < frame; i++ ){
                                
                d = frame - i;
                
                xPos = center[X] + (int)(x[i][0]/maxX * (L-R));
                yPos = center[Y] + (int)(x[i][1]/maxY * (L-R));
            
                g.drawLine(prevPosX,getSize().height-prevPosY,xPos,getSize().height-yPos);
                
                prevPosX = xPos;
                prevPosY = yPos;
                                
                if ( d == 1 ){
                    g.setColor(Color.BLUE);
                    g.fillOval(xPos-(R>>1),getSize().height-yPos-(R>>1),R,R);
                }
                else if ( d <= trail + 1 ){
                    float component = (float)d/(trail+1);
                    g.setColor(new Color(component,component,1f));
                    g.fillOval(xPos-(R>>1),getSize().height-yPos-(R>>1),R,R);
                }
            }
            
            // Desenha e identifica a condição inicial.
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            xPos = center[X] + (int)(x[0][0]/maxX * (L-R));
            yPos = center[Y] + (int)(x[0][1]/maxY * (L-R));
            
            g.setColor(Color.RED);
            g.fillRect(xPos-(R>>2), getSize().height-yPos-(R>>2), R>>1, R>>1 );
            g.drawLine(xPos-(R>>1), getSize().height-yPos, xPos+(R>>1), getSize().height-yPos );
            g.drawLine(xPos, getSize().height-yPos-(R>>1), xPos, getSize().height-yPos+(R>>1) );
            g.drawString( IC, xPos + 5, getSize().height - yPos - 5 );
            
            // Escreve o rótulo dos eixos coordenados.
            g.setColor(Color.BLACK);
            g.drawString( "x", center[X] + L + GAP, center[Y] + (metrics.getHeight()>>2) );
            g.drawString( "dx/dt", center[X] - (metrics.stringWidth("dx/dt")>>1), center[Y] - L - GAP );
            
            // Escreve a legenda.
            int[] basePt = new int[]{center[X] - L, center[Y] + L + (M>>1)};
            g.setStroke(new BasicStroke(1.5f));                        
            
            g.setColor(Color.BLUE);
            g.drawLine( basePt[X], basePt[Y], basePt[X] + 2*GAP, basePt[Y]);
            g.drawString( caption[0], basePt[X] + 3*GAP, basePt[Y] + (metrics.getHeight()>>2) );
            
            g.setColor(Color.GRAY);
            g.drawLine( basePt[X] + 5*GAP + metrics.stringWidth(caption[0]), basePt[Y], basePt[X] + 7*GAP + metrics.stringWidth(caption[0]), basePt[Y] );
            g.drawString( caption[1], basePt[X] + 8*GAP + metrics.stringWidth(caption[0]), basePt[Y] + (metrics.getHeight()>>2) );
            
            g.setColor(Color.RED);
            g.drawString( caption[2], basePt[X] + 10*GAP + metrics.stringWidth(caption[0]) + metrics.stringWidth(caption[1]), basePt[Y] + (metrics.getHeight()>>2) );            
        }
    }
        
    private class LogoPanel extends JComponent implements MouseListener{
    
		private static final long serialVersionUID = 1L;
		
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
    
    // Componentes GUI
    private AnimationPanel animationPanel;
    private JButton[] button;
    private JLabel[] label;
    private JPanel[] panel;
    private JSpinner[] spinner;
    private JTextField[] textField;
    private JRadioButtonMenuItem[] radioButton;
    private JScrollPane scrollPane;
    private JTabbedPane tabbedPane;
    private JMenu languageMenu;
    private JMenu aboutMenu;
    private JMenuBar menuBar;    
    private JTextArea textArea;
    private JMenuItem aboutOption;
    
    // Membros privados (não-GUI).
    private Locale[] availableLocales;
    private ResourceBundle idiom;
    private ImageIcon startIcon, stopIcon;
    private Timer timer;
    private DecimalFormat[] numberFormat;
    private Font font;
    
    private String[] caption;
    private String IC;
    
    private RungeKuttaCashKarpSolver vanderPol;
        
    private int nFrames;
    private int frame;
    
    private double spAmplitude, spPhase;
    
    private double[] t, tmp_t;
    private double[][] x, tmp_x, x_hO;
    private double dt;
    
    private double maxX, maxY;
    private double tStart, trueTime;
    
    private double x0,        prev_x0;
    private double dx0,       prev_dx0;
    private double epsilon,   prev_epsilon;
    private double F,         prev_F;
    private int    tI,        prev_tI;
    private int    tF,        prev_tF;
    private int    f,         prev_f;
    private int    frameRate, prev_frameRate;
    private int    trail,     prev_trail;
    
    private final double STD_X0         = 1;   // Posição inicial (unidades arbitrárias, u).
    private final double STD_DX0        = 0.5; // Velocidade inicial (u/s).
    private final double STD_EPSILON    = 0.9; // Parâmetro de amortecimento da equação de Van-der Pol. (1/u^2/s)
    private final double STD_F          = 0;   // Intensidade do sinal externo (u/s/s).
    private final int    STD_TI         = 0;   // Tempo inicial (s).    
    private final int    STD_TF         = 30;  // Duração da animação (s).
    private final int    STD_FREQUENCY  = 0;   // Freqüencia do sinal externo (Hz).    
    private final int    STD_FRAME_RATE = 30;  // Frame-rate, em quadros/segundo.
    private final int    STD_TRAIL      = 5;   // Rastro, em segundos.
        
    private final int ODE_ORDER = 2;
    private final int MAX_FRAMES = 6000; // Quantidade máxima de frames = duração máxima * frame-rate máximo.
}
