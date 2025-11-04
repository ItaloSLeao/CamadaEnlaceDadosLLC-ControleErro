package model;

import controller.ControllerTelaPrincipal;

/**
 * Ponto de entrada para o processo de transmissao da mensagem.
 * <p>
 * Esta classe eh responsavel por capturar a mensagem inserida pelo usuario
 * na interface grafica e entrega-la a primeira camada da pilha de protocolos,
 * a CamadaAplicacaoTransmissora, para iniciar a simulacao.
 *
 * @author  Italo de Souza Leao (Matricula: 202410120)
 * @version 04/11/2025 (Ultima alteracao)
 * @since   02/10/2025 (Inicio)
 */
public class AplicacaoTransmissora {

  /**
   * Captura a mensagem da GUI e a envia para a camada de aplicacao.
   * <p>
   * Este eh o metodo inicial da simulacao de transmissao. Ele obtem o texto
   * digitado pelo usuario atraves do controlador e o passa para a proxima
   * camada, dando inicio ao fluxo de envio da mensagem.
   *
   * @param controller O controlador da interface grafica do usuario.
   */
  public static void aplicacaoTransmissora(ControllerTelaPrincipal controller){
    System.out.println("\nAPLICACAO TRANSMISSORA--------------");
    String mensagem = controller.getMensagem();
    System.out.println("\nMensagem enviada: " + mensagem);
    CamadaAplicacaoTransmissora.camadaAplicacaoTransmissora(mensagem, controller);

  } //Fim aplicacaoTransmissora
  
} //Fim da classe AplicacaoTransmissora