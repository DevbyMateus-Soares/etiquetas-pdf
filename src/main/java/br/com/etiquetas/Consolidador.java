package br.com.etiquetas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Classe resposavel por agrupar os itens do Excel por volume e, dentro de cada voluem, por SKU
 * (Soma a quantidade quando o mesmo SKU aparece mais de uma vez no volume)
 * também descobre qual a descrição da cor de cada SKU usando a (TabelaCores)
 *
 * Os volumes saem em ordem crescente e os SKUs em ordem alfabética, isso não tem impacto negativo, pois,
 * o importante é ter na etiqueta a identificação dos SKUs dentro do volume.
 */

public class Consolidador {

    private Consolidador() {
    }

    public static Map<Integer, List<LinhaEtiqueta>> porVolume(List<ItemSeparacao> itens, List<String>avisos) {
        // volume --> (SKU -> item somado)
        Map<Integer, SortedMap<String, ItemSeparacao>> agrupado = new TreeMap<>();
        for (ItemSeparacao item : itens) {
         agrupado.computeIfAbsent(item.volume(), v -> new TreeMap<>())
                 .merge(item.sku(), item, (a,b)-> new ItemSeparacao(
                         a.volume(), a.sku(), a.descricao(),a.quantidade() + b.quantidade()));
    }

        Map<Integer, List<LinhaEtiqueta>> resultado = new TreeMap<>();
        for (Map.Entry<Integer, SortedMap<String, ItemSeparacao>> volume : agrupado.entrySet()){
            List<LinhaEtiqueta> linhas = new ArrayList<>();
            for (ItemSeparacao item : volume.getValue().values()) {
                Sku sku = Sku.de(item.sku());
                String cor = TabelaCores.buscar(sku.produto(), sku.codigoCor()).orElseGet(() -> {
                    avisos.add("volume" + item.volume() + ": cor não encontrada na tabela para esse SKU"
                              + item.sku() + "(Produto" + sku.produto() + ", código" + sku.codigoCor() + ")");
                    return "??? COR" + sku.codigoCor();
                });
                linhas.add(new LinhaEtiqueta(
                        sku.completo(), item.descricao(), cor, sku.tamanho(), item.quantidade()));
                }
                resultado.put(volume.getKey(), linhas);
                }
               return resultado;
        }
}
