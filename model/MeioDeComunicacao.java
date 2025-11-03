package model;

import controller.ControllerTelaPrincipal;
import java.util.Random;
 
/**
 * Modela o funcionamento de um meio de comunicacao em uma rede.
 * <p>
 * Esta classe eh responsavel por simular a transmissao de informacoes de um
 * ponto a outro (transmissor para receptor), dando continuidade ao fluxo de
 * envio de dados entre as camadas da simulacao. Alem disso, ha uma simulacao
 * de ocorrencia de um erro de transmissao, mediante a probabilidade escolhida.
 *
 * @author  Italo de Souza Leao (Matricula: 202410120)
 * @version 16/10/2025 (Ultima alteracao)
 * @since   02/10/2025 (Inicio)
 */
public class MeioDeComunicacao {

  /**
   * Simula a transmissao de dados bit a bit em um meio fisico.
   * <p>
   * Este metodo recebe um fluxo de bits do transmissor (Ponto A), itera sobre
   * cada bit individualmente, atualiza a interface grafica para visualizar a
   * transmissao do sinal e, ao final, entrega o fluxo de bits completo para o
   * receptor (Ponto B), que o encaminha para a camada fisica receptora.
   *
   * @param fluxoBits   O fluxo de bits a ser transmitido, vindo da camada fisica transmissora.
   * @param controller  O controlador da interface grafica para atualizacoes visuais.
   * @param ehACK       Indica se a transmissao eh um ACK (para animacao reversa).
   */
  protected static void meioDeComunicacao(int fluxoBits[], ControllerTelaPrincipal controller, boolean ehACK) {

    int fluxoBitsPontoA[] = fluxoBits; //Ponto A
    int fluxoBitsPontoB[] = new int[fluxoBits.length]; //Ponto B
    int bitParaEnviar;

    Random random = new Random();
    
    int probErro = controller.getErro();
    int erroRandom = random.nextInt(100);

    //Se random acertar o intervalo [0,probErro], gera um indice aleatorio, se nao eh 0
    int indiceFluxoErro = (erroRandom < probErro) ? random.nextInt(fluxoBitsPontoA.length) : 0;

    boolean erro = erroRandom < probErro;

    if (!ehACK) {
      System.out.println("\nMEIO DE COMUNICACAO----------------------\n" 
          + "Probabilidade de erro: " + probErro + "%\n");
    }

    for (int i = 0; i < fluxoBits.length; i++) { //Laco para processar cada inteiro de 32 bits

      int bits = fluxoBitsPontoA[i];
      int bitComparacao = 1;
      bitParaEnviar = 0;

      for (int x = 0; x < 32; x++) { //Laco para processar cada bit

        bitParaEnviar |= (bits & bitComparacao);
        
        //Usa animacao normal para ACK (mesma direcao dos quadros normais)
        controller.atualizarSinais(); //Atualiza a visualizacao dos sinais na GUI
        controller.sinalizar((bits & bitComparacao) != 0 ? 1 : 0); //Define o sinal atual a ser exibido

        //Duplica o sinal na GUI, ja que nao ha transicoes na cod. binaria
        if (controller.getCodificacao() == 1) {
          controller.atualizarSinais();
          controller.sinalizar((bits & bitComparacao) != 0 ? 1 : 0);
        } //Fim do if

        bitComparacao <<= 1; //Prepara para o proximo bit, 1 bitwise left

      } //Fim for bits

      controller.atualizarSinais();
      
      if(erro && !ehACK){ //ACKs nao sofrem erros

        if(i == indiceFluxoErro){
          int bitErro = random.nextInt(32); //Posicao do bit que tera erro
          bitParaEnviar ^= (1 << bitErro); //Bitwise XOR para alterar o bit
        }

      } //Fim if

      fluxoBitsPontoB[i] = bitParaEnviar;

      try {Thread.sleep(controller.getVelocidade());} 
      catch (Exception e) {e.printStackTrace();} //Fim de try-catch

    } //Fim for fluxoBits[]
    
    if (ehACK) {

      //ACK precisa ser decodificado antes de chegar ao transmissor
      //Passa pela camada fisica para decodificacao (Manchester, etc)
      System.out.println("> MeioDeComunicacao: ACK recebido, iniciando decodificacao (codificacao=" + controller.getCodificacao() + ")");
      int[] ackDecodificado = null;
      
      //Decodifica o ACK de acordo com a codificacao escolhida
      //Como eh um ACK, nao precisa passar por desenquadramento, apenas decodificacao
      switch (controller.getCodificacao()) {
        case 1: //Binaria
          ackDecodificado = CamadaFisicaReceptora.camadaFisicaReceptoraDecodificacaoBinaria(fluxoBitsPontoB);
          System.out.println("> MeioDeComunicacao: ACK decodificado (binaria) - length=" 
              + (ackDecodificado != null ? ackDecodificado.length : 0));
          if (ackDecodificado != null && ackDecodificado.length > 0) {
            System.out.println("> MeioDeComunicacao: Primeiro byte do ACK decodificado: 0x" + Integer.toHexString(ackDecodificado[0] & 0xFF));
          }
          break;
        case 2: //Manchester
          ackDecodificado = CamadaFisicaReceptora.camadaFisicaReceptoraDecodificacaoManchester(fluxoBitsPontoB);
          System.out.println("> MeioDeComunicacao: ACK decodificado (Manchester) - length=" 
              + (ackDecodificado != null ? ackDecodificado.length : 0));
          break;
        default: //Manchester Diferencial
          ackDecodificado = CamadaFisicaReceptora.camadaFisicaReceptoraDecodificacaoManchesterDiferencial(fluxoBitsPontoB);
          System.out.println("> MeioDeComunicacao: ACK decodificado (Manchester Diferencial) - length=" 
              + (ackDecodificado != null ? ackDecodificado.length : 0));
          break;
      }
      
      //ACK vai para o transmissor (ja decodificado)
      System.out.println("> MeioDeComunicacao: Enviando ACK decodificado para ACKtemporizador");
      CamadaEnlaceDadosTransmissora.ACKtemporizador(ackDecodificado, controller);

    } else {
      CamadaFisicaReceptora.camadaFisicaReceptora(fluxoBitsPontoB, controller);
    }

  } //Fim meioDeComunicacao
  
} //Fim da classe MeioDeComunicacao