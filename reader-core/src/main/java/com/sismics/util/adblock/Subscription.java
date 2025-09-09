/*
 * This file is part of Adblock Plus <http://adblockplus.org/>,
 * Copyright (C) 2006-2013 Eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sismics.util.adblock;

public class Subscription {
    private String title;
    private String specialization;
    private String url;
    private String homepage;
    private String[] prefixes;
    private String author;

    // Getters
    public String getTitle() {
        return title;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getUrl() {
        return url;
    }

    public String getHomepage() {
        return homepage;
    }

    public String[] getPrefixes() {
        return prefixes;
    }

    public String getAuthor() {
        return author;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public void setPrefixes(String[] prefixes) {
        this.prefixes = prefixes;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
