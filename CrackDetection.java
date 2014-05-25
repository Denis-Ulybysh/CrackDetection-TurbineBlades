// uld May 16  2014
//Q1: what to do in case of file does not exist? Lock will wait forever in thread FFT, cause file never be 
// read.  Shall I kill 2-nd, 3-rd and 4-th threads? 
		
//OK = Q4: write to output file: what is the appropriate 
//class for writing real numbers? OutputStreamWriter  works 
//too slow, OutputStream does not work with double type
//Ans: don't write output file at all 
//
//Q5: if I create threads in main method once, then I will create ReadAudioData thread in scheduler every 
// 30 minutes. But what about other threads? How to make them sleep (not to die) after their run() method
// is done until next iteration when ReadAudioData thread wakes up by 
//scheduler after 30 mins? May be restart threads 2,3,4  after they ended so that they will wait 30 mins until 
//thread 1 wakes up and its run() method is called? 
//Ans: use while (1). Thread 1 will go to sleep for 30 mins in the end after it is done 

//Q8: implement real-time scheduler, while (1), check on Pi
//
//#
//Q9: how many threads do we need? Why do we need multiple threads at all? 
//
//Q10: Modify reading WAV: we need 2 channels, not only first channel (modify formula with bit shifts  >> )


//uld 24 Apr. 
//import javax.realtime.*;  works but creating realtime thread by 
//(new RealtimeThread(new ReadAudioData())).start();
//
//I get the following error
//Exception in thread "main" java.lang.NoClassDefFoundError: 
////com/fiji/fivm/r1/ThreadStartHook
//	at ReadAudioData.main(ReadAudioData.java:385)
//Caused by: java.lang.ClassNotFoundException: 
////com.fiji.fivm.r1.ThreadStartHook
//	at java.net.URLClassLoader$1.run(Unknown Source)


//uld 23 APr.
// 1) Remove Thread.currentThread..., Thread.currentThread.sleep

//uld 17 Apr. 
///1) uld 17 Apr. it should not be just sorted array, !!! implement looking for slopes
		    ///2) then sort it (or use selection algo) and choose first 20 points (or last 20)
    //use slopes (search for local maximums) instead of just sorting  
//
//1) the same for right part as for left
//2) rename to shorter names 
//
//4) read sample rate and num of channels from file,
//don't hardcode it 
//
//5) what is the class name to work with Date and Time in 
//
//Fiji? class Date is not working as usually
		
//uld 16 Apr. 
//1) remove %d from println in Wave.java
//2) Improve cycles here : don't go from 0 until i_datalength: scan only 
//interval (pumpingFreq-22; pumpingFreq+22). Or not 22 - see high level design

//uld 15 Apr.2014 
//Gustavo's reading WAV method will work only for stereo (2 channels) and 16 bits per sample.
//I.e. 32 bits (4 bytes) in every line of imaginary dat-file

//uld 09 Apr. 2014
//ToDo  09 Apr. 2014
//1. Scheduler: start threads 1 every 30 mins = it works for thread 1. repeat for thread 4
//2. do the same with wait() notify() between threads 2,3 
//3. Implement thread 4 to clean old data 
//4. Implement new functionality inside thread 3 (compare output dataset with a baseline dataset)
//5. Print running thread names and their states (to see when threads terminate ) 
//6. Commit. Diverge new project . Commit old project with correct threads but without scheduler
//7.1. Try one external class ReadAudioData and internal class - like it is done in OK example in 
// java - ScheduledExecutorService one thread many tasks - Stack Overflow.htm
//  7.2.   or try use Executors.newFixedThreadPool(1)  instead of  
//     Executors.newSingleThreadScheduledExecutor();
// 7.3.  or try in main () in the end:   SERVICE.shutdown();
//7.4. see service.isTerminated(); 
//7.7. read this: http://code.nomad-labs.com/2011/12/09/mother-fk-the-scheduledexecutorservice/


//File java - ScheduledExecutorService one thread many tasks - Stack Overflow.htm   says :
// Reading the Javadoc would be very helpful in this case. It does explain that though the executor will be 
//created with a single thread, it will be operating with an unbounded queue. This means that you can submit
//multiple tasks to the executor and they will be queued up to run one after the other, up to the maximum 
//bounds of the queue (which in this case is infinity) or until the JVM runs out of resources.
//    Creates an Executor that uses a single worker thread operating off an unbounded queue. (Note 
//however that if this single thread terminates due to a failure during execution prior to shutdown, a new 
//one will take its place if needed to execute subsequent tasks.) Tasks are guaranteed to execute 
//sequentially, and no more than one task will be active at any given time. Unlike the otherwise equivalent 
//newFixedThreadPool(1) the returned executor is guaranteed not to be reconfigurable to use additional 
//threads.

 
// Questions to the meeting: 
//1. We will compare output datasets with initial baseline dataset. But characteristics of blade might shift 
//with time, so we may always give a conclusion 'cracked' but the blade has just changed its characteristics
//and spectrum
//Possible Solution: to change initial baseline dataset i.e. every year 

