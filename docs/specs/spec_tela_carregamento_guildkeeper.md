# Spec — Guildkeeper: Tela de Carregamento (Loading Screen)

## Conceito

A tela de carregamento é o segundo ponto de "primeira impressão" do jogo (depois do ícone) — vale usar pra reforçar a identidade Dark Fantasy antes mesmo do jogador ver o mapa. Como o carregamento real (ler `SharedPreferences`) é praticamente instantâneo, essa tela funciona mais como um **momento de marca** do que uma necessidade técnica — por isso vale ter uma duração mínima artificial, pra não passar em 50ms e virar um "flash" sem graça.

## Direção visual

- Fundo: mesmo gradiente radial azul-marinho → preto já usado no mapa (consistência com o resto do jogo).
- Centro: o emblema do ícone (Brasão + Chave, quando finalizado) — grande, centralizado.
- Abaixo do emblema: nome "Guildkeeper" na wordmark serifada dourada já definida.
- Elemento de progresso: em vez de uma barra de loading genérica, considerar algo temático — sugestões, do mais simples ao mais elaborado:
  1. **Chama pulsante** (mais simples): uma pequena chama de tocha pulsando enquanto carrega, sem indicador numérico — só transmite "algo está acontecendo".
  2. **Barra estilizada como corrente/runa**: uma barra horizontal fina, dourada, preenchendo da esquerda pra direita, com textura de corrente de metal ou runas se acendendo em sequência.
  3. **Texto de dica rotativo** (opcional, adiciona personalidade): abaixo da barra, uma frase curta de "lore" trocando a cada carregamento, tipo tela de loading de RPG clássico.

**Recomendação:** combinar 1 (chama pulsante, barata de fazer) + 3 (dica de texto, dá personalidade sem exigir asset novo) — bom custo-benefício antes de partir pra algo mais elaborado como a opção 2.

### Exemplos de frases de dica (ajustar tom conforme preferir)
- "Heróis descansam mais rápido perto de uma fogueira acesa."
- "A Ferraria melhora o equipamento — e o equipamento melhora a sobrevivência."
- "Nem todo contrato vale a pena aceitar sem ouro de sobra no cofre."
- "Orcs só respeitam guildas de Rank D ou superior."

## Implementação técnica (Compose)

- **Splash nativo do Android (system splash, API 31+):** cobre o instante entre o toque no ícone e o app abrir de fato — configurar com o ícone/cor de fundo da marca via `SplashScreen` API, mas isso é curto e não controlável em duração.
- **Tela de loading in-app (o que de fato dá controle criativo):** uma `Composable` exibida antes da `MainScreen`, controlando:
  - Duração mínima artificial (ex: 900ms-1200ms) mesmo que o carregamento real termine antes, via `delay()` numa coroutine, garantindo que a marca apareça por tempo suficiente pra ser percebida sem parecer trava.
  - Animação da chama usando `rememberInfiniteTransition` (Compose) — variação sutil de escala/opacidade em loop, sem precisar de sprite sheet animado se for feito via forma geométrica simples (ou usar o sprite de fogueira que já existe no projeto, reaproveitando asset).
  - Frase de dica sorteada aleatoriamente de uma lista fixa a cada abertura do app.
- Transição de saída: fade suave para a tela do Mapa/Guilda, evitando corte abrupto.

## Ordem sugerida

1. Fechar primeiro a direção do ícone (spec anterior) — a tela de loading depende do emblema final pra não ficar com asset provisório em dois lugares diferentes.
2. Implementar versão simples (chama + dica de texto) via Compose puro, sem depender de asset novo gerado por IA.
3. (Opcional, depois) revisar pra versão mais elaborada (barra de runas) se o resultado simples não satisfizer.

## Critério de aceite

- [ ] Tela aparece por tempo suficiente para ser lida/percebida, mesmo em carregamento instantâneo
- [ ] Emblema e wordmark consistentes com o ícone final aprovado
- [ ] Nenhum travamento perceptível na transição de entrada/saída da tela
- [ ] Frase de dica legível e sem erro de português/tipografia
