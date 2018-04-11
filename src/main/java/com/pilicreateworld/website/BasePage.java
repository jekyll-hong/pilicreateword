package com.pilicreateworld.website;

import com.pilicreateworld.Settings;

import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;

import java.io.IOException;

abstract class BasePage {
    private static final int TIMEOUT_MSECOND = 5000;
    
    private String mUrl;

    protected BasePage(String url) {
        mUrl = url;
    }

    protected Document load() throws IOException {
        Connection connection = HttpConnection.connect(mUrl);
        connection.timeout(TIMEOUT_MSECOND);
        connection.proxy(Settings.getInstance().getProxy());

        return connection.get();
    }
}
