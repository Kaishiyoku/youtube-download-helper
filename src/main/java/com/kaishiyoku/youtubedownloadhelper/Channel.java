package com.kaishiyoku.youtubedownloadhelper;

public class Channel {
    private String description;
    private String url;
    private String localPath;

    public Channel(String description, String url, String localPath) {
        this.description = description;
        this.url = url;
        this.localPath = localPath;
    }

    public Channel(String configLine) {
        String[] values = configLine.split(";");

        description = values[0];
        url = values[1];
        localPath = values[2];
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getLocalPath() {
        return localPath;
    }
}
