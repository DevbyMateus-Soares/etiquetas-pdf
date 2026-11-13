package br.com.etiquetas;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Lê o Excel do romaneio de separação (Arquivo_Separação.xlsx).
 * <p>
 * As colunas são localizadas pelo NOME do cabeçalho (e não pela posição),
 * então o programa continua funcionando se uma coluna mudar de lugar.
 * Colunas usadas: "Vol.", "Cód. Merc.", "Nome Mercadoria" e "Qt. Separação".
 */
public class LeitorExcel {

    public record DadosSeparacao(String pedidoRomaneio, List<ItemSeparacao> itens) {
    }

    private static final String COL_VOLUME = "vol.";
    private static final String COL_SKU = "cod. merc.";
    private static final String COL_DESCRICAO = "nome mercadoria";
    private static final String COL_QUANTIDADE = "qt. separacao";

    private static final int LINHAS_PARA_PROCURAR_CABECALHO = 30;
    private static final Pattern PADRAO_PEDIDO = Pattern.compile("Ped\\.:\\s*(\\d+)");

    private final DataFormatter formatador = new DataFormatter();

    public DadosSeparacao ler(Path arquivo) throws IOException {
        try (InputStream entrada = Files.newInputStream(arquivo);
             Workbook planilha = WorkbookFactory.create(entrada)) {


            Sheet aba = planilha.getSheetAt(0);
            int linhaCabecalho = acharLinhaCabecalho(aba);
            Map<String, Integer> colunas = mapearColunas(aba.getRow(linhaCabecalho));

            String pedidoRomaneio = acharPedidoRomaneio(aba, linhaCabecalho);

            List<ItemSeparacao> itens = new ArrayList<>();
            for (int i = linhaCabecalho + 1; i <= aba.getLastRowNum(); i++) {
                Row linha = aba.getRow(i);
                if (linha == null) {
                    continue;
                }
                String textoVolume = texto(linha, colunas.get(COL_VOLUME));
                String sku = texto(linha, colunas.get(COL_SKU));
                String descricao = texto(linha, colunas.get(COL_DESCRICAO));
                String textoQuantidade = texto(linha, colunas.get(COL_QUANTIDADE));

                if (textoVolume.isEmpty() && sku.isEmpty()) {
                    continue;
                }
                try {

                    itens.add(new ItemSeparacao(
                            Integer.parseInt(textoVolume), sku, descricao, Integer.parseInt(textoQuantidade)));
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Linha " + (i + 1) + " do Excel com volume (\"" + textoVolume
                            + "\") ou quantidade (\"" + textoQuantidade + "\") inválidos.", e);
                }
            }
            return new DadosSeparacao(pedidoRomaneio, itens);
        }
    }

    /**
     * procura nas primeiras linhas a que contém os títulos das colunas.
     */
    private int acharLinhaCabecalho(Sheet aba) {
        int limite = Math.min(aba.getLastRowNum(), LINHAS_PARA_PROCURAR_CABECALHO);
        for (int i = 0; i <= limite; i++) {
            Row linha = aba.getRow(i);
            if (linha != null && mapearColunas(linha).keySet()
                    .containsAll(List.of(COL_VOLUME, COL_SKU, COL_DESCRICAO, COL_QUANTIDADE))) {
                return i;
            }
        }
        throw new IllegalStateException("Não encontrei o cabeçalho com as colunas \"Vol.\", \"Cód. Merc.\", "
                + "\"Nome Mercadoria\" e \"Qt. Separação\" nas primeiras " + LINHAS_PARA_PROCURAR_CABECALHO
                + " linhas do Excel.");
    }

    private Map<String, Integer> mapearColunas(Row cabecalho) {
        Map<String, Integer> colunas = new HashMap<>();
        for (Cell celula : cabecalho) {
            colunas.putIfAbsent(normalizar(formatador.formatCellValue(celula)), celula.getColumnIndex());
        }
        return colunas;
    }

    /**
     * Lê o numero do pedido do romanio (ex: Ped:06510) nas linhas acima do cabeçalho.
     */
    private String acharPedidoRomaneio(Sheet aba, int linhaCabecalho) {
        for (int i = 0; i < linhaCabecalho; i++) {
            Row linha = aba.getRow(i);
            if (linha == null) {
                continue;
            }
            for (Cell celula : linha) {
                String conteudo = formatador.formatCellValue(celula).replace('\u00a0', ' ');
                Matcher m = PADRAO_PEDIDO.matcher(conteudo);
                if (m.find()) {
                    return m.group(1);
                }
            }
        }
        return null;
    }

    private String texto(Row linha, int coluna) {
        Cell celula = linha.getCell(coluna);
        return celula == null ? "" : formatador.formatCellValue(celula).replace('\u00a0', ' ').trim();
    }

    /**
     * remove acentos, espaços repetidos e maiúsculas: "Cód. Merc" vira "cod.merc,".
     */

    private static String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.replace('\u00a0', ' ').replaceAll("\\s+", " ").trim().toLowerCase();

    }
}
