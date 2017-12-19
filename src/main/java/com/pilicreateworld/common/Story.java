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
            connection.connect();

            int statusCode = connection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || statusCode == HttpURLConnection.HTTP_MOVED_PERM
                    || statusCode == HttpURLConnection.HTTP_SEE_OTHER) {
                /**
                 * 重定向
                 */
                mUrl = connection.getHeaderField("Location");
            }
            else if (statusCode == HttpURLConnection.HTTP_OK) {
                /**
                 * 成功
                 */
                imageInputStream = connection.getInputStream();
            }
            else {
                /**
                 * 失败
                 */
                throw new IOException("http access fail");
            }
        }
        while (imageInputStream == null);

        return new Text(imageInputStream);
    }
}
