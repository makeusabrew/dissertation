import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.SwingUtilities;
import java.io.*;

public class Batch implements ActionListener, ItemListener
{
    private Node [] nodes;
    
    private JFrame frame;
    
    private JPanel mainPanel;
    
    private JLabel passesLabel;
    
    private JPanel cards;
    private JPanel card1;
    private JPanel card2;
    
    private JComboBox optionsBox;
    
    private JButton bStartButton;
    private JButton bStopButton;
    
    private JButton saveButton;
    
    private JTextArea log;
    
    private JMenuBar menuBar;
    
    private JMenu menu;
    
    private JMenuItem menuItem;
    
    
    private int numNodes;
    private int threshhold;
    
    private double elapsedTime=0.0;
    
    private int totalMsgs = 0;
    private int recvdMsgs = 0;
    
    private int     metresWidth;
    private int     metresHeight;
    
    private int loopNodes = 100;
    private int loopSize = 4;
    private int loopThresh = 25;
    
    private Tabulator gather;
    
    private double scale;
    
    private boolean running=false;
    
    private int     cfgBatchMode;
    private int     cfgRadius;
    private int     cfgBaseNumNodes;
    private int     cfgNodeIncr;
    private int     cfgNodeLoop;
    private int     cfgNodeSpeed;
    private int     cfgAreaWidth;
    private int     cfgAreaHeight;
    private int     cfgBaseThreshhold;
    private int     cfgThreshLoop;
    private int     cfgThreshIncr;
    
    private String  cfgSaveFile;
    
    public Batch()
    {
        loadConfig(CONST.BATCH_CONFIG_FILE);
        
        setMapSize(cfgAreaWidth, cfgAreaHeight);  // set the terrain size
        
        gather = new Tabulator("data pool gatherer");
        
        JFrame.setDefaultLookAndFeelDecorated(true);

        frame = new JFrame("MANET/B - 28/04/05");
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
        mainPanel = new JPanel(new GridLayout(0, 1));
        
        log = new JTextArea(10,50);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);
        logScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        card1 = new JPanel(new GridLayout(0, 3));
        
        bStartButton = new JButton("start batch");
        bStartButton.setActionCommand("start batch");
        bStartButton.addActionListener(this);
        bStartButton.setMaximumSize(new Dimension(100, 50));
        
        bStopButton  = new JButton("stop batch");
        bStopButton.setActionCommand("stop batch");
        bStopButton.addActionListener(this);
        bStopButton.setMaximumSize(new Dimension(100, 50));
        
        
        card1.add(new JLabel(""));
        card1.add(new JLabel(""));
        card1.add(new JLabel(""));
        card1.add(new JLabel(""));
        card1.add(new JLabel(""));
        card1.add(new JLabel(""));
        card1.add(bStartButton);
        card1.add(new JLabel(""));
        card1.add(bStopButton);
        

        
        mainPanel.add(card1);

        mainPanel.add(logScrollPane);
    
        
        frame.add(mainPanel);
        
        buildMenu();
        
        frame.pack();
    
        frame.setLocationRelativeTo(null);
        
        frame.setVisible(true);
        
        log.append("loaded batch variants & constants from batch.cfg"+ CONST.N);
        log.append("batch mode: " + cfgBatchMode + CONST.N);
        if (cfgBatchMode == 1)
        {
            log.append("variants - no. of nodes" + CONST.N);
            log.append("constants - node speed: " + cfgNodeSpeed + "m/sec,  node radius: " + cfgRadius +
                       ", threshhold: " + cfgBaseThreshhold + ", area size: " + metresWidth + "*" + metresHeight +
                       "m" + CONST.N);
        }
        else if (cfgBatchMode == 2)
        {
            log.append("variants - threshhold" + CONST.N);
            log.append("constants - node speed: " + cfgNodeSpeed + "m/sec,  node radius: " + cfgRadius +
                       ", no. of nodes: " + cfgBaseNumNodes + ", area size: " + metresWidth + "*" + metresHeight +
                       "m" + CONST.N);
        }
        else if (cfgBatchMode == 3)
        {
            log.append("variants - area size" + CONST.N);
            log.append("constants - node speed: " + cfgNodeSpeed + "m/sec,  node radius: " + cfgRadius +
                       ", no. of nodes: " + cfgBaseNumNodes + ", threshhold: " + cfgBaseThreshhold + CONST.N);
        }
        else if (cfgBatchMode == 4)
        {
            log.append("variants - no. of nodes, threshold" + CONST.N);
            log.append("constants - node speed: " + cfgNodeSpeed + "m/sec,  node radius: " + cfgRadius +
                       ", area size: " + metresWidth + "*" + metresHeight + "m" + CONST.N);
        }
        
