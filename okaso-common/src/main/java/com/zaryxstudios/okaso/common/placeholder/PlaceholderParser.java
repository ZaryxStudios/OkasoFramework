package com.zaryxstudios.okaso.common.placeholder;

import java.util.Optional;

public interface PlaceholderParser {
    String parse(String text, Object player);
    Optional<String> getPlaceholderValue(String placeholder, Object player);
    boolean hasPlaceholders(String text);
    String removePlaceholders(String text);
}
