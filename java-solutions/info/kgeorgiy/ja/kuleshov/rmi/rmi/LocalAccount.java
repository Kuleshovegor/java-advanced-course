package info.kgeorgiy.ja.kuleshov.rmi.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;

public class LocalAccount extends AbstractAccount implements Account, Serializable {
    public LocalAccount(String id) {
        super(id);
    }

    public LocalAccount(Account account) throws RemoteException {
        super(account);
    }

    public static Account of(Account account) throws RemoteException {
        return new LocalAccount(account);
    }
}
