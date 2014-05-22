

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Executors;


public class CrackDetection implements Runnable {

	private final static int samplingRate = 44100;
	private final static double dPumpingFreqRightBorder  = 16.0;
	
	private final static double dFreqIntervalExcludedAroundProbing  = 2.0;
	private final static double dFreqIntervalEnergySearch = 20;  //20 Hz

	 //Array magnitude1 will contain FFT output data, i.e. the magnitude spectral information 
	  // from the spectral analysis process, i.e. what we need 
	double[] magnitude;
	
	double[] aod_angleOut;  //will be filled in method run () {constructor} below 


	///uld OK 17 Apr. 2014: false is normal mode, we don't write to output file 
	private boolean b_CrackAlarmRaised = false;  
	 // test 19 May private boolean b_CrackAlarmRaised = true;
	
	///uld 17 Apr. -takes too much time to write to file private boolean b_CrackAlarmRaised = true;  //output file will be written - check performance
	//indicator whether FFT output data analysis raised crack alarm

	 String outputFileName = "output_dat-7000.dat";

	private int iNumOfPointsHighestEnergies = 20;  // how many points with highest energies do we catch
	                																   //around probing Frequency

	final static Object flock = new Object();  //released flock indicates that FFT is done 

	// OK worked 16 May public CrackDetection(double[] aod_realOut, double[] aod_imagOut,  int iData_Length) 
	public CrackDetection( )
	{
		// TODO Auto-generated constructor stub
	}  // end of constructor 

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("[CrackDetection] thread has started; run() is called ");
		
		// 10 Apr. not working on Fiji: System.out.println("[CrackDetect]" + Thread.currentThread().getName()+" End. Time = "+new Date());
	
		//energies and frequencies from interval 
		//[ dProbingFrequencyFound - dProbingFreqSearchInterval  ; dProbingFrequencyFound - dFreqIntervalExcludedAroundProbing]
	     
		double[] aodLeftSpectrumEnergy;
		double[] aodLeftSpectrumFreq;
		
		int iData_Length = ReadAudioData.MAX_NUMBER_OF_INPUT_POINTS;   //19 May
		aod_angleOut = new double[iData_Length];
		  
		double pi = Math.PI;
		
		double dfreqResolutionStep = 1.0;				// step in frequency axis
		double dfreq = 0.0;										// current frequency 
		
		double dPumpingFrequency = 0.0;				// Pumping frequency
		//OK release 3 sec 131072 points  double dProbingFrequency = 4000.0;		// Expected Probing frequency
		//OK for Scott's double dProbingFrequency = 7623.0;		// Expected Probing frequency
		// OK for Scott's 16 Apr. 2014  double dProbingFrequency = 7623.0;		// Expected Probing frequency
		double dProbingFrequency = 7000.0;       // Expected Probing frequency
		//OK double dProbingFrequency = 6000.0;       // Expected Probing frequency
	
		double dProbingFreqSearchInterval = 50.0;
		double dLeftFreqBorder = dProbingFrequency - dProbingFreqSearchInterval/2 ;
		
		double dProbingFrequencyFound = 0.0;   // Real probing frequency found
		
		double dMaxMagnitudePumping = 0.0;		//Magnitude corresponding to pumping frequency
		double dMaxMagnitudeProbing = 0.0;		//Magnitude corresponding to probing frequency
		
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
	    magnitude = new double[iData_Length];
		
	    // etst 19 May while (true)    {
	    	
	    	/* test 19 May synchronized(flock)  	{
			    try
			    {
				     while (!FFT.get_t2_Ready() ) {
				    	       System.out.println("[CrackDetection] Waiting for flock to be released ...");
				                flock.wait();
				                System.out.println("[CrackDetection]  flock has been released ...");
				     }
			    }  catch(InterruptedException e) 
			    {
		               e.printStackTrace();
		        }  */
			    
	    	
				//19 May test  try{   //19 May  can remove it  cause we will NOT write output result in file 
			    	//Open an input stream.

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
			    	magnitude[0] =  (Math.sqrt(FFT.realOut[0] [0] * FFT.realOut[0] [0]  + 
			    			                                     FFT.imagOut[0] [0] * FFT.imagOut[0] [0])) / iData_Length;
			    	
			    	//uld dMaxMagnitudePumping = magnitude[0] ;	//wrong cause magnitude[0] can be huge
			    	dMaxMagnitudePumping = 0 ;
			    	//end of initialization 
			    	    			
				    for(int cnt = 0;cnt < iData_Length;cnt++)
				    {
				    	//fill the output array magnitude[]  of FFT results. Very important !!!
				      magnitude[cnt] =
				           (Math.sqrt(FFT.realOut[0] [cnt] * FFT.realOut [0] [cnt]
				           + FFT.imagOut[0] [cnt] * FFT.imagOut[0] [cnt])) / iData_Length;
				      
				      //copy magnitude[cnt] to output file 
				      

				      
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
				      
				      if(FFT.imagOut [0] [cnt] == 0.0
				                         && FFT.realOut[0] [cnt] == 0.0){
				          aod_angleOut[cnt] = 0.0; 
				      }//end if
				      else{
				        aod_angleOut[cnt] = Math.atan(
				        		FFT.imagOut [0] [cnt] / FFT.realOut[0] [cnt])*180.0 / pi;
				      }//end else
				
				      if(FFT.realOut[0] [cnt] < 0.0
				                         && FFT.imagOut[0] [cnt] == 0.0){
				        aod_angleOut[cnt] = 180.0;
				      }else if(FFT.realOut[0] [cnt] < 0.0
				                        && FFT.imagOut[0] [cnt] == -0.0){
				        aod_angleOut[cnt] = -180.0;
				      }else if(FFT.realOut[0] [cnt] < 0.0
				                          && FFT.imagOut[0] [cnt] > 0.0){
				        aod_angleOut[cnt] += 180.0;
				      }else if(FFT.realOut[0] [cnt] < 0.0
				                          && FFT.imagOut[0] [cnt] < 0.0){
				        aod_angleOut[cnt] += -180.0;
				      }//end else
				
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
				    dMaxMagnitudeProbing = 0;
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
				    	if ( magnitude[j] > dMaxMagnitudeProbing ) 
				    	  {
				    		  //renew values of pumping frequency and corresponding energy 
				    		  dMaxMagnitudeProbing = magnitude[j];
				    		  dProbingFrequencyFound = dfreq;
				    	  }
				    	
					      
				    }  //end  for(int j = iLeftFreqIndex; j < iRightFreqIndex; j++){ 
				    
