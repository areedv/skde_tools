/**
 * no.skde.report.norgast
 * NorgastCommonScriptlet.java Sep 14 2014 Are Edvardsen
 * 
 * 
 *  Copyleft 2014, 2015 SKDE
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

public class NorgastCommonScriptlet extends JRDefaultScriptlet {

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
			log.info("Start generating R report using " + NorgastCommonScriptlet.class.getName());
			
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
			
			// set path to library, to be removed since Rapporteket uses same directory for all R files (noweb, libs and report funs)
			String libkat = "'/opt/jasper/r/'";
			rconn.voidEval("libkat=" + libkat);
			
			// Load up primitive arrays with query data
			JRDataSource ds = (JRDataSource) ((JRFillParameter) parametersMap
					.get("REPORT_DATA_SOURCE")).getValue();
			
			JRField AvdRESHField = (JRField) fieldsMap.get("AvdRESH");
			JRField AvdelingField = (JRField) fieldsMap.get("Avdeling");
			JRField BMI_CATEGORYField = (JRField) fieldsMap.get("BMI_CATEGORY");
			JRField WEIGHTLOSSField = (JRField) fieldsMap.get("WEIGHTLOSS");
			JRField DIABETESField = (JRField) fieldsMap.get("DIABETES");
			JRField CHEMOTHERAPY_ONLYField = (JRField) fieldsMap.get("CHEMOTHERAPY_ONLY");
			JRField RADIATION_THERAPY_ONLYField = (JRField) fieldsMap.get("RADIATION_THERAPY_ONLY");
			JRField CHEMORADIOTHERAPYField = (JRField) fieldsMap.get("CHEMORADIOTHERAPY");
			JRField WHO_ECOG_SCOREField = (JRField) fieldsMap.get("WHO_ECOG_SCORE");
			JRField MODIFIED_GLASGOW_SCOREField = (JRField) fieldsMap.get("MODIFIED_GLASGOW_SCORE");
			JRField ASAField = (JRField) fieldsMap.get("ASA");
			JRField ANESTHESIA_STARTField = (JRField) fieldsMap.get("ANESTHESIA_START");
			JRField NCSPField = (JRField) fieldsMap.get("NCSP");
			JRField OPERATION_DATEField = (JRField) fieldsMap.get("OPERATION_DATE");
			JRField ANASTOMOSISField = (JRField) fieldsMap.get("ANASTOMOSIS");
			JRField OSTOMYField = (JRField) fieldsMap.get("OSTOMY");
			JRField ABDOMINAL_ACCESSField = (JRField) fieldsMap.get("ABDOMINAL_ACCESS");
			JRField ROBOTASSISTANCEField = (JRField) fieldsMap.get("ROBOTASSISTANCE");
			JRField THORAX_ACCESSField = (JRField) fieldsMap.get("THORAX_ACCESS");
			JRField RELAPAROTOMYField = (JRField) fieldsMap.get("RELAPAROTOMY");
			JRField ACCORDION_SCOREField = (JRField) fieldsMap.get("ACCORDION_SCORE");
			JRField isMaleField = (JRField) fieldsMap.get("isMale");
			JRField decimalAgeField = (JRField) fieldsMap.get("decimalAge");
			JRField PRS_SCOREField = (JRField) fieldsMap.get("PRS_SCORE");
			JRField READMISSION_STATUSField = (JRField) fieldsMap.get("READMISSION_STATUS");
			JRField STATUSField = (JRField) fieldsMap.get("STATUS");
			
			JRField RELAPAROTOMY_YESField = (JRField) fieldsMap.get("RELAPAROTOMY_YES");
			JRField READMISSION_ACCORDION_SCOREField = (JRField) fieldsMap.get("READMISSION_ACCORDION_SCORE");
			JRField READMISSION_RELAPAROTOMYField = (JRField) fieldsMap.get("READMISSION_RELAPAROTOMY");
			JRField READMISSION_RELAPAROTOMY_YESField = (JRField) fieldsMap.get("READMISSION_RELAPAROTOMY_YES");

			
			log.debug("Arrays of primitive data loaded");
			
			
			// Create "slug arrays" with very big sizes (default limit to
			// 1000000) to accommodate large queries
			// We cannot find out how many rows are returned so we have to fetch
			// first and then
			// rebuild arrays of the proper size before passing to R
			//
			// Arrays MUST be defined as objects and not its primitive since
			// returned values do contain 'null'
			Double[] sAvdRESH = new Double[1000000];
			String[] sAvdeling = new String[1000000];
			String[] sBMI_CATEGORY = new String[1000000];
			Double[] sWEIGHTLOSS = new Double[1000000];
			Double[] sDIABETES = new Double[1000000];
			Double[] sCHEMOTHERAPY_ONLY = new Double[1000000];
			Double[] sRADIATION_THERAPY_ONLY = new Double[1000000];
			Double[] sCHEMORADIOTHERAPY = new Double[1000000];
			Double[] sWHO_ECOG_SCORE = new Double[1000000];
			Double[] sMODIFIED_GLASGOW_SCORE = new Double[1000000];
			Double[] sASA = new Double[1000000];
			String[] sANESTHESIA_START = new String[1000000];
			String[] sNCSP = new String[1000000];
			String[] sOPERATION_DATE = new String[1000000];
			Double[] sANASTOMOSIS = new Double[1000000];
			Double[] sOSTOMY = new Double[1000000];
			Double[] sABDOMINAL_ACCESS = new Double[1000000];
			Double[] sROBOTASSISTANCE = new Double[1000000];
			Double[] sTHORAX_ACCESS = new Double[1000000];
			Double[] sRELAPAROTOMY = new Double[1000000];
			String[] sACCORDION_SCORE = new String[1000000];
			Double[] sisMale = new Double[1000000];
			Double[] sdecimalAge = new Double[1000000];
			Double[] sPRS_SCORE = new Double[1000000];
			Double[] sREADMISSION_STATUS = new Double[1000000];
			Double[] sSTATUS = new Double[1000000];
			
			Double[] sRELAPAROTOMY_YES = new Double[1000000];
			String[] sREADMISSION_ACCORDION_SCORE = new String[1000000];
			Double[] sREADMISSION_RELAPAROTOMY = new Double[1000000];
			Double[] sREADMISSION_RELAPAROTOMY_YES = new Double[1000000];
			
			int rowidx = 0;

			
			// Assume we get 1 row
			boolean getRow = true;		
			log.debug("getting rows...");

			while (getRow) {
				sAvdRESH[rowidx] = (Double) ds.getFieldValue(AvdRESHField);
				sAvdeling[rowidx] = (String) ds.getFieldValue(AvdelingField);
				sBMI_CATEGORY[rowidx] = (String) ds.getFieldValue(BMI_CATEGORYField);
				sWEIGHTLOSS[rowidx] = (Double) ds.getFieldValue(WEIGHTLOSSField);
				sDIABETES[rowidx] = (Double) ds.getFieldValue(DIABETESField);
				sCHEMOTHERAPY_ONLY[rowidx] = (Double) ds.getFieldValue(CHEMOTHERAPY_ONLYField);
				sRADIATION_THERAPY_ONLY[rowidx] = (Double) ds.getFieldValue(RADIATION_THERAPY_ONLYField);
				sCHEMORADIOTHERAPY[rowidx] = (Double) ds.getFieldValue(CHEMORADIOTHERAPYField);
				sWHO_ECOG_SCORE[rowidx] = (Double) ds.getFieldValue(WHO_ECOG_SCOREField);
				sMODIFIED_GLASGOW_SCORE[rowidx] = (Double) ds.getFieldValue(MODIFIED_GLASGOW_SCOREField);
				sASA[rowidx] = (Double) ds.getFieldValue(ASAField);
				sANESTHESIA_START[rowidx] = (String) ds.getFieldValue(ANESTHESIA_STARTField);
				sNCSP[rowidx] = (String) ds.getFieldValue(NCSPField);
				sOPERATION_DATE[rowidx] = (String) ds.getFieldValue(OPERATION_DATEField);
				sANASTOMOSIS[rowidx] = (Double) ds.getFieldValue(ANASTOMOSISField);
				sOSTOMY[rowidx] = (Double) ds.getFieldValue(OSTOMYField);
				sABDOMINAL_ACCESS[rowidx] = (Double) ds.getFieldValue(ABDOMINAL_ACCESSField);
				sROBOTASSISTANCE[rowidx] = (Double) ds.getFieldValue(ROBOTASSISTANCEField);
				sTHORAX_ACCESS[rowidx] = (Double) ds.getFieldValue(THORAX_ACCESSField);
				sRELAPAROTOMY[rowidx] = (Double) ds.getFieldValue(RELAPAROTOMYField);
				sACCORDION_SCORE[rowidx] = (String) ds.getFieldValue(ACCORDION_SCOREField);
				sisMale[rowidx] = (Double) ds.getFieldValue(isMaleField);
				sdecimalAge[rowidx] = (Double) ds.getFieldValue(decimalAgeField);
				sPRS_SCORE[rowidx] = (Double) ds.getFieldValue(PRS_SCOREField);
				sREADMISSION_STATUS[rowidx] = (Double) ds.getFieldValue(READMISSION_STATUSField);
				sSTATUS[rowidx] = (Double) ds.getFieldValue(STATUSField);
				
				sRELAPAROTOMY_YES[rowidx] = (Double) ds.getFieldValue(RELAPAROTOMY_YESField);
				sREADMISSION_ACCORDION_SCORE[rowidx] = (String) ds.getFieldValue(READMISSION_ACCORDION_SCOREField);
				sREADMISSION_RELAPAROTOMY[rowidx] = (Double) ds.getFieldValue(READMISSION_RELAPAROTOMYField);
				sREADMISSION_RELAPAROTOMY_YES[rowidx] = (Double) ds.getFieldValue(READMISSION_RELAPAROTOMY_YESField);
				
				getRow = ds.next();
				rowidx++;
			}
			rowidx--;
			
			log.debug("Finnished getting " + rowidx + " rows...");

			
			// Create and populate properly sized arrays
			double[] AvdRESH = new double[rowidx + 1];
			String[] Avdeling = new String[rowidx + 1];
			String[] BMI_CATEGORY = new String[rowidx + 1];
			double[] WEIGHTLOSS = new double[rowidx + 1];
			double[] DIABETES = new double[rowidx + 1];
			double[] CHEMOTHERAPY_ONLY = new double[rowidx + 1];
			double[] RADIATION_THERAPY_ONLY = new double[rowidx + 1];
			double[] CHEMORADIOTHERAPY = new double[rowidx + 1];
			double[] WHO_ECOG_SCORE = new double[rowidx + 1];
			double[] MODIFIED_GLASGOW_SCORE = new double[rowidx + 1];
			double[] ASA = new double[rowidx + 1];
			String[] ANESTHESIA_START = new String[rowidx + 1];
			String[] NCSP = new String[rowidx + 1];
			String[] OPERATION_DATE = new String[rowidx + 1];
			double[] ANASTOMOSIS = new double[rowidx + 1];
			double[] OSTOMY = new double[rowidx + 1];
			double[] ABDOMINAL_ACCESS = new double[rowidx + 1];
			double[] ROBOTASSISTANCE = new double[rowidx + 1];
			double[] THORAX_ACCESS = new double[rowidx + 1];
			double[] RELAPAROTOMY = new double[rowidx + 1];
			String[] ACCORDION_SCORE = new String[rowidx + 1];
			double[] isMale = new double[rowidx + 1];
			double[] decimalAge = new double[rowidx + 1];
			double[] PRS_SCORE = new double[rowidx + 1];
			double[] READMISSION_STATUS = new double[rowidx + 1];
			double[] STATUS = new double[rowidx + 1];
			
			double[] RELAPAROTOMY_YES = new double[rowidx + 1];
			String[] READMISSION_ACCORDION_SCORE = new String[rowidx + 1];
			double[] READMISSION_RELAPAROTOMY = new double[rowidx + 1];
			double[] READMISSION_RELAPAROTOMY_YES = new double[rowidx + 1];
			
			// ifs are needed because underlying query returns null. Since ints
			// cannot be null, these are returned as type double by the query
			int i = 0;
			while (i <= rowidx) {
				if (sAvdRESH[i] == null) {
					AvdRESH[i] = java.lang.Double.NaN;
				}
				else {
					AvdRESH[i] = sAvdRESH[i];
				}

				Avdeling[i] = sAvdeling[i];
				BMI_CATEGORY[i] = sBMI_CATEGORY[i];
				if (sWEIGHTLOSS[i] == null) {
					WEIGHTLOSS[i] = java.lang.Double.NaN;
				}
				else {
					WEIGHTLOSS[i] = sWEIGHTLOSS[i];
				}

				if (sDIABETES[i] == null) {
					DIABETES[i] = java.lang.Double.NaN;
				}
				else {
					DIABETES[i] = sDIABETES[i];
				}

				if (sCHEMOTHERAPY_ONLY[i] == null) {
					CHEMOTHERAPY_ONLY[i] = java.lang.Double.NaN;
				}
				else {
					CHEMOTHERAPY_ONLY[i] = sCHEMOTHERAPY_ONLY[i];
				}

				if (sRADIATION_THERAPY_ONLY[i] == null) {
					RADIATION_THERAPY_ONLY[i] = java.lang.Double.NaN;
				}
				else {
					RADIATION_THERAPY_ONLY[i] = sRADIATION_THERAPY_ONLY[i];
				}

				if (sCHEMORADIOTHERAPY[i] == null) {
					CHEMORADIOTHERAPY[i] = java.lang.Double.NaN;
				}
				else {
					CHEMORADIOTHERAPY[i] = sCHEMORADIOTHERAPY[i];
				}

				if (sWHO_ECOG_SCORE[i] == null) {
					WHO_ECOG_SCORE[i] = java.lang.Double.NaN;
				}
				else {
					WHO_ECOG_SCORE[i] = sWHO_ECOG_SCORE[i];
				}

				if (sMODIFIED_GLASGOW_SCORE[i] == null) {
					MODIFIED_GLASGOW_SCORE[i] = java.lang.Double.NaN;
				}
				else {
					MODIFIED_GLASGOW_SCORE[i] = sMODIFIED_GLASGOW_SCORE[i];
				}

				if (sASA[i] == null) {
					ASA[i] = java.lang.Double.NaN;
				}
				else {
					ASA[i] = sASA[i];
				}

				ANESTHESIA_START[i] = sANESTHESIA_START[i];
				NCSP[i] = sNCSP[i];
				OPERATION_DATE[i] = sOPERATION_DATE[i];
				if (sANASTOMOSIS[i] == null) {
					ANASTOMOSIS[i] = java.lang.Double.NaN;
				}
				else {
					ANASTOMOSIS[i] = sANASTOMOSIS[i];
				}

				if (sOSTOMY[i] == null) {
					OSTOMY[i] = java.lang.Double.NaN;
				}
				else {
					OSTOMY[i] = sOSTOMY[i];
				}

				if (sABDOMINAL_ACCESS[i] == null) {
					ABDOMINAL_ACCESS[i] = java.lang.Double.NaN;
				}
				else {
					ABDOMINAL_ACCESS[i] = sABDOMINAL_ACCESS[i];
				}

				if (sROBOTASSISTANCE[i] == null) {
					ROBOTASSISTANCE[i] = java.lang.Double.NaN;
				}
				else {
					ROBOTASSISTANCE[i] = sROBOTASSISTANCE[i];
				}

				if (sTHORAX_ACCESS[i] == null) {
					THORAX_ACCESS[i] = java.lang.Double.NaN;
				}
				else {
					THORAX_ACCESS[i] = sTHORAX_ACCESS[i];
				}

				if (sRELAPAROTOMY[i] == null) {
					RELAPAROTOMY[i] = java.lang.Double.NaN;
				}
				else {
					RELAPAROTOMY[i] = sRELAPAROTOMY[i];
				}

				ACCORDION_SCORE[i] = sACCORDION_SCORE[i];
				if (sisMale[i] == null) {
					isMale[i] = java.lang.Double.NaN;
				}
				else {
					isMale[i] = sisMale[i];
				}

				if (sdecimalAge[i] == null) {
					decimalAge[i] = java.lang.Double.NaN;
				}
				else {
					decimalAge[i] = sdecimalAge[i];
				}
				if (sPRS_SCORE[i] == null) {
					PRS_SCORE[i] = java.lang.Double.NaN;
				}
				else {
					PRS_SCORE[i] = sPRS_SCORE[i];
				}
				if (sREADMISSION_STATUS[i] == null) {
					READMISSION_STATUS[i] = java.lang.Double.NaN;
				}
				else {
					READMISSION_STATUS[i] = sREADMISSION_STATUS[i];
				}
				if (sSTATUS[i] == null) {
					STATUS[i] = java.lang.Double.NaN;
				}
				else {
					STATUS[i] = sSTATUS[i];
				}
				
				if (sRELAPAROTOMY_YES[i] == null) {
					RELAPAROTOMY_YES[i] = java.lang.Double.NaN;
				}
				else {
					RELAPAROTOMY_YES[i] = sRELAPAROTOMY_YES[i];
				}
				READMISSION_ACCORDION_SCORE[i] = sREADMISSION_ACCORDION_SCORE[i];
				if (sREADMISSION_RELAPAROTOMY[i] == null) {
					READMISSION_RELAPAROTOMY[i] = java.lang.Double.NaN;
				}
				else {
					READMISSION_RELAPAROTOMY[i] = sREADMISSION_RELAPAROTOMY[i];
				}
				if (sREADMISSION_RELAPAROTOMY_YES[i] == null) {
					READMISSION_RELAPAROTOMY_YES[i] = java.lang.Double.NaN;
				}
				else {
					READMISSION_RELAPAROTOMY_YES[i] = sREADMISSION_RELAPAROTOMY_YES[i];
				}
				i++;
			}

			
			log.debug("Creating the R dataframe");

			RList l = new RList();
			l.put("AvdRESH", new REXPDouble(AvdRESH));
			l.put("Avdeling", new REXPString(Avdeling));
			l.put("BMI_CATEGORY", new REXPString(BMI_CATEGORY));
			l.put("WEIGHTLOSS", new REXPDouble(WEIGHTLOSS));
			l.put("DIABETES", new REXPDouble(DIABETES));
			l.put("CHEMOTHERAPY_ONLY", new REXPDouble(CHEMOTHERAPY_ONLY));
			l.put("RADIATION_THERAPY_ONLY", new REXPDouble(RADIATION_THERAPY_ONLY));
			l.put("CHEMORADIOTHERAPY", new REXPDouble(CHEMORADIOTHERAPY));
			l.put("WHO_ECOG_SCORE", new REXPDouble(WHO_ECOG_SCORE));
			l.put("MODIFIED_GLASGOW_SCORE", new REXPDouble(MODIFIED_GLASGOW_SCORE));
			l.put("ASA", new REXPDouble(ASA));
			l.put("ANESTHESIA_START", new REXPString(ANESTHESIA_START));
			l.put("NCSP", new REXPString(NCSP));
			l.put("OPERATION_DATE", new REXPString(OPERATION_DATE));
			l.put("ANASTOMOSIS", new REXPDouble(ANASTOMOSIS));
			l.put("OSTOMY", new REXPDouble(OSTOMY));
			l.put("ABDOMINAL_ACCESS", new REXPDouble(ABDOMINAL_ACCESS));
			l.put("ROBOTASSISTANCE", new REXPDouble(ROBOTASSISTANCE));
			l.put("THORAX_ACCESS", new REXPDouble(THORAX_ACCESS));
			l.put("RELAPAROTOMY", new REXPDouble(RELAPAROTOMY));
			l.put("ACCORDION_SCORE", new REXPString(ACCORDION_SCORE));
			l.put("isMale", new REXPDouble(isMale));
			l.put("decimalAge", new REXPDouble(decimalAge));
			l.put("PRS_SCORE", new REXPDouble(PRS_SCORE));
			l.put("READMISSION_STATUS", new REXPDouble(READMISSION_STATUS));
			l.put("STATUS", new REXPDouble(STATUS));
			
			l.put("RELAPAROTOMY_YES", new REXPDouble(RELAPAROTOMY_YES));
			l.put("READMISSION_ACCORDION_SCORE", new REXPString(READMISSION_ACCORDION_SCORE));
			l.put("READMISSION_RELAPAROTOMY", new REXPDouble(READMISSION_RELAPAROTOMY));
			l.put("READMISSION_RELAPAROTOMY_YES", new REXPDouble(READMISSION_RELAPAROTOMY_YES));
			
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
