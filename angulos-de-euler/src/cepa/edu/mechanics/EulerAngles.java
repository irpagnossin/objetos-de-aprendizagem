/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cepa.edu.mechanics;


import com.mnstarfire.loaders3d.Inspector3DS;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.PlatformGeometry;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Group;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 *
 * @author ivan.pagnossin
 */
public class EulerAngles extends JFrame{

    private Quat4d[] aux = new Quat4d[3];

    private TransparencyAttributes tAttributes = new TransparencyAttributes(TransparencyAttributes.BLENDED,0.8f);
    private TransformGroup[] model = null;

    private Transform3D t = null; // A multi-purpose 3D transform.


    private final double eps = 0.001;
    private boolean hasNodeLine = false;

    //private TransformGroup nodeGroup;
    private TransformGroup staticFrame, targetGroup;

    private Canvas3D canvas3D = null;
    private final float SCALE = 0.5f;
    private SimpleUniverse universe = null;
    private BranchGroup scene;

    private final int ALPHA = 0, BETA = 1, GAMMA = 2;

    private boolean animating = false;

    private int angularSpeed = 360; // Graus por segundo.
    private int frameRate = 30;     // Quadros por segundo.

    private Transform3D transformation;
    private TransformGroup rotatingFrame;
    private TransformGroup lineOfNodesGroup;
    private BranchGroup nodeLineBGroup;
    private Quat4d allRotations = null;
    private Vector3d[] axis, targetAxis;
    private final Vector3d x = new Vector3d(1,0,0);
    private final Vector3d z = new Vector3d(0,0,1);

    private JSlider[] slider;

    private double azimute = 0;
    private double[] eulerAngle;

    private Hashtable<Integer,JLabel>[] sliderLabels;
    private JLabel undoLabel;


    private Vector<InstantHistory> history;

    private Hashtable<Integer,JLabel> angleScale;

    private InstantHistory[] lastCursorPos;

    // Define Look & Feel.

    public static void main (String[] args) {
        EulerAngles ea = new EulerAngles();
        ea.setVisible(true);
        ea.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ea.setSize(800,800);
    }

