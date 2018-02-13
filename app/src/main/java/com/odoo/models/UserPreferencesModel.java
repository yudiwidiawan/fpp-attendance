package com.odoo.models;

import java.util.List;

/**
 * Created by makan on 27/08/2017.
 */

public class UserPreferencesModel {

    private int empID;
    private String checkInTimeToday;
    private String checkOutTimeToday;
    private long totalWorkedToday;
    private List<ProjectModel> projectsPinned;
    private List<TaskModel> tasksPinned;

    public UserPreferencesModel() {
    }

    public int getEmpID() {
        return empID;
    }

    public void setEmpID(int empID) {
        this.empID = empID;
    }

    public String getCheckInTimeToday() {
        return checkInTimeToday;
    }

    public void setCheckInTimeToday(String checkInTimeToday) {
        this.checkInTimeToday = checkInTimeToday;
    }

    public String getCheckOutTimeToday() {
        return checkOutTimeToday;
    }

    public void setCheckOutTimeToday(String checkOutTimeToday) {
        this.checkOutTimeToday = checkOutTimeToday;
    }

    public long getTotalWorkedToday() {
        return totalWorkedToday;
    }

    public void setTotalWorkedToday(long totalWorkedToday) {
        this.totalWorkedToday = totalWorkedToday;
    }

    public List<ProjectModel> getProjectsPinned() {
        return projectsPinned;
    }

    public void setProjectsPinned(List<ProjectModel> projectsPinned) {
        this.projectsPinned = projectsPinned;
    }

    public List<TaskModel> getTasksPinned() {
        return tasksPinned;
    }

    public void setTasksPinned(List<TaskModel> tasksPinned) {
        this.tasksPinned = tasksPinned;
    }
}
