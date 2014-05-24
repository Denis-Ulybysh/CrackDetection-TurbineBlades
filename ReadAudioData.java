/** Crack detection algorithm   17 Apr.2014
  */

/**
 * @author Denis Ulybyshev
 *
 * Actuator produces input sound signal with frequency 4 kHz  or other (probing frequency). 
 * I.e. it can be other frequency as well, e.g. 6 kHz, 7kHz etc. 
 * Output audio signal is captured in WAV - file, which should be converted to DAT-file afterwards 
 * (e.g. by using "sox" utility: $ sox input_file.wav output_file.dat). 
 * Then FFT  is applied to input DAT file "BadBlade_65536_OK_001.dat" (or other name) 
 * to compute  the magnitude of the spectral content. The output values of energies (magnitudes) are
 * written to  specified file, e.g.  "output_B3_7000.dat".
 *
 * Warning: Input dataset length must be a power of two (according to FFT algorithm requirement).
 * 
 *    Note:  when input file is changed be careful: 
 *    Changes in ReadAudioData.java: 
 *    			change input file name (2 places in this source code), 
 *          	change size of array in 3 places here:  int ui_numofdatapoints = 262144;  int len = 262144;
 *              final int MAX_NUMBER_OF_INPUT_POINTS = 262144;
 *    
 *    Changes in ForwardRealToComplex.java:        
 *    			change samplingRate and expected probing frequency dProbingFrequency 
 *    			change output file name (output_B2_6000.dat)  in 
 *    					FileOutputStream stream = new FileOutputStream("output_B3_7000.dat");
 */
/* 
 16 Apr. 2014
When feeding new input audio data file change 3 things: 
1) number of points - in this file in 2 places
2) Input File name - here in one place
3) output file name - in CrackDetection.java in one place 
4) Expected probing frequency - in CrackDetection.java  
5) sampling rate (which is now hard coded)
 */

	//1 step in frequency domain = 44100/ 65536 = 0.67291259765625  Hz
	//1 step in Sungmin data        = 96000 / 262144 = 0.3662109375   Hz
 //   1 step in Sungmin's data     = 96000 / 524288 =  0.18310546875

    //6) With new Sungmin's data (23 Oct): sampling frequency is 96kHz 



import java.io.File;


///ReadAudioData will be used as a thread which reads input audio data from the file.
///All the functionality is in method run()  
public class ReadAudioData implements Runnable {
// error from Fiji!  public class ReadAudioData extends RealtimeThread {

	/**
	 * @param args
	 */
	///OK worked 10 Apr. static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	
	 //added 10 Apr. 12:10 am  to check memory leakage 
	/// Worked OK on laptop 10 Apr.2014   static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();	
	/// uld 25 Mar.2014  final double pi = Math.PI;  
	
	//21 sec for sampling rate 48 kHz, cause 1048576 / 48 000 = 21.8 sec
	///25 Mar.2014: within 1 sec sound card receives 48000 audio measures (points)

	//sampling rate 44.1 kHz
	  final static int MAX_NUMBER_OF_INPUT_POINTS =  1048576;
	  ///11 Apr. final int MAX_NUMBER_OF_INPUT_POINTS =  1048576/32; - works fast on Android Pi
	  
	  //OK release sampling rate 96kHz 
	  //final int MAX_NUMBER_OF_INPUT_POINTS =  2097152;
	
	//OK Release Sungmin's data samplingrate-96 kHz  
	//int ui_numofdatapoints = 2097152;
	
	//for samplingrate = 48 kHz -= Scott's data

	  public static int ui_numofdatapoints = MAX_NUMBER_OF_INPUT_POINTS;  //on Pi I tried /8, 11 Apr.2014		
	
	//test int ui_numofdatapoints = 4; //number of input points, which m.b. power of 2. Here it is 2^18, i.e. 6 sec

	  //Array data1 contains input sound data which will be read from input DAT-file
	  // It will be the input for FFT 
	  static double[] [] data1 = new double[2] [MAX_NUMBER_OF_INPUT_POINTS];  

	 //Following arrays receive information back
	  // from the spectral analysis that is not used in this program.
	  //	  10 Apr. 2014  double[] real;
	  //	  10 Apr. 2014  double[] imag;
	  //	  10 Apr. 2014  double[] angle;

	  //Array magnitude1 will contain FFT output data, i.e. the magnitude spectral information 
	  // from the spectral analysis process, i.e. what we need 
	  //11 Apr: not required anymore  double[] magnitude1 = new double[ui_numofdatapoints];
	 //10 Apr.  magnitude1 = new double[ui_numofdatapoints];
	  

