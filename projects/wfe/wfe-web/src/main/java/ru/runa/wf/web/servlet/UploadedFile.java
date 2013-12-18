package ru.runa.wf.web.servlet;

import java.io.InputStream;

import com.google.common.base.Objects;

public class UploadedFile {
    private String name;
    private String size;
    private String mimeType;

    private byte[] content;

    public String getName() {
        return name;
    }

    public void setName(String fileName) {
        this.name = fileName;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String fileSize) {
        this.size = fileSize;
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
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(getClass()).add("name", name).toString();
    }
}