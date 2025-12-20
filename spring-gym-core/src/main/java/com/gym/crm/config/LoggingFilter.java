package com.gym.crm.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import jakarta.servlet.FilterConfig; 

import java.io.IOException;
import java.util.UUID;

public class LoggingFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String TRANSACTION_ID_KEY = "transactionId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String transactionId = UUID.randomUUID().toString();

        try {
            MDC.put(TRANSACTION_ID_KEY, transactionId);
            LOGGER.info(">>> Request Start: {} {} | Remote Addr: {}",
                    req.getMethod(), req.getRequestURI(), req.getRemoteAddr());
            chain.doFilter(request, response);
        } finally {
            LOGGER.info("<<< Response End: {} {} | Status: {}",
                    req.getMethod(), req.getRequestURI(), res.getStatus());
            MDC.remove(TRANSACTION_ID_KEY);
        }
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("LoggingFilter initialized.");
    }


    @Override
    public void destroy() {
        LOGGER.info("LoggingFilter destroyed.");
    }

}