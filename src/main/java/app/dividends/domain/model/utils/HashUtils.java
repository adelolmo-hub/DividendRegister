package app.dividends.domain.model.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

	private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
	
	 public static String hashSHA256(String input) throws NoSuchAlgorithmException {
	        // 1. Obtener una instancia del algoritmo SHA-256
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");

	        // 2. Convertir la entrada a bytes (usando UTF-8 es común)
	        byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

	        // 3. Convertir el array de bytes a una cadena hexadecimal
	        return bytesToHex(encodedhash);
	    }
	
}
