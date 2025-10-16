package model;

import controller.ControllerTelaPrincipal;
import util.Util;
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
   */
  protected static void meioDeComunicacao(int fluxoBits[], ControllerTelaPrincipal controller) {

    int fluxoBitsPontoA[] = fluxoBits; //Ponto A
    int fluxoBitsPontoB[] = new int[fluxoBits.length]; //Ponto B
    int bitParaEnviar;

    Random random = new Random();
    int probErro = controller.getErro();
    int erroRandom = random.nextInt(100);
    //Se random acertar o intervalo [0,probErro], gera um indice aleatorio, se nao eh 0
    int indiceFluxoErro = (erroRandom < probErro) ? random.nextInt(fluxoBitsPontoA.length) : 0;
    boolean erro = erroRandom < probErro;

    System.out.println("\nMEIO DE COMUNICACAO----------------------\n" 
    + "Probabilidade de erro: " + probErro + "%\n");

    for (int i = 0; i < fluxoBits.length; i++) { //Laco para processar cada inteiro de 32 bits

      System.out.println("Bits enviados: " + Util.bitsParaString(fluxoBitsPontoA[i]));

      int bits = fluxoBitsPontoA[i];
      int bitComparacao = 1;
      bitParaEnviar = 0;

      for (int x = 0; x < 32; x++) { //Laco para processar cada bit

        System.out.println("Inteiro: " + Integer.toString(i + 1) + " Bit: " + Integer.toString(x + 1) 
        + " == " + Integer.toString((bits & bitComparacao) != 0 ? 1 : 0));

        bitParaEnviar |= (bits & bitComparacao);
        
        controller.atualizarSinais(); //Atualiza a visualizacao dos sinais na GUI
        controller.sinalizar((bits & bitComparacao) != 0 ? 1 : 0); //Define o sinal atual a ser exibido

        //Duplica o sinal na GUI, ja que nao ha transicoes na cod. binaria
        if (controller.getCodificacao() == 1) {
          controller.atualizarSinais();
          controller.sinalizar((bits & bitComparacao) != 0 ? 1 : 0);
        } //Fim do if

        bitComparacao <<= 1; //Prepara para o proximo bit, 1 bitwise left

        try {Thread.sleep(controller.getVelocidade());} 
        catch (Exception e) {e.printStackTrace();} //Fim de try-catch

      } //Fim for bits
      
      if(erro){ //Se acertou o intervalo [0,probErro]

        if(i == indiceFluxoErro){
          int bitErro = random.nextInt(32); //Posicao do bit que tera erro
          bitParaEnviar ^= (1 << bitErro); //Bitwise XOR para alterar o bit
        }

      } //Fim if

      fluxoBitsPontoB[i] = bitParaEnviar;
      System.out.println("Bits recebidos: " + Util.bitsParaString(fluxoBitsPontoB[i]) + "\n");

    } //Fim for fluxoBits[]
    
    CamadaFisicaReceptora.camadaFisicaReceptora(fluxoBitsPontoB, controller);

  } //Fim meioDeComunicacao
  
} //Fim da classe MeioDeComunicacao