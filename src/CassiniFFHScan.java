package pds.ruleset.plugin;

// import igpp.ruleset.*;
import igpp.ruleset.Action;
import igpp.ruleset.Ruleset;

// import igpp.util.*
import igpp.util.Option;
import igpp.util.MultiTime;

// import java.util.*;
import java.util.ArrayList;

// import java.io.*;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/*
 * CassiniFfhScan
 *This code reads a *.FFH file and assigns values from the ffh file to these
 *keywords. If there is no value the word "NULL" will be printed. It reformats
 *the cassini SCLK into a PDS time.
 *
 *  $SFTIME	creation date of the file
 *  $STIME	start time of the data file
 *  $ETIME	stop time of the data file
 *  $SSCLK	start sclk of the data file
 *  $ESCLK	stop sclk of the data file
 *  $SNATIVE	start time of the data file in native format
 *  $ENATIVE	stop time of the data file in native format
 *  $RECS	number of records in the file
 *  $COLS	number of columns in the file
 *  $REC_BYTES	number of bytes in one record
 *  $FILE_NOTE  the CASSINI ABSTRACT portion of the .FFH file
 *
 * @author      Erin Means
 * @author      Planetary Data System
 * @version     1.2, 01/27/04
 * @version     1.3, 05/24/04, jmafi - "." changed to ":" in SCLK output 
 * @since       1.0
 */


public class CassiniFFHScan {

	private static ArrayList lines			= new ArrayList();
	private static String dataFile			= "NULL";
	private static int    hdr_bytes			= 0;
	private static String recl			= "NULL";
	private static String recs			= "NULL";
	private static String ncols			= "NULL";
	private static String fileStartTime		= "NULL";
	private static String fileStopTime		= "NULL";
	private static String binStartTime		= "NULL";
	private static String binStopTime		= "NULL";
	private static String startTime			= "NULL";
	private static String stopTime			= "NULL";
	private static String startSclk			= "NULL";
	private static String stopSclk			= "NULL";
	private static String startNative		= "NULL";
	private static String stopNative		= "NULL";
	private static String cTime			= "NULL";
	private static String ffAbstract		= "NULL";
	private static String file			= null;
	private static String chronosLoc		= null;
	private static String chronosSetup 		= null;

	public static void main(String[] args){

		file		= Option.find(args, "FFH_FILE",              null, 0);
		chronosLoc	= Option.find(args, "CHRONOS_LOCATION",      null, 0);
		chronosSetup	= Option.find(args, "CHRONOS_SETUP_FILE",    null, 0);

		if(file == null || chronosLoc == null || chronosSetup == null) {
			errorMessage("CassiniFFHScan called incorrectly. Usage: java CassiniFFHScan FFH_FILE=$FFH_FILE CHRONOS_LOCATION=[path to chronos] CHRONOS_SETUP_FILE=[location of the chronos setup file]", true);
		}
		ffhScan();
	}


	public static void ffhScan() {
		File ffhFile = new File(file);
		if(!ffhFile.exists()) {
			errorMessage("File does not exist.", true);
		}
		readFfhFile(ffhFile);
		parse();
		// If all goes well it prints the contents to the screen.
		Ruleset.showRule(Action.ASSIGN, "SFTIME",         cTime		);
		Ruleset.showRule(Action.ASSIGN, "STIME",          startTime	);
		Ruleset.showRule(Action.ASSIGN, "ETIME",          stopTime	);
		Ruleset.showRule(Action.ASSIGN, "SSCLK",          startSclk	);
		Ruleset.showRule(Action.ASSIGN, "ESCLK",          stopSclk	);
		Ruleset.showRule(Action.ASSIGN, "NSTART",         startNative	);
		Ruleset.showRule(Action.ASSIGN, "NSTOP",          stopNative	);
		Ruleset.showRule(Action.ASSIGN, "RECS",           recs		);
		Ruleset.showRule(Action.ASSIGN, "COLS",           ncols		);
		Ruleset.showRule(Action.ASSIGN, "REC_BYTES",      recl		);
		Ruleset.showRule(Action.ASSIGN, "HDR_BYTES",      hdr_bytes	);
		Ruleset.showRule(Action.ASSIGN, "FILE_NOTE",      ffAbstract	);
	}

