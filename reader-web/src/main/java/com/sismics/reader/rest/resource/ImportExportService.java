package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.FeedSubscriptionDao;
import com.sismics.reader.core.dao.jpa.UserDao;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.event.SubscriptionImportedEvent;
import com.sismics.reader.core.model.jpa.User;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.rest.exception.ServerException;
import com.sismics.util.MessageUtil;
import com.sun.jersey.multipart.FormDataBodyPart;
import org.apache.commons.io.IOUtils;
import javax.xml.transform.dom.DOMSource;
import org.codehaus.jettison.json.JSONException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.List;

public class ImportExportService {
    // private final String userId;
    // private Locale locale;
    // private String name;

    // public ImportExportService(String userId, Locale locale, String name) {
    //     this.userId = userId;
    //     this.locale = locale;
    //     this.name = name;
    // }

    public void importSubscriptions(String userId, FormDataBodyPart fileBodyPart) throws JSONException {
        ValidationUtil.validateRequired(fileBodyPart, "file");
        UserDao userDao = new UserDao();
        User user = userDao.getById(userId);
        InputStream in = fileBodyPart.getValueAs(InputStream.class);
        File importFile = null;
        try {
            importFile = File.createTempFile("reader_opml_import", null);
            IOUtils.copy(in, new FileOutputStream(importFile));
            SubscriptionImportedEvent event = new SubscriptionImportedEvent();
            event.setUser(user);
            event.setImportFile(importFile);
            AppContext.getInstance().getImportEventBus().post(event);
        } catch (Exception e) {
            if (importFile != null && importFile.exists()) {
                try{
                    importFile.delete();
                } catch (SecurityException e2) {
                    // Ignore
                }
            }
            throw new ServerException("ImportError", "Error importing OPML file", e);
        }
    }

    public DOMSource exportSubscriptions(String userId, Locale locale, String name) throws JSONException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ServerException("UnknownError", "Error building export file", e);
        }
        DOMImplementation impl = builder.getDOMImplementation();
        Document opmlDocument = impl.createDocument(null, null, null);
        opmlDocument.setXmlStandalone(true);
        Element opmlElement = opmlDocument.createElement("opml");
        opmlElement.setAttribute("version", "1.0");
        opmlDocument.appendChild(opmlElement);

        // Create head element
        Element headElement = opmlDocument.createElement("head");
        opmlElement.appendChild(headElement);
        Element titleElement = opmlDocument.createElement("title");
        // In a real app, pass the locale and actual user name
        titleElement.setTextContent(MessageUtil.getMessage(locale, "reader.export.title", name));
        headElement.appendChild(titleElement);

        // Create body element
        Element bodyElement = opmlDocument.createElement("body");
        opmlElement.appendChild(bodyElement);

        // Retrieve subscriptions for this user
        FeedSubscriptionDao subscriptionDao = new FeedSubscriptionDao();
        List<FeedSubscriptionDto> subscriptions = subscriptionDao.findByCriteria(new com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria().setUserId(userId));

        String oldCategoryId = null;
        Element currentCategoryElement = bodyElement;
        for (FeedSubscriptionDto obj : subscriptions) {
            FeedSubscriptionDto sub = (FeedSubscriptionDto) obj;
            String categoryId = sub.getCategoryId();
            if (!categoryId.equals(oldCategoryId)) {
                if (sub.getCategoryParentId() != null) {
                    currentCategoryElement = opmlDocument.createElement("outline");
                    currentCategoryElement.setAttribute("title", sub.getCategoryName());
                    currentCategoryElement.setAttribute("text", sub.getCategoryName());
                    bodyElement.appendChild(currentCategoryElement);
                } else {
                    currentCategoryElement = bodyElement;
                }
            }
            Element subElement = opmlDocument.createElement("outline");
            subElement.setAttribute("type", "rss");
            subElement.setAttribute("title", sub.getFeedSubscriptionTitle());
            subElement.setAttribute("text", sub.getFeedSubscriptionTitle());
            subElement.setAttribute("xmlUrl", sub.getFeedRssUrl());
            subElement.setAttribute("htmlUrl", sub.getFeedUrl());
            currentCategoryElement.appendChild(subElement);
            oldCategoryId = categoryId;
        }
        return new DOMSource(opmlDocument);
    }
}
