/**
 * no.skde.tools.report
 * ProcessShellScriptRapporteket.java April 23 2014 Are Edvardsen
 * 
 * Initially for running svn updates on misc files (R and alike)
 * executed from a standard report.
 * 
 *  Copyleft 2014 SKDE
 */

package no.skde.tools.report;

import org.apache.log4j.Logger;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.fill.*;

public class ProcessShellScriptRapporteket extends JRDefaultScriptlet {
	
	static Logger log = Logger.getLogger("report");
	
	
	public void afterReportInit() throws JRScriptletException {
		log.debug(ProcessShellScriptRapporteket.class.getName() + ": Scriptlet started, 'afterReportInit()'...");
		runShellScript();
		super.afterReportInit();
	}
	
	private void runShellScript() {
		
		log.info("Running shell script initiated by report");
		
		String shellScriptPathAndName = "";
		String shellScriptParams = "";
		
		try {
			shellScriptPathAndName = (String) ((JRFillParameter) parametersMap.get("shellScriptPathAndName")).getValue();
			shellScriptParams = (String) ((JRFillParameter) parametersMap.get("shellScriptParams")).getValue();
			log.info("Script to be run: " + shellScriptPathAndName);
			log.info("Param(s) provided: " + shellScriptParams);
		} catch (Exception e) {
			log.error("Mandatory parameters in the report definition calling this scriptlet were not defined: " + e.getMessage());
		}
		
		String[] env = {"PATH=/bin:/usr/bin/"};
		
		try {
			String cmd = shellScriptPathAndName + " " + shellScriptParams;
			Process process = Runtime.getRuntime().exec(cmd, env);
			process.waitFor();
			log.info("Shell script was run");
		} catch (Exception e) {
			log.error("Shell scipt was not run: " + e.getMessage());
		}
		
	}
	
}