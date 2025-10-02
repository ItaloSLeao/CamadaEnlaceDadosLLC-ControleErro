package model;

import controller.ControllerTelaPrincipal;
 
/**
 * Simula o funcionamento da camada de aplicacao de um transmissor.
 * <p>
 * Esta classe recebe a mensagem do usuario, transforma cada caractere em seu
 * valor inteiro correspondente na tabela ASCII e passa o array de inteiros
 * resultante para a proxima camada da pilha de protocolos, a Camada de Enlace.
 *
 * @author  Italo de Souza Leao (Matricula: 202410120)
 * @version 30/09/2025 (Ultima alteracao)
 * @since   02/10/2025 (Inicio)
 */
public class CamadaAplicacaoTransmissora {

  /**
   * Inicia o processo de transmissao da mensagem em uma nova thread.
   * <p>
   * Este metodo converte a mensagem de texto em um array de inteiros (quadro)
   * contendo os valores ASCII de cada caractere. Em seguida, invoca a proxima
   * camada (enlace de dados) para dar continuidade ao processo de transmissao.
   * A operacao eh executada em uma thread separada para nao bloquear a UI.
   *
   * @param mensagem    O texto a ser transmitido.
   * @param controller  O controlador da interface grafica.
   */
  protected static void camadaAplicacaoTransmissora(String mensagem, ControllerTelaPrincipal controller) {

    new Thread(() -> {
      //Tratamento de excecao, em caso de Thread Interruption
      try {
        int quadro[] = new int[mensagem.length()];
        String textoExibicao;

        System.out.println("\nCAMADA DE APLICACAO TRANSMISSORA -------------------");
        for (int i = 0; i < mensagem.length(); i++) { //Laco que percorre todos os caracteres da mensagem
          textoExibicao = "";
          textoExibicao += mensagem.charAt(i);
          textoExibicao += " = ";
          quadro[i] = mensagem.charAt(i); //Valor ASCII do caractere i eh adicionado a posicao i do array
          textoExibicao += quadro[i];
          textoExibicao += ";";

          String textoAtual = textoExibicao; //Armazena o texto construido na string

          Thread.sleep(controller.getVelocidade());
          System.out.println(textoAtual); //Imprime no console a codificacao ascii
        } //Fim do for da mensagem

        CamadaEnlaceDadosTransmissora.camadaEnlaceDadosTransmissora(quadro, controller);
      } catch (Exception e) {
        System.out.println(e.getStackTrace());
      } //Fim try-catch
      
    }).start(); //Inicializa a thread de transmissao

  } //Fim camadaAplicacaoTransmissora
} //Fim da classe CamadaAplicacaoTransmissora