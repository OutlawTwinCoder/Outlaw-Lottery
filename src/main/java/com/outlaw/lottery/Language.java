package com.outlaw.lottery;

public enum Language {
    EN,
    FR;

    public static Language fromString(String input) {
        if (input == null) {
            return null;
        }
        for (Language language : values()) {
            if (language.name().equalsIgnoreCase(input)) {
                return language;
            }
        }
        return null;
    }

    public static Language orDefault(String input, Language defaultLanguage) {
        Language language = fromString(input);
        return language != null ? language : defaultLanguage;
    }
}
