package br.com.saveeditor.brasfoot.util;

import java.lang.reflect.Field;

/**
 * Classe de utilitários para operações de reflexão (Reflection), como obter e
 * definir valores de campos privados e converter tipos.
 */
public class ReflectionUtils {

    public static Object getFieldValue(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    public static void setFieldValue(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    public static Object converterStringParaTipoDoCampo(String valorStr, Class<?> tipoCampo) throws NumberFormatException {
        if (tipoCampo == int.class || tipoCampo == Integer.class) return Integer.parseInt(valorStr);
        if (tipoCampo == long.class || tipoCampo == Long.class) return Long.parseLong(valorStr);
        if (tipoCampo == double.class || tipoCampo == Double.class) return Double.parseDouble(valorStr);
        if (tipoCampo == float.class || tipoCampo == Float.class) return Float.parseFloat(valorStr);
        if (tipoCampo == boolean.class || tipoCampo == Boolean.class) {
            String lowerStr = valorStr.toLowerCase();
            if ("true".equals(lowerStr) || "false".equals(lowerStr)) return Boolean.parseBoolean(valorStr);
            throw new NumberFormatException("Valor inválido para booleano. Use 'true' ou 'false'.");
        }
        if (tipoCampo == String.class) return valorStr;
        throw new NumberFormatException("Tipo de campo '" + tipoCampo.getSimpleName() + "' não suportado para modificação.");
    }

    public static boolean isComplexObject(Object obj) {
        if (obj == null || obj.getClass().isPrimitive()) {
            return false;
        }
        Package pkg = obj.getClass().getPackage();
        return pkg == null || !pkg.getName().startsWith("java");
    }
}
