package info.kgeorgiy.ja.kuleshov.rmi.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Person extends Remote {
    /**
     * Returns person's first name.
     * @return person's first name.
     */
    String getFirstName() throws RemoteException;

    /**
     * Returns person's last name.
     * @return person's last name.
     */
    String getLastName() throws RemoteException;

    /**
     * Returns person's passport ID.
     * @return person's passport ID.
     */
    String getPassportNumber() throws RemoteException;

    /**
     * Creates a new account for this person with specified identifier if it is not already exists.
     * @param id account id for this person.
     * @return created or existing account.
     */
    Account createAccount(String id) throws RemoteException;

    /**
     * Returns account for this person by identifier.
     * @param id account id for this person.
     * @return account with specified identifier or {@code null} if such account does not exists.
     */
    Account getAccount(String id) throws RemoteException;

    /**
     * Returns all accounts for this person.
     * @return map from account id {@link String} to account {@link Account}.
     */
    Map<String, Account> getAccounts() throws RemoteException;
}
