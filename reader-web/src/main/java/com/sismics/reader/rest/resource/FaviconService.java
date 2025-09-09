package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.FeedSubscriptionDao;
import com.sismics.reader.core.model.jpa.FeedSubscription;
import com.sismics.reader.core.util.DirectoryUtil;
import com.sismics.rest.exception.ClientException;

import java.io.File;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jettison.json.JSONException;

public class FaviconService {

    public File getFaviconFile(String subscriptionId, String userId) throws JSONException{
        FeedSubscriptionDao subscriptionDao = new FeedSubscriptionDao();
        FeedSubscription subscription = subscriptionDao.getFeedSubscription(subscriptionId, userId);
        if (subscription == null) {
            throw new ClientException("SubscriptionNotFound", MessageFormat.format("Subscription not found: {0}", subscriptionId));
        }
        File faviconDirectory = DirectoryUtil.getFaviconDirectory();
        File[] matchingFiles = faviconDirectory.listFiles((dir, name) -> name.startsWith(subscription.getFeedId()));
        if (matchingFiles != null && matchingFiles.length > 0) {
            return matchingFiles[0];
        } else {
            return new File(getClass().getResource("/image/subscription.png").getFile());
        }
    }

    public String getExpiresHeader() {
        long expiresTime = System.currentTimeMillis() + 3600000L * 24 * 7;
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
        return sdf.format(new Date(expiresTime));
    }

    public String getContentDispositionHeader(String fileName) {
        return MessageFormat.format("attachment; filename=\"{0}\"", fileName);
    }
}
