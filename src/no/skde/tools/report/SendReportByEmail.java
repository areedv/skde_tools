/**
 * no.skde.tools.report
 * SendReportByEmail.java Jul 6 2011 Are Edvardsen
 * 
 * 
 *  Copyleft 2011 SKDE
 */

package no.skde.tools.report;

import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
//import java.io.BufferedReader;
import java.io.FileReader;
import org.apache.log4j.Logger;

public class SendReportByEmail {

//	protected static Logger log = Logger.getLogger(SendReportByEmail.class);
	protected static Logger log = Logger.getLogger("report");
	
	private static final String  MAIL_SERVER_CONFIG_PATH_FILE = 
		"/opt/jasper/mail/conf/MailServerReport.conf";
	
	
	public void sendEmail (String toAddress, String subject,
			String attachmentFile, boolean dryRun) {
	      // Get the default Session object.
	      Session session = Session.getDefaultInstance(fMailServerConfig, null);

	      try {
	         // Create a default MimeMessage object.
	         MimeMessage message = new MimeMessage(session);

	         // Set To: header field of the header.
	         message.addRecipient(Message.RecipientType.TO,
	                                  new InternetAddress(toAddress));

	         // Set Subject: header field
	         message.setSubject(subject);
	         
	         // create the message part
	         BodyPart messageBodyPart = new MimeBodyPart();

	         // set the actual message, a general from file
	         String emailMessage = fileToString("/opt/jasper/mail/message/general.txt");
	         messageBodyPart.setText(emailMessage);
	         
	         
	         // create a multipart message
	         Multipart multipart = new MimeMultipart();
	         
	         // set text message part
	         multipart.addBodyPart(messageBodyPart);
	         
	         // second part is attachment
	         messageBodyPart = new MimeBodyPart();
//	         String fileName = "attachment.txt";
	         String[] attachment = attachmentFile.split("/");
	         String attachmentFileName = attachment[attachment.length-1];
	         DataSource source = new FileDataSource(attachmentFile);
	         messageBodyPart.setDataHandler(new DataHandler(source));
	         messageBodyPart.setFileName(attachmentFileName);
	         multipart.addBodyPart(messageBodyPart);

	         // complete message parts
	         message.setContent(multipart);
	         
	         // Send message
	         if (dryRun) {
	        	 log.info("Everything ok, but Email not actually sent since dryRun is set true.");
	         }
	         else {
	        	 Transport.send(message);
	        	 log.info("Email was sent successfully.");
	         }
	      }
	      catch (MessagingException e) {
	         log.error("Could not send email: " + e);
	      }
	   }

	
	   // read file content
	   public static String fileToString(String fileName) {
		   File file = new File(fileName);
		   StringBuilder contents = new StringBuilder();
		   String separator = System.getProperty("line.separator");
		   BufferedReader input = null;
		   try {
			   input = new BufferedReader(new FileReader(file));
			   String line = null;
			   while ((line = input.readLine()) != null)
				   contents.append(line).append(separator);
		   }
		   catch (Exception ex) {
			   ex.printStackTrace();
		   }
		   finally {
			   try {
				   if(input != null) input.close();
			   }
			   catch (Exception ex) {
				   ex.printStackTrace();
			   }
		   }
		   return contents.toString();
	   }
	   
	   /**
	    * Allows config to be refreshed at runtime
	    */
	   public static void refreshConfig() {
		   fMailServerConfig.clear();
		   fetchConfig();
	   }
	   
	   private static Properties fMailServerConfig = new Properties();
	   
	   static {
		   fetchConfig();
	   }
	   
	   private static void fetchConfig() {
		   InputStream input = null;
		   try {
			   input = new FileInputStream(MAIL_SERVER_CONFIG_PATH_FILE);
			   fMailServerConfig.load( input );
		   }
		   catch (IOException e) {
			   log.error("Cannot open/load conf file: " + e);
		   }
		   finally {
			   try {
				   if ( input != null ) input.close();
			   }
			   catch ( IOException e) {
				   log.error("Cannot close conf file: " + e);
			   }
		   }
	   }
	
}
