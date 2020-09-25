package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("MetalsPDFComp.fxml"));
			
			Parent root = loader.load();
		
			Scene scene = new Scene(root);

			primaryStage.setTitle("Metals PDF Compare Tool");
			primaryStage.setScene(scene);
			
			MainController mc = (MainController) loader.getController();
			mc.setStage(primaryStage);
			
			primaryStage.show();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
