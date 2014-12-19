/**
 * no.skde.tools.report
 * NirCommonScriptlet.java Aug 8 2012 Are Edvardsen
 * 
 * 
 *  Copyleft 2011, 2012, 2014 SKDE
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
				log.debug("'valgtVar' set to: " + varName);
				rconn.voidEval("valgtVar=" + "'" + varName + "'");
			} catch (Exception e) {
				log.debug("Parameter 'varName' is not defined: " + e.getMessage());
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
			
			Date startDate;
			try {
				startDate = (Date) ((JRFillParameter) parametersMap.get("startDate")).getValue();
				if (startDate == null) {
					startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2010-01-01");
				}
				StringBuilder startDateString = new StringBuilder(rFormat.format(startDate));
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
			
			// set path to library
			String libkat = "'/opt/jasper/r/'";
			rconn.voidEval("libkat=" + libkat);
			
			// Load up primitive arrays with query data
			JRDataSource ds = (JRDataSource) ((JRFillParameter) parametersMap.get("REPORT_DATA_SOURCE")).getValue();
			
			JRField AvdelingField = (JRField) fieldsMap.get("Avdeling");
			JRField AvdRESHField = (JRField) fieldsMap.get("AvdRESH");
			JRField BilirubinField = (JRField) fieldsMap.get("Bilirubin");
			JRField BrainDamageField = (JRField) fieldsMap.get("BrainDamage");
			JRField CerebralCirculationAbolishedField = (JRField) fieldsMap.get("CerebralCirculationAbolished");
			JRField CerebralCirculationAbolishedReasonForNoField = (JRField) fieldsMap.get("CerebralCirculationAbolishedReasonForNo");
			JRField ChronicDiseasesField = (JRField) fieldsMap.get("ChronicDiseases");
			JRField DateAdmittedIntensiveField = (JRField) fieldsMap.get("DateAdmittedIntensive");
			JRField DateDischargedIntensiveField = (JRField) fieldsMap.get("DateDischargedIntensive");
			JRField DaysAdmittedIntensivField = (JRField) fieldsMap.get("DaysAdmittedIntensiv");
			JRField DeadPatientDuring24HoursField = (JRField) fieldsMap.get("DeadPatientDuring24Hours");
			JRField decimalAgeField = (JRField) fieldsMap.get("decimalAge");
			JRField DischargedHospitalStatusField = (JRField) fieldsMap.get("DischargedHospitalStatus");
			JRField DischargedIntensiveStatusField = (JRField) fieldsMap.get("DischargedIntensiveStatus");
			JRField GlasgowField = (JRField) fieldsMap.get("Glasgow");
			JRField Hco3Field = (JRField) fieldsMap.get("Hco3");
			JRField HeartRateField = (JRField) fieldsMap.get("HeartRate");
			JRField isMaleField = (JRField) fieldsMap.get("isMale");
			JRField LeukocytesField = (JRField) fieldsMap.get("Leukocytes");
			JRField MechanicalRespiratorField = (JRField) fieldsMap.get("MechanicalRespirator");
			JRField MoreThan24HoursField = (JRField) fieldsMap.get("MoreThan24Hours");
			JRField MovedPatientToAnotherIntensivDuring24HoursField = (JRField) fieldsMap.get("MovedPatientToAnotherIntensivDuring24Hours");
			JRField MvOrCpapField = (JRField) fieldsMap.get("MvOrCpap");
			JRField NasField = (JRField) fieldsMap.get("Nas");
			JRField NemsField = (JRField) fieldsMap.get("Nems");
			JRField OrganDonationCompletedReasonForNoStatusField = (JRField) fieldsMap.get("OrganDonationCompletedReasonForNoStatus");
			JRField OrganDonationCompletedStatusField = (JRField) fieldsMap.get("OrganDonationCompletedStatus");
			JRField PatientTransferredFromHospitalField = (JRField) fieldsMap.get("PatientTransferredFromHospital");
			JRField PatientTransferredToHospitalField = (JRField) fieldsMap.get("PatientTransferredToHospital");
			JRField PotassiumField = (JRField) fieldsMap.get("Potassium");
			JRField ReAdmittedField = (JRField) fieldsMap.get("ReAdmitted");
			JRField RespiratorField = (JRField) fieldsMap.get("Respirator");
			JRField Saps2ScoreField = (JRField) fieldsMap.get("Saps2Score");
			JRField Saps2ScoreNumberField = (JRField) fieldsMap.get("Saps2ScoreNumber");
			JRField SerumUreaOrBunField = (JRField) fieldsMap.get("SerumUreaOrBun");
			JRField ShNavnField = (JRField) fieldsMap.get("ShNavn");
			JRField ShTypeField = (JRField) fieldsMap.get("ShType");
			JRField ShTypeTxtField = (JRField) fieldsMap.get("ShTypeTxt");
			JRField SodiumField = (JRField) fieldsMap.get("Sodium");
			JRField SystolicBloodPressureField = (JRField) fieldsMap.get("SystolicBloodPressure");
			JRField TemperatureField = (JRField) fieldsMap.get("Temperature");
			JRField TransferredStatusField = (JRField) fieldsMap.get("TransferredStatus");
			JRField TypeOfAdmissionField = (JRField) fieldsMap.get("TypeOfAdmission");
			JRField UrineOutputField = (JRField) fieldsMap.get("UrineOutput");

			log.debug("Arrays of primitive data loaded");
			
			
			// Slug array
			String[] sAvdeling = new String[250000];
			Double[] sAvdRESH = new Double[250000];
			String[] sBilirubin = new String[250000];
			Double[] sBrainDamage = new Double[250000];
			Double[] sCerebralCirculationAbolished = new Double[250000];
			String[] sCerebralCirculationAbolishedReasonForNo = new String[250000];
			String[] sChronicDiseases = new String[250000];
			String[] sDateAdmittedIntensive = new String[250000];
			String[] sDateDischargedIntensive = new String[250000];
			Double[] sDaysAdmittedIntensiv = new Double[250000];
			Double[] sDeadPatientDuring24Hours = new Double[250000];
			Double[] sdecimalAge = new Double[250000];
			String[] sDischargedHospitalStatus = new String[250000];
			String[] sDischargedIntensiveStatus = new String[250000];
			Double[] sGlasgow = new Double[250000];
			Double[] sHco3 = new Double[250000];
			Double[] sHeartRate = new Double[250000];
			Double[] sisMale = new Double[250000];
			Double[] sLeukocytes = new Double[250000];
			Double[] sMechanicalRespirator = new Double[250000];
			Double[] sMoreThan24Hours = new Double[250000];
			Double[] sMovedPatientToAnotherIntensivDuring24Hours = new Double[250000];
			String[] sMvOrCpap = new String[250000];
			Double[] sNas = new Double[250000];
			Double[] sNems = new Double[250000];
			String[] sOrganDonationCompletedReasonForNoStatus = new String[250000];
			Double[] sOrganDonationCompletedStatus = new Double[250000];
			Double[] sPatientTransferredFromHospital = new Double[250000];
			Double[] sPatientTransferredToHospital = new Double[250000];
			Double[] sPotassium = new Double[250000];
			Double[] sReAdmitted = new Double[250000];
			Double[] sRespirator = new Double[250000];
			Double[] sSaps2Score = new Double[250000];
			Double[] sSaps2ScoreNumber = new Double[250000];
			String[] sSerumUreaOrBun = new String[250000];
			String[] sShNavn = new String[250000];
			String[] sShType = new String[250000];
			String[] sShTypeTxt = new String[250000];
			String[] sSodium = new String[250000];
			String[] sSystolicBloodPressure = new String[250000];
			Double[] sTemperature = new Double[250000];
			String[] sTransferredStatus = new String[250000];
			String[] sTypeOfAdmission = new String[250000];
			String[] sUrineOutput = new String[250000];

			// Fetch rows, assume that first is available...
			int rowidx = 0;
			boolean getRow = true;
			while (getRow) {
				sAvdeling[rowidx] = (String) ds.getFieldValue(AvdelingField);
				sAvdRESH[rowidx] = (Double) ds.getFieldValue(AvdRESHField);
				sBilirubin[rowidx] = (String) ds.getFieldValue(BilirubinField);
				sBrainDamage[rowidx] = (Double) ds.getFieldValue(BrainDamageField);
				sCerebralCirculationAbolished[rowidx] = (Double) ds.getFieldValue(CerebralCirculationAbolishedField);
				sCerebralCirculationAbolishedReasonForNo[rowidx] = (String) ds.getFieldValue(CerebralCirculationAbolishedReasonForNoField);
				sChronicDiseases[rowidx] = (String) ds.getFieldValue(ChronicDiseasesField);
				sDateAdmittedIntensive[rowidx] = (String) ds.getFieldValue(DateAdmittedIntensiveField);
				sDateDischargedIntensive[rowidx] = (String) ds.getFieldValue(DateDischargedIntensiveField);
				sDaysAdmittedIntensiv[rowidx] = (Double) ds.getFieldValue(DaysAdmittedIntensivField);
				sDeadPatientDuring24Hours[rowidx] = (Double) ds.getFieldValue(DeadPatientDuring24HoursField);
				sdecimalAge[rowidx] = (Double) ds.getFieldValue(decimalAgeField);
				sDischargedHospitalStatus[rowidx] = (String) ds.getFieldValue(DischargedHospitalStatusField);
				sDischargedIntensiveStatus[rowidx] = (String) ds.getFieldValue(DischargedIntensiveStatusField);
				sGlasgow[rowidx] = (Double) ds.getFieldValue(GlasgowField);
				sHco3[rowidx] = (Double) ds.getFieldValue(Hco3Field);
				sHeartRate[rowidx] = (Double) ds.getFieldValue(HeartRateField);
				sisMale[rowidx] = (Double) ds.getFieldValue(isMaleField);
				sLeukocytes[rowidx] = (Double) ds.getFieldValue(LeukocytesField);
				sMechanicalRespirator[rowidx] = (Double) ds.getFieldValue(MechanicalRespiratorField);
				sMoreThan24Hours[rowidx] = (Double) ds.getFieldValue(MoreThan24HoursField);
				sMovedPatientToAnotherIntensivDuring24Hours[rowidx] = (Double) ds.getFieldValue(MovedPatientToAnotherIntensivDuring24HoursField);
				sMvOrCpap[rowidx] = (String) ds.getFieldValue(MvOrCpapField);
				sNas[rowidx] = (Double) ds.getFieldValue(NasField);
				sNems[rowidx] = (Double) ds.getFieldValue(NemsField);
				sOrganDonationCompletedReasonForNoStatus[rowidx] = (String) ds.getFieldValue(OrganDonationCompletedReasonForNoStatusField);
				sOrganDonationCompletedStatus[rowidx] = (Double) ds.getFieldValue(OrganDonationCompletedStatusField);
				sPatientTransferredFromHospital[rowidx] = (Double) ds.getFieldValue(PatientTransferredFromHospitalField);
				sPatientTransferredToHospital[rowidx] = (Double) ds.getFieldValue(PatientTransferredToHospitalField);
				sPotassium[rowidx] = (Double) ds.getFieldValue(PotassiumField);
				sReAdmitted[rowidx] = (Double) ds.getFieldValue(ReAdmittedField);
				sRespirator[rowidx] = (Double) ds.getFieldValue(RespiratorField);
				sSaps2Score[rowidx] = (Double) ds.getFieldValue(Saps2ScoreField);
				sSaps2ScoreNumber[rowidx] = (Double) ds.getFieldValue(Saps2ScoreNumberField);
				sSerumUreaOrBun[rowidx] = (String) ds.getFieldValue(SerumUreaOrBunField);
				sShNavn[rowidx] = (String) ds.getFieldValue(ShNavnField);
				sShType[rowidx] = (String) ds.getFieldValue(ShTypeField);
				sShTypeTxt[rowidx] = (String) ds.getFieldValue(ShTypeTxtField);
				sSodium[rowidx] = (String) ds.getFieldValue(SodiumField);
				sSystolicBloodPressure[rowidx] = (String) ds.getFieldValue(SystolicBloodPressureField);
				sTemperature[rowidx] = (Double) ds.getFieldValue(TemperatureField);
				sTransferredStatus[rowidx] = (String) ds.getFieldValue(TransferredStatusField);
				sTypeOfAdmission[rowidx] = (String) ds.getFieldValue(TypeOfAdmissionField);
				sUrineOutput[rowidx] = (String) ds.getFieldValue(UrineOutputField);
				getRow = ds.next();
				rowidx++;
			}
			rowidx--;

			log.debug("Finnished getting " + rowidx + " rows...");
			
			// Create and populate properly sized arrays
			String[] Avdeling = new String[rowidx + 1];
			double[] AvdRESH = new double[rowidx + 1];
			String[] Bilirubin = new String[rowidx + 1];
			double[] BrainDamage = new double[rowidx + 1];
			double[] CerebralCirculationAbolished = new double[rowidx + 1];
			String[] CerebralCirculationAbolishedReasonForNo = new String[rowidx + 1];
			String[] ChronicDiseases = new String[rowidx + 1];
			String[] DateAdmittedIntensive = new String[rowidx + 1];
			String[] DateDischargedIntensive = new String[rowidx + 1];
			double[] DaysAdmittedIntensiv = new double[rowidx + 1];
			double[] DeadPatientDuring24Hours = new double[rowidx + 1];
			double[] decimalAge = new double[rowidx + 1];
			String[] DischargedHospitalStatus = new String[rowidx + 1];
			String[] DischargedIntensiveStatus = new String[rowidx + 1];
			double[] Glasgow = new double[rowidx + 1];
			double[] Hco3 = new double[rowidx + 1];
			double[] HeartRate = new double[rowidx + 1];
			double[] isMale = new double[rowidx + 1];
			double[] Leukocytes = new double[rowidx + 1];
			double[] MechanicalRespirator = new double[rowidx + 1];
			double[] MoreThan24Hours = new double[rowidx + 1];
			double[] MovedPatientToAnotherIntensivDuring24Hours = new double[rowidx + 1];
			String[] MvOrCpap = new String[rowidx + 1];
			double[] Nas = new double[rowidx + 1];
			double[] Nems = new double[rowidx + 1];
			String[] OrganDonationCompletedReasonForNoStatus = new String[rowidx + 1];
			double[] OrganDonationCompletedStatus = new double[rowidx + 1];
			double[] PatientTransferredFromHospital = new double[rowidx + 1];
			double[] PatientTransferredToHospital = new double[rowidx + 1];
			double[] Potassium = new double[rowidx + 1];
			double[] ReAdmitted = new double[rowidx + 1];
			double[] Respirator = new double[rowidx + 1];
			double[] Saps2Score = new double[rowidx + 1];
			double[] Saps2ScoreNumber = new double[rowidx + 1];
			String[] SerumUreaOrBun = new String[rowidx + 1];
			String[] ShNavn = new String[rowidx + 1];
			String[] ShType = new String[rowidx + 1];
			String[] ShTypeTxt = new String[rowidx + 1];
			String[] Sodium = new String[rowidx + 1];
			String[] SystolicBloodPressure = new String[rowidx + 1];
			double[] Temperature = new double[rowidx + 1];
			String[] TransferredStatus = new String[rowidx + 1];
			String[] TypeOfAdmission = new String[rowidx + 1];
			String[] UrineOutput = new String[rowidx + 1];


			// ifs are needed because underlying query returns null. Since ints
			// cannot be null, these are returned as type double by the query
			int i = 0;
			while (i <= rowidx) {
				Avdeling[i] = sAvdeling[i];
				if (sAvdRESH[i] == null) {
					AvdRESH[i] = java.lang.Double.NaN;
				}
				else {
					AvdRESH[i] = sAvdRESH[i];
				}

				Bilirubin[i] = sBilirubin[i];
				if (sBrainDamage[i] == null) {
					BrainDamage[i] = java.lang.Double.NaN;
				}
				else {
					BrainDamage[i] = sBrainDamage[i];
				}

				if (sCerebralCirculationAbolished[i] == null) {
					CerebralCirculationAbolished[i] = java.lang.Double.NaN;
				}
				else {
					CerebralCirculationAbolished[i] = sCerebralCirculationAbolished[i];
				}

				CerebralCirculationAbolishedReasonForNo[i] = sCerebralCirculationAbolishedReasonForNo[i];
				ChronicDiseases[i] = sChronicDiseases[i];
				DateAdmittedIntensive[i] = sDateAdmittedIntensive[i];
				DateDischargedIntensive[i] = sDateDischargedIntensive[i];
				if (sDaysAdmittedIntensiv[i] == null) {
					DaysAdmittedIntensiv[i] = java.lang.Double.NaN;
				}
				else {
					DaysAdmittedIntensiv[i] = sDaysAdmittedIntensiv[i];
				}

				if (sDeadPatientDuring24Hours[i] == null) {
					DeadPatientDuring24Hours[i] = java.lang.Double.NaN;
				}
				else {
					DeadPatientDuring24Hours[i] = sDeadPatientDuring24Hours[i];
				}

				if (sdecimalAge[i] == null) {
					decimalAge[i] = java.lang.Double.NaN;
				}
				else {
					decimalAge[i] = sdecimalAge[i];
				}

				DischargedHospitalStatus[i] = sDischargedHospitalStatus[i];
				DischargedIntensiveStatus[i] = sDischargedIntensiveStatus[i];
				if (sGlasgow[i] == null) {
					Glasgow[i] = java.lang.Double.NaN;
				}
				else {
					Glasgow[i] = sGlasgow[i];
				}

				if (sHco3[i] == null) {
					Hco3[i] = java.lang.Double.NaN;
				}
				else {
					Hco3[i] = sHco3[i];
				}

				if (sHeartRate[i] == null) {
					HeartRate[i] = java.lang.Double.NaN;
				}
				else {
					HeartRate[i] = sHeartRate[i];
				}

				if (sisMale[i] == null) {
					isMale[i] = java.lang.Double.NaN;
				}
				else {
					isMale[i] = sisMale[i];
				}

				if (sLeukocytes[i] == null) {
					Leukocytes[i] = java.lang.Double.NaN;
				}
				else {
					Leukocytes[i] = sLeukocytes[i];
				}

				if (sMechanicalRespirator[i] == null) {
					MechanicalRespirator[i] = java.lang.Double.NaN;
				}
				else {
					MechanicalRespirator[i] = sMechanicalRespirator[i];
				}

				if (sMoreThan24Hours[i] == null) {
					MoreThan24Hours[i] = java.lang.Double.NaN;
				}
				else {
					MoreThan24Hours[i] = sMoreThan24Hours[i];
				}

				if (sMovedPatientToAnotherIntensivDuring24Hours[i] == null) {
					MovedPatientToAnotherIntensivDuring24Hours[i] = java.lang.Double.NaN;
				}
				else {
					MovedPatientToAnotherIntensivDuring24Hours[i] = sMovedPatientToAnotherIntensivDuring24Hours[i];
				}

				MvOrCpap[i] = sMvOrCpap[i];
				if (sNas[i] == null) {
					Nas[i] = java.lang.Double.NaN;
				}
				else {
					Nas[i] = sNas[i];
				}

				if (sNems[i] == null) {
					Nems[i] = java.lang.Double.NaN;
				}
				else {
					Nems[i] = sNems[i];
				}

				OrganDonationCompletedReasonForNoStatus[i] = sOrganDonationCompletedReasonForNoStatus[i];
				if (sOrganDonationCompletedStatus[i] == null) {
					OrganDonationCompletedStatus[i] = java.lang.Double.NaN;
				}
				else {
					OrganDonationCompletedStatus[i] = sOrganDonationCompletedStatus[i];
				}

				if (sPatientTransferredFromHospital[i] == null) {
					PatientTransferredFromHospital[i] = java.lang.Double.NaN;
				}
				else {
					PatientTransferredFromHospital[i] = sPatientTransferredFromHospital[i];
				}

				if (sPatientTransferredToHospital[i] == null) {
					PatientTransferredToHospital[i] = java.lang.Double.NaN;
				}
				else {
					PatientTransferredToHospital[i] = sPatientTransferredToHospital[i];
				}

				if (sPotassium[i] == null) {
					Potassium[i] = java.lang.Double.NaN;
				}
				else {
					Potassium[i] = sPotassium[i];
				}

				if (sReAdmitted[i] == null) {
					ReAdmitted[i] = java.lang.Double.NaN;
				}
				else {
					ReAdmitted[i] = sReAdmitted[i];
				}

				if (sRespirator[i] == null) {
					Respirator[i] = java.lang.Double.NaN;
				}
				else {
					Respirator[i] = sRespirator[i];
				}

				if (sSaps2Score[i] == null) {
					Saps2Score[i] = java.lang.Double.NaN;
				}
				else {
					Saps2Score[i] = sSaps2Score[i];
				}

				if (sSaps2ScoreNumber[i] == null) {
					Saps2ScoreNumber[i] = java.lang.Double.NaN;
				}
				else {
					Saps2ScoreNumber[i] = sSaps2ScoreNumber[i];
				}

				SerumUreaOrBun[i] = sSerumUreaOrBun[i];
				ShNavn[i] = sShNavn[i];
				ShType[i] = sShType[i];
				ShTypeTxt[i] = sShTypeTxt[i];
				Sodium[i] = sSodium[i];
				SystolicBloodPressure[i] = sSystolicBloodPressure[i];
				if (sTemperature[i] == null) {
					Temperature[i] = java.lang.Double.NaN;
				}
				else {
					Temperature[i] = sTemperature[i];
				}

				TransferredStatus[i] = sTransferredStatus[i];
				TypeOfAdmission[i] = sTypeOfAdmission[i];
				UrineOutput[i] = sUrineOutput[i];
				i++;
			}

			
			log.debug("Creating the R dataframe");
			
			RList l = new RList();
			
			l.put("Avdeling", new REXPString(Avdeling));
			l.put("AvdRESH", new REXPDouble(AvdRESH));
			l.put("Bilirubin", new REXPString(Bilirubin));
			l.put("BrainDamage", new REXPDouble(BrainDamage));
			l.put("CerebralCirculationAbolished", new REXPDouble(CerebralCirculationAbolished));
			l.put("CerebralCirculationAbolishedReasonForNo", new REXPString(CerebralCirculationAbolishedReasonForNo));
			l.put("ChronicDiseases", new REXPString(ChronicDiseases));
			l.put("DateAdmittedIntensive", new REXPString(DateAdmittedIntensive));
			l.put("DateDischargedIntensive", new REXPString(DateDischargedIntensive));
			l.put("DaysAdmittedIntensiv", new REXPDouble(DaysAdmittedIntensiv));
			l.put("DeadPatientDuring24Hours", new REXPDouble(DeadPatientDuring24Hours));
			l.put("decimalAge", new REXPDouble(decimalAge));
			l.put("DischargedHospitalStatus", new REXPString(DischargedHospitalStatus));
			l.put("DischargedIntensiveStatus", new REXPString(DischargedIntensiveStatus));
			l.put("Glasgow", new REXPDouble(Glasgow));
			l.put("Hco3", new REXPDouble(Hco3));
			l.put("HeartRate", new REXPDouble(HeartRate));
			l.put("isMale", new REXPDouble(isMale));
			l.put("Leukocytes", new REXPDouble(Leukocytes));
			l.put("MechanicalRespirator", new REXPDouble(MechanicalRespirator));
			l.put("MoreThan24Hours", new REXPDouble(MoreThan24Hours));
			l.put("MovedPatientToAnotherIntensivDuring24Hours", new REXPDouble(MovedPatientToAnotherIntensivDuring24Hours));
			l.put("MvOrCpap", new REXPString(MvOrCpap));
			l.put("Nas", new REXPDouble(Nas));
			l.put("Nems", new REXPDouble(Nems));
			l.put("OrganDonationCompletedReasonForNoStatus", new REXPString(OrganDonationCompletedReasonForNoStatus));
			l.put("OrganDonationCompletedStatus", new REXPDouble(OrganDonationCompletedStatus));
			l.put("PatientTransferredFromHospital", new REXPDouble(PatientTransferredFromHospital));
			l.put("PatientTransferredToHospital", new REXPDouble(PatientTransferredToHospital));
			l.put("Potassium", new REXPDouble(Potassium));
			l.put("ReAdmitted", new REXPDouble(ReAdmitted));
			l.put("Respirator", new REXPDouble(Respirator));
			l.put("Saps2Score", new REXPDouble(Saps2Score));
			l.put("Saps2ScoreNumber", new REXPDouble(Saps2ScoreNumber));
			l.put("SerumUreaOrBun", new REXPString(SerumUreaOrBun));
			l.put("ShNavn", new REXPString(ShNavn));
			l.put("ShType", new REXPString(ShType));
			l.put("ShTypeTxt", new REXPString(ShTypeTxt));
			l.put("Sodium", new REXPString(Sodium));
			l.put("SystolicBloodPressure", new REXPString(SystolicBloodPressure));
			l.put("Temperature", new REXPDouble(Temperature));
			l.put("TransferredStatus", new REXPString(TransferredStatus));
			l.put("TypeOfAdmission", new REXPString(TypeOfAdmission));
			l.put("UrineOutput", new REXPString(UrineOutput));
			
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
			
			// Source the function
			rconn.assign("source_file", "/opt/jasper/r/" + rScriptName);
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