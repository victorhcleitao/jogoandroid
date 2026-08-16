# Spec — Guildkeeper: Normalização de Escala de Sprites + Rebalanceamento de Coleta

## PARTE A — Normalização de escala de sprites (bug visual)

**Diagnóstico:** cada fonte de sprite (packs curados, ComfyUI, PNG do herói) tem resolução nativa diferente, e cada uma recebeu um multiplicador de escala manual (`sizeMultiplier`) escolhido isoladamente. Resultado: monstros, árvores e pedras aparecem visivelmente maiores que os heróis e até que a Guilda, quebrando a hierarquia visual — o jogador não consegue localizar o próprio herói facilmente no mapa.

**Correção — escala derivada, não multiplicador fixo por asset:**
1. Definir um **tamanho de referência único em tela** para "um personagem/objeto de porte médio" a zoom 1.0x — sugestão: 48px de altura.
2. Para cada sprite, calcular o multiplicador de escala dinamicamente: `scale = tamanhoReferencia / resoluçãoNativaDoAsset`, em vez de usar constantes hardcoded diferentes por asset (eliminar o `3.2f` fixo do Guerreiro/Orc e equivalentes).
3. Definir uma **hierarquia de porte relativo** intencional (não todos do mesmo tamanho — isso também ficaria estranho):
   - Heróis e monstros: porte "padrão" (referência base, ~48px)
   - Elementos decorativos (árvores, pedras): podem ser um pouco maiores que personagens (dão volume ao cenário), mas não devem ultrapassar ~1.3x o tamanho do herói
   - Guilda (estrutura central): sempre a maior estrutura do mapa, proporcional ao nível
4. Aplicar a mesma lógica a **todos** os sprites existentes (heróis, monstros, recursos coletáveis, decoração) numa única passada, para evitar essa inconsistência se repetir a cada asset novo.

**Validação:** print do mapa mostrando heróis claramente distinguíveis e não menores que os elementos de cenário ao redor.

---

## PARTE B — Rebalanceamento das missões de coleta (bug econômico crítico)

**Diagnóstico com números reais (checar contra os valores atuais do jogo, podem ter mudado):**

| Contrato | Recompensa paga hoje | Material garantido (100%) | Valor de venda | Resultado |
|---|---|---|---|---|
| Coleta de Madeira | 10g | 1x Madeira | 2g | Perda de 8g |
| Mineração de Pedra | 10g | 1x Pedra | 2g | Perda de 8g |
| Coleta de Ervas | 12g | 1x Erva Medicinal | 4g | Perda de 8g |
| Coleta de Ferro | 20g | 1x Minério de Ferro | 12g | Perda de 8g |

Como o drop de coleta é **100% garantido e determinístico** (sem variância de sorte), toda missão de coleta gera prejuízo certo pra guilda — isso drena o caixa de forma consistente e proibia a guilda de se sustentar, reproduzindo o soft-lock que já corrigimos para contratos de combate, mas numa categoria que ficou de fora daquela correção.

**Correção — aplicar a mesma fórmula já usada nos contratos de combate:**
- Custo de publicação do contrato de coleta = **50-65% do valor de venda do material garantido** (já que a chance é 100%, o "valor esperado" é simplesmente o preço de venda do item, sem precisar multiplicar por probabilidade).
- Exemplos de correção, usando 60% como referência:
  - Coleta de Madeira: 2g de material → contrato deveria custar **~1.2g**, não 10g
  - Mineração de Pedra: 2g de material → **~1.2g**, não 10g
  - Coleta de Ervas: 4g de material → **~2.4g**, não 12g
  - Coleta de Ferro: 12g de material → **~7.2g**, não 20g
- Ajustar também o valor mínimo prático (arredondar pra cima o suficiente pra não zerar, ex: mínimo de 1g por contrato).

**Nota de processo:** essa é a segunda vez que uma fórmula de balanceamento correta é aplicada só a uma parte do sistema (antes: só combate; agora falta coleta). Ao pedir correções de balanceamento no futuro, especificar explicitamente "aplicar a **todas** as categorias de contrato existentes e futuras", não só à categoria que motivou o pedido.

**Validação:** repetir o teste do checklist de contrato (publicar → completar → vender → confirmar saldo) especificamente para uma missão de coleta, confirmando que o ciclo agora é sustentável (venda do material cobre o custo do próprio contrato com margem, não gera prejuízo).

---

## Critério de aceite

- [ ] Print do mapa mostrando heróis em porte comparável ou maior que elementos decorativos ao redor
- [ ] Nenhum sprite (monstro, árvore, pedra) visivelmente maior que a Guilda
- [ ] Print do ciclo de missão de coleta completo (publicar → coletar → vender) mostrando saldo final positivo ou neutro, nunca negativo
- [ ] Confirmar que os 4 tipos de coleta (Madeira, Pedra, Ervas, Ferro) foram todos corrigidos, não só um como exemplo
