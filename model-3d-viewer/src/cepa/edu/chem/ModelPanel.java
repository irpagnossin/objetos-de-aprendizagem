package cepa.edu.chem;

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
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.PointLight;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

/**
 *
 * @author irpagnossin
 */
public class ModelPanel extends JPanel{

    public ModelPanel () {
                
        rootBranchGroup = new BranchGroup();
        
        rootBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        rootBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        rootBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
        
        modelBranchGroup = new BranchGroup();
        modelBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
        modelBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

        modelTransform = new Transform3D();
        modelTransformGroup = new TransformGroup(modelTransform);
        modelTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        
        fileBranchGroup = new BranchGroup();
        fileBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
        
        
        // Cria o Canvas3D com as configurações-padrão do SimpleUniverse.
        canvas3D = new Canvas3D(SimpleUniverse.getPreferredConfiguration());

        // Cria o SimpleUniverse no Canvas3D e adiciona a ele os objetos da cena, exceto o modelo.
        SimpleUniverse universe = new SimpleUniverse(canvas3D);
        createSceneGraph();
        universe.addBranchGraph(rootBranchGroup);

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
        //tGroup.setTransform(t);

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

    public void loadModel ( final URL url ) {
    	          
        /*
         * fileBranchGroup -> modelTransformGroup -> modelBranchGroup -> rootBranchGroup
         * 
         * Para substituir um modelo tridimensional por outro � preciso remover os tr�s
         * grupos abaixo do rootBranchGroup, redefinir o fileBranchGroup com base no novo
         * arquivo e reinserir os grupos como antes. TODO: H� uma forma mais simples?
         */        
        rootBranchGroup.removeChild(modelBranchGroup);
        modelBranchGroup.removeChild(modelTransformGroup);
        modelTransformGroup.removeChild(fileBranchGroup);          
          
        // TODO: desenvolver o carregamento com as duas bibliotecas: 3ds e xml.
		String path = url.toString();
		int i = path.lastIndexOf('.');
		if (i > 0 &&  i < path.length() - 1) {
			String ext = path.substring(++i).toLowerCase();
			if (ext.equals("xml")) useXML = true;
			else useXML = false;
		}        

        
        try {
        	// Carrega o modelo no formato XML (Blender 3D).
        	if (useXML) {
        		XMLDecoder decoder = new XMLDecoder(
					new BufferedInputStream(
							new FileInputStream(url.toString())));
        		
        		fileBranchGroup.removeAllChildren();
        		fileBranchGroup.addChild((Shape3D) decoder.readObject());
        	}
        	// Carrega o modelo no formato 3DS (3D Studio).
        	else {
	        	Loader3DS loader = new Loader3DS();
	            loader.setFlags(Loader3DS.LOAD_ALL);
	            
	            Scene modelScene = loader.load(url);
	            fileBranchGroup = modelScene.getSceneGroup();
        	}
        }
        catch (FileNotFoundException e) {
            System.err.println(e); // TODO: não parar o aplicativo.
        }
        catch (ParsingErrorException e) {
            System.err.println(e); // TODO: não parar o aplicativo.
        }
        catch (IncorrectFormatException e) {
            System.err.println(e); // TODO: não parar o aplicativo.
        }               
        
        
        modelTransformGroup.addChild(fileBranchGroup);        
        modelBranchGroup.addChild(modelTransformGroup);
        rootBranchGroup.addChild(modelBranchGroup);        
    }

    private void createSceneGraph(){
        
        // Define o fundo da cena (background).
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
        //Background bg = new Background(new Color3f(0.05f, 0.05f, 0.2f));
        Background bg = new Background(new Color3f(0,0,0));
        bg.setApplicationBounds(bounds);
        rootBranchGroup.addChild(bg);

        // Luzes
        AmbientLight aLgt = new AmbientLight(new Color3f(0.3f, 0.3f, 0.3f));
        aLgt.setInfluencingBounds(bounds);
        rootBranchGroup.addChild(aLgt);

        Point3f[] lightPosition = new Point3f[]{
            new Point3f(-1f,-1f,2f),
            new Point3f(.5f,0,0)
        };

        Color3f[] lightColor = new Color3f[]{
        	new Color3f(1f, 1f, 1f),//new Color3f(1.0f, 0.0f, 0.0f),
        	new Color3f(1f, 1f, 1f)//new Color3f(0.0f, 1.0f, 0.0f)
        };

        Point3f attenuation = new Point3f(0.8f,0f,0f);

        PointLight[] light = new PointLight[2];
        for ( int i = 0; i < 2; i++ ){
            light[i] = new PointLight(lightColor[i],lightPosition[i],attenuation);
            light[i].setInfluencingBounds(bounds);
            rootBranchGroup.addChild(light[i]);
        }

        Color3f[] axisColor = {
            new Color3f(1f,0f,0f),
            new Color3f(0f,1f,0f),
            new Color3f(0f,0f,1f)
        };

        rootBranchGroup.addChild(new ReferenceFrame(1f,axisColor));

        rootBranchGroup.compile();
    }

    public void rescale( final double scale ){
        modelTransform.setScale(scale);
        modelTransformGroup.setTransform(modelTransform);
    }

    private Canvas3D canvas3D;
    private Transform3D modelTransform;
    private TransformGroup modelTransformGroup;
    private BranchGroup rootBranchGroup, modelBranchGroup, fileBranchGroup;
    private boolean useXML = false;
}
