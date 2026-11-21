# SmartInventory

[![build](https://github.com/V1n1c1u5P4iva/SmartInventory/actions/workflows/build.yml/badge.svg)](https://github.com/V1n1c1u5P4iva/SmartInventory/actions/workflows/build.yml)

SmartInventory é um mod [Fabric](https://fabricmc.net/) para Minecraft Java
26.3 que ajuda a analisar e organizar o inventário com segurança.

## Funcionalidades

- Organização automática do inventário, com prévia antes de aplicar
- Junção de pilhas compatíveis
- Slots favoritos protegidos (não são movidos pela organização)
- Análise do inventário e lista de itens repetidos
- HUD de ocupação configurável
- Traduções em português (pt_br) e inglês (en_us)

## Atalhos

| Tecla | Ação                         |
|-------|------------------------------|
| J     | Abrir o painel               |
| H     | Mostrar/ocultar o HUD        |
| K     | Configurações do HUD         |
| B     | Cancelar a organização       |

Os atalhos podem ser alterados em *Opções → Controles*.

## Instalação

1. Instale o [Fabric Loader](https://fabricmc.net/use/) para Minecraft 26.3.
2. Baixe o [Fabric API](https://modrinth.com/mod/fabric-api) e coloque na
   pasta `mods`.
3. Baixe o `.jar` do SmartInventory na página de
   [Releases](https://github.com/V1n1c1u5P4iva/SmartInventory/releases) e
   coloque na pasta `mods`.

## Compilar a partir do código

Requisitos: Java 25.

```powershell
.\gradlew.bat clean build      # Windows
./gradlew clean build          # Linux/macOS
```

O jar fica em `build/libs/`. Para abrir o cliente de desenvolvimento:

```powershell
.\gradlew.bat runClient
```

## Estrutura

A organização dos pacotes está documentada em
[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md). O checklist de validação manual
está em [docs/TESTING.md](docs/TESTING.md).

## Licença

Distribuído sob a licença MIT. Veja [LICENSE](LICENSE).
