/*
*
  EMRE SERBEST
  20160602115
*
* */
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;



public class Client {

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 6789)) {
            socket.setSoTimeout(5000);
            DataInputStream inStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String clientInput = "",
                    serverResponse = "";


            byte[] plain_text;
            byte[] encrypted_text;
            byte[] decrypted_text;
            Cipher cipher;

            String clientID = inStream.readUTF();

            System.out.println("Assigned clientId: " + clientID);

            byte[] keyBytes = Files.readAllBytes(new File("./KeyStore/client"+ clientID +"keyPairs/privateKey").toPath());
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = kf.generatePrivate(spec);

            keyBytes = Files.readAllBytes(new File("./KeyStore/client"+ clientID +"keyPairs/publicKey").toPath());
            X509EncodedKeySpec spec2 = new X509EncodedKeySpec(keyBytes);
            PublicKey publicKey = kf.generatePublic(spec2);

            cipher = Cipher.getInstance("RSA");


            while (!clientInput.equals("exit")) {
                System.out.println("Enter your message to be transformed...");
                clientInput = br.readLine();

                plain_text = clientInput.getBytes(StandardCharsets.UTF_8);
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                encrypted_text = cipher.doFinal(plain_text);
                String clientInputBase64 = Base64.getEncoder().encodeToString(encrypted_text);

                outStream.writeUTF(clientInputBase64);
                outStream.flush();

                serverResponse = inStream.readUTF();

                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                encrypted_text = Base64.getDecoder().decode(serverResponse);
                decrypted_text = cipher.doFinal(encrypted_text);

                String decryptedClientInput = new String(decrypted_text);

                System.out.println("THE SERVER TRANSFORMED THE MESSAGE: " + decryptedClientInput);

            }

            inStream.close();
            outStream.close();
            socket.close();

        } catch (SocketTimeoutException e) {
            System.out.println("The socket timed out");
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

}



