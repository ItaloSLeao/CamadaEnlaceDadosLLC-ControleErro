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

    //Verifica se eh ACK antes de processar (ACK tem 1 inteiro de 32 bits = 0x80000000)
    if (fluxoBits != null && fluxoBits.length == 1 && fluxoBits[0] == 0x80000000) {
      //ACK nao precisa ser decodificado - encaminha diretamente
      return;
    }

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

    //Logs detalhados removidos para organizacao - apenas processa os dados
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

    // Array temporario com tamanho maximo possivel
    int[] decodificadoTemp = new int[fluxoBits.length * 2];
    int bytesDecodificados = 0;
    
    for (int i = 0; i < fluxoBits.length; i++) {
        int intRecebido = fluxoBits[i];
        
        // Se o inteiro eh completamente zero, pula (padding final)
        if (intRecebido == 0) {
            break;
        }
        
        // Decodifica o primeiro caractere (bits 31-16)
        int primeiroChar = 0;
        boolean primeiraMetadeValida = false;
        
        for (int j = 0; j < 8; j++) {
            int par = (intRecebido >> (30 - j*2)) & 0b11;
            primeiroChar <<= 1;
            if(par == 0b10){
                primeiroChar |= 1;
            }
            if (par != 0) {
                primeiraMetadeValida = true;
            }
        }
        
        // So adiciona se a primeira metade tem dados validos
        if (primeiraMetadeValida) {
            decodificadoTemp[bytesDecodificados++] = primeiroChar;
        }
        
        // Decodifica o segundo caractere (bits 15-0)
        int segundoChar = 0;
        boolean segundaMetadeValida = false;
        
        for (int j = 0; j < 8; j++) {
            int par = (intRecebido >> (14 - j*2)) & 0b11;
            segundoChar <<= 1;
            if(par == 0b10){
                segundoChar |= 1;
            }
            if (par != 0) {
                segundaMetadeValida = true;
            }
        }
        
        // So adiciona se a segunda metade tem dados validos
        if (segundaMetadeValida) {
            decodificadoTemp[bytesDecodificados++] = segundoChar;
        }
    }
    
    // Cria array final com tamanho exato
    int[] decodificado = new int[bytesDecodificados];
    System.arraycopy(decodificadoTemp, 0, decodificado, 0, bytesDecodificados);
    
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

    // Array temporario com tamanho maximo possivel
    int[] decodificadoTemp = new int[fluxoBits.length * 2];
    int bytesDecodificados = 0;
    boolean ultimoSinal = true;

    for (int i = 0; i < fluxoBits.length; i++) {
        int intRecebido = fluxoBits[i];
        
        // Se o inteiro eh completamente zero, pula (padding final)
        if (intRecebido == 0) {
            break;
        }
        
        // Decodifica o primeiro caractere (bits 31-16)
        int primeiroChar = 0;
        boolean primeiraMetadeValida = false;
        
        for (int j = 0; j < 8; j++) {
            int primeiroSinalPar = (intRecebido >> (31 - j * 2)) & 1;
            int segundoSinalPar = (intRecebido >> (30 - j * 2)) & 1;

            primeiroChar <<= 1;

            if ((primeiroSinalPar == 1) == ultimoSinal) {
                primeiroChar |= 1;
            }
            ultimoSinal = (segundoSinalPar == 1);
            
            if (primeiroSinalPar != 0 || segundoSinalPar != 0) {
                primeiraMetadeValida = true;
            }
        }

        // So adiciona se a primeira metade tem dados validos
        if (primeiraMetadeValida) {
            decodificadoTemp[bytesDecodificados++] = primeiroChar;
        }

        // Decodifica o segundo caractere (bits 15-0)
        int segundoChar = 0;
        boolean segundaMetadeValida = false;
        
        for (int j = 0; j < 8; j++) {
            int primeiroSinalPar = (intRecebido >> (15 - j * 2)) & 1;
            int segundoSinalPar = (intRecebido >> (14 - j * 2)) & 1;

            segundoChar <<= 1;
            if ((primeiroSinalPar == 1) == ultimoSinal) {
                segundoChar |= 1;
            }
            ultimoSinal = (segundoSinalPar == 1);
            
            if (primeiroSinalPar != 0 || segundoSinalPar != 0) {
                segundaMetadeValida = true;
            }
        }
        
        // So adiciona se a segunda metade tem dados validos
        if (segundaMetadeValida) {
            decodificadoTemp[bytesDecodificados++] = segundoChar;
        }
    }
    
    // Cria array final com tamanho exato
    int[] decodificado = new int[bytesDecodificados];
    System.arraycopy(decodificadoTemp, 0, decodificado, 0, bytesDecodificados);
    
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

    //Verifica se eh ACK: ACK tem 1 byte (0x80 ou 0x81) que vira 1 inteiro de 32 bits (0x80000000 ou 0x81000000)
    boolean ehACK = false;
    if (quadro != null && quadro.length == 1) {
      int primeiroByte = quadro[0] & 0xFF;
      if ((primeiroByte & 0x80) == 0x80) {
        //ACK tem bit 7 setado - pode ser 0x80, 0x81, etc
        ehACK = true;
      }
    }
    
    if (!ehACK) {
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
    }
    
    MeioDeComunicacao.meioDeComunicacao(fluxoBitsFinal, controller, ehACK);
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
          int bit = (caractere >> j) & 1; //Isola o bit da posicao 'j'
          int primeiroSinal, segundoSinal;

          if (bit == 0) { //Bit 0: transicao no inicio do periodo
            primeiroSinal = ultimoSinal ? 0 : 1;
            segundoSinal = ultimoSinal ? 1 : 0;
          } else { //Bit 1: sem transicao no inicio do periodo
            primeiroSinal = ultimoSinal ? 1 : 0;
            segundoSinal = ultimoSinal ? 0 : 1;
          }
          
          informacao <<= 2; //Abre espaco para os proximos 2 bits
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

} //Fim da classe CamadaFisicaReceptora