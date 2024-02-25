package util;

import java.util.Vector;

public class Arrow {
	
    private Vector<float[]> points = new Vector<float[]>();
    private float a = 0f, b = 1f/3, c = 1f/10, d = 1f/4, L, lastL;
        
    public Arrow( float L, float offset ){
        a = offset;
        this.setLength(L);        
        this.L = lastL = L;
    }
	
    public void setLength( float L ){
        setLength(L,false);
    }
    public void setLength( float L, boolean hold ){        
        this.L = L;
        
        points.clear();
        
        float l = L;
        if ( hold ) l = lastL;
        
        points.add(new float[]{            -a*L,      0 });
        points.add(new float[]{ (1-a)*L+(c-b)*l,      0 }); 
        points.add(new float[]{     (1-a)*L-b*l, +d*l/2 });
        points.add(new float[]{         (1-a)*L,      0 });
        points.add(new float[]{     (1-a)*L-b*l, -d*l/2 });
    }
    
    public void holdHeadSize(){
        lastL = L;
    }
    
    public Vector<float[]> getArrow( float angle ){
        
        float[][] R = new float[2][2]; // Operador de rotação.
        R[0][0] =  (float)Math.cos(angle);
        R[0][1] = -(float)Math.sin(angle);
        R[1][0] =  (float)Math.sin(angle);
        R[1][1] =  (float)Math.cos(angle);		

        Vector<float[]> rotated = new Vector<float[]>();
        for ( int i = 0; i < points.size(); i++ ){
            rotated.add( new float[]{
                R[0][0] * points.elementAt(i)[0] + R[0][1] * points.elementAt(i)[1],
                R[1][0] * points.elementAt(i)[0] + R[1][1] * points.elementAt(i)[1]
            });
        }
        
        return rotated;
    }
    
}