//2. When thread starts running (every 30 mins) and FFT operation is in progress I see that CPU usage is
// increased for some short moment  up to 93% , and around 60MB of RAM is used.
//Currently, reading input data from file, apply FFT and do some crack detection analysis takes around
//9 seconds on my laptop. Is it OK?  

//3. How do I prevent memory leackage? Because threads are created periodically and then they terminate
//-probably some memory area is not released. It seems that during 48 iterations (1 day of turbine's work)
//available memory decreased on 70 MB. But later memory usage lloked OK. I checked in Task Manager 
// memory for javaw process and available physical memory
//Ans: read JavaThread\Scheduler html files, one of them, exactly this: 
//java - ScheduledExecutorService one thread many tasks - Stack Overflow.htm 
//said smth about Scheduler, queues, memory leak   
//Possible solution: check for memory available and if less than 10% left then cancel current scheduler
//and restart it again 


//uld 03 Apr. 2014
//1. use only 1 argument data1 to call ForwardRealToComplex. Reduce number of parameters
  //in constructors 
//OK = 2. Declare double magnitude in CrackDetection (i.e. as a global variable) = done - ? 
//OK = 3. Rename angleOut to aod_angleOut, add one more argument to the constructor  
//4. Don't always write results to file - write in array first and if crack is detected then write to file
//5. Build for android, run on android, =measure performance
//OK=6. Synchronized keyword - must run() methods be synchronized ?  
//Ans: No
//OK = 6.1. Do I need to make run methods  synchronized ? 
//Ans: DO NOT synchronize run ( ) method. Synchronize other methods

//6.2. How do I make thread to wait for results from another thread? 
//Ans:
//in Java you can call object.wait() with a timeout. 
//If you want to change the timeout from another thread, change some 'waitValue' variable and notify(). 
//The thread will then 'immediately' run and then wait again with the new timeout value.
		
//In your code that must wait for the dbThread, you must have something like this:
//
////do some work
//synchronized(objectYouNeedToLockOn){
//    while (!dbThread.isReady()){
//        objectYouNeedToLockOn.wait();
//    }
//}
////continue with work after dbThread is ready
//
//In your dbThread's method, you would need to do something like this:
//
////do db work
//synchronized(objectYouNeedToLockOn){
//    //set ready flag to true (so isReady returns true)
//    ready = true;
//    objectYouNeedToLockOn.notifyAll();
//}
//end thread run method here

//Or I can do in dbThread's method the following: 

//Use a CountDownLatch with a counter of 1.
//
//CountDownLatch latch = new CountDownLatch(1);
//
//Now in the app thread do-
//
//latch.await();
//
//In the db thread, after you are done, do -
//
//latch.countDown();
//

//6.3  Do I make threads as daemons (not to wait)   - ?   09 Apr.2014 
//Ans: no. threads 1,4  will start independently every 30 mins and terminate after method run () is done,
 // i.e. after it is proceeded until the end. Thread 2 waits until thread 1 is done and then terminates after
//its method run() is done. Thread 3 waits until thread 2 is done and then terminates after
//its method run() is done. I.e. thread 1 is created every 30 mins and starts every 30 min, read input data,
//creates thread 2, runs it and terminates itself. Thus thread 2 is created every 30 mins, applies FFT, 
//creates thread 3, runs it and terminates itself. Thus thread 3 is created every 30 mins, does crack
//detection analysis based on statistical comparison of historical output datasets, makes a conclusion 
// about is blade ctacked or not and terminates itself. 
//Thread 4 is created every 30 mins, it starts itself, checks whether there are some old files to be deleted
//(if SD card spaces became smaller than e.g. 500 MB), deletes old output files if necessary and then 
//terminates itself. Thus, all 4 threads don't exist forever. They appear every 30 mins in order 1->2->3,  4
// do their functionality and terminate when method run() is over

// Should I change the state of thread at the end of all operations to WAITED or BLOCKED ? 
//Ans: it could be useful to print state of thread 
// How do threads exchange signals ? 
//Ans: currently we only need methods wait() and notify(), notifyAll().

