package model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import controller.ControllerTelaPrincipal;
import javafx.application.Platform;
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
  private static Semaphore mutex = new Semaphore(1);
  private static Temporizador temporizador;

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

    camadaEnlaceDadosTransmissoraControleDeFluxo(quadroControleErro, controller);

    //CamadaFisicaTransmissora.camadaFisicaTransmissora(quadroControleErro, controller);

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

    // Converte o quadro em uma lista de bits (MSB primeiro)
    List<Integer> bitsOriginais = new ArrayList<>();
    for (int byteDado : quadro) {
        for (int i = 7; i >= 0; i--) {  // Extrai do bit 7 ao bit 0
            bitsOriginais.add((byteDado >> i) & 1);
        }
    }
    
    int m = bitsOriginais.size(); // Numero de bits de dados
    
    // Calcula quantos bits de paridade sao necessarios: 2^r >= m + r + 1
    int r = 0;
    while ((1 << r) < (m + r + 1)) {
        r++;
    }
    
    int n = m + r; // Tamanho total com paridade
    
    // Cria array para os bits codificados (posicao 0 nao usada, facilita calculo)
    int[] bitsCodificados = new int[n + 1];
    
    // Insere os bits de dados nas posicoes que NAO sao potencia de 2
    int indiceDado = 0;
    for (int i = 1; i <= n; i++) {
        if ((i & (i - 1)) != 0) { // Nao e potencia de 2
            if (indiceDado < bitsOriginais.size()) {
                bitsCodificados[i] = bitsOriginais.get(indiceDado++);
            }
        }
    }
    
    // Calcula os bits de paridade
    for (int i = 0; i < r; i++) {
        int posicaoParidade = 1 << i; // 1, 2, 4, 8, 16...
        int paridade = 0;
        
        // Verifica todos os bits cuja representacao binaria tem o bit i ligado
        for (int j = 1; j <= n; j++) {
            if ((j & posicaoParidade) != 0) {
                paridade ^= bitsCodificados[j];
            }
        }
        
        bitsCodificados[posicaoParidade] = paridade;
    }
    
    // Converte de volta para bytes (8 bits por posicao, MSB primeiro)
    int numBytes = (int) Math.ceil(n / 8.0);
    int[] quadroComHamming = new int[numBytes];
    
    // Preenche os bytes
    for (int i = 1; i <= n; i++) {  // Comeca de 1 (ignora posicao 0)
        int indiceBit = i - 1;  // Ajusta para indice 0-based
        int indiceByte = indiceBit / 8;
        int posicaoNoByte = 7 - (indiceBit % 8);  // MSB primeiro
        
        if (bitsCodificados[i] == 1) {
            quadroComHamming[indiceByte] |= (1 << posicaoNoByte);
        }
    }
    
    return quadroComHamming;

  } //Fim camadaEnlaceDadosTransmissoraControleDeErrosCodigoHamming



  
  private static void camadaEnlaceDadosTransmissoraControleDeFluxo(int quadro[], ControllerTelaPrincipal controller) {
    System.out.println("Iniciando Controle de Fluxo Stop-and-Wait");
    temporizador = new Temporizador(quadro, controller);
    temporizador.start();
  } //Fim camadaEnlaceDadosTransmissoraControleDeFluxo




  static class Temporizador extends Thread {
    private int[] quadro; //A mensagem inteira
    private boolean liberado; //Flag para parar o timer
    private ControllerTelaPrincipal controller;

    public Temporizador(int[] quadro, ControllerTelaPrincipal controller) {
      this.quadro = quadro;
      this.liberado = false;
      this.controller = controller;
    }

    //Para o temporizador
    public void liberar() {
      this.liberado = true;
      interrupt(); //Interrompe o sleep
    }

    @Override
    public void run() {
      try {
        mutex.acquire(); //Trava o semaforo
        
        System.out.println("Enviando mensagem... (Tentativa 1)");
        CamadaFisicaTransmissora.camadaFisicaTransmissora(quadro, controller);
        
        int tentativas = 2;
        while (!liberado) {
          Thread.sleep(5000); //Tempo de Timeout (5 segundos)

          if (!liberado) {
            System.err.println("TIMEOUT! Retransmitindo mensagem... (Tentativa " + (tentativas++) + ")");
            CamadaFisicaTransmissora.camadaFisicaTransmissora(quadro, controller);
          }
        }
        System.out.println("Mensagem confirmada (ACK recebido).");

      } catch (InterruptedException e) {
        //Ocorreu quando .liberar() eh chamado. Sucesso.
        if(!liberado) {
          System.err.println("Temporizador interrompido inesperadamente.");
          mutex.release(); //Libera em caso de erro
          Platform.runLater(() -> controller.reativar());
        }
      }
    } //Fim run
  } //Fim classe Temporizador




  protected static void ACKtemporizador(int[] ack, ControllerTelaPrincipal controller) {
    System.out.println("CAMADA ENLACE (TX): ACK Recebido!");

    //Logica simples de verificacao de ACK
    if (ack != null && ack.length > 0 && ack[0] == (1 << 31)) {
      if (temporizador != null) {
        temporizador.liberar(); //Para o temporizador atual
        System.out.println("Transmissao concluida com sucesso!");
        mutex.release(); // Libera o semaforo
        Platform.runLater(() -> controller.reativar()); //Reativa a GUI
      }
    } else {
      //Deixa o temporizador esgotar
    }
  }










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
  private static int[] camadaEnlaceDadosReceptoraEnquadramento(int quadro[], ControllerTelaPrincipal controller) {

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

    for(int i = 0; i < quadroDesenquadrado.length; i++){
      if(!(quadroDesenquadrado == null)){
        System.out.println("quadroDesenquadrado[" + i + "] = " + Util.bitsParaString(quadroDesenquadrado[i]));
      }
      
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
  private static int[] camadaEnlaceDadosReceptoraEnquadramentoContagemDeCaracteres(int quadro[]){

    //Usa um array dinamico, pois o tamanho original da mensagem eh desconhecido a priori
    ArrayList<Integer> quadroOriginalList = new ArrayList<>();

    int indiceEntrada = 0; //Controla a posicao de leitura no quadroEnquadrado

    //Altera o loop para verificar ativamente as condicoes de parada,
    //em vez de apenas iterar sobre o comprimento do array.
    while (true) {
      
      //Condicao de parada 1: Atingiu o fim fisico do array.
      if (indiceEntrada >= quadro.length) {
        break; //Fim da transmissao
      }

      //Le o primeiro byte do quadro, que eh o byte de contagem.
      int tamanhoDoBloco = quadro[indiceEntrada++];

      //Condicao de parada 2: Encontrou um byte de contagem 0.
      //Isso indica padding ou o fim logico da transmissao.
      if (tamanhoDoBloco == 0) {
        break; //Fim logico da transmissao
      }

      int caracteresCargaUtil = tamanhoDoBloco - 1;

      //Condicao de seguranca: Verifica se o byte de contagem eh valido.
      //Se for corrompido e apontar para fora do array, lanca a excecao
      //que eh esperada e tratada pela camadaEnlaceDadosReceptora.
      if (indiceEntrada + caracteresCargaUtil > quadro.length) {
        throw new ArrayIndexOutOfBoundsException("Contagem de caracteres corrompida");
      }

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
  private static int[] camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBytes(int quadro[]){

    final char FLAG = 'i';
    final char ESCAPE = '/';

    ArrayList<Integer> quadroOriginalList = new ArrayList<>();

    boolean dentroDoQuadro = false; //Controla se esta dentro do quadro, para determinar o fim
    boolean proximoEhDado = false; //Controla a relacao do escape lido anteriormente com o byte atual

    for (int i = 0; i < quadro.length; i++) {

      int caractere = quadro[i];

      if (proximoEhDado) {
        quadroOriginalList.add(caractere);
        proximoEhDado = false;
        continue;
      }

      if (caractere == FLAG) {
        //Se estava fora de um quadro, esta FLAG inicia um novo.
        if (!dentroDoQuadro) {
          dentroDoQuadro = true;
        }
        //Se estava dentro de um quadro, esta FLAG o finaliza.
        else {
          dentroDoQuadro = false;
          
          //Logica de parada: verifica se existe um proximo byte no array.
          boolean existeProximoByte = (i + 1) < quadro.length;
          
          //Se nao existe proximo byte, ou se o proximo byte nao eh FLAG,
          //a transmissao eh considerada finalizada e o laco eh interrompido.
          if (!existeProximoByte || (existeProximoByte && quadro[i + 1] != FLAG)) {
            break; 
          }
          //Se a condicao acima for falsa, significa que o proximo byte e uma FLAG
          //que iniciara um novo quadro na proxima iteracao, entao o laco continua.
        }
      }
      else if (dentroDoQuadro) {
        if (caractere == ESCAPE) {
          proximoEhDado = true;
        } else {
          quadroOriginalList.add(caractere);
        }
      }
      
    }

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
  private static int[] camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBits(int quadro[]) {

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
              //Implementacao do desestufamento com uma funcao desestufarBits
              ArrayList<Integer> cargaUtilLimpa = desestufarBits(cargaUtilBruta);
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

    if(!cargaUtilBruta.isEmpty()){
      ArrayList<Integer> cargaUtilLimpa = desestufarBits(cargaUtilBruta);
      quadroOriginalList.addAll(cargaUtilLimpa);
    }

    int[] quadroOriginal = new int[quadroOriginalList.size()];

    for (int i = 0; i < quadroOriginalList.size(); i++) {
      quadroOriginal[i] = quadroOriginalList.get(i);
    }

    return quadroOriginal;

  } //Fim camadaEnlaceDadosReceptoraInsercaoDeBits



  /**
   * Funcao auxiliar que realiza o desestufamento em uma lista de bytes.
   * @param cargaUtilBruta      A lista de bytes que compoe a carga util de um quadro.
   * @return ArrayList<Integer> A lista de bytes apos a remocao dos bits de stuffing.
   */
  private static ArrayList<Integer> desestufarBits(ArrayList<Integer> cargaUtilBruta) {
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
  } //Fim desestufarBits



  /**
   * Realiza o desenquadramento pela tecnica de violacao da camada fisica.
   * <p>
   * Simplesmente recebe o vetor de inteiros desenquadrado, decodificado
   * e tambem desempacotado, com os caracteres posicionados.
   *
   * @param quadro O fluxo de dados codificado.
   * @return int[] A mensagem original.
   */
  private static int[] camadaEnlaceDadosReceptoraEnquadramentoViolacaoCamadaFisica(int quadro[]){
    return quadro;
  } //Fim camadaEnlaceDadosReceptoraEnquadramentoViolacaoCamadaFisica


  

  /**
   * Realiza o controle de erros com os quadros de caracteres enquadrados recebidos,
   * conforme a escolha na GUI.
   *
   * @param quadro     Vetor com os caracteres da mensagem decodificados, ainda enquadrados.
   * @param controller Controlador da interface grafica.
   * @return int[]     O resultado do controle de erros.
   */
  private static int[] camadaEnlaceDadosReceptoraControleDeErros(int quadro[], ControllerTelaPrincipal controller){

    int tipoControleErros = controller.getControleErro();
    int[] quadroControleErros;

    switch (tipoControleErros) {
      case 1:
        quadroControleErros = camadaEnlaceDadosReceptoraControleDeErrosBitParidadePar(quadro);
        break;
      case 2:
        quadroControleErros = camadaEnlaceDadosReceptoraControleDeErrosBitParidadeImpar(quadro);
        break;
      case 3:
        quadroControleErros = camadaEnlaceDadosReceptoraControleDeErrosCRC(quadro);
        break;
      default:
        quadroControleErros = camadaEnlaceDadosReceptoraControleDeErrosCodigoHamming(quadro);
        break;
    } //Fim switch

    if(!(quadroControleErros == null)){
      for(int i = 0; i < quadroControleErros.length; i++){
      System.out.println("quadroControleErros[" + i + "] = " + Util.bitsParaString(quadroControleErros[i]));
      
      try{Thread.sleep(controller.getVelocidade());} 
      catch (Exception e){e.printStackTrace();}
      } //Fim for      
    }

    System.out.println("\n");
    
    return quadroControleErros;

  } //Fim de camadaEnlaceDadosReceptoraControleDeErros



  /**
   * Realiza o controle de erros pelo bit de paridade par.
   * <p>
   * Faz a contagem de bits '1' na sequencia e compara com o bit de paridade esperado
   * com aquele recebido, se diferirem, ha um erro.
   *
   * @param quadro Vetor de inteiros contendo a mensagem decodificada e enquadrada.
   * @return int[] Vetor de inteiros com o bit de paridade retirado.
   */ 
  private static int[] camadaEnlaceDadosReceptoraControleDeErrosBitParidadePar(int[] quadro){

    int paridade = 0;
    //Calcula a paridade byte por byte, bit por bit, ate o penultimo
    for (int i = 0; i < quadro.length - 1; i++) {
      int caractere = quadro[i];
      for (int j = 0; j < 8; j++) {
        if (((caractere >> j) & 1) == 1) {
          paridade++;
        } //Fim if
      } //Fim for
    } //Fim for

    int bitParidadeRecebido = quadro[quadro.length - 1];
    int bitParidadeEsperado = (paridade % 2 == 0) ? 0 : 1;

    if (bitParidadeRecebido != bitParidadeEsperado) {
      return null; //Erro detectado
    }

    //Retorna o quadro original sem o bit de paridade
    int[] quadroOriginal = new int[quadro.length - 1];
    System.arraycopy(quadro, 0, quadroOriginal, 0, quadroOriginal.length);

    return quadroOriginal;

  } //Fim camadaEnlaceDadosReceptoraControleDeErrosBitParidadePar



  /**
   * Realiza o controle de erros pelo bit de paridade impar.
   * <p>
   * Faz a contagem de bits '1' na sequencia e compara com o bit de paridade esperado
   * com aquele recebido, se diferirem, ha um erro.
   *
   * @param quadro Vetor de inteiros contendo a mensagem decodificada e enquadrada.
   * @return int[] Vetor de inteiros com o bit de paridade retirado.
   */ 
  private static int[] camadaEnlaceDadosReceptoraControleDeErrosBitParidadeImpar(int[] quadro){
    
    int paridade = 0;
    //Calcula a paridade byte por byte, bit por bit, ate o penultimo
    for (int i = 0; i < quadro.length - 1; i++) {
      int caractere = quadro[i];
      for (int j = 0; j < 8; j++) {
        if (((caractere >> j) & 1) == 1) {
          paridade++;
        } //Fim if
      } //Fim for
    } //Fim for

    int bitParidadeRecebido = quadro[quadro.length - 1];
    int bitParidadeEsperado = (paridade % 2 == 0) ? 1 : 0; //Se paridade par, 1, se nao, 0

    if (bitParidadeRecebido != bitParidadeEsperado) {
      return null; //Erro detectado
    }

    //Retorna o quadro original sem o bit de paridade
    int[] quadroOriginal = new int[quadro.length - 1];
    System.arraycopy(quadro, 0, quadroOriginal, 0, quadroOriginal.length);

    return quadroOriginal;

  } //Fim camadaEnlaceDadosReceptoraControleDeErrosBitParidadeImpar



  /**
   * Realiza o controle de erros pela verificacao ciclica de redundancia.
   * <p>
   * Faz a divisao polinomial da (mensagem + CRC) recebida pelo p.Gerador CRC-32, se
   * o resto da divisao for diferente de 0, um erro ocorreu, pois M(x) + R(x) eh 
   * perfeitamente divisivel por G(x).
   *
   * @param quadro Vetor de inteiros contendo a mensagem enquadrada.
   * @return int[] Vetor de inteiros com o bit de paridade inserido.
   */ 
  private static int[] camadaEnlaceDadosReceptoraControleDeErrosCRC(int[] quadro){
    
    //Polinomio CRC-32 IEEE: 100000100110000010001110110110111 (33 bits)
    BigInteger polinomio = new BigInteger("100000100110000010001110110110111", 2);

    //Converte todo o quadro (mensagem + CRC) para bits
    StringBuilder bitsCompleto = new StringBuilder();
    for (int byteDado : quadro) {
        String bits = String.format("%8s", Integer.toBinaryString(byteDado & 0xFF)).replace(' ', '0');
        bitsCompleto.append(bits);
    }

    //Converte para BigInteger
    BigInteger mensagem = new BigInteger(bitsCompleto.toString(), 2);

    //Realiza a divisao polinomial usando XOR
    int tamanhoPolinomio = 33; //Grau 32 + 1
    int tamanhoBitsMensagem = bitsCompleto.length();

    //Faz a divisao bit a bit com XOR
    for (int i = 0; i <= tamanhoBitsMensagem - tamanhoPolinomio; i++) {
        //Verifica se o bit mais significativo eh 1
        if (mensagem.testBit(tamanhoBitsMensagem - 1 - i)) {
            //Desloca o polinomio para alinhar com a posicao atual e faz XOR
            BigInteger polinomioDeslocado = polinomio.shiftLeft(tamanhoBitsMensagem - tamanhoPolinomio - i);
            mensagem = mensagem.xor(polinomioDeslocado);
        }
    }

    //O resto deve ser zero se nao houver erro
    if (!mensagem.equals(BigInteger.ZERO)) {
        return null; //Erro detectado!
    }

    //Retorna a mensagem original sem os ultimos 4 bytes (CRC)
    int tamanhoOriginal = quadro.length - 4;
    int[] quadroOriginal = new int[tamanhoOriginal];
    System.arraycopy(quadro, 0, quadroOriginal, 0, tamanhoOriginal);

    return quadroOriginal;

  } //Fim camadaEnlaceDadosReceptoraControleDeErrosCRC
  
  

  /**
   * Realiza o controle de erros pelo codigo de hamming.
   * <p>
   * Faz a comparacao dos bits de paridade esperados (de acordo com os resultados das
   * operacoes XOR com os bits a cada um associados), com os bits de paridade recebidos.
   * Se os bits diferirem, ha um erro, a sindrome eh calculada e o bit 'errado' eh corrigido.
   *
   * @param quadro Vetor de inteiros contendo a mensagem enquadrada.
   * @return int[] Vetor de inteiros com o bit de paridade inserido.
   */ 
  private static int[] camadaEnlaceDadosReceptoraControleDeErrosCodigoHamming(int[] quadro){

    // Converte o quadro em lista de bits (posicao 0 nao usada)
    List<Integer> bitsRecebidos = new ArrayList<>();
    bitsRecebidos.add(0); // Posicao 0 nao usada
    
    for (int byteDado : quadro) {
        for (int i = 7; i >= 0; i--) {  // Extrai do bit 7 ao bit 0
            bitsRecebidos.add((byteDado >> i) & 1);
        }
    }
    
    int n = bitsRecebidos.size() - 1; // Tamanho total (sem posicao 0)
    
    // Calcula quantos bits de paridade existem
    int r = 0;
    int temp = 1;
    while (temp <= n) {
        r++;
        temp = temp << 1;
    }
    
    // Calcula a sindrome (detecta posicao do erro)
    int sindrome = 0;
    for (int i = 0; i < r; i++) {
        int posicaoParidade = 1 << i;  // 1, 2, 4, 8, 16...
        
        if (posicaoParidade > n) break;  // Evita acessar posicoes invalidas
        
        int paridade = 0;
        
        for (int j = 1; j <= n; j++) {
            if ((j & posicaoParidade) != 0) {
                paridade ^= bitsRecebidos.get(j);
            }
        }
        
        if (paridade != 0) {
            sindrome += posicaoParidade;
        }
    }
    
    // Se sindrome != 0, corrige o bit errado
    if (sindrome != 0) {
        if (sindrome <= n) {
            System.out.println("Erro detectado na posicao: " + sindrome + " (corrigindo)");
            bitsRecebidos.set(sindrome, bitsRecebidos.get(sindrome) ^ 1);
        }
    }
    
    // Extrai apenas os bits de dados (remove bits de paridade)
    List<Integer> bitsDados = new ArrayList<>();
    for (int i = 1; i <= n; i++) {
        if ((i & (i - 1)) != 0) { // Nao e potencia de 2
            bitsDados.add(bitsRecebidos.get(i));
        }
    }
    
    // Converte bits de volta para bytes
    int numBytes = bitsDados.size() / 8;  // Apenas bytes completos
    int[] quadroOriginal = new int[numBytes];
    
    for (int i = 0; i < numBytes * 8; i++) {
        int indiceByte = i / 8;
        int posicaoNoByte = 7 - (i % 8);  // MSB primeiro
        
        if (bitsDados.get(i) == 1) {
            quadroOriginal[indiceByte] |= (1 << posicaoNoByte);
        }
    }
    
    return quadroOriginal;

  } //Fim camadaEnlaceDadosReceptoraControleDeErrosCodigoHamming


} //Fim da classe CamadaEnlaceDadosTransmissora