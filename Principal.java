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

@SuppressWarnings("unused") //Notacao de supressao de avisos

/**
 * Classe principal que inicia a aplicacao JavaFX.
 * <p>
 * Eh responsavel por carregar a interface grafica principal a partir do
 * arquivo FXML, configurar o palco (Stage) e exibir a cena para o usuario,
 * dando inicio a execucao do programa.
 *
 * @author  Italo de Souza Leao (Matricula: 202410120)
 * @version /10/2025 (Ultima alteracao)
 * @since   02/10/2025 (Inicio)
 */
public class Principal extends Application {
  
  /**
   * Ponto de entrada da aplicacao Java.
   * <p>
   * Este metodo invoca launch(args) para iniciar o toolkit JavaFX
   * e o ciclo de vida da aplicacao.
   *
   * @param args Os argumentos da linha de comando.
   */
  public static void main(String[] args) {
    launch(args); //Lanca uma aplicacao autonoma
  } //Fim main

  /**
   * Metodo principal do ciclo de vida da aplicacao JavaFX.
   * <p>
   * Este metodo eh chamado apos o launch() e eh responsavel por
   * carregar a interface a partir do arquivo FXML, configurar a cena,
   * definir o titulo e icone da janela, e finalmente exibir o palco (Stage).
   *
   * @param primaryStage O palco principal da aplicacao, fornecido pelo runtime do JavaFX.
   * @throws Exception   Se ocorrer um erro durante o carregamento do arquivo FXML.
   */
  @Override //Notacao de sobrescricao
  public void start(Stage primaryStage) throws Exception {
    //Carrega o arquivo fxml, define o no raiz, carrega o controller, define a cena e define o palco
    FXMLLoader loader = new FXMLLoader(getClass().getResource("view/telaPrincipal.fxml"));
    Parent root = loader.load();
    Scene scene = new Scene(root);
    primaryStage.setScene(scene);

    primaryStage.getIcons().add( //Instancia uma imagem para ser icone da aplicacao
      new Image(getClass().getResourceAsStream("/assets/app-icon.png"))
    );

    primaryStage.setTitle("ControLace de Erros: Redes De Computadores"); //Definicao do titulo do Stage
    primaryStage.resizableProperty().setValue(false); //Stage fica nao redimensionavel

    primaryStage.show(); //Apresentacao do Stage para o usuario
  } //Fim start
} //Fim da classe Principal