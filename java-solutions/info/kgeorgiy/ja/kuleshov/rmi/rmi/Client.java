package info.kgeorgiy.ja.kuleshov.rmi.rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public final class Client {
    /**
     * Utility class.
     */
    private Client() {
    }

    private static int DEFAULT_PORT=8888;

    public static void main(final String... args) throws RemoteException {
        final int port = args.length > 6 ? Integer.parseInt(args[6]) : DEFAULT_PORT;
        final Bank bank;
        try {
            Registry registry = LocateRegistry.getRegistry(port);
            bank = (Bank) registry.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        }

        if (args.length < 5 || args.length > 6) {
            System.err.println("Expected 5 or 6 arguments, but found " + args.length);
            return;
        }
        String firstName = args[0];
        String lastName = args[1];
        String passportNumber = args[2];
        String accountId = args[3];
        int diff;
        try {
            diff = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("Expected that diff is integer value, but " + e.getMessage());
            return;
        }

        Person person = bank.getRemotePerson(passportNumber);
        if (person != null && (!person.getFirstName().equals(firstName) || !person.getLastName().equals(lastName))) {
            System.err.println("Incorrect personal data");
            return;
        }
        if (person == null) {
            System.out.println("Creating person");
            person = bank.createPerson(firstName, lastName, passportNumber);
        } else {
            System.out.println("Person already exists");
        }
        Account account = person.getAccount(accountId);
        if (account == null) {
            System.out.println("Creating account");
            account = person.createAccount(accountId);
        } else {
            System.out.println("Account already exists");
        }
        System.out.println("Account id: " + account.getId());
        System.out.println("Money: " + account.getAmount());
        System.out.println("Adding money");
        account.addAmount(diff);
        System.out.println("Money: " + account.getAmount());
    }
}
