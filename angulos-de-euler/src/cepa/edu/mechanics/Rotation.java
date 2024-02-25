package cepa.edu.mechanics;

/**
 * A classe <source>Rotation</source> representa uma rotação ao redor de um dos eixos coordenados: x, y ou z.
 * @author ivan.pagnossin
 */
public class Rotation {
	
	/** Representa o eixo x. */
	public static final int X = 0;
	
	/** Representa o eixo y. */
	public static final int Y = 1;
	
	/** Representa o eixo z. */
	public static final int Z = 2;
	    
	/**
	 * Constrói uma rotação de <source>angle</source> radianos ao redor do eixo <source>axis</source>, que pode ser <source>Rotation.X</source>, <source>Y</source> ou <source>Z</source>.  
	 */
    public Rotation( final double angle, final int axis ) throws IllegalArgumentException {
    	if ( axis != X && axis != Y && axis != Z ) throw new IllegalArgumentException("Invalid axis index: " + axis);
    	
        this.axis = axis;
        this.angle = angle - Math.floor(angle/PI2)*PI2;
    }

    /**
     * Constrói uma rotação a partir de outra, <source>r</source>.
     */
    public Rotation(Rotation r) {
        this(r.getAngle(),r.getAxisIndex());
    }    
    
    /** 
     * @return o índice do eixo ao redor do qual ocorre esta rotação: 0 para x, 1 para y e 2 para z.
     */
    public int getAxisIndex(){ return axis; }
    
    /**
     * @return o ângulo da rotação, em radianos.
     */
    public double getAngle(){ return angle; }
    
    /**
     * Verifica se esta rotação é igual a <source>r</source>
     * @return <source>true</source> se sim; <source>false</source> em caso contrário.
     */
    public boolean equals( final Rotation r ){
        return this.getAxisIndex() == r.getAxisIndex() && Math.abs(getAngle()-r.getAngle()) < eps ? true : false;
    }
    
    /**
     * Dois ângulos (<source>double</source>) que diferem entre si de um número inferior a <source>eps</source> são considerados iguais.
     * @param eps o "epsilon" utilizado para comparar rotações.
     */
    public void setEPS( final double eps ){ this.eps = eps; }
    
    /**
     * @return o "epsilon" (veja <source>setEPS</source>).
     */
    public double getEPS(){ return eps; }
    
    @Override
    public String toString(){ 
        return new String("Rotation: " + angle + " rad around axis index " + axis);
    }

    private final int axis; // Índice do eixo sobre o qual ocorre a rotação.
    private final double angle; // Ângulo da rotação, em radianos.    
    private double eps = 0.005; // Se a diferença entre dois ângulos for menor que eps, eles são considerados iguais.
    
    private static final double PI2 = 2 * Math.PI;
}
