/**
 * The MIT License
 *
 * Copyright for portions of OpenUnirest/uniresr-java are held by Mashape (c) 2013 as part of Kong/unirest-java.
 * All other copyright for OpenUnirest/unirest-java are held by OpenUnirest (c) 2018.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package unirest;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.nio.entity.NByteArrayEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class RequestPrep {
    private static final String CONTENT_TYPE = "content-type";
    private static final String ACCEPT_ENCODING_HEADER = "accept-encoding";
    private static final String USER_AGENT_HEADER = "user-agent";
    private static final String USER_AGENT = "unirest-java/1.3.11";
    private static final UriFormatter URI_FORMATTER = new UriFormatter();

    static HttpRequestBase prepareRequest(HttpRequest request, boolean async) {

        if (!request.getHeaders().containsKey(USER_AGENT_HEADER)) {
            request.header(USER_AGENT_HEADER, USER_AGENT);
        }
        if (!request.getHeaders().containsKey(ACCEPT_ENCODING_HEADER)) {
            request.header(ACCEPT_ENCODING_HEADER, "gzip");
        }

        HttpRequestBase reqObj = getHttpRequest(request);
        setRequestHeaders(request, reqObj);
        setBody(request, async, reqObj);

        return reqObj;
    }

    private static void setBody(HttpRequest request, boolean async, HttpRequestBase reqObj) {
        if (!(request.getHttpMethod() == HttpMethod.GET || request.getHttpMethod() == HttpMethod.HEAD)) {
            if (request.getBody() != null) {
                HttpEntity entity = request.getBody().getEntity();
                if (async) {
                    if (reqObj.getHeaders(CONTENT_TYPE) == null || reqObj.getHeaders(CONTENT_TYPE).length == 0) {
                        reqObj.setHeader(entity.getContentType());
                    }
                    try {
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        entity.writeTo(output);
                        NByteArrayEntity en = new NByteArrayEntity(output.toByteArray());
                        ((HttpEntityEnclosingRequestBase) reqObj).setEntity(en);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    ((HttpEntityEnclosingRequestBase) reqObj).setEntity(entity);
                }
            }
        }
    }

    private static void setRequestHeaders(HttpRequest request, HttpRequestBase reqObj) {
        request.getHeaders().entries().forEach(reqObj::addHeader);
    }

    private static HttpRequestBase getHttpRequest(HttpRequest request) {
        HttpRequestBase reqObj = null;

        String urlToRequest = URI_FORMATTER.apply(request);

        switch (request.getHttpMethod()) {
            case GET:
                reqObj = new HttpGet(urlToRequest);
                break;
            case POST:
                reqObj = new HttpPost(urlToRequest);
                break;
            case PUT:
                reqObj = new HttpPut(urlToRequest);
                break;
            case DELETE:
                reqObj = new HttpDeleteWithBody(urlToRequest);
                break;
            case PATCH:
                reqObj = new HttpPatchWithBody(urlToRequest);
                break;
            case OPTIONS:
                reqObj = new HttpOptions(urlToRequest);
                break;
            case HEAD:
                reqObj = new HttpHead(urlToRequest);
                break;
        }
        return reqObj;
    }
}