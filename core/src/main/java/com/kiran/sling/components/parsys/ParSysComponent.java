package com.kiran.sling.components.parsys;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This is the Adaptables Component to render the
 *  Response from the Child Components.
 *
 * @author Kiran. Created on 30th Nov. 2018.
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class ParSysComponent {

    @Self
    private SlingHttpServletRequest request;

    public List<String> getModel() {
        final Resource currentResource = request.getResource();
        final Iterator<Resource> childResources = currentResource.listChildren();
        final List<String> resourcePaths = new ArrayList<String>();
        while (childResources.hasNext()) {
            final Resource childResource = childResources.next();
            resourcePaths.add(childResource.getPath());
        }
        return resourcePaths;
    }
}
