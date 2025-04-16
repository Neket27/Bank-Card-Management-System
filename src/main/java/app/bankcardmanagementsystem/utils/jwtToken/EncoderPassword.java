package app.bankcardmanagementsystem.utils.jwtToken;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class EncoderPassword {
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static String encode(String password){
        return passwordEncoder.encode(password);
    }

    public static boolean equalsPasswords(String rawPassword,String encodingPassword){
        return passwordEncoder.matches(rawPassword,encodingPassword);
    }

}
