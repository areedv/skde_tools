/**
 * no.skde.tools.report
 * ProcessNowebRapporteket.java Nov 12 2013 Are Edvardsen
 * 
 * 
 *  Copyleft 2013 SKDE
 */

package no.skde.tools.report;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.fill.*;

import org.apache.log4j.Logger;
import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.*;

public class ProcessNowebRapporteket extends JRDefaultScriptlet {
	protected RConnection rconn;
	
	private String jasperReportFeedback;
	private String attachmentPathFileName;
	private String loggedInUserEmailAddress;
	
	static Logger log = Logger.getLogger("report");
	
	// since at Rapporteket, hardcode the reply-to email address
	private static final String REPLY_TO_EMAIL_ADDRESS = "noreply@helseregister.no";
	
	// getters and setters
	public void setJasperReportFeedback(String jasperReportFeedback) {
		this.jasperReportFeedback = jasperReportFeedback;
	}

	public String getJasperReportFeedback() {
		return jasperReportFeedback;
	}
	
	public void setAttachmentPathFileName(String attachmentPathFileName) {
		this.attachmentPathFileName = attachmentPathFileName;
	}
	
	public String getAttachmentPathFileName() {
		return attachmentPathFileName;
	}
	
	public String getLoggedInUserEmailAddress() {
		return loggedInUserEmailAddress;
	}

