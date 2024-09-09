package org.apache.coyote.http11.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.apache.coyote.http11.Cookie;
import org.apache.coyote.http11.HttpHeaders;
import org.apache.coyote.http11.HttpMethod;

public class HttpRequest {

    private final RequestLine requestLine;
    private final HttpHeaders httpHeaders;
    private final Optional<RequestBody> requestBody;


    public HttpRequest(RequestLine requestLine, HttpHeaders httpHeaders, Optional<RequestBody> requestBody) {
        this.requestLine = requestLine;
        this.httpHeaders = httpHeaders;
        this.requestBody = requestBody;
    }

    public static HttpRequest from(BufferedReader bufferedReader) {
        try {
            RequestLine requestLine = new RequestLine(bufferedReader.readLine());
            HttpHeaders httpHeader = HttpHeaders.readRequestHeader(bufferedReader);
            Optional<RequestBody> body = initializeBody(bufferedReader, requestLine, httpHeader);
            return new HttpRequest(requestLine, httpHeader, body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static Optional<RequestBody> initializeBody(
            BufferedReader reader,
            RequestLine requestLine,
            HttpHeaders httpHeader
    ) {
        if (!(requestLine.getMethod() == HttpMethod.POST)) {
            return Optional.empty();
        }
        return Optional.of(RequestBody.read(reader, httpHeader.contentLength()));
    }

    public boolean isMethod(HttpMethod method) {
        return method == requestLine.getMethod();
    }

    public boolean isQueryStringRequest() {
        return requestLine.isQueryStringRequest();
    }

    public Map<String, String> getQueryParameters(){
        return requestLine.getParameters();
    }

    public boolean hasCookie(){
        return httpHeaders.hasCookie();
    }

    public Cookie getCookie(){
        if(!hasCookie()){
            throw  new IllegalStateException("쿠키가 존재하지 않습니다.");
        }
        return httpHeaders.getCookie();
    }
    public RequestLine getRequestLine() {
        return requestLine;
    }

    public String getRequestUri() {
        return requestLine.getRequestURI();
    }

    public RequestBody getRequestBody() {
        return requestBody.orElseThrow(() -> new IllegalStateException("requestBody is null"));
    }
}
