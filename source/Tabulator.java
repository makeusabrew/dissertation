import java.io.*;

class Data
{
    private String type="null";
    private Object [] dataSeq;
    private int seqLength;
    
    private int next;
    
    public Data(String t)
    {
        type = t;
        seqLength = 0;
        dataSeq = new Object[10000];
    }
    
    public void setType(String t)
    {
        type = t;
        next = 0;
    }
    
    public void add(int data)
    {
        if (type.equals("int"))
        {
        
            dataSeq[seqLength++] = new Integer(data);
        }
    }
    
    public void add(double data)
    {
        if (type.equals("double"))
        {
        
            dataSeq[seqLength++] = new Double(data);
        }
    }
    
    public void add(String data)
    {
        if (type.equals("String"))
        {
            dataSeq[seqLength++] = new String(data);
        }
    }
    
    public void printSeq()
    {
        for (int i = 0; i < seqLength; i++)
        {
            System.out.println(dataSeq[i]);
        }
    }
    
    public int getLength()
    {
        return seqLength;
    }
    
    public void setLength(int l)
    {
        seqLength = l;
    }   
    
    public Object getItemInSeq(int i)
    {
        if (i < seqLength)
        {
            return dataSeq[i];
        }
        
        return null;
    }
}

public class Tabulator
{
    private String title;  // the title of this tabulator
    
    private int poolingInterval=0;
    private int poolTime=0;
    
    private int numDataRecords=0;
    
    private String [] dataTitles;
    
    private Data [] data;
    
    private BufferedWriter out;
    
    public Tabulator(String t)
    {
        title = t;
        
        poolingInterval = 120;  // milliseconds
        numDataRecords = 0;
        
        dataTitles = new String[50];
        data = new Data[50];
    }
    
    public void clearAllData()
    {
        for (int i = 0; i < numDataRecords; i++)  // go through all existing records
        {
            data[i].setLength(0);                 // wipe them
        }
        
        numDataRecords = 0;                       // clear out our records too
    }
    
    
    public void addDataRecord(String s, String t)
    {
        for (int i = 0; i < numDataRecords; i++)
        {
            if (dataTitles[i].equals(s))
            {
                // already exists, no duplicate titles!
                System.out.println("addDataRecord() - duplicate title attempt!");
                return;
            }
        }
        
        dataTitles[numDataRecords]  = new String(s);
        data[numDataRecords]        = new Data(t);
        
        numDataRecords ++;
    }

    
    public void setPoolingInterval(int mSecs)
    {
        poolingInterval = mSecs;
    }
    
    public boolean checkForPool()  // also ticks interval
    {
        poolTime -= CONST.RATE;
        
        if (poolTime <= 0)
        {
            poolTime = poolingInterval;
            return true;
        }
        
        return false;
    }
    
    public void addData(int d, String dataTitle)
    {
        boolean foundTitle=false;
        
        for (int i = 0; i < numDataRecords; i++)
        {
            if (dataTitles[i].equals(dataTitle))  // this is the data title we want
            {
                foundTitle = true;
                data[i].add(d);
            }
        }
        
        if (!foundTitle)  // couldn't find the title
        {
            if (CONST.STRICT)  // strict mode?
            {
                // well this is very sloppy so don't allow it
                System.out.println("STRICT mode: Tabulator.addData() didn't find "+
                                   "requested title - have you explicitly created it?\n"+
                                   "use addDataRecord()");
            }
            else
            {
                // sloppy, but ok, add a new title of this name
                addDataRecord(dataTitle, "int");
                
                addData(d, dataTitle);  // well, let's add it properly!
            }
        }
    }
    
    public void addData(double d, String dataTitle)
    {
        boolean foundTitle=false;
        
        for (int i = 0; i < numDataRecords; i++)
        {
            if (dataTitles[i].equals(dataTitle))  // this is the data title we want
            {
                foundTitle = true;
                data[i].add(d);
            }
        }
        
        if (!foundTitle)  // couldn't find the title
        {
            if (CONST.STRICT)  // strict mode?
            {
                // well this is very sloppy so don't allow it
                System.out.println("STRICT mode: Tabulator.addData() didn't find "+
                                   "requested title - have you explicitly created it?\n"+
                                   "use addDataRecord()");
            }
            else
            {
                // sloppy, but ok, add a new title of this name
                addDataRecord(dataTitle, "double");
                
                addData(d, dataTitle);  // well, let's add it properly!
            }
        }
    }
    
    public void printData(String dataTitle)
    {
        for (int i = 0; i < numDataRecords; i++)
        {
            if (dataTitles[i].equals(dataTitle))  // this is the data title we want
            {
                data[i].printSeq();
            }
        }
    }
        
    public void tabulateData(String [] columns, String file)
    {
        if (columns == null)
        {
            System.out.println("tabulateData() - columns [] is null");
            return;
        }
        
        
        int [] recordIndex = new int[columns.length];
        
        // output the data in a file that we can then then excel
        try
        {
            BufferedWriter out = new BufferedWriter(new PrintWriter(file));
    
            
            
            
            for (int i = 0; i < columns.length; i++)
            {
                for (int j = 0; j < numDataRecords; j++)
                {
                    if (dataTitles[j].equals(columns[i]))  // this is the data title we want
                    {
                        recordIndex[i] = j;
                    }
                }
            }
            
            
            int seqLength = -1;
            for (int i = 0; i < columns.length; i++)
            {
                /*04/04 THIS ACTUALLY DOESNT MATTER
                 * SPREADSHEETS CAN COPE WITH COLUMNS HAVING 
                 * DIFFERENT AMOUNTS OF DATA, OBVIOUSLY!
                 * I THINK I INITIALLY PUT THIS SAFEGUARD
                 * IN BECAUSE I NEEDED TO ENSURE SOME KIND
                 * OF DATA CONSISTENCY.
                 * DONT THINK THATS NECESSARY, OR MAYBE BUT 
                 * NOT HERE!
                 *
                 
                if (!(data[recordIndex[0]].getLength() == data[recordIndex[i]].getLength()))
                {
                    System.out.println("differing sequence lengths, oh dear!");
                    return;
                }
                
                */
                
                if (data[recordIndex[i]].getLength() > seqLength)
                {
                        seqLength = data[recordIndex[i]].getLength();
                }
            }
            
            // write out the column headers first
            
            for (int i = 0; i < columns.length; i++)
            {
                out.write(dataTitles[recordIndex[i]] + "\t");
            }
            
            out.write(CONST.N);
            
            // now loop through writing each item in the data
            // sequence for each column
            
            // we have to do this by making the outer loop the "row" index
            // of our output table
            // the inner loop then writes each column's particular object
            // for that row, if it has one (i.e. it is not null)
            
            for (int i = 0; i < seqLength; i++)
            {
                for (int j = 0; j < columns.length; j++)
                {
                    if (data[recordIndex[j]].getItemInSeq(i) != null)
                    {
                        out.write(data[recordIndex[j]].getItemInSeq(i) + "\t");
                    }
                    else
                    {
                        out.write("\t");
                    }
                }
                
                out.write(CONST.N);         
            }
            
            out.close();    
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
    }
}
