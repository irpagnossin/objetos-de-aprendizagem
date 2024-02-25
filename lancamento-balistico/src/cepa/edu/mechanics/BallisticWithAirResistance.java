package cepa.edu.mechanics;

import cepa.util.AnimationTimer;
import cepa.util.Arrow;
import cepa.util.GraphCanvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

/**
 *
 * @author  ivan.pagnossin
 */
public class BallisticWithAirResistance extends JApplet {
  
	private static final long serialVersionUID = 1L;
	
	// Constantes.
    private final int FRAME_RATE = 20;    
    private final double dt = 1.0/FRAME_RATE;

    private final int A = 0, B = 1;
    private final int X = 0, Y = 1;

    private final double[] X_RANGE = new double[]{ 0, 100 };
    private final double[] Y_RANGE = new double[]{ 0, 100 }; 
            
    private final double PROPCTE = 1.0;
    private final double DFLT_G  = 9.82;
    private final double DFLT_R  = 0.5;
    private final double DFLT_X  = X_RANGE[0];
    private final double DFLT_Y  = Y_RANGE[0];
    private final double DFLT_V  = PROPCTE * Math.sqrt( Math.pow(X_RANGE[1]-X_RANGE[0],2) + Math.pow(Y_RANGE[1]-Y_RANGE[0],2) ) / 2;
    private final double DFLT_A  = Math.atan2( Y_RANGE[1] - Y_RANGE[0], X_RANGE[1] - X_RANGE[0] );
    private final double DFLT_K  = Math.pow(DFLT_V,2)/2;
    private final double DFLT_U  = DFLT_G * DFLT_Y;
 
    private final double MIN_X = X_RANGE[0];
    private final double MAX_X = X_RANGE[1];
    private final double STEP_X = (MAX_X - MIN_X)/99;
    
    private final double MIN_Y = Y_RANGE[0];
    private final double MAX_Y = Y_RANGE[1];
    private final double STEP_Y = (MAX_Y - MIN_Y)/99;
    
    private final double MIN_V  = 0;
    private final double MAX_V  = PROPCTE * Math.sqrt(Math.pow(X_RANGE[1]-X_RANGE[0],2) + Math.pow(Y_RANGE[1]-Y_RANGE[0],2));
    private final double STEP_V = (MAX_V - MIN_V)/99;
    
    private final double MIN_K  = Math.pow(MIN_V,2)/2;
    private final double MAX_K  = Math.pow(MAX_V,2)/2;
    private final double STEP_K = (MAX_K - MIN_K)/99;
    
    private final double MIN_U  = DFLT_G * MIN_Y;
    private final double MAX_U  = DFLT_G * MAX_Y;
    private final double STEP_U = (MAX_U - MIN_U)/99;
    
    private final double MIN_R  = 0;
    private final double MAX_R  = 2;
    private final double STEP_R = (MAX_R - MIN_R)/99;

    private final double MIN_G  = 5;
    private final double MAX_G  = 15;
    private final double STEP_G = (MAX_G - MIN_G)/99;    
    
    private boolean stopAnimation = false;
    private ResourceBundle languageBundle;
    
    private Vector<Locale> locales;
    
    
    private double maximumEnergy;
        
    double[] pt,    // Ponto (x,y) da trajetória no tempo t.
            prevPt; // Idem ao anterior, no tempo t - dt.    
    
    private Launch thisLaunch;
    private Vector<Launch> launches;

    private AnimationTimer timer;
    
    private double[][] p;
        
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
    public void init(){
        
        launches = new Vector<Launch>();
        thisLaunch = new Launch();
        
        // O objeto timer repinta o launchPanel aproximadamente FRAME_RATE vezes por segundo.
        timer = new AnimationTimer( (int)(1000/FRAME_RATE), new ActionListener(){

            public void actionPerformed(ActionEvent e) {                                
                jPanel1.repaint();
                jPanel2.repaint();
                jPanel3.repaint();
                jPanel4.repaint();
            }
        } );
        
        p = new double[][]{
            {DFLT_X,DFLT_Y}, // Este ponto referencia a posição de lançamento (energia potencial).
            {DFLT_V*Math.cos(DFLT_A)/PROPCTE,DFLT_V*Math.sin(DFLT_A)/PROPCTE}
        };
        
        thisLaunch.setR(p[A]);
        thisLaunch.setV(new double[]{DFLT_V*Math.cos(DFLT_A),DFLT_V*Math.sin(DFLT_A)});
        thisLaunch.setG(DFLT_G);
        thisLaunch.setViscosityParameter(DFLT_R);
                                
        //double Dx = X_RANGE[1]-X_RANGE[0];
        //double Dy = Y_RANGE[1]-Y_RANGE[0];
        
        maximumEnergy = Math.pow(thisLaunch.getV()[X],2)/2 + Math.pow(thisLaunch.getV()[Y],2)/2 + thisLaunch.getG() * thisLaunch.getR()[Y];
        
        //maximumEnergy = (double)Math.pow(PROPCTE * Dx,2)/2 + Dy * (double)Math.max( Math.pow(PROPCTE,2)* Dy/2, DFLT_G );
        //maximumEnergy = maximumEnergy / 2;
        
        initComponents();
    }
    
