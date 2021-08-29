package info.kgeorgiy.ja.kuleshov.rmi.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalPerson extends AbstractPerson implements Serializable, Person {
    private Map<String, Account> accounts;

    public LocalPerson(String firstName, String lastName, String passportNumber) {
        super(firstName, lastName, passportNumber);
        accounts = new ConcurrentHashMap<>();
    }

    public LocalPerson(Person person) throws RemoteException {
        super(person.getFirstName(), person.getLastName(), person.getPassportNumber());
        accounts = new ConcurrentHashMap<>();
        for (Map.Entry<String, Account> accountEntry : person.getAccounts().entrySet()) {
            accounts.put(accountEntry.getKey(), LocalAccount.of(accountEntry.getValue()));
        }
    }

    @Override
    public synchronized Account createAccount(String id) {
        final Account account = new LocalAccount(id);
        if (accounts.putIfAbsent(id, account) == null) {
            return account;
        } else {
            return getAccount(id);
        }
    }

    @Override
    public Account getAccount(String id) {
        return accounts.get(id);
    }

    @Override
    public Map<String, Account> getAccounts() {
        return accounts;
    }

    @Override
    public String toString() {
        return "LocalPerson{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", passportNumber='" + passportNumber + '\'' +
                ", accounts=" + accounts +
                '}';
    }
}
