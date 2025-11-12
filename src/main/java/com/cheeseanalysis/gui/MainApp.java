package com.cheeseanalysis.gui;

import com.cheeseanalysis.hadoop.HadoopReader;
import com.cheeseanalysis.model.CheeseRecord;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;

public class MainApp extends Application {

    private TableView<CheeseRecord> table;
    private BarChart<String, Number> chart;
    private ComboBox<String> manufacturerProvCodeCombo;
    private ComboBox<String> categoryTypeCombo;
    private ComboBox<String> milkTypeCombo;
    private ComboBox<String> calcTypeCombo;
    private List<CheeseRecord> allRecords;
    private Label resultLabel;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load data from HDFS
        try {
            allRecords = HadoopReader.readCSV("/cheese_data/cheese_data.csv");
            System.out.println("Successfully loaded " + allRecords.size() + " cheese records from HDFS");
        } catch (Exception e) {
            // For testing purposes, create empty list if HDFS is not available
            allRecords = FXCollections.observableArrayList();
            System.err.println("Could not load data from HDFS: " + e.getMessage());
            e.printStackTrace();
        }

        BorderPane root = new BorderPane();

        // Create filters
        createFilters(root);

        // Create table
        createTable(root);

        // Create chart
        createChart(root);

        // Initialize filter options
        initializeFilterOptions();

        Scene scene = new Scene(root, 1400, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Cheese Analysis Application");
        primaryStage.show();

        // Apply filters once on startup if data is available
        if (!allRecords.isEmpty()) {
            applyFilters();
        }
    }

    private void createFilters(BorderPane root) {
        // Filter 1: ManufacturerProvCode
        manufacturerProvCodeCombo = new ComboBox<>();
        manufacturerProvCodeCombo.setPromptText("Select Province Code");

        // Filter 2: CategoryTypeEn
        categoryTypeCombo = new ComboBox<>();
        categoryTypeCombo.setPromptText("Select Cheese Category");

        // Filter 3: MilkTypeEn
        milkTypeCombo = new ComboBox<>();
        milkTypeCombo.setPromptText("Select Milk Type");

        // Filter 4: Calculation Type
        calcTypeCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Average Moisture Percent", "Organic Percentage"));
        calcTypeCombo.setPromptText("Select Calculation Type");

        Button applyBtn = new Button("Apply Filters");
        applyBtn.setOnAction(e -> applyFilters());

        Button clearBtn = new Button("Clear Filters");
        clearBtn.setOnAction(e -> clearFilters());

        // Result label
        resultLabel = new Label("Select filters and click Apply to see results");
        resultLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: blue;");

        VBox filterBox = new VBox(10);
        HBox filterRow1 = new HBox(10, 
            new Label("Province Code:"), manufacturerProvCodeCombo,
            new Label("Category:"), categoryTypeCombo);
        HBox filterRow2 = new HBox(10,
            new Label("Milk Type:"), milkTypeCombo,
            new Label("Calculation:"), calcTypeCombo);
        HBox buttonRow = new HBox(10, applyBtn, clearBtn);
        
        filterBox.getChildren().addAll(filterRow1, filterRow2, buttonRow, resultLabel);
        filterBox.setStyle("-fx-padding: 10;");
        
        root.setTop(filterBox);
    }

