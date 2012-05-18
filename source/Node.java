import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;

public class Node
{
    private double x;
    private double y;
    
    private int dx;  // drawing x
    private int dy;
    
    private double vx;
    private double vy;
    
    private double scale;
    
    private int broadcastRadius;
    
    private int dbroadcastRadius;
    
    private int ID;
    
    private int msgFromID=-1;
    
    private int encounters;
    private int encounterThreshold;
    
    private int broadcasts=0;
    private int msgsRecieved=0;
    
    private int destX;
    private int destY;
    
    private int stationaryTime;
    private int moveTime;  // DEBUG REALLY...
    
    private int neighbours;
    private int oldNeighbours;
    private Node [] neighbour;
    private Node [] oldNeighbour;
    
    private Color c;
    
    private boolean gotMsg=false; 
    
    private boolean broadcasting=false;
    
    private boolean selected=false;
    
    private boolean encounterLimit=false;
    
    private boolean helloMsg=false;
    
    private boolean newMsgRecvd=false;
    
    private double velocity;
    
    int floodTimer = 200 + (int)(Math.random()*1601);  // msec
    int helloTimer = 25;
    
    int areaSize;
    
    
    public Node(double x, double y, int broadcastRadius, int ID, int numNeighbours, int encounterThreshold)
    {
        this.x = x;
        this.y = y;
        
        destX = 0;
        destY = 0;
        
        stationaryTime = 0;
        
        this.broadcastRadius = broadcastRadius;
        
        this.ID = ID;
        
        encounters = 0;
        this.encounterThreshold = encounterThreshold;
        
        neighbours = 0;
        oldNeighbours = 0;
        neighbour = new Node[numNeighbours];
        oldNeighbour = new Node[numNeighbours];
        
        c = Color.red;
        
        moveTime = 3000 + (int)(Math.random()*17001);
    }
    
    public void setScale(double scale)
    {
        this.scale = scale;
    }
    
    public double getX()
    {
        return x;
    }
    
    public void setX(double x)
    {
        this.x = x;
    }
    
    public double getY()
    {
        return (int)y;
    }
    
    public void setY(double y)
    {
        this.y = y;
    }
    
    public double getVelX()
    {
        return vx*CONST.FPS;
    }
    
    public void setVelX(double vx)
    {
        this.vx = vx;
        this.vx = (vx/CONST.FPS);
    }
    
    public double getVelY()
    {
        return vy*CONST.FPS;
    }
    
    public void setVelY(double vy)
    {
        this.vy = vy;
        this.vy = (vy/CONST.FPS);
    }
    
    public void setVelocity(double v)
    {
        velocity = v/CONST.FPS;
    }
    
    public void setAngleInDegrees(double a)
    {
        double rad = a * (Math.PI/180.0);
        
        vx = Math.cos(rad) * velocity;
        vy = Math.sin(rad) * velocity;
    }
        
    
    public double getRadius()
    {
        return broadcastRadius;
    }
    
    public int getID()
    {
        return ID;
    }
    
    public int getNeighbours()
    {
        return neighbours;
    }
    
    public String getNeighbourList()
    {
        String ns = "";
        if (neighbours == 0)
        {
            return "none";
        }
        
        for (int i = 0; i < neighbours; i++)
        {
            ns += "" + neighbour[i].getID() + ", ";
        }
        
        return ns;
    }
    
    public boolean getMsgStatus()
    {
        return gotMsg;
    }
    
    public void setMsgStatus(boolean gotMsg)
    {
        this.gotMsg = gotMsg;
    }
    
    public int getBroadcasts()
    {
        return broadcasts;
    }
    
    public int getMsgsRecieved()
    {
        return msgsRecieved;
    }
    
    public void recvNewMsg(int fromID)
    {
        msgFromID = fromID;
        newMsgRecvd = true;
        setMsgStatus(true);
    }
    
    public boolean isSelected()
    {
        return selected;
    }
    
