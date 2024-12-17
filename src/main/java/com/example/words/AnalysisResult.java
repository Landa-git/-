package com.example.words;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс для хранения результатов анализа.
 */
public class AnalysisResult {

    private final Map<String, Integer> scores = new HashMap<>();
    private final Map<String, Map<String, Integer>> matches = new HashMap<>();

    /**
     * Добавляет общий счёт для словаря.
     *
     * @param dictionaryName название словаря.
     * @param score общий счёт.
     */
    public void addScore(String dictionaryName, int score) {
        scores.put(dictionaryName, score);
    }

    /**
     * Добавляет совпадение для термина из словаря.
     *
     * @param dictionaryName название словаря.
     * @param term термин.
     * @param count количество совпадений.
     */
    public void addMatch(String dictionaryName, String term, int count) {
        matches.computeIfAbsent(dictionaryName, k -> new HashMap<>()).put(term, count);
    }

    /**
     * Возвращает общий счёт для указанного словаря.
     *
     * @param dictionaryName название словаря.
     * @return общий счёт.
     */
    public int getTotalScore(String dictionaryName) {
        return scores.getOrDefault(dictionaryName, 0);
    }

    /**
     * Возвращает количество совпадений для термина в указанном словаре.
     *
     * @param dictionaryName название словаря.
     * @param term термин.
     * @return количество совпадений.
     */
    public int getMatches(String dictionaryName, String term) {
        return matches.getOrDefault(dictionaryName, new HashMap<>()).getOrDefault(term, 0);
    }

    /**
     * Возвращает сводку по результатам анализа.
     *
     * @return строка с общей информацией по словарям.
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        scores.forEach((name, score) -> summary.append(name).append(": ").append(score).append("\n"));
        return summary.toString();
    }

    /**
     * Возвращает детализированную информацию о совпадениях.
     *
     * @return строка с информацией о совпадениях по каждому словарю.
     */
    public String getDetailedMatches() {
        StringBuilder details = new StringBuilder();
        matches.forEach((dictionaryName, terms) -> {
            details.append(dictionaryName).append(":\n");
            terms.entrySet().stream()
                    .filter(entry -> entry.getValue() > 0)
                    .forEach(entry -> details.append("  - ").append(entry.getKey())
                            .append(": ").append(entry.getValue()).append("\n"));
        });

        return details.toString();
    }

    /**
     * Возвращает название словаря с максимальным количеством совпадений.
     *
     * @return название словаря с наибольшим счётом.
     */
    public String getDetectedType() {
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Не определено");
    }
}
