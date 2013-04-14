/**
 * no.helsenord.tools.report
 * ProcessSweave.java Aug 8 2012 Are Edvardsen
 * 
 * 
 *  Copyleft 2011, 2012 SKDE
 */

package no.skde.tools.report;

import java.io.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.fill.*;
import org.apache.log4j.Logger;
//import no.skde.tools.report.*;
import no.helseregister.tools.security.*;
import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.*;

public class ProcessSweave extends JRDefaultScriptlet {
	protected RConnection rconn;
	
	private String jasperReportFeedback;
	
	private HRegUser hregUser = null;
	
	private String attachmentPathFileName;
	
	static Logger log = Logger.getLogger("report");
	
	
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
	
	
	// override empty method of JRDefaultScriptlet
	public void afterReportInit() throws JRScriptletException {
		// get hreg_key from report report params
		if (parametersMap.containsKey("hreg_key")) {
			String hreg_key = (String) ((JRFillParameter) parametersMap
					.get("hreg_key")).getValue();
			if (hreg_key != null) {
				hreg_key = hreg_key.replaceAll(" ", "+");
				loginUser(hreg_key);
			}
			log.debug("parametersMap contains Key ='hreg_key' : "
					+ hreg_key);
		} else {
			log.warn("parametersMap do not contain Key ='hreg_key'");
		}
		
		generateReport();

		super.afterReportInit();
	}
	
	
	// report actions
	private void generateReport() {
		try {
			rconn = new RConnection();
			
			log.info("Report requested by " + hregUser.getQregUserFirstName()
					+ " " + hregUser.getQregUserLastName());
			
			// get report parameters
			String reportFileName = (String) ((JRFillParameter) parametersMap.get("reportFileName")).getValue();
			if (reportFileName == "") {
				log.warn("reportFileName is empty. Eventually, processing an undfined report will fail");
			}
			else {
				// cannot get the name of the report as it is stated in jrxml definition
				// thus, file name will have to do, at least the report can be identified from the log entry
				log.info("Start to generate report " + reportFileName);
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
			
			// make Sweave and LaTeX 
			log.debug("Doing Sweave and LaTeX...");
			REXP rWorkdir = rconn.eval("getwd()");
			String workdir = rWorkdir.asString();
			log.debug("The Rserve session's current working directry is: " + workdir);
			rconn.assign("workfile", "../" + reportFileName);
			rconn.assign("reportTmpFileName", reportFileName);
			REXP workfilename = rconn.eval("paste(workfile, '.Rnw', sep='')");
			log.debug("Rserve current workfile is: " + workfilename.asString());
			rconn.voidEval("Sweave(paste(workfile, '.Rnw', sep=''))");
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
			sendEmail(emailSubject, dryRun);
			
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
	
	private void sendEmail(String emailSubject, boolean dryRun) {
		log.debug("Report being shipped by email...");
		try {
			SendReportByEmail sender = new SendReportByEmail();
			String userEmailAddress = hregUser.getHregUserEmail();
			sender.sendEmail(userEmailAddress, emailSubject, getAttachmentPathFileName(), dryRun);
			log.info("Report sent to " + userEmailAddress);
			setJasperReportFeedback("Epost sendt til " + userEmailAddress);
		} catch (Exception e) {
			setJasperReportFeedback("Feil: epost ble ikke sendt!");
			log.error("Could not send email: " + e.getMessage());
		}
	}
	
	private void loginUser(String hreg_key) throws JRScriptletException {
		log.debug("loginUser: ");
		try {
			hregUser = new HRegUser(hreg_key, "certFile.cert", true);
			log.debug("Valid user, moving on...");
		} catch (Exception e) {
			log.warn("Couldn't create HRegUser from parameter. " + e);
		}
	}
}
