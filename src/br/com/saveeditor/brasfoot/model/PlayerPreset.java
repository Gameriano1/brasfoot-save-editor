package br.com.saveeditor.brasfoot.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Representa um preset de atributos para jogadores.
 * Suporta presets built-in e customizados pelo usuário.
 */
public class PlayerPreset {
    
    private String id;
    private String name;
    private String description;
    private String icon;
    private PresetType type;
    private Map<String, Object> attributes;  // Suporta Integer e Boolean
    private ValidationRules validation;
    
    public PlayerPreset() {
        this.attributes = new HashMap<>();
        this.validation = new ValidationRules();
    }
    
    public PlayerPreset(String id, String name, String description, String icon, PresetType type) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.type = type;
    }
    
    /**
     * Aplica o preset a um jogador (objeto refletido)
     */
    public void applyTo(Object playerObject) throws Exception {
        if (playerObject == null) {
            throw new IllegalArgumentException("Objeto jogador não pode ser nulo");
        }
        
        // Validar antes de aplicar
        if (!validate()) {
            throw new IllegalStateException("Preset inválido: validação falhou");
        }
        
        // Aplicar cada atributo usando reflexão
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            
            try {
                java.lang.reflect.Field field = playerObject.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                
                // Converter para o tipo correto
                Class<?> fieldType = field.getType();
                
                if (value instanceof Integer) {
                    Integer intValue = (Integer) value;
                    if (fieldType == int.class || fieldType == Integer.class) {
                        field.set(playerObject, intValue);
                    } else if (fieldType == byte.class || fieldType == Byte.class) {
                        field.set(playerObject, intValue.byteValue());
                    } else if (fieldType == short.class || fieldType == Short.class) {
                        field.set(playerObject, intValue.shortValue());
                    }
                } else if (value instanceof Boolean) {
                    if (fieldType == boolean.class || fieldType == Boolean.class) {
                        field.set(playerObject, value);
                    }
                } else {
                    throw new IllegalArgumentException("Tipo de valor não suportado: " + value.getClass());
                }
            } catch (NoSuchFieldException e) {
                System.err.println("⚠ Campo '" + fieldName + "' não existe no jogador");
            }
        }
    }
    
    /**
     * Valida o preset
     */
    public boolean validate() {
        if (id == null || id.trim().isEmpty()) return false;
        if (name == null || name.trim().isEmpty()) return false;
        if (attributes == null || attributes.isEmpty()) return false;
        
        // Validar valores dos atributos
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            Object value = entry.getValue();
            
            // Aceitar Integer e Boolean - sem limites artificiais
            if (!(value instanceof Integer || value instanceof Boolean)) {
                // Tipo não suportado
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Adiciona um atributo numérico ao preset
     */
    public void addAttribute(String fieldName, int value) {
        // Permitir qualquer valor - o jogo define os limites
        attributes.put(fieldName, value);
    }
    
    /**
     * Adiciona um atributo booleano ao preset
     */
    public void addBooleanAttribute(String fieldName, boolean value) {
        attributes.put(fieldName, value);
    }
    
    /**
     * Remove um atributo do preset
     */
    public void removeAttribute(String fieldName) {
        attributes.remove(fieldName);
    }
    
    /**
     * Cria uma cópia do preset
     */
    public PlayerPreset copy() {
        PlayerPreset copy = new PlayerPreset(
            this.id + "_copy",
            this.name + " (Cópia)",
            this.description,
            this.icon,
            PresetType.CUSTOM
        );
        copy.attributes = new HashMap<>(this.attributes);
        copy.validation = this.validation.copy();
        return copy;
    }
    
    // Getters e Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public PresetType getType() {
        return type;
    }
    
    public void setType(PresetType type) {
        this.type = type;
    }
    
    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }
    
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = new HashMap<>(attributes);
    }
    
    public ValidationRules getValidation() {
        return validation;
    }
    
    public void setValidation(ValidationRules validation) {
        this.validation = validation;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerPreset)) return false;
        PlayerPreset that = (PlayerPreset) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return icon + " " + name;
    }
    
    /**
     * Tipos de preset
     */
    public enum PresetType {
        BUILT_IN,   // Preset pré-configurado
        CUSTOM,     // Criado pelo usuário
        FAVORITE    // Marcado como favorito
    }
    
    /**
     * Regras de validação para aplicação do preset
     */
    public static class ValidationRules {
        private int minAge = 15;
        private int maxAge = 45;
        private boolean requireBackup = true;
        private boolean confirmBeforeApply = true;
        
        public ValidationRules() {}
        
        public ValidationRules copy() {
            ValidationRules copy = new ValidationRules();
            copy.minAge = this.minAge;
            copy.maxAge = this.maxAge;
            copy.requireBackup = this.requireBackup;
            copy.confirmBeforeApply = this.confirmBeforeApply;
            return copy;
        }
        
        // Getters e Setters
        public int getMinAge() { return minAge; }
        public void setMinAge(int minAge) { this.minAge = minAge; }
        public int getMaxAge() { return maxAge; }
        public void setMaxAge(int maxAge) { this.maxAge = maxAge; }
        public boolean isRequireBackup() { return requireBackup; }
        public void setRequireBackup(boolean requireBackup) { this.requireBackup = requireBackup; }
        public boolean isConfirmBeforeApply() { return confirmBeforeApply; }
        public void setConfirmBeforeApply(boolean confirmBeforeApply) { this.confirmBeforeApply = confirmBeforeApply; }
    }
}
