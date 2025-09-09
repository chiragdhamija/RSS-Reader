package com.sismics.reader.core.dao.file.rss;
// package com.sismics.reader.core.dao.file.rss;

// import org.joda.time.format.DateTimeFormatter;
// import org.joda.time.format.DateTimeFormatterBuilder;
// import org.joda.time.format.DateTimeFormat;
// import org.joda.time.format.DateTimeParser;
// import java.util.Locale;

// public final class DateFormats {

//     public static final DateTimeFormatter DF_RSS = new DateTimeFormatterBuilder()
//             .append(null, new DateTimeParser[] {
//                     DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm zzz").getParser(),
//                     DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z").getParser(),
//                     DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss zzz").getParser(),
//                     DateTimeFormat.forPattern("EEE,  d MMM yyyy HH:mm:ss zzz").getParser(),
//                     DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss Z").getParser(),
//                     DateTimeFormat.forPattern("yyyy-mm-dd HH:mm:ss").getParser(),
//                     DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss").getParser(),
//                     DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss zzz").getParser(),
//                     DateTimeFormat.forPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z Z").getParser()
//             }).toFormatter().withOffsetParsed().withLocale(Locale.ENGLISH);

//     public static final DateTimeFormatter DC_DATE_FORMATS = new DateTimeFormatterBuilder()
//             .append(null, new DateTimeParser[] {
//                     DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm zzz").getParser(),
//                     DateTimeFormat.forPattern("EEE,  d MMM yyyy HH:mm:ss zzz").getParser(),
//                     DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss Z").getParser(),
//                     DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss").getParser(),
//                     DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss zzz").getParser(),
//                     DateTimeFormat.forPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z Z").getParser()
//             }).toFormatter().withOffsetParsed().withLocale(Locale.ENGLISH);

//     private DateFormats() {
//     } // Prevent instantiation
// }