    public boolean isAtLimit()
    {
        return encounterLimit;
    }
    
    public boolean isAtDest()
    {
        if ((int)x == destX && (int)y == destY)
        {
            return true;
        }
        
        return false;
    }
    
    public void setNewDest(int dx, int dy)
    {
        destX = dx;
        destY = dy;
        
        double distX = dx - x;
        double distY = dy - y;
        
        double angle = Math.atan2(distY, distY);
        
        vx = Math.cos(angle)*velocity;
        vy = Math.sin(angle)*velocity;
        
    }
    public void setSelected(boolean s)
    {
        selected = s;
    }
    
    public boolean isBroadcasting()
    {
        return broadcasting;
    }
    
    public boolean isBroadcastingHello()
    {
        return helloMsg;
    }
    
    public boolean isNewEncounter(Node n)
    {
        for (int i = 0; i < oldNeighbours; i++)
        {
            if (oldNeighbour[i] != null && n.getID() == oldNeighbour[i].getID())  // this in't a new encounter
            {
                return false;
            }
        }
        
        // ok, it must be a new encounter then!
        return true;
    }
    
    
    public void move()
    {
        if (stationaryTime <= 0)
        {
        
            x += vx;
            y += vy;
            
            //if (isAtDest())
            //{
            //  stationaryTime = 1000 + (int)(Math.random()*1001);
            //  setNewDest((int)(Math.random()*areaSize), (int)(Math.random()*areaSize));
            //}
            
            moveTime -= CONST.RATE;
            if (moveTime <= 0)
            {
                stationaryTime = 1000 + (int)(Math.random()*1001);
                moveTime = 3000 + (int)(Math.random()*20001);
                setAngleInDegrees(Math.random()*360);
            }
        }
        else
        {
            stationaryTime -= CONST.RATE;
        }       
    }
    
    public void draw(Graphics2D g)  // draw onto this graphics context. WE ASSUME THIS IS A BufferedImage
    {
        if (!gotMsg)
            g.setColor(Color.red);
        else if (encounterLimit)
            g.setColor(Color.white);
        else
            g.setColor(Color.blue);
        
        dbroadcastRadius = (int)((double)broadcastRadius * scale);
        dx = (int)(x*scale);
        dy = (int)(y*scale);
        
        g.draw(new Ellipse2D.Double(dx-dbroadcastRadius, dy-dbroadcastRadius, 
                                    dbroadcastRadius*2, dbroadcastRadius*2));       
        g.fill(new Ellipse2D.Double(dx-2, dy-2, 4, 4));
    }
    
    public void drawNeighbourLines(Graphics2D g)
    {
        for (int i = 0; i < neighbours; i++)
        {
            if (neighbour[i].hasNeighbour(getID()))// && neighbour[i].getID() < getID())
            {
                g.setColor(Color.green);
            
            
                dx  = (int)(x*scale);
                dy  = (int)(y*scale);
                int dx1 = (int)(neighbour[i].getX()*scale);
                int dy1 = (int)(neighbour[i].getY()*scale);
                
                g.draw(new Line2D.Double(dx, dy, dx1, dy1));
            }
            else if (neighbour[i].getRadius() != getRadius())
            {
                g.setColor(Color.orange);
                dx  = (int)(x*scale);
                dy  = (int)(y*scale);
                int dx1 = (int)(neighbour[i].getX()*scale);
                int dy1 = (int)(neighbour[i].getY()*scale);
                
                g.draw(new Line2D.Double(dx, dy, dx1, dy1));
            }
        }
    }
    
