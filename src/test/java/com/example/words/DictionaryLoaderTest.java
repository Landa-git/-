package com.example.words;

import static org.junit.jupiter.api.Assertions.*;

import com.github.demidko.aot.MorphologyTag;
import com.github.demidko.aot.WordformMeaning;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
class DictionaryLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void testLoadDictionaries() throws IOException {
        // Создаем временные файлы для теста
        Path dictionaryFile = tempDir.resolve("dictionary.txt");
        Path configFile = tempDir.resolve("config.txt");

        // Записываем тестовые данные в файл словаря
        Files.writeString(dictionaryFile, "слово;мир;привет");

        // Записываем путь к словарю в файл конфигурации
        Files.writeString(configFile, "Тестовый словарь;" + dictionaryFile.toString());

        // Создаем DictionaryLoader
        DictionaryLoader loader = new DictionaryLoader(configFile.toString());

        // Загружаем словари
        List<Dictionary> dictionaries = loader.loadDictionaries();

        // Проверяем корректность загрузки
        assertNotNull(dictionaries);
        assertEquals(1, dictionaries.size());

        Dictionary dictionary = dictionaries.get(0);
        assertEquals("Тестовый словарь", dictionary.getName());
        assertEquals(3, dictionary.getEntries().size());

        DictionaryEntry entry = dictionary.getEntries().get(0);
        assertEquals("слово", entry.getTerm());
        assertNotNull(entry.getForms());
        assertTrue(entry.getForms().contains("слово"));
    }

    @Test
    void testGenerateWordFormsSingleWord() throws IOException {
        // Создаем DictionaryLoader
        DictionaryLoader loader = new DictionaryLoader("dummy_config");

        // Генерируем формы для одного слова
        List<String> forms = loader.generateWordForms("слово");

        // Проверяем корректность генерации
        assertNotNull(forms);
        assertTrue(forms.contains("слова"));
        assertTrue(forms.contains("словом"));
        assertTrue(forms.contains("слове"));
    }

    @Test
    void testGenerateWordFormsTwoWords() throws IOException {
        // Создаем DictionaryLoader
        DictionaryLoader loader = new DictionaryLoader("dummy_config");

        // Генерируем формы для словосочетания
        List<String> forms = loader.generateWordForms("железный меч");

        // Проверяем корректность генерации
        assertNotNull(forms);
        assertTrue(forms.contains("железного меча") || forms.contains("меча железного"));
        assertTrue(forms.contains("железным мечом") || forms.contains("мечом железным"));
    }

}