        log.append("ready..." + CONST.N);
        
    
        log.setCaretPosition(log.getDocument().getLength());
        
    }
    
    public void actionPerformed(ActionEvent e)
    {
        if ("start batch".equals(e.getActionCommand()))
        {
            if (!running)
            {
                running = true;
                //runSim();
                
                final SwingWorker worker = new SwingWorker() 
                {
                    public Object construct() 
                    {
                        //...code that might take a while to execute is here...
                        runSim();
                        return null;
                    }
                };
                
                worker.start();  //required for SwingWorker 3
            }
        }
        else if ("stop batch".equals(e.getActionCommand()))
        {
            if (running)
            {
                running = false;
                
            }
        }
        else if ("About".equals(e.getActionCommand()))
        {
            JOptionPane.showMessageDialog(frame,
            "Batch Simulation of a MANET using different scenarios" + CONST.N + CONST.N +
            "WARNING: This process will hog alot of CPU until it has finished!");
        }
    }
    
    public void buildMenu()
    {
        menuBar = new JMenuBar();
        
        menu = new JMenu("Menu");
        
        menuItem = new JMenuItem("About");
        menuItem.setActionCommand("About");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        menuItem = new JMenuItem("Quit");
        menuItem.setActionCommand("Quit");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        menuBar.add(menu);
        
        frame.setJMenuBar(menuBar);
    }
    
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
    
    public void itemStateChanged(ItemEvent e) 
    {
        CardLayout cl = (CardLayout)(cards.getLayout());
        cl.show(cards, (String)e.getItem());
    }
    
    public void runBatch(int mode)
    {
        if (mode == 1)
        {
            //
            numNodes = cfgBaseNumNodes;
            threshhold = cfgBaseThreshhold;
            
            log.append("Starting batch" + CONST.N);
            log.setCaretPosition(log.getDocument().getLength());
            
            gather.clearAllData();
            
            for (int j = 0; j < cfgNodeLoop; j ++)
            {           
                setMapSize(cfgAreaWidth, cfgAreaHeight);
                initNodes(numNodes, threshhold);
                elapsedTime = 0;            
                log.append("" + numNodes + " nodes, " + metresWidth + "*" +metresHeight + " metres, threshhold: " + threshhold + "" + CONST.N);
                log.setCaretPosition(log.getDocument().getLength());
                
                long start = System.currentTimeMillis();
                
                while (running) // run the sim
                {
                    elapsedTime += CONST.RATE;//(int)1000/fps;
                                                
                    runLogic();
                    
                    double pc = 0;
            
                    if (totalMsgs > 0)
                    {
                        pc = (double)recvdMsgs / (double)totalMsgs;
                    }
                    pc *= 100;
                    
                    
                    if (checkCoverage() == 100 || threshholdsReached())
                    {
                        gather.addData(numNodes, "no. of nodes");
                        gather.addData(threshhold, "msg threshhold");
                        gather.addData(metresWidth, "area size (m^2)");
                        gather.addData(checkCoverage(), "network coverage (%)");
                        gather.addData(pc, "efficiency");
                        gather.addData((int)(elapsedTime/1000), "total time");
                        break;  // out of the while loop 
                    }
                }
                
                long end = System.currentTimeMillis() - start;
                
                
                numNodes += cfgNodeIncr;
                if (numNodes > 2000)
                {
                    log.append("no. of nodes has exceeded maximum allowed  batch value (2000), aborting loop..." +CONST.N);
                    break;
                }
            }
            
            log.append("Done!" +CONST.N);
            log.append("Saving data to: " + cfgSaveFile);
            log.setCaretPosition(log.getDocument().getLength());
            
            String [] columns = {"no. of nodes", "msg threshhold", "area size (m^2)", "network coverage (%)", "efficiency", "total time"};                              
            gather.tabulateData(columns, cfgSaveFile);
        }
        else if (mode == 2)
        {
            //
            numNodes = cfgBaseNumNodes;
            threshhold = cfgBaseThreshhold;
            loopThresh = cfgThreshLoop;
            
            log.append("Starting batch" + CONST.N);
            log.setCaretPosition(log.getDocument().getLength());
            
            gather.clearAllData();
            
            for (int j = 0; j < cfgThreshLoop; j ++)
            {           
                setMapSize(cfgAreaWidth, cfgAreaHeight);
                initNodes(numNodes, threshhold);
                elapsedTime = 0;            
                log.append("" + numNodes + " nodes, " + metresWidth + "*" +metresHeight + " metres, threshhold: " + threshhold + "" + CONST.N);
                log.setCaretPosition(log.getDocument().getLength());
                
                long start = System.currentTimeMillis();
                
                while (running) // run the sim
                {
                    elapsedTime += CONST.RATE;//(int)1000/fps;
                                                
                    runLogic();
                    
                    double pc = 0;
            
                    if (totalMsgs > 0)
                    {
                        pc = (double)recvdMsgs / (double)totalMsgs;
                    }
                    pc *= 100;
                    
                    
                    if (checkCoverage() == 100 || threshholdsReached())
                    {
                        gather.addData(numNodes, "no. of nodes");
                        gather.addData(threshhold, "msg threshhold");
                        gather.addData(metresWidth, "area size (m^2)");
                        gather.addData(checkCoverage(), "network coverage (%)");
                        gather.addData(pc, "efficiency");
                        gather.addData((int)(elapsedTime/1000), "total time");
                        break;  // out of the while loop 
                    }
                }
                
                long end = System.currentTimeMillis() - start;
                
                
                threshhold += cfgThreshIncr;
                if (threshhold > CONST.MAX_THRESHHOLD)
                {
                    log.append("threshhold has exceeded maximum allowed value, aborting loop..." +CONST.N);
                    break;
                }
            }
            
            log.append("Done!" +CONST.N);
            log.append("Saving data to: " + cfgSaveFile);
            log.setCaretPosition(log.getDocument().getLength());
            
            String [] columns = {"no. of nodes", "msg threshhold", "area size (m^2)", "network coverage (%)", "efficiency", "total time"};                              
            gather.tabulateData(columns, cfgSaveFile);
        }
        else if (mode == 3)
        {
            //
            numNodes = cfgBaseNumNodes;
            threshhold = cfgBaseThreshhold;
            
            log.append("Starting batch" + CONST.N);
            log.setCaretPosition(log.getDocument().getLength());
            
            gather.clearAllData();
            
            for (int j = 0; j < 4; j ++)
            {           
                setMapSize(j);
                initNodes(numNodes, threshhold);
                elapsedTime = 0;            
                log.append("" + numNodes + " nodes, " + metresWidth + "*" +metresHeight + " metres, threshhold: " + threshhold + "" + CONST.N);
                log.setCaretPosition(log.getDocument().getLength());
                
                long start = System.currentTimeMillis();
                
                while (running) // run the sim
                {
                    elapsedTime += CONST.RATE;//(int)1000/fps;
                                                
                    runLogic();
                    
                    double pc = 0;
            
                    if (totalMsgs > 0)
                    {
                        pc = (double)recvdMsgs / (double)totalMsgs;
                    }
                    pc *= 100;
                    
                    
                    if (checkCoverage() == 100 || threshholdsReached())
                    {
                        gather.addData(numNodes, "no. of nodes");
                        gather.addData(threshhold, "msg threshhold");
                        gather.addData(metresWidth, "area size (m^2)");
                        gather.addData(checkCoverage(), "network coverage (%)");
                        gather.addData(pc, "efficiency");
                        gather.addData((int)(elapsedTime/1000), "total time");
                        break;  // out of the while loop 
                    }
                }
                
                long end = System.currentTimeMillis() - start;
                
                
            }
            
            log.append("Done!" +CONST.N);
            log.append("Saving data to: " + cfgSaveFile);
            log.setCaretPosition(log.getDocument().getLength());
            
            String [] columns = {"no. of nodes", "msg threshhold", "area size (m^2)", "network coverage (%)", "efficiency", "total time"};                              
            gather.tabulateData(columns, cfgSaveFile);
        }
        else if (mode == 4)
        {
            numNodes = cfgBaseNumNodes;
            threshhold = cfgBaseThreshhold;
            log.append("Starting batch" + CONST.N);
            log.setCaretPosition(log.getDocument().getLength());
            
            
            gather.clearAllData();
            
            for (int j = 0; j < cfgNodeLoop; j++)
            {   
                        
                threshhold = cfgBaseThreshhold;
                
                for (int k = 0; k < cfgThreshLoop; k ++)
                {
                    setMapSize(cfgAreaWidth, cfgAreaHeight);
                    initNodes(numNodes, threshhold);
                    elapsedTime = 0;                    
                    log.append("" + numNodes + " nodes, " + metresWidth + "*" +metresHeight + " metres, threshhold: " + threshhold + "" + CONST.N);
                    log.setCaretPosition(log.getDocument().getLength());
                    
                    long start = System.currentTimeMillis();
                    
                    while (running) // run the anim 
                    {
                        elapsedTime += CONST.RATE;//(int)1000/fps;
                                
                        runLogic();
                        
                        double pc = 0;
                
                        if (totalMsgs > 0)
                        {
                            pc = (double)recvdMsgs / (double)totalMsgs;
                        }
                        pc *= 100;
                        
                        
                        
                        if (checkCoverage() == 100 || threshholdsReached())
                        {
                            gather.addData(numNodes, "no. of nodes");
                            gather.addData(threshhold, "msg threshhold");
                            gather.addData(metresWidth, "area size (m^2)");
                            gather.addData(checkCoverage(), "network coverage (%)");
                            gather.addData(pc, "efficiency");
                            gather.addData((int)(elapsedTime/1000), "total time");
                           break;  // out of the while loop 
                        }
                    }
                    
                    long end = System.currentTimeMillis() - start;
                    
                    threshhold += cfgThreshIncr;
                    if (threshhold > CONST.MAX_THRESHHOLD)
                    {
                        log.append("threshhold has exceeded maximum allowed value, aborting loop..." +CONST.N);
                        break;
                    }
                    
                }
                
                numNodes += cfgNodeIncr;
                if (numNodes > 2000)
                {
                    log.append("no. of nodes has exceeded maximum allowed  batch value (2000), aborting loop..." +CONST.N);
                    break;
                }
                
            }
            
            log.append("Done!" +CONST.N);
            log.append("Saving data to: " + cfgSaveFile);
            log.setCaretPosition(log.getDocument().getLength());
            
            String [] columns = {"no. of nodes", "msg threshhold", "area size (m^2)", "network coverage (%)", "efficiency", "total time"};                              
            gather.tabulateData(columns, cfgSaveFile);
        }
    }


    
    public void runSim()
    {
        runBatch(cfgBatchMode);
    }
    
    public void setMapSize(int width, int height)
    {
        
        metresWidth = width;
        metresHeight = height;
        
        scale = (double)CONST.ANIM_W / (double)metresWidth;
        
    }
    
    public void setMapSize(int index)
    {
        if (index == 0)
        {
            metresWidth = CONST.SML_MW;
            metresHeight = CONST.SML_MH;
        }
        else if (index == 1)
        {
            metresWidth = CONST.STD_MW;
            metresHeight = CONST.STD_MH;
        }
        else if (index == 2)
        {
            metresWidth = CONST.LRG_MW;
            metresHeight = CONST.LRG_MH;
        }
        else if (index == 3)
        {
            metresWidth = CONST.HGE_MW;
            metresHeight = CONST.HGE_MH;
        }
        
        scale = (double)CONST.ANIM_W / (double)metresWidth;
        
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
        threshhold = t;
        
        nodes = new Node[numNodes];
        
        for (int i = 0; i < numNodes; i++)
        {
            nodes[i] = new Node((Math.random()*metresWidth), 
                                (Math.random()*metresHeight), 
                                cfgRadius, i, n, t);
            
            nodes[i].setVelocity(cfgNodeSpeed);
            nodes[i].setAngleInDegrees(Math.random()*360);
            
            nodes[i].setScale(scale);
        }
        
        nodes[0].setMsgStatus(true);
        
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
            boolean batchMode = false;
            boolean nodeIncr = false;
            boolean nodeLoop = false;
            boolean threshIncr = false;
            boolean threshLoop = false;
            boolean saveFile = false;
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
                
                if (split[0].equals("BATCHMODE"))
                {
                    batchMode = true;
                    
                    if (split.length != 2)  // wrong!
                    {                       
                        cfgBatchMode = 1;
                    }
                    else
                    {
                        cfgBatchMode = Integer.parseInt(split[1]);
                        if (cfgBatchMode < 1)
                        {
                            cfgBatchMode = 1;
                        }
                    }
                }
                else if (split[0].equals("NODERADIUS"))
                {
                    radius = true;
                    
                    if (split.length != 2)  // wrong!
                    {
                        cfgRadius = CONST.DEFAULT_NODE_RADIUS;
                    }
                    else
                    {
                        cfgRadius = Integer.parseInt(split[1]);
                        
                        if (cfgRadius > CONST.MAX_NODE_RADIUS ||
                            cfgRadius < CONST.MIN_NODE_RADIUS)
                        {
                            cfgRadius = CONST.DEFAULT_NODE_RADIUS;
                        }                       
                    }
                        
                }
                else if (split[0].equals("BASENUMNODES"))
                {
                    nodes = true;
                    
                    if (split.length != 2)  // wrong!
                    {                       
                        cfgBaseNumNodes = CONST.DEFAULT_NUM_NODES;
                    }
                    else
                    {
                        cfgBaseNumNodes = Integer.parseInt(split[1]);
                        
                        if (cfgBaseNumNodes < CONST.MIN_NUM_NODES ||
                            cfgBaseNumNodes > CONST.MAX_NUM_NODES)
                        {
                            cfgBaseNumNodes = CONST.DEFAULT_NUM_NODES;
                        }
                    }
                }
                else if (split[0].equals("NODEINCREMENT"))
                {
                    nodeIncr = true;
                    
                    if (split.length != 2)  // wrong!
                    {                       
                        cfgNodeIncr = 10;
                    }
                    else
                    {
                        cfgNodeIncr = Integer.parseInt(split[1]);
                        if (cfgNodeIncr < 0)
                        {
                            cfgNodeIncr = 0;
                        }
                    }
                }
                else if (split[0].equals("NODELOOP"))
                {
                    nodeLoop = true;
                    if (split.length != 2)  // wrong!
                    {                       
                        cfgNodeLoop = 1;
                    }
                    else
                    {
                        cfgNodeLoop = Integer.parseInt(split[1]);
                        if (cfgNodeLoop <= 0)
                        {
                            cfgNodeLoop = 1;
                        }                       
                    }
                }
                else if (split[0].equals("NODESPEED"))
                {
                    speed = true;
                    // NODESPEED [int]
                    
                    if (split.length != 2)  // wrong!
                    {                       
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
                else if (split[0].equals("BASETHRESHHOLD"))
                {
                    threshhold = true;
                    if (split.length != 2)  // wrong!
                    {                       
                        cfgBaseThreshhold = CONST.MIN_THRESHHOLD;
                    }
                    else
                    {
                        cfgBaseThreshhold = Integer.parseInt(split[1]);
                        
                        if (cfgBaseThreshhold < CONST.MIN_THRESHHOLD)
                        {
                            cfgBaseThreshhold = CONST.MIN_THRESHHOLD;
                        }
                        else if (cfgBaseThreshhold > CONST.MAX_THRESHHOLD)
                        {
                            cfgBaseThreshhold = CONST.MAX_THRESHHOLD;
                        }
                    }
                }
                else if (split[0].equals("THRESHINCREMENT"))
                {
                    threshIncr = true;
                    if (split.length != 2)  // wrong!
                    {                       
                        cfgThreshIncr = 1;
                    }
                    else
                    {
                        cfgThreshIncr = Integer.parseInt(split[1]);
                        if (cfgThreshIncr < 0)
                        {
                            cfgThreshIncr = 0;
                        }                       
                    }
                }
                else if (split[0].equals("THRESHLOOP"))
                {
                    threshLoop = true;
                    if (split.length != 2)  // wrong!
                    {                       
                        cfgThreshLoop = 1;
                    }
                    else
                    {
                        cfgThreshLoop = Integer.parseInt(split[1]);
                        if (cfgThreshLoop <= 0)
                        {
                            cfgThreshLoop = 1;
                        }                       
                    }
                }
                else if (split[0].equals("SAVEFILE"))
                {
                    saveFile = true;
                    if (split.length != 2)  // wrong!
                    {                       
                        cfgSaveFile = "default.txt";
                    }
                    else
                    {
                        cfgSaveFile = split[1];                     
                    }
                }
                
                
            }
            
            in.close();
        }
        catch (Exception e)
        {
            System.out.println("Couldn't load batch.cfg!\nCreating new file...");
            
            try
            {
                BufferedWriter out = new BufferedWriter(new FileWriter("batch.cfg"));
                
                out.write("# Nick's MANET A/B PROJECT config file for BATCH MODE" + CONST.N);
                out.write("# EDITING THIS CONFIGURATION FILE IS FOR ADVANCED USERS" + CONST.N);
                out.write("# IF YOU BREAK THE PROGRAM BY CHANGING VALUES IN HERE" + CONST.N);
                out.write("# DELETE THIS .CFG AND THE PROGRAM WILL GENERATE A NEW ONE" + CONST.N);
                out.write("#" + CONST.N);
                out.write("# ALL INFORMATION FOR THIS FILE AND WHAT IT DOES IS" + CONST.N);
                out.write("# CONTAINED IN THE USER MANUAL IN THE REPORT PROVIDED" + CONST.N);
                out.write("# WITH THIS PROGRAM" + CONST.N);
                out.write("" + CONST.N);
                out.write("BATCHMODE 1" + CONST.N);
                out.write("" + CONST.N);
                out.write("NODEINCREMENT 50" + CONST.N);
                out.write("" + CONST.N);
                out.write("THRESHINCREMENT 1" + CONST.N);
                out.write("" + CONST.N);
                out.write("NODELOOP 10" + CONST.N);
                out.write("" + CONST.N);
                out.write("THRESHLOOP 10" + CONST.N);
                out.write("" + CONST.N);
                out.write("SAVEFILE default.txt" + CONST.N);
                out.write("" + CONST.N);
                out.write("BASENUMNODES 50" + CONST.N);
                out.write("" + CONST.N);
                out.write("BASETHRESHHOLD 5" + CONST.N);
                out.write("" + CONST.N);
                out.write("NODERADIUS 30" + CONST.N);
                out.write("" + CONST.N);
                out.write("NODESPEED 15" + CONST.N);
                out.write("" + CONST.N);
                out.write("AREASIZE 250" + CONST.N);
                out.write("" + CONST.N);
                out.write("# END OF CONFIG" + CONST.N);
                
                
                out.close();
                
                loadConfig(CONST.BATCH_CONFIG_FILE);
            }
            catch (Exception ee)
            {
                System.out.println("Could not write to batch.cfg file. Is it in use?");
            }
        }
    }
        
}
