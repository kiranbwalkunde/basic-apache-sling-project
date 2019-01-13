package com.kiran.sling.jobs.creator;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * The My Components Job Creator to test how to create the Jobs in Apache Sling.
 *
 * Reference: https://sling.apache.org/documentation/bundles/apache-sling-eventing-and-job-handling.html
 *
 * @author shiva. Created on 12th Jan. 2019.
 */
@Component(immediate = true)
@Service({MyComponentsJobCreator.class})
public class MyComponentsJobCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyComponentsJobCreator.class);

    @Reference
    private JobManager jobManager;

    @Activate
    public void activate() {
        LOGGER.info("The Activate Method has been called for the Job Trigger Agent.");
        final Map<String, Object> props = new HashMap<>();
        props.put("item1", "shiva");
        props.put("count", 5);
        final Job job = jobManager.addJob("shakti-shiva", props);
        LOGGER.info("The Fired Job is {}", job);
    }
}
