package ru.runa.wf.web.servlet;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.common.base.Charsets;

public class MultipartRequestHandler {

    public static List<FileMeta> uploadByJavaServletAPI(HttpServletRequest request) throws IOException, ServletException {
        List<FileMeta> files = new LinkedList<FileMeta>();
        Collection<Part> parts = request.getParts();
        FileMeta temp = null;
        for (Part part : parts) {
            if (part.getContentType() != null) {
                temp = new FileMeta();
                temp.setFileName(getFilename(part));
                temp.setFileSize(part.getSize() / 1024 + " Kb");
                temp.setFileType(part.getContentType());
                temp.setContent(part.getInputStream());
                files.add(temp);
            }
        }
        return files;
    }

    public static String uploadByApacheFileUpload(HttpServletRequest request, List<FileMeta> files) throws IOException {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        FileMeta temp = null;
        String name = "";
        if (isMultipart) {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            try {
                List<FileItem> items = upload.parseRequest(request);
                for (FileItem item : items) {
                    if (item.isFormField()) {
                        if (item.getFieldName().equals("name")) {
                            name = item.getString(Charsets.UTF_8.name());
                        }
                    } else {
                        temp = new FileMeta();
                        temp.setFileName(item.getName());
                        temp.setContent(item.getInputStream());
                        temp.setFileType(item.getContentType());
                        temp.setFileSize(item.getSize() / 1024 + "Kb");
                        files.add(temp);
                    }
                }
            } catch (FileUploadException e) {
                throw new IOException(e);
            }
        }
        return name;
    }

    private static String getFilename(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                //     MSIE fix.
                return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); 
            }
        }
        return null;
    }
}