	/*
	Reads the *.FFH file if it exists. If an error occurs
	while reading the file the Program prints a message
	to STD output and exits.
	*/
	private static void readFfhFile(File ffhFile){
		BufferedReader fin = null;
		String line = null;
		try{
			fin = new BufferedReader(new FileReader(ffhFile));
			line = fin.readLine();
			hdr_bytes = line.length();
			for(int index = 0; (line.length() - index) >= 72; index+=72){
				lines.add(line.substring(index, index+72));
			}
		} catch(Exception e){
			errorMessage("Error reading file: " + e.getMessage(), true);
		}
	}

	/*
	Parses the file and pulls out all the relavent info.
	It also puts the time in PDS time format by calling
	PPITime converter.
	*/
	private static void parse(){
		String	buffer;
		String	value;
		int		n;
		
		MultiTime converter = new MultiTime();
		for(int linesIndex = 0; linesIndex < lines.size(); linesIndex++){
			buffer = (String) lines.get(linesIndex);
			buffer = buffer.trim();
			value = "";
			n = buffer.indexOf('=') + 1;
			value = buffer.substring(n).trim();
			if(buffer.startsWith("DATA")) dataFile = value;
			if(buffer.startsWith("RECL")) recl = value;
			if(buffer.startsWith("NCOLS"))	ncols = value;
			if(buffer.startsWith("NROWS"))	recs = value;
			if(buffer.startsWith("SPACECRAFT_CLOCK_START_COUNT")) startSclk = value;
			if(buffer.startsWith("SPACECRAFT_CLOCK_STOP_COUNT")) stopSclk = value;
			if(buffer.startsWith("CDATE")){
				cTime = value;
				if(converter.convert("yyyy DDD MMM dd HH:mm:ss", cTime)){
					cTime = converter.format(MultiTime.PDS);
				} else {
					errorMessage("There was a problem converting the CDATE to a PDS format. Aborting file", true);
				}
			}
			if(buffer.startsWith("ABSTRACT")) {
				ffAbstract = "";
				while(!buffer.equals("END")) {
					if(buffer.startsWith("FIRST TIME")) {
						fileStartTime = value;
						if(converter.convert("yyyy DDD MMM dd HH:mm:ss.SSS", fileStartTime) == false){ errorMessage("There was a problem converting FIRST TIME to a binary time. Aborting file.", true);}
						binStartTime = converter.format(MultiTime.BINARY);
						String[] parts = binStartTime.split("\\.");
						int decimalPart = 0;
						int nonDecimalPart = 0;
						try{
							decimalPart = Integer.parseInt(parts[1]);
							nonDecimalPart = Integer.parseInt(parts[0]);
							nonDecimalPart = nonDecimalPart + 252460800;	// Offset from 1958/1/1 to 1966/1/1
						} catch(Exception e) {
							errorMessage(e.getMessage(), true);
						}
						startNative = new Integer(nonDecimalPart).toString() + "." + parts[1];
						double temp  = decimalPart * 0.2555;
						String decpt = new Long(Math.round(temp)).toString();
						if(decpt.length() == 1){
							decpt = "00" + decpt;
						} else if(decpt.length() == 2) {
							decpt = "0" + decpt;
						}
						startSclk = "1/" + new Integer(nonDecimalPart).toString() + ":" + decpt;
						startTime = convertToScet(startSclk).trim();
						
						if(converter.convert("yyyy-MM-dd HH:mm:ss.SSS", startTime) == false){ errorMessage("There was a problem converting FIRST TIME to a PDS time. Aborting file.", true);}
						startTime = converter.format(MultiTime.PDS);

					} else if(buffer.startsWith("LAST TIME")){
						fileStopTime = value;
						if(converter.convert("yyyy DDD MMM dd HH:mm:ss.SSS", fileStopTime) == false){ errorMessage("There was a problem converting LAST TIME to a binary time. Aborting file.", true);}
						binStopTime = converter.format(MultiTime.BINARY);
						String[] parts = binStopTime.split("\\.");
						int decimalPart = 0;
						int nonDecimalPart = 0;
						try{
							decimalPart = Integer.parseInt(parts[1]);
							nonDecimalPart = Integer.parseInt(parts[0]);
							nonDecimalPart = nonDecimalPart + 252460800;
						} catch(Exception e) {
							errorMessage(e.getMessage(), true);
						}
						stopNative = new Integer(nonDecimalPart).toString() + "." + parts[1];
						double temp  = decimalPart * 0.2555;
						String decpt = new Long(Math.round(temp)).toString();
						if(decpt.length() == 1){
							decpt = "00" + decpt;
						} else if(decpt.length() == 2){
							decpt = "0" + decpt;
						}
						stopSclk = "1/" + new Integer(nonDecimalPart).toString() + ":" + decpt;
						stopTime = convertToScet(stopSclk).trim();
						if(converter.convert("yyyy-MM-dd HH:mm:ss.SSS", stopTime) == false){ errorMessage("There was a problem converting LAST TIME to a PDS time. Aborting file.", true);}
						stopTime = converter.format(MultiTime.PDS);

					} else if(!buffer.startsWith("OWNER")){
						ffAbstract += buffer + "\n";
					}
					linesIndex++;
					buffer = (String) lines.get(linesIndex);
					buffer = buffer.trim();
					value = "";
					n = buffer.indexOf('=') + 1;
					value = buffer.substring(n).trim();
				}
			}
		}
		ffAbstract = ffAbstract.substring(0,ffAbstract.length() - 1);
		if(startTime.equals("NULL")) {
			errorMessage("FIRST_TIME does not exist in header file.", false);
		} if(stopTime.equals("NULL")) {
			errorMessage("LAST_TIME does not exist in header file.",  false);
		}
	}

