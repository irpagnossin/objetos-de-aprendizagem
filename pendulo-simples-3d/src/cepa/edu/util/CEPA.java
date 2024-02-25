package cepa.edu.util;

import java.sql.Time;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 *
 * @author ivan.pagnossin
 */
public class CEPA {
    
    private CEPA(){}
    
    /*
     * getLocales("pt_BR en_US it_IT") = { pt_BR, en_US, it_IT } (os elementos da matriz são objetos da classe Locale).
     */
    public static Locale[] getLocales( String list ){
        
        Pattern pattern = Pattern.compile("\\s+");
        Vector<Locale> vector = new Vector<Locale>(); 
                
        String[] locales = pattern.split(list);        
                
        for ( int i = 0; i < locales.length; i++ ){
            String[] codes = Pattern.compile("_").split(locales[i]);            
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
    
    public static String getCurrentTime(){
        return (new Time(System.currentTimeMillis())).toString();
    }
}
