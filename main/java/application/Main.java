package application;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = { "controller","controller.window","controller.functions", "service", "application" })
public class Main extends Application {

	public FXMLLoader loader;
	private static Stage stage;
	private Scene scene;
	private ConfigurableApplicationContext context;

	@Override
	public void init() throws Exception {
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			SpringApplicationBuilder builder = new SpringApplicationBuilder(Main.class);
			builder.headless(false);
			context = builder.run();
			loader = new FXMLLoader(getClass().getResource("/view/MainWindow.fxml"));			
			loader.setControllerFactory(context::getBean);
			Parent root = (Parent) loader.load();
			scene = new Scene(root);
			scene.getStylesheets().add("style/style.css");

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		primaryStage.setTitle("Produkcja");
		primaryStage.setResizable(true);
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image("/icons/Hills36.PNG"));
		
		stage = primaryStage;
		stage.show();
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent arg0) {
				Platform.exit();
				System.exit(0);	
			}
		});
	}

	public static void main(String[] args) {
		launch(args);
		
	}

	@Override
	public void stop() {
		context.close();
	}

}
