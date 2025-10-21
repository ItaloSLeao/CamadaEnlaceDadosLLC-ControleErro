package controller;

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Arrays;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import model.AplicacaoTransmissora;

/**
 * Controlador principal da interface grafica (GUI) da aplicacao.
 * <p>
 * Esta classe gerencia todos os componentes visuais definidos no arquivo FXML,
 * como botoes, areas de texto e paineis de selecao. Ela tambem eh responsavel
 * por tratar os eventos gerados pelo usuario, como cliques de botao e
 * interacoes com os menus.
 *
 * @author Italo de Souza Leao (Matricula: 202410120)
 * @version 20/10/2025 (Ultima alteracao)
 * @since 02/10/2025 (Inicio)
 */
public class ControllerTelaPrincipal implements Initializable {
  @FXML //Notacao de insercao do id FXML
  private Button botaoEnviar, botaoFechar, botaoMinimizar;

  @FXML
  private TextArea msgEnviadaTextArea, bitsCodificadosTextArea, bitsEnquadradosTextArea, msgRecebidaTextArea;

  @FXML
  private ComboBox<String> comboBoxCodificacao, comboBoxEnquadramento, comboBoxErro, comboBoxControleErro;

  //Instanciacao de ImageViews para representar graficamente os sinais transmitidos
  @FXML
  private ImageView lowImagem0, lowImagem1, lowImagem2, lowImagem3, lowImagem4, lowImagem5, lowImagem6, lowImagem7,
                    lowImagem8, lowImagem9, lowImagem10, lowImagem11;

  @FXML
  private ImageView midImagem0, midImagem1, midImagem2, midImagem3, midImagem4, midImagem5, midImagem6, midImagem7,
                    midImagem8, midImagem9, midImagem10, midImagem11;

  @FXML
  private ImageView highImagem0, highImagem1, highImagem2, highImagem3, highImagem4, highImagem5, highImagem6,
                    highImagem7, highImagem8, highImagem9, highImagem10, highImagem11;

  @FXML
  private Slider sliderDeVelocidade;

  private ImageView lowImagens[], midImagens[], highImagens[];

  /**Opcoes de codificacao, de enquadramento e de probabilidade de erro na transmissao da mensagem */
  private String[] codificacao = { "Codificacao Binaria", "Codificacao Manchester", "Codificacao Manchester Diferencial" };
  private String[] enquadramento = { "Contagem de caracteres", "Insercao de bytes", "Insercao de bits", "Violacao da Camada Fisica" };
  private String[] erro = { "0%", "10%", "20%", "30%", "40%", "50%", "60%", "70%", "80%", "90%", "100%" };
  private String[] controleErro = {"Bit de paridade par", "Bit de paridade impar", "CRC", "Codigo de Hamming"};

  private int sinalAnterior = 0;
  private int milissegundos = 307;

  /**
   * Metodo invocado para inicializar o controlador apos a interface ser
   * carregada.
   * <p>
   * Este metodo configura os componentes da GUI, como preencher as ComboBoxes com
   * suas respectivas opcoes, agrupar as ImageViews em arrays para facilitar a
   * manipulacao e definir as acoes (event handlers) para os botoes e o slider.
   *
   * @param location  A localizacao para resolver caminhos relativos para o no raiz.
   * @param resources Os recursos usados para localizar o no raiz.
   */
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    lowImagens = new ImageView[] { lowImagem0, lowImagem1, lowImagem2, lowImagem3, lowImagem4, lowImagem5, lowImagem6,
      lowImagem7, lowImagem8, lowImagem9, lowImagem10, lowImagem11 }; //As imagens LOW sao vetorizadas

    midImagens = new ImageView[] { midImagem0, midImagem1, midImagem2, midImagem3, midImagem4, midImagem5, midImagem6,
      midImagem7, midImagem8, midImagem9, midImagem10, midImagem11 }; //As imagens MID sao vetorizadas

    highImagens = new ImageView[] { highImagem0, highImagem1, highImagem2, highImagem3, highImagem4, highImagem5,
      highImagem6, highImagem7, highImagem8, highImagem9, highImagem10, highImagem11 }; //As imagens HIGH sao vetorizadas

    //Insere as opcoes de codificacao, de enquadramento, de probabilidade de erro e de controle de erro nas ComboBoxes
    comboBoxCodificacao.getItems().addAll(Arrays.asList(codificacao));
    comboBoxEnquadramento.getItems().addAll(Arrays.asList(enquadramento));
    comboBoxErro.getItems().addAll(Arrays.asList(erro));
    comboBoxControleErro.getItems().addAll(Arrays.asList(controleErro));

    //Seleciona as opcoes padrao para as ComboBoxes, em tempo de execucao
    comboBoxCodificacao.setValue(codificacao[0]);
    comboBoxEnquadramento.setValue(enquadramento[0]);
    comboBoxErro.setValue(erro[0]);
    comboBoxControleErro.setValue(controleErro[0]);

