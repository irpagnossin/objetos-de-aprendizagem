/*
 * Autor: Ivan Ramos Pagnossin
 * 
 * Campo vetorial bidimensional F(x,y) = Fx(x,y) i + Fy(x,y) j. Fz = 0 sempre.
 * 
 * Dezembro de 2007
 * Versão 1.0
 */
package util;

public class VectorField2D{
		
    private String description;
    private boolean conservative = false;    
    private MathExpression Fx = null, Fy = null, scalar = null;
    private ParametricCurve2D fieldLine;
    
	/**
     * Constrói um campo escalar <source>(Fx,Fy)</source>. Inicialmente o campo é não conservativo
     * (para torná-lo conservativo, utilize o método <source>setConservative</source>).
     */
    public VectorField2D( String Fx, String Fy ){
        this.Fx = new MathExpression( Fx );
        this.Fy = new MathExpression( Fy );
        this.description = new String( "F = (" + Fx + "," + Fy + ")" );
    }
        
    public void addFieldLine( ParametricCurve2D fieldLine ){
        this.fieldLine = fieldLine;
    }
    
    public ParametricCurve2D getFieldLine(){
        return fieldLine;
    }
    
    /**
     * Informa se o campo é conservativo (<source>true</source>) ou não (<source>false</source>).
     */
    public boolean isConservative(){
        return conservative;
    }
    
    /**
     * Define a função escalar que define o campo vetorial, tornando-o conservativo.
     * obs.: não confere a consistência com as componentes definidas no construtor.
     */
    public void setConservative( String scalar ){
        this.scalar = new MathExpression( scalar );
        conservative = true;
    }
    
    /**
     * Define uma descrição do campo vetorial.
     */
    public void setDescription( String description ){
        this.description = new String( description );
    }
    
    /**
     * Retorna a descrição do campo vetorial.
     */
    public String getDescription(){
        return description;
    }

    /**
     * Retorna <source>F(x,y)</source>.
     */
    public float[] getField( float x, float y ){
        return new float[]{ (float)Fx.value(x,y), (float)Fy.value(x,y) };
    }
    public float[] getField( float[] p ){
        return this.getField(p[0],p[1]);
    }
    
	/**
	 * Retorna <source>|F(x,y)|</source>.
	 */
	public float getModulus( float x, float y ){
		return (float)Math.sqrt(Math.pow(Fx.value(x,y), 2) + Math.pow(Fy.value(x,y), 2));		
	}
    public float getModulus( float[] p ){
        return this.getModulus(p[0],p[1]);
    }
                
	/**
	 * Retorna o ângulo de F(x,y) com relação ao eixo x, entre -PI/2 e PI/2.
	 */
	public float getOrientation( float x, float y ){				
		return (float)Math.atan2( Fy.value(x,y), Fx.value(x,y) );
	}        
    public float getOrientation( float[] p ){
        return this.getOrientation(p[0],p[1]);
    }
	
	/**
	 * Retorna o valor do potencial do campo em (x,y).
	 */
	public float getScalar( float x, float y){
        if ( conservative ) return (float)scalar.value(x,y);
        else return 0f;
	}
    public float getScalar( float[] p ){
        return getScalar(p[0],p[1]);
    }

}
