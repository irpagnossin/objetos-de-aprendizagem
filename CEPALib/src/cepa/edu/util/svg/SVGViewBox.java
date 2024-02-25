package cepa.edu.util.svg;

import java.util.regex.Pattern;

public class SVGViewBox {
	
	private double[] parameter = new double[4];
	
	public SVGViewBox ( final String attribute ) {
		String[] aux = Pattern.compile(" ").split(attribute); // NOI18N        
		for ( short i = 0; i < 4; i++ ) parameter[i] = Double.parseDouble(aux[i]);		
	}
	
	public double getX () {return parameter[0];}
	public double getY () {return parameter[1];}
	public double getWidth () {return parameter[2];}
	public double getHeight () {return parameter[3];}
	
	public void setX (double x) {parameter[0] = x;}
	public void setY (double y) {parameter[1] = y;}
	public void setWidth (double width) {parameter[2] = width;}
	public void setHeight (double height) {parameter[3] = height;}
}
