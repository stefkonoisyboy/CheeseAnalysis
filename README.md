# Cheese Analysis Application

This Java application uses Hadoop for processing and analyzing cheese data with an interactive GUI for filtering and visualization.

## Data Structure

The dataset includes the following columns:
- **CheeseId**: Unique identifier for the cheese
- **ManufacturerProvCode**: Province code of the manufacturer (e.g., AB, NS)
- **ManufacturingTypeEn**: Manufacturing type (e.g., Artisan, Industrial, Farmstead)
- **MoisturePercent**: Moisture percentage of the cheese
- **FlavourEn**: Flavor characteristics description
- **CharacteristicsEn**: Additional cheese characteristics
- **Organic**: Indicator if cheese is organic (1 for organic, 0 for non-organic)
- **CategoryTypeEn**: Cheese category (e.g., Fresh Cheese, Semi-soft Cheese)
- **MilkTypeEn**: Type of milk (e.g., Cow, Ewe)
- **MilkTreatmentTypeEn**: Milk treatment type (e.g., Pasteurized, Thermised)
- **RindTypeEn**: Type of cheese rind
- **CheeseName**: Name of the cheese
- **FatLevel**: Fat level (e.g., higher fat, lower fat)

## Features

### GUI Filters
1. **Filter 1**: ManufacturerProvCode (e.g., AB, NS)
2. **Filter 2**: CategoryTypeEn (e.g., Fresh Cheese, Semi-soft Cheese, Hard Cheese)
3. **Filter 3**: MilkTypeEn (e.g., Cow, Ewe)
4. **Filter 4**: Calculation Type with two options:
   - **Option 1**: Average moisture percentage for selected cheese category and milk type
   - **Option 2**: Percentage of organic cheeses compared to all cheeses in selected category and province

### MapReduce Components

#### Moisture Analysis
- **MoistureMapper**: Maps cheese records to category|milkType keys with moisture values
- **MoistureReducer**: Calculates average moisture percentage for each category-milk type combination
- **MoistureDriver**: Hadoop job driver for moisture analysis

#### Organic Percentage Analysis
- **OrganicMapper**: Maps cheese records to category|province keys with organic indicators
- **OrganicReducer**: Calculates organic percentage for each category-province combination
- **OrganicDriver**: Hadoop job driver for organic percentage analysis

## Building and Running

### Prerequisites
- Java 8
- Maven 3.x
- Hadoop 2.10.1 (for HDFS access)
- JavaFX (included in dependencies)

### Build
```bash
mvn clean compile
```

### Run GUI Application
```bash
mvn exec:java -Dexec.mainClass="com.cheeseanalysis.gui.MainApp"
```

Or with JavaFX plugin:
```bash
mvn javafx:run
```

### Run MapReduce Jobs

#### Moisture Analysis
```bash
hadoop jar target/CheeseAnalysis-1.0-SNAPSHOT.jar com.cheeseanalysis.MoistureDriver /input/cheese_data.csv /output/moisture
```

#### Organic Percentage Analysis
```bash
hadoop jar target/CheeseAnalysis-1.0-SNAPSHOT.jar com.cheeseanalysis.OrganicDriver /input/cheese_data.csv /output/organic
```

## Data Setup

1. Place your cheese data CSV file in HDFS:
```bash
hdfs dfs -put cheese_data.csv /cheese_data/
```

2. Update the HDFS path in `HadoopReader.java` if needed (default: `/cheese_data/cheese.csv`)

## Project Structure

```
src/main/java/com/cheeseanalysis/
├── gui/
│   └── MainApp.java          # Main GUI application
├── hadoop/
│   └── HadoopReader.java     # HDFS data reader
├── model/
│   └── CheeseRecord.java     # Data model class
├── MoistureMapper.java       # Mapper for moisture analysis
├── MoistureReducer.java      # Reducer for moisture analysis
├── MoistureDriver.java       # Driver for moisture MapReduce job
├── OrganicMapper.java        # Mapper for organic percentage analysis
├── OrganicReducer.java       # Reducer for organic percentage analysis
├── OrganicDriver.java        # Driver for organic MapReduce job
└── Main.java                 # Main entry point
```

## Usage

1. Start the application using Maven
2. The GUI will load with four filter options
3. Select desired filters:
   - Province Code (optional)
   - Cheese Category (required for calculations)
   - Milk Type (required for moisture calculation)
   - Calculation Type (required)
4. Click "Apply Filters" to see results
5. Results are displayed in:
   - A data table showing filtered records
   - A bar chart visualizing the calculation result
   - A text label showing the calculated value

## Notes

- The application will attempt to load data from HDFS at `/cheese_data/cheese_data.csv`
- If HDFS is not available, the application will start with empty data
- All filters are optional except for the calculation type
- Specific filter combinations are required for each calculation type as indicated in the GUI
