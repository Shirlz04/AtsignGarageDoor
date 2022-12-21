package com.example.atsigngaragedoor;

import android.annotation.SuppressLint;

import org.atsign.client.api.AtClient;
import org.atsign.client.api.AtEvents;
import org.atsign.client.api.Secondary;
import org.atsign.client.api.impl.clients.AtClientImpl;
import org.atsign.client.api.impl.connections.AtRootConnection;
import org.atsign.client.api.impl.connections.DefaultAtConnectionFactory;
import org.atsign.client.api.impl.events.SimpleAtEventBus;
import org.atsign.client.api.impl.secondaries.RemoteSecondary;
import org.atsign.client.util.ArgsUtil;
import org.atsign.common.AtException;
import org.atsign.common.AtSign;
import org.atsign.common.KeyBuilders;
import org.atsign.common.Keys;
import org.atsign.common.NoSuchSecondaryException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
//import java.util.Base64;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class DataHandler{
    private AtSign atSignApp;
    private AtSign atSignDoor;
    private String atSignAppKey;
    private String root;
    private int ttl;
    private String key;
    String selfEncryptKey;
    FileInputStream file;
    private AtClient atClient;
    private Keys.PublicKey sk;
    private OutputStream out;
    private PrintStream printStream;
    private Boolean failed;
    //private Map<String, String> keys;


    DataHandler(String atSignAppName, String atSignDoorName, String atSignAppNameKey,
                FileOutputStream filesDir, FileInputStream fileInputStream) throws IOException, AtException, ExecutionException, InterruptedException {
        Security.addProvider(new BouncyCastleProvider());
        atSignApp = new AtSign(atSignAppName);
        atSignDoor = new AtSign(atSignDoorName);
        atSignAppKey = atSignAppNameKey;
        file = fileInputStream;
        //System.out.println(filesDir);
        FileOutputStream fout = filesDir;
        byte[] b = atSignAppName.getBytes();
        //filesDir.write(b);
        printStream = new PrintStream(filesDir);
        //

        // System.setOut(printStream);
        failed = false;
        //System.out.println("hello shirlz");
        key = "Instructions";
        //atSignApp.toString();
        ttl = 30 * 6 * 1000; // 30 minutes
        root = "root.atsign.org:64";
        intiAtsign();


    }

    /*
    The initAtsign function calls the decrypter() function to decrypt the keys that were
    scanned in from the .AtKeys file and connect to the AtSign server with the default AtSign which
    in this case will be @acidrock20.
     */
    private void intiAtsign() throws ExecutionException, InterruptedException, IOException {
        DefaultAtConnectionFactory connectionFactory = new DefaultAtConnectionFactory();
        AtEvents.AtEventBus eventBus = new SimpleAtEventBus();
        AtRootConnection hyh;
        AtRootConnection rootConnection = connectionFactory.getRootConnection(eventBus,root,true);
        Thread nextThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                    Map<String, String> keys = new HashMap<>();

                    String [] tempKeys = atSignAppKey.split(",");
                    selfEncryptKey = "";
                    Map<String,String> keyHolder = getEncryptKeys(tempKeys);
                    keys.put("aesEncryptPrivateKey",decrypter(keyHolder.get("aesEncryptPrivateKey"),selfEncryptKey));
                    keys.put("aesEncryptPublicKey",decrypter(keyHolder.get("aesEncryptPublicKey"),selfEncryptKey));
                    keys.put("aesPkamPrivateKey",decrypter(keyHolder.get("aesPkamPrivateKey"),selfEncryptKey));
                    keys.put("aesPkamPublicKey",decrypter(keyHolder.get("aesPkamPublicKey"),selfEncryptKey));
                    rootConnection.connect();
                    Secondary.AddressFinder saFinder = ArgsUtil.createAddressFinder(root);
                    Secondary.Address sAddress = null;
                    try {
                        sAddress = saFinder.findSecondary(atSignApp);

                    } catch (NoSuchSecondaryException | IOException e) {
                        System.err.println(e);
                        e.printStackTrace();
                        failed = true;
                    }
                    RemoteSecondary secondary = new RemoteSecondary(eventBus, atSignApp, sAddress,
                            keys, connectionFactory,true);
                    atClient = new AtClientImpl(eventBus,atSignApp,keys,secondary);
                    //atClient = new AtClient.withRemoteSecondary(root,atSignApp);*/
                    //System.out.println(secondary.toString());
                    sk = new KeyBuilders.PublicKeyBuilder(atSignApp).key(key).build();
                    sk.metadata.ttl = ttl;//delete if getting error now
                    //putSharedKey("1");
                    //getSharedKEy();
                    //filesDir.close();
                    //readFile(fileInputStream);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        nextThread.start();
    }

    /*
    The getEncryptKeys() function searches through the array string and seperates each key from
    its data nnd returns a map with the keys as the different data keys and the values are the
    encrypted data.
     */
    private Map<String,String> getEncryptKeys(String [] tempKeys){
        Map<String,String> keyHolder = new HashMap<>();
        for (int i = 0; i < tempKeys.length; i++){
            char [] temp = tempKeys[i].toCharArray();
            //System.out.println(temp[0]);
            int count = 0;
            boolean newWord = false;
            String putkey = "";
            String putVal = "";
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < temp.length; j++){
                if(temp[j] == '"' && newWord){
                    newWord = false;
                    count++;
                    if(count == 1){
                        putkey = stringBuilder.toString();
                        stringBuilder = new StringBuilder();
                        continue;
                    } else if(count == 2){
                        putVal = stringBuilder.toString();
                    }
                }
                else if(temp[j] == '"'){
                    newWord = true;
                    continue;
                }
                if (newWord){
                    stringBuilder.append(temp[j]);
                }
            }
            if(putkey.compareTo("selfEncryptionKey") == 0){
                //keys.put(putkey,putVal);
                System.out.println("Found encryp key");
                keyHolder.put(putkey,putVal);
                selfEncryptKey = putVal;
            } else {

                keyHolder.put(putkey,putVal);
            }
        }
        return keyHolder;
    }

    /*
    The getStatus function returns the last status of the door that was sent to the AtSign server
    in this case will return 1 or 2 depending on if the door is opem or closed.
     */
    public String getStatus() throws IOException, ExecutionException, InterruptedException {
        return getSharedKEy();
    }

    /*
    The decrypter() function decrypts the data using "AES/CTR" decrypting modes and returns the new
    data as a string.
     */
    private String decrypter(String ciph,String keyBase) throws NoSuchPaddingException, NoSuchAlgorithmException,
            NoSuchProviderException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException {
        Security.addProvider(new BouncyCastleProvider());
        byte[] keyBytes = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            keyBytes = Base64.getDecoder().decode(keyBase.getBytes());
        }
        SecretKey secKey = new SecretKeySpec(keyBytes,"AES");
        @SuppressLint("DeprecatedProvider") Cipher nh = Cipher.getInstance("AES/CTR/PKCS7Padding","BC");
        nh.init(Cipher.DECRYPT_MODE, secKey, new IvParameterSpec(new byte[16]));
        byte[] decrypted = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            decrypted = nh.doFinal(Base64.getDecoder().decode(ciph));
        }
        return new String(decrypted);
    }

    /*
    The getSharedKey9) function is used to get the data that was sent to the server .
    Needed to add jackson-annotation, jackson-databind and jackson-core libraries in order to use
    the get.
     */
    public String getSharedKEy() throws ExecutionException, InterruptedException, IOException {
        String response = "";
        try {
            System.out.println("atClient value is: "+atClient.toString());
            response = atClient.get(sk).get();

        } catch (NullPointerException e){
            System.err.println("atClient is empty");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("The data received is: "+response);
        //readFile();
        return response;
    }

    /*
    The putSharedKey() function is used to write data to the AtSign server.
     */
    public void putSharedKey(String value) throws IOException, NoSuchSecondaryException, ExecutionException, InterruptedException {
        String response = "";
        //Keys.PublicKey sk = new KeyBuilders.PublicKeyBuilder(atSignApp).key(key).build();
        //sk.metadata.ttl = ttl;
        //response = atClient.getAtKeys("@acidrock20").get();
        try {
            System.out.println(atClient.toString());
            response = atClient.put(sk,value).get();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Failed to put key " + e);
            e.printStackTrace();
        } catch (NullPointerException e){
            System.err.println("atClient is empty");
        }
        System.out.println("The put response is: "+response);
    }

    private void readFile() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(file);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();
        String input;
        //printStream.flush();
        System.out.println("Sysytem output here  :");
        PrintStream consoleOutput = System.out;
        //inputStreamReader.;
        printStream = new PrintStream(System.out);
        //System.setOut(consoleOutput);
        System.out.println(printStream);
        while ((input = bufferedReader.readLine()) != null){
            //input = bufferedReader.readLine();
            stringBuilder.append(input);
        }

        System.out.println("This is the stringBuilder : "+stringBuilder);
    }
}
