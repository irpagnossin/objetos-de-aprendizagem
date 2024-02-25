package cepa.edu.util;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JComponent;

public class LogoPanel extends JComponent implements MouseListener{

	private static final long serialVersionUID = 1L;
	
	BufferedImage logo = null;

    public LogoPanel( URL url ){
        super();

        try{
            logo = ImageIO.read(url);
        }
        catch( IOException e ){}

        addMouseListener(this);
    }
    
    
    public LogoPanel( String path ){
        super();

        try{
            ClassLoader classLoader = this.getClass().getClassLoader();
            URL url = classLoader.getResource(path);
            if ( url != null ) logo = ImageIO.read(url);
        }
        catch( IOException e ){}

        addMouseListener(this);
    }
    
    @Override
    protected void paintComponent( Graphics g ){
        if ( logo != null ) g.drawImage( logo, (getWidth() - logo.getWidth() >> 1), (getHeight() - logo.getHeight() >> 1), null );
    }

    public void mouseClicked(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
        setVisible(false);
    }

    public void mouseReleased(MouseEvent e) {
        mousePressed(e);
    }

    public void mouseEntered(MouseEvent e) {
        requestFocus();
    }

    public void mouseExited(MouseEvent e) {}
}

