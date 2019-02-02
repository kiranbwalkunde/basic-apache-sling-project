package com.kiran.sling.servlets.authoring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.kiran.sling.models.PackageInfoModel;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import org.apache.jackrabbit.vault.packaging.PackageId;
import org.apache.jackrabbit.vault.packaging.JcrPackageDefinition;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.Packaging;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import java.io.IOException;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The OSGI Servlet to get the Latest Installed Packages from the Sling Instance.
 *
 * @author Shiva. Created on 2nd Feb. 2019.
 */
@Component(label = "Get Latest Installed Packages",
        description = "The OSGI Servlet to get the paths of the installed packages", metatype = true)
@Service(Servlet.class)
@Properties({
        @Property(name = "sling.servlet.paths", value = {"/bin/services/sadashiv/private/packages"}),
        @Property(name = "sling.servlet.methods", value = {HttpConstants.METHOD_GET}),
        // The Package Names to be scanned in the package directory for latest version check.
        @Property(name = "packages", value = {"shiva", "shakti"}, unbounded = PropertyUnbounded.ARRAY)

    })
public class GetLatestInstalledPackages extends SlingSafeMethodsServlet {

    /** The Packaging Dependency to get the Package Manager. */
    @Reference
    private Packaging packaging;

    /** The Set of Package Names to get the latest paths for. */
    private Set<String> setOfPackages = new HashSet<>();

    /** The Default Logger to log the Servlet Events. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GetLatestInstalledPackages.class);

    /**
     * The Activate Method to get the configured values for the package names.
     *
     * @param componentContext the component context to get the configured values.
     */
    @Activate
    public void activate(final ComponentContext componentContext) {
        final Dictionary properties = componentContext.getProperties();
        // Get the Array of the Configured package names to get the URLs for.
        final Object packages = properties.get("packages");
        // Check if the properties configured are not null and should be an instance of String Array.
        if (packages instanceof String[]) {
            // Evaluate the Set from the String array for easy future operations.
            final String[] propertiesArray = (String[]) packages;
            setOfPackages.addAll(Arrays.asList(propertiesArray));
        }
    }

    /**
     * The Method to call on the GET Request of the Current Servlet.
     *
     * @param request the request object provided by the Sling Servlet Container.
     * @param response the Response Object to write the values into.
     * @throws ServletException the Servlet Exception if there is any issue / exception in Servlet Container.
     * @throws IOException the IO Exception if there is any Exception while writing the response.
     */
    @Override
    protected void doGet(final SlingHttpServletRequest request,
                         final SlingHttpServletResponse response)
            throws ServletException, IOException {
        // Get the JCR Session so as to get the JCR Nodes / packages as per the current logged in users permissions.
        final Session jcrSession = getJcrSession(request);
        if (jcrSession != null) {
            // Get the JCR Manager for the Package Level Activities.
            final JcrPackageManager packageManager = packaging.getPackageManager(jcrSession);
            try {
                // Get the JCR packages whose names has specified in the Configured Properties.
                final List<JcrPackage> packageList = getListOfPackagesFromConfiguration(packageManager);
                // Get the Map containing the basic details which has the latest version of the mentioned packages.
                final Map<String, PackageInfoModel> packagesMap = getMapOfLatestPackages(packageList);
                // Write the Map to the Response.
                writeThePackageInfoToResponse(response, packagesMap);
            } catch (final RepositoryException repositoryException) {
                LOGGER.error("Exception while reading the List of Packages ", repositoryException);
                writeErrorResponse(response, 500, "Exception while reading packages information.");
            }
        } else {
            writeErrorResponse(response, 403, "Please verify your permissions.");
        }
    }

    /**
     * The Method to get the Map of the Latest Installed packages on the Apache Sling.
     *
     * @param jcrPackages the list of JCR packages given by the JCR Package Manager.
     * @return modelMap the Map of the Latest Installed Packages.
     */
    private Map<String, PackageInfoModel> getMapOfLatestPackages(final List<JcrPackage> jcrPackages) {

        final Map<String, PackageInfoModel> modelMap = new HashMap<>();
        jcrPackages.forEach(item -> {
            final PackageInfoModel model = new PackageInfoModel();
            try {
                final JcrPackageDefinition packageDefinition = item.getDefinition();
                final PackageId packageId = packageDefinition.getId();
                final Calendar lastUnwrapped = packageDefinition.getLastUnwrapped();
                final String packageName = packageId.getName();
                final PackageInfoModel existingModel = modelMap.get(packageName);
                // Below condition should evaluate as per the Short Hand Evaluation.
                if (existingModel == null || existingModel.getLastInstalled().compareTo(lastUnwrapped) < 0) {
                    final String packagePath = packageId.getInstallationPath() + ".zip";
                    final String packageVersion = packageId.getVersionString();
                    model.setPackagePath(packagePath);
                    model.setPackageName(packageName);
                    model.setPackageVersion(packageVersion);
                    model.setLastInstalled(lastUnwrapped);
                    modelMap.put(packageName, model);
                }
            } catch (final RepositoryException repositoryException) {
                LOGGER.error("Unable to access package definition for package checking ",
                        repositoryException);
            }
        });
        return modelMap;
    }

    /**
     * The Method to get the List of Packages from the JCR.
     *
     * This method is responsible to return the list of packages only if it satisfies the Predicate Criteria.
     *
     * @param jcrPackageManager the JCR Package Manger to get the list of packages.
     * @return list of pacakges.
     * @throws RepositoryException the Repository Exception if unable to read the JCR packages.
     */
    private List<JcrPackage> getListOfPackagesFromConfiguration(final JcrPackageManager jcrPackageManager)
            throws RepositoryException {

        final Predicate<JcrPackage> predicate = (jcrPackage) -> {
            try {
                final String packageName = jcrPackage.getDefinition().getId().getName();
                return setOfPackages.contains(packageName);
            } catch (final RepositoryException repositoryException) {
                LOGGER.error("Exception while processing list of packages ",
                        repositoryException);
            }
            return false;
        };
        return jcrPackageManager
                .listPackages()
                .stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * The Method to get the Resource Resolver from the JCR Session.
     *
     * @param request the Request to get the Resource Resolver and later adapt to Session.
     * @return session the JCR Session.
     */
    private Session getJcrSession(final SlingHttpServletRequest request) {
        return request.getResourceResolver().adaptTo(Session.class);
    }

    /**
     * The Method to write the Error / Exception cases.
     *
     * @param response the Response Object to write into.
     * @param status the HTTP Status to send to the author.
     * @param message the message describing the issue.
     * @throws IOException the IO Exception if any.
     */
    private void writeErrorResponse(final SlingHttpServletResponse response,
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
    private void writeThePackageInfoToResponse(final SlingHttpServletResponse response,
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
            LOGGER.debug("The Response has been written {}", jsonString);
        } catch (final JsonProcessingException jsonProcessingException) {
            LOGGER.error("Unable to process the created map {}", packagesMap, jsonProcessingException);
            writeErrorResponse(response, 500, "Unable to process the created map.");
        }
    }
}
