package model;

import controller.ControllerTelaPrincipal;
import util.Util;

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
      int valores[] = new int[4];
      int aux = fluxoBits[i];

      for (int j = 0; j < 4; j++) { //Laco para cada caractere do inteiro de 32 bits
        valores[j] = aux & 255; //Extrai os ultimos 8 bits (255 = 11111111 em binario)
        aux >>= 8;
      } //Fim for

      int novosBits[] = valores; //Vetor com os 4 novos inteiros (representando caracteres) extraidos

      for (int x = 0; x < 4; x++) { //Laco para alocar os inteiros decodificados no array correspondente
        decodificado[i * 4 + x] = novosBits[x];
      } //Fim for
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
    int numero;
    int bit; //Variavel mascara usada para comparar cada um dos 32 bits
    int novoBit; //Variavel usada para receber todos os bits do grupo
    
    for (int i = 0; i < fluxoBits.length; i++) { //Laco para iterar sobre cada grupo de 32 bits em fluxoBits
      numero = 0;
      bit = 1;
      novoBit = fluxoBits[i];

      for (int j = 0; j < 8; j++) { //Laco para iterar sobre cada um dos 8 pares de bits
        numero += (((novoBit & bit) != 0) ? 1 : 0) << j;
        bit <<= 2; //Avanca 2 bits a esquerda na mascara
      } //Fim for

      decodificado[i * 2] = numero;
      numero = 0; //Reinicia a variavel
      bit = 1; //Reinicia a mascara
      novoBit >>= 16;

      for (int j = 0; j < 8; j++) { //Itera sobre cada um dos 8 pares de bits do segundo caractere
        numero += ((novoBit & bit) != 0 ? 1 : 0) << j;
        bit <<= 2;
      } //Fim for

      decodificado[i * 2 + 1] = numero; //Aloca o segundo caractere decodificado, a cada dois caracteres
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

    for (int i = 0; i < fluxoBits.length; i++) { //Laco que itera sobre cada grupo de 32 bits
      Boolean ultimoSinal;
      int aux = fluxoBits[i];
      int bit = 1;
      int informacao;

      if ((aux & bit) != 0) { //Se a operacao AND no grupo de 32 bits e a mascara for diferente de 0
        informacao = 1;
        ultimoSinal = false; //O ultimo bit eh LOW(false), devido a transicao no meio do clock
      } else {
        informacao = 0;
        ultimoSinal = true; //O ultimo bit eh HIGH(true), devido a transicao no meio do clock
      } //Fim if-else

      bit <<= 2;

      for (int j = 1; j < 8; j++) { //Itera sobre cada um dos 7 pares de bits restantes do caractere

        if ((aux & bit) != 0) { //Se o bit atual for diferente 0

          if (ultimoSinal) { //Se o ultimo bit era true
            informacao |= (1 << j);
            ultimoSinal = !ultimoSinal; //Inverte o bit, pela transicao no clock
          } else { //Se o ultimo bit era false
            informacao |= (0 << j);
          } //Fim if-else

        } else { //Se o bit atual eh igual a 0

          if (ultimoSinal) { //Se o ultimo bit era true
            informacao |= 0 << j;
          } else { //Se o ultimo bit era false
            informacao |= 1 << j;
            ultimoSinal = !ultimoSinal; //Faz a troca do ultimo bit, pela transicao no clock
          } //Fim if-else

        } //Fim if-else

        bit <<= 2;

      } //Fim for

      decodificado[i * 2] = informacao;
      informacao = 0; //Reinicia a variavel de acumular bits

      for (int j = 0; j < 8; j++) { //Itera sobre os 8 pares de bits do segundo caractere do grupo de 32 bits

        if ((aux & bit) != 0) {

          if (ultimoSinal) { //Se o ultimoBit era true
            informacao |= 1 << j;
            ultimoSinal = !ultimoSinal; //Realiza a troca do sinal de ultimoBit
          } else { //Se o ultimoBit era false
            informacao |= 0 << j;
          } //Fim if-else

        } else {

          if (ultimoSinal) { //Se o ultimoBit era true (HIGH)
            informacao |= 0 << j;
          } else { //Se o ultimoBit era false (LOW)
            informacao |= 1 << j;
            ultimoSinal = !ultimoSinal; //Inverte o sinal de ultimoBit
          } //Fim if-else

        } //Fim if-else

        bit <<= 2; //Move a mascara duas posicoes a esquerda para dar continuidade a leitura

      } //Fim for

      decodificado[i * 2 + 1] = informacao;
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

    // Validacao basica: precisa ter pelo menos as duas flags (4 bits)
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