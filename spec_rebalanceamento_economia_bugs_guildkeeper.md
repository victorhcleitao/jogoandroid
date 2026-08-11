# Spec — Guildkeeper: Rebalanceamento Econômico + Bugs Visuais

## Contexto

O sistema de caução de contratos (guilda paga adiantado, recebe material, vende pra se capitalizar de novo) é a mecânica pretendida e deve ser **mantida** — não reverter. O problema identificado é de **calibração numérica**: o custo do contrato foi definido de forma independente do valor de material que a caçada correspondente realmente gera, causando um soft-lock econômico (guilda fica sem ouro suficiente pra publicar qualquer contrato novo, mesmo vendendo todo o estoque).

**Exemplo do bug observado:** contrato de Slime custa 30g de caução. Guilda tinha 5g em caixa + estoque vendável totalizando 23g (6x Gel de Slime a 3g + 1x Pele de Lobo a 5g). Mesmo vendendo tudo, 28g < 30g — impossível publicar o próximo contrato.

---

## PARTE A — Rebalanceamento econômico (prioridade máxima)

### A.1 — Custo de contrato derivado do valor esperado de material

Em vez de valores fixos escolhidos manualmente para `goldReward`/custo de caução por contrato, calcular a partir da tabela de `LootDrop` já existente em `Monster.kt`:

- Calcular o **valor médio esperado de material** por caçada daquele tipo de monstro (soma de `chance_de_drop * quantidade * preço_de_venda` para cada item possível na tabela de loot).
- Definir o custo de caução do contrato como uma **fração** desse valor esperado (sugestão inicial: 50-65% do valor médio de material) — isso garante margem de lucro pra guilda na maioria das caçadas, sem exigir sorte acima da média pra se manter sustentável.
- Reward do herói pode continuar vindo de uma parte desse valor (ex: os 70% que já foi implementado antes) — o ponto principal é garantir que **custo de caução ≤ valor esperado de material**, nunca o contrário.

### A.2 — Contrato "iniciante" sempre disponível

Independente da fórmula acima, garantir que exista sempre pelo menos um contrato de custo baixo o suficiente para ser pago com o ouro inicial padrão de uma guilda nova (hoje 5g), evitando qualquer cenário de trava total no início de uma run nova. Pode ser um contrato de recompensa/caução simbólica (1-2g) contra o monstro mais fraco disponível.

### A.3 — Validação

Depois de implementado, simular manualmente (ou via teste automatizado simples) o ciclo: guilda começa com X ouro → publica contrato mais barato disponível → recebe material → vende → confirma que o saldo resultante é suficiente para publicar o próximo contrato na sequência, em pelo menos um caminho possível. Isso evita reintroduzir o soft-lock.

---

## PARTE B — Bugs visuais (reincidência + novo)

### B.1 — Overlap de labels voltou a acontecer

Nos prints mais recentes, Arthur e Valeria aparecem novamente empilhados na mesma posição, com o texto "Guilda Lvl 1" cortado atrás deles. O sistema de espalhamento em 4 posições (NO/NE/SO/SE) que foi implementado e reportado como corrigido em sessão anterior não está ativo neste build, ou regrediu. Verificar se a lógica ainda existe no código e por que não está sendo aplicada neste cenário específico (pode ser um caso não coberto, como heróis retornando de caçada versus heróis recém-criados).

### B.2 — Texto "Arthur" quebrando letra por letra na vertical (bug novo)

No combate, o nome "Arthur" está sendo desenhado uma letra por linha, empilhado verticalmente ao lado do personagem, em vez de horizontal numa linha só. Isso é um padrão clássico de container/`Text` composable com largura insuficiente forçando quebra de linha por caractere. Verificar o componente responsável por desenhar o nome do herói durante o combate — provavelmente falta `Modifier.width` adequado, ou está usando um container de largura fixa pequena demais herdado de outro contexto (ex: copiou o estilo do label de repouso na guilda, que tem espaço mais restrito).

---

## Critério de aceite

- [ ] Simulação de ciclo completo (contrato → caçada → venda → novo contrato) não resulta em travamento em nenhum ponto testado
- [ ] Existe contrato acessível com o ouro inicial de uma guilda nova
- [ ] Print mostrando Arthur e Valeria com labels legíveis, sem sobreposição, em cenário de descanso na guilda
- [ ] Print mostrando nome "Arthur" (ou qualquer herói) em uma linha horizontal única durante combate, sem quebra por caractere
