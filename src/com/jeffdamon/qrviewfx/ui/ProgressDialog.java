package com.jeffdamon.qrviewfx.ui;

import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProgressDialog {
    private final Stage stage;
    private final ProgressBar bar = new ProgressBar();
    private final ProgressIndicator indicator = new ProgressIndicator();

    public ProgressDialog(){
        stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);

        final Label label = new Label("Scanning Images...");
        bar.setProgress(-1F);
        indicator.setProgress(-1F);
        final HBox hb = new HBox();
        hb.setSpacing(5);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().addAll(label, bar, indicator);

        Scene scene = new Scene(hb);
        stage.setScene(scene);
    }

    public void activateProgressBar(final Task<?> task)  {
        bar.progressProperty().bind(task.progressProperty());
        indicator.progressProperty().bind(task.progressProperty());
        stage.show();
    }

    public Stage getDialogStage() {
        return stage;
    }
}
