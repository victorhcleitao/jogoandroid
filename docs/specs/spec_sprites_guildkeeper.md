# Spec — Guildkeeper: Produção de Sprites Reais (PNG) e Correção de Overlap de Labels

## Bug reportado (corrigir primeiro, é rápido)

**Overlap de texto no castelo:** No mapa isométrico, o nome da heroína (`Valeria`) está renderizando na mesma posição que outro label (provavelmente nome da Guilda), gerando texto ilegível sobreposto. Corrigir espaçamento vertical entre labels quando há colisão de posição — ex: escalonar labels que compartilham a mesma coordenada de tela.

---

## Parte técnica — Migração de renderização por código para PNG (para o Antigravity)

**Situação atual:** Personagens e monstros são desenhados via matrizes de pixel hardcoded diretamente em Kotlin (`Canvas`/`Path`), o que limita o nível de detalhe e não escala para animações.

**Mudança proposta:**
1. Criar pasta de recursos `res/drawable/sprites/` para armazenar os sprite sheets em PNG.
2. Cada personagem/monstro terá um sprite sheet único contendo os frames de cada estado de animação lado a lado (formato padrão de grid).
3. Trocar a renderização atual (`Path`/pixel matrix) por `ImageBitmap`/`BitmapFactory` carregando a região correta do sprite sheet conforme o estado atual (idle, andando, atacando) e o frame do ciclo de animação.
4. Manter o sistema de billboard + sombra oval já implementado — só troca o que é desenhado dentro do billboard (PNG em vez de path).
5. Elementos puramente geométricos que já funcionam bem como código (o castelo 3D procedural, por exemplo) podem continuar como estão — a migração para PNG é prioritariamente para personagens/monstros que precisam de mais detalhe e variação.

---

## Especificação de arte — lista de assets necessários

**Resolução base:** 32x32 pixels por frame (padrão para dar detalhe sem pesar produção; pode escalar 3x-4x na tela via `nearest neighbor` para manter o look pixelado nítido).

**Paleta:** seguir estritamente a paleta Dark Fantasy já estabelecida (dourado, azul místico, cinza escuro) + tons neutros de pele/metal/couro necessários para os personagens.

### Heróis (4 classes)
Cada classe precisa de 3 estados de animação:
- **Idle** (parado, respirando/2 frames)
- **Andando** (ciclo de caminhada, 4 frames)
- **Atacando** (1-2 frames de ataque, específico por classe: espada para Guerreiro, cajado para Maga, arco para Arqueiro, cura/luz para Clériga)

Classes: Guerreiro, Maga, Arqueiro, Clériga — mantendo silhuetas já estabelecidas (cores de identificação: laranja/Guerreiro, roxo/Maga conforme legenda atual do mapa).

*Variação de equipamento (opcional, fase 2):* overlay de arma/armadura por tier da Ferraria (Bronze/Ferro/Aço), para refletir visualmente as compras autônomas dos heróis.

### Monstros (3 tipos atuais + espaço para expansão)
- Slime, Lobo, Goblin — cada um com Idle (2 frames), Andando (3-4 frames) e um estado de "derrotado" (pode ser estático, vira a Lápide já existente).

### Ambiente / decoração (podem ser estáticos, sem animação)
- Árvores secas (2-3 variações)
- Rochas/pedregulhos (2-3 variações)
- Tocha (1-2 frames de "chama tremulando" é o único elemento de ambiente que vale animar)
- Tiles de terreno (4 variações já em uso — considerar recriar como PNG também para consistência visual com os personagens, ou manter como está se o resultado atual já for satisfatório)

### Castelo da Guilda
- Manter como está (procedural em código) — funciona bem e escalar por nível de guilda é mais fácil via código do que trocando sprite sheets inteiros a cada upgrade.

---

## Opções de workflow para produzir a arte

1. **Aseprite** — ferramenta padrão da indústria para pixel art e sprite sheets, paga (~$20, licença única) mas com curva de aprendizado razoável e exporta sprite sheets prontos para uso.
2. **Artista freelancer** — plataformas como itch.io (seção de freelancers), Fiverr ou ArtStation Jobs; contratar por asset ou pacote fechado, com a lista acima como escopo/briefing pronto.
3. **Asset packs prontos** — itch.io tem packs de pixel art "dark fantasy RPG" com heróis/monstros genéricos que podem servir de base e ser adaptados, mais rápido que produção do zero (verificar licença de uso comercial se aplicável).

## Ordem sugerida

1. Corrigir bug de overlap de labels (rápido, não bloqueia o resto).
2. Antigravity prepara a estrutura de código para carregar PNG (`res/drawable/sprites/`, lógica de troca de frame) — pode ser feito em paralelo à produção de arte, usando placeholders.
3. Produção da arte (paralelo, por quem for fazer — você, artista ou asset pack).
4. Integração final: substituir placeholders pelos sprites reais.
