package com.odoo.models;

import java.util.Date;

/**
 * Created by makan on 22/08/2017.
 */

public class ProjectModel {
    private String name;
    private int id;
    private int task_count;
    private int doc_count;
    private String lastUpdatedOn;
    private String recentLogDate;
    private String is_favorite;
    private boolean is_pinned = true;
    private int company_id;
    private String company;
    private int partner_id;
    private String customer;

    public ProjectModel() {
    }

    public ProjectModel(String name, int id, int task_count, int doc_count, String lastUpdatedOn, String recentLogDate, String is_favorite) {
        this.name = name;
        this.id = id;
        this.task_count = task_count;
        this.doc_count = doc_count;
        this.lastUpdatedOn = lastUpdatedOn;
        this.recentLogDate = recentLogDate;
        this.is_favorite = is_favorite;
    }

    public int getCompany_id() {
        return company_id;
    }

    public int getPartner_id() {
        return partner_id;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setCompany_id(int company_id) {
        this.company_id = company_id;
    }

    public String getCompany() {
        return company;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getCustomer() {
        return customer;
    }

    public void setPartner_id(int partner_id) {
        this.partner_id = partner_id;
    }

    public boolean is_pinned() {
        return is_pinned;
    }

    public void setIs_pinned(boolean is_pinned) {
        this.is_pinned = is_pinned;
    }

    public String getIs_favorite() {
        return is_favorite;
    }

    public void setIs_favorite(String is_favorite) {
        this.is_favorite = is_favorite;
    }

    public String getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    public void setLastUpdatedOn(String lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    public String getRecentLogDate() {
        return recentLogDate;
    }

    public void setRecentLogDate(String recentLogDate) {
        this.recentLogDate = recentLogDate;
    }

    public int getTask_count() {
        return task_count;
    }

    public void setTask_count(int task_count) {
        this.task_count = task_count;
    }

    public int getDoc_count() {
        return doc_count;
    }

    public void setDoc_count(int doc_count) {
        this.doc_count = doc_count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
