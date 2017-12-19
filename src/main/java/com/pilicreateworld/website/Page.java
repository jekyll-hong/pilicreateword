package com.pilicreateworld.website;

import com.pilicreateworld.Settings;
import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;

import java.io.IOException;

abstract class Page {
    protected Document mDocument;

    public Page(String url) throws IOException {
        mDocument = load(url);
    }

    private static Document load(String url) throws IOException {
        Connection connection = HttpConnection.connect(url);
        connection.proxy(Settings.getInstance().getProxy());

        return connection.get();
    }
}
