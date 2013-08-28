package ru.runa.bp.handler;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import ru.runa.alfresco.AlfSession;
import ru.runa.alfresco.WSConnectionSettings;
import ru.runa.bp.AlfHandler;
import ru.runa.bp.AlfHandlerData;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.var.FileVariable;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;

/**
 * Handler which executes alfresco web script.
 * 
 * @author dofs
 */
public abstract class AlfExecuteWebScriptHandler extends AlfHandler {

    @Override
    protected void executeAction(AlfSession session, AlfHandlerData alfHandlerData) throws Exception {
        byte[] response = getResponse(session, alfHandlerData);
        log.debug(new String(response, Charsets.UTF_8.name()));
        handleResponse(alfHandlerData, response);
    }

    protected byte[] getResponse(AlfSession session, AlfHandlerData alfHandlerData) throws Exception {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getCredentialsProvider().setCredentials(new AuthScope(null, -1, null),
                new UsernamePasswordCredentials(WSConnectionSettings.getInstance().getLogin(), WSConnectionSettings.getInstance().getPassword()));
        String alfBaseUrl = WSConnectionSettings.getInstance().getAlfBaseUrl();
        HttpUriRequest request = null;
        if (useHttpPost()) {
            request = formHttpPostRequest(alfBaseUrl, alfHandlerData);
        } else {
            request = formHttpGetRequest(alfBaseUrl, alfHandlerData);
        }
        HttpResponse response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        log.debug("WebScript status code = " + statusCode);
        if (statusCode != 200 && throwExceptionOnErrorState()) {
            throw new InternalApplicationException("WebScript " + request.getRequestLine() + " status code is " + statusCode);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.getEntity().writeTo(baos);
        return baos.toByteArray();
    }

    protected HttpGet formHttpGetRequest(String alfBaseUrl, AlfHandlerData alfHandlerData) throws Exception {
        StringBuffer url = new StringBuffer();
        url.append(alfBaseUrl).append("service/").append(getWebScriptUri(alfHandlerData));
        Map<String, String> params = getWebScriptParameters(alfHandlerData);
        boolean first = true;
        for (String paramName : params.keySet()) {
            String paramValue = params.get(paramName);
            paramValue = URLEncoder.encode(paramValue, Charsets.UTF_8.name());
            if (first) {
                url.append("?");
            } else {
                url.append("&");
            }
            url.append(paramName).append("=").append(paramValue);
            first = false;
        }
        log.debug("Executing WebScript " + url);
        HttpGet request = new HttpGet(url.toString());
        return request;
    }

    protected HttpPost formHttpPostRequest(String alfBaseUrl, AlfHandlerData alfHandlerData) throws Exception {
        StringBuffer url = new StringBuffer();
        url.append(alfBaseUrl).append("service/").append(getWebScriptUri(alfHandlerData));
        Map<String, String> params = getWebScriptParameters(alfHandlerData);
        Map<String, FileVariable> fileParams = getWebScriptFileVariableParameters(alfHandlerData);
        HttpPost request = new HttpPost(url.toString());
        MultipartEntity entity = new MultipartEntity();

        for (String paramName : params.keySet()) {
            String paramValue = params.get(paramName);
            entity.addPart(paramName, new StringBody(paramValue, Charsets.UTF_8));
        }

        for (String paramName : fileParams.keySet()) {
            byte[] fileData = fileParams.get(paramName).getData();
            InputStreamBody body = new InputStreamBody(ByteStreams.newInputStreamSupplier(fileData).getInput(), fileParams.get(paramName)
                    .getContentType(), fileParams.get(paramName).getName());
            entity.addPart(paramName, body);
        }
        long length = entity.getContentLength();
        request.setEntity(entity);

        log.debug("Executing WebScript via post " + url + " content-length = " + length);
        return request;
    }

    protected String getWebScriptUri(AlfHandlerData alfHandlerData) {
        return alfHandlerData.getInputParam(String.class, "webScriptUri");
    }

    protected boolean throwExceptionOnErrorState() {
        return true;
    }

    protected boolean useHttpPost() {
        return false;
    }

    protected void handleResponse(AlfHandlerData alfHandlerData, byte[] response) {

    }

    protected abstract Map<String, String> getWebScriptParameters(AlfHandlerData alfHandlerData);

    protected Map<String, FileVariable> getWebScriptFileVariableParameters(AlfHandlerData alfHandlerData) {
        Map<String, FileVariable> map = new HashMap<String, FileVariable>();
        return map;
    }

}
