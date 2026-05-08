package com.cpt202.projectselection.controller;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class GlobalExceptionHandler implements ErrorController {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Throwable exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        HttpStatus status = statusCode == null ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.resolve(statusCode);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (exception != null) {
            log.warn("Request failed with status {} on {}", status.value(), request.getRequestURI(), exception);
        }
        populateErrorModel(model, status, request.getRequestURI());
        return "error/error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request, Model model) {
        log.warn("Bad request on {}: {}", request.getRequestURI(), ex.getMessage());
        populateErrorModel(model, HttpStatus.BAD_REQUEST, request.getRequestURI());
        model.addAttribute("message", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex, HttpServletRequest request, Model model) {
        log.error("Unexpected error on {}", request.getRequestURI(), ex);
        populateErrorModel(model, HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURI());
        return "error/error";
    }

    private void populateErrorModel(Model model, HttpStatus status, String path) {
        model.addAttribute("status", status.value());
        model.addAttribute("title", titleFor(status));
        model.addAttribute("message", messageFor(status));
        model.addAttribute("path", path);
    }

    private String titleFor(HttpStatus status) {
        if (status == HttpStatus.NOT_FOUND) {
            return "Page not found";
        }
        if (status == HttpStatus.FORBIDDEN) {
            return "Access denied";
        }
        if (status == HttpStatus.BAD_REQUEST) {
            return "Request cannot be processed";
        }
        return "Something went wrong";
    }

    private String messageFor(HttpStatus status) {
        if (status == HttpStatus.NOT_FOUND) {
            return "The page you are looking for does not exist or has been moved.";
        }
        if (status == HttpStatus.FORBIDDEN) {
            return "Your current account does not have permission to access this page.";
        }
        if (status == HttpStatus.BAD_REQUEST) {
            return "Please check the request and try again.";
        }
        return "The system encountered an unexpected problem. Please return to the dashboard and try again later.";
    }
}
