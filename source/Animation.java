import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.Timer;
import java.awt.image.BufferedImage;

public class Animation implements ActionListener, ItemListener, MouseListener
{   
    private Node []     nodes;
    private int         numNodes;
    
    private boolean     antiAliasing = false;
    private boolean     nodeNeighbours = false;
    
    private boolean     stopped = true;
        
    private double      elapsedTime=0.0;
    private long        startTime=0;
    
    private int         totalMsgs=0;
    private int         recvdMsgs=0;
    
    private double      scale=0.5;
    
    private int         metresWidth;
    private int         metresHeight;
    
    private int         trackingNodeID=-1;
    
    private int         cfgMinRadius;
    private int         cfgMaxRadius;
    private int         cfgNumNodes;
    private int         cfgAreaWidth;
    private int         cfgAreaHeight;
    private int         cfgThreshhold;
    private int         cfgNodeSpeed;

    
    private Tabulator   gather;
    
    
    private Timer       t;
    
    private JPanelAnim  animPanel;
    private JPanel      leftPanel;
    private JPanel      rightPanel;
    private JPanel      guiPanel;
    private JPanel      graphicsPanel;
    private JPanel      outputPanel;
    private JPanel      indvNodePanel;
    private JPanel      textOutputPanel;
    private JPanel      keyPanel;
    
    private JButton     startButton;        
    private JButton     stopButton;
    
    private JCheckBox   antiAliasingBox;
    private JCheckBox   nodeNeighboursBox;
    
    private JComboBox   mapSize;
    
    private JSpinner    numNodeSpinner;
    private JSpinner    threshholdSpinner;
    
    private JLabel      msgsBroadcastLabel;
    private JLabel      msgsRecievedLabel;
    private JLabel      networkEfficiencyLabel;
    private JLabel      networkCoverageLabel;
    
    private JLabel      nodeIDLabel;
    private JLabel      nodeMsgsBroadcastLabel;
    private JLabel      nodeNeighboursLabel;
    private JLabel      nodeMsgsVelocityXY;
    private JLabel      nodeRadiusLabel;
    
    private JMenuBar    menuBar;
    
    private JMenu       menu;
    private JMenu       subMenu;
    
    private JMenuItem   menuItem;
    
    private JFrame      frame;
    
    private JTextArea   textOutputArea;
    private JScrollPane textScrollPane;
    
    private JCheckBoxMenuItem       cbMenu;
    private JRadioButtonMenuItem    rbMenu;
    
    
    
    
    public Animation()
    {
        loadConfig(CONST.ANIM_CONFIG_FILE);  // load in the config file
        
        setMapSize(cfgAreaWidth, cfgAreaHeight);  // set the terrain size
        
        gather = new Tabulator("data pooling gatherer");
        
        setTimer();
        
        setupComponents();
        
        frame.setVisible(true);     
    }
    
    public void actionPerformed(ActionEvent e)
    {
        if ("start".equals(e.getActionCommand()))
        {
            if (!t.isRunning())
            {
                if (stopped)  // ok, the app was either properly stopped, or not run yet
                {
                    startTime = System.currentTimeMillis();
                    elapsedTime = 0;
                    gather.clearAllData();
                    Integer nI = (Integer)numNodeSpinner.getValue();
                    int n = nI.intValue();
                    Integer tI = (Integer)threshholdSpinner.getValue();
                    int t = tI.intValue();
                    String size = (String)mapSize.getSelectedItem();
                    setMapSize(size);
                    initNodes(n, t);
                    saveConfig(CONST.ANIM_CONFIG_FILE);
                    stopped = false;
                }
                
                t.start();
                startButton.setText("pause");
            }
            else if (t.isRunning())
            {
                t.stop();
                startButton.setText("resume");
            }
            
        }
        else if ("stop".equals(e.getActionCommand()))
        {
            if (!stopped)
            {
                t.stop();
                stopped = true;
                startButton.setText("start");
                animPanel.clear();
                animPanel.flip();
            }
        }
    }
    
