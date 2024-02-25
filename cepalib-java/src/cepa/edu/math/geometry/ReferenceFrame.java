package cepa.edu.math.geometry;

import com.sun.j3d.utils.geometry.Cone;
import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

public class ReferenceFrame extends TransformGroup {
        
    private LineAttributes lineAttributes = null;
    private TransparencyAttributes transparencyAttributes = null;
    
    public void setLineAttributes( LineAttributes lineAttributes ){
        this.lineAttributes = lineAttributes;
    }
    
    public void setTransparencyAttributes( TransparencyAttributes transparencyAttributes ){
        this.transparencyAttributes = transparencyAttributes;
    }
    
    public ReferenceFrame( float L, Color3f[] color ){
            
        float arrowHeadRadius = L / 20;
        float arrowHeadHeight = L / 10;
        
        TransformGroup[] transformGroup = new TransformGroup[3];
        Appearance[] appearance = new Appearance[3];
        Cone[] cone = new Cone[3];
        
        Quat4d quaternion = new Quat4d();
        
        Transform3D transform;
        AxisAngle4d rotation;
        
        float[] rotationAngle = new float[]{ -(float)Math.PI/2, 0, +(float)Math.PI/2 };        
        Vector3d[] rotationAxis = new Vector3d[]{ new Vector3d(0,0,1), new Vector3d(0,1,0), new Vector3d(1,0,0) };        
        Vector3d[] translation = new Vector3d[]{ new Vector3d(L,0,0), new Vector3d(0,L,0), new Vector3d(0,0,L) };        
        //Color3f[] color = new Color3f[]{ new Color3f(1f,0f,0f), new Color3f(0f,1f,0f), new Color3f(0f,0f,1f) };        
        Point3d[] endingArrowBodyPt = new Point3d[]{ new Point3d(L,0,0), new Point3d(0,L,0), new Point3d(0,0,L) };
        
        float scale = 1f;
        
        //----------------------------------------------------------------------
        // Constrói os eixos x, y e z (vermelho, verde e azul, respectivamente).
        //----------------------------------------------------------------------
        for ( int i = 0; i < 3; i++ ){
            appearance[i] = new Appearance();
            if ( lineAttributes != null ) appearance[i].setLineAttributes(lineAttributes);
            if ( transparencyAttributes != null ) appearance[i].setTransparencyAttributes(transparencyAttributes);
            appearance[i].setColoringAttributes(new ColoringAttributes(color[i],ColoringAttributes.SHADE_GOURAUD));
            
            
            cone[i] = new Cone(arrowHeadRadius,arrowHeadHeight);
            cone[i].setAppearance(appearance[i]);
            
            rotation = new AxisAngle4d(rotationAxis[i],rotationAngle[i]);
            quaternion.set(rotation);
            transform = new Transform3D(quaternion,translation[i],scale);
            transformGroup[i] = new TransformGroup(transform);

            transformGroup[i].addChild(cone[i]);
            this.addChild(transformGroup[i]);
            
            
            
            LineArray line = new LineArray( 2, GeometryArray.COORDINATES );
            line.setCoordinate(0, new Point3d(0,0,0));
            line.setCoordinate(1, endingArrowBodyPt[i]);

            Shape3D arrowbody = new Shape3D(line);
            arrowbody.setAppearance(appearance[i]);
            
            this.addChild(arrowbody);
        }
    }
}

