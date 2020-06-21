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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Server {
    static String caseResponse;
    static String shiftResponse;
    static String colorResponse;
    static String clientResponse;
    static int clientID = 1;

    public static void main(String argv[]) {
        try (ServerSocket welcomeSocket = new ServerSocket(6789)) {
            while (true) {
                GenerateKeys keyGenerator = new GenerateKeys();
                keyGenerator.generateKeyPairs(clientID);
                new Transformer(welcomeSocket.accept(), clientID).start();
                clientID++;
            }
        } catch (Exception e) {
            System.out.println("Welcome server error: " + e.getMessage());
        }
    }

    static class Transformer extends Thread {
        Socket socket;
        int clientNo;
        DataInputStream inStream;
        DataOutputStream outStream;
        String clientInput;
        byte[] plain_text;
        byte[] encrypted_text;
        byte[] decrypted_text;
        Cipher cipher;
        PrivateKey privateKey;
        PublicKey publicKey;


        public Transformer(Socket socket, int clientNumber) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException {
            this.socket = socket;
            this.clientNo = clientNumber;
            inStream = new DataInputStream(this.socket.getInputStream());
            outStream = new DataOutputStream(this.socket.getOutputStream());
            clientInput = "";

            byte[] keyBytes = Files.readAllBytes(new File("./KeyStore/client" + clientID + "keyPairs/privateKey").toPath()); // read from related client-server file
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            privateKey = kf.generatePrivate(spec);

            keyBytes = Files.readAllBytes(new File("./KeyStore/client" + clientID + "keyPairs/publicKey").toPath()); // read from related client-server file
            X509EncodedKeySpec spec2 = new X509EncodedKeySpec(keyBytes);
            publicKey = kf.generatePublic(spec2);

            cipher = Cipher.getInstance("RSA");
        }

        @Override
        public void run() {
            try {
                System.out.println("Client" + clientNo + " is connected to the server");
                outStream.writeUTF(String.valueOf(clientNo));
                outStream.flush();
                while (!clientInput.equals("exit")) {
                    clientInput = inStream.readUTF();  // ENCODED message got from Client" + clientNo
                    encrypted_text = Base64.getDecoder().decode(clientInput); // DECODE
                    cipher.init(Cipher.DECRYPT_MODE, privateKey);
                    decrypted_text = cipher.doFinal(encrypted_text);

                    String decryptedClientInput = new String(decrypted_text); // DECRYPTED message got from Client" + clientNo
                    System.out.println("Server got a message from client"+clientNo+" =>>>>>>>> " + decryptedClientInput);


                    Thread caseThread = new Thread(new ChangeCaseType(decryptedClientInput));
                    caseThread.start();
                    caseThread.join();

                    Thread shiftTread = new Thread(new ShiftLetters(caseResponse));
                    shiftTread.start();
                    shiftTread.join();

                    Thread colorThread = new Thread(new ChangeColor(shiftResponse));
                    colorThread.start();
                    colorThread.join();

                    clientResponse = colorResponse;
                    System.out.println("Server transformed the client"+clientNo+" message into =>>>>>>>> " + clientResponse);

                    // ENCRYPT message
                    plain_text = clientResponse.getBytes(StandardCharsets.UTF_8);

                    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                    encrypted_text = cipher.doFinal(plain_text);
                    String responseMsgBase64 = Base64.getEncoder().encodeToString(encrypted_text); // encode before send it to the client

                    outStream.writeUTF(responseMsgBase64);
                    outStream.flush();
                }
                inStream.close();
                outStream.close();
                socket.close();

            } catch (IOException | InterruptedException e) {
                System.out.println("Oopss err: ");
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } finally {
                try {
                    inStream.close();
                    outStream.close();
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static class ChangeCaseType implements Runnable {
        private String clientMessage;

        public ChangeCaseType(String message) {
            this.clientMessage = message;
        }

        @Override
        public void run() {
            caseResponse = clientMessage.toUpperCase();
        }
    }

    public static class ShiftLetters implements Runnable {
        private String clientMessage;

        public ShiftLetters(String message) {
            this.clientMessage = message;
        }

        @Override
        public void run() {
            shiftResponse = "";
            for (int i = 0; i < clientMessage.length(); i++) {
                int asciiCode = (int) clientMessage.charAt(i) + 3;
                char shiftedLetter = (char) asciiCode;
                shiftResponse += String.valueOf(shiftedLetter);
            }
        }
    }

    public static class ChangeColor implements Runnable {
        private String clientMessage;

        public ChangeColor(String message) {
            this.clientMessage = message;
        }

        @Override
        public void run() {
            colorResponse = "";

            final String ANSI_RED = "\u001B[31m";
            final String ANSI_RESET = "\u001B[0m";

            for (int i = 0; i < clientMessage.length(); i++) {
                colorResponse = colorResponse + (ANSI_RED + clientMessage.charAt(i) + ANSI_RESET);
            }
        }
    }

}
