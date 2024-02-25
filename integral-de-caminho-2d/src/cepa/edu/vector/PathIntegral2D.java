package cepa.edu.vector;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.DecimalFormat;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import cepa.util.Arrow;
import cepa.util.ExtendedCubicBezier;
import cepa.util.FieldList;
import cepa.util.GraphCanvas;
import cepa.util.VectorField2D;
import java.awt.RenderingHints;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * @author ivan.pagnossin
 */
public final class PathIntegral2D extends JApplet{

	private static final long serialVersionUID = 1L;

	private ResourceBundle languageBundle;

    private DecimalFormat numberFormat = new DecimalFormat("###,##0.00"); // Formato de apresentação dos números.
    
    private JLabel lineIntegral = new JLabel("0", JLabel.CENTER);           // Exibição do valor da integral.
    private JLabel dPhi = new JLabel("0", JLabel.CENTER );                  // Exibição da diferença de potencial.    
    private JLabel selPtLabel = new JLabel("none", JLabel.CENTER );
    
    private JPopupMenu popup = new JPopupMenu();
       
    private JMenuItem opt_addPoint, opt_delPoint, opt_closePath, opt_openPath, opt_resetPath, opt_clearPath;
    private JCheckBoxMenuItem opt_editPath, opt_show_axis, opt_show_path, opt_show_field, opt_show_dL;
    
    private boolean closedPath = false;
    
    private boolean show_clickedPoint = false;
    
    private int nIntegral = 750;
    private int nPath = 10;
    private int nGrid = 15;
    private int fF = 20;
    
    private float integral = 0f;
    
    private float maxStrength = 0f;
    //private JRadioButtonMenuItem[] fieldOptions;
    private ButtonGroup fieldGroup = new ButtonGroup();
    
    private Arrow fieldArrow, fieldOverPathArrow, pathArrow;
    
    private VectorField2D actualField;
    private float[] clickedPoint;
    private GraphCanvas map;
    private float xI = -5, xF = +5, yI = -5, yF = +5, dx, dy, R;
    
    private boolean editPath = true;
    private int selected = -1;
        
    JCheckBox dLoption = new JCheckBox();
    
    private ExtendedCubicBezier bezier = new ExtendedCubicBezier(2);
    
    private FieldPanel fieldPanel = new FieldPanel();
    
    private int MAX_nGRID = 30, MIN_nGRID = 10;
    
