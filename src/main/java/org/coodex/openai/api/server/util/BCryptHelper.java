package org.coodex.openai.api.server.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class BCryptHelper {
    private static int BCRYPT_DEFAULT_COST = 12;
    public static String hash(String pass) {
        return BCrypt.withDefaults().hashToString(BCRYPT_DEFAULT_COST, pass.toCharArray());
    }

    public static boolean verify(String pass, String hash) {
        return BCrypt.verifyer().verify(pass.toCharArray(), hash.toCharArray()).verified;
    }
}