	  private static boolean b_t1Ready = false;
     //OK 16.04.2014  String inputFileName = "B3_6000.wav";
	 //OK 16.04.2014: cmpared with old results from Crack_Detect_2013_11_05 and they are same. Very good 
	 // String inputFileName = "B3_7623_1.wav";
	  String inputFileName = "dat-8000-stereo.wav";
	  Wave w = new Wave();
	
	   private int j = 0;
	   private int i = 0;
	
	  public void run() {

		  while (true)
		  {  
			  
			  System.out.println("[ReadAudioData] thread has been started, run() is called  " );
			  // not working on Fiji  System.out.println(Thread.currentThread().getName()+" Start. Time = "+new Date());
			  
				//Compute magnitude spectra of the raw input data and save it in output arrays. 
			    //Note that the real, imag, and angle arrays are not
				// used later, so they are discarded each time a new spectral analysis is performed.
				
		//		10 Apr. 2014  real = new double[ui_numofdatapoints];
		//		10 Apr. 2014  imag = new double[ui_numofdatapoints];
		//		10 Apr. 2014  angle = new double[ui_numofdatapoints];
				 
			  this.set_t1_Ready(false);   //indicator that thread 1 ReadAudioData is not done yet
		
			  if(new File(inputFileName).exists()){
				  
				   //added to test lock seize-release without while (!ReadAudioData.get_t1_Ready() )
				   try {
					Thread.sleep(100);
				   } catch (InterruptedException e)
				   {
					// TODO Auto-generated catch block
					e.printStackTrace();
				   }
				   // */
			  
				   getData(inputFileName);   
					
			      }//end if
				  else
				  {
					  System.out.print("[ReadAudioData] ERROR: input file not found " );
					  // 14 May 2014: destroy threads 2,3,4 here !!!
					  ///  kill threads 2,3,4 if input file does not exist
				  }
				  
				  //Print length of input dataset 
				  System.out.println(" [ReadAudioData.run] Number of data points from microphone: " + ui_numofdatapoints);
				  /// not working on Fiji System.out.println(Thread.currentThread().getName()+" End. Time = "+new Date());
				 
				  //19 May : give the opportunity for other threads to work, i.e. unblock 
				  try {
						Thread.sleep(100);
					   } catch (InterruptedException e)
					   {
						// TODO Auto-generated catch block
						e.printStackTrace();
					   }
				  
				 set_t1_Ready (false);  //before going to sleep make b_t1_isReady false so that thread 2 waits
				                                         // 30 mins until thread 1 wakes up and reads new dataset
				  
				  try {
						// OK should be in the release Thread.sleep(1800000);  //30 min = 0.5 hour
					  Thread.sleep(40000);  //40 sec
					   } catch (InterruptedException e)
					   {
						// TODO Auto-generated catch block
						e.printStackTrace();
					   }
				  
			  
		  }  // end of while( true)   
	       
		    /// 09 Apr. set_t1_Ready (true);
		    ///09 Apr. can't call notify() method  here cause 
	//       A thread becomes the owner of the object's monitor in one of three ways:
	//	        By executing a synchronized instance method of that object.
	//	        By executing the body of a synchronized statement that synchronizes on the object.
	//	        For objects of type Class, by executing a synchronized static method of that class.
	
		    ///uld Mar.2014: we'll start other thread ForwardRealToComplex in main () instead of calling
	        //	a method transform
		    ///uld 2014 ForwardRealToComplexFFT.transform(data1, real,imag,angle,magnitude1);
	    
	  }//end of run () 
//	  }, 0, 5, TimeUnit.SECONDS);

	  //-------------------------------------------//