//7.  How do I make thread awake every 30 mins?
//Answer: 
//	ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
//	exec.scheduleAtFixedRate(new Runnable() {
//	  @Override
//	  public void run() {
//	    // do stuff
//	  }
//	}, 0, 1800, TimeUnit.SECONDS);  //1800 seconds = 30 mins 

//OK = 7.2. May be if thread 1 works every 30 mins then thread 2 will work every 31 min, thread 2 every 32 mins - 
//to give time to thread 1 to complete reading, to give time to thread 2 to complete FFT
//Ans: use method wait ()   from 6.2. Threads 1,4 are independent and run every 30 mins. After thread 1
//is done - thread 2 prpceeds (method wait() is used in thread 2 and in thread 1 in the synchronized 
//method getData method notifyAll() is called and conditional variable b_t1_isReady is assigned to true
//	may be for thread 4 it is OK to start every 2 hours (or once a day) in order not to slow down performance 
	
//8. Currently I read input data from file. Should I remove previous input file and read again after
//30 mins from a new file with the same name? 
//But later we will read them directly from sound card. 
//9. Which methods have to be synchronized? None 2 diff. threads access same object. But: 
//9.1.  data from file   (or sound card) must be read until the end and only after that start aplying FFT
// 9.2. Start crack detection algo only after FFT is done (for the same dataset. Theoretically, FFT can 
//already handle a new dataset while crack detection is still working with previous dataset
//9.3. Write output result to file only after CrackDetection finished processing the FFT dataset

//10. only threads 1 and 4 are periodic. thread 2 creates in main () and then waits until t1 is done, i.e. when 
//it raises b_t1Ready to true. t3 waits until t2 raises b_t2Ready to true.abstract

//11. Figure out which object is locked in FFTForward and how to call wait(). 
//We will use conditional variables. Ethan said wait() and notify () architecture
//Ans: OK, it is done 09 Apr.2014 

//I would really recommend that you go through a tutorial like 
//Sun's Java Concurrency before you commence in the magical world of multithreading.

//12. Thread ForwardFFT (t2) will start every 30 mins when t1 starts then after completing its work it will be 
// terminated and after 30 minus minus some minutes it will be created again 

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Executors;


public class CrackDetection implements Runnable {

	//OK release 3 sec private final static int samplingRate = 44100;
	
	//Scott's data collected by RPi
	private final static int samplingRate = 44100;
	//OK 16 Apr. private final static int samplingRate = 44100;
	// 16 Apr. it must be now read from WAV-file, not hardcoded here 
	
	
	//OK Release 07 Nov.: for Sungmin's data Sampling Rate is 96 kHz
	//OK 16 Apr. 2014 for Sungmins data private final static int samplingRate = 96000;
	
	//scan for pumping frequency within interval 0..500 Hz. In our dataset it is 9.6 Hz and it can not
    //change significantly
    
	///OK was working private final static double dPumpingFreqRightBorder  = 500.0;
	///Gustavo said rotational frequency of the blade can not be greater than 6 Hz
	//OK release 07 Nov. private final static double dPumpingFreqRightBorder  = 6.0;
	private final static double dPumpingFreqRightBorder  = 16.0;
	
	//17 Apr. 2014: we will search for peak energies in interval 
	//[ dProbingFrequencyFound - dProbingFreqSearchInterval  ; dProbingFrequencyFound - dFreqIntervalExcludedAroundProbing]
    // [dProbingFrequencyFound +dFreqIntervalExcludedAroundProbing;  dProbingFrequencyFound + dProbingFreqSearchInterval ]
    // for 20 points with highest energy
	private final static double dFreqIntervalExcludedAroundProbing  = 1;	// release 1.0;
	private final static double dFreqIntervalEnergySearch =  20 ; //release 20;  //20 Hz

	 //Array magnitude1 will contain FFT output data, i.e. the magnitude spectral information 
	  // from the spectral analysis process, i.e. what we need 
	// OK May 22 double[] magnitude;
	
	// OK May 22 double[] aod_angleOut;  //will be filled in method run () {constructor} below
	double[] [] aod_angleOut;  //will be filled in method run () {constructor} below
 
	///uld OK 17 Apr. 2014: false is normal mode, we don't write to output file 
	private boolean b_CrackAlarmRaised = false;  
	 // test 19 May private boolean b_CrackAlarmRaised = true;
	
	///uld 17 Apr. -takes too much time to write to file private boolean b_CrackAlarmRaised = true;  //output file will be written - check performance
	//indicator whether FFT output data analysis raised crack alarm

	 String outputFileName = "output_dat-8000.dat";

	private int iNumOfPointsHighestEnergies = 20;  // how many points with highest energies do we catch
	                																   //around probing Frequency

