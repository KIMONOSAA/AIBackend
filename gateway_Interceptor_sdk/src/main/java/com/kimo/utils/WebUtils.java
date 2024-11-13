package com.kimo.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class WebUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void writeJson(HttpServletResponse response, Object object) {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            String jsonResponse = objectMapper.writeValueAsString(object);
            writer.write(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
