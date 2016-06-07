/**
 * no.skde.tools.report
 * NirCommonScriptletRPackage.java Jun 6 2016 Are Edvardsen
 * 
 * 
 *  Copyleft 2016 SKDE
 */

package no.skde.report.nir;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.fill.*;

import org.apache.log4j.Logger;
import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.*;



public class NirCommonScriptletRPackage extends JRDefaultScriptlet
{
	
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
			log.info("Start generating report " + NirCommonScriptletRPackage.class.getName());
			
			// Create the connection
			log.debug("Getting R connection...");
			rconn = new RConnection();
			log.debug("R connection: " + rconn.toString());


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
				log.debug("'valgtVar' set to: " + varName);
				rconn.voidEval("valgtVar=" + "'" + varName + "'");
			} catch (Exception e) {
				log.debug("Parameter 'varName' is not defined: " + e.getMessage());
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
			
			Integer minAge;
			try {
				minAge = (Integer) ((JRFillParameter) parametersMap.get("minAge")).getValue();
				if (minAge == null) {
					minAge = 0;
				}
				log.debug("'minald' set to :" + minAge.toString());
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
				log.debug("'maxald' set to: " + maxAge.toString());
				rconn.voidEval("maxald=" + maxAge.toString());
			} catch (Exception e) {
				log.debug("Parameter maxAge is not defined: " + e.getMessage());
			}

			
			// convert dates to something that can be understood by R
			SimpleDateFormat rFormat = new SimpleDateFormat("yyyy-MM-dd");
			
			Date beginDate;
			try {
				beginDate = (Date) ((JRFillParameter) parametersMap.get("startDate")).getValue();
				if (beginDate == null) {
					beginDate = new SimpleDateFormat("yyyy-MM-dd").parse("2010-01-01");
				}
				StringBuilder startDateString = new StringBuilder(rFormat.format(beginDate));
				log.debug("'datoFra' set to: " + startDateString);
				rconn.voidEval("datoFra=" + "'" + startDateString + "'");
			} catch (Exception e) {
				log.debug("Parameter startDate is not defined: " + e.getMessage());
			}

			Date endDate;
			try {
				endDate = (Date) ((JRFillParameter) parametersMap.get("endDate")).getValue();
				if (endDate == null) {
					endDate = new Date();
				}
				StringBuilder endDateString = new StringBuilder(rFormat.format(endDate));
				log.debug("'datoTil' set to: " + endDateString);
				rconn.voidEval("datoTil=" + "'" + endDateString + "'");
			} catch (Exception e) {
				log.debug("Parameter endDate is not defined: " + e.getMessage());
			}
			
			String hospitalsLevel;
			try {
				hospitalsLevel = (String) ((JRFillParameter) parametersMap.get("hospitalsLevel")).getValue();
				if (hospitalsLevel == null) {
					hospitalsLevel = "region";
				}
				log.debug("'shType' set to: " + hospitalsLevel);
				rconn.voidEval("ShType=" + "'" + hospitalsLevel.toString() + "'");
			} catch (Exception e) {
				log.debug("Parameter hospitalsLevel is not defined: " + e.getMessage());
			}
			
			// the above might be deprecated, new one below
			Integer grType;
			try {
				grType = (Integer) ((JRFillParameter) parametersMap.get("grType")).getValue();
				if (grType == null) {
					grType = 99;
				}
				log.debug("'grType' set to: " + grType.toString());
				rconn.voidEval("grType=" + grType.toString());
			} catch (Exception e) {
				log.debug("Parameter grType is not defined: " + e.getMessage());
			}
			
			Integer inFromSituation;
			try {
				inFromSituation = (Integer) ((JRFillParameter) parametersMap.get("inFromSituation")).getValue();
				if (inFromSituation == null) {
					inFromSituation = 99;
				}
				log.debug("'InnMaate' set to: " + inFromSituation.toString());
				rconn.voidEval("InnMaate=" + inFromSituation.toString());
			} catch (Exception e) {
				log.debug("Parameter inFromSituation is not defined: " + e.getMessage());
			}
			
			
			Integer vitalStatus;
			try {
				vitalStatus = (Integer) ((JRFillParameter) parametersMap.get("vitalStatus")).getValue();
				if (vitalStatus == null) {
					vitalStatus = 99;
				}
				log.debug("'dodInt' set to: " + vitalStatus.toString());
				rconn.voidEval("dodInt=" + "'" + vitalStatus.toString() + "'");
			} catch (Exception e) {
				log.debug("Parameter vitalStatus is not defined: " + e.getMessage());
			}
			
			Integer erMann;
			try {
				erMann = (Integer) ((JRFillParameter) parametersMap.get("erMann")).getValue();
				if (erMann == null) {
					erMann = 99;
				}
				log.debug("'erMann' set to: " + erMann.toString());
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
			log.debug("setting image filepath and name");
			tmpdir = "/opt/jasper/img/";
			File dirFile = new File(tmpdir);
			String fileBaseName = "nir_" + reportName + "-";
			String file = (File.createTempFile(fileBaseName, ".png", dirFile)).getName();
			p_filename = tmpdir + file;
			log.debug("Image to be stored as: " + p_filename);
			setFileName(p_filename);
	
			log.debug("Filename: " + p_filename);
			rconn.assign("outfile", p_filename);
			
			String rcmd = rFunctionCallString;

			// Call the function to generate the report
			rconn.voidEval(rcmd);
			
			// Close RServ connection, ensure garbage collection removes pointer too!
			log.debug("Closing connection to R instance and removing pointer");
			rconn.close();
			rconn = null;
			log.info("Finished report");

		} catch (RserveException rse) {
			log.error("Rserve exception: " + rse.getMessage());
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