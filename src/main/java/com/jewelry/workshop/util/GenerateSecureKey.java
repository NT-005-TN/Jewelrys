package com.jewelry.workshop.util;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Base64;

public class GenerateSecureKey {
    public static void main(String[] args) {
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512); // Генерирует 512-битный ключ
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println(base64Key);
    }
}