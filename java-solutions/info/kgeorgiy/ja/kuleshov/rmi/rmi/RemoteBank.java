package info.kgeorgiy.ja.kuleshov.rmi.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank, Serializable {
    private final int port;
    private final ConcurrentMap<String, Account> accounts;
    private final ConcurrentMap<String, Person> persons;

    public RemoteBank(final int port) {
        this.port = port;
        accounts = new ConcurrentHashMap<>();
        persons = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized Account createAccount(final String id) throws RemoteException {
        final Account account = new RemoteAccount(id);
        if (accounts.putIfAbsent(id, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getAccount(id);
        }
    }

    @Override
    public Account getAccount(final String id) {
        return accounts.get(id);
    }

    @Override
    public synchronized Person getLocalPerson(String passportNumber) throws RemoteException {
        if (persons.get(passportNumber) == null) {
            return null;
        }
        return new LocalPerson(persons.get(passportNumber));
    }

    @Override
    public synchronized Person getRemotePerson(String passportNumber) {
        return persons.get(passportNumber);
    }

    @Override
    public synchronized Person createPerson(String firstName, String lastName, String passportNumber) throws RemoteException {
        final Person person = new RemotePerson(firstName, lastName, passportNumber, this);
        if (persons.putIfAbsent(passportNumber, person) == null) {
            UnicastRemoteObject.exportObject(person, port);
            return person;
        } else {
            return getRemotePerson(passportNumber);
        }
    }

    @Override
    public synchronized Map<String, Account> getAccounts(Person person) throws RemoteException {
        Map<String, Account> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, Account> entry : accounts.entrySet()) {
            if (entry.getKey().startsWith(person.getPassportNumber() + ":")) {
                result.put(entry.getKey().substring(person.getPassportNumber().length() + 1), entry.getValue());
            }
        }
        return result;
    }
}
