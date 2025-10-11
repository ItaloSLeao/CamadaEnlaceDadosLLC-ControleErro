package model;

import java.util.Arrays;

import controller.ControllerTelaPrincipal;
import util.Util;

/**
 * Simula o funcionamento da camada fisica de um transmissor em uma rede.
 * <p>
 * Esta classe recebe os quadros da camada de enlace, transforma-os em um fluxo
 * de bits de acordo com a codificacao escolhida na GUI, possivelmente enquadra
 * e envia para o meio de comunicacao.
 *
 * @author  Italo de Souza Leao (Matricula: 202410120)
 * @version 30/09/2025 (Ultima alteracao)
 * @since   02/10/2025 (Inicio)
 */
public class CamadaFisicaTransmissora {

  /**
   * Metodo principal que inicia a transmissao na camada fisica.
   * <p>
   * Seleciona o tipo de codificacao de acordo com a escolha na GUI, codifica
   * os quadros e envia o fluxo de bits resultante para o meio de comunicacao.
   *
   * @param quadro      Array de inteiros com os dados vindos da camada de enlace.
   * @param controller  Controlador da interface grafica.
   */
  protected static void camadaFisicaTransmissora(int quadro[], ControllerTelaPrincipal controller) {
    int tipoCodificacao = controller.getCodificacao();
    int[] fluxoBits;

    String codificacao = "";

    switch (tipoCodificacao) { //Seleciona o tipo de codificacao com base na escolha do usuario
      case 1: //Codificacao binaria simples
        fluxoBits = camadaFisicaTransmissoraCodificacaoBinaria(quadro, controller);
        codificacao = "Binaria";
        break;
      case 2: //Codificacao Manchester
        fluxoBits = camadaFisicaTransmissoraCodificacaoManchester(quadro, controller);
        codificacao = "Manchester";
        break;
      default: //Codificacao Manchester Diferencial
        fluxoBits = camadaFisicaTransmissoraCodificacaoManchesterDiferencial(quadro, controller);
        codificacao = "Manchester Diferencial";
        break;
    } //Fim switch

    int[] fluxoBitsFinal; //Fluxo de bits final, possivelmente enquadrado pela Camada Fisica

    if(controller.getEnquadramento() == 4){
      fluxoBitsFinal = camadaFisicaTransmissoraEnquadramentoViolacao(fluxoBits);
    } else{
      fluxoBitsFinal = fluxoBits;
    } //Fim if-else

    for(int c : fluxoBits){
      controller.adicionarBitsCodificadosTextArea(Util.bitsParaString(c));
    }

    System.out.println("\nCAMADA FISICA TRANSMISSORA ---------------------" + 
    "\nA codificacao escolhida foi: " + codificacao + "\n");
    for(int c : fluxoBitsFinal){
      if(controller.getEnquadramento() == 4){
        controller.adicionarBitsEnquadradosTextArea(Util.bitsParaString(c));
      } //Fim if
      
      System.out.println(Util.bitsParaString(c));
    } //Fim for

    MeioDeComunicacao.meioDeComunicacao(fluxoBitsFinal, controller);
  } //Fim camadaFisicaTransmissora



  /**
   * Realiza a codificacao binaria dos dados.
   * <p>
   * Neste metodo, quatro caracteres de 8 bits (representados como inteiros)
   * sao empacotados em um unico inteiro de 32 bits para transmissao.
   *
   * @param quadro      Vetor com os caracteres a serem codificados.
   * @param controller  Controlador da interface grafica.
   * @return int[]      Array de inteiros onde cada elemento representa um bloco de 32 bits codificados.
   */
  protected static int[] camadaFisicaTransmissoraCodificacaoBinaria(int quadro[], ControllerTelaPrincipal controller) {

    int tamanho = (quadro.length - 1) / 4 + 1; //Calcula o tamanho necessario para o array de bits codificados
    int codificado[] = new int[tamanho];

    for (int i = 0; i < codificado.length; i++) { //Laco para gerar cada bloco codificado
      int informacao = 0; //Variavel inteira que vai empacotar os 32 bits

      informacao |= quadro[i * 4] << 24; //Armazena de 4 em 4

      if (i * 4 + 1 < quadro.length) {informacao |= (quadro[i * 4 + 1] << 16);} //Segundos 8 bits (posicoes 23-16)
      if (i * 4 + 2 < quadro.length) {informacao |= (quadro[i * 4 + 2] << 8);} //Terceiros 8 bits (posicoes 15-8)
      if (i * 4 + 3 < quadro.length) {informacao |= (quadro[i * 4 + 3]);} //Quartos 8 bits (posicoes 7-0)
      
      codificado[i] = informacao; //Armazena os bits acumulados de 4 caracteres no vetor

      try {Thread.sleep(controller.getVelocidade());} 
      catch (Exception e) {e.printStackTrace();}
    } //Fim for

    return codificado;

  } //Fim camadaFisicaTransmissoraCodificaoBinaria


