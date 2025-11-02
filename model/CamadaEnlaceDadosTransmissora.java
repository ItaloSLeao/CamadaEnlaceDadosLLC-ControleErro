package model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import controller.ControllerTelaPrincipal;
import util.Util;

/**
 * Classe que simula o funcionamento de uma camada de enlace de dados de um transmissor,
 * em uma rede de computadores, gerenciando em quadros a mensagem transmitida e tratando
 * erros (este ultimo, ainda nao implementado).
 *
 * @author  Italo de Souza Leao (Matricula: 202410120)
 * @version 16/10/2025 (Ultima alteracao)
 * @since   02/10/2025 (Inicio)
 */
public class CamadaEnlaceDadosTransmissora {

  /**
   * Realiza as funcionalidades da camada de enlace de dados transmissora.
   * <p>
   * Este metodo eh responsavel por aplicar o enquadramento aos dados recebidos.
   * Atualmente, as funcionalidades de controle de erros e controle de fluxo
   * ainda nao foram implementadas.
   *
   * @param quadro     Array de inteiros contendo os codigos ASCII dos caracteres.
   * @param controller Controlador da interface grafica para interacoes com a UI.
   */
  protected static void camadaEnlaceDadosTransmissora(int quadro[], ControllerTelaPrincipal controller) {

    System.out.println("\nCAMADA DE ENLACE DE DADOS TRANSMISSORA--------------\n");

    int[] quadroEnquadrado = camadaEnlaceDadosTransmissoraEnquadramento(quadro, controller);

    int[] quadroControleErro = camadaEnlaceDadosTransmissoraControleDeErros(quadroEnquadrado, controller);

    CamadaFisicaTransmissora.camadaFisicaTransmissora(quadroControleErro, controller);

  } //Fim camadaEnlaceDadosTransmissora




  /**
   * Enquadra os quadros de caracteres recebidos da camada de aplicacao, conforme
   * o enquadramento escolhido na GUI.
   *
   * @param quadro     Array de inteiros com os codigos ascii dos caracteres.
   * @param controller Controlador da interface grafica.
   * @return int[] O resultado do enquadramento.
   */
  private static int[] camadaEnlaceDadosTransmissoraEnquadramento(int quadro[], ControllerTelaPrincipal controller) {

    int tipoEnquadramento = controller.getEnquadramento(); //Captura o enquadramento escolhido
    int[] quadroEnquadrado;

    String enquadramento = "";

    switch (tipoEnquadramento) {
      case 1:
        quadroEnquadrado = camadaEnlaceDadosTransmissoraEnquadramentoContagemDeCaracteres(quadro);
        enquadramento = "Contagem de Caracteres";
        break;
      case 2:
        quadroEnquadrado = camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBytes(quadro);
        enquadramento = "Insercao de Bytes";
        break;
      case 3:
        quadroEnquadrado = camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBits(quadro);
        enquadramento = "Insercao de Bits";
        break;
      default:
        quadroEnquadrado = camadaEnlaceDadosTransmissoraEnquadramentoViolacaoCamadaFisica(quadro);
        enquadramento = "Violacao da Camada Fisica";
        break;
    } //Fim switch

    System.out.println("\nO enquadramento escolhido foi: " + enquadramento + "\n");

    for(int i = 0; i < quadroEnquadrado.length; i++){

      if(!(tipoEnquadramento == 4)){
        controller.adicionarBitsEnquadradosTextArea(Util.bitsParaString(quadroEnquadrado[i]) + "\n");
        System.out.println("quadroEnquadrado[" + i + "] = " + Util.bitsParaString(quadroEnquadrado[i]));
      }
      
      try{Thread.sleep(controller.getVelocidade());} 
      catch (Exception e){e.printStackTrace();}

    } //Fim for

    return quadroEnquadrado;

  } //Fim camadaEnlaceDadosTransmissoraEnquadramento