    public void buildMenu()
    {
        menuBar = new JMenuBar();
        
        menu = new JMenu("File");
        menuItem = new JMenuItem("Quit");
        menu.add(menuItem);
        menuBar.add(menu);
        
        menu = new JMenu("Configure");
        subMenu = new JMenu("Graphics");
        cbMenu = new JCheckBoxMenuItem("Anti Aliasing");
        cbMenu.setSelected(antiAliasing);
        cbMenu.addItemListener(this);
        subMenu.add(cbMenu);
        cbMenu = new JCheckBoxMenuItem("Neighbour Lines");
        cbMenu.setSelected(nodeNeighbours);
        cbMenu.addItemListener(this);
        subMenu.add(cbMenu);
        menu.add(subMenu);
        //menu.addSeparator();
        subMenu = new JMenu("Protocol");
        rbMenu = new JRadioButtonMenuItem("Flooding");
        subMenu.add(rbMenu);
        rbMenu = new JRadioButtonMenuItem("Other");
        subMenu.add(rbMenu);
        menu.add(subMenu);
        
        subMenu = new JMenu("Area size");
        rbMenu = new JRadioButtonMenuItem("Small");
        subMenu.add(rbMenu);
        rbMenu = new JRadioButtonMenuItem("Medium");
        subMenu.add(rbMenu);
        rbMenu = new JRadioButtonMenuItem("Large");
        subMenu.add(rbMenu);
        rbMenu = new JRadioButtonMenuItem("Huge");
        subMenu.add(rbMenu);
        menu.add(subMenu);
        menuBar.add(menu);
        
        
        menu = new JMenu("Help");
        menuItem = new JMenuItem("Help Contents");
        menu.add(menuItem);
        menuItem = new JMenuItem("About...");
        menu.add(menuItem);
        menuBar.add(menu);
        
        frame.setJMenuBar(menuBar);
    }
    
    // update this if we incorperate maps etc
    // bit ugly having to call all the nodes
    // .get...() and set...() methods, but makes 
    // more sense for the 'world' to check the 
    // bounds of each node, rather than the node itself
    
    public void checkBounds(Node n)
    {
        if (n.getX() < 0)
        {
            n.setX(0);
            n.setVelX(-n.getVelX());
        }
        if (n.getX() > metresWidth)
        {
            n.setX(metresWidth);
            n.setVelX(-n.getVelX());
        }
        
        if (n.getY() < 0)
        {
            n.setY(0);
            n.setVelY(-n.getVelY());
        }
        if (n.getY() > metresHeight)
        {
            n.setY(metresHeight);
            n.setVelY(-n.getVelY());
        }
    }
    
    public int checkCoverage()
    {
        int msgsGot = 0;
        for (int i = 0; i < numNodes; i++)
        {
            if (nodes[i].getMsgStatus())
                msgsGot ++;
        }
        
        double pc = (double)msgsGot / (double)numNodes;
        pc *= 100;
        return (int)pc;
    }
    
    public void initNodes(int n, int t)
    {
        numNodes = n;
        cfgNumNodes = n;
        cfgThreshhold = t;
        
        nodes = new Node[numNodes];
        
        for (int i = 0; i < numNodes; i++)
        {
            nodes[i] = new Node((Math.random()*metresWidth), 
                       (Math.random()*metresHeight), 
                       cfgMinRadius + (int)(Math.random()*(cfgMaxRadius+1-cfgMinRadius)), 
                       i, cfgNumNodes, cfgThreshhold);
            
            
            nodes[i].setVelocity(cfgNodeSpeed);
            nodes[i].setAngleInDegrees(Math.random()*360);
            
            nodes[i].setScale(scale);
        }
        
        nodes[0].setMsgStatus(true);
        
        trackingNodeID = -1;
    }
    
    public void itemStateChanged(ItemEvent e) 
    {
        Object source = (e.getSource());
        
        if ((source.getClass().getName()).equals("javax.swing.JCheckBoxMenuItem"))
        {
            JMenuItem menuItem = (JMenuItem)source;
         
         if ((menuItem.getText()).equals("Anti Aliasing"))
         {
            if (e.getStateChange() == ItemEvent.DESELECTED)
            {
                antiAliasing = false;
            }
            else
            {
                antiAliasing = true;
            }
         }
         
         else if ((menuItem.getText()).equals("Neighbour Lines"))
         {
            if (e.getStateChange() == ItemEvent.DESELECTED)
            {
                nodeNeighbours = false;
            }
            else
            {
                nodeNeighbours = true;
            }
         }
         }
        
         // otherwise, it's not a check box menu
    }
    
