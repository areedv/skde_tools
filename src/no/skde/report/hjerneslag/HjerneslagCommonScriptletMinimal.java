/**
 * no.skde.report.hjerneslag
 * HjerneslagCommonScriptletMinimal.java Feb 19 2014 Are Edvardsen
 * 
 * Minimal in terms of less fields in the data set
 * 
 *  Copyleft 2014, SKDE
 */

package no.skde.report.hjerneslag;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.fill.*;
import org.apache.log4j.Logger;
import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.*;

public class HjerneslagCommonScriptletMinimal extends JRDefaultScriptlet {

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
			log.info("Start generating R report using " + HjerneslagCommonScriptlet.class.getName());
			
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
			
			String innl4t;
			try {
				innl4t = (String) ((JRFillParameter) parametersMap.get("innl4t")).getValue();
				if (innl4t == "") {
					innl4t = "9";
				}
				rconn.voidEval("innl4t=" + innl4t);
			} catch (Exception e) {
				log.debug("Parameter innl4t is not defined: " + e.getMessage());
			}
			
			String NIHSSinn;
			try {
				NIHSSinn = (String) ((JRFillParameter) parametersMap.get("NIHSSinn")).getValue();
				if (NIHSSinn == "") {
					NIHSSinn = "99";
				}
				rconn.voidEval("NIHSSinn=" + NIHSSinn);
			} catch (Exception e) {
				log.debug("Parameter NIHSSinn is not defined: " + e.getMessage());
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
			
			
			
			// set path to library, to be removed since Rapporteket uses same directory for all R files (noweb, libs and report funs)
			String libkat = "'/opt/jasper/r/'";
			rconn.voidEval("libkat=" + libkat);
			
			log.debug("Getting Jasper Report data source...");
			
			// Load up primitive arrays with query data
			JRDataSource ds = (JRDataSource) ((JRFillParameter) parametersMap
					.get("REPORT_DATA_SOURCE")).getValue();
			
			log.debug("Getting Jasper Report Fields from report data source...");
			
			JRField ReshIdField = (JRField) fieldsMap.get("ReshId");
			JRField RHFField = (JRField) fieldsMap.get("RHF");
			JRField AvdelingField = (JRField) fieldsMap.get("Avdeling");
			JRField KjonnField = (JRField) fieldsMap.get("Kjonn");
			JRField InnleggelsestidspunktField = (JRField) fieldsMap.get("Innleggelsestidspunkt");
			JRField SymptomdebutField = (JRField) fieldsMap.get("Symptomdebut");
			JRField TrombolyseStarttidField = (JRField) fieldsMap.get("TrombolyseStarttid");
			JRField TidInnTrombolyseField = (JRField) fieldsMap.get("TidInnTrombolyse");
			JRField UtskrivingsdatoField = (JRField) fieldsMap.get("Utskrivingsdato");
			JRField AtaksiField = (JRField) fieldsMap.get("Ataksi");
			JRField DobbeltsynField = (JRField) fieldsMap.get("Dobbeltsyn");
			JRField NIHSSikkeUtfortField = (JRField) fieldsMap.get("NIHSSikkeUtfort");
			JRField NeglektField = (JRField) fieldsMap.get("Neglekt");
			JRField SensibilitetsutfallField = (JRField) fieldsMap.get("Sensibilitetsutfall");
			JRField SynsfeltutfallField = (JRField) fieldsMap.get("Synsfeltutfall");
			JRField TrombolyseIngenField = (JRField) fieldsMap.get("TrombolyseIngen");
			JRField UtIngenMedikamField = (JRField) fieldsMap.get("UtIngenMedikam");
			JRField VertigoField = (JRField) fieldsMap.get("Vertigo");
			JRField AntDagerInnlField = (JRField) fieldsMap.get("AntDagerInnl");
			JRField AndreFokaleSymptField = (JRField) fieldsMap.get("AndreFokaleSympt");
			JRField ArmpareseField = (JRField) fieldsMap.get("Armparese");
			JRField AtrieflimmerField = (JRField) fieldsMap.get("Atrieflimmer");
			JRField AvdForstInnlagtField = (JRField) fieldsMap.get("AvdForstInnlagt");
			JRField AvdForstInnlagtHvilkenField = (JRField) fieldsMap.get("AvdForstInnlagtHvilken");
			JRField AvdUtskrFraField = (JRField) fieldsMap.get("AvdUtskrFra");
			JRField AvdUtskrFraHvilkenField = (JRField) fieldsMap.get("AvdUtskrFraHvilken");
			JRField BeinpareseField = (JRField) fieldsMap.get("Beinparese");
			JRField BevissthetsgradInnleggelseField = (JRField) fieldsMap.get("BevissthetsgradInnleggelse");
			JRField BildediagnostikkHjerneField = (JRField) fieldsMap.get("BildediagnostikkHjerne");
			JRField BildediagnostikkHjerteField = (JRField) fieldsMap.get("BildediagnostikkHjerte");
			JRField RegistreringHjerterytmeField = (JRField) fieldsMap.get("RegistreringHjerterytme");
			JRField BildediagnostikkIntraraniellField = (JRField) fieldsMap.get("BildediagnostikkIntraraniell");
			JRField BildediagnostikkEkstrakranKarField = (JRField) fieldsMap.get("BildediagnostikkEkstrakranKar");
			JRField BoligforholdPreField = (JRField) fieldsMap.get("BoligforholdPre");
			JRField Boligforhold3mndField = (JRField) fieldsMap.get("Boligforhold3mnd");
			JRField BosituasjonPreField = (JRField) fieldsMap.get("BosituasjonPre");
			JRField Bosituasjon3mndField = (JRField) fieldsMap.get("Bosituasjon3mnd");
			JRField SvelgtestUtfortField = (JRField) fieldsMap.get("SvelgtestUtfort");
			JRField FacialispareseField = (JRField) fieldsMap.get("Facialisparese");
			JRField ForflytningPreField = (JRField) fieldsMap.get("ForflytningPre");
			JRField Forflytning3mndField = (JRField) fieldsMap.get("Forflytning3mnd");
			JRField OppfolgUtfField = (JRField) fieldsMap.get("OppfolgUtf");
			JRField PaakledningPreField = (JRField) fieldsMap.get("PaakledningPre");
			JRField Paakledning3mndField = (JRField) fieldsMap.get("Paakledning3mnd");
			JRField MRSPreField = (JRField) fieldsMap.get("MRSPre");
			JRField MRS3mndField = (JRField) fieldsMap.get("MRS3mnd");
			JRField RoykerPreField = (JRField) fieldsMap.get("RoykerPre");
			JRField Royker3mndField = (JRField) fieldsMap.get("Royker3mnd");
			JRField YrkesaktivUnderHjerneslag2Field = (JRField) fieldsMap.get("YrkesaktivUnderHjerneslag2");
			JRField YrkesaktivNaaField = (JRField) fieldsMap.get("YrkesaktivNaa");
			JRField KjorteBilForHjerneslagField = (JRField) fieldsMap.get("KjorteBilForHjerneslag");
			JRField KjorerBilNaaField = (JRField) fieldsMap.get("KjorerBilNaa");
			JRField SivilstatusPreField = (JRField) fieldsMap.get("SivilstatusPre");
			JRField Sivilstatus3mndField = (JRField) fieldsMap.get("Sivilstatus3mnd");
			JRField SlagdiagnoseField = (JRField) fieldsMap.get("Slagdiagnose");
			JRField SpraakTaleproblemField = (JRField) fieldsMap.get("SpraakTaleproblem");
			JRField VaaknetMedSymptomField = (JRField) fieldsMap.get("VaaknetMedSymptom");
			JRField TimerSymptomdebutInnleggField = (JRField) fieldsMap.get("TimerSymptomdebutInnlegg");
			JRField TilfredshetField = (JRField) fieldsMap.get("Tilfredshet");
			JRField ToalettbesokPreField = (JRField) fieldsMap.get("ToalettbesokPre");
			JRField Toalettbesok3mndField = (JRField) fieldsMap.get("Toalettbesok3mnd");
			JRField TrombolyseField = (JRField) fieldsMap.get("Trombolyse");
			JRField TrombektomiField = (JRField) fieldsMap.get("Trombektomi");
			JRField UtA2AntagonistField = (JRField) fieldsMap.get("UtA2Antagonist");
			JRField UtACEhemmerField = (JRField) fieldsMap.get("UtACEhemmer");
			JRField UtASAField = (JRField) fieldsMap.get("UtASA");
			JRField UtBetablokkerField = (JRField) fieldsMap.get("UtBetablokker");
			JRField UtDipyridamolField = (JRField) fieldsMap.get("UtDipyridamol");
			JRField UtDiureticaField = (JRField) fieldsMap.get("UtDiuretica");
			JRField UtKalsiumantagonistField = (JRField) fieldsMap.get("UtKalsiumantagonist");
			JRField UtKlopidogrelField = (JRField) fieldsMap.get("UtKlopidogrel");
			JRField UtStatinerLipidField = (JRField) fieldsMap.get("UtStatinerLipid");
			JRField UtWarfarinField = (JRField) fieldsMap.get("UtWarfarin");
			JRField UtAndreEnnWarfarinField = (JRField) fieldsMap.get("UtAndreEnnWarfarin");
			JRField UtPlatehemField = (JRField) fieldsMap.get("UtPlatehem");
			JRField UtAntikoagField = (JRField) fieldsMap.get("UtAntikoag");
			JRField UtBTsenkField = (JRField) fieldsMap.get("UtBTsenk");
			JRField UtskrTilField = (JRField) fieldsMap.get("UtskrTil");
			JRField AarsakManglendeOppfField = (JRField) fieldsMap.get("AarsakManglendeOppf");
			JRField AlderField = (JRField) fieldsMap.get("Alder");
			JRField DagerSymptDebutTilOppfField = (JRField) fieldsMap.get("DagerSymptDebutTilOppf");
			JRField NIHSSinnkomstField = (JRField) fieldsMap.get("NIHSSinnkomst");
			JRField NIHSSpreTrombolyseField = (JRField) fieldsMap.get("NIHSSpreTrombolyse");
			JRField NIHSSetterTrombolyseField = (JRField) fieldsMap.get("NIHSSetterTrombolyse");
			JRField NIHSSpreTrombektomiField = (JRField) fieldsMap.get("NIHSSpreTrombektomi");
			JRField NIHSSetterTrombektomiField = (JRField) fieldsMap.get("NIHSSetterTrombektomi");
			JRField DagerInnleggelseTilDodField = (JRField) fieldsMap.get("DagerInnleggelseTilDod");
			JRField TransportmetodeField = (JRField) fieldsMap.get("Transportmetode");
			
			
			// Create "slug arrays" with very big sizes (default limit to
			// 1000000) to accommodate large queries
			// We cannot find out how many rows are returned so we have to fetch
			// first and then
			// rebuild arrays of the proper size before passing to R
			//
			// Arrays MUST be defined as objects and not its primitive since
			// returned values do contain 'null'
			
			log.debug("Making empty slug array...");
			
			Double[] sReshId = new Double[100000];
			String[] sRHF = new String[100000];
			String[] sAvdeling = new String[100000];
			String[] sKjonn = new String[100000];
			String[] sInnleggelsestidspunkt = new String[100000];
			String[] sSymptomdebut = new String[100000];
			String[] sTrombolyseStarttid = new String[100000];
			Double[] sTidInnTrombolyse = new Double[100000];
			String[] sUtskrivingsdato = new String[100000];
			Double[] sAtaksi = new Double[100000];
			Double[] sDobbeltsyn = new Double[100000];
			Double[] sNIHSSikkeUtfort = new Double[100000];
			Double[] sNeglekt = new Double[100000];
			Double[] sSensibilitetsutfall = new Double[100000];
			Double[] sSynsfeltutfall = new Double[100000];
			Double[] sTrombolyseIngen = new Double[100000];
			Double[] sUtIngenMedikam = new Double[100000];
			Double[] sVertigo = new Double[100000];
			Double[] sAntDagerInnl = new Double[100000];
			Double[] sAndreFokaleSympt = new Double[100000];
			Double[] sArmparese = new Double[100000];
			Double[] sAtrieflimmer = new Double[100000];
			Double[] sAvdForstInnlagt = new Double[100000];
			Double[] sAvdForstInnlagtHvilken = new Double[100000];
			Double[] sAvdUtskrFra = new Double[100000];
			Double[] sAvdUtskrFraHvilken = new Double[100000];
			Double[] sBeinparese = new Double[100000];
			Double[] sBevissthetsgradInnleggelse = new Double[100000];
			Double[] sBildediagnostikkHjerne = new Double[100000];
			Double[] sBildediagnostikkHjerte = new Double[100000];
			Double[] sRegistreringHjerterytme = new Double[100000];
			Double[] sBildediagnostikkIntraraniell = new Double[100000];
			Double[] sBildediagnostikkEkstrakranKar = new Double[100000];
			Double[] sBoligforholdPre = new Double[100000];
			Double[] sBoligforhold3mnd = new Double[100000];
			Double[] sBosituasjonPre = new Double[100000];
			Double[] sBosituasjon3mnd = new Double[100000];
			Double[] sSvelgtestUtfort = new Double[100000];
			Double[] sFacialisparese = new Double[100000];
			Double[] sForflytningPre = new Double[100000];
			Double[] sForflytning3mnd = new Double[100000];
			Double[] sOppfolgUtf = new Double[100000];
			Double[] sPaakledningPre = new Double[100000];
			Double[] sPaakledning3mnd = new Double[100000];
			Double[] sMRSPre = new Double[100000];
			Double[] sMRS3mnd = new Double[100000];
			Double[] sRoykerPre = new Double[100000];
			Double[] sRoyker3mnd = new Double[100000];
			Double[] sYrkesaktivUnderHjerneslag2 = new Double[100000];
			Double[] sYrkesaktivNaa = new Double[100000];
			Double[] sKjorteBilForHjerneslag = new Double[100000];
			Double[] sKjorerBilNaa = new Double[100000];
			Double[] sSivilstatusPre = new Double[100000];
			Double[] sSivilstatus3mnd = new Double[100000];
			Double[] sSlagdiagnose = new Double[100000];
			Double[] sSpraakTaleproblem = new Double[100000];
			Double[] sVaaknetMedSymptom = new Double[100000];
			Double[] sTimerSymptomdebutInnlegg = new Double[100000];
			Double[] sTilfredshet = new Double[100000];
			Double[] sToalettbesokPre = new Double[100000];
			Double[] sToalettbesok3mnd = new Double[100000];
			Double[] sTrombolyse = new Double[100000];
			Double[] sTrombektomi = new Double[100000];
			Double[] sUtA2Antagonist = new Double[100000];
			Double[] sUtACEhemmer = new Double[100000];
			Double[] sUtASA = new Double[100000];
			Double[] sUtBetablokker = new Double[100000];
			Double[] sUtDipyridamol = new Double[100000];
			Double[] sUtDiuretica = new Double[100000];
			Double[] sUtKalsiumantagonist = new Double[100000];
			Double[] sUtKlopidogrel = new Double[100000];
			Double[] sUtStatinerLipid = new Double[100000];
			Double[] sUtWarfarin = new Double[100000];
			Double[] sUtAndreEnnWarfarin = new Double[100000];
			Double[] sUtPlatehem = new Double[100000];
			Double[] sUtAntikoag = new Double[100000];
			Double[] sUtBTsenk = new Double[100000];
			Double[] sUtskrTil = new Double[100000];
			Double[] sAarsakManglendeOppf = new Double[100000];
			Double[] sAlder = new Double[100000];
			Double[] sDagerSymptDebutTilOppf = new Double[100000];
			Double[] sNIHSSinnkomst = new Double[100000];
			Double[] sNIHSSpreTrombolyse = new Double[100000];
			Double[] sNIHSSetterTrombolyse = new Double[100000];
			Double[] sNIHSSpreTrombektomi = new Double[100000];
			Double[] sNIHSSetterTrombektomi = new Double[100000];
			Double[] sDagerInnleggelseTilDod = new Double[100000];
			Double[] sTransportmetode = new Double[100000];


			
			log.debug("populating slug array with report data...");
			
			int rowidx = 0;
			// Assume we get 1 row
			boolean getRow = true;
			while (getRow) {
				sReshId[rowidx] = (Double) ds.getFieldValue(ReshIdField);
				sRHF[rowidx] = (String) ds.getFieldValue(RHFField);
				sAvdeling[rowidx] = (String) ds.getFieldValue(AvdelingField);
				sKjonn[rowidx] = (String) ds.getFieldValue(KjonnField);
				sInnleggelsestidspunkt[rowidx] = (String) ds.getFieldValue(InnleggelsestidspunktField);
				sSymptomdebut[rowidx] = (String) ds.getFieldValue(SymptomdebutField);
				sTrombolyseStarttid[rowidx] = (String) ds.getFieldValue(TrombolyseStarttidField);
				sTidInnTrombolyse[rowidx] = (Double) ds.getFieldValue(TidInnTrombolyseField);
				sUtskrivingsdato[rowidx] = (String) ds.getFieldValue(UtskrivingsdatoField);
				sAtaksi[rowidx] = (Double) ds.getFieldValue(AtaksiField);
				sDobbeltsyn[rowidx] = (Double) ds.getFieldValue(DobbeltsynField);
				sNIHSSikkeUtfort[rowidx] = (Double) ds.getFieldValue(NIHSSikkeUtfortField);
				sNeglekt[rowidx] = (Double) ds.getFieldValue(NeglektField);
				sSensibilitetsutfall[rowidx] = (Double) ds.getFieldValue(SensibilitetsutfallField);
				sSynsfeltutfall[rowidx] = (Double) ds.getFieldValue(SynsfeltutfallField);
				sTrombolyseIngen[rowidx] = (Double) ds.getFieldValue(TrombolyseIngenField);
				sUtIngenMedikam[rowidx] = (Double) ds.getFieldValue(UtIngenMedikamField);
				sVertigo[rowidx] = (Double) ds.getFieldValue(VertigoField);
				sAntDagerInnl[rowidx] = (Double) ds.getFieldValue(AntDagerInnlField);
				sAndreFokaleSympt[rowidx] = (Double) ds.getFieldValue(AndreFokaleSymptField);
				sArmparese[rowidx] = (Double) ds.getFieldValue(ArmpareseField);
				sAtrieflimmer[rowidx] = (Double) ds.getFieldValue(AtrieflimmerField);
				sAvdForstInnlagt[rowidx] = (Double) ds.getFieldValue(AvdForstInnlagtField);
				sAvdForstInnlagtHvilken[rowidx] = (Double) ds.getFieldValue(AvdForstInnlagtHvilkenField);
				sAvdUtskrFra[rowidx] = (Double) ds.getFieldValue(AvdUtskrFraField);
				sAvdUtskrFraHvilken[rowidx] = (Double) ds.getFieldValue(AvdUtskrFraHvilkenField);
				sBeinparese[rowidx] = (Double) ds.getFieldValue(BeinpareseField);
				sBevissthetsgradInnleggelse[rowidx] = (Double) ds.getFieldValue(BevissthetsgradInnleggelseField);
				sBildediagnostikkHjerne[rowidx] = (Double) ds.getFieldValue(BildediagnostikkHjerneField);
				sBildediagnostikkHjerte[rowidx] = (Double) ds.getFieldValue(BildediagnostikkHjerteField);
				sRegistreringHjerterytme[rowidx] = (Double) ds.getFieldValue(RegistreringHjerterytmeField);
				sBildediagnostikkIntraraniell[rowidx] = (Double) ds.getFieldValue(BildediagnostikkIntraraniellField);
				sBildediagnostikkEkstrakranKar[rowidx] = (Double) ds.getFieldValue(BildediagnostikkEkstrakranKarField);
				sBoligforholdPre[rowidx] = (Double) ds.getFieldValue(BoligforholdPreField);
				sBoligforhold3mnd[rowidx] = (Double) ds.getFieldValue(Boligforhold3mndField);
				sBosituasjonPre[rowidx] = (Double) ds.getFieldValue(BosituasjonPreField);
				sBosituasjon3mnd[rowidx] = (Double) ds.getFieldValue(Bosituasjon3mndField);
				sSvelgtestUtfort[rowidx] = (Double) ds.getFieldValue(SvelgtestUtfortField);
				sFacialisparese[rowidx] = (Double) ds.getFieldValue(FacialispareseField);
				sForflytningPre[rowidx] = (Double) ds.getFieldValue(ForflytningPreField);
				sForflytning3mnd[rowidx] = (Double) ds.getFieldValue(Forflytning3mndField);
				sOppfolgUtf[rowidx] = (Double) ds.getFieldValue(OppfolgUtfField);
				sPaakledningPre[rowidx] = (Double) ds.getFieldValue(PaakledningPreField);
				sPaakledning3mnd[rowidx] = (Double) ds.getFieldValue(Paakledning3mndField);
				sMRSPre[rowidx] = (Double) ds.getFieldValue(MRSPreField);
				sMRS3mnd[rowidx] = (Double) ds.getFieldValue(MRS3mndField);
				sRoykerPre[rowidx] = (Double) ds.getFieldValue(RoykerPreField);
				sRoyker3mnd[rowidx] = (Double) ds.getFieldValue(Royker3mndField);
				sYrkesaktivUnderHjerneslag2[rowidx] = (Double) ds.getFieldValue(YrkesaktivUnderHjerneslag2Field);
				sYrkesaktivNaa[rowidx] = (Double) ds.getFieldValue(YrkesaktivNaaField);
				sKjorteBilForHjerneslag[rowidx] = (Double) ds.getFieldValue(KjorteBilForHjerneslagField);
				sKjorerBilNaa[rowidx] = (Double) ds.getFieldValue(KjorerBilNaaField);
				sSivilstatusPre[rowidx] = (Double) ds.getFieldValue(SivilstatusPreField);
				sSivilstatus3mnd[rowidx] = (Double) ds.getFieldValue(Sivilstatus3mndField);
				sSlagdiagnose[rowidx] = (Double) ds.getFieldValue(SlagdiagnoseField);
				sSpraakTaleproblem[rowidx] = (Double) ds.getFieldValue(SpraakTaleproblemField);
				sVaaknetMedSymptom[rowidx] = (Double) ds.getFieldValue(VaaknetMedSymptomField);
				sTimerSymptomdebutInnlegg[rowidx] = (Double) ds.getFieldValue(TimerSymptomdebutInnleggField);
				sTilfredshet[rowidx] = (Double) ds.getFieldValue(TilfredshetField);
				sToalettbesokPre[rowidx] = (Double) ds.getFieldValue(ToalettbesokPreField);
				sToalettbesok3mnd[rowidx] = (Double) ds.getFieldValue(Toalettbesok3mndField);
				sTrombolyse[rowidx] = (Double) ds.getFieldValue(TrombolyseField);
				sTrombektomi[rowidx] = (Double) ds.getFieldValue(TrombektomiField);
				sUtA2Antagonist[rowidx] = (Double) ds.getFieldValue(UtA2AntagonistField);
				sUtACEhemmer[rowidx] = (Double) ds.getFieldValue(UtACEhemmerField);
				sUtASA[rowidx] = (Double) ds.getFieldValue(UtASAField);
				sUtBetablokker[rowidx] = (Double) ds.getFieldValue(UtBetablokkerField);
				sUtDipyridamol[rowidx] = (Double) ds.getFieldValue(UtDipyridamolField);
				sUtDiuretica[rowidx] = (Double) ds.getFieldValue(UtDiureticaField);
				sUtKalsiumantagonist[rowidx] = (Double) ds.getFieldValue(UtKalsiumantagonistField);
				sUtKlopidogrel[rowidx] = (Double) ds.getFieldValue(UtKlopidogrelField);
				sUtStatinerLipid[rowidx] = (Double) ds.getFieldValue(UtStatinerLipidField);
				sUtWarfarin[rowidx] = (Double) ds.getFieldValue(UtWarfarinField);
				sUtAndreEnnWarfarin[rowidx] = (Double) ds.getFieldValue(UtAndreEnnWarfarinField);
				sUtPlatehem[rowidx] = (Double) ds.getFieldValue(UtPlatehemField);
				sUtAntikoag[rowidx] = (Double) ds.getFieldValue(UtAntikoagField);
				sUtBTsenk[rowidx] = (Double) ds.getFieldValue(UtBTsenkField);
				sUtskrTil[rowidx] = (Double) ds.getFieldValue(UtskrTilField);
				sAarsakManglendeOppf[rowidx] = (Double) ds.getFieldValue(AarsakManglendeOppfField);
				sAlder[rowidx] = (Double) ds.getFieldValue(AlderField);
				sDagerSymptDebutTilOppf[rowidx] = (Double) ds.getFieldValue(DagerSymptDebutTilOppfField);
				sNIHSSinnkomst[rowidx] = (Double) ds.getFieldValue(NIHSSinnkomstField);
				sNIHSSpreTrombolyse[rowidx] = (Double) ds.getFieldValue(NIHSSpreTrombolyseField);
				sNIHSSetterTrombolyse[rowidx] = (Double) ds.getFieldValue(NIHSSetterTrombolyseField);
				sNIHSSpreTrombektomi[rowidx] = (Double) ds.getFieldValue(NIHSSpreTrombektomiField);
				sNIHSSetterTrombektomi[rowidx] = (Double) ds.getFieldValue(NIHSSetterTrombektomiField);
				sDagerInnleggelseTilDod[rowidx] = (Double) ds.getFieldValue(DagerInnleggelseTilDodField);
				sTransportmetode[rowidx] = (Double) ds.getFieldValue(TransportmetodeField);
				getRow = ds.next();
				rowidx++;
			}
			rowidx--;

			
			log.debug("Slug array filled with " + rowidx + " records from report data");

			
			log.debug("Creating proper sized array...");
			// Create and populate properly sized arrays

			double[] ReshId = new double[rowidx + 1];
			String[] RHF = new String[rowidx + 1];
			String[] Avdeling = new String[rowidx + 1];
			String[] Kjonn = new String[rowidx + 1];
			String[] Innleggelsestidspunkt = new String[rowidx + 1];
			String[] Symptomdebut = new String[rowidx + 1];
			String[] TrombolyseStarttid = new String[rowidx + 1];
			double[] TidInnTrombolyse = new double[rowidx + 1];
			String[] Utskrivingsdato = new String[rowidx + 1];
			double[] Ataksi = new double[rowidx + 1];
			double[] Dobbeltsyn = new double[rowidx + 1];
			double[] NIHSSikkeUtfort = new double[rowidx + 1];
			double[] Neglekt = new double[rowidx + 1];
			double[] Sensibilitetsutfall = new double[rowidx + 1];
			double[] Synsfeltutfall = new double[rowidx + 1];
			double[] TrombolyseIngen = new double[rowidx + 1];
			double[] UtIngenMedikam = new double[rowidx + 1];
			double[] Vertigo = new double[rowidx + 1];
			double[] AntDagerInnl = new double[rowidx + 1];
			double[] AndreFokaleSympt = new double[rowidx + 1];
			double[] Armparese = new double[rowidx + 1];
			double[] Atrieflimmer = new double[rowidx + 1];
			double[] AvdForstInnlagt = new double[rowidx + 1];
			double[] AvdForstInnlagtHvilken = new double[rowidx + 1];
			double[] AvdUtskrFra = new double[rowidx + 1];
			double[] AvdUtskrFraHvilken = new double[rowidx + 1];
			double[] Beinparese = new double[rowidx + 1];
			double[] BevissthetsgradInnleggelse = new double[rowidx + 1];
			double[] BildediagnostikkHjerne = new double[rowidx + 1];
			double[] BildediagnostikkHjerte = new double[rowidx + 1];
			double[] RegistreringHjerterytme = new double[rowidx + 1];
			double[] BildediagnostikkIntraraniell = new double[rowidx + 1];
			double[] BildediagnostikkEkstrakranKar = new double[rowidx + 1];
			double[] BoligforholdPre = new double[rowidx + 1];
			double[] Boligforhold3mnd = new double[rowidx + 1];
			double[] BosituasjonPre = new double[rowidx + 1];
			double[] Bosituasjon3mnd = new double[rowidx + 1];
			double[] SvelgtestUtfort = new double[rowidx + 1];
			double[] Facialisparese = new double[rowidx + 1];
			double[] ForflytningPre = new double[rowidx + 1];
			double[] Forflytning3mnd = new double[rowidx + 1];
			double[] OppfolgUtf = new double[rowidx + 1];
			double[] PaakledningPre = new double[rowidx + 1];
			double[] Paakledning3mnd = new double[rowidx + 1];
			double[] MRSPre = new double[rowidx + 1];
			double[] MRS3mnd = new double[rowidx + 1];
			double[] RoykerPre = new double[rowidx + 1];
			double[] Royker3mnd = new double[rowidx + 1];
			double[] YrkesaktivUnderHjerneslag2 = new double[rowidx + 1];
			double[] YrkesaktivNaa = new double[rowidx + 1];
			double[] KjorteBilForHjerneslag = new double[rowidx + 1];
			double[] KjorerBilNaa = new double[rowidx + 1];
			double[] SivilstatusPre = new double[rowidx + 1];
			double[] Sivilstatus3mnd = new double[rowidx + 1];
			double[] Slagdiagnose = new double[rowidx + 1];
			double[] SpraakTaleproblem = new double[rowidx + 1];
			double[] VaaknetMedSymptom = new double[rowidx + 1];
			double[] TimerSymptomdebutInnlegg = new double[rowidx + 1];
			double[] Tilfredshet = new double[rowidx + 1];
			double[] ToalettbesokPre = new double[rowidx + 1];
			double[] Toalettbesok3mnd = new double[rowidx + 1];
			double[] Trombolyse = new double[rowidx + 1];
			double[] Trombektomi = new double[rowidx + 1];
			double[] UtA2Antagonist = new double[rowidx + 1];
			double[] UtACEhemmer = new double[rowidx + 1];
			double[] UtASA = new double[rowidx + 1];
			double[] UtBetablokker = new double[rowidx + 1];
			double[] UtDipyridamol = new double[rowidx + 1];
			double[] UtDiuretica = new double[rowidx + 1];
			double[] UtKalsiumantagonist = new double[rowidx + 1];
			double[] UtKlopidogrel = new double[rowidx + 1];
			double[] UtStatinerLipid = new double[rowidx + 1];
			double[] UtWarfarin = new double[rowidx + 1];
			double[] UtAndreEnnWarfarin = new double[rowidx + 1];
			double[] UtPlatehem = new double[rowidx + 1];
			double[] UtAntikoag = new double[rowidx + 1];
			double[] UtBTsenk = new double[rowidx + 1];
			double[] UtskrTil = new double[rowidx + 1];
			double[] AarsakManglendeOppf = new double[rowidx + 1];
			double[] Alder = new double[rowidx + 1];
			double[] DagerSymptDebutTilOppf = new double[rowidx + 1];
			double[] NIHSSinnkomst = new double[rowidx + 1];
			double[] NIHSSpreTrombolyse = new double[rowidx + 1];
			double[] NIHSSetterTrombolyse = new double[rowidx + 1];
			double[] NIHSSpreTrombektomi = new double[rowidx + 1];
			double[] NIHSSetterTrombektomi = new double[rowidx + 1];
			double[] DagerInnleggelseTilDod = new double[rowidx + 1];
			double[] Transportmetode = new double[rowidx + 1];



			
			// ifs are needed because underlying query returns null. Since ints
			// cannot be null, these are returned as type double by the query
			log.debug("Populating proper sized array with data from slug array, also checking for NULLs...");
			int i = 0;
			while (i <= rowidx) {
				if (sReshId[i] == null) {
					ReshId[i] = java.lang.Double.NaN;
				}
				else {
					ReshId[i] = sReshId[i];
				}

				RHF[i] = sRHF[i];
				Avdeling[i] = sAvdeling[i];
				Kjonn[i] = sKjonn[i];
				Innleggelsestidspunkt[i] = sInnleggelsestidspunkt[i];
				Symptomdebut[i] = sSymptomdebut[i];
				TrombolyseStarttid[i] = sTrombolyseStarttid[i];
				if (sTidInnTrombolyse[i] == null) {
					TidInnTrombolyse[i] = java.lang.Double.NaN;
				}
				else {
					TidInnTrombolyse[i] = sTidInnTrombolyse[i];
				}

				Utskrivingsdato[i] = sUtskrivingsdato[i];
				if (sAtaksi[i] == null) {
					Ataksi[i] = java.lang.Double.NaN;
				}
				else {
					Ataksi[i] = sAtaksi[i];
				}

				if (sDobbeltsyn[i] == null) {
					Dobbeltsyn[i] = java.lang.Double.NaN;
				}
				else {
					Dobbeltsyn[i] = sDobbeltsyn[i];
				}

				if (sNIHSSikkeUtfort[i] == null) {
					NIHSSikkeUtfort[i] = java.lang.Double.NaN;
				}
				else {
					NIHSSikkeUtfort[i] = sNIHSSikkeUtfort[i];
				}

				if (sNeglekt[i] == null) {
					Neglekt[i] = java.lang.Double.NaN;
				}
				else {
					Neglekt[i] = sNeglekt[i];
				}

				if (sSensibilitetsutfall[i] == null) {
					Sensibilitetsutfall[i] = java.lang.Double.NaN;
				}
				else {
					Sensibilitetsutfall[i] = sSensibilitetsutfall[i];
				}

				if (sSynsfeltutfall[i] == null) {
					Synsfeltutfall[i] = java.lang.Double.NaN;
				}
				else {
					Synsfeltutfall[i] = sSynsfeltutfall[i];
				}

				if (sTrombolyseIngen[i] == null) {
					TrombolyseIngen[i] = java.lang.Double.NaN;
				}
				else {
					TrombolyseIngen[i] = sTrombolyseIngen[i];
				}

				if (sUtIngenMedikam[i] == null) {
					UtIngenMedikam[i] = java.lang.Double.NaN;
				}
				else {
					UtIngenMedikam[i] = sUtIngenMedikam[i];
				}

				if (sVertigo[i] == null) {
					Vertigo[i] = java.lang.Double.NaN;
				}
				else {
					Vertigo[i] = sVertigo[i];
				}

				if (sAntDagerInnl[i] == null) {
					AntDagerInnl[i] = java.lang.Double.NaN;
				}
				else {
					AntDagerInnl[i] = sAntDagerInnl[i];
				}

				if (sAndreFokaleSympt[i] == null) {
					AndreFokaleSympt[i] = java.lang.Double.NaN;
				}
				else {
					AndreFokaleSympt[i] = sAndreFokaleSympt[i];
				}

				if (sArmparese[i] == null) {
					Armparese[i] = java.lang.Double.NaN;
				}
				else {
					Armparese[i] = sArmparese[i];
				}

				if (sAtrieflimmer[i] == null) {
					Atrieflimmer[i] = java.lang.Double.NaN;
				}
				else {
					Atrieflimmer[i] = sAtrieflimmer[i];
				}

				if (sAvdForstInnlagt[i] == null) {
					AvdForstInnlagt[i] = java.lang.Double.NaN;
				}
				else {
					AvdForstInnlagt[i] = sAvdForstInnlagt[i];
				}

				if (sAvdForstInnlagtHvilken[i] == null) {
					AvdForstInnlagtHvilken[i] = java.lang.Double.NaN;
				}
				else {
					AvdForstInnlagtHvilken[i] = sAvdForstInnlagtHvilken[i];
				}

				if (sAvdUtskrFra[i] == null) {
					AvdUtskrFra[i] = java.lang.Double.NaN;
				}
				else {
					AvdUtskrFra[i] = sAvdUtskrFra[i];
				}

				if (sAvdUtskrFraHvilken[i] == null) {
					AvdUtskrFraHvilken[i] = java.lang.Double.NaN;
				}
				else {
					AvdUtskrFraHvilken[i] = sAvdUtskrFraHvilken[i];
				}

				if (sBeinparese[i] == null) {
					Beinparese[i] = java.lang.Double.NaN;
				}
				else {
					Beinparese[i] = sBeinparese[i];
				}

				if (sBevissthetsgradInnleggelse[i] == null) {
					BevissthetsgradInnleggelse[i] = java.lang.Double.NaN;
				}
				else {
					BevissthetsgradInnleggelse[i] = sBevissthetsgradInnleggelse[i];
				}

				if (sBildediagnostikkHjerne[i] == null) {
					BildediagnostikkHjerne[i] = java.lang.Double.NaN;
				}
				else {
					BildediagnostikkHjerne[i] = sBildediagnostikkHjerne[i];
				}

				if (sBildediagnostikkHjerte[i] == null) {
					BildediagnostikkHjerte[i] = java.lang.Double.NaN;
				}
				else {
					BildediagnostikkHjerte[i] = sBildediagnostikkHjerte[i];
				}

				if (sRegistreringHjerterytme[i] == null) {
					RegistreringHjerterytme[i] = java.lang.Double.NaN;
				}
				else {
					RegistreringHjerterytme[i] = sRegistreringHjerterytme[i];
				}

				if (sBildediagnostikkIntraraniell[i] == null) {
					BildediagnostikkIntraraniell[i] = java.lang.Double.NaN;
				}
				else {
					BildediagnostikkIntraraniell[i] = sBildediagnostikkIntraraniell[i];
				}

				if (sBildediagnostikkEkstrakranKar[i] == null) {
					BildediagnostikkEkstrakranKar[i] = java.lang.Double.NaN;
				}
				else {
					BildediagnostikkEkstrakranKar[i] = sBildediagnostikkEkstrakranKar[i];
				}

				if (sBoligforholdPre[i] == null) {
					BoligforholdPre[i] = java.lang.Double.NaN;
				}
				else {
					BoligforholdPre[i] = sBoligforholdPre[i];
				}

				if (sBoligforhold3mnd[i] == null) {
					Boligforhold3mnd[i] = java.lang.Double.NaN;
				}
				else {
					Boligforhold3mnd[i] = sBoligforhold3mnd[i];
				}

				if (sBosituasjonPre[i] == null) {
					BosituasjonPre[i] = java.lang.Double.NaN;
				}
				else {
					BosituasjonPre[i] = sBosituasjonPre[i];
				}

				if (sBosituasjon3mnd[i] == null) {
					Bosituasjon3mnd[i] = java.lang.Double.NaN;
				}
				else {
					Bosituasjon3mnd[i] = sBosituasjon3mnd[i];
				}

				if (sSvelgtestUtfort[i] == null) {
					SvelgtestUtfort[i] = java.lang.Double.NaN;
				}
				else {
					SvelgtestUtfort[i] = sSvelgtestUtfort[i];
				}

				if (sFacialisparese[i] == null) {
					Facialisparese[i] = java.lang.Double.NaN;
				}
				else {
					Facialisparese[i] = sFacialisparese[i];
				}

				if (sForflytningPre[i] == null) {
					ForflytningPre[i] = java.lang.Double.NaN;
				}
				else {
					ForflytningPre[i] = sForflytningPre[i];
				}

				if (sForflytning3mnd[i] == null) {
					Forflytning3mnd[i] = java.lang.Double.NaN;
				}
				else {
					Forflytning3mnd[i] = sForflytning3mnd[i];
				}

				if (sOppfolgUtf[i] == null) {
					OppfolgUtf[i] = java.lang.Double.NaN;
				}
				else {
					OppfolgUtf[i] = sOppfolgUtf[i];
				}

				if (sPaakledningPre[i] == null) {
					PaakledningPre[i] = java.lang.Double.NaN;
				}
				else {
					PaakledningPre[i] = sPaakledningPre[i];
				}

				if (sPaakledning3mnd[i] == null) {
					Paakledning3mnd[i] = java.lang.Double.NaN;
				}
				else {
					Paakledning3mnd[i] = sPaakledning3mnd[i];
				}

				if (sMRSPre[i] == null) {
					MRSPre[i] = java.lang.Double.NaN;
				}
				else {
					MRSPre[i] = sMRSPre[i];
				}

				if (sMRS3mnd[i] == null) {
					MRS3mnd[i] = java.lang.Double.NaN;
				}
				else {
					MRS3mnd[i] = sMRS3mnd[i];
				}

				if (sRoykerPre[i] == null) {
					RoykerPre[i] = java.lang.Double.NaN;
				}
				else {
					RoykerPre[i] = sRoykerPre[i];
				}

				if (sRoyker3mnd[i] == null) {
					Royker3mnd[i] = java.lang.Double.NaN;
				}
				else {
					Royker3mnd[i] = sRoyker3mnd[i];
				}

				if (sYrkesaktivUnderHjerneslag2[i] == null) {
					YrkesaktivUnderHjerneslag2[i] = java.lang.Double.NaN;
				}
				else {
					YrkesaktivUnderHjerneslag2[i] = sYrkesaktivUnderHjerneslag2[i];
				}

				if (sYrkesaktivNaa[i] == null) {
					YrkesaktivNaa[i] = java.lang.Double.NaN;
				}
				else {
					YrkesaktivNaa[i] = sYrkesaktivNaa[i];
				}

				if (sKjorteBilForHjerneslag[i] == null) {
					KjorteBilForHjerneslag[i] = java.lang.Double.NaN;
				}
				else {
					KjorteBilForHjerneslag[i] = sKjorteBilForHjerneslag[i];
				}

				if (sKjorerBilNaa[i] == null) {
					KjorerBilNaa[i] = java.lang.Double.NaN;
				}
				else {
					KjorerBilNaa[i] = sKjorerBilNaa[i];
				}

				if (sSivilstatusPre[i] == null) {
					SivilstatusPre[i] = java.lang.Double.NaN;
				}
				else {
					SivilstatusPre[i] = sSivilstatusPre[i];
				}

				if (sSivilstatus3mnd[i] == null) {
					Sivilstatus3mnd[i] = java.lang.Double.NaN;
				}
				else {
					Sivilstatus3mnd[i] = sSivilstatus3mnd[i];
				}

				if (sSlagdiagnose[i] == null) {
					Slagdiagnose[i] = java.lang.Double.NaN;
				}
				else {
					Slagdiagnose[i] = sSlagdiagnose[i];
				}

				if (sSpraakTaleproblem[i] == null) {
					SpraakTaleproblem[i] = java.lang.Double.NaN;
				}
				else {
					SpraakTaleproblem[i] = sSpraakTaleproblem[i];
				}

				if (sVaaknetMedSymptom[i] == null) {
					VaaknetMedSymptom[i] = java.lang.Double.NaN;
				}
				else {
					VaaknetMedSymptom[i] = sVaaknetMedSymptom[i];
				}

				if (sTimerSymptomdebutInnlegg[i] == null) {
					TimerSymptomdebutInnlegg[i] = java.lang.Double.NaN;
				}
				else {
					TimerSymptomdebutInnlegg[i] = sTimerSymptomdebutInnlegg[i];
				}

				if (sTilfredshet[i] == null) {
					Tilfredshet[i] = java.lang.Double.NaN;
				}
				else {
					Tilfredshet[i] = sTilfredshet[i];
				}

				if (sToalettbesokPre[i] == null) {
					ToalettbesokPre[i] = java.lang.Double.NaN;
				}
				else {
					ToalettbesokPre[i] = sToalettbesokPre[i];
				}

				if (sToalettbesok3mnd[i] == null) {
					Toalettbesok3mnd[i] = java.lang.Double.NaN;
				}
				else {
					Toalettbesok3mnd[i] = sToalettbesok3mnd[i];
				}

				if (sTrombolyse[i] == null) {
					Trombolyse[i] = java.lang.Double.NaN;
				}
				else {
					Trombolyse[i] = sTrombolyse[i];
				}

				if (sTrombektomi[i] == null) {
					Trombektomi[i] = java.lang.Double.NaN;
				}
				else {
					Trombektomi[i] = sTrombektomi[i];
				}

				if (sUtA2Antagonist[i] == null) {
					UtA2Antagonist[i] = java.lang.Double.NaN;
				}
				else {
					UtA2Antagonist[i] = sUtA2Antagonist[i];
				}

				if (sUtACEhemmer[i] == null) {
					UtACEhemmer[i] = java.lang.Double.NaN;
				}
				else {
					UtACEhemmer[i] = sUtACEhemmer[i];
				}

				if (sUtASA[i] == null) {
					UtASA[i] = java.lang.Double.NaN;
				}
				else {
					UtASA[i] = sUtASA[i];
				}

				if (sUtBetablokker[i] == null) {
					UtBetablokker[i] = java.lang.Double.NaN;
				}
				else {
					UtBetablokker[i] = sUtBetablokker[i];
				}

				if (sUtDipyridamol[i] == null) {
					UtDipyridamol[i] = java.lang.Double.NaN;
				}
				else {
					UtDipyridamol[i] = sUtDipyridamol[i];
				}

				if (sUtDiuretica[i] == null) {
					UtDiuretica[i] = java.lang.Double.NaN;
				}
				else {
					UtDiuretica[i] = sUtDiuretica[i];
				}

				if (sUtKalsiumantagonist[i] == null) {
					UtKalsiumantagonist[i] = java.lang.Double.NaN;
				}
				else {
					UtKalsiumantagonist[i] = sUtKalsiumantagonist[i];
				}

				if (sUtKlopidogrel[i] == null) {
					UtKlopidogrel[i] = java.lang.Double.NaN;
				}
				else {
					UtKlopidogrel[i] = sUtKlopidogrel[i];
				}

				if (sUtStatinerLipid[i] == null) {
					UtStatinerLipid[i] = java.lang.Double.NaN;
				}
				else {
					UtStatinerLipid[i] = sUtStatinerLipid[i];
				}

				if (sUtWarfarin[i] == null) {
					UtWarfarin[i] = java.lang.Double.NaN;
				}
				else {
					UtWarfarin[i] = sUtWarfarin[i];
				}

				if (sUtAndreEnnWarfarin[i] == null) {
					UtAndreEnnWarfarin[i] = java.lang.Double.NaN;
				}
				else {
					UtAndreEnnWarfarin[i] = sUtAndreEnnWarfarin[i];
				}

				if (sUtPlatehem[i] == null) {
					UtPlatehem[i] = java.lang.Double.NaN;
				}
				else {
					UtPlatehem[i] = sUtPlatehem[i];
				}

				if (sUtAntikoag[i] == null) {
					UtAntikoag[i] = java.lang.Double.NaN;
				}
				else {
					UtAntikoag[i] = sUtAntikoag[i];
				}

				if (sUtBTsenk[i] == null) {
					UtBTsenk[i] = java.lang.Double.NaN;
				}
				else {
					UtBTsenk[i] = sUtBTsenk[i];
				}

				if (sUtskrTil[i] == null) {
					UtskrTil[i] = java.lang.Double.NaN;
				}
				else {
					UtskrTil[i] = sUtskrTil[i];
				}

				if (sAarsakManglendeOppf[i] == null) {
					AarsakManglendeOppf[i] = java.lang.Double.NaN;
				}
				else {
					AarsakManglendeOppf[i] = sAarsakManglendeOppf[i];
				}

				if (sAlder[i] == null) {
					Alder[i] = java.lang.Double.NaN;
				}
				else {
					Alder[i] = sAlder[i];
				}

				if (sDagerSymptDebutTilOppf[i] == null) {
					DagerSymptDebutTilOppf[i] = java.lang.Double.NaN;
				}
				else {
					DagerSymptDebutTilOppf[i] = sDagerSymptDebutTilOppf[i];
				}

				if (sNIHSSinnkomst[i] == null) {
					NIHSSinnkomst[i] = java.lang.Double.NaN;
				}
				else {
					NIHSSinnkomst[i] = sNIHSSinnkomst[i];
				}

				if (sNIHSSpreTrombolyse[i] == null) {
					NIHSSpreTrombolyse[i] = java.lang.Double.NaN;
				}
				else {
					NIHSSpreTrombolyse[i] = sNIHSSpreTrombolyse[i];
				}

				if (sNIHSSetterTrombolyse[i] == null) {
					NIHSSetterTrombolyse[i] = java.lang.Double.NaN;
				}
				else {
					NIHSSetterTrombolyse[i] = sNIHSSetterTrombolyse[i];
				}

				if (sNIHSSpreTrombektomi[i] == null) {
					NIHSSpreTrombektomi[i] = java.lang.Double.NaN;
				}
				else {
					NIHSSpreTrombektomi[i] = sNIHSSpreTrombektomi[i];
				}

				if (sNIHSSetterTrombektomi[i] == null) {
					NIHSSetterTrombektomi[i] = java.lang.Double.NaN;
				}
				else {
					NIHSSetterTrombektomi[i] = sNIHSSetterTrombektomi[i];
				}

				if (sDagerInnleggelseTilDod[i] == null) {
					DagerInnleggelseTilDod[i] = java.lang.Double.NaN;
				}
				else {
					DagerInnleggelseTilDod[i] = sDagerInnleggelseTilDod[i];
				}

				if (sTransportmetode[i] == null) {
					Transportmetode[i] = java.lang.Double.NaN;
				}
				else {
					Transportmetode[i] = sTransportmetode[i];
				}

				i++;
			}

			
			log.debug("Creating the R dataframe...");

			RList l = new RList();
			l.put("ReshId", new REXPDouble(ReshId));
			l.put("RHF", new REXPString(RHF));
			l.put("Avdeling", new REXPString(Avdeling));
			l.put("Kjonn", new REXPString(Kjonn));
			l.put("Innleggelsestidspunkt", new REXPString(Innleggelsestidspunkt));
			l.put("Symptomdebut", new REXPString(Symptomdebut));
			l.put("TrombolyseStarttid", new REXPString(TrombolyseStarttid));
			l.put("TidInnTrombolyse", new REXPDouble(TidInnTrombolyse));
			l.put("Utskrivingsdato", new REXPString(Utskrivingsdato));
			l.put("Ataksi", new REXPDouble(Ataksi));
			l.put("Dobbeltsyn", new REXPDouble(Dobbeltsyn));
			l.put("NIHSSikkeUtfort", new REXPDouble(NIHSSikkeUtfort));
			l.put("Neglekt", new REXPDouble(Neglekt));
			l.put("Sensibilitetsutfall", new REXPDouble(Sensibilitetsutfall));
			l.put("Synsfeltutfall", new REXPDouble(Synsfeltutfall));
			l.put("TrombolyseIngen", new REXPDouble(TrombolyseIngen));
			l.put("UtIngenMedikam", new REXPDouble(UtIngenMedikam));
			l.put("Vertigo", new REXPDouble(Vertigo));
			l.put("AntDagerInnl", new REXPDouble(AntDagerInnl));
			l.put("AndreFokaleSympt", new REXPDouble(AndreFokaleSympt));
			l.put("Armparese", new REXPDouble(Armparese));
			l.put("Atrieflimmer", new REXPDouble(Atrieflimmer));
			l.put("AvdForstInnlagt", new REXPDouble(AvdForstInnlagt));
			l.put("AvdForstInnlagtHvilken", new REXPDouble(AvdForstInnlagtHvilken));
			l.put("AvdUtskrFra", new REXPDouble(AvdUtskrFra));
			l.put("AvdUtskrFraHvilken", new REXPDouble(AvdUtskrFraHvilken));
			l.put("Beinparese", new REXPDouble(Beinparese));
			l.put("BevissthetsgradInnleggelse", new REXPDouble(BevissthetsgradInnleggelse));
			l.put("BildediagnostikkHjerne", new REXPDouble(BildediagnostikkHjerne));
			l.put("BildediagnostikkHjerte", new REXPDouble(BildediagnostikkHjerte));
			l.put("RegistreringHjerterytme", new REXPDouble(RegistreringHjerterytme));
			l.put("BildediagnostikkIntraraniell", new REXPDouble(BildediagnostikkIntraraniell));
			l.put("BildediagnostikkEkstrakranKar", new REXPDouble(BildediagnostikkEkstrakranKar));
			l.put("BoligforholdPre", new REXPDouble(BoligforholdPre));
			l.put("Boligforhold3mnd", new REXPDouble(Boligforhold3mnd));
			l.put("BosituasjonPre", new REXPDouble(BosituasjonPre));
			l.put("Bosituasjon3mnd", new REXPDouble(Bosituasjon3mnd));
			l.put("SvelgtestUtfort", new REXPDouble(SvelgtestUtfort));
			l.put("Facialisparese", new REXPDouble(Facialisparese));
			l.put("ForflytningPre", new REXPDouble(ForflytningPre));
			l.put("Forflytning3mnd", new REXPDouble(Forflytning3mnd));
			l.put("OppfolgUtf", new REXPDouble(OppfolgUtf));
			l.put("PaakledningPre", new REXPDouble(PaakledningPre));
			l.put("Paakledning3mnd", new REXPDouble(Paakledning3mnd));
			l.put("MRSPre", new REXPDouble(MRSPre));
			l.put("MRS3mnd", new REXPDouble(MRS3mnd));
			l.put("RoykerPre", new REXPDouble(RoykerPre));
			l.put("Royker3mnd", new REXPDouble(Royker3mnd));
			l.put("YrkesaktivUnderHjerneslag2", new REXPDouble(YrkesaktivUnderHjerneslag2));
			l.put("YrkesaktivNaa", new REXPDouble(YrkesaktivNaa));
			l.put("KjorteBilForHjerneslag", new REXPDouble(KjorteBilForHjerneslag));
			l.put("KjorerBilNaa", new REXPDouble(KjorerBilNaa));
			l.put("SivilstatusPre", new REXPDouble(SivilstatusPre));
			l.put("Sivilstatus3mnd", new REXPDouble(Sivilstatus3mnd));
			l.put("Slagdiagnose", new REXPDouble(Slagdiagnose));
			l.put("SpraakTaleproblem", new REXPDouble(SpraakTaleproblem));
			l.put("VaaknetMedSymptom", new REXPDouble(VaaknetMedSymptom));
			l.put("TimerSymptomdebutInnlegg", new REXPDouble(TimerSymptomdebutInnlegg));
			l.put("Tilfredshet", new REXPDouble(Tilfredshet));
			l.put("ToalettbesokPre", new REXPDouble(ToalettbesokPre));
			l.put("Toalettbesok3mnd", new REXPDouble(Toalettbesok3mnd));
			l.put("Trombolyse", new REXPDouble(Trombolyse));
			l.put("Trombektomi", new REXPDouble(Trombektomi));
			l.put("UtA2Antagonist", new REXPDouble(UtA2Antagonist));
			l.put("UtACEhemmer", new REXPDouble(UtACEhemmer));
			l.put("UtASA", new REXPDouble(UtASA));
			l.put("UtBetablokker", new REXPDouble(UtBetablokker));
			l.put("UtDipyridamol", new REXPDouble(UtDipyridamol));
			l.put("UtDiuretica", new REXPDouble(UtDiuretica));
			l.put("UtKalsiumantagonist", new REXPDouble(UtKalsiumantagonist));
			l.put("UtKlopidogrel", new REXPDouble(UtKlopidogrel));
			l.put("UtStatinerLipid", new REXPDouble(UtStatinerLipid));
			l.put("UtWarfarin", new REXPDouble(UtWarfarin));
			l.put("UtAndreEnnWarfarin", new REXPDouble(UtAndreEnnWarfarin));
			l.put("UtPlatehem", new REXPDouble(UtPlatehem));
			l.put("UtAntikoag", new REXPDouble(UtAntikoag));
			l.put("UtBTsenk", new REXPDouble(UtBTsenk));
			l.put("UtskrTil", new REXPDouble(UtskrTil));
			l.put("AarsakManglendeOppf", new REXPDouble(AarsakManglendeOppf));
			l.put("Alder", new REXPDouble(Alder));
			l.put("DagerSymptDebutTilOppf", new REXPDouble(DagerSymptDebutTilOppf));
			l.put("NIHSSinnkomst", new REXPDouble(NIHSSinnkomst));
			l.put("NIHSSpreTrombolyse", new REXPDouble(NIHSSpreTrombolyse));
			l.put("NIHSSetterTrombolyse", new REXPDouble(NIHSSetterTrombolyse));
			l.put("NIHSSpreTrombektomi", new REXPDouble(NIHSSpreTrombektomi));
			l.put("NIHSSetterTrombektomi", new REXPDouble(NIHSSetterTrombektomi));
			l.put("DagerInnleggelseTilDod", new REXPDouble(DagerInnleggelseTilDod));
			l.put("Transportmetode", new REXPDouble(Transportmetode));
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
