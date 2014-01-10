package ru.runa.wf.web.servlet;

import ru.runa.wfe.var.FileVariable;

import com.google.common.base.Objects;

// TODO s:
/*
 * Options not implemented: acceptFileTypes, maxFileSize
 * 
 * Validation error breaks on next line
 * 
 * Remove buttons in EditListTag break on next lines
 * 
 * IE file removal does not remove it on server-side eventually
 */
public class UploadedFile {
    private String name;
    private String size;
    private String mimeType;
    private byte[] content;

    public UploadedFile() {
    }

    public UploadedFile(FileVariable value) {
        this.name = value.getName();
        this.mimeType = value.getContentType();
        setContent(value.getData());
    }

    public String getName() {
        return name;
    }

    public void setName(String fileName) {
        this.name = fileName;
    }

    public String getSize() {
        return size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String fileType) {
        this.mimeType = fileType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
        if (content.length > 1024 * 1024) {
            this.size = content.length / (1024 * 1024) + " Mb";
        } else {
            this.size = content.length / 1024 + " Kb";
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(getClass()).add("name", name).toString();
    }
}