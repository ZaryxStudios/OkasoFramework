package com.zaryxstudios.okaso.common.webhook;

public interface WebhookClient {
    void send(String url, String message);
    void sendEmbed(String url, WebhookEmbed embed);
    boolean isRateLimited();
}
