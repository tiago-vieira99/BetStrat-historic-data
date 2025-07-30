package com.api.BetStrat.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class TelegramBotNotifications {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotNotifications.class);
    // one instance, reuse
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    public static void sendToTelegram(String message) throws UnsupportedEncodingException {
        String urlString = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";

        //Add Telegram token
        String apiToken = "6089369159:AAH-zuVZwWQVVI0UMJyMGenV8bG0pBdA1T4";

        //Add chatId
        String chatId = "-1001789104446";

        HttpPost httppost = new HttpPost("https://api.telegram.org/bot6089369159:AAH-zuVZwWQVVI0UMJyMGenV8bG0pBdA1T4/sendMessage");
        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair("chat_id", "-1001789104446"));
        params.add(new BasicNameValuePair("text", message));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        message = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());

        LOGGER.info("Sending message to Telegram: \n\r" + message);
        //Execute and get the response.
        try {
            CloseableHttpResponse response = httpClient.execute(httppost);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                try (InputStream instream = entity.getContent()) {
                    // do something useful
                }
            }
            response.close();
        } catch (Exception e) {
            LOGGER.error("Error sending message to Telegram: \n\r" + e.getMessage());
            e.printStackTrace();
        }
    }
}