  /**
   * Realiza a codificacao Manchester dos dados.
   * <p>
   * Cada bit eh transformado em dois: o bit 0 vira 01 e o bit 1 vira 10.
   * Por isso, cada caractere de 8 bits passa a ocupar 16 bits. Dois caracteres
   * codificados sao empacotados em um inteiro de 32 bits.
   *
   * @param quadro      Vetor com os caracteres a serem codificados.
   * @param controller  Controlador da interface grafica.
   * @return int[]      Array de inteiros com os bits codificados.
   */
  protected static int[] camadaFisicaTransmissoraCodificacaoManchester(int quadro[], ControllerTelaPrincipal controller) {

    int tamanho = (quadro.length + 1) / 2;
    int codificado[] = new int[tamanho];
    int informacao;

    for (int i = 0; i < tamanho; i++) { //Laco para processar os caracteres de entrada em pares
      informacao = 0; //A variavel eh zerada para ser preenchida

      //Laco que ira iterar sobre os 8 bits do primeiro caractere do par envolvido
      for (int j = 7; j >= 0; j--) {
        //Manchester codifica cada bit como 0 -> 01 e 1 -> 10
        int bit = (quadro[i*2] >> j) & 1;
        int par = (bit == 1) ? 0b10 : 0b01;
        informacao = (informacao << 2) | par;
      } //Fim for

      if (i * 2 + 1 < quadro.length) { //Verifica se ha proximo caractere para leitura
        for (int j = 7; j >= 0; j--) {
          int bit = (quadro[i*2 + 1] >> j) & 1;
          int par = (bit == 1) ? 0b10 : 0b01;
          informacao = (informacao << 2) | par;
        } //Fim for
      } //Fim if

      codificado[i] = informacao;
    } //Fim for

    return codificado;

  } //Fim camadaFisicaTransmissoraCodificacaoManchester


  /**
   * Realiza a codificacao Manchester Diferencial dos dados.
   * <p>
   * A codificacao depende do bit anterior. Um bit '0' causa uma transicao
   * no inicio do periodo, enquanto um bit '1' mantem o nivel de sinal do bit anterior.
   * Uma transicao sempre ocorre no meio do periodo para manter o clock.
   *
   * @param quadro      Vetor com os caracteres a serem codificados.
   * @param controller  Controlador da interface grafica.
   * @return int[]      Array de inteiros com os bits codificados.
   */
  protected static int[] camadaFisicaTransmissoraCodificacaoManchesterDiferencial(int quadro[], ControllerTelaPrincipal controller) {

    int tamanho = (quadro.length + 1) / 2;
    int codificado[] = new int[tamanho];
    int informacao;

    Boolean ultimoSinal = true; //Armazena o estado do ultimo bit, isto eh, o diferencial dessa codificacao

    for (int i = 0; i < tamanho; i++) {
      informacao = 0; //O bloco eh zerado para ser preenchido

      for (int k = 0; k < 2; k++) { //Processa os dois caracteres do par
        if (i * 2 + k >= quadro.length) continue; //Pula se nao houver segundo caractere

        int caractere = quadro[i * 2 + k];

        for (int j = 7; j >= 0; j--) { //Loop processa do bit 7 ao 0
          int bit = (caractere >> j) & 1; //Isola o bit da posição 'j'
          int primeiroSinal, segundoSinal;

          if (bit == 0) { //Bit 0: transição no inicio do periodo
            primeiroSinal = ultimoSinal ? 0 : 1;
            segundoSinal = ultimoSinal ? 1 : 0;
          } else { //Bit 1: sem transicao no inicio do periodo
            primeiroSinal = ultimoSinal ? 1 : 0;
            segundoSinal = ultimoSinal ? 0 : 1;
          }
          
          informacao <<= 2; //Abre espaço para os proximos 2 bits
          informacao |= (primeiroSinal << 1) | segundoSinal; //Adiciona o par de bits codificados
          ultimoSinal = (segundoSinal == 1); //Atualiza o ultimo sinal para o proximo bit
        } //Fim for
    } //Fim for k

      codificado[i] = informacao;
    } //Fim for

    return codificado;

  } //Fim camadaFisicaTransmissoraCodificacaoManchesterDiferencial


