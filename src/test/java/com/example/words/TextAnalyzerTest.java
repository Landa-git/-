package com.example.words;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextAnalyzerTest {

    @Test
    void testAnalyzeSingleDictionarySingleWord() {
        // Создаем словарь с одним словом
        DictionaryEntry entry = new DictionaryEntry("слово", List.of("слово", "слова", "словом"));
        Dictionary dictionary = new Dictionary("Тестовый словарь", List.of(entry));

        // Создаем анализатор с этим словарем
        TextAnalyzer analyzer = new TextAnalyzer(List.of(dictionary));

        // Анализируем текст
        String text = "Слово было сказано. Это слово было важным.";
        AnalysisResult result = analyzer.analyze(text);

        // Проверяем результаты
        assertNotNull(result);
        assertEquals(2, result.getMatches("Тестовый словарь", "слово")); // Слово найдено 2 раза
        assertEquals(2, result.getTotalScore("Тестовый словарь")); // Общий балл = 2
    }

    @Test
    void testAnalyzeMultipleDictionaries() {
        // Создаем несколько словарей
        DictionaryEntry entry1 = new DictionaryEntry("мир", List.of("мир", "мира", "миром"));
        DictionaryEntry entry2 = new DictionaryEntry("война", List.of("война", "войны", "войной"));
        Dictionary dictionary1 = new Dictionary("Словарь 1", List.of(entry1));
        Dictionary dictionary2 = new Dictionary("Словарь 2", List.of(entry2));

        // Создаем анализатор с этими словарями
        TextAnalyzer analyzer = new TextAnalyzer(List.of(dictionary1, dictionary2));

        // Анализируем текст
        String text = "Мир и война. Миром восхищались, войной пугали.";
        AnalysisResult result = analyzer.analyze(text);

        // Проверяем результаты
        assertNotNull(result);
        assertEquals(2, result.getMatches("Словарь 1", "мир")); // "Мир" найдено 2 раза
        assertEquals(2, result.getMatches("Словарь 2", "война")); // "Война" найдена 2 раза
        assertEquals(2, result.getTotalScore("Словарь 1")); // Общий балл = 2
        assertEquals(2, result.getTotalScore("Словарь 2")); // Общий балл = 2
    }

    @Test
    void testAnalyzeTwoWordTerm() {
        // Создаем словарь с двухсловным термином
        DictionaryEntry entry = new DictionaryEntry("железный меч", List.of("железный меч", "железного меча"));
        Dictionary dictionary = new Dictionary("Словарь", List.of(entry));

        // Создаем анализатор
        TextAnalyzer analyzer = new TextAnalyzer(List.of(dictionary));

        // Анализируем текст
        String text = "На стене висел железный меч. Этот меч железный был острым.";
        AnalysisResult result = analyzer.analyze(text);

        // Проверяем результаты
        assertNotNull(result);
        assertEquals(2, result.getMatches("Словарь", "железный меч")); // Найдено 2 раза (оба порядка)
        assertEquals(2, result.getTotalScore("Словарь")); // Общий балл = 2
    }

    @Test
    void testCountTermMatchesSingleWord() {
        // Создаем запись словаря
        DictionaryEntry entry = new DictionaryEntry("слово", List.of("слово", "слова", "словом"));

        // Создаем анализатор без словарей (тестируем напрямую метод countTermMatches)
        TextAnalyzer analyzer = new TextAnalyzer(List.of());

        // Анализируем текст
        String text = "Слово было сказано, слова были важны.";
        int count = analyzer.countTermMatches(entry, text);

        // Проверяем результат
        assertEquals(2, count); // Найдено 2 совпадения
    }


    @Test
    void testCountExactWordMatches() {
        // Создаем анализатор без словарей (тестируем напрямую метод countExactWordMatches)
        TextAnalyzer analyzer = new TextAnalyzer(List.of());

        // Анализируем текст
        String text = "Мир и война. Миром восхищались, войной пугали.";
        int count = analyzer.countExactWordMatches("мир", text);

        // Проверяем результат
        assertEquals(1, count); // "Мир" найден 1 раз
    }
}