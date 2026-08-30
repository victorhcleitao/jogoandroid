# Spec — Curadoria de Sprites (pasta-repositório → projeto)

## Estrutura de pastas

- **Repositório bruto (não mexer, não deletar nada aqui):** `G:\Projetos\Guildkeeper\Sprites`
  Fica como está — acervo de tudo que foi baixado, pra referência futura e reaproveitamento.
- **Destino final no projeto:** `G:\Projetos\Guildkeeper\app\src\main\assets\sprites\`
  Só recebe **cópias** (não mover) dos arquivos aprovados na curadoria, já renomeados e organizados em subpastas (`herois/`, `monstros/`, `ambiente/`).

## Critérios de seleção (aplicar por imagem, não por nome de arquivo)

Antes de aprovar um sprite, verificar visualmente:

1. **É pixel art de verdade?** — descartar qualquer asset vetorial, "flat design" ou estilo pintado/suavizado. Tem que ter os pixels visíveis/quadrados, sem anti-aliasing suave.
2. **Resolução compatível** — idealmente múltiplo de 32x32 (16x16, 32x32, 64x64 são fáceis de escalar; resoluções "quebradas" tipo 30x45 geram distorção). Anotar a resolução real de cada aprovado.
3. **Fundo transparente** — arquivo precisa ter canal alpha (PNG transparente), não fundo branco/colorido sólido atrás do personagem.
4. **Compatibilidade de paleta** — não precisa ser idêntico, mas não pode destoar visualmente da paleta Dark Fantasy já em uso (dourado/azul místico/cinza escuro). Descartar packs muito coloridos/"fofos"/cartunescos claros.
5. **Estados de animação presentes** — checar se o sprite sheet já separa idle/andando/atacando em frames distintos, ou se é só uma pose única. Anotar o que cada asset aprovado oferece (isso decide o que ainda falta produzir depois).

## O que fazer com cada categoria necessária

Usar a lista de assets da spec anterior (`spec_sprites_guildkeeper.md`) como checklist do que procurar:
- Heróis: Guerreiro, Maga, Arqueiro, Clériga
- Monstros: Slime, Lobo, Goblin
- Ambiente: árvores secas, rochas, tocha (com frame de chama, se possível)

Para cada item da lista:
- Se encontrar um sprite adequado no repositório → copiar pra pasta de destino correta, renomear em `snake_case` minúsculo (ex: `guerreiro_andando.png`), registrar de onde veio (nome da pasta original) para referência de licença.
- Se **não** encontrar nada adequado no repositório → deixar como pendente e sinalizar de volta (não forçar um asset ruim só pra preencher a lista).
- Se encontrar **mais de uma opção boa** para o mesmo papel → escolher a que tiver mais estados de animação completos; se empate, escolher a que melhor combina com a paleta já em uso. Não integrar duas opções concorrentes ao mesmo tempo.

## Licenciamento

Ao aprovar um asset, anotar (em comentário no código ou arquivo `CREDITS.md` na raiz do projeto) o nome do pack/autor original e o tipo de licença (CC0, uso livre com crédito, etc.) — mesmo em fase de hobby, evita retrabalho se o projeto crescer depois.

## Saída esperada desta etapa

Um relatório simples do Antigravity indicando:
- O que foi aprovado e copiado (com origem)
- O que ficou pendente (nenhum candidato adequado encontrado)
- Qualquer conflito (duas opções boas pro mesmo papel, escolha feita e por quê)
