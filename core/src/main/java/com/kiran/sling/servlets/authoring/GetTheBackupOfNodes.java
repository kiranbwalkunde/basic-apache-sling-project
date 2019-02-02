package com.kiran.sling.servlets.authoring;

import com.kiran.sling.constants.ServletUrlConstants;
import com.kiran.sling.utils.ServletUtils;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import org.apache.jackrabbit.vault.fs.api.PathFilterSet;
import org.apache.jackrabbit.vault.fs.api.ProgressTrackerListener;
import org.apache.jackrabbit.vault.fs.config.DefaultWorkspaceFilter;

import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.JcrPackageDefinition;
import org.apache.jackrabbit.vault.packaging.Packaging;
import org.apache.jackrabbit.vault.packaging.PackageException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import org.osgi.service.component.ComponentContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;
import java.util.Date;

/**
 * The OSGI Servlet to get the JCR Nodes Backup in the ZIP format.
 *
 * @author Kiran. Created on 02nd Feb. 2019.
 */
@Component(label = "Get Backup of the JCR Nodes",
        description = "The OSGI Servlet to get the Backup of the Content Pages or other JCR Paths", metatype = true)
@Service(Servlet.class)
@Properties({
        @Property(name = "sling.servlet.paths", value = {ServletUrlConstants.GET_JCR_NODES_BACKUP}),
        @Property(name = "sling.servlet.methods", value = {HttpConstants.METHOD_GET}),
        // The Package Names to be scanned in the package directory for latest version check.
        @Property(name = "paths", value = {"/apps/kiran", "/content/shiva", "/conf/kiran"},
                unbounded = PropertyUnbounded.ARRAY)
    })
public class GetTheBackupOfNodes extends SlingSafeMethodsServlet {

    /** The Default Logger to log this Servlets Activity. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GetTheBackupOfNodes.class);

    /** The Group Name of the package with which to create the same. */
    private static final String PACKAGE_GROUP = "shiva";

    /** The Name of the Package. */
    private static final String PACKAGE_NAME = "shakti";

    /** The Packaging Dependency to get the Package Manager. */
    @Reference
    private Packaging packaging;

    /** The Set of Package Names to get the latest paths for. */
    private Set<String> setOfPaths = new HashSet<>();

    /**
     * The Activate Method to get the configured values for the package names.
     *
     * @param componentContext the component context to get the configured values.
     */
    @Activate
    public void activate(final ComponentContext componentContext) {
        final Dictionary properties = componentContext.getProperties();
        // Get the Array of the Configured package names to get the URLs for.
        final Object packages = properties.get("paths");
        // Check if the properties configured are not null and should be an instance of String Array.
        if (packages instanceof String[]) {
            // Evaluate the Set from the String array for easy future operations.
            final String[] propertiesArray = (String[]) packages;
            setOfPaths.addAll(Arrays.asList(propertiesArray));
        }
    }

    /**
     * The GET Method to call for the Registered Servlet URL and on HTTP GET Method.
     *
     * @param request the request object provided by the Sling Servlet Container.
     * @param response the Response Object to write the Response Into.
     * @throws ServletException The Servlet Exception if any by the Servlet Container.
     * @throws IOException the IO Exception if any while writing the Response.
     */
    @Override
    protected void doGet(final SlingHttpServletRequest request,
                         final SlingHttpServletResponse response)
            throws ServletException, IOException {
        final Session jcrSession = ServletUtils.getJcrSession(request);
        if (jcrSession != null) {
            final JcrPackageManager jcrPackageManager = packaging.getPackageManager(jcrSession);
            try {
                final JcrPackage jcrPackage = getOrCreateJcrPackage(jcrPackageManager);
                if (jcrPackage != null) {
                    final JcrPackageDefinition jcrPackageDefinition = jcrPackage.getDefinition();
                    jcrPackageDefinition.setFilter(getPackageFilters(), true);
                    jcrPackageManager.assemble(jcrPackage, getProgressListener());
                    LOGGER.debug("Package Assembling has been completed. ");
                    writeThePackageToResponse(response, jcrPackage);
                }
            } catch (final RepositoryException | PackageException repositoryException) {
                LOGGER.error("Unable to get / create the JCR Package. ", repositoryException);
                ServletUtils.writeErrorResponse(response,
                        500,
                        repositoryException.getMessage());
            }
        } else {
            ServletUtils.writeErrorResponse(response, 403, "Please check the access rights.");
        }
    }

