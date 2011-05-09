package ruleset.plugin;

import pds.ruleset.*;
import pds.util.*;
import java.util.*;
import java.io.*;
import java.sql.Date;
import java.sql.Time;

/**
 *PvoTabProperties is a function that will return the first and last values in the table
 *you give, between the bytes you give.
 *
 *parameters are: <br><pre>
 *	TABLE      = [character string] The location of the file.
 *	SKIP_ROWS  = [integer] The the number of rows in the beginning of the file to skip.
 *      </pre>
 *
 *defaults: <br><pre>
 *	SKIP_ROWS = 0
 *      </pre>
 *
 *returns:<br><pre>
 *      </pre>
 *
 * @author      Erin Means
 * @author      Planetary Data System
 * @version     1.0, 02/20/04
 * @since       1.0
 */
 
 public class PvoTabProperties {
 	
 	
 	public static void main(String[] args) {
 		//the values to return from the program
 		String startTime = "";
 		String stopTime  = "";
 		String currentLineTime  = "";
 		int RECS = 1;
 		int RECL = 0;
 		Date cdate = null;
 		Time ctime = null;
 		String creationDate = "";
 		
 		//the indeces for the substring and the number of rows to skip.
 		int startIndex = 0;
 		int stopIndex  = 0;
 		int skip       = 0;
 		
 		//sets all the values to the appropriate things from the command line arguments
 		String tabStr	 = PPIOption.find(args, "TABLE",      null, 0);
 		String skipStr   = PPIOption.find(args, "SKIP_ROWS",  "0",  0);
 		
 		//This is error checking to see if all the values have been passed.
 		if(tabStr == null) {
 			errorMessage("PvoTabProperties called incorrectly, usage java PvoTabProperties TABLE='location of the Table' ", true);
 		}
 		
 		//Creates a file object so the table can be read
 		File tab = new File(tabStr);
 		
 		//Error checking to make sure the file exists.
 		if (!tab.exists()) {
 			errorMessage("The file " + tabStr + " does not exist. ", true);
 		}
 		
 		//Reads the file and gets the values
 		try {
 			BufferedReader fin = new BufferedReader(new FileReader(tab));
 			String line = "";
 			while (skip > 0) {
 				line = fin.readLine();
 				skip--;
 			}
 			line = fin.readLine();
 			RECS = 1;
 			RECL = line.length() + 2;
 			startTime = stopTime = line.substring(1,25).trim();
 			while ((line = fin.readLine()) != null) {
 				currentLineTime = line.substring(1,25).trim();
 				if ( currentLineTime.compareTo(startTime) < 0 )
 					startTime = currentLineTime;
 				if ( currentLineTime.compareTo(stopTime) > 0 )
 					stopTime = currentLineTime;
 				RECS++;
 			}
 			cdate = new Date(tab.lastModified());
 			ctime = new Time(tab.lastModified());
 			creationDate = cdate.toString() + "T" + ctime.toString();
 		}catch(Exception e) {
 			errorMessage("There was a problem reading the table " + tabStr, true);
 		}
 		
 		//Prints them to STDOUT.
 		PPIRuleset.showRule(PPIAction.ASSIGN, "STIME", startTime);
 		PPIRuleset.showRule(PPIAction.ASSIGN, "ETIME", stopTime);
 		PPIRuleset.showRule(PPIAction.ASSIGN, "REC_BYTES", RECL);
 		PPIRuleset.showRule(PPIAction.ASSIGN, "RECS", RECS);
 		PPIRuleset.showRule(PPIAction.ASSIGN, "CTIME", creationDate);
 		
 	}
 	
 	/* This is my error message function.
 	 It prints the error in proper label ruleset format.
 	 If abort = true it quits the ruleset otherwise it prints
 	 a message and continues.
 	*/
 	private static void errorMessage(String message, boolean abort) {
		PPIRuleset.showRule(PPIAction.MESSAGE, "$RULE_SET");
		PPIRuleset.showRule(PPIAction.MESSAGE, "\t$FILE_PATH/$FILE_NAME");
		PPIRuleset.showRule(PPIAction.MESSAGE, "\t" + message);
		if (abort) {
			PPIRuleset.showRule(PPIAction.ABORT, "");
			System.exit(1);
		}
	}
}