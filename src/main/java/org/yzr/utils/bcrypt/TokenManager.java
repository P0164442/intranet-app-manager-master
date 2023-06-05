package org.yzr.utils.bcrypt;

import com.qcloud.cos.utils.Md5Utils;


public class TokenManager {
    // 秘钥
    static final String SECRET = "X-app-manager-Token";

    /**
     *
     *
     * @param username
     * @param password
     * @return
     */
    public static String generateToken(String username, String password) {
        return Md5Utils.md5Hex(username + "&&" + password + "&&" + SECRET);
    }

}
