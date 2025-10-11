package model;

import controller.ControllerTelaPrincipal;
import util.Util;

import java.util.Arrays;

/**
 * Simula o funcionamento da camada fisica de um receptor em uma rede.
 * <p>
 * Esta classe eh responsavel por receber o fluxo de bits do meio de
 * comunicacao, decodifica-lo para o formato original, possivelmente
 * desenquadrar e enviar para a camada de enlace de dados receptora.
 *
 * @author  Italo de Souza Leao (Matricula: 202410120)
 * @version 30/09/2025 (Ultima alteracao)
 * @since   02/10/2025 (Inicio)
 */
public class CamadaFisicaReceptora {

  /**
   * Metodo principal da camada fisica receptora.
   * <p>
   * Recebe o fluxo de bits e seleciona o metodo de decodificacao apropriado
   * com base na escolha do usuario na interface grafica.
   *
   * @param fluxoBits   O fluxo de bits codificado recebido do meio de comunicacao.
   * @param controller  O controlador da interface grafica.
   */
  protected static void camadaFisicaReceptora(int fluxoBits[], ControllerTelaPrincipal controller) {
    try {Thread.sleep(controller.getVelocidade());} 
    catch (Exception e) {e.printStackTrace();} //Fim do try-catch

    int[] fluxoBitsDecodificar; //Fluxo de bits, possivelmente enquadrado, a ser decodificado

    if(controller.getEnquadramento() == 4){ //Se for Violacao da Cod. da Camada Fisica
      fluxoBitsDecodificar = camadaFisicaReceptoraDesenquadramentoViolacao(fluxoBits);
    } else {
      fluxoBitsDecodificar = fluxoBits;
    }

    int quadro[];
    
    switch (controller.getCodificacao()) { //Obtem a codificacao escolhida na interface grafica
      case 1: //Decodificacao Binaria
        quadro = camadaFisicaReceptoraDecodificacaoBinaria(fluxoBitsDecodificar);
        break;
      case 2: //Decodificacao Manchester
        quadro = camadaFisicaReceptoraDecodificacaoManchester(fluxoBitsDecodificar);
        break;
      default: //Decodificacao Manchester Diferencial
        quadro = camadaFisicaReceptoraDecodificacaoManchesterDiferencial(fluxoBitsDecodificar);
        break;
    } //Fim switch-case

    if(controller.getControleErro() == 3){ //Se o controle de erro for CRC
      int tamanhoSemPadding = quadro.length;

      if(quadro[tamanhoSemPadding-1] == 0){
        tamanhoSemPadding--;
      }

      quadro = Arrays.copyOf(quadro, tamanhoSemPadding);
    }

    System.out.println("\nCAMADA FISICA RECEPTORA-----------------------");
    for(int c : quadro){
      System.out.println(Util.bitsParaString(c));
    }

    CamadaEnlaceDadosReceptora.camadaEnlaceDadosReceptora(quadro, controller);
  } //Fim camadaFisicaReceptora



  /**
   * Realiza a decodificacao binaria do fluxo de bits.
   * <p>
   * Este metodo desempacota cada inteiro de 32 bits do fluxo de entrada
   * em quatro caracteres de 8 bits (representados como inteiros).
   *
   * @param fluxoBits   Fluxo de bits a ser decodificado.
   * @return int[]      Array de inteiros representando os caracteres originais.
   */
  protected static int[] camadaFisicaReceptoraDecodificacaoBinaria(int fluxoBits[]) {

    int decodificado[] = new int[fluxoBits.length * 4];

    for (int i = 0; i < fluxoBits.length; i++) {
      int aux = fluxoBits[i];

      decodificado[i*4] = (aux >> 24) & 255; //Extrai os bits de 31-24
      decodificado[i*4 + 1] = (aux >> 16) & 255; //Extrai de 23-16
      decodificado[i*4 + 2] = (aux >> 8) & 255; //Extrai de 15-8
      decodificado[i*4 + 3] = aux & 255; //Extrai de 7-0

    } //Fim for

    return decodificado;
    
  } //Fim camadaFisicaReceptoraDecodificacaoBinaria


  /**
   * Realiza a decodificacao Manchester do fluxo de bits.
   * <p>
   * Reverte a codificacao Manchester, onde cada par de bits (01 ou 10) eh
   * convertido de volta para um unico bit (0 ou 1). Cada inteiro de 32 bits
   * do fluxo de entrada eh decodificado em dois caracteres de 8 bits.
   *
   * @param fluxoBits   Fluxo de bits a ser decodificado.
   * @return int[]      Array de inteiros representando os caracteres originais.
   */
  protected static int[] camadaFisicaReceptoraDecodificacaoManchester(int fluxoBits[]) {

    int decodificado[] = new int[fluxoBits.length * 2];
    
    for (int i = 0; i < fluxoBits.length; i++) { //Laco para iterar sobre 32 bits
      int intRecebido = fluxoBits[i];
      int primeiroChar = 0;
      int segundoChar = 0;
      

      for (int j = 0; j < 8; j++) { //Laco para iterar sobre cada um dos 8 pares de bits
        int par = (intRecebido >> (30 - j*2)) & 0b11;
        primeiroChar <<= 1;
        if(par == 0b10){
          primeiroChar |= 1;
        }
      } //Fim for

      decodificado[i * 2] = primeiroChar;

      for (int j = 0; j < 8; j++) { //Itera sobre 8 pares de bits do segundo caractere
        int par = (intRecebido >> (14 - j*2)) & 0b11;
        segundoChar <<= 1;
        if(par == 0b10){
          segundoChar |= 1;
        }
      } //Fim for

      decodificado[i * 2 + 1] = segundoChar; //Aloca o segundo caractere decodificado
    } //Fim for

    return decodificado;

  } //Fim camadaFisicaReceptoraDecodificacaoManchester


