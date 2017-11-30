import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;


public class Encryption {

    public static String get_SHA_512_SecurePassword(String passwordToHash, String salt) {
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(passwordToHash.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    public static SecretKey getSecretEncryptionKey(String data) throws Exception {
        //while (data.length() < 100) {
        //    data += 'x';
        //}
        byte[] key = data.getBytes();
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);//256 not avail
        // rebuild key using SecretKeySpec
        SecretKey originalKey = new SecretKeySpec(key, "AES");
        return originalKey;
    }


    public static byte[] encryptText(String plainText, SecretKey secKey) throws Exception {
        // AES defaults to AES/ECB/PKCS5Padding in Java 7
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, secKey);
        byte[] byteCipherText = aesCipher.doFinal(plainText.getBytes());
        return byteCipherText;
    }


    public static String decryptText(byte[] byteCipherText, SecretKey secKey) throws Exception {
        // AES defaults to AES/ECB/PKCS5Padding in Java 7
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, secKey);
        byte[] bytePlainText = aesCipher.doFinal(byteCipherText);
        return new String(bytePlainText);
    }
    public static String hashvalue(String passwordd,String username,String message){
    SecretKey key;
        try {
                key = Encryption.getSecretEncryptionKey(passwordd);
            } catch (Exception ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            String salt = passwordd + username;
            String hashvalue = Encryption.get_SHA_512_SecurePassword(message, salt);
            return hashvalue;

    }
    public static String encryptedtext(String message,String passwordd) throws Exception{
        SecretKey key = null;
        try {
                key = Encryption.getSecretEncryptionKey(passwordd);
            } catch (Exception ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
    return DatatypeConverter.printHexBinary(encryptText(message, key));
    }
    public static String decryptedtext(String message,String password){
        String msg="";
        try {
                            msg = Encryption.decryptText(DatatypeConverter.parseHexBinary(message), Encryption.getSecretEncryptionKey(password));
                        } catch (Exception ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
    return msg;

    }
}
