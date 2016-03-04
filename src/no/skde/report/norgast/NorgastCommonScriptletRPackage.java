/**
 * no.skde.report.norgast
 * NorgastCommonScriptletRPackage.java Jan 26 2016 Are Edvardsen
 * 
 * 
 *  Copyleft 2016 SKDE
 */

package no.skde.report.norgast;

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

public class NorgastCommonScriptletRPackage extends JRDefaultScriptlet {

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
			log.info("Start generating R report using " + NorgastCommonScriptletRPackage.class.getName());
			
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
			
			
			


			// bmi; multi select list of values
			List<String> bmiList = new ArrayList<String>();
			String bmi;
			try {
				bmiList = (List<String>) ((JRFillParameter) parametersMap.get("bmi")).getValue();
				bmi = "c(";
				// if (bmiList.contains("all")) {
				if (bmiList.isEmpty()) {
					bmi = bmi + "'')";
				} else {
					Iterator<String> iterator = bmiList.iterator();
					while (iterator.hasNext()) {
						bmi = bmi + "'" + iterator.next() + "',";
					}
					bmi = bmi.substring(0, bmi.length()-1);
					bmi = bmi + ")";
				}
				log.debug("R concat for BMI vector is " + bmi);
				rconn.voidEval("BMI=" + bmi);
			} catch (Exception e) {
				log.debug("Parameter bmi is not defined: " + e.getMessage());
			}

			// preTreat; multi select list of values
			Integer preTreat;
			try {
				preTreat = (Integer) ((JRFillParameter) parametersMap.get("preTreat")).getValue();
				if (preTreat == null) {
					preTreat = 99;
				}
				rconn.voidEval("forbehandling=" + preTreat.toString());
			} catch (Exception e) {
				log.debug("Parameter preTreat is not defined: " + e.getMessage());
			}

			
			Integer elektiv;
			try {
				elektiv = (Integer) ((JRFillParameter) parametersMap.get("elektiv")).getValue();
				if (elektiv == null) {
					elektiv = 99;
				}
				rconn.voidEval("elektiv=" + elektiv.toString());
			} catch (Exception e) {
				log.debug("Parameter elektiv is not defined: " + e.getMessage());
			}

			// selectDepts; multi select list of values
			List<String> selectDeptsList = new ArrayList<String>();
			String selectDepts;
			try {
				selectDeptsList = (List<String>) ((JRFillParameter) parametersMap.get("selectDepts")).getValue();
				selectDepts = "c(";
				if (selectDeptsList.isEmpty()) {
					selectDepts = selectDepts + "'')";
				} else {
					Iterator<String> iterator = selectDeptsList.iterator();
					while (iterator.hasNext()) {
						selectDepts = selectDepts + "'" + iterator.next() + "',";
					}
					selectDepts = selectDepts.substring(0, selectDepts.length()-1);
					selectDepts = selectDepts + ")";
				}
				log.debug("R concat for valgtShus vector is " + selectDepts);
				rconn.voidEval("valgtShus=" + selectDepts);
			} catch (Exception e) {
				log.debug("Parameter selectDepts is not defined: " + e.getMessage());
			}			
			
			// ---
			
			// asa; multi select list of values
			List<String> asaList = new ArrayList<String>();
			String asa;
			try {
				asaList = (List<String>) ((JRFillParameter) parametersMap.get("asa")).getValue();
				asa = "c(";
				if (asaList.isEmpty()) {
					asa = asa + "'')";
				} else {
					Iterator<String> iterator = asaList.iterator();
					while (iterator.hasNext()) {
						asa = asa + "'" + iterator.next() + "',";
					}
					asa = asa.substring(0, asa.length()-1);
					asa = asa + ")";
				}
				log.debug("R concat for ASA vector is " + asa);
				rconn.voidEval("ASA=" + asa);
			} catch (Exception e) {
				log.debug("Parameter asa is not defined: " + e.getMessage());
			}                                            

			// whoEcog; multi select list of values
			List<String> whoEcogList = new ArrayList<String>();
			String whoEcog;
			try {
				whoEcogList = (List<String>) ((JRFillParameter) parametersMap.get("whoEcog")).getValue();
				whoEcog = "c(";
				if (whoEcogList.isEmpty()) {
					whoEcog = whoEcog + "'')";
				} else {
					Iterator<String> iterator = whoEcogList.iterator();
					while (iterator.hasNext()) {
						whoEcog = whoEcog + "'" + iterator.next() + "',";
					}
					whoEcog = whoEcog.substring(0, whoEcog.length()-1);
					whoEcog = whoEcog + ")";
				}
				log.debug("R concat for whoEcog vector is " + whoEcog);
				rconn.voidEval("whoEcog=" + whoEcog);
			} catch (Exception e) {
				log.debug("Parameter whoEcog is not defined: " + e.getMessage());
			}


			Integer tilgang;
			try {
				tilgang = (Integer) ((JRFillParameter) parametersMap.get("tilgang")).getValue();
				if (tilgang == null) {
					tilgang = 99;
				}
				rconn.voidEval("tilgang=" + tilgang.toString());
			} catch (Exception e) {
				log.debug("Parameter tilgang is not defined: " + e.getMessage());
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
			log.info("RegData is no longer provided by Norgast scriptlets");


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
