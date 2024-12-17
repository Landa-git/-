package com.example.words;

import java.util.List;

/**
 * Класс для представления словаря.
 */
public class Dictionary {

    private final String name;
    private final List<DictionaryEntry> entries;

    public Dictionary(String name, List<DictionaryEntry> entries) {
        this.name = name;
        this.entries = entries;
    }

    public String getName() {
        return name;
    }

    public List<DictionaryEntry> getEntries() {
        return entries;
    }
}
