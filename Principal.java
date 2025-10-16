import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import controller.ControllerTelaPrincipal; //Controller da aplicacao

import model.MeioDeComunicacao;
import model.CamadaFisicaTransmissora;
import model.CamadaAplicacaoTransmissora;

@SuppressWarnings("unused")

/**
 * Classe principal que inicia a aplicacao JavaFX.
 *
 * @author  Italo de Souza Leao (Matricula: 202410120)
 * @version 16/10/2025 (Ultima alteracao)
 * @since   02/10/2025 (Inicio)
 */
public class Principal extends Application {
  
  /**
   * Inicia a aplicacao Java.
   *
   * @param args Os argumentos da linha de comando.
   */
  public static void main(String[] args) {
    launch(args);
  } //Fim main


  /**
   * Carrega a GUI do JavaFX a partir do FXML.
   *
   * @param primaryStage O palco principal da aplicacao.
   * @throws Exception   Erro durante o carregamento do arquivo FXML.
   */
  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("view/telaPrincipal.fxml"));
    Parent root = loader.load();
    Scene scene = new Scene(root);
    primaryStage.setScene(scene);

    primaryStage.getIcons().add(
      new Image(getClass().getResourceAsStream("/assets/app-icon.png"))
    );

    primaryStage.setTitle("ControLace de Erros: Redes De Computadores");
    primaryStage.resizableProperty().setValue(false);

    primaryStage.show();
  } //Fim start

} //Fim Principal