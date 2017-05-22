package examples.rmi;

import java.rmi.*;
import java.rmi.server.*;
import java.net.*;

public class Server {
    private final static int PORT = 8888;
    public static void main(String[] args) {
        Bank bank = new BankImpl(PORT);
        try {
            UnicastRemoteObject.exportObject(bank, PORT);
            Naming.rebind("//localhost/bank", bank);
        } catch (RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL");
        }
        System.out.println("Server started");
    }
}
