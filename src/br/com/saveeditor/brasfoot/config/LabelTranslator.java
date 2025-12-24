package br.com.saveeditor.brasfoot.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Tradutor de labels de campos técnicos para nomes amigáveis.
 * Suporta múltiplos idiomas e traduções customizadas.
 */
public class LabelTranslator {
    
    private static LabelTranslator instance;
    private final Map<String, Map<String, String>> translations;
    private String currentLocale;
    private final PreferencesManager preferencesManager;
    
    private LabelTranslator() {
        this.translations = new HashMap<>();
        this.preferencesManager = PreferencesManager.getInstance();
        this.currentLocale = preferencesManager.getPreferences().getEditor().getActiveLocale();
        initializeTranslations();
    }
    
    /**
     * Obtém a instância singleton.
     */
    public static synchronized LabelTranslator getInstance() {
        if (instance == null) {
            instance = new LabelTranslator();
        }
        return instance;
    }
    
    /**
     * Inicializa as traduções padrão.
     */
    private void initializeTranslations() {
        // Português Brasil
        Map<String, String> ptBR = new HashMap<>();
        ptBR.put("dm", "Nome");
        ptBR.put("eq", "Força");
        ptBR.put("em", "Idade");
        ptBR.put("el", "Estrela Local");
        ptBR.put("ek", "Estrela Mundial");
        translations.put("pt_BR", ptBR);
        
        // English
        Map<String, String> enUS = new HashMap<>();
        enUS.put("dm", "Name");
        enUS.put("eq", "Overall");
        enUS.put("em", "Age");
        enUS.put("el", "Local Star");
        enUS.put("ek", "World Star");
        translations.put("en_US", enUS);
        
        // Español
        Map<String, String> esES = new HashMap<>();
        esES.put("dm", "Nombre");
        esES.put("eq", "Fuerza");
        esES.put("em", "Edad");
        esES.put("el", "Estrella Local");
        esES.put("ek", "Estrella Mundial");
        translations.put("es_ES", esES);
    }
    
    /**
     * Obtém a tradução de um campo.
     * Prioridade: Custom > Locale Atual > Técnico
     */
    public String getLabel(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return "";
        }
        
        // 1. Verificar tradução customizada
        if (preferencesManager.getPreferences().getEditor().isEnableCustomLabels()) {
            String custom = preferencesManager.getCustomTranslation(fieldName);
            if (custom != null && !custom.isEmpty()) {
                return custom;
            }
        }
        
        // 2. Verificar tradução do locale atual
        Map<String, String> localeTranslations = translations.get(currentLocale);
        if (localeTranslations != null) {
            String translation = localeTranslations.get(fieldName);
            if (translation != null) {
                return translation;
            }
        }
        
        // 3. Fallback: nome técnico
        return fieldName;
    }
    
    /**
     * Obtém a tradução com ícone (se disponível).
     */
    public String getLabelWithIcon(String fieldName) {
        String label = getLabel(fieldName);
        String icon = getIcon(fieldName);
        
        if (icon != null && !icon.isEmpty()) {
            return icon + " " + label;
        }
        
        return label;
    }
    
    /**
     * Obtém o ícone associado a um campo.
     */
    public String getIcon(String fieldName) {
        switch (fieldName) {
            case "dm": return "👤";
            case "eq": return "⚡";
            case "em": return "📅";
            case "el": return "⭐";
            case "ek": return "🌟";
            default: return "";
        }
    }
    
    /**
     * Obtém a descrição/tooltip de um campo.
     */
    public String getDescription(String fieldName) {
        if (!preferencesManager.getPreferences().getEditor().isShowFieldDescriptions()) {
            return null;
        }
        
        switch (fieldName) {
            case "dm":
                return "Nome completo do jogador";
            case "eq":
                return "Força/Overall do jogador (1-99)";
            case "em":
                return "Idade do jogador em anos";
            case "el":
                return "Se o jogador é considerado estrela local";
            case "ek":
                return "Se o jogador é considerado estrela mundial";
            default:
                return null;
        }
    }
    
    /**
     * Define o locale atual.
     */
    public void setLocale(String locale) {
        if (translations.containsKey(locale)) {
            this.currentLocale = locale;
            preferencesManager.getPreferences().getEditor().setActiveLocale(locale);
            preferencesManager.save();
        }
    }
    
    /**
     * Obtém o locale atual.
     */
    public String getCurrentLocale() {
        return currentLocale;
    }
    
    /**
     * Obtém todos os locales disponíveis.
     */
    public String[] getAvailableLocales() {
        return translations.keySet().toArray(new String[0]);
    }
    
    /**
     * Define uma tradução customizada.
     */
    public void setCustomTranslation(String fieldName, String translation) {
        preferencesManager.setCustomTranslation(fieldName, translation);
    }
    
    /**
     * Remove uma tradução customizada.
     */
    public void removeCustomTranslation(String fieldName) {
        preferencesManager.getPreferences().getCustomTranslations().remove(fieldName);
        preferencesManager.save();
    }
    
    /**
     * Obtém todas as traduções customizadas.
     */
    public Map<String, String> getCustomTranslations() {
        return new HashMap<>(preferencesManager.getPreferences().getCustomTranslations());
    }
    
    /**
     * Verifica se um campo tem tradução customizada.
     */
    public boolean hasCustomTranslation(String fieldName) {
        return preferencesManager.getCustomTranslation(fieldName) != null;
    }
    
    /**
     * Obtém nome do locale em formato amigável.
     */
    public String getLocaleName(String locale) {
        switch (locale) {
            case "pt_BR": return "Português (Brasil)";
            case "en_US": return "English (US)";
            case "es_ES": return "Español";
            default: return locale;
        }
    }
    
    /**
     * Formata um valor de campo para exibição.
     */
    public String formatValue(String fieldName, Object value) {
        if (value == null) {
            return "-";
        }
        
        // Formatar booleanos
        if (value instanceof Boolean) {
            boolean boolValue = (Boolean) value;
            return boolValue ? "✓ Sim" : "✗ Não";
        }
        
        // Formatar idade
        if ("em".equals(fieldName) && value instanceof Number) {
            return value + " anos";
        }
        
        // Formatar força com cor
        if ("eq".equals(fieldName) && value instanceof Number) {
            int strength = ((Number) value).intValue();
            if (strength >= 90) {
                return value + " (Excelente)";
            } else if (strength >= 80) {
                return value + " (Muito Bom)";
            } else if (strength >= 70) {
                return value + " (Bom)";
            } else if (strength >= 60) {
                return value + " (Regular)";
            } else {
                return value + " (Fraco)";
            }
        }
        
        return value.toString();
    }
    
    /**
     * Valida se um campo é conhecido.
     */
    public boolean isKnownField(String fieldName) {
        Map<String, String> localeTranslations = translations.get("pt_BR");
        return localeTranslations != null && localeTranslations.containsKey(fieldName);
    }
}
