package info.kgeorgiy.ja.kuleshov.rmi.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it is not already exists.
     * @param id account id
     * @return created or existing account.
     */
    Account createAccount(String id) throws RemoteException;

    /**
     * Returns account by identifier.
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exists.
     */
    Account getAccount(String id) throws RemoteException;

    /**
     * Returns local person {@link LocalPerson} by passportNumber.
     * @param passportNumber person passport number.
     * @return person with specified passport number or {@code null} if such person does not exists.
     */
    Person getLocalPerson(String passportNumber) throws RemoteException;

    /**
     * Returns remote person {@link RemotePerson} by passportNumber.
     * @param passportNumber person passport number.
     * @return person with specified passport number or {@code null} if such person does not exists.
     */
    Person getRemotePerson(String passportNumber) throws RemoteException;

    /**
     * Creates a new person with date if it is not already exists.
     * @param firstName person's first name.
     * @param lastName person's last name.
     * @param passportNumber person's passport number.
     * @return created or existing remote person.
     */
    Person createPerson(String firstName, String lastName, String passportNumber) throws RemoteException;

    /**
     * Returns accounts by person.
     * @param person persons.
     * @return map from account id {@link String} for this people to account {@link Account}
     */
    Map<String, Account> getAccounts(Person person) throws RemoteException;
}