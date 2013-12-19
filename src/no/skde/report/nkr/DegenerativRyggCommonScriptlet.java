/**
 * no.skde.report.nkr
 * DegenerativRyggCommonScriptlet.java Dec 19 2013 Are Edvardsen
 * 
 * 
 *  Copyleft 2013, SKDE
 */

package no.skde.report.nkr;

import java.io.*;
//import java.text.SimpleDateFormat;
//import java.util.Date;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.fill.*;
import org.apache.log4j.Logger;
import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.*;

public class DegenerativRyggCommonScriptlet extends JRDefaultScriptlet {
	
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
			log.info("Start generating R report using " + DegenerativRyggCommonScriptlet.class.getName());
			
			// Create the connection
			log.debug("Getting connection to R instance...");
			rconn = new RConnection();
			log.debug("R connection provided: " + rconn.toString());


			// Get parameters
			// these must always be provided when this scriptlet class is used
			String loggedInUserFullName = "";
			String loggedInUserAVD_RESH = "";
			String reportName = "";
			String rScriptName = "";
			String rFunctionCallString = "";
			try {
				loggedInUserFullName = (String) ((JRFillParameter) parametersMap.get("LoggedInUserFullName")).getValue();
				loggedInUserAVD_RESH = (String) ((JRFillParameter) parametersMap.get("LoggedInUserAVD_RESH")).getValue();
				reportName = (String) ((JRFillParameter) parametersMap.get("reportName")).getValue();
				rScriptName = (String) ((JRFillParameter) parametersMap.get("rScriptName")).getValue();
				rFunctionCallString = (String) ((JRFillParameter) parametersMap.get("rFunctionCallString")).getValue();
				log.info("Report to be run: " + reportName);
				log.info("Report requested by JRS user " + loggedInUserFullName + ", AVD_RESH " + loggedInUserAVD_RESH);
				log.debug("R script to be called: " + rScriptName);
				log.debug("R function call string: " + rFunctionCallString);
				
				rconn.voidEval("reshID=" + loggedInUserAVD_RESH);
			} catch (Exception e) {
				log.error("Mandatory parameters in the report definition calling this scriptlet were not defined: " + e.getMessage());
			}
			
			// the rest of parameters are optional, but must match whatever needed by R
			try {
				Integer gender = (Integer) ((JRFillParameter) parametersMap.get("gender"))
						.getValue();
				if (gender == null) {
					gender = 0;
				}
				log.debug("Parameter 'gender' mapped to value: " + gender.toString());
				rconn.voidEval("kjonn=" + gender.toString());
			} catch (Exception e) {
				log.debug("Parameter 'gender' is not provided: " + e.getMessage());
			}
			
