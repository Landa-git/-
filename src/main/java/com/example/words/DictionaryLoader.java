package com.example.words;

import com.github.demidko.aot.MorphologyTag;
import com.github.demidko.aot.PartOfSpeech;
import com.github.demidko.aot.WordformMeaning;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.demidko.aot.WordformMeaning.lookupForMeanings;

/**
 * Класс для загрузки словарей из конфигурационного файла.
 */
public class DictionaryLoader {

    private static final Logger logger = LogManager.getLogger(DictionaryLoader.class);
    private final String configPath;

    /**
     * Конструктор для инициализации пути к конфигурационному файлу.
     *
     * @param configPath путь к конфигурационному файлу.
     */
    public DictionaryLoader(String configPath) {
        this.configPath = configPath;
    }

    /**
     * Загружает словари из конфигурационного файла.
     * Каждый словарь представляет собой набор терминов, загруженных из указанного файла.
     *
     * @return список словарей.
     */
    public List<Dictionary> loadDictionaries() {
        List<Dictionary> dictionaries = new ArrayList<>();
        try {
            logger.info("Загрузка словарей из конфигурационного файла: " + configPath);
            List<String> lines = Files.readAllLines(Paths.get(configPath));

            for (String line : lines) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    String name = parts[0].trim();
                    String filePath = parts[1].trim();

                    // Чтение записей из файла словаря
                    List<DictionaryEntry> entries = new ArrayList<>();
                    List<String> dictionaryLines = Files.readAllLines(Paths.get(filePath));
                    for (String dictLine : dictionaryLines) {
                        String[] terms = dictLine.split(";");
                        for (String term : terms) {
                            List<String> forms = generateWordForms(term.trim());
                            entries.add(new DictionaryEntry(term.trim(), forms));
                        }
                    }

                    dictionaries.add(new Dictionary(name, entries));
                    logger.info("Загружен словарь: " + name);
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка загрузки словарей", e);
        }
        return dictionaries;
    }