  /**
   * Realiza o enquadramento dos dados com a Violacao da Codificacao da Camada Fisica.
   * <p>
   * Este metodo insere uma flag de violacao (dois bits '1' consecutivos)
   * no inicio e no fim do fluxo de bits para delimitar o quadro.
   *
   * @param fluxoBrutoDeBits      Vetor com os bits codificados.
   * @return int[]                Array de inteiros com os bits codificados enquadrados.
   */
  protected static int[] camadaFisicaTransmissoraEnquadramentoViolacao(int[] fluxoBrutoDeBits) {

    //Variaveis para controlar a posicao bit a bit nos arrays de inteiros
    int deslocamentoFluxoBruto = 31; //Posicao do bit (31 a 0) no int de entrada
    int deslocamentoFluxoEnquadrado = 31; //Posicao do bit (31 a 0) no int de saida
    int idxFluxoBruto = 0; //Indice do array de int de entrada
    int idxFluxoEnquadrado = 0; //Indice do array de int de saida
    int bit; //Armazena o bit lido
    int proximosBits; //Verificacao de padding

    //Array temporario com espaco extra para as flags e possivel expansao
    int[] fluxoEnquadradoTemp = new int[fluxoBrutoDeBits.length * 2];

    //Insere a flag de violacao (11) no inicio do quadro
    fluxoEnquadradoTemp[idxFluxoEnquadrado] |= 1 << deslocamentoFluxoEnquadrado--;
    fluxoEnquadradoTemp[idxFluxoEnquadrado] |= 1 << deslocamentoFluxoEnquadrado--;

    //Itera por cada bit do fluxo de entrada
    for (int i = 0; i < fluxoBrutoDeBits.length * 32; i++) {
      //Avanca para o proximo int do array de entrada se necessario
      if (deslocamentoFluxoBruto < 0) {
        idxFluxoBruto++;
        deslocamentoFluxoBruto = 31;
      }

      //Encerra se todos os ints de entrada foram lidos
      if(idxFluxoBruto >= fluxoBrutoDeBits.length) break;

      //A cada dois bits, verifica se o restante do fluxo eh apenas padding (00000000)
      if (i % 2 == 0) {
        if(deslocamentoFluxoBruto - 1 < 0) continue; //Pula se nao ha dois bits para ler
        int bit1 = Math.abs((fluxoBrutoDeBits[idxFluxoBruto] & (1 << deslocamentoFluxoBruto)) >> deslocamentoFluxoBruto);
        int bit2 = Math.abs((fluxoBrutoDeBits[idxFluxoBruto] & (1 << deslocamentoFluxoBruto - 1)) >> deslocamentoFluxoBruto - 1);
        proximosBits = bit1 + bit2;
        //Se encontrou um par de bits '00'
        if (proximosBits == 0 && bit1 == 0 && bit2 == 0) {
          boolean fimReal = true;
          //Verifica se todos os bits restantes em todo o array sao zero
          for(int k=idxFluxoBruto; k<fluxoBrutoDeBits.length; k++){
            if(fluxoBrutoDeBits[k] != 0) fimReal = false;
          }
          //Se for, encerra o loop para evitar processar zeros desnecessarios
          if(fimReal) break;
        } //Fim if
      } //Fim if

      //Extrai o bit atual da entrada e o copia para a saida
      bit = (fluxoBrutoDeBits[idxFluxoBruto] & (1 << deslocamentoFluxoBruto)) >> deslocamentoFluxoBruto;
      bit = Math.abs(bit);
      fluxoEnquadradoTemp[idxFluxoEnquadrado] |= (bit) << deslocamentoFluxoEnquadrado;

      deslocamentoFluxoBruto--;
      deslocamentoFluxoEnquadrado--;

      //Avanca para o proximo int do array de saida se necessario
      if (deslocamentoFluxoEnquadrado < 0) {
        idxFluxoEnquadrado++;
        if(idxFluxoEnquadrado >= fluxoEnquadradoTemp.length) break; //Evita estourar o array temporario
        deslocamentoFluxoEnquadrado = 31;
      }
    } //Fim for 

    //Garante alinhamento para inserir a flag final
    if(deslocamentoFluxoEnquadrado % 2 == 0) {
      deslocamentoFluxoEnquadrado--;
    }
    
    //Avanca para o proximo int se nao houver espaco para a flag
    if(deslocamentoFluxoEnquadrado < 1) {
      idxFluxoEnquadrado++;
      deslocamentoFluxoEnquadrado = 31;
    }
    //Insere a flag de violacao (11) no final do quadro
    fluxoEnquadradoTemp[idxFluxoEnquadrado] |= 1 << deslocamentoFluxoEnquadrado--;
    fluxoEnquadradoTemp[idxFluxoEnquadrado] |= 1 << deslocamentoFluxoEnquadrado--;

    //Remocao de espacos nao utilizados
    int tamanhoFinal = (deslocamentoFluxoEnquadrado < 31) ? idxFluxoEnquadrado + 1 : idxFluxoEnquadrado;
    if (tamanhoFinal == 0 && fluxoBrutoDeBits.length > 0) tamanhoFinal = 1;

    int[] fluxoBitsFinal = Arrays.copyOf(fluxoEnquadradoTemp, tamanhoFinal);

    return fluxoBitsFinal;

  } //Fim camadaFisicaTransmissoraEnquadramentoViolacao

} //Fim da classe CamadaFisicaTransmissora