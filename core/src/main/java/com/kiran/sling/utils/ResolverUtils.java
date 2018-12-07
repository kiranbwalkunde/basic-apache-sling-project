package com.kiran.sling.utils;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * The Resolver Utils to get / create and cleanup the Resource Resolver.
 *
 * @author Shiva. Created on 07th Dec. 2018.
 */
public final class ResolverUtils {

    private ResolverUtils(){}

    public static ResourceResolver getServiceResourceResolver(
            final ResourceResolverFactory resourceResolverFactory,
              final String subService) throws LoginException {
        final Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put(ResourceResolverFactory.SUBSERVICE, subService);

        resourceResolverFactory.getServiceResourceResolver(stringObjectMap);
        return null;
    }

    public static void cleanupResolver(final ResourceResolver resourceResolver) {

        if (resourceResolver != null && resourceResolver.isLive()) {
            resourceResolver.close();
        }
    }
}
