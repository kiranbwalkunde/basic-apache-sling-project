package com.kiran.sling.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.kiran.sling.models.PackageInfoModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import javax.jcr.Session;
import java.io.IOException;
import java.util.Map;

/**
 * The Utils method to contain the Common Methods for the Servlets.
 *
 * @author Kiran. Created on 02nd Feb. 2019.
 */
public final class ServletUtils {

    /** Private Constructor to prevent instantiation. */
    private ServletUtils(){}

    /**
     * The Method to write the Error / Exception cases.
     *
     * @param response the Response Object to write into.
     * @param status the HTTP Status to send to the author.
     * @param message the message describing the issue.
     * @throws IOException the IO Exception if any.
     */
    public static void writeErrorResponse(final SlingHttpServletResponse response,
                                    final int status,
                                    final String message) throws IOException {
        response.sendError(status, message);
    }

    /**
     * The Method to write the Response of the evaluated packages.
     *
     * @param response the Response object to write the values into.
     * @param packagesMap the evaluated packages map.
     * @throws IOException the IO Exception if any.
     */
    public static void writeThePackageInfoToResponse(final SlingHttpServletResponse response,
                                               final Map<String, PackageInfoModel> packagesMap)
            throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try {
            final String jsonString = objectMapper.writeValueAsString(packagesMap);
            // Send the Response in JSON format.
            response.setContentType("application/json");
            // Write the Response to the Response writer.
            response.getWriter().write(jsonString);
        } catch (final JsonProcessingException jsonProcessingException) {
            writeErrorResponse(response, 500, "Unable to process the created map.");
        }
    }

    /**
     * The Method to get the Resource Resolver from the JCR Session.
     *
     * @param request the Request to get the Resource Resolver and later adapt to Session.
     * @return session the JCR Session.
     */
    public static Session getJcrSession(final SlingHttpServletRequest request) {
        return request.getResourceResolver().adaptTo(Session.class);
    }
}
