package cepa.edu.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

public class AnimationTimer extends Timer{
 
	private static final long serialVersionUID = 1L;
	
	private int t = 0;        
    
    public AnimationTimer( final int dt, ActionListener listener ){
        super(dt,listener);
        super.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                t += dt;
            }
        });
    }
            
    @Override
    public void stop(){
        super.stop();
        t = 0;
    }
    
    public void pause(){
        super.stop();
    }
    
    public void set( int t ){
        this.t = t;
    }
    
    public int getElapsedTime(){
        return t;
    }
}    
