import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class JPanelAnim extends JPanel
{
	BufferedImage backBuffer;
	private int w; 
	private int h;
	
	Graphics gPanel;
	Graphics2D gBackBuffer;
	
	public JPanelAnim(int w, int h)
	{
		super();
		backBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		(getBackBuffer()).clearRect(0, 0, w, h);
		this.w = w;
		this.h = h;
		setMinimumSize(new Dimension(w+10, h+10));
		setPreferredSize(new Dimension(w+10, h+10));
		setMaximumSize(new Dimension(w+10, h+10));
		
	}
	
	public Graphics2D getBackBuffer()
	{
		return backBuffer.createGraphics();
	}
	
	public void clear()
	{
		(getBackBuffer()).clearRect(0, 0, w, h);
	}
	
	// call this when we want to render everything to the panel
	public void flip()
	{
		(getGraphics()).drawImage(backBuffer, 5, 5, this);		
	}
	
	public void paint(Graphics g)
	{
		g.drawImage(backBuffer, 5, 5, this);	
	}
}
