package com.kiran.sling.jobs.listener;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The My Component Job Consumer.
 *
 * @author shiva. Created on 12th Jan. 2019.
 */
@Component(immediate = true)
@Service({JobConsumer.class})
@Property(name=JobConsumer.PROPERTY_TOPICS, value = "shakti-shiva")
public class MyComponentJobConsumer implements JobConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyComponentJobConsumer.class);

    @Override
    public JobResult process(Job job) {
        LOGGER.info("The Job has been received {}", job.getQueueName());
        LOGGER.info("The Job has been received {}", job.getTopic());
        return JobResult.OK;
    }
}
