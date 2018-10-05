import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.lang.String;
import java.nio.file.*;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Semaphore;

//main class
public class c3244203A2P3
{
    public static void main(String[] args) throws IOException
    {
        //Reads File from input
        //CAUSE I REMEMBERED THIS TIME

        //true false
        boolean success = false;    

        //setting variables for num of customers
        int cNum=0;

        //data from the file
        List<String> input = new LinkedList<String>();
        String token1 = "";
        List<String> id = new LinkedList<String>();
        List<Integer> duration  = new ArrayList<Integer>();

        //scanner saves next line file name into data
        Scanner data = new Scanner(System.in);
        System.out.println("Enter file name eg input.txt: ");

        //loops till it finds a valid file
        while (!success) 
        {
            //try catch for getting the correct file
            try 
            {
                //changes line into string
                String str = data.nextLine();
                //converts the string name of the file into an actual file 
                //that the code can read
                Scanner file1 = new Scanner(new File(str));


                //try catch for getting correct contents of the file
                try 
                {
                    //split by  into string array outside while loop
                    //split by  into string array outside while loop
                    while(file1.hasNext())
                    {
                        token1 = file1.next();
                        input.add(token1);
                        //System.out.println(token1);
                    }                  

                    //saves the amount of customers
                    cNum = Integer.parseInt(input.get(0));

                    //saves all the values into lists
                    for(int i = 1; i < input.size(); i+=2)
                    {

                        id.add(input.get(i));
                        duration.add(Integer.parseInt(input.get(i+1)));

                    }

                    //finishes the file reading
                    success = true;

                } 
                catch (NoSuchElementException e) 
                {
                    System.out.println("Empty file, or invalid contents!");
                }
                file1.close();
            } 
            catch (FileNotFoundException e) 
            { 
                System.out.println("File not found! Please give a valid file!");
            }
        }
        //close file
        data.close();

        //creating customers
        Customer[] c = new Customer[cNum];
        //creating 5 seats
        //
        //
        //Still not sure if i should make this into an array or not
        Timer timer1 = new Timer(cNum);
        Thread timeThread;
        timeThread = new Thread(timer1);
        timeThread.start();

        CoffeeMachine cm = new CoffeeMachine(timer1);
        Thread th;

        for(int i = 0; i<cNum;i++)
        {
            try 
            { 
                //helps threads arriving at the same time, first in first serve
                Thread.sleep(10); 
            } 
            catch (InterruptedException e) 
            { 
                // TODO Auto-generated catch block 
                e.printStackTrace(); 
            } 

            //creates the customer with values
            c[i] = new Customer(id.get(i), duration.get(i), i,cm, timer1);

            //makes customer into a thread
            th = new Thread(c[i]);
            th.start();
            //System.out.println("order"+c[i].getOrder());
        }
    }
    
}

class Customer implements Runnable
{

    //variables
    private Timer timer;
    private CoffeeMachine cm;
    private String id;
    private int duration;
    private ReentrantLock r;
    private boolean finished;
    private int order;
    private int counter;

    //constructor
    Customer(String id, int duration, int order, CoffeeMachine cm, Timer timer)
    {
        this.order = order;
        this.id = id;
        this.duration = duration;
        this.cm = cm;
        this.timer = timer;
        r = new ReentrantLock();
        finished = false;
        counter = 0;

    }

    //Setters
    public void cFinished()
    {
        finished = true;
    }

    //Getters
    public String getId()
    {
        return this.id;
    }
    public int getDuration()
    {
        return this.duration;
    }
    public int getOrder()
    {
        return this.order;
    }


    //reentrant lock, even if multiple threads have it, only
    //the first thread that gains access will be able to lock and have access
    //the other threads have to wait before they can do shit


    public synchronized void run()
    {

        System.out.println(this.getId()+" order:"+this.getOrder());

            if(order == counter)
            {
                r.lock();
                counter++; 
                r.unlock();

                System.out.println(this.getId());

                //if hot coffee is brewing and a cold coffee order comes in
                //lock the thread
                if(this.id.contains("H") && cm.getC()>0 || cm.getU()==3|| this.id.contains("C") && cm.getH()>0)
                {
                    r.lock();
                    while(finished != true)
                    {

                        if(this.id.contains("H") && cm.getC()==0 && cm.getU()<3||this.id.contains("C") && cm.getH()==0 && cm.getU()<3)
                        {
                            
                            cm.serveCoffee(this);
                            //System.out.println("unlocked");
                            

                        }
                        else
                        {
                            //System.out.println("waiting");
                            try 
                            { 
                                //just wait
                                Thread.sleep(1); 
                            } 
                            catch (InterruptedException e) 
                            { 
                                // TODO Auto-generated catch block 
                                e.printStackTrace(); 
                            } 
                        }
                            
                    }
                    r.unlock();

                }
                else
                {
                    cm.serveCoffee(this);
                    r.lock();
                    counter++; 
                    r.unlock();
                }
            }


            

                
    }
}

class CoffeeMachine
{

    //variables
    private Customer c;
    private Timer timer;
    private int hCounter;
    private int cCounter;
    private int mUsed;

    //constructor
    CoffeeMachine(Timer timer)
    {
        this.timer = timer;
        hCounter = 0;
        cCounter = 0;
        mUsed = 0;
    }

    //setters
    public void upH()
    {
        hCounter++;
    }
    public void upC()
    {
        cCounter++;
    }

    //getters
    public int getH()
    {
        return hCounter;
    }
    public int getC()
    {
        return cCounter;
    }
    public int getU() 
    {
        return mUsed;
    }

    //this is where the magic happens
    public void serveCoffee(Customer c)
    {
     
        
        //if hot up hot counter in coffee machine
        if(c.getId().contains("H"))
        {
            hCounter++;
        }
        //else cold counter ++
        else
        {
            cCounter++;
        }

        try 
        {
            mUsed++;

            //System.out.println("mUsed is :"+mUsed+" hCounter is :"+hCounter+" cCounter is :"+cCounter);
            System.out.println("("+timer.getClock()+") "+ c.getId()+ " uses dispenser " +mUsed + "(time: "+ c.getDuration()+")");

            //thread is brewing delicious coffee
            Thread.sleep(1000*c.getDuration());
            //System.out.println("did it");
        
        } 
        catch (Exception e) 
        {
            //TODO: handle exception
        }

        c.cFinished();

        //System.out.println(c.getId()+" has exited");
        if(c.getId().contains("H"))
        {
            hCounter--;
        }
        else
        {
            cCounter--;
        }
        mUsed--;
        timer.upExit();
        System.out.println(c.getId()+" used counter:"+mUsed + " hot counter:"+hCounter+" cold counter:" +cCounter);
        
        


    }
}

class Timer implements Runnable
{
    int customerNum;
    int clockCounter;
    int exitedCustomer;

    Timer(int customerNum)
    {
        clockCounter = 0;
        this.customerNum = customerNum;
        this.exitedCustomer = 0;
    }

    //Setters
    public void upExit()
    {
        exitedCustomer++;
    }

    //Getters
    public int getClock()
    {
        return clockCounter;
    }

    //not functional yet
    public void run()
    {
        //while customers still exist run
        while(exitedCustomer != customerNum) 
        { 
                //increases clock counter after a second
                //to allow more threads to try and access
                try 
                { 
                    //1second
                    Thread.sleep(1000); 
                    clockCounter++; 
                } 
                catch (InterruptedException e) 
                { 
                    // TODO Auto-generated catch block 
                    e.printStackTrace(); 
                } 
            
            
        }
        System.out.println("("+clockCounter+")  DONE");
    }
}