    /**
     * The Method to get the Default Workspace Filter with Configured Filter Paths.
     *
     * @return filters the default workspace filter.
     */
    private DefaultWorkspaceFilter getPackageFilters() {
        final DefaultWorkspaceFilter filters = new DefaultWorkspaceFilter();
        setOfPaths.forEach(jcrPath -> {
            final PathFilterSet pathFilterSet = new PathFilterSet(jcrPath);
            pathFilterSet.seal();
            filters.add(pathFilterSet);
        });
        return filters;
    }

    /**
     * The Progress Track Listener for the Assembling Process.
     *
     * @return progressTrackListener.
     */
    private ProgressTrackerListener getProgressListener() {
        return new ProgressTrackerListener() {
            @Override
            public void onMessage(final Mode mode, final String action, final String path) {
                LOGGER.debug("Package Message [{}], [{}]", action, path);
            }

            @Override
            public void onError(final Mode mode, final String path, final Exception e) {
                LOGGER.error("Exception while assembling {}", path, e);
            }
        };
    }

    /**
     * The Method to get or Create the JCR Package with the Group and Package Name.
     *
     * @param jcrPackageManager the JCR Package Manager to get or create a package.
     * @return jcrPackage
     * @throws RepositoryException the Repository Exception if unable to create the package.
     * @throws IOException if unable to create the Package.
     */
    private JcrPackage getOrCreateJcrPackage(final JcrPackageManager jcrPackageManager)
            throws RepositoryException, IOException {

        final Node jcrNode = jcrPackageManager.getPackageRoot();
        JcrPackage jcrPackage = null;
        if (jcrNode != null) {
            final String expectedPackagePath = PACKAGE_GROUP + "/" + PACKAGE_NAME + ".zip";
            if (jcrNode.hasNode(expectedPackagePath)) {
                jcrPackage = jcrPackageManager.open(jcrNode.getNode(expectedPackagePath));
            } else {
                jcrPackage = jcrPackageManager.create(PACKAGE_GROUP, PACKAGE_NAME);
            }
        }
        return jcrPackage;
    }

    /**
     * Writes the Packaged File to the Response.
     *
     * @param response the Response object to write the JCR Package into.
     * @param jcrPackage the JCR package to get the Input Stream.
     * @throws IOException the IO Exception if any while reading the package or writing the response.
     * @throws RepositoryException if Repository Exception while reading JCR Package Info.
     */
    private void writeThePackageToResponse(final SlingHttpServletResponse response,
                                           final JcrPackage jcrPackage)
            throws IOException, RepositoryException {

        // References : https://www.codejava.net/java-ee/servlet/java-servlet-download-file-example
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                String.format("attachment; filename=%s", getFileName()));
        final OutputStream outputStream = response.getOutputStream();

        final InputStream inputStream = jcrPackage.getData().getBinary().getStream();

        byte[] buffer = new byte[4096];
        int bytesRead = -1;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        response.flushBuffer();
        inputStream.close();
        outputStream.close();
    }

    /**
     * Gets the File Name to send as the Response File Name.
     *
     * @return fileName
     */
    private String getFileName() {
        return PACKAGE_GROUP + "_" + PACKAGE_NAME + getSimpleDateFormat() + ".zip";
    }

    /**
     * Gets the String value in DD-MM-YYYY format.
     *
     * @return dateAsString
     */
    private String getSimpleDateFormat() {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("DD-MM-YYYY");
        return simpleDateFormat.format(new Date());
    }
}
