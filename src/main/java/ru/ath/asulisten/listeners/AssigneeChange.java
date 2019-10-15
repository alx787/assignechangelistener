package ru.ath.asulisten.listeners;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import ru.ath.asulisten.tools.PluginTools;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class AssigneeChange implements InitializingBean, DisposableBean {

    private final Logger logger = Logger.getLogger(this.getClass());

    @ComponentImport
    private EventPublisher eventPublisher;

    @Inject
    public void assigneeChange(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void destroy() throws Exception {
        this.eventPublisher.unregister(this);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.eventPublisher.register(this);
    }


    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) {

        Long eventTypeId = issueEvent.getEventTypeId();
        Issue issue = issueEvent.getIssue();

//        this.logger.warn(" ============= begin log ============= ");
//        this.logger.warn(issueEvent.toString());

        if (eventTypeId.equals(EventType.ISSUE_ASSIGNED_ID)) {

            String textForMail = PluginTools.getMailText(issue);

            PluginTools.sendEmail(issue, textForMail);

//            this.logger.warn(" -> issue " + issue.getKey() + " <- ");
//            this.logger.warn(" -> assignee changing <- ");
//
//            this.logger.warn(" -> text for mail " + textForMail + " <- ");

        }
//        this.logger.warn(" ============= end log ============= ");
    }
}
