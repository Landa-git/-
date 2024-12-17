package com.example.words;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс для анализа текста с использованием словарей.
 */
public class TextAnalyzer {
    private static final Logger logger = LogManager.getLogger(TextAnalyzer.class);

    private final List<Dictionary> dictionaries;

    public TextAnalyzer(List<Dictionary> dictionaries) {
        this.dictionaries = dictionaries;
    }

    /**
     * Выполняет анализ текста и возвращает результат.
     *
     * @param text текст для анализа.
     * @return результат анализа.
     */
    public AnalysisResult analyze(String text) {
        AnalysisResult result = new AnalysisResult();
        logger.info("Начало анализа текста");

        // Приведение текста к нижнему регистру для регистрационной независимости
        text = text.toLowerCase();

        for (Dictionary dictionary : dictionaries) {
            logger.info("Анализ текста с использованием словаря: {}", dictionary.getName());
            int matches = 0;
            for (DictionaryEntry entry : dictionary.getEntries()) {
                int count = countTermMatches(entry, text);
                matches += count;
                result.addMatch(dictionary.getName(), entry.getTerm(), count);
                if (count > 0) {
                    logger.debug("Найдено совпадений для термина '{}': {}", entry.getTerm(), count);
                }
            }
            result.addScore(dictionary.getName(), matches);
            logger.info("Словарь '{}' завершён. Общий счёт: {}", dictionary.getName(), matches);
        }
        logger.info("Анализ текста завершён");
        return result;
    }

    /**
     * Считает количество совпадений для термина и всех его форм в тексте.
     *
     * @param entry запись из словаря.
     * @param text текст для анализа.
     * @return количество совпадений.
     */
    int countTermMatches(DictionaryEntry entry, String text) {

        int totalCount = 0;

        // Проверяем каждую форму термина
        for (String form : entry.getForms()) {
            form=form.toLowerCase();
            if (form.length() < 3) {
                logger.debug("Пропущена форма термина из-за короткой длины: '{}'", form);
                continue; // Пропускаем формы короче 3 символов
            }

            // Проверяем количество совпадений для исходной формы
            totalCount += countExactWordMatches(form, text);


            // Если форма состоит из двух слов, проверяем также перестановку
            String[] words = form.split("\\s+");
            if (words.length == 2) {
                String reversedForm = words[1] + " " + words[0];
                int reversedCount = countExactWordMatches(reversedForm, text);
                totalCount += reversedCount;
                if (reversedCount > 0) {
                    logger.debug("Найдено совпадений для перестановки термина '{}': {}", reversedForm, reversedCount);
                }
            }
        }
        logger.debug("Общее количество совпадений для термина '{}': {}", entry.getTerm(), totalCount);

        return totalCount;
    }

    /**
     * Считает количество точных совпадений слова или фразы в тексте.
     *
     * @param term слово или фраза.
     * @param text текст для анализа.
     * @return количество совпадений.
     */
    int countExactWordMatches(String term, String text) {
        // Регулярное выражение для точного поиска слова
        String regex = "(?<!\\p{L})" + Pattern.quote(term) + "(?!\\p{L})";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(text);

        int count = 0;
        while (matcher.find()) {
            count++;
        }
        if (count > 0) {
            logger.debug("Найдено совпадений для термина '{}': {}", term, count);
        }
        return count;
    }
}

