package com.sismics.reader.core.dao.file.rss;

import java.io.InputStream;

public class FeedPаrserFactory {
    public static BaseParser getParser(InputStream is){
        return new BaseParser();
    }
}
