/**
 * no.skde.report.nkr
 * DegenerativRyggCommonScriptlet.java Dec 19 2013 Are Edvardsen
 * 
 * 
 *  Copyleft 2013, 2014, 2016 SKDE
 */


package no.skde.report.nkr;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.fill.*;

import org.apache.log4j.Logger;
import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.*;

public class DegenerativRyggCommonScriptletRPackage extends JRDefaultScriptlet {
	
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
	private void generateReport() {
		
		try {
			log.info("Start generating R report using " + DegenerativRyggCommonScriptletRPackage.class.getName());
			
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
			
			try {
				String varName = (String) ((JRFillParameter) parametersMap.get("varName")).getValue();
				if (varName == "") {
					varName = "EMPTY";
				}
				log.debug("Prameter 'varName' mapped to value: " + varName);
				// kept for compatibility of older R-code
				rconn.voidEval("variabel=" + "'" + varName + "'");
				// same, according to current standard
				rconn.voidEval("valgtVar=" + "'" + varName + "'");
			} catch (Exception e) {
				log.debug("Parameter 'varName' is not provided: " + e.getMessage());
			}

			// replicate above but different parameter name
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
			
			try {
				Integer mainCat = (Integer) ((JRFillParameter) parametersMap.get("mainCat"))
						.getValue();
				if (mainCat == null) {
					mainCat = 99;
				}
				log.debug("Parameter 'mainCat' mapped to value: " + mainCat.toString());
				rconn.voidEval("hovedkat=" + mainCat.toString());
			} catch (Exception e) {
				log.debug("Parameter 'mainCat' is not provided: " + e.getMessage());
			}
			
			try {
				Integer year = (Integer) ((JRFillParameter) parametersMap.get("qYearNkr")).getValue();
				if (year == null) {
					year = 0;
				}
				log.debug("Parameter 'qYearNkr' mapped to value: " + year.toString());
				rconn.voidEval("aar=" + year.toString());
			} catch (Exception e) {
				log.debug("Parameter 'qYearNkr' is not provided: " + e.getMessage());
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
					beginDate = new SimpleDateFormat("yyyy-MM-dd").parse("2007-01-01");
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
			
			try {
				Integer dispNumberInfo = (Integer) ((JRFillParameter) parametersMap.get("dispNumberInfo")).getValue();
				if (dispNumberInfo == null) {
					dispNumberInfo = 0;
				}
				log.debug("Parameter 'dispNumberInfo mapped to value: " + dispNumberInfo.toString());
				rconn.voidEval("medTall=" + dispNumberInfo.toString());
			} catch (Exception e) {
				log.debug("Parameter 'dispNumberInfo' is not provided: " + e.getMessage());
			}
			 
			try {
				Integer surgHistory = (Integer) ((JRFillParameter) parametersMap.get("surgHistory")).getValue();
				if (surgHistory == null) {
					surgHistory = 0;
				}
				log.debug("Parameter 'surgHistory' mapped to value: " + surgHistory.toString());
				rconn.voidEval("tidlOp=" + surgHistory.toString());
			} catch (Exception e) {
				log.debug("Parameter 'surgHistory' is not provided: " + e.getMessage());
			}
			
			try {
				Integer postControl = (Integer) ((JRFillParameter) parametersMap.get("postControl")).getValue();
				if (postControl == null) {
					postControl = 1;
				}
				log.debug("Parameter 'postControl' mapped to value: " + postControl.toString());
				rconn.voidEval("ktr=" + postControl.toString());
			} catch (Exception e) {
				log.debug("Parameter 'postControl' is not proivided: " + e.getMessage());
			}
			
			try {
				String plotType = (String) ((JRFillParameter) parametersMap.get("plotType")).getValue();
				if (plotType == "") {
					plotType = "S";
				}
				log.debug("Parameter 'plotType' mapped to value: " + plotType);
				String test = "plotType=" + "'" + plotType + "'";
				log.debug("String to evaluated by R session: " + test);
				rconn.voidEval("plotType=" + "'" + plotType + "'");
			} catch (Exception e) {
				log.debug("Parameter 'plotType' is not provided: " + e.getMessage());
			}
			
			try {
				Integer hospitalType = (Integer) ((JRFillParameter) parametersMap.get("hospitalType")).getValue();
				if (hospitalType == null) {
					hospitalType = 1;
				}
				log.debug("Parameter 'hospitalType' mapped to value: " + hospitalType.toString());
				rconn.voidEval("shtype=" + hospitalType.toString());
			} catch (Exception e) {
				log.debug("Parameter 'hospitalType' is not proivided: " + e.getMessage());
			}
			
			String statMeasureMethod;
			try {
				statMeasureMethod = (String) ((JRFillParameter) parametersMap.get("statMeasureMethod")).getValue();
				if (statMeasureMethod == null) {
					statMeasureMethod = "Gjsn";
				}
				log.debug("'Gjsn' set to: " + statMeasureMethod);
				rconn.voidEval("valgtMaal=" + "'" + statMeasureMethod.toString() + "'");
			} catch (Exception e) {
				log.debug("Parameter statMeasureMethod is not defined: " + e.getMessage());
			}
			
			// Now, removed loading of data through this scriptlet
			log.info("RegData is no longer provided by this scriptlet");


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
			String fileBaseName = "degenerativRygg_" + reportName + "-";
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