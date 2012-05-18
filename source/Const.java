/*
 * SIMPLY A WAY OF GROUPING GLOBAL CONSTANTS
 */
public class CONST
{
	static final int FPS = 25;
	static final int RATE = 1000/FPS;
	
	static final boolean STRICT=false;
	
	static final boolean DEBUG=true;
	
	static final String N = System.getProperty("line.separator");
	 
	static final int 	STD_MW = 250;
	static final int	STD_MH = 250;
	
	static final int	SML_MW = STD_MW/2;
	static final int	SML_MH = STD_MH/2;
	
	static final int	LRG_MW = STD_MW*2;
	static final int	LRG_MH = STD_MH*2;
	
	static final int	HGE_MW = STD_MW*4;
	static final int	HGE_MH = STD_MH*4;
	
	static final int 	ANIM_W = 500;
	static final int 	ANIM_H = 500;
	
	
	
	
	static final int NODE_RESTTIME = 1000;
	
	
	static final String ANIM_CONFIG_FILE = "anim.cfg";
	static final String BATCH_CONFIG_FILE = "batch.cfg";
	
	static final int MIN_NODE_RADIUS = 15;
	static final int MAX_NODE_RADIUS = 50;
	static final int DEFAULT_NODE_RADIUS = 30;
	
	static final int MIN_NODE_SPEED = 5;
	static final int MAX_NODE_SPEED = 25;
	static final int DEFAULT_NODE_SPEED = 15;
	
	static final int MIN_NUM_NODES = 2;
	static final int MAX_NUM_NODES = 5000;
	static final int DEFAULT_NUM_NODES = 50;
	
	static final int MIN_THRESHHOLD = 1;
	static final int MAX_THRESHHOLD = 20;
	static final int DEFAULT_THRESHHOLD = 5;
}
