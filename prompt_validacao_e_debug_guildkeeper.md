# Prompt para Antigravity — Validação Consolidada + Modo Debug

Antes de seguirmos para novas features, preciso fechar alguns pontos que ficaram sem confirmação visual, e também gostaria de um modo de debug para facilitar validações futuras sem depender só de descrição em texto.

---

## PARTE 1 — Itens pendentes de confirmação (todos precisam de print, não só descrição)

### 1.1 — Hierarquia de tamanho Guilda vs. Prédios (pendente há duas rodadas)
Os últimos números reportados foram: Guilda Lvl 1 = 34f de altura, Ferraria = 75f, Mercador = 70f. Isso indicaria os prédios auxiliares **maiores** que a Guilda central, o que contraria a regra combinada (Guilda > Edifícios > Heróis/Monstros > Decoração).
**Pedido:** enviar print mostrando a Guilda e pelo menos um prédio construído lado a lado na mesma tela. Se os prédios estiverem realmente maiores, ajustar a escala deles para ficar abaixo da altura atual da Guilda no nível correspondente.

### 1.2 — Economia de missões de coleta
Foi reportado que a fórmula de 50-65% do valor esperado de material foi aplicada às missões de coleta (Madeira, Pedra, Ervas, Ferro).
**Pedido:** print de um ciclo completo de uma missão de coleta (publicar contrato → completar → vender material) mostrando que o saldo final da guilda não fica negativo.

### 1.3 — Roster completo de sprites
Foi reportado que Maga, Arqueiro e Clériga agora têm sprite PNG real (antes só Guerreiro e Orc tinham).
**Pedido:** print do mapa ou da tela da Guilda mostrando os 4 heróis (ou pelo menos os que estiverem ativos) com sprite real visível, sem fallback procedural.

### 1.4 — Ícone e tela de carregamento
Foi reportado que o ícone "Brasão + Chave" e a tela de loading (fogueira + dicas) foram implementados.
**Pedido:** print do ícone do app na tela inicial do celular (launcher), e print/gravação curta da tela de carregamento em ação.

### 1.5 — Build v0.1.0
**Pedido:** confirmar se o APK `guildkeeper-v0.1.0-*.apk` gerado é o que está instalado no celular de teste, ou se há uma build mais recente não versionada rodando por cima.

---

## PARTE 2 — Modo Debug (novo pedido)

Para reduzir a dependência de screenshots manuais em validações futuras, gostaria de um **modo debug** ativável (ex: um toggle escondido nas Configurações, ou um long-press em algum canto da tela) que exiba, sobreposto ao jogo:

1. **Overlay de dimensões de sprite:** ao ativar, mostrar ao lado de cada sprite renderizado no mapa (herói, monstro, prédio, decoração) um pequeno texto com sua altura final em pixels na tela naquele momento (já considerando trim + escala + zoom atual). Isso torna qualquer problema de hierarquia de tamanho visível imediatamente, sem precisar comparar números escritos em relatório com o que aparece na tela.
2. **Log de cálculo de contrato:** ao gerar/publicar um novo contrato, exibir (no Log de Eventos existente, ou num console debug separado) o cálculo usado: valor esperado do material, fração aplicada, e o custo final resultante. Isso permite auditar rapidamente se a fórmula está sendo aplicada corretamente em qualquer categoria de missão, atual ou futura.
3. **Indicador de FPS/tick:** um contador simples de FPS de renderização e o tempo desde o último tick de lógica, útil para detectar regressões de fluidez sem precisar "sentir" no dedo.

**Importante:** esse modo debug deve ser fácil de ligar/desligar e não deve aparecer para o jogador comum — é uma ferramenta nossa de desenvolvimento, não uma feature de produto.

---

## Ordem sugerida

1. Implementar o modo debug primeiro (Parte 2) — ele mesmo vai ajudar a verificar os itens da Parte 1 mais rápido.
2. Usar o modo debug pra confirmar/corrigir os itens 1.1 e 1.2 (os dois com maior risco de estarem errados).
3. Confirmar 1.3, 1.4 e 1.5 com prints simples, sem necessidade do debug mode pra esses.

Depois de tudo confirmado, seguimos para a próxima etapa de conteúdo (Expedições).
