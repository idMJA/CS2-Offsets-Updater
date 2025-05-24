# CS2Offsets

CS2Offsets is a Kotlin-based project that leverages JavaFX for its graphical user interface and various libraries for HTTP communication, JSON processing, and coroutines.

## Features
- JavaFX-based GUI
- HTTP communication using OkHttp
- JSON processing with Jackson
- Kotlin coroutines for asynchronous programming

## Requirements
- JDK 21
- Gradle

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/idMJA/CS2-Offsets-Updater.git
   cd CS2Offsets
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

3. Run the application:
   ```bash
   ./gradlew run
   ```

## Testing
To run the tests, execute:
```bash
./gradlew test
```

## Packaging
This project uses Launch4j to create a Windows executable. The configuration file for Launch4j is located at `src/main/resources/launch4j-config.xml`.

To generate the executable, run:
```bash
./gradlew launch4j
```

## GitHub Actions Workflow

This project includes a GitHub Actions workflow to automate building, packaging, and releasing the project.

### How to Use the Workflow

1. **Triggering the Workflow**:
   - The workflow runs automatically on pushes or pull requests to the `main` branch.
   - You can also trigger it manually using the "Run workflow" button in the GitHub Actions tab.

2. **Artifacts**:
   - The workflow builds the project and packages the following artifacts:
     - `.jar` files located in `build/libs/`
     - `.exe` files located in `build/launch4j/`

3. **Releases**:
   - The workflow creates a GitHub release with the built artifacts attached.
   - Ensure you have a valid tag name when pushing to trigger the release step.

For more details, see the workflow file at `.github/workflows/build-and-release.yml`.

## Credits

This project is inspired by and includes contributions from the [cs2-dumper](https://github.com/a2x/cs2-dumper) project.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
