package com._talent.lets_play.utils;

/**
 * Utilitaire pour masquer les informations sensibles
 */
public class SecurityMaskingUtils {
    /**
     * Masque le nom d'utilisateur pour les logs de sécurité
     */
    public static String maskUsername(String username) {
        if (username == null || username.length() <= 2) {
            return "***";
        }
        return username.substring(0, 2) + "***";
    }
}