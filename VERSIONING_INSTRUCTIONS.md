# Diretrizes de Versionamento e Nomenclatura de APK — Guildkeeper

Para manter o projeto organizado e rastreável, adotamos o seguinte padrão para controle de versões e nomenclatura de builds.

---

## 📌 1. Padrão de Nomenclatura do APK

O nome do arquivo APK gerado pelo Gradle é padronizado automaticamente com base na versão atual e no tipo de compilação:

`guildkeeper-v<VersionName>-<BuildType>.apk`

*   **Debug (Desenvolvimento):** `guildkeeper-v0.1.0-debug.apk`
*   **Release (Produção):** `guildkeeper-v0.1.0-release.apk`

---

## 🛠️ 2. Como Atualizar a Versão

Sempre que preparar uma nova versão do jogo, siga estes passos:

1.  Abra o arquivo [`app/build.gradle.kts`](file:///c:/projetos/Guildkeeper/app/build.gradle.kts).
2.  Altere a constante `appVersionName` (ex: de `"0.1.0"` para `"0.1.1"`):
    ```kotlin
    val appVersionName = "0.1.1"
    ```
3.  No bloco `defaultConfig`, incremente o valor de `versionCode` em `1`:
    ```kotlin
    versionCode = 2  // Incremente sempre de 1 em 1
    ```

---

## 📦 3. Compilando Localmente

Para compilar o APK utilizando as regras de nomenclatura padronizadas:

1.  Certifique-se de que a variável de ambiente `JAVA_HOME` está apontando para o JDK 17+ (ou JDK 26 recém-instalado):
    ```powershell
    $env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.2.1"
    ```
2.  Execute o comando de build:
    *   **Para Debug:** `.\gradlew assembleDebug`
    *   **Para Release:** `.\gradlew assembleRelease`
3.  O APK final será gerado no diretório:
    `app/build/outputs/apk/debug/` ou `app/build/outputs/apk/release/`

O APK gerado para distribuição deve ser copiado para a pasta `/APK` na raiz do projeto.

---

## 🏷️ 4. Etiquetando Versões no Git (Git Tags)

Durante a fase de desenvolvimento, use tags no formato `v0.x.x` (ex: `v0.1.0`):

```bash
# 1. Crie a tag localmente
git tag -a v0.1.0 -m "Release v0.1.0 - Correção de softlock no combate e novos sprites"

# 2. Envie a tag para o GitHub
git push origin v0.1.0
```

Quando o jogo for oficialmente lançado de forma completa, a numeração passará a seguir o padrão `v1.0.0`, `v1.0.1`, etc.

