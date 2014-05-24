import java.util.Date;

/**The static method transform performs a real to complex Fourier transform using a  
 * complex-to-complex FFT algorithm.  It is crippled in the sense that it is not being used to its
full potential as a complex-to-complex forward or inverse FFT algorithm.

Returns real, imag, magnitude, and phase angle in degrees.

Incoming parameters are:
  double[] data - incoming real data
  double[] realOut - outgoing real data
  double[] imagOut - outgoing imaginary data
  double[] angleOut - outgoing phase angle in degrees
  double[] magnitude - outgoing amplitude spectrum

!! Input data length MUST BE A POWER OF TWO. 

The magnitude is computed as the square root of
 the sum of the squares of the real and imaginary
 parts.  This value is divided by the incoming
 data length, which is given by data.length.

Returns a number of points in the frequency
domain equal to the incoming data length.  Those
points are uniformly distributed between zero and
one less than the sampling frequency.

ACKNOWLEDGEMENTS:
Method "complexToComplex" was written by  R.G.Baldwin

************************************************/

public class FFT implements Runnable {

	static int iDatalength = ReadAudioData.data1[0].length;
	// OK 16 Apr., worked initially  double[] realOut = new double[ReadAudioData.ui_numofdatapoints];
	static double[] [] realOut = new double[2] [iDatalength];
	// OK tried 16 Apr., worked   double[] imagOut = new double[ReadAudioData.ui_numofdatapoints];
   static double[] [] imagOut = new double [2] [iDatalength];

	double[] angleOut = new double [ReadAudioData.MAX_NUMBER_OF_INPUT_POINTS];
	   
	final static Object rlock = new Object();

	private static boolean b_t2Ready = false;
	
	boolean b_res = false;
	final  static Object flock = new Object();
	
	//uld 26 Mar. No need to make constructor synchronized cause it only be called from one place, i.e. 
	//from thread ReadAudioData  
	///OK worked 23 Apr. public FFT (double[] data) 
	
	public FFT() {
		// TODO Auto-generated constructor stub
	}    //end of constructor

	/**
	 * 
	 */
	//13 May synchronized 
	private boolean b_InputDataset_Ready() {
		
		synchronized(rlock)
		{
		    try
		    {
			     while (!ReadAudioData.get_t1_Ready()) {
			    	       System.out.println("[FFT] Waiting for rlock to be released ...");
			                rlock.wait();
			                System.out.println("[FFT]  rlock has been released ...");
			     }
		    }  catch(InterruptedException e) 
		    {
	               e.printStackTrace();
	        }
		    
		}
	    	/*//OK worked 12 May 2014 
		while (!ReadAudioData.get_t1_Ready()) {

			 ///* OK worked OK without try catch blockk 23 Apr. 
				try {
					///		lock.wait();
					Thread.sleep(100);
					lock.wait();
					/// OK worked 23 Apr.  Thread.sleep(10);
					/// OK worked 23 Apr. Thread.yield();
							/// 23 Apr. ReadAudioData.this.wait();
					//FFT.this.wait();
					System.out	.println("[ForwardRealToComplexFFT] waits...  wait() was called ");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //end of catch 
				///  OK worked OK without try catch blockk 23 Apr.  */
	    	/*
			} // end of while  OK worked 13 May 2014  */  
	    
		b_res=true;
		System.out	.println("[FFT.b_InputDataset_Ready] b_res =  " + b_res + "  ; end of method");
		//return b_res;
		return b_res;
	}
	
