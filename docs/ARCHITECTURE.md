# Arquitetura

O código do SmartInventory usa o pacote raiz `com.smartinventory.mod` e
separa responsabilidades por área:

- `com.smartinventory.mod`: inicialização comum do mod.
- `com.smartinventory.mod.client`: inicialização e eventos exclusivos do
  cliente.
- `com.smartinventory.mod.gui`: telas e interação visual.
- `com.smartinventory.mod.hud`: registro e renderização do HUD.
- `com.smartinventory.mod.inventory`: análise, prévia e organização do
  inventário.
- `com.smartinventory.mod.config`: leitura e gravação das configurações e
  favoritos.

Os recursos permanecem em `src/main/resources`, incluindo
`fabric.mod.json` e os assets do mod (ícone e traduções). Esta reorganização
altera apenas pacotes, caminhos e imports; os algoritmos de inventário e a
configuração do Fabric permanecem os mesmos.
