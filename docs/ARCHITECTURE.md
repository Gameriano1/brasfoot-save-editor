# 🏗️ ARQUITETURA DE MELHORIAS - BRASFOOT SAVE EDITOR V3.0

**Autor:** Arquiteto Sênior UI/UX  
**Data:** Novembro 2025  
**Versão:** 3.0 (Enterprise Edition)  
**Compatibilidade:** Java 8+

---

## 📋 ÍNDICE

1. [Visão Geral](#visão-geral)
2. [Redesign UI/UX](#1-redesign-de-uiux)
3. [Sistema de Presets](#2-sistema-de-presets)
4. [Sistema de Tradução](#3-sistema-de-tradução)
5. [Persistência de Preferências](#4-persistência-de-preferências)
6. [Auto-Refresh](#5-auto-refresh)
7. [Estrutura de Código](#6-estrutura-de-código)
8. [Dependências](#7-dependências)

---

## 🎯 VISÃO GERAL

### Problemas Identificados
1. ✗ Interface sobrecarregada com campos técnicos (eq, em, el, ek)
2. ✗ Sem presets pré-configurados para ações comuns
3. ✗ Labels não intuitivos sem tradução
4. ✗ Sem persistência de preferências do usuário
5. ✗ Sem detecção automática de mudanças externas

### Solução Proposta
✓ **Arquitetura modular em camadas (MVC + Service Layer)**  
✓ **UI moderna com categorização por tabs/accordions**  
✓ **Sistema de presets extensível com validação**  
✓ **I18n completo com suporte a múltiplos idiomas**  
✓ **Persistência robusta usando Preferences API + JSON**  
✓ **FileWatcher assíncrono com resolução de conflitos**

---

## 1. REDESIGN DE UI/UX

### 1.1 Layout Proposto (Material Design + Fluent Design)

```
┌────────────────────────────────────────────────────────────────┐
│  🎮 Brasfoot Save Editor v3.0        [_] [□] [×]               │
├────────────────────────────────────────────────────────────────┤
│ Arquivo  Editar  Presets  Ferramentas  Ajuda                   │
├────────────────────────────────────────────────────────────────┤
│ [📂Abrir] [💾Salvar] [🔄Refresh] [⭐Preset] [🔍Buscar] [⚙️Cfg]│
├─────────────────┬──────────────────────────────────────────────┤
│                 │                                              │
│  NAVEGAÇÃO   │  EDIÇÃO DE DADOS                         │
│  ├─ Times       │  ┌────────────────────────────────────────┐ │
│  ├─ Jogadores   │  │  EDITAR JOGADOR                      │ │
│  ├─ Ligas       │  ├────────────────────────────────────────┤ │
│  └─ Campeonatos │  │  ┌─ Informações Básicas ──────────┐   │ │
│                 │  │  │ Nome: [Lionel Messi_________]  │   │ │
│  │  │ Idade: [35]                │   │ │
│  │  └────────────────────────────────┘   │ │
│  │  ┌─ Atributos ────────────────────┐   │ │
│  │  │ Força (eq): [99] ▓▓▓▓▓▓▓▓▓▓  │   │ │
│  │  │ Idade (em): [35]             │   │ │
│  │  │ Estrela Local (el): [✓]     │   │ │
│  │  │ Estrela Mundial (ek): [✓]   │   │ │
│  │  └────────────────────────────────┘   │ │
│                 │  │  [Salvar] [Cancelar] [Preset] │ │
│                 │  └────────────────────────────────────────┘ │
├─────────────────┴──────────────────────────────────────────────┤
│ LOG:                                                         │
│ Arquivo carregado: brasileirao2023.s22                      │
│ ✔ Preset "Buff Máximo" aplicado a 22 jogadores                │
│ 💾 Arquivo salvo com sucesso!                                  │
├────────────────────────────────────────────────────────────────┤
│ ✅ Pronto | Auto-Refresh: ON | 1.234 objetos | Última edição: 13:45│
└────────────────────────────────────────────────────────────────┘
```

### 1.2 Paleta de Cores (Dark Theme - Material Design 3)

```java
// Cores Principais
Background:     #1E1E1E  (Cinza escuro profundo)
Surface:        #2D2D2D  (Cards e painéis)
Primary:        #BB86FC  (Roxo - ações principais)
Secondary:      #03DAC6  (Verde água - ações secundárias)
Error:          #CF6679  (Vermelho coral)
Success:        #4CAF50  (Verde sucesso)
Warning:        #FFC107  (Amarelo aviso)

// Cores de Texto
Text Primary:   #E1E1E1  (Branco suave)
Text Secondary: #B0B0B0  (Cinza claro)
Text Disabled:  #707070  (Cinza médio)

// Cores Semânticas
Player Strong:  #4CAF50  (Verde - força alta)
Player Medium:  #FFC107  (Amarelo - força média)
Player Weak:    #CF6679  (Vermelho - força baixa)
```

### 1.3 Hierarquia Visual

**Prioridade 1 (Alta):**
- Botões de ação: Abrir, Salvar, Aplicar Preset
- Campo de busca
- Indicador de status (arquivo modificado)

**Prioridade 2 (Média):**
- Lista de navegação (times, jogadores)
- Campos de edição agrupados por categoria
- Log de atividades

**Prioridade 3 (Baixa):**
- Barra de status
- Informações auxiliares (tooltips)

---

## 2. SISTEMA DE PRESETS

### 2.1 Arquitetura de Presets

```
PlayerPreset (Interface)
├─ name: String
├─ description: String
├─ icon: String
├─ attributes: Map<String, Integer>
├─ apply(Player): void
└─ validate(): boolean

PresetManager
├─ builtInPresets: List<PlayerPreset>
├─ customPresets: List<PlayerPreset>
├─ loadPresets(): void
├─ saveCustomPreset(PlayerPreset): void
├─ applyPreset(Player, PresetId): void
└─ deleteCustomPreset(PresetId): void
```

### 2.2 Presets Pré-Configurados

1. **"⚡ Buffar ao Máximo"**
   - eq (Força): 99
   - em (Idade): 25
   - el (Estrela Local): true
   - ek (Estrela Mundial): true

2. **"🌟 Estrela Mundial"**
   - eq (Força): 95
   - em (Idade): 27
   - el (Estrela Local): true
   - ek (Estrela Mundial): true

3. **"⭐ Estrela Local"**
   - eq (Força): 85
   - em (Idade): 24
   - el (Estrela Local): true
   - ek (Estrela Mundial): false

4. **"👴 Aposentar"**
   - eq (Força): 50
   - em (Idade): 42
   - el (Estrela Local): false
   - ek (Estrela Mundial): false

5. **"🔄 Reset Padrão"**
   - Restaura valores originais do backup

### 2.3 Formato de Armazenamento (JSON)

```json
{
  "presets": [
    {
      "id": "buff-max",
      "name": "Buffar ao Máximo",
      "description": "Eleva força para 99 e torna estrela mundial",
      "icon": "⚡",
      "type": "built-in",
      "attributes": {
        "eq": 99,
        "em": 25,
        "el": true,
        "ek": true
      },
      "validation": {
        "minAge": 18,
        "maxAge": 35,
        "requireBackup": true
      }
    }
  ]
}
```

---

## 3. SISTEMA DE TRADUÇÃO

### 3.1 Arquitetura de I18n

```
LabelTranslator
├─ translations: Map<String, Map<String, String>>
├─ currentLocale: Locale
├─ customTranslations: Map<String, String>
├─ getLabel(fieldName, locale): String
├─ setCustomLabel(fieldName, label): void
└─ saveTranslations(): void

Supported Locales:
├─ pt_BR (Português Brasil)
├─ en_US (English)
├─ es_ES (Español)
└─ Custom (Personalizável)
```

### 3.2 Mapeamento de Campos

```java
// Campos técnicos do Brasfoot (classe F - Jogador)
"dm" → "Nome do Jogador"
"eq" → "Força/Overall" (int 1-99)
"em" → "Idade" (int)
"el" → "Estrela Local" (boolean)
"ek" → "Estrela Mundial" (boolean)
```

### 3.3 Formato de Armazenamento

```json
{
  "translations": {
    "pt_BR": {
      "eq": "Força",
      "em": "Idade",
      "el": "Estrela Local",
      "ek": "Estrela Mundial"
    },
    "custom": {
      "eq": "Overall",
      "em": "Anos",
      "el": "⭐ Local",
      "ek": "🌟 Mundial"
    }
  },
  "activeLocale": "pt_BR",
  "enableCustomLabels": true
}
```

---

## 4. PERSISTÊNCIA DE PREFERÊNCIAS

### 4.1 Arquitetura

```
PreferencesManager
├─ prefs: Preferences (Java Preferences API)
├─ lastOpenDirectory: String
├─ windowSize: Dimension
├─ windowPosition: Point
├─ theme: ThemeEnum
├─ recentFiles: List<String>
├─ customTranslations: Map<String, String>
├─ favoritePresets: List<String>
├─ autoRefreshEnabled: boolean
├─ save(): void
└─ load(): void
```

### 4.2 Preferências Salvas

**Arquivo:** `~/.brasfoot-editor/preferences.json`

```json
{
  "version": "3.0",
  "lastModified": "2025-11-11T23:30:00Z",
  "ui": {
    "theme": "dark",
    "windowWidth": 1600,
    "windowHeight": 1000,
    "windowX": 100,
    "windowY": 50,
    "splitPaneDivider": 350,
    "fontSize": 13,
    "showTooltips": true
  },
  "files": {
    "lastOpenDirectory": "/home/user/brasfoot/saves",
    "recentFiles": [
      "/home/user/brasfoot/brasileirao2023.s22",
      "/home/user/brasfoot/mundial2022.s22"
    ],
    "maxRecentFiles": 10,
    "autoBackup": true
  },
  "editor": {
    "autoRefresh": true,
    "autoRefreshInterval": 5000,
    "confirmBeforeApplyPreset": true,
    "showFieldDescriptions": true,
    "highlightModifiedFields": true
  },
  "customTranslations": {
    "eq": "Overall",
    "em": "Idade"
  },
  "favoritePresets": ["buff-max", "balancear"]
}
```

---

## 5. AUTO-REFRESH

### 5.1 Arquitetura de FileWatcher

```
FileWatcherService
├─ watchService: WatchService (java.nio)
├─ watchedFile: Path
├─ lastKnownHash: String
├─ watcherThread: Thread
├─ conflictResolver: ConflictResolver
├─ startWatching(Path): void
├─ stopWatching(): void
├─ onFileChanged(): void
└─ checkForConflicts(): ConflictStatus

ConflictResolver
├─ hasLocalChanges: boolean
├─ hasExternalChanges: boolean
├─ resolve(): ResolutionStrategy
└─ showConflictDialog(): UserChoice

ResolutionStrategy (Enum)
├─ KEEP_LOCAL (Manter alterações locais)
├─ LOAD_EXTERNAL (Carregar mudanças externas)
├─ MERGE (Tentar merge - avançado)
└─ SHOW_DIFF (Mostrar diferenças)
```

### 5.2 Fluxo de Detecção

```
┌─────────────────────────┐
│  Arquivo modificado     │
│  externamente           │
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐
│  WatchService detecta   │
│  evento ENTRY_MODIFY    │
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐
│  Calcular hash do       │
│  arquivo (SHA-256)      │
└───────────┬─────────────┘
            │
            ▼
      ┌─────┴─────┐
      │Hash mudou?│
      └─────┬─────┘
       Não  │  Sim
     ┌──────┘  └──────┐
     │                │
     ▼                ▼
  Ignorar   ┌──────────────────┐
            │ Verificar se há  │
            │ mudanças locais  │
            │ não salvas       │
            └────────┬─────────┘
                     │
           ┌─────────┴─────────┐
           │ Mudanças locais?  │
           └─────────┬─────────┘
              Não    │    Sim
          ┌──────────┘    └──────────┐
          │                          │
          ▼                          ▼
┌──────────────────┐       ┌──────────────────┐
│ Recarregar auto  │       │ Mostrar diálogo  │
│ (sem confirmação)│       │ de conflito      │
└──────────────────┘       └──────────────────┘
```

### 5.3 Diálogo de Conflito

```
┌─────────────────────────────────────────────────┐
│  ⚠️  Conflito Detectado                         │
├─────────────────────────────────────────────────┤
│                                                 │
│  O arquivo foi modificado externamente.         │
│  Você tem alterações não salvas localmente.     │
│                                                 │
│  Arquivo: brasileirao2023.s22                   │
│  Modificado: 11/11/2025 às 23:45                │
│                                                 │
│  Escolha uma ação:                              │
│                                                 │
│  ○ Manter minhas alterações (ignorar externas) │
│  ○ Carregar alterações externas (perder locais)│
│  ○ Salvar em novo arquivo e carregar externas  │
│                                                 │
│  ☑ Sempre fazer backup antes de sobrescrever   │
│                                                 │
│  [Continuar]  [Ver Diferenças]  [Cancelar]     │
└─────────────────────────────────────────────────┘
```

---

## 6. ESTRUTURA DE CÓDIGO

### 6.1 Diagrama de Pacotes

```
br.com.saveeditor.brasfoot
├─ config
│  ├─ PreferencesManager.java
│  ├─ LabelTranslator.java
│  └─ ConfigConstants.java
├─ gui
│  ├─ MainWindow.java
│  ├─ components
│  │  ├─ NavigationPanel.java
│  │  ├─ DataTablePanel.java
│  │  ├─ PresetPanel.java (NOVO)
│  │  ├─ CategoryAccordion.java (NOVO)
│  │  └─ AttributeSlider.java (NOVO)
│  └─ dialogs
│     ├─ EditPlayerDialog.java
│     ├─ PresetManagerDialog.java (NOVO)
│     ├─ ConflictResolutionDialog.java (NOVO)
│     ├─ LabelEditorDialog.java (NOVO)
│     └─ PreferencesDialog.java (NOVO)
├─ model
│  ├─ NavegacaoState.java
│  ├─ PlayerPreset.java (NOVO)
│  ├─ PresetConfig.java (NOVO)
│  ├─ TranslationConfig.java (NOVO)
│  └─ UserPreferences.java (NOVO)
├─ service
│  ├─ EditorService.java
│  ├─ SaveFileService.java
│  ├─ PresetService.java (NOVO)
│  ├─ FileWatcherService.java (NOVO)
│  ├─ TranslationService.java (NOVO)
│  └─ ValidationService.java (NOVO)
└─ util
   ├─ ReflectionUtils.java
   ├─ FileHashUtils.java (NOVO)
   └─ JsonUtils.java (NOVO)
```

### 6.2 Padrões de Design Utilizados

1. **MVC (Model-View-Controller)**
   - Model: Classes em `model/`
   - View: Classes em `gui/`
   - Controller: Classes em `service/`

2. **Observer Pattern**
   - FileWatcherService notifica MainWindow
   - PreferencesManager notifica componentes UI

3. **Strategy Pattern**
   - PlayerPreset com diferentes estratégias de buff
   - ConflictResolver com diferentes estratégias de resolução

4. **Singleton Pattern**
   - PreferencesManager (única instância)
   - LabelTranslator (única instância)

5. **Factory Pattern**
   - PresetFactory para criar presets built-in
   - DialogFactory para criar diálogos padronizados

---

## 7. DEPENDÊNCIAS

### 7.1 Maven (pom.xml) - RECOMENDADO

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>br.com.saveeditor</groupId>
    <artifactId>brasfoot-editor</artifactId>
    <version>3.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Serialização -->
        <dependency>
            <groupId>com.esotericsoftware</groupId>
            <artifactId>kryo</artifactId>
            <version>4.0.2</version>
        </dependency>

        <!-- Look and Feel Moderno -->
        <dependency>
            <groupId>com.formdev</groupId>
            <artifactId>flatlaf</artifactId>
            <version>3.2.5</version>
        </dependency>

        <!-- JSON -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>

        <!-- Logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.7.36</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.2.11</version>
        </dependency>

        <!-- Utilitários -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.12.0</version>
        </dependency>

        <!-- Testes -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.4.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>br.com.saveeditor.brasfoot.Main</mainClass>
                                </transformer>
                            </transformers>
                            <finalName>brasfoot-editor-v3</finalName>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### 7.2 Gradle (build.gradle) - ALTERNATIVA

```groovy
plugins {
    id 'java'
    id 'application'
    id 'com.github.johnrengelman.shadow' version '7.1.2'
}

group = 'br.com.saveeditor'
version = '3.0.0'
sourceCompatibility = '1.8'
targetCompatibility = '1.8'

repositories {
    mavenCentral()
}

dependencies {
    // Serialização
    implementation 'com.esotericsoftware:kryo:4.0.2'
    
    // Look and Feel
    implementation 'com.formdev:flatlaf:3.2.5'
    
    // JSON
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // Logging
    implementation 'org.slf4j:slf4j-api:1.7.36'
    implementation 'ch.qos.logback:logback-classic:1.2.11'
    
    // Utilitários
    implementation 'org.apache.commons:commons-lang3:3.12.0'
    
    // Testes
    testImplementation 'junit:junit:4.13.2'
}

application {
    mainClass = 'br.com.saveeditor.brasfoot.Main'
}

shadowJar {
    archiveBaseName.set('brasfoot-editor-v3')
    archiveClassifier.set('')
    archiveVersion.set('')
}
```

---

## 8. CHECKLIST DE IMPLEMENTAÇÃO

### Fase 1: Infraestrutura (Semana 1-2)
- [ ] Criar estrutura de pacotes
- [ ] Configurar Maven/Gradle
- [ ] Implementar PreferencesManager
- [ ] Implementar LabelTranslator
- [ ] Testes unitários básicos

### Fase 2: Sistema de Presets (Semana 3)
- [ ] Criar modelo PlayerPreset
- [ ] Implementar PresetService
- [ ] Criar presets built-in
- [ ] Implementar PresetManagerDialog
- [ ] Sistema de validação

### Fase 3: FileWatcher (Semana 4)
- [ ] Implementar FileWatcherService
- [ ] Sistema de detecção de hash
- [ ] ConflictResolutionDialog
- [ ] Testes de concorrência

### Fase 4: Redesign UI (Semana 5-6)
- [ ] Implementar CategoryAccordion
- [ ] Criar AttributeSlider
- [ ] Redesenhar EditPlayerDialog
- [ ] Implementar PreferencesDialog
- [ ] Aplicar nova paleta de cores

### Fase 5: Integração (Semana 7)
- [ ] Integrar todos os componentes
- [ ] Testes end-to-end
- [ ] Documentação de usuário
- [ ] Correção de bugs

### Fase 6: Polimento (Semana 8)
- [ ] Otimização de performance
- [ ] Acessibilidade (a11y)
- [ ] Tooltips e ajuda contextual
- [ ] Build de release

---

## 9. EDGE CASES E TRATAMENTO

### 9.1 Casos de Borda

1. **Arquivo corrompido durante auto-refresh**
   - Detectar via hash inválido
   - Restaurar do backup automático
   - Notificar usuário

2. **Preset aplicado em jogador inválido**
   - Validar estrutura antes de aplicar
   - Rollback em caso de falha
   - Log detalhado de erro

3. **Conflito de tradução customizada**
   - Prioridade: Custom > Locale > Default
   - Fallback para label técnico

4. **Preferências corrompidas**
   - Tentar recovery do JSON
   - Reset para defaults
   - Backup de preferências antigas

5. **Múltiplas instâncias editando**
   - Lock file (.lock)
   - Aviso ao abrir arquivo em uso

6. **Memória insuficiente (arquivos grandes)**
   - Stream parsing para navegação
   - Lazy loading de objetos
   - Limite de desfazer/refazer

### 9.2 Performance

- **FileWatcher**: Debounce de 500ms para evitar múltiplos triggers
- **UI Rendering**: Virtual scrolling para listas grandes
- **Save**: Async com progress bar
- **Search**: Indexação em background

---

## 10. CONSIDERAÇÕES FINAIS

Esta arquitetura foi projetada para:

✅ **Escalabilidade**: Fácil adicionar novos presets, traduções e funcionalidades  
✅ **Manutenibilidade**: Código modular com responsabilidades bem definidas  
✅ **Performance**: Operações assíncronas e otimizações  
✅ **UX**: Interface intuitiva com feedback visual constante  
✅ **Robustez**: Tratamento completo de erros e edge cases  

**Estimativa de Desenvolvimento**: 6-8 semanas (1 desenvolvedor full-time)  
**LOC Estimado**: ~8.000 linhas de código novo  
**Testes**: ~2.000 linhas de testes unitários/integração

---

**Arquiteto Sênior UI/UX**  
*"Excelência em Design e Engenharia de Software"*