    public void loadConfig(String path)
    {
        String str = null;
        try 
        {
            boolean radius = false;
            boolean nodes = false;
            boolean area = false;
            boolean threshhold = false;
            boolean speed = false;
            BufferedReader in = new BufferedReader(new FileReader(path));
            
            while ((str = in.readLine()) != null)
            {
                if (str.equals("") || str.charAt(0) == '#')
                {
                    continue;
                }
                
                // ok, it wasn't a comment or an empty line, we assume its
                // an option variable. but it must follow the rules!
                
                String [] split = str.split("\\s");
                
                if (split[0].equals("RADIUSMINMAX"))
                {
                    radius = true;
                    // RADMINMAX [int] [int]
                    
                    if (split.length != 3)  // wrong!
                    {
                        System.out.println("RADMINMAX config option has incorrect" +
                                           "number of parameters. Using default values.");
                                           
                        cfgMinRadius = CONST.MIN_NODE_RADIUS;
                        cfgMaxRadius = CONST.MAX_NODE_RADIUS;
                    }
                    else
                    {
                        cfgMinRadius = Integer.parseInt(split[1]);
                        if (cfgMinRadius < CONST.MIN_NODE_RADIUS)
                        {
                            System.out.println("min radius too small");
                            cfgMinRadius = CONST.MIN_NODE_RADIUS;
                        }
                        else if (cfgMinRadius > CONST.MAX_NODE_RADIUS)
                        {
                            System.out.println("min radius too large");
                            cfgMinRadius = CONST.MAX_NODE_RADIUS;
                        }
                        
                        cfgMaxRadius = Integer.parseInt(split[2]);
                        
                        if (cfgMaxRadius < cfgMinRadius)
                        {
                            System.out.println("max radius smaller than min");
                            cfgMaxRadius = cfgMinRadius;
                        }
                        else if (cfgMaxRadius > CONST.MAX_NODE_RADIUS)
                        {
                            System.out.println("max radius too large");
                            cfgMaxRadius = CONST.MAX_NODE_RADIUS;
                        }
                        
                    }
                        
                }
                else if (split[0].equals("NUMNODES"))
                {
                    nodes = true;
                    // NUMNODES [int]
                    
                    if (split.length != 2)  // wrong!
                    {
                        System.out.println("NUMNODES config option has incorrect" +
                                           "number of parameters. Using default values.");
                        
                        cfgNumNodes = CONST.DEFAULT_NUM_NODES;
                    }
                    else
                    {
                        cfgNumNodes = Integer.parseInt(split[1]);
                        
                        if (cfgNumNodes < CONST.MIN_NUM_NODES ||
                            cfgNumNodes > CONST.MAX_NUM_NODES)
                        {
                            cfgNumNodes = CONST.DEFAULT_NUM_NODES;
                        }
                    }
                }
                else if (split[0].equals("NODESPEED"))
                {
                    speed = true;
                    // NODESPEED [int]
                    
                    if (split.length != 2)  // wrong!
                    {
                        System.out.println("NODESPEED config option has incorrect" +
                                           "number of parameters. Using default values.");
                        
                        cfgNodeSpeed = CONST.DEFAULT_NODE_SPEED;
                    }
                    else
                    {
                        cfgNodeSpeed = Integer.parseInt(split[1]);
                        
                        if (cfgNodeSpeed < CONST.MIN_NODE_SPEED ||
                            cfgNodeSpeed > CONST.MAX_NODE_SPEED)
                        {
                            cfgNodeSpeed = CONST.DEFAULT_NODE_SPEED;
                        }
                    }
                }
                else if (split[0].equals("AREASIZE"))
                {
                    area = true;
                    // AREASIZE [int]
                    
                    if (split.length != 2)  // wrong!
                    {
                        System.out.println("AREASIZE config option has incorrect" +
                                           "number of parameters. Using default values.");
                        
                        cfgAreaWidth  = CONST.STD_MW;
                        cfgAreaHeight = CONST.STD_MH;
                    }
                    else
                    {
                        cfgAreaWidth = cfgAreaHeight = Integer.parseInt(split[1]);
                        
                        if (!(cfgAreaWidth == CONST.SML_MW ||
                            cfgAreaWidth == CONST.STD_MW ||
                            cfgAreaWidth == CONST.LRG_MW ||
                            cfgAreaWidth == CONST.HGE_MW))
                        {
                            
                            cfgAreaWidth  = CONST.STD_MW;
                            cfgAreaHeight = CONST.STD_MH; 
                        }
                    }
                }
                else if (split[0].equals("THRESHHOLD"))
                {
                    threshhold = true;
                    // THRESHHOLD [int]
                    if (split.length != 2)  // wrong!
                    {
                        System.out.println("THRESHHOLD config option has incorrect" +
                                           "number of parameters. Using default value.");
                        
                        cfgThreshhold = 8;
                    }
                    else
                    {
                        cfgThreshhold = Integer.parseInt(split[1]);
                        
                        if (cfgThreshhold < 1)
                        {
                            cfgThreshhold = 1;
                        }
                        else if (cfgThreshhold > 25)
                        {
                            cfgThreshhold = 25;
                        }
                    }
                }
                
            }
            
            // ok, did we read everything in?
            if (!radius)
            {
                cfgMinRadius = CONST.MIN_NODE_RADIUS;
                cfgMaxRadius = CONST.MAX_NODE_RADIUS;
            }
            
            if (!nodes)
            {
                cfgNumNodes = CONST.DEFAULT_NUM_NODES;
            }
            
            if (!speed)
            {
                cfgNodeSpeed = CONST.DEFAULT_NODE_SPEED;
            }
            
            if (!area)
            {
                cfgAreaWidth  = CONST.STD_MW;
                cfgAreaHeight = CONST.STD_MH;
            }
            
            if (!threshhold)
            {
                cfgThreshhold = CONST.DEFAULT_THRESHHOLD;
            }
            
            in.close();
        }
        catch (Exception e)
        {
            System.out.println("Couldn't load config.cfg! Does it exist?" + CONST.N +
                               "Using standard config options...");
            
            cfgMinRadius = CONST.DEFAULT_NODE_RADIUS;
            cfgMaxRadius = CONST.DEFAULT_NODE_RADIUS;
            cfgNumNodes = CONST.DEFAULT_NUM_NODES;
            cfgNodeSpeed = CONST.DEFAULT_NODE_SPEED;
            cfgAreaWidth  = CONST.STD_MW;
            cfgAreaHeight = CONST.STD_MH;
            cfgThreshhold = CONST.DEFAULT_THRESHHOLD;
        }
    }
    
