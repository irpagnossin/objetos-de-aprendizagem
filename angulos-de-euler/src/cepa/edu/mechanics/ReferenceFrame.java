package cepa.edu.mechanics;

import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

/**
 *
 * @author ivan.pagnossin
 */
public class ReferenceFrame{

    /**
     * Constrói um sistema de referência cartesiano (ortonormal).
     */
    public ReferenceFrame(){
        z = new Quat4d();
        reset();
    }

    /**
     * Aplica a rotação <source>quaternion</source> sobre o sistema de referências. Isto é, rotaciona todos os três eixos cartesianos.
     * Chamadas sucessivas a este método são cumulativas.
     */
    public void rotate( final Quat4d quaternion ){
        for ( int i = 0; i < 3; i++ ){
            z.normalize(quaternion); // z = quaternion
            axes[i].mulInverse(z);   // x = x * (1/z) [idem para y e z]
            z.mul(axes[i]);          // z = z * x * (1/z)            
            axes[i].set(z);          // x = z
        }            
    }

    /**
     * Retorna o versor x, decomposto no sistema de coordenadas inicial.
     */
    public Vector3d getX(){ return getAxis(0); }
    /**
     * Análogo a <source>getX</source>.
     */
    public Vector3d getY(){ return getAxis(1); }
    /**
     * Análogo a <source>getX</source>.
     */    
    public Vector3d getZ(){ return getAxis(2); }

    /*
     * Retorna o eixo de índice i.
     */
    private Vector3d getAxis( int i ){
        return new Vector3d(axes[i].getX(),axes[i].getY(),axes[i].getZ());
    }
    
    /*
     * Retorna o sistema de coordenadas à situação inicial (antes de qualquer rotação). Isto é, retoma os
     */
    private void reset(){
        axes = new Quat4d[]{
            new Quat4d(1,0,0,0),
            new Quat4d(0,1,0,0),
            new Quat4d(0,0,1,0)
        };
    }

    private Quat4d[] axes = null;
    private Quat4d z = null;    

}
