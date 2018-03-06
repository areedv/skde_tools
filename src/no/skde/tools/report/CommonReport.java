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
//import org.rosuda.REngine.*;
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
			String rPackageName = "";
			String reportName = "";
			String rFunctionCallString = "";
			try {
				loggedInUserFullName = (String) ((JRFillParameter) parametersMap.get("LoggedInUserFullName")).getValue();
				loggedInUserAVD_RESH = (String) ((JRFillParameter) parametersMap.get("LoggedInUserAVD_RESH")).getValue();
				rPackageName = (String) ((JRFillParameter) parametersMap.get("rPackageName")).getValue();
				reportName = (String) ((JRFillParameter) parametersMap.get("reportName")).getValue();
				rFunctionCallString = (String) ((JRFillParameter) parametersMap.get("rFunctionCallString")).getValue();
				log.info("Report requested by JRS user " + loggedInUserFullName + ", AVD_RESH " + loggedInUserAVD_RESH);
				log.info("R package to be loaded in this R session: " + rPackageName);
				log.info("Report to be run: " + reportName);
				log.debug("R function call string: " + rFunctionCallString);
				rconn.voidEval("require(" + rPackageName + ")");
				rconn.voidEval("reshID=" + loggedInUserAVD_RESH);
			} catch (Exception e) {
				log.error("Mandatory parameters in the report definition calling this scriptlet were not defined: " + e.getMessage());
			}
			
			// the rest of parameters are optional, but must match whatever needed by R
						
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
			List<String> multiValgList_1 = new ArrayList<String>();
			String multiValg_1;
			try {
				multiValgList_1 = (List<String>) ((JRFillParameter) parametersMap.get("multiValg_1")).getValue();
				multiValg_1 = "c(";
				if (multiValgList_1.isEmpty()) {
					multiValg_1 = multiValg_1 + "'')";
				} else {
					Iterator<String> iterator = multiValgList_1.iterator();
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

			List<String> multiValgList_2 = new ArrayList<String>();
			String multiValg_2;
			try {
				multiValgList_2 = (List<String>) ((JRFillParameter) parametersMap.get("multiValg_2")).getValue();
				multiValg_2 = "c(";
				if (multiValgList_2.isEmpty()) {
					multiValg_2 = multiValg_2 + "'')";
				} else {
					Iterator<String> iterator = multiValgList_2.iterator();
					while (iterator.hasNext()) {
						multiValg_2 = multiValg_2 + "'" + iterator.next() + "',";
					}
					multiValg_2 = multiValg_2.substring(0, multiValg_2.length()-1);
					multiValg_2 = multiValg_2 + ")";
				}
				log.debug("R concat for multiValg_2 vector is " + multiValg_2);
				rconn.voidEval("multiValg_2=" + multiValg_2);
			} catch (Exception e) {
				log.debug("Parameter multiValg_2 is not defined: " + e.getMessage());
			}

			List<String> multiValgList_3 = new ArrayList<String>();
			String multiValg_3;
			try {
				multiValgList_3 = (List<String>) ((JRFillParameter) parametersMap.get("multiValg_3")).getValue();
				multiValg_3 = "c(";
				if (multiValgList_3.isEmpty()) {
					multiValg_3 = multiValg_3 + "'')";
				} else {
					Iterator<String> iterator = multiValgList_3.iterator();
					while (iterator.hasNext()) {
						multiValg_3 = multiValg_3 + "'" + iterator.next() + "',";
					}
					multiValg_3 = multiValg_3.substring(0, multiValg_3.length()-1);
					multiValg_3 = multiValg_3 + ")";
				}
				log.debug("R concat for multiValg_3 vector is " + multiValg_3);
				rconn.voidEval("multiValg_3=" + multiValg_3);
			} catch (Exception e) {
				log.debug("Parameter multiValg_3 is not defined: " + e.getMessage());
			}

			List<String> multiValgList_4 = new ArrayList<String>();
			String multiValg_4;
			try {
				multiValgList_4 = (List<String>) ((JRFillParameter) parametersMap.get("multiValg_4")).getValue();
				multiValg_4 = "c(";
				if (multiValgList_4.isEmpty()) {
					multiValg_4 = multiValg_4 + "'')";
				} else {
					Iterator<String> iterator = multiValgList_4.iterator();
					while (iterator.hasNext()) {
						multiValg_4 = multiValg_4 + "'" + iterator.next() + "',";
					}
					multiValg_4 = multiValg_4.substring(0, multiValg_4.length()-1);
					multiValg_4 = multiValg_4 + ")";
				}
				log.debug("R concat for multiValg_4 vector is " + multiValg_4);
				rconn.voidEval("multiValg_4=" + multiValg_4);
			} catch (Exception e) {
				log.debug("Parameter multiValg_4 is not defined: " + e.getMessage());
			}

			List<String> multiValgList_5 = new ArrayList<String>();
			String multiValg_5;
			try {
				multiValgList_5 = (List<String>) ((JRFillParameter) parametersMap.get("multiValg_5")).getValue();
				multiValg_5 = "c(";
				if (multiValgList_5.isEmpty()) {
					multiValg_5 = multiValg_5 + "'')";
				} else {
					Iterator<String> iterator = multiValgList_5.iterator();
					while (iterator.hasNext()) {
						multiValg_5 = multiValg_5 + "'" + iterator.next() + "',";
					}
					multiValg_5 = multiValg_5.substring(0, multiValg_5.length()-1);
					multiValg_5 = multiValg_5 + ")";
				}
				log.debug("R concat for multiValg_5 vector is " + multiValg_5);
				rconn.voidEval("multiValg_5=" + multiValg_5);
			} catch (Exception e) {
				log.debug("Parameter multiValg_5 is not defined: " + e.getMessage());
			}

			
			Integer integer_1;
			try {
				integer_1 = (Integer) ((JRFillParameter) parametersMap.get("integer_1")).getValue();
				if (integer_1 == null) {
					integer_1 = 99;
				}
				rconn.voidEval("integer_1=" + integer_1.toString());
			} catch (Exception e) {
				log.debug("Parameter iInteger_1 is not defined: " + e.getMessage());
			}
			
			Integer integer_2;
			try {
				integer_2 = (Integer) ((JRFillParameter) parametersMap.get("integer_2")).getValue();
				if (integer_2 == null) {
					integer_2 = 99;
				}
				rconn.voidEval("integer_2=" + integer_2.toString());
			} catch (Exception e) {
				log.debug("Parameter integer_2 is not defined: " + e.getMessage());
			}
			
			Integer integer_3;
			try {
				integer_3 = (Integer) ((JRFillParameter) parametersMap.get("integer_3")).getValue();
				if (integer_3 == null) {
					integer_3 = 99;
				}
				rconn.voidEval("integer_3=" + integer_3.toString());
			} catch (Exception e) {
				log.debug("Parameter integer_3 is not defined: " + e.getMessage());
			}
			
			Integer integer_4;
			try {
				integer_4 = (Integer) ((JRFillParameter) parametersMap.get("integer_4")).getValue();
				if (integer_4 == null) {
					integer_4 = 99;
				}
				rconn.voidEval("integer_4=" + integer_4.toString());
			} catch (Exception e) {
				log.debug("Parameter integer_4 is not defined: " + e.getMessage());
			}
			
			Integer integer_5;
			try {
				integer_5 = (Integer) ((JRFillParameter) parametersMap.get("integer_5")).getValue();
				if (integer_5 == null) {
					integer_5 = 99;
				}
				rconn.voidEval("integer_5=" + integer_5.toString());
			} catch (Exception e) {
				log.debug("Parameter integer_5 is not defined: " + e.getMessage());
			}
			
			Integer integer_6;
			try {
				integer_6 = (Integer) ((JRFillParameter) parametersMap.get("integer_6")).getValue();
				if (integer_6 == null) {
					integer_6 = 99;
				}
				rconn.voidEval("integer_6=" + integer_6.toString());
			} catch (Exception e) {
				log.debug("Parameter integer_6 is not defined: " + e.getMessage());
			}
			
			Integer integer_7;
			try {
				integer_7 = (Integer) ((JRFillParameter) parametersMap.get("integer_7")).getValue();
				if (integer_7 == null) {
					integer_7 = 99;
				}
				rconn.voidEval("integer_7=" + integer_7.toString());
			} catch (Exception e) {
				log.debug("Parameter integer_7 is not defined: " + e.getMessage());
			}
			
			Integer integer_8;
			try {
				integer_8 = (Integer) ((JRFillParameter) parametersMap.get("integer_8")).getValue();
				if (integer_8 == null) {
					integer_8 = 99;
				}
				rconn.voidEval("integer_8=" + integer_8.toString());
			} catch (Exception e) {
				log.debug("Parameter integer_8 is not defined: " + e.getMessage());
			}
			
			Integer integer_9;
			try {
				integer_9 = (Integer) ((JRFillParameter) parametersMap.get("integer_9")).getValue();
				if (integer_9 == null) {
					integer_9 = 99;
				}
				rconn.voidEval("integer_9=" + integer_9.toString());
			} catch (Exception e) {
				log.debug("Parameter integer_9 is not defined: " + e.getMessage());
			}
			
			Integer integer_10;
			try {
				integer_10 = (Integer) ((JRFillParameter) parametersMap.get("integer_10")).getValue();
				if (integer_10 == null) {
					integer_10 = 99;
				}
				rconn.voidEval("integer_10=" + integer_10.toString());
			} catch (Exception e) {
				log.debug("Parameter integer_10 is not defined: " + e.getMessage());
			}
			
			String streng_1;
			try {
				log.debug("Getting parameter values");
				streng_1 = (String) ((JRFillParameter) parametersMap.get("streng_1")).getValue();
				if (streng_1 == null) {
					streng_1 = "nada";
				}
				rconn.voidEval("streng_1=" + "'" + streng_1 + "'");
			} catch (Exception e) {
				log.debug("Parameter streng_1 is not defined: " + e.getMessage());
			}

			String streng_2;
			try {
				log.debug("Getting parameter values");
				streng_2 = (String) ((JRFillParameter) parametersMap.get("streng_2")).getValue();
				if (streng_2 == null) {
					streng_2 = "nada";
				}
				rconn.voidEval("streng_2=" + "'" + streng_2 + "'");
			} catch (Exception e) {
				log.debug("Parameter streng_2 is not defined: " + e.getMessage());
			}

			String streng_3;
			try {
				log.debug("Getting parameter values");
				streng_3 = (String) ((JRFillParameter) parametersMap.get("streng_3")).getValue();
				if (streng_3 == null) {
					streng_3 = "nada";
				}
				rconn.voidEval("streng_3=" + "'" + streng_3 + "'");
			} catch (Exception e) {
				log.debug("Parameter streng_3 is not defined: " + e.getMessage());
			}

			String streng_4;
			try {
				log.debug("Getting parameter values");
				streng_4 = (String) ((JRFillParameter) parametersMap.get("streng_4")).getValue();
				if (streng_4 == null) {
					streng_4 = "nada";
				}
				rconn.voidEval("streng_4=" + "'" + streng_4 + "'");
			} catch (Exception e) {
				log.debug("Parameter streng_4 is not defined: " + e.getMessage());
			}

			String streng_5;
			try {
				log.debug("Getting parameter values");
				streng_5 = (String) ((JRFillParameter) parametersMap.get("streng_5")).getValue();
				if (streng_5 == null) {
					streng_5 = "nada";
				}
				rconn.voidEval("streng_5=" + "'" + streng_5 + "'");
			} catch (Exception e) {
				log.debug("Parameter streng_5 is not defined: " + e.getMessage());
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

			Double desimaltall_2;
			try {
				desimaltall_2 = (Double) ((JRFillParameter) parametersMap.get("desimaltall_2")).getValue();
				if (desimaltall_2 == null) {
					desimaltall_2 = 0.0;
				}
				rconn.voidEval("desimaltall_2=" + desimaltall_2.toString());
			} catch (Exception e) {
				log.debug("Parameter desimaltall_2 is not defined: " + e.getMessage());
			}

			Double desimaltall_3;
			try {
				desimaltall_3 = (Double) ((JRFillParameter) parametersMap.get("desimaltall_3")).getValue();
				if (desimaltall_3 == null) {
					desimaltall_3 = 0.0;
				}
				rconn.voidEval("desimaltall_3=" + desimaltall_3.toString());
			} catch (Exception e) {
				log.debug("Parameter desimaltall_3 is not defined: " + e.getMessage());
			}

			Double desimaltall_4;
			try {
				desimaltall_4 = (Double) ((JRFillParameter) parametersMap.get("desimaltall_4")).getValue();
				if (desimaltall_4 == null) {
					desimaltall_4 = 0.0;
				}
				rconn.voidEval("desimaltall_4=" + desimaltall_4.toString());
			} catch (Exception e) {
				log.debug("Parameter desimaltall_4 is not defined: " + e.getMessage());
			}

			Double desimaltall_5;
			try {
				desimaltall_5 = (Double) ((JRFillParameter) parametersMap.get("desimaltall_5")).getValue();
				if (desimaltall_5 == null) {
					desimaltall_5 = 0.0;
				}
				rconn.voidEval("desimaltall_5=" + desimaltall_5.toString());
			} catch (Exception e) {
				log.debug("Parameter desimaltall_5 is not defined: " + e.getMessage());
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

			Date dato_2;
			try {
				dato_2 = (Date) ((JRFillParameter) parametersMap.get("dato_2")).getValue();
				if (dato_2 == null) {
					dato_2 = new Date();
				}
				StringBuilder datoString = new StringBuilder(rFormat.format(dato_2));
				rconn.voidEval("dato_2=" + "'" + datoString + "'");
			} catch (Exception e) {
				log.debug("Parameter dato_2 is not defined: " + e.getMessage());
			}

			Date dato_3;
			try {
				dato_3 = (Date) ((JRFillParameter) parametersMap.get("dato_3")).getValue();
				if (dato_3 == null) {
					dato_3 = new Date();
				}
				StringBuilder datoString = new StringBuilder(rFormat.format(dato_3));
				rconn.voidEval("dato_3=" + "'" + datoString + "'");
			} catch (Exception e) {
				log.debug("Parameter dato_3 is not defined: " + e.getMessage());
			}

			Date dato_4;
			try {
				dato_4 = (Date) ((JRFillParameter) parametersMap.get("dato_4")).getValue();
				if (dato_4 == null) {
					dato_4 = new Date();
				}
				StringBuilder datoString = new StringBuilder(rFormat.format(dato_4));
				rconn.voidEval("dato_4=" + "'" + datoString + "'");
			} catch (Exception e) {
				log.debug("Parameter dato_4 is not defined: " + e.getMessage());
			}

			Date dato_5;
			try {
				dato_5 = (Date) ((JRFillParameter) parametersMap.get("dato_5")).getValue();
				if (dato_5 == null) {
					dato_5 = new Date();
				}
				StringBuilder datoString = new StringBuilder(rFormat.format(dato_5));
				rconn.voidEval("dato_5=" + "'" + datoString + "'");
			} catch (Exception e) {
				log.debug("Parameter dato_5 is not defined: " + e.getMessage());
			}
			// END generic, common report user controls
			  
			
			// Set up the tmp directory, file names and reportUserInfo
			String tmpdir = "";
			String p_filename = "";
			log.debug("Setting report image filepath and name");
			tmpdir = "/opt/jasper/img/";
			File dirFile = new File(tmpdir);
			String fileBaseName = rPackageName + reportName + "-";
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
