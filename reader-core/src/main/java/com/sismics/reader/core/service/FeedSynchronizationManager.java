package com.sismics.reader.core.service;

import com.sismics.reader.core.dao.jpa.FeedDao;
import com.sismics.reader.core.dao.jpa.FeedSynchronizationDao;
import com.sismics.reader.core.dao.jpa.criteria.FeedCriteria;
import com.sismics.reader.core.dao.jpa.dto.FeedDto;
// import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.reader.core.model.jpa.FeedSynchronization;
import com.sismics.reader.core.util.TransactionUtil;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class FeedSynchronizationManager {
    private static final Logger log = LoggerFactory.getLogger(FeedSynchronizationManager.class);
    // private FeedDao feedDao = new FeedDao();
    // private FeedSynchronizationDao feedSynchronizationDao = new FeedSynchronizationDao();
    private FeedSyncProcessor feedSyncProcessor = new FeedSyncProcessor();

    public void synchronizeAllFeeds() {
        // Retrieve all feeds with active subscriptions.
        FeedDao feedDao = new FeedDao();
        FeedCriteria feedCriteria = new FeedCriteria().setWithUserSubscription(true);
        List<FeedDto> feedList = feedDao.findByCriteria(feedCriteria);
        List<FeedSynchronization> syncList = new ArrayList<FeedSynchronization>();

        for (FeedDto feedDto : feedList) {
            FeedSynchronization sync = new FeedSynchronization();
            sync.setFeedId(feedDto.getId());
            sync.setSuccess(true);
            long startTime = System.currentTimeMillis();
            try {
                // Process a single feed
                feedSyncProcessor.synchronize(feedDto.getRssUrl(), "", false);
            } catch (Exception e) {
                log.error(MessageFormat.format("Error synchronizing feed at URL: {0}", feedDto.getRssUrl()), e);
                sync.setSuccess(false);
                sync.setMessage(ExceptionUtils.getStackTrace(e));
            }
            sync.setDuration((int) (System.currentTimeMillis() - startTime));
            syncList.add(sync);
            TransactionUtil.commit();
        }

        // Check whether the network is probably down.
        FeedSynchronizationDao feedSynchronizationDao = new FeedSynchronizationDao();
        boolean networkDown = true;
        for (FeedSynchronization sync : syncList) {
            if (sync.isSuccess()) {
                networkDown = false;
                break;
            }
        }

        // Update synchronization logs if at least one feed succeeded.
        if (!networkDown) {
            for (FeedSynchronization sync : syncList) {
                feedSynchronizationDao.create(sync);
                feedSynchronizationDao.deleteOldFeedSynchronization(sync.getFeedId(), 600);
            }
        }
    }

    public FeedSyncProcessor getFeedSyncProcessor() {
        return feedSyncProcessor;
    }
}
