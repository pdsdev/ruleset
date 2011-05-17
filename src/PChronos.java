package pds.ruleset.plugin;

// import igpp.ruleset.*;
import igpp.ruleset.Action;
import igpp.ruleset.Ruleset;

// import igpp.util.*;
import igpp.util.Option;

// import java.util.*;
import java.util.ArrayList;

// import java.io.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 *			PChronos.java August 5, 2003
 *	PChronos is a program that runs chronos externally.
 *	This returns the time in the specified system and format. <br>
 *
 *	parameters are:	<br><pre>
 *		PARAMETER	the parameter you want to return ie) $PARAMETER = value
 *		CHRONOS		the complete path of the chronos.exe file.
 *		SETUP 		the complete path of the setup file.
 *		FROM		the time system the time is in ie) UTC.
 *		FTYPE		the time type that the time is in ie) SCET.
 *		TO			the time system you want to output from the program. 
 *		TTYPE		the time type that you want to output from the program. 
 *		TIME		the time you want to convert to a different time system. 
 *		FORMAT		the format you want the output time to appear in.
 *		NO_LABEL	if you don't want the label on the output time.
 *             </pre>
 *	default values:<br> <pre>
 *		NO_LABEL = true
 *              </pre>
 *
 * @author      Erin Means
 * @author      Planetary Data System
 * @version     1.0, 08/5/03
 * @since       1.0
 */

public class PChronos {

	public static void main(String args[]) {
		String chronosLocation	= Option.find(args, "CHRONOS",	null,		0);
		String setupFile		= Option.find(args, "SETUP",		null,		0);
		String time 			= Option.find(args, "TIME",		null,		0);
		String param			= Option.find(args, "PARAMETER",	null,		0);
		String from				= Option.find(args, "FROM",		null,		0);
		String fromType 		= Option.find(args, "FTYPE",		null,		0);
		String to				= Option.find(args, "TO",		null,		0);
		String toType			= Option.find(args, "TTYPE",		null,		0);
		String format			= Option.find(args, "FORMAT", 	null,		0);
		String noLabel			= Option.find(args, "NO_LABEL",	"true",		0);
		

		if(chronosLocation == null || setupFile == null || time == null || param == null || from == null || fromType == null || to == null || toType == null) {
			errorMessage("PChronos called incorrectly, usage: java PChronos PARAM=paremeter CHRNONS=[location of chronos.exe] SETUP=[location of the setup file] FROM=[time system time is in] F_TYPE=[time type time is in] TO=[time system to return] T_TYPE=[time type to return] TIME=[time to convert]. ", true);
		}
		
		String 		buffer;
		String 		output = "";
		ArrayList	argList;
		Process 	process;
		Runtime		runtime;
		
		buffer = chronosLocation + " -setup " + setupFile + " -from " + from + " -fromtype " + fromType + " -to " + to + " -totype " + toType + " -time " + time;
		if(format != null) {
			buffer = buffer + " -format " + format;
		} if( noLabel.equals("true")) {
			buffer = buffer + " -nolabel";
		}
			
		runtime = Runtime.getRuntime();
		try{
			process = runtime.exec(buffer);
			InputStream chronos_in = process.getInputStream();
			InputStreamReader osr = new InputStreamReader(chronos_in);
			BufferedReader br = new BufferedReader(osr);
			try{
				String value = "";
				while((value = br.readLine()) != null) {
					output = value;
				}
			} catch(Exception e){}

		}catch(Exception e){}
		// Determine how we should run application
//		boolean dontParseArg = false;
//		String hold = System.getProperty("os.name");
//		if(hold.length() >= 6) { dontParseArg = (hold.substring(0, 6).compareToIgnoreCase("WINDOW") == 0); }
//
//		if(dontParseArg) {	// Let OS parse it
//			try{
//				process = runtime.exec(buffer);
//			}catch(Exception e){}
//		} else {
////			argList = PPIRuleset.argSplit(buffer, true);
////			String[] argArray = new String[argList.size()];
////			argArray = (String[]) argList.toArray(argArray);
////			process = runtime.exec(argArray);
//		}
		Ruleset.showRule(Action.ASSIGN, param, output);
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
