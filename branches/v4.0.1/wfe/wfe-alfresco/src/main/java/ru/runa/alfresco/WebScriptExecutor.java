package ru.runa.alfresco;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.var.FileVariable;

import com.google.common.io.ByteStreams;

/**
 * Executes web script.
 * 
 * @author Gritsenko_S
 */
public class WebScriptExecutor {
    private static final Log log = LogFactory.getLog(WebScriptExecutor.class);

    private boolean useHttpPost;
    private boolean throwExceptionOnErrorState = true;
    private final String webScriptUri;
    private final Map<String, String> webScriptParameters;
    private Map<String, FileVariable> webScriptFileParameters = new HashMap<String, FileVariable>();

    public WebScriptExecutor(String webScriptUri, Map<String, String> webScriptParameters) {
        this.webScriptUri = webScriptUri;
        this.webScriptParameters = webScriptParameters;
    }

    public void setWebScriptFileParameters(Map<String, FileVariable> webScriptFileParameters) {
        this.webScriptFileParameters = webScriptFileParameters;
    }

    public void setUseHttpPost(boolean useHttpPost) {
        this.useHttpPost = useHttpPost;
    }

    public void setThrowExceptionOnErrorState(boolean throwExceptionOnErrorState) {
        this.throwExceptionOnErrorState = throwExceptionOnErrorState;
    }

    public boolean isUseHttpPost() {
        return useHttpPost;
    }

    public boolean isThrowExceptionOnErrorState() {
        return throwExceptionOnErrorState;
    }

    public String getWebScriptUri() {
        return webScriptUri;
    }

    public Map<String, String> getWebScriptParameters() {
        return webScriptParameters;
    }

    public Map<String, FileVariable> getWebScriptFileParameters() {
        return webScriptFileParameters;
    }

    public ByteArrayOutputStream doRequest() throws Exception {
        final DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getCredentialsProvider().setCredentials(new AuthScope(null, -1, null),
                new UsernamePasswordCredentials(WSConnectionSettings.getSystemLogin(), WSConnectionSettings.getSystemPassword()));
        String alfBaseUrl = WSConnectionSettings.getAlfBaseUrl();
        final HttpUriRequest request;
        if (useHttpPost) {
            request = formHttpPostRequest(alfBaseUrl);
        } else {
            request = formHttpGetRequest(alfBaseUrl);
        }
        return new AlfSessionWrapper<ByteArrayOutputStream>() {

            @Override
            protected ByteArrayOutputStream code() throws Exception {
                HttpResponse response = httpClient.execute(request);
                int statusCode = response.getStatusLine().getStatusCode();
                log.debug("WebScript status code = " + statusCode);
                if (statusCode != 200 && throwExceptionOnErrorState) {
                    throw new InternalApplicationException("WebScript " + request.getRequestLine() + " status code is " + statusCode);
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                response.getEntity().writeTo(baos);
                return baos;
            }

        }.runInSession();
    }

    private HttpGet formHttpGetRequest(String alfBaseUrl) throws Exception {
        StringBuffer url = new StringBuffer();
        url.append(alfBaseUrl).append("service/").append(webScriptUri);
        boolean first = true;
        for (String paramName : webScriptParameters.keySet()) {
            String paramValue = webScriptParameters.get(paramName);
            paramValue = URLEncoder.encode(paramValue, "UTF-8");
            if (first) {
                url.append("?");
            } else {
                url.append("&");
            }
            url.append(paramName).append("=").append(paramValue);
            first = false;
        }
        log.info("Executing WebScript " + url);
        HttpGet request = new HttpGet(url.toString());
        return request;
    }

    private HttpPost formHttpPostRequest(String alfBaseUrl) throws Exception {
        StringBuffer url = new StringBuffer();
        url.append(alfBaseUrl).append("service/").append(webScriptUri);
        HttpPost request = new HttpPost(url.toString());
        MultipartEntity entity = new MultipartEntity();

        for (String paramName : webScriptParameters.keySet()) {
            String paramValue = webScriptParameters.get(paramName);
            entity.addPart(paramName, new StringBody(paramValue, Charset.forName("UTF-8")));
        }

        for (String paramName : webScriptFileParameters.keySet()) {
            byte[] fileData = webScriptFileParameters.get(paramName).getData();
            InputStreamBody body = new InputStreamBody(ByteStreams.newInputStreamSupplier(fileData).getInput(), webScriptFileParameters
                    .get(paramName).getContentType(), webScriptFileParameters.get(paramName).getName());
            entity.addPart(paramName, body);
        }
        long length = entity.getContentLength();
        request.setEntity(entity);

        log.info("Executing WebScript via post " + url + " content-length = " + length);
        return request;
    }

}
