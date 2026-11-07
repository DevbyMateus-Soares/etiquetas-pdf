package br.com.etiquetas;

/**
 * Linha de será impressa no PDF de um volume.
 * Corresponde ás colunas: SKU, DESCRICAO, COR, Numeração e Quantidade.
 */
public record LinhaEtiqueta(String sku, String descricao, String cor, String numeracao, int quantidade){
}