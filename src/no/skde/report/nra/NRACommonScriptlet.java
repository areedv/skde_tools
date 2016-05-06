/**
 * no.skde.report.nra
 * nraCommonScriptletRPackage.java Jan 26 2016 Are Edvardsen
 * 
 * 
 *  Copyleft 2016 SKDE
 */

package no.skde.report.nra;

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

public class NRACommonScriptlet extends JRDefaultScriptlet {

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
			log.info("Start generating R report using " + NRACommonScriptlet.class.getName());
			
			//TODO
			// Make log entry if class is built as a snapshot. SET MANUALLY!
			boolean classIsSnapshot = true;
			if (classIsSnapshot) {
				log.warn(NRACommonScriptlet.class.getName() + " is a snapshot. Not to be used in production environment");
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


			// valgtShus; multi select list of values
			List<String> valgtShusList = new ArrayList<String>();
			String valgtShus;
			try {
				valgtShusList = (List<String>) ((JRFillParameter) parametersMap.get("valgtShus")).getValue();
				valgtShus = "c(";
				if (valgtShusList.isEmpty()) {
					valgtShus = valgtShus + "'')";
				} else {
					Iterator<String> iterator = valgtShusList.iterator();
					while (iterator.hasNext()) {
						valgtShus = valgtShus + "'" + iterator.next() + "',";
					}
					valgtShus = valgtShus.substring(0, valgtShus.length()-1);
					valgtShus = valgtShus + ")";
				}
				log.debug("R concat for valgtShus vector is " + valgtShus);
				rconn.voidEval("valgtShus=" + valgtShus);
			} catch (Exception e) {
				log.debug("Parameter valgtShus is not defined: " + e.getMessage());
			}
			
			// forlopstype1; multi select list of values
			List<String> forlopstype1List = new ArrayList<String>();
			String forlopstype1;
			try {
				forlopstype1List = (List<String>) ((JRFillParameter) parametersMap.get("forlopstype1")).getValue();
				forlopstype1 = "c(";
				if (forlopstype1List.isEmpty()) {
					forlopstype1 = forlopstype1 + "'')";
				} else {
					Iterator<String> iterator = forlopstype1List.iterator();
					while (iterator.hasNext()) {
						forlopstype1 = forlopstype1 + "'" + iterator.next() + "',";
					}
					forlopstype1 = forlopstype1.substring(0, forlopstype1.length()-1);
					forlopstype1 = forlopstype1 + ")";
				}
				log.debug("R concat for forlopstype1 vector is " + forlopstype1);
				rconn.voidEval("forlopstype1=" + forlopstype1);
			} catch (Exception e) {
				log.debug("Parameter forlopstype1 is not defined: " + e.getMessage());
			}
			
			// forlopstype2; multi select list of values
			List<String> forlopstype2List = new ArrayList<String>();
			String forlopstype2;
			try {
				forlopstype2List = (List<String>) ((JRFillParameter) parametersMap.get("forlopstype2")).getValue();
				forlopstype2 = "c(";
				if (forlopstype2List.isEmpty()) {
					forlopstype2 = forlopstype2 + "'')";
				} else {
					Iterator<String> iterator = forlopstype2List.iterator();
					while (iterator.hasNext()) {
						forlopstype2 = forlopstype2 + "'" + iterator.next() + "',";
					}
					forlopstype2 = forlopstype2.substring(0, forlopstype2.length()-1);
					forlopstype2 = forlopstype2 + ")";
				}
				log.debug("R concat for forlopstype2 vector is " + forlopstype2);
				rconn.voidEval("forlopstype2=" + forlopstype2);
			} catch (Exception e) {
				log.debug("Parameter forlopstype2 is not defined: " + e.getMessage());
			}

			Integer inkl_konf;
			try {
				inkl_konf = (Integer) ((JRFillParameter) parametersMap.get("inkl_konf")).getValue();
				if (inkl_konf == null) {
					inkl_konf = 99;
				}
				rconn.voidEval("inkl_konf=" + inkl_konf.toString());
			} catch (Exception e) {
				log.debug("Parameter inkl_konf is not defined: " + e.getMessage());
			}

			String tidsenhet;
			try {
				log.debug("Getting parameter values");
				tidsenhet = (String) ((JRFillParameter) parametersMap.get("tidsenhet")).getValue();
				if (tidsenhet == null) {
					tidsenhet = "Aar";
				}
				rconn.voidEval("tidsenhet=" + "'" + tidsenhet + "'");
			} catch (Exception e) {
				log.debug("Parameter tidsenhet is not defined: " + e.getMessage());
			}
			
			Integer sammenlign;
			try {
				sammenlign = (Integer) ((JRFillParameter) parametersMap.get("sammenlign")).getValue();
				if (sammenlign == null) {
					sammenlign = 1;
				}
				rconn.voidEval("sammenlign=" + sammenlign.toString());
			} catch (Exception e) {
				log.debug("Parameter sammenlign is not defined: " + e.getMessage());
			}
			// ---
			
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
			
			
			// Now, removed loading of data through this scriptlet
			log.info("RegData is no longer provided by nra scriptlets");


			log.debug("Creating dummy R dataframe to ensure compatibility with existing R scripts");

			RList l = new RList();

			// anything goes...
			l.put("Rapportnavn", new REXPString(reportName));
			
			REXP df = REXP.createDataFrame(l);
			log.debug("Assigning data frame to R instance");
			rconn.assign("RegData", df);
			
			
			// Set up the tmp directory, file names and reportUserInfo
			String tmpdir = "";
			String p_filename = "";
			log.debug("Setting report image filepath and name");
			tmpdir = "/opt/jasper/img/";
			File dirFile = new File(tmpdir);
			String fileBaseName = "nra_" + reportName + "-";
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