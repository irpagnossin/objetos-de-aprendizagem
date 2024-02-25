package cepa.util;

import java.util.Vector;

public class Arrow {
	
    private Vector<double[]> points = new Vector<double[]>();
    private double a = 0f, b = 1f/3, c = 1f/10, d = 1f/4, L, lastL;
        
    public Arrow( double L, double offset ){
        a = offset;
        this.setLength(L);        
        this.L = lastL = L;
    }
	
    public void setLength( double L ){
        setLength(L,false);
    }
    public void setLength( double L, boolean hold ){        
        this.L = L;
        
        points.clear();
        
        double l = L;
        if ( hold ) l = lastL;
        
        points.add(new double[]{            -a*L,      0 });
        points.add(new double[]{ (1-a)*L+(c-b)*l,      0 }); 
        points.add(new double[]{     (1-a)*L-b*l, +d*l/2 });
        points.add(new double[]{         (1-a)*L,      0 });
        points.add(new double[]{     (1-a)*L-b*l, -d*l/2 });
    }
    
    public void holdHeadSize(){
        lastL = L;
    }
    
    public Vector<double[]> getArrow( double angle ){
        
        double[][] R = new double[2][2]; // Operador de rotação.
        R[0][0] =  (double)Math.cos(angle);
        R[0][1] = -(double)Math.sin(angle);
        R[1][0] =  (double)Math.sin(angle);
        R[1][1] =  (double)Math.cos(angle);		

        Vector<double[]> rotated = new Vector<double[]>();
        for ( int i = 0; i < points.size(); i++ ){
            rotated.add( new double[]{
                R[0][0] * points.elementAt(i)[0] + R[0][1] * points.elementAt(i)[1],
                R[1][0] * points.elementAt(i)[0] + R[1][1] * points.elementAt(i)[1]
            });
        }
        
        return rotated;
    }
    
}
