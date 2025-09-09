package com.sismics.reader.core.dao.file.rss;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.util.DateUtil;

public class HtmlToRssAdapter implements FeedParserAdapter {
    private final Feed feed;
    private List<Article> articles = new ArrayList<>();;
    private static final String NEWSAPI_KEY = "daa494d111554b3ca4d3081652d219dc"; // Replace with your API key

    public HtmlToRssAdapter(String url, String query) throws Exception {
        String domain = extractDomain(url);
        this.feed = new Feed();
        this.feed.setTitle("News from " + domain + " about " + query);
        this.feed.setRssUrl("https://" + domain + "&q=" + query);
        try {
            this.articles = fetchArticlesFromNewsAPI(domain, query);
        } catch (Exception e) {
            this.articles = new ArrayList<>();
            System.err.println("Error fetching articles from NewsAPI: " + e.getMessage());
        }
        validateFeed();
        fixGuid();
    }

    /**
     * Fetch website title using Jsoup
     */
    // private String getWebsiteTitle(String url) throws Exception {
    // Document doc = Jsoup.connect(url).get();
    // return doc.title();
    // }

    /**
     * Fetch articles using NewsAPI
     */
    private List<Article> fetchArticlesFromNewsAPI(String domain, String query) throws Exception {
        List<Article> articleList = new ArrayList<>();
        // String apiUrl = "https://newsapi.org/v2/everything?q=" + url + "&apiKey=" +
        // NEWSAPI_KEY;
        String apiUrl = String.format(
                "https://newsapi.org/v2/everything?q=%s&domains=%s&apiKey=%s",
                query, domain, NEWSAPI_KEY);

        String jsonResponse = sendGetRequest(apiUrl);
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray articlesArray = jsonObject.getJSONArray("articles");
        for (int i = 0; i < articlesArray.length(); i++) {
            JSONObject articleJson = articlesArray.getJSONObject(i);
            Article article = new Article();
            article.setTitle(articleJson.getString("title"));
            article.setUrl(articleJson.getString("url"));
            article.setDescription(articleJson.optString("description", "No description available"));
            // article.setPublicationDate(articleJson.optString("publishedAt", ""));
            String publishedAt = articleJson.optString("publishedAt", "");
            Date parsedDate = DateUtil.parseDate(publishedAt, DF);
            article.setPublicationDate(parsedDate);
            articleList.add(article);
        }

        return articleList;
    }

    /**
     * Send an HTTP GET request
     */
    private String sendGetRequest(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();
        return response.toString();
    }

    @Override
    public Feed getFeed() {
        return feed;
    }

    @Override
    public List<Article> getArticles() {
        return articles;
    }

    private void validateFeed() throws Exception {
        if (feed == null) {
            throw new Exception("No feed found");
        }
    }

    /**
     * Try to guess a value for GUID element values in RSS feeds.
     */
    private void fixGuid() {
        if (articles != null) {
            for (Article article : articles) {
                GuidFixer.fixGuid(article);
            }
        }
    }

    private String extractDomain(String url) throws MalformedURLException {
        URL parsedUrl = new URL(url);
        return parsedUrl.getHost(); // e.g., "bbc.com"
    }

    public static final DateTimeFormatter DF = new DateTimeFormatterBuilder()
            .append(null, new DateTimeParser[] {
                    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm zzz").getParser(),
                    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z").getParser(),
                    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss zzz").getParser(),
                    DateTimeFormat.forPattern("EEE,  d MMM yyyy HH:mm:ss zzz").getParser(),
                    DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss Z").getParser(),
                    DateTimeFormat.forPattern("yyyy-mm-dd HH:mm:ss").getParser(),
                    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss").getParser(),
                    DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss zzz").getParser(),
                    DateTimeFormat.forPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z Z").getParser(),
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").getParser(),
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").getParser(),
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").getParser(),
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").getParser(),
            }).toFormatter().withOffsetParsed().withLocale(Locale.ENGLISH);

}
