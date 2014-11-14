/**
 * no.skde.report.hjerneslag
 * HjerneslagCommonScriptlet.java Nov 7 2013 Are Edvardsen
 * 
 * 
 *  Copyleft 2013, SKDE
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

public class HjerneslagCommonScriptlet extends JRDefaultScriptlet {

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
			
			// Deprecated, use orgUnitSelection
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
			
			// set path to library, to be removed since Rapporteket uses same directory for all R files (noweb, libs and report funs)
			String libkat = "'/opt/jasper/r/'";
			rconn.voidEval("libkat=" + libkat);
			
			log.debug("Getting Jasper Report data source...");
			
			// Load up primitive arrays with query data
			JRDataSource ds = (JRDataSource) ((JRFillParameter) parametersMap
					.get("REPORT_DATA_SOURCE")).getValue();
			
			log.debug("Getting Jasper Report Fields from report data source...");
			
			JRField PasientIdField = (JRField) fieldsMap.get("PasientId");
			JRField PatientInRegistryKeyField = (JRField) fieldsMap.get("PatientInRegistryKey");
			JRField ReshIdField = (JRField) fieldsMap.get("ReshId");
			JRField RHFField = (JRField) fieldsMap.get("RHF");
			JRField RHFreshField = (JRField) fieldsMap.get("RHFresh");
			JRField OrganisasjonField = (JRField) fieldsMap.get("Organisasjon");
			JRField OrgRESHField = (JRField) fieldsMap.get("OrgRESH");
			JRField AvdelingField = (JRField) fieldsMap.get("Avdeling");
			JRField DataSettField = (JRField) fieldsMap.get("DataSett");
			JRField RelatedIDField = (JRField) fieldsMap.get("RelatedID");
			JRField OppfolgningField = (JRField) fieldsMap.get("Oppfolgning");
			JRField SorteringsParameterVerdiField = (JRField) fieldsMap.get("SorteringsParameterVerdi");
			JRField OpprettetDatoField = (JRField) fieldsMap.get("OpprettetDato");
			JRField RapportgrunnlagIDField = (JRField) fieldsMap.get("RapportgrunnlagID");
			JRField KontaktIDField = (JRField) fieldsMap.get("KontaktID");
			JRField KontaktNavnField = (JRField) fieldsMap.get("KontaktNavn");
			JRField KontaktFraDatoField = (JRField) fieldsMap.get("KontaktFraDato");
			JRField KontaktTilDatoField = (JRField) fieldsMap.get("KontaktTilDato");
			JRField DataSettIDField = (JRField) fieldsMap.get("DataSettID");
			JRField FraDatoField = (JRField) fieldsMap.get("FraDato");
			JRField TildatoField = (JRField) fieldsMap.get("Tildato");
			JRField DSPasientnummerField = (JRField) fieldsMap.get("DSPasientnummer");
			JRField OverflyttetFraSykehusHvilketField = (JRField) fieldsMap.get("OverflyttetFraSykehusHvilket");
			JRField ADLAndrespesifisertField = (JRField) fieldsMap.get("ADLAndrespesifisert");
			JRField BesvartAvAndreSpesifiserField = (JRField) fieldsMap.get("BesvartAvAndreSpesifiser");
			JRField DodsaarsakField = (JRField) fieldsMap.get("Dodsaarsak");
			JRField FodselsaarField = (JRField) fieldsMap.get("Fodselsaar");
			JRField HelseforetakField = (JRField) fieldsMap.get("Helseforetak");
			JRField KjonnField = (JRField) fieldsMap.get("Kjonn");
			JRField KommunenummerField = (JRField) fieldsMap.get("Kommunenummer");
			JRField PasientnummerField = (JRField) fieldsMap.get("Pasientnummer");
			JRField PostnummerField = (JRField) fieldsMap.get("Postnummer");
			JRField PoststedField = (JRField) fieldsMap.get("Poststed");
			JRField RegionField = (JRField) fieldsMap.get("Region");
			JRField RegistreringsavdelingField = (JRField) fieldsMap.get("Registreringsavdeling");
			JRField RehabAnnetSpesField = (JRField) fieldsMap.get("RehabAnnetSpes");
			JRField SkjematypeField = (JRField) fieldsMap.get("Skjematype");
			JRField SykehusField = (JRField) fieldsMap.get("Sykehus");
			JRField UtskrTilAnnetField = (JRField) fieldsMap.get("UtskrTilAnnet");
			JRField YrkeField = (JRField) fieldsMap.get("Yrke");
			JRField AarsakManglendeOppfAnnenField = (JRField) fieldsMap.get("AarsakManglendeOppfAnnen");
			JRField TrombolyseHvilketSykehusField = (JRField) fieldsMap.get("TrombolyseHvilketSykehus");
			JRField TrombektomiHvilketSykehusField = (JRField) fieldsMap.get("TrombektomiHvilketSykehus");
			JRField HemikraniektomiHvilketSykehusField = (JRField) fieldsMap.get("HemikraniektomiHvilketSykehus");
			JRField BlodningsstoppBehKlokkeslettField = (JRField) fieldsMap.get("BlodningsstoppBehKlokkeslett");
			JRField InnleggelsestidspunktField = (JRField) fieldsMap.get("Innleggelsestidspunkt");
			JRField MorsdatoField = (JRField) fieldsMap.get("Morsdato");
			JRField OppfolgDatoField = (JRField) fieldsMap.get("OppfolgDato");
			JRField SymptomdebutField = (JRField) fieldsMap.get("Symptomdebut");
			JRField TrombolyseStarttidField = (JRField) fieldsMap.get("TrombolyseStarttid");
			JRField TrombektomiStarttidspunktField = (JRField) fieldsMap.get("TrombektomiStarttidspunkt");
			JRField HemikraniektomiStarttidspunktField = (JRField) fieldsMap.get("HemikraniektomiStarttidspunkt");
			JRField TidInnTrombolyseField = (JRField) fieldsMap.get("TidInnTrombolyse");
			JRField UtskrivingsdatoField = (JRField) fieldsMap.get("Utskrivingsdato");
			JRField VarslingstidspunktField = (JRField) fieldsMap.get("Varslingstidspunkt");
			JRField ADLAndreField = (JRField) fieldsMap.get("ADLAndre");
			JRField ADLFamilieField = (JRField) fieldsMap.get("ADLFamilie");
			JRField ADLHjemmehjelpField = (JRField) fieldsMap.get("ADLHjemmehjelp");
			JRField ADLHjemmesykepleienField = (JRField) fieldsMap.get("ADLHjemmesykepleien");
			JRField ADLIngenField = (JRField) fieldsMap.get("ADLIngen");
			JRField ADLInstitusjonField = (JRField) fieldsMap.get("ADLInstitusjon");
			JRField AtaksiField = (JRField) fieldsMap.get("Ataksi");
			JRField BesvartAvAndreField = (JRField) fieldsMap.get("BesvartAvAndre");
			JRField BesvartAvFamilieField = (JRField) fieldsMap.get("BesvartAvFamilie");
			JRField BesvartAvHelsepersonellField = (JRField) fieldsMap.get("BesvartAvHelsepersonell");
			JRField BesvartAvPasientField = (JRField) fieldsMap.get("BesvartAvPasient");
			JRField DobbeltsynField = (JRField) fieldsMap.get("Dobbeltsyn");
			JRField PreIngenMedikamField = (JRField) fieldsMap.get("PreIngenMedikam");
			JRField RisikofaktorerIngenField = (JRField) fieldsMap.get("RisikofaktorerIngen");
			JRField NIHSSikkeUtfortField = (JRField) fieldsMap.get("NIHSSikkeUtfort");
			JRField NeglektField = (JRField) fieldsMap.get("Neglekt");
			JRField RehabAnnetField = (JRField) fieldsMap.get("RehabAnnet");
			JRField RehabDagField = (JRField) fieldsMap.get("RehabDag");
			JRField RehabDognField = (JRField) fieldsMap.get("RehabDogn");
			JRField RehabSykehjemField = (JRField) fieldsMap.get("RehabSykehjem");
			JRField RehabHjemmeField = (JRField) fieldsMap.get("RehabHjemme");
			JRField RehabIngenField = (JRField) fieldsMap.get("RehabIngen");
			JRField RehabOpptreninngssenterField = (JRField) fieldsMap.get("RehabOpptreninngssenter");
			JRField RehabFysInstField = (JRField) fieldsMap.get("RehabFysInst");
			JRField RehabUkjentField = (JRField) fieldsMap.get("RehabUkjent");
			JRField SensibilitetsutfallField = (JRField) fieldsMap.get("Sensibilitetsutfall");
			JRField SupplerendeUndersIngenField = (JRField) fieldsMap.get("SupplerendeUndersIngen");
			JRField SynsfeltutfallField = (JRField) fieldsMap.get("Synsfeltutfall");
			JRField HemikraniektomiIngenField = (JRField) fieldsMap.get("HemikraniektomiIngen");
			JRField TrombektomiIngenField = (JRField) fieldsMap.get("TrombektomiIngen");
			JRField TrombolyseIngenField = (JRField) fieldsMap.get("TrombolyseIngen");
			JRField OpphIngenAntikoagulasjonField = (JRField) fieldsMap.get("OpphIngenAntikoagulasjon");
			JRField UtIngenMedikamField = (JRField) fieldsMap.get("UtIngenMedikam");
			JRField VertigoField = (JRField) fieldsMap.get("Vertigo");
			JRField AMKIkkeVarsletField = (JRField) fieldsMap.get("AMKIkkeVarslet");
			JRField AntDagerInnlField = (JRField) fieldsMap.get("AntDagerInnl");
			JRField AkutteFokaleutfallPosBilleddiagField = (JRField) fieldsMap.get("AkutteFokaleutfallPosBilleddiag");
			JRField AkutteFokaleUtfallUtenBilleddiagField = (JRField) fieldsMap.get("AkutteFokaleUtfallUtenBilleddiag");
			JRField AndreFokaleSymptField = (JRField) fieldsMap.get("AndreFokaleSympt");
			JRField ArmpareseField = (JRField) fieldsMap.get("Armparese");
			JRField AtrieflimmerField = (JRField) fieldsMap.get("Atrieflimmer");
			JRField AvdForstInnlagtField = (JRField) fieldsMap.get("AvdForstInnlagt");
			JRField AvdForstInnlagtHvilkenField = (JRField) fieldsMap.get("AvdForstInnlagtHvilken");
			JRField AvdUtskrFraField = (JRField) fieldsMap.get("AvdUtskrFra");
			JRField AvdUtskrFraHvilkenField = (JRField) fieldsMap.get("AvdUtskrFraHvilken");
			JRField BedringEtterHjerneslagField = (JRField) fieldsMap.get("BedringEtterHjerneslag");
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
			JRField CerebralCTInnField = (JRField) fieldsMap.get("CerebralCTInn");
			JRField PreDiabetesField = (JRField) fieldsMap.get("PreDiabetes");
			JRField SvelgtestUtfortField = (JRField) fieldsMap.get("SvelgtestUtfort");
			JRField FacialispareseField = (JRField) fieldsMap.get("Facialisparese");
			JRField ForflytningPreField = (JRField) fieldsMap.get("ForflytningPre");
			JRField Forflytning3mndField = (JRField) fieldsMap.get("Forflytning3mnd");
			JRField HjerneblodningsstoppBehField = (JRField) fieldsMap.get("HjerneblodningsstoppBeh");
			JRField HjerneblodningsstoppBehHvilkenField = (JRField) fieldsMap.get("HjerneblodningsstoppBehHvilken");
			JRField PreA2AntagonistField = (JRField) fieldsMap.get("PreA2Antagonist");
			JRField PreACEhemmerField = (JRField) fieldsMap.get("PreACEhemmer");
			JRField PreASAField = (JRField) fieldsMap.get("PreASA");
			JRField PreKombinasjonsbehField = (JRField) fieldsMap.get("PreKombinasjonsbeh");
			JRField PreBetablokkerField = (JRField) fieldsMap.get("PreBetablokker");
			JRField PreDipyridamolField = (JRField) fieldsMap.get("PreDipyridamol");
			JRField PreDiureticaField = (JRField) fieldsMap.get("PreDiuretica");
			JRField PreKalsiumanatgonistField = (JRField) fieldsMap.get("PreKalsiumanatgonist");
			JRField PreKlopidogrelField = (JRField) fieldsMap.get("PreKlopidogrel");
			JRField PreStatinerLipidField = (JRField) fieldsMap.get("PreStatinerLipid");
			JRField PreWarfarinField = (JRField) fieldsMap.get("PreWarfarin");
			JRField PreAndreEnnWarfarinField = (JRField) fieldsMap.get("PreAndreEnnWarfarin");
			JRField PreHjerteKarintervensjField = (JRField) fieldsMap.get("PreHjerteKarintervensj");
			JRField PreHjerteKarintervensjTidsintervField = (JRField) fieldsMap.get("PreHjerteKarintervensjTidsinterv");
			JRField TverrfagligVurderingField = (JRField) fieldsMap.get("TverrfagligVurdering");
			JRField HjelpEtterHjerneslagField = (JRField) fieldsMap.get("HjelpEtterHjerneslag");
			JRField PreMedikBehLipidsenkningField = (JRField) fieldsMap.get("PreMedikBehLipidsenkning");
			JRField InnlagtSykehusEtterUtskrField = (JRField) fieldsMap.get("InnlagtSykehusEtterUtskr");
			JRField MobiliseringInnen24TimerField = (JRField) fieldsMap.get("MobiliseringInnen24Timer");
			JRField LegekontrollEtterHjerneslagField = (JRField) fieldsMap.get("LegekontrollEtterHjerneslag");
			JRField PreMedikBehHoytBTField = (JRField) fieldsMap.get("PreMedikBehHoytBT");
			JRField MedisinMotBlodproppField = (JRField) fieldsMap.get("MedisinMotBlodpropp");
			JRField MedisinHoytBlodtrykk3mndField = (JRField) fieldsMap.get("MedisinHoytBlodtrykk3mnd");
			JRField MedisinHoytKolesterolField = (JRField) fieldsMap.get("MedisinHoytKolesterol");
			JRField ObdusertField = (JRField) fieldsMap.get("Obdusert");
			JRField OppfolgUtfField = (JRField) fieldsMap.get("OppfolgUtf");
			JRField OverflyttetFraSykehusField = (JRField) fieldsMap.get("OverflyttetFraSykehus");
			JRField PasientstatusField = (JRField) fieldsMap.get("Pasientstatus");
			JRField PaakledningPreField = (JRField) fieldsMap.get("PaakledningPre");
			JRField Paakledning3mndField = (JRField) fieldsMap.get("Paakledning3mnd");
			JRField MRSPreField = (JRField) fieldsMap.get("MRSPre");
			JRField MRS3mndField = (JRField) fieldsMap.get("MRS3mnd");
			JRField OperertHalspulsaareField = (JRField) fieldsMap.get("OperertHalspulsaare");
			JRField ReinnlagtTypeSlagField = (JRField) fieldsMap.get("ReinnlagtTypeSlag");
			JRField RoykerPreField = (JRField) fieldsMap.get("RoykerPre");
			JRField Royker3mndField = (JRField) fieldsMap.get("Royker3mnd");
			JRField YrkesaktivUnderHjerneslag2Field = (JRField) fieldsMap.get("YrkesaktivUnderHjerneslag2");
			JRField YrkesaktivNaaField = (JRField) fieldsMap.get("YrkesaktivNaa");
			JRField KjorteBilForHjerneslagField = (JRField) fieldsMap.get("KjorteBilForHjerneslag");
			JRField KjorerBilNaaField = (JRField) fieldsMap.get("KjorerBilNaa");
			JRField SidelokasjonField = (JRField) fieldsMap.get("Sidelokasjon");
			JRField SivilstatusPreField = (JRField) fieldsMap.get("SivilstatusPre");
			JRField Sivilstatus3mndField = (JRField) fieldsMap.get("Sivilstatus3mnd");
			JRField SlagdiagnoseField = (JRField) fieldsMap.get("Slagdiagnose");
			JRField SpraakTaleproblemField = (JRField) fieldsMap.get("SpraakTaleproblem");
			JRField SpraakTaleproblEtterHjslagField = (JRField) fieldsMap.get("SpraakTaleproblEtterHjslag");
			JRField SynsproblEtterHjslagField = (JRField) fieldsMap.get("SynsproblEtterHjslag");
			JRField StatusField = (JRField) fieldsMap.get("Status");
			JRField SykehusIRegionenField = (JRField) fieldsMap.get("SykehusIRegionen");
			JRField VaaknetMedSymptomField = (JRField) fieldsMap.get("VaaknetMedSymptom");
			JRField TimerSymptomdebutInnleggField = (JRField) fieldsMap.get("TimerSymptomdebutInnlegg");
			JRField TidlHjerneslagField = (JRField) fieldsMap.get("TidlHjerneslag");
			JRField TidlHjerteinfarktField = (JRField) fieldsMap.get("TidlHjerteinfarkt");
			JRField TidlTIAField = (JRField) fieldsMap.get("TidlTIA");
			JRField TidlHjerneslagTypeField = (JRField) fieldsMap.get("TidlHjerneslagType");
			JRField TidlTIANaarField = (JRField) fieldsMap.get("TidlTIANaar");
			JRField TilfredshetField = (JRField) fieldsMap.get("Tilfredshet");
			JRField ToalettbesokPreField = (JRField) fieldsMap.get("ToalettbesokPre");
			JRField Toalettbesok3mndField = (JRField) fieldsMap.get("Toalettbesok3mnd");
			JRField TreningEtterHjerneslagField = (JRField) fieldsMap.get("TreningEtterHjerneslag");
			JRField OpphAntikoagulasjonField = (JRField) fieldsMap.get("OpphAntikoagulasjon");
			JRField OpphAntikoagProfylakseBehField = (JRField) fieldsMap.get("OpphAntikoagProfylakseBeh");
			JRField HjerneblInnen36timerField = (JRField) fieldsMap.get("HjerneblInnen36timer");
			JRField TrombolyseField = (JRField) fieldsMap.get("Trombolyse");
			JRField TrombektomiField = (JRField) fieldsMap.get("Trombektomi");
			JRField HemikraniektomiField = (JRField) fieldsMap.get("Hemikraniektomi");
			JRField UtA2AntagonistField = (JRField) fieldsMap.get("UtA2Antagonist");
			JRField UtACEhemmerField = (JRField) fieldsMap.get("UtACEhemmer");
			JRField UtASAField = (JRField) fieldsMap.get("UtASA");
			JRField UtKombinasjonsbehField = (JRField) fieldsMap.get("UtKombinasjonsbeh");
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
			JRField HvorOppstoHjerneslagetField = (JRField) fieldsMap.get("HvorOppstoHjerneslaget");
			JRField AlderField = (JRField) fieldsMap.get("Alder");
			JRField DagerSymptDebutTilOppfField = (JRField) fieldsMap.get("DagerSymptDebutTilOppf");
			JRField NIHSSinnkomstField = (JRField) fieldsMap.get("NIHSSinnkomst");
			JRField NIHSSpreTrombolyseField = (JRField) fieldsMap.get("NIHSSpreTrombolyse");
			JRField NIHSSetterTrombolyseField = (JRField) fieldsMap.get("NIHSSetterTrombolyse");
			JRField NIHSSpreTrombektomiField = (JRField) fieldsMap.get("NIHSSpreTrombektomi");
			JRField NIHSSetterTrombektomiField = (JRField) fieldsMap.get("NIHSSetterTrombektomi");
			JRField DagerInnleggelseTilDodField = (JRField) fieldsMap.get("DagerInnleggelseTilDod");
			JRField MindreEnn4tSymptInnleggField = (JRField) fieldsMap.get("MindreEnn4tSymptInnlegg");
			JRField TransportmetodeField = (JRField) fieldsMap.get("Transportmetode");
			JRField UpdatedField = (JRField) fieldsMap.get("Updated");
			
			
			// Create "slug arrays" with very big sizes (default limit to
			// 1000000) to accommodate large queries
			// We cannot find out how many rows are returned so we have to fetch
			// first and then
			// rebuild arrays of the proper size before passing to R
			//
			// Arrays MUST be defined as objects and not its primitive since
			// returned values do contain 'null'
			
			log.debug("Making empty slug array...");
			
			String[] sPasientId = new String[50000];
			String[] sPatientInRegistryKey = new String[50000];
			Double[] sReshId = new Double[50000];
			String[] sRHF = new String[50000];
			Double[] sRHFresh = new Double[50000];
			String[] sOrganisasjon = new String[50000];
			Double[] sOrgRESH = new Double[50000];
			String[] sAvdeling = new String[50000];
			Double[] sDataSett = new Double[50000];
			Double[] sRelatedID = new Double[50000];
			Double[] sOppfolgning = new Double[50000];
			String[] sSorteringsParameterVerdi = new String[50000];
			String[] sOpprettetDato = new String[50000];
			Double[] sRapportgrunnlagID = new Double[50000];
			Double[] sKontaktID = new Double[50000];
			String[] sKontaktNavn = new String[50000];
			String[] sKontaktFraDato = new String[50000];
			String[] sKontaktTilDato = new String[50000];
			Double[] sDataSettID = new Double[50000];
			String[] sFraDato = new String[50000];
			String[] sTildato = new String[50000];
			String[] sDSPasientnummer = new String[50000];
			String[] sOverflyttetFraSykehusHvilket = new String[50000];
			String[] sADLAndrespesifisert = new String[50000];
			String[] sBesvartAvAndreSpesifiser = new String[50000];
			String[] sDodsaarsak = new String[50000];
			String[] sFodselsaar = new String[50000];
			String[] sHelseforetak = new String[50000];
			String[] sKjonn = new String[50000];
			String[] sKommunenummer = new String[50000];
			String[] sPasientnummer = new String[50000];
			String[] sPostnummer = new String[50000];
			String[] sPoststed = new String[50000];
			String[] sRegion = new String[50000];
			String[] sRegistreringsavdeling = new String[50000];
			String[] sRehabAnnetSpes = new String[50000];
			String[] sSkjematype = new String[50000];
			String[] sSykehus = new String[50000];
			String[] sUtskrTilAnnet = new String[50000];
			String[] sYrke = new String[50000];
			String[] sAarsakManglendeOppfAnnen = new String[50000];
			String[] sTrombolyseHvilketSykehus = new String[50000];
			String[] sTrombektomiHvilketSykehus = new String[50000];
			String[] sHemikraniektomiHvilketSykehus = new String[50000];
			String[] sBlodningsstoppBehKlokkeslett = new String[50000];
			String[] sInnleggelsestidspunkt = new String[50000];
			String[] sMorsdato = new String[50000];
			String[] sOppfolgDato = new String[50000];
			String[] sSymptomdebut = new String[50000];
			String[] sTrombolyseStarttid = new String[50000];
			String[] sTrombektomiStarttidspunkt = new String[50000];
			String[] sHemikraniektomiStarttidspunkt = new String[50000];
			Double[] sTidInnTrombolyse = new Double[50000];
			String[] sUtskrivingsdato = new String[50000];
			String[] sVarslingstidspunkt = new String[50000];
			Double[] sADLAndre = new Double[50000];
			Double[] sADLFamilie = new Double[50000];
			Double[] sADLHjemmehjelp = new Double[50000];
			Double[] sADLHjemmesykepleien = new Double[50000];
			Double[] sADLIngen = new Double[50000];
			Double[] sADLInstitusjon = new Double[50000];
			Double[] sAtaksi = new Double[50000];
			Double[] sBesvartAvAndre = new Double[50000];
			Double[] sBesvartAvFamilie = new Double[50000];
			Double[] sBesvartAvHelsepersonell = new Double[50000];
			Double[] sBesvartAvPasient = new Double[50000];
			Double[] sDobbeltsyn = new Double[50000];
			Double[] sPreIngenMedikam = new Double[50000];
			Double[] sRisikofaktorerIngen = new Double[50000];
			Double[] sNIHSSikkeUtfort = new Double[50000];
			Double[] sNeglekt = new Double[50000];
			Double[] sRehabAnnet = new Double[50000];
			Double[] sRehabDag = new Double[50000];
			Double[] sRehabDogn = new Double[50000];
			Double[] sRehabSykehjem = new Double[50000];
			Double[] sRehabHjemme = new Double[50000];
			Double[] sRehabIngen = new Double[50000];
			Double[] sRehabOpptreninngssenter = new Double[50000];
			Double[] sRehabFysInst = new Double[50000];
			Double[] sRehabUkjent = new Double[50000];
			Double[] sSensibilitetsutfall = new Double[50000];
			Double[] sSupplerendeUndersIngen = new Double[50000];
			Double[] sSynsfeltutfall = new Double[50000];
			Double[] sHemikraniektomiIngen = new Double[50000];
			Double[] sTrombektomiIngen = new Double[50000];
			Double[] sTrombolyseIngen = new Double[50000];
			Double[] sOpphIngenAntikoagulasjon = new Double[50000];
			Double[] sUtIngenMedikam = new Double[50000];
			Double[] sVertigo = new Double[50000];
			Double[] sAMKIkkeVarslet = new Double[50000];
			Double[] sAntDagerInnl = new Double[50000];
			Double[] sAkutteFokaleutfallPosBilleddiag = new Double[50000];
			Double[] sAkutteFokaleUtfallUtenBilleddiag = new Double[50000];
			Double[] sAndreFokaleSympt = new Double[50000];
			Double[] sArmparese = new Double[50000];
			Double[] sAtrieflimmer = new Double[50000];
			Double[] sAvdForstInnlagt = new Double[50000];
			Double[] sAvdForstInnlagtHvilken = new Double[50000];
			Double[] sAvdUtskrFra = new Double[50000];
			Double[] sAvdUtskrFraHvilken = new Double[50000];
			Double[] sBedringEtterHjerneslag = new Double[50000];
			Double[] sBeinparese = new Double[50000];
			Double[] sBevissthetsgradInnleggelse = new Double[50000];
			Double[] sBildediagnostikkHjerne = new Double[50000];
			Double[] sBildediagnostikkHjerte = new Double[50000];
			Double[] sRegistreringHjerterytme = new Double[50000];
			Double[] sBildediagnostikkIntraraniell = new Double[50000];
			Double[] sBildediagnostikkEkstrakranKar = new Double[50000];
			Double[] sBoligforholdPre = new Double[50000];
			Double[] sBoligforhold3mnd = new Double[50000];
			Double[] sBosituasjonPre = new Double[50000];
			Double[] sBosituasjon3mnd = new Double[50000];
			Double[] sCerebralCTInn = new Double[50000];
			Double[] sPreDiabetes = new Double[50000];
			Double[] sSvelgtestUtfort = new Double[50000];
			Double[] sFacialisparese = new Double[50000];
			Double[] sForflytningPre = new Double[50000];
			Double[] sForflytning3mnd = new Double[50000];
			Double[] sHjerneblodningsstoppBeh = new Double[50000];
			Double[] sHjerneblodningsstoppBehHvilken = new Double[50000];
			Double[] sPreA2Antagonist = new Double[50000];
			Double[] sPreACEhemmer = new Double[50000];
			Double[] sPreASA = new Double[50000];
			Double[] sPreKombinasjonsbeh = new Double[50000];
			Double[] sPreBetablokker = new Double[50000];
			Double[] sPreDipyridamol = new Double[50000];
			Double[] sPreDiuretica = new Double[50000];
			Double[] sPreKalsiumanatgonist = new Double[50000];
			Double[] sPreKlopidogrel = new Double[50000];
			Double[] sPreStatinerLipid = new Double[50000];
			Double[] sPreWarfarin = new Double[50000];
			Double[] sPreAndreEnnWarfarin = new Double[50000];
			Double[] sPreHjerteKarintervensj = new Double[50000];
			Double[] sPreHjerteKarintervensjTidsinterv = new Double[50000];
			Double[] sTverrfagligVurdering = new Double[50000];
			Double[] sHjelpEtterHjerneslag = new Double[50000];
			Double[] sPreMedikBehLipidsenkning = new Double[50000];
			Double[] sInnlagtSykehusEtterUtskr = new Double[50000];
			Double[] sMobiliseringInnen24Timer = new Double[50000];
			Double[] sLegekontrollEtterHjerneslag = new Double[50000];
			Double[] sPreMedikBehHoytBT = new Double[50000];
			Double[] sMedisinMotBlodpropp = new Double[50000];
			Double[] sMedisinHoytBlodtrykk3mnd = new Double[50000];
			Double[] sMedisinHoytKolesterol = new Double[50000];
			Double[] sObdusert = new Double[50000];
			Double[] sOppfolgUtf = new Double[50000];
			Double[] sOverflyttetFraSykehus = new Double[50000];
			Double[] sPasientstatus = new Double[50000];
			Double[] sPaakledningPre = new Double[50000];
			Double[] sPaakledning3mnd = new Double[50000];
			Double[] sMRSPre = new Double[50000];
			Double[] sMRS3mnd = new Double[50000];
			Double[] sOperertHalspulsaare = new Double[50000];
			Double[] sReinnlagtTypeSlag = new Double[50000];
			Double[] sRoykerPre = new Double[50000];
			Double[] sRoyker3mnd = new Double[50000];
			Double[] sYrkesaktivUnderHjerneslag2 = new Double[50000];
			Double[] sYrkesaktivNaa = new Double[50000];
			Double[] sKjorteBilForHjerneslag = new Double[50000];
			Double[] sKjorerBilNaa = new Double[50000];
			Double[] sSidelokasjon = new Double[50000];
			Double[] sSivilstatusPre = new Double[50000];
			Double[] sSivilstatus3mnd = new Double[50000];
			Double[] sSlagdiagnose = new Double[50000];
			Double[] sSpraakTaleproblem = new Double[50000];
			Double[] sSpraakTaleproblEtterHjslag = new Double[50000];
			Double[] sSynsproblEtterHjslag = new Double[50000];
			Double[] sStatus = new Double[50000];
			Double[] sSykehusIRegionen = new Double[50000];
			Double[] sVaaknetMedSymptom = new Double[50000];
			Double[] sTimerSymptomdebutInnlegg = new Double[50000];
			Double[] sTidlHjerneslag = new Double[50000];
			Double[] sTidlHjerteinfarkt = new Double[50000];
			Double[] sTidlTIA = new Double[50000];
			Double[] sTidlHjerneslagType = new Double[50000];
			Double[] sTidlTIANaar = new Double[50000];
			Double[] sTilfredshet = new Double[50000];
			Double[] sToalettbesokPre = new Double[50000];
			Double[] sToalettbesok3mnd = new Double[50000];
			Double[] sTreningEtterHjerneslag = new Double[50000];
			Double[] sOpphAntikoagulasjon = new Double[50000];
			Double[] sOpphAntikoagProfylakseBeh = new Double[50000];
			Double[] sHjerneblInnen36timer = new Double[50000];
			Double[] sTrombolyse = new Double[50000];
			Double[] sTrombektomi = new Double[50000];
			Double[] sHemikraniektomi = new Double[50000];
			Double[] sUtA2Antagonist = new Double[50000];
			Double[] sUtACEhemmer = new Double[50000];
			Double[] sUtASA = new Double[50000];
			Double[] sUtKombinasjonsbeh = new Double[50000];
			Double[] sUtBetablokker = new Double[50000];
			Double[] sUtDipyridamol = new Double[50000];
			Double[] sUtDiuretica = new Double[50000];
			Double[] sUtKalsiumantagonist = new Double[50000];
			Double[] sUtKlopidogrel = new Double[50000];
			Double[] sUtStatinerLipid = new Double[50000];
			Double[] sUtWarfarin = new Double[50000];
			Double[] sUtAndreEnnWarfarin = new Double[50000];
			Double[] sUtPlatehem = new Double[50000];
			Double[] sUtAntikoag = new Double[50000];
			Double[] sUtBTsenk = new Double[50000];
			Double[] sUtskrTil = new Double[50000];
			Double[] sAarsakManglendeOppf = new Double[50000];
			Double[] sHvorOppstoHjerneslaget = new Double[50000];
			Double[] sAlder = new Double[50000];
			Double[] sDagerSymptDebutTilOppf = new Double[50000];
			Double[] sNIHSSinnkomst = new Double[50000];
			Double[] sNIHSSpreTrombolyse = new Double[50000];
			Double[] sNIHSSetterTrombolyse = new Double[50000];
			Double[] sNIHSSpreTrombektomi = new Double[50000];
			Double[] sNIHSSetterTrombektomi = new Double[50000];
			Double[] sDagerInnleggelseTilDod = new Double[50000];
			Double[] sMindreEnn4tSymptInnlegg = new Double[50000];
			Double[] sTransportmetode = new Double[50000];
			String[] sUpdated = new String[50000];


			
			log.debug("populating slug array with report data...");
			
			int rowidx = 0;
			// Assume we get 1 row
			boolean getRow = true;
			while (getRow) {
				sPasientId[rowidx] = (String) ds.getFieldValue(PasientIdField);
				sPatientInRegistryKey[rowidx] = (String) ds.getFieldValue(PatientInRegistryKeyField);
				sReshId[rowidx] = (Double) ds.getFieldValue(ReshIdField);
				sRHF[rowidx] = (String) ds.getFieldValue(RHFField);
				sRHFresh[rowidx] = (Double) ds.getFieldValue(RHFreshField);
				sOrganisasjon[rowidx] = (String) ds.getFieldValue(OrganisasjonField);
				sOrgRESH[rowidx] = (Double) ds.getFieldValue(OrgRESHField);
				sAvdeling[rowidx] = (String) ds.getFieldValue(AvdelingField);
				sDataSett[rowidx] = (Double) ds.getFieldValue(DataSettField);
				sRelatedID[rowidx] = (Double) ds.getFieldValue(RelatedIDField);
				sOppfolgning[rowidx] = (Double) ds.getFieldValue(OppfolgningField);
				sSorteringsParameterVerdi[rowidx] = (String) ds.getFieldValue(SorteringsParameterVerdiField);
				sOpprettetDato[rowidx] = (String) ds.getFieldValue(OpprettetDatoField);
				sRapportgrunnlagID[rowidx] = (Double) ds.getFieldValue(RapportgrunnlagIDField);
				sKontaktID[rowidx] = (Double) ds.getFieldValue(KontaktIDField);
				sKontaktNavn[rowidx] = (String) ds.getFieldValue(KontaktNavnField);
				sKontaktFraDato[rowidx] = (String) ds.getFieldValue(KontaktFraDatoField);
				sKontaktTilDato[rowidx] = (String) ds.getFieldValue(KontaktTilDatoField);
				sDataSettID[rowidx] = (Double) ds.getFieldValue(DataSettIDField);
				sFraDato[rowidx] = (String) ds.getFieldValue(FraDatoField);
				sTildato[rowidx] = (String) ds.getFieldValue(TildatoField);
				sDSPasientnummer[rowidx] = (String) ds.getFieldValue(DSPasientnummerField);
				sOverflyttetFraSykehusHvilket[rowidx] = (String) ds.getFieldValue(OverflyttetFraSykehusHvilketField);
				sADLAndrespesifisert[rowidx] = (String) ds.getFieldValue(ADLAndrespesifisertField);
				sBesvartAvAndreSpesifiser[rowidx] = (String) ds.getFieldValue(BesvartAvAndreSpesifiserField);
				sDodsaarsak[rowidx] = (String) ds.getFieldValue(DodsaarsakField);
				sFodselsaar[rowidx] = (String) ds.getFieldValue(FodselsaarField);
				sHelseforetak[rowidx] = (String) ds.getFieldValue(HelseforetakField);
				sKjonn[rowidx] = (String) ds.getFieldValue(KjonnField);
				sKommunenummer[rowidx] = (String) ds.getFieldValue(KommunenummerField);
				sPasientnummer[rowidx] = (String) ds.getFieldValue(PasientnummerField);
				sPostnummer[rowidx] = (String) ds.getFieldValue(PostnummerField);
				sPoststed[rowidx] = (String) ds.getFieldValue(PoststedField);
				sRegion[rowidx] = (String) ds.getFieldValue(RegionField);
				sRegistreringsavdeling[rowidx] = (String) ds.getFieldValue(RegistreringsavdelingField);
				sRehabAnnetSpes[rowidx] = (String) ds.getFieldValue(RehabAnnetSpesField);
				sSkjematype[rowidx] = (String) ds.getFieldValue(SkjematypeField);
				sSykehus[rowidx] = (String) ds.getFieldValue(SykehusField);
				sUtskrTilAnnet[rowidx] = (String) ds.getFieldValue(UtskrTilAnnetField);
				sYrke[rowidx] = (String) ds.getFieldValue(YrkeField);
				sAarsakManglendeOppfAnnen[rowidx] = (String) ds.getFieldValue(AarsakManglendeOppfAnnenField);
				sTrombolyseHvilketSykehus[rowidx] = (String) ds.getFieldValue(TrombolyseHvilketSykehusField);
				sTrombektomiHvilketSykehus[rowidx] = (String) ds.getFieldValue(TrombektomiHvilketSykehusField);
				sHemikraniektomiHvilketSykehus[rowidx] = (String) ds.getFieldValue(HemikraniektomiHvilketSykehusField);
				sBlodningsstoppBehKlokkeslett[rowidx] = (String) ds.getFieldValue(BlodningsstoppBehKlokkeslettField);
				sInnleggelsestidspunkt[rowidx] = (String) ds.getFieldValue(InnleggelsestidspunktField);
				sMorsdato[rowidx] = (String) ds.getFieldValue(MorsdatoField);
				sOppfolgDato[rowidx] = (String) ds.getFieldValue(OppfolgDatoField);
				sSymptomdebut[rowidx] = (String) ds.getFieldValue(SymptomdebutField);
				sTrombolyseStarttid[rowidx] = (String) ds.getFieldValue(TrombolyseStarttidField);
				sTrombektomiStarttidspunkt[rowidx] = (String) ds.getFieldValue(TrombektomiStarttidspunktField);
				sHemikraniektomiStarttidspunkt[rowidx] = (String) ds.getFieldValue(HemikraniektomiStarttidspunktField);
				sTidInnTrombolyse[rowidx] = (Double) ds.getFieldValue(TidInnTrombolyseField);
				sUtskrivingsdato[rowidx] = (String) ds.getFieldValue(UtskrivingsdatoField);
				sVarslingstidspunkt[rowidx] = (String) ds.getFieldValue(VarslingstidspunktField);
				sADLAndre[rowidx] = (Double) ds.getFieldValue(ADLAndreField);
				sADLFamilie[rowidx] = (Double) ds.getFieldValue(ADLFamilieField);
				sADLHjemmehjelp[rowidx] = (Double) ds.getFieldValue(ADLHjemmehjelpField);
				sADLHjemmesykepleien[rowidx] = (Double) ds.getFieldValue(ADLHjemmesykepleienField);
				sADLIngen[rowidx] = (Double) ds.getFieldValue(ADLIngenField);
				sADLInstitusjon[rowidx] = (Double) ds.getFieldValue(ADLInstitusjonField);
				sAtaksi[rowidx] = (Double) ds.getFieldValue(AtaksiField);
				sBesvartAvAndre[rowidx] = (Double) ds.getFieldValue(BesvartAvAndreField);
				sBesvartAvFamilie[rowidx] = (Double) ds.getFieldValue(BesvartAvFamilieField);
				sBesvartAvHelsepersonell[rowidx] = (Double) ds.getFieldValue(BesvartAvHelsepersonellField);
				sBesvartAvPasient[rowidx] = (Double) ds.getFieldValue(BesvartAvPasientField);
				sDobbeltsyn[rowidx] = (Double) ds.getFieldValue(DobbeltsynField);
				sPreIngenMedikam[rowidx] = (Double) ds.getFieldValue(PreIngenMedikamField);
				sRisikofaktorerIngen[rowidx] = (Double) ds.getFieldValue(RisikofaktorerIngenField);
				sNIHSSikkeUtfort[rowidx] = (Double) ds.getFieldValue(NIHSSikkeUtfortField);
				sNeglekt[rowidx] = (Double) ds.getFieldValue(NeglektField);
				sRehabAnnet[rowidx] = (Double) ds.getFieldValue(RehabAnnetField);
				sRehabDag[rowidx] = (Double) ds.getFieldValue(RehabDagField);
				sRehabDogn[rowidx] = (Double) ds.getFieldValue(RehabDognField);
				sRehabSykehjem[rowidx] = (Double) ds.getFieldValue(RehabSykehjemField);
				sRehabHjemme[rowidx] = (Double) ds.getFieldValue(RehabHjemmeField);
				sRehabIngen[rowidx] = (Double) ds.getFieldValue(RehabIngenField);
				sRehabOpptreninngssenter[rowidx] = (Double) ds.getFieldValue(RehabOpptreninngssenterField);
				sRehabFysInst[rowidx] = (Double) ds.getFieldValue(RehabFysInstField);
				sRehabUkjent[rowidx] = (Double) ds.getFieldValue(RehabUkjentField);
				sSensibilitetsutfall[rowidx] = (Double) ds.getFieldValue(SensibilitetsutfallField);
				sSupplerendeUndersIngen[rowidx] = (Double) ds.getFieldValue(SupplerendeUndersIngenField);
				sSynsfeltutfall[rowidx] = (Double) ds.getFieldValue(SynsfeltutfallField);
				sHemikraniektomiIngen[rowidx] = (Double) ds.getFieldValue(HemikraniektomiIngenField);
				sTrombektomiIngen[rowidx] = (Double) ds.getFieldValue(TrombektomiIngenField);
				sTrombolyseIngen[rowidx] = (Double) ds.getFieldValue(TrombolyseIngenField);
				sOpphIngenAntikoagulasjon[rowidx] = (Double) ds.getFieldValue(OpphIngenAntikoagulasjonField);
				sUtIngenMedikam[rowidx] = (Double) ds.getFieldValue(UtIngenMedikamField);
				sVertigo[rowidx] = (Double) ds.getFieldValue(VertigoField);
				sAMKIkkeVarslet[rowidx] = (Double) ds.getFieldValue(AMKIkkeVarsletField);
				sAntDagerInnl[rowidx] = (Double) ds.getFieldValue(AntDagerInnlField);
				sAkutteFokaleutfallPosBilleddiag[rowidx] = (Double) ds.getFieldValue(AkutteFokaleutfallPosBilleddiagField);
				sAkutteFokaleUtfallUtenBilleddiag[rowidx] = (Double) ds.getFieldValue(AkutteFokaleUtfallUtenBilleddiagField);
				sAndreFokaleSympt[rowidx] = (Double) ds.getFieldValue(AndreFokaleSymptField);
				sArmparese[rowidx] = (Double) ds.getFieldValue(ArmpareseField);
				sAtrieflimmer[rowidx] = (Double) ds.getFieldValue(AtrieflimmerField);
				sAvdForstInnlagt[rowidx] = (Double) ds.getFieldValue(AvdForstInnlagtField);
				sAvdForstInnlagtHvilken[rowidx] = (Double) ds.getFieldValue(AvdForstInnlagtHvilkenField);
				sAvdUtskrFra[rowidx] = (Double) ds.getFieldValue(AvdUtskrFraField);
				sAvdUtskrFraHvilken[rowidx] = (Double) ds.getFieldValue(AvdUtskrFraHvilkenField);
				sBedringEtterHjerneslag[rowidx] = (Double) ds.getFieldValue(BedringEtterHjerneslagField);
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
				sCerebralCTInn[rowidx] = (Double) ds.getFieldValue(CerebralCTInnField);
				sPreDiabetes[rowidx] = (Double) ds.getFieldValue(PreDiabetesField);
				sSvelgtestUtfort[rowidx] = (Double) ds.getFieldValue(SvelgtestUtfortField);
				sFacialisparese[rowidx] = (Double) ds.getFieldValue(FacialispareseField);
				sForflytningPre[rowidx] = (Double) ds.getFieldValue(ForflytningPreField);
				sForflytning3mnd[rowidx] = (Double) ds.getFieldValue(Forflytning3mndField);
				sHjerneblodningsstoppBeh[rowidx] = (Double) ds.getFieldValue(HjerneblodningsstoppBehField);
				sHjerneblodningsstoppBehHvilken[rowidx] = (Double) ds.getFieldValue(HjerneblodningsstoppBehHvilkenField);
				sPreA2Antagonist[rowidx] = (Double) ds.getFieldValue(PreA2AntagonistField);
				sPreACEhemmer[rowidx] = (Double) ds.getFieldValue(PreACEhemmerField);
				sPreASA[rowidx] = (Double) ds.getFieldValue(PreASAField);
				sPreKombinasjonsbeh[rowidx] = (Double) ds.getFieldValue(PreKombinasjonsbehField);
				sPreBetablokker[rowidx] = (Double) ds.getFieldValue(PreBetablokkerField);
				sPreDipyridamol[rowidx] = (Double) ds.getFieldValue(PreDipyridamolField);
				sPreDiuretica[rowidx] = (Double) ds.getFieldValue(PreDiureticaField);
				sPreKalsiumanatgonist[rowidx] = (Double) ds.getFieldValue(PreKalsiumanatgonistField);
				sPreKlopidogrel[rowidx] = (Double) ds.getFieldValue(PreKlopidogrelField);
				sPreStatinerLipid[rowidx] = (Double) ds.getFieldValue(PreStatinerLipidField);
				sPreWarfarin[rowidx] = (Double) ds.getFieldValue(PreWarfarinField);
				sPreAndreEnnWarfarin[rowidx] = (Double) ds.getFieldValue(PreAndreEnnWarfarinField);
				sPreHjerteKarintervensj[rowidx] = (Double) ds.getFieldValue(PreHjerteKarintervensjField);
				sPreHjerteKarintervensjTidsinterv[rowidx] = (Double) ds.getFieldValue(PreHjerteKarintervensjTidsintervField);
				sTverrfagligVurdering[rowidx] = (Double) ds.getFieldValue(TverrfagligVurderingField);
				sHjelpEtterHjerneslag[rowidx] = (Double) ds.getFieldValue(HjelpEtterHjerneslagField);
				sPreMedikBehLipidsenkning[rowidx] = (Double) ds.getFieldValue(PreMedikBehLipidsenkningField);
				sInnlagtSykehusEtterUtskr[rowidx] = (Double) ds.getFieldValue(InnlagtSykehusEtterUtskrField);
				sMobiliseringInnen24Timer[rowidx] = (Double) ds.getFieldValue(MobiliseringInnen24TimerField);
				sLegekontrollEtterHjerneslag[rowidx] = (Double) ds.getFieldValue(LegekontrollEtterHjerneslagField);
				sPreMedikBehHoytBT[rowidx] = (Double) ds.getFieldValue(PreMedikBehHoytBTField);
				sMedisinMotBlodpropp[rowidx] = (Double) ds.getFieldValue(MedisinMotBlodproppField);
				sMedisinHoytBlodtrykk3mnd[rowidx] = (Double) ds.getFieldValue(MedisinHoytBlodtrykk3mndField);
				sMedisinHoytKolesterol[rowidx] = (Double) ds.getFieldValue(MedisinHoytKolesterolField);
				sObdusert[rowidx] = (Double) ds.getFieldValue(ObdusertField);
				sOppfolgUtf[rowidx] = (Double) ds.getFieldValue(OppfolgUtfField);
				sOverflyttetFraSykehus[rowidx] = (Double) ds.getFieldValue(OverflyttetFraSykehusField);
				sPasientstatus[rowidx] = (Double) ds.getFieldValue(PasientstatusField);
				sPaakledningPre[rowidx] = (Double) ds.getFieldValue(PaakledningPreField);
				sPaakledning3mnd[rowidx] = (Double) ds.getFieldValue(Paakledning3mndField);
				sMRSPre[rowidx] = (Double) ds.getFieldValue(MRSPreField);
				sMRS3mnd[rowidx] = (Double) ds.getFieldValue(MRS3mndField);
				sOperertHalspulsaare[rowidx] = (Double) ds.getFieldValue(OperertHalspulsaareField);
				sReinnlagtTypeSlag[rowidx] = (Double) ds.getFieldValue(ReinnlagtTypeSlagField);
				sRoykerPre[rowidx] = (Double) ds.getFieldValue(RoykerPreField);
				sRoyker3mnd[rowidx] = (Double) ds.getFieldValue(Royker3mndField);
				sYrkesaktivUnderHjerneslag2[rowidx] = (Double) ds.getFieldValue(YrkesaktivUnderHjerneslag2Field);
				sYrkesaktivNaa[rowidx] = (Double) ds.getFieldValue(YrkesaktivNaaField);
				sKjorteBilForHjerneslag[rowidx] = (Double) ds.getFieldValue(KjorteBilForHjerneslagField);
				sKjorerBilNaa[rowidx] = (Double) ds.getFieldValue(KjorerBilNaaField);
				sSidelokasjon[rowidx] = (Double) ds.getFieldValue(SidelokasjonField);
				sSivilstatusPre[rowidx] = (Double) ds.getFieldValue(SivilstatusPreField);
				sSivilstatus3mnd[rowidx] = (Double) ds.getFieldValue(Sivilstatus3mndField);
				sSlagdiagnose[rowidx] = (Double) ds.getFieldValue(SlagdiagnoseField);
				sSpraakTaleproblem[rowidx] = (Double) ds.getFieldValue(SpraakTaleproblemField);
				sSpraakTaleproblEtterHjslag[rowidx] = (Double) ds.getFieldValue(SpraakTaleproblEtterHjslagField);
				sSynsproblEtterHjslag[rowidx] = (Double) ds.getFieldValue(SynsproblEtterHjslagField);
				sStatus[rowidx] = (Double) ds.getFieldValue(StatusField);
				sSykehusIRegionen[rowidx] = (Double) ds.getFieldValue(SykehusIRegionenField);
				sVaaknetMedSymptom[rowidx] = (Double) ds.getFieldValue(VaaknetMedSymptomField);
				sTimerSymptomdebutInnlegg[rowidx] = (Double) ds.getFieldValue(TimerSymptomdebutInnleggField);
				sTidlHjerneslag[rowidx] = (Double) ds.getFieldValue(TidlHjerneslagField);
				sTidlHjerteinfarkt[rowidx] = (Double) ds.getFieldValue(TidlHjerteinfarktField);
				sTidlTIA[rowidx] = (Double) ds.getFieldValue(TidlTIAField);
				sTidlHjerneslagType[rowidx] = (Double) ds.getFieldValue(TidlHjerneslagTypeField);
				sTidlTIANaar[rowidx] = (Double) ds.getFieldValue(TidlTIANaarField);
				sTilfredshet[rowidx] = (Double) ds.getFieldValue(TilfredshetField);
				sToalettbesokPre[rowidx] = (Double) ds.getFieldValue(ToalettbesokPreField);
				sToalettbesok3mnd[rowidx] = (Double) ds.getFieldValue(Toalettbesok3mndField);
				sTreningEtterHjerneslag[rowidx] = (Double) ds.getFieldValue(TreningEtterHjerneslagField);
				sOpphAntikoagulasjon[rowidx] = (Double) ds.getFieldValue(OpphAntikoagulasjonField);
				sOpphAntikoagProfylakseBeh[rowidx] = (Double) ds.getFieldValue(OpphAntikoagProfylakseBehField);
				sHjerneblInnen36timer[rowidx] = (Double) ds.getFieldValue(HjerneblInnen36timerField);
				sTrombolyse[rowidx] = (Double) ds.getFieldValue(TrombolyseField);
				sTrombektomi[rowidx] = (Double) ds.getFieldValue(TrombektomiField);
				sHemikraniektomi[rowidx] = (Double) ds.getFieldValue(HemikraniektomiField);
				sUtA2Antagonist[rowidx] = (Double) ds.getFieldValue(UtA2AntagonistField);
				sUtACEhemmer[rowidx] = (Double) ds.getFieldValue(UtACEhemmerField);
				sUtASA[rowidx] = (Double) ds.getFieldValue(UtASAField);
				sUtKombinasjonsbeh[rowidx] = (Double) ds.getFieldValue(UtKombinasjonsbehField);
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
				sHvorOppstoHjerneslaget[rowidx] = (Double) ds.getFieldValue(HvorOppstoHjerneslagetField);
				sAlder[rowidx] = (Double) ds.getFieldValue(AlderField);
				sDagerSymptDebutTilOppf[rowidx] = (Double) ds.getFieldValue(DagerSymptDebutTilOppfField);
				sNIHSSinnkomst[rowidx] = (Double) ds.getFieldValue(NIHSSinnkomstField);
				sNIHSSpreTrombolyse[rowidx] = (Double) ds.getFieldValue(NIHSSpreTrombolyseField);
				sNIHSSetterTrombolyse[rowidx] = (Double) ds.getFieldValue(NIHSSetterTrombolyseField);
				sNIHSSpreTrombektomi[rowidx] = (Double) ds.getFieldValue(NIHSSpreTrombektomiField);
				sNIHSSetterTrombektomi[rowidx] = (Double) ds.getFieldValue(NIHSSetterTrombektomiField);
				sDagerInnleggelseTilDod[rowidx] = (Double) ds.getFieldValue(DagerInnleggelseTilDodField);
				sMindreEnn4tSymptInnlegg[rowidx] = (Double) ds.getFieldValue(MindreEnn4tSymptInnleggField);
				sTransportmetode[rowidx] = (Double) ds.getFieldValue(TransportmetodeField);
				sUpdated[rowidx] = (String) ds.getFieldValue(UpdatedField);
				getRow = ds.next();
				rowidx++;
			}
			rowidx--;

			
			log.debug("Slug array filled with " + rowidx + " records from report data");

			
			log.debug("Creating proper sized array...");
			// Create and populate properly sized arrays

			String[] PasientId = new String[rowidx + 1];
			String[] PatientInRegistryKey = new String[rowidx + 1];
			double[] ReshId = new double[rowidx + 1];
			String[] RHF = new String[rowidx + 1];
			double[] RHFresh = new double[rowidx + 1];
			String[] Organisasjon = new String[rowidx + 1];
			double[] OrgRESH = new double[rowidx + 1];
			String[] Avdeling = new String[rowidx + 1];
			double[] DataSett = new double[rowidx + 1];
			double[] RelatedID = new double[rowidx + 1];
			double[] Oppfolgning = new double[rowidx + 1];
			String[] SorteringsParameterVerdi = new String[rowidx + 1];
			String[] OpprettetDato = new String[rowidx + 1];
			double[] RapportgrunnlagID = new double[rowidx + 1];
			double[] KontaktID = new double[rowidx + 1];
			String[] KontaktNavn = new String[rowidx + 1];
			String[] KontaktFraDato = new String[rowidx + 1];
			String[] KontaktTilDato = new String[rowidx + 1];
			double[] DataSettID = new double[rowidx + 1];
			String[] FraDato = new String[rowidx + 1];
			String[] Tildato = new String[rowidx + 1];
			String[] DSPasientnummer = new String[rowidx + 1];
			String[] OverflyttetFraSykehusHvilket = new String[rowidx + 1];
			String[] ADLAndrespesifisert = new String[rowidx + 1];
			String[] BesvartAvAndreSpesifiser = new String[rowidx + 1];
			String[] Dodsaarsak = new String[rowidx + 1];
			String[] Fodselsaar = new String[rowidx + 1];
			String[] Helseforetak = new String[rowidx + 1];
			String[] Kjonn = new String[rowidx + 1];
			String[] Kommunenummer = new String[rowidx + 1];
			String[] Pasientnummer = new String[rowidx + 1];
			String[] Postnummer = new String[rowidx + 1];
			String[] Poststed = new String[rowidx + 1];
			String[] Region = new String[rowidx + 1];
			String[] Registreringsavdeling = new String[rowidx + 1];
			String[] RehabAnnetSpes = new String[rowidx + 1];
			String[] Skjematype = new String[rowidx + 1];
			String[] Sykehus = new String[rowidx + 1];
			String[] UtskrTilAnnet = new String[rowidx + 1];
			String[] Yrke = new String[rowidx + 1];
			String[] AarsakManglendeOppfAnnen = new String[rowidx + 1];
			String[] TrombolyseHvilketSykehus = new String[rowidx + 1];
			String[] TrombektomiHvilketSykehus = new String[rowidx + 1];
			String[] HemikraniektomiHvilketSykehus = new String[rowidx + 1];
			String[] BlodningsstoppBehKlokkeslett = new String[rowidx + 1];
			String[] Innleggelsestidspunkt = new String[rowidx + 1];
			String[] Morsdato = new String[rowidx + 1];
			String[] OppfolgDato = new String[rowidx + 1];
			String[] Symptomdebut = new String[rowidx + 1];
			String[] TrombolyseStarttid = new String[rowidx + 1];
			String[] TrombektomiStarttidspunkt = new String[rowidx + 1];
			String[] HemikraniektomiStarttidspunkt = new String[rowidx + 1];
			double[] TidInnTrombolyse = new double[rowidx + 1];
			String[] Utskrivingsdato = new String[rowidx + 1];
			String[] Varslingstidspunkt = new String[rowidx + 1];
			double[] ADLAndre = new double[rowidx + 1];
			double[] ADLFamilie = new double[rowidx + 1];
			double[] ADLHjemmehjelp = new double[rowidx + 1];
			double[] ADLHjemmesykepleien = new double[rowidx + 1];
			double[] ADLIngen = new double[rowidx + 1];
			double[] ADLInstitusjon = new double[rowidx + 1];
			double[] Ataksi = new double[rowidx + 1];
			double[] BesvartAvAndre = new double[rowidx + 1];
			double[] BesvartAvFamilie = new double[rowidx + 1];
			double[] BesvartAvHelsepersonell = new double[rowidx + 1];
			double[] BesvartAvPasient = new double[rowidx + 1];
			double[] Dobbeltsyn = new double[rowidx + 1];
			double[] PreIngenMedikam = new double[rowidx + 1];
			double[] RisikofaktorerIngen = new double[rowidx + 1];
			double[] NIHSSikkeUtfort = new double[rowidx + 1];
			double[] Neglekt = new double[rowidx + 1];
			double[] RehabAnnet = new double[rowidx + 1];
			double[] RehabDag = new double[rowidx + 1];
			double[] RehabDogn = new double[rowidx + 1];
			double[] RehabSykehjem = new double[rowidx + 1];
			double[] RehabHjemme = new double[rowidx + 1];
			double[] RehabIngen = new double[rowidx + 1];
			double[] RehabOpptreninngssenter = new double[rowidx + 1];
			double[] RehabFysInst = new double[rowidx + 1];
			double[] RehabUkjent = new double[rowidx + 1];
			double[] Sensibilitetsutfall = new double[rowidx + 1];
			double[] SupplerendeUndersIngen = new double[rowidx + 1];
			double[] Synsfeltutfall = new double[rowidx + 1];
			double[] HemikraniektomiIngen = new double[rowidx + 1];
			double[] TrombektomiIngen = new double[rowidx + 1];
			double[] TrombolyseIngen = new double[rowidx + 1];
			double[] OpphIngenAntikoagulasjon = new double[rowidx + 1];
			double[] UtIngenMedikam = new double[rowidx + 1];
			double[] Vertigo = new double[rowidx + 1];
			double[] AMKIkkeVarslet = new double[rowidx + 1];
			double[] AntDagerInnl = new double[rowidx + 1];
			double[] AkutteFokaleutfallPosBilleddiag = new double[rowidx + 1];
			double[] AkutteFokaleUtfallUtenBilleddiag = new double[rowidx + 1];
			double[] AndreFokaleSympt = new double[rowidx + 1];
			double[] Armparese = new double[rowidx + 1];
			double[] Atrieflimmer = new double[rowidx + 1];
			double[] AvdForstInnlagt = new double[rowidx + 1];
			double[] AvdForstInnlagtHvilken = new double[rowidx + 1];
			double[] AvdUtskrFra = new double[rowidx + 1];
			double[] AvdUtskrFraHvilken = new double[rowidx + 1];
			double[] BedringEtterHjerneslag = new double[rowidx + 1];
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
			double[] CerebralCTInn = new double[rowidx + 1];
			double[] PreDiabetes = new double[rowidx + 1];
			double[] SvelgtestUtfort = new double[rowidx + 1];
			double[] Facialisparese = new double[rowidx + 1];
			double[] ForflytningPre = new double[rowidx + 1];
			double[] Forflytning3mnd = new double[rowidx + 1];
			double[] HjerneblodningsstoppBeh = new double[rowidx + 1];
			double[] HjerneblodningsstoppBehHvilken = new double[rowidx + 1];
			double[] PreA2Antagonist = new double[rowidx + 1];
			double[] PreACEhemmer = new double[rowidx + 1];
			double[] PreASA = new double[rowidx + 1];
			double[] PreKombinasjonsbeh = new double[rowidx + 1];
			double[] PreBetablokker = new double[rowidx + 1];
			double[] PreDipyridamol = new double[rowidx + 1];
			double[] PreDiuretica = new double[rowidx + 1];
			double[] PreKalsiumanatgonist = new double[rowidx + 1];
			double[] PreKlopidogrel = new double[rowidx + 1];
			double[] PreStatinerLipid = new double[rowidx + 1];
			double[] PreWarfarin = new double[rowidx + 1];
			double[] PreAndreEnnWarfarin = new double[rowidx + 1];
			double[] PreHjerteKarintervensj = new double[rowidx + 1];
			double[] PreHjerteKarintervensjTidsinterv = new double[rowidx + 1];
			double[] TverrfagligVurdering = new double[rowidx + 1];
			double[] HjelpEtterHjerneslag = new double[rowidx + 1];
			double[] PreMedikBehLipidsenkning = new double[rowidx + 1];
			double[] InnlagtSykehusEtterUtskr = new double[rowidx + 1];
			double[] MobiliseringInnen24Timer = new double[rowidx + 1];
			double[] LegekontrollEtterHjerneslag = new double[rowidx + 1];
			double[] PreMedikBehHoytBT = new double[rowidx + 1];
			double[] MedisinMotBlodpropp = new double[rowidx + 1];
			double[] MedisinHoytBlodtrykk3mnd = new double[rowidx + 1];
			double[] MedisinHoytKolesterol = new double[rowidx + 1];
			double[] Obdusert = new double[rowidx + 1];
			double[] OppfolgUtf = new double[rowidx + 1];
			double[] OverflyttetFraSykehus = new double[rowidx + 1];
			double[] Pasientstatus = new double[rowidx + 1];
			double[] PaakledningPre = new double[rowidx + 1];
			double[] Paakledning3mnd = new double[rowidx + 1];
			double[] MRSPre = new double[rowidx + 1];
			double[] MRS3mnd = new double[rowidx + 1];
			double[] OperertHalspulsaare = new double[rowidx + 1];
			double[] ReinnlagtTypeSlag = new double[rowidx + 1];
			double[] RoykerPre = new double[rowidx + 1];
			double[] Royker3mnd = new double[rowidx + 1];
			double[] YrkesaktivUnderHjerneslag2 = new double[rowidx + 1];
			double[] YrkesaktivNaa = new double[rowidx + 1];
			double[] KjorteBilForHjerneslag = new double[rowidx + 1];
			double[] KjorerBilNaa = new double[rowidx + 1];
			double[] Sidelokasjon = new double[rowidx + 1];
			double[] SivilstatusPre = new double[rowidx + 1];
			double[] Sivilstatus3mnd = new double[rowidx + 1];
			double[] Slagdiagnose = new double[rowidx + 1];
			double[] SpraakTaleproblem = new double[rowidx + 1];
			double[] SpraakTaleproblEtterHjslag = new double[rowidx + 1];
			double[] SynsproblEtterHjslag = new double[rowidx + 1];
			double[] Status = new double[rowidx + 1];
			double[] SykehusIRegionen = new double[rowidx + 1];
			double[] VaaknetMedSymptom = new double[rowidx + 1];
			double[] TimerSymptomdebutInnlegg = new double[rowidx + 1];
			double[] TidlHjerneslag = new double[rowidx + 1];
			double[] TidlHjerteinfarkt = new double[rowidx + 1];
			double[] TidlTIA = new double[rowidx + 1];
			double[] TidlHjerneslagType = new double[rowidx + 1];
			double[] TidlTIANaar = new double[rowidx + 1];
			double[] Tilfredshet = new double[rowidx + 1];
			double[] ToalettbesokPre = new double[rowidx + 1];
			double[] Toalettbesok3mnd = new double[rowidx + 1];
			double[] TreningEtterHjerneslag = new double[rowidx + 1];
			double[] OpphAntikoagulasjon = new double[rowidx + 1];
			double[] OpphAntikoagProfylakseBeh = new double[rowidx + 1];
			double[] HjerneblInnen36timer = new double[rowidx + 1];
			double[] Trombolyse = new double[rowidx + 1];
			double[] Trombektomi = new double[rowidx + 1];
			double[] Hemikraniektomi = new double[rowidx + 1];
			double[] UtA2Antagonist = new double[rowidx + 1];
			double[] UtACEhemmer = new double[rowidx + 1];
			double[] UtASA = new double[rowidx + 1];
			double[] UtKombinasjonsbeh = new double[rowidx + 1];
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
			double[] HvorOppstoHjerneslaget = new double[rowidx + 1];
			double[] Alder = new double[rowidx + 1];
			double[] DagerSymptDebutTilOppf = new double[rowidx + 1];
			double[] NIHSSinnkomst = new double[rowidx + 1];
			double[] NIHSSpreTrombolyse = new double[rowidx + 1];
			double[] NIHSSetterTrombolyse = new double[rowidx + 1];
			double[] NIHSSpreTrombektomi = new double[rowidx + 1];
			double[] NIHSSetterTrombektomi = new double[rowidx + 1];
			double[] DagerInnleggelseTilDod = new double[rowidx + 1];
			double[] MindreEnn4tSymptInnlegg = new double[rowidx + 1];
			double[] Transportmetode = new double[rowidx + 1];
			String[] Updated = new String[rowidx + 1];


			
			// ifs are needed because underlying query returns null. Since ints
			// cannot be null, these are returned as type double by the query
			log.debug("Populating proper sized array with data from slug array, also checking for NULLs...");
			int i = 0;
			while (i <= rowidx) {
				PasientId[i] = sPasientId[i];
				PatientInRegistryKey[i] = sPatientInRegistryKey[i];
				if (sReshId[i] == null) {
					ReshId[i] = java.lang.Double.NaN;
				}
				else {
					ReshId[i] = sReshId[i];
				}

				RHF[i] = sRHF[i];
				if (sRHFresh[i] == null) {
					RHFresh[i] = java.lang.Double.NaN;
				}
				else {
					RHFresh[i] = sRHFresh[i];
				}

				Organisasjon[i] = sOrganisasjon[i];
				if (sOrgRESH[i] == null) {
					OrgRESH[i] = java.lang.Double.NaN;
				}
				else {
					OrgRESH[i] = sOrgRESH[i];
				}

				Avdeling[i] = sAvdeling[i];
				if (sDataSett[i] == null) {
					DataSett[i] = java.lang.Double.NaN;
				}
				else {
					DataSett[i] = sDataSett[i];
				}

				if (sRelatedID[i] == null) {
					RelatedID[i] = java.lang.Double.NaN;
				}
				else {
					RelatedID[i] = sRelatedID[i];
				}

				if (sOppfolgning[i] == null) {
					Oppfolgning[i] = java.lang.Double.NaN;
				}
				else {
					Oppfolgning[i] = sOppfolgning[i];
				}

				SorteringsParameterVerdi[i] = sSorteringsParameterVerdi[i];
				OpprettetDato[i] = sOpprettetDato[i];
				if (sRapportgrunnlagID[i] == null) {
					RapportgrunnlagID[i] = java.lang.Double.NaN;
				}
				else {
					RapportgrunnlagID[i] = sRapportgrunnlagID[i];
				}

				if (sKontaktID[i] == null) {
					KontaktID[i] = java.lang.Double.NaN;
				}
				else {
					KontaktID[i] = sKontaktID[i];
				}

				KontaktNavn[i] = sKontaktNavn[i];
				KontaktFraDato[i] = sKontaktFraDato[i];
				KontaktTilDato[i] = sKontaktTilDato[i];
				if (sDataSettID[i] == null) {
					DataSettID[i] = java.lang.Double.NaN;
				}
				else {
					DataSettID[i] = sDataSettID[i];
				}

				FraDato[i] = sFraDato[i];
				Tildato[i] = sTildato[i];
				DSPasientnummer[i] = sDSPasientnummer[i];
				OverflyttetFraSykehusHvilket[i] = sOverflyttetFraSykehusHvilket[i];
				ADLAndrespesifisert[i] = sADLAndrespesifisert[i];
				BesvartAvAndreSpesifiser[i] = sBesvartAvAndreSpesifiser[i];
				Dodsaarsak[i] = sDodsaarsak[i];
				Fodselsaar[i] = sFodselsaar[i];
				Helseforetak[i] = sHelseforetak[i];
				Kjonn[i] = sKjonn[i];
				Kommunenummer[i] = sKommunenummer[i];
				Pasientnummer[i] = sPasientnummer[i];
				Postnummer[i] = sPostnummer[i];
				Poststed[i] = sPoststed[i];
				Region[i] = sRegion[i];
				Registreringsavdeling[i] = sRegistreringsavdeling[i];
				RehabAnnetSpes[i] = sRehabAnnetSpes[i];
				Skjematype[i] = sSkjematype[i];
				Sykehus[i] = sSykehus[i];
				UtskrTilAnnet[i] = sUtskrTilAnnet[i];
				Yrke[i] = sYrke[i];
				AarsakManglendeOppfAnnen[i] = sAarsakManglendeOppfAnnen[i];
				TrombolyseHvilketSykehus[i] = sTrombolyseHvilketSykehus[i];
				TrombektomiHvilketSykehus[i] = sTrombektomiHvilketSykehus[i];
				HemikraniektomiHvilketSykehus[i] = sHemikraniektomiHvilketSykehus[i];
				BlodningsstoppBehKlokkeslett[i] = sBlodningsstoppBehKlokkeslett[i];
				Innleggelsestidspunkt[i] = sInnleggelsestidspunkt[i];
				Morsdato[i] = sMorsdato[i];
				OppfolgDato[i] = sOppfolgDato[i];
				Symptomdebut[i] = sSymptomdebut[i];
				TrombolyseStarttid[i] = sTrombolyseStarttid[i];
				TrombektomiStarttidspunkt[i] = sTrombektomiStarttidspunkt[i];
				HemikraniektomiStarttidspunkt[i] = sHemikraniektomiStarttidspunkt[i];
				if (sTidInnTrombolyse[i] == null) {
					TidInnTrombolyse[i] = java.lang.Double.NaN;
				}
				else {
					TidInnTrombolyse[i] = sTidInnTrombolyse[i];
				}

				Utskrivingsdato[i] = sUtskrivingsdato[i];
				Varslingstidspunkt[i] = sVarslingstidspunkt[i];
				if (sADLAndre[i] == null) {
					ADLAndre[i] = java.lang.Double.NaN;
				}
				else {
					ADLAndre[i] = sADLAndre[i];
				}

				if (sADLFamilie[i] == null) {
					ADLFamilie[i] = java.lang.Double.NaN;
				}
				else {
					ADLFamilie[i] = sADLFamilie[i];
				}

				if (sADLHjemmehjelp[i] == null) {
					ADLHjemmehjelp[i] = java.lang.Double.NaN;
				}
				else {
					ADLHjemmehjelp[i] = sADLHjemmehjelp[i];
				}

				if (sADLHjemmesykepleien[i] == null) {
					ADLHjemmesykepleien[i] = java.lang.Double.NaN;
				}
				else {
					ADLHjemmesykepleien[i] = sADLHjemmesykepleien[i];
				}

				if (sADLIngen[i] == null) {
					ADLIngen[i] = java.lang.Double.NaN;
				}
				else {
					ADLIngen[i] = sADLIngen[i];
				}

				if (sADLInstitusjon[i] == null) {
					ADLInstitusjon[i] = java.lang.Double.NaN;
				}
				else {
					ADLInstitusjon[i] = sADLInstitusjon[i];
				}

				if (sAtaksi[i] == null) {
					Ataksi[i] = java.lang.Double.NaN;
				}
				else {
					Ataksi[i] = sAtaksi[i];
				}

				if (sBesvartAvAndre[i] == null) {
					BesvartAvAndre[i] = java.lang.Double.NaN;
				}
				else {
					BesvartAvAndre[i] = sBesvartAvAndre[i];
				}

				if (sBesvartAvFamilie[i] == null) {
					BesvartAvFamilie[i] = java.lang.Double.NaN;
				}
				else {
					BesvartAvFamilie[i] = sBesvartAvFamilie[i];
				}

				if (sBesvartAvHelsepersonell[i] == null) {
					BesvartAvHelsepersonell[i] = java.lang.Double.NaN;
				}
				else {
					BesvartAvHelsepersonell[i] = sBesvartAvHelsepersonell[i];
				}

				if (sBesvartAvPasient[i] == null) {
					BesvartAvPasient[i] = java.lang.Double.NaN;
				}
				else {
					BesvartAvPasient[i] = sBesvartAvPasient[i];
				}

				if (sDobbeltsyn[i] == null) {
					Dobbeltsyn[i] = java.lang.Double.NaN;
				}
				else {
					Dobbeltsyn[i] = sDobbeltsyn[i];
				}

				if (sPreIngenMedikam[i] == null) {
					PreIngenMedikam[i] = java.lang.Double.NaN;
				}
				else {
					PreIngenMedikam[i] = sPreIngenMedikam[i];
				}

				if (sRisikofaktorerIngen[i] == null) {
					RisikofaktorerIngen[i] = java.lang.Double.NaN;
				}
				else {
					RisikofaktorerIngen[i] = sRisikofaktorerIngen[i];
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

				if (sRehabAnnet[i] == null) {
					RehabAnnet[i] = java.lang.Double.NaN;
				}
				else {
					RehabAnnet[i] = sRehabAnnet[i];
				}

				if (sRehabDag[i] == null) {
					RehabDag[i] = java.lang.Double.NaN;
				}
				else {
					RehabDag[i] = sRehabDag[i];
				}

				if (sRehabDogn[i] == null) {
					RehabDogn[i] = java.lang.Double.NaN;
				}
				else {
					RehabDogn[i] = sRehabDogn[i];
				}

				if (sRehabSykehjem[i] == null) {
					RehabSykehjem[i] = java.lang.Double.NaN;
				}
				else {
					RehabSykehjem[i] = sRehabSykehjem[i];
				}

				if (sRehabHjemme[i] == null) {
					RehabHjemme[i] = java.lang.Double.NaN;
				}
				else {
					RehabHjemme[i] = sRehabHjemme[i];
				}

				if (sRehabIngen[i] == null) {
					RehabIngen[i] = java.lang.Double.NaN;
				}
				else {
					RehabIngen[i] = sRehabIngen[i];
				}

				if (sRehabOpptreninngssenter[i] == null) {
					RehabOpptreninngssenter[i] = java.lang.Double.NaN;
				}
				else {
					RehabOpptreninngssenter[i] = sRehabOpptreninngssenter[i];
				}

				if (sRehabFysInst[i] == null) {
					RehabFysInst[i] = java.lang.Double.NaN;
				}
				else {
					RehabFysInst[i] = sRehabFysInst[i];
				}

				if (sRehabUkjent[i] == null) {
					RehabUkjent[i] = java.lang.Double.NaN;
				}
				else {
					RehabUkjent[i] = sRehabUkjent[i];
				}

				if (sSensibilitetsutfall[i] == null) {
					Sensibilitetsutfall[i] = java.lang.Double.NaN;
				}
				else {
					Sensibilitetsutfall[i] = sSensibilitetsutfall[i];
				}

				if (sSupplerendeUndersIngen[i] == null) {
					SupplerendeUndersIngen[i] = java.lang.Double.NaN;
				}
				else {
					SupplerendeUndersIngen[i] = sSupplerendeUndersIngen[i];
				}

				if (sSynsfeltutfall[i] == null) {
					Synsfeltutfall[i] = java.lang.Double.NaN;
				}
				else {
					Synsfeltutfall[i] = sSynsfeltutfall[i];
				}

				if (sHemikraniektomiIngen[i] == null) {
					HemikraniektomiIngen[i] = java.lang.Double.NaN;
				}
				else {
					HemikraniektomiIngen[i] = sHemikraniektomiIngen[i];
				}

				if (sTrombektomiIngen[i] == null) {
					TrombektomiIngen[i] = java.lang.Double.NaN;
				}
				else {
					TrombektomiIngen[i] = sTrombektomiIngen[i];
				}

				if (sTrombolyseIngen[i] == null) {
					TrombolyseIngen[i] = java.lang.Double.NaN;
				}
				else {
					TrombolyseIngen[i] = sTrombolyseIngen[i];
				}

				if (sOpphIngenAntikoagulasjon[i] == null) {
					OpphIngenAntikoagulasjon[i] = java.lang.Double.NaN;
				}
				else {
					OpphIngenAntikoagulasjon[i] = sOpphIngenAntikoagulasjon[i];
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

				if (sAMKIkkeVarslet[i] == null) {
					AMKIkkeVarslet[i] = java.lang.Double.NaN;
				}
				else {
					AMKIkkeVarslet[i] = sAMKIkkeVarslet[i];
				}

				if (sAntDagerInnl[i] == null) {
					AntDagerInnl[i] = java.lang.Double.NaN;
				}
				else {
					AntDagerInnl[i] = sAntDagerInnl[i];
				}

				if (sAkutteFokaleutfallPosBilleddiag[i] == null) {
					AkutteFokaleutfallPosBilleddiag[i] = java.lang.Double.NaN;
				}
				else {
					AkutteFokaleutfallPosBilleddiag[i] = sAkutteFokaleutfallPosBilleddiag[i];
				}

				if (sAkutteFokaleUtfallUtenBilleddiag[i] == null) {
					AkutteFokaleUtfallUtenBilleddiag[i] = java.lang.Double.NaN;
				}
				else {
					AkutteFokaleUtfallUtenBilleddiag[i] = sAkutteFokaleUtfallUtenBilleddiag[i];
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

				if (sBedringEtterHjerneslag[i] == null) {
					BedringEtterHjerneslag[i] = java.lang.Double.NaN;
				}
				else {
					BedringEtterHjerneslag[i] = sBedringEtterHjerneslag[i];
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

				if (sCerebralCTInn[i] == null) {
					CerebralCTInn[i] = java.lang.Double.NaN;
				}
				else {
					CerebralCTInn[i] = sCerebralCTInn[i];
				}

				if (sPreDiabetes[i] == null) {
					PreDiabetes[i] = java.lang.Double.NaN;
				}
				else {
					PreDiabetes[i] = sPreDiabetes[i];
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

				if (sHjerneblodningsstoppBeh[i] == null) {
					HjerneblodningsstoppBeh[i] = java.lang.Double.NaN;
				}
				else {
					HjerneblodningsstoppBeh[i] = sHjerneblodningsstoppBeh[i];
				}

				if (sHjerneblodningsstoppBehHvilken[i] == null) {
					HjerneblodningsstoppBehHvilken[i] = java.lang.Double.NaN;
				}
				else {
					HjerneblodningsstoppBehHvilken[i] = sHjerneblodningsstoppBehHvilken[i];
				}

				if (sPreA2Antagonist[i] == null) {
					PreA2Antagonist[i] = java.lang.Double.NaN;
				}
				else {
					PreA2Antagonist[i] = sPreA2Antagonist[i];
				}

				if (sPreACEhemmer[i] == null) {
					PreACEhemmer[i] = java.lang.Double.NaN;
				}
				else {
					PreACEhemmer[i] = sPreACEhemmer[i];
				}

				if (sPreASA[i] == null) {
					PreASA[i] = java.lang.Double.NaN;
				}
				else {
					PreASA[i] = sPreASA[i];
				}

				if (sPreKombinasjonsbeh[i] == null) {
					PreKombinasjonsbeh[i] = java.lang.Double.NaN;
				}
				else {
					PreKombinasjonsbeh[i] = sPreKombinasjonsbeh[i];
				}

				if (sPreBetablokker[i] == null) {
					PreBetablokker[i] = java.lang.Double.NaN;
				}
				else {
					PreBetablokker[i] = sPreBetablokker[i];
				}

				if (sPreDipyridamol[i] == null) {
					PreDipyridamol[i] = java.lang.Double.NaN;
				}
				else {
					PreDipyridamol[i] = sPreDipyridamol[i];
				}

				if (sPreDiuretica[i] == null) {
					PreDiuretica[i] = java.lang.Double.NaN;
				}
				else {
					PreDiuretica[i] = sPreDiuretica[i];
				}

				if (sPreKalsiumanatgonist[i] == null) {
					PreKalsiumanatgonist[i] = java.lang.Double.NaN;
				}
				else {
					PreKalsiumanatgonist[i] = sPreKalsiumanatgonist[i];
				}

				if (sPreKlopidogrel[i] == null) {
					PreKlopidogrel[i] = java.lang.Double.NaN;
				}
				else {
					PreKlopidogrel[i] = sPreKlopidogrel[i];
				}

				if (sPreStatinerLipid[i] == null) {
					PreStatinerLipid[i] = java.lang.Double.NaN;
				}
				else {
					PreStatinerLipid[i] = sPreStatinerLipid[i];
				}

				if (sPreWarfarin[i] == null) {
					PreWarfarin[i] = java.lang.Double.NaN;
				}
				else {
					PreWarfarin[i] = sPreWarfarin[i];
				}

				if (sPreAndreEnnWarfarin[i] == null) {
					PreAndreEnnWarfarin[i] = java.lang.Double.NaN;
				}
				else {
					PreAndreEnnWarfarin[i] = sPreAndreEnnWarfarin[i];
				}

				if (sPreHjerteKarintervensj[i] == null) {
					PreHjerteKarintervensj[i] = java.lang.Double.NaN;
				}
				else {
					PreHjerteKarintervensj[i] = sPreHjerteKarintervensj[i];
				}

				if (sPreHjerteKarintervensjTidsinterv[i] == null) {
					PreHjerteKarintervensjTidsinterv[i] = java.lang.Double.NaN;
				}
				else {
					PreHjerteKarintervensjTidsinterv[i] = sPreHjerteKarintervensjTidsinterv[i];
				}

				if (sTverrfagligVurdering[i] == null) {
					TverrfagligVurdering[i] = java.lang.Double.NaN;
				}
				else {
					TverrfagligVurdering[i] = sTverrfagligVurdering[i];
				}

				if (sHjelpEtterHjerneslag[i] == null) {
					HjelpEtterHjerneslag[i] = java.lang.Double.NaN;
				}
				else {
					HjelpEtterHjerneslag[i] = sHjelpEtterHjerneslag[i];
				}

				if (sPreMedikBehLipidsenkning[i] == null) {
					PreMedikBehLipidsenkning[i] = java.lang.Double.NaN;
				}
				else {
					PreMedikBehLipidsenkning[i] = sPreMedikBehLipidsenkning[i];
				}

				if (sInnlagtSykehusEtterUtskr[i] == null) {
					InnlagtSykehusEtterUtskr[i] = java.lang.Double.NaN;
				}
				else {
					InnlagtSykehusEtterUtskr[i] = sInnlagtSykehusEtterUtskr[i];
				}

				if (sMobiliseringInnen24Timer[i] == null) {
					MobiliseringInnen24Timer[i] = java.lang.Double.NaN;
				}
				else {
					MobiliseringInnen24Timer[i] = sMobiliseringInnen24Timer[i];
				}

				if (sLegekontrollEtterHjerneslag[i] == null) {
					LegekontrollEtterHjerneslag[i] = java.lang.Double.NaN;
				}
				else {
					LegekontrollEtterHjerneslag[i] = sLegekontrollEtterHjerneslag[i];
				}

				if (sPreMedikBehHoytBT[i] == null) {
					PreMedikBehHoytBT[i] = java.lang.Double.NaN;
				}
				else {
					PreMedikBehHoytBT[i] = sPreMedikBehHoytBT[i];
				}

				if (sMedisinMotBlodpropp[i] == null) {
					MedisinMotBlodpropp[i] = java.lang.Double.NaN;
				}
				else {
					MedisinMotBlodpropp[i] = sMedisinMotBlodpropp[i];
				}

				if (sMedisinHoytBlodtrykk3mnd[i] == null) {
					MedisinHoytBlodtrykk3mnd[i] = java.lang.Double.NaN;
				}
				else {
					MedisinHoytBlodtrykk3mnd[i] = sMedisinHoytBlodtrykk3mnd[i];
				}

				if (sMedisinHoytKolesterol[i] == null) {
					MedisinHoytKolesterol[i] = java.lang.Double.NaN;
				}
				else {
					MedisinHoytKolesterol[i] = sMedisinHoytKolesterol[i];
				}

				if (sObdusert[i] == null) {
					Obdusert[i] = java.lang.Double.NaN;
				}
				else {
					Obdusert[i] = sObdusert[i];
				}

				if (sOppfolgUtf[i] == null) {
					OppfolgUtf[i] = java.lang.Double.NaN;
				}
				else {
					OppfolgUtf[i] = sOppfolgUtf[i];
				}

				if (sOverflyttetFraSykehus[i] == null) {
					OverflyttetFraSykehus[i] = java.lang.Double.NaN;
				}
				else {
					OverflyttetFraSykehus[i] = sOverflyttetFraSykehus[i];
				}

				if (sPasientstatus[i] == null) {
					Pasientstatus[i] = java.lang.Double.NaN;
				}
				else {
					Pasientstatus[i] = sPasientstatus[i];
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

				if (sOperertHalspulsaare[i] == null) {
					OperertHalspulsaare[i] = java.lang.Double.NaN;
				}
				else {
					OperertHalspulsaare[i] = sOperertHalspulsaare[i];
				}

				if (sReinnlagtTypeSlag[i] == null) {
					ReinnlagtTypeSlag[i] = java.lang.Double.NaN;
				}
				else {
					ReinnlagtTypeSlag[i] = sReinnlagtTypeSlag[i];
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

				if (sSidelokasjon[i] == null) {
					Sidelokasjon[i] = java.lang.Double.NaN;
				}
				else {
					Sidelokasjon[i] = sSidelokasjon[i];
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

				if (sSpraakTaleproblEtterHjslag[i] == null) {
					SpraakTaleproblEtterHjslag[i] = java.lang.Double.NaN;
				}
				else {
					SpraakTaleproblEtterHjslag[i] = sSpraakTaleproblEtterHjslag[i];
				}

				if (sSynsproblEtterHjslag[i] == null) {
					SynsproblEtterHjslag[i] = java.lang.Double.NaN;
				}
				else {
					SynsproblEtterHjslag[i] = sSynsproblEtterHjslag[i];
				}

				if (sStatus[i] == null) {
					Status[i] = java.lang.Double.NaN;
				}
				else {
					Status[i] = sStatus[i];
				}

				if (sSykehusIRegionen[i] == null) {
					SykehusIRegionen[i] = java.lang.Double.NaN;
				}
				else {
					SykehusIRegionen[i] = sSykehusIRegionen[i];
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

				if (sTidlHjerneslag[i] == null) {
					TidlHjerneslag[i] = java.lang.Double.NaN;
				}
				else {
					TidlHjerneslag[i] = sTidlHjerneslag[i];
				}

				if (sTidlHjerteinfarkt[i] == null) {
					TidlHjerteinfarkt[i] = java.lang.Double.NaN;
				}
				else {
					TidlHjerteinfarkt[i] = sTidlHjerteinfarkt[i];
				}

				if (sTidlTIA[i] == null) {
					TidlTIA[i] = java.lang.Double.NaN;
				}
				else {
					TidlTIA[i] = sTidlTIA[i];
				}

				if (sTidlHjerneslagType[i] == null) {
					TidlHjerneslagType[i] = java.lang.Double.NaN;
				}
				else {
					TidlHjerneslagType[i] = sTidlHjerneslagType[i];
				}

				if (sTidlTIANaar[i] == null) {
					TidlTIANaar[i] = java.lang.Double.NaN;
				}
				else {
					TidlTIANaar[i] = sTidlTIANaar[i];
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

				if (sTreningEtterHjerneslag[i] == null) {
					TreningEtterHjerneslag[i] = java.lang.Double.NaN;
				}
				else {
					TreningEtterHjerneslag[i] = sTreningEtterHjerneslag[i];
				}

				if (sOpphAntikoagulasjon[i] == null) {
					OpphAntikoagulasjon[i] = java.lang.Double.NaN;
				}
				else {
					OpphAntikoagulasjon[i] = sOpphAntikoagulasjon[i];
				}

				if (sOpphAntikoagProfylakseBeh[i] == null) {
					OpphAntikoagProfylakseBeh[i] = java.lang.Double.NaN;
				}
				else {
					OpphAntikoagProfylakseBeh[i] = sOpphAntikoagProfylakseBeh[i];
				}

				if (sHjerneblInnen36timer[i] == null) {
					HjerneblInnen36timer[i] = java.lang.Double.NaN;
				}
				else {
					HjerneblInnen36timer[i] = sHjerneblInnen36timer[i];
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

				if (sHemikraniektomi[i] == null) {
					Hemikraniektomi[i] = java.lang.Double.NaN;
				}
				else {
					Hemikraniektomi[i] = sHemikraniektomi[i];
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

				if (sUtKombinasjonsbeh[i] == null) {
					UtKombinasjonsbeh[i] = java.lang.Double.NaN;
				}
				else {
					UtKombinasjonsbeh[i] = sUtKombinasjonsbeh[i];
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

				if (sHvorOppstoHjerneslaget[i] == null) {
					HvorOppstoHjerneslaget[i] = java.lang.Double.NaN;
				}
				else {
					HvorOppstoHjerneslaget[i] = sHvorOppstoHjerneslaget[i];
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

				if (sMindreEnn4tSymptInnlegg[i] == null) {
					MindreEnn4tSymptInnlegg[i] = java.lang.Double.NaN;
				}
				else {
					MindreEnn4tSymptInnlegg[i] = sMindreEnn4tSymptInnlegg[i];
				}

				if (sTransportmetode[i] == null) {
					Transportmetode[i] = java.lang.Double.NaN;
				}
				else {
					Transportmetode[i] = sTransportmetode[i];
				}

				Updated[i] = sUpdated[i];
				i++;
			}

			
			log.debug("Creating the R dataframe...");

			RList l = new RList();
			l.put("PasientId", new REXPString(PasientId));
			l.put("PatientInRegistryKey", new REXPString(PatientInRegistryKey));
			l.put("ReshId", new REXPDouble(ReshId));
			l.put("RHF", new REXPString(RHF));
			l.put("RHFresh", new REXPDouble(RHFresh));
			l.put("Organisasjon", new REXPString(Organisasjon));
			l.put("OrgRESH", new REXPDouble(OrgRESH));
			l.put("Avdeling", new REXPString(Avdeling));
			l.put("DataSett", new REXPDouble(DataSett));
			l.put("RelatedID", new REXPDouble(RelatedID));
			l.put("Oppfolgning", new REXPDouble(Oppfolgning));
			l.put("SorteringsParameterVerdi", new REXPString(SorteringsParameterVerdi));
			l.put("OpprettetDato", new REXPString(OpprettetDato));
			l.put("RapportgrunnlagID", new REXPDouble(RapportgrunnlagID));
			l.put("KontaktID", new REXPDouble(KontaktID));
			l.put("KontaktNavn", new REXPString(KontaktNavn));
			l.put("KontaktFraDato", new REXPString(KontaktFraDato));
			l.put("KontaktTilDato", new REXPString(KontaktTilDato));
			l.put("DataSettID", new REXPDouble(DataSettID));
			l.put("FraDato", new REXPString(FraDato));
			l.put("Tildato", new REXPString(Tildato));
			l.put("DSPasientnummer", new REXPString(DSPasientnummer));
			l.put("OverflyttetFraSykehusHvilket", new REXPString(OverflyttetFraSykehusHvilket));
			l.put("ADLAndrespesifisert", new REXPString(ADLAndrespesifisert));
			l.put("BesvartAvAndreSpesifiser", new REXPString(BesvartAvAndreSpesifiser));
			l.put("Dodsaarsak", new REXPString(Dodsaarsak));
			l.put("Fodselsaar", new REXPString(Fodselsaar));
			l.put("Helseforetak", new REXPString(Helseforetak));
			l.put("Kjonn", new REXPString(Kjonn));
			l.put("Kommunenummer", new REXPString(Kommunenummer));
			l.put("Pasientnummer", new REXPString(Pasientnummer));
			l.put("Postnummer", new REXPString(Postnummer));
			l.put("Poststed", new REXPString(Poststed));
			l.put("Region", new REXPString(Region));
			l.put("Registreringsavdeling", new REXPString(Registreringsavdeling));
			l.put("RehabAnnetSpes", new REXPString(RehabAnnetSpes));
			l.put("Skjematype", new REXPString(Skjematype));
			l.put("Sykehus", new REXPString(Sykehus));
			l.put("UtskrTilAnnet", new REXPString(UtskrTilAnnet));
			l.put("Yrke", new REXPString(Yrke));
			l.put("AarsakManglendeOppfAnnen", new REXPString(AarsakManglendeOppfAnnen));
			l.put("TrombolyseHvilketSykehus", new REXPString(TrombolyseHvilketSykehus));
			l.put("TrombektomiHvilketSykehus", new REXPString(TrombektomiHvilketSykehus));
			l.put("HemikraniektomiHvilketSykehus", new REXPString(HemikraniektomiHvilketSykehus));
			l.put("BlodningsstoppBehKlokkeslett", new REXPString(BlodningsstoppBehKlokkeslett));
			l.put("Innleggelsestidspunkt", new REXPString(Innleggelsestidspunkt));
			l.put("Morsdato", new REXPString(Morsdato));
			l.put("OppfolgDato", new REXPString(OppfolgDato));
			l.put("Symptomdebut", new REXPString(Symptomdebut));
			l.put("TrombolyseStarttid", new REXPString(TrombolyseStarttid));
			l.put("TrombektomiStarttidspunkt", new REXPString(TrombektomiStarttidspunkt));
			l.put("HemikraniektomiStarttidspunkt", new REXPString(HemikraniektomiStarttidspunkt));
			l.put("TidInnTrombolyse", new REXPDouble(TidInnTrombolyse));
			l.put("Utskrivingsdato", new REXPString(Utskrivingsdato));
			l.put("Varslingstidspunkt", new REXPString(Varslingstidspunkt));
			l.put("ADLAndre", new REXPDouble(ADLAndre));
			l.put("ADLFamilie", new REXPDouble(ADLFamilie));
			l.put("ADLHjemmehjelp", new REXPDouble(ADLHjemmehjelp));
			l.put("ADLHjemmesykepleien", new REXPDouble(ADLHjemmesykepleien));
			l.put("ADLIngen", new REXPDouble(ADLIngen));
			l.put("ADLInstitusjon", new REXPDouble(ADLInstitusjon));
			l.put("Ataksi", new REXPDouble(Ataksi));
			l.put("BesvartAvAndre", new REXPDouble(BesvartAvAndre));
			l.put("BesvartAvFamilie", new REXPDouble(BesvartAvFamilie));
			l.put("BesvartAvHelsepersonell", new REXPDouble(BesvartAvHelsepersonell));
			l.put("BesvartAvPasient", new REXPDouble(BesvartAvPasient));
			l.put("Dobbeltsyn", new REXPDouble(Dobbeltsyn));
			l.put("PreIngenMedikam", new REXPDouble(PreIngenMedikam));
			l.put("RisikofaktorerIngen", new REXPDouble(RisikofaktorerIngen));
			l.put("NIHSSikkeUtfort", new REXPDouble(NIHSSikkeUtfort));
			l.put("Neglekt", new REXPDouble(Neglekt));
			l.put("RehabAnnet", new REXPDouble(RehabAnnet));
			l.put("RehabDag", new REXPDouble(RehabDag));
			l.put("RehabDogn", new REXPDouble(RehabDogn));
			l.put("RehabSykehjem", new REXPDouble(RehabSykehjem));
			l.put("RehabHjemme", new REXPDouble(RehabHjemme));
			l.put("RehabIngen", new REXPDouble(RehabIngen));
			l.put("RehabOpptreninngssenter", new REXPDouble(RehabOpptreninngssenter));
			l.put("RehabFysInst", new REXPDouble(RehabFysInst));
			l.put("RehabUkjent", new REXPDouble(RehabUkjent));
			l.put("Sensibilitetsutfall", new REXPDouble(Sensibilitetsutfall));
			l.put("SupplerendeUndersIngen", new REXPDouble(SupplerendeUndersIngen));
			l.put("Synsfeltutfall", new REXPDouble(Synsfeltutfall));
			l.put("HemikraniektomiIngen", new REXPDouble(HemikraniektomiIngen));
			l.put("TrombektomiIngen", new REXPDouble(TrombektomiIngen));
			l.put("TrombolyseIngen", new REXPDouble(TrombolyseIngen));
			l.put("OpphIngenAntikoagulasjon", new REXPDouble(OpphIngenAntikoagulasjon));
			l.put("UtIngenMedikam", new REXPDouble(UtIngenMedikam));
			l.put("Vertigo", new REXPDouble(Vertigo));
			l.put("AMKIkkeVarslet", new REXPDouble(AMKIkkeVarslet));
			l.put("AntDagerInnl", new REXPDouble(AntDagerInnl));
			l.put("AkutteFokaleutfallPosBilleddiag", new REXPDouble(AkutteFokaleutfallPosBilleddiag));
			l.put("AkutteFokaleUtfallUtenBilleddiag", new REXPDouble(AkutteFokaleUtfallUtenBilleddiag));
			l.put("AndreFokaleSympt", new REXPDouble(AndreFokaleSympt));
			l.put("Armparese", new REXPDouble(Armparese));
			l.put("Atrieflimmer", new REXPDouble(Atrieflimmer));
			l.put("AvdForstInnlagt", new REXPDouble(AvdForstInnlagt));
			l.put("AvdForstInnlagtHvilken", new REXPDouble(AvdForstInnlagtHvilken));
			l.put("AvdUtskrFra", new REXPDouble(AvdUtskrFra));
			l.put("AvdUtskrFraHvilken", new REXPDouble(AvdUtskrFraHvilken));
			l.put("BedringEtterHjerneslag", new REXPDouble(BedringEtterHjerneslag));
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
			l.put("CerebralCTInn", new REXPDouble(CerebralCTInn));
			l.put("PreDiabetes", new REXPDouble(PreDiabetes));
			l.put("SvelgtestUtfort", new REXPDouble(SvelgtestUtfort));
			l.put("Facialisparese", new REXPDouble(Facialisparese));
			l.put("ForflytningPre", new REXPDouble(ForflytningPre));
			l.put("Forflytning3mnd", new REXPDouble(Forflytning3mnd));
			l.put("HjerneblodningsstoppBeh", new REXPDouble(HjerneblodningsstoppBeh));
			l.put("HjerneblodningsstoppBehHvilken", new REXPDouble(HjerneblodningsstoppBehHvilken));
			l.put("PreA2Antagonist", new REXPDouble(PreA2Antagonist));
			l.put("PreACEhemmer", new REXPDouble(PreACEhemmer));
			l.put("PreASA", new REXPDouble(PreASA));
			l.put("PreKombinasjonsbeh", new REXPDouble(PreKombinasjonsbeh));
			l.put("PreBetablokker", new REXPDouble(PreBetablokker));
			l.put("PreDipyridamol", new REXPDouble(PreDipyridamol));
			l.put("PreDiuretica", new REXPDouble(PreDiuretica));
			l.put("PreKalsiumanatgonist", new REXPDouble(PreKalsiumanatgonist));
			l.put("PreKlopidogrel", new REXPDouble(PreKlopidogrel));
			l.put("PreStatinerLipid", new REXPDouble(PreStatinerLipid));
			l.put("PreWarfarin", new REXPDouble(PreWarfarin));
			l.put("PreAndreEnnWarfarin", new REXPDouble(PreAndreEnnWarfarin));
			l.put("PreHjerteKarintervensj", new REXPDouble(PreHjerteKarintervensj));
			l.put("PreHjerteKarintervensjTidsinterv", new REXPDouble(PreHjerteKarintervensjTidsinterv));
			l.put("TverrfagligVurdering", new REXPDouble(TverrfagligVurdering));
			l.put("HjelpEtterHjerneslag", new REXPDouble(HjelpEtterHjerneslag));
			l.put("PreMedikBehLipidsenkning", new REXPDouble(PreMedikBehLipidsenkning));
			l.put("InnlagtSykehusEtterUtskr", new REXPDouble(InnlagtSykehusEtterUtskr));
			l.put("MobiliseringInnen24Timer", new REXPDouble(MobiliseringInnen24Timer));
			l.put("LegekontrollEtterHjerneslag", new REXPDouble(LegekontrollEtterHjerneslag));
			l.put("PreMedikBehHoytBT", new REXPDouble(PreMedikBehHoytBT));
			l.put("MedisinMotBlodpropp", new REXPDouble(MedisinMotBlodpropp));
			l.put("MedisinHoytBlodtrykk3mnd", new REXPDouble(MedisinHoytBlodtrykk3mnd));
			l.put("MedisinHoytKolesterol", new REXPDouble(MedisinHoytKolesterol));
			l.put("Obdusert", new REXPDouble(Obdusert));
			l.put("OppfolgUtf", new REXPDouble(OppfolgUtf));
			l.put("OverflyttetFraSykehus", new REXPDouble(OverflyttetFraSykehus));
			l.put("Pasientstatus", new REXPDouble(Pasientstatus));
			l.put("PaakledningPre", new REXPDouble(PaakledningPre));
			l.put("Paakledning3mnd", new REXPDouble(Paakledning3mnd));
			l.put("MRSPre", new REXPDouble(MRSPre));
			l.put("MRS3mnd", new REXPDouble(MRS3mnd));
			l.put("OperertHalspulsaare", new REXPDouble(OperertHalspulsaare));
			l.put("ReinnlagtTypeSlag", new REXPDouble(ReinnlagtTypeSlag));
			l.put("RoykerPre", new REXPDouble(RoykerPre));
			l.put("Royker3mnd", new REXPDouble(Royker3mnd));
			l.put("YrkesaktivUnderHjerneslag2", new REXPDouble(YrkesaktivUnderHjerneslag2));
			l.put("YrkesaktivNaa", new REXPDouble(YrkesaktivNaa));
			l.put("KjorteBilForHjerneslag", new REXPDouble(KjorteBilForHjerneslag));
			l.put("KjorerBilNaa", new REXPDouble(KjorerBilNaa));
			l.put("Sidelokasjon", new REXPDouble(Sidelokasjon));
			l.put("SivilstatusPre", new REXPDouble(SivilstatusPre));
			l.put("Sivilstatus3mnd", new REXPDouble(Sivilstatus3mnd));
			l.put("Slagdiagnose", new REXPDouble(Slagdiagnose));
			l.put("SpraakTaleproblem", new REXPDouble(SpraakTaleproblem));
			l.put("SpraakTaleproblEtterHjslag", new REXPDouble(SpraakTaleproblEtterHjslag));
			l.put("SynsproblEtterHjslag", new REXPDouble(SynsproblEtterHjslag));
			l.put("Status", new REXPDouble(Status));
			l.put("SykehusIRegionen", new REXPDouble(SykehusIRegionen));
			l.put("VaaknetMedSymptom", new REXPDouble(VaaknetMedSymptom));
			l.put("TimerSymptomdebutInnlegg", new REXPDouble(TimerSymptomdebutInnlegg));
			l.put("TidlHjerneslag", new REXPDouble(TidlHjerneslag));
			l.put("TidlHjerteinfarkt", new REXPDouble(TidlHjerteinfarkt));
			l.put("TidlTIA", new REXPDouble(TidlTIA));
			l.put("TidlHjerneslagType", new REXPDouble(TidlHjerneslagType));
			l.put("TidlTIANaar", new REXPDouble(TidlTIANaar));
			l.put("Tilfredshet", new REXPDouble(Tilfredshet));
			l.put("ToalettbesokPre", new REXPDouble(ToalettbesokPre));
			l.put("Toalettbesok3mnd", new REXPDouble(Toalettbesok3mnd));
			l.put("TreningEtterHjerneslag", new REXPDouble(TreningEtterHjerneslag));
			l.put("OpphAntikoagulasjon", new REXPDouble(OpphAntikoagulasjon));
			l.put("OpphAntikoagProfylakseBeh", new REXPDouble(OpphAntikoagProfylakseBeh));
			l.put("HjerneblInnen36timer", new REXPDouble(HjerneblInnen36timer));
			l.put("Trombolyse", new REXPDouble(Trombolyse));
			l.put("Trombektomi", new REXPDouble(Trombektomi));
			l.put("Hemikraniektomi", new REXPDouble(Hemikraniektomi));
			l.put("UtA2Antagonist", new REXPDouble(UtA2Antagonist));
			l.put("UtACEhemmer", new REXPDouble(UtACEhemmer));
			l.put("UtASA", new REXPDouble(UtASA));
			l.put("UtKombinasjonsbeh", new REXPDouble(UtKombinasjonsbeh));
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
			l.put("HvorOppstoHjerneslaget", new REXPDouble(HvorOppstoHjerneslaget));
			l.put("Alder", new REXPDouble(Alder));
			l.put("DagerSymptDebutTilOppf", new REXPDouble(DagerSymptDebutTilOppf));
			l.put("NIHSSinnkomst", new REXPDouble(NIHSSinnkomst));
			l.put("NIHSSpreTrombolyse", new REXPDouble(NIHSSpreTrombolyse));
			l.put("NIHSSetterTrombolyse", new REXPDouble(NIHSSetterTrombolyse));
			l.put("NIHSSpreTrombektomi", new REXPDouble(NIHSSpreTrombektomi));
			l.put("NIHSSetterTrombektomi", new REXPDouble(NIHSSetterTrombektomi));
			l.put("DagerInnleggelseTilDod", new REXPDouble(DagerInnleggelseTilDod));
			l.put("MindreEnn4tSymptInnlegg", new REXPDouble(MindreEnn4tSymptInnlegg));
			l.put("Transportmetode", new REXPDouble(Transportmetode));
			l.put("Updated", new REXPString(Updated));
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
