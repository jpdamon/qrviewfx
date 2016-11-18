package com.jeffdamon.qrviewfx.ui;

import com.google.zxing.NotFoundException;
import com.jeffdamon.qrviewfx.ImageFileFilter;
import com.jeffdamon.qrviewfx.QRCodeScanner;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends Application {

    private List<String> imageFiles = new ArrayList<>();

    private final TableView<QRCode> table = new TableView<>();
    private ListView<QRCode> encounterListView = new ListView<>();
    private final ObservableList<QRCode> qrCodes = FXCollections.observableArrayList();

    /** Copy of table & encounters for use during a scan. Necessary so we don't manipulate FX collections in separate thread **/
    private final List<QRCode> encounterWorkingCopy = new ArrayList<>();
    private final List<QRCode> qrCodesWorkingCopy = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception{
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Scan mode
        ToggleGroup scanModeGroup = new ToggleGroup();
        Label scanModeLabel = new Label("Scan Mode:");
        RadioButton rbSpeed = new RadioButton("Performance - faster scanning (recommended)");
        rbSpeed.setSelected(true);
        rbSpeed.setToggleGroup(scanModeGroup);
        RadioButton rbAccuracy = new RadioButton("Accuracy - spend more time attempting to detect codes");
        rbAccuracy.setToggleGroup(scanModeGroup);
        VBox scanModeBox = new VBox();
        scanModeBox.setSpacing(5);
        scanModeBox.getChildren().addAll(scanModeLabel, rbSpeed, rbAccuracy);
        grid.add(scanModeBox, 0, 1, 2, 1);

        // Images Found text
        Label imagesLabel = new Label();
        imagesLabel.setVisible(false);
        HBox imagesBox = new HBox();
        imagesBox.setAlignment(Pos.CENTER_LEFT);
        imagesBox.getChildren().add(imagesLabel);
        grid.add(imagesBox, 1, 2);

        // Start Scan button
        Button startButton = new Button();
        startButton.setText("Start Scan");
        startButton.setOnAction(event ->{
            encounterWorkingCopy.clear();
            qrCodesWorkingCopy.clear();

            ProgressDialog progressDialog = new ProgressDialog();

            Task<Void> task = new Task<Void>(){
                @Override
                public Void call(){
                    QRCodeScanner qrScanner = new QRCodeScanner();
                    long processed = 0;
                    for(String file : imageFiles){
                        try{
                            String text = qrScanner.decode(file, rbAccuracy.isSelected());
                            addQRCode(text);
                            System.out.println(text);
                        } catch(NotFoundException e){
                            // no qr code in image
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            updateProgress(++processed, imageFiles.size());
                        }
                    }
                    return null;
                }
            };

            progressDialog.activateProgressBar(task);
            task.setOnSucceeded(e ->{
                progressDialog.getDialogStage().close();
                startButton.setDisable(false);

                // Copy working lists to active
                table.getItems().clear();
                table.getItems().addAll(qrCodesWorkingCopy);
                encounterListView.getItems().clear();
                encounterListView.getItems().addAll(encounterWorkingCopy);

                // Refresh table and encounter list
                table.setVisible(false);
                table.setVisible(true);
                encounterListView.setVisible(false);
                encounterListView.setVisible(true);
            });

            startButton.setDisable(true);
            progressDialog.getDialogStage().show();
            Thread thread = new Thread(task);
            thread.start();
        });
        grid.add(startButton, 0, 2);

        // Directory Chooser
        Label folderLabel = new Label("Images Folder:");
        grid.add(folderLabel, 0, 0);
        TextField folderField = new TextField();
        folderField.setPrefWidth(300);
        grid.add(folderField, 1, 0);
        Button browseBtn = new Button();
        browseBtn.setText("Browse");
        browseBtn.setOnAction(event ->{
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(primaryStage);

            if(selectedDirectory != null){
                folderField.setText(selectedDirectory.getAbsolutePath());
                File[] images = selectedDirectory.listFiles(new ImageFileFilter());
                if(images == null){
                    imageFiles.clear();
                }
                else{
                    imageFiles = Arrays.stream(images).map(File::getAbsolutePath).collect(Collectors.toList());
                }

                imagesLabel.setText(String.valueOf(imageFiles.size()) + " images found");
                if(imageFiles.size() > 0){
                    imagesLabel.setTextFill(Color.FORESTGREEN);
                } else{
                    imagesLabel.setTextFill(Color.ORANGERED);
                }
                imagesLabel.setVisible(true);
            }
        });
        grid.add(browseBtn, 2, 0);



        // Alias table
        Label aliasLabel = new Label("QR Codes Found:");
        grid.add(aliasLabel, 0, 3);
        initEditableQRTable();
        grid.add(table, 0, 4, 3, 1);


        // QR Code encounter list
        Label encounterLabel = new Label("Encounter Order:");
        grid.add(encounterLabel, 0, 5);
        encounterListView.setItems(FXCollections.observableArrayList());
        grid.add(encounterListView, 0, 6, 3, 1);

        Scene scene = new Scene(grid, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("QRViewFX");
        primaryStage.show();
    }

    /** Add a QR code to encounter list and to the table, incrementing times seen **/
    private void addQRCode(String text) {
        QRCode newCode = null;

        // Increment times seen if we've seen this code before
        for(QRCode qr : qrCodesWorkingCopy){
            if(qr.getQRCodeText().equals(text)){
                newCode = qr;
                int timesSeen = Integer.valueOf(newCode.getTimesSeen()) + 1;
                newCode.setTimesSeen(String.valueOf(timesSeen));
                break;
            }
        }

        // Otherwise, just add it to table
        if(newCode == null){
            newCode = new QRCode(text, null, "1");
            qrCodesWorkingCopy.add(newCode);
        }

        // And append the code to the 'encounter list'
        // We must use the same QRCode object in both encounterList and table
        // that way changes to the alias are reflected.
        encounterWorkingCopy.add(newCode);
    }

    private void initEditableQRTable() {
        table.setEditable(true);
        Callback<TableColumn<QRCode, String>,
                TableCell<QRCode, String>> cellFactory = (TableColumn<QRCode, String> p) -> new EditingCell();

        TableColumn<QRCode, String> qrCodeCol = new TableColumn<>("QR Code Text");
        TableColumn<QRCode, String> aliasCol = new TableColumn<>("Alias");
        TableColumn<QRCode, String> timesCol = new TableColumn<>("Times Seen");

        qrCodeCol.setCellValueFactory(new PropertyValueFactory<>("QRCodeText"));
        qrCodeCol.setCellFactory(cellFactory);
        qrCodeCol.setOnEditCommit(
                t -> t.getTableView().getItems().get(
                        t.getTablePosition().getRow()).setQRCodeText(t.getNewValue())
        );
        qrCodeCol.setEditable(false);

        aliasCol.setCellValueFactory(new PropertyValueFactory<>("alias"));
        aliasCol.setCellFactory(cellFactory);
        aliasCol.setOnEditCommit(
                t -> t.getTableView().getItems().get(t.getTablePosition().getRow()).setAlias(t.getNewValue())
        );

        timesCol.setCellValueFactory(new PropertyValueFactory<>("timesSeen"));
        timesCol.setCellFactory(cellFactory);
        timesCol.setOnEditCommit(
                t -> t.getTableView().getItems().get(t.getTablePosition().getRow()).setTimesSeen(t.getNewValue())
        );
        timesCol.setEditable(false);

        table.setItems(qrCodes);
        table.getColumns().addAll(qrCodeCol, aliasCol, timesCol);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
