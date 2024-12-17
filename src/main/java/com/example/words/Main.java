package com.example.words;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Точка входа в приложение.
 */
public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        logger.info("Запуск приложения");

        try {
            // Создание необходимых объектов
            DictionaryLoader dictionaryLoader = new DictionaryLoader("dicts/dict_config.txt");
            List<Dictionary> dictionaries = dictionaryLoader.loadDictionaries();

            TextAnalyzer textAnalyzer = new TextAnalyzer(dictionaries);
            TextAnalysisAppLauncher.launch(textAnalyzer);

        } catch (Exception e) {
            logger.error("Ошибка при запуске приложения", e);
        }
    }
}