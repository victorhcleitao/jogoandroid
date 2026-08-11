# Spec de Melhorias Visuais — Guildkeeper (Fase: Atmosfera do Mapa)

**Contexto:** O game loop, combate e persistência já estão funcionando (BUILD SUCCESSFUL). Esta spec cobre exclusivamente a camada visual do `MapScreen.kt`, que hoje está com fundo preto liso e grid de debug — abaixo do padrão "Dark Fantasy premium" definido no plano original.

**Arquivos afetados:** `ui/screens/MapScreen.kt`, `theme/Color.kt`, `theme/Theme.kt`

**Paleta já estabelecida:** dourado (guilda/UI), azul místico, cinza escuro. Usar essas 3 famílias de cor em todo o trabalho abaixo — não introduzir cores novas fora dessa paleta.

---

## 1. Fundo do mapa (prioridade máxima — maior impacto x menor esforço)

**Problema:** fundo preto puro (`#000000` ou próximo) com grid genérico de debug visível.

**Solução:**
- Substituir o preto sólido por um **gradiente radial** partindo do centro do mapa (posição da Guilda) para as bordas:
  - Centro: azul-marinho escuro suave (`~#1A2238` ou similar, ajustar ao tom místico já definido em `Color.kt`)
  - Bordas: preto quase puro (`~#0A0A0F`)
- Reduzir a opacidade/contraste das linhas de grid atuais em ~60-70% — hoje competem visualmente com os sprites. O grid pode continuar existindo como guia sutil, mas não deve ser o elemento mais visível da tela.
- Adicionar uma leve textura de "terreno" por baixo do grid: pode ser tiles de pixel art simples (grama escura/terra) desenhados via `Canvas`, repetidos em padrão, ou um `Brush` com ruído sutil se pixel art de tile for trabalho demais nesta fase.

## 2. Halo de luz na Guilda

**Problema:** o castelo da Guilda está solto no mapa, sem se destacar como o "porto seguro".

**Solução:**
- Desenhar um **glow radial dourado** atrás/ao redor do sprite da Guilda, com raio aproximado de 2-3x o tamanho do sprite, opacidade baixa (~15-25%), fade suave nas bordas.
- Opcional (nice-to-have, não bloqueante): leve pulsação de opacidade (`animateFloat` infinito, ciclo de ~3s) para dar sensação de "vida" ao halo.

## 3. Densidade visual — elementos decorativos estáticos

**Problema:** grandes áreas vazias no mapa quebram a imersão.

**Solução:**
- Adicionar 8-15 elementos decorativos estáticos (não-interativos) espalhados pelo mapa em posições fixas ou levemente randomizadas por seed:
  - Árvores mortas / silhuetas de árvore (tom cinza-azulado escuro)
  - Rochas/pedregulhos
  - Tochas acesas próximas à Guilda (pontinho de luz laranja/dourado, reforça o tema)
  - Cercas ou ruínas simples ao redor do perímetro
- Esses elementos devem ficar **atrás** dos heróis/monstros na ordem de desenho (z-index), nunca sobrepor sprites interativos.
- Manter estilo pixel art consistente com Hero.kt/Monster.kt já existentes (mesma resolução de matriz de pixels).

## 4. Feedback de combate

**Problema:** barra de vida do monstro é só uma linha vermelha fina; sem números de dano, sem impacto visual no golpe.

**Solução:**
- **Números de dano flutuantes:** ao registrar um golpe no `GameViewModel`, disparar um texto (`-X`) que nasce na posição do alvo e sobe ~30-40px em ~0.6-0.8s com fade-out, cor branca ou vermelha clara para dano recebido, dourada/verde para cura (Clérigo).
- **Flash no impacto:** o sprite atingido pisca brevemente (alterar alpha ou tint por 1-2 frames) no momento do golpe.
- **Micro-shake de tela (opcional, avaliar custo):** leve deslocamento de câmera de poucos pixels por ~100ms ao herói desferir um golpe crítico ou ao levar dano — só aplicar se não impactar performance em dispositivos mais fracos.
- Melhorar a barra de vida atual: adicionar borda/contorno escuro e leve gradiente na barra (não precisa ser lisa vermelha), e considerar mostrar o valor numérico (HP atual/máximo) como texto pequeno acima ou dentro da barra.

## 5. Trilha do herói (ajuste fino, baixa prioridade)

**Problema:** linha tracejada fica "solta" num fundo vazio.

**Solução:** nenhuma mudança estrutural necessária — deve se resolver naturalmente assim que os itens 1-3 forem implementados (o path vai se integrar visualmente ao invés de flutuar num vazio). Se ainda destoar depois, considerar reduzir opacidade da linha ou trocar o tom vermelho por um tom mais discreto (cinza-azulado) que combine com a paleta.

---

## Ordem de implementação sugerida

1. Gradiente + suavização do grid de fundo
2. Halo de luz da Guilda
3. Elementos decorativos estáticos
4. Números de dano flutuantes + flash de impacto
5. (Opcional) micro-shake de câmera
6. Reavaliar a trilha do herói após os itens acima

## Critério de aceite

Rodar o mapa lado a lado com o print atual (Guilda Lvl 1, heróis Arthur/Valeria, monstros Slime/Lobo/Goblin) e confirmar visualmente:
- [ ] Fundo não é mais preto liso — tem gradiente e/ou textura visível
- [ ] Guilda se destaca claramente como ponto focal (halo visível)
- [ ] Mapa não parece "vazio" — pelo menos 8 elementos decorativos distribuídos
- [ ] Um golpe em combate gera número flutuante visível
- [ ] Nenhuma queda perceptível de performance/FPS no emulador
