import java.util.Date;


public class CleaningThread implements Runnable  {

	public void run() {

		  System.out.println("[CleaningThread] thread has been started, run() is called  " );
		// 10 Apr. not working on Fiji: System.out.println("[CleaningThread]" + Thread.currentThread().getName()+" Start. Time = "+new Date());
		  
		  //0. Calculate available SD card free space
		  //1. go to folder with historic output datasets  
		  //2. Find those files whose name contains "!!" and if free space is less than 500 MB and (or-?) file creation 
		  //    date is older than 1 year (Current.date - fileCreationDate > 1 year  then remove file
		  
		// 10 Apr. not working on Fiji:  System.out.println("[CleaningThread]" + Thread.currentThread().getName()+" End. Time = "+new Date());
	} //end of run()		  
}
