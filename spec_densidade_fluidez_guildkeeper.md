# Spec de Melhorias — Guildkeeper (Fase: Densidade Visual + Fluidez de Movimento)

**Contexto:** A transição para isométrico 2.5D funcionou estruturalmente (projeção, z-sorting e castelo 3D procedural estão corretos). Dois problemas identificados após teste: (1) o mapa está visualmente esparso — falta conteúdo/densidade — e (2) o movimento dos personagens está "engasgado", parecendo rodar a poucos FPS.

**Confirmado com o time:** manter estilo pixel art e paleta Dark Fantasy (dourado/azul místico/cinza escuro). Nenhuma mudança de direção de arte — o problema é volume de conteúdo, não estilo.

---

## PARTE 1 — Correção de fluidez de movimento (prioridade máxima)

**Diagnóstico:** O game loop atual roda a lógica de jogo inteira (posição, combate, decisões) em um único `tick()` de 1Hz (1x por segundo). Isso faz os heróis "saltarem" de posição a cada segundo em vez de se moverem suavemente — sensação de baixo FPS.

**Correção — separar lógica de renderização:**
- **Manter** o tick de lógica de jogo (combate, decisões de missão, economia, drops) rodando a 1Hz — não precisa mudar, não há ganho em rodar mais rápido.
- **Adicionar** uma camada de interpolação visual independente para posição de heróis/monstros no mapa:
  - Quando o tick de lógica decide que um herói vai da posição A para a posição B, a posição "visual" desse herói deve **interpolar suavemente** entre A e B ao longo do próximo segundo, atualizando a cada frame de renderização (idealmente via `Animatable<Offset>` do Compose, ou `withFrameNanos` para controle manual).
  - A posição usada em cálculos de jogo (colisão, chegada ao alvo) pode continuar sendo a posição lógica/discreta; apenas o *desenho na tela* usa a posição interpolada.
- Validar com o profiler do Android Studio se o Canvas está sendo redesenhado no framerate da tela (~60fps) e não apenas a cada tick de lógica.
- Checar também se elementos estáticos (ex: o `Path` do castelo 3D) estão sendo **recalculados a cada frame** — se sim, cachear o `Path` fora do bloco de desenho e só recriar quando necessário (ex: mudança de nível da guilda), não a cada recomposição.

## PARTE 2 — Densidade de conteúdo visual

**Diagnóstico:** Comparado a referências do gênero (mesmo em pixel art, como Stardew Valley/Moonlighter), o mapa atual tem terreno pouco variado e poucos elementos por área, resultando em grandes vazios escuros.

**Adições sugeridas, por prioridade:**

1. **Variedade de tiles de terreno** — hoje o chão é uma cor/textura única. Adicionar 3-4 variações de tile (grama escura, terra batida, pedra, musgo) distribuídas com leve aleatoriedade para quebrar a monotonia visual, mantendo a paleta escura já definida.
2. **Caminhos/trilhas conectando pontos de interesse** — trilhas de terra entre a Guilda e os pontos de spawn de monstro, reforçando a leitura do mapa e preenchendo o espaço vazio entre eles.
3. **Mais elementos decorativos estáticos** — dobrar a quantidade atual (de ~8-10 para ~20-25), variando tipos: arbustos, tocos de árvore, pedras de tamanhos diferentes, ossos/lápides espalhadas, pequenas poças d'água escuras (se fizer sentido no lore).
4. **Segundo plano com profundidade** — considerar uma camada de elementos mais distantes/menores nas bordas do mapa visível (silhuetas de montanhas ou floresta densa ao fundo) para dar sensação de mundo maior do que a área jogável.
5. **NPCs ambiente (opcional, avaliar custo):** pequenos elementos animados não-interativos no fundo (pássaro voando, folha caindo) para dar sensação de "mundo vivo" sem custo de lógica de jogo.

---

## Ordem de implementação sugerida

1. Corrigir interpolação de movimento (Parte 1) — é o problema mais perceptível e mais barato de resolver.
2. Cachear elementos estáticos pesados (Path do castelo) — checagem rápida de performance.
3. Variedade de tiles de terreno.
4. Trilhas conectando pontos de interesse.
5. Expandir elementos decorativos.
6. (Opcional) camada de fundo distante + NPCs ambiente.

## Critério de aceite

- [ ] Heróis se movem de forma visivelmente suave no mapa, sem "saltos" perceptíveis
- [ ] Nenhuma queda de FPS perceptível mesmo com mais elementos decorativos em tela
- [ ] Mapa não tem mais grandes áreas de vazio escuro sem elementos
- [ ] Terreno tem variação visual perceptível (não é mais uma cor/textura única)
- [ ] Paleta permanece fiel ao Dark Fantasy (dourado/azul místico/cinza escuro) — nenhuma cor vibrante tipo "grama verde clara" foi introduzida
