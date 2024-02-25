package cepa.util;

import java.util.Vector;

/**
 *
 * @author Ivan Ramos Pagnossin
 */
public class FieldList extends Vector<VectorField2D>{

	private static final long serialVersionUID = 1L;

	public FieldList(){
        int N = 7;
        
        VectorField2D field[] = new VectorField2D[N];
        
        // Campos conservativos.
        field[0] = new VectorField2D( "1", "1" );
        field[0].setConservative( "x+y" );
        
        field[1] = new VectorField2D( "x/SQRT(x^2+y^2)", "y/SQRT(x^2+y^2)" );
        field[1].setConservative( "SQRT(x^2+y^2)" );
        
        field[2] = new VectorField2D( "2*x*y^2", "2*x^2*y" );
        field[2].setConservative( "x^2*y^2" );
        
        field[3] = new VectorField2D( "y*SIN(x*y)", "x*SIN(x*y)" );
        field[3].setConservative( "COS(x*y)" );
        
        // Campos não-conservativos.
        field[4] = new VectorField2D( "x*y", "0" );
        field[5] = new VectorField2D( "y^2", "x^2" );
        field[6] = new VectorField2D( "SIN(y)", "SIN(x)" );         
        
        for ( int i = 0; i < N; i++ ) this.add(field[i]);
    }
}
