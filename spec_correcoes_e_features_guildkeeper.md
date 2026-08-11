# Spec — Guildkeeper: Correção de Discrepâncias + Novas Features

## PARTE 0 — Antes de tudo: investigar as discrepâncias do relatório anterior

O `walkthrough_jogo.md` mais recente afirma integração completa de sprites PNG (Guerreiro, Orc, fogueiras, pedras), mas o teste no dispositivo mostra:
- Arthur (Guerreiro) não renderiza em lugar nenhum do mapa, apesar do contador indicar 1 herói em caçada.
- Orc não aparece como monstro em nenhum spawn.
- Fogueiras/tochas duplicadas incorretamente ao longo de toda a trilha de terra (parecem estar sendo desenhadas por tile do caminho, não nas posições fixas pretendidas).

**Ação solicitada:** investigar e corrigir antes de seguir para as novas features, com print de tela confirmando cada correção:
1. Por que o Arthur não desenha (nem o PNG, nem o fallback procedural)? Checar se há exceção sendo capturada silenciosamente no carregador de assets ou no cálculo de posição.
2. Confirmar se o Orc está de fato sendo spawnado em algum ponto do ciclo de jogo, ou se só foi importado como asset mas nunca ligado à lógica de spawn.
3. Corrigir o posicionamento das fogueiras/tochas para aparecerem apenas nos pontos pretendidos (entrada da guilda + entrada de cada spawn), não repetidas ao longo da trilha inteira.

**Regra de processo daqui pra frente:** toda entrega visual deve incluir print de tela como evidência, além da confirmação de build bem-sucedido. "Compilou sem erro" não é o mesmo que "está funcionando visualmente como pedido".

---

## PARTE 1 — Zoom in/out no mapa

- Implementar gesto de pinça (`detectTransformGestures` no `pointerInput`) sobre a área do Canvas do mapa.
- Adicionar estado mutável de escala (`remember { mutableStateOf(1f) }`), aplicado como multiplicador final sobre as coordenadas já projetadas isometricamente (depois do cálculo de `screenX`/`screenY`, não antes — evita distorcer a proporção do losango).
- Definir limites: zoom mínimo ~0.6x (visão geral) e máximo ~2.2x (detalhe de perto). Fora desses limites, ignorar o gesto.
- Junto ao zoom, permitir arrastar o mapa (pan) — sem isso, dar zoom in vai cortar partes do mapa da área visível sem forma de navegar até lá. Adicionar offset de câmera (`Offset` mutável) somado ao resultado final da projeção.
- Opcional: duplo toque para resetar zoom/pan ao padrão (centrado na guilda, escala 1x).

## PARTE 2 — Spawn aleatório de monstros com distância mínima do assentamento

- Substituir as posições fixas atuais dos spawns (Slime, Lobo, Goblin sempre nos mesmos pontos) por posição aleatória a cada spawn/respawn.
- Algoritmo sugerido:
  - Definir `minDistance` e `maxDistance` (em unidades lógicas do mapa, não pixels de tela) a partir do centro da guilda `(300, 300)` — ex: mínimo 150, máximo 400 (ajustar por playtesting).
  - Sortear ângulo aleatório entre 0° e 360°, sortear distância aleatória dentro do intervalo `[minDistance, maxDistance]`.
  - Calcular posição: `x = 300 + distancia * cos(angulo)`, `y = 300 + distancia * sin(angulo)`.
  - Validar que a posição sorteada não colide com elementos decorativos estáticos (árvores, rochas) — se colidir, sortear novamente (limitar a poucas tentativas pra não travar).
- Quando um monstro for derrotado, o próximo da mesma espécie deve spawnar em nova posição aleatória (não retornar ao ponto fixo antigo).

## PARTE 3 — Sprite central progressivo por nível + prédios modulares

**Conceito:** a Guilda começa visualmente simples (nível 1) e o modelo evolui conforme sobe de nível. Prédios construídos (Ferraria, etc.) aparecem como estruturas adicionais ao lado da Guilda, não substituindo o modelo central.

- **Guilda (estrutura central):**
  - Nível 1: modelo simples (ex: tenda/casebre de madeira — mais simples que o castelo atual de 3 torres).
  - Nível 2+: evolução gradual (adicionar muralha, depois torres, até chegar no castelo elaborado de 3 torres já implementado como "nível máximo" ou nível intermediário alto).
  - Implementação sugerida: função que recebe `guildLevel` e retorna qual variante de `Path`/geometria desenhar — reaproveitando a técnica já usada no castelo atual (blocos com face esquerda/direita/topo sombreados), só variando a complexidade da estrutura por nível.
- **Prédios modulares (Ferraria e futuros):**
  - Cada prédio construído ganha uma posição fixa de "slot" ao redor do pátio da guilda (semelhante ao sistema de 4 posições já implementado para heróis descansando — mas usar posições diferentes, para não sobrepor).
  - Só desenhar o prédio se `isConstructed == true` para aquele tipo.
  - Cada prédio pode ter seu próprio mini-modelo geométrico simples (não precisa ser tão elaborado quanto a Guilda central) ou usar sprite PNG se/quando disponível.
  - Estrutura de dados sugerida: lista de `BuildingSlot(tipo, offsetX, offsetY, isConstructed)` associada ao `GameState`, iterada no desenho do mapa.

## PARTE 4 — Correção de drop: monstros devem soltar material, não dinheiro direto

**Bug de regressão:** o plano original especificava que monstros derrotados soltam **materiais** (Gel de Slime, Pele de Lobo, Garra de Fera, Minério de Ferro), que os heróis carregam de volta e trocam por ouro na Guilda — não ouro direto no momento da morte do monstro. O comportamento atual parece ter pulado essa etapa.

- Verificar a função de recompensa de combate no `GameViewModel` — hoje parece estar creditando ouro diretamente ao derrotar o monstro.
- Corrigir para: monstro derrotado → adiciona item de material ao inventário do herói (usando a tabela de `LootDrop` já definida em `Monster.kt`) → herói retorna à Guilda → materiais são convertidos em ouro (e/ou pontos de reputação) na entrega, conforme já descrito no plano original.
- Isso também é importante para o sistema de Ferraria funcionar como planejado — a compra de armas/armaduras depende do fluxo de "materiais vendidos geram ouro", então um bug aqui pode estar mascarando outros problemas na cadeia econômica do jogo.

---

## Ordem de execução sugerida

1. Investigar e corrigir as discrepâncias da Parte 0 (bloqueante — não faz sentido empilhar mais features em cima de um estado que não bate com o relatado).
2. Corrigir o drop de material (Parte 4) — é bug de lógica central da economia, prioridade alta.
3. Zoom/pan (Parte 1) — melhoria de UX, independente do resto.
4. Spawn aleatório (Parte 2).
5. Sprite progressivo + prédios modulares (Parte 3) — a mais trabalhosa, fazer por último.

## Critério de aceite

- [ ] Print de tela mostrando Arthur visível e se movendo no mapa
- [ ] Print de tela mostrando Orc como monstro válido em algum spawn
- [ ] Fogueiras aparecem só nas posições pretendidas, não repetidas pela trilha
- [ ] Pinça para zoom funciona com limites sensatos, mapa pode ser arrastado
- [ ] Monstros spawnam em posições variadas a cada morte, respeitando distância mínima/máxima da guilda
- [ ] Guilda nível 1 visualmente mais simples que o castelo atual; evolui ao subir de nível
- [ ] Ferraria (quando construída) aparece como estrutura visível ao lado da guilda
- [ ] Combate concede material ao herói, não ouro direto; ouro só aparece após venda na guilda