    for (int i = 0; i < 12; i++) { //Torna todas as imagens dos sinais, exceto do LOW, invisiveis
      midImagens[i].setVisible(false);
      highImagens[i].setVisible(false);
    } //Fim for

    botaoEnviar.setOnAction(event -> {

      if(msgEnviadaTextArea.getText().isEmpty()){ //Se o campo de texto de envio estiver vazio

        //Cria e exibe um painel de alerta
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.getDialogPane().getStylesheets().add(
          ControllerTelaPrincipal.class.getResource("/view/styles.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.setTitle("ATENCAO");
        alert.setHeaderText(null);
        alert.setContentText("Nao eh possivel enviar uma mensagem vazia.");
        alert.showAndWait();

      } else{

        if (getCodificacao() == 1 && getEnquadramento() == 4) { //Se V. Camada Fisica && Cod. Binaria
          Alert alert = new Alert(AlertType.INFORMATION);
          alert.getDialogPane().getStylesheets().add(
            ControllerTelaPrincipal.class.getResource("/view/styles.css").toExternalForm());
          alert.getDialogPane().getStyleClass().add("dialog-pane");
          alert.setTitle("ATENCAO");
          alert.setHeaderText(null);
          alert.setContentText("Nao eh possivel violar a Camada Fisica com a Codificacao Binaria escolhida.");
          alert.showAndWait();
        } else {
          // Desabilita o botao e as combo boxes
          botaoEnviar.setDisable(true);
          comboBoxCodificacao.setDisable(true);
          comboBoxEnquadramento.setDisable(true);
          comboBoxErro.setDisable(true);
          comboBoxControleErro.setDisable(true);
          limparTextArea();
          AplicacaoTransmissora.aplicacaoTransmissora(this);
        } //Fim if-else

      } //Fim if-else msgEnviada
      
    }); //Fim setOnAction (botaoEnviar)

    botaoMinimizar.setOnAction(event -> {
      Stage stage = (Stage) botaoMinimizar.getScene().getWindow();
      stage.setIconified(true);
    }); //Fim setOnAction (botaoMinimizar)

    sliderDeVelocidade.setOnMouseDragged(event -> {
      milissegundos = 950 - 9 * (int) sliderDeVelocidade.getValue(); //950ms a 50ms de sono
    }); //Fim setOnMouseDragged

  } //Fim initialize

  /**
   * Encerra a execucao da aplicacao.
   * Este metodo eh acionado pelo botaoFechar da interface.
   */
  @FXML
  public void fechar() {
    System.exit(0); //Termina a JVM rodando com status 0
  } //Fim fechar

  /**
   * Reativa os componentes da GUI apos a conclusao de uma transmissao.
   * Garante que a atualizacao ocorra na thread da aplicacao JavaFX.
   */
  public void reativar() {
    Platform.runLater(() -> {
      botaoEnviar.setDisable(false);
      comboBoxCodificacao.setDisable(false);
      comboBoxEnquadramento.setDisable(false);
      comboBoxErro.setDisable(false);
      comboBoxControleErro.setDisable(false);
    }); //Fim runLater
  } //Fim reativar

  /**
   * Limpa as areas de texto da GUI para uma nova transmissao.
   */
  public void limparTextArea() {
    bitsCodificadosTextArea.setText("");
    bitsEnquadradosTextArea.setText("");
    msgRecebidaTextArea.setText("");
  } //Fim limparTextArea

  /**
   * Desloca a animacao dos sinais para a direita na tela.
   * <p>
   * Cria um efeito de 'onda' ao fazer com que cada imagem de sinal assuma
   * o estado de visibilidade da imagem a sua esquerda, com um laco regressivo.
   */
  public void atualizarSinais() {
    Platform.runLater(() -> {
      for (int i = 11; i >= 1; i--) {
        lowImagens[i].setVisible(lowImagens[i - 1].isVisible());
        midImagens[i].setVisible(midImagens[i - 1].isVisible());
        highImagens[i].setVisible(highImagens[i - 1].isVisible());
      } //Fim for
    }); //Fim runLater
  } //Fim atualizarSinais

  /**
   * Desenha o novo bit de sinal na primeira posicao da animacao na tela.
   * <p>
   * Define a visibilidade das imagens (LOW para 0, HIGH para 1) e tambem
   * exibe uma imagem de transicao (MID) se o novo bit for diferente do anterior.
   *
   * @param bit O bit (0 ou 1) que esta sendo transmitido e sera desenhado.
   */
  public void sinalizar(int bit) {
    Platform.runLater(() -> {
      highImagens[0].setVisible(false);
      midImagens[0].setVisible(false);
      lowImagens[0].setVisible(false);

      if (bit != sinalAnterior) {
        midImagens[1].setVisible(true);
      }

      if (bit == 0) {
        lowImagens[0].setVisible(true);
      } else {
        highImagens[0].setVisible(true);
      }

      sinalAnterior = bit;
    }); //Fim runLater
  } //Fim sinalizar

  /**
   * Adiciona texto ao painel de exibicao de bits codificados.
   *
   * @param mensagem A string a ser adicionada.
   */
  public void adicionarBitsCodificadosTextArea(String mensagem) {
    Platform.runLater(() -> {
      bitsCodificadosTextArea.setText(bitsCodificadosTextArea.getText() + mensagem);
    }); //Fim runLater
  } //Fim adicionarBitsCodificadosTextArea

  /**
   * Adiciona texto ao painel de exibicao de bits enquadrados.
   *
   * @param mensagem A string a ser adicionada.
   */
  public void adicionarBitsEnquadradosTextArea(String mensagem) {
    Platform.runLater(() -> {
      bitsEnquadradosTextArea.setText(bitsEnquadradosTextArea.getText() + mensagem);
    }); //Fim runLater
  } //Fim adicionarBitsEnquadradosTextArea

  /**
   * Adiciona texto ao painel de exibicao da mensagem recebida.
   *
   * @param mensagem A string a ser adicionada.
   */
  public void adicionarMsgRecebidaTextArea(String mensagem) {
    Platform.runLater(() -> {
      msgRecebidaTextArea.setText(msgRecebidaTextArea.getText() + mensagem);
    }); //Fim runLater
  } //Fim adicionarMsgRecebidaTextArea

  /**
   * Retorna o tipo de codificacao selecionado pelo usuario.
   *
   * @return int O valor correspondente a codificacao (1: Binaria, 2: Manchester,
   *         3: Manchester Diferencial).
   */
  public int getCodificacao() {
    if (comboBoxCodificacao.getValue().equals("Codificacao Binaria")) {
      return 1;
    } else if (comboBoxCodificacao.getValue().equals("Codificacao Manchester")) {
      return 2;
    } else { //Ao caso da opcao escolhida ser "Codificacao Manchester Diferencial"
      return 3;
    } //Fim if-else
  } //Fim getCodificacao

  /**
   * Retorna o tipo de enquadramento selecionado pelo usuario.
   *
   * @return int O valor correspondente ao enquadramento (1-4).
   */
  public int getEnquadramento() {
    if (comboBoxEnquadramento.getValue().equals("Contagem de caracteres")) {
      return 1;
    } else if (comboBoxEnquadramento.getValue().equals("Insercao de bytes")) {
      return 2;
    } else if (comboBoxEnquadramento.getValue().equals("Insercao de bits")) {
      return 3;
    } else { //Ao caso da opcao escolhida ser "Violacao da Camada Fisica"
      return 4;
    } //Fim if-else
  } //Fim getEnquadramento

  /**
   * Retorna a probabilidade de erro selecionado pelo usuario.
   *
   * @return int O valor correspondente a probabilidade de erro.
   */
  public int getErro() {
    if (comboBoxErro.getValue().equals("0%")) {
      return 0;
    } else if (comboBoxErro.getValue().equals("10%")) {
      return 10;
    } else if (comboBoxErro.getValue().equals("20%")) {
      return 20;
    } else if (comboBoxErro.getValue().equals("30%")) {
      return 30;
    } else if (comboBoxErro.getValue().equals("40%")) {
      return 40;
    } else if (comboBoxErro.getValue().equals("50%")) {
      return 50;
    } else if (comboBoxErro.getValue().equals("60%")) {
      return 60;
    } else if (comboBoxErro.getValue().equals("70%")) {
      return 70;
    } else if (comboBoxErro.getValue().equals("80%")) {
      return 80;
    } else if (comboBoxErro.getValue().equals("90%")) {
      return 90;
    } else {
      return 100;
    } //Fim if-else
  } //Fim getErro

  /**
   * Retorna o tipo de controle de erro selecionado pelo usuario.
   *
   * @return int O valor correspondente ao controle de erro (1-4).
   */
  public int getControleErro() {
    if (comboBoxControleErro.getValue().equals("Bit de paridade par")) {
      return 1;
    } else if (comboBoxControleErro.getValue().equals("Bit de paridade impar")) {
      return 2;
    } else if (comboBoxControleErro.getValue().equals("CRC")) {
      return 3;
    } else { //Ao caso da opcao escolhida ser "Codigo de Hamming"
      return 4;
    } //Fim if-else
  } //Fim getControleErro

  /**
   * Retorna a mensagem digitada pelo usuario na area de texto.
   *
   * @return String A mensagem a ser transmitida.
   */
  public String getMensagem() {
    return msgEnviadaTextArea.getText();
  } //Fim getMensagem

  /**
   * Retorna a velocidade de transmissao definida pelo slider.
   *
   * @return int O valor do delay em milissegundos para a animacao.
   */
  public int getVelocidade() {
    return milissegundos;
  } //Fim getVelocidade

  /**
   * Faz beep.
   *
   * @return void
   */
  public void beep(){
    try {
      AudioClip beep = Applet.newAudioClip(getClass().getResource("/assets/beep.wav"));
      beep.play();
    } catch (Exception e) {
      System.err.println("Excecao no beep do controller" + e.getStackTrace());
    }
  }

} //Fim da classe ControllerTelaPrincipal