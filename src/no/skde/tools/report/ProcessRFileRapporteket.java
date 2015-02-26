/**
 * no.skde.tools.report
 * ProcessRFileRapporteket.java Feb 24 2015 Are Edvardsen
 * 
 * For shipping any file produced by R as an email attachment.
 * Tailored for routine exports of data, but might be extended.
 * Recipient email address is defined in the jrxml report definition.
 *   
 * Copyleft 2015 SKDE
 */

package no.skde.tools.report;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.fill.*;
import org.apache.log4j.Logger;
import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.*;


public class ProcessRFileRapporteket extends JRDefaultScriptlet {
	protected RConnection rconn;
	
	private String jasperReportFeedback;
	private String attachmentPathFileName;
	private String recipientEmailAddress;
	private String rFunctionFeedback;
	
	static Logger log = Logger.getLogger("report");
	
	// since at Rapporteket, hardcode the reply-to email address
	private static final String REPLY_TO_EMAIL_ADDRESS = "noreply@helseregiste.no";
	
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
	
	public String getRecipientEmailAddress() {
		return recipientEmailAddress;
	}

	public void setRecipientEmailAddress(String recipientEmailAddress) {
		this.recipientEmailAddress = recipientEmailAddress;
	}
	
	public String getRFunctionFeedback() {
		return rFunctionFeedback;
	}
	
	public void setRFunctionFeedback(String rFunctionFeedback) {
		this.rFunctionFeedback = rFunctionFeedback;
	}
	
	
	// override empty method of JRDefaultScriptlet
	public void afterReportInit() throws JRScriptletException {
		
		generateReport();

		super.afterReportInit();
	}
	
	
	// report actions
	private void generateReport() {
		try {
			log.info("Start generating report using " + ProcessRFileRapporteket.class.getName());
			
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
			
			String recipientEmailAddress = (String) ((JRFillParameter) parametersMap.get("recipientEmailAddress")).getValue();
			if (recipientEmailAddress == "") {
				log.warn("recipientEmailAddress is empty. Obviously, shipping by email will not be possible");
			}
			else {
				setRecipientEmailAddress(recipientEmailAddress);
				log.debug("recipientEmailAddress=" + getRecipientEmailAddress());
			}
			
			String loggedInUserAVD_RESH = (String) ((JRFillParameter) parametersMap.get("LoggedInUserAVD_RESH")).getValue();
			if (loggedInUserAVD_RESH == "") {
				log.warn("loggedInUserAVD_RESH is empty. At least, check that data access is not borked...");
			}
			else {
				log.debug("loggedInUserAVD_RESH=" + loggedInUserAVD_RESH);
			}
			
			String rSourceFileName = (String) ((JRFillParameter) parametersMap.get("reportFileName")).getValue();
			if (rSourceFileName == "") {
				log.warn("rSourceFileName is empty. Eventually, processing an undefined report will fail");
			}
			else {
				// cannot get the name of the report as it is stated in jrxml definition
				// thus, file name will have to do, at least the report can be identified from the log entry
				log.info("rSourceFileName: " + rSourceFileName);
			}
			
			String rFunctionCallString = (String) ((JRFillParameter) parametersMap.get("rFunctionCallString")).getValue();
			if (rFunctionCallString == "") {
				log.warn("rFunctionCallString is empty. Eventualley, the report will fail. Fix the report definition (jrxml)");
			}
			else {
				log.debug("rFunctionCallString: " + rFunctionCallString);
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
								
			
			// process R function(s) 
			log.debug("R is now processing...");
			REXP rWorkdir = rconn.eval("getwd()");
			String workdir = rWorkdir.asString();
			log.debug("The Rserve session's current working directry is: " + workdir);
			rconn.assign("workfile", "../" + rSourceFileName);
			
			// source the R file
			log.debug("Sourcing R-function ../" + rSourceFileName);
			rconn.assign("source_file", "../" + rSourceFileName);
			rconn.voidEval("source(source_file)");
			
			// run the R function
			log.debug("Running R-function: " + rFunctionCallString);
			String rcmd = rFunctionCallString;
			log.debug("rcmd: " + rcmd);
			rconn.voidEval(rcmd);
			//rconn.voidEval(rFunctionCallString);
			
			// get the attachment filename from R
			log.debug("Getting attachement file name from R function");
			REXP rAttachmentFileName = rconn.eval("out$attachmentFileName");
			String attachmentFileName = rAttachmentFileName.asString();
			log.debug("Attachment file name: " + attachmentFileName);
			
			// get any message from R
			log.debug("Getting any messages from R function");
			REXP rMessage = rconn.eval("out$message");
			setRFunctionFeedback(rMessage.asString());
			
			// set name and path of attachment
			setAttachmentPathFileName(workdir + "/" + attachmentFileName);
			log.debug("Attachment file name and path: " + getAttachmentPathFileName());
			
			// start process for email shipment
			sendEmail(emailSubject, REPLY_TO_EMAIL_ADDRESS, dryRun);
			
			// Clean up by removing Rserve workdir ever created
			log.info("Cleaning up and closing down Rserve leftovers...");
			rconn.voidEval("setwd('../')");
			REXP rWorkdirNow = rconn.eval("getwd()");
			String workdirNow = rWorkdirNow.asString();
			log.debug("The Rserve session's current working directry is: " + workdirNow);
			log.debug("Asking R-session to delete its own working directory: " + workdir);
			rconn.voidEval("unlink('" + workdir + "', recursive = T)");
			
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
			sender.sendEmail(getRecipientEmailAddress(), emailSubject, getAttachmentPathFileName(), replyToEmailAddress, dryRun);
			log.info("Report sent to " + getRecipientEmailAddress());
			setJasperReportFeedback("Epost sendt til " + getRecipientEmailAddress() + getRFunctionFeedback());
		} catch (Exception e) {
			setJasperReportFeedback("Feil: epost ble ikke sendt!");
			log.error("Could not send email: " + e.getMessage());
		}
	}
}
