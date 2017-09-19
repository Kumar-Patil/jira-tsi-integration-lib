package com.bmc.truesight.saas.jira.beans;

import java.util.List;

public class JiraEventResponse {

    private Integer totalCountAvailable;
    private List<TSIEvent> validEventList;
    private List<TSIEvent> invalidEventList;
    private List<String> invalidEventIdsList;

    public List<String> getInvalidEventIdsList() {
        return invalidEventIdsList;
    }

    public void setInvalidEventIdsList(List<String> invalidEventIdsList) {
        this.invalidEventIdsList = invalidEventIdsList;
    }

    public List<TSIEvent> getInvalidEventList() {
        return invalidEventList;
    }

    public void setInvalidEventList(List<TSIEvent> invalidEventList) {
        this.invalidEventList = invalidEventList;
    }

    public List<TSIEvent> getValidEventList() {
        return validEventList;
    }

    public void setValidEventList(List<TSIEvent> validEventList) {
        this.validEventList = validEventList;
    }

    public Integer getTotalCountAvailable() {
        return totalCountAvailable;
    }

    public void setTotalCountAvailable(Integer totalCountAvailable) {
        this.totalCountAvailable = totalCountAvailable;
    }

}
