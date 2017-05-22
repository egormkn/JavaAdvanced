package examples.rmi;

import java.rmi.*;

public interface Bank extends Remote {
    // Создает счет
    public Account createAccount(String id) 
        throws RemoteException;
    // Возвращает счет
    public Account getAccount(String id) 
        throws RemoteException;
}