	//22 May new variables cause of 2 channels instead of 1
	double[] dProbingFrequencyFound = new double[2];   // Real probing frequency found
	double[] dMaxMagnitudeProbing = new double[2] ;		//Magnitude corresponding to probing 
																								// frequencies for 2 channels
	
	double[] dLeft1Freq = new double[2];							// Fprob - w
	double[] dLeft2Freq = new double[2];							// Fprob - w
	int[] iLeftFreqIndexAreaCalcLeft1 = new int [2];	   		//index for Fprob - w
	int[] iLeftFreqIndexAreaCalcLeft2 = new int [2];  			//index for Fprob - eps
	
	double[] dRight3Freq = new double[2];							// Fprob + eps
	double[] dRight4Freq = new double[2];							// Fprob +  w
	int[] iRightFreqIndexAreaCalcRight3 = new int [2];		//index for Fprob + eps 
	int[] iRightFreqIndexAreaCalcRight4 = new int [2];		//index for Fprob + w
	
	public double[] aodArea = new double[2];

	//monitor for FFT 
	// final static Object flock = new Object();  //released flock indicates that FFT is done 

	// OK worked 16 May public CrackDetection(double[] aod_realOut, double[] aod_imagOut,  int iData_Length) 
	public CrackDetection( )
	{
		// TODO Auto-generated constructor stub
	}  // end of constructor 