	public void run() {
			
		System.out.println("[FFT] thread has started; run() is called ");

		String str2 = Thread.currentThread().getName(); 
		System.out	.println("[FFT] currentThread is : " + str2);
		Thread.currentThread();
				
		while (true) 
		{
			System.out.println("[FFT] Thread is awaken ");
			set_t2_Ready (false);
			
					if (b_InputDataset_Ready() ) 
					{
					
						System.out.println("[FFT] Number of data points: " + ReadAudioData.ui_numofdatapoints);
			
						
						    //The complexToComplex FFT method does an in-place transform causing the output
						    // complex data to be stored in the arrays containing the input complex data.
						    // Therefore, it is necessary to copy the input data to this method into the real
						    // part of the complex data passed to the complexToComplex method.
						    //i.e. we copy data to realOut  
					    	
						//test 23 May 
						//23 May t3 synchro doesnot work synchronized (flock )  
						//23 May t3 synchro doesnot work {
							
						   if ( ReadAudioData.ui_numofdatapoints >= iDatalength)
						   {
							   System.arraycopy(ReadAudioData.data1[0],0, realOut[0],0,iDatalength);  //may be change it to for-cycle to copy
							   System.arraycopy(ReadAudioData.data1[1],0, realOut[1],0,iDatalength);  //may be change it to for-cycle to copy
							   //element by element 
					
							   //Perform the spectral analysis.  The results are stored in realOut and imagOut. 
							   // The +1 causes it to be a forward transform. A -1 would cause it to be an inverse transform.
							   complexToComplex(1,iDatalength,realOut[0],imagOut[0]);
							   complexToComplex(1,iDatalength,realOut[1],imagOut[1]);
							   
							   System.out.println("[FFT] FFT is done ");
						   }
						   else
						   {
							   System.out.println("[FFT] !! WARNING!! Number of audio data samples " +
						              "obtained from ReadAudioData thread " + ReadAudioData.ui_numofdatapoints +
					                   " \n is less than required  " + iDatalength +
					                   ". \n FFT results can be untrusted and can spoil history. " + 
					                   "\n Thus FFT will not be applied and result will not be saved in history");
						   } //from if
						   
						   //24 May give the opportunity to CrackDetection thread to reach flock.wait()
						   /* 23 May t3 synchro doesnot work
						   try 
						   {
								Thread.sleep(500);
							   } catch (InterruptedException e)
							   {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						   
						   set_t2_Ready (true);
						   flock.notifyAll();
						   //23 May set_t2_Ready (false);	   
						} // from synchronized (flock)  */
						   //23 May t3 synchro doesnot work
						   
												
					}  //from if (b_InputDataset_Ready() )
					
				    
				    /// OK worked 23 Apr. this.notifyAll();   //but if I don't use wait() this line is not necessary 
				    //OK worked 13 may without lock object  this.notifyAll();
				    //23 May CrackDetection.flock.notifyAll();
				    
				    System.out.println ( " [FFT.run ()] end of FFT.run() method, next iteration  will proceed now...");
			 		 
			// test 19 May } // from synchronized (CrackDetection.flock)
	
		}  //from while (true)	
		
		// 10 Apr. not working on Fiji: System.out.println("[FFT]" + Thread.currentThread().getName()+" End. Time = "+new Date());
		
	} // from run method 
	
	public static boolean get_t2_Ready () {
		  return b_t2Ready;
	  }
		
	  public void set_t2_Ready (boolean b) {
		  b_t2Ready = b;
	  }
	  
	//This method computes a complex-to-complex
  // FFT.  The value of sign must be 1 for a forward FFT.
	public static void complexToComplex(
                                  int sign,
                                  int len,
                                  double real[],
                                  double imag[]){
    double scale = 1.0;
    //Reorder the input data into reverse binary
    // order.
    int i,j;
    for (i=j=0; i < len; ++i) {
      if (j>=i) {
        double tempr = real[j]*scale;
        double tempi = imag[j]*scale;
        real[j] = real[i]*scale;
        imag[j] = imag[i]*scale;
        real[i] = tempr;
        imag[i] = tempi;
      }//end if
      int m = len/2;
      while (m>=1 && j>=m) {
        j -= m;
        m /= 2;
      }//end while loop
      j += m;
    }//end for loop

    //Input data has been reordered.
    int stage = 0;
    int maxSpectraForStage,stepSize;
    //Loop once for each stage in the spectral
    // recombination process.
    for(maxSpectraForStage = 1,
                stepSize = 2*maxSpectraForStage;
                maxSpectraForStage < len;
                maxSpectraForStage = stepSize,
                stepSize = 2*maxSpectraForStage){
      double deltaAngle =
                 sign*Math.PI/maxSpectraForStage;
      //Loop once for each individual spectra
      for (int spectraCnt = 0;
                 spectraCnt < maxSpectraForStage;
                 ++spectraCnt){
        double angle = spectraCnt*deltaAngle;
        double realCorrection = Math.cos(angle);
        double imagCorrection = Math.sin(angle);

        int right = 0;
        for (int left = spectraCnt;
                   left < len;left += stepSize){
          right = left + maxSpectraForStage;
          double tempReal =
                    realCorrection*real[right]
                    - imagCorrection*imag[right];
          double tempImag =
                    realCorrection*imag[right]
                    + imagCorrection*real[right];
          real[right] = real[left]-tempReal;
          imag[right] = imag[left]-tempImag;
          real[left] += tempReal;
          imag[left] += tempImag;
        }//end for loop
      }//end for loop for individual spectra
      maxSpectraForStage = stepSize;
    }//end for loop for stages
  }//end complexToComplex method

}//end class FFT
