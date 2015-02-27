/**
 * no.skde.report.norscir
 * NorScirCommonScriptlet.java Apr 14 2013 Are Edvardsen
 * 
 * 
 *  Copyleft 2013, SKDE
 */

package no.skde.report.norscir;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.fill.*;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.*;

public class NorScirCommonScriptlet extends JRDefaultScriptlet {

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
			log.info("Start generating R report using " + NorScirCommonScriptlet.class.getName());
			
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
			
			
			String isTrauma;
			try {
				isTrauma = (String) ((JRFillParameter) parametersMap.get("isTrauma")).getValue();
				if (isTrauma == null) {
					isTrauma = "na";
				}

				rconn.voidEval("traume=" + "'" + isTrauma.toString() + "'");
			} catch (Exception e) {
				log.debug("Parameter isTrauma is not defined: " + e.getMessage());
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
				log.warn("Parameter 'myDept' is deprecated and may be removed in future versions. Replaced by 'orgUnitSelection'");
				rconn.voidEval("egenavd=" + myDept.toString());
			} catch (Exception e) {
				log.debug("Parameter myDept is not defined: " + e.getMessage());
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
			
			// AIS; multi select list of values
			List<String> aisList = new ArrayList<String>();
			String ais;
			try {
				aisList = (ArrayList<String>) ((JRFillParameter) parametersMap.get("ais")).getValue();
				ais = "c(";
				if (aisList.contains("all")) {
					ais = ais + "'')";
				} else {
					Iterator<String> iterator = aisList.iterator();
					while (iterator.hasNext()) {
						ais = ais + "'" + iterator.next() + "',";
					}
					ais = ais.substring(0, ais.length()-1);
					ais = ais + ")";
				}
				log.debug("R concat for AIS vector is " + ais);
				rconn.voidEval("AIS=" + ais);
			} catch (Exception e) {
				log.debug("Parameter ais is not defined: " + e.getMessage());
			}
			
			
			// set path to library, to be removed since Rapporteket uses same directory for all R files (noweb, libs and report funs)
			String libkat = "'/opt/jasper/r/'";
			rconn.voidEval("libkat=" + libkat);
			
			// Load up primitive arrays with query data
			JRDataSource ds = (JRDataSource) ((JRFillParameter) parametersMap
					.get("REPORT_DATA_SOURCE")).getValue();
			
			JRField KontaktFraDatoField = (JRField) fieldsMap.get("KontaktFraDato");
			JRField DataSettIDField = (JRField) fieldsMap.get("DataSettID");
			JRField ScietiolField = (JRField) fieldsMap.get("Scietiol");
			JRField isVrtbrInjField = (JRField) fieldsMap.get("isVrtbrInj");
			JRField isAssocInjField = (JRField) fieldsMap.get("isAssocInj");
			JRField isSpnlSurgField = (JRField) fieldsMap.get("isSpnlSurg");
			JRField VentAssiField = (JRField) fieldsMap.get("VentAssi");
			JRField PlaceDisField = (JRField) fieldsMap.get("PlaceDis");
			JRField ASensLvlAreaLField = (JRField) fieldsMap.get("ASensLvlAreaL");
			JRField ASensLvlAreaRField = (JRField) fieldsMap.get("ASensLvlAreaR");
			JRField AMtrLvlAreaLField = (JRField) fieldsMap.get("AMtrLvlAreaL");
			JRField AMtrLvlAreaRField = (JRField) fieldsMap.get("AMtrLvlAreaR");
			JRField AAisField = (JRField) fieldsMap.get("AAis");
			JRField FSensLvlAreaLField = (JRField) fieldsMap.get("FSensLvlAreaL");
			JRField FSensLvlAreaRField = (JRField) fieldsMap.get("FSensLvlAreaR");
			JRField FMtrLvlAreaLField = (JRField) fieldsMap.get("FMtrLvlAreaL");
			JRField FMtrLvlAreaRField = (JRField) fieldsMap.get("FMtrLvlAreaR");
			JRField FAisField = (JRField) fieldsMap.get("FAis");
			JRField BirthDtField = (JRField) fieldsMap.get("BirthDt");
			JRField InjuryDtField = (JRField) fieldsMap.get("InjuryDt");
			JRField AdmitDtField = (JRField) fieldsMap.get("AdmitDt");
			JRField DischgDtField = (JRField) fieldsMap.get("DischgDt");
			JRField ANeuExmDtField = (JRField) fieldsMap.get("ANeuExmDt");
			JRField FNeuExmDtField = (JRField) fieldsMap.get("FNeuExmDt");
			JRField QolDtField = (JRField) fieldsMap.get("QolDt");
			JRField AdmitRehDtField = (JRField) fieldsMap.get("AdmitRehDt");
			JRField ANeuNoMeasureField = (JRField) fieldsMap.get("ANeuNoMeasure");
			JRField FNeuNoMeasureField = (JRField) fieldsMap.get("FNeuNoMeasure");
			JRField InjuryDateUnknownField = (JRField) fieldsMap.get("InjuryDateUnknown");
			JRField OutOfHosptlDyField = (JRField) fieldsMap.get("OutOfHosptlDy");
			JRField HosptlDyField = (JRField) fieldsMap.get("HosptlDy");
			JRField isMaleField = (JRField) fieldsMap.get("isMale");
			JRField SatGenrlField = (JRField) fieldsMap.get("SatGenrl");
			JRField SatPhysField = (JRField) fieldsMap.get("SatPhys");
			JRField SatPsychField = (JRField) fieldsMap.get("SatPsych");
			JRField SkjemaIDField = (JRField) fieldsMap.get("SkjemaID");
			JRField ReshIdField = (JRField) fieldsMap.get("ReshId");
			JRField PIDField = (JRField) fieldsMap.get("PID");
			JRField DagerRehabField = (JRField) fieldsMap.get("DagerRehab");
			JRField DagerTilRehabField = (JRField) fieldsMap.get("DagerTilRehab");
			JRField AlderAarField = (JRField) fieldsMap.get("AlderAar");
			JRField ShNavnField = (JRField) fieldsMap.get("ShNavn");
			JRField NevrNivaaInnField = (JRField) fieldsMap.get("NevrNivaaInn");
			JRField NevrNivaaUtField = (JRField) fieldsMap.get("NevrNivaaUt");
			
			log.debug("Arrays of primitive data loaded");
			
			
			// Create "slug arrays" with very big sizes (default limit to
			// 1000000) to accommodate large queries
			// We cannot find out how many rows are returned so we have to fetch
			// first and then
			// rebuild arrays of the proper size before passing to R
			//
			// Arrays MUST be defined as objects and not its primitive since
			// returned values do contain 'null'
			String[] sKontaktFraDato = new String[1000000];
			Integer[] sDataSettID = new Integer[1000000];
			String[] sScietiol = new String[1000000];
			Double[] sisVrtbrInj = new Double[1000000];
			Double[] sisAssocInj = new Double[1000000];
			Double[] sisSpnlSurg = new Double[1000000];
			String[] sVentAssi = new String[1000000];
			String[] sPlaceDis = new String[1000000];
			String[] sASensLvlAreaL = new String[1000000];
			String[] sASensLvlAreaR = new String[1000000];
			String[] sAMtrLvlAreaL = new String[1000000];
			String[] sAMtrLvlAreaR = new String[1000000];
			String[] sAAis = new String[1000000];
			String[] sFSensLvlAreaL = new String[1000000];
			String[] sFSensLvlAreaR = new String[1000000];
			String[] sFMtrLvlAreaL = new String[1000000];
			String[] sFMtrLvlAreaR = new String[1000000];
			String[] sFAis = new String[1000000];
			String[] sBirthDt = new String[1000000];
			String[] sInjuryDt = new String[1000000];
			String[] sAdmitDt = new String[1000000];
			String[] sDischgDt = new String[1000000];
			String[] sANeuExmDt = new String[1000000];
			String[] sFNeuExmDt = new String[1000000];
			String[] sQolDt = new String[1000000];
			String[] sAdmitRehDt = new String[1000000];
			Double[] sANeuNoMeasure = new Double[1000000];
			Double[] sFNeuNoMeasure = new Double[1000000];
			Double[] sInjuryDateUnknown = new Double[1000000];
			Double[] sOutOfHosptlDy = new Double[1000000];
			Double[] sHosptlDy = new Double[1000000];
			Double[] sisMale = new Double[1000000];
			Double[] sSatGenrl = new Double[1000000];
			Double[] sSatPhys = new Double[1000000];
			Double[] sSatPsych = new Double[1000000];
			Double[] sSkjemaID = new Double[1000000];
			Double[] sReshId = new Double[1000000];
			String[] sPID = new String[1000000];
			Double[] sDagerRehab = new Double[1000000];
			Double[] sDagerTilRehab = new Double[1000000];
			Double[] sAlderAar = new Double[1000000];
			String[] sShNavn = new String[1000000];
			Double[] sNevrNivaaInn = new Double[1000000];
			Double[] sNevrNivaaUt = new Double[1000000];
			
			
			int rowidx = 0;

			
			// Assume we get 1 row
			boolean getRow = true;		
			log.debug("getting rows...");

			while (getRow) {
				sKontaktFraDato[rowidx] = (String) ds.getFieldValue(KontaktFraDatoField);
				sDataSettID[rowidx] = (Integer) ds.getFieldValue(DataSettIDField);
				sScietiol[rowidx] = (String) ds.getFieldValue(ScietiolField);
				sisVrtbrInj[rowidx] = (Double) ds.getFieldValue(isVrtbrInjField);
				sisAssocInj[rowidx] = (Double) ds.getFieldValue(isAssocInjField);
				sisSpnlSurg[rowidx] = (Double) ds.getFieldValue(isSpnlSurgField);
				sVentAssi[rowidx] = (String) ds.getFieldValue(VentAssiField);
				sPlaceDis[rowidx] = (String) ds.getFieldValue(PlaceDisField);
				sASensLvlAreaL[rowidx] = (String) ds.getFieldValue(ASensLvlAreaLField);
				sASensLvlAreaR[rowidx] = (String) ds.getFieldValue(ASensLvlAreaRField);
				sAMtrLvlAreaL[rowidx] = (String) ds.getFieldValue(AMtrLvlAreaLField);
				sAMtrLvlAreaR[rowidx] = (String) ds.getFieldValue(AMtrLvlAreaRField);
				sAAis[rowidx] = (String) ds.getFieldValue(AAisField);
				sFSensLvlAreaL[rowidx] = (String) ds.getFieldValue(FSensLvlAreaLField);
				sFSensLvlAreaR[rowidx] = (String) ds.getFieldValue(FSensLvlAreaRField);
				sFMtrLvlAreaL[rowidx] = (String) ds.getFieldValue(FMtrLvlAreaLField);
				sFMtrLvlAreaR[rowidx] = (String) ds.getFieldValue(FMtrLvlAreaRField);
				sFAis[rowidx] = (String) ds.getFieldValue(FAisField);
				sBirthDt[rowidx] = (String) ds.getFieldValue(BirthDtField);
				sInjuryDt[rowidx] = (String) ds.getFieldValue(InjuryDtField);
				sAdmitDt[rowidx] = (String) ds.getFieldValue(AdmitDtField);
				sDischgDt[rowidx] = (String) ds.getFieldValue(DischgDtField);
				sANeuExmDt[rowidx] = (String) ds.getFieldValue(ANeuExmDtField);
				sFNeuExmDt[rowidx] = (String) ds.getFieldValue(FNeuExmDtField);
				sQolDt[rowidx] = (String) ds.getFieldValue(QolDtField);
				sAdmitRehDt[rowidx] = (String) ds.getFieldValue(AdmitRehDtField);
				sANeuNoMeasure[rowidx] = (Double) ds.getFieldValue(ANeuNoMeasureField);
				sFNeuNoMeasure[rowidx] = (Double) ds.getFieldValue(FNeuNoMeasureField);
				sInjuryDateUnknown[rowidx] = (Double) ds.getFieldValue(InjuryDateUnknownField);
				sOutOfHosptlDy[rowidx] = (Double) ds.getFieldValue(OutOfHosptlDyField);
				sHosptlDy[rowidx] = (Double) ds.getFieldValue(HosptlDyField);
				sisMale[rowidx] = (Double) ds.getFieldValue(isMaleField);
				sSatGenrl[rowidx] = (Double) ds.getFieldValue(SatGenrlField);
				sSatPhys[rowidx] = (Double) ds.getFieldValue(SatPhysField);
				sSatPsych[rowidx] = (Double) ds.getFieldValue(SatPsychField);
				sSkjemaID[rowidx] = (Double) ds.getFieldValue(SkjemaIDField);
				sReshId[rowidx] = (Double) ds.getFieldValue(ReshIdField);
				sPID[rowidx] = (String) ds.getFieldValue(PIDField);
				sDagerRehab[rowidx] = (Double) ds.getFieldValue(DagerRehabField);
				sDagerTilRehab[rowidx] = (Double) ds.getFieldValue(DagerTilRehabField);
				sAlderAar[rowidx] = (Double) ds.getFieldValue(AlderAarField);
				sShNavn[rowidx] = (String) ds.getFieldValue(ShNavnField);
				sNevrNivaaInn[rowidx] = (Double) ds.getFieldValue(NevrNivaaInnField);
				sNevrNivaaUt[rowidx] = (Double) ds.getFieldValue(NevrNivaaUtField);

				getRow = ds.next();
				rowidx++;
			}
			rowidx--;
			
			log.debug("Finnished getting " + rowidx + " rows...");

			
			// Create and populate properly sized arrays
			String[] KontaktFraDato = new String[rowidx + 1];
			int[] DataSettID = new int[rowidx + 1];
			String[] Scietiol = new String[rowidx + 1];
			double[] isVrtbrInj = new double[rowidx + 1];
			double[] isAssocInj = new double[rowidx + 1];
			double[] isSpnlSurg = new double[rowidx + 1];
			String[] VentAssi = new String[rowidx + 1];
			String[] PlaceDis = new String[rowidx + 1];
			String[] ASensLvlAreaL = new String[rowidx + 1];
			String[] ASensLvlAreaR = new String[rowidx + 1];
			String[] AMtrLvlAreaL = new String[rowidx + 1];
			String[] AMtrLvlAreaR = new String[rowidx + 1];
			String[] AAis = new String[rowidx + 1];
			String[] FSensLvlAreaL = new String[rowidx + 1];
			String[] FSensLvlAreaR = new String[rowidx + 1];
			String[] FMtrLvlAreaL = new String[rowidx + 1];
			String[] FMtrLvlAreaR = new String[rowidx + 1];
			String[] FAis = new String[rowidx + 1];
			String[] BirthDt = new String[rowidx + 1];
			String[] InjuryDt = new String[rowidx + 1];
			String[] AdmitDt = new String[rowidx + 1];
			String[] DischgDt = new String[rowidx + 1];
			String[] ANeuExmDt = new String[rowidx + 1];
			String[] FNeuExmDt = new String[rowidx + 1];
			String[] QolDt = new String[rowidx + 1];
			String[] AdmitRehDt = new String[rowidx + 1];
			double[] ANeuNoMeasure = new double[rowidx + 1];
			double[] FNeuNoMeasure = new double[rowidx + 1];
			double[] InjuryDateUnknown = new double[rowidx + 1];
			double[] OutOfHosptlDy = new double[rowidx + 1];
			double[] HosptlDy = new double[rowidx + 1];
			double[] isMale = new double[rowidx + 1];
			double[] SatGenrl = new double[rowidx + 1];
			double[] SatPhys = new double[rowidx + 1];
			double[] SatPsych = new double[rowidx + 1];
			double[] SkjemaID = new double[rowidx + 1];
			double[] ReshId = new double[rowidx + 1];
			String[] PID = new String[rowidx + 1];
			double[] DagerRehab = new double[rowidx + 1];
			double[] DagerTilRehab = new double[rowidx + 1];
			double[] AlderAar = new double[rowidx + 1];
			String[] ShNavn = new String[rowidx + 1];
			double[] NevrNivaaInn = new double[rowidx + 1];
			double[] NevrNivaaUt = new double[rowidx + 1];

			
			int i = 0;
			
			// ifs are needed because underlying query returns null. Since ints
			// cannot be null, these are returned as type double by the query
			while (i <= rowidx) {
				DataSettID[i] = sDataSettID[i];
				
				KontaktFraDato[i] = sKontaktFraDato[i];
				Scietiol[i] = sScietiol[i];
				VentAssi[i] = sVentAssi[i];
				PlaceDis[i] = sPlaceDis[i];
				ASensLvlAreaL[i] = sASensLvlAreaL[i];
				ASensLvlAreaR[i] = sASensLvlAreaR[i];
				AMtrLvlAreaL[i] = sAMtrLvlAreaL[i];
				AMtrLvlAreaR[i] = sAMtrLvlAreaR[i];
				AAis[i] = sAAis[i];
				FSensLvlAreaL[i] = sFSensLvlAreaL[i];
				FSensLvlAreaR[i] = sFSensLvlAreaR[i];
				FMtrLvlAreaL[i] = sFMtrLvlAreaL[i];
				FMtrLvlAreaR[i] = sFMtrLvlAreaR[i];
				FAis[i] = sFAis[i];
				BirthDt[i] = sBirthDt[i];
				InjuryDt[i] = sInjuryDt[i];
				AdmitDt[i] = sAdmitDt[i];
				DischgDt[i] = sDischgDt[i];
				ANeuExmDt[i] = sANeuExmDt[i];
				FNeuExmDt[i] = sFNeuExmDt[i];
				QolDt[i] = sQolDt[i];
				AdmitRehDt[i] = sAdmitRehDt[i];
				PID[i] = sPID[i];
				ShNavn[i] = sShNavn[i];

				if (sisVrtbrInj[i] == null) {
					isVrtbrInj[i] = java.lang.Double.NaN;
				}
				else {
					isVrtbrInj[i] = sisVrtbrInj[i];
				}
							
				if (sisAssocInj[i] == null) {
					isAssocInj[i] = java.lang.Double.NaN;
				}
				else {
					isAssocInj[i] = sisAssocInj[i];
				}
							
				if (sisSpnlSurg[i] == null) {
					isSpnlSurg[i] = java.lang.Double.NaN;
				}
				else {
					isSpnlSurg[i] = sisSpnlSurg[i];
				}
							
				if (sANeuNoMeasure[i] == null) {
					ANeuNoMeasure[i] = java.lang.Double.NaN;
				}
				else {
					ANeuNoMeasure[i] = sANeuNoMeasure[i];
				}
							
				if (sFNeuNoMeasure[i] == null) {
					FNeuNoMeasure[i] = java.lang.Double.NaN;
				}
				else {
					FNeuNoMeasure[i] = sFNeuNoMeasure[i];
				}
							
				if (sInjuryDateUnknown[i] == null) {
					InjuryDateUnknown[i] = java.lang.Double.NaN;
				}
				else {
					InjuryDateUnknown[i] = sInjuryDateUnknown[i];
				}
							
				if (sOutOfHosptlDy[i] == null) {
					OutOfHosptlDy[i] = java.lang.Double.NaN;
				}
				else {
					OutOfHosptlDy[i] = sOutOfHosptlDy[i];
				}
							
				if (sHosptlDy[i] == null) {
					HosptlDy[i] = java.lang.Double.NaN;
				}
				else {
					HosptlDy[i] = sHosptlDy[i];
				}
							
				if (sisMale[i] == null) {
					isMale[i] = java.lang.Double.NaN;
				}
				else {
					isMale[i] = sisMale[i];
				}
							
				if (sSatGenrl[i] == null) {
					SatGenrl[i] = java.lang.Double.NaN;
				}
				else {
					SatGenrl[i] = sSatGenrl[i];
				}
							
				if (sSatPhys[i] == null) {
					SatPhys[i] = java.lang.Double.NaN;
				}
				else {
					SatPhys[i] = sSatPhys[i];
				}
							
				if (sSatPsych[i] == null) {
					SatPsych[i] = java.lang.Double.NaN;
				}
				else {
					SatPsych[i] = sSatPsych[i];
				}
							
				if (sSkjemaID[i] == null) {
					SkjemaID[i] = java.lang.Double.NaN;
				}
				else {
					SkjemaID[i] = sSkjemaID[i];
				}
							
				if (sReshId[i] == null) {
					ReshId[i] = java.lang.Double.NaN;
				}
				else {
					ReshId[i] = sReshId[i];
				}
							
				if (sDagerRehab[i] == null) {
					DagerRehab[i] = java.lang.Double.NaN;
				}
				else {
					DagerRehab[i] = sDagerRehab[i];
				}
							
				if (sDagerTilRehab[i] == null) {
					DagerTilRehab[i] = java.lang.Double.NaN;
				}
				else {
					DagerTilRehab[i] = sDagerTilRehab[i];
				}
							
				if (sAlderAar[i] == null) {
					AlderAar[i] = java.lang.Double.NaN;
				}
				else {
					AlderAar[i] = sAlderAar[i];
				}
							
				if (sNevrNivaaInn[i] == null) {
					NevrNivaaInn[i] = java.lang.Double.NaN;
				}
				else {
					NevrNivaaInn[i] = sNevrNivaaInn[i];
				}
							
				if (sNevrNivaaUt[i] == null) {
					NevrNivaaUt[i] = java.lang.Double.NaN;
				}
				else {
					NevrNivaaUt[i] = sNevrNivaaUt[i];
				}
							
				i++;
			}

			
			log.debug("Creating the R dataframe");

			RList l = new RList();

			l.put("KontaktFraDato", new REXPString(KontaktFraDato));
			l.put("DataSettID", new REXPInteger(DataSettID));
			l.put("Scietiol", new REXPString(Scietiol));
			l.put("isVrtbrInj", new REXPDouble(isVrtbrInj));
			l.put("isAssocInj", new REXPDouble(isAssocInj));
			l.put("isSpnlSurg", new REXPDouble(isSpnlSurg));
			l.put("VentAssi", new REXPString(VentAssi));
			l.put("PlaceDis", new REXPString(PlaceDis));
			l.put("ASensLvlAreaL", new REXPString(ASensLvlAreaL));
			l.put("ASensLvlAreaR", new REXPString(ASensLvlAreaR));
			l.put("AMtrLvlAreaL", new REXPString(AMtrLvlAreaL));
			l.put("AMtrLvlAreaR", new REXPString(AMtrLvlAreaR));
			l.put("AAis", new REXPString(AAis));
			l.put("FSensLvlAreaL", new REXPString(FSensLvlAreaL));
			l.put("FSensLvlAreaR", new REXPString(FSensLvlAreaR));
			l.put("FMtrLvlAreaL", new REXPString(FMtrLvlAreaL));
			l.put("FMtrLvlAreaR", new REXPString(FMtrLvlAreaR));
			l.put("FAis", new REXPString(FAis));
			l.put("BirthDt", new REXPString(BirthDt));
			l.put("InjuryDt", new REXPString(InjuryDt ));
			l.put("AdmitDt", new REXPString(AdmitDt));
			l.put("DischgDt", new REXPString(DischgDt));
			l.put("ANeuExmDt", new REXPString(ANeuExmDt));
			l.put("FNeuExmDt", new REXPString(FNeuExmDt));
			l.put("QolDt", new REXPString(QolDt));
			l.put("AdmitRehDt", new REXPString(AdmitRehDt));
			l.put("ANeuNoMeasure", new REXPDouble(ANeuNoMeasure));
			l.put("FNeuNoMeasure", new REXPDouble(FNeuNoMeasure));
			l.put("InjuryDateUnknown", new REXPDouble(InjuryDateUnknown));
			l.put("OutOfHosptlDy", new REXPDouble(OutOfHosptlDy));
			l.put("HosptlDy", new REXPDouble(HosptlDy));
			l.put("isMale", new REXPDouble(isMale));
			l.put("SatGenrl", new REXPDouble(SatGenrl));
			l.put("SatPhys", new REXPDouble(SatPhys));
			l.put("SatPsych", new REXPDouble(SatPsych));
			l.put("SkjemaID", new REXPDouble(SkjemaID));
			l.put("ReshId", new REXPDouble(ReshId));			
			l.put("PID", new REXPString(PID));
			l.put("DagerRehab", new REXPDouble(DagerRehab));
			l.put("DagerTilRehab", new REXPDouble(DagerTilRehab));
			l.put("AlderAar", new REXPDouble(AlderAar));
			l.put("ShNavn", new REXPString(ShNavn));
			l.put("NevrNivaaInn", new REXPDouble(NevrNivaaInn));
			l.put("NevrNivaaUt", new REXPDouble(NevrNivaaUt));

			REXP df = REXP.createDataFrame(l);
			log.debug("Assigning data frame to R instance");
			rconn.assign("RegData", df);
			
			
			// Set up the tmp directory, file names and reportUserInfo
			String tmpdir = "";
			String p_filename = "";
			log.debug("Setting report image filepath and name");
			tmpdir = "/opt/jasper/img/";
			File dirFile = new File(tmpdir);
			String fileBaseName = "norScir_" + reportName + "-";
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
