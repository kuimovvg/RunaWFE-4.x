package ru.runa.wf.web.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;

public class FileUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Map<String, List<FileMeta>> map = Maps.newHashMap();

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doOptions(request, response);
        response.setHeader("Access-Control-Allow-Origin", "*");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Upload File Using Java Servlet API
        // files.addAll(MultipartRequestHandler.uploadByJavaServletAPI(request));

        // 1. Upload File Using Apache FileUpload
        List<FileMeta> files = Lists.newArrayList();
        String name = MultipartRequestHandler.uploadByApacheFileUpload(request, files);
        map.put(name, files);

        // 2. Set response type to json
        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", "*");

        // 3. Convert List<FileMeta> into JSON format
        JSONArray jsonArray = new JSONArray();
        for (FileMeta f : files) {
            JSONObject object = new JSONObject();
            object.put("fileName", f.getFileName());
            object.put("fileSize", f.getFileSize());
            object.put("name", name);
            jsonArray.add(object);
        }
        response.getOutputStream().write(jsonArray.toString().getBytes(Charsets.UTF_8));
        response.getOutputStream().flush();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        String action = request.getParameter("action");
        String name = request.getParameter("name");
        int index = Integer.parseInt(request.getParameter("index"));
        FileMeta fileMeta = map.get(name).get(index);
        if ("delete".equals(action)) {
            map.get(name).remove(fileMeta);
        }
        if ("view".equals(action)) {
            response.setContentType(fileMeta.getFileType());
            response.setHeader("Content-disposition", "attachment; filename=\"" + fileMeta.getFileName() + "\"");
            response.setHeader("Access-Control-Allow-Origin", "*");
            Closer closer = Closer.create();
            try {
                InputStream in = closer.register(fileMeta.getContent());
                OutputStream out = closer.register(response.getOutputStream());
                ByteStreams.copy(in, out);
            } catch (Throwable e) {
                throw closer.rethrow(e);
            } finally {
                closer.close();
            }
        }
    }
}