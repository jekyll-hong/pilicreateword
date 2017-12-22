package com.pilicreateworld.website;

import com.pilicreateworld.Settings;
import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;

import java.io.IOException;

abstract class BasePage {
    private String mUrl;

    protected BasePage(String url) {
        mUrl = url;
    }

    protected Document load() throws IOException {
        Connection connection = HttpConnection.connect(mUrl);
        connection.timeout(5 * 1000);
        connection.proxy(Settings.getInstance().getProxy());

        return connection.get();
    }
}
