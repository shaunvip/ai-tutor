package com.aitutor.api.common;

import com.aitutor.api.config.AppConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiRequestLoggingFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.equals("/api") && !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        long startedAt = System.nanoTime();
        String method = wrappedRequest.getMethod();
        String path = wrappedRequest.getRequestURI();
        String remoteAddress = wrappedRequest.getRemoteAddr();

        log.info(
                "api request started method={} path={} remoteAddress={} contentType={} contentLength={}",
                method,
                path,
                remoteAddress,
                wrappedRequest.getContentType(),
                wrappedRequest.getContentLengthLong()
        );

        Exception failure = null;
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (IOException | ServletException | RuntimeException ex) {
            failure = ex;
            throw ex;
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            logBodies(path, wrappedRequest, wrappedResponse);
            if (failure == null) {
                log.info(
                        "api request completed method={} path={} status={} durationMs={}",
                        method,
                        path,
                        wrappedResponse.getStatus(),
                        durationMs
                );
            } else {
                log.warn(
                        "api request failed method={} path={} status={} durationMs={} errorType={}",
                        method,
                        path,
                        wrappedResponse.getStatus(),
                        durationMs,
                        failure.getClass().getSimpleName()
                );
            }
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logBodies(String path, ContentCachingRequestWrapper request,
                           ContentCachingResponseWrapper response) {
        if (isJson(request.getContentType())) {
            String body = cachedBody(request.getContentAsByteArray());
            if (!body.isBlank()) {
                log.info("api request body path={} body={}", path, sanitize(body));
            }
        }
        if (isJson(response.getContentType())) {
            String body = cachedBody(response.getContentAsByteArray());
            if (!body.isBlank()) {
                log.info("api response body path={} body={}", path, sanitize(body));
            }
        }
    }

    private boolean isJson(String contentType) {
        return contentType != null && contentType.toLowerCase(Locale.ROOT).contains("application/json");
    }

    private String cachedBody(byte[] bytes) {
        if (bytes.length == 0) {
            return "";
        }
        String body = new String(bytes, StandardCharsets.UTF_8).replaceAll("\\s+", " ").trim();
        if (body.length() <= AppConstants.API_REQUEST_LOG_MAX_BODY_CHARS) {
            return body;
        }
        return body.substring(0, AppConstants.API_REQUEST_LOG_MAX_BODY_CHARS) + "...";
    }

    private String sanitize(String body) {
        return body
                .replaceAll("(?i)\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"")
                .replaceAll("(?i)\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"***\"");
    }
}
