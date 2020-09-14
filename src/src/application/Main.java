package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("SK3Browser.fxml"));
		Scene scene = new Scene(root);

		primaryStage.setTitle("SK3 Browser");
		primaryStage.setScene(scene);
		primaryStage.show();

	}

}
