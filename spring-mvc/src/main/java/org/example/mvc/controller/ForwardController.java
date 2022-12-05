package org.example.mvc.controller;

import org.example.mvc.annotation.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class ForwardController {
    private String forwardUrl;
    public ForwardController(String forwardUrl) {
        this.forwardUrl = forwardUrl;
    }
    public String handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return forwardUrl;
    }
}