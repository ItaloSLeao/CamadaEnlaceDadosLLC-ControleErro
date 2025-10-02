package util;

/**
 * Classe com metodos estaticos utilitarios para a simulacao.
 * <p>
 * Fornece funcionalidades de apoio, como a formatacao de dados para exibicao
 * na interface grafica, utilizadas pelas camadas de enlace e pelo meio de comunicacao.
 *
 * @author  Italo de Souza Leao (Matricula: 202410120)
 * @version /10/2025 (Ultima alteracao)
 * @since   02/10/2025 (Inicio)
 */
public class Util {
  
  /**
   * Converte um inteiro de 32 bits para sua representacao em string binaria.
   * <p>
   * O metodo gera uma string contendo '0's e '1's que representam o inteiro.
   * Para facilitar a leitura, um espaco eh inserido a cada 8 bits, separando
   * visualmente os bytes.
   *
   * @param bits O numero inteiro de 32 bits a ser formatado.
   * @return String A representacao binaria do inteiro em formato de texto.
   */
  public static String bitsParaString(int bits) {
    String retorno = "";
    int mascara = 1; //A mascara comeca no primeiro bit (LSB)
  
    for (int i = 0; i < 32; i++) {
      if (i > 0 && i % 8 == 0) { //Adiciona espaco a cada 8 bits (exceto no inicio)
        retorno = ' ' + retorno;
      } 
      //Correcao de formatacao: Primeiro verifica, depois desloca.
      if ((mascara & bits) != 0) { //Verifica o bit na posicao atual da mascara
        retorno = "1" + retorno;
      } else {
        retorno = "0" + retorno;
      } //Fim if-else
      mascara <<= 1; //Move a mascara para a proxima posicao a esquerda
    }
  
    return retorno.trim(); //.trim() para remover o espaco extra no comeco
  } //Fim bitsParaString

} //Fim da classe Util