	public void setLoggedInUserEmailAddress(String loggedInUserEmailAddress) {
		this.loggedInUserEmailAddress = loggedInUserEmailAddress;
	}
	
	
		// override empty method of JRDefaultScriptlet
	public void afterReportInit() throws JRScriptletException {
		
		generateReport();

		super.afterReportInit();
	}
	
	
	// report actions
	// Probably because List array of type string cannot know what will be returned by the JRFillParameter
	@SuppressWarnings("unchecked")
	private void generateReport() {
		try {
			
			rconn = new RConnection();
			log.debug("R connection provided: " + rconn.toString());
			
			// get report parameters
			log.debug("Getting report parameters...");
			
			String loggedInUserFullName = (String) ((JRFillParameter) parametersMap.get("LoggedInUserFullName")).getValue();
			log.debug("Got first prameter...");
			if (loggedInUserFullName == "") {
				log.warn("loggedInUserFullName is empty. No good, patron...");
			}
			else {
				log.info("Report requested by " + loggedInUserFullName);
			}
			
			String loggedInUserEmailAddress = (String) ((JRFillParameter) parametersMap.get("LoggedInUserEmailAddress")).getValue();
			if (loggedInUserEmailAddress == "") {
				log.warn("loggedInUserEmailAddress is empty. Obviously, shipping by email will not be possible");
			}
			else {
				setLoggedInUserEmailAddress(loggedInUserEmailAddress);
				log.debug("loggedInUserEmailAddress=" + getLoggedInUserEmailAddress());
			}
			
			String loggedInUserAVD_RESH = (String) ((JRFillParameter) parametersMap.get("LoggedInUserAVD_RESH")).getValue();
			if (loggedInUserAVD_RESH == "") {
				log.warn("loggedInUserAVD_RESH is empty. At least, check that data access is not borked...");
			}
			else {
				log.debug("loggedInUserAVD_RESH=" + loggedInUserAVD_RESH);
			}
			
			String reportFileName = (String) ((JRFillParameter) parametersMap.get("reportFileName")).getValue();
			if (reportFileName == "") {
				log.warn("reportFileName is empty. Eventually, processing an undefined report will fail");
			}
			else {
				// cannot get the name of the report as it is stated in jrxml definition
				// thus, file name will have to do, at least the report can be identified from the log entry
				log.info("Start to generate report " + reportFileName);
			}
			
			boolean useRPackage = false;
			String rPackage = "";
			try {
				rPackage = (String) ((JRFillParameter) parametersMap.get("rPackage")).getValue();
				if (rPackage == null) {
					rPackage = "";
					log.debug("Parameter rPackage is not defined");
				} else {
					useRPackage = true;
					rconn.voidEval("library(" + rPackage + ")");
					log.debug("Library " + rPackage + " is loaded in the R session");
				}
			} catch (Exception e) {
				log.warn("Could not get parameter rPackage from report definition: " + e.getMessage());
			}
			
			Integer useKnitr = (Integer) ((JRFillParameter) parametersMap.get("useKnitr")).getValue();
			boolean knitr = false;
			if (useKnitr == 1) {
				knitr = true;
			}
			
			Integer doSendEmail = (Integer) ((JRFillParameter) parametersMap.get("doSendEmail")).getValue();
			boolean dryRun = false;
			if (doSendEmail == 0) {
				dryRun = true;
			}
			
			String emailSubject = (String) ((JRFillParameter) parametersMap.get("emailSubject")).getValue();
			if (emailSubject == "") {
				log.warn("No email subject provided, might confuse the recipient...");
			}
			
			Integer usePdfAnnotation;
			try {
				usePdfAnnotation = (Integer) ((JRFillParameter) parametersMap.get("usePdfAnnotation")).getValue();
				if (usePdfAnnotation == null) {
					usePdfAnnotation = 0;
				}
				if (usePdfAnnotation == 0) {
					rconn.voidEval("usePdfAnnotation=F");
					log.info("PDF annotations were not requested in this report");
				} else {
					rconn.voidEval("usePdfAnnotation=T");
					log.info("PDF annotations are requested in this report");
				}
			} catch (Exception e) {
				log.debug("Parameter usePdfAnnotations is not defined: " + e.getMessage());
			}
			
			Integer reportYear;
			try {
				reportYear = (Integer) ((JRFillParameter) parametersMap.get("reportYear")).getValue();
				if (reportYear == null) {
					reportYear = Calendar.getInstance().get(Calendar.YEAR) - 1;
				}
				rconn.voidEval("reportYear=" + reportYear.toString());
				log.debug("Prameter 'reportYear' set to " + reportYear.toString());
			} catch (Exception e) {
				log.debug("Parameter 'reportYear' is not defined: " + e.getMessage());
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
				log.debug("Parameter 'beginDate' set to " + beginDateString);
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
				log.debug("Parameter 'endDate' set to " + endDateString);
			} catch (Exception e) {
				log.debug("Parameter endDate is not defined: " + e.getMessage());
			}

			// generic multi select list of values
			List<String> flervalgslisteList = new ArrayList<String>();
			String flervalgsliste;
			try {
				flervalgslisteList = (List<String>) ((JRFillParameter) parametersMap.get("flervalgsliste")).getValue();
				flervalgsliste = "c(";
				if (flervalgslisteList.isEmpty()) {
					flervalgsliste = flervalgsliste + "'')";
				} else {
					Iterator<String> iterator = flervalgslisteList.iterator();
					while (iterator.hasNext()) {
						flervalgsliste = flervalgsliste + "'" + iterator.next() + "',";
					}
					flervalgsliste = flervalgsliste.substring(0, flervalgsliste.length()-1);
					flervalgsliste = flervalgsliste + ")";
				}
				log.debug("R concat for flervalgsliste vector is " + flervalgsliste);
				rconn.voidEval("flervalgsliste=" + flervalgsliste);
			} catch (Exception e) {
				log.debug("Parameter 'flervalgsliste' is not defined: " + e.getMessage());
			}
					
			
			// process noweb files 
			log.debug("noweb processing...");
			REXP rWorkdir = rconn.eval("getwd()");
			String workdir = rWorkdir.asString();
			log.debug("The Rserve session's current working directry is: " + workdir);
			log.debug("Making loggedInUserAVD_RESH available to current R session");
			rconn.assign("reshID", loggedInUserAVD_RESH);
			rconn.assign("reportTmpFileName", reportFileName);
			
			if (useRPackage) {
				log.debug("R-stuff to be taken from package");
				String Rcmd = "system.file('" + reportFileName + ".Rnw', package='" + rPackage + "')";
				REXP packageNowebFile = rconn.eval(Rcmd);
				log.debug("Path and name of noweb file: " + packageNowebFile.asString());
				rconn.assign("workfile", packageNowebFile.asString());
				if (knitr) {
					log.debug("Continue processing using Knitr, forcing utf8 encoding...");
					rconn.voidEval("knitr::knit(workfile, encoding='UTF-8')");
				}
				else {
					log.debug("Continue processing using Sweave. If this is not what you want, edit jrxml report definition accordingly");
					rconn.voidEval("Sweave(workfile, encoding='utf8')");
				}
			
			}
			else {
				rconn.assign("workfile", "../" + reportFileName);
				REXP workfilename = rconn.eval("paste(workfile, '.Rnw', sep='')");
				log.debug("Rserve current workfile is: " + workfilename.asString());
				if (knitr) {
					log.debug("Continue processing using Knitr...");
					rconn.voidEval("library(knitr)");
					rconn.voidEval("file.copy(paste0(workfile, '.Rnw'), '.')");
					rconn.voidEval("knit(basename(paste(workfile, '.Rnw', sep='')))");
				}
				else {
					log.debug("Continue processing using Sweave. If this is not what you want, edit jrxml report definition accordingly");
					rconn.voidEval("Sweave(paste(workfile, '.Rnw', sep='')" + ", encoding=" + "'" + "utf8" + "'" + ")");
				}
			}
			log.debug("Running texi2dvi on resulting LaTeX file...");
			rconn.voidEval("tools::texi2dvi(paste(reportTmpFileName, '.tex', sep=''), pdf=T, clean=T)");
			
			// make a temporary file name for attachment
			File tmpFile = new File(workdir);
			String attachmentFile = (File.createTempFile(reportFileName, ".pdf", tmpFile)).getName();
			log.debug("Attachment file name: " + attachmentFile);
			setAttachmentPathFileName(workdir + "/" + attachmentFile);
			log.debug("Attachment file name and path: " + getAttachmentPathFileName());
			
			// copy report to temp file
			log.debug("Rserve assignment: reportFileNamePath<-" + getAttachmentPathFileName());
			rconn.assign("reportFileNamePath", getAttachmentPathFileName());
			log.debug("Rserve file copy from: " + reportFileName + ".pdf to " + getAttachmentPathFileName());
			rconn.voidEval("file.copy(paste(reportTmpFileName, '.pdf', sep=''), reportFileNamePath, overwrite=T)");
			log.debug("Report is prepared and ready to be shipped by email");
			
			// start process for email shipment
			sendEmail(emailSubject, REPLY_TO_EMAIL_ADDRESS, dryRun);
			
			// Clean up by removing Rserve workdir ever created
			log.debug("Cleaning up and closing down Rserve leftovers...");
			rconn.voidEval("file.remove(dir(pattern='pdf$|tex$|Rnw$'))");
			rconn.voidEval("setwd('../')");
			REXP rWorkdirNow = rconn.eval("getwd()");
			String workdirNow = rWorkdirNow.asString();
			log.debug("The Rserve session's current working directry is: " + workdirNow);
			rconn.voidEval("file.remove(dir(pattern='conn*'))");
			
			rconn.close();
			rconn = null;
		} catch (RserveException rse) {
			log.error("Rserv exception " + rse.getMessage());
			rconn.close();
			rconn = null;
		} catch (Exception e) {
			log.error("Something went wrong, but it is not Rserv: " + e.getMessage());
			rconn.close();
			rconn = null;
		}
		
	}
	
	private void sendEmail(String emailSubject, String replyToEmailAddress, boolean dryRun) {
		log.debug("Report being shipped by email...");
		try {
			SendReportByEmail sender = new SendReportByEmail();
			sender.sendEmail(getLoggedInUserEmailAddress(), emailSubject, getAttachmentPathFileName(), replyToEmailAddress, dryRun);
			log.info("Report sent to " + getLoggedInUserEmailAddress());
			setJasperReportFeedback("Epost sendt til " + getLoggedInUserEmailAddress());
		} catch (Exception e) {
			setJasperReportFeedback("Feil: epost ble ikke sendt!");
			log.error("Could not send email: " + e.getMessage());
		}
	}
}
