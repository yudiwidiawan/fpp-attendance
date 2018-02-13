package com.odoo.models;

/**
 * Created by makan on 20/09/2017.
 */

public class AttachmentModel {
    private int id;
    private String attachmentByte;
    private String name;

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

    public String getAttachmentByte() {
        return attachmentByte;
    }

    public void setAttachmentByte(String attachmentByte) {
        this.attachmentByte = attachmentByte;
    }
}