    public EulerAngles () {
    
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch( Exception e ){/*Nada*/}
    
        for ( byte i = 0; i < 3; i++ ) aux[i] = new Quat4d();

        model = new TransformGroup[2];
        t = new Transform3D();

        nodeLineBGroup = new BranchGroup();

        JButton reset = new JButton("Reset");
        reset.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                reset();
            }

        });


        JButton play = new JButton("sortear");
        play.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                targetPositioning();
            }
        });

        lastCursorPos = new InstantHistory[3];
        for ( int i = 0; i < 3; i++ ) lastCursorPos[i] = new InstantHistory(i,0);

        history = new Vector<InstantHistory>();


        angleScale = new Hashtable<Integer,JLabel>();
        for ( Integer angle = 0; angle <= 360; angle += 90 ) angleScale.put(angle,new JLabel(angle.toString()));

        sliderLabels = new Hashtable[3];
        for ( int i = 0; i < 3; i++ ) sliderLabels[i] = new Hashtable<Integer,JLabel>(angleScale);

        //ImageIcon icon = getIcon("icons/media-record.png");
        ImageIcon icon = getIcon("icons/undo1.png");
        undoLabel = new JLabel(icon);

        eulerAngle = new double[]{0,0,0};

        axis = new Vector3d[3];
        axis[0] = new Vector3d( 1, 0, 0 );  // Versor i original.
        axis[1] = new Vector3d( 0, 1, 0 );  // Versor j original.
        axis[2] = new Vector3d( 0, 0, 1 );  // Versor k original.

        targetAxis = new Vector3d[3];
        targetAxis[0] = new Vector3d( 1, 0, 0 );  // Versor i original.
        targetAxis[1] = new Vector3d( 0, 1, 0 );  // Versor j original.
        targetAxis[2] = new Vector3d( 0, 0, 1 );  // Versor k original.


        allRotations = new Quat4d(0,0,0,1);    // TransformaÃ§Ã£o inicial, representada por um quaternion identidade.
        transformation = new Transform3D();
        transformation.set(allRotations,new Vector3d(),SCALE);


        slider = new JSlider[3];

        JPanel controlPanel = new JPanel(new GridLayout(3,1));

        SliderListener listener = new SliderListener();
        for ( int i = 0; i < 3; i++ ){
            slider[i] = new JSlider(0,360,0);
            slider[i].addChangeListener(listener);
            slider[i].setMajorTickSpacing(90);
            slider[i].setMinorTickSpacing(10);
            slider[i].setPaintLabels(true);
            slider[i].setPaintTicks(true);
            slider[i].createStandardLabels(45);
            slider[i].setLabelTable(sliderLabels[i]);
            controlPanel.add(slider[i]);
        }

        canvas3D = createUniverse();
        scene = createScene();
        universe.addBranchGraph(scene);

        setLayout(new BorderLayout());
        add(canvas3D, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        add(reset, BorderLayout.EAST);
        add(play, BorderLayout.WEST);

    }

    private Canvas3D createUniverse(){

        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 10.0);

        PlatformGeometry platformGeometry = new PlatformGeometry();

        // Luz ambiente.
        Color3f ambientColor = new Color3f(1f, 1f, 1f);
        AmbientLight ambientLightNode = new AmbientLight(true,ambientColor);
        ambientLightNode.setInfluencingBounds(bounds);
        platformGeometry.addChild(ambientLightNode);

        // Duas luzes direcionais.
        Color3f light1Color = new Color3f(1.0f, 1.0f, 0.9f);
        Vector3f light1Direction  = new Vector3f(1.0f, 1.0f, 1.0f);
        Color3f light2Color = new Color3f(1.0f, 1.0f, 1.0f);
        Vector3f light2Direction  = new Vector3f(-1.0f, -1.0f, -1.0f);

        DirectionalLight light1 = new DirectionalLight(light1Color, light1Direction);
        light1.setInfluencingBounds(bounds);
        platformGeometry.addChild(light1);

        DirectionalLight light2 = new DirectionalLight(light2Color, light2Direction);
        light2.setInfluencingBounds(bounds);
        platformGeometry.addChild(light2);



        Canvas3D canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());

        universe = new SimpleUniverse(canvas);

        //----------------------------------------------------------------------
        // Prepara a cÃ¢mera.
        //----------------------------------------------------------------------
        ViewingPlatform viewingPlatform = universe.getViewingPlatform();
        viewingPlatform.setPlatformGeometry(platformGeometry);
        TransformGroup steerTG = viewingPlatform.getViewPlatformTransform();
        Transform3D t = new Transform3D();
        steerTG.getTransform(t);
        t.lookAt(new Point3d(1.5,1.5,1.5), new Point3d(0,0,0), new Vector3d(0,0,1));
        t.invert();
        steerTG.setTransform(t);

        OrbitBehavior orbit = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_TRANSLATE | OrbitBehavior.REVERSE_ROTATE  );
        orbit.setSchedulingBounds(bounds);
        viewingPlatform.setViewPlatformBehavior(orbit);

        return canvas;
    }

    public BranchGroup createScene(){


        // Carrega os modelos 3D.
        URL url = this.getClass().getClassLoader().getResource("resources/models/legoman.3DS");

        Inspector3DS loader = new Inspector3DS(url);
        loader.setURLBase("resources/models");
        loader.parseIt();
        model[0] = loader.getModel();

        loader = new Inspector3DS(url);
        loader.setURLBase("resources/models");
        loader.parseIt();
        model[1] = loader.getModel();

        Enumeration children = model[1].getAllChildren();
        traverse(children);


        targetGroup = createTargetGroup();
        targetGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        t.setIdentity();
        //t.setScale(SCALE);
        //targetGroup.setTransform(t);
        targetGroup.addChild(model[1]);
        //eulerRotation(targetGroup,new double[]{Math.PI/4,Math.PI/4,Math.PI/4});






        nodeLineBGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        nodeLineBGroup.setCapability(BranchGroup.ALLOW_DETACH);

        lineOfNodesGroup = new TransformGroup();
        lineOfNodesGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        TransformGroup staticGroup = createStaticGroup();

        //rotatingFrame = new TransformGroup();
        rotatingFrame = createRotatingGroup();
        rotatingFrame.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        rotatingFrame.addChild(model[0]);
        //rotatingFrame.addChild(new ColorCube(0.4));

        // Define a cor do fundo.
        /*Color3f bgColor = new Color3f(0f, 0f, 0f);
        Background bgNode = new Background(bgColor);
        bgNode.setApplicationBounds(new BoundingSphere(new Point3d(0.0,0.0,0.0), 1.0));*/

        TransformGroup objScale = new TransformGroup();
        Transform3D t3d = new Transform3D();
        t3d.setScale(0.4);
        objScale.setTransform(t3d);


        TransformGroup objTrans = new TransformGroup();
        objScale.addChild(objTrans);

        Background bg = new Background();
        bg.setApplicationBounds(new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0));
	BranchGroup backGeoBranch = new BranchGroup();
        Sphere sphereObj = new Sphere(1.0f, Sphere.GENERATE_NORMALS |
			          Sphere.GENERATE_NORMALS_INWARD |
				  Sphere.GENERATE_TEXTURE_COORDS, 45);
        Appearance backgroundApp = sphereObj.getAppearance();
        backGeoBranch.addChild(sphereObj);
        bg.setGeometry(backGeoBranch);
        objTrans.addChild(bg);


        URL bgImage = getClass().getClassLoader().getResource("resources/images/bg.jpg");
        TextureLoader tex = new TextureLoader(bgImage,
					      new String("RGB"), this);
        /*if (tex != null)
	    backgroundApp.setTexture(tex.getTexture());*/



        Appearance nodeApp = new Appearance();
        nodeApp.setLineAttributes(new LineAttributes(1f,LineAttributes.PATTERN_DASH,true));
        LineArray nodal = new LineArray(2,LineArray.COORDINATES);
        nodal.setCoordinate(0, new Point3d(+1,0,0));
        nodal.setCoordinate(1, new Point3d(-1,0,0));
        Shape3D nodalShape = new Shape3D(nodal,nodeApp);

        lineOfNodesGroup.addChild(nodalShape);
        nodeLineBGroup.addChild(lineOfNodesGroup);