    private void initComponents() {
        
        jPanel1 = new LaunchPanel();
        jPanel2 = new EnergyPanel();
        
        spinner = new JSpinner[8];
        for ( int i = 0; i < 8; i++ ) spinner[i] = new JSpinner();
        
        jButton = new JButton[2];
        for ( int i = 0; i < 2; i++ ) jButton[i] = new JButton();

        
        jPanel3 = new JPanel();
        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        jLabel5 = new JLabel();
        jLabel9 = new JLabel();
        jLabel10 = new JLabel();
        jLabel3 = new JLabel();
        jSeparator1 = new JSeparator();
        jPanel4 = new JPanel();
        jLabel7 = new JLabel();
        jLabel6 = new JLabel();
        jMenuBar1 = new JMenuBar();
        jMenu1 = new JMenu();
        jMenu2 = new JMenu();

        jCheckBoxMenuItem = new JCheckBoxMenuItem[4];
        for ( int i = 0; i < 4; i++ ){
            jCheckBoxMenuItem[i] = new JCheckBoxMenuItem();
            jCheckBoxMenuItem[i].addActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent e) {
                    jPanel1.repaint();
                }                
            });
            jMenu1.add(jCheckBoxMenuItem[i]);
        }
        

        
        jMenuBar1.add(jMenu1);
        jMenuBar1.add(new JPanel());
        jMenuBar1.add(jMenu2);
        setJMenuBar(jMenuBar1);        
        
        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(153, 153, 153)));
        jPanel1.setMaximumSize(new java.awt.Dimension(500, 500));
        jPanel1.setMinimumSize(new java.awt.Dimension(500, 500));
        jPanel1.addMouseListener(jPanel1);
        jPanel1.addMouseMotionListener(jPanel1);
        
        
        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 498, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 498, Short.MAX_VALUE)
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(153, 153, 153)));
        jPanel2.setMaximumSize(new java.awt.Dimension(50, 500));
        jPanel2.setMinimumSize(new java.awt.Dimension(50, 500));
        //jPanel2.addMouseListener(jPanel2);
        //jPanel2.addMouseMotionListener(jPanel2);

        
        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 48, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 498, Short.MAX_VALUE)
        );

        
        jButton[0].addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                
                for ( int i = 0; i < 8; i++ ) spinner[i].setEnabled(false);
                for ( int i = 0; i < 2; i++ ) jButton[i].setEnabled(false);
                
                timer.start();
            }
        });
        jButton[1].addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                launches.removeAllElements();
                
                jPanel1.repaint();
            }
        });
        
        spinner[0].setModel(new SpinnerNumberModel(DFLT_X, MIN_X, MAX_X, STEP_X));
        spinner[1].setModel(new SpinnerNumberModel(DFLT_Y, MIN_Y, MAX_Y, STEP_Y));
        spinner[2].setModel(new SpinnerNumberModel(DFLT_V, MIN_V, MAX_V, STEP_V));
        spinner[4].setModel(new SpinnerNumberModel(DFLT_K, MIN_K, MAX_K, STEP_K));
        spinner[5].setModel(new SpinnerNumberModel(DFLT_U, MIN_U, MAX_U, STEP_U));
        spinner[3].setModel(new SpinnerNumberModel(Math.toDegrees(DFLT_A), 0.0d, 360.0d, 1.0d));
        spinner[7].setModel(new SpinnerNumberModel(DFLT_R, MIN_R, MAX_R, STEP_R));
        spinner[6].setModel(new SpinnerNumberModel(DFLT_G, MIN_R, MAX_G, STEP_G));
                
        // Define as ações a serem tomadas quando da interação com os spinners.
        spinner[0].addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e) {
                
                double displacement = (double)((Double)spinner[0].getValue() - p[A][X]);
                
                p[A][X] += displacement;
                p[B][X] += displacement;
                
                jPanel1.repaint();
            }
        });        
        spinner[1].addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e) {
                
                double displacement = (double)((Double)spinner[1].getValue() - p[A][Y]);
                
                p[A][Y] += displacement;
                p[B][Y] += displacement;
                
                thisLaunch.setR( p[A].clone() );

                // Atualiza o spinner da energia potencial.
                double U = thisLaunch.getG() * p[A][Y];
                spinner[5].setValue(U);                
                
                maximumEnergy = Math.pow(thisLaunch.getV()[X],2)/2 + Math.pow(thisLaunch.getV()[Y],2)/2 + thisLaunch.getG() * thisLaunch.getR()[Y];
                
                jPanel1.repaint();
                jPanel2.repaint();
            }
        });
        spinner[2].addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e) {
                
                double speed = (Double) spinner[2].getValue();
                double angle = thisLaunch.getLaunchAngle();
                
                double vx = speed * Math.cos(angle);
                double vy = speed * Math.sin(angle);
                
                p[B][X] = p[A][X] + vx/PROPCTE;
                p[B][Y] = p[A][Y] + vy/PROPCTE;
                
                thisLaunch.setV(new double[]{vx,vy});
                
                // Atualiza o spinner da energia cinética.
                double K = Math.pow(speed,2)/2;
                spinner[4].setValue(K);
                
                maximumEnergy = Math.pow(thisLaunch.getV()[X],2)/2 + Math.pow(thisLaunch.getV()[Y],2)/2 + thisLaunch.getG() * thisLaunch.getR()[Y];
                
                // Atualiza os painéis de exibição.
                jPanel1.repaint();
                jPanel2.repaint();                
            }
        });
        spinner[3].addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e) {
                
                double speed = Math.sqrt(Math.pow(thisLaunch.getV()[X],2) + Math.pow(thisLaunch.getV()[Y],2));
                double angle = Math.toRadians((Double) spinner[3].getValue());
                
                double vx = speed * Math.cos(angle);
                double vy = speed * Math.sin(angle);
                
                p[B][X] = p[A][X] + vx/PROPCTE;
                p[B][Y] = p[A][Y] + vy/PROPCTE;
                
                thisLaunch.setV(new double[]{vx,vy});
                
                jPanel1.repaint();
            }
        });
        spinner[4].addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e) {
                
                double K = (Double) spinner[4].getValue();
                double speed = Math.sqrt(2*K);
                double angle = thisLaunch.getLaunchAngle();                
                
                double vx = speed * Math.cos(angle);
                double vy = speed * Math.sin(angle);
                
                p[B][X] = p[A][X] + vx/PROPCTE;
                p[B][Y] = p[A][Y] + vy/PROPCTE;
                
                thisLaunch.setV(new double[]{vx,vy});
                
                // Atualiza o spinner da velocidade.
                spinner[2].setValue(speed);
                
                maximumEnergy = Math.pow(thisLaunch.getV()[X],2)/2 + Math.pow(thisLaunch.getV()[Y],2)/2 + thisLaunch.getG() * thisLaunch.getR()[Y];
                
                jPanel1.repaint();
                jPanel2.repaint();
            }
        });
        spinner[5].addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e) {
                
                double U = (Double) spinner[5].getValue();
                double dy = p[B][Y] - p[A][Y];
                p[A][Y] = U / thisLaunch.getG();
                p[B][Y] = p[A][Y] + dy;
                
                thisLaunch.setR(p[A]);
                
                // Atualiza o spinner da posição inicial (y0).
                spinner[1].setValue(p[A][Y]);
                
                maximumEnergy = Math.pow(thisLaunch.getV()[X],2)/2 + Math.pow(thisLaunch.getV()[Y],2)/2 + thisLaunch.getG() * thisLaunch.getR()[Y];
                
                // Atualiza os painéis de exibição.
                jPanel1.repaint();
                jPanel2.repaint();
            }
        });
        spinner[6].addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e) {
                thisLaunch.setG((Double) spinner[6].getValue());
                maximumEnergy = Math.pow(thisLaunch.getV()[X],2)/2 + Math.pow(thisLaunch.getV()[Y],2)/2 + thisLaunch.getG() * thisLaunch.getR()[Y];
            }
        });
        spinner[7].addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e) {
                thisLaunch.setViscosityParameter((Double) spinner[7].getValue());
            }
        });
        
        // Organiza os spinners do primeiro grupo: x0, y0, v, theta, K e U.
        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(spinner[5], javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                            .addComponent(spinner[4], javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 83, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(spinner[3], javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                            .addComponent(spinner[2], javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                            .addComponent(spinner[1], javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                            .addComponent(spinner[0], javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(spinner[0], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(spinner[1], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(spinner[2], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(spinner[3], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(spinner[4], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(spinner[5], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(6, Short.MAX_VALUE))
        );
        
        // Organiza os spinners do segundo grupo: g e gamma.
        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(spinner[7])
                    .addComponent(spinner[6], javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(spinner[6], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(spinner[7], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        // Organiza os painéis.
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton[0])
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton[1])))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton[0])
                            .addComponent(jButton[1])))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(18, 18, 18))
        );

        //------------------------------------------------------------------------
        // Configura o menu de línguas.
        //------------------------------------------------------------------------        
        try{
            languageBundle = ResourceBundle.getBundle("LanguageBundle", Locale.getDefault()); // NOI18N         
        }
        catch( MissingResourceException e ){ /* Nada */ }
        
        if ( languageBundle == null ) languageBundle = ResourceBundle.getBundle("LanguageBundle", new Locale("en", "US")); // NOI18N 

        
        StringTokenizer[] tokenizer = new StringTokenizer[2];
        
        tokenizer[0] = new StringTokenizer(languageBundle.getString("locale.available"));
        int nTokens = tokenizer[0].countTokens();
        
        locales = new Vector<Locale>();
        
        if ( nTokens == 0 ) System.exit(-1);
        else if ( nTokens > 1 ){  
            
            for ( int i = 0; i < nTokens; i++ ){

                tokenizer[1] = new StringTokenizer( tokenizer[0].nextToken(), "_" );
                if ( tokenizer[1].countTokens() < 2 ) continue;
                
                String language = tokenizer[1].nextToken();
                String country  = tokenizer[1].nextToken();
                String variation = null;
                
                if (tokenizer[1].hasMoreElements()) variation = tokenizer[1].nextToken();
                
                if ( variation == null ) locales.add(new Locale(language,country));
                else locales.add(new Locale(language,country,variation));
            }
        }
        
        ButtonGroup languageGroup = new ButtonGroup();
        jRadioButtonMenuItem = new JRadioButtonMenuItem[nTokens];
        for ( int i = 0; i < nTokens; i++ ){
            
            final Locale locale = locales.elementAt(i);
            
            jRadioButtonMenuItem[i] = new JRadioButtonMenuItem();            
            jRadioButtonMenuItem[i].addActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent e) {
                    languageBundle = ResourceBundle.getBundle( "LanguageBundle", locale );
                    setI18NProperties();
                }
            });            
            jRadioButtonMenuItem[i].setText( ResourceBundle.getBundle("LanguageBundle",locale).getString("language.name") ); // NOI18N
            
            languageGroup.add(jRadioButtonMenuItem[i]);
            jMenu2.add(jRadioButtonMenuItem[i]);
            
            if ( ResourceBundle.getBundle("LanguageBundle",locale) == languageBundle ) jRadioButtonMenuItem[i].setSelected(true);
        }
                
        this.setI18NProperties();
    }
   
    private void setI18NProperties(){
        
        ImageIcon languageMenuIcon = getIcon(languageBundle.getString("languageMenu.icon"));            
        if ( languageMenuIcon != null ) jMenu2.setIcon(languageMenuIcon);
        else jMenu2.setText(languageBundle.getString("language"));
        
        jPanel3.setBorder(BorderFactory.createTitledBorder(languageBundle.getString("launch.parameters")));
        jButton[0].setText(languageBundle.getString("launch"));
        jButton[1].setText(languageBundle.getString("clear"));
        jLabel1.setText(languageBundle.getString("starting.position.x"));
        jLabel2.setText(languageBundle.getString("starting.speed"));
        jLabel5.setText(languageBundle.getString("angle"));
        jLabel9.setText(languageBundle.getString("kinetic.energy"));
        jLabel10.setText(languageBundle.getString("potential.energy"));
        jLabel3.setText(languageBundle.getString("starting.position.y"));
        jPanel4.setBorder(BorderFactory.createTitledBorder(languageBundle.getString("atmosphere.parameters")));
        jLabel7.setText(languageBundle.getString("air.resistance"));
        jLabel6.setText(languageBundle.getString("gravity.acceleration"));    
        jMenu1.setText(languageBundle.getString("exhibit"));          
                
        jCheckBoxMenuItem[0].setText(languageBundle.getString("coordinate.axis"));
        jCheckBoxMenuItem[1].setText(languageBundle.getString("speed"));
        jCheckBoxMenuItem[2].setText(languageBundle.getString("air.resistance(opt)"));
        jCheckBoxMenuItem[3].setText(languageBundle.getString("gravity"));
        
        // (Re)define o formato dos números nos spinners.
        for( int i = 0; i < 8; i++ ){
            JFormattedTextField textField = ((JSpinner.DefaultEditor) spinner[i].getEditor()).getTextField();
            DefaultFormatterFactory factory = (DefaultFormatterFactory) textField.getFormatterFactory();
            ((NumberFormatter) factory.getDefaultFormatter()).setFormat(new DecimalFormat("##.##", new DecimalFormatSymbols(languageBundle.getLocale())));            
            spinner[i].repaint();
        }         
    }
    
    private ImageIcon getIcon( String filename ){
                
        URL url = null;
        
        ClassLoader classLoader = this.getClass().getClassLoader();
        url = classLoader.getResource(filename);//ClassLoader.getSystemResource(filename);
        
        if ( url == null ) return null;
        else return new ImageIcon(url);
    }    
    
    private double dist( double[] p1, double[] p2 ){
        return (double)Math.sqrt( Math.pow(p1[0]-p2[0],2) + Math.pow(p1[1]-p2[1],2) );
    }    
    
    private class LaunchPanel extends JPanel implements MouseListener, MouseMotionListener{
        
		private static final long serialVersionUID = 1L;
		
		private final int CANVAS_MARGIN = 25; // Margem da área de exibição do lançamento, em pixels.
        private final int[] CANVAS_SIZE = new int[]{500,500}; // Dimensões da área de exibição do lançamento, em pixels (em x e em y).
        private int selectedPt = -1;
        private double R;        
        private double[] cursorPos;
        private GraphCanvas map;        
        private Graphics2D graphics2D;
        private Arrow arrow;
        
        public LaunchPanel(){
            super();
                        
            // Inicialmente o cursor está no centro da tela.
            cursorPos = new double[]{(X_RANGE[0]+X_RANGE[1])/2,(Y_RANGE[0]+Y_RANGE[1])/2};
            
            map = new GraphCanvas(
                new double[]{X_RANGE[0],X_RANGE[1],Y_RANGE[0],Y_RANGE[1]},
                new int[]{CANVAS_SIZE[0],CANVAS_SIZE[1]},
                new int[]{CANVAS_MARGIN,CANVAS_MARGIN}
            );   
            
            R = Math.min(Math.abs(Y_RANGE[1]-Y_RANGE[0]),Math.abs(X_RANGE[1]-X_RANGE[0]))/100;
            
            arrow = new Arrow(5*R,0);
        }
                
        @Override
        public void paintComponent( Graphics graphics  ){
            super.paintComponent(graphics);
                        
            this.graphics2D = (Graphics2D) graphics;
                        
            //////////////////////////////////////////////////////////////////////////
            // Encerra a animação [comandado pelo quadro (frame) anterior].
            //////////////////////////////////////////////////////////////////////////                        
            if ( stopAnimation ){
                stopAnimation = false;
                
                for ( int i = 0; i < 8; i++ ) spinner[i].setEnabled(true);
                for ( int i = 0; i < 2; i++ ) jButton[i].setEnabled(true);
                
                timer.stop();
            }            
            
            //////////////////////////////////////////////////////////////////////////
            // Desenha os eixos coordenados.
            //////////////////////////////////////////////////////////////////////////
            if ( jCheckBoxMenuItem[0].isSelected() ){
                graphics2D.setColor( Color.BLACK );
                drawLine(new double[]{0,0},new double[]{X_RANGE[1],0});
                drawString("x (m)",new double[]{X_RANGE[1]-3,-4});
                for ( int i = 0; i <= X_RANGE[1]; i+=10 ){
                    drawLine(new double[]{i,0},new double[]{i,-1});                
                    if ( i != X_RANGE[0] && i != X_RANGE[1] ) drawString(Integer.toString(i),new double[]{i-1.0,-4});
                }
                drawLine(new double[]{0,0},new double[]{0,Y_RANGE[1]});            
                drawString("y (m)",new double[]{-3,Y_RANGE[1]+1.5});
                for ( int i = 0; i <= Y_RANGE[1]; i+=10 ){
                    drawLine(new double[]{0,i},new double[]{-1,i});                
                    if ( i != Y_RANGE[0] && i != Y_RANGE[1] ) drawString( Integer.toString(i), new double[]{-4,i-1} );
                }            
            }
            //////////////////////////////////////////////////////////////////////////
            // Desenha o chão (se os eixos coordenados não estiverem em exibição).
            //////////////////////////////////////////////////////////////////////////            
            else{
                graphics2D.setColor( Color.BLACK );
                drawLine(new double[]{0,0},new double[]{X_RANGE[1],0});                
                for( int i = 0; i < X_RANGE[1]; i+=2 ){
                    drawLine( new double[]{i,-1}, new double[]{i+1,0} );
                }
            }
            
            //////////////////////////////////////////////////////////////////////////
            // Desenha a reta diretiva.
            //////////////////////////////////////////////////////////////////////////            
            if ( !timer.isRunning() ){
                graphics2D.setColor(Color.LIGHT_GRAY);
                drawLine(p[A],p[B]);
                drawCircle(p[A],R,true);            
                drawSquare(p[B],R,true);

                if ( selectedPt == A ){
                    graphics2D.setColor(Color.BLACK);
                    drawCircle(p[A],R,true);
                }
                else if ( selectedPt == B ){
                    graphics2D.setColor(Color.BLACK);
                    drawSquare(p[B],R,true);
                }
            }
            
            //////////////////////////////////////////////////////////////////////////
            // Redesenha os lançamentos anteriores.
            //////////////////////////////////////////////////////////////////////////
            graphics2D.setColor( Color.LIGHT_GRAY );

            for ( Launch launch : launches ){

                prevPt = launch.getR();

                for ( double t = dt; t < launch.getDuration(); t += dt ){

                    pt = launch.positionAt(t);
                    if ( map.has(pt) ) drawLine(prevPt,pt);
                                        
                    prevPt = pt.clone();

                    sleep();
                }
                
                drawCircle(launch.getEndingR(),R,true);
            }
            
            //////////////////////////////////////////////////////////////////////////
            // Efetua o lançamento (animação).
            //////////////////////////////////////////////////////////////////////////            
            if ( timer.isRunning() ){
                prevPt = thisLaunch.getR().clone();
                
                graphics2D.setColor( Color.BLUE );

                int n = 3;
                
                double elapsed = timer.getElapsedTime()/1000f;
                
                
                for ( double t = dt; t < elapsed; t += dt ){
                    
                    pt = thisLaunch.positionAt(t);
                    
                    if ( map.has(pt) ){
                        if ( elapsed - t > n * dt ) drawLine(prevPt,pt);
                    }
                    else if ( pt[Y] < Y_RANGE[0] + R ){
                        
                        double angle = Math.atan2(pt[Y]-prevPt[Y],pt[X]-prevPt[X]);
                        pt = new double[]{
                            prevPt[X] + (R-prevPt[Y])/Math.tan(angle),
                            R                            
                        };
                        
                        thisLaunch.setEndingR(pt.clone());                        
                        thisLaunch.setDuration(t);
                        
                        launches.add(new Launch(thisLaunch));
                        
                        stopAnimation = true;
                        break;
                    }
                                            
                    prevPt = pt.clone();                    
                    sleep();
                }
                
                drawCircle(pt,R,true);
                double[] v = thisLaunch.velocity(elapsed);
                
                double n0 = 5, n1 = 10;
                double v0 = Math.sqrt(Math.pow(thisLaunch.getV()[X],2)+Math.pow(thisLaunch.getV()[Y],2));
                double arrowSize = n0 + (n1-n0)*Math.sqrt(Math.pow(v[X],2)+Math.pow(v[Y],2))/v0;
                
                //////////////////////////////////////////////////////////////////////////
                // Desenha o vetor de velocidade.
                //////////////////////////////////////////////////////////////////////////                 
                if ( jCheckBoxMenuItem[1].isSelected() ){     
                    graphics2D.setColor(Color.BLUE);
                    arrow.setLength(arrowSize*R);
                    drawArrow(arrow,pt,Math.atan2(v[Y],v[X]));
                }
                
                //////////////////////////////////////////////////////////////////////////
                // Desenha o vetor de resistência do ar.
                //////////////////////////////////////////////////////////////////////////                 
                if ( jCheckBoxMenuItem[2].isSelected() && thisLaunch.getViscosityParameter() > 0.01 ){
                    graphics2D.setColor(Color.RED);
                    arrowSize *= 1 - thisLaunch.getViscosityParameter()/MAX_R/2;
                    arrow.setLength(arrowSize*R);
                    drawArrow(arrow,pt,Math.atan2(-v[Y],-v[X]));
                }
                
                //////////////////////////////////////////////////////////////////////////
                // Desenha o vetor da aceleração da gravidade.
                //////////////////////////////////////////////////////////////////////////                                 
                if ( jCheckBoxMenuItem[3].isSelected() ){
                    graphics2D.setColor(Color.BLACK);
                    arrow.setLength(5*R);
                    drawArrow(arrow,pt,-Math.PI/2);
                }
            }   
        }
        private void sleep(){
            try{
                Thread.sleep(0);
            }
            catch( InterruptedException e ) {/* Nada */}
        }        
        private void drawCircle( double[] p, double r, boolean filled ){
            int[] pp = map.translate(p);
            int radius = map.translateX(r);

            if ( filled ) graphics2D.fillOval( pp[0]-radius, pp[1]-radius, 2*radius, 2*radius );
            else graphics2D.drawOval( pp[0]-radius, pp[1]-radius, 2*radius, 2*radius );
        }   
        private void drawSquare( double[] p, double side, boolean filled ){
            int[] pp = map.translate(p);
            int size = map.translateX(side);

            if ( filled ) graphics2D.fillRect( pp[0]-size, pp[1]-size, 2*size, 2*size );
            else graphics2D.drawRect( pp[0]-size, pp[1]-size, 2*size, 2*size );        
        }        
        private void drawLine( double[] p1, double[] p2 ){
            int[] pp1 = map.translate(p1);
            int[] pp2 = map.translate(p2);

            graphics2D.drawLine( pp1[0], pp1[1], pp2[0], pp2[1] );
        }   
        private void drawString( String string, double[] p ){
            int pp[] = map.translate(p);
            graphics2D.drawString( string, pp[0], pp[1] );
        }        
        private void drawArrow( Arrow arrow, double[] p0, double angle ){

            Vector<double[]> arrowPoints = arrow.getArrow(angle);
            Polygon polygon = new Polygon();
            
            double[] pA = arrowPoints.elementAt(0).clone();
            pA[0] += p0[0]; pA[1] += p0[1];
            
            double[] pB = arrowPoints.elementAt(1).clone();
            pB[0] += p0[0]; pB[1] += p0[1];
            
            for ( int i = 1; i < arrowPoints.size(); i++ ){
                double[] p = arrowPoints.elementAt(i);
                p[0] += p0[0]; p[1] += p0[1];
                
                int[] pp = map.translate(p);
                
                polygon.addPoint( pp[0], pp[1] );
                sleep();
            }
            
            drawLine(pA,pB);
            graphics2D.fillPolygon(polygon);            
        }
        
        public void mouseClicked(MouseEvent e) {
            // Nada.
        }

        public void mousePressed(MouseEvent e) {
            jPanel1.requestFocus();
                    
            cursorPos = map.translate( e.getX(), e.getY() );            
            //boolean isInnerPt = map.has(cursorPos);
            
            selectedPt = -1;
            if ( dist(cursorPos,p[A]) < R ) selectedPt = A;
            else if ( dist(cursorPos,p[B]) < R ) selectedPt = B;
            
            jPanel1.repaint();
            jPanel2.repaint();
        }

        public void mouseReleased(MouseEvent e) {
            this.mousePressed(e);            
        }

        public void mouseEntered(MouseEvent e) {
            jPanel1.requestFocus();
        }

        public void mouseExited(MouseEvent e) {
            // Nada.
        }

        public void mouseDragged(MouseEvent e) {
            cursorPos = map.translate( e.getX(), e.getY() );
            //boolean isInnerPt = map.has(cursorPos);
                        
            if ( selectedPt != -1 ){
                double[] displacement = new double[]{
                    cursorPos[X] - p[A][X],
                    cursorPos[Y] - p[A][Y]
                };

                p[selectedPt] = cursorPos.clone();
                if ( selectedPt == A ){
                    thisLaunch.setR(p[selectedPt]);
                    p[B][X] += displacement[X];
                    p[B][Y] += displacement[Y];
                }

                // Atualiza os spinners.
                double vx = PROPCTE*(p[B][X]-p[A][X]);
                double vy = PROPCTE*(p[B][Y]-p[A][Y]);            

                spinner[0].setValue(p[A][X]);
                spinner[1].setValue(p[A][Y]);
                spinner[2].setValue(Math.sqrt(Math.pow(vx,2)+Math.pow(vy,2)));
                spinner[3].setValue(Math.toDegrees(Math.atan2(vy,vx)));
                spinner[4].setValue((Math.pow(vx,2)+Math.pow(vy,2))/2);
                spinner[5].setValue(thisLaunch.getG()*p[A][Y]);
                
                maximumEnergy = Math.pow(thisLaunch.getV()[X],2)/2 + Math.pow(thisLaunch.getV()[Y],2)/2 + thisLaunch.getG() * thisLaunch.getR()[Y];
                
                // Atualiza os painéis de exibição.
                jPanel1.repaint();
                jPanel2.repaint();                
            }
        }

        public void mouseMoved(MouseEvent e) {
            // Nada.
        }
        
    }    
    private class EnergyPanel extends JPanel/* implements MouseListener, MouseMotionListener*/{

		private static final long serialVersionUID = 1L;
		
		private final int CANVAS_MARGIN = 15;                // Margem, em pixels
        private final int[] CANVAS_SIZE = new int[]{50,500}; // Dimensões em x e y da área disponível, em pixels.
        private final int HANDLE_SIZE = CANVAS_MARGIN >> 1;  // Tamanho dos handles de controle das energias cinética e potencial gravitacional.
        private final int X = 0, Y = 1;                      // Enumeração auxiliar para identificar componentes x e y de vetores.
        private final int K = 0, U = 1;                      // Enumeração auxiliar para identificar energias potencial (U) e cinética (K).
        
        private int[][] handle;                              // Posição dos handles de energia cinética e potencial gravitacional, em pixels.
        private int selectedHandle;                          // Índice de identificação do handle selecionado: K ou U.
        
        private boolean updateHandle = true;                 // Flag auxiliar (evita recálculo)
        
        public EnergyPanel(){
            super();
            
            // Os elementos handle[K ou U][Y] são dinâmicos. Estes não mudam.
            handle = new int[2][2];
            handle[K][X] = CANVAS_SIZE[X] - HANDLE_SIZE;
            handle[U][X] = HANDLE_SIZE;
            
            updateHandles();
            
            // Inicialmente nenhum handle está selecionado.
            selectedHandle = -1;
        }
        
        @Override        
        public void paintComponent( Graphics graphics ){
            super.paintComponent(graphics);
            
            if ( updateHandle ) updateHandles();
            else updateHandle = true;
            
            // Desenha o retângulo vermelho na parte inferior, cuja altura representa a energia total.
            graphics.setColor(Color.BLACK);
            graphics.drawRect( CANVAS_MARGIN-1, CANVAS_MARGIN-1, CANVAS_SIZE[X] - 2*CANVAS_MARGIN + 1, CANVAS_SIZE[Y] - 2*CANVAS_MARGIN + 1);
            
            // Desenha o retângulo vermelho na parte inferior, cuja altura representa a energia cinética.
            graphics.setColor(Color.RED);
            graphics.fillRect( CANVAS_MARGIN, handle[K][Y], CANVAS_SIZE[X] - 2*CANVAS_MARGIN, CANVAS_SIZE[Y] - CANVAS_MARGIN - handle[K][Y] );
            
            /*graphics.drawLine( CANVAS_MARGIN, handle[K][Y], CANVAS_SIZE[X] - 3*(CANVAS_MARGIN >> 2), handle[K][Y] );
            
            if ( selectedHandle == K )
                graphics.fillRect( CANVAS_SIZE[X] - 3*(CANVAS_MARGIN >> 2), handle[K][Y] - (CANVAS_MARGIN >> 2), HANDLE_SIZE, HANDLE_SIZE );                            
            else
                graphics.drawRect( CANVAS_SIZE[X] - 3*(CANVAS_MARGIN >> 2), handle[K][Y] - (CANVAS_MARGIN >> 2), HANDLE_SIZE, HANDLE_SIZE );   
            */
            
            // Desenha o retângulo azul na parte superior, cuja altura representa a energia potencial gravitacional.            
            graphics.setColor(Color.BLUE);
            graphics.fillRect( CANVAS_MARGIN, handle[U][Y], CANVAS_SIZE[X] - 2*CANVAS_MARGIN, handle[K][Y] - handle[U][Y] );
            
            /*graphics.drawLine( 3 * (CANVAS_MARGIN >> 2), handle[U][Y], CANVAS_SIZE[X] - CANVAS_MARGIN, handle[U][Y] );
            
            if ( selectedHandle == U )
                graphics.fillRect( CANVAS_MARGIN >> 2, handle[U][Y] - (CANVAS_MARGIN >> 2), HANDLE_SIZE, HANDLE_SIZE );                
            else
                graphics.drawRect( CANVAS_MARGIN >> 2, handle[U][Y] - (CANVAS_MARGIN >> 2), HANDLE_SIZE, HANDLE_SIZE );   
            */            
        }
        
        /*
         * Recalcula a posição dos handles de energia cinética e potencial gravitacional com base no objeto thisLaunch.
         */
        private void updateHandles(){
 
            double[] energy = new double[2];
                        
            if ( timer.isRunning() ){
                double t = timer.getElapsedTime()/1000f;
                
                // Energia potencial gravitacional no instante t (da animação).
                energy[U] = thisLaunch.getG() * thisLaunch.positionAt(t)[Y];

                // Energia cinética no instante t (da animação).
                energy[K] = (double)Math.pow(thisLaunch.velocity(t)[X],2)/2 + (double)Math.pow(thisLaunch.velocity(t)[Y],2)/2;
            }
            else{
                // Energia potencial gravitacional no instante t = 0 (antes do lançamento).
                energy[U] = thisLaunch.getG() * thisLaunch.getR()[Y];    

                // Energia cinética no instante t = 0 (antes do lançamento).
                energy[K] = (double)Math.pow(thisLaunch.getV()[X],2)/2 + (double)Math.pow(thisLaunch.getV()[Y],2)/2;
                
            }
            
            energy[K] = Math.min(maximumEnergy, energy[K]);
                        
            int hU = (int)Math.rint( energy[U]/maximumEnergy * (CANVAS_SIZE[Y] - 2*CANVAS_MARGIN) );            
            int hK = (int)Math.rint( energy[K]/maximumEnergy * (CANVAS_SIZE[Y] - 2*CANVAS_MARGIN) );
            
            handle[K][Y] = CANVAS_SIZE[Y] - CANVAS_MARGIN - hK;
            handle[U][Y] = handle[K][Y] - hU;
        }
    
        /*
         * Recalcula o objeto thisLaunch com base na posição dos handles de energia cinética e potencial gravitacional.
         */
        private void updateEnergies(){
            
            double[] energy = new double[2];
            
            energy[K] = maximumEnergy * ( CANVAS_SIZE[Y] - CANVAS_MARGIN - handle[K][Y] ) / ( CANVAS_SIZE[Y] - 2 * CANVAS_MARGIN );
            energy[U] = maximumEnergy * ( handle[K][Y] - handle[U][Y] ) / ( CANVAS_SIZE[Y] - 2 * CANVAS_MARGIN );
            
            double angle = thisLaunch.getLaunchAngle();            
            double x0 = thisLaunch.getR()[X];
            double y0 = energy[U]/thisLaunch.getG();
            double vx0 = (double) (Math.sqrt( 2 * energy[K] ) / Math.sqrt( 1 + Math.pow(Math.tan(angle),2) ));
            double vy0 = (double) (vx0 * Math.tan(angle));
            
            thisLaunch.setR(new double[]{x0,y0});
            thisLaunch.setV(new double[]{vx0,vy0});
            
            // Atualiza os spinners.
            spinner[1].setValue(energy[U]/thisLaunch.getG());
            spinner[2].setValue(Math.sqrt(Math.pow(vx0,2)+Math.pow(vy0,2)));
            spinner[3].setValue(Math.toDegrees(angle));
            spinner[4].setValue(energy[K]);
            spinner[5].setValue(energy[U]);
            
            updateHandle = false;
        }
        
        private double dist( int[] p1, int[] p2 ){
            return (double)Math.sqrt( Math.pow(p1[0]-p2[0],2) + Math.pow(p1[1]-p2[1],2) );
        }
    
        public void mouseClicked(MouseEvent e) {
            // Nada.
        }

        public void mousePressed(MouseEvent e) {
            jPanel2.requestFocus();
            
            int[] cursorPos = new int[]{e.getX(),e.getY()};
            
            selectedHandle = -1;
            for ( int i = 0; i < 2; i++ ){
                if ( dist(cursorPos,handle[i]) < 1.5*HANDLE_SIZE ){
                    selectedHandle = i;
                    break;
                }
            }
            
            jPanel1.repaint();
            jPanel2.repaint();
        }

        public void mouseReleased(MouseEvent e) {
            this.mousePressed(e);
        }

        public void mouseEntered(MouseEvent e) {
            jPanel2.requestFocus();
        }

        public void mouseExited(MouseEvent e) {
            // Nada.
        }

        public void mouseDragged(MouseEvent e) {
           
            int dE = handle[K][Y] - handle[U][Y];
            
            if ( selectedHandle == U ){
                handle[U][Y] = Math.max( CANVAS_MARGIN, Math.min( handle[K][Y], e.getY() ) );
            }
            else if ( selectedHandle == K ){
                handle[K][Y] = Math.max( handle[K][Y]-handle[U][Y]+CANVAS_MARGIN, Math.min( CANVAS_SIZE[Y] - CANVAS_MARGIN, e.getY() ) );
                handle[U][Y] = handle[K][Y] - dE;
            }
                        
            updateEnergies();
            
            jPanel1.repaint();
            jPanel2.repaint();            
        }

        public void mouseMoved(MouseEvent e) {
            // Nada.
        }
    }
    
    private class Launch{
        
        private static final int X = 0, Y = 1;
        
        private double[] endingR = new double[2];
        private double[] r, v;
        private double g = 9.8d, gamma = 1, duration = 0;
        
        public Launch(){            
        }
        public Launch( Launch launch ){
            
            setR(launch.getR().clone());
            setV(launch.getV().clone());            
            setG(launch.getG());            
            setViscosityParameter(launch.getViscosityParameter());
            setDuration(launch.getDuration());
            setEndingR(launch.getEndingR().clone());
        }
        
        public void setEndingR( double[] endingR ){
            this.endingR = endingR;
        }
        
        public double[] getEndingR(){
            return endingR;
        }
        
        public double getLaunchAngle(){
            return Math.atan2(v[Y],v[X]);
        }
        
        public void setR( double[] r ){
            this.r = r;
        }
        
        public void setV( double[] v ){
            this.v = v;
        }
        
        public void setG( double g ){
            this.g = g;
        }
        
        public void setViscosityParameter( double gamma ){
            this.gamma = gamma;
        }
        
        public void setDuration( double duration ){
            this.duration = duration;
        }
                
        public double[] getR(){
            return r;
        }
        
        public double[] getV(){
            return v;
        }
        
        public double getG(){
            return g;
        }
        
        public double getViscosityParameter(){
            return gamma;
        }
        
        public double getDuration(){
            return duration;
        }
        
        public double[] positionAt( double time ){

            double x, y;
            
            if ( gamma < 0.01 ){
                x = r[X] + v[X] * time;
                y = r[Y] + v[Y] * time - g*Math.pow(time,2)/2;
            }
            else{
                x = r[X] - v[X]/gamma * ( Math.exp(-gamma*time) - 1 );
                y = r[Y] - g/gamma * time - 1/gamma * (v[Y]+g/gamma)*(Math.exp(-gamma*time)-1);
            }

            return new double[]{x,y};
        }

        public double[] velocity( double time ){

            double vx, vy;
            
            if ( gamma < 0.01 ){
                vx = v[X];
                vy = v[Y] - g * time;
            }
            else{
                vx = v[X] * Math.exp( -gamma * time );
                vy = -g/gamma + (v[Y] + g/gamma)*Math.exp(-gamma*time);
            }

            return new double[]{ (double)vx, (double)vy };
        }        
                   
    }    
    
    private JButton[] jButton;
    private JLabel jLabel1;
    private JLabel jLabel10;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JLabel jLabel9;
    private JMenu jMenu1;
    private JMenu jMenu2;
    private JMenuBar jMenuBar1;
    private LaunchPanel jPanel1;
    private EnergyPanel jPanel2;
    private JPanel jPanel3;
    private JPanel jPanel4;
    private JSeparator jSeparator1;

    private JCheckBoxMenuItem[] jCheckBoxMenuItem;
    private JRadioButtonMenuItem[] jRadioButtonMenuItem;
    
    private JSpinner[] spinner;
}
