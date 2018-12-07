package com.kiran.sling.components.clientlib;

import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.scripting.sightly.pojo.Use;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import java.util.Iterator;

/**
 * The Clientlibs Component to get the List of CSS and JS
 *  files in the Versioned Manner to avoid the issues with Browser Caching.
 *
 * @author Shiva. Created on 07th Dec. 2018.
 */
public class ClientlibsComponent implements Use {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(ClientlibsComponent.class);

    private static final String ARTIFACT_LOCATION = "/etc/clientlibs/sadashiv";

    /** The Resource Resolver Factory to get the Resolver. */
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private String fileType;

    private String fileName;

    /** The Resource Resovler to get the files. */
    private ResourceResolver resourceResolver;

    @Override
    public void init(Bindings bindings) {

        resourceResolver = (ResourceResolver) bindings.get("resolver");
        fileType = PropertiesUtil.toString(bindings.get("fileType"), "js");
        fileName = PropertiesUtil.toString(bindings.get("fileName"), "bundle");
    }

    public String getArtifactFile() {

        String filePath = null;
        final Resource clientLibs = resourceResolver.getResource(ARTIFACT_LOCATION);
        if (clientLibs != null) {
            final Iterator<Resource> resourceIterator = clientLibs.listChildren();
            while (resourceIterator.hasNext()) {
                final Resource childResource = resourceIterator.next();
                final String resourceName = childResource.getName();
                LOGGER.info("The File path is [{}]", resourceName);
                if (resourceName.contains(fileName) && resourceName.endsWith(fileType)) {
                    filePath = childResource.getPath();
                    break;
                }
            }
        } else {
            LOGGER.error("ClientLibs resource does not exists. [{}]", ARTIFACT_LOCATION);
        }
        return filePath;
    }
}
