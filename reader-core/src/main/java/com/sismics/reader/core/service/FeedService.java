package com.sismics.reader.core.service;

// import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.reader.core.util.TransactionUtil;
import com.google.common.util.concurrent.AbstractScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class FeedService extends AbstractScheduledService {
    private static final Logger log = LoggerFactory.getLogger(FeedService.class);
    private FeedSynchronizationManager syncManager = new FeedSynchronizationManager();

    @Override
    protected void startUp() throws Exception {
        // Initialization if needed
    }

    @Override
    protected void shutDown() throws Exception {
        // Cleanup if needed
    }

    @Override
    protected void runOneIteration() {
        try {
            TransactionUtil.handle(() -> 
                syncManager.synchronizeAllFeeds()
            );
        } catch (Throwable t) {
            log.error("Error synchronizing feeds", t);
        }
    }

    @Override
    protected Scheduler scheduler() {
        // Scheduling every 10 minutes (improve as needed)
        return Scheduler.newFixedDelaySchedule(0, 10, TimeUnit.MINUTES);
    }

    public FeedSynchronizationManager getSyncManager() {
        return syncManager;
    }
}