	public static String convertToScet(String time) {
		String		buffer;
		String		value = "";
		ArrayList	argList;
		Process		process;
		Runtime		runtime;
		ArrayList	outputList = new ArrayList();

		buffer = chronosLoc + " -setup " + chronosSetup + " -from sclk -fromtype sclk -to utc -totype scet -time " + time + " -NOLABEL"; 
		runtime = Runtime.getRuntime();
		try{
			process = runtime.exec(buffer);
			InputStream chronos_in = process.getInputStream();
			InputStreamReader osr = new InputStreamReader(chronos_in);
			BufferedReader br = new BufferedReader(osr);
			try{
				while((value = br.readLine()) != null) {
					outputList.add(value);
				}
			} catch(Exception e){}


		}catch(Exception e){}
		// Determine how we should run application
		boolean dontParseArg = false;
		String hold = System.getProperty("os.name");
		if(hold.length() >= 6) { dontParseArg = (hold.substring(0, 6).compareToIgnoreCase("WINDOW") == 0); }
		value = "";
		if(outputList.size() > 1) {
			int outputIndex = 7;
			while(outputIndex < outputList.size() && outputList.get(outputIndex).toString().indexOf("traceback") < 0) {
				value += outputList.get(outputIndex).toString() + "\n\t";
				outputIndex++;
			}
			errorMessage("Problem running Chronos:\n\t " + value, false);
			return "NULL";
		} else {
			value = outputList.get(0).toString().trim();
			return value;
		}
	}

	private static void errorMessage(String message, boolean abort) {
		Ruleset.showRule(Action.MESSAGE, "$RULE_SET");
		Ruleset.showRule(Action.MESSAGE, "\t$FILE_PATH/$FILE_NAME");
		Ruleset.showRule(Action.MESSAGE, "\t" + message);
		if (abort) {
			Ruleset.showRule(Action.ABORT, "");
			System.exit(1);
		}
	}

}
