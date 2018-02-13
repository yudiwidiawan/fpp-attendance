package com.odoo.models;

import android.util.Log;

/**
 * Created by makan on 03/08/2017.
 */

public class TimesheetModel {
    private String date;
    private String desc;
    private String project;
    private String hours;
    private String task;
    private String uname;
    private int sheet_id;
    private int project_id;
    private int task_id;
    private int id;
    private int uid;

    public TimesheetModel() {

    }


    public TimesheetModel(int id, String date, String desc, String project, String hours, String task,int project_id
                          ,int task_id) {
        this.id = id;
        this.date = date;
        this.desc = desc;
        this.project = project;
        this.hours = hours;
        this.task = task;
        this.task_id = task_id;
        this.project_id = project_id;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getSheet_id() {
        return sheet_id;
    }

    public void setSheet_id(int sheet_id) {
        this.sheet_id = sheet_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public int getProject_id() {
        return project_id;
    }

    public String getTask() {
        return task;
    }

    public int getTask_id() {
        return task_id;
    }

    public void setProject_id(int project_id) {
        this.project_id = project_id;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    @Override
    public String toString() {
        return "id:"+id+",date:"+date+",desc:"+desc+",project_id:"+project_id+",project:"+project+
                ",task_id:"+task_id+",task:"+task+",sheet_id:"+sheet_id+",hours:"+hours+"";
    }
}
