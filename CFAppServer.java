/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cfappserver;

import com.sun.net.ssl.internal.ssl.Provider;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

/**
 *
 * @author User1
 */
public class CFAppServer {

    static Connection conn;
    static Statement stmt;

    public static void main(String[] args) throws Exception {
        /*Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        //STEP 3: Open a connection
        System.out.println("Connecting to database...");
        conn = DriverManager.getConnection("jdbc:sqlserver://173.227.86.24:1433", "Cfisd", "cfisd01");
        System.out.println("Connecting to database2...");

        //STEP 4: Execute a query
        stmt = conn.createStatement();*/
        Security.addProvider(new Provider());
        
        System.setProperty("javax.net.ssl.keyStore", "mySrvKeystore");
        
        System.setProperty("javax.net.ssl.keyStorePassword","123456");
        //ServerSocket ss = new ServerSocket(6789);
        SSLServerSocketFactory sslserversocketfactory
                = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslserversocket
                = (SSLServerSocket) sslserversocketfactory.createServerSocket(6789);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        while (true) {
            SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();
            Runnable worker = new WorkerThread("", sslsocket);
            executor.execute(worker);
            //Thread.sleep(1000);
        }
    }
}

class WorkerThread implements Runnable {

    private String command;
    private String lastUpdate = "";
    private SSLSocket soc;
    private DataOutputStream oc;
    private BufferedReader br;
    private int indx;
    public int amount;
    private String name;

    public WorkerThread(String s, SSLSocket so) throws IOException {
        this.command = s;
        soc = so;
        oc = new DataOutputStream(so.getOutputStream());
        br = new BufferedReader(new InputStreamReader(so.getInputStream()));
    }

    @Override
    public void run() {
        processCommand();
    }

    private void processCommand() {
        try {
            long a = System.currentTimeMillis();
            String str = br.readLine();
            String[] userpass = str.split(" ");
            if (userpass.length > 1) {
                if (userpass.length > 2 && ((int) userpass[2].charAt(0)) > 65) {

                } else {
                    System.out.println(userpass[0] + " " + userpass[1]);
                    oc.writeBytes(Bot.genData(userpass[0], userpass[1], Integer.parseInt(userpass[2])) + '\n');
                }
                System.out.println("Runtime: " + ((System.currentTimeMillis() - a) / 1000) + "s");
            }
            soc.close();
        } catch (Exception ex) {
            Logger.getLogger(WorkerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