    // Define Look & Feel.
    @Override
    public void start(){
        super.start();
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
            SwingUtilities.updateComponentTreeUI(popup);
        }
        catch( Exception e ){/*Nada*/}        
    }
    
    // Inicia os componentes.
    @Override
    public void init(){
        
        //------------------------------------------------------------------------
        // Configura o menu de línguas.
        //------------------------------------------------------------------------        
        try{
            languageBundle = ResourceBundle.getBundle("LanguageBundle", Locale.getDefault()); // NOI18N         
        }
        catch( MissingResourceException e ){ /* Nada */ }
        
        if ( languageBundle == null ) languageBundle = ResourceBundle.getBundle("LanguageBundle", new Locale("en", "US")); // NOI18N 
        
        
        dLoption.setText(languageBundle.getString("show_as_infinitesimal"));
        popup.setLabel(languageBundle.getString("path"));
        selPtLabel.setText(languageBundle.getString("none"));
        
        int CANVAS_SIZE = 500, CANVAS_MARGIN = 50;
        
        map = new GraphCanvas(
            new float[]{xI,xF,yI,yF},
            new int[]{CANVAS_SIZE,CANVAS_SIZE},
            new int[]{CANVAS_MARGIN,CANVAS_MARGIN}
        );
        
        R = Math.max(Math.abs(yF-yI),Math.abs(xF-xI))/100;
        
        dx = (xF - xI)/(nGrid - 1);
        dy = (yF - yI)/(nGrid - 1);        
        fieldArrow = new Arrow( 0.7f*Math.min(dx,dy), 0.5f );        
        fieldOverPathArrow = new Arrow( Math.max(dx,dy), 0f );        
        pathArrow  = new Arrow( 1f, 0f );
        pathArrow.holdHeadSize();
        
        fieldPanel.addMouseListener( new FieldPanelListener() );
        fieldPanel.addMouseMotionListener( new FieldPanelListener() );
             
        //----------------------------------------------------------------------
        // Compõe o menu "Campos vetoriais".
        //----------------------------------------------------------------------        
        FieldList fields = new FieldList();
        JMenu fieldsMenu = new JMenu( languageBundle.getString("vector_fields") );
        JMenu exhibitionMenu = new JMenu( languageBundle.getString("exhibit") );
        
        fieldsMenu.add( new JLabel(languageBundle.getString("conservative_fields")) );
        fieldsMenu.addSeparator();
        fieldsMenu.add( new JLabel(languageBundle.getString("non_conservative_fields")) );
                
        for ( int i = 0; i < fields.size(); i++ ){
            final VectorField2D vF = fields.elementAt(i);

            final JRadioButtonMenuItem fieldOption = new JRadioButtonMenuItem(vF.getDescription());                
            fieldOption.addActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent e) {
                    if ( fieldOption.isSelected() ) actualField = vF;
                    fieldPanel.setMaxFieldStrength();
                    fieldPanel.repaint();
                }

            });                
            
            fieldGroup.add(fieldOption);
            
            if ( vF.isConservative() ) fieldsMenu.add(fieldOption,1);
            else fieldsMenu.add(fieldOption);      

            // Habilita a primeira opção.
            if ( i == 0 ){                    
                fieldOption.setSelected(true);                    
                actualField = vF;
                fieldPanel.setMaxFieldStrength();
            }
        }        

        //----------------------------------------------------------------------
        // Compõe as opções do menu "popup" e "edição".
        //----------------------------------------------------------------------     
        opt_editPath = new JCheckBoxMenuItem( languageBundle.getString("handle_path") );    
        opt_addPoint = new JMenuItem( languageBundle.getString("add_point") );
        opt_delPoint = new JMenuItem( languageBundle.getString("remove_point") );
        opt_closePath = new JMenuItem( languageBundle.getString("close_path") );                        // Opção (de menu) "fechar o caminho".
        opt_openPath = new JMenuItem( languageBundle.getString("open_path") );        
        opt_resetPath = new JMenuItem( languageBundle.getString("initial_path") );             // Opção (de menu) "retornar ao caminho inicial".
        opt_clearPath = new JMenuItem( languageBundle.getString("erase_path") );                   // Opção (de menu) "apagar o caminho".

            
        opt_show_axis  = new JCheckBoxMenuItem( languageBundle.getString("exhibit_coordinate_axis") );
        opt_show_path  = new JCheckBoxMenuItem( languageBundle.getString("exhibit_path") );
        opt_show_field = new JCheckBoxMenuItem( languageBundle.getString("exhibit_field_over_path") );
        opt_show_dL   = new JCheckBoxMenuItem( languageBundle.getString("exhibit_displacement_vectors_over_path") );
        
        opt_show_dL.setSelected(false);
        dLoption.setEnabled(false);
        opt_show_path.setSelected(true);
        opt_editPath.setSelected(true);
        
        opt_show_axis.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                fieldPanel.repaint();
            }
            
        });
        opt_show_path.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                fieldPanel.repaint();
            }
            
        });
        opt_show_field.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                fieldPanel.repaint();
            }
            
        });        
        opt_show_dL.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                if ( opt_show_dL.isSelected() ) dLoption.setEnabled(true);
                else dLoption.setEnabled(false);
                fieldPanel.repaint();
            }
            
        });        
        dLoption.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                fieldPanel.repaint();
            }
            
        });
                
        opt_editPath.addActionListener(new ActionListener(){
            
            public void actionPerformed(ActionEvent e) {
                editPath = opt_editPath.isSelected();
                fieldPanel.repaint();
            }            
        });           
        opt_addPoint.addActionListener(new ActionListener(){

            // Adiciona um ponto (do tipo âncora) à curva Bezier.
            public void actionPerformed(ActionEvent e) {
                                        
                if ( clickedPoint[0] >= xI + 5*R && 
                     clickedPoint[0] <= xF - 5*R && 
                     clickedPoint[1] >= yI + 5*R && 
                     clickedPoint[1] <= yF - 5*R ){

                     bezier.addHandle(clickedPoint);
                     show_clickedPoint = false;
                     selected = bezier.size()-1; 
                }
                
                closedPath = false;
                                
                opt_editPath.setEnabled(!bezier.isEmpty());
                opt_addPoint.setEnabled(!closedPath);
                opt_delPoint.setEnabled(!bezier.isEmpty());
                opt_closePath.setEnabled(!closedPath);
                opt_openPath.setEnabled(closedPath);
                opt_resetPath.setEnabled(true);
                opt_clearPath.setEnabled(!bezier.isEmpty());                
                
                fieldPanel.repaint();
            }
        });        
        opt_delPoint.addActionListener(new ActionListener(){

            // Remove um ponto da curva Bezier.
            public void actionPerformed(ActionEvent e) {
                bezier.delete(selected);
                                           
                if ( bezier.size() < 4 || closedPath && (selected == 0 || selected == bezier.size() - 1) ){
                    closedPath = false;
                }
                
                opt_editPath.setEnabled(!bezier.isEmpty());
                opt_addPoint.setEnabled(!closedPath);
                opt_delPoint.setEnabled(!bezier.isEmpty());
                opt_closePath.setEnabled(bezier.size() >= 4);
                opt_openPath.setEnabled(closedPath);
                opt_resetPath.setEnabled(true);
                opt_clearPath.setEnabled(!bezier.isEmpty());                 
                                
                selected = -1;
                show_clickedPoint = false;
                fieldPanel.repaint();
            }            
        });
        opt_closePath.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                
                float[] pA = bezier.getPoint(0);
                float[] pB = bezier.getPoint(bezier.size()-1);
                
                if ( dist(pA,pB) < 2 * R ){
                    bezier.specialSet(pA,bezier.size()-1);
                }
                else{
                    bezier.addHandle(pA);
                }
                
                closedPath = true;
                
                opt_editPath.setEnabled(!bezier.isEmpty());
                opt_addPoint.setEnabled(!closedPath);
                opt_delPoint.setEnabled(!bezier.isEmpty());
                opt_closePath.setEnabled(!closedPath);
                opt_openPath.setEnabled(closedPath);
                opt_resetPath.setEnabled(true);
                opt_clearPath.setEnabled(!bezier.isEmpty());
                
                show_clickedPoint = false;
                
                fieldPanel.repaint();
            }            
        });
        opt_openPath.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                
                int size = bezier.size();                
                if ( size > 0 ){
                    float[] p = bezier.getPoint(size-1);
                    p[0] += 2*R;
                    p[1] += 2*R;
                    bezier.specialSet(p, size-1);
                }
                
                closedPath = false;
                
                opt_editPath.setEnabled(!bezier.isEmpty());
                opt_addPoint.setEnabled(!closedPath);
                opt_delPoint.setEnabled(!bezier.isEmpty());
                opt_closePath.setEnabled(!closedPath);
                opt_openPath.setEnabled(closedPath);
                opt_resetPath.setEnabled(true);
                opt_clearPath.setEnabled(!bezier.isEmpty());                
                
                fieldPanel.repaint();
            }            
        });
        opt_clearPath.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                if ( !bezier.isEmpty() ) bezier.delete();
                
                closedPath = false;
                
                opt_editPath.setEnabled(!bezier.isEmpty());
                opt_addPoint.setEnabled(!closedPath);
                opt_delPoint.setEnabled(!bezier.isEmpty());
                opt_closePath.setEnabled(closedPath);
                opt_openPath.setEnabled(closedPath);
                opt_resetPath.setEnabled(true);
                opt_clearPath.setEnabled(!bezier.isEmpty());
                
                fieldPanel.repaint();
            }
            
        });        
        opt_resetPath.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                setDefaultPath();
                
                opt_editPath.setEnabled(!bezier.isEmpty());
                opt_addPoint.setEnabled(!closedPath);
                opt_delPoint.setEnabled(!bezier.isEmpty());
                opt_closePath.setEnabled(!closedPath);
                opt_openPath.setEnabled(closedPath);
                opt_resetPath.setEnabled(true);
                opt_clearPath.setEnabled(!bezier.isEmpty());                
                
                fieldPanel.repaint();
            }
            
        });
                
        //----------------------------------------------------------------------
        // Monta o menu popup.
        //----------------------------------------------------------------------        
        popup.add(opt_editPath);
        popup.addSeparator();
        popup.add(opt_addPoint);
        popup.add(opt_delPoint);
        popup.addSeparator();
        popup.add(opt_closePath);
        popup.add(opt_openPath);
        popup.addSeparator();
        popup.add(opt_resetPath);
        popup.add(opt_clearPath);
                
        exhibitionMenu.add(opt_show_axis);
        exhibitionMenu.add(opt_show_path);
        exhibitionMenu.add(opt_show_field);
        exhibitionMenu.add(opt_show_dL);
        
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);         
        menuBar.add(fieldsMenu);
        menuBar.add(exhibitionMenu);
                   

        
        
        

        final JSlider fadingSlider = new JSlider(0,100,fF);
        final JSlider gridSlider = new JSlider(MIN_nGRID,MAX_nGRID,nGrid);
        final JSlider pathSlider = new JSlider(3,20,nPath);
        
        fadingSlider.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e) {
                fF = fadingSlider.getValue();
                fieldPanel.repaint();
            }
            
        });
        gridSlider.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e) {
                nGrid = gridSlider.getValue();
                fieldPanel.repaint();
            }
            
        });
        pathSlider.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e) {
                nPath = pathSlider.getValue();
                fieldPanel.repaint();
            }
            
        });
        
        JPanel sidePanel     = new JPanel( new GridLayout(8,1) );
        JPanel dPhiPanel     = new JPanel( new BorderLayout() );
        JPanel integralPanel = new JPanel( new BorderLayout() );        
        JPanel nPathPanel    = new JPanel( new BorderLayout() );
        
        String[] accuracies = new String[]{ languageBundle.getString("maximum"), languageBundle.getString("mean"), languageBundle.getString("minimum") };
        final int[] iterations = new int[]{1000,500,100};
        JLabel accuracyLabel = new JLabel( languageBundle.getString("acuracy") );
        final JComboBox accuracyOption = new JComboBox(accuracies);
        accuracyOption.setMaximumRowCount(3);
        accuracyOption.setSelectedIndex(1);
        accuracyOption.setSize(30,5);
        JPanel accuracyPanel = new JPanel( new GridLayout(1,2) );
        accuracyPanel.add(accuracyLabel);        
        accuracyPanel.add(accuracyOption);    
        accuracyOption.addItemListener(new ItemListener(){

            public void itemStateChanged(ItemEvent e) {
                int item = accuracyOption.getSelectedIndex();
                nIntegral = iterations[item];
                fieldPanel.repaint();
            }
            
        });
        
        
        TitledBorder integralBorder = new TitledBorder(
                new EtchedBorder(EtchedBorder.LOWERED), 
                languageBundle.getString("path_integral"),
                TitledBorder.CENTER,
                TitledBorder.TOP );
        integralBorder.setTitleColor( Color.BLACK );        
        integralPanel.add(lineIntegral,BorderLayout.CENTER);
        integralPanel.add(accuracyPanel, BorderLayout.EAST);
        integralPanel.setBorder(integralBorder);
        
        TitledBorder dPhiBorder = new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), languageBundle.getString("scalar_function_difference"), TitledBorder.CENTER, TitledBorder.TOP );
        dPhiBorder.setTitleColor( Color.BLACK );        
        dPhiPanel.add(dPhi, BorderLayout.CENTER);
        dPhiPanel.setBorder(dPhiBorder);
        
        TitledBorder fadingBorder = new TitledBorder( new EtchedBorder(EtchedBorder.LOWERED), languageBundle.getString("brightness"), TitledBorder.CENTER, TitledBorder.TOP );
        fadingBorder.setTitleColor( Color.BLACK );
        fadingSlider.setBorder(fadingBorder);
        //fadingPanel.add(fadingSlider, BorderLayout.CENTER );
        //fadingPanel.setBorder(fadingBorder);
        
        TitledBorder nGridBorder = new TitledBorder(
                new EtchedBorder(EtchedBorder.LOWERED), 
                languageBundle.getString("grid_size"), 
                TitledBorder.CENTER, 
                TitledBorder.TOP );
        nGridBorder.setTitleColor( Color.BLACK );
        gridSlider.setBorder(nGridBorder);
        gridSlider.setInverted(true);
        
        //nGridPanel.add(gridSlider, BorderLayout.CENTER );
        //nGridPanel.setBorder(nGridBorder);
        
        TitledBorder nPathBorder = new TitledBorder(
                new EtchedBorder(EtchedBorder.LOWERED),
                languageBundle.getString("displacement_over_path"),
                TitledBorder.CENTER,
                TitledBorder.TOP );
        
        
        nPathBorder.setTitleColor( Color.BLACK );
        nPathPanel.add(pathSlider, BorderLayout.CENTER );
        nPathPanel.add(dLoption, BorderLayout.SOUTH );
        nPathPanel.setBorder(nPathBorder);
        
        TitledBorder selPtBorder = new TitledBorder( new EtchedBorder(EtchedBorder.LOWERED), languageBundle.getString("selected_point"), TitledBorder.CENTER, TitledBorder.TOP );
        selPtBorder.setTitleColor( Color.BLACK );
        selPtLabel.setBorder(selPtBorder);
        
        sidePanel.add(dPhiPanel);
        sidePanel.add(integralPanel);        
        sidePanel.add(new JPanel());
        sidePanel.add(fadingSlider);        
        sidePanel.add(gridSlider);
        sidePanel.add(nPathPanel);        
        sidePanel.add(new JPanel());
        sidePanel.add(selPtLabel);
        

                
        setDefaultPath();
        
        this.add(fieldPanel, BorderLayout.CENTER);
        this.add(sidePanel, BorderLayout.EAST);
    }
        
    private void setDefaultPath(){
        if ( !bezier.isEmpty() ) bezier.delete();

        bezier.addHandle(new float[]{-2, -3});
        bezier.addHandle(new float[]{+3, -3});
        bezier.addHandle(new float[]{+3, +1});
        
        closedPath = false;
        
        opt_editPath.setEnabled(true);
        opt_editPath.setSelected(true);
        opt_addPoint.setEnabled(true);
        opt_delPoint.setEnabled(true);
        opt_closePath.setEnabled(true);        
        opt_openPath.setEnabled(false);
        opt_resetPath.setEnabled(false);
        opt_clearPath.setEnabled(true);
                
        integral = 0f;
        selected = -1;
    }
    
    protected float dist( float[] p1, float[] p2 ){
        return (float)Math.sqrt( Math.pow(p1[0]-p2[0],2) + Math.pow(p1[1]-p2[1],2) );
    }    
              
    private class FieldPanel extends JPanel{
        
		private static final long serialVersionUID = 1L;
		
		private Graphics2D graphics;
        
        @Override
        public void paintComponent( Graphics g ){
            super.paintComponent(g);
            
            this.graphics = (Graphics2D) g;
            RenderingHints hints = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHints(hints);
                        
            // Cor de fundo da área de exibição do campo vetorial: branca.
            setBackground(Color.WHITE);
            
            // Redefine a grade de exibição do campo vetorial (através de nGrid).
            dx = (xF - xI)/(nGrid - 1);
            dy = (yF - yI)/(nGrid - 1);
            
            R = Math.max(Math.abs(yF-yI),Math.abs(xF-xI))/100;

            // Redefine o tamanho das flechas (de modo a não se sobreporem demasiadamente).
            fieldArrow.setLength( 0.7f*Math.min(dx,dy) );        
            pathArrow.setLength( 2f*Math.min(dx,dy), true );
            
            
            
            
            //////////////////////////////////////////////////////////////////////////
            // Desenha o campo vetorial.
            //////////////////////////////////////////////////////////////////////////            		
            for( int i = 0; i <= nGrid-1; i++){
                
                float x = xI,
                      y = yI;
                
                x = xI + i * dx;
                for( int j = 0; j <= nGrid-1; j++ ){

                    y = yI + j * dy;
                    
                    this.graphics.setColor( vectorColor( actualField.getModulus(x,y)/maxStrength, true ) );                        
                    drawArrow(fieldArrow, new float[]{x,y}, actualField.getOrientation(x,y));
                }
                y = yI;
            }           
            
            //////////////////////////////////////////////////////////////////////////
            // Desenha os eixos coordenados.
            //////////////////////////////////////////////////////////////////////////                        
            if ( opt_show_axis.isSelected() ){
                
                graphics.setColor( Color.LIGHT_GRAY );                                   // Define a cor dos eixos e da escala.
                graphics.setFont( new Font( "Serif", Font.ROMAN_BASELINE, 12 ) );   // Define a fonte da escala.
                
                Arrow axis = new Arrow( 1f, 0f );                                   // Define a flecha a ser utilizada com eixo coordenado.
                axis.holdHeadSize();                                                // Fixa o tamanho da ponta da flecha (determinado por tentativa e erro).
                axis.setLength(Math.abs(xF-xI),true);                               // Define o tamanho da flecha (= extensão do gráfico).
                                
                //------------------------------------------------------------------------
                // Desenha o eixo x e sua escala.
                //------------------------------------------------------------------------
                drawArrow(axis,new float[]{xI,0f},0f);
                
                for ( Integer x = (int)xI; x < (int)xF; x++ ){
                    float[] p1 = new float[]{x,-0.1f},
                            p2 = new float[]{x,+0.1f},
                            p3 = new float[]{p1[0]-( x < 0 ? 0.1f : 0.05f ),p1[1]-0.3f};
                    
                    if ( x != 0 ){
                        drawLine( p1, p2 );            
                        drawString( x.toString(), p3 );
                    }
                }                                
                
                int[] xLabelPos = map.translate(new float[]{xF,0});
                xLabelPos[0] += 10;
                xLabelPos[1] += 5;
                graphics.drawString( "x", xLabelPos[0], xLabelPos[1] );     // Desenhando em termos de pixels fica independente da escala do gráfico.
                
                //------------------------------------------------------------------------
                // Desenha o eixo y e sua escala.
                //------------------------------------------------------------------------                
                drawArrow(axis,new float[]{0f,yI},(float)Math.PI/2);
                
                for ( Integer y = (int)yI; y < (int)yF; y++ ){
                    float[] p1 = new float[]{-0.1f,y},
                            p2 = new float[]{+0.1f,y},
                            p3 = new float[]{p1[0]-0.3f,p1[1]-0.1f};
                    
                    if ( y != 0 ){
                        drawLine( p1, p2 );                        
                        drawString( y.toString(), p3 );
                    }
                }   
                
                int[] yLabelPos = map.translate(new float[]{0,yF});
                yLabelPos[0] -= 5;
                yLabelPos[1] -= 10;
                graphics.drawString( "y", yLabelPos[0], yLabelPos[1] );     // Desenhando em termos de pixels fica independente da escala do gráfico.
            }
            
            
            //////////////////////////////////////////////////////////////////////////
            // Desenha o percurso e seus vetores (campo e deslocamento).
            //////////////////////////////////////////////////////////////////////////            
            float dt;
            if ( bezier.size() > 3 ){
                                
                // Desenha a curva Bezier.
                if ( opt_show_path.isSelected() ){
                    this.graphics.setColor(Color.BLUE);
                    this.graphics.setStroke(new BasicStroke(1.3f));
                    dt = 0.01f;
                    for( float t = 0; t < bezier.getRange(); t += dt ){
                    drawLine( bezier.at(t), bezier.at(t+dt) );
                    }                
                }
                                      
                graphics.setStroke(new BasicStroke(1.2f));
                
                dt = 1f / ( nPath - 1 );
                for ( float t = 0; t < bezier.getRange(); t += dt ){
                    
                    float[] p0 = bezier.at(t),
                            p1 = bezier.at(t+dt);
                    
                    // Desenha o campo vetorial sobre o percurso.
                    float angle = actualField.getOrientation(p0);
                    fieldOverPathArrow.setLength(0.7f*Math.max(dx,dy));
                                        
                    if ( opt_show_field.isSelected() ){                        
                        graphics.setColor( vectorColor(actualField.getModulus(p0)/maxStrength, false) );
                        drawArrow(fieldOverPathArrow,p0,angle);                        
                    }
                            
                    // Desenha os elementos de deslocamento do percurso.
                    if ( opt_show_dL.isSelected() ){
                        if ( dLoption.isSelected() ){
                            float derivative[] = bezier.derivative(t);
                            angle = (float)Math.atan2(derivative[1],derivative[0]);

                            graphics.setColor(Color.BLACK);
                            drawArrow(pathArrow,p0,angle);
                        }
                        else{
                            angle = (float)Math.atan2(p1[1]-p0[1],p1[0]-p0[0]);                    
                            pathArrow.setLength(dist(p0,p1), true );

                            graphics.setColor(Color.BLACK);
                            drawArrow(pathArrow,p0,angle);
                        }
                    }
                    
                }
            }
            
            //////////////////////////////////////////////////////////////////////////
            // Desenha as âncoras e os pontos-de-controle do percurso.
            //////////////////////////////////////////////////////////////////////////            
            if ( editPath ){
                this.graphics.setColor( Color.GRAY );
                this.graphics.setStroke(new BasicStroke(1f));
                
                // Desenha as linhas que ligam os "handles" aos seus "control-points".                
                for( int index1 = 0; index1 < bezier.size(); index1++ ){
                    int index2 = bezier.getHandleIndex(index1);
                    if ( index1 != index2 ) drawLine( bezier.getPoint(index1), bezier.getPoint(index2) );
                }

                // Desenha os pontos que definem a curva Bezier.
                for ( int i = 0; i < bezier.size(); i++ ){
                    float[] p = bezier.getPoint(i);
                    if (bezier.isHandle(i)) drawCircle(p, true);
                    else drawSquare(p, true);
                }
                
                // Destaca o ponto selecionado, se houver um.
                if ( selected != -1 ){
                    this.graphics.setColor(Color.BLACK);
                    float[] p = bezier.getPoint(selected);
                    if (bezier.isHandle(selected)) drawCircle(p, true);
                    else drawSquare(p, true);
                }                
            }
                       
            //////////////////////////////////////////////////////////////////////////
            // Desenha "A" e "B" próximos aos pontos inicial e final, respectivamente.
            //////////////////////////////////////////////////////////////////////////
            this.graphics.setColor( Color.BLACK );
            this.graphics.setFont( new Font( "Serif", Font.BOLD, 12 ) );
            
            if ( bezier.size() == 1 ){
                float p[] = bezier.getPoint(0);
                p[0] -= 2 * R;
                drawString( "A", p );
            }
            else if ( bezier.size() > 3 ){
                
                float[] p0, p1; float angle;
                
                p0 = bezier.getPoint(0);
                p1 = bezier.getPoint(1);
                if ( !editPath ) drawCircle(p0,true);                
                
                angle = (float)Math.atan2(p1[1]-p0[1],p1[0]-p0[0]);
                p0[0] -= 3 * R * (float)Math.cos(angle);
                p0[1] -= 3 * R * (float)Math.sin(angle);
                
                drawString( "A", p0 );
                
                p0 = bezier.getPoint(bezier.size()-2);
                p1 = bezier.getPoint(bezier.size()-1);
                if ( !editPath ) drawCircle(p1,true);
                
                angle = (float)(Math.atan2(p1[1]-p0[1],p1[0]-p0[0]) - Math.PI/4);
                p1[0] += 3 * R * (float)Math.cos(angle);
                p1[1] += 3 * R * (float)Math.sin(angle);  
                                
                drawString( "B", p1 );
                
            }            
            
            //////////////////////////////////////////////////////////////////////////
            // Desenha o ponto clicado.
            //////////////////////////////////////////////////////////////////////////              
            if ( show_clickedPoint ){ 
                float p1[] = new float[]{clickedPoint[0]-0.1f,clickedPoint[1]-0.1f},
                      p2[] = new float[]{clickedPoint[0]+0.1f,clickedPoint[1]+0.1f},
                      p3[] = new float[]{clickedPoint[0]-0.1f,clickedPoint[1]+0.1f},
                      p4[] = new float[]{clickedPoint[0]+0.1f,clickedPoint[1]-0.1f};
                drawLine(p1,p2);
                drawLine(p3,p4);
            }
            
            //////////////////////////////////////////////////////////////////////////
            // Calcula a integral de linha e a diferença de potencial.
            //////////////////////////////////////////////////////////////////////////            
            integral = 0;
            if ( bezier.size() > 3 ){
                
                // Calcula a integral e linha através da regra do trapézio.
                dt = (float)bezier.getRange()/(nIntegral-1);
                
                for ( int i = 0; i < nIntegral; i++ ){
                    float t = i * dt;

                    float sympsonFactor = 1f;
                    if ( i == 0 || i == nIntegral-1 ) sympsonFactor = 0.5f;
                    else sympsonFactor = 1f;

                    float[] F = actualField.getField(bezier.at(t));
                    float[] dL = bezier.derivative(t);
                    integral += sympsonFactor * ( F[0] * dL[0] + F[1] * dL[1] );
                }
                integral *= dt;
            }
                
            //////////////////////////////////////////////////////////////////////////
            // Atualiza a exibição da integral de linha e da diferença de potencial.
            //////////////////////////////////////////////////////////////////////////            
            lineIntegral.setText( numberFormat.format(integral) );

            if ( actualField.isConservative() ){
                if ( bezier.size() > 3 ){
                    float phi_A = actualField.getScalar(bezier.getPoint(0));
                    float phi_B = actualField.getScalar(bezier.getPoint(bezier.size()-1));
                    dPhi.setText( numberFormat.format(phi_B - phi_A) );                    
                }                    
                else dPhi.setText( "0,00" );
            }
            else dPhi.setText( languageBundle.getString("undefined") );
            
            
            //////////////////////////////////////////////////////////////////////////
            // Atualiza a exibição da posição do ponto selecionado.
            //////////////////////////////////////////////////////////////////////////            
            if ( selected != -1 ){
                String[] str = new String[]{
                    numberFormat.format(bezier.getPoint(selected)[0]),
                    numberFormat.format(bezier.getPoint(selected)[1])
                };
                selPtLabel.setText( "(" + str[0] + ", " + str[1] + ")" );
            }
            else selPtLabel.setText(languageBundle.getString("none"));
            
            
            
        }
        
        private Color vectorColor( float t, boolean fade ){
            
            // Mantém t no intervalo [0,1].
            t = Math.max( 0, Math.min(1,t) );
            
            float red   = 0.1f * t + 0.9f,
                  green = ( fade ? ((1f-fF/100f) - 0.9f) * t + 0.9f : -0.9f * t + 0.9f ), 
                  blue  = green;
            
            return new Color(red,green,blue);            
        }
        private void setMaxFieldStrength(){
            
            float x = xI, y = yI;		
                        
            maxStrength = 0;
            for( int i = 0; i <= nGrid-1; i++){
                x = xI + i * dx;
                
                for( int j = 0; j <= nGrid-1; j++ ){								
                    y = yI + i * dy;

                    float strength = actualField.getModulus(x,y);                    
                    if ( Float.isNaN(strength) ) continue;
                    maxStrength = Math.max(maxStrength,strength);
                    
                }
                y = yI;
            }		            
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
        private void drawSquare( float[] p, boolean filled ){
            int[] pp = map.translate(p);
            int size = map.translateX(0.886f*R);

            if ( filled ) graphics.fillRect( pp[0]-size, pp[1]-size, 2*size, 2*size );
            else graphics.drawRect( pp[0]-size, pp[1]-size, 2*size, 2*size );        
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
            }
            
            drawLine(pA,pB);
            graphics.fillPolygon(polygon);            
        }
        private void drawString( String string, float[] p ){
            int pp[] = map.translate(p);
            graphics.drawString( string, pp[0], pp[1] );
        }
//        private void drawImage( Image image, float[] p, float size_x, float size_y ){
//            int[] pp = map.translate(p);
//            int[] size = new int[]{map.translateX(size_x), map.translateY(size_y)};
//            graphics.drawImage( image, pp[0], pp[1], size[0], size[1], null );
//        }        
    }

    private class FieldPanelListener implements MouseListener,
                                                MouseMotionListener{
        
        public void mouseClicked(MouseEvent e) {}

        public void mousePressed(MouseEvent e) {
            fieldPanel.requestFocus();

            clickedPoint = map.translate(e.getX(),e.getY());

            if ( clickedPoint[0] < xI + 5*R ||
                 clickedPoint[0] > xF - 5*R ||
                 clickedPoint[1] < yI + 5*R ||
                 clickedPoint[1] > yF - 5*R ) show_clickedPoint = false;
            else show_clickedPoint = true;

            selected = -1;
            for ( int i = 0; i < bezier.size(); i++ ){
                if ( dist(clickedPoint,bezier.getPoint(i)) < R ){
                    selected = i;
                    show_clickedPoint = false;
                }
            }       
            
            if ( e.isPopupTrigger() ){
                // Apresenta o menu popup.
                if ( selected == -1 ){
                    opt_addPoint.setEnabled(show_clickedPoint & !closedPath );
                    opt_delPoint.setEnabled(false);
                }
                else{
                    opt_addPoint.setEnabled(false);
                    opt_delPoint.setEnabled(true);
                }
                popup.show(fieldPanel,e.getX(),e.getY());
            }
            
            fieldPanel.repaint();
        }

        public void mouseReleased(MouseEvent e) {
            mousePressed(e);
        }

        public void mouseEntered(MouseEvent e) {
            fieldPanel.requestFocus();
        }

        public void mouseExited(MouseEvent e) {}

        public void mouseDragged(MouseEvent e) {
            
            if ( editPath && selected != -1 ){
                
                float[] pF = map.translate(e.getX(), e.getY());    // ponto "final" (no momento do evento).
                                
                if (bezier.isHandle(selected)){
                    
                    // Limita a ÂNCORA arrastada à área do gráfico.
                    pF[0] = Math.max( xI+5*R, Math.min( xF-5*R, pF[0] ) );
                    pF[1] = Math.max( yI+5*R, Math.min( yF-5*R, pF[1] ) );
                                        
                    // Atualiza a ÂNCORA no objeto "bezier".
                    bezier.set(pF, selected);
                    
                    // Limita à área do gráfico os PONTOS-DE-CONTROLE associados à ÂNCORA movida.                    
                    for ( int i = selected-1; i <= selected+1; i+=2 ){
                        
                        if ( i > 0 && i < bezier.size()-1 ){
                            float[] p = bezier.getPoint(i);
                            
                            p[0] = Math.max( xI+2*R, Math.min(xF-2*R, p[0]) );
                            p[1] = Math.max( yI+2*R, Math.min(yF-2*R, p[1]) );
                                                        
                            bezier.specialSet(p,i);
                        }                        
                    }
                    
                    if ( closedPath && ( selected == 0 || selected == bezier.size()-1 ) ){
                                                                        
                        int hPIndex = 0;
                        if ( selected == 0 ) hPIndex = bezier.size() - 1;
                                                
                        // Atualiza a ÂNCORA no objeto "bezier".
                        bezier.set(pF, hPIndex);

                        // Limita à área do gráfico os PONTOS-DE-CONTROLE associados à ÂNCORA movida.                    
                        for ( int i = hPIndex-1; i <= hPIndex+1; i+=2 ){

                            if ( i > 0 && i < bezier.size()-1 ){
                                float[] p = bezier.getPoint(i);

                                p[0] = Math.max( xI+2*R, Math.min(xF-2*R, p[0]) );
                                p[1] = Math.max( yI+2*R, Math.min(yF-2*R, p[1]) );                                
                                
                                bezier.specialSet(p,i);
                            }
                        }
                    }                                        
                }
                else{
                    // Limita o PONTO-DE-CONTROLE à área do gráfico.
                    pF[0] = Math.max( xI+2*R, Math.min( xF-2*R, pF[0] ) );
                    pF[1] = Math.max( yI+2*R, Math.min( yF-2*R, pF[1] ) );
                                    
                    // Impede que o PONTO-DE-CONTROLE se aproxime demais de sua ÂNCORA.
                    float[] hP = bezier.getHandle(selected);
                    
                    for ( int i = 0; i < 2; i++ ){
                        float aux = pF[i] - hP[i];
                        if ( Math.abs(aux) < R ){
                            if ( aux < 0 ) pF[i] = hP[i] - R;
                            else pF[i] = hP[i] + R;
                        }
                    }
                                     
                    // Atualiza o ponto arrastado no objeto "bezier".
                    bezier.set(pF, selected);                    
                }
                
                fieldPanel.repaint();
            }
        }

        public void mouseMoved(MouseEvent e) {}
            
    }
}
