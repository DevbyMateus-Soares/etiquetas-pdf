package br.com.etiquetas;

/**
 * Uma linha lida do Excel (já com os campos que interessam)
 * @param volume  numero do volume (coluna "V"ol." do arquivo de separação)
 * @param sku código completo do produto (coluna "Cod.merc.")
 * @param descricao nome da mercadoria (coluna "nome mercadoria")
 * @param quantidade quantidade separada (coluna "Qt.Separação")
 */

public record ItemSeparacao(int volume, String sku, String descricao, int quantidade){
}