  /**
   * Realiza a decodificacao Manchester Diferencial do fluxo de bits.
   * <p>
   * A decodificacao eh feita analisando a transicao de sinal no inicio de cada
   * periodo de bit em relacao ao final do periodo anterior. Uma transicao
   * representa um bit 0, e a ausencia de transicao representa um bit 1.
   *
   * @param fluxoBits   Fluxo de bits a ser decodificado.
   * @return int[]      Array de inteiros representando os caracteres originais.
   */
  protected static int[] camadaFisicaReceptoraDecodificacaoManchesterDiferencial(int fluxoBits[]) {

    int decodificado[] = new int[fluxoBits.length * 2];
    boolean ultimoSinal = true;

    for (int i = 0; i < fluxoBits.length; i++) { //Laco que itera sobre cada grupo de 32 bits
      int intRecebido = fluxoBits[i];
      int primeiroChar = 0;
      int segundoChar = 0;

      //Decodifica o primeiro caractere
      for (int j = 0; j < 8; j++) { //Loop para 8 bits
        int primeiroSinalPar = (intRecebido >> (31 - j * 2)) & 1; //Pega o primeiro sinal do par
        int segundoSinalPar = (intRecebido >> (30 - j * 2)) & 1; //Pega o segundo sinal do par

        primeiroChar <<= 1;

        if ((primeiroSinalPar == 1) == ultimoSinal) { //Sem transicao no inicio -> bit 1
          primeiroChar |= 1;
        } //Com transicao -> bit 0, nao precisa fazer nada
        ultimoSinal = (segundoSinalPar == 1); //Atualiza o estado do sinal para o proximo par
      }

      decodificado[i * 2] = primeiroChar;

      //Decodifica o segundo caractere
      for (int j = 0; j < 8; j++) { //Loop para 8 bits
        int primeiroSinalPar = (intRecebido >> (15 - j * 2)) & 1; //Pega o primeiro sinal do par
        int segundoSinalPar = (intRecebido >> (14 - j * 2)) & 1; //Pega o segundo sinal do par

        segundoChar <<= 1; //Abre espaço para o proximo bit
        if ((primeiroSinalPar == 1) == ultimoSinal) { //Sem transicao no inicio -> bit 1
          segundoChar |= 1;
        }
        ultimoSinal = (segundoSinalPar == 1); //Atualiza o estado do sinal
      }
      decodificado[i * 2 + 1] = segundoChar;
    } //Fim for

    return decodificado;

  } //Fim camadaFisicaReceptoraDecodificacaoManchesterDiferencial
  

  /**
   * Realiza o desenquadramento dos dados com a Violacao da Codificacao da Camada Fisica.
   * <p>
   * Este metodo procura pela flag de violacao (dois bits '1' consecutivos),
   * que delimita o inicio e o fim do quadro, extraindo apenas os dados contidos entre elas.
   *
   * @param fluxoBitsEnquadrado   Fluxo de bits enquadrado a ser desenquadrado.
   * @return int[]                Array de bits codificados desenquadrados.
   */
  protected static int[] camadaFisicaReceptoraDesenquadramentoViolacao(int[] fluxoBitsEnquadrado) {

    if (fluxoBitsEnquadrado == null || fluxoBitsEnquadrado.length == 0) {
      return new int[0];
    }

    //Encontra o numero total de bits significativos no fluxo de entrada
    int totalBits = 0;
    int ultimoIntComDados = -1;
    for (int i = fluxoBitsEnquadrado.length - 1; i >= 0; i--) {
      if (fluxoBitsEnquadrado[i] != 0) {
        ultimoIntComDados = i;
        break;
      }
    } //Fim for

    if (ultimoIntComDados != -1) {
      int ultimoInt = fluxoBitsEnquadrado[ultimoIntComDados];
      int posUltimoBit = 31 - Integer.numberOfLeadingZeros(ultimoInt);
      totalBits = (ultimoIntComDados * 32) + posUltimoBit + 1;
    }

    //Validacao basica: precisa ter pelo menos as duas flags (4 bits)
    if (totalBits < 4) {
      System.err.println("Erro de desenquadramento: fluxo de bits muito curto.");
      return new int[0];
    }

    //Calcula o tamanho do novo fluxo de bits (sem as flags)
    int numDataBits = totalBits - 4;
    int tamanhoArraySaida = (numDataBits + 31) / 32;
    int[] fluxoDesenquadrado = new int[tamanhoArraySaida];

    //Copia os bits de dados (pulando as flags)
    int idxEntrada = 0;
    int deslocamentoEntrada = 29; //Pula os 2 primeiros bits (pos 31 e 30)
    int idxSaida = 0;
    int deslocamentoSaida = 31;

    for (int i = 0; i < numDataBits; i++) {
      //Pega o bit da entrada
      int bit = (fluxoBitsEnquadrado[idxEntrada] >> deslocamentoEntrada) & 1;

      //Coloca o bit na saida
      fluxoDesenquadrado[idxSaida] |= bit << deslocamentoSaida;

      //Atualiza os ponteiros
      deslocamentoEntrada--;
      deslocamentoSaida--;

      if (deslocamentoEntrada < 0) {
        deslocamentoEntrada = 31;
        idxEntrada++;
      }
      if (deslocamentoSaida < 0) {
        deslocamentoSaida = 31;
        idxSaida++;
      }

    } //Fim for

    return fluxoDesenquadrado;

  } //Fim camadaFisicaReceptoraDesenquadramentoViolacao

} //Fim da classe CamadaFisicaReceptora