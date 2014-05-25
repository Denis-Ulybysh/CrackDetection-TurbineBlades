
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
