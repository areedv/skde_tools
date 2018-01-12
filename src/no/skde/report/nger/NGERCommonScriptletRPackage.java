/**
 * no.skde.report.nger
 * NGERCommonScriptletRPackage.java Feb 23 2016 Are Edvardsen
 * 
 * 
 *  Copyleft 2016 SKDE
 */

package no.skde.report.nger;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.fill.*;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class NGERCommonScriptletRPackage extends JRDefaultScriptlet {

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
	
	// Probably because List array of type string cannot know what will be returned by the JRFillParameter
	@SuppressWarnings("unchecked")

	// report actions
	private void generateReport() {
		try {
			log.info("Start generating R report using " + NGERCommonScriptletRPackage.class.getName());
			
			//TODO
			// Make log entry if class is built as a snapshot. SET MANUALLY!
			boolean classIsSnapshot = false;
			if (classIsSnapshot) {
				log.warn(NGERCommonScriptlet.class.getName() + " is a snapshot. Not to be used in production environment");
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
			
			String varName;
			try {
				log.debug("Getting parameter values");
				varName = (String) ((JRFillParameter) parametersMap.get("varName")).getValue();
				if (varName == null) {
					varName = "nada";
				}
				rconn.voidEval("valgtVar=" + "'" + varName + "'");
			} catch (Exception e) {
				log.debug("Parameter varName is not defined: " + e.getMessage());
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
			
			Integer myDept;
			try {
				myDept = (Integer) ((JRFillParameter) parametersMap.get("myDept")).getValue();
				if (myDept == null) {
					myDept = 1;
				}
				rconn.voidEval("egenavd=" + myDept.toString());
			} catch (Exception e) {
				log.debug("Parameter myDept is not defined: " + e.getMessage());
			}
			
			Integer opGroup;
			try {
				opGroup = (Integer) ((JRFillParameter) parametersMap.get("opGroup")).getValue();
				if (opGroup == null) {
					opGroup = 0;
				}
				rconn.voidEval("op_gruppe=" + opGroup.toString());
			} catch (Exception e) {
				log.debug("Parameter opGroup is not defined: " + e.getMessage());
			}
			
			Integer minASA;
			try {
				minASA = (Integer) ((JRFillParameter) parametersMap.get("minASA")).getValue();
				if (minASA == null) {
					minASA = 1;
				}
				rconn.voidEval("minASA=" + minASA.toString());
			} catch (Exception e) {
				log.debug("Parameter minASA is not defined: " + e.getMessage());
			}
			
			Integer maxASA;
			try {
				maxASA = (Integer) ((JRFillParameter) parametersMap.get("maxASA")).getValue();
				if (maxASA == null) {
					maxASA = 5;
				}
				rconn.voidEval("maxASA=" + maxASA.toString());
			} catch (Exception e) {
				log.debug("Parameter maxASA is not defined: " + e.getMessage());
			}
			
			Integer minCom;
			try {
				minCom = (Integer) ((JRFillParameter) parametersMap.get("minCom")).getValue();
				if (minCom == null) {
					minCom = 1;
				}
				rconn.voidEval("minCom=" + minCom.toString());
			} catch (Exception e) {
				log.debug("Parameter minCom is not defined: " + e.getMessage());
			}
			
			Integer maxCom;
			try {
				maxCom = (Integer) ((JRFillParameter) parametersMap.get("maxCom")).getValue();
				if (maxCom == null) {
					maxCom = 4;
				}
				rconn.voidEval("maxCom=" + maxCom.toString());
			} catch (Exception e) {
				log.debug("Parameter maxCom is not defined: " + e.getMessage());
			}
			
			Integer minBMI;
			try {
				minBMI = (Integer) ((JRFillParameter) parametersMap.get("minBMI")).getValue();
				if (minBMI == null) {
					minBMI = 0;
				}
				rconn.voidEval("minBMI=" + minBMI.toString());
			} catch (Exception e) {
				log.debug("Parameter minBMI is not defined: " + e.getMessage());
			}
			
			Integer maxBMI;
			try {
				maxBMI = (Integer) ((JRFillParameter) parametersMap.get("maxBMI")).getValue();
				if (maxBMI == null) {
					maxBMI = 100;
				}
				rconn.voidEval("maxBMI=" + maxBMI.toString());
			} catch (Exception e) {
				log.debug("Parameter maxBMI is not defined: " + e.getMessage());
			}
			
			String stack;
			try {
				stack = (String) ((JRFillParameter) parametersMap.get("stack")).getValue();
				if (stack == "") {
					stack = "T";
				}
				rconn.voidEval("stabel=" + stack);
			} catch (Exception e) {
				log.debug("Parameter stack is not defined: " + e.getMessage());
			}
			
			String ratio;
			try {
				ratio = (String) ((JRFillParameter) parametersMap.get("ratio")).getValue();
				if (ratio == "") {
					ratio = "F";
				}
				rconn.voidEval("andel=" + ratio);
			} catch (Exception e) {
				log.debug("Parameter ratio is not defined: " + e.getMessage());
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
			
			Integer MCEType;
			try {
				MCEType = (Integer) ((JRFillParameter) parametersMap.get("MCEType")).getValue();
				if (MCEType == null) {
					MCEType = 99;
				}
				log.debug("Parameter MCEType, value to be set in R session: " + MCEType.toString());
				rconn.voidEval("MCEType=" + MCEType.toString());
			} catch (Exception e) {
				log.debug("Parameter MCEType is not defined: " + e.getMessage());
			}
			
			Integer velgDiag;
			try {
				velgDiag = (Integer) ((JRFillParameter) parametersMap.get("velgDiag")).getValue();
				if (velgDiag == null) {
					velgDiag = 0;
				}
				log.debug("Parameter velgDiag, value to be set in R session: " + velgDiag.toString());
				rconn.voidEval("velgDiag=" + velgDiag.toString());
			} catch (Exception e) {
				log.debug("Parameter velgDiag is not defined: " + e.getMessage());
			}
			
			// velgAvd; multi select list of values
			List<String> velgAvdList = new ArrayList<String>();
			String velgAvd;
			try {
				velgAvdList = (List<String>) ((JRFillParameter) parametersMap.get("velgAvd")).getValue();
				velgAvd = "c(";
				if (velgAvdList.isEmpty()) {
					velgAvd = velgAvd + "'')";
				} else {
					Iterator<String> iterator = velgAvdList.iterator();
					while (iterator.hasNext()) {
						velgAvd = velgAvd + "'" + iterator.next() + "',";
					}
					velgAvd = velgAvd.substring(0, velgAvd.length()-1);
					velgAvd = velgAvd + ")";
				}
				log.debug("R concat for velgAvd vector is " + velgAvd);
				rconn.voidEval("velgAvd=" + velgAvd);
			} catch (Exception e) {
				log.debug("Parameter velgAvd is not defined: " + e.getMessage());
			}
			
			// AlvorlighetKompl; multi select list of values
			List<String> AlvorlighetKomplList = new ArrayList<String>();
			String AlvorlighetKompl;
			try {
				AlvorlighetKomplList = (List<String>) ((JRFillParameter) parametersMap.get("AlvorlighetKompl")).getValue();
				AlvorlighetKompl = "c(";
				if (AlvorlighetKomplList.isEmpty()) {
					AlvorlighetKompl = AlvorlighetKompl + "'')";
				} else {
					Iterator<String> iterator = AlvorlighetKomplList.iterator();
					while (iterator.hasNext()) {
						AlvorlighetKompl = AlvorlighetKompl + "'" + iterator.next() + "',";
					}
					AlvorlighetKompl = AlvorlighetKompl.substring(0, AlvorlighetKompl.length()-1);
					AlvorlighetKompl = AlvorlighetKompl + ")";
				}
				log.debug("R concat for AlvorlighetKompl vector is " + AlvorlighetKompl);
				rconn.voidEval("AlvorlighetKompl=" + AlvorlighetKompl);
			} catch (Exception e) {
				log.debug("Parameter AlvorlighetKompl is not defined: " + e.getMessage());
			}

			//Hastegrad; multi select list of values
			List<String> HastegradList = new ArrayList<String>();
			String Hastegrad;
			try {
				HastegradList = (List<String>) ((JRFillParameter) parametersMap.get("Hastegrad")).getValue();
				Hastegrad = "c(";
				// if (HastegradList.contains("all")) {
				if (HastegradList.isEmpty()) {
					Hastegrad = Hastegrad + "'')";
				} else {
					Iterator<String> iterator = HastegradList.iterator();
					while (iterator.hasNext()) {
						Hastegrad = Hastegrad + "'" + iterator.next() + "',";
					}
					Hastegrad = Hastegrad.substring(0, Hastegrad.length()-1);
					Hastegrad = Hastegrad + ")";
				}
				log.debug("R concat for Hastegrad vector is " + Hastegrad);
				rconn.voidEval("Hastegrad=" + Hastegrad);
			} catch (Exception e) {
				log.debug("Parameter Hastegrad is not defined: " + e.getMessage());
			}
			
			// Now, removed loading of data through this scriptlet
			log.info("RegData is no longer provided by NGER scriptlets");


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
			String fileBaseName = "NGER_" + reportName + "-";
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