	@Override
	public void run() {
		
		// TODO Auto-generated method stub
		
		// 10 Apr. not working on Fiji: System.out.println("[CrackDetect]" + Thread.currentThread().getName()+" End. Time = "+new Date());
	
		//energies and frequencies from interval 
		//[ dProbingFrequencyFound - dProbingFreqSearchInterval  ; dProbingFrequencyFound - dFreqIntervalExcludedAroundProbing]
	     
		
		int iData_Length = ReadAudioData.MAX_NUMBER_OF_INPUT_POINTS;   //19 May
		//OK May 22 aod_angleOut = new double[iData_Length];
		aod_angleOut = new double[2] [iData_Length];
		  
		double pi = Math.PI;
		
		double dfreqResolutionStep = 1.0;				// step in frequency axis
		double dfreq = 0.0;										// current frequency 
		
		double dPumpingFrequency = 0.0;				// Pumping frequency
		//OK release 3 sec 131072 points  double dProbingFrequency = 4000.0;		// Expected Probing frequency
		//OK for Scott's double dProbingFrequency = 7623.0;		// Expected Probing frequency
	
		double dProbingFrequency = 8000.0;       // Expected Probing frequency
		//OK double dProbingFrequency = 6000.0;       // Expected Probing frequency
	
		double dProbingFreqSearchInterval = 50.0;
		double dLeftFreqBorder = dProbingFrequency - dProbingFreqSearchInterval/2 ;
		
		double dMaxMagnitudePumping = 0.0;		//Magnitude corresponding to pumping frequency
		
		double[] dMaxMagnitudeAroundProbing = new double[iNumOfPointsHighestEnergies];
		double[] dMaxMagnitudeAroundProbingFreq = new double [iNumOfPointsHighestEnergies];
		
		double dProbingMinusPumpingFrequency = 0.0;		//Probing minus Pumping frequency initial
		double dProbingMinusPumpingFrequencyFound = 0.0;	  //Probing minus Pumping frequency found
		//Magnitude corresponding to probing minus pumping frequency
		double dMaxMagnitudeProbingMinusPumping = 0.0;
		
		
		double dProbingPlusPumpingFrequency = 0.0;		//Probing plus Pumping frequency initial
		double dProbingPlusPumpingFrequencyFound = 0.0;		//Probing plus Pumping frequency found
		//Magnitude corresponding to probing minus pumping frequency
		double dMaxMagnitudeProbingPlusPumping = 0.0;
		
		int iProbingMinusPumpingFreqIndex = 0;
		int iProbingPlusPumpingFreqIndex = 0;
		
		//17 Apr.2014 
		//OK worked 24 Apr. OutputStreamWriter out = null;
		OutputStream out = null;
		
		 //Compute magnitude spectra of the raw input data and save it in output arrays. 
	    //OK May 22 magnitude = new double[iData_Length];
		double[] [] magnitude = new double[2] [iData_Length];
		
		//new variables 22 May 
	    int k = 0;
	    int g = 0;
	    int cnt = 0;
	    
		//23 May t3 synchro doesnot work, run t3 just once  while (true)    {
	    
	    //22 May:cause flock is not working yet I just need to wait approx. 6-10 sec  when FFT is done and 
	    //then do crack detection analysis with correct FFT output results
	    System.out.println("[CrackDetection] thread has started; run() is called ");
	    
	    /* check 24 May- no need to wait cause CrackDetection.run() is called from FFT after FFT is done
	    try {
		  Thread.sleep(5000);  // 5 sec
		   } catch (InterruptedException e)
		   {
			// TODO Auto-generated catch block
			e.printStackTrace();
		   }
		   */
	    
	    	/* test 23 May */ 
			
			//added 24 May: every iteration in while we need to reset the area under the curve
	    	for (k = 0; k<2; k++)
			{
			    aodArea[k] = 0.0 ;
			    dProbingFrequencyFound[k] = 0.0;
			    dMaxMagnitudeProbing[k] = 0.0;
			}		    			

			/* 23 May t3 synchro doesnot work
	    	 synchronized(FFT.flock)  	{
			    try
			    {
				     while (!FFT.get_t2_Ready() ) {
				    	       System.out.println("[CrackDetection] Waiting for flock to be released ...");
				                FFT.flock.wait();
				                System.out.println("[CrackDetection]  flock has been released ...");
				     }
			    }  catch(InterruptedException e) 
			    {
		               e.printStackTrace();
		        }   
	    	 } //from synchronized(FFT.flock)   
	    	*/
				//19 May test  try{   //19 May  can remove it  cause we will NOT write output result in file 
			    	//Open an input stream.
			        ///uld 07 Oct BufferedWriter outData =  new BufferedWriter(new FileWriter("output.dat"));
			    	
			    	///25 Mar.2014:  create a 3-rd thread which will compare 20 energy values with the baseline 
			    	
			    	//OK worked 15 Apr.2014 FileOutputStream stream = new FileOutputStream("output_B3_7623_1.dat");
					
					 if (b_CrackAlarmRaised) //17 Apr. 2014: change it - do it after crack detection analysis is done
					{
						//OK worked 24 Apr. FileOutputStream stream = new FileOutputStream("output_dat-7000.dat");
						 //try 24 Apr. FileOutputStream stream = new FileOutputStream(outputFileName);
						
						//17 Apr. 2014 worked OK OutputStreamWriter out = new OutputStreamWriter(stream, "US-ASCII");
						//OK worked 24 Apr. 2014 out = new OutputStreamWriter(stream, "US-ASCII");
						//OK worked 24 Apr. out = new OutputStreamWriter(stream, "binary");
						
						 //test 19 May out = new FileOutputStream(outputFileName);
					}
					 
			    	///RandomAccessFile raf = new RandomAccessFile("output2.dat","rw");
			    	dfreqResolutionStep = samplingRate / (double) iData_Length;
			    	System.out.println("[CrackDetection] freqResolutionStep = " + dfreqResolutionStep);
			    	
			    	//Initialization
			    	magnitude[0] [0] =  (Math.sqrt(FFT.realOut[0] [0] * FFT.realOut[0] [0]  + 
			    			                                     FFT.imagOut[0] [0] * FFT.imagOut[0] [0])) / iData_Length;
			    	
			    	magnitude[1] [0] =  (Math.sqrt(FFT.realOut[1] [0] * FFT.realOut[1] [0]  + 
                            FFT.imagOut[1] [0] * FFT.imagOut[1] [0])) / iData_Length;
			    	
			    	    			
				    for(cnt = 0;cnt < iData_Length;cnt++)
				    {
				    	//fill the output array magnitude[] []  of FFT results. Very important !!!
				    	
				    	/* check 24 May */ 
				      magnitude[0] [cnt] =
				           (Math.sqrt(FFT.realOut[0] [cnt] * FFT.realOut [0] [cnt]
				           + FFT.imagOut[0] [cnt] * FFT.imagOut[0] [cnt])) / iData_Length;
				      
				      magnitude[1] [cnt] =
					           (Math.sqrt(FFT.realOut[1] [cnt] * FFT.realOut [1] [cnt]
					           + FFT.imagOut[1] [cnt] * FFT.imagOut[1] [cnt])) / iData_Length;
				      
				    	
				      //copy magnitude[cnt] to output file 
				      
				      ///String.valueOf(magnitude[cnt]);  
				      //write to a text stream in UTF text encoding with Writer.write.
				      
				      
				      ///25 Mar: don't write every time to file ! file only if crack is detected
				      // 17 Apr. 2014  To test performance writing to file will be switched off temporarily
				      
						if (b_CrackAlarmRaised) ///uld 17 Apr.2014 
						{
							/* OK worked 24 Apr. 
							out.write(String.valueOf(magnitude[cnt]));
							out.write("\r\n");
							*/
							//test 19 May out.write((int) magnitude[cnt]);
							// out.write("\r\n");
						}
						
						
				      ///3-rd approach raf.writeDouble(magnitude[cnt]);
				       
				          /// outData.write(magnitude[cnt].toString() );   
	
						for (k = 0; k<2; k++)
						{
						      if(FFT.imagOut [k] [cnt] == 0.0
						                         && FFT.realOut[k] [cnt] == 0.0){
						          aod_angleOut[k] [cnt] = 0.0; 
						      }//end if
						      else{
						        aod_angleOut[k] [cnt] = Math.atan(
						        		FFT.imagOut [k] [cnt] / FFT.realOut[k] [cnt])*180.0 / pi;
						      }//end else
						
						      if(FFT.realOut[k] [cnt] < 0.0
						                         && FFT.imagOut[k] [cnt] == 0.0){
						        aod_angleOut[k] [cnt] = 180.0;
						      }else if(FFT.realOut[k] [cnt] < 0.0
						                        && FFT.imagOut[k] [cnt] == -0.0){
						        aod_angleOut[k] [cnt] = -180.0;
						      }else if(FFT.realOut[k] [cnt] < 0.0
						                          && FFT.imagOut[k] [cnt] > 0.0){
						        aod_angleOut[k] [cnt] += 180.0;
						      }else if(FFT.realOut[k] [cnt] < 0.0
						                          && FFT.imagOut[k] [cnt] < 0.0){
						        aod_angleOut[k] [cnt] += -180.0;
						      }//end else
						} //end for (k=0; k<2; k++)
						
				    }//end for loop
				    
				    System.out.println("[CrackDetection] Pumping frequency found: " + dPumpingFrequency);
				    System.out.println("[CrackDetection] Energy at Pumping frequency: " + dMaxMagnitudePumping);
				    
				    //17 Apr. 2014  To test performance writing to file will be switched off temporarily
				    if (b_CrackAlarmRaised) //17 Apr. 2014: change it - do it after crack detection analysis is done
					{
				    	//test 19 May out.close();
					}
				    
				    //09 Oct: uld   looking for probing frequency and sidebands 
				  //uld we already have magnitude[cnt] array and produced output file. THis time we will just scan 
				    //through all frequencies to find probing freq, probing minus pumping, probing+pumping freq
				    
				    //initialization
				    //uld dMaxMagnitudeProbing = magnitude[0];   //wrong cause first magnitude could be huge
				    
				    //end of initialization
				    
				    //09 Oct. Firstly we need to find probing frequency which may slightly differ from the expected
				    //17 Apr. 2014 probing frequency specified in dProbingFrequency field of class CrackDetection
				    //we will search for exact probing frequency in interval  
				    //dProbingFrequency +- (10* dProbingFreqSearchInterval),  i.e. 7000 +-500, i.e. from 6500 to 7500 
				    
				    //uld  17 Apr. 2014 
				    //Find the index of probing frequency  - dProbingFreqSearchInterval / 2 , i.e. starting from what
				    //frequency (exactly, starting from what index corresponding to that frequency ) 
				    // we will start searching for real probing frequency
				    int iLeftFreqIndex = (int) ( (dProbingFrequency - (dProbingFreqSearchInterval *10 ) ) / dfreqResolutionStep);
				    System.out.println("[CrackDetection] Left searching frequency: " +  (dProbingFrequency - (dProbingFreqSearchInterval *10) ));
				    System.out.println("[CrackDetection] Index for left searching frequency: " +  iLeftFreqIndex );
				    
				  //Find the index of probing frequency  - dProbingFreqSearchInterval / 2 , i.e. starting from what
				    //frequency (exactly, starting from what index corresponding to that frequency ) 
				    // we will start searching for real probing frequency
				    int iRightFreqIndex = (int) ( (dProbingFrequency + (dProbingFreqSearchInterval*10 )) / dfreqResolutionStep);
				    System.out.println("[CrackDetection] Left searching frequency: " + ( dProbingFrequency + (dProbingFreqSearchInterval *10) ));
				    System.out.println("[CrackDetection] Index for left searching frequency: " +  iRightFreqIndex );
				    
				    //looking for probing frequency 
				    for (k = 0; k<2; k++)
					{
					    for(int j = iLeftFreqIndex; j < iRightFreqIndex; j++){
					    //before April 2014 it was for(int j = 0; j < iDataLegth; j++){ 
					    	
					    	dfreq = j * dfreqResolutionStep; 
						 
					    	/* worked before 17 Apr.2014 in old design 
						      if (  ( dfreq > (dProbingFrequency - 50*dPumpingFrequency)   )  
						    		  && (dfreq < (dProbingFrequency + 50*dPumpingFrequency)  ) )
						      {
						    	  if ( magnitude[j] > dMaxMagnitudeProbing ) 
						    	  {
						    		  //renew values of pumping frequency and corresponding energy 
						    		  dMaxMagnitudeProbing = magnitude[j];
						    		  dProbingFrequencyFound = dfreq;
						    	  }
						      }
						      end of  worked before 17 Apr.2014 in old design */
					    	
					    	//17 Apr. 2014 
					    	if ( magnitude[k] [j] > dMaxMagnitudeProbing[k] ) 
					    	  {
					    		  //renew values of pumping frequency and corresponding energy 
					    		  dMaxMagnitudeProbing[k] = magnitude[k] [j];
					    		  dProbingFrequencyFound[k] = dfreq;
					    	  }
					    	
					    	
						      
					    }  //end  for(int j = iLeftFreqIndex; j < iRightFreqIndex; j++){ 
					}  //end for (k = 0; k<2; k++)
				    
				    System.out.println("[CrackDetection] Probing frequency found: for k=0 " + 
				                                      dProbingFrequencyFound[0] + "for k=1 : " + dProbingFrequencyFound[1]);
				    System.out.println("[CrackDetection] Energy at Probing frequency: for k=0 : " + 
				                                      dMaxMagnitudeProbing[0] + " for k=1 : " + dMaxMagnitudeProbing[1]);
			
				    //22 May 2014   Now I need to calculate areas under the curves for 2 channels
				    
				    //Firstly, calculate indexes for left sideband interval 
				    // [iLeftFreqIndexAreaCalcLeft1; iLeftFreqIndexAreaCalcLeft2]
				    //corresponding to [dLeft1Freq; dLeft2Freq ] 
				    //and right sideband interval 
				    // [iRightFreqIndexAreaCalcRight3; [iRightFreqIndexAreaCalcRight4]
				    //corresponding to [dRight3Freq; dRight4Freq ]
				    for (k = 0; k<2; k++)
					{
				    
				    	dLeft1Freq [k] = dProbingFrequencyFound[k] - dFreqIntervalEnergySearch;
				    	dLeft2Freq [k] = dProbingFrequencyFound[k] - dFreqIntervalExcludedAroundProbing;
					    iLeftFreqIndexAreaCalcLeft1[k] = (int) ( dLeft1Freq [k] / dfreqResolutionStep);
					    iLeftFreqIndexAreaCalcLeft2[k] = (int) ( dLeft2Freq [k]  / dfreqResolutionStep);
					    	
					    System.out.println("[CrackDetection] Left1 searching frequency: " +  dLeft1Freq[k] );
					    System.out.println("[CrackDetection] Index iLeftFreqIndexAreaCalcLeft1 for left1 searching frequency: " +  iLeftFreqIndexAreaCalcLeft1[k] );
					    System.out.println("[CrackDetection] Left2 searching frequency: " + dLeft2Freq [k]  );
					    System.out.println("[CrackDetection] Index iLeftFreqIndexPeakEnergySearchLeft2 for left2 searching frequency: " +  iLeftFreqIndexAreaCalcLeft2[k] );
					    
					    dRight3Freq[k] = dProbingFrequencyFound[k] + dFreqIntervalExcludedAroundProbing;
					    dRight4Freq[k] = dProbingFrequencyFound[k] + dFreqIntervalEnergySearch;
					    iRightFreqIndexAreaCalcRight3[k] = (int) ( dRight3Freq[k] / dfreqResolutionStep); 
					    iRightFreqIndexAreaCalcRight4[k] = (int) ( dRight4Freq[k] / dfreqResolutionStep); 
					  	
					    System.out.println("[CrackDetection] Right3 searching frequency: " + dRight3Freq[k]  );
					    System.out.println("[CrackDetection] Index iRightFreqIndexAreaCalcRight3 for right3 searching frequency: " +  iRightFreqIndexAreaCalcRight3[k] );
					    System.out.println("[CrackDetection] Right4 searching frequency: " + dRight4Freq[k]  );
					    System.out.println("[CrackDetection] Index iRightFreqIndexAreaCalcRight4 for right4 searching frequency: " +  iRightFreqIndexAreaCalcRight4[k] );
					}    
				    
				    //now start calculating area : A = Aleft + Aright
				    for (k = 0; k<2; k++)
					{
				    	for(g = iLeftFreqIndexAreaCalcLeft1[k]; g < iLeftFreqIndexAreaCalcLeft2[k]; g++)
					    {
					    	aodArea[k] +=  dfreqResolutionStep * magnitude[k] [g];
					    }  // end for(g = iLeftFreqIndexAreaCalcLeft1[k]; g < iLeftFreqIndexAreaCalcLeft2[k]; g++)
				    	
				    	for(g = iRightFreqIndexAreaCalcRight3[k]; g < iRightFreqIndexAreaCalcRight4[k]; g++)
					    {
					    	aodArea[k] +=  dfreqResolutionStep * magnitude[k] [g];
					    }  // end for(g = iRightFreqIndexAreaCalcRight3[k]; g < iRightFreqIndexAreaCalcRight4[k]; g++)
				    	
					}  // end of  for (k = 0; k<2; k++)
				    
				    for (k = 0; k<2; k++)
					{
				    	System.out.println("[CrackDetection] For channel = " + k + ", Area =  " + aodArea[k]  );
					}
				    
				    //22 May: now I can make a conclusion: 
				    // if (  (aodArea[0]-aodArea[1])  /  min {aodArea[0],aodArea[1]} ) > Threshold  then Crack!!! 
				    
				    //17 Apr. 2014: Now I need to scan intervals
				    
				    //( dProbingFrequencyFound - dProbingFreqSearchInterval  ) ; dProbingFrequencyFound -2 
				    // dProbingFrequencyFound +2;  ( dProbingFrequencyFound + (dProbingFreqSearchInterval /2 ) )
				    // for 20 points with highest energy. apply selection algorithm, which is O(n), don't use sorting
				    //	which is O(nlogn).  +2 may be changed to +1.5, or +1 (and -2 to -1.5 or -1) - define it 
				    //experimentally
				    //we will scan intervals [left1, left2]  and [right3; right4] , i.e. 
				    // [  iLeftFreqIndexPeakEnergySearchLeft1;  iLeftFreqIndexPeakEnergySearchLeft2  ] 
				    // [  iRightFreqIndexPeakEnergySearchRight3; iRightFreqIndexPeakEnergySearchRight4 ]
				    
				    //build array of 20 peak energies both for left and right intervals. Peaks are not just highest values
				    //i.e. I need to analyze slope: points before and after 
				    
				    /// OK for (int q = 0; q < iNumOfPointsHighestEnergies ; q++) {
				    //22 May 
				    /* 22 May 
				    int iNumOfPointsLeftSpectrum = iLeftFreqIndexAreaCalcLeft2 - iLeftFreqIndexAreaCalcLeft1;
				     
				    aodLeftSpectrumEnergy = new double[iNumOfPointsLeftSpectrum];
				    aodLeftSpectrumFreq = new double[iNumOfPointsLeftSpectrum];
				    System.out.println("[CrackDetection] Number of energies in left interval="+ iNumOfPointsLeftSpectrum);
				    
				    
				    //17 Apr. 2014 fiil-in left interval arrays of energies and corresponding frequencies
				    /* test 19 May 
				    for(int g = iLeftFreqIndexPeakEnergySearchLeft1; g < iLeftFreqIndexPeakEnergySearchLeft2; g++)
				    {
				    	dfreq = g * dfreqResolutionStep;
				    	aodLeftSpectrumEnergy[ g- iLeftFreqIndexPeakEnergySearchLeft1]= magnitude[g];
				    	aodLeftSpectrumFreq [g- iLeftFreqIndexPeakEnergySearchLeft1] = dfreq;
				    }
				    test 19 May  
				    /// 17 Apr. 2014: sort array of energies from left spectrum
				    Arrays.sort (aodLeftSpectrumEnergy, 0, iNumOfPointsLeftSpectrum);
				    
				    System.out.println("[CrackDetection] Sorted array of energies in left interval");
				    		
				    for (int q=0; q<iNumOfPointsLeftSpectrum; q++)
				    {
				    	System.out.println(" q = " + q + ";  aodLeftSpectrumEnergy[q] = " + aodLeftSpectrumEnergy[q] );
				    }
					22 May */   		 
				    
			  //test 19 May   }catch(IOException e){}
	    
			//test 19 May }  // end of synchronized (flock) 
	    	
				//19 May test: while flock is not working, let thread 3 just sleep 40 sec - same as thread 1
				/* 
				try {
					// OK should be in the release Thread.sleep(1800000);  //30 min = 0.5 hour
				  Thread.sleep(40000);  //40 sec
				   } catch (InterruptedException e)
				   {
					// TODO Auto-generated catch block
					e.printStackTrace();
				   }
				   */ 
				
	     //test 19 May - try only 1 iteration first 
		//23 May t3 synchro doesnot work } //end of while (true)		
		
	}  // end of run() method 
	
} // end of class CrackDetection 
