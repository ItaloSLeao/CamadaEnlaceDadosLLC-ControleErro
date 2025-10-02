package model;

import java.util.ArrayList;

import controller.ControllerTelaPrincipal;
import javafx.application.Platform;
import util.Util;

/**
 * Classe que simula o funcionamento de uma camada de enlace de dados de um receptor.
 * Eh responsavel por receber os quadros, realizar o desenquadramento, verificar
 * erros e encaminhar a mensagem para a camada de aplicacao.
 *
 * @author  Italo de Souza Leao (Matricula: 202410120)
 * @version 30/09/2025 (Ultima alteracao)
 * @since   02/10/2025 (Inicio)
 */
public class CamadaEnlaceDadosReceptora {

  /**
   * Metodo principal da camada de enlace de dados receptora.
   * <p>
   * Coordena as operacoes de desenquadramento, controle de erros e
   * controle de fluxo antes de enviar a mensagem para a camada de aplicacao.
   *
   * @param quadro      Array de inteiros com os dados recebidos da camada fisica.
   * @param controller  Controlador da interface grafica.
   */
  protected static void camadaEnlaceDadosReceptora(int quadro[], ControllerTelaPrincipal controller) {

    /*Tratamento de uma excecao previsivel durante uma transmissao usando o enquadramento 
    * de contagem de caracteres. A randomicidade de erros gerada no meio pode alterar um
    * caracter de contagem, corrompendo o desenquadramento*/
    try{

      int[] quadroEnquadrado = camadaEnlaceDadosReceptoraEnquadramento(quadro, controller);
      CamadaAplicacaoReceptora.camadaAplicacaoReceptora(quadroEnquadrado, controller);

    } catch(Exception e){

      System.err.println("\nEXCECAO NA CAMADA DE ENLACE RECEPTORA --> ArrayIndexOutOfBoundsException" +
        "\nUm erro em um caracter de contagem impossibilitou o termino da transmissao");

      Platform.runLater(() -> {
        controller.adicionarMsgRecebidaTextArea("A transmissao foi interrompida em virtude de" + 
          " uma Excecao da Contagem de Caracteres");
        controller.reativar();
      }); //Fim runLater

    } //Fim try-catch

  } //Fim camadaEnlaceDadosReceptora

  /**
   * Seleciona e executa o algoritmo de desenquadramento apropriado.
   * <p>
   * Com base na escolha do usuario na interface grafica, este metodo chama a
   * implementacao especifica de desenquadramento (contagem de caracteres,
   * insercao de bytes, etc.).
   *
   * @param quadro      O fluxo de dados brutos vindo da camada fisica.
   * @param controller  Controlador da GUI para obter o tipo de enquadramento.
   * @return int[]      O quadro de dados original, ja desenquadrado.
   */
  protected static int[] camadaEnlaceDadosReceptoraEnquadramento(int quadro[], ControllerTelaPrincipal controller) {

    int tipoEnquadramento = controller.getEnquadramento(); //Captura o enquadramento escolhido na interface grafica
    int[] quadroDesenquadrado;

    switch(tipoEnquadramento){
      case 1:
        quadroDesenquadrado = camadaEnlaceDadosReceptoraEnquadramentoContagemDeCaracteres(quadro);
        break;
      case 2:
        quadroDesenquadrado = camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBytes(quadro);
        break;
      case 3:
        quadroDesenquadrado = camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBits(quadro);
        break;
      default:
        quadroDesenquadrado = camadaEnlaceDadosReceptoraEnquadramentoViolacaoCamadaFisica(quadro);
        break;
    } //Fim switch

    System.out.println("\nCAMADA DE ENLACE DE DADOS RECEPTORA ------------------");
    for(int i = 0; i < quadroDesenquadrado.length; i++){
      System.out.println("quadroDesenquadrado[" + i + "] = " + Util.bitsParaString(quadroDesenquadrado[i]));

      try{Thread.sleep(controller.getVelocidade());} 
      catch (Exception e){e.printStackTrace();}
    } //Fim for

    return quadroDesenquadrado;

  } //Fim de camadaEnlaceDadosReceptoraEnquadramento


