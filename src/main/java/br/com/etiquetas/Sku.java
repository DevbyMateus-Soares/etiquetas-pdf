package br.com.etiquetas;

/**
* Divide o código a mercadoria (ex: "4AM011-100-L") em suas partes:
 * produto "4AM011" código da cor "100" e Tamanho "L".
 *
 * O tamanho é tudo que vem depois do segundo hifen, podem haver tamanhos com duas letras.
 * */
public record Sku (String completo, String produto, String codigoCor, String tamanho){

    public static Sku de(String texto){
     String limpo = texto == null ?"" :texto.trim();
     String [] partes = limpo.split ("-",3);
     if (partes.length < 3 || partes[0].isBlank() || partes[1].isBlank()|| partes[2].isBlank()) {
              throw new IllegalArgumentException("SKU fora do padrão PRODUTO-COR-TAMANHO: \"" + texto + "\"");
     }
        return new Sku (limpo, partes[0].trim(), partes[1].trim(), partes[2].trim());
    }
}
