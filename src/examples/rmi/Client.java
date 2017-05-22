package examples.rmi;

import java.rmi.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws RemoteException {
        Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }
        Account account = bank.getAccount("geo");
        if (account == null) {
            System.out.println("Creating account");
            account = bank.createAccount("geo");
        } else {
            System.out.println("Account already exists");
        }
        System.out.println("Money: " + account.getAmount());
        System.out.println("Adding money");
        account.setAmount(account.getAmount() + 100);        
        System.out.println("Money: " + account.getAmount());
    }
}
