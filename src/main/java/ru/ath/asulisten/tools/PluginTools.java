package ru.ath.asulisten.tools;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.mail.MailException;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.Email;
import com.atlassian.velocity.VelocityManager;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class PluginTools {

    public static String getMailText(Issue issue) {
        String result = "";

        VelocityManager vm = ComponentAccessor.getVelocityManager();

        CustomField userNameCf = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(10300L);
        CustomField userEmailCf = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(10302L);

        String userName = (String)issue.getCustomFieldValue(userNameCf);
        String userEmail = (String)issue.getCustomFieldValue(userEmailCf);

        String reporter = "";

        if (userName.equals("")) {
            userName = null;
        }

        if (userEmail.equals("")) {
            userEmail = null;
        }

        if (userName != null) {
            reporter = userName;
        } else {
            if (userEmail != null) {
                reporter = userEmail;
            }
        }

        Map params = new HashMap();
        params.put("reporter", reporter);
        params.put("summary", issue.getSummary());
        params.put("description", issue.getDescription());
        params.put("assignee", issue.getAssignee().getDisplayName());

        result = vm.getEncodedBody("/templates/", "mail.vm", "UTF-8", params);

        return result;
    }

    public static void sendEmail(Issue issue, String mailBody) {

        CustomField userEmailCf = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(10302L);

        String userEmail = (String)issue.getCustomFieldValue(userEmailCf);
        //userEmail = "aak@kiravto.ru";

        final Logger log = Logger.getLogger(mailBody.getClass());


        if (userEmail.equals("") || userEmail == null) {
            log.warn(" ======= not found user email address. mail not send");
            return;
        }

        SMTPMailServer mailServer = ComponentAccessor.getMailServerManager().getDefaultSMTPMailServer();

        if (mailServer == null) {
            log.warn(" ======= not found mail server. mail not send");
            return;
        }

        Email email = new Email(userEmail);

        email.setMimeType("text/html");
        email.setSubject("Задача поставлена в очередь");
        email.setBody(mailBody);

        try {
            mailServer.send(email);
        } catch (MailException e) {
            log.warn(" ======= error when trying send mail. mail not send");
            log.warn(ExceptionUtils.getStackTrace(e));
            return;

        }

    }

}