    /**
     * Генерирует все возможные формы слова или словосочетания.
     * Обрабатывает одиночные слова и словосочетания (до двух слов).
     *
     * @param term исходный термин.
     * @return список форм термина.
     */
    List<String> generateWordForms(String term) {
        List<String> allForms = new ArrayList<>();
        String[] words = term.split("\\s+");

        if (words.length == 1) {
            // Для одного слова генерируем его формы
            allForms.addAll(fetchWordForms(words[0]));
        } else if (words.length == 2) {
            // Для словосочетаний обрабатываем с учетом частей речи
            List<List<WordformMeaning>> wordMeaningsList = new ArrayList<>();

            boolean allWordsHaveMeanings = true;
            for (String word : words) {
                List<WordformMeaning> meanings = lookupForMeanings(word);
                if (meanings.isEmpty()) {
                    allWordsHaveMeanings = false; // Если у хотя бы одного слова нет значений, пропускаем генерацию
                    break;
                }
                wordMeaningsList.add(meanings);
            }

            if (allWordsHaveMeanings) {
                allForms.addAll(generateAgreedCombinations(wordMeaningsList));
            } else {
                // Если хотя бы одно слово не имеет значений, добавляем исходный термин
                allForms.add(term.toLowerCase());
            }
        }

        return allForms.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Генерирует формы для одного слова.
     *
     * @param word слово.
     * @return список всех форм слова.
     */
    private List<String> fetchWordForms(String word) {
        List<String> wordForms = new ArrayList<>();
        List<WordformMeaning> meanings = lookupForMeanings(word);

        if (!meanings.isEmpty()) {
            List<WordformMeaning> transformations = meanings.get(0).getTransformations();
            transformations.forEach(transformation -> wordForms.add(transformation.toString().toLowerCase()));
        } else {
            wordForms.add(word); // Если формы не найдены, добавляем само слово
        }

        return wordForms;
    }

    /**
     * Генерирует согласованные комбинации для двух слов.
     * Учитывает части речи, род, число и падеж.
     *
     * @param wordMeaningsList список морфологических значений для каждого слова.
     * @return список согласованных комбинаций.
     */
    List<String> generateAgreedCombinations(List<List<WordformMeaning>> wordMeaningsList) {
        List<String> combinations = new ArrayList<>();
        if (wordMeaningsList.isEmpty() || wordMeaningsList.size() != 2) {
            return combinations; // Обрабатываем только двухсловные термины
        }

        List<WordformMeaning> firstWordMeanings = wordMeaningsList.get(0);
        List<WordformMeaning> secondWordMeanings = wordMeaningsList.get(1);

        PartOfSpeech firstPartOfSpeech = firstWordMeanings.get(0).getPartOfSpeech();
        PartOfSpeech secondPartOfSpeech = secondWordMeanings.get(0).getPartOfSpeech();

        if (firstPartOfSpeech == PartOfSpeech.Noun && secondPartOfSpeech == PartOfSpeech.Noun) {
            // Случай "Существительное + Существительное"
            combinations.addAll(generateNounNounCombinations(firstWordMeanings.get(0).getTransformations(), secondWordMeanings.get(0).getTransformations()));
        } else if (firstPartOfSpeech == PartOfSpeech.Noun && secondPartOfSpeech == PartOfSpeech.Adjective || firstPartOfSpeech == PartOfSpeech.Adjective && secondPartOfSpeech == PartOfSpeech.Noun) {
            // Случай "Существительное + Прилагательное"
            combinations.addAll(generateNounAdjectiveCombinations(firstWordMeanings.get(0).getTransformations(), secondWordMeanings.get(0).getTransformations()));
        }

        return combinations;
    }
    /**
     * Генерирует согласованные комбинации для существительное + существительное.
     * Учитывает род, число и падеж.
     *
     * @param firstWordMeanings список морфологических значений для первого слова.
     * @param secondWordMeanings список морфологических значений для второго слова.
     * @return список согласованных комбинаций.
     */
    private List<String> generateNounNounCombinations(List<WordformMeaning> firstWordMeanings, List<WordformMeaning> secondWordMeanings) {
        List<String> combinations = new ArrayList<>();

        // Все формы первого + неизменное второе
        String secondWord = secondWordMeanings.get(0).toString().toLowerCase();
        for (WordformMeaning firstMeaning : firstWordMeanings) {
            combinations.add(firstMeaning.toString().toLowerCase() + " " + secondWord);
        }

        // Неизменное первое + все формы второго
        String firstWord = firstWordMeanings.get(0).toString().toLowerCase();
        for (WordformMeaning secondMeaning : secondWordMeanings) {
            combinations.add(firstWord + " " + secondMeaning.toString().toLowerCase());
        }

        return combinations;
    }
    /**
     * Генерирует согласованные комбинации для существительное + прилагательное.
     * Учитывает род, число и падеж.
     *
     * @param nounMeanings список морфологических значений для существительного.
     * @param adjectiveMeanings список морфологических значений для прилагательного.
     * @return список согласованных комбинаций.
     */
    private List<String> generateNounAdjectiveCombinations(List<WordformMeaning> nounMeanings, List<WordformMeaning> adjectiveMeanings) {
        List<String> combinations = new ArrayList<>();

        for (WordformMeaning nounMeaning : nounMeanings) {
            for (WordformMeaning adjectiveMeaning : adjectiveMeanings) {
                if (areMorphologiesAgreed(nounMeaning.getMorphology(), adjectiveMeaning.getMorphology())) {
                    combinations.add(adjectiveMeaning.toString().toLowerCase() + " " + nounMeaning.toString().toLowerCase());
                }
            }
        }

        return combinations;
    }
    /**
     * Проверка согласованности
     *
     * @param firstMorphology список морфологических значений для первого.
     * @param secondMorphology список морфологических значений для второго.
     * @return список согласованных комбинаций.
     */
    private boolean areMorphologiesAgreed(List<MorphologyTag> firstMorphology, List<MorphologyTag> secondMorphology) {
        // Отбираем теги, относящиеся к числу и падежу
        List<MorphologyTag> relevantTags = List.of(
                MorphologyTag.Singular, MorphologyTag.Plural,                        // Число
                MorphologyTag.Nominative, MorphologyTag.Genitive, MorphologyTag.Dative, // Падеж
                MorphologyTag.Accusative, MorphologyTag.Ablative, MorphologyTag.Prepositional
        );

        // Проверяем, оба ли слова в множественном числе
        boolean firstIsPlural = firstMorphology.contains(MorphologyTag.Plural);
        boolean secondIsPlural = secondMorphology.contains(MorphologyTag.Plural);

        // Если оба слова множественные, проверяем только число и падеж
        if (firstIsPlural && secondIsPlural) {
            List<MorphologyTag> firstRelevant = firstMorphology.stream()
                    .filter(relevantTags::contains)
                    .collect(Collectors.toList());
            List<MorphologyTag> secondRelevant = secondMorphology.stream()
                    .filter(relevantTags::contains)
                    .collect(Collectors.toList());

            return firstRelevant.containsAll(secondRelevant) && secondRelevant.containsAll(firstRelevant);
        }

        // Если слова не оба множественные, включаем проверку рода
        if(!firstIsPlural&&!secondIsPlural) {
            List<MorphologyTag> extendedRelevantTags = new ArrayList<>(relevantTags);
            extendedRelevantTags.addAll(List.of(MorphologyTag.Male, MorphologyTag.Female, MorphologyTag.NeuterGender)); // Род

            List<MorphologyTag> firstExtendedRelevant = firstMorphology.stream()
                    .filter(extendedRelevantTags::contains)
                    .collect(Collectors.toList());
            List<MorphologyTag> secondExtendedRelevant = secondMorphology.stream()
                    .filter(extendedRelevantTags::contains)
                    .collect(Collectors.toList());

            boolean b = firstExtendedRelevant.containsAll(secondExtendedRelevant) && secondExtendedRelevant.containsAll(firstExtendedRelevant);
            return b;
        }
        return false;
    }

}