  /**
   * Realiza o enquadramento pelo metodo de contagem de caracteres.
   * <p>
   * Cada quadro da mensagem tem por cabecalho um caracter de contagem de bytes,
   * que devem segui-lo na transmissao, e que delimita o final do quadro.
   *
   * @param quadro Array de inteiros com os codigos ascii dos caracteres.
   * @return int[] O resultado do enquadramento de contagem de caracteres.
   */
  private static int[] camadaEnlaceDadosTransmissoraEnquadramentoContagemDeCaracteres(int quadro[]) {

    final int tamanhoQuadro = 3;
    int numCaracteres = quadro.length;

    //Calcula quantos blocos de carga util serao necessarios para enquadrar a mensagem em quadro[]
    int numBlocos = (int) Math.ceil(numCaracteres / (double) tamanhoQuadro);

    //O vetor enquadrado de saida tera os caracteres de carga util + 1 de controle para cada bloco
    int tamanhoSaida = numCaracteres + numBlocos;
    int[] quadroEnquadrado = new int[tamanhoSaida];

    int indiceSaida = 0; //Indice para controlar o armazenamento dos caracteres no novo quadro

    for (int i = 0; i < numCaracteres; i += tamanhoQuadro) { //Faz o enquadramento
      //Calcula quantos caracteres ha no quadro
      int charsQuadro = Math.min(tamanhoQuadro, numCaracteres - i);

      quadroEnquadrado[indiceSaida++] = charsQuadro + 1;

      for (int j = 0; j < charsQuadro; j++) { //Adiciona a carga util logo apos o char de contagem
        quadroEnquadrado[indiceSaida++] = quadro[i + j];
      } //Fim do for
    } //Fim do for de quadro[]

    return quadroEnquadrado;

  } //Fim camadaEnlaceDadosTransmissoraEnquadramentoContagemDeCaracteres



  /**
   * Realiza o enquadramento pelo metodo de insercao de bytes (byte stuffing).
   * <p>
   * Cada quadro da mensagem comeca e termina com bytes especiais (flags). Esse
   * metodo insere bytes de escape para diferenciar os dados das flags,
   * delimitando os quadros de forma segura.
   *
   * @param quadro Array de inteiros com os codigos ascii dos caracteres.
   * @return int[] O resultado do enquadramento por insercao de bytes.
   */
  private static int[] camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBytes(int quadro[]) {

    final int TAMANHO_CARGA_UTIL = 5; //Numero fixo maximo de caracteres por quadro de carga util
    final char FLAG = 'i';
    final char ESCAPE = '/';

    //Calcula o pior caso de insercao de flags e escapes e aloca o vetor
    int tamanho = 2 + (2*quadro.length) + (2*quadro.length/TAMANHO_CARGA_UTIL);
    int[] quadroEnquadrado = new int[tamanho];

    int indiceSaida = 0; //Indice para controlar a insercao de bytes
    int contCargaUtil = 0; //Contagem de caracteres de carga no quadro atual

    quadroEnquadrado[indiceSaida++] = FLAG; //FLAG inicial

    for (int i = 0; i < quadro.length; i++) {
      int caractere = quadro[i];

      //Se o caractere for FLAG ou ESCAPE, insere ESCAPE antes
      if (caractere == FLAG || caractere == ESCAPE) {
        quadroEnquadrado[indiceSaida++] = ESCAPE;
      } //Fim if

      //Insere o caractere i da mensagem original
      quadroEnquadrado[indiceSaida++] = caractere;
      contCargaUtil++;

      //Se atingiu o tamanho maximo, fecha o quadro com FLAG
      if (contCargaUtil >= TAMANHO_CARGA_UTIL) {
        quadroEnquadrado[indiceSaida++] = FLAG;
        contCargaUtil = 0;

        //Se houver mais caracteres, insere FLAG inicial para o proximo quadro
        if (i + 1 < quadro.length) {
          quadroEnquadrado[indiceSaida++] = FLAG;
        } //Fim if
      } //Fim if TAMANHO_CARGA_UTIL
    } //Fim for

    //Fecha o ultimo quadro, caso ainda nao esteja fechado
    if (contCargaUtil > 0) {
      quadroEnquadrado[indiceSaida++] = FLAG;
    }

    //Redimensiona o array, eliminando os espacos alocados nao usados
    quadroEnquadrado = Arrays.copyOf(quadroEnquadrado, indiceSaida);

    return quadroEnquadrado;

  } //Fim camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBytes



  /**
   * Realiza o enquadramento pelo metodo de insercao de bits (bit stuffing).
   * <p>
   * Cada quadro eh delimitado pela flag (01111110). Para evitar que essa
   * sequencia de bits apareca nos dados, este metodo insere um bit 0 apos
   * toda sequencia de cinco bits 1 consecutivos nos dados.
   *
   * @param quadro Array de inteiros com os codigos ascii dos caracteres.
   * @return int[] O resultado do enquadramento por insercao de bits.
   */
  private static int[] camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBits(int quadro[]) {

    final int FLAG = 0b01111110; //01111110
    final int TAMANHO_CARGA_UTIL = 5; //Maximo de 5 bytes de dados por quadro

    ArrayList<Integer> quadroEnquadradoList = new ArrayList<>();
    quadroEnquadradoList.add(FLAG);

    int contBits1 = 0;
    int byteSaidaAtual = 0;
    int bitsNoByteSaida = 0;
    int contCargaUtil = 0; //Contador de bytes de carga util no quadro atual

    for (int caractereEntrada : quadro) {
      //Processa bit a bit para fazer o stuffing
      for (int i = 7; i >= 0; i--) {
        int bit = (caractereEntrada >> i) & 1;

        if (bit == 1) {
          contBits1++;
        } else {
          contBits1 = 0;
        } //Fim if-else

        //Adiciona o bit lido ao byte util sendo construido
        byteSaidaAtual = (byteSaidaAtual << 1) | bit;
        bitsNoByteSaida++;

        if (bitsNoByteSaida == 8) {
          quadroEnquadradoList.add(byteSaidaAtual);
          byteSaidaAtual = 0;
          bitsNoByteSaida = 0;
        }

        if (contBits1 == 5) {
          byteSaidaAtual = (byteSaidaAtual << 1) | 0; //Stuffing com bit 0
          bitsNoByteSaida++;
          if (bitsNoByteSaida == 8) { //Se o bit stuff completou um byte util
            quadroEnquadradoList.add(byteSaidaAtual);
            byteSaidaAtual = 0;
            bitsNoByteSaida = 0;
          }
          contBits1 = 0;
        }
      } //Fim for

      contCargaUtil++; //Incrementa o contador de bytes de carga util processados

      //Verifica se o quadro de carga util atingiu o tamanho maximo
      if (contCargaUtil >= TAMANHO_CARGA_UTIL) {
        //Se houver bits restantes no byte de saida, completa com padding e envia
        if (bitsNoByteSaida > 0) {
          byteSaidaAtual = byteSaidaAtual << (8 - bitsNoByteSaida);
          quadroEnquadradoList.add(byteSaidaAtual);
          byteSaidaAtual = 0;
          bitsNoByteSaida = 0;
        }

        quadroEnquadradoList.add(FLAG); //Fecha o quadro atual
        contCargaUtil = 0; //Zera o contador para o proximo quadro

        if (caractereEntrada != quadro[quadro.length - 1]) {
          quadroEnquadradoList.add(FLAG);
        }
      } //Fim if
    } //Fim for

    //Se o ultimo quadro nao foi fechado pela logica de tamanho
    if (contCargaUtil > 0) {
        //Se houver bits restantes no byte de saida, completa com padding e envia
        if (bitsNoByteSaida > 0) {
          byteSaidaAtual = byteSaidaAtual << (8 - bitsNoByteSaida);
          quadroEnquadradoList.add(byteSaidaAtual);
        }
        quadroEnquadradoList.add(FLAG); //Adiciona a FLAG final
    }

    int[] quadroEnquadrado = new int[quadroEnquadradoList.size()];

    for (int i = 0; i < quadroEnquadradoList.size(); i++) {
      quadroEnquadrado[i] = quadroEnquadradoList.get(i);
    }
    
    return quadroEnquadrado;

  } //Fim camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBits



  /**
   * Realiza o enquadramento pela tecnica de violacao da camada fisica.
   * <p>
   * Simplesmente manda o mesmo fluxo de caracteres que recebeu, para
   * a camada fisica fazer o devido enquadramento.
   *
   * @param quadro Vetor de inteiros contendo os caracteres da mensagem.
   * @return int[] O proprio vetor quadro[].
   */
  private static int[] camadaEnlaceDadosTransmissoraEnquadramentoViolacaoCamadaFisica(int quadro[]) {
    return quadro;
  } //Fim camadaEnlaceDadosTransmissoraEnquadramentoViolacaoCamadaFisica


  