    private void createTable(BorderPane root) {
        table = new TableView<>();
        
        TableColumn<CheeseRecord, String> colCheeseId = new TableColumn<>("Cheese ID");
        colCheeseId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCheeseId()));
        
        TableColumn<CheeseRecord, String> colCheeseName = new TableColumn<>("Cheese Name");
        colCheeseName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCheeseName()));
        
        TableColumn<CheeseRecord, String> colProvCode = new TableColumn<>("Province Code");
        colProvCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getManufacturerProvCode()));
        
        TableColumn<CheeseRecord, String> colCategory = new TableColumn<>("Category");
        colCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoryTypeEn()));
        
        TableColumn<CheeseRecord, String> colMilkType = new TableColumn<>("Milk Type");
        colMilkType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMilkTypeEn()));
        
        TableColumn<CheeseRecord, Double> colMoisture = new TableColumn<>("Moisture %");
        colMoisture.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getMoisturePercent()).asObject());
        
        TableColumn<CheeseRecord, String> colOrganic = new TableColumn<>("Organic");
        colOrganic.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getOrganic() == 1 ? "Yes" : "No"));
        
        table.getColumns().addAll(colCheeseId, colCheeseName, colProvCode, colCategory, 
                                 colMilkType, colMoisture, colOrganic);
        
        root.setCenter(table);
    }

    private void createChart(BorderPane root) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Analysis Results");
        chart.setPrefHeight(300);
        root.setBottom(chart);
    }

    private void initializeFilterOptions() {
        if (allRecords.isEmpty()) {
            return;
        }

        // Populate ManufacturerProvCode filter
        Set<String> provCodes = allRecords.stream()
                .map(CheeseRecord::getManufacturerProvCode)
                .filter(code -> code != null && !code.trim().isEmpty())
                .collect(Collectors.toSet());
        manufacturerProvCodeCombo.setItems(FXCollections.observableArrayList(provCodes).sorted());

        // Populate CategoryTypeEn filter
        Set<String> categories = allRecords.stream()
                .map(CheeseRecord::getCategoryTypeEn)
                .filter(cat -> cat != null && !cat.trim().isEmpty())
                .collect(Collectors.toSet());
        categoryTypeCombo.setItems(FXCollections.observableArrayList(categories).sorted());

        // Populate MilkTypeEn filter
        Set<String> milkTypes = allRecords.stream()
                .map(CheeseRecord::getMilkTypeEn)
                .filter(type -> type != null && !type.trim().isEmpty())
                .collect(Collectors.toSet());
        milkTypeCombo.setItems(FXCollections.observableArrayList(milkTypes).sorted());
        
        // Debug: Print available options
        System.out.println("Available Province Codes: " + provCodes);
        System.out.println("Available Categories: " + categories);
        System.out.println("Available Milk Types: " + milkTypes);
    }

    private void applyFilters() {
        String selectedProvCode = manufacturerProvCodeCombo.getValue();
        String selectedCategory = categoryTypeCombo.getValue();
        String selectedMilkType = milkTypeCombo.getValue();
        String selectedCalcType = calcTypeCombo.getValue();

        if (selectedCalcType == null) {
            resultLabel.setText("Please select a calculation type");
            return;
        }

        System.out.println("Applying filters:");
        System.out.println("  Province Code: " + selectedProvCode);
        System.out.println("  Category: " + selectedCategory);
        System.out.println("  Milk Type: " + selectedMilkType);
        System.out.println("  Calculation Type: " + selectedCalcType);

        List<CheeseRecord> filtered = allRecords.stream()
                .filter(record -> selectedProvCode == null || selectedProvCode.equals(record.getManufacturerProvCode()))
                .filter(record -> selectedCategory == null || selectedCategory.equals(record.getCategoryTypeEn()))
                .filter(record -> selectedMilkType == null || selectedMilkType.equals(record.getMilkTypeEn()))
                .collect(Collectors.toList());

        System.out.println("Filtered records count: " + filtered.size());

        // Update table with filtered data
        ObservableList<CheeseRecord> tableData = FXCollections.observableArrayList(filtered);
        table.setItems(tableData);

        // Perform calculation based on selected type
        double result = 0.0;
        String resultDescription = "";

        if ("Average Moisture Percent".equals(selectedCalcType)) {
            if (selectedCategory != null && selectedMilkType != null) {
                result = filtered.stream()
                        .filter(r -> selectedCategory.equals(r.getCategoryTypeEn()) && 
                                   selectedMilkType.equals(r.getMilkTypeEn()))
                        .mapToDouble(CheeseRecord::getMoisturePercent)
                        .average()
                        .orElse(0.0);
                resultDescription = String.format("Average Moisture for %s cheese made from %s milk: %.2f%%", 
                                                selectedCategory, selectedMilkType, result);
            } else {
                resultLabel.setText("Please select both Category and Milk Type for moisture calculation");
                return;
            }
        } else if ("Organic Percentage".equals(selectedCalcType)) {
            if (selectedCategory != null && selectedProvCode != null) {
                List<CheeseRecord> categoryProvRecords = filtered.stream()
                        .filter(r -> selectedCategory.equals(r.getCategoryTypeEn()) && 
                                   selectedProvCode.equals(r.getManufacturerProvCode()))
                        .collect(Collectors.toList());
                
                long organicCount = categoryProvRecords.stream()
                        .mapToInt(CheeseRecord::getOrganic)
                        .sum();
                
                result = categoryProvRecords.size() > 0 ? 
                        (double) organicCount / categoryProvRecords.size() * 100.0 : 0.0;
                
                resultDescription = String.format("Organic cheese percentage for %s in %s: %.2f%%", 
                                                selectedCategory, selectedProvCode, result);
            } else {
                resultLabel.setText("Please select both Category and Province Code for organic percentage calculation");
                return;
            }
        }

        // Update result label
        resultLabel.setText(resultDescription);

        // Update chart
        updateChart(selectedCalcType, result, selectedCategory, selectedMilkType, selectedProvCode);
    }

    private void updateChart(String calcType, double result, String category, String milkType, String provCode) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        if ("Average Moisture Percent".equals(calcType)) {
            series.setName("Average Moisture Percentage");
            String label = category + " (" + milkType + ")";
            series.getData().add(new XYChart.Data<>(label, result));
            chart.setTitle("Average Moisture Percentage by Category and Milk Type");
        } else if ("Organic Percentage".equals(calcType)) {
            series.setName("Organic Percentage");
            String label = category + " (" + provCode + ")";
            series.getData().add(new XYChart.Data<>(label, result));
            chart.setTitle("Organic Cheese Percentage by Category and Province");
        }
        
        chart.getData().add(series);
    }

    private void clearFilters() {
        manufacturerProvCodeCombo.setValue(null);
        categoryTypeCombo.setValue(null);
        milkTypeCombo.setValue(null);
        calcTypeCombo.setValue(null);
        table.setItems(FXCollections.observableArrayList(allRecords));
        chart.getData().clear();
        resultLabel.setText("Filters cleared. Select filters and click Apply to see results");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
