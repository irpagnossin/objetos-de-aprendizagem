/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cepa.util;

import java.awt.Component;

/**
 *
 * @author ivan.pagnossin
 */
public class GraphCanvas extends Component{
      
	private static final long serialVersionUID = 1L;

	private double eps = 1e-15;

    private int[] canvasMargin = new int[]{50,50};
    private int[] canvasSize   = new int[]{500,500};

    private double[] r = new double[2];
    private double[] graphRange = new double[4];    
    
    public GraphCanvas( double[] graphRange, int[] canvasSize, int[] canvasMargin){
        
        this.canvasSize   = canvasSize;
        this.canvasMargin = canvasMargin;
        setGraphRange(graphRange);
                
    }
    
	public int[] translate( double p[] ) throws IllegalArgumentException{
		/*	
        if ( p[0] < graphRange[0] || p[0] > graphRange[1] || p[1] < graphRange[2] || p[1] > graphRange[3] ){
            throw new IllegalArgumentException( "O ponto (" + p[0] + "," + p[1] + ") está fora do gráfico." );
        }
        */        
		return new int[]{
            (int)Math.rint((p[0] - graphRange[0]) * r[0]) + canvasMargin[0],
            (int)Math.rint((graphRange[3] - p[1]) * r[1]) + canvasMargin[1]
        };
	}
    
    public int translateX( double x ){
        return (int)(x * r[0]);
    }
    
    public int translateY( double y ){
        return (int)(-y * r[1]);
    }
    
    public double[] translate( int p[] ) throws IllegalArgumentException{
        /*
        if ( p[0] < 0 || p[0] > canvasSize[0] || p[1] < 0 || p[1] > canvasSize[1] ){
            throw new IllegalArgumentException( "O ponto (" + p[0] + "," + p[1] + ") está fora da área disponível." );
        }
        */
        return new double[]{
            graphRange[0] + ( p[0] - canvasMargin[0] )/r[0],
            graphRange[3] - ( p[1] - canvasMargin[1] )/r[1]
        };
    } 
    public double[] translate( int x, int y ){
        return this.translate(new int[]{x,y});
    }
    
    public boolean has( double[] p ){
        
        boolean inside = true;
        
        if ( p[0] < graphRange[0] || p[0] > graphRange[1] || p[1] < graphRange[2] || p[1] > graphRange[3] ) inside = !inside;
        
        return inside;
    }
    
    public void setGraphRange( double[] graphRange ){
        this.graphRange = graphRange;

        r[0] = (this.canvasSize[0] - 2 * this.canvasMargin[0]) / Math.abs( this.graphRange[1] - this.graphRange[0] );
        r[1] = (this.canvasSize[1] - 2 * this.canvasMargin[1]) / Math.abs( this.graphRange[3] - this.graphRange[2] );

        if ( r[0] < eps || r[1] < eps ) throw new IllegalArgumentException( "Margem muito grande." );         
    }
}