  /**
   * Realiza o controle de erros com os quadros de caracteres enquadrados recebidos,
   * conforme a escolha na GUI.
   *
   * @param quadro     Vetor com os caracteres da mensagem enquadrados.
   * @param controller Controlador da interface grafica.
   * @return int[]     O resultado do controle de erros.
   */
  private static int[] camadaEnlaceDadosTransmissoraControleDeErros(int quadro[], ControllerTelaPrincipal controller) {

    int tipoControleErros = controller.getControleErro(); //Captura o controle de erros escolhido
    int[] quadroControleErros;

    String controleErros = "";

    switch (tipoControleErros) {
      case 1:
        quadroControleErros = camadaEnlaceDadosTransmissoraControleDeErrosBitParidadePar(quadro);
        controleErros = "Bit de paridade par";
        break;
      case 2:
        quadroControleErros = camadaEnlaceDadosTransmissoraControleDeErrosBitParidadeImpar(quadro);
        controleErros = "Bit de paridade impar";
        break;
      case 3:
        quadroControleErros = camadaEnlaceDadosTransmissoraControleDeErrosCRC(quadro);
        controleErros = "CRC";
        break;
      default:
        quadroControleErros = camadaEnlaceDadosTransmissoraControleDeErrosCodigoHamming(quadro);
        controleErros = "Codigo de Hamming";
        break;
    } //Fim switch

    System.out.println("\nO controle de erro escolhido foi: " + controleErros + "\n");

    for(int i = 0; i < quadroControleErros.length; i++){
      controller.adicionarBitsEnquadradosTextArea(Util.bitsParaString(quadroControleErros[i]) + "\n");
      System.out.println("quadroControleErros[" + i + "] = " + Util.bitsParaString(quadroControleErros[i]));
      
      try{Thread.sleep(controller.getVelocidade());} 
      catch (Exception e){e.printStackTrace();}
    } //Fim for

    return quadroControleErros;

  } //Fim camadaEnlaceDadosTransmissoraControleDeErros



  /**
   * Realiza o controle de erros pelo bit de paridade par.
   * <p>
   * Faz a contagem de bits '1' na sequencia e insere o bit correspondente,
   * isto eh, se par, 0, se impar, 1.
   *
   * @param quadro Vetor de inteiros contendo a mensagem enquadrada.
   * @return int[] Vetor de inteiros com o bit de paridade inserido.
   */ 
  private static int[] camadaEnlaceDadosTransmissoraControleDeErrosBitParidadePar(int[] quadro){

    int paridade = 0;

    for (int caractere : quadro) { //Para cada caractere em quadro
        for (int i = 0; i < 8; i++) { //Para cada bit
          if (((caractere >> i) & 1) == 1) { //Se o bit for 1
            paridade++; //Adiciona
          } //Fim if
        } //Fim for
    } //Fim for quadro

    int[] quadroComParidade = new int[quadro.length + 1];
    System.arraycopy(quadro, 0, quadroComParidade, 0, quadro.length);
    quadroComParidade[quadro.length] = (paridade % 2 == 0) ? 0 : 1; //Insere o bit de paridade par
    
    return quadroComParidade;

  } //Fim camadaEnlaceDadosTransmissoraControleDeErrosBitParidadePar



  /**
   * Realiza o controle de erros pelo bit de paridade impar.
   * <p>
   * Faz a contagem de bits '1' na sequencia e insere o bit correspondente,
   * isto eh, se par, 1, se impar, 0.
   *
   * @param quadro Vetor de inteiros contendo a mensagem enquadrada.
   * @return int[] Vetor de inteiros com o bit de paridade inserido.
   */ 
  private static int[] camadaEnlaceDadosTransmissoraControleDeErrosBitParidadeImpar(int[] quadro){
    
    int paridade = 0;

    for (int caractere : quadro) {
        for (int i = 0; i < 8; i++) { 
          if (((caractere >> i) & 1) == 1) {
            paridade++;
          } //Fim if
        } //Fim for
    } //Fim for quadro

    int[] quadroComParidade = new int[quadro.length + 1];
    System.arraycopy(quadro, 0, quadroComParidade, 0, quadro.length);
    quadroComParidade[quadro.length] = (paridade % 2 == 0) ? 1 : 0; //Insere o bit de paridade impar
    
    return quadroComParidade;

  } //Fim camadaEnlaceDadosTransmissoraControleDeErrosBitParidadeImpar



