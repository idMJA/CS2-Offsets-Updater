package my.mjba.CS2Offsets

import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.Stage
import kotlinx.coroutines.*

class CS2OffsetApp : Application() {
    private val updater = CS2OffsetsUpdater
    private lateinit var logArea: TextArea
    private lateinit var progressBar: ProgressBar
    private lateinit var updateButton: Button
    private var currentJob: Job? = null

    private fun log(message: String) {
        Platform.runLater {
            logArea.appendText("${message}\n")
            logArea.positionCaret(logArea.length)
        }
    }

    private fun showAboutDialog() {
        Alert(Alert.AlertType.INFORMATION).apply {
            title = "About CS2 Offsets Updater"
            headerText = "CS2 Offsets Updater"
            contentText = "This application automatically updates CS2 game offsets from the latest available data."
            showAndWait()
        }
    }

    override fun stop() {
        currentJob?.cancel()
        super.stop()
    }

    private suspend fun startUpdate() {
        currentJob?.cancel()
        try {
            withContext(Dispatchers.Main) {
                updateButton.isDisable = true
                progressBar.isVisible = true
                progressBar.progress = 0.0
            }

            log("Checking local files...")
            if (updater.getLocalFiles().isEmpty()) {
                log("No local files found. Please add some .json, .cs, .hpp, or .rs files to the application directory.")
                return
            }
            
            log("Checking for updates...")
            val updates = withContext(Dispatchers.IO) {
                updater.checkForUpdates()
            }
            
            if (updates.isEmpty()) {
                log("All files are up to date!")
                return
            }

            log("Found ${updates.size} files to update:")
            updates.forEach { file -> log("- ${file.name}") }

            val totalFiles = updates.size
            var completedFiles = 0

            updates.forEach { file ->
                try {
                    withContext(Dispatchers.IO) {
                        updater.updateFile(file)
                    }
                    completedFiles++
                    withContext(Dispatchers.Main) {
                        progressBar.progress = completedFiles.toDouble() / totalFiles
                    }
                    log("✓ Updated ${file.name}")
                } catch (e: Exception) {
                    log("❌ Failed to update ${file.name}: ${e.message}")
                }
            }

            log("Update completed successfully!")
        } catch (e: Exception) {
            log("Error during update: ${e.message}")
            e.printStackTrace()
        } finally {
            withContext(Dispatchers.Main) {
                updateButton.isDisable = false
                progressBar.isVisible = false
                progressBar.progress = 0.0
            }
        }
    }

    override fun start(stage: Stage) {
        val root = VBox(10.0).apply {
            padding = Insets(10.0)
            prefWidth = 600.0
        }

        // Status area
        logArea = TextArea().apply {
            isEditable = false
            prefRowCount = 20
            isWrapText = true
            styleClass.add("log-area")
        }

        // Progress bar
        progressBar = ProgressBar(0.0).apply {
            prefWidth = Double.MAX_VALUE
            isVisible = false
            styleClass.add("progress-bar")
        }

        // Update button
        updateButton = Button("Check for Updates").apply {
            prefWidth = Double.MAX_VALUE
            styleClass.add("update-button")
            setOnAction {
                val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
                scope.launch {
                    try {
                        startUpdate()
                    } catch (e: Exception) {
                        log("Error: ${e.message}")
                    }
                }
            }
        }

        // Create menu bar
        val menuBar = MenuBar().apply {
            menus.add(Menu("File").apply {
                items.addAll(
                    MenuItem("Exit").apply {
                        setOnAction { Platform.exit() }
                    }
                )
            })
            menus.add(Menu("Help").apply {
                items.addAll(
                    MenuItem("About").apply {
                        setOnAction { showAboutDialog() }
                    }
                )
            })
        }

        root.children.addAll(
            menuBar,
            Label("CS2 Offsets Updater").apply { styleClass.add("title-label") },
            logArea,
            progressBar,
            updateButton
        )

        val scene = Scene(root)
        scene.stylesheets.add(javaClass.getResource("/styles.css")?.toExternalForm() ?: "")

        stage.apply {
            title = "CS2 Offsets Updater"
            this.scene = scene
            width = 800.0
            height = 600.0
            show()
        }        // Initial message
        log("Welcome to CS2 Offsets Updater")
        log("Place the offset files you want to keep updated in the same directory as this application")
        log("The application will only update files that already exist")
        log("Click 'Check for Updates' to start the update process")
    }
}