				    System.out.println("[CrackDetection] Probing frequency found: " + dProbingFrequencyFound);
				    System.out.println("[CrackDetection] Energy at Probing frequency: " + dMaxMagnitudeProbing);
				    
				    //17 Apr. 2014: Now I need to scan intervals
				    
				    //( dProbingFrequencyFound - dProbingFreqSearchInterval  ) ; dProbingFrequencyFound -2 
				    // dProbingFrequencyFound +2;  ( dProbingFrequencyFound + (dProbingFreqSearchInterval /2 ) )
				    // for 20 points with highest energy. apply selection algorithm, which is O(n), don't use sorting
				    //	which is O(nlogn).  +2 may be changed to +1.5, or +1 (and -2 to -1.5 or -1) - define it 
				    //experimentally
				    //we will scan intervals [left1, left2]  and [right3; right4] , i.e. 
				    // [  iLeftFreqIndexPeakEnergySearchLeft1;  iLeftFreqIndexPeakEnergySearchLeft2  ] 
				    // [  iRightFreqIndexPeakEnergySearchRight3; iRightFreqIndexPeakEnergySearchRight4 ]
				    int iLeftFreqIndexPeakEnergySearchLeft1 = (int) ( (dProbingFrequencyFound - dFreqIntervalEnergySearch ) / dfreqResolutionStep);
				    int iLeftFreqIndexPeakEnergySearchLeft2 = (int) ( (dProbingFrequencyFound - dFreqIntervalExcludedAroundProbing ) / dfreqResolutionStep);
				  	    
				    System.out.println("[CrackDetection] Left1 searching frequency: " +  (dProbingFrequencyFound - dFreqIntervalEnergySearch ) );
				    System.out.println("[CrackDetection] Index iLeftFreqIndexPeakEnergySearchLeft1 for left1 searching frequency: " +  iLeftFreqIndexPeakEnergySearchLeft1 );
				    
				    System.out.println("[CrackDetection] Left2 searching frequency: " +  (dProbingFrequencyFound - dFreqIntervalExcludedAroundProbing ) );
				    System.out.println("[CrackDetection] Index iLeftFreqIndexPeakEnergySearchLeft2 for left2 searching frequency: " +  iLeftFreqIndexPeakEnergySearchLeft2 );
				    
				    //uld 17 Apr. 2014 : repeat this search 20 times excluding previous result - apply selection algorithm 
				    int iRightFreqIndexPeakEnergySearchRight3 = (int) ( (dProbingFrequencyFound + dFreqIntervalExcludedAroundProbing ) / dfreqResolutionStep); 
				    int iRightFreqIndexPeakEnergySearchRight4 = (int) ( (dProbingFrequencyFound + dFreqIntervalEnergySearch ) / dfreqResolutionStep); 
				  	    
				    System.out.println("[CrackDetection] Right3 searching frequency: " +  (dProbingFrequencyFound + dFreqIntervalExcludedAroundProbing )  );
				    System.out.println("[CrackDetection] Index iRightFreqIndexPeakEnergySearchRight3 for right3 searching frequency: " +  iRightFreqIndexPeakEnergySearchRight3 );
				    
				    System.out.println("[CrackDetection] Right4 searching frequency: " +  (dProbingFrequencyFound + dFreqIntervalEnergySearch ) );
				    System.out.println("[CrackDetection] Index iRightFreqIndexPeakEnergySearchRight4 for right4 searching frequency: " +  iRightFreqIndexPeakEnergySearchRight4 );
				    
				    //build array of 20 peak energies both for left and right intervals. Peaks are not just highest values
				    //i.e. I need to analyze slope: points before and after 
				    
				    /// OK for (int q = 0; q < iNumOfPointsHighestEnergies ; q++) {
				    int iNumOfPointsLeftSpectrum = iLeftFreqIndexPeakEnergySearchLeft2 - iLeftFreqIndexPeakEnergySearchLeft1;
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
				    test 19 May */ 
				    /// 17 Apr. 2014: sort array of energies from left spectrum
				    Arrays.sort (aodLeftSpectrumEnergy, 0, iNumOfPointsLeftSpectrum);
				    
				    System.out.println("[CrackDetection] Sorted array of energies in left interval");
				    		
				    for (int q=0; q<iNumOfPointsLeftSpectrum; q++)
				    {
				    	System.out.println(" q = " + q + ";  aodLeftSpectrumEnergy[q] = " + aodLeftSpectrumEnergy[q] );
				    }
					   		 
				    
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
				
	     //test 19 May - try only 1 iteration first } //end of while (true)		
		
	}  // end of run() method 
	
} // end of clas CrackDetection 