  /**
   * Realiza o controle de erros pela verificacao ciclica de redundancia.
   * <p>
   * Faz a divisao polinomial da sequencia de bits da mensagem enquadrada pelo
   * polinomio gerador CRC-32 e insere o checksum (resto da divisao) na mensagem
   * a ser enviada pelas camadas.
   *
   * @param quadro Vetor de inteiros contendo a mensagem enquadrada.
   * @return int[] Vetor de inteiros contendo a mensagem + CRC.
   */ 
  private static int[] camadaEnlaceDadosTransmissoraControleDeErrosCRC(int[] quadro){
    
    //Polinomio CRC-32 IEEE: x^32 + x^26 + x^23 + x^22 + x^16 + x^12 + x^11 + x^10 + x^8 + x^7 + x^5 + x^4 + x^2 + x + 1
    //Em binario: 100000100110000010001110110110111 (33 bits)
    BigInteger polinomio = new BigInteger("100000100110000010001110110110111", 2);

    //Converte o quadro para uma string de bits
    StringBuilder bitsMensagem = new StringBuilder();
    for (int byteDado : quadro) {
        String bits = String.format("%8s", Integer.toBinaryString(byteDado & 0xFF)).replace(' ', '0');
        bitsMensagem.append(bits);
    }

    //Adiciona 32 zeros ao final para a divisao polinomial
    bitsMensagem.append("00000000000000000000000000000000");

    //Converte para BigInteger
    BigInteger mensagem = new BigInteger(bitsMensagem.toString(), 2);

    //Realiza a divisao polinomial usando XOR
    int tamanhoPolinomio = 33; //Grau 32 + 1
    int tamanhoBitsMensagem = bitsMensagem.length();

    //Faz a divisao bit a bit com XOR
    for (int i = 0; i <= tamanhoBitsMensagem - tamanhoPolinomio; i++) {
        //Verifica se o bit mais significativo eh 1
        if (mensagem.testBit(tamanhoBitsMensagem - 1 - i)) {
            //Desloca o polinomio para alinhar com a posicao atual e faz XOR
            BigInteger polinomioDeslocado = polinomio.shiftLeft(tamanhoBitsMensagem - tamanhoPolinomio - i);
            mensagem = mensagem.xor(polinomioDeslocado);
        }
    }

    //O resto (CRC) sao os ultimos 32 bits
    BigInteger crc = mensagem;

    //Converte o CRC para string binaria com 32 bits (completa com zeros a esquerda)
    String crcBits = String.format("%32s", crc.toString(2)).replace(' ', '0');

    //Cria novo quadro com os 4 bytes do CRC adicionados
    int[] quadroComCRC = new int[quadro.length + 4];
    System.arraycopy(quadro, 0, quadroComCRC, 0, quadro.length);

    //Adiciona os 4 bytes do CRC ao final
    for (int i = 0; i < 4; i++) {
        String byteBits = crcBits.substring(i * 8, (i + 1) * 8);
        quadroComCRC[quadro.length + i] = Integer.parseInt(byteBits, 2);
    }

    return quadroComCRC;
    
  } //Fim camadaEnlaceDadosTransmissoraControleDeErrosCRC
  


  /**
   * Realiza o controle de erros pelo codigo de hamming.
   * <p>
   * Insere bits de paridade, que verificam a paridade dos bits associados
   * a cada um da mensagem enquadrada. Serao inseridos 'r' bits de paridade,
   * sendo 'r' o primeiro inteiro a tornar 2^r >= m + r + 1 verdadeiro.
   *
   * @param quadro Vetor de inteiros contendo a mensagem enquadrada.
   * @return int[] Vetor de inteiros com o bit de paridade inserido.
   */ 
  private static int[] camadaEnlaceDadosTransmissoraControleDeErrosCodigoHamming(int[] quadro){

    return quadro;

  } //Fim camadaEnlaceDadosTransmissoraControleDeErrosCodigoHamming



  
  private static void camadaEnlaceDadosTransmissoraControleDeFluxo(int quadro[]) {
  } //Fim camadaEnlaceDadosTransmissoraControleDeFluxo

} //Fim da classe CamadaEnlaceDadosTransmissora
