/**
 * no.skde.report.nkr
 * DegenerativRyggCommonScriptlet.java Dec 19 2013 Are Edvardsen
 * 
 * 
 *  Copyleft 2013, 2014 SKDE
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
			
			// Deprecated. Should be removed
			try {
				Integer gender = (Integer) ((JRFillParameter) parametersMap.get("gender"))
						.getValue();
				if (gender == null) {
					gender = 0;
				}
				log.debug("Parameter 'gender' mapped to value: " + gender.toString());
				log.warn("Use of parameter 'gender' is deprecated. Please use 'erMann'");
				rconn.voidEval("kjonn=" + gender.toString());
			} catch (Exception e) {
				log.debug("Parameter 'gender' is not provided: " + e.getMessage());
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
			
			// Deprecated. Should be removed
			try {
				Integer myDept = (Integer) ((JRFillParameter) parametersMap.get("myDept"))
						.getValue();
				if (myDept == null) {
					myDept = 1;
				}
				log.debug("Parameter 'myDept' mapped to value: " + myDept.toString());
				log.warn("Use of parameter 'myDept' is deprecated. Please use 'orgUnitSelection'");
				rconn.voidEval("egenavd=" + myDept.toString());
			} catch (Exception e) {
				log.debug("Parameter 'mydept' is not provided: " + e.getMessage());
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
			
			// set path to library, to be removed since Rapporteket uses same directory for all R files (noweb, libs and report funs)
			String libkat = "'/opt/jasper/r/'";
			rconn.voidEval("libkat=" + libkat);
						
			log.debug("Getting Jasper Report data source...");
						
			
			// Load up primitive arrays with query data
			JRDataSource ds = (JRDataSource) ((JRFillParameter) parametersMap.get("REPORT_DATA_SOURCE")).getValue();
						
			log.debug("Getting Jasper Report Fields from report data source...");
			
			JRField PIDField = (JRField) fieldsMap.get("PID");
			JRField BMIField = (JRField) fieldsMap.get("BMI");
			JRField UtdField = (JRField) fieldsMap.get("Utd");
			JRField AlderField = (JRField) fieldsMap.get("Alder");
			JRField LiggedognField = (JRField) fieldsMap.get("Liggedogn");
			JRField DagkirurgiField = (JRField) fieldsMap.get("Dagkirurgi");
			JRField TidlOprField = (JRField) fieldsMap.get("TidlOpr");
			JRField PeropKompField = (JRField) fieldsMap.get("PeropKomp");
			JRField PeropKompAnafyField = (JRField) fieldsMap.get("PeropKompAnafy");
			JRField PeropKompAnnetField = (JRField) fieldsMap.get("PeropKompAnnet");
			JRField PeropKompDuraField = (JRField) fieldsMap.get("PeropKompDura");
			JRField PeropKompFeilnivSideField = (JRField) fieldsMap.get("PeropKompFeilnivSide");
			JRField PeropKompFeilplassImpField = (JRField) fieldsMap.get("PeropKompFeilplassImp");
			JRField PeropKompKardioField = (JRField) fieldsMap.get("PeropKompKardio");
			JRField PeropKompNerveField = (JRField) fieldsMap.get("PeropKompNerve");
			JRField PeropKompRespField = (JRField) fieldsMap.get("PeropKompResp");
			JRField PeropKompTransfuBlodningField = (JRField) fieldsMap.get("PeropKompTransfuBlodning");
			JRField KpBlod3MndField = (JRField) fieldsMap.get("KpBlod3Mnd");
			JRField KpDVT3MndField = (JRField) fieldsMap.get("KpDVT3Mnd");
			JRField KpInfDyp3MndField = (JRField) fieldsMap.get("KpInfDyp3Mnd");
			JRField KpInfOverfla3MndField = (JRField) fieldsMap.get("KpInfOverfla3Mnd");
			JRField KpLE3MndField = (JRField) fieldsMap.get("KpLE3Mnd");
			JRField KpLungebet3MndField = (JRField) fieldsMap.get("KpLungebet3Mnd");
			JRField KpMiktProb3MndField = (JRField) fieldsMap.get("KpMiktProb3Mnd");
			JRField KpSarinfUspesType3MndField = (JRField) fieldsMap.get("KpSarinfUspesType3Mnd");
			JRField KpUVI3MndField = (JRField) fieldsMap.get("KpUVI3Mnd");
			JRField Utfylt3MndField = (JRField) fieldsMap.get("Utfylt3Mnd");
			JRField AntibiotikaField = (JRField) fieldsMap.get("Antibiotika");
			JRField ASAField = (JRField) fieldsMap.get("ASA");
			JRField OpKatField = (JRField) fieldsMap.get("OpKat");
			JRField ArbstatusPreField = (JRField) fieldsMap.get("ArbstatusPre");
			JRField Arbstatus3mndField = (JRField) fieldsMap.get("Arbstatus3mnd");
			JRField Arbstatus12mndField = (JRField) fieldsMap.get("Arbstatus12mnd");
			JRField SmBePreField = (JRField) fieldsMap.get("SmBePre");
			JRField SmBe3mndField = (JRField) fieldsMap.get("SmBe3mnd");
			JRField SmBe12mndField = (JRField) fieldsMap.get("SmBe12mnd");
			JRField SmRyPreField = (JRField) fieldsMap.get("SmRyPre");
			JRField SmRy3mndField = (JRField) fieldsMap.get("SmRy3mnd");
			JRField SmRy12mndField = (JRField) fieldsMap.get("SmRy12mnd");
			JRField OswTotPreField = (JRField) fieldsMap.get("OswTotPre");
			JRField OswTot3mndField = (JRField) fieldsMap.get("OswTot3mnd");
			JRField OswTot12mndField = (JRField) fieldsMap.get("OswTot12mnd");
			JRField EQ5DPreField = (JRField) fieldsMap.get("EQ5DPre");
			JRField EQ5D3mndField = (JRField) fieldsMap.get("EQ5D3mnd");
			JRField EQ5D12mndField = (JRField) fieldsMap.get("EQ5D12mnd");
			JRField Nytte3mndField = (JRField) fieldsMap.get("Nytte3mnd");
			JRField Nytte12mndField = (JRField) fieldsMap.get("Nytte12mnd");
			JRField Fornoyd3mndField = (JRField) fieldsMap.get("Fornoyd3mnd");
			JRField Fornoyd12mndField = (JRField) fieldsMap.get("Fornoyd12mnd");
			JRField KjonnField = (JRField) fieldsMap.get("Kjonn");
			JRField HovedInngrepField = (JRField) fieldsMap.get("HovedInngrep");
			JRField HovedInngreptxtField = (JRField) fieldsMap.get("HovedInngreptxt");
			JRField InngrepField = (JRField) fieldsMap.get("Inngrep");
			JRField InngreptxtField = (JRField) fieldsMap.get("Inngreptxt");
			JRField AvdReshIDField = (JRField) fieldsMap.get("AvdReshID");
			JRField AvdNavnField = (JRField) fieldsMap.get("AvdNavn");
			JRField OpDatoField = (JRField) fieldsMap.get("OpDato");
			JRField AarField = (JRField) fieldsMap.get("Aar");

			
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

			Double[] sPID = new Double[100000];
			Double[] sBMI = new Double[100000];
			Double[] sUtd = new Double[100000];
			Double[] sAlder = new Double[100000];
			Double[] sLiggedogn = new Double[100000];
			Double[] sDagkirurgi = new Double[100000];
			Double[] sTidlOpr = new Double[100000];
			Double[] sPeropKomp = new Double[100000];
			Double[] sPeropKompAnafy = new Double[100000];
			String[] sPeropKompAnnet = new String[100000];
			Double[] sPeropKompDura = new Double[100000];
			Double[] sPeropKompFeilnivSide = new Double[100000];
			Double[] sPeropKompFeilplassImp = new Double[100000];
			Double[] sPeropKompKardio = new Double[100000];
			Double[] sPeropKompNerve = new Double[100000];
			Double[] sPeropKompResp = new Double[100000];
			Double[] sPeropKompTransfuBlodning = new Double[100000];
			Double[] sKpBlod3Mnd = new Double[100000];
			Double[] sKpDVT3Mnd = new Double[100000];
			Double[] sKpInfDyp3Mnd = new Double[100000];
			Double[] sKpInfOverfla3Mnd = new Double[100000];
			Double[] sKpLE3Mnd = new Double[100000];
			Double[] sKpLungebet3Mnd = new Double[100000];
			Double[] sKpMiktProb3Mnd = new Double[100000];
			Double[] sKpSarinfUspesType3Mnd = new Double[100000];
			Double[] sKpUVI3Mnd = new Double[100000];
			Double[] sUtfylt3Mnd = new Double[100000];
			Double[] sAntibiotika = new Double[100000];
			Double[] sASA = new Double[100000];
			Double[] sOpKat = new Double[100000];
			Double[] sArbstatusPre = new Double[100000];
			Double[] sArbstatus3mnd = new Double[100000];
			Double[] sArbstatus12mnd = new Double[100000];
			Double[] sSmBePre = new Double[100000];
			Double[] sSmBe3mnd = new Double[100000];
			Double[] sSmBe12mnd = new Double[100000];
			Double[] sSmRyPre = new Double[100000];
			Double[] sSmRy3mnd = new Double[100000];
			Double[] sSmRy12mnd = new Double[100000];
			Double[] sOswTotPre = new Double[100000];
			Double[] sOswTot3mnd = new Double[100000];
			Double[] sOswTot12mnd = new Double[100000];
			Double[] sEQ5DPre = new Double[100000];
			Double[] sEQ5D3mnd = new Double[100000];
			Double[] sEQ5D12mnd = new Double[100000];
			Double[] sNytte3mnd = new Double[100000];
			Double[] sNytte12mnd = new Double[100000];
			Double[] sFornoyd3mnd = new Double[100000];
			Double[] sFornoyd12mnd = new Double[100000];
			Double[] sKjonn = new Double[100000];
			Double[] sHovedInngrep = new Double[100000];
			String[] sHovedInngreptxt = new String[100000];
			Double[] sInngrep = new Double[100000];
			String[] sInngreptxt = new String[100000];
			String[] sAvdReshID = new String[100000];
			String[] sAvdNavn = new String[100000];
			String[] sOpDato = new String[100000];
			String[] sAar = new String[100000];

		
			
			log.debug("populating slug array with report data...");			
			
			int rowidx = 0;
			// Assume we get 1 row
			boolean getRow = true;
			while (getRow) {
				sPID[rowidx] = (Double) ds.getFieldValue(PIDField);
				sBMI[rowidx] = (Double) ds.getFieldValue(BMIField);
				sUtd[rowidx] = (Double) ds.getFieldValue(UtdField);
				sAlder[rowidx] = (Double) ds.getFieldValue(AlderField);
				sLiggedogn[rowidx] = (Double) ds.getFieldValue(LiggedognField);
				sDagkirurgi[rowidx] = (Double) ds.getFieldValue(DagkirurgiField);
				sTidlOpr[rowidx] = (Double) ds.getFieldValue(TidlOprField);
				sPeropKomp[rowidx] = (Double) ds.getFieldValue(PeropKompField);
				sPeropKompAnafy[rowidx] = (Double) ds.getFieldValue(PeropKompAnafyField);
				sPeropKompAnnet[rowidx] = (String) ds.getFieldValue(PeropKompAnnetField);
				sPeropKompDura[rowidx] = (Double) ds.getFieldValue(PeropKompDuraField);
				sPeropKompFeilnivSide[rowidx] = (Double) ds.getFieldValue(PeropKompFeilnivSideField);
				sPeropKompFeilplassImp[rowidx] = (Double) ds.getFieldValue(PeropKompFeilplassImpField);
				sPeropKompKardio[rowidx] = (Double) ds.getFieldValue(PeropKompKardioField);
				sPeropKompNerve[rowidx] = (Double) ds.getFieldValue(PeropKompNerveField);
				sPeropKompResp[rowidx] = (Double) ds.getFieldValue(PeropKompRespField);
				sPeropKompTransfuBlodning[rowidx] = (Double) ds.getFieldValue(PeropKompTransfuBlodningField);
				sKpBlod3Mnd[rowidx] = (Double) ds.getFieldValue(KpBlod3MndField);
				sKpDVT3Mnd[rowidx] = (Double) ds.getFieldValue(KpDVT3MndField);
				sKpInfDyp3Mnd[rowidx] = (Double) ds.getFieldValue(KpInfDyp3MndField);
				sKpInfOverfla3Mnd[rowidx] = (Double) ds.getFieldValue(KpInfOverfla3MndField);
				sKpLE3Mnd[rowidx] = (Double) ds.getFieldValue(KpLE3MndField);
				sKpLungebet3Mnd[rowidx] = (Double) ds.getFieldValue(KpLungebet3MndField);
				sKpMiktProb3Mnd[rowidx] = (Double) ds.getFieldValue(KpMiktProb3MndField);
				sKpSarinfUspesType3Mnd[rowidx] = (Double) ds.getFieldValue(KpSarinfUspesType3MndField);
				sKpUVI3Mnd[rowidx] = (Double) ds.getFieldValue(KpUVI3MndField);
				sUtfylt3Mnd[rowidx] = (Double) ds.getFieldValue(Utfylt3MndField);
				sAntibiotika[rowidx] = (Double) ds.getFieldValue(AntibiotikaField);
				sASA[rowidx] = (Double) ds.getFieldValue(ASAField);
				sOpKat[rowidx] = (Double) ds.getFieldValue(OpKatField);
				sArbstatusPre[rowidx] = (Double) ds.getFieldValue(ArbstatusPreField);
				sArbstatus3mnd[rowidx] = (Double) ds.getFieldValue(Arbstatus3mndField);
				sArbstatus12mnd[rowidx] = (Double) ds.getFieldValue(Arbstatus12mndField);
				sSmBePre[rowidx] = (Double) ds.getFieldValue(SmBePreField);
				sSmBe3mnd[rowidx] = (Double) ds.getFieldValue(SmBe3mndField);
				sSmBe12mnd[rowidx] = (Double) ds.getFieldValue(SmBe12mndField);
				sSmRyPre[rowidx] = (Double) ds.getFieldValue(SmRyPreField);
				sSmRy3mnd[rowidx] = (Double) ds.getFieldValue(SmRy3mndField);
				sSmRy12mnd[rowidx] = (Double) ds.getFieldValue(SmRy12mndField);
				sOswTotPre[rowidx] = (Double) ds.getFieldValue(OswTotPreField);
				sOswTot3mnd[rowidx] = (Double) ds.getFieldValue(OswTot3mndField);
				sOswTot12mnd[rowidx] = (Double) ds.getFieldValue(OswTot12mndField);
				sEQ5DPre[rowidx] = (Double) ds.getFieldValue(EQ5DPreField);
				sEQ5D3mnd[rowidx] = (Double) ds.getFieldValue(EQ5D3mndField);
				sEQ5D12mnd[rowidx] = (Double) ds.getFieldValue(EQ5D12mndField);
				sNytte3mnd[rowidx] = (Double) ds.getFieldValue(Nytte3mndField);
				sNytte12mnd[rowidx] = (Double) ds.getFieldValue(Nytte12mndField);
				sFornoyd3mnd[rowidx] = (Double) ds.getFieldValue(Fornoyd3mndField);
				sFornoyd12mnd[rowidx] = (Double) ds.getFieldValue(Fornoyd12mndField);
				sKjonn[rowidx] = (Double) ds.getFieldValue(KjonnField);
				sHovedInngrep[rowidx] = (Double) ds.getFieldValue(HovedInngrepField);
				sHovedInngreptxt[rowidx] = (String) ds.getFieldValue(HovedInngreptxtField);
				sInngrep[rowidx] = (Double) ds.getFieldValue(InngrepField);
				sInngreptxt[rowidx] = (String) ds.getFieldValue(InngreptxtField);
				sAvdReshID[rowidx] = (String) ds.getFieldValue(AvdReshIDField);
				sAvdNavn[rowidx] = (String) ds.getFieldValue(AvdNavnField);
				sOpDato[rowidx] = (String) ds.getFieldValue(OpDatoField);
				sAar[rowidx] = (String) ds.getFieldValue(AarField);
				getRow = ds.next();
				rowidx++;
			}
			rowidx--;

			
			log.debug("Slug array filled with " + rowidx + " records from report data");
			
			
			log.debug("Creating proper sized array...");

			double[] PID = new double[rowidx + 1];
			double[] BMI = new double[rowidx + 1];
			double[] Utd = new double[rowidx + 1];
			double[] Alder = new double[rowidx + 1];
			double[] Liggedogn = new double[rowidx + 1];
			double[] Dagkirurgi = new double[rowidx + 1];
			double[] TidlOpr = new double[rowidx + 1];
			double[] PeropKomp = new double[rowidx + 1];
			double[] PeropKompAnafy = new double[rowidx + 1];
			String[] PeropKompAnnet = new String[rowidx + 1];
			double[] PeropKompDura = new double[rowidx + 1];
			double[] PeropKompFeilnivSide = new double[rowidx + 1];
			double[] PeropKompFeilplassImp = new double[rowidx + 1];
			double[] PeropKompKardio = new double[rowidx + 1];
			double[] PeropKompNerve = new double[rowidx + 1];
			double[] PeropKompResp = new double[rowidx + 1];
			double[] PeropKompTransfuBlodning = new double[rowidx + 1];
			double[] KpBlod3Mnd = new double[rowidx + 1];
			double[] KpDVT3Mnd = new double[rowidx + 1];
			double[] KpInfDyp3Mnd = new double[rowidx + 1];
			double[] KpInfOverfla3Mnd = new double[rowidx + 1];
			double[] KpLE3Mnd = new double[rowidx + 1];
			double[] KpLungebet3Mnd = new double[rowidx + 1];
			double[] KpMiktProb3Mnd = new double[rowidx + 1];
			double[] KpSarinfUspesType3Mnd = new double[rowidx + 1];
			double[] KpUVI3Mnd = new double[rowidx + 1];
			double[] Utfylt3Mnd = new double[rowidx + 1];
			double[] Antibiotika = new double[rowidx + 1];
			double[] ASA = new double[rowidx + 1];
			double[] OpKat = new double[rowidx + 1];
			double[] ArbstatusPre = new double[rowidx + 1];
			double[] Arbstatus3mnd = new double[rowidx + 1];
			double[] Arbstatus12mnd = new double[rowidx + 1];
			double[] SmBePre = new double[rowidx + 1];
			double[] SmBe3mnd = new double[rowidx + 1];
			double[] SmBe12mnd = new double[rowidx + 1];
			double[] SmRyPre = new double[rowidx + 1];
			double[] SmRy3mnd = new double[rowidx + 1];
			double[] SmRy12mnd = new double[rowidx + 1];
			double[] OswTotPre = new double[rowidx + 1];
			double[] OswTot3mnd = new double[rowidx + 1];
			double[] OswTot12mnd = new double[rowidx + 1];
			double[] EQ5DPre = new double[rowidx + 1];
			double[] EQ5D3mnd = new double[rowidx + 1];
			double[] EQ5D12mnd = new double[rowidx + 1];
			double[] Nytte3mnd = new double[rowidx + 1];
			double[] Nytte12mnd = new double[rowidx + 1];
			double[] Fornoyd3mnd = new double[rowidx + 1];
			double[] Fornoyd12mnd = new double[rowidx + 1];
			double[] Kjonn = new double[rowidx + 1];
			double[] HovedInngrep = new double[rowidx + 1];
			String[] HovedInngreptxt = new String[rowidx + 1];
			double[] Inngrep = new double[rowidx + 1];
			String[] Inngreptxt = new String[rowidx + 1];
			String[] AvdReshID = new String[rowidx + 1];
			String[] AvdNavn = new String[rowidx + 1];
			String[] OpDato = new String[rowidx + 1];
			String[] Aar = new String[rowidx + 1];


			
			
			// ifs are needed because underlying query returns null. Since ints
			// cannot be null, these are returned as type double by the query
			log.debug("Populating proper sized array with data from slug array, also checking for NULLs...");
			int i = 0;
			while (i <= rowidx) {
				if (sPID[i] == null) {
					PID[i] = java.lang.Double.NaN;
				}
				else {
					PID[i] = sPID[i];
				}
				if (sBMI[i] == null) {
					BMI[i] = java.lang.Double.NaN;
				}
				else {
					BMI[i] = sBMI[i];
				}

				if (sUtd[i] == null) {
					Utd[i] = java.lang.Double.NaN;
				}
				else {
					Utd[i] = sUtd[i];
				}

				if (sAlder[i] == null) {
					Alder[i] = java.lang.Double.NaN;
				}
				else {
					Alder[i] = sAlder[i];
				}

				if (sLiggedogn[i] == null) {
					Liggedogn[i] = java.lang.Double.NaN;
				}
				else {
					Liggedogn[i] = sLiggedogn[i];
				}

				if (sDagkirurgi[i] == null) {
					Dagkirurgi[i] = java.lang.Double.NaN;
				}
				else {
					Dagkirurgi[i] = sDagkirurgi[i];
				}

				if (sTidlOpr[i] == null) {
					TidlOpr[i] = java.lang.Double.NaN;
				}
				else {
					TidlOpr[i] = sTidlOpr[i];
				}

				if (sPeropKomp[i] == null) {
					PeropKomp[i] = java.lang.Double.NaN;
				}
				else {
					PeropKomp[i] = sPeropKomp[i];
				}

				if (sPeropKompAnafy[i] == null) {
					PeropKompAnafy[i] = java.lang.Double.NaN;
				}
				else {
					PeropKompAnafy[i] = sPeropKompAnafy[i];
				}

				PeropKompAnnet[i] = sPeropKompAnnet[i];
				if (sPeropKompDura[i] == null) {
					PeropKompDura[i] = java.lang.Double.NaN;
				}
				else {
					PeropKompDura[i] = sPeropKompDura[i];
				}

				if (sPeropKompFeilnivSide[i] == null) {
					PeropKompFeilnivSide[i] = java.lang.Double.NaN;
				}
				else {
					PeropKompFeilnivSide[i] = sPeropKompFeilnivSide[i];
				}

				if (sPeropKompFeilplassImp[i] == null) {
					PeropKompFeilplassImp[i] = java.lang.Double.NaN;
				}
				else {
					PeropKompFeilplassImp[i] = sPeropKompFeilplassImp[i];
				}

				if (sPeropKompKardio[i] == null) {
					PeropKompKardio[i] = java.lang.Double.NaN;
				}
				else {
					PeropKompKardio[i] = sPeropKompKardio[i];
				}

				if (sPeropKompNerve[i] == null) {
					PeropKompNerve[i] = java.lang.Double.NaN;
				}
				else {
					PeropKompNerve[i] = sPeropKompNerve[i];
				}

				if (sPeropKompResp[i] == null) {
					PeropKompResp[i] = java.lang.Double.NaN;
				}
				else {
					PeropKompResp[i] = sPeropKompResp[i];
				}

				if (sPeropKompTransfuBlodning[i] == null) {
					PeropKompTransfuBlodning[i] = java.lang.Double.NaN;
				}
				else {
					PeropKompTransfuBlodning[i] = sPeropKompTransfuBlodning[i];
				}

				if (sKpBlod3Mnd[i] == null) {
					KpBlod3Mnd[i] = java.lang.Double.NaN;
				}
				else {
					KpBlod3Mnd[i] = sKpBlod3Mnd[i];
				}

				if (sKpDVT3Mnd[i] == null) {
					KpDVT3Mnd[i] = java.lang.Double.NaN;
				}
				else {
					KpDVT3Mnd[i] = sKpDVT3Mnd[i];
				}

				if (sKpInfDyp3Mnd[i] == null) {
					KpInfDyp3Mnd[i] = java.lang.Double.NaN;
				}
				else {
					KpInfDyp3Mnd[i] = sKpInfDyp3Mnd[i];
				}

				if (sKpInfOverfla3Mnd[i] == null) {
					KpInfOverfla3Mnd[i] = java.lang.Double.NaN;
				}
				else {
					KpInfOverfla3Mnd[i] = sKpInfOverfla3Mnd[i];
				}

				if (sKpLE3Mnd[i] == null) {
					KpLE3Mnd[i] = java.lang.Double.NaN;
				}
				else {
					KpLE3Mnd[i] = sKpLE3Mnd[i];
				}

				if (sKpLungebet3Mnd[i] == null) {
					KpLungebet3Mnd[i] = java.lang.Double.NaN;
				}
				else {
					KpLungebet3Mnd[i] = sKpLungebet3Mnd[i];
				}

				if (sKpMiktProb3Mnd[i] == null) {
					KpMiktProb3Mnd[i] = java.lang.Double.NaN;
				}
				else {
					KpMiktProb3Mnd[i] = sKpMiktProb3Mnd[i];
				}

				if (sKpSarinfUspesType3Mnd[i] == null) {
					KpSarinfUspesType3Mnd[i] = java.lang.Double.NaN;
				}
				else {
					KpSarinfUspesType3Mnd[i] = sKpSarinfUspesType3Mnd[i];
				}

				if (sKpUVI3Mnd[i] == null) {
					KpUVI3Mnd[i] = java.lang.Double.NaN;
				}
				else {
					KpUVI3Mnd[i] = sKpUVI3Mnd[i];
				}

				if (sUtfylt3Mnd[i] == null) {
					Utfylt3Mnd[i] = java.lang.Double.NaN;
				}
				else {
					Utfylt3Mnd[i] = sUtfylt3Mnd[i];
				}

				if (sAntibiotika[i] == null) {
					Antibiotika[i] = java.lang.Double.NaN;
				}
				else {
					Antibiotika[i] = sAntibiotika[i];
				}

				if (sASA[i] == null) {
					ASA[i] = java.lang.Double.NaN;
				}
				else {
					ASA[i] = sASA[i];
				}

				if (sOpKat[i] == null) {
					OpKat[i] = java.lang.Double.NaN;
				}
				else {
					OpKat[i] = sOpKat[i];
				}

				if (sArbstatusPre[i] == null) {
					ArbstatusPre[i] = java.lang.Double.NaN;
				}
				else {
					ArbstatusPre[i] = sArbstatusPre[i];
				}

				if (sArbstatus3mnd[i] == null) {
					Arbstatus3mnd[i] = java.lang.Double.NaN;
				}
				else {
					Arbstatus3mnd[i] = sArbstatus3mnd[i];
				}

				if (sArbstatus12mnd[i] == null) {
					Arbstatus12mnd[i] = java.lang.Double.NaN;
				}
				else {
					Arbstatus12mnd[i] = sArbstatus12mnd[i];
				}

				if (sSmBePre[i] == null) {
					SmBePre[i] = java.lang.Double.NaN;
				}
				else {
					SmBePre[i] = sSmBePre[i];
				}

				if (sSmBe3mnd[i] == null) {
					SmBe3mnd[i] = java.lang.Double.NaN;
				}
				else {
					SmBe3mnd[i] = sSmBe3mnd[i];
				}

				if (sSmBe12mnd[i] == null) {
					SmBe12mnd[i] = java.lang.Double.NaN;
				}
				else {
					SmBe12mnd[i] = sSmBe12mnd[i];
				}

				if (sSmRyPre[i] == null) {
					SmRyPre[i] = java.lang.Double.NaN;
				}
				else {
					SmRyPre[i] = sSmRyPre[i];
				}

				if (sSmRy3mnd[i] == null) {
					SmRy3mnd[i] = java.lang.Double.NaN;
				}
				else {
					SmRy3mnd[i] = sSmRy3mnd[i];
				}

				if (sSmRy12mnd[i] == null) {
					SmRy12mnd[i] = java.lang.Double.NaN;
				}
				else {
					SmRy12mnd[i] = sSmRy12mnd[i];
				}

				if (sOswTotPre[i] == null) {
					OswTotPre[i] = java.lang.Double.NaN;
				}
				else {
					OswTotPre[i] = sOswTotPre[i];
				}

				if (sOswTot3mnd[i] == null) {
					OswTot3mnd[i] = java.lang.Double.NaN;
				}
				else {
					OswTot3mnd[i] = sOswTot3mnd[i];
				}

				if (sOswTot12mnd[i] == null) {
					OswTot12mnd[i] = java.lang.Double.NaN;
				}
				else {
					OswTot12mnd[i] = sOswTot12mnd[i];
				}

				if (sEQ5DPre[i] == null) {
					EQ5DPre[i] = java.lang.Double.NaN;
				}
				else {
					EQ5DPre[i] = sEQ5DPre[i];
				}

				if (sEQ5D3mnd[i] == null) {
					EQ5D3mnd[i] = java.lang.Double.NaN;
				}
				else {
					EQ5D3mnd[i] = sEQ5D3mnd[i];
				}

				if (sEQ5D12mnd[i] == null) {
					EQ5D12mnd[i] = java.lang.Double.NaN;
				}
				else {
					EQ5D12mnd[i] = sEQ5D12mnd[i];
				}

				if (sNytte3mnd[i] == null) {
					Nytte3mnd[i] = java.lang.Double.NaN;
				}
				else {
					Nytte3mnd[i] = sNytte3mnd[i];
				}

				if (sNytte12mnd[i] == null) {
					Nytte12mnd[i] = java.lang.Double.NaN;
				}
				else {
					Nytte12mnd[i] = sNytte12mnd[i];
				}

				if (sFornoyd3mnd[i] == null) {
					Fornoyd3mnd[i] = java.lang.Double.NaN;
				}
				else {
					Fornoyd3mnd[i] = sFornoyd3mnd[i];
				}

				if (sFornoyd12mnd[i] == null) {
					Fornoyd12mnd[i] = java.lang.Double.NaN;
				}
				else {
					Fornoyd12mnd[i] = sFornoyd12mnd[i];
				}

				if (sKjonn[i] == null) {
					Kjonn[i] = java.lang.Double.NaN;
				}
				else {
					Kjonn[i] = sKjonn[i];
				}
				
				if (sHovedInngrep[i] == null) {
					HovedInngrep[i] = java.lang.Double.NaN;
				}
				else {
					HovedInngrep[i] = sHovedInngrep[i];
				}
				
				HovedInngreptxt[i] = sHovedInngreptxt[i];
				
				if (sInngrep[i] == null) {
					Inngrep[i] = java.lang.Double.NaN;
				}
				else {
					Inngrep[i] = sInngrep[i];
				}
				
				Inngreptxt[i] = sInngreptxt[i];
				AvdReshID[i] = sAvdReshID[i];
				AvdNavn[i] = sAvdNavn[i];
				OpDato[i] = sOpDato[i];
				Aar[i] = sAar[i];
				i++;
			}
			
			
			log.debug("Creating the R dataframe...");
			RList l = new RList();
			l.put("PID", new REXPDouble(PID));
			l.put("BMI", new REXPDouble(BMI));
			l.put("Utd", new REXPDouble(Utd));
			l.put("Alder", new REXPDouble(Alder));
			l.put("Liggedogn", new REXPDouble(Liggedogn));
			l.put("Dagkirurgi", new REXPDouble(Dagkirurgi));
			l.put("TidlOpr", new REXPDouble(TidlOpr));
			l.put("PeropKomp", new REXPDouble(PeropKomp));
			l.put("PeropKompAnafy", new REXPDouble(PeropKompAnafy));
			l.put("PeropKompAnnet", new REXPString(PeropKompAnnet));
			l.put("PeropKompDura", new REXPDouble(PeropKompDura));
			l.put("PeropKompFeilnivSide", new REXPDouble(PeropKompFeilnivSide));
			l.put("PeropKompFeilplassImp", new REXPDouble(PeropKompFeilplassImp));
			l.put("PeropKompKardio", new REXPDouble(PeropKompKardio));
			l.put("PeropKompNerve", new REXPDouble(PeropKompNerve));
			l.put("PeropKompResp", new REXPDouble(PeropKompResp));
			l.put("PeropKompTransfuBlodning", new REXPDouble(PeropKompTransfuBlodning));
			l.put("KpBlod3Mnd", new REXPDouble(KpBlod3Mnd));
			l.put("KpDVT3Mnd", new REXPDouble(KpDVT3Mnd));
			l.put("KpInfDyp3Mnd", new REXPDouble(KpInfDyp3Mnd));
			l.put("KpInfOverfla3Mnd", new REXPDouble(KpInfOverfla3Mnd));
			l.put("KpLE3Mnd", new REXPDouble(KpLE3Mnd));
			l.put("KpLungebet3Mnd", new REXPDouble(KpLungebet3Mnd));
			l.put("KpMiktProb3Mnd", new REXPDouble(KpMiktProb3Mnd));
			l.put("KpSarinfUspesType3Mnd", new REXPDouble(KpSarinfUspesType3Mnd));
			l.put("KpUVI3Mnd", new REXPDouble(KpUVI3Mnd));
			l.put("Utfylt3Mnd", new REXPDouble(Utfylt3Mnd));
			l.put("Antibiotika", new REXPDouble(Antibiotika));
			l.put("ASA", new REXPDouble(ASA));
			l.put("OpKat", new REXPDouble(OpKat));
			l.put("ArbstatusPre", new REXPDouble(ArbstatusPre));
			l.put("Arbstatus3mnd", new REXPDouble(Arbstatus3mnd));
			l.put("Arbstatus12mnd", new REXPDouble(Arbstatus12mnd));
			l.put("SmBePre", new REXPDouble(SmBePre));
			l.put("SmBe3mnd", new REXPDouble(SmBe3mnd));
			l.put("SmBe12mnd", new REXPDouble(SmBe12mnd));
			l.put("SmRyPre", new REXPDouble(SmRyPre));
			l.put("SmRy3mnd", new REXPDouble(SmRy3mnd));
			l.put("SmRy12mnd", new REXPDouble(SmRy12mnd));
			l.put("OswTotPre", new REXPDouble(OswTotPre));
			l.put("OswTot3mnd", new REXPDouble(OswTot3mnd));
			l.put("OswTot12mnd", new REXPDouble(OswTot12mnd));
			l.put("EQ5DPre", new REXPDouble(EQ5DPre));
			l.put("EQ5D3mnd", new REXPDouble(EQ5D3mnd));
			l.put("EQ5D12mnd", new REXPDouble(EQ5D12mnd));
			l.put("Nytte3mnd", new REXPDouble(Nytte3mnd));
			l.put("Nytte12mnd", new REXPDouble(Nytte12mnd));
			l.put("Fornoyd3mnd", new REXPDouble(Fornoyd3mnd));
			l.put("Fornoyd12mnd", new REXPDouble(Fornoyd12mnd));
			l.put("Kjonn", new REXPDouble(Kjonn));
			l.put("HovedInngrep", new REXPDouble(HovedInngrep));
			l.put("HovedInngreptxt", new REXPString(HovedInngreptxt));
			l.put("Inngrep", new REXPDouble(Inngrep));
			l.put("Inngreptxt", new REXPString(Inngreptxt));
			l.put("AvdReshID", new REXPString(AvdReshID));
			l.put("AvdNavn", new REXPString(AvdNavn));
			l.put("OpDato", new REXPString(OpDato));
			l.put("Aar", new REXPString(Aar));
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