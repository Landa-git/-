package com.example.words;

import javafx.application.Application;

/**
 * Лаунчер для запуска приложения. Нужен чтобы передать объект TextAnalyzer
 */
public class TextAnalysisAppLauncher {
    private static TextAnalyzer textAnalyzer;

    public static void launch(TextAnalyzer textAn) {
        textAnalyzer=textAn;
        Application.launch(TextAnalysisApp.class);
    }

    public static TextAnalyzer getTextAnalyzer() {
        return textAnalyzer;
    }
}
