# FHIR StructureDefinition ↔ UML Transformation Tool

## Overview

This project focuses on developing tools and scripts that enable bidirectional, automated transformation between FHIR StructureDefinition resources and UML class diagrams. By bridging the gap between FHIR data models and traditional software modeling tools, the project aims to streamline healthcare systems development and foster interoperability. The resulting solution will also be integrated into the TermX platform, thereby enhancing its modeling capabilities.

### Key Objectives

1. **Bidirectional Conversion:**  
   - Automatically generate UML class diagrams (e.g., PlantUML) from FHIR StructureDefinitions.
   - Convert UML class diagrams back into valid FHIR StructureDefinitions.
   
2. **Integration with PlantUML:**  
   - Utilize PlantUML to produce readable and clear UML diagrams.
   
3. **TermX Platform Integration:**  
   - Add UML support to the TermX modeling environment, enabling users to view, edit, and maintain FHIR resources as UML diagrams.

### Significance

By enabling direct transformations between standard FHIR definitions and UML diagrams, this project:
- Simplifies the work of developers who rely on established modeling methodologies.
- Enhances interoperability and accelerates the development of healthcare IT systems.
- Expands TermX platform functionality, promoting open-source healthcare solution advancement.

## Requirements

- **Java 21 or above** (tested with OpenJDK)
- **Gradle** (latest version recommended)
- **HAPI FHIR libraries** (configured in `build.gradle`)
- **PlantUML**  
- **Graphviz** (required by PlantUML to generate diagrams)

### Checking and Installing Graphviz

**Check if Graphviz is installed:**
```bash
dot -V
```
If Graphviz is installed, this command should print a version number (e.g., `dot - graphviz version X.YZ`). If you get a "command not found" error, you need to install it.

**Install Graphviz:**

- On **Ubuntu/Debian**:
  ```bash
  sudo apt-get update
  sudo apt-get install graphviz
  ```

- On **Fedora/CentOS/RHEL**:
  ```bash
  sudo dnf install graphviz
  ```
  *or*
  ```bash
  sudo yum install graphviz
  ```

- On **macOS** (with Homebrew):
  ```bash
  brew install graphviz
  ```

- On **Windows**:  
  Download and install Graphviz from the official site: [https://graphviz.org/download/](https://graphviz.org/download/)

Once Graphviz is installed, `dot -V` should work, and PlantUML can generate UML diagrams.

## Building Converter

1. **Clone the repository:**
   ```bash
   git clone <repository_url>
   cd <project_directory>
   ```

2. **Go to the converter folder:**
   ```bash
   cd ./converter
   ```

3. **Build with Gradle Converter:**
   ```bash
   ./gradlew build
   ```
   This command will:
   - Download and install all required dependencies.
   - Compile the source code.
   - Run any included tests.
   - Produce a JAR file in `build/libs/`.

## Running the Converter from the Command Line

After a successful build, you can run the application as follows:

```bash
java -jar build/libs/fhir-uml-generation.jar --input path/to/structuredefinition.json --output path/to/output.png
```

**Command-line Parameters**

The `fhir-uml-generation.jar` supports a range of options to customize the transformation between FHIR and UML formats.

### Basic Usage

```bash
java -jar build/libs/fhir-uml-generation.jar \
    --input path/to/input.json \
    --output path/to/output.png \
    [--txt [output.txt]] \
    [--mode uml|fhir] \
    [--view snapshot|differential] \
    [--hide_removed_objects true|false] \
    [--show_constraints true|false] \
    [--show_bindings true|false] \
    [--reduce_slice_classes true|false] \
    [--hide_legend true|false] \
    [--help]
```

### Parameters

- `--input`  
  Path to the input file (FHIR StructureDefinition JSON or UML .txt depending on mode).
  
- `--output`  
  Path to the output file (PNG for UML mode, JSON for FHIR mode).
  
- `--txt` *(optional)*  
  Also generate the PlantUML text file. You can specify a custom filename or let it default.

- `--mode`  
  Conversion direction:  
  - `uml` (default): FHIR → UML  
  - `fhir`: UML → FHIR

- `--view`  
  What elements to include from the StructureDefinition:  
  - `snapshot` (default)  
  - `differential`

- `--hide_removed_objects`  
  Whether to exclude removed/unsupported FHIR elements. Default: `true`.

- `--show_constraints`  
  Whether to include constraints in the UML diagram. Default: `true`.

- `--show_bindings`  
  Whether to show value set bindings. Default: `true`.

- `--reduce_slice_classes`  
  Simplifies sliced elements into fewer UML classes. Default: `false`.

- `--hide_legend`  
  Whether to hide the legend and notes in the UML output. Default: `false`.

- `--help`  
  Prints full usage instructions and exits.

---

**Example (FHIR to UML):**
```bash
java -jar build/libs/fhir-uml-generation.jar \
  --input resources/example-structuredefinition.json \
  --output diagrams/generated-class-diagram.png
```

## License

This project is licensed under the **MIT** license.
