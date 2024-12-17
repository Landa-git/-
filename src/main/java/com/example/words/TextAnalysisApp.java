package com.example.words;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Основной класс приложения JavaFX.
 * Реализует пользовательский интерфейс для выбора файла, запуска анализа текста и отображения результатов.
 * Поддерживаемые форматы файлов: .txt, .doc, .docx.
 */
public class TextAnalysisApp extends Application {

    private static final Logger logger = LogManager.getLogger(TextAnalysisApp.class);

    private TextAnalyzer textAnalyzer;
    private TextArea resultArea;
    private TextField textFilePathField;

    /**
     * Запуск графического интерфейса JavaFX.
     *
     * @param primaryStage главный контейнер приложения.
     */
    @Override
    public void start(Stage primaryStage) {
        this.textAnalyzer = TextAnalysisAppLauncher.getTextAnalyzer();

        logger.info("Инициализация интерфейса");

        primaryStage.setTitle("Анализатор текста");

        // Элементы интерфейса
        Label textFileLabel = new Label("Текстовый файл:");
        textFilePathField = new TextField();
        textFilePathField.setEditable(false);
        Button chooseTextFileButton = new Button("Выбрать файл");
        Button analyzeButton = new Button("Анализировать текст");

        resultArea = new TextArea();
        resultArea.setEditable(false);

        // Логика выбора текстового файла
        chooseTextFileButton.setOnAction(e -> chooseTextFile(primaryStage));

        // Логика анализа текста
        analyzeButton.setOnAction(e -> analyzeText());

        VBox layout = new VBox(10, textFileLabel, textFilePathField, chooseTextFileButton, analyzeButton, resultArea);

        Scene scene = new Scene(layout, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Метод для открытия диалогового окна выбора файла.
     * Устанавливает путь к выбранному файлу в текстовое поле.
     *
     * @param primaryStage главный контейнер приложения.
     */
    private void chooseTextFile(Stage primaryStage) {
        logger.info("Выбор текстового файла");
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt", "*.doc", "*.docx")
        );
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            textFilePathField.setText(selectedFile.getAbsolutePath());
            logger.info("Выбран файл: " + selectedFile.getAbsolutePath());
        }
    }

    /**
     * Метод для анализа текста из выбранного файла.
     * Выполняет анализ текста, используя TextAnalyzer, и отображает результаты в текстовой области.
     * В случае ошибки выводит сообщение об ошибке.
     */
    private void analyzeText() {
        logger.info("Начало анализа текста");

        String textFilePath = textFilePathField.getText();
        if (textFilePath.isEmpty()) {
            resultArea.setText("Пожалуйста, выберите текстовый файл.");
            logger.warn("Текстовый файл не выбран");
            return;
        }

        try {
            // Чтение текста из файла
            String text = readTextFromFile(textFilePath).toLowerCase();
            // Выполнение анализа текста
            AnalysisResult result = textAnalyzer.analyze(text);

            // Формирование и отображение результатов
            StringBuilder resultBuilder = new StringBuilder();
            resultBuilder.append("Результаты анализа:\n")
                    .append(result.getSummary())
                    .append("\n\nДетализация вхождений:\n")
                    .append(result.getDetailedMatches())
                    .append("\nТип текста: ")
                    .append(result.getDetectedType());

            resultArea.setText(resultBuilder.toString());
            logger.info("Анализ завершён успешно");
        } catch (Exception ex) {
            resultArea.setText("Ошибка анализа текста: " + ex.getMessage());
            logger.error("Ошибка анализа текста", ex);
        }
    }

    /**
     * Метод для чтения текста из файла.
     * Поддерживаются форматы .txt, .doc и .docx.
     *
     * @param filePath путь к файлу.
     * @return текст, извлечённый из файла.
     * @throws Exception в случае ошибки чтения файла.
     */
    private String readTextFromFile(String filePath) throws Exception {
        if (filePath.endsWith(".txt")) {
            return Files.readString(Paths.get(filePath));
        } else if (filePath.endsWith(".doc")) {
            try (FileInputStream fis = new FileInputStream(filePath);
                 HWPFDocument doc = new HWPFDocument(fis);
                 WordExtractor extractor = new WordExtractor(doc)) {
                return extractor.getText();
            }
        } else if (filePath.endsWith(".docx")) {
            try (FileInputStream fis = new FileInputStream(filePath);
                 XWPFDocument docx = new XWPFDocument(fis)) {
                return docx.getParagraphs().stream()
                        .map(p -> p.getText())
                        .reduce("", (p1, p2) -> p1 + "\n" + p2);
            }
        } else {
            throw new IllegalArgumentException("Неподдерживаемый формат файла: " + filePath);
        }
    }
}