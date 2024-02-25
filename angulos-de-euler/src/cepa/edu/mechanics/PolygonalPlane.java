package cepa.edu.mechanics;

import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TriangleFanArray;
import javax.vecmath.Point3d;

/**
 * Representa um plano centralizado em (0,0,0) com normal (0,0,1). Formato a partir de um <source>TriangleFanArray</source>, pode apresentar um
 * formato triangular (<source>nPtos = 3</source>), quadrangular (<source>nPtos = 4</source>), etc, dependendo de <source>nPtos</source>.
 * @author ivan.pagnossin
 */
public class PolygonalPlane extends Shape3D{
    
    /**
     * Constrói um polígono com <source>nPtos</source> sobre uma circunferência de raio <source>R</source>.
     */
	public PolygonalPlane( double R, int nPtos ){
		this(R,nPtos,false);
	}
    public PolygonalPlane( double R, int nPtos, boolean includeCenter ){
        
        if ( R <= 0 ) throw new IllegalArgumentException( "Radius must be greather than zero." );
        
        if ( includeCenter && nPtos < 2 ) throw new IllegalArgumentException( "nPtos has to be > 1." );
        else if ( !includeCenter && nPtos < 3 ) throw new IllegalArgumentException( "nPtos has to be > 2." );
        
        setGeometry(createGeometry(R,nPtos,includeCenter));
    }
    
    /**
     * Constrói a geometria propriamente dita.
     */
    private Geometry createGeometry( double R, int nPtos, boolean includeCenter ){
        
        double A = 0, dA = 2 * Math.PI / (nPtos-1);
        
        GeometryArray array = null;
        Point3d pt = new Point3d(0,0,0);
        
        if ( includeCenter ){
       	
	        array = (TriangleFanArray) new TriangleFanArray(nPtos+2, TriangleFanArray.COORDINATES, new int[]{nPtos+2});
	        
	        pt.set(0,0,0);
	        array.setCoordinate(0,pt);
	        array.setCoordinate(nPtos+1,pt);
	        
	        for ( byte i = 1; i <= nPtos; i++ ){
	        	System.out.println("i = " + i);
	        	pt.set(R*Math.cos(A),R*Math.sin(A),0);
	            array.setCoordinate(i,pt);
	            A += dA;
	        }
	        
        }
        else{        
        	
	        array = new LineArray(2*nPtos,LineArray.COORDINATES);
	        	        
	        pt.set(R*Math.cos(A),R*Math.sin(A),0);	        
	        array.setCoordinate(0,pt);
	        array.setCoordinate(2*nPtos-1,pt);
	        
	        for ( byte i = 1; i < 2*nPtos-1; i+=2 ){	        
	        	pt.set(R*Math.cos(A), R*Math.sin(A), 0);
	            array.setCoordinate(i,pt);
	            array.setCoordinate(i+1,pt);
	            A += dA;
	        }	        
        }
        
        return array;
    }
}
