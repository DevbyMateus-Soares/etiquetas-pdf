# etiquetas-pdf

Lê o Excel de separação (romaneio) e gera **um PDF por volume**, no layout do modelo `Vol07_PEDIDO06510.pdf`.

## Como usar

1. Coloque o `Arquivo_Separação.xlsx` na pasta do projeto (ao lado do `pom.xml`).
2. Abra `src/main/java/br/com/etiquetas/Main.java` e altere os dois campos do topo:
   ```java
   static final String PEDIDO = "4500402007";
   static final String NOTA_FISCAL = "000000724";
   ```
3. Rode a classe `Main` na IDE (IntelliJ/VS Code) ou pelo terminal:
   ```
   mvn compile exec:java
   ```
   Para usar outro Excel ou outra pasta de saída:
   ```
   mvn compile exec:java -Dexec.args="OutroArquivo.xlsx minha_saida"
   ```
4. Os PDFs saem na pasta `saida/`, com nomes como `Vol07_PEDIDO06510.pdf`.

## Requisitos

- JDK 25 (LTS). Se você usa outro JDK, troque `<java.version>` no `pom.xml` (ex.: 21).
- Maven 3.9.x (ou o Maven embutido da IDE).

## Regras de preenchimento

| Campo no PDF | Origem |
|---|---|
| MARCA | fixo: `ADIDAS` (`Main.MARCA`) |
| RAZAO_SOCIAL | fixo (`Main.RAZAO_SOCIAL`) |
| PEDIDO | `Main.PEDIDO` (você altera) |
| NOTA FISCAL | `Main.NOTA_FISCAL` (você altera) |
| SKU | coluna **Cód. Merc.** |
| DESCRICAO | coluna **Nome Mercadoria** |
| COR | `TabelaCores` (produto + código da cor, ambos tirados do SKU) |
| Numeração | texto depois do 2º hífen do **Cód. Merc.** (`4AM011-100-L` → `L`) |
| Quantidade | coluna **Qt. Separação** (somada se o mesmo SKU repetir no volume) |

O arquivo é dividido pela coluna **Vol.**: um PDF por volume.
O número do pedido usado no **nome do arquivo** (`PEDIDO06510`) é lido do próprio Excel (linha "Ped.: 06510").

## Estrutura

```
src/main/java/br/com/etiquetas/
  Main.java           campos editáveis e fluxo principal
  LeitorExcel.java    lê o .xlsx (Apache POI), localizando as colunas pelo nome
  Consolidador.java   agrupa por volume e SKU, e descobre a cor
  TabelaCores.java    tabela produto + código -> nome da cor (327 combinações)
  Sku.java            separa produto / cor / numeração
  GeradorPdf.java     desenha o PDF (Apache PDFBox)
  ItemSeparacao.java, LinhaEtiqueta.java   registros de dados
```

## Adicionar ou corrigir uma cor

Em `TabelaCores.java`, acrescente uma linha no bloco `DADOS`, no formato `PRODUTO;CODIGO;NOME`.
Se algum SKU não tiver cor na tabela, o PDF sai com `??? COR <código>` e o programa mostra um aviso no final.
