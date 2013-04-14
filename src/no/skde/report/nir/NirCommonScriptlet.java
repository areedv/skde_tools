package no.skde.report.nir;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.fill.*;

import org.apache.log4j.Logger;

//import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.*;

//import no.helseregister.tools.security.*;

public class NirCommonScriptlet extends JRDefaultScriptlet
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
			log.info("Start generating report " + NirCommonScriptlet.class.getName());
			
			// Create the connection
			log.debug("Getting R connection...");
			rconn = new RConnection();
			log.debug("R connection: " + rconn.toString());


			// Get parameters
			// these must always be provided when this scriptlet class is used
			String reportName = (String) ((JRFillParameter) parametersMap.get("reportName")).getValue();
			String rScriptName = (String) ((JRFillParameter) parametersMap.get("rScriptName")).getValue();
			String rFunctionCallString = (String) ((JRFillParameter) parametersMap.get("rFunctionCallString")).getValue();
			
			// the rest of parameters are optional, but must match whatever needed by R
			String varNam;
			try {
				log.debug("Getting parameter values");
				varNam = (String) ((JRFillParameter) parametersMap.get("MeanMedVarName")).getValue();
				if (varNam == null) {
					varNam = "nada";
				}
				rconn.voidEval("valgtVar=" + "'" + varNam + "'");
			} catch (Exception e) {
				log.debug("Parameter MeanMedVarName is not defined: " + e.getMessage());
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
			
			Date startDate;
			try {
				startDate = (Date) ((JRFillParameter) parametersMap.get("startDate")).getValue();
				if (startDate == null) {
					startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2010-01-01");
				}
				StringBuilder startDateString = new StringBuilder(rFormat.format(startDate));
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
				rconn.voidEval("ShType=" + "'" + hospitalsLevel.toString() + "'");
			} catch (Exception e) {
				log.debug("Parameter hospitalsLevel is not defined: " + e.getMessage());
			}			
			
			Integer inFromSituation;
			try {
				inFromSituation = (Integer) ((JRFillParameter) parametersMap.get("inFromSituation")).getValue();
				if (inFromSituation == null) {
					inFromSituation = 99;
				}

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
				rconn.voidEval("erMann=" + erMann.toString());
			} catch (Exception e) {
				log.debug("Parameter erMann is not defined: " + e.getMessage());
			}
			
			
			// set path to library
			String libkat = "'/opt/jasper/lib/r/'";
			rconn.voidEval("libkat=" + libkat);
			
			
			// Set up the tmp directory, file names and reportUserInfo
			String tmpdir = "";
			String p_filename = "";
			log.debug("setting image filepath and name");
			tmpdir = "/opt/jasper/img/";
			File dirFile = new File(tmpdir);
			String fileBaseName = "nir_" + reportName + "-";
//			String file = (File.createTempFile("nir_" + reportName + "-", ".png", dirFile)).getName();
			String file = (File.createTempFile(fileBaseName, ".png", dirFile)).getName();
			p_filename = tmpdir + file;
			log.debug("Image to be stored as: " + p_filename);
			setFileName(p_filename);
	
			log.debug("Filename: " + p_filename);
			rconn.assign("outfile", p_filename);
			
			String rcmd = rFunctionCallString;
			
			// Source the function
			rconn.assign("source_file", "/opt/jasper/rscripts/nir/" + rScriptName);
			rconn.voidEval("source(source_file)");
			log.debug("Sourced sourcefile");

			// Call the function to generate the report
			rconn.voidEval(rcmd);
			
			// Close RServ connection, ensure garbage collection removes pointer too!
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