package ni.edu.uam.practicahilos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Clase principal que inicializa la aplicación JavaFX, carga la vista FXML
 * y gestiona el ciclo de vida de la ventana y de los hilos de ejecución.
 * 
 * Universidad Americana (UAM) - Programación de Aplicaciones de Escritorio
 * Práctica Guiada: Hilos en JavaFX (Thread, Runnable y Platform.runLater())
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            MainApp.class.getResource("procesador-view.fxml")
        );
        Scene scene = new Scene(loader.load());


        stage.setTitle("Práctica - Hilos en JavaFX | NicaData Solutions");
        stage.setScene(scene);
        stage.setMinWidth(650);
        stage.setMinHeight(560);
        stage.show();

        // Obtener el controlador para gestionar la detención controlada al cerrar la ventana
        ProcesadorController controller = loader.getController();
        stage.setOnCloseRequest(event -> controller.detenerAlCerrar());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