  /**
   * Realiza o desenquadramento pelo metodo de contagem de caracteres.
   * <p>
   * Le o byte de contagem no inicio de cada quadro para determinar seu tamanho
   * e extrair a mensagem original.
   *
   * @param quadro O fluxo de dados enquadrado.
   * @return int[] A mensagem original, sem os bytes de contagem.
   */
  protected static int[] camadaEnlaceDadosReceptoraEnquadramentoContagemDeCaracteres(int quadro[]){

    //Usa um array dinamico, pois o tamanho original da mensagem eh desconhecido a priori
    ArrayList<Integer> quadroOriginalList = new ArrayList<>();

    int indiceEntrada = 0; //Controla a posicao de leitura no quadroEnquadrado

    while (indiceEntrada < quadro.length) {
      //Le o primeiro byte do quadro, que eh o byte de contagem.
      int tamanhoDoBloco = quadro[indiceEntrada++];

      int caracteresCargaUtil = tamanhoDoBloco - 1;

      //Le os caracteres da carga util e os adiciona a lista
      for (int i = 0; i < caracteresCargaUtil; i++) {
        quadroOriginalList.add(quadro[indiceEntrada++]);
      } //Fim do for
    } //Fim do while

    int[] quadroOriginal = new int[quadroOriginalList.size()];
    //Converte a lista de inteiros de volta para um int[]
    for (int i = 0; i < quadroOriginalList.size(); i++) {
      quadroOriginal[i] = quadroOriginalList.get(i);
    }

    return quadroOriginal;

  } //Fim de camadaEnlaceDadosReceptoraEnquadramentoContagemDeCaracteres


  /**
   * Realiza o desenquadramento pelo metodo de insercao de bytes (byte stuffing).
   * <p>
   * Remove as flags de inicio/fim e os bytes de escape para restaurar
   * a mensagem original.
   *
   * @param quadro O fluxo de dados enquadrado com flags e escapes.
   * @return int[] A mensagem original.
   */
  protected static int[] camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBytes(int quadro[]){

    final char FLAG = 'i';
    final char ESCAPE = '/';

    ArrayList<Integer> quadroOriginalList = new ArrayList<>();
    boolean escapeLido = false; //Flag para saber se o caractere anterior foi um ESCAPE

    for (int i = 0; i < quadro.length; i++) { //Itera sobre os bytes em quadro[]
      int caractere = quadro[i];

      if (escapeLido) {
        //Se o escape foi lido antes, o caractere atual eh um dado literal
        quadroOriginalList.add(caractere);
        escapeLido = false;
      } else { //Se o caractere anterior nao foi um escape
        if (caractere == ESCAPE) {
          escapeLido = true;
        } else if (caractere == FLAG) {
          //Ignora o caractere, descartando-o
        } else {
          quadroOriginalList.add(caractere);
        } //Fim if-else if
      } //Fim if-else escapeLido
    } //Fim for

    int[] quadroOriginal = new int[quadroOriginalList.size()];

    for (int i = 0; i < quadroOriginalList.size(); i++) {
      quadroOriginal[i] = quadroOriginalList.get(i);
    }

    return quadroOriginal;

  } //Fim camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBytes


  /**
   * Realiza o desenquadramento pelo metodo de insercao de bits (bit stuffing).
   * <p>
   * Procura pela sequencia de cinco bits '1' e remove o bit '0' inserido (stuffed)
   * para restaurar o fluxo de dados original. As flags sao usadas para
   * delimitar o quadro.
   *
   * @param quadro O fluxo de bits enquadrado.
   * @return int[] A mensagem original.
   */
  protected static int[] camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBits(int quadro[]) {

    final int FLAG = 0b01111110; //01111110
    ArrayList<Integer> quadroOriginalList = new ArrayList<>();
    ArrayList<Integer> cargaUtilBruta = new ArrayList<>(); //Lista para bytes entre as FLAGs
    boolean dentroDoQuadro = false;

    //Separa a Carga Util das FLAGs
    for (int byteEntrada : quadro) {
      if (byteEntrada == FLAG) {
        if (dentroDoQuadro) {
          //FLAG de fim de quadro encontrada. Processa o que acumulou.
          if (!cargaUtilBruta.isEmpty()) {
              //Implementacao do de-stuffing com uma funcao deStuffBits
              ArrayList<Integer> cargaUtilLimpa = deStuffBits(cargaUtilBruta);
              quadroOriginalList.addAll(cargaUtilLimpa);
              cargaUtilBruta.clear(); //Limpa para o proximo quadro
          }
          dentroDoQuadro = false; //Sai do quadro
        } else {
          //FLAG de inicio de quadro encontrada
          dentroDoQuadro = true;
        } //Fim if-else
      } else {
        if (dentroDoQuadro) {
          //Este eh um byte de dados (carga util), adiciona para processamento posterior
          cargaUtilBruta.add(byteEntrada);
        } //Fim if
      } //Fim if-else
    } //Fim for

    int[] quadroOriginal = new int[quadroOriginalList.size()];

    for (int i = 0; i < quadroOriginalList.size(); i++) {
      quadroOriginal[i] = quadroOriginalList.get(i);
    }

    return quadroOriginal;

  } //Fim camadaEnlaceDadosReceptoraInsercaoDeBits


  /**
   * Funcao auxiliar que realiza o de-stuffing em uma lista de bytes.
   * @param cargaUtilBruta A lista de bytes que compoe a carga util de um quadro.
   * @return ArrayList<Integer> A lista de bytes apos a remocao dos bits de stuffing.
   */
  private static ArrayList<Integer> deStuffBits(ArrayList<Integer> cargaUtilBruta) {
    ArrayList<Integer> bytesDecodificados = new ArrayList<>();
    int contBits1 = 0;
    int byteSaidaAtual = 0;
    int bitsNoByteSaida = 0;

    for (int byteEntrada : cargaUtilBruta) {
      for (int j = 7; j >= 0; j--) {
        int bit = (byteEntrada >> j) & 1;

        if (contBits1 == 5) {
          if (bit == 0) { //Encontrou um bit de stuffing
            contBits1 = 0;
            continue; //Descarta o bit e continua
          }
        } //Fim de if contBits1

        //Se nao for bit de stuffing, eh um bit de dados
        byteSaidaAtual = (byteSaidaAtual << 1) | bit;
        bitsNoByteSaida++;

        if (bit == 1) {
          contBits1++;
        } else {
          contBits1 = 0;
        } //Fim de if bit

        if (bitsNoByteSaida == 8) {
          bytesDecodificados.add(byteSaidaAtual);
          byteSaidaAtual = 0;
          bitsNoByteSaida = 0;
        } //Fim if
      } //Fim for bits
    } //Fim for bytes

    return bytesDecodificados;
  } //Fim deStuffBits


  /**
   * Realiza o desenquadramento pela tecnica de violacao da camada fisica.
   * <p>
   * Simplesmente recebe o vetor de inteiros desenquadrado, decodificado
   * e tambem desempacotado, com os caracteres posicionados.
   *
   * @param quadro O fluxo de dados codificado.
   * @return int[] A mensagem original.
   */
  protected static int[] camadaEnlaceDadosReceptoraEnquadramentoViolacaoCamadaFisica(int quadro[]){
    return quadro;
  } //Fim camadaEnlaceDadosReceptoraEnquadramentoViolacaoCamadaFisica


  protected static void camadaEnlaceDadosReceptoraControleDeErros(int quadro[]){
  } //Fim de camadaEnlaceDadosReceptoraControleDeErros

  
  protected static void camadaEnlaceDadosReceptoraControleDeFluxo(int quadro[]){
  } //Fim de camadaEnlaceDadosReceptoraControleDeFluxo

} //Fim da classe CamadaEnlaceDadosReceptora