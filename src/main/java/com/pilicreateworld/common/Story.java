package com.pilicreateworld.common;

import com.pilicreateworld.Settings;
import com.pilicreateworld.image.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;

public class Story {
    private String mUrl;

    public Story(String url) {
        mUrl = url;
    }

    public Text getText() throws IOException {
        InputStream imageInputStream = null;

        do {
            URL httpUrl = new URL(mUrl);

            HttpURLConnection connection = (HttpURLConnection)httpUrl.openConnection(
                    Settings.getInstance().getProxy());
            connection.setConnectTimeout(5 * 1000);
            connection.setConnectTimeout(30 * 1000);
            connection.connect();

            switch (connection.getResponseCode()) {
                case HttpURLConnection.HTTP_MOVED_TEMP:
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_SEE_OTHER: {
                    /**
                     * 重定向
                     */
                    mUrl = connection.getHeaderField("Location");
                    break;
                }
                case HttpURLConnection.HTTP_OK: {
                    /**
                     * 成功
                     */
                    imageInputStream = connection.getInputStream();
                    break;
                }
                default: {
                    /**
                     * 失败
                     */
                    throw new IOException("resource access fail");
                }
            }
        }
        while (imageInputStream == null);

        return new Text(imageInputStream);
    }
}
