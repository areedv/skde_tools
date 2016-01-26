/**
 * no.skde.tools.report
 * RunRFunction.java Jan 22 2016 Are Edvardsen
 * 
 * Generic scriptlet for running R function from reports at Rapporteket.
 * Any output from R function called by this scriptlet is assumed to be
 * text messages that is to be returned to (and used in) the report.
 * Thus, returning large objects (e.g. data sets) might not be suitable
 * here...
 *   
 * Copyleft 2016 SKDE
 */

package no.skde.tools.report;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.fill.*;
import org.apache.log4j.Logger;
import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.*;


public class RunRFunction extends JRDefaultScriptlet {
	protected RConnection rconn;
	
	private String rFunctionMessage;
	
	static Logger log = Logger.getLogger("report");
	
	
	// getters and setters
	public String getRFunctionMessage() {
		return rFunctionMessage;
	}
	
	public void setRFunctionMessage(String rFunctionMessage) {
		this.rFunctionMessage = rFunctionMessage;
	}
	
	
	// override empty method of JRDefaultScriptlet
	public void afterReportInit() throws JRScriptletException {
		
		generateReport();

		super.afterReportInit();
	}
	
	
	// report actions
	private void generateReport() {
		try {
			log.info("Start generating report using " + RunRFunction.class.getName());
			
			rconn = new RConnection();
			log.debug("R connection provided: " + rconn.toString());
			
			// get report parameters
			log.debug("Getting report parameters...");
			
			String rPackageName;
			try {
				rPackageName = (String) ((JRFillParameter) parametersMap.get("rPackageName")).getValue();
				if (rPackageName == null || rPackageName == "") {
					rPackageName = "";
					log.warn("Parameter rPackageName is empty. No R package will be loaded");
				} else {
					log.info("Package to be loaded in the R session: " + rPackageName);
					rconn.voidEval("require(" + rPackageName + ")");
				}
			} catch (Exception e) {
				log.warn("No package loaded in R session: " + e.getMessage());
			}
			
			String rFunctionName = (String) ((JRFillParameter) parametersMap.get("rFunctionName")).getValue();
			if (rFunctionName == "") {
				log.warn("rFunctionName is empty. Eventualley, the report will fail. Fix the report definition (jrxml)");
			}
			else {
				log.debug("rFunctionName: " + rFunctionName);
			}
			
			String rFunctionParams = (String) ((JRFillParameter) parametersMap.get("rFunctionParams")).getValue();
			if (rFunctionName == "") {
				log.warn("rFunctionParams is empty. This might be ok, though...");
			}
			else {
				log.debug("rFunctionPrams: " + rFunctionParams);
			}
								
			// make the R function call
			String functionCall = "message <- " + rFunctionName + "(" + rFunctionParams + ")";
			log.debug("R function call: " + functionCall);
			
			// run function
			log.debug("Running function and assigning output to 'message'");
			rconn.voidEval(functionCall);
			log.debug("Fetching 'message' to scriptlet");
			REXP functionMessage = rconn.eval("message");
			log.debug("Setting message in scriptlet");
			setRFunctionMessage(functionMessage.asString());
			
			rconn.close();
			rconn = null;
		} catch (RserveException rse) {
			log.error("Rserv exception " + rse.getMessage());
			rconn.close();
			rconn = null;
		} catch (Exception e) {
			log.error("Something went wrong, but it is not Rserv: " + e.getMessage());
			rconn.close();
			rconn = null;
		}
		
	}
	
}
