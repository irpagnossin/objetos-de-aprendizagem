package cepa.edu.math.geometry;

import javax.media.j3d.Geometry;
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
        
        if ( R <= 0 ) throw new IllegalArgumentException( "Radius must be greather than zero." );
        if ( nPtos <= 2 ) throw new IllegalArgumentException( "Number of points must be greather than two." );
        
        setGeometry(createGeometry(R,nPtos));
    }
    
    /**
     * Constrói a geometria propriamente dita.
     */
    private Geometry createGeometry( double R, int nPtos ){
        
        double A = 0, dA = 2 * Math.PI / nPtos;
        
        TriangleFanArray array = new TriangleFanArray(nPtos+2, TriangleFanArray.COORDINATES, new int[]{nPtos+2});
        
        array.setCoordinate(0, new Point3d(0,0,0));
        for ( byte i = 1; i <= nPtos; i++ ){            
            array.setCoordinate(i, new Point3d(R*Math.cos(A),R*Math.sin(A),0));
            A += dA;
        }
        array.setCoordinate(nPtos+1,new Point3d(R,0,0));
        
        return array;
    }
}
