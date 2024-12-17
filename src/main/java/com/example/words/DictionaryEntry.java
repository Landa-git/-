package com.example.words;
import java.util.List;

/**
 * Класс для представления записи в словаре.
 */
public class DictionaryEntry {

    private final String term;
    private final List<String> forms;

    public DictionaryEntry(String term, List<String> forms) {
        this.term = term;
        this.forms = forms;
    }

    public String getTerm() {
        return term;
    }

    public List<String> getForms() {
        return forms;
    }
}
