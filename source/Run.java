import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.io.*;

public class Run
{
	public static void main(String [] args)
    {
        Object [] options = { "Batch", "Animation", "Neither, quit" };
         
        int selected = JOptionPane.showOptionDialog(
        null, 
        "Which do you wish to run?", 
        "Nick Payne's MANET simulation / animation Project",
        JOptionPane.DEFAULT_OPTION, 
        JOptionPane.QUESTION_MESSAGE,
        null, 
        options, 
        options[1]);
        
        if (selected == 1)
        { 
            Animation a = new Animation();
    
        }
        else if (selected == 0)
        {
            Batch b = new Batch();
        }
    }
}