    public void mouseClicked(MouseEvent e)
    {
        // ok, get the location
        if (e.getButton() == MouseEvent.BUTTON1)
        {
            PointerInfo pi = MouseInfo.getPointerInfo();
            
            Point p = pi.getLocation();
            
            // we need all these absolute values as they are the offsets that
            // we cannot get by calling Object.getX() on them - these have
            // been calculated by trial and error but do seem to work even
            // on different operating systems...
            
            double mX = p.getX() - animPanel.getX() - 5 - 5 - frame.getX();
            double mY = p.getY() - animPanel.getY() - 5 - 5 -5 - 1 - 20 - 20 - frame.getY();
            
            mX  = (int)mX / scale;
            mY  = (int)mY / scale;
            
            for (int i = 0; i < numNodes; i++)
            {
                if (nodes[i].checkPoint((int)mX, (int)mY))
                {
                    trackingNodeID = i;
                }
            }
        }
        else
        {
            trackingNodeID = -1;
        }
    }
    
    public void mouseReleased(MouseEvent e) 
    {
       // don't care
    }
    
    public void mousePressed(MouseEvent e) 
    {
       // don't care
    }

    public void mouseEntered(MouseEvent e) 
    {
        // don't care
       
    }

    public void mouseExited(MouseEvent e) 
    {
        // don't care
       
    }
    
