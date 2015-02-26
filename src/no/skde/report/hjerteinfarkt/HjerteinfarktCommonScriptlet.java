/**
 * no.skde.report.hjerneslag
 * HjerteinfarktCommonScriptlet.java Jun 11 2014 Are Edvardsen
 * 
 * 
 *  Copyleft 2014, SKDE
 */

package no.skde.report.hjerteinfarkt;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.fill.*;
import org.apache.log4j.Logger;
import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.*;

public class HjerteinfarktCommonScriptlet extends JRDefaultScriptlet {

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
			log.info("Start generating R report using " + HjerteinfarktCommonScriptlet.class.getName());
			
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
					beginDate = new SimpleDateFormat("yyyy-MM-dd").parse("2012-01-01");
				}
				StringBuilder startDateString = new StringBuilder(rFormat.format(beginDate));
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
			
			String diagnose;
			try {
				diagnose = (String) ((JRFillParameter) parametersMap.get("diagnose")).getValue();
				if (diagnose == "") {
					diagnose = "99";
				}
				rconn.voidEval("diagnose=" + diagnose);
			} catch (Exception e) {
				log.debug("Parameter diagnose is not defined: " + e.getMessage());
			}
			
//			String innl4t;
//			try {
//				innl4t = (String) ((JRFillParameter) parametersMap.get("innl4t")).getValue();
//				if (innl4t == "") {
//					innl4t = "9";
//				}
//				rconn.voidEval("innl4t=" + innl4t);
//			} catch (Exception e) {
//				log.debug("Parameter innl4t is not defined: " + e.getMessage());
//			}
//			
//			String NIHSSinn;
//			try {
//				NIHSSinn = (String) ((JRFillParameter) parametersMap.get("NIHSSinn")).getValue();
//				if (NIHSSinn == "") {
//					NIHSSinn = "99";
//				}
//				rconn.voidEval("NIHSSinn=" + NIHSSinn);
//			} catch (Exception e) {
//				log.debug("Parameter NIHSSinn is not defined: " + e.getMessage());
//			}
			
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
			
			Integer tidlInf;
			try {
				tidlInf = (Integer)	((JRFillParameter) parametersMap.get("tidlInf")).getValue();
				if (tidlInf == null) {
					tidlInf = 99;
				}
				log.debug("Parameter 'tidlInf' set to: " + tidlInf.toString());
				rconn.voidEval("tidlInf=" + tidlInf.toString());
			} catch (Exception e) {
				log.debug("Parameter tidlInf is not defined: " + e.getMessage());
			}
			
			String typeInf;
			try {
				typeInf = (String) ((JRFillParameter) parametersMap.get("typeInf")).getValue();
				if (typeInf == "") {
					typeInf = "STEMI";
				}
				rconn.voidEval("typeInf=" + typeInf);
			} catch (Exception e) {
				log.debug("Parameter typeInf is not defined: " + e.getMessage());
			}
			
			Integer overfl;
			try {
				overfl = (Integer) ((JRFillParameter) parametersMap.get("overfl")).getValue();
				if (overfl == null) {
					overfl = 99;
				}
				rconn.voidEval("overfl=" + overfl.toString());
			} catch (Exception e) {
				log.debug("Parameter overfl is not defined: " + e.getMessage());
			}
			
			
			// set path to library, to be removed since Rapporteket uses same directory for all R files (noweb, libs and report funs)
			String libkat = "'/opt/jasper/r/'";
			rconn.voidEval("libkat=" + libkat);
			
			log.debug("Getting Jasper Report data source...");
			
			// Load up primitive arrays with query data
			JRDataSource ds = (JRDataSource) ((JRFillParameter) parametersMap
					.get("REPORT_DATA_SOURCE")).getValue();
			
			log.debug("Getting Jasper Report Fields from report data source...");
			
			JRField AlderField = (JRField) fieldsMap.get("Alder");
			JRField AvdelingField = (JRField) fieldsMap.get("Avdeling");
			JRField AvdelingNumField = (JRField) fieldsMap.get("AvdelingNum");
			JRField AvdNavnField = (JRField) fieldsMap.get("AvdNavn");
			JRField BMIField = (JRField) fieldsMap.get("BMI");
			JRField BMIHoydeField = (JRField) fieldsMap.get("BMIHoyde");
			JRField BMIUkjentField = (JRField) fieldsMap.get("BMIUkjent");
			JRField BMIVektField = (JRField) fieldsMap.get("BMIVekt");
			JRField BTPOvertrykkField = (JRField) fieldsMap.get("BTPOvertrykk");
			JRField BTPPulsField = (JRField) fieldsMap.get("BTPPuls");
			JRField BTPUkjentField = (JRField) fieldsMap.get("BTPUkjent");
			JRField BTPUndertrykkField = (JRField) fieldsMap.get("BTPUndertrykk");
			JRField DoedUnderOppholdField = (JRField) fieldsMap.get("DoedUnderOpphold");
			JRField DoedUnderOppholdNumField = (JRField) fieldsMap.get("DoedUnderOppholdNum");
			JRField EKGDiagnostiskField = (JRField) fieldsMap.get("EKGDiagnostisk");
			JRField EKGDiagnostiskNumField = (JRField) fieldsMap.get("EKGDiagnostiskNum");
			JRField EKGNyQbolgeField = (JRField) fieldsMap.get("EKGNyQbolge");
			JRField EKGNyQbolgeNumField = (JRField) fieldsMap.get("EKGNyQbolgeNum");
			JRField EKGRytmeField = (JRField) fieldsMap.get("EKGRytme");
			JRField EKGRytmeNumField = (JRField) fieldsMap.get("EKGRytmeNum");
			JRField EKGSteminStemiField = (JRField) fieldsMap.get("EKGSteminStemi");
			JRField EKGSteminStemiNumField = (JRField) fieldsMap.get("EKGSteminStemiNum");
			JRField EkkoEFField = (JRField) fieldsMap.get("EkkoEF");
			JRField EkkoEfNumField = (JRField) fieldsMap.get("EkkoEfNum");
			JRField EkkoValgField = (JRField) fieldsMap.get("EkkoValg");
			JRField EkkoValgNumField = (JRField) fieldsMap.get("EkkoValgNum");
			JRField PasientIDField = (JRField) fieldsMap.get("PasientID");
			JRField FoedselsaarField = (JRField) fieldsMap.get("Foedselsaar");
			JRField FraHvilkenRegionField = (JRField) fieldsMap.get("FraHvilkenRegion");
			JRField FraHvilkenRegionNumField = (JRField) fieldsMap.get("FraHvilkenRegionNum");
			JRField FraHvilketSykehus2Field = (JRField) fieldsMap.get("FraHvilketSykehus2");
			JRField HvorBefantPasientenSegField = (JRField) fieldsMap.get("HvorBefantPasientenSeg");
			JRField HvorBefantPasientenSegNumField = (JRField) fieldsMap.get("HvorBefantPasientenSegNum");
			JRField InfarktlokalisasjonField = (JRField) fieldsMap.get("Infarktlokalisasjon");
			JRField InfarktlokalisasjonNumField = (JRField) fieldsMap.get("InfarktlokalisasjonNum");
			JRField InnleggelsestidspunktField = (JRField) fieldsMap.get("Innleggelsestidspunkt");
			JRField Innleggelsestidspunkt2Field = (JRField) fieldsMap.get("Innleggelsestidspunkt2");
			JRField InvAntSykekarField = (JRField) fieldsMap.get("InvAntSykekar");
			JRField InvAntSykekarTekstField = (JRField) fieldsMap.get("InvAntSykekarTekst");
			JRField InvKunKorAngioField = (JRField) fieldsMap.get("InvKunKorAngio");
			JRField InvKunKorAngioNumField = (JRField) fieldsMap.get("InvKunKorAngioNum");
			JRField InvKunKorAngioPCITidField = (JRField) fieldsMap.get("InvKunKorAngioPCITid");
			JRField InvKunKorAngioTidField = (JRField) fieldsMap.get("InvKunKorAngioTid");
			JRField InvPCIunderoppholdetField = (JRField) fieldsMap.get("InvPCIunderoppholdet");
			JRField InvPCIunderoppholdetNumField = (JRField) fieldsMap.get("InvPCIunderoppholdetNum");
			JRField InvStentField = (JRField) fieldsMap.get("InvStent");
			JRField InvStentTekstField = (JRField) fieldsMap.get("InvStentTekst");
			JRField InvStentTypeField = (JRField) fieldsMap.get("InvStentType");
			JRField InvStentTypeTekstField = (JRField) fieldsMap.get("InvStentTypeTekst");
			JRField IvnPCITidField = (JRField) fieldsMap.get("IvnPCITid");
			JRField KjoennField = (JRField) fieldsMap.get("Kjoenn");
			JRField KommunenrField = (JRField) fieldsMap.get("Kommunenr");
			JRField KomplAtrieflimmerFlutterField = (JRField) fieldsMap.get("KomplAtrieflimmerFlutter");
			JRField KomplAtrieflimmerFlutterNumField = (JRField) fieldsMap.get("KomplAtrieflimmerFlutterNum");
			JRField KomplAVblokk2el3Field = (JRField) fieldsMap.get("KomplAVblokk2el3");
			JRField KomplAVblokk2el3NumField = (JRField) fieldsMap.get("KomplAVblokk2el3Num");
			JRField KomplBloedAnnenField = (JRField) fieldsMap.get("KomplBloedAnnen");
			JRField KomplBloedAnnenNumField = (JRField) fieldsMap.get("KomplBloedAnnenNum");
			JRField KomplBloedCerebralField = (JRField) fieldsMap.get("KomplBloedCerebral");
			JRField KomplBloedCerebralNumField = (JRField) fieldsMap.get("KomplBloedCerebralNum");
			JRField KomplBloedGIField = (JRField) fieldsMap.get("KomplBloedGI");
			JRField KomplBloedGINumField = (JRField) fieldsMap.get("KomplBloedGINum");
			JRField KomplBloedInnstikkField = (JRField) fieldsMap.get("KomplBloedInnstikk");
			JRField KomplBloedInnstikkNumField = (JRField) fieldsMap.get("KomplBloedInnstikkNum");
			JRField KomplBloedningField = (JRField) fieldsMap.get("KomplBloedning");
			JRField KomplBloedningNumField = (JRField) fieldsMap.get("KomplBloedningNum");
			JRField KomplHjerneslagField = (JRField) fieldsMap.get("KomplHjerneslag");
			JRField KomplHjerneslagNumField = (JRField) fieldsMap.get("KomplHjerneslagNum");
			JRField KomplHjertesviktField = (JRField) fieldsMap.get("KomplHjertesvikt");
			JRField KomplHjertesviktNumField = (JRField) fieldsMap.get("KomplHjertesviktNum");
			JRField KomplIngenField = (JRField) fieldsMap.get("KomplIngen");
			JRField KomplKardiogentSjokkField = (JRField) fieldsMap.get("KomplKardiogentSjokk");
			JRField KomplKardiogentSjokkNumField = (JRField) fieldsMap.get("KomplKardiogentSjokkNum");
			JRField KomplMekaniskKomplikasjonField = (JRField) fieldsMap.get("KomplMekaniskKomplikasjon");
			JRField KomplMekaniskKomplikasjonNumField = (JRField) fieldsMap.get("KomplMekaniskKomplikasjonNum");
			JRField KomplReinfarktField = (JRField) fieldsMap.get("KomplReinfarkt");
			JRField KomplReinfarktNumField = (JRField) fieldsMap.get("KomplReinfarktNum");
			JRField KomplVTVFe48NumField = (JRField) fieldsMap.get("KomplVTVFe48Num");
			JRField KomplVTVFe48tField = (JRField) fieldsMap.get("KomplVTVFe48t");
			JRField KomplVTVFf48tField = (JRField) fieldsMap.get("KomplVTVFf48t");
			JRField KomplVTVFf48tNumField = (JRField) fieldsMap.get("KomplVTVFf48tNum");
			JRField KoronarProsedyreEtPlanField = (JRField) fieldsMap.get("KoronarProsedyreEtPlan");
			JRField KoronarProsedyreEtPlanNumField = (JRField) fieldsMap.get("KoronarProsedyreEtPlanNum");
			JRField LabGlukoseField = (JRField) fieldsMap.get("LabGlukose");
			JRField LabHbA1CField = (JRField) fieldsMap.get("LabHbA1C");
			JRField LabHDLField = (JRField) fieldsMap.get("LabHDL");
			JRField LabInfarktmarkoerField = (JRField) fieldsMap.get("LabInfarktmarkoer");
			JRField LabInfarktmarkoererNumField = (JRField) fieldsMap.get("LabInfarktmarkoererNum");
			JRField LabInfarktmarkorHoyField = (JRField) fieldsMap.get("LabInfarktmarkorHoy");
			JRField LabInfarktmarkorLavField = (JRField) fieldsMap.get("LabInfarktmarkorLav");
			JRField LabKreatininField = (JRField) fieldsMap.get("LabKreatinin");
			JRField LabLDLField = (JRField) fieldsMap.get("LabLDL");
			JRField LabTotalkolesterolField = (JRField) fieldsMap.get("LabTotalkolesterol");
			JRField LabTriglyseriderField = (JRField) fieldsMap.get("LabTriglyserider");
			JRField LiggetidField = (JRField) fieldsMap.get("Liggetid");
			JRField LokalRegionField = (JRField) fieldsMap.get("LokalRegion");
			JRField LokalRegion2Field = (JRField) fieldsMap.get("LokalRegion2");
			JRField LokalRegionNumField = (JRField) fieldsMap.get("LokalRegionNum");
			JRField MedInnACEhemmerAIIField = (JRField) fieldsMap.get("MedInnACEhemmerAII");
			JRField MedInnACEhemmerAIINumField = (JRField) fieldsMap.get("MedInnACEhemmerAIINum");
			JRField MedInnADPField = (JRField) fieldsMap.get("MedInnADP");
			JRField MedInnADPNumField = (JRField) fieldsMap.get("MedInnADPNum");
			JRField MedInnAnnenPlatehemmingField = (JRField) fieldsMap.get("MedInnAnnenPlatehemming");
			JRField MedInnAnnenPlatehemmingNumField = (JRField) fieldsMap.get("MedInnAnnenPlatehemmingNum");
			JRField MedInnAntikoagulasjonField = (JRField) fieldsMap.get("MedInnAntikoagulasjon");
			JRField MedInnAntikoagulasjonNumField = (JRField) fieldsMap.get("MedInnAntikoagulasjonNum");
			JRField MedInnASAField = (JRField) fieldsMap.get("MedInnASA");
			JRField MedInnASANumField = (JRField) fieldsMap.get("MedInnASANum");
			JRField MedInnBetablokkerField = (JRField) fieldsMap.get("MedInnBetablokker");
			JRField MedInnBetablokkerNumField = (JRField) fieldsMap.get("MedInnBetablokkerNum");
			JRField MedInnDiuretikaField = (JRField) fieldsMap.get("MedInnDiuretika");
			JRField MedInnDiuretikaNumField = (JRField) fieldsMap.get("MedInnDiuretikaNum");
			JRField MedInnIngenField = (JRField) fieldsMap.get("MedInnIngen");
			JRField MedInnLipidsenkereField = (JRField) fieldsMap.get("MedInnLipidsenkere");
			JRField MedInnLipidsenkereNumField = (JRField) fieldsMap.get("MedInnLipidsenkereNum");
			JRField MedInnplatehemmingField = (JRField) fieldsMap.get("MedInnplatehemming");
			JRField MedInnplatehemmingNumField = (JRField) fieldsMap.get("MedInnplatehemmingNum");
			JRField MedShACEhemmerAIIField = (JRField) fieldsMap.get("MedShACEhemmerAII");
			JRField MedShACEhemmerAIINumField = (JRField) fieldsMap.get("MedShACEhemmerAIINum");
			JRField MedShADPField = (JRField) fieldsMap.get("MedShADP");
			JRField MedShADPNumField = (JRField) fieldsMap.get("MedShADPNum");
			JRField MedShAndreAntiarytmikaField = (JRField) fieldsMap.get("MedShAndreAntiarytmika");
			JRField MedShAndreAntiarytmikaNumField = (JRField) fieldsMap.get("MedShAndreAntiarytmikaNum");
			JRField MedShAnnenAntikoagulasjonField = (JRField) fieldsMap.get("MedShAnnenAntikoagulasjon");
			JRField MedShAnnenAntikoagulasjonNumField = (JRField) fieldsMap.get("MedShAnnenAntikoagulasjonNum");
			JRField MedShAnnenPlatehemmingField = (JRField) fieldsMap.get("MedShAnnenPlatehemming");
			JRField MedShAnnenPlatehemmingNumField = (JRField) fieldsMap.get("MedShAnnenPlatehemmingNum");
			JRField MedShAntikoagulasjonField = (JRField) fieldsMap.get("MedShAntikoagulasjon");
			JRField MedShAntikoagulasjonNumField = (JRField) fieldsMap.get("MedShAntikoagulasjonNum");
			JRField MedShASAField = (JRField) fieldsMap.get("MedShASA");
			JRField MedShASANumField = (JRField) fieldsMap.get("MedShASANum");
			JRField MedShBetablokkerField = (JRField) fieldsMap.get("MedShBetablokker");
			JRField MedShBetablokkerNumField = (JRField) fieldsMap.get("MedShBetablokkerNum");
			JRField MedShDiuretikaField = (JRField) fieldsMap.get("MedShDiuretika");
			JRField MedShDiuretikaNumField = (JRField) fieldsMap.get("MedShDiuretikaNum");
			JRField MedShHeparinField = (JRField) fieldsMap.get("MedShHeparin");
			JRField MedShHeparinNumField = (JRField) fieldsMap.get("MedShHeparinNum");
			JRField MedShIIbIIIaField = (JRField) fieldsMap.get("MedShIIbIIIa");
			JRField MedShIIbIIIaNumField = (JRField) fieldsMap.get("MedShIIbIIIaNum");
			JRField MedShIngenField = (JRField) fieldsMap.get("MedShIngen");
			JRField MedShInotropeMedField = (JRField) fieldsMap.get("MedShInotropeMed");
			JRField MedShInotropeMedNumField = (JRField) fieldsMap.get("MedShInotropeMedNum");
			JRField MedShKvitaminField = (JRField) fieldsMap.get("MedShKvitamin");
			JRField MedShKvitaminNumField = (JRField) fieldsMap.get("MedShKvitaminNum");
			JRField MedShPlatehemmingField = (JRField) fieldsMap.get("MedShPlatehemming");
			JRField MedShPlatehemmingNumField = (JRField) fieldsMap.get("MedShPlatehemmingNum");
			JRField MedShTrombinhemmerField = (JRField) fieldsMap.get("MedShTrombinhemmer");
			JRField MedShTrombinhemmerNumField = (JRField) fieldsMap.get("MedShTrombinhemmerNum");
			JRField MedUtACEhemmerA2AAField = (JRField) fieldsMap.get("MedUtACEhemmerA2AA");
			JRField MedUtACEhemmerA2AANumField = (JRField) fieldsMap.get("MedUtACEhemmerA2AANum");
			JRField MedUtADPField = (JRField) fieldsMap.get("MedUtADP");
			JRField MedUtADPNumField = (JRField) fieldsMap.get("MedUtADPNum");
			JRField MedUtAndreLipidsenkereField = (JRField) fieldsMap.get("MedUtAndreLipidsenkere");
			JRField MedUtAndreLipidsenkereNumField = (JRField) fieldsMap.get("MedUtAndreLipidsenkereNum");
			JRField MedUtAnnenAntikoagulasjonField = (JRField) fieldsMap.get("MedUtAnnenAntikoagulasjon");
			JRField MedUtAnnenAntikoagulasjonNumField = (JRField) fieldsMap.get("MedUtAnnenAntikoagulasjonNum");
			JRField MedUtAnnenPlatehemmingField = (JRField) fieldsMap.get("MedUtAnnenPlatehemming");
			JRField MedUtAnnenPlatehemmingNumField = (JRField) fieldsMap.get("MedUtAnnenPlatehemmingNum");
			JRField MedUtAntikoagulasjonField = (JRField) fieldsMap.get("MedUtAntikoagulasjon");
			JRField MedUtAntikoagulasjonNumField = (JRField) fieldsMap.get("MedUtAntikoagulasjonNum");
			JRField MedUtASAField = (JRField) fieldsMap.get("MedUtASA");
			JRField MedUtASANumField = (JRField) fieldsMap.get("MedUtASANum");
			JRField MedUtBetablokkerField = (JRField) fieldsMap.get("MedUtBetablokker");
			JRField MedUtBetablokkerNumField = (JRField) fieldsMap.get("MedUtBetablokkerNum");
			JRField MedUtDiuretikaField = (JRField) fieldsMap.get("MedUtDiuretika");
			JRField MedUtDiuretikaNumField = (JRField) fieldsMap.get("MedUtDiuretikaNum");
			JRField MedUtHeparinField = (JRField) fieldsMap.get("MedUtHeparin");
			JRField MedUtHeparinlavmolField = (JRField) fieldsMap.get("MedUtHeparinlavmol");
			JRField MedUtHeparinlavmolNumField = (JRField) fieldsMap.get("MedUtHeparinlavmolNum");
			JRField MedUtIIbIIIaField = (JRField) fieldsMap.get("MedUtIIbIIIa");
			JRField MedUtIngenField = (JRField) fieldsMap.get("MedUtIngen");
			JRField MedUtKvitaminField = (JRField) fieldsMap.get("MedUtKvitamin");
			JRField MedUtKvitaminNumField = (JRField) fieldsMap.get("MedUtKvitaminNum");
			JRField MedUtPlatehemmingField = (JRField) fieldsMap.get("MedUtPlatehemming");
			JRField MedUtPlatehemmingNumField = (JRField) fieldsMap.get("MedUtPlatehemmingNum");
			JRField MedUtStatin2Field = (JRField) fieldsMap.get("MedUtStatin2");
			JRField MedUtStatin2NumField = (JRField) fieldsMap.get("MedUtStatin2Num");
			JRField MedUtTrombinhemmerField = (JRField) fieldsMap.get("MedUtTrombinhemmer");
			JRField MedUtTrombinhemmerNumField = (JRField) fieldsMap.get("MedUtTrombinhemmerNum");
			JRField MorsdatoField = (JRField) fieldsMap.get("Morsdato");
			JRField OppfUtreisedatoField = (JRField) fieldsMap.get("OppfUtreisedato");
			JRField OrganisasjonField = (JRField) fieldsMap.get("Organisasjon");
			JRField OrgRESHField = (JRField) fieldsMap.get("OrgRESH");
			JRField OverflytningstidField = (JRField) fieldsMap.get("Overflytningstid");
			JRField OverflyttetPasientField = (JRField) fieldsMap.get("OverflyttetPasient");
			JRField OverflyttetPasientNumField = (JRField) fieldsMap.get("OverflyttetPasientNum");
			JRField PasientnummerField = (JRField) fieldsMap.get("Pasientnummer");
			JRField PatientInRegistryKeyField = (JRField) fieldsMap.get("PatientInRegistryKey");
			JRField PCIsykehusField = (JRField) fieldsMap.get("PCIsykehus");
			JRField PostnrField = (JRField) fieldsMap.get("Postnr");
			JRField PoststedField = (JRField) fieldsMap.get("Poststed");
			JRField RegistreringsavdField = (JRField) fieldsMap.get("Registreringsavd");
			JRField ReshIDField = (JRField) fieldsMap.get("ReshID");
			JRField ResusciteringField = (JRField) fieldsMap.get("Resuscitering");
			JRField ResusciteringNumField = (JRField) fieldsMap.get("ResusciteringNum");
			JRField RHFnavnField = (JRField) fieldsMap.get("RHFnavn");
			JRField RHFreshField = (JRField) fieldsMap.get("RHFresh");
			JRField RoeykerField = (JRField) fieldsMap.get("Roeyker");
			JRField RoeykerNumField = (JRField) fieldsMap.get("RoeykerNum");
			JRField SivilstatusField = (JRField) fieldsMap.get("Sivilstatus");
			JRField SkjematypeField = (JRField) fieldsMap.get("Skjematype");
			JRField SorteringsParameterVerdiField = (JRField) fieldsMap.get("SorteringsParameterVerdi");
			JRField StatinField = (JRField) fieldsMap.get("Statin");
			JRField StatinNumField = (JRField) fieldsMap.get("StatinNum");
			JRField SykehusField = (JRField) fieldsMap.get("Sykehus");
			JRField SymptomdebutField = (JRField) fieldsMap.get("Symptomdebut");
			JRField TeknDataSettIDField = (JRField) fieldsMap.get("TeknDataSettID");
			JRField TeknFradatoField = (JRField) fieldsMap.get("TeknFradato");
			JRField TeknKontaktFraDatoField = (JRField) fieldsMap.get("TeknKontaktFraDato");
			JRField TeknKontaktIDField = (JRField) fieldsMap.get("TeknKontaktID");
			JRField TeknKontaktNavnField = (JRField) fieldsMap.get("TeknKontaktNavn");
			JRField TeknKontaktTilDatoField = (JRField) fieldsMap.get("TeknKontaktTilDato");
			JRField TeknOpprettetDatoField = (JRField) fieldsMap.get("TeknOpprettetDato");
			JRField TeknRapportgrunnlagIDField = (JRField) fieldsMap.get("TeknRapportgrunnlagID");
			JRField TeknRelatedIDField = (JRField) fieldsMap.get("TeknRelatedID");
			JRField TeknSkjemaIDField = (JRField) fieldsMap.get("TeknSkjemaID");
			JRField TeknTildatoField = (JRField) fieldsMap.get("TeknTildato");
			JRField TeknUpdatedField = (JRField) fieldsMap.get("TeknUpdated");
			JRField TidlDiabetesField = (JRField) fieldsMap.get("TidlDiabetes");
			JRField TidlDiabetesNumField = (JRField) fieldsMap.get("TidlDiabetesNum");
			JRField TidlFamiliaerOpphopningField = (JRField) fieldsMap.get("TidlFamiliaerOpphopning");
			JRField TidlFamiliaerOpphopningNumField = (JRField) fieldsMap.get("TidlFamiliaerOpphopningNum");
			JRField TidlHjerneslagField = (JRField) fieldsMap.get("TidlHjerneslag");
			JRField TidlHjerneslagNumField = (JRField) fieldsMap.get("TidlHjerneslagNum");
			JRField TidlHjerteinfarktField = (JRField) fieldsMap.get("TidlHjerteinfarkt");
			JRField TidlHjerteinfarktNumField = (JRField) fieldsMap.get("TidlHjerteinfarktNum");
			JRField TidlHypertensjonsbehField = (JRField) fieldsMap.get("TidlHypertensjonsbeh");
			JRField TidlHypertensjonsbehNumField = (JRField) fieldsMap.get("TidlHypertensjonsbehNum");
			JRField TidlIngenField = (JRField) fieldsMap.get("TidlIngen");
			JRField TidlKoronaroperertField = (JRField) fieldsMap.get("TidlKoronaroperert");
			JRField TidlKoronaroperertNumField = (JRField) fieldsMap.get("TidlKoronaroperertNum");
			JRField TidlKroniskHjertesviktField = (JRField) fieldsMap.get("TidlKroniskHjertesvikt");
			JRField TidlKroniskHjertesviktNumField = (JRField) fieldsMap.get("TidlKroniskHjertesviktNum");
			JRField TidlPCIField = (JRField) fieldsMap.get("TidlPCI");
			JRField TidlPCINumField = (JRField) fieldsMap.get("TidlPCINum");
			JRField TidlPeriferVaskulaerSykdomField = (JRField) fieldsMap.get("TidlPeriferVaskulaerSykdom");
			JRField TidlPeriferVaskulaerSykdomNumField = (JRField) fieldsMap.get("TidlPeriferVaskulaerSykdomNum");
			JRField TiltakCPAPBiPAPField = (JRField) fieldsMap.get("TiltakCPAPBiPAP");
			JRField TiltakCPAPBiPAPNumField = (JRField) fieldsMap.get("TiltakCPAPBiPAPNum");
			JRField TiltakHypotermibehField = (JRField) fieldsMap.get("TiltakHypotermibeh");
			JRField TiltakHypotermibehNumField = (JRField) fieldsMap.get("TiltakHypotermibehNum");
			JRField TiltakIABPField = (JRField) fieldsMap.get("TiltakIABP");
			JRField TiltakIABPNumField = (JRField) fieldsMap.get("TiltakIABPNum");
			JRField TiltakIngenField = (JRField) fieldsMap.get("TiltakIngen");
			JRField TiltakPMICDField = (JRField) fieldsMap.get("TiltakPMICD");
			JRField TiltakPMICDNumField = (JRField) fieldsMap.get("TiltakPMICDNum");
			JRField TiltakRespiratorField = (JRField) fieldsMap.get("TiltakRespirator");
			JRField TiltakRespiratorNumField = (JRField) fieldsMap.get("TiltakRespiratorNum");
			JRField TrombolysebehField = (JRField) fieldsMap.get("Trombolysebeh");
			JRField TrombolysebehNumField = (JRField) fieldsMap.get("TrombolysebehNum");
			JRField TrombolysetidField = (JRField) fieldsMap.get("Trombolysetid");
			JRField TroponingStigningFallField = (JRField) fieldsMap.get("TroponingStigningFall");
			JRField TroponingStigningFalNumField = (JRField) fieldsMap.get("TroponingStigningFalNum");
			JRField TypeInfarktField = (JRField) fieldsMap.get("TypeInfarkt");
			JRField TypeInfarktTekstField = (JRField) fieldsMap.get("TypeInfarktTekst");
			JRField UtreisedatoField = (JRField) fieldsMap.get("Utreisedato");
			JRField UtskrivesTil2Field = (JRField) fieldsMap.get("UtskrivesTil2");
			JRField UtskrivesTil2NumField = (JRField) fieldsMap.get("UtskrivesTil2Num");
			JRField VisInnkomstvariablerField = (JRField) fieldsMap.get("VisInnkomstvariabler");
			JRField YrkeField = (JRField) fieldsMap.get("Yrke");
			JRField DatasettGuidField = (JRField) fieldsMap.get("DatasettGuid");
			
			
			// Create "slug arrays" with very big sizes (default limit to
			// 1000000) to accommodate large queries
			// We cannot find out how many rows are returned so we have to fetch
			// first and then
			// rebuild arrays of the proper size before passing to R
			//
			// Arrays MUST be defined as objects and not its primitive since
			// returned values do contain 'null'
			
			log.debug("Making empty slug array...");
			
			Double[] sAlder = new Double[50000];
			String[] sAvdeling = new String[50000];
			Double[] sAvdelingNum = new Double[50000];
			String[] sAvdNavn = new String[50000];
			Double[] sBMI = new Double[50000];
			Double[] sBMIHoyde = new Double[50000];
			Double[] sBMIUkjent = new Double[50000];
			Double[] sBMIVekt = new Double[50000];
			Double[] sBTPOvertrykk = new Double[50000];
			Double[] sBTPPuls = new Double[50000];
			Double[] sBTPUkjent = new Double[50000];
			Double[] sBTPUndertrykk = new Double[50000];
			String[] sDoedUnderOpphold = new String[50000];
			Double[] sDoedUnderOppholdNum = new Double[50000];
			String[] sEKGDiagnostisk = new String[50000];
			Double[] sEKGDiagnostiskNum = new Double[50000];
			String[] sEKGNyQbolge = new String[50000];
			Double[] sEKGNyQbolgeNum = new Double[50000];
			String[] sEKGRytme = new String[50000];
			Double[] sEKGRytmeNum = new Double[50000];
			String[] sEKGSteminStemi = new String[50000];
			Double[] sEKGSteminStemiNum = new Double[50000];
			String[] sEkkoEF = new String[50000];
			Double[] sEkkoEfNum = new Double[50000];
			String[] sEkkoValg = new String[50000];
			Double[] sEkkoValgNum = new Double[50000];
			String[] sPasientID = new String[50000];
			String[] sFoedselsaar = new String[50000];
			String[] sFraHvilkenRegion = new String[50000];
			Double[] sFraHvilkenRegionNum = new Double[50000];
			String[] sFraHvilketSykehus2 = new String[50000];
			String[] sHvorBefantPasientenSeg = new String[50000];
			Double[] sHvorBefantPasientenSegNum = new Double[50000];
			String[] sInfarktlokalisasjon = new String[50000];
			Double[] sInfarktlokalisasjonNum = new Double[50000];
			String[] sInnleggelsestidspunkt = new String[50000];
			String[] sInnleggelsestidspunkt2 = new String[50000];
			String[] sInvAntSykekar = new String[50000];
			String[] sInvAntSykekarTekst = new String[50000];
			String[] sInvKunKorAngio = new String[50000];
			Double[] sInvKunKorAngioNum = new Double[50000];
			String[] sInvKunKorAngioPCITid = new String[50000];
			String[] sInvKunKorAngioTid = new String[50000];
			String[] sInvPCIunderoppholdet = new String[50000];
			Double[] sInvPCIunderoppholdetNum = new Double[50000];
			String[] sInvStent = new String[50000];
			String[] sInvStentTekst = new String[50000];
			String[] sInvStentType = new String[50000];
			String[] sInvStentTypeTekst = new String[50000];
			String[] sIvnPCITid = new String[50000];
			String[] sKjoenn = new String[50000];
			String[] sKommunenr = new String[50000];
			String[] sKomplAtrieflimmerFlutter = new String[50000];
			Double[] sKomplAtrieflimmerFlutterNum = new Double[50000];
			String[] sKomplAVblokk2el3 = new String[50000];
			Double[] sKomplAVblokk2el3Num = new Double[50000];
			String[] sKomplBloedAnnen = new String[50000];
			Double[] sKomplBloedAnnenNum = new Double[50000];
			String[] sKomplBloedCerebral = new String[50000];
			Double[] sKomplBloedCerebralNum = new Double[50000];
			String[] sKomplBloedGI = new String[50000];
			Double[] sKomplBloedGINum = new Double[50000];
			String[] sKomplBloedInnstikk = new String[50000];
			Double[] sKomplBloedInnstikkNum = new Double[50000];
			String[] sKomplBloedning = new String[50000];
			Double[] sKomplBloedningNum = new Double[50000];
			String[] sKomplHjerneslag = new String[50000];
			Double[] sKomplHjerneslagNum = new Double[50000];
			String[] sKomplHjertesvikt = new String[50000];
			Double[] sKomplHjertesviktNum = new Double[50000];
			Double[] sKomplIngen = new Double[50000];
			String[] sKomplKardiogentSjokk = new String[50000];
			Double[] sKomplKardiogentSjokkNum = new Double[50000];
			String[] sKomplMekaniskKomplikasjon = new String[50000];
			Double[] sKomplMekaniskKomplikasjonNum = new Double[50000];
			String[] sKomplReinfarkt = new String[50000];
			Double[] sKomplReinfarktNum = new Double[50000];
			Double[] sKomplVTVFe48Num = new Double[50000];
			String[] sKomplVTVFe48t = new String[50000];
			String[] sKomplVTVFf48t = new String[50000];
			Double[] sKomplVTVFf48tNum = new Double[50000];
			String[] sKoronarProsedyreEtPlan = new String[50000];
			Double[] sKoronarProsedyreEtPlanNum = new Double[50000];
			String[] sLabGlukose = new String[50000];
			String[] sLabHbA1C = new String[50000];
			String[] sLabHDL = new String[50000];
			String[] sLabInfarktmarkoer = new String[50000];
			Double[] sLabInfarktmarkoererNum = new Double[50000];
			String[] sLabInfarktmarkorHoy = new String[50000];
			String[] sLabInfarktmarkorLav = new String[50000];
			String[] sLabKreatinin = new String[50000];
			String[] sLabLDL = new String[50000];
			String[] sLabTotalkolesterol = new String[50000];
			String[] sLabTriglyserider = new String[50000];
			Double[] sLiggetid = new Double[50000];
			String[] sLokalRegion = new String[50000];
			String[] sLokalRegion2 = new String[50000];
			Double[] sLokalRegionNum = new Double[50000];
			String[] sMedInnACEhemmerAII = new String[50000];
			Double[] sMedInnACEhemmerAIINum = new Double[50000];
			String[] sMedInnADP = new String[50000];
			Double[] sMedInnADPNum = new Double[50000];
			String[] sMedInnAnnenPlatehemming = new String[50000];
			Double[] sMedInnAnnenPlatehemmingNum = new Double[50000];
			String[] sMedInnAntikoagulasjon = new String[50000];
			Double[] sMedInnAntikoagulasjonNum = new Double[50000];
			String[] sMedInnASA = new String[50000];
			Double[] sMedInnASANum = new Double[50000];
			String[] sMedInnBetablokker = new String[50000];
			Double[] sMedInnBetablokkerNum = new Double[50000];
			String[] sMedInnDiuretika = new String[50000];
			Double[] sMedInnDiuretikaNum = new Double[50000];
			Double[] sMedInnIngen = new Double[50000];
			String[] sMedInnLipidsenkere = new String[50000];
			Double[] sMedInnLipidsenkereNum = new Double[50000];
			String[] sMedInnplatehemming = new String[50000];
			Double[] sMedInnplatehemmingNum = new Double[50000];
			String[] sMedShACEhemmerAII = new String[50000];
			Double[] sMedShACEhemmerAIINum = new Double[50000];
			String[] sMedShADP = new String[50000];
			Double[] sMedShADPNum = new Double[50000];
			String[] sMedShAndreAntiarytmika = new String[50000];
			Double[] sMedShAndreAntiarytmikaNum = new Double[50000];
			String[] sMedShAnnenAntikoagulasjon = new String[50000];
			Double[] sMedShAnnenAntikoagulasjonNum = new Double[50000];
			String[] sMedShAnnenPlatehemming = new String[50000];
			Double[] sMedShAnnenPlatehemmingNum = new Double[50000];
			String[] sMedShAntikoagulasjon = new String[50000];
			Double[] sMedShAntikoagulasjonNum = new Double[50000];
			String[] sMedShASA = new String[50000];
			Double[] sMedShASANum = new Double[50000];
			String[] sMedShBetablokker = new String[50000];
			Double[] sMedShBetablokkerNum = new Double[50000];
			String[] sMedShDiuretika = new String[50000];
			Double[] sMedShDiuretikaNum = new Double[50000];
			String[] sMedShHeparin = new String[50000];
			Double[] sMedShHeparinNum = new Double[50000];
			String[] sMedShIIbIIIa = new String[50000];
			Double[] sMedShIIbIIIaNum = new Double[50000];
			Double[] sMedShIngen = new Double[50000];
			String[] sMedShInotropeMed = new String[50000];
			Double[] sMedShInotropeMedNum = new Double[50000];
			String[] sMedShKvitamin = new String[50000];
			Double[] sMedShKvitaminNum = new Double[50000];
			String[] sMedShPlatehemming = new String[50000];
			Double[] sMedShPlatehemmingNum = new Double[50000];
			String[] sMedShTrombinhemmer = new String[50000];
			Double[] sMedShTrombinhemmerNum = new Double[50000];
			String[] sMedUtACEhemmerA2AA = new String[50000];
			Double[] sMedUtACEhemmerA2AANum = new Double[50000];
			String[] sMedUtADP = new String[50000];
			Double[] sMedUtADPNum = new Double[50000];
			String[] sMedUtAndreLipidsenkere = new String[50000];
			Double[] sMedUtAndreLipidsenkereNum = new Double[50000];
			String[] sMedUtAnnenAntikoagulasjon = new String[50000];
			Double[] sMedUtAnnenAntikoagulasjonNum = new Double[50000];
			String[] sMedUtAnnenPlatehemming = new String[50000];
			Double[] sMedUtAnnenPlatehemmingNum = new Double[50000];
			String[] sMedUtAntikoagulasjon = new String[50000];
			Double[] sMedUtAntikoagulasjonNum = new Double[50000];
			String[] sMedUtASA = new String[50000];
			Double[] sMedUtASANum = new Double[50000];
			String[] sMedUtBetablokker = new String[50000];
			Double[] sMedUtBetablokkerNum = new Double[50000];
			String[] sMedUtDiuretika = new String[50000];
			Double[] sMedUtDiuretikaNum = new Double[50000];
			String[] sMedUtHeparin = new String[50000];
			String[] sMedUtHeparinlavmol = new String[50000];
			Double[] sMedUtHeparinlavmolNum = new Double[50000];
			String[] sMedUtIIbIIIa = new String[50000];
			Double[] sMedUtIngen = new Double[50000];
			String[] sMedUtKvitamin = new String[50000];
			Double[] sMedUtKvitaminNum = new Double[50000];
			String[] sMedUtPlatehemming = new String[50000];
			Double[] sMedUtPlatehemmingNum = new Double[50000];
			String[] sMedUtStatin2 = new String[50000];
			Double[] sMedUtStatin2Num = new Double[50000];
			String[] sMedUtTrombinhemmer = new String[50000];
			Double[] sMedUtTrombinhemmerNum = new Double[50000];
			String[] sMorsdato = new String[50000];
			String[] sOppfUtreisedato = new String[50000];
			String[] sOrganisasjon = new String[50000];
			Double[] sOrgRESH = new Double[50000];
			String[] sOverflytningstid = new String[50000];
			String[] sOverflyttetPasient = new String[50000];
			Double[] sOverflyttetPasientNum = new Double[50000];
			String[] sPasientnummer = new String[50000];
			String[] sPatientInRegistryKey = new String[50000];
			Double[] sPCIsykehus = new Double[50000];
			String[] sPostnr = new String[50000];
			String[] sPoststed = new String[50000];
			String[] sRegistreringsavd = new String[50000];
			Double[] sReshID = new Double[50000];
			String[] sResuscitering = new String[50000];
			Double[] sResusciteringNum = new Double[50000];
			String[] sRHFnavn = new String[50000];
			Double[] sRHFresh = new Double[50000];
			String[] sRoeyker = new String[50000];
			Double[] sRoeykerNum = new Double[50000];
			String[] sSivilstatus = new String[50000];
			String[] sSkjematype = new String[50000];
			String[] sSorteringsParameterVerdi = new String[50000];
			String[] sStatin = new String[50000];
			Double[] sStatinNum = new Double[50000];
			String[] sSykehus = new String[50000];
			String[] sSymptomdebut = new String[50000];
			Double[] sTeknDataSettID = new Double[50000];
			String[] sTeknFradato = new String[50000];
			String[] sTeknKontaktFraDato = new String[50000];
			Double[] sTeknKontaktID = new Double[50000];
			String[] sTeknKontaktNavn = new String[50000];
			String[] sTeknKontaktTilDato = new String[50000];
			String[] sTeknOpprettetDato = new String[50000];
			Double[] sTeknRapportgrunnlagID = new Double[50000];
			Double[] sTeknRelatedID = new Double[50000];
			Double[] sTeknSkjemaID = new Double[50000];
			String[] sTeknTildato = new String[50000];
			String[] sTeknUpdated = new String[50000];
			String[] sTidlDiabetes = new String[50000];
			Double[] sTidlDiabetesNum = new Double[50000];
			String[] sTidlFamiliaerOpphopning = new String[50000];
			Double[] sTidlFamiliaerOpphopningNum = new Double[50000];
			String[] sTidlHjerneslag = new String[50000];
			Double[] sTidlHjerneslagNum = new Double[50000];
			String[] sTidlHjerteinfarkt = new String[50000];
			Double[] sTidlHjerteinfarktNum = new Double[50000];
			String[] sTidlHypertensjonsbeh = new String[50000];
			Double[] sTidlHypertensjonsbehNum = new Double[50000];
			Double[] sTidlIngen = new Double[50000];
			String[] sTidlKoronaroperert = new String[50000];
			Double[] sTidlKoronaroperertNum = new Double[50000];
			String[] sTidlKroniskHjertesvikt = new String[50000];
			Double[] sTidlKroniskHjertesviktNum = new Double[50000];
			String[] sTidlPCI = new String[50000];
			Double[] sTidlPCINum = new Double[50000];
			String[] sTidlPeriferVaskulaerSykdom = new String[50000];
			Double[] sTidlPeriferVaskulaerSykdomNum = new Double[50000];
			String[] sTiltakCPAPBiPAP = new String[50000];
			Double[] sTiltakCPAPBiPAPNum = new Double[50000];
			String[] sTiltakHypotermibeh = new String[50000];
			Double[] sTiltakHypotermibehNum = new Double[50000];
			String[] sTiltakIABP = new String[50000];
			Double[] sTiltakIABPNum = new Double[50000];
			Double[] sTiltakIngen = new Double[50000];
			String[] sTiltakPMICD = new String[50000];
			Double[] sTiltakPMICDNum = new Double[50000];
			String[] sTiltakRespirator = new String[50000];
			Double[] sTiltakRespiratorNum = new Double[50000];
			String[] sTrombolysebeh = new String[50000];
			Double[] sTrombolysebehNum = new Double[50000];
			String[] sTrombolysetid = new String[50000];
			String[] sTroponingStigningFall = new String[50000];
			Double[] sTroponingStigningFalNum = new Double[50000];
			String[] sTypeInfarkt = new String[50000];
			String[] sTypeInfarktTekst = new String[50000];
			String[] sUtreisedato = new String[50000];
			String[] sUtskrivesTil2 = new String[50000];
			Double[] sUtskrivesTil2Num = new Double[50000];
			Double[] sVisInnkomstvariabler = new Double[50000];
			String[] sYrke = new String[50000];
			String[] sDatasettGuid = new String[50000];


			
			log.debug("populating slug array with report data...");
			
			int rowidx = 0;
			// Assume we get 1 row
			boolean getRow = true;
			while (getRow) {
				sAlder[rowidx] = (Double) ds.getFieldValue(AlderField);
				sAvdeling[rowidx] = (String) ds.getFieldValue(AvdelingField);
				sAvdelingNum[rowidx] = (Double) ds.getFieldValue(AvdelingNumField);
				sAvdNavn[rowidx] = (String) ds.getFieldValue(AvdNavnField);
				sBMI[rowidx] = (Double) ds.getFieldValue(BMIField);
				sBMIHoyde[rowidx] = (Double) ds.getFieldValue(BMIHoydeField);
				sBMIUkjent[rowidx] = (Double) ds.getFieldValue(BMIUkjentField);
				sBMIVekt[rowidx] = (Double) ds.getFieldValue(BMIVektField);
				sBTPOvertrykk[rowidx] = (Double) ds.getFieldValue(BTPOvertrykkField);
				sBTPPuls[rowidx] = (Double) ds.getFieldValue(BTPPulsField);
				sBTPUkjent[rowidx] = (Double) ds.getFieldValue(BTPUkjentField);
				sBTPUndertrykk[rowidx] = (Double) ds.getFieldValue(BTPUndertrykkField);
				sDoedUnderOpphold[rowidx] = (String) ds.getFieldValue(DoedUnderOppholdField);
				sDoedUnderOppholdNum[rowidx] = (Double) ds.getFieldValue(DoedUnderOppholdNumField);
				sEKGDiagnostisk[rowidx] = (String) ds.getFieldValue(EKGDiagnostiskField);
				sEKGDiagnostiskNum[rowidx] = (Double) ds.getFieldValue(EKGDiagnostiskNumField);
				sEKGNyQbolge[rowidx] = (String) ds.getFieldValue(EKGNyQbolgeField);
				sEKGNyQbolgeNum[rowidx] = (Double) ds.getFieldValue(EKGNyQbolgeNumField);
				sEKGRytme[rowidx] = (String) ds.getFieldValue(EKGRytmeField);
				sEKGRytmeNum[rowidx] = (Double) ds.getFieldValue(EKGRytmeNumField);
				sEKGSteminStemi[rowidx] = (String) ds.getFieldValue(EKGSteminStemiField);
				sEKGSteminStemiNum[rowidx] = (Double) ds.getFieldValue(EKGSteminStemiNumField);
				sEkkoEF[rowidx] = (String) ds.getFieldValue(EkkoEFField);
				sEkkoEfNum[rowidx] = (Double) ds.getFieldValue(EkkoEfNumField);
				sEkkoValg[rowidx] = (String) ds.getFieldValue(EkkoValgField);
				sEkkoValgNum[rowidx] = (Double) ds.getFieldValue(EkkoValgNumField);
				sPasientID[rowidx] = (String) ds.getFieldValue(PasientIDField);
				sFoedselsaar[rowidx] = (String) ds.getFieldValue(FoedselsaarField);
				sFraHvilkenRegion[rowidx] = (String) ds.getFieldValue(FraHvilkenRegionField);
				sFraHvilkenRegionNum[rowidx] = (Double) ds.getFieldValue(FraHvilkenRegionNumField);
				sFraHvilketSykehus2[rowidx] = (String) ds.getFieldValue(FraHvilketSykehus2Field);
				sHvorBefantPasientenSeg[rowidx] = (String) ds.getFieldValue(HvorBefantPasientenSegField);
				sHvorBefantPasientenSegNum[rowidx] = (Double) ds.getFieldValue(HvorBefantPasientenSegNumField);
				sInfarktlokalisasjon[rowidx] = (String) ds.getFieldValue(InfarktlokalisasjonField);
				sInfarktlokalisasjonNum[rowidx] = (Double) ds.getFieldValue(InfarktlokalisasjonNumField);
				sInnleggelsestidspunkt[rowidx] = (String) ds.getFieldValue(InnleggelsestidspunktField);
				sInnleggelsestidspunkt2[rowidx] = (String) ds.getFieldValue(Innleggelsestidspunkt2Field);
				sInvAntSykekar[rowidx] = (String) ds.getFieldValue(InvAntSykekarField);
				sInvAntSykekarTekst[rowidx] = (String) ds.getFieldValue(InvAntSykekarTekstField);
				sInvKunKorAngio[rowidx] = (String) ds.getFieldValue(InvKunKorAngioField);
				sInvKunKorAngioNum[rowidx] = (Double) ds.getFieldValue(InvKunKorAngioNumField);
				sInvKunKorAngioPCITid[rowidx] = (String) ds.getFieldValue(InvKunKorAngioPCITidField);
				sInvKunKorAngioTid[rowidx] = (String) ds.getFieldValue(InvKunKorAngioTidField);
				sInvPCIunderoppholdet[rowidx] = (String) ds.getFieldValue(InvPCIunderoppholdetField);
				sInvPCIunderoppholdetNum[rowidx] = (Double) ds.getFieldValue(InvPCIunderoppholdetNumField);
				sInvStent[rowidx] = (String) ds.getFieldValue(InvStentField);
				sInvStentTekst[rowidx] = (String) ds.getFieldValue(InvStentTekstField);
				sInvStentType[rowidx] = (String) ds.getFieldValue(InvStentTypeField);
				sInvStentTypeTekst[rowidx] = (String) ds.getFieldValue(InvStentTypeTekstField);
				sIvnPCITid[rowidx] = (String) ds.getFieldValue(IvnPCITidField);
				sKjoenn[rowidx] = (String) ds.getFieldValue(KjoennField);
				sKommunenr[rowidx] = (String) ds.getFieldValue(KommunenrField);
				sKomplAtrieflimmerFlutter[rowidx] = (String) ds.getFieldValue(KomplAtrieflimmerFlutterField);
				sKomplAtrieflimmerFlutterNum[rowidx] = (Double) ds.getFieldValue(KomplAtrieflimmerFlutterNumField);
				sKomplAVblokk2el3[rowidx] = (String) ds.getFieldValue(KomplAVblokk2el3Field);
				sKomplAVblokk2el3Num[rowidx] = (Double) ds.getFieldValue(KomplAVblokk2el3NumField);
				sKomplBloedAnnen[rowidx] = (String) ds.getFieldValue(KomplBloedAnnenField);
				sKomplBloedAnnenNum[rowidx] = (Double) ds.getFieldValue(KomplBloedAnnenNumField);
				sKomplBloedCerebral[rowidx] = (String) ds.getFieldValue(KomplBloedCerebralField);
				sKomplBloedCerebralNum[rowidx] = (Double) ds.getFieldValue(KomplBloedCerebralNumField);
				sKomplBloedGI[rowidx] = (String) ds.getFieldValue(KomplBloedGIField);
				sKomplBloedGINum[rowidx] = (Double) ds.getFieldValue(KomplBloedGINumField);
				sKomplBloedInnstikk[rowidx] = (String) ds.getFieldValue(KomplBloedInnstikkField);
				sKomplBloedInnstikkNum[rowidx] = (Double) ds.getFieldValue(KomplBloedInnstikkNumField);
				sKomplBloedning[rowidx] = (String) ds.getFieldValue(KomplBloedningField);
				sKomplBloedningNum[rowidx] = (Double) ds.getFieldValue(KomplBloedningNumField);
				sKomplHjerneslag[rowidx] = (String) ds.getFieldValue(KomplHjerneslagField);
				sKomplHjerneslagNum[rowidx] = (Double) ds.getFieldValue(KomplHjerneslagNumField);
				sKomplHjertesvikt[rowidx] = (String) ds.getFieldValue(KomplHjertesviktField);
				sKomplHjertesviktNum[rowidx] = (Double) ds.getFieldValue(KomplHjertesviktNumField);
				sKomplIngen[rowidx] = (Double) ds.getFieldValue(KomplIngenField);
				sKomplKardiogentSjokk[rowidx] = (String) ds.getFieldValue(KomplKardiogentSjokkField);
				sKomplKardiogentSjokkNum[rowidx] = (Double) ds.getFieldValue(KomplKardiogentSjokkNumField);
				sKomplMekaniskKomplikasjon[rowidx] = (String) ds.getFieldValue(KomplMekaniskKomplikasjonField);
				sKomplMekaniskKomplikasjonNum[rowidx] = (Double) ds.getFieldValue(KomplMekaniskKomplikasjonNumField);
				sKomplReinfarkt[rowidx] = (String) ds.getFieldValue(KomplReinfarktField);
				sKomplReinfarktNum[rowidx] = (Double) ds.getFieldValue(KomplReinfarktNumField);
				sKomplVTVFe48Num[rowidx] = (Double) ds.getFieldValue(KomplVTVFe48NumField);
				sKomplVTVFe48t[rowidx] = (String) ds.getFieldValue(KomplVTVFe48tField);
				sKomplVTVFf48t[rowidx] = (String) ds.getFieldValue(KomplVTVFf48tField);
				sKomplVTVFf48tNum[rowidx] = (Double) ds.getFieldValue(KomplVTVFf48tNumField);
				sKoronarProsedyreEtPlan[rowidx] = (String) ds.getFieldValue(KoronarProsedyreEtPlanField);
				sKoronarProsedyreEtPlanNum[rowidx] = (Double) ds.getFieldValue(KoronarProsedyreEtPlanNumField);
				sLabGlukose[rowidx] = (String) ds.getFieldValue(LabGlukoseField);
				sLabHbA1C[rowidx] = (String) ds.getFieldValue(LabHbA1CField);
				sLabHDL[rowidx] = (String) ds.getFieldValue(LabHDLField);
				sLabInfarktmarkoer[rowidx] = (String) ds.getFieldValue(LabInfarktmarkoerField);
				sLabInfarktmarkoererNum[rowidx] = (Double) ds.getFieldValue(LabInfarktmarkoererNumField);
				sLabInfarktmarkorHoy[rowidx] = (String) ds.getFieldValue(LabInfarktmarkorHoyField);
				sLabInfarktmarkorLav[rowidx] = (String) ds.getFieldValue(LabInfarktmarkorLavField);
				sLabKreatinin[rowidx] = (String) ds.getFieldValue(LabKreatininField);
				sLabLDL[rowidx] = (String) ds.getFieldValue(LabLDLField);
				sLabTotalkolesterol[rowidx] = (String) ds.getFieldValue(LabTotalkolesterolField);
				sLabTriglyserider[rowidx] = (String) ds.getFieldValue(LabTriglyseriderField);
				sLiggetid[rowidx] = (Double) ds.getFieldValue(LiggetidField);
				sLokalRegion[rowidx] = (String) ds.getFieldValue(LokalRegionField);
				sLokalRegion2[rowidx] = (String) ds.getFieldValue(LokalRegion2Field);
				sLokalRegionNum[rowidx] = (Double) ds.getFieldValue(LokalRegionNumField);
				sMedInnACEhemmerAII[rowidx] = (String) ds.getFieldValue(MedInnACEhemmerAIIField);
				sMedInnACEhemmerAIINum[rowidx] = (Double) ds.getFieldValue(MedInnACEhemmerAIINumField);
				sMedInnADP[rowidx] = (String) ds.getFieldValue(MedInnADPField);
				sMedInnADPNum[rowidx] = (Double) ds.getFieldValue(MedInnADPNumField);
				sMedInnAnnenPlatehemming[rowidx] = (String) ds.getFieldValue(MedInnAnnenPlatehemmingField);
				sMedInnAnnenPlatehemmingNum[rowidx] = (Double) ds.getFieldValue(MedInnAnnenPlatehemmingNumField);
				sMedInnAntikoagulasjon[rowidx] = (String) ds.getFieldValue(MedInnAntikoagulasjonField);
				sMedInnAntikoagulasjonNum[rowidx] = (Double) ds.getFieldValue(MedInnAntikoagulasjonNumField);
				sMedInnASA[rowidx] = (String) ds.getFieldValue(MedInnASAField);
				sMedInnASANum[rowidx] = (Double) ds.getFieldValue(MedInnASANumField);
				sMedInnBetablokker[rowidx] = (String) ds.getFieldValue(MedInnBetablokkerField);
				sMedInnBetablokkerNum[rowidx] = (Double) ds.getFieldValue(MedInnBetablokkerNumField);
				sMedInnDiuretika[rowidx] = (String) ds.getFieldValue(MedInnDiuretikaField);
				sMedInnDiuretikaNum[rowidx] = (Double) ds.getFieldValue(MedInnDiuretikaNumField);
				sMedInnIngen[rowidx] = (Double) ds.getFieldValue(MedInnIngenField);
				sMedInnLipidsenkere[rowidx] = (String) ds.getFieldValue(MedInnLipidsenkereField);
				sMedInnLipidsenkereNum[rowidx] = (Double) ds.getFieldValue(MedInnLipidsenkereNumField);
				sMedInnplatehemming[rowidx] = (String) ds.getFieldValue(MedInnplatehemmingField);
				sMedInnplatehemmingNum[rowidx] = (Double) ds.getFieldValue(MedInnplatehemmingNumField);
				sMedShACEhemmerAII[rowidx] = (String) ds.getFieldValue(MedShACEhemmerAIIField);
				sMedShACEhemmerAIINum[rowidx] = (Double) ds.getFieldValue(MedShACEhemmerAIINumField);
				sMedShADP[rowidx] = (String) ds.getFieldValue(MedShADPField);
				sMedShADPNum[rowidx] = (Double) ds.getFieldValue(MedShADPNumField);
				sMedShAndreAntiarytmika[rowidx] = (String) ds.getFieldValue(MedShAndreAntiarytmikaField);
				sMedShAndreAntiarytmikaNum[rowidx] = (Double) ds.getFieldValue(MedShAndreAntiarytmikaNumField);
				sMedShAnnenAntikoagulasjon[rowidx] = (String) ds.getFieldValue(MedShAnnenAntikoagulasjonField);
				sMedShAnnenAntikoagulasjonNum[rowidx] = (Double) ds.getFieldValue(MedShAnnenAntikoagulasjonNumField);
				sMedShAnnenPlatehemming[rowidx] = (String) ds.getFieldValue(MedShAnnenPlatehemmingField);
				sMedShAnnenPlatehemmingNum[rowidx] = (Double) ds.getFieldValue(MedShAnnenPlatehemmingNumField);
				sMedShAntikoagulasjon[rowidx] = (String) ds.getFieldValue(MedShAntikoagulasjonField);
				sMedShAntikoagulasjonNum[rowidx] = (Double) ds.getFieldValue(MedShAntikoagulasjonNumField);
				sMedShASA[rowidx] = (String) ds.getFieldValue(MedShASAField);
				sMedShASANum[rowidx] = (Double) ds.getFieldValue(MedShASANumField);
				sMedShBetablokker[rowidx] = (String) ds.getFieldValue(MedShBetablokkerField);
				sMedShBetablokkerNum[rowidx] = (Double) ds.getFieldValue(MedShBetablokkerNumField);
				sMedShDiuretika[rowidx] = (String) ds.getFieldValue(MedShDiuretikaField);
				sMedShDiuretikaNum[rowidx] = (Double) ds.getFieldValue(MedShDiuretikaNumField);
				sMedShHeparin[rowidx] = (String) ds.getFieldValue(MedShHeparinField);
				sMedShHeparinNum[rowidx] = (Double) ds.getFieldValue(MedShHeparinNumField);
				sMedShIIbIIIa[rowidx] = (String) ds.getFieldValue(MedShIIbIIIaField);
				sMedShIIbIIIaNum[rowidx] = (Double) ds.getFieldValue(MedShIIbIIIaNumField);
				sMedShIngen[rowidx] = (Double) ds.getFieldValue(MedShIngenField);
				sMedShInotropeMed[rowidx] = (String) ds.getFieldValue(MedShInotropeMedField);
				sMedShInotropeMedNum[rowidx] = (Double) ds.getFieldValue(MedShInotropeMedNumField);
				sMedShKvitamin[rowidx] = (String) ds.getFieldValue(MedShKvitaminField);
				sMedShKvitaminNum[rowidx] = (Double) ds.getFieldValue(MedShKvitaminNumField);
				sMedShPlatehemming[rowidx] = (String) ds.getFieldValue(MedShPlatehemmingField);
				sMedShPlatehemmingNum[rowidx] = (Double) ds.getFieldValue(MedShPlatehemmingNumField);
				sMedShTrombinhemmer[rowidx] = (String) ds.getFieldValue(MedShTrombinhemmerField);
				sMedShTrombinhemmerNum[rowidx] = (Double) ds.getFieldValue(MedShTrombinhemmerNumField);
				sMedUtACEhemmerA2AA[rowidx] = (String) ds.getFieldValue(MedUtACEhemmerA2AAField);
				sMedUtACEhemmerA2AANum[rowidx] = (Double) ds.getFieldValue(MedUtACEhemmerA2AANumField);
				sMedUtADP[rowidx] = (String) ds.getFieldValue(MedUtADPField);
				sMedUtADPNum[rowidx] = (Double) ds.getFieldValue(MedUtADPNumField);
				sMedUtAndreLipidsenkere[rowidx] = (String) ds.getFieldValue(MedUtAndreLipidsenkereField);
				sMedUtAndreLipidsenkereNum[rowidx] = (Double) ds.getFieldValue(MedUtAndreLipidsenkereNumField);
				sMedUtAnnenAntikoagulasjon[rowidx] = (String) ds.getFieldValue(MedUtAnnenAntikoagulasjonField);
				sMedUtAnnenAntikoagulasjonNum[rowidx] = (Double) ds.getFieldValue(MedUtAnnenAntikoagulasjonNumField);
				sMedUtAnnenPlatehemming[rowidx] = (String) ds.getFieldValue(MedUtAnnenPlatehemmingField);
				sMedUtAnnenPlatehemmingNum[rowidx] = (Double) ds.getFieldValue(MedUtAnnenPlatehemmingNumField);
				sMedUtAntikoagulasjon[rowidx] = (String) ds.getFieldValue(MedUtAntikoagulasjonField);
				sMedUtAntikoagulasjonNum[rowidx] = (Double) ds.getFieldValue(MedUtAntikoagulasjonNumField);
				sMedUtASA[rowidx] = (String) ds.getFieldValue(MedUtASAField);
				sMedUtASANum[rowidx] = (Double) ds.getFieldValue(MedUtASANumField);
				sMedUtBetablokker[rowidx] = (String) ds.getFieldValue(MedUtBetablokkerField);
				sMedUtBetablokkerNum[rowidx] = (Double) ds.getFieldValue(MedUtBetablokkerNumField);
				sMedUtDiuretika[rowidx] = (String) ds.getFieldValue(MedUtDiuretikaField);
				sMedUtDiuretikaNum[rowidx] = (Double) ds.getFieldValue(MedUtDiuretikaNumField);
				sMedUtHeparin[rowidx] = (String) ds.getFieldValue(MedUtHeparinField);
				sMedUtHeparinlavmol[rowidx] = (String) ds.getFieldValue(MedUtHeparinlavmolField);
				sMedUtHeparinlavmolNum[rowidx] = (Double) ds.getFieldValue(MedUtHeparinlavmolNumField);
				sMedUtIIbIIIa[rowidx] = (String) ds.getFieldValue(MedUtIIbIIIaField);
				sMedUtIngen[rowidx] = (Double) ds.getFieldValue(MedUtIngenField);
				sMedUtKvitamin[rowidx] = (String) ds.getFieldValue(MedUtKvitaminField);
				sMedUtKvitaminNum[rowidx] = (Double) ds.getFieldValue(MedUtKvitaminNumField);
				sMedUtPlatehemming[rowidx] = (String) ds.getFieldValue(MedUtPlatehemmingField);
				sMedUtPlatehemmingNum[rowidx] = (Double) ds.getFieldValue(MedUtPlatehemmingNumField);
				sMedUtStatin2[rowidx] = (String) ds.getFieldValue(MedUtStatin2Field);
				sMedUtStatin2Num[rowidx] = (Double) ds.getFieldValue(MedUtStatin2NumField);
				sMedUtTrombinhemmer[rowidx] = (String) ds.getFieldValue(MedUtTrombinhemmerField);
				sMedUtTrombinhemmerNum[rowidx] = (Double) ds.getFieldValue(MedUtTrombinhemmerNumField);
				sMorsdato[rowidx] = (String) ds.getFieldValue(MorsdatoField);
				sOppfUtreisedato[rowidx] = (String) ds.getFieldValue(OppfUtreisedatoField);
				sOrganisasjon[rowidx] = (String) ds.getFieldValue(OrganisasjonField);
				sOrgRESH[rowidx] = (Double) ds.getFieldValue(OrgRESHField);
				sOverflytningstid[rowidx] = (String) ds.getFieldValue(OverflytningstidField);
				sOverflyttetPasient[rowidx] = (String) ds.getFieldValue(OverflyttetPasientField);
				sOverflyttetPasientNum[rowidx] = (Double) ds.getFieldValue(OverflyttetPasientNumField);
				sPasientnummer[rowidx] = (String) ds.getFieldValue(PasientnummerField);
				sPatientInRegistryKey[rowidx] = (String) ds.getFieldValue(PatientInRegistryKeyField);
				sPCIsykehus[rowidx] = (Double) ds.getFieldValue(PCIsykehusField);
				sPostnr[rowidx] = (String) ds.getFieldValue(PostnrField);
				sPoststed[rowidx] = (String) ds.getFieldValue(PoststedField);
				sRegistreringsavd[rowidx] = (String) ds.getFieldValue(RegistreringsavdField);
				sReshID[rowidx] = (Double) ds.getFieldValue(ReshIDField);
				sResuscitering[rowidx] = (String) ds.getFieldValue(ResusciteringField);
				sResusciteringNum[rowidx] = (Double) ds.getFieldValue(ResusciteringNumField);
				sRHFnavn[rowidx] = (String) ds.getFieldValue(RHFnavnField);
				sRHFresh[rowidx] = (Double) ds.getFieldValue(RHFreshField);
				sRoeyker[rowidx] = (String) ds.getFieldValue(RoeykerField);
				sRoeykerNum[rowidx] = (Double) ds.getFieldValue(RoeykerNumField);
				sSivilstatus[rowidx] = (String) ds.getFieldValue(SivilstatusField);
				sSkjematype[rowidx] = (String) ds.getFieldValue(SkjematypeField);
				sSorteringsParameterVerdi[rowidx] = (String) ds.getFieldValue(SorteringsParameterVerdiField);
				sStatin[rowidx] = (String) ds.getFieldValue(StatinField);
				sStatinNum[rowidx] = (Double) ds.getFieldValue(StatinNumField);
				sSykehus[rowidx] = (String) ds.getFieldValue(SykehusField);
				sSymptomdebut[rowidx] = (String) ds.getFieldValue(SymptomdebutField);
				sTeknDataSettID[rowidx] = (Double) ds.getFieldValue(TeknDataSettIDField);
				sTeknFradato[rowidx] = (String) ds.getFieldValue(TeknFradatoField);
				sTeknKontaktFraDato[rowidx] = (String) ds.getFieldValue(TeknKontaktFraDatoField);
				sTeknKontaktID[rowidx] = (Double) ds.getFieldValue(TeknKontaktIDField);
				sTeknKontaktNavn[rowidx] = (String) ds.getFieldValue(TeknKontaktNavnField);
				sTeknKontaktTilDato[rowidx] = (String) ds.getFieldValue(TeknKontaktTilDatoField);
				sTeknOpprettetDato[rowidx] = (String) ds.getFieldValue(TeknOpprettetDatoField);
				sTeknRapportgrunnlagID[rowidx] = (Double) ds.getFieldValue(TeknRapportgrunnlagIDField);
				sTeknRelatedID[rowidx] = (Double) ds.getFieldValue(TeknRelatedIDField);
				sTeknSkjemaID[rowidx] = (Double) ds.getFieldValue(TeknSkjemaIDField);
				sTeknTildato[rowidx] = (String) ds.getFieldValue(TeknTildatoField);
				sTeknUpdated[rowidx] = (String) ds.getFieldValue(TeknUpdatedField);
				sTidlDiabetes[rowidx] = (String) ds.getFieldValue(TidlDiabetesField);
				sTidlDiabetesNum[rowidx] = (Double) ds.getFieldValue(TidlDiabetesNumField);
				sTidlFamiliaerOpphopning[rowidx] = (String) ds.getFieldValue(TidlFamiliaerOpphopningField);
				sTidlFamiliaerOpphopningNum[rowidx] = (Double) ds.getFieldValue(TidlFamiliaerOpphopningNumField);
				sTidlHjerneslag[rowidx] = (String) ds.getFieldValue(TidlHjerneslagField);
				sTidlHjerneslagNum[rowidx] = (Double) ds.getFieldValue(TidlHjerneslagNumField);
				sTidlHjerteinfarkt[rowidx] = (String) ds.getFieldValue(TidlHjerteinfarktField);
				sTidlHjerteinfarktNum[rowidx] = (Double) ds.getFieldValue(TidlHjerteinfarktNumField);
				sTidlHypertensjonsbeh[rowidx] = (String) ds.getFieldValue(TidlHypertensjonsbehField);
				sTidlHypertensjonsbehNum[rowidx] = (Double) ds.getFieldValue(TidlHypertensjonsbehNumField);
				sTidlIngen[rowidx] = (Double) ds.getFieldValue(TidlIngenField);
				sTidlKoronaroperert[rowidx] = (String) ds.getFieldValue(TidlKoronaroperertField);
				sTidlKoronaroperertNum[rowidx] = (Double) ds.getFieldValue(TidlKoronaroperertNumField);
				sTidlKroniskHjertesvikt[rowidx] = (String) ds.getFieldValue(TidlKroniskHjertesviktField);
				sTidlKroniskHjertesviktNum[rowidx] = (Double) ds.getFieldValue(TidlKroniskHjertesviktNumField);
				sTidlPCI[rowidx] = (String) ds.getFieldValue(TidlPCIField);
				sTidlPCINum[rowidx] = (Double) ds.getFieldValue(TidlPCINumField);
				sTidlPeriferVaskulaerSykdom[rowidx] = (String) ds.getFieldValue(TidlPeriferVaskulaerSykdomField);
				sTidlPeriferVaskulaerSykdomNum[rowidx] = (Double) ds.getFieldValue(TidlPeriferVaskulaerSykdomNumField);
				sTiltakCPAPBiPAP[rowidx] = (String) ds.getFieldValue(TiltakCPAPBiPAPField);
				sTiltakCPAPBiPAPNum[rowidx] = (Double) ds.getFieldValue(TiltakCPAPBiPAPNumField);
				sTiltakHypotermibeh[rowidx] = (String) ds.getFieldValue(TiltakHypotermibehField);
				sTiltakHypotermibehNum[rowidx] = (Double) ds.getFieldValue(TiltakHypotermibehNumField);
				sTiltakIABP[rowidx] = (String) ds.getFieldValue(TiltakIABPField);
				sTiltakIABPNum[rowidx] = (Double) ds.getFieldValue(TiltakIABPNumField);
				sTiltakIngen[rowidx] = (Double) ds.getFieldValue(TiltakIngenField);
				sTiltakPMICD[rowidx] = (String) ds.getFieldValue(TiltakPMICDField);
				sTiltakPMICDNum[rowidx] = (Double) ds.getFieldValue(TiltakPMICDNumField);
				sTiltakRespirator[rowidx] = (String) ds.getFieldValue(TiltakRespiratorField);
				sTiltakRespiratorNum[rowidx] = (Double) ds.getFieldValue(TiltakRespiratorNumField);
				sTrombolysebeh[rowidx] = (String) ds.getFieldValue(TrombolysebehField);
				sTrombolysebehNum[rowidx] = (Double) ds.getFieldValue(TrombolysebehNumField);
				sTrombolysetid[rowidx] = (String) ds.getFieldValue(TrombolysetidField);
				sTroponingStigningFall[rowidx] = (String) ds.getFieldValue(TroponingStigningFallField);
				sTroponingStigningFalNum[rowidx] = (Double) ds.getFieldValue(TroponingStigningFalNumField);
				sTypeInfarkt[rowidx] = (String) ds.getFieldValue(TypeInfarktField);
				sTypeInfarktTekst[rowidx] = (String) ds.getFieldValue(TypeInfarktTekstField);
				sUtreisedato[rowidx] = (String) ds.getFieldValue(UtreisedatoField);
				sUtskrivesTil2[rowidx] = (String) ds.getFieldValue(UtskrivesTil2Field);
				sUtskrivesTil2Num[rowidx] = (Double) ds.getFieldValue(UtskrivesTil2NumField);
				sVisInnkomstvariabler[rowidx] = (Double) ds.getFieldValue(VisInnkomstvariablerField);
				sYrke[rowidx] = (String) ds.getFieldValue(YrkeField);
				sDatasettGuid[rowidx] = (String) ds.getFieldValue(DatasettGuidField);
				getRow = ds.next();
				rowidx++;
			}
			rowidx--;

			
			log.debug("Slug array filled with " + rowidx + " records from report data");

			
			log.debug("Creating proper sized array...");
			// Create and populate properly sized arrays


			double[] Alder = new double[rowidx + 1];
			String[] Avdeling = new String[rowidx + 1];
			double[] AvdelingNum = new double[rowidx + 1];
			String[] AvdNavn = new String[rowidx + 1];
			double[] BMI = new double[rowidx + 1];
			double[] BMIHoyde = new double[rowidx + 1];
			double[] BMIUkjent = new double[rowidx + 1];
			double[] BMIVekt = new double[rowidx + 1];
			double[] BTPOvertrykk = new double[rowidx + 1];
			double[] BTPPuls = new double[rowidx + 1];
			double[] BTPUkjent = new double[rowidx + 1];
			double[] BTPUndertrykk = new double[rowidx + 1];
			String[] DoedUnderOpphold = new String[rowidx + 1];
			double[] DoedUnderOppholdNum = new double[rowidx + 1];
			String[] EKGDiagnostisk = new String[rowidx + 1];
			double[] EKGDiagnostiskNum = new double[rowidx + 1];
			String[] EKGNyQbolge = new String[rowidx + 1];
			double[] EKGNyQbolgeNum = new double[rowidx + 1];
			String[] EKGRytme = new String[rowidx + 1];
			double[] EKGRytmeNum = new double[rowidx + 1];
			String[] EKGSteminStemi = new String[rowidx + 1];
			double[] EKGSteminStemiNum = new double[rowidx + 1];
			String[] EkkoEF = new String[rowidx + 1];
			double[] EkkoEfNum = new double[rowidx + 1];
			String[] EkkoValg = new String[rowidx + 1];
			double[] EkkoValgNum = new double[rowidx + 1];
			String[] PasientID = new String[rowidx + 1];
			String[] Foedselsaar = new String[rowidx + 1];
			String[] FraHvilkenRegion = new String[rowidx + 1];
			double[] FraHvilkenRegionNum = new double[rowidx + 1];
			String[] FraHvilketSykehus2 = new String[rowidx + 1];
			String[] HvorBefantPasientenSeg = new String[rowidx + 1];
			double[] HvorBefantPasientenSegNum = new double[rowidx + 1];
			String[] Infarktlokalisasjon = new String[rowidx + 1];
			double[] InfarktlokalisasjonNum = new double[rowidx + 1];
			String[] Innleggelsestidspunkt = new String[rowidx + 1];
			String[] Innleggelsestidspunkt2 = new String[rowidx + 1];
			String[] InvAntSykekar = new String[rowidx + 1];
			String[] InvAntSykekarTekst = new String[rowidx + 1];
			String[] InvKunKorAngio = new String[rowidx + 1];
			double[] InvKunKorAngioNum = new double[rowidx + 1];
			String[] InvKunKorAngioPCITid = new String[rowidx + 1];
			String[] InvKunKorAngioTid = new String[rowidx + 1];
			String[] InvPCIunderoppholdet = new String[rowidx + 1];
			double[] InvPCIunderoppholdetNum = new double[rowidx + 1];
			String[] InvStent = new String[rowidx + 1];
			String[] InvStentTekst = new String[rowidx + 1];
			String[] InvStentType = new String[rowidx + 1];
			String[] InvStentTypeTekst = new String[rowidx + 1];
			String[] IvnPCITid = new String[rowidx + 1];
			String[] Kjoenn = new String[rowidx + 1];
			String[] Kommunenr = new String[rowidx + 1];
			String[] KomplAtrieflimmerFlutter = new String[rowidx + 1];
			double[] KomplAtrieflimmerFlutterNum = new double[rowidx + 1];
			String[] KomplAVblokk2el3 = new String[rowidx + 1];
			double[] KomplAVblokk2el3Num = new double[rowidx + 1];
			String[] KomplBloedAnnen = new String[rowidx + 1];
			double[] KomplBloedAnnenNum = new double[rowidx + 1];
			String[] KomplBloedCerebral = new String[rowidx + 1];
			double[] KomplBloedCerebralNum = new double[rowidx + 1];
			String[] KomplBloedGI = new String[rowidx + 1];
			double[] KomplBloedGINum = new double[rowidx + 1];
			String[] KomplBloedInnstikk = new String[rowidx + 1];
			double[] KomplBloedInnstikkNum = new double[rowidx + 1];
			String[] KomplBloedning = new String[rowidx + 1];
			double[] KomplBloedningNum = new double[rowidx + 1];
			String[] KomplHjerneslag = new String[rowidx + 1];
			double[] KomplHjerneslagNum = new double[rowidx + 1];
			String[] KomplHjertesvikt = new String[rowidx + 1];
			double[] KomplHjertesviktNum = new double[rowidx + 1];
			double[] KomplIngen = new double[rowidx + 1];
			String[] KomplKardiogentSjokk = new String[rowidx + 1];
			double[] KomplKardiogentSjokkNum = new double[rowidx + 1];
			String[] KomplMekaniskKomplikasjon = new String[rowidx + 1];
			double[] KomplMekaniskKomplikasjonNum = new double[rowidx + 1];
			String[] KomplReinfarkt = new String[rowidx + 1];
			double[] KomplReinfarktNum = new double[rowidx + 1];
			double[] KomplVTVFe48Num = new double[rowidx + 1];
			String[] KomplVTVFe48t = new String[rowidx + 1];
			String[] KomplVTVFf48t = new String[rowidx + 1];
			double[] KomplVTVFf48tNum = new double[rowidx + 1];
			String[] KoronarProsedyreEtPlan = new String[rowidx + 1];
			double[] KoronarProsedyreEtPlanNum = new double[rowidx + 1];
			String[] LabGlukose = new String[rowidx + 1];
			String[] LabHbA1C = new String[rowidx + 1];
			String[] LabHDL = new String[rowidx + 1];
			String[] LabInfarktmarkoer = new String[rowidx + 1];
			double[] LabInfarktmarkoererNum = new double[rowidx + 1];
			String[] LabInfarktmarkorHoy = new String[rowidx + 1];
			String[] LabInfarktmarkorLav = new String[rowidx + 1];
			String[] LabKreatinin = new String[rowidx + 1];
			String[] LabLDL = new String[rowidx + 1];
			String[] LabTotalkolesterol = new String[rowidx + 1];
			String[] LabTriglyserider = new String[rowidx + 1];
			double[] Liggetid = new double[rowidx + 1];
			String[] LokalRegion = new String[rowidx + 1];
			String[] LokalRegion2 = new String[rowidx + 1];
			double[] LokalRegionNum = new double[rowidx + 1];
			String[] MedInnACEhemmerAII = new String[rowidx + 1];
			double[] MedInnACEhemmerAIINum = new double[rowidx + 1];
			String[] MedInnADP = new String[rowidx + 1];
			double[] MedInnADPNum = new double[rowidx + 1];
			String[] MedInnAnnenPlatehemming = new String[rowidx + 1];
			double[] MedInnAnnenPlatehemmingNum = new double[rowidx + 1];
			String[] MedInnAntikoagulasjon = new String[rowidx + 1];
			double[] MedInnAntikoagulasjonNum = new double[rowidx + 1];
			String[] MedInnASA = new String[rowidx + 1];
			double[] MedInnASANum = new double[rowidx + 1];
			String[] MedInnBetablokker = new String[rowidx + 1];
			double[] MedInnBetablokkerNum = new double[rowidx + 1];
			String[] MedInnDiuretika = new String[rowidx + 1];
			double[] MedInnDiuretikaNum = new double[rowidx + 1];
			double[] MedInnIngen = new double[rowidx + 1];
			String[] MedInnLipidsenkere = new String[rowidx + 1];
			double[] MedInnLipidsenkereNum = new double[rowidx + 1];
			String[] MedInnplatehemming = new String[rowidx + 1];
			double[] MedInnplatehemmingNum = new double[rowidx + 1];
			String[] MedShACEhemmerAII = new String[rowidx + 1];
			double[] MedShACEhemmerAIINum = new double[rowidx + 1];
			String[] MedShADP = new String[rowidx + 1];
			double[] MedShADPNum = new double[rowidx + 1];
			String[] MedShAndreAntiarytmika = new String[rowidx + 1];
			double[] MedShAndreAntiarytmikaNum = new double[rowidx + 1];
			String[] MedShAnnenAntikoagulasjon = new String[rowidx + 1];
			double[] MedShAnnenAntikoagulasjonNum = new double[rowidx + 1];
			String[] MedShAnnenPlatehemming = new String[rowidx + 1];
			double[] MedShAnnenPlatehemmingNum = new double[rowidx + 1];
			String[] MedShAntikoagulasjon = new String[rowidx + 1];
			double[] MedShAntikoagulasjonNum = new double[rowidx + 1];
			String[] MedShASA = new String[rowidx + 1];
			double[] MedShASANum = new double[rowidx + 1];
			String[] MedShBetablokker = new String[rowidx + 1];
			double[] MedShBetablokkerNum = new double[rowidx + 1];
			String[] MedShDiuretika = new String[rowidx + 1];
			double[] MedShDiuretikaNum = new double[rowidx + 1];
			String[] MedShHeparin = new String[rowidx + 1];
			double[] MedShHeparinNum = new double[rowidx + 1];
			String[] MedShIIbIIIa = new String[rowidx + 1];
			double[] MedShIIbIIIaNum = new double[rowidx + 1];
			double[] MedShIngen = new double[rowidx + 1];
			String[] MedShInotropeMed = new String[rowidx + 1];
			double[] MedShInotropeMedNum = new double[rowidx + 1];
			String[] MedShKvitamin = new String[rowidx + 1];
			double[] MedShKvitaminNum = new double[rowidx + 1];
			String[] MedShPlatehemming = new String[rowidx + 1];
			double[] MedShPlatehemmingNum = new double[rowidx + 1];
			String[] MedShTrombinhemmer = new String[rowidx + 1];
			double[] MedShTrombinhemmerNum = new double[rowidx + 1];
			String[] MedUtACEhemmerA2AA = new String[rowidx + 1];
			double[] MedUtACEhemmerA2AANum = new double[rowidx + 1];
			String[] MedUtADP = new String[rowidx + 1];
			double[] MedUtADPNum = new double[rowidx + 1];
			String[] MedUtAndreLipidsenkere = new String[rowidx + 1];
			double[] MedUtAndreLipidsenkereNum = new double[rowidx + 1];
			String[] MedUtAnnenAntikoagulasjon = new String[rowidx + 1];
			double[] MedUtAnnenAntikoagulasjonNum = new double[rowidx + 1];
			String[] MedUtAnnenPlatehemming = new String[rowidx + 1];
			double[] MedUtAnnenPlatehemmingNum = new double[rowidx + 1];
			String[] MedUtAntikoagulasjon = new String[rowidx + 1];
			double[] MedUtAntikoagulasjonNum = new double[rowidx + 1];
			String[] MedUtASA = new String[rowidx + 1];
			double[] MedUtASANum = new double[rowidx + 1];
			String[] MedUtBetablokker = new String[rowidx + 1];
			double[] MedUtBetablokkerNum = new double[rowidx + 1];
			String[] MedUtDiuretika = new String[rowidx + 1];
			double[] MedUtDiuretikaNum = new double[rowidx + 1];
			String[] MedUtHeparin = new String[rowidx + 1];
			String[] MedUtHeparinlavmol = new String[rowidx + 1];
			double[] MedUtHeparinlavmolNum = new double[rowidx + 1];
			String[] MedUtIIbIIIa = new String[rowidx + 1];
			double[] MedUtIngen = new double[rowidx + 1];
			String[] MedUtKvitamin = new String[rowidx + 1];
			double[] MedUtKvitaminNum = new double[rowidx + 1];
			String[] MedUtPlatehemming = new String[rowidx + 1];
			double[] MedUtPlatehemmingNum = new double[rowidx + 1];
			String[] MedUtStatin2 = new String[rowidx + 1];
			double[] MedUtStatin2Num = new double[rowidx + 1];
			String[] MedUtTrombinhemmer = new String[rowidx + 1];
			double[] MedUtTrombinhemmerNum = new double[rowidx + 1];
			String[] Morsdato = new String[rowidx + 1];
			String[] OppfUtreisedato = new String[rowidx + 1];
			String[] Organisasjon = new String[rowidx + 1];
			double[] OrgRESH = new double[rowidx + 1];
			String[] Overflytningstid = new String[rowidx + 1];
			String[] OverflyttetPasient = new String[rowidx + 1];
			double[] OverflyttetPasientNum = new double[rowidx + 1];
			String[] Pasientnummer = new String[rowidx + 1];
			String[] PatientInRegistryKey = new String[rowidx + 1];
			double[] PCIsykehus = new double[rowidx + 1];
			String[] Postnr = new String[rowidx + 1];
			String[] Poststed = new String[rowidx + 1];
			String[] Registreringsavd = new String[rowidx + 1];
			double[] ReshID = new double[rowidx + 1];
			String[] Resuscitering = new String[rowidx + 1];
			double[] ResusciteringNum = new double[rowidx + 1];
			String[] RHFnavn = new String[rowidx + 1];
			double[] RHFresh = new double[rowidx + 1];
			String[] Roeyker = new String[rowidx + 1];
			double[] RoeykerNum = new double[rowidx + 1];
			String[] Sivilstatus = new String[rowidx + 1];
			String[] Skjematype = new String[rowidx + 1];
			String[] SorteringsParameterVerdi = new String[rowidx + 1];
			String[] Statin = new String[rowidx + 1];
			double[] StatinNum = new double[rowidx + 1];
			String[] Sykehus = new String[rowidx + 1];
			String[] Symptomdebut = new String[rowidx + 1];
			double[] TeknDataSettID = new double[rowidx + 1];
			String[] TeknFradato = new String[rowidx + 1];
			String[] TeknKontaktFraDato = new String[rowidx + 1];
			double[] TeknKontaktID = new double[rowidx + 1];
			String[] TeknKontaktNavn = new String[rowidx + 1];
			String[] TeknKontaktTilDato = new String[rowidx + 1];
			String[] TeknOpprettetDato = new String[rowidx + 1];
			double[] TeknRapportgrunnlagID = new double[rowidx + 1];
			double[] TeknRelatedID = new double[rowidx + 1];
			double[] TeknSkjemaID = new double[rowidx + 1];
			String[] TeknTildato = new String[rowidx + 1];
			String[] TeknUpdated = new String[rowidx + 1];
			String[] TidlDiabetes = new String[rowidx + 1];
			double[] TidlDiabetesNum = new double[rowidx + 1];
			String[] TidlFamiliaerOpphopning = new String[rowidx + 1];
			double[] TidlFamiliaerOpphopningNum = new double[rowidx + 1];
			String[] TidlHjerneslag = new String[rowidx + 1];
			double[] TidlHjerneslagNum = new double[rowidx + 1];
			String[] TidlHjerteinfarkt = new String[rowidx + 1];
			double[] TidlHjerteinfarktNum = new double[rowidx + 1];
			String[] TidlHypertensjonsbeh = new String[rowidx + 1];
			double[] TidlHypertensjonsbehNum = new double[rowidx + 1];
			double[] TidlIngen = new double[rowidx + 1];
			String[] TidlKoronaroperert = new String[rowidx + 1];
			double[] TidlKoronaroperertNum = new double[rowidx + 1];
			String[] TidlKroniskHjertesvikt = new String[rowidx + 1];
			double[] TidlKroniskHjertesviktNum = new double[rowidx + 1];
			String[] TidlPCI = new String[rowidx + 1];
			double[] TidlPCINum = new double[rowidx + 1];
			String[] TidlPeriferVaskulaerSykdom = new String[rowidx + 1];
			double[] TidlPeriferVaskulaerSykdomNum = new double[rowidx + 1];
			String[] TiltakCPAPBiPAP = new String[rowidx + 1];
			double[] TiltakCPAPBiPAPNum = new double[rowidx + 1];
			String[] TiltakHypotermibeh = new String[rowidx + 1];
			double[] TiltakHypotermibehNum = new double[rowidx + 1];
			String[] TiltakIABP = new String[rowidx + 1];
			double[] TiltakIABPNum = new double[rowidx + 1];
			double[] TiltakIngen = new double[rowidx + 1];
			String[] TiltakPMICD = new String[rowidx + 1];
			double[] TiltakPMICDNum = new double[rowidx + 1];
			String[] TiltakRespirator = new String[rowidx + 1];
			double[] TiltakRespiratorNum = new double[rowidx + 1];
			String[] Trombolysebeh = new String[rowidx + 1];
			double[] TrombolysebehNum = new double[rowidx + 1];
			String[] Trombolysetid = new String[rowidx + 1];
			String[] TroponingStigningFall = new String[rowidx + 1];
			double[] TroponingStigningFalNum = new double[rowidx + 1];
			String[] TypeInfarkt = new String[rowidx + 1];
			String[] TypeInfarktTekst = new String[rowidx + 1];
			String[] Utreisedato = new String[rowidx + 1];
			String[] UtskrivesTil2 = new String[rowidx + 1];
			double[] UtskrivesTil2Num = new double[rowidx + 1];
			double[] VisInnkomstvariabler = new double[rowidx + 1];
			String[] Yrke = new String[rowidx + 1];
			String[] DatasettGuid = new String [rowidx + 1];




			
			// ifs are needed because underlying query returns null. Since ints
			// cannot be null, these are returned as type double by the query
			log.debug("Populating proper sized array with data from slug array, also checking for NULLs...");
			int i = 0;
			while (i <= rowidx) {
				if (sAlder[i] == null) {
					Alder[i] = java.lang.Double.NaN;
				}
				else {
					Alder[i] = sAlder[i];
				}

				Avdeling[i] = sAvdeling[i];
				if (sAvdelingNum[i] == null) {
					AvdelingNum[i] = java.lang.Double.NaN;
				}
				else {
					AvdelingNum[i] = sAvdelingNum[i];
				}

				AvdNavn[i] = sAvdNavn[i];
				if (sBMI[i] == null) {
					BMI[i] = java.lang.Double.NaN;
				}
				else {
					BMI[i] = sBMI[i];
				}
				if (sBMIHoyde[i] == null) {
					BMIHoyde[i] = java.lang.Double.NaN;
				}
				else {
					BMIHoyde[i] = sBMIHoyde[i];
				}
				if (sBMIUkjent[i] == null) {
					BMIUkjent[i] = java.lang.Double.NaN;
				}
				else {
					BMIUkjent[i] = sBMIUkjent[i];
				}
				if (sBMIVekt[i] == null) {
					BMIVekt[i] = java.lang.Double.NaN;
				}
				else {
					BMIVekt[i] = sBMIVekt[i];
				}
				if (sBTPOvertrykk[i] == null) {
					BTPOvertrykk[i] = java.lang.Double.NaN;
				}
				else {
					BTPOvertrykk[i] = sBTPOvertrykk[i];
				}

				if (sBTPPuls[i] == null) {
					BTPPuls[i] = java.lang.Double.NaN;
				}
				else {
					BTPPuls[i] = sBTPPuls[i];
				}

				if (sBTPUkjent[i] == null) {
					BTPUkjent[i] = java.lang.Double.NaN;
				}
				else {
					BTPUkjent[i] = sBTPUkjent[i];
				}

				if (sBTPUndertrykk[i] == null) {
					BTPUndertrykk[i] = java.lang.Double.NaN;
				}
				else {
					BTPUndertrykk[i] = sBTPUndertrykk[i];
				}

				DoedUnderOpphold[i] = sDoedUnderOpphold[i];
				if (sDoedUnderOppholdNum[i] == null) {
					DoedUnderOppholdNum[i] = java.lang.Double.NaN;
				}
				else {
					DoedUnderOppholdNum[i] = sDoedUnderOppholdNum[i];
				}

				EKGDiagnostisk[i] = sEKGDiagnostisk[i];
				if (sEKGDiagnostiskNum[i] == null) {
					EKGDiagnostiskNum[i] = java.lang.Double.NaN;
				}
				else {
					EKGDiagnostiskNum[i] = sEKGDiagnostiskNum[i];
				}

				EKGNyQbolge[i] = sEKGNyQbolge[i];
				if (sEKGNyQbolgeNum[i] == null) {
					EKGNyQbolgeNum[i] = java.lang.Double.NaN;
				}
				else {
					EKGNyQbolgeNum[i] = sEKGNyQbolgeNum[i];
				}

				EKGRytme[i] = sEKGRytme[i];
				if (sEKGRytmeNum[i] == null) {
					EKGRytmeNum[i] = java.lang.Double.NaN;
				}
				else {
					EKGRytmeNum[i] = sEKGRytmeNum[i];
				}

				EKGSteminStemi[i] = sEKGSteminStemi[i];
				if (sEKGSteminStemiNum[i] == null) {
					EKGSteminStemiNum[i] = java.lang.Double.NaN;
				}
				else {
					EKGSteminStemiNum[i] = sEKGSteminStemiNum[i];
				}

				EkkoEF[i] = sEkkoEF[i];
				if (sEkkoEfNum[i] == null) {
					EkkoEfNum[i] = java.lang.Double.NaN;
				}
				else {
					EkkoEfNum[i] = sEkkoEfNum[i];
				}

				EkkoValg[i] = sEkkoValg[i];
				if (sEkkoValgNum[i] == null) {
					EkkoValgNum[i] = java.lang.Double.NaN;
				}
				else {
					EkkoValgNum[i] = sEkkoValgNum[i];
				}

				PasientID[i] = sPasientID[i];
				Foedselsaar[i] = sFoedselsaar[i];
				FraHvilkenRegion[i] = sFraHvilkenRegion[i];
				if (sFraHvilkenRegionNum[i] == null) {
					FraHvilkenRegionNum[i] = java.lang.Double.NaN;
				}
				else {
					FraHvilkenRegionNum[i] = sFraHvilkenRegionNum[i];
				}

				FraHvilketSykehus2[i] = sFraHvilketSykehus2[i];
				HvorBefantPasientenSeg[i] = sHvorBefantPasientenSeg[i];
				if (sHvorBefantPasientenSegNum[i] == null) {
					HvorBefantPasientenSegNum[i] = java.lang.Double.NaN;
				}
				else {
					HvorBefantPasientenSegNum[i] = sHvorBefantPasientenSegNum[i];
				}

				Infarktlokalisasjon[i] = sInfarktlokalisasjon[i];
				if (sInfarktlokalisasjonNum[i] == null) {
					InfarktlokalisasjonNum[i] = java.lang.Double.NaN;
				}
				else {
					InfarktlokalisasjonNum[i] = sInfarktlokalisasjonNum[i];
				}

				Innleggelsestidspunkt[i] = sInnleggelsestidspunkt[i];
				Innleggelsestidspunkt2[i] = sInnleggelsestidspunkt2[i];
				InvAntSykekar[i] = sInvAntSykekar[i];
				InvAntSykekarTekst[i] = sInvAntSykekarTekst[i];
				InvKunKorAngio[i] = sInvKunKorAngio[i];
				if (sInvKunKorAngioNum[i] == null) {
					InvKunKorAngioNum[i] = java.lang.Double.NaN;
				}
				else {
					InvKunKorAngioNum[i] = sInvKunKorAngioNum[i];
				}

				InvKunKorAngioPCITid[i] = sInvKunKorAngioPCITid[i];
				InvKunKorAngioTid[i] = sInvKunKorAngioTid[i];
				InvPCIunderoppholdet[i] = sInvPCIunderoppholdet[i];
				if (sInvPCIunderoppholdetNum[i] == null) {
					InvPCIunderoppholdetNum[i] = java.lang.Double.NaN;
				}
				else {
					InvPCIunderoppholdetNum[i] = sInvPCIunderoppholdetNum[i];
				}

				InvStent[i] = sInvStent[i];
				InvStentTekst[i] = sInvStentTekst[i];
				InvStentType[i] = sInvStentType[i];
				InvStentTypeTekst[i] = sInvStentTypeTekst[i];
				IvnPCITid[i] = sIvnPCITid[i];
				Kjoenn[i] = sKjoenn[i];
				Kommunenr[i] = sKommunenr[i];
				KomplAtrieflimmerFlutter[i] = sKomplAtrieflimmerFlutter[i];
				if (sKomplAtrieflimmerFlutterNum[i] == null) {
					KomplAtrieflimmerFlutterNum[i] = java.lang.Double.NaN;
				}
				else {
					KomplAtrieflimmerFlutterNum[i] = sKomplAtrieflimmerFlutterNum[i];
				}

				KomplAVblokk2el3[i] = sKomplAVblokk2el3[i];
				if (sKomplAVblokk2el3Num[i] == null) {
					KomplAVblokk2el3Num[i] = java.lang.Double.NaN;
				}
				else {
					KomplAVblokk2el3Num[i] = sKomplAVblokk2el3Num[i];
				}

				KomplBloedAnnen[i] = sKomplBloedAnnen[i];
				if (sKomplBloedAnnenNum[i] == null) {
					KomplBloedAnnenNum[i] = java.lang.Double.NaN;
				}
				else {
					KomplBloedAnnenNum[i] = sKomplBloedAnnenNum[i];
				}

				KomplBloedCerebral[i] = sKomplBloedCerebral[i];
				if (sKomplBloedCerebralNum[i] == null) {
					KomplBloedCerebralNum[i] = java.lang.Double.NaN;
				}
				else {
					KomplBloedCerebralNum[i] = sKomplBloedCerebralNum[i];
				}

				KomplBloedGI[i] = sKomplBloedGI[i];
				if (sKomplBloedGINum[i] == null) {
					KomplBloedGINum[i] = java.lang.Double.NaN;
				}
				else {
					KomplBloedGINum[i] = sKomplBloedGINum[i];
				}

				KomplBloedInnstikk[i] = sKomplBloedInnstikk[i];
				if (sKomplBloedInnstikkNum[i] == null) {
					KomplBloedInnstikkNum[i] = java.lang.Double.NaN;
				}
				else {
					KomplBloedInnstikkNum[i] = sKomplBloedInnstikkNum[i];
				}

				KomplBloedning[i] = sKomplBloedning[i];
				if (sKomplBloedningNum[i] == null) {
					KomplBloedningNum[i] = java.lang.Double.NaN;
				}
				else {
					KomplBloedningNum[i] = sKomplBloedningNum[i];
				}

				KomplHjerneslag[i] = sKomplHjerneslag[i];
				if (sKomplHjerneslagNum[i] == null) {
					KomplHjerneslagNum[i] = java.lang.Double.NaN;
				}
				else {
					KomplHjerneslagNum[i] = sKomplHjerneslagNum[i];
				}

				KomplHjertesvikt[i] = sKomplHjertesvikt[i];
				if (sKomplHjertesviktNum[i] == null) {
					KomplHjertesviktNum[i] = java.lang.Double.NaN;
				}
				else {
					KomplHjertesviktNum[i] = sKomplHjertesviktNum[i];
				}

				if (sKomplIngen[i] == null) {
					KomplIngen[i] = java.lang.Double.NaN;
				}
				else {
					KomplIngen[i] = sKomplIngen[i];
				}

				KomplKardiogentSjokk[i] = sKomplKardiogentSjokk[i];
				if (sKomplKardiogentSjokkNum[i] == null) {
					KomplKardiogentSjokkNum[i] = java.lang.Double.NaN;
				}
				else {
					KomplKardiogentSjokkNum[i] = sKomplKardiogentSjokkNum[i];
				}

				KomplMekaniskKomplikasjon[i] = sKomplMekaniskKomplikasjon[i];
				if (sKomplMekaniskKomplikasjonNum[i] == null) {
					KomplMekaniskKomplikasjonNum[i] = java.lang.Double.NaN;
				}
				else {
					KomplMekaniskKomplikasjonNum[i] = sKomplMekaniskKomplikasjonNum[i];
				}

				KomplReinfarkt[i] = sKomplReinfarkt[i];
				if (sKomplReinfarktNum[i] == null) {
					KomplReinfarktNum[i] = java.lang.Double.NaN;
				}
				else {
					KomplReinfarktNum[i] = sKomplReinfarktNum[i];
				}

				if (sKomplVTVFe48Num[i] == null) {
					KomplVTVFe48Num[i] = java.lang.Double.NaN;
				}
				else {
					KomplVTVFe48Num[i] = sKomplVTVFe48Num[i];
				}

				KomplVTVFe48t[i] = sKomplVTVFe48t[i];
				KomplVTVFf48t[i] = sKomplVTVFf48t[i];
				if (sKomplVTVFf48tNum[i] == null) {
					KomplVTVFf48tNum[i] = java.lang.Double.NaN;
				}
				else {
					KomplVTVFf48tNum[i] = sKomplVTVFf48tNum[i];
				}

				KoronarProsedyreEtPlan[i] = sKoronarProsedyreEtPlan[i];
				if (sKoronarProsedyreEtPlanNum[i] == null) {
					KoronarProsedyreEtPlanNum[i] = java.lang.Double.NaN;
				}
				else {
					KoronarProsedyreEtPlanNum[i] = sKoronarProsedyreEtPlanNum[i];
				}

				LabGlukose[i] = sLabGlukose[i];
				LabHbA1C[i] = sLabHbA1C[i];
				LabHDL[i] = sLabHDL[i];
				LabInfarktmarkoer[i] = sLabInfarktmarkoer[i];
				if (sLabInfarktmarkoererNum[i] == null) {
					LabInfarktmarkoererNum[i] = java.lang.Double.NaN;
				}
				else {
					LabInfarktmarkoererNum[i] = sLabInfarktmarkoererNum[i];
				}

				LabInfarktmarkorHoy[i] = sLabInfarktmarkorHoy[i];
				LabInfarktmarkorLav[i] = sLabInfarktmarkorLav[i];
				LabKreatinin[i] = sLabKreatinin[i];
				LabLDL[i] = sLabLDL[i];
				LabTotalkolesterol[i] = sLabTotalkolesterol[i];
				LabTriglyserider[i] = sLabTriglyserider[i];
				if (sLiggetid[i] == null) {
					Liggetid[i] = java.lang.Double.NaN;
				}
				else {
					Liggetid[i] = sLiggetid[i];
				}

				LokalRegion[i] = sLokalRegion[i];
				LokalRegion2[i] = sLokalRegion2[i];
				if (sLokalRegionNum[i] == null) {
					LokalRegionNum[i] = java.lang.Double.NaN;
				}
				else {
					LokalRegionNum[i] = sLokalRegionNum[i];
				}

				MedInnACEhemmerAII[i] = sMedInnACEhemmerAII[i];
				if (sMedInnACEhemmerAIINum[i] == null) {
					MedInnACEhemmerAIINum[i] = java.lang.Double.NaN;
				}
				else {
					MedInnACEhemmerAIINum[i] = sMedInnACEhemmerAIINum[i];
				}

				MedInnADP[i] = sMedInnADP[i];
				if (sMedInnADPNum[i] == null) {
					MedInnADPNum[i] = java.lang.Double.NaN;
				}
				else {
					MedInnADPNum[i] = sMedInnADPNum[i];
				}

				MedInnAnnenPlatehemming[i] = sMedInnAnnenPlatehemming[i];
				if (sMedInnAnnenPlatehemmingNum[i] == null) {
					MedInnAnnenPlatehemmingNum[i] = java.lang.Double.NaN;
				}
				else {
					MedInnAnnenPlatehemmingNum[i] = sMedInnAnnenPlatehemmingNum[i];
				}

				MedInnAntikoagulasjon[i] = sMedInnAntikoagulasjon[i];
				if (sMedInnAntikoagulasjonNum[i] == null) {
					MedInnAntikoagulasjonNum[i] = java.lang.Double.NaN;
				}
				else {
					MedInnAntikoagulasjonNum[i] = sMedInnAntikoagulasjonNum[i];
				}

				MedInnASA[i] = sMedInnASA[i];
				if (sMedInnASANum[i] == null) {
					MedInnASANum[i] = java.lang.Double.NaN;
				}
				else {
					MedInnASANum[i] = sMedInnASANum[i];
				}

				MedInnBetablokker[i] = sMedInnBetablokker[i];
				if (sMedInnBetablokkerNum[i] == null) {
					MedInnBetablokkerNum[i] = java.lang.Double.NaN;
				}
				else {
					MedInnBetablokkerNum[i] = sMedInnBetablokkerNum[i];
				}

				MedInnDiuretika[i] = sMedInnDiuretika[i];
				if (sMedInnDiuretikaNum[i] == null) {
					MedInnDiuretikaNum[i] = java.lang.Double.NaN;
				}
				else {
					MedInnDiuretikaNum[i] = sMedInnDiuretikaNum[i];
				}

				if (sMedInnIngen[i] == null) {
					MedInnIngen[i] = java.lang.Double.NaN;
				}
				else {
					MedInnIngen[i] = sMedInnIngen[i];
				}

				MedInnLipidsenkere[i] = sMedInnLipidsenkere[i];
				if (sMedInnLipidsenkereNum[i] == null) {
					MedInnLipidsenkereNum[i] = java.lang.Double.NaN;
				}
				else {
					MedInnLipidsenkereNum[i] = sMedInnLipidsenkereNum[i];
				}

				MedInnplatehemming[i] = sMedInnplatehemming[i];
				if (sMedInnplatehemmingNum[i] == null) {
					MedInnplatehemmingNum[i] = java.lang.Double.NaN;
				}
				else {
					MedInnplatehemmingNum[i] = sMedInnplatehemmingNum[i];
				}

				MedShACEhemmerAII[i] = sMedShACEhemmerAII[i];
				if (sMedShACEhemmerAIINum[i] == null) {
					MedShACEhemmerAIINum[i] = java.lang.Double.NaN;
				}
				else {
					MedShACEhemmerAIINum[i] = sMedShACEhemmerAIINum[i];
				}

				MedShADP[i] = sMedShADP[i];
				if (sMedShADPNum[i] == null) {
					MedShADPNum[i] = java.lang.Double.NaN;
				}
				else {
					MedShADPNum[i] = sMedShADPNum[i];
				}

				MedShAndreAntiarytmika[i] = sMedShAndreAntiarytmika[i];
				if (sMedShAndreAntiarytmikaNum[i] == null) {
					MedShAndreAntiarytmikaNum[i] = java.lang.Double.NaN;
				}
				else {
					MedShAndreAntiarytmikaNum[i] = sMedShAndreAntiarytmikaNum[i];
				}

				MedShAnnenAntikoagulasjon[i] = sMedShAnnenAntikoagulasjon[i];
				if (sMedShAnnenAntikoagulasjonNum[i] == null) {
					MedShAnnenAntikoagulasjonNum[i] = java.lang.Double.NaN;
				}
				else {
					MedShAnnenAntikoagulasjonNum[i] = sMedShAnnenAntikoagulasjonNum[i];
				}

				MedShAnnenPlatehemming[i] = sMedShAnnenPlatehemming[i];
				if (sMedShAnnenPlatehemmingNum[i] == null) {
					MedShAnnenPlatehemmingNum[i] = java.lang.Double.NaN;
				}
				else {
					MedShAnnenPlatehemmingNum[i] = sMedShAnnenPlatehemmingNum[i];
				}

				MedShAntikoagulasjon[i] = sMedShAntikoagulasjon[i];
				if (sMedShAntikoagulasjonNum[i] == null) {
					MedShAntikoagulasjonNum[i] = java.lang.Double.NaN;
				}
				else {
					MedShAntikoagulasjonNum[i] = sMedShAntikoagulasjonNum[i];
				}

				MedShASA[i] = sMedShASA[i];
				if (sMedShASANum[i] == null) {
					MedShASANum[i] = java.lang.Double.NaN;
				}
				else {
					MedShASANum[i] = sMedShASANum[i];
				}

				MedShBetablokker[i] = sMedShBetablokker[i];
				if (sMedShBetablokkerNum[i] == null) {
					MedShBetablokkerNum[i] = java.lang.Double.NaN;
				}
				else {
					MedShBetablokkerNum[i] = sMedShBetablokkerNum[i];
				}

				MedShDiuretika[i] = sMedShDiuretika[i];
				if (sMedShDiuretikaNum[i] == null) {
					MedShDiuretikaNum[i] = java.lang.Double.NaN;
				}
				else {
					MedShDiuretikaNum[i] = sMedShDiuretikaNum[i];
				}

				MedShHeparin[i] = sMedShHeparin[i];
				if (sMedShHeparinNum[i] == null) {
					MedShHeparinNum[i] = java.lang.Double.NaN;
				}
				else {
					MedShHeparinNum[i] = sMedShHeparinNum[i];
				}

				MedShIIbIIIa[i] = sMedShIIbIIIa[i];
				if (sMedShIIbIIIaNum[i] == null) {
					MedShIIbIIIaNum[i] = java.lang.Double.NaN;
				}
				else {
					MedShIIbIIIaNum[i] = sMedShIIbIIIaNum[i];
				}

				if (sMedShIngen[i] == null) {
					MedShIngen[i] = java.lang.Double.NaN;
				}
				else {
					MedShIngen[i] = sMedShIngen[i];
				}

				MedShInotropeMed[i] = sMedShInotropeMed[i];
				if (sMedShInotropeMedNum[i] == null) {
					MedShInotropeMedNum[i] = java.lang.Double.NaN;
				}
				else {
					MedShInotropeMedNum[i] = sMedShInotropeMedNum[i];
				}

				MedShKvitamin[i] = sMedShKvitamin[i];
				if (sMedShKvitaminNum[i] == null) {
					MedShKvitaminNum[i] = java.lang.Double.NaN;
				}
				else {
					MedShKvitaminNum[i] = sMedShKvitaminNum[i];
				}

				MedShPlatehemming[i] = sMedShPlatehemming[i];
				if (sMedShPlatehemmingNum[i] == null) {
					MedShPlatehemmingNum[i] = java.lang.Double.NaN;
				}
				else {
					MedShPlatehemmingNum[i] = sMedShPlatehemmingNum[i];
				}

				MedShTrombinhemmer[i] = sMedShTrombinhemmer[i];
				if (sMedShTrombinhemmerNum[i] == null) {
					MedShTrombinhemmerNum[i] = java.lang.Double.NaN;
				}
				else {
					MedShTrombinhemmerNum[i] = sMedShTrombinhemmerNum[i];
				}

				MedUtACEhemmerA2AA[i] = sMedUtACEhemmerA2AA[i];
				if (sMedUtACEhemmerA2AANum[i] == null) {
					MedUtACEhemmerA2AANum[i] = java.lang.Double.NaN;
				}
				else {
					MedUtACEhemmerA2AANum[i] = sMedUtACEhemmerA2AANum[i];
				}

				MedUtADP[i] = sMedUtADP[i];
				if (sMedUtADPNum[i] == null) {
					MedUtADPNum[i] = java.lang.Double.NaN;
				}
				else {
					MedUtADPNum[i] = sMedUtADPNum[i];
				}

				MedUtAndreLipidsenkere[i] = sMedUtAndreLipidsenkere[i];
				if (sMedUtAndreLipidsenkereNum[i] == null) {
					MedUtAndreLipidsenkereNum[i] = java.lang.Double.NaN;
				}
				else {
					MedUtAndreLipidsenkereNum[i] = sMedUtAndreLipidsenkereNum[i];
				}

				MedUtAnnenAntikoagulasjon[i] = sMedUtAnnenAntikoagulasjon[i];
				if (sMedUtAnnenAntikoagulasjonNum[i] == null) {
					MedUtAnnenAntikoagulasjonNum[i] = java.lang.Double.NaN;
				}
				else {
					MedUtAnnenAntikoagulasjonNum[i] = sMedUtAnnenAntikoagulasjonNum[i];
				}

				MedUtAnnenPlatehemming[i] = sMedUtAnnenPlatehemming[i];
				if (sMedUtAnnenPlatehemmingNum[i] == null) {
					MedUtAnnenPlatehemmingNum[i] = java.lang.Double.NaN;
				}
				else {
					MedUtAnnenPlatehemmingNum[i] = sMedUtAnnenPlatehemmingNum[i];
				}

				MedUtAntikoagulasjon[i] = sMedUtAntikoagulasjon[i];
				if (sMedUtAntikoagulasjonNum[i] == null) {
					MedUtAntikoagulasjonNum[i] = java.lang.Double.NaN;
				}
				else {
					MedUtAntikoagulasjonNum[i] = sMedUtAntikoagulasjonNum[i];
				}

				MedUtASA[i] = sMedUtASA[i];
				if (sMedUtASANum[i] == null) {
					MedUtASANum[i] = java.lang.Double.NaN;
				}
				else {
					MedUtASANum[i] = sMedUtASANum[i];
				}

				MedUtBetablokker[i] = sMedUtBetablokker[i];
				if (sMedUtBetablokkerNum[i] == null) {
					MedUtBetablokkerNum[i] = java.lang.Double.NaN;
				}
				else {
					MedUtBetablokkerNum[i] = sMedUtBetablokkerNum[i];
				}

				MedUtDiuretika[i] = sMedUtDiuretika[i];
				if (sMedUtDiuretikaNum[i] == null) {
					MedUtDiuretikaNum[i] = java.lang.Double.NaN;
				}
				else {
					MedUtDiuretikaNum[i] = sMedUtDiuretikaNum[i];
				}

				MedUtHeparin[i] = sMedUtHeparin[i];
				MedUtHeparinlavmol[i] = sMedUtHeparinlavmol[i];
				if (sMedUtHeparinlavmolNum[i] == null) {
					MedUtHeparinlavmolNum[i] = java.lang.Double.NaN;
				}
				else {
					MedUtHeparinlavmolNum[i] = sMedUtHeparinlavmolNum[i];
				}

				MedUtIIbIIIa[i] = sMedUtIIbIIIa[i];
				if (sMedUtIngen[i] == null) {
					MedUtIngen[i] = java.lang.Double.NaN;
				}
				else {
					MedUtIngen[i] = sMedUtIngen[i];
				}

				MedUtKvitamin[i] = sMedUtKvitamin[i];
				if (sMedUtKvitaminNum[i] == null) {
					MedUtKvitaminNum[i] = java.lang.Double.NaN;
				}
				else {
					MedUtKvitaminNum[i] = sMedUtKvitaminNum[i];
				}

				MedUtPlatehemming[i] = sMedUtPlatehemming[i];
				if (sMedUtPlatehemmingNum[i] == null) {
					MedUtPlatehemmingNum[i] = java.lang.Double.NaN;
				}
				else {
					MedUtPlatehemmingNum[i] = sMedUtPlatehemmingNum[i];
				}

				MedUtStatin2[i] = sMedUtStatin2[i];
				if (sMedUtStatin2Num[i] == null) {
					MedUtStatin2Num[i] = java.lang.Double.NaN;
				}
				else {
					MedUtStatin2Num[i] = sMedUtStatin2Num[i];
				}

				MedUtTrombinhemmer[i] = sMedUtTrombinhemmer[i];
				if (sMedUtTrombinhemmerNum[i] == null) {
					MedUtTrombinhemmerNum[i] = java.lang.Double.NaN;
				}
				else {
					MedUtTrombinhemmerNum[i] = sMedUtTrombinhemmerNum[i];
				}

				Morsdato[i] = sMorsdato[i];
				OppfUtreisedato[i] = sOppfUtreisedato[i];
				Organisasjon[i] = sOrganisasjon[i];
				if (sOrgRESH[i] == null) {
					OrgRESH[i] = java.lang.Double.NaN;
				}
				else {
					OrgRESH[i] = sOrgRESH[i];
				}

				Overflytningstid[i] = sOverflytningstid[i];
				OverflyttetPasient[i] = sOverflyttetPasient[i];
				if (sOverflyttetPasientNum[i] == null) {
					OverflyttetPasientNum[i] = java.lang.Double.NaN;
				}
				else {
					OverflyttetPasientNum[i] = sOverflyttetPasientNum[i];
				}

				Pasientnummer[i] = sPasientnummer[i];
				PatientInRegistryKey[i] = sPatientInRegistryKey[i];
				if (sPCIsykehus[i] == null) {
					PCIsykehus[i] = java.lang.Double.NaN;
				}
				else {
					PCIsykehus[i] = sPCIsykehus[i];
				}

				Postnr[i] = sPostnr[i];
				Poststed[i] = sPoststed[i];
				Registreringsavd[i] = sRegistreringsavd[i];
				if (sReshID[i] == null) {
					ReshID[i] = java.lang.Double.NaN;
				}
				else {
					ReshID[i] = sReshID[i];
				}

				Resuscitering[i] = sResuscitering[i];
				if (sResusciteringNum[i] == null) {
					ResusciteringNum[i] = java.lang.Double.NaN;
				}
				else {
					ResusciteringNum[i] = sResusciteringNum[i];
				}

				RHFnavn[i] = sRHFnavn[i];
				if (sRHFresh[i] == null) {
					RHFresh[i] = java.lang.Double.NaN;
				}
				else {
					RHFresh[i] = sRHFresh[i];
				}

				Roeyker[i] = sRoeyker[i];
				if (sRoeykerNum[i] == null) {
					RoeykerNum[i] = java.lang.Double.NaN;
				}
				else {
					RoeykerNum[i] = sRoeykerNum[i];
				}

				Sivilstatus[i] = sSivilstatus[i];
				Skjematype[i] = sSkjematype[i];
				SorteringsParameterVerdi[i] = sSorteringsParameterVerdi[i];
				Statin[i] = sStatin[i];
				if (sStatinNum[i] == null) {
					StatinNum[i] = java.lang.Double.NaN;
				}
				else {
					StatinNum[i] = sStatinNum[i];
				}

				Sykehus[i] = sSykehus[i];
				Symptomdebut[i] = sSymptomdebut[i];
				if (sTeknDataSettID[i] == null) {
					TeknDataSettID[i] = java.lang.Double.NaN;
				}
				else {
					TeknDataSettID[i] = sTeknDataSettID[i];
				}

				TeknFradato[i] = sTeknFradato[i];
				TeknKontaktFraDato[i] = sTeknKontaktFraDato[i];
				if (sTeknKontaktID[i] == null) {
					TeknKontaktID[i] = java.lang.Double.NaN;
				}
				else {
					TeknKontaktID[i] = sTeknKontaktID[i];
				}

				TeknKontaktNavn[i] = sTeknKontaktNavn[i];
				TeknKontaktTilDato[i] = sTeknKontaktTilDato[i];
				TeknOpprettetDato[i] = sTeknOpprettetDato[i];
				if (sTeknRapportgrunnlagID[i] == null) {
					TeknRapportgrunnlagID[i] = java.lang.Double.NaN;
				}
				else {
					TeknRapportgrunnlagID[i] = sTeknRapportgrunnlagID[i];
				}

				if (sTeknRelatedID[i] == null) {
					TeknRelatedID[i] = java.lang.Double.NaN;
				}
				else {
					TeknRelatedID[i] = sTeknRelatedID[i];
				}

				if (sTeknSkjemaID[i] == null) {
					TeknSkjemaID[i] = java.lang.Double.NaN;
				}
				else {
					TeknSkjemaID[i] = sTeknSkjemaID[i];
				}

				TeknTildato[i] = sTeknTildato[i];
				TeknUpdated[i] = sTeknUpdated[i];
				TidlDiabetes[i] = sTidlDiabetes[i];
				if (sTidlDiabetesNum[i] == null) {
					TidlDiabetesNum[i] = java.lang.Double.NaN;
				}
				else {
					TidlDiabetesNum[i] = sTidlDiabetesNum[i];
				}

				TidlFamiliaerOpphopning[i] = sTidlFamiliaerOpphopning[i];
				if (sTidlFamiliaerOpphopningNum[i] == null) {
					TidlFamiliaerOpphopningNum[i] = java.lang.Double.NaN;
				}
				else {
					TidlFamiliaerOpphopningNum[i] = sTidlFamiliaerOpphopningNum[i];
				}

				TidlHjerneslag[i] = sTidlHjerneslag[i];
				if (sTidlHjerneslagNum[i] == null) {
					TidlHjerneslagNum[i] = java.lang.Double.NaN;
				}
				else {
					TidlHjerneslagNum[i] = sTidlHjerneslagNum[i];
				}

				TidlHjerteinfarkt[i] = sTidlHjerteinfarkt[i];
				if (sTidlHjerteinfarktNum[i] == null) {
					TidlHjerteinfarktNum[i] = java.lang.Double.NaN;
				}
				else {
					TidlHjerteinfarktNum[i] = sTidlHjerteinfarktNum[i];
				}

				TidlHypertensjonsbeh[i] = sTidlHypertensjonsbeh[i];
				if (sTidlHypertensjonsbehNum[i] == null) {
					TidlHypertensjonsbehNum[i] = java.lang.Double.NaN;
				}
				else {
					TidlHypertensjonsbehNum[i] = sTidlHypertensjonsbehNum[i];
				}

				if (sTidlIngen[i] == null) {
					TidlIngen[i] = java.lang.Double.NaN;
				}
				else {
					TidlIngen[i] = sTidlIngen[i];
				}

				TidlKoronaroperert[i] = sTidlKoronaroperert[i];
				if (sTidlKoronaroperertNum[i] == null) {
					TidlKoronaroperertNum[i] = java.lang.Double.NaN;
				}
				else {
					TidlKoronaroperertNum[i] = sTidlKoronaroperertNum[i];
				}

				TidlKroniskHjertesvikt[i] = sTidlKroniskHjertesvikt[i];
				if (sTidlKroniskHjertesviktNum[i] == null) {
					TidlKroniskHjertesviktNum[i] = java.lang.Double.NaN;
				}
				else {
					TidlKroniskHjertesviktNum[i] = sTidlKroniskHjertesviktNum[i];
				}

				TidlPCI[i] = sTidlPCI[i];
				if (sTidlPCINum[i] == null) {
					TidlPCINum[i] = java.lang.Double.NaN;
				}
				else {
					TidlPCINum[i] = sTidlPCINum[i];
				}

				TidlPeriferVaskulaerSykdom[i] = sTidlPeriferVaskulaerSykdom[i];
				if (sTidlPeriferVaskulaerSykdomNum[i] == null) {
					TidlPeriferVaskulaerSykdomNum[i] = java.lang.Double.NaN;
				}
				else {
					TidlPeriferVaskulaerSykdomNum[i] = sTidlPeriferVaskulaerSykdomNum[i];
				}

				TiltakCPAPBiPAP[i] = sTiltakCPAPBiPAP[i];
				if (sTiltakCPAPBiPAPNum[i] == null) {
					TiltakCPAPBiPAPNum[i] = java.lang.Double.NaN;
				}
				else {
					TiltakCPAPBiPAPNum[i] = sTiltakCPAPBiPAPNum[i];
				}

				TiltakHypotermibeh[i] = sTiltakHypotermibeh[i];
				if (sTiltakHypotermibehNum[i] == null) {
					TiltakHypotermibehNum[i] = java.lang.Double.NaN;
				}
				else {
					TiltakHypotermibehNum[i] = sTiltakHypotermibehNum[i];
				}

				TiltakIABP[i] = sTiltakIABP[i];
				if (sTiltakIABPNum[i] == null) {
					TiltakIABPNum[i] = java.lang.Double.NaN;
				}
				else {
					TiltakIABPNum[i] = sTiltakIABPNum[i];
				}

				if (sTiltakIngen[i] == null) {
					TiltakIngen[i] = java.lang.Double.NaN;
				}
				else {
					TiltakIngen[i] = sTiltakIngen[i];
				}

				TiltakPMICD[i] = sTiltakPMICD[i];
				if (sTiltakPMICDNum[i] == null) {
					TiltakPMICDNum[i] = java.lang.Double.NaN;
				}
				else {
					TiltakPMICDNum[i] = sTiltakPMICDNum[i];
				}

				TiltakRespirator[i] = sTiltakRespirator[i];
				if (sTiltakRespiratorNum[i] == null) {
					TiltakRespiratorNum[i] = java.lang.Double.NaN;
				}
				else {
					TiltakRespiratorNum[i] = sTiltakRespiratorNum[i];
				}

				Trombolysebeh[i] = sTrombolysebeh[i];
				if (sTrombolysebehNum[i] == null) {
					TrombolysebehNum[i] = java.lang.Double.NaN;
				}
				else {
					TrombolysebehNum[i] = sTrombolysebehNum[i];
				}

				Trombolysetid[i] = sTrombolysetid[i];
				TroponingStigningFall[i] = sTroponingStigningFall[i];
				if (sTroponingStigningFalNum[i] == null) {
					TroponingStigningFalNum[i] = java.lang.Double.NaN;
				}
				else {
					TroponingStigningFalNum[i] = sTroponingStigningFalNum[i];
				}

				TypeInfarkt[i] = sTypeInfarkt[i];
				TypeInfarktTekst[i] = sTypeInfarktTekst[i];
				Utreisedato[i] = sUtreisedato[i];
				UtskrivesTil2[i] = sUtskrivesTil2[i];
				if (sUtskrivesTil2Num[i] == null) {
					UtskrivesTil2Num[i] = java.lang.Double.NaN;
				}
				else {
					UtskrivesTil2Num[i] = sUtskrivesTil2Num[i];
				}

				if (sVisInnkomstvariabler[i] == null) {
					VisInnkomstvariabler[i] = java.lang.Double.NaN;
				}
				else {
					VisInnkomstvariabler[i] = sVisInnkomstvariabler[i];
				}

				Yrke[i] = sYrke[i];
				DatasettGuid[i] = sDatasettGuid[i];
				i++;
			}

			
			log.debug("Creating the R dataframe...");

			RList l = new RList();
			l.put("Alder", new REXPDouble(Alder));
			l.put("Avdeling", new REXPString(Avdeling));
			l.put("AvdelingNum", new REXPDouble(AvdelingNum));
			l.put("AvdNavn", new REXPString(AvdNavn));
			l.put("BMI", new REXPDouble(BMI));
			l.put("BMIHoyde", new REXPDouble(BMIHoyde));
			l.put("BMIUkjent", new REXPDouble(BMIUkjent));
			l.put("BMIVekt", new REXPDouble(BMIVekt));
			l.put("BTPOvertrykk", new REXPDouble(BTPOvertrykk));
			l.put("BTPPuls", new REXPDouble(BTPPuls));
			l.put("BTPUkjent", new REXPDouble(BTPUkjent));
			l.put("BTPUndertrykk", new REXPDouble(BTPUndertrykk));
			l.put("DoedUnderOpphold", new REXPString(DoedUnderOpphold));
			l.put("DoedUnderOppholdNum", new REXPDouble(DoedUnderOppholdNum));
			l.put("EKGDiagnostisk", new REXPString(EKGDiagnostisk));
			l.put("EKGDiagnostiskNum", new REXPDouble(EKGDiagnostiskNum));
			l.put("EKGNyQbolge", new REXPString(EKGNyQbolge));
			l.put("EKGNyQbolgeNum", new REXPDouble(EKGNyQbolgeNum));
			l.put("EKGRytme", new REXPString(EKGRytme));
			l.put("EKGRytmeNum", new REXPDouble(EKGRytmeNum));
			l.put("EKGSteminStemi", new REXPString(EKGSteminStemi));
			l.put("EKGSteminStemiNum", new REXPDouble(EKGSteminStemiNum));
			l.put("EkkoEF", new REXPString(EkkoEF));
			l.put("EkkoEfNum", new REXPDouble(EkkoEfNum));
			l.put("EkkoValg", new REXPString(EkkoValg));
			l.put("EkkoValgNum", new REXPDouble(EkkoValgNum));
			l.put("PasientID", new REXPString(PasientID));
			l.put("Foedselsaar", new REXPString(Foedselsaar));
			l.put("FraHvilkenRegion", new REXPString(FraHvilkenRegion));
			l.put("FraHvilkenRegionNum", new REXPDouble(FraHvilkenRegionNum));
			l.put("FraHvilketSykehus2", new REXPString(FraHvilketSykehus2));
			l.put("HvorBefantPasientenSeg", new REXPString(HvorBefantPasientenSeg));
			l.put("HvorBefantPasientenSegNum", new REXPDouble(HvorBefantPasientenSegNum));
			l.put("Infarktlokalisasjon", new REXPString(Infarktlokalisasjon));
			l.put("InfarktlokalisasjonNum", new REXPDouble(InfarktlokalisasjonNum));
			l.put("Innleggelsestidspunkt", new REXPString(Innleggelsestidspunkt));
			l.put("Innleggelsestidspunkt2", new REXPString(Innleggelsestidspunkt2));
			l.put("InvAntSykekar", new REXPString(InvAntSykekar));
			l.put("InvAntSykekarTekst", new REXPString(InvAntSykekarTekst));
			l.put("InvKunKorAngio", new REXPString(InvKunKorAngio));
			l.put("InvKunKorAngioNum", new REXPDouble(InvKunKorAngioNum));
			l.put("InvKunKorAngioPCITid", new REXPString(InvKunKorAngioPCITid));
			l.put("InvKunKorAngioTid", new REXPString(InvKunKorAngioTid));
			l.put("InvPCIunderoppholdet", new REXPString(InvPCIunderoppholdet));
			l.put("InvPCIunderoppholdetNum", new REXPDouble(InvPCIunderoppholdetNum));
			l.put("InvStent", new REXPString(InvStent));
			l.put("InvStentTekst", new REXPString(InvStentTekst));
			l.put("InvStentType", new REXPString(InvStentType));
			l.put("InvStentTypeTekst", new REXPString(InvStentTypeTekst));
			l.put("IvnPCITid", new REXPString(IvnPCITid));
			l.put("Kjoenn", new REXPString(Kjoenn));
			l.put("Kommunenr", new REXPString(Kommunenr));
			l.put("KomplAtrieflimmerFlutter", new REXPString(KomplAtrieflimmerFlutter));
			l.put("KomplAtrieflimmerFlutterNum", new REXPDouble(KomplAtrieflimmerFlutterNum));
			l.put("KomplAVblokk2el3", new REXPString(KomplAVblokk2el3));
			l.put("KomplAVblokk2el3Num", new REXPDouble(KomplAVblokk2el3Num));
			l.put("KomplBloedAnnen", new REXPString(KomplBloedAnnen));
			l.put("KomplBloedAnnenNum", new REXPDouble(KomplBloedAnnenNum));
			l.put("KomplBloedCerebral", new REXPString(KomplBloedCerebral));
			l.put("KomplBloedCerebralNum", new REXPDouble(KomplBloedCerebralNum));
			l.put("KomplBloedGI", new REXPString(KomplBloedGI));
			l.put("KomplBloedGINum", new REXPDouble(KomplBloedGINum));
			l.put("KomplBloedInnstikk", new REXPString(KomplBloedInnstikk));
			l.put("KomplBloedInnstikkNum", new REXPDouble(KomplBloedInnstikkNum));
			l.put("KomplBloedning", new REXPString(KomplBloedning));
			l.put("KomplBloedningNum", new REXPDouble(KomplBloedningNum));
			l.put("KomplHjerneslag", new REXPString(KomplHjerneslag));
			l.put("KomplHjerneslagNum", new REXPDouble(KomplHjerneslagNum));
			l.put("KomplHjertesvikt", new REXPString(KomplHjertesvikt));
			l.put("KomplHjertesviktNum", new REXPDouble(KomplHjertesviktNum));
			l.put("KomplIngen", new REXPDouble(KomplIngen));
			l.put("KomplKardiogentSjokk", new REXPString(KomplKardiogentSjokk));
			l.put("KomplKardiogentSjokkNum", new REXPDouble(KomplKardiogentSjokkNum));
			l.put("KomplMekaniskKomplikasjon", new REXPString(KomplMekaniskKomplikasjon));
			l.put("KomplMekaniskKomplikasjonNum", new REXPDouble(KomplMekaniskKomplikasjonNum));
			l.put("KomplReinfarkt", new REXPString(KomplReinfarkt));
			l.put("KomplReinfarktNum", new REXPDouble(KomplReinfarktNum));
			l.put("KomplVTVFe48Num", new REXPDouble(KomplVTVFe48Num));
			l.put("KomplVTVFe48t", new REXPString(KomplVTVFe48t));
			l.put("KomplVTVFf48t", new REXPString(KomplVTVFf48t));
			l.put("KomplVTVFf48tNum", new REXPDouble(KomplVTVFf48tNum));
			l.put("KoronarProsedyreEtPlan", new REXPString(KoronarProsedyreEtPlan));
			l.put("KoronarProsedyreEtPlanNum", new REXPDouble(KoronarProsedyreEtPlanNum));
			l.put("LabGlukose", new REXPString(LabGlukose));
			l.put("LabHbA1C", new REXPString(LabHbA1C));
			l.put("LabHDL", new REXPString(LabHDL));
			l.put("LabInfarktmarkoer", new REXPString(LabInfarktmarkoer));
			l.put("LabInfarktmarkoererNum", new REXPDouble(LabInfarktmarkoererNum));
			l.put("LabInfarktmarkorHoy", new REXPString(LabInfarktmarkorHoy));
			l.put("LabInfarktmarkorLav", new REXPString(LabInfarktmarkorLav));
			l.put("LabKreatinin", new REXPString(LabKreatinin));
			l.put("LabLDL", new REXPString(LabLDL));
			l.put("LabTotalkolesterol", new REXPString(LabTotalkolesterol));
			l.put("LabTriglyserider", new REXPString(LabTriglyserider));
			l.put("Liggetid", new REXPDouble(Liggetid));
			l.put("LokalRegion", new REXPString(LokalRegion));
			l.put("LokalRegion2", new REXPString(LokalRegion2));
			l.put("LokalRegionNum", new REXPDouble(LokalRegionNum));
			l.put("MedInnACEhemmerAII", new REXPString(MedInnACEhemmerAII));
			l.put("MedInnACEhemmerAIINum", new REXPDouble(MedInnACEhemmerAIINum));
			l.put("MedInnADP", new REXPString(MedInnADP));
			l.put("MedInnADPNum", new REXPDouble(MedInnADPNum));
			l.put("MedInnAnnenPlatehemming", new REXPString(MedInnAnnenPlatehemming));
			l.put("MedInnAnnenPlatehemmingNum", new REXPDouble(MedInnAnnenPlatehemmingNum));
			l.put("MedInnAntikoagulasjon", new REXPString(MedInnAntikoagulasjon));
			l.put("MedInnAntikoagulasjonNum", new REXPDouble(MedInnAntikoagulasjonNum));
			l.put("MedInnASA", new REXPString(MedInnASA));
			l.put("MedInnASANum", new REXPDouble(MedInnASANum));
			l.put("MedInnBetablokker", new REXPString(MedInnBetablokker));
			l.put("MedInnBetablokkerNum", new REXPDouble(MedInnBetablokkerNum));
			l.put("MedInnDiuretika", new REXPString(MedInnDiuretika));
			l.put("MedInnDiuretikaNum", new REXPDouble(MedInnDiuretikaNum));
			l.put("MedInnIngen", new REXPDouble(MedInnIngen));
			l.put("MedInnLipidsenkere", new REXPString(MedInnLipidsenkere));
			l.put("MedInnLipidsenkereNum", new REXPDouble(MedInnLipidsenkereNum));
			l.put("MedInnplatehemming", new REXPString(MedInnplatehemming));
			l.put("MedInnplatehemmingNum", new REXPDouble(MedInnplatehemmingNum));
			l.put("MedShACEhemmerAII", new REXPString(MedShACEhemmerAII));
			l.put("MedShACEhemmerAIINum", new REXPDouble(MedShACEhemmerAIINum));
			l.put("MedShADP", new REXPString(MedShADP));
			l.put("MedShADPNum", new REXPDouble(MedShADPNum));
			l.put("MedShAndreAntiarytmika", new REXPString(MedShAndreAntiarytmika));
			l.put("MedShAndreAntiarytmikaNum", new REXPDouble(MedShAndreAntiarytmikaNum));
			l.put("MedShAnnenAntikoagulasjon", new REXPString(MedShAnnenAntikoagulasjon));
			l.put("MedShAnnenAntikoagulasjonNum", new REXPDouble(MedShAnnenAntikoagulasjonNum));
			l.put("MedShAnnenPlatehemming", new REXPString(MedShAnnenPlatehemming));
			l.put("MedShAnnenPlatehemmingNum", new REXPDouble(MedShAnnenPlatehemmingNum));
			l.put("MedShAntikoagulasjon", new REXPString(MedShAntikoagulasjon));
			l.put("MedShAntikoagulasjonNum", new REXPDouble(MedShAntikoagulasjonNum));
			l.put("MedShASA", new REXPString(MedShASA));
			l.put("MedShASANum", new REXPDouble(MedShASANum));
			l.put("MedShBetablokker", new REXPString(MedShBetablokker));
			l.put("MedShBetablokkerNum", new REXPDouble(MedShBetablokkerNum));
			l.put("MedShDiuretika", new REXPString(MedShDiuretika));
			l.put("MedShDiuretikaNum", new REXPDouble(MedShDiuretikaNum));
			l.put("MedShHeparin", new REXPString(MedShHeparin));
			l.put("MedShHeparinNum", new REXPDouble(MedShHeparinNum));
			l.put("MedShIIbIIIa", new REXPString(MedShIIbIIIa));
			l.put("MedShIIbIIIaNum", new REXPDouble(MedShIIbIIIaNum));
			l.put("MedShIngen", new REXPDouble(MedShIngen));
			l.put("MedShInotropeMed", new REXPString(MedShInotropeMed));
			l.put("MedShInotropeMedNum", new REXPDouble(MedShInotropeMedNum));
			l.put("MedShKvitamin", new REXPString(MedShKvitamin));
			l.put("MedShKvitaminNum", new REXPDouble(MedShKvitaminNum));
			l.put("MedShPlatehemming", new REXPString(MedShPlatehemming));
			l.put("MedShPlatehemmingNum", new REXPDouble(MedShPlatehemmingNum));
			l.put("MedShTrombinhemmer", new REXPString(MedShTrombinhemmer));
			l.put("MedShTrombinhemmerNum", new REXPDouble(MedShTrombinhemmerNum));
			l.put("MedUtACEhemmerA2AA", new REXPString(MedUtACEhemmerA2AA));
			l.put("MedUtACEhemmerA2AANum", new REXPDouble(MedUtACEhemmerA2AANum));
			l.put("MedUtADP", new REXPString(MedUtADP));
			l.put("MedUtADPNum", new REXPDouble(MedUtADPNum));
			l.put("MedUtAndreLipidsenkere", new REXPString(MedUtAndreLipidsenkere));
			l.put("MedUtAndreLipidsenkereNum", new REXPDouble(MedUtAndreLipidsenkereNum));
			l.put("MedUtAnnenAntikoagulasjon", new REXPString(MedUtAnnenAntikoagulasjon));
			l.put("MedUtAnnenAntikoagulasjonNum", new REXPDouble(MedUtAnnenAntikoagulasjonNum));
			l.put("MedUtAnnenPlatehemming", new REXPString(MedUtAnnenPlatehemming));
			l.put("MedUtAnnenPlatehemmingNum", new REXPDouble(MedUtAnnenPlatehemmingNum));
			l.put("MedUtAntikoagulasjon", new REXPString(MedUtAntikoagulasjon));
			l.put("MedUtAntikoagulasjonNum", new REXPDouble(MedUtAntikoagulasjonNum));
			l.put("MedUtASA", new REXPString(MedUtASA));
			l.put("MedUtASANum", new REXPDouble(MedUtASANum));
			l.put("MedUtBetablokker", new REXPString(MedUtBetablokker));
			l.put("MedUtBetablokkerNum", new REXPDouble(MedUtBetablokkerNum));
			l.put("MedUtDiuretika", new REXPString(MedUtDiuretika));
			l.put("MedUtDiuretikaNum", new REXPDouble(MedUtDiuretikaNum));
			l.put("MedUtHeparin", new REXPString(MedUtHeparin));
			l.put("MedUtHeparinlavmol", new REXPString(MedUtHeparinlavmol));
			l.put("MedUtHeparinlavmolNum", new REXPDouble(MedUtHeparinlavmolNum));
			l.put("MedUtIIbIIIa", new REXPString(MedUtIIbIIIa));
			l.put("MedUtIngen", new REXPDouble(MedUtIngen));
			l.put("MedUtKvitamin", new REXPString(MedUtKvitamin));
			l.put("MedUtKvitaminNum", new REXPDouble(MedUtKvitaminNum));
			l.put("MedUtPlatehemming", new REXPString(MedUtPlatehemming));
			l.put("MedUtPlatehemmingNum", new REXPDouble(MedUtPlatehemmingNum));
			l.put("MedUtStatin2", new REXPString(MedUtStatin2));
			l.put("MedUtStatin2Num", new REXPDouble(MedUtStatin2Num));
			l.put("MedUtTrombinhemmer", new REXPString(MedUtTrombinhemmer));
			l.put("MedUtTrombinhemmerNum", new REXPDouble(MedUtTrombinhemmerNum));
			l.put("Morsdato", new REXPString(Morsdato));
			l.put("OppfUtreisedato", new REXPString(OppfUtreisedato));
			l.put("Organisasjon", new REXPString(Organisasjon));
			l.put("OrgRESH", new REXPDouble(OrgRESH));
			l.put("Overflytningstid", new REXPString(Overflytningstid));
			l.put("OverflyttetPasient", new REXPString(OverflyttetPasient));
			l.put("OverflyttetPasientNum", new REXPDouble(OverflyttetPasientNum));
			l.put("Pasientnummer", new REXPString(Pasientnummer));
			l.put("PatientInRegistryKey", new REXPString(PatientInRegistryKey));
			l.put("PCIsykehus", new REXPDouble(PCIsykehus));
			l.put("Postnr", new REXPString(Postnr));
			l.put("Poststed", new REXPString(Poststed));
			l.put("Registreringsavd", new REXPString(Registreringsavd));
			l.put("ReshID", new REXPDouble(ReshID));
			l.put("Resuscitering", new REXPString(Resuscitering));
			l.put("ResusciteringNum", new REXPDouble(ResusciteringNum));
			l.put("RHFnavn", new REXPString(RHFnavn));
			l.put("RHFresh", new REXPDouble(RHFresh));
			l.put("Roeyker", new REXPString(Roeyker));
			l.put("RoeykerNum", new REXPDouble(RoeykerNum));
			l.put("Sivilstatus", new REXPString(Sivilstatus));
			l.put("Skjematype", new REXPString(Skjematype));
			l.put("SorteringsParameterVerdi", new REXPString(SorteringsParameterVerdi));
			l.put("Statin", new REXPString(Statin));
			l.put("StatinNum", new REXPDouble(StatinNum));
			l.put("Sykehus", new REXPString(Sykehus));
			l.put("Symptomdebut", new REXPString(Symptomdebut));
			l.put("TeknDataSettID", new REXPDouble(TeknDataSettID));
			l.put("TeknFradato", new REXPString(TeknFradato));
			l.put("TeknKontaktFraDato", new REXPString(TeknKontaktFraDato));
			l.put("TeknKontaktID", new REXPDouble(TeknKontaktID));
			l.put("TeknKontaktNavn", new REXPString(TeknKontaktNavn));
			l.put("TeknKontaktTilDato", new REXPString(TeknKontaktTilDato));
			l.put("TeknOpprettetDato", new REXPString(TeknOpprettetDato));
			l.put("TeknRapportgrunnlagID", new REXPDouble(TeknRapportgrunnlagID));
			l.put("TeknRelatedID", new REXPDouble(TeknRelatedID));
			l.put("TeknSkjemaID", new REXPDouble(TeknSkjemaID));
			l.put("TeknTildato", new REXPString(TeknTildato));
			l.put("TeknUpdated", new REXPString(TeknUpdated));
			l.put("TidlDiabetes", new REXPString(TidlDiabetes));
			l.put("TidlDiabetesNum", new REXPDouble(TidlDiabetesNum));
			l.put("TidlFamiliaerOpphopning", new REXPString(TidlFamiliaerOpphopning));
			l.put("TidlFamiliaerOpphopningNum", new REXPDouble(TidlFamiliaerOpphopningNum));
			l.put("TidlHjerneslag", new REXPString(TidlHjerneslag));
			l.put("TidlHjerneslagNum", new REXPDouble(TidlHjerneslagNum));
			l.put("TidlHjerteinfarkt", new REXPString(TidlHjerteinfarkt));
			l.put("TidlHjerteinfarktNum", new REXPDouble(TidlHjerteinfarktNum));
			l.put("TidlHypertensjonsbeh", new REXPString(TidlHypertensjonsbeh));
			l.put("TidlHypertensjonsbehNum", new REXPDouble(TidlHypertensjonsbehNum));
			l.put("TidlIngen", new REXPDouble(TidlIngen));
			l.put("TidlKoronaroperert", new REXPString(TidlKoronaroperert));
			l.put("TidlKoronaroperertNum", new REXPDouble(TidlKoronaroperertNum));
			l.put("TidlKroniskHjertesvikt", new REXPString(TidlKroniskHjertesvikt));
			l.put("TidlKroniskHjertesviktNum", new REXPDouble(TidlKroniskHjertesviktNum));
			l.put("TidlPCI", new REXPString(TidlPCI));
			l.put("TidlPCINum", new REXPDouble(TidlPCINum));
			l.put("TidlPeriferVaskulaerSykdom", new REXPString(TidlPeriferVaskulaerSykdom));
			l.put("TidlPeriferVaskulaerSykdomNum", new REXPDouble(TidlPeriferVaskulaerSykdomNum));
			l.put("TiltakCPAPBiPAP", new REXPString(TiltakCPAPBiPAP));
			l.put("TiltakCPAPBiPAPNum", new REXPDouble(TiltakCPAPBiPAPNum));
			l.put("TiltakHypotermibeh", new REXPString(TiltakHypotermibeh));
			l.put("TiltakHypotermibehNum", new REXPDouble(TiltakHypotermibehNum));
			l.put("TiltakIABP", new REXPString(TiltakIABP));
			l.put("TiltakIABPNum", new REXPDouble(TiltakIABPNum));
			l.put("TiltakIngen", new REXPDouble(TiltakIngen));
			l.put("TiltakPMICD", new REXPString(TiltakPMICD));
			l.put("TiltakPMICDNum", new REXPDouble(TiltakPMICDNum));
			l.put("TiltakRespirator", new REXPString(TiltakRespirator));
			l.put("TiltakRespiratorNum", new REXPDouble(TiltakRespiratorNum));
			l.put("Trombolysebeh", new REXPString(Trombolysebeh));
			l.put("TrombolysebehNum", new REXPDouble(TrombolysebehNum));
			l.put("Trombolysetid", new REXPString(Trombolysetid));
			l.put("TroponingStigningFall", new REXPString(TroponingStigningFall));
			l.put("TroponingStigningFalNum", new REXPDouble(TroponingStigningFalNum));
			l.put("TypeInfarkt", new REXPString(TypeInfarkt));
			l.put("TypeInfarktTekst", new REXPString(TypeInfarktTekst));
			l.put("Utreisedato", new REXPString(Utreisedato));
			l.put("UtskrivesTil2", new REXPString(UtskrivesTil2));
			l.put("UtskrivesTil2Num", new REXPDouble(UtskrivesTil2Num));
			l.put("VisInnkomstvariabler", new REXPDouble(VisInnkomstvariabler));
			l.put("Yrke", new REXPString(Yrke));
			l.put("DatasettGuid", new REXPString(DatasettGuid));
			REXP df = REXP.createDataFrame(l);
			log.debug("Assigning data frame to R instance");
			rconn.assign("RegData", df);


			
			
			// Set up the tmp directory, file names and reportUserInfo
			String tmpdir = "";
			String p_filename = "";
			log.debug("Setting report image filepath and name");
			tmpdir = "/opt/jasper/img/";
			File dirFile = new File(tmpdir);
			String fileBaseName = "hjerneslag_" + reportName + "-";
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