    public void checkNeighbours(Node [] n)
    {
        neighbours = 0;
        
        //boolean broadcast = false;
        
        for (int i = 0; i < n.length; i++)
        {
            if (n[i].getID() != getID())
            {
                // right, check the preliminary boundaries of this node
                
                // but first test if it's broadcasting a hello
                if (n[i].isBroadcastingHello())
                {

                    
                
                    if (getX() >= (n[i].getX() - n[i].getRadius()) &&
                        getX() <= (n[i].getX() + n[i].getRadius()) &&
                        getY() >= (n[i].getY() - n[i].getRadius()) &&
                        getY() <= (n[i].getY() + n[i].getRadius())   )
                    {
                        double dx = x - n[i].getX();
                        double dy = y - n[i].getY(); 
                        
                        double dist = Math.sqrt((dx*dx) + (dy*dy));
                        if (dist <= n[i].getRadius())
                        {
                            // ok! we're in this node's radius, we got a neighbour!
                            
                                
                            neighbour[neighbours ++] = n[i];
                                                
                        }
                    }
                }
            }
        }
        
    }
    
    public void encounterMe(Node n)
    {
        oldNeighbour[oldNeighbours++] = n;
    }
    
    public void checkEncounters()
    {
        boolean broadcast = false;
        
        for (int i = 0; i < neighbours; i++)
        {
                        
                
            if (isNewEncounter(neighbour[i]))
            {
                //n[i].encounterMe(this);               
        
                if (getMsgStatus() && msgFromID != neighbour[i].getID())
                {
                    if (encounters < encounterThreshold)
                    {
                        if (broadcast == false) // have we already broadcast this frame?
                        {
                            encounters ++;
                            broadcasts ++;
                            broadcast = true;
                        }
                        
                        neighbour[i].recvNewMsg(getID());
                        neighbour[i].passOnMsg(getID());
                        
                        if (encounters >= encounterThreshold)
                        {
                            encounterLimit = true;
                        }
                                                        
                    }
                }
            }
        }
        
        
        msgFromID = -1;
        
        oldNeighbours = neighbours;
        
        for (int i = 0; i < neighbours; i ++)
        {
            oldNeighbour[i] = neighbour[i];
        }   
    }
    
    public void passOnMsg(int fromID)
    {
        if (neighbours == 1 || newMsgRecvd == false)
        {
            return;
        }
        
        if (newMsgRecvd == true)
        {
            newMsgRecvd = false;
            
            boolean broadcast = false;
            
            for (int i = 0; i < neighbours; i ++)
            {
                if (neighbour[i].getID() == fromID)
                {
                    //System.out.println("ignoring sender");
                    continue;
                }
                else
                {   
                    if (getMsgStatus())  // really, we know this will be true...
                    {
                        if (encounters < encounterThreshold)
                        {
                            if (broadcast == false) // have we already broadcast this frame?
                            {
                                encounters ++;
                                broadcasts ++;
                                broadcast = true;
                            }
                            
                            if (neighbour[i].getMsgStatus() == false)
                            {
                                neighbour[i].recvNewMsg(getID());
                                neighbour[i].passOnMsg(getID());
                            }
                            
                            if (encounters >= encounterThreshold)
                            {
                                encounterLimit = true;
                            }
                                                            
                        }
                    }
                }
            }

        }           
    }
    
    public boolean checkPoint(int px, int py)
    {
         if (px >= (getX() - getRadius()) &&
             px <= (getX() + getRadius()) &&
             py >= (getY() - getRadius()) &&
             py <= (getY() + getRadius())   )
        {
            double dx = px - getX();
            double dy = py - getY();
            
            
            if (Math.sqrt((dx*dx) + (dy*dy)) <= getRadius())
            {
                return true;        
            }
        }
        
        return false;
    }
    
    public boolean hasNeighbour(int neighbourID)
    {
        if (neighbours == 0)
            return false;
            
        for (int i = 0; i < neighbours; i++)
        {
            if (neighbour[i].getID() == neighbourID)
            {
                return true;
            }
        }
        
        return false;
    }
    
    
    public void sayHello()
    {
        helloTimer -= CONST.RATE;
        
        if (helloTimer <= 0)
        {
            helloTimer = 25;
            helloMsg = true;
        }
        else
        {
            helloMsg = false;
        }
    }
}