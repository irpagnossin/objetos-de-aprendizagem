package cepa.edu.util;

import java.net.URL;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

public class Util {
	public static double distance ( final double[] ptA, final double[] ptB ) {
		// TODO: generalizar para qualquer dimensão.
		double d = Math.sqrt(Math.pow(ptA[0]-ptB[0],2)+Math.pow(ptA[1]-ptB[1],2));
		return d;
	}
	
    /*
     * Retorna o ícone, objeto de ImageIcon, associado ao arquivo de nome filename.
     */
    public static ImageIcon getIcon( final String filename ){

        URL url = ClassLoader.getSystemClassLoader().getResource(filename);
		
        if ( url == null ) return null;
        else return new ImageIcon(url);
    }

    /*
     * getLocales("pt_BR en_US it_IT") = { pt_BR, en_US, it_IT } (os elementos da matriz são objetos da classe Locale).
     */
    public static Locale[] getLocales( final String list ){

        Vector<Locale> vector = new Vector<Locale>();

        String[] locales = Pattern.compile(" ").split(list); // NOI18N

        for ( int i = 0; i < locales.length; i++ ){
            String[] codes = Pattern.compile("_").split(locales[i]); // NOI18N
            if ( codes.length == 2 ) vector.add( new Locale(codes[0],codes[1]) );
        }

        return vector.toArray(new Locale[locales.length]);
    }
    
    public static void sleep( int time ){
        try{
            Thread.sleep(time);
        }
        catch( InterruptedException e ) {/* Nada */}
    }    

    public static double Heaviside (final double x) {
    	return (1 + Math.signum(x))/2;
    }
    
}
