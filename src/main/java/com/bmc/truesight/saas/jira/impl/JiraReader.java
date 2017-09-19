package com.bmc.truesight.saas.jira.impl;

import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bmc.truesight.saas.jira.api.JiraAPI;
import com.bmc.truesight.saas.jira.beans.Configuration;
import com.bmc.truesight.saas.jira.beans.JiraEventResponse;
import com.bmc.truesight.saas.jira.beans.Response;
import com.bmc.truesight.saas.jira.beans.Template;
import com.bmc.truesight.saas.jira.exception.JiraApiInstantiationFailedException;
import com.bmc.truesight.saas.jira.exception.JiraLoginFailedException;
import com.bmc.truesight.saas.jira.exception.JiraReadFailedException;
import com.bmc.truesight.saas.jira.exception.ParsingException;
import com.bmc.truesight.saas.jira.integration.adapter.JiraEntryEventAdapter;
import com.bmc.truesight.saas.jira.util.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Santosh Patil/vitiwari
 *
 */
public class JiraReader {

    private final static Logger log = LoggerFactory.getLogger(JiraReader.class);

    private Template template;
    private JiraAPI jiraAPI;

    public JiraReader(Template template) throws JiraApiInstantiationFailedException, JiraLoginFailedException {
        this.template = template;
        this.jiraAPI = new JiraAPI(template.getConfig());
    }

    public boolean validateCredentials() throws JiraLoginFailedException, JiraApiInstantiationFailedException {
        boolean isValid = jiraAPI.isValidCredentials();
        return isValid;
    }

    public JiraEventResponse readJiraTickets(int startFrom, int chunkSize, JiraEntryEventAdapter adapter) throws JiraReadFailedException, JiraApiInstantiationFailedException {
        Configuration config = template.getConfig();
        JiraEventResponse jiraResponse = null;
        String searchQuery = jiraAPI.buildJQLQuery(template.getFilter(), config.getStartDateTime(), config.getEndDateTime(), template.getJqlQuery());
        log.debug("SearchQuery formed as ->{}", searchQuery);
        String searchUrl = jiraAPI.getSearchUrl(chunkSize, startFrom, searchQuery, "");
        JsonNode response;
        try {
            response = jiraAPI.search(searchUrl);
        } catch (ParsingException e) {
            throw new JiraReadFailedException("Fetching the issues failed " + e.getMessage());
        }
        if (!response.isNull()) {
            JsonNode responseFiledsNode = response.get(Constants.JSON_ISSUES_NODE);
            if (!responseFiledsNode.isNull()) {
                jiraResponse = adapter.eventList(responseFiledsNode, template);
            }
            JsonNode totalCountNode = response.get(Constants.JSON_ISSUES_TOTAL_KEY);
            if (!totalCountNode.isNull()) {
                Integer count = totalCountNode.asInt();
                jiraResponse.setTotalCountAvailable(count);
            }

        }
        return jiraResponse;
    }

    public int getAvailableRecordsCount() throws JiraReadFailedException, ParseException, JiraApiInstantiationFailedException {
        Configuration config = template.getConfig();
        int recordsCount = 0;
        String searchQuery = jiraAPI.buildJQLQuery(template.getFilter(), config.getStartDateTime(), config.getEndDateTime(), template.getJqlQuery());
        log.debug("SearchQuery formed as ->{}", searchQuery);
        String finalSearchUrl = jiraAPI.getSearchUrl(0, 0, searchQuery, Constants.JIRA_NONE_FIELD);
        log.debug("finalSearchUrl formed as ->{}", finalSearchUrl);
        try {
            JsonNode responseNode = jiraAPI.search(finalSearchUrl);
            ObjectMapper mapper = new ObjectMapper();
            Response response = mapper.treeToValue(responseNode, Response.class);
            log.debug("Response as ->" + response.getTotal());
            recordsCount = response.getTotal();
            if (response.getErrorMessages() != null) {
                StringBuilder errorMessage = new StringBuilder();
                for (String error : response.getErrorMessages()) {
                    errorMessage.append(error).append("\n");
                }
                throw new JiraReadFailedException(errorMessage.toString());
            }
            if (response.getWarningMessages() != null) {
                StringBuilder errorMessage = new StringBuilder();
                for (String warn : response.getWarningMessages()) {
                    errorMessage.append(warn).append("\n");
                }
                throw new JiraReadFailedException(errorMessage.toString());
            }
        } catch (ParsingException e) {
            throw new JiraReadFailedException(e.getMessage());
        } catch (JsonProcessingException e) {
            throw new JiraReadFailedException(e.getMessage());
        }
        return recordsCount;
    }

}
