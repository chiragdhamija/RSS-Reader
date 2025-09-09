package com.sismics.reader.core.service;

import com.sismics.reader.core.event.FaviconUpdateRequestedEvent;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.model.jpa.Feed;
import org.joda.time.DateTime;
import org.joda.time.Days;
// import org.joda.time.DurationFieldType;
import org.joda.time.Instant;

// import java.util.Date;

public class FaviconManager {
    /**
     * Determines if the favicon should be updated (e.g. once a week).
     */
    public boolean shouldUpdateFavicon(Feed feed) {
        boolean newDay = feed.getLastFetchDate() == null ||
                DateTime.now().getDayOfYear() != new DateTime(feed.getLastFetchDate()).getDayOfYear();
        int daysFromCreation = Days.daysBetween(new Instant(feed.getCreateDate().getTime()), Instant.now()).getDays();
        return newDay && daysFromCreation % 7 == 0;
    }

    /**
     * Posts an event requesting the feed’s favicon to be updated.
     */
    public void requestFaviconUpdate(Feed feed) {
        FaviconUpdateRequestedEvent event = new FaviconUpdateRequestedEvent();
        event.setFeed(feed);
        AppContext.getInstance().getAsyncEventBus().post(event);
    }
}
