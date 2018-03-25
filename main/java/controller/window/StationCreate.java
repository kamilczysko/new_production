package controller.window;

import java.io.IOException;

import org.springframework.stereotype.Component;

import controller.StationDefinitionController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@Component
public class StationCreate {

	private FXMLLoader loader;
	private Stage stationCreateStage;
	private Parent root;
	private StationDefinitionController stationController;
	
	public StationCreate(){
		initWindow();
	}
	
	private void initWindow(){
		stationCreateStage = new Stage();
		try {
			loader = new FXMLLoader(getClass().getResource("/view/creationViews/StationDefinitionView.fxml"));
			
			loader.setClassLoader(this.getClass().getClassLoader());
			
			root = (Parent) loader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add("style/style.css");
			stationCreateStage.setScene(scene);
			stationCreateStage.setTitle("Nowe stanowisko");
			stationCreateStage.setResizable(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		stationController = (StationDefinitionController)loader.getController();

	}
	
	public StationDefinitionController getController(){
		return stationController;
	}
	
	public void show(){
		stationCreateStage.show();
	}
	
}
