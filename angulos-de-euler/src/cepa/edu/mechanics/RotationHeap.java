package cepa.edu.mechanics;

import java.util.Vector;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

public class RotationHeap {

	private Vector<Rotation> heap = null;
	
	/**
	 * Constrói uma pilha (vazia) de rotações.
	 */
	public RotationHeap () {
		heap = new Vector<Rotation>();
		
		allRotations = new Quat4d();
		thisRotation = new Quat4d();
	}
	
	/**
	 * Contrói uma pilha de rotações a partir de outra, <source>rHeap</source>.
	 */
	public RotationHeap ( RotationHeap rHeap ){
		this();
		for ( int i = 0; i < rHeap.size(); i++ ) heap.add(rHeap.getRotation(i)); 
	}
	
	/**
	 * @return o tamanho da pilha de rotações.
	 */
	public int size(){ return heap.size(); }
	
	/**
	 * Retorna a rotação (elemento) de índice <source>n</source>.
	 */
	public Rotation getRotation ( final int n ){ return heap.elementAt(n); }

	public void addRotation ( Rotation r ){
		heap.add(r);
		thisRotation.set(new AxisAngle4d(axis[r.getAxisIndex()],r.getAngle()));
		allRotations.mul(thisRotation);
		
		rotateAxis();
	}
	
    private void rotateAxis(){
        
    	for ( byte i = 0; i < 3; i++ ){
    		aux[i].set(axis[i].getX(), axis[i].getY(), axis[i].getZ(), 0);
    	}
                
        for ( int i = 0; i < 3; i++ ){
            
            Quat4d z = (Quat4d)allRotations.clone();
            aux[i].mulInverse(z);
            z.mul(aux[i]);
            
            axis[i].setX( z.getX() );
            axis[i].setY( z.getY() );
            axis[i].setZ( z.getZ() );            
        }
    }	
	
	private Quat4d allRotations = null;
	private Quat4d thisRotation = null;
	private Quat4d[] aux = null;
	
	private Vector3d[] axis = {
		new Vector3d(1,0,0),
		new Vector3d(0,1,0),
		new Vector3d(0,0,1),
	};
	
	
}