			try {
				Integer mydept = (Integer) ((JRFillParameter) parametersMap.get("mydept"))
						.getValue();
				if (mydept == null) {
					mydept = 1;
				}
				log.debug("Parameter 'mydept' mapped to value: " + mydept.toString());
				rconn.voidEval("egenavd=" + mydept.toString());
			} catch (Exception e) {
				log.debug("Parameter 'mydept' is not provided: " + e.getMessage());
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
			
			
			// set path to library, to be removed since Rapporteket uses same directory for all R files (noweb, libs and report funs)
			String libkat = "'/opt/jasper/r/'";
			rconn.voidEval("libkat=" + libkat);
						
			log.debug("Getting Jasper Report data source...");
						
			
			// Load up primitive arrays with query data
			JRDataSource ds = (JRDataSource) ((JRFillParameter) parametersMap.get("REPORT_DATA_SOURCE")).getValue();
						
			log.debug("Getting Jasper Report Fields from report data source...");
			
			JRField UtdField = (JRField) fieldsMap.get("Utd");
			JRField KjonnField = (JRField) fieldsMap.get("Kjonn");
			JRField HovedInngrepField = (JRField) fieldsMap.get("HovedInngrep");
			JRField HovedInngreptxtField = (JRField) fieldsMap.get("HovedInngreptxt");
			JRField InngrepField = (JRField) fieldsMap.get("Inngrep");
			JRField InngreptxtFiled = (JRField) fieldsMap.get("Inngreptxt");
			JRField AvdReshIDField = (JRField) fieldsMap.get("AvdReshID");
			JRField AvdNavnField = (JRField) fieldsMap.get("AvdNavn");
			JRField OpDatoField = (JRField) fieldsMap.get("OpDato");
			
			log.debug("Primitive arrays loaded with query data");
			
			
			// Create "slug arrays" with very big sizes (default limit to
			// 1000000) to accommodate large queries
			// We cannot find out how many rows are returned so we have to fetch
			// first and then
			// rebuild arrays of the proper size before passing to R
			//
			// Arrays MUST be defined as objects and not its primitive since
			// returned values do contain 'null'
			
			log.debug("Making empty slug array...");

			Double[] sUtd = new Double[1000000];
			Integer[] sKjonn = new Integer[1000000];
			Integer[] sHovedInngrep = new Integer[1000000];
			String[] sHovedInngreptxt = new String[1000000];
			Integer[] sInngrep = new Integer[1000000];
			String[] sInngreptxt = new String[1000000];
			String[] sAvdReshID = new String[1000000];
			String[] sAvdNavn = new String[1000000];
			String[] sOpDato = new String[1000000];
		
			
			log.debug("populating slug array with report data...");			
			
			int rowidx = 0;
			// Assume we get 1 row
			boolean getRow = true;
			while (getRow) {
				sUtd[rowidx] = (Double) ds.getFieldValue(UtdField);
				sKjonn[rowidx] = (Integer) ds.getFieldValue(KjonnField);
				sHovedInngrep[rowidx] = (Integer) ds.getFieldValue(HovedInngrepField);
				sHovedInngreptxt[rowidx] = (String) ds.getFieldValue(HovedInngreptxtField);
				sInngrep[rowidx] = (Integer) ds.getFieldValue(InngrepField);
				sInngreptxt[rowidx] = (String) ds.getFieldValue(InngreptxtFiled);
				sAvdReshID[rowidx] = (String) ds.getFieldValue(AvdReshIDField);
				sAvdNavn[rowidx] = (String) ds.getFieldValue(AvdNavnField);
				sOpDato[rowidx] = (String) ds.getFieldValue(OpDatoField);
				getRow = ds.next();
				rowidx++;
			}
			rowidx--;
			
			log.debug("Slug array filled with " + rowidx + " records from report data");
			
			
			log.debug("Creating proper sized array...");
			
			double[] Utd = new double[rowidx + 1];
			int[] Kjonn = new int[rowidx + 1];
			int[] HovedInngrep = new int[rowidx + 1];
			String[] HovedInngreptxt = new String[rowidx + 1];
			int[] Inngrep = new int[rowidx + 1];
			String[] Inngreptxt = new String[rowidx + 1];
			String[] AvdReshID = new String[rowidx + 1];
			String[] AvdNavn = new String[rowidx + 1];
			String[] OpDato = new String[rowidx + 1];
			
			
			// ifs are needed because underlying query returns null. Since ints
			// cannot be null, these are returned as type double by the query
			log.debug("Populating proper sized array with data from slug array, also checking for NULLs...");
			int i = 0;
			while (i <= rowidx) {
				if (sUtd[i] == null) {
					Utd[i] = java.lang.Double.NaN;
				}
				else {
					Utd[i] = sUtd[i];
				}
				Kjonn[i] = sKjonn[i];
				HovedInngrep[i] = sHovedInngrep[i];
				HovedInngreptxt[i] = sHovedInngreptxt[i];
				Inngrep[i] = sInngrep[i];
				Inngreptxt[i] = sInngreptxt[i];
				AvdReshID[i] = sAvdReshID[i];
				AvdNavn[i] = sAvdNavn[i];
				OpDato[i] = sOpDato[i];
				i++;				
				
			}
			
			
			log.debug("Creating the R dataframe...");
			RList l = new RList();
			l.put("Utd", new REXPDouble(Utd));
			l.put("Kjonn", new REXPInteger(Kjonn));
			l.put("HovedInngrep", new REXPInteger(HovedInngrep));
			l.put("HovedInngreptxt", new REXPString(HovedInngreptxt));
			l.put("Inngrep", new REXPInteger(Inngrep));
			l.put("Inngreptxt", new REXPString(Inngreptxt));
			l.put("AvdReshID", new REXPString(AvdReshID));
			l.put("AvdNavn", new REXPString(AvdNavn));
			l.put("OpDato", new REXPString(OpDato));

			REXP df = REXP.createDataFrame(l);
			log.debug("Assigning data frame to R instance");
			rconn.assign("opdata", df);

			
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
			
			// Source the function
			rconn.assign("source_file", "/opt/jasper/r/" + rScriptName);
			log.debug("In R instance: sourcing R code...");
			rconn.voidEval("source(source_file)");

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