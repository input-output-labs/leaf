package fr.iolabs.leaf.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringHasher {
	
	private static Logger logger = LoggerFactory.getLogger(StringHasher.class);
	
    private static final String SALT = "EUtebITLAE2vGdqE7ZRaamPBpWj1tANt";
    
    private StringHasher() {
    	throw new IllegalStateException("Utility class");
    }
    
    public static String hashString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(SALT.getBytes());
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage());
        }
        return null;
    }
}