	  //This method reads input DAT-file with sound data and stores values in data1 array 
	  // OK worked 13 May 2014 synchronized void getData(String filename ) {
	  void getData(String filename  ) {
	   
		  synchronized (FFT.rlock ) 
		  { 
					w.readFile(filename);
					
					System.out.println("[getData] Number of audio data samples from 1 channel of WAV-file (from microphone): " + w.nSamples);
			
					if (w.nSamples >= MAX_NUMBER_OF_INPUT_POINTS )  //WAV-file has more samples than we need in FFT algo
					{
						System.arraycopy(w._samples[0], 0, data1[0], 0, MAX_NUMBER_OF_INPUT_POINTS);
						System.arraycopy(w._samples[1], 0, data1[1], 0, MAX_NUMBER_OF_INPUT_POINTS);
					}
					else 
					{
						System.arraycopy(w._samples[0],0,data1[0], 0, (int) w.nSamples);
						System.arraycopy(w._samples[1],0,data1[1], 0, (int) w.nSamples);
						//we copied to data1 whose size is ui_numofdatapoints  number of point less than ui_numofdatapoints
						//it means that the rest (the end) of data1 will be filled with 0-es, it will distort FFT result
						ui_numofdatapoints = (int) w.nSamples;  
						//number of points which will be fed to FFT has been changed !!! This is very cautios situation,
						//cause we will feed to FFT less points than planned before. And this new number of points 
						// (or samples) may be NOT a power of 2, then FFT algo will crash 
						//In this case program should not crash, but its results should not be written in history and thus
						//should not be considered in future comparisons cause they were not trusted
						System.out.println("[getData] !! WARNING!! Number of audio data samples " + ui_numofdatapoints +
								                          " is less than required  " + MAX_NUMBER_OF_INPUT_POINTS);
						//change it:
						//if WAV-file has less samples (points) than maximum limit for FFT then don't just feed this number 
						//of samples to FFT: reduce it down until nearest power of 2
						
					}
					
					for (j = 0; j<2 ; j++)
					{
					    for (i=0; i<MAX_NUMBER_OF_INPUT_POINTS; i+=300000)
					    {
					    		System.out.println( "\r\n i =  " + i + "j = " + j);
					    		System.out.print("  ;   data1[j] [i]=" + data1 [j] [i]);
					    }
					    
					}
					
					set_t1_Ready (true);
				    /// OK worked 23 Apr. this.notifyAll();   //but if I don't use wait() this line is not necessary 
				    //OK worked 13 may without lock object  this.notifyAll();
				    FFT.rlock.notifyAll();
				   
			 		System.out.println ( " [getData] end of getData method, method run() of ReadAudioData  thread "
				   			                  + " will be proceeded now ...");
			 		
			 		//may be call w.close to clean memory and remove w's descriptors to prevent memory leak?
		  } //end of synchronized (lock)
	}//end getData
	  //-------------------------------------------//
	  
  public static boolean get_t1_Ready () {
	  return b_t1Ready;
  }
	
  public void set_t1_Ready (boolean b) {
	  b_t1Ready = b;
  }
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//this.ReadAudioData();
		
		/// 09 Apr.2014  ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

		/// worked OK on laptop 10 Apr.2014  Runnable r = new ReadAudioData();
        //OK 09 Apr.2014 worked, then moved to global variables  ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		//OK 9 Apr. 2014-was working 1 hour:  ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		
		/// worked OK on laptop 10 Apr.2014    service.scheduleAtFixedRate(r, 0, 30, TimeUnit.SECONDS);
		
      //09 Apr.2014  If r is a Runnable object, and e is an Executor object you can replace
      // (new Thread(r)).start();    with       e.execute(r);
        //see Executor Interfaces (The Java™ Tutorials ) Essential Classes ) Concurrency).htm
        /// 10 Apr. service.shutdown(); 
        //shutdown just terminates app immediately 
		/// worked OK on laptop 10 Apr.2014   System.out.println("[ReadAudioData main] service.isterminated() returns: " + service.isTerminated() );
        
		//causes NoClassDefFoundError (new RealtimeThread(new ReadAudioData())).start();    //uld  it should be run by scheduler, not just by calling start
		// worked OK 28 Apr. (new Thread(new ReadAudioData())).start();    //uld  it should be run by scheduler, not just by calling start
		(new Thread(new ReadAudioData())).start();
		 
	        //OK 09 Apr.2014 worked ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
			//OK 9 Apr. 2014-was working 1 hour:  ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		 // 
		//OK worked 10 Apr. Runnable r = new ReadAudioData();
		//OK worked 10 Apr.  service.scheduleAtFixedRate(r, 0, 30, TimeUnit.SECONDS);
        
	        //10 Apr.2014: start thread 4 which will clean old data from SD card 
		(new Thread(new CleaningThread())).start();
		
		 (new Thread(new FFT())).start();
		 (new Thread(new CrackDetection())).start();
	
	        
//09 Apr.2014		 ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
//09.04.2014	     service.scheduleAtFixedRate(ReadAudioData, 0, 30, TimeUnit.SECONDS);
	        
	}  //end of main(...)

} //class ReadAudioData