//        Tetrahedron tetrahedron = new Tetrahedron(0.5);
//        Appearance app = new Appearance();
//        app.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_LINE,PolygonAttributes.CULL_NONE,0));
//        app.setLineAttributes(new LineAttributes(0.5f,LineAttributes.PATTERN_SOLID,true));
//        tetrahedron.setAppearance(app);
//
//        rotatingFrame.addChild(tetrahedron);






        //----------------------------------------------------------------------
        // CompÃµe o "branch group" raiz, que contÃ©m todos os elementos acima.
        //----------------------------------------------------------------------
        rotatingFrame.setTransform(transformation);
        BranchGroup root = new BranchGroup();
        root.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        root.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        root.setCapability(BranchGroup.ALLOW_DETACH);

        root.addChild(targetGroup);
        root.addChild(rotatingFrame);
        //root.addChild(bgNode);


        staticGroup.getTransform(t);
        t.setScale(1.3*SCALE);
        staticGroup.setTransform(t);


        root.addChild(staticGroup);
        root.addChild(objScale);

        root.compile();
        return root;
    }

    private double[] targetEulerAngle = new double[3];

    private void targetPositioning(){

        Quat4d[] q = new Quat4d[3];

        for ( byte i = 0; i < 3; i++ ){
            targetEulerAngle[i] = 2*Math.PI*Math.random();
            q[i] = new Quat4d();
        }

        q[0].set(new AxisAngle4d(axis[2],targetEulerAngle[0]));
        q[1].set(new AxisAngle4d(axis[1],targetEulerAngle[1]));
        q[2].set(new AxisAngle4d(axis[2],targetEulerAngle[2]));

        q[0].mul(q[1]);
        q[0].mul(q[2]);

        t.setIdentity();
        t.setScale(1.3*SCALE);
        t.setRotation(q[0]);

        targetGroup.setTransform(t);


        // -------- Rotaciona os eixos coordenados do sistema de referÃªncia alvo -------

        aux[0].set(1,0,0,0); // Versor i original (na forma de quaternion).
        aux[1].set(0,1,0,0); // Versor j original (na forma de quaternion).
        aux[2].set(0,0,1,0); // Versor k original (na forma de quaternion).

        for ( int i = 0; i < 3; i++ ){

            Quat4d z = new Quat4d(q[0]);
            aux[i].mulInverse(z);
            z.mul(aux[i]);

            targetAxis[i].setX( z.getX() );
            targetAxis[i].setY( z.getY() );
            targetAxis[i].setZ( z.getZ() );
        }

        /*
         * Linha nodal.
         */
        Vector3d u = new Vector3d();
        u.cross(z, targetAxis[2]);
        if ( u.length() > eps ){
            azimute = x.angle(u);
            if ( x.dot(targetAxis[2]) < 0 ) azimute = Math.PI - azimute;
            Transform3D t = new Transform3D();
            t.rotZ(azimute);
            lineOfNodesGroup.setTransform(t);
            if ( !hasNodeLine ){
                scene.addChild(nodeLineBGroup);
                hasNodeLine = true;
            }
        }
        else if ( hasNodeLine ){
            scene.removeChild(nodeLineBGroup);
            hasNodeLine = false;
        }
    }

    private final TransformGroup createRotatingGroup(){

        // Os eixos coordenados.
        RectangularAxes3D axes = new RectangularAxes3D(1.2f, new Color3f[]{RED,GREEN,BLUE});
        axes.setLineAttributes(new LineAttributes(2, LineAttributes.PATTERN_SOLID, true));

        // O plano xy.
        Appearance appearance = new Appearance();
        appearance.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.BLENDED,0.3f));
        appearance.setColoringAttributes(new ColoringAttributes(new Color3f(0f,0.5f,0.5f),ColoringAttributes.SHADE_GOURAUD));
        appearance.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_FILL,PolygonAttributes.CULL_NONE,0));
        PolygonalPlane xyPlane = new PolygonalPlane(1,30,true);
        xyPlane.setAppearance(appearance);


        PolygonalPlane arc = new PolygonalPlane(1,30);

        // O plano xy.
        Appearance appearance2 = new Appearance();
        appearance2.setColoringAttributes(new ColoringAttributes(new Color3f(0f,0f,0f),ColoringAttributes.SHADE_GOURAUD));
        appearance2.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_LINE,PolygonAttributes.CULL_NONE,0));
        arc.setAppearance(appearance2);

        TransformGroup group = new TransformGroup();
        group.addChild(axes);
        //group.addChild(xyPlane);
        //group.addChild(arc);

        return group;
    }

    private final TransformGroup createStaticGroup(){

        // Os eixos coordenados.
        RectangularAxes3D axes = new RectangularAxes3D(1.1f, new Color3f[]{RED,GREEN,BLUE});
        axes.setLineAttributes(new LineAttributes(0.5f, LineAttributes.PATTERN_SOLID ,true));

        // O plano xy.
        Appearance appearance = new Appearance();
        appearance.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.BLENDED,0.7f));
        appearance.setColoringAttributes(new ColoringAttributes(new Color3f(1f,0f,0f),ColoringAttributes.SHADE_GOURAUD));
        appearance.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_FILL,PolygonAttributes.CULL_NONE,0));
        PolygonalPlane xyPlane = new PolygonalPlane(1,30,true);
        xyPlane.setAppearance(appearance);

        PolygonalPlane arc = new PolygonalPlane(1,30);

        TransformGroup group = new TransformGroup();
        //group.addChild(axes);
        group.addChild(xyPlane);
        group.addChild(arc);

        return group;
    }

    private final TransformGroup createTargetGroup(){

        // Os eixos coordenados.
        RectangularAxes3D axes = new RectangularAxes3D(1.2f, new Color3f[]{RED,GREEN,BLUE});
        axes.setLineAttributes(new LineAttributes(0.5f, LineAttributes.PATTERN_DASH, true));

        // O plano xy.
        Appearance appearance = new Appearance();
        appearance.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.BLENDED,0.7f));
        appearance.setColoringAttributes(new ColoringAttributes(new Color3f(.5f,.5f,0f),ColoringAttributes.SHADE_GOURAUD));
        appearance.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_FILL,PolygonAttributes.CULL_NONE,0));
        PolygonalPlane xyPlane = new PolygonalPlane(1,30,true);
        xyPlane.setAppearance(appearance);

        Appearance appearance2 = new Appearance();
        appearance2.setColoringAttributes(new ColoringAttributes(new Color3f(0f,0f,0f),ColoringAttributes.SHADE_GOURAUD));
        appearance2.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_LINE,PolygonAttributes.CULL_NONE,0));

        PolygonalPlane arc = new PolygonalPlane(1,30);
        arc.setAppearance(appearance2);

        TransformGroup group = new TransformGroup();
        group.addChild(axes);
        group.addChild(xyPlane);
        group.addChild(arc);

        return group;
    }

    public void traverse(Enumeration enumeration){
        while ( enumeration.hasMoreElements() ){
            Object object = enumeration.nextElement();
            if ( object instanceof Shape3D ){
                //System.out.println(">> Shape3D");
                Shape3D shape = (Shape3D) object;
                shape.getAppearance().setTransparencyAttributes(tAttributes);
            }
            else if ( object instanceof Group ){
                Group group = (Group) object;
                traverse(group.getAllChildren());
                //System.out.println("Um grupo");
            }
            else{
                //System.out.println("Outra coisa");
            }
        }
    }


    private void resetAngleScale(){
        for ( int i = 0; i < 3; i++ ){
            sliderLabels[i] = new Hashtable<Integer,JLabel>(angleScale);
            slider[i].setLabelTable(sliderLabels[i]);
        }
    }

    private void eulerRotation( TransformGroup tGroup, double[] angles ){

        Quat4d[] quaternion = new Quat4d[3];
        for ( byte i = 0; i < 3; i++ ) quaternion[i] = new Quat4d();

        quaternion[0].set(new AxisAngle4d(axis[2],angles[0]));
        quaternion[1].set(new AxisAngle4d(axis[0],angles[1]));
        quaternion[2].set(new AxisAngle4d(axis[2],angles[2]));

        quaternion[0].mul(quaternion[1]);
        quaternion[0].mul(quaternion[2]);

        tGroup.getTransform(t);
        t.setRotation(quaternion[0]);
        tGroup.setTransform(t);
    }

    private class SliderListener implements ChangeListener{

        public void stateChanged(ChangeEvent e){
            JSlider source = (JSlider)e.getSource();

            if ( history.isEmpty() ){
                for ( int i = 0; i < 3; i++ ) lastCursorPos[i] = new InstantHistory(i,0);
            }

            int eulerAngleIndex = 0, // Ã�ndice do Ã‚NGULO de Euler: 0 para alfa, 1 para beta e 2 para gama.
                eulerAxisIndex  = 0; // Ã�ndice do EIXO utilizado na rotaÃ§Ã£o: 0 para x (beta), 1 para y (nÃ£o utilizado) e 2 para z (alfa e gamma).

            if ( source == slider[ALPHA] ){
                eulerAngleIndex = ALPHA;
                eulerAxisIndex  = 2;
            }
            else if ( source == slider[BETA] ){
                eulerAngleIndex = BETA;
                eulerAxisIndex  = 0;
            }
            else if ( source == slider[GAMMA] ){
                eulerAngleIndex = GAMMA;
                eulerAxisIndex  = 2;
            }

            int sliderPos = slider[eulerAngleIndex].getValue();

            /*
             * Tratamento dos nÃ­veis de undo.
             */
            if ( !animating ){

                InstantHistory lastRecordedInstant = null;
                if ( !history.isEmpty() ){
                    lastRecordedInstant = history.lastElement();

                    if ( eulerAngleIndex == lastRecordedInstant.getAxis() && Math.abs(lastRecordedInstant.getAngle()-sliderPos) < 5 ){
                        sliderPos = lastRecordedInstant.getAngle();
                        slider[eulerAngleIndex].setValue(sliderPos);
                        //slider[eulerAngleIndex].repaint();
                    }
                }

                if ( !slider[eulerAngleIndex].getValueIsAdjusting() ){

                    InstantHistory instant = new InstantHistory(eulerAngleIndex,sliderPos);

                    if ( history.isEmpty() ){
                        lastCursorPos[eulerAngleIndex] = new InstantHistory(eulerAngleIndex,0);
                        history.add(lastCursorPos[eulerAngleIndex]);

                        if ( !instant.equals(lastCursorPos[eulerAngleIndex]) ){
                            sliderLabels[eulerAngleIndex].put(lastCursorPos[eulerAngleIndex].getAngle(),undoLabel);
                            slider[eulerAngleIndex].setLabelTable(sliderLabels[eulerAngleIndex]);
                        }

                        lastCursorPos[eulerAngleIndex] = new InstantHistory(instant);
                    }
                    else{
                        if ( !lastRecordedInstant.equals(instant) ){

                            history.add(lastCursorPos[eulerAngleIndex]);

                            sliderLabels[lastRecordedInstant.getAxis()] = new Hashtable<Integer,JLabel>(angleScale);
                            slider[lastRecordedInstant.getAxis()].setLabelTable(sliderLabels[lastRecordedInstant.getAxis()]);
                            //slider[lastRecordedInstant.getAxis()].repaint();

                            sliderLabels[lastCursorPos[eulerAngleIndex].getAxis()].put(lastCursorPos[eulerAngleIndex].getAngle(),undoLabel);
                            slider[lastCursorPos[eulerAngleIndex].getAxis()].setLabelTable(sliderLabels[lastCursorPos[eulerAngleIndex].getAxis()]);
                        }
                        else{
                            sliderLabels[lastRecordedInstant.getAxis()] = new Hashtable<Integer,JLabel>(angleScale);
                            slider[lastRecordedInstant.getAxis()].setLabelTable(sliderLabels[lastRecordedInstant.getAxis()]);
                            //slider[lastRecordedInstant.getAxis()].repaint();

                            history.remove(history.size()-1);

                            if ( !history.isEmpty() ){
                                lastRecordedInstant = history.lastElement();

                                sliderLabels[lastRecordedInstant.getAxis()].put(lastRecordedInstant.getAngle(),undoLabel);
                                slider[lastRecordedInstant.getAxis()].setLabelTable(sliderLabels[lastRecordedInstant.getAxis()]);
                                //slider[lastRecordedInstant.getAxis()].repaint();
                            }
                        }

                        lastCursorPos[eulerAngleIndex] = new InstantHistory(instant);
                    }
                }
            }

            /*
            if ( slider[BETA].getValue() != 0 ){
                Transform3D t = new Transform3D();
                t.rotZ(Math.toRadians(slider[BETA].getValue()));
                nodeGroup.setTransform(t);
                BranchGroup g = new BranchGroup();
                g.addChild(nodeGroup);
                staticFrame.addChild(g);
            }
            else{
                //staticFrame.removeChild(nodeGroup);
            }*/




            /*
             * Efetua a rotaÃ§Ã£o propriamente dita.
             */
            double thisAngle = Math.toRadians(sliderPos);
            //System.out.println( "Cursor do eixo " + eulerAngleIndex + " na posicao " + sliderPos );
            Quat4d thisRotation = new Quat4d();
            thisRotation.set(new AxisAngle4d( axis[eulerAxisIndex], thisAngle-eulerAngle[eulerAngleIndex] ));
            thisRotation.mul(allRotations);

            double dAngle = thisAngle - eulerAngle[eulerAngleIndex]; // variaÃ§Ã£o no Ã¢ngulo.
            eulerAngle[eulerAngleIndex] = thisAngle;

            //transformation.set(allRotations,new Vector3d(),SCALE);
            transformation.set(thisRotation,new Vector3d(),SCALE);

            // Aplica a transformaÃ§Ã£o (rotaÃ§Ã£o) ao grupo de objetos "rotatingFrame".
            rotatingFrame.setTransform(transformation);

            // Atribui ao quaternion "allRotations" a seqÃ¼Ãªncia de todas as rotaÃ§Ãµes, armazenada pelo quaternion "thisRotations".
            allRotations.set(thisRotation);


            /*
             * Linha nodal.
             */
//            Vector3d u = new Vector3d();
//            u.cross(z, targetAxis[2]);
//            if ( u.length() > eps ){
//                azimute = x.angle(u);
//                if ( x.dot(targetAxis[2]) < 0 ) azimute = Math.PI - azimute;
//                Transform3D t = new Transform3D();
//                t.rotZ(azimute);
//                lineOfNodesGroup.setTransform(t);
//                if ( !hasNodeLine ){
//                    scene.addChild(nodeLineBGroup);
//                    hasNodeLine = true;
//                }
//            }
//            else if ( hasNodeLine ){
//                scene.removeChild(nodeLineBGroup);
//                hasNodeLine = false;
//            }



            rotateAxis(); // Rotaciona os eixos do sistema de coordenadas.

            // Atualiza a exibiÃ§Ã£o dos botÃµes deslizantes.
            for ( int i = 0; i < 3; i++ ) slider[i].repaint();
            //canvas3D.repaint();
        }
    }

    private ImageIcon getIcon( String filename ){

        URL url = null;

        ClassLoader classLoader = this.getClass().getClassLoader();
        url = classLoader.getResource(filename);//ClassLoader.getSystemResource(filename);

        if ( url == null ) return null;
        else return new ImageIcon(url);
    }

    /*
     * Rotaciona os trÃªs versores i, j e k originais.
     * EstratÃ©gia: como o quaternion "allRotations" representa TODAS as rotaÃ§Ãµes
     * efetuadas atÃ© o momento, rotacionamos os versores originais.
     */



    private void rotateAxis(){

        aux[0].set(1,0,0,0); // Versor i original (na forma de quaternion).
        aux[1].set(0,1,0,0); // Versor j original (na forma de quaternion).
        aux[2].set(0,0,1,0); // Versor k original (na forma de quaternion).

        for ( int i = 0; i < 3; i++ ){

            Quat4d z = (Quat4d)allRotations.clone();
            aux[i].mulInverse(z);
            z.mul(aux[i]);

            axis[i].setX( z.getX() );
            axis[i].setY( z.getY() );
            axis[i].setZ( z.getZ() );
        }
    }

    private void reset(){
        if ( !history.isEmpty() ){
            animating = true;
            zipHistory();
            resetAngleScale();

            int A, dA = (int) ( angularSpeed / frameRate ), targetA;
            boolean increasingA;

            InstantHistory target;
            for ( int i = history.size()-1; i >= 0; i-- ){

                target = history.elementAt(i);

                A = slider[target.getAxis()].getValue();
                targetA = target.getAngle();
                increasingA = ( targetA - A > 0 );

                //System.out.println( "history[" + i + "] --> Cursor do eixo " + target.getAxis() + "na posiÃ§Ã£o " + A + "Âº indo para " + target.getAngle());
                //System.out.println( "Rodando atÃ© " + target.getAngle() + " sobre o eixo " + target.getAxis() );

                do{
                    A = ( increasingA ? A+dA : A-dA );
                    A = ( increasingA ? Math.min(A,targetA) : Math.max(A,targetA) );

                    slider[target.getAxis()].setValue(A);

                    sleep((int)(1000/frameRate));

                } while ( A != targetA );
            }

            history.removeAllElements();
            animating = false;
        }
    }

    /*
     * Aguarda <source>time</source> milisegundos (enquanto isso, retorna o controle
     * ao processador. Uma chamada <source>sleep(0)</source> deve ser feita em todos
     * os laÃ§os possivelmente demorados).
     */
    private void sleep( int time ){
        try{
            Thread.sleep(time);
        }
        catch( InterruptedException e ) {/* Nada */}
    }

    /*
     * RotaÃ§Ãµes ao redor de um MESMO EIXO sÃ£o comutativas. Isto posto, quando o usuÃ¡rio
     * faz duas ou mais rotaÃ§Ãµes sucessivas sobre um mesmo eixo, esta seqÃ¼encia pode ser
     * substituÃ­da (na pilha de undo) por uma ÃšNICA rotaÃ§Ã£o. Isto permite que o objeto
     * seja retornado Ã  posiÃ§Ã£o inicial (via animaÃ§Ã£o) sem efetuar rotaÃ§Ãµes desnecessÃ¡rias.
     * E isto que este mÃ©todo faz.
     */
    private void zipHistory(){

        //System.out.println( "--------- HistÃ³rico antes de compactar -----------" );
        for( InstantHistory i : history ){
            //System.out.println(i);
        }

        Vector<InstantHistory> zipped = new Vector<InstantHistory>();

        InstantHistory previous = history.firstElement();
        int minimo = previous.getAngle();

        for ( int i = 0; i < history.size(); i++ ){
            InstantHistory instant = history.elementAt(i);

            if ( instant.getAxis() == previous.getAxis() ){
                minimo = Math.min( minimo, instant.getAngle() );
                if ( i == history.size()-1 ) zipped.add(new InstantHistory(instant.getAxis(),minimo));
            }
            else{
                zipped.add(new InstantHistory(previous.getAxis(),minimo));
                if ( i == history.size()-1 ) zipped.add(instant);
                minimo = instant.getAngle();
            }

            previous = new InstantHistory(instant);
        }

        history = new Vector<InstantHistory>(zipped);

        //System.out.println( "--------- HistÃ³rico COMPACTADO -----------" );
        for( InstantHistory i : history ){
            //System.out.println(i);
        }

    }




    private class InstantHistory{

        private static final int ALPHA = 0, BETA = 1, GAMMA = 2;

        private int angle;
        private int axis;

        public InstantHistory( InstantHistory instantHistory ){
            this(instantHistory.getAxis(),instantHistory.getAngle());
        }
        public InstantHistory( int axis, int angle ) throws IllegalArgumentException{
            if ( axis == ALPHA || axis == BETA || axis == GAMMA ) this.axis = axis;
            else throw new IllegalArgumentException( "Axis index must be 0, 1 or 2." );

            this.angle = angle % 360;
        }

        public int getAxis(){
            return axis;
        }

        public int getAngle(){
            return angle;
        }

        public boolean equals( InstantHistory arg ){
            return( getAxis() == arg.getAxis() && getAngle() == arg.getAngle() );
        }

        @Override
        public String toString(){

            String axisName = null;
            switch(axis){
                case ALPHA:
                    axisName = new String("ALPHA");
                    break;
                case BETA:
                    axisName = new String("BETA");
                    break;
                case GAMMA:
                    axisName = new String("GAMMA");
                    break;
            }

            return new String( "[InstantHistory: " + axisName + ", " + angle + "Âº]" );
        }

    }


    private static final Color3f GREY = new Color3f( .3f, .3f, .3f );
    private static final Color3f RED = new Color3f( 1f, 0f, 0f );
    private static final Color3f GREEN = new Color3f( 0f, 1f, 0f );
    private static final Color3f BLUE = new Color3f( 0f, 0f, 1f );
}







