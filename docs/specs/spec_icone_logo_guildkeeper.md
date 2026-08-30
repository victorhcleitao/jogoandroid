# Spec — Guildkeeper: Ícone e Logo Definitivos (geração via ComfyUI local)

## Contexto

Direção já definida em sessão anterior: conceito **"Brasão + Chave"** venceu entre as opções exploradas (Castelo minimalista, Brasão+Chave, Emblema de fronteira) por comunicar diretamente o nome "Guildkeeper" (quem guarda/administra) mantendo estética de fantasia via formato de escudo, e por ser legível mesmo em tamanhos pequenos.

Usar a pipeline SDXL local já calibrada (512x512, sem crash de OOM) para gerar as opções.

---

## Prompt sugerido (ícone principal)

```
app icon, dark fantasy guild emblem crest, heraldic shield with an ornate
golden key crossed at center, pixel art style, deep navy blue and charcoal
background, gold and amber accent colors, medieval RPG aesthetic, clean bold
silhouette readable at small size, centered composition, no text, crisp pixel
shading, high contrast
```

**Negative prompt sugerido (se o workflow suportar):** `blurry, photorealistic, 3d render, text, watermark, multiple objects, cluttered background, soft gradient, anti-aliased edges`

**Parâmetros:** 512x512, workflow de pixel art (`sdxl_pixelart_api_workflow.json` ou equivalente já em uso), gerar **4-6 variações** (seeds diferentes) para escolher a melhor composição antes de refinar.

## Variações a testar

1. Versão com o escudo mais "robusto"/anguloso (estilo brasão medieval clássico)
2. Versão com escudo mais arredondado/orgânico
3. Chave na vertical (mais formal/heráldica) vs. chave na diagonal (mais dinâmica)
4. Com leve halo/glow dourado ao redor do escudo vs. sem halo (mais limpo/flat)

---

## Requisitos técnicos do ícone final (Android)

- **Adaptive icon** (Android 8+): precisa de duas camadas separadas — `foreground` (o emblema em si, com margem de segurança de ~66% da área central, pois o sistema pode recortar em círculo/quadrado/gota) e `background` (pode ser um tom sólido da paleta, ex: azul-marinho escuro).
- Exportar em pelo menos 3 resoluções: 512x512 (loja/Play Store), 192x192 (launcher), 48x48 (teste de legibilidade mínima — abrir o resultado nesse tamanho antes de aprovar).
- Testar o ícone final sobre fundo claro E escuro (o launcher do Android varia por skin/fabricante) — garantir que a silhueta se destaca nos dois casos.

## Wordmark (logo com texto)

Já validado anteriormente: fonte serifada média (remete a selo/pergaminho medieval sem cair em "gótica pesada" ilegível), cor dourada, bom letter-spacing. Não precisa gerar via IA — pode ser tipografia direta (ex: fonte serifada já disponível no sistema/projeto) combinada com o ícone aprovado ao lado.

## Critério de aceite

- [ ] Pelo menos 4 variações geradas para comparação lado a lado
- [ ] Ícone escolhido testado em 48x48px — silhueta ainda reconhecível
- [ ] Camadas foreground/background separadas para adaptive icon
- [ ] Paleta consistente com o resto do jogo (dourado/azul místico/cinza escuro), sem introduzir cor nova
