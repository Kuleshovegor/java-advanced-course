package info.kgeorgiy.ja.kuleshov.rmi.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Account extends Remote {
    /** Returns account identifier. */
    String getId() throws RemoteException;

    /** Returns amount of money at the account. */
    long getAmount() throws RemoteException;

    /** Sets amount of money at the account. */
    void setAmount(long amount) throws RemoteException;

    /** Adds amount of money at the account. */
    void addAmount(long diff) throws RemoteException;
}