    // the simulation logic to run each frame
    public void runLogic()
    {
        totalMsgs = 0;
        recvdMsgs = 0;
        for (int i = 0; i < numNodes; i++)
        {
            nodes[i].move();
            checkBounds(nodes[i]);
            nodes[i].sayHello();
        }
        
        for (int i = 0; i < numNodes; i++)
        {
            nodes[i].checkNeighbours(nodes);
        }
        
        for (int i = 0; i < numNodes; i++)
        {
            nodes[i].checkEncounters();
        }
        
        for (int i = 0; i < numNodes; i++)
        {
            
            totalMsgs += nodes[i].getBroadcasts();
            
            if (nodes[i].getMsgStatus() == true)
            {
            
                recvdMsgs ++;
            }
        }
        
        totalMsgs += 1;  // for the first node
    }
    
    // called by the timer
    public void runAnim()
    {
        elapsedTime += CONST.RATE;//(int)1000/fps;
        
        animPanel.clear();
        
        Graphics2D gAnim = animPanel.getBackBuffer();  // we now have the back buffer
        
        if (antiAliasing)
        {
            gAnim.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
        }
                
        runLogic();
        
        for (int i = 0; i < numNodes; i++)
        {
            nodes[i].draw(gAnim);
            
            
            if (nodeNeighbours)
            {
                nodes[i].drawNeighbourLines(gAnim);
            }
        }
        
        
        if (trackingNodeID != -1)
        {
            gAnim.setColor(Color.white);
            
            int tRadius = (int)(nodes[trackingNodeID].getRadius() * scale);
            int tx = (int)(nodes[trackingNodeID].getX()*scale);
            int ty = (int)(nodes[trackingNodeID].getY()*scale);
            
            gAnim.draw(new Rectangle2D.Double(tx-tRadius, ty-tRadius, 
                                     tRadius*2, tRadius*2));
        }
        
        
        gAnim.setColor(Color.white);
        gAnim.drawString(new String((int)elapsedTime/1000 + " simulated seconds"), 1, 11);
        gAnim.drawString(new String((System.currentTimeMillis()-startTime)/1000 
                   + " actual seconds"), 1, 21);
        animPanel.flip();
        
        
        double pc = 0;
        
        if (totalMsgs > 0)
        {
            pc = (double)recvdMsgs / (double)totalMsgs;
        }
        pc *= 100;
        
        msgsBroadcastLabel.setText(" messages broadcast: "+totalMsgs);
        msgsRecievedLabel.setText(" nodes with message: "+recvdMsgs);
        networkEfficiencyLabel.setText(" network efficiency: "+(int)pc+"%");
        networkCoverageLabel.setText(" network coverage: "+checkCoverage()+"%");
        
        
        if (trackingNodeID != -1)
        {
            if (nodes[trackingNodeID].getMsgStatus() == true)
            {
                nodeIDLabel.setText(" Node ID: " + 
                        nodes[trackingNodeID].getID() + " (has msg)");
            }
            else
            {
                nodeIDLabel.setText(" Node ID: " + 
                        nodes[trackingNodeID].getID() + " (no msg)");
            }
                       
            nodeMsgsBroadcastLabel.setText(" messages broadcast: " + 
                                  nodes[trackingNodeID].getBroadcasts());
                                  
                                  
            nodeMsgsVelocityXY.setText(" velocity (m/sec): (" + 
                              (int)nodes[trackingNodeID].getVelX() + ", " +
                              (int)nodes[trackingNodeID].getVelY() + ")");
                              
            nodeRadiusLabel.setText(" radius (m): " +
                           nodes[trackingNodeID].getRadius());
                           
            String neighbourText = " neighbours: " + nodes[trackingNodeID].getNeighbourList();
            nodeNeighboursLabel.setText(neighbourText);
        }
        else
        {
            nodeIDLabel.setText(" Node ID: N/A");
            nodeMsgsBroadcastLabel.setText(" messages broadcast: N/A");
            nodeMsgsVelocityXY.setText(" velocity (m/sec): N/A");
            nodeRadiusLabel.setText(" radius (m): N/A");
            nodeNeighboursLabel.setText(" neighbours: N/A");
        }
            
                      
        if (gather.checkForPool())  // time to pool!
        {
            gather.addData(totalMsgs, "total messages broadcast");
            gather.addData(recvdMsgs, "nodes with message");
            gather.addData(pc, "network efficiency");
            gather.addData((int)elapsedTime, "total time taken");
        }
        
        if (checkCoverage() == 100 || threshholdsReached())
        {
            t.stop();
            stopped = true;
            startButton.setText("start");
            
            gather.addData(totalMsgs, "total messages broadcast");
            gather.addData(recvdMsgs, "nodes with message");
            gather.addData(pc, "network efficiency");
            gather.addData((int)elapsedTime, "total time taken");
            
            //gather.printData("total messages broadcast");
            
            String [] columns = {"total messages broadcast",
                                 "nodes with message",
                                 "network efficiency",
                                 "total time taken"};
                                                     
            gather.tabulateData(columns, new String("n" + numNodes + 
                                                    " t" + cfgThreshhold + 
                                                    " - " + (int)elapsedTime/1000 + 
                                                    " secs.txt"));
        }
        
        
    }
    
    public void saveConfig(String path)
    {
        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(path));
            
            out.write("# Nick's MANET A/B PROJECT config file for ANIMATION MODE" + CONST.N);
            out.write("# NOTE THAT ALL LINES STARTING WITH A HASH (#) ARE ASSUMED TO BE COMMENTS!"
            + CONST.N);
            out.write("#" + CONST.N);
            out.write("# ONLY HAND EDIT THIS IF YOU REALLY WANT..." + CONST.N);
            out.write("# ... AND DON'T BREAK ANYTHING!" + CONST.N);
            out.write("#" + CONST.N);
            out.write("# AREASIZE [int]" + CONST.N);
            out.write("# valid arguments: 125, 250, 500, 1000" + CONST.N);
            out.write("# default: 250" + CONST.N);
            out.write("" + CONST.N);
            out.write("AREASIZE " + cfgAreaWidth + "" + CONST.N);
            out.write("" + CONST.N);
            out.write("# THRESHHOLD [int]" + CONST.N);
            out.write("# valid arguments: " + CONST.MIN_THRESHHOLD + " - " + CONST.MAX_THRESHHOLD
            + "" + CONST.N);
            out.write("# default: " + CONST.DEFAULT_THRESHHOLD + "" + CONST.N);
            out.write("" + CONST.N);
            out.write("THRESHHOLD " + cfgThreshhold + "" + CONST.N);
            out.write("" + CONST.N);
            out.write("# RADIUSMINMAX [int1] [int2]" + CONST.N);
            out.write("" + CONST.N);
            out.write("# valid arguments: any two integers, though int2 >= int1" + CONST.N);
            out.write("# certain limits are imposed on int1 and int2. if values are" + CONST.N);
            out.write("# too big or small, these limits are automatically imposed." + CONST.N);
            out.write("# default:  " + CONST.DEFAULT_NODE_RADIUS + " " + CONST.DEFAULT_NODE_RADIUS
            + "" + CONST.N);
            out.write("" + CONST.N);
            out.write("RADIUSMINMAX " + cfgMinRadius + " " + cfgMaxRadius + "" + CONST.N);
            out.write("" + CONST.N);
            out.write("# NUMNODES [int]" + CONST.N);
            out.write("# valid arguments: " + CONST.MIN_NUM_NODES + " - " + CONST.MAX_NUM_NODES
            + "" + CONST.N);
            out.write("# default: " + CONST.DEFAULT_NUM_NODES + "" + CONST.N);
            out.write("" + CONST.N);
            out.write("NUMNODES " + cfgNumNodes + "" + CONST.N);
            out.write("" + CONST.N);
            out.write("# NODESPEED [int]" + CONST.N);
            out.write("# valid arguments: " + CONST.MIN_NODE_SPEED + " - " + CONST.MAX_NODE_SPEED
            + "" + CONST.N);
            out.write("# default: " + CONST.DEFAULT_NODE_SPEED + "" + CONST.N);
            out.write("" + CONST.N);
            out.write("NODESPEED " + cfgNodeSpeed + "" + CONST.N);
            out.write("" + CONST.N);
            out.write("# END OF CONFIG" + CONST.N);
            
            
            out.close();
        }
        catch (Exception e)
        {
            System.out.println("Could not write to config file. Is it in use?");
        }
    }
    
    
    // set the area size using integers
    
    public void setMapSize(int width, int height)
    {
        
        metresWidth = width;
        metresHeight = height;
        
        scale = (double)CONST.ANIM_W / (double)metresWidth;
        
    }
    
    
    // set the area size by taking the string from
    // the area size combo box
    
    public void setMapSize(String size)
    {
        if (size.equals("125x125 metres"))
        {
            setMapSize(CONST.SML_MW, CONST.SML_MH);
            
            cfgAreaWidth = cfgAreaHeight = CONST.SML_MW;
        }
        else if (size.equals("250x250 metres"))
        {
            setMapSize(CONST.STD_MW, CONST.STD_MH);
            
            cfgAreaWidth = cfgAreaHeight = CONST.STD_MW;
        }
        else if (size.equals("500x500 metres"))
        {
            setMapSize(CONST.LRG_MW, CONST.LRG_MH);
            
            cfgAreaWidth = cfgAreaHeight = CONST.LRG_MW;
        }
        else if (size.equals("1000x1000 metres"))
        {
            setMapSize(CONST.HGE_MW, CONST.HGE_MH);
            
            cfgAreaWidth = cfgAreaHeight = CONST.HGE_MW;
        }
    }
    
    public void setTimer()
    {
        // set the timer with an interval of 40
        // 1000/40 = 25, therefore we want a frame rate
        // of 25/sec ideally
        
        t = new Timer(CONST.RATE, new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) 
            {
                // when this timer is called, simply run the animation
                runAnim();
            }
        });
    }
    
    public boolean threshholdsReached()
    {
        for (int i = 0; i < numNodes; i++)
        {
            if (nodes[i].getMsgStatus() == true && nodes[i].isAtLimit() == false)
            {
                return false;
            }
        }
        
        return true;
    }
    
    public void setupComponents()
    {
        JFrame.setDefaultLookAndFeelDecorated(true);

        frame = new JFrame("MANET/A - 28/04/05");
        frame.setLayout(new GridBagLayout());
        GridBagConstraints frameC = new GridBagConstraints();
        
        
        leftPanel = new JPanel(new GridBagLayout());
        GridBagConstraints leftC = new GridBagConstraints();
        
        guiPanel = new JPanel(new GridLayout(2, 4));
        guiPanel.setPreferredSize(new Dimension(510, 80));
        guiPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), 
                     "Animation options"));
        
        graphicsPanel= new JPanel(new GridLayout(2, 1));
        
        
        outputPanel = new JPanel(new GridLayout(0, 1));
        outputPanel.setPreferredSize(new Dimension(280, 200));
        outputPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
        BorderFactory.createTitledBorder("Network Statistics")));
        
        
        indvNodePanel = new JPanel(new GridLayout(0, 1));
        indvNodePanel.setPreferredSize(new Dimension(280, 200));
        indvNodePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
        BorderFactory.createTitledBorder("Node Tracking Details")));
        
        keyPanel = new JPanel(new GridLayout(0, 1));
        keyPanel.setPreferredSize(new Dimension(280, 200));
        keyPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
        BorderFactory.createTitledBorder("Key")));
        
        textOutputPanel = new JPanel();
        textOutputPanel.setPreferredSize(new Dimension(280, 280));
        
        rightPanel = new JPanel(new GridLayout(3, 3));
        rightPanel.setPreferredSize(new Dimension(300, 600));
        rightPanel.setMaximumSize(new Dimension(300, 600));
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                      BorderFactory.createRaisedBevelBorder(),
                      BorderFactory.createLoweredBevelBorder()));
        
        
        startButton = new JButton("start");
        startButton.setPreferredSize(new Dimension(80, 25));
        startButton.setMaximumSize(new Dimension(80, 25));
        startButton.setActionCommand("start");
        startButton.addActionListener(this);
                
        stopButton = new JButton("reset");
        stopButton.setPreferredSize(new Dimension(80, 25)); 
        stopButton.setMaximumSize(new Dimension(80, 25));   
        stopButton.setActionCommand("stop");
        stopButton.addActionListener(this);               

                
        
        String [] mapChoices = {"125x125 metres",
                                "250x250 metres",
                                "500x500 metres",
                                "1000x1000 metres"};
                                
        mapSize = new JComboBox(mapChoices);
        mapSize.setEditable(false);
        mapSize.addActionListener(this);
        
        if (cfgAreaWidth == CONST.SML_MW)
        {
            mapSize.setSelectedIndex(0);
        }
        else if (cfgAreaWidth == CONST.STD_MW)
        {
            mapSize.setSelectedIndex(1);
        }
        else if (cfgAreaWidth == CONST.LRG_MW)
        {
            mapSize.setSelectedIndex(2);
        }
        else if (cfgAreaWidth == CONST.HGE_MW)
        {
            mapSize.setSelectedIndex(3);
        }
        
        
        msgsBroadcastLabel      = new JLabel(" messages broadcast: 0");
        msgsRecievedLabel       = new JLabel(" nodes with message: 0");
        networkEfficiencyLabel  = new JLabel(" network efficiency: N/A");
        networkCoverageLabel    = new JLabel(" network coverage: 0%");
                               
        numNodeSpinner = new JSpinner(new SpinnerNumberModel(cfgNumNodes, //initial value
                               CONST.MIN_NUM_NODES, //min
                               CONST.MAX_NUM_NODES, //max
                               1));
                               
        threshholdSpinner = new JSpinner(new SpinnerNumberModel(cfgThreshhold,
                                                                CONST.MIN_THRESHHOLD,
                                                                CONST.MAX_THRESHHOLD,
                                                                1));   
        
        animPanel = new JPanelAnim(CONST.ANIM_W, CONST.ANIM_H);
        animPanel.addMouseListener(this);
        
        buildMenu();
        
        guiPanel.add(startButton);
        JLabel text = new JLabel("");
        guiPanel.add(text);
        text = new JLabel("      area size");
        guiPanel.add(text);     
        text = new JLabel("  msg threshold");
        guiPanel.add(text);     
        text = new JLabel("       no. nodes");
        guiPanel.add(text); 
        
        guiPanel.add(stopButton);
        text = new JLabel("");
        guiPanel.add(text); 
        guiPanel.add(mapSize);
        guiPanel.add(threshholdSpinner);
        guiPanel.add(numNodeSpinner);
        
        
                      
        
        outputPanel.add(msgsBroadcastLabel); 
        outputPanel.add(msgsRecievedLabel); 
        outputPanel.add(networkEfficiencyLabel); 
        outputPanel.add(networkCoverageLabel);            
                      
        
        leftC.gridx = 0;
        leftC.gridy = 0;
        leftPanel.add(guiPanel, leftC);
        leftC.gridx = 0;
        leftC.gridy = 1;
        leftPanel.add(animPanel, leftC);
        
        nodeIDLabel = new JLabel(" Node ID: N/A");
        nodeMsgsBroadcastLabel = new JLabel(" messages broadcast: N/A");
        nodeNeighboursLabel = new JLabel(" neighbours: N/A");
        nodeMsgsVelocityXY = new JLabel(" velocity (m/sec): N/A");
        nodeRadiusLabel = new JLabel(" radius (m): N/A");
        
        indvNodePanel.add(nodeIDLabel);
        indvNodePanel.add(nodeMsgsBroadcastLabel);
        indvNodePanel.add(nodeNeighboursLabel);
        indvNodePanel.add(nodeMsgsVelocityXY);
        indvNodePanel.add(nodeRadiusLabel);
        
        text = new JLabel("<html><font color=#ff0000>RED</font> nodes have not " +
                    "received the message");
        keyPanel.add(text);
        text = new JLabel("<html><font color=#0000ff>BLUE</font> nodes have received " +
                    "the message");
        keyPanel.add(text);
        text = new JLabel("<html><font color=#aaaaaa>WHITE</font> nodes have reached " +
                    "their transmission threshhold");
        keyPanel.add(text);
        text = new JLabel("<html><font color=#00ff00>GREEN</font> lines indicate " +
                    "bidirectional communication links");
        keyPanel.add(text);
        text = new JLabel("<html><font color=#ff8800>ORANGE</font> lines indicate " +
                          "monodirectional communication links");
        keyPanel.add(text);
        
        
        rightPanel.add(indvNodePanel);
        rightPanel.add(outputPanel);
        rightPanel.add(keyPanel);
        
        
        frameC.gridx = 0;
        frameC.gridy = 0;
        frame.add(leftPanel, frameC);
        frameC.gridx = 1;
        frameC.gridy = 0;
        frame.add(rightPanel, frameC);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.pack();
        
    
        frame.setLocationRelativeTo(null);
	}
}