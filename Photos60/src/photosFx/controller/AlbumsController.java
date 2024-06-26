package photosFx.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import photosFx.model.Album;
import photosFx.model.ContentSerializer;
import photosFx.model.Content;

/** Albums view, which allows creation, deletion, and renaming of albums.
 * @author Rayyan Sarkhot
*/
public class AlbumsController implements Initializable {

    public static String username = "";
    public static Content content;
    @FXML
    public Label status;
    @FXML
    public Button searchButton;
    @FXML
    private Label windowLabel;

    @FXML
    private Button logoutButton;
    @FXML
    private Button createAlbumButton;
    @FXML
    private Button deleteAlbumButton;
    @FXML
    private Button renameAlbumButton;
    @FXML
    private Button openAlbumButton;
    @FXML
    private TableView<Album> tableView;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        // This method will be called when the FXML file is loaded
        if (userExists(username)) {
            try {
                content = ContentSerializer.loadContent(username);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                content = new Content();
            }
        } else {
            content = new Content();
        }

        displayData(content.albums);

        try {
            ContentSerializer.saveContent(content, username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Display the data in the TableView
     *
     * @param albums List of albums to display
     */
    private void displayData(List<Album> albums) {

        tableView.getItems().clear();

        // Retrieve the existing TableColumn from the TableView
        TableColumn<Album, String> nameColumn = (TableColumn<Album, String>) tableView.getColumns().get(0);
        TableColumn<Album, String> numPhotosColumn = (TableColumn<Album, String>) tableView.getColumns().get(1);
        TableColumn<Album, String> earliestPhotoColumn = (TableColumn<Album, String>) tableView.getColumns().get(2);
        TableColumn<Album, String> latestPhotoColumn = (TableColumn<Album, String>) tableView.getColumns().get(3);

        // Set up cell value factory to extract the name from the Album object
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        numPhotosColumn.setCellValueFactory(new PropertyValueFactory<>("numPhotos"));
         earliestPhotoColumn.setCellValueFactory(new
         PropertyValueFactory<>("earliestPhoto"));
         latestPhotoColumn.setCellValueFactory(new
         PropertyValueFactory<>("latestPhoto"));

        // Add data to the TableView
        tableView.getItems().addAll(albums);
    }

    /**
     * Check if the user exists
     *
     * @param name Username
     * @return true if the user exists, false otherwise
     */
    public boolean userExists(String name) {

        String directoryPath = "src/photosFx/model/users";
        String fileName = name.toLowerCase() + ".dat";
        File directory = new File(directoryPath);
        File file = new File(directory, fileName);

        // Check if the file exists
        if (file.exists()) {
            System.out.println("file exists.");
            return true;
        } else {
            return false;
        }

    }

    /**
     * Handle the Rename Album button click event
     */
    @FXML
    private void handleRenameAlbumButtonClicked() {
        Album selectedAlbum = tableView.getSelectionModel().getSelectedItem();
        if (selectedAlbum != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Enter new Album Name for " + selectedAlbum.getName());
            dialog.setHeaderText("Rename");
            dialog.setContentText("Please enter the new album name:");

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(albumName -> {
                // Handle the entered album name here
                String enteredText = result.get().toUpperCase().strip();

                if (enteredText.equals("")) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Empty album name not allowed!");
                    alert.showAndWait();
                    return;
                } else {
                    boolean albumExists = content.getAlbumNames().stream()
                            .anyMatch(existingAlbumName -> existingAlbumName.equalsIgnoreCase(enteredText));
                    if (albumExists) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Album already exists!");
                        alert.showAndWait();
                        return;
                    }
                }

                selectedAlbum.setName(enteredText.toUpperCase());
                displayData(content.albums);
                try {
                    ContentSerializer.saveContent(content, username);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            // Show message popup indicating no album is selected
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No album selected. Please select an album to rename.");
            alert.showAndWait();
        }
    }

    /**
     * Save the content to the serialized user file (username.dat)
     */
    public static void updateContent() {
        try {
            ContentSerializer.saveContent(content, username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Handle the Create Album button click event
     */
    @FXML
    private void handleCreateAlbumButtonClicked() {
        Optional<String> result = openAlbumCreationDialog();

        // lambda method:
        // result.ifPresent(albumName -> content.albums.add(new Album(albumName)));
        if (result.isPresent()) {
            String albumName = result.get();
            content.albums.add(new Album(albumName));
        }

        try {
            ContentSerializer.saveContent(content, username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        displayData(content.albums);
    }

    /**
     * Open the Album Creation dialog
     *
     * @return Optional<String> containing the entered album name
     */
    public static Optional<String> openAlbumCreationDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter Album Name");
        dialog.setHeaderText(null);
        dialog.setContentText("Please enter the album name:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String enteredText = result.get().toUpperCase().strip();

            if (enteredText.equals("")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Empty album name not allowed!");
                alert.showAndWait();
                return Optional.empty();
            } else {
                boolean albumExists = content.getAlbumNames().stream()
                        .anyMatch(existingAlbumName -> existingAlbumName.equalsIgnoreCase(enteredText));
                if (albumExists) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Album already exits!");
                    alert.showAndWait();
                    return Optional.empty();
                }
            }
        }

        return result;
    }

    /**
     * Handle the Delete Album button click event
     */
    @FXML
    private void handleDeleteAlbumButtonClicked() {
        Album selectedAlbum = tableView.getSelectionModel().getSelectedItem();
        if (selectedAlbum != null) {
            // Delete the selected album
            String deletedAlbumName = deleteAlbum(selectedAlbum);

            // Show message popup indicating album is deleted
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText(null);
            alert.setContentText(deletedAlbumName + " Album deleted successfully.");
            alert.showAndWait();

        } else {
            // Show message popup indicating no album is selected
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No album selected. Please select an album to delete.");
            alert.showAndWait();
        }
    }

    /**
     * Delete the selected album
     *
     * @param selectedAlbum Album to delete
     * @return Name of the deleted album
     */
    @FXML
    private String deleteAlbum(Album selectedAlbum) {

        String name = selectedAlbum.getName();
        content.albums.remove(selectedAlbum);
        displayData(content.albums);

        try {
            ContentSerializer.saveContent(content, username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return name;

    }

    /**
     * Open the selected album
     */
    @FXML
    private void openAlbum() {
        Album selectedAlbum = tableView.getSelectionModel().getSelectedItem();
        if (selectedAlbum == null) {
            System.out.println("no album selected");
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No album selected. Please select an album to open.");
            alert.showAndWait();
            return;
        }
        try {
            // Load the FXML file for the new scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/photogrid.fxml"));
            Parent secondSceneRoot = loader.load();


            // Create a new scene with the loaded FXML file
            Scene secondScene = new Scene(secondSceneRoot, 800, 600);

            System.out.println("Current album: " + selectedAlbum);
            PhotoGridController photoGridController = loader.getController();
            photoGridController.setCurrentAlbum(selectedAlbum);

            // Get the stage from the button and set the new scene
            Stage stage = (Stage) openAlbumButton.getScene().getWindow();

            stage.setTitle("Opening " + selectedAlbum.getName() + " Album ...");
            photoGridController.refresh();

            stage.setScene(secondScene);
            stage.show();
            stage.setTitle(selectedAlbum.getName() + " Album");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle the Logout button click event
     */
    @FXML
    private void handleLogoutButtonClicked() {
        try {
            // Load the FXML file for the new scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/login.fxml"));
            Parent secondSceneRoot = loader.load();

            // Create a new scene with the loaded FXML file
            Scene secondScene = new Scene(secondSceneRoot, 600, 400);

            // Get the stage from the button and set the new scene
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(secondScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle the Search button click event
     */
    @FXML
    public void search(ActionEvent actionEvent) {
        try {
            // Load the FXML file for the new scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/searchpage.fxml"));
            Parent secondSceneRoot = loader.load();

            // Create a new scene with the loaded FXML file
            Scene secondScene = new Scene(secondSceneRoot, 800, 600);

            // Get the stage from the button and set the new scene
            Stage stage = (Stage) searchButton.getScene().getWindow();
            stage.setScene(secondScene);
            stage.setTitle("Search");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
