package info.kgeorgiy.ja.kuleshov.rmi.rmi;

import java.rmi.RemoteException;
import java.util.Map;

public class RemotePerson extends AbstractPerson implements Person {
    private Bank bank;

    public RemotePerson(String firstName, String lastName, String passportNumber, Bank bank) {
        super(firstName, lastName, passportNumber);
        this.bank = bank;
    }

    @Override
    public synchronized Account createAccount(String id) throws RemoteException {
        return bank.createAccount(passportNumber + ":" + id);
    }

    @Override
    public synchronized Account getAccount(String id) throws RemoteException {
        return bank.getAccount(passportNumber + ":" + id);
    }

    @Override
    public synchronized Map<String, Account> getAccounts() throws RemoteException {
        return bank.getAccounts(this);
    }


}
