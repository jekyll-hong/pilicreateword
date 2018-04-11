package com.pilicreateworld.ocr.tencent;

import java.awt.image.BufferedImage;

import java.util.Base64;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

public final class TencentApi implements OcrService {
	private static TencentApi sInstance = null;

	public static TencentApi getInstance() {
		if (sInstance == null) {
			sInstance = new TencentApi();
		}

		return sInstance;
	}

	private static final String URL = "http://recognition.image.myqcloud.com/ocr/general";

	private static final String APP_ID = "1256449924";
	private static final String SECRET_ID = "AKIDiR5Xot4QxNdlolK9ZnVYVHcO8h5HZ7nK";
	private static final String SECRET_KEY = "W1OdLySnxxrdFft20rX2LSSGtKzRo00p";

	private static final String BUCKET_NAME = "pilicreateworld";
	private static final long EXPIRED_SEC = 12 * 3600;

	private static final String TIMEOUT_SEC = 5;

	private static final String IMAGE_TYPE = "png"
	private static final String IMAGE_MIME = "image/png";

	private OkHttpClient mHttpClient;
	private String mAuthorization;

	private TencentApi() {
        mHttpClient = createClient();
        mAuthorization = getAuthorization();
	}

	private static String getAuthorization() {
    	String signText = getSignText();
        byte[] signDigest = sign(signText, SECRET_KEY);

        byte[] temp = new byte[signDigest.length + signText.getBytes().length];
        System.arraycopy(signDigest, 0, temp, 0, signDigest.length);
        System.arraycopy(signText.getBytes(), 0, temp, signDigest.length, signText.getBytes().length);

        return Base64.getEncoder().encodeToString(temp);
    }

    private static String getSignText() {
    	long now = System.currentTimeMillis() / 1000;
        
        return String.format("a=%d&b=%s&k=%s&t=%d&e=%d&r=%d&u=%d", 
        	APP_ID, BUCKET_NAME, SECRET_ID, now, now + EXPIRED_SEC, 
        	Math.abs(new Random().nextInt()), 0);
    }

    private static byte[] sign(String str, String key) {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key.getBytes(), "HmacSHA1"));

        return mac.doFinal(str.getBytes());
    }

	public String process(BufferedImage image) {
		Request request = createRequest(image);

		Response response = mHttpClient.newCall(request).execute();
		if (response.isSuccessful()) {
			String json = response.body().string();

			return parseResult(json);
		}
		else {
			response.close();

			return "";
		}
	}

	private static OkHttpClient createClient() {
		OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS);
        builder.readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS);
        builder.writeTimeout(TIMEOUT_SEC, TimeUnit.SECONDS);
        
        return builder.build();
	}

    private static Request createPostRequest(BufferedImage image) {
        Request.Builder builder = new Request.Builder();

        builder.url(URL);
        builder.header("authorization", mAuthorization);
        builder.post(createMultipartBody(image));

        return builder.build();
    }

    private static MultipartBody createMultipartBody(BufferedImage image) {
        MultipartBody.Builder builder = new MultipartBody.Builder();

        builder.addFormDataPart("appid", APP_ID);
        builder.addFormDataPart("bucket", BUCKET_NAME);
        builder.addFormDataPart("image", "temp.png", createRequestBody(image)));

        return builder.build();
    }

    private static RequestBody createRequestBody(BufferedImage image) {
    	ByteArrayOutputStream output = new ByteArrayOutputStream();

    	try {
    		ImageIO.write(image, IMAGE_TYPE, output);
    	}
    	catch (IOException e) {
    		//ignore
    	}

    	return RequestBody.create(MediaType.parse(IMAGE_MIME), output.toByteArray());
    }

    private static String parseResult(String json) {
    	StringBuffer buffer = new StringBuffer();

        JsonObject root = new JsonParser().parse(json).getAsJsonObject();
        if (root.get("code").getAsInteger() == 0) {
            JsonObject data = root.get("data").getAsJsonObject();

            JsonArray itemArray = data.get("items").getAsJsonArray();
            for (int i = 0; i < itemArray.size(); i++) {
            	JsonObject item = itemArray.get(i).getAsJsonObject();

                String itemStr = item.get("itemstring").getAsString();
                buffer.append(itemStr);
            }
        }

        return buffer.toString();
    }
}
