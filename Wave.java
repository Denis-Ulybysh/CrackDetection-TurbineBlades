//import java.io.File;


// Class to read Wave Files
class Wave {

    byte [] _wave;              /*whole byte content of input audio WAV file */
    double [] [] _samples;   /*array will contain double values of amplitude read from WAV, .i.e. what we need*/
    // 20 May double [] _samples2;   /*array will contain double values of amplitude read from WAV, .i.e. what we need*/
    
    int  i_numofChannels = 2;  			//number of channels, obtained from WAV-file 
    long l_SamplingRate = 44100;
    int iBitsperSample = 16;				//1 audio value is encoded in iBitsperSample  bits
    
    long nSamples = 0;   //number of lines in data section of wav-file, i.e. number of samples in 1 channel

    
    public double [] samples(short channel) {
    	return _samples[channel] ;
    }
    
    public Wave() {
    }

    public boolean readFile(String fileName) {
	BytesStreamsAndFiles waveFile = new BytesStreamsAndFiles();
	_wave = waveFile.read(fileName);

	int fileSize = _wave.length;
	System.out.println("size="+fileSize);

	System.out.println("ChunkID="+_wave[0]+": "+ _wave[1]+": "+_wave[2]+": \n"+_wave[3]);
	System.out.println("WAVE=%d"+_wave[8]+": %d"+ _wave[9]+": %d"+_wave[10]+":%d\n" + _wave[11]);
	
	
	// Audio format 
	// uld System.out.printf("sampleRate=%c"+_wave[24]+": %c"+ _wave[25]+": %c"+_wave[26]+":%c\n"+_wave[27] );
	System.out.println("sampleRate=%d"+_wave[24]+": %d"+ _wave[25]+": %d"+_wave[26]+":%d\n"+_wave[27] );
	/* OK, worked 14 Apr.2014 long sampleRate = (0xFF&(long)_wave[24])+((0xFF & (long)_wave[25])<<8)+
	    ((0xFF & (long)_wave[26])<<16)+((0xFF&(long)_wave[27])<<24);   */
	l_SamplingRate = (0xFF&(long)_wave[24])+((0xFF & (long)_wave[25])<<8)+
		    ((0xFF & (long)_wave[26])<<16)+((0xFF&(long)_wave[27])<<24);
	System.out.println("Sample Rate=" + l_SamplingRate);
	
	i_numofChannels = _wave[22]+(_wave[23]<<8);
	System.out.println("Number of audio channels in WAV file ="+ i_numofChannels);
	iBitsperSample =  _wave[34]+(_wave[35]<<8);
	System.out.println("Number of bits per sample in audio WAV file ="+iBitsperSample);

	
	System.out.println("Audio format=%d \r\n" +  (_wave[20]+(_wave[21]<<8)) );
	//uld 2014_04_15  System.out.printf does not work on Fiji
	/*
	System.out.printf("Channels=%d \r\n", _wave[22]+(_wave[23]<<8));
	System.out.println("SampleRate=%d \r\n", l_SamplingRate );
	System.out.println("BitsPerSample=%d \r\n", _wave[34]+(_wave[35]<<8));
   */
	
	long nBytesInData = 
	    (0xFF&(long)_wave[40])+
	    ((0xFF&(long)_wave[41])<<8)+
	    ((0xFF&(long)_wave[42])<<16)+
	    ((0xFF&(long)_wave[43])<<24);
	// uld  System.out.printf("Number of Bytes In Data = %d \r\n", nBytesInData);
	System.out.println("Number of Bytes In Data = %d \r\n" + nBytesInData);

	// We use Stereo mode cause we need to capture sound from 2 blades simultaneously 
   //  Each sample (i.e. each line)  takes 4 bytes. 2 bytes per each channel. 
	// We will work with both channels. Every 4 bytes in each line will make 2 samples of type double
	// In case of 1 channel it would be: long nSamples = nBytesInData/2; 
	// OK worked 15 Apr.2014  long nSamples = nBytesInData/4;
	nSamples = nBytesInData/ (2*i_numofChannels) ;
	System.out.println("Number of data samples in 1 channel of audio WAV file =" + nSamples);

	// Actual data start from byte 44
	//unsigned char * sound = _wave + 44;
	int i=0;

	// Create _samples array
	_samples = new double [2] [(int) nSamples];
	
		
	try {
	    for (i=0; i < nSamples; i++) {
		//System.out.println("i="+i);
		// worked OK 15 Apr.2014  int sample = ( _wave[44+i*4] & 0xff) | (short) (_wave[44+i*4+1] << 8);
	    	int sample1 = ( _wave[44+i*(2*i_numofChannels) ] & 0xff) | (short) (_wave[44+i*(2*i_numofChannels)+1] << 8);
		_samples [0] [i]= (double)sample1/(1<<15);
		
		int sample2 = ( _wave[44+i*(2*i_numofChannels)+ 2 ] & 0xff) | (short) (_wave[44+i*(2*i_numofChannels)+3] << 8);
		_samples [1] [i]= (double)sample2/(1<<15);
		
	    }
	}
	catch( Exception e) {
	    System.out.println("i max="+i*4 + " b len="+_wave.length+" nsamples="+nSamples);
	    return false;
	}

	return true;
    }
    
    void printSamples(int channel) {
		int len = _samples[0].length;
		
		// Print samples
		for (int i=0; i < len; i++) {
		    System.out.println(""+i+":"+_samples[channel] [i]);
		}
    }
    
    void writeSamples_toFile(int channel) {
    	
    		
		int len = _samples[0].length;
	
		// Write samples
//		for (int i=0; i < len; i++) {
//		    System.out.println(""+i+":"+_samples[channel] [i]);
//		}
    }
    
    
}
