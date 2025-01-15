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

- **Java 17 or above** (tested with OpenJDK)
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

## Building the Project

1. **Clone the repository:**
   ```bash
   git clone <repository_url>
   cd <project_directory>
   ```

2. **Build with Gradle:**
   ```bash
   ./gradlew build
   ```
   This command will:
   - Download and install all required dependencies.
   - Compile the source code.
   - Run any included tests.
   - Produce a JAR file in `build/libs/`.

## Running the Application from the Command Line

After a successful build, you can run the application as follows:

```bash
java -jar build/libs/fhir-uml-generation.jar --input path/to/structuredefinition.json --output path/to/output.png
```

**Command-line parameters:**

- `--input` specifies the input FHIR StructureDefinition.
- `--output` specifies the desired output file path as image.
- Additional options can be found by running:
  ```bash
  java -jar build/libs/fhir-uml-generation.jar --help
  ```

**Example (FHIR to UML):**
```bash
java -jar build/libs/fhir-uml-generation.jar \
  --input resources/example-structuredefinition.json \
  --output diagrams/generated-class-diagram.png
```

## License

This project is licensed under the **MIT** license.