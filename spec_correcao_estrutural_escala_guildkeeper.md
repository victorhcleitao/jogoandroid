# Spec — Guildkeeper: Correção Estrutural de Escala de Sprites (Follow-up)

## Contexto

A correção aplicada (retornar o Guerreiro para multiplicador fixo `3.2f`, Ferraria para `3.5f`, Mercador para `3.2f`) resolveu o sintoma visível, mas reintroduziu o padrão de "número mágico por asset" que a Parte A da spec anterior (`spec_escala_sprites_e_coleta_guildkeeper.md`) pedia para eliminar. Isso não escala: cada sprite novo (Maga, Arqueiro, Clériga, monstros futuros, prédios futuros) provavelmente terá sua própria proporção de padding transparente, exigindo calibração manual individual pra sempre.

**Causa raiz correta, identificada pelo próprio diagnóstico do Antigravity:** o cálculo de escala (`referência / resolução nativa`) estava usando o tamanho bruto do arquivo (ex: 100x100 por frame) como "resolução nativa", mas o personagem real ocupa só uma fração central desse frame — o resto é margem transparente. A fórmula em si está correta; o dado de entrada (resolução nativa) é que está errado.

---

## PARTE A — Correção estrutural (recomendada, substitui os multiplicadores fixos)

**Opção 1 — Trim manual (mais rápida de implementar agora):**
Para cada sprite sheet existente com padding excessivo (Guerreiro, Orc, e quaisquer outros no mesmo caso), abrir o arquivo, identificar a bounding box real do personagem dentro do frame (ignorando pixels transparentes nas bordas), recortar o frame para esse tamanho real, e re-exportar o sprite sheet já sem a margem. Depois disso, a fórmula `referência / resolução nativa` volta a funcionar corretamente sem precisar de multiplicador extra por asset.

**Opção 2 — Trim automático no carregamento (mais robusta a longo prazo):**
Implementar uma função que, ao carregar um sprite sheet, calcule a bounding box de pixels não-transparentes de cada frame automaticamente (varrendo o canal alpha), e use essa dimensão real — não o tamanho bruto do arquivo — como entrada da fórmula de escala. Isso elimina a necessidade de mexer manualmente em cada asset novo que entrar no projeto, incluindo os gerados via ComfyUI no futuro (que também podem ter padding variável).

**Recomendação:** Opção 2 se o tempo permitir — resolve pra sempre. Opção 1 como correção imediata caso a Opção 2 demande mais investigação.

## PARTE B — Validação de hierarquia de tamanho (Guilda > Prédios > Heróis/Monstros)

A regra definida anteriormente era: a Guilda deve ser sempre a maior estrutura visível do mapa. Os valores aplicados aos prédios (Ferraria ~130px, Mercador ~120px) foram escolhidos como números absolutos, sem confirmar se ainda respeitam essa hierarquia em relação ao tamanho atual da Guilda em cada nível.

**Ação solicitada:** enviar um print mostrando a Guilda (em qualquer nível atual) e pelo menos um prédio construído (Ferraria ou Mercador) lado a lado na mesma tela, para confirmar visualmente que a Guilda continua sendo a estrutura de maior porte da cena. Se a Ferraria ou o Mercador estiverem maiores que a Guilda, os valores de escala dos prédios precisam ser reduzidos proporcionalmente.

---

## Critério de aceite

- [ ] Guerreiro (e demais sprites com o mesmo problema de padding) renderizando em tamanho correto sem depender de multiplicador manual arbitrário — validar testando com um sprite novo hipotético sem precisar ajustar número por número
- [ ] Print comparativo confirmando Guilda > Ferraria/Mercador > Heróis/Monstros em porte visual, na ordem certa
- [ ] Nenhuma regressão nos elementos que já estavam corretos (árvores, pedras, monstros, cenário) após a mudança
