/**
 * no.skde.tools.report
 * CommonReport.java Nov 15 2017 Are Edvardsen, Kevin Thon
 * 
 * Common scriptlet to replace 'all' registry specific scriptlets
 * 
 * Copyleft 2017 SKDE
 */

package no.skde.tools.report;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.fill.*;

import org.apache.log4j.Logger;
import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class CommonReport extends JRDefaultScriptlet {

	protected RConnection rconn;
	private String fileName;
	
	static Logger log = Logger.getLogger("report");
	
	// getters and setters
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	
	// override empty method of JRDefaultScriptlet
	public void afterReportInit() throws JRScriptletException {
		log.debug("scriptlet started, 'afterReportInit()'...");
		generateReport();

		super.afterReportInit();
	}
	
	
	// report actions
	// Probably because List array of type string cannot know what will be returned by the JRFillParameter
	@SuppressWarnings("unchecked")
	private void generateReport() {
		try {
			log.info("Start generating R report using " + CommonReport.class.getName());
			
			//TODO
			// Make log entry if class is built as a snapshot. SET MANUALLY!
			boolean classIsSnapshot = false;
			if (classIsSnapshot) {
				log.warn(CommonReport.class.getName() + " is a snapshot. Not to be used in production environment");
			}
			
			// Create the connection
			log.debug("Getting connection to R instance...");
			rconn = new RConnection();
			log.debug("R connection provided: " + rconn.toString());


			// Get parameters
			// these must always be provided when this scriptlet class is used
			String loggedInUserFullName = "";
			String loggedInUserAVD_RESH = "";
			String reportName = "";
			String rFunctionCallString = "";
			try {
				loggedInUserFullName = (String) ((JRFillParameter) parametersMap.get("LoggedInUserFullName")).getValue();
				loggedInUserAVD_RESH = (String) ((JRFillParameter) parametersMap.get("LoggedInUserAVD_RESH")).getValue();
				reportName = (String) ((JRFillParameter) parametersMap.get("reportName")).getValue();
				rFunctionCallString = (String) ((JRFillParameter) parametersMap.get("rFunctionCallString")).getValue();
				log.info("Report to be run: " + reportName);
				log.info("Report requested by JRS user " + loggedInUserFullName + ", AVD_RESH " + loggedInUserAVD_RESH);
				log.debug("R function call string: " + rFunctionCallString);
				rconn.voidEval("reshID=" + loggedInUserAVD_RESH);
			} catch (Exception e) {
				log.error("Mandatory parameters in the report definition calling this scriptlet were not defined: " + e.getMessage());
			}
			
			// the rest of parameters are optional, but must match whatever needed by R
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
			
			
			// START specific, common report user controls
			Integer minAge;
			try {
				minAge = (Integer) ((JRFillParameter) parametersMap.get("minAge")).getValue();
				if (minAge == null) {
					minAge = 0;
				}
				rconn.voidEval("minald=" + minAge.toString());
			} catch (Exception e) {
				log.debug("Parameter minAge is not defined: " + e.getMessage());
			}

			Integer maxAge;
			try {
				maxAge = (Integer) ((JRFillParameter) parametersMap.get("maxAge")).getValue();
				if (maxAge == null) {
					maxAge = 130;
				}
				rconn.voidEval("maxald=" + maxAge.toString());
			} catch (Exception e) {
				log.debug("Parameter maxAge is not defined: " + e.getMessage());
			}

			
			// convert dates to something that can be understood by R
			SimpleDateFormat rFormat = new SimpleDateFormat("yyyy-MM-dd");
			
			Date beginDate;
			try {
				beginDate = (Date) ((JRFillParameter) parametersMap.get("beginDate")).getValue();
				if (beginDate == null) {
					beginDate = new SimpleDateFormat("yyyy-MM-dd").parse("2010-01-01");
				}
				StringBuilder beginDateString = new StringBuilder(rFormat.format(beginDate));
				rconn.voidEval("datoFra=" + "'" + beginDateString + "'");
			} catch (Exception e) {
				log.debug("Parameter beginDate is not defined: " + e.getMessage());
			}

			Date endDate;
			try {
				endDate = (Date) ((JRFillParameter) parametersMap.get("endDate")).getValue();
				if (endDate == null) {
					endDate = new Date();
				}
				StringBuilder endDateString = new StringBuilder(rFormat.format(endDate));
				rconn.voidEval("datoTil=" + "'" + endDateString + "'");
			} catch (Exception e) {
				log.debug("Parameter endDate is not defined: " + e.getMessage());
			}			
			
			Integer erMann;
			try {
				erMann = (Integer) ((JRFillParameter) parametersMap.get("erMann")).getValue();
				if (erMann == null) {
					erMann = 99;
				}
				rconn.voidEval("erMann=" + erMann.toString());
			} catch (Exception e) {
				log.debug("Parameter erMann is not defined: " + e.getMessage());
			}

			String statMeasureMethod;
			try {
				statMeasureMethod = (String) ((JRFillParameter) parametersMap.get("statMeasureMethod")).getValue();
				if (statMeasureMethod == null) {
					statMeasureMethod = "Gjsn";
				}
				rconn.voidEval("valgtMaal=" + "'" + statMeasureMethod.toString() + "'");
			} catch (Exception e) {
				log.debug("Parameter statMeasureMethod is not defined: " + e.getMessage());
			}
			
			Integer orgUnitSelection;
			try {
				orgUnitSelection =  (Integer) ((JRFillParameter) parametersMap.get("orgUnitSelection")).getValue();
				if (orgUnitSelection == null) {
					orgUnitSelection = 1;
				}
				rconn.voidEval("enhetsUtvalg=" + orgUnitSelection.toString());
			} catch (Exception e) {
				log.debug("Parameter orgUnitSelection is not defined: " + e.getMessage());
			}
			
			String valgtVar;
			try {
				log.debug("Getting parameter values");
				valgtVar = (String) ((JRFillParameter) parametersMap.get("valgtVar")).getValue();
				if (valgtVar == null) {
					valgtVar = "nada";
				}
				rconn.voidEval("valgtVar=" + "'" + valgtVar + "'");
			} catch (Exception e) {
				log.debug("Parameter valgtVar is not defined: " + e.getMessage());
			}
			// END common report user controls
			
			
			// START generic, common report user controls
			List<String> multiValgList = new ArrayList<String>();
			String multiValg_1;
			try {
				multiValgList = (List<String>) ((JRFillParameter) parametersMap.get("multiValg_1")).getValue();
				multiValg_1 = "c(";
				if (multiValgList.isEmpty()) {
					multiValg_1 = multiValg_1 + "'')";
				} else {
					Iterator<String> iterator = multiValgList.iterator();
					while (iterator.hasNext()) {
						multiValg_1 = multiValg_1 + "'" + iterator.next() + "',";
					}
					multiValg_1 = multiValg_1.substring(0, multiValg_1.length()-1);
					multiValg_1 = multiValg_1 + ")";
				}
				log.debug("R concat for multiValg_1 vector is " + multiValg_1);
				rconn.voidEval("multiValg_1=" + multiValg_1);
			} catch (Exception e) {
				log.debug("Parameter multiValg_1 is not defined: " + e.getMessage());
			}

			Integer enkeltValgInteger_1;
			try {
				enkeltValgInteger_1 = (Integer) ((JRFillParameter) parametersMap.get("enkeltValgInteger_1")).getValue();
				if (enkeltValgInteger_1 == null) {
					enkeltValgInteger_1 = 99;
				}
				rconn.voidEval("enkeltValgInteger_1=" + enkeltValgInteger_1.toString());
			} catch (Exception e) {
				log.debug("Parameter enkeltValgInteger_1 is not defined: " + e.getMessage());
			}

			String enkeltValgStreng_1;
			try {
				log.debug("Getting parameter values");
				enkeltValgStreng_1 = (String) ((JRFillParameter) parametersMap.get("enkeltValgStreng_1")).getValue();
				if (enkeltValgStreng_1 == null) {
					enkeltValgStreng_1 = "nada";
				}
				rconn.voidEval("enkeltValgStreng_1=" + "'" + enkeltValgStreng_1 + "'");
			} catch (Exception e) {
				log.debug("Parameter enkeltValgStreng_1 is not defined: " + e.getMessage());
			}

			Integer heltall_1;
			try {
				heltall_1 = (Integer) ((JRFillParameter) parametersMap.get("heltall_1")).getValue();
				if (heltall_1 == null) {
					heltall_1 = 0;
				}
				rconn.voidEval("heltall_1=" + heltall_1.toString());
			} catch (Exception e) {
				log.debug("Parameter heltall_1 is not defined: " + e.getMessage());
			}

			Double desimaltall_1;
			try {
				desimaltall_1 = (Double) ((JRFillParameter) parametersMap.get("desimaltall_1")).getValue();
				if (desimaltall_1 == null) {
					desimaltall_1 = 0.0;
				}
				rconn.voidEval("desimaltall_1=" + desimaltall_1.toString());
			} catch (Exception e) {
				log.debug("Parameter desimaltall_1 is not defined: " + e.getMessage());
			}

			Date dato_1;
			try {
				dato_1 = (Date) ((JRFillParameter) parametersMap.get("dato_1")).getValue();
				if (dato_1 == null) {
					dato_1 = new Date();
				}
				StringBuilder datoString = new StringBuilder(rFormat.format(dato_1));
				rconn.voidEval("dato_1=" + "'" + datoString + "'");
			} catch (Exception e) {
				log.debug("Parameter dato_1 is not defined: " + e.getMessage());
			}
			// END generic, common report user controls
			  
			
			// Set up the tmp directory, file names and reportUserInfo
			String tmpdir = "";
			String p_filename = "";
			log.debug("Setting report image filepath and name");
			tmpdir = "/opt/jasper/img/";
			File dirFile = new File(tmpdir);
			String fileBaseName = "noRGast_" + reportName + "-";
			String file = (File.createTempFile(fileBaseName, ".png", dirFile)).getName();
			p_filename = tmpdir + file;
			log.debug("In R instance: image to be stored as: " + p_filename);
			setFileName(p_filename);
	
			log.debug("In R instance: assigning Filename: " + p_filename);
			rconn.assign("outfile", p_filename);
			
			String rcmd = rFunctionCallString;

			// Call the function to generate the report
			log.debug("In R instance: calling function");
			rconn.voidEval(rcmd);
			
			// Close RServ connection, ensure garbage collection removes pointer too!
			log.debug("Closing connection to R instance and removing pointer");
			rconn.close();
			rconn = null;
			log.info("Finished report");

		} catch (RserveException rse) {
			log.error("Rserve exception: " + rse.getMessage());
			log.error("Gory details in Rserve.log");
			rconn.close();
			rconn = null;
		} catch (Exception e) {
			log.error("Something went wrong, but it's not the Rserve: " + e.getMessage());
			e.printStackTrace();
			rconn.close();
			rconn = null;
		}
	}
}
