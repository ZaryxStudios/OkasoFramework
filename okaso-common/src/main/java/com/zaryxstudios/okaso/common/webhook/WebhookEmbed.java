package com.zaryxstudios.okaso.common.webhook;

import java.util.List;

public interface WebhookEmbed {
    String getTitle();
    String getDescription();
    int getColor();
    List<String> getFields();
}
