package com.odoo.models;

/**
 * Created by makan on 05/09/2017.
 */

public class AttendanceModel {
    private int id;
    private String check_in;
    private String check_out;
    private String hours;

    public AttendanceModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCheck_in() {
        return check_in;
    }

    public void setCheck_in(String check_in) {
        this.check_in = check_in;
    }

    public String getCheck_out() {
        return check_out;
    }

    public void setCheck_out(String check_out) {
        this.check_out = check_out;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getHours() {
        return hours;
    }
}
