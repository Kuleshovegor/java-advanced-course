package info.kgeorgiy.ja.kuleshov.rmi.rmi;

import org.junit.Before;
import org.junit.Test;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class BankTest {

    private static Bank bank;
    private static Random random;
    private static String firstName;
    private static String lastName;
    private static String passport;
    private static int test = 0;
    private static String accountId;

    @org.junit.BeforeClass
    public static void beforeClass() throws Exception {
        final Bank bank2 = new RemoteBank(8881);
        final Registry registry = LocateRegistry.createRegistry(8881);
        Remote stub = UnicastRemoteObject.exportObject(bank2, 0);
        registry.rebind("//localhost/bank", stub);
        bank = (Bank) registry.lookup("//localhost/bank");
        random = new Random();
    }

    @Before
    public void before() {
        test++;
        firstName = "test" + test + "firstName";
        lastName = "test" + test + "lastName";
        passport = "test" + test + "Passport";
        accountId = String.valueOf(random.nextInt());
    }

    @Test
    public void testLocalPerson() throws RemoteException {
        Person localPerson = new LocalPerson(firstName, lastName, passport);
        assertEquals(localPerson.getFirstName(), firstName);
        assertEquals(localPerson.getLastName(), lastName);
        assertEquals(localPerson.getPassportNumber(), passport);
    }

    @Test
    public void testRemotePerson() throws RemoteException {
        Person remotePerson = new RemotePerson(firstName, lastName, passport, bank);
        assertEquals(remotePerson.getFirstName(), firstName);
        assertEquals(remotePerson.getLastName(), lastName);
        assertEquals(remotePerson.getPassportNumber(), passport);
    }

    @Test
    public void createAndGetPerson() throws RemoteException {
        for (int i = 0; i < 200; i++) {
            bank.createPerson(firstName + i, lastName + i, passport + i);
        }
        for (int i = 0; i < 200; i++) {
            Person localPerson = bank.getLocalPerson(passport + i);
            Person remotePerson = bank.getRemotePerson(passport + i);
            assertEquals(localPerson.getFirstName(), firstName + i);
            assertEquals(localPerson.getLastName(), lastName + i);
            assertEquals(localPerson.getPassportNumber(), passport + i);
            assertEquals(remotePerson.getFirstName(), firstName + i);
            assertEquals(remotePerson.getLastName(), lastName + i);
            assertEquals(remotePerson.getPassportNumber(), passport + i);
        }
    }

    @Test
    public void creatAndGetRemoteAccount() throws RemoteException {
        for (int p = 0; p < 20; p++) {
            firstName += p;
            lastName += p;
            passport += p;
            bank.createPerson(firstName, lastName, passport);
            Person remotePerson = bank.getRemotePerson(passport);
            for (int i = 0; i < 100; i++) {
                Account account = remotePerson.createAccount(String.valueOf(i));
                account.setAmount(i * 10);
            }
            for (int i = 0; i < 100; i++) {
                Account account = remotePerson.getAccount(String.valueOf(i));
                assertNotNull(account);
                assertEquals(account.getAmount(), i * 10);
            }
            for (int i = 0; i < 100; i++) {
                Account fromPerson = remotePerson.getAccount(String.valueOf(i));
                Account fromBank = bank.getAccount(passport + ":" + i);
                assertNotNull(fromBank);
                assertEquals(fromBank.getId(), fromPerson.getId());
                assertEquals(fromBank.getAmount(), fromPerson.getAmount());
            }
        }
    }

    @Test
    public void creatAndGetLocalAccount() throws RemoteException {
        String test = "test5";
        for (int p = 0; p < 20; p++) {
            String firstName = test + "FirstName" + p;
            String lastName = test + "LastName" + p;
            String passport = test + "Passport" + p;
            bank.createPerson(firstName, lastName, passport);
            Person localPerson = bank.getLocalPerson(passport);
            for (int i = 0; i < 100; i++) {
                Account account = localPerson.createAccount(String.valueOf(i));
                account.setAmount(i * 10);
            }
            for (int i = 0; i < 100; i++) {
                Account account = localPerson.getAccount(String.valueOf(i));
                assertNotNull(account);
                assertEquals(account.getAmount(), i * 10);
            }
            for (int i = 0; i < 100; i++) {
                Account fromBank = bank.getAccount(passport + ":" + i);
                assertNull(fromBank);
            }
        }
    }

    @Test
    public void createAccountLocalPersonCheckLocalPerson() throws RemoteException {
        bank.createPerson(firstName, lastName, passport);
        Person firstLocalPerson = bank.getLocalPerson(passport);
        Person secondLocalPerson = bank.getLocalPerson(passport);
        secondLocalPerson.createAccount(accountId);
        assertNull(firstLocalPerson.getAccount(accountId));
    }

    @Test
    public void changeLocalPersonCheckLocalPerson() throws RemoteException {
        Person remotePerson = bank.createPerson(firstName, lastName, passport);
        remotePerson.createAccount(accountId);
        Person firstLocalPerson = bank.getLocalPerson(passport);
        Person secondLocalPerson = bank.getLocalPerson(passport);
        firstLocalPerson.getAccount(accountId).setAmount(-1000);
        secondLocalPerson.getAccount(accountId).setAmount(100);
        assertNotEquals(firstLocalPerson.getAccount(accountId).getAmount(),
                secondLocalPerson.getAccount(accountId).getAmount());
    }

    @Test
    public void changeRemotePersonCheckRemotePerson() throws RemoteException {
        bank.createPerson(firstName, lastName, passport);
        Person firstRemotePerson = bank.getRemotePerson(passport);
        Person secondRemotePerson = bank.getRemotePerson(passport);
        firstRemotePerson.createAccount(accountId);
        assertNotNull(secondRemotePerson.getAccount(accountId));
        firstRemotePerson.getAccount(accountId).setAmount(random.nextInt());
        assertEquals(secondRemotePerson.getAccount(accountId).getAmount(),
                firstRemotePerson.getAccount(accountId).getAmount());
    }

    @Test
    public void changeRemotePersonCheckLocalPerson() throws RemoteException {
        String accountId = String.valueOf(random.nextInt());
        bank.createPerson(firstName, lastName, passport);
        Person remotePerson = bank.getRemotePerson(passport);
        Person firstLocalPerson = bank.getLocalPerson(passport);
        remotePerson.createAccount(accountId);
        assertNull(firstLocalPerson.getAccount(accountId));
        Person secondLocalPerson = bank.getLocalPerson(passport);
        assertNotNull(secondLocalPerson.getAccount(accountId));
        assertEquals(secondLocalPerson.getAccount(accountId).getAmount(), remotePerson.getAccount(accountId).getAmount());
        remotePerson.getAccount(accountId).setAmount(random.nextInt());
        assertNotEquals(secondLocalPerson.getAccount(accountId).getAmount(), remotePerson.getAccount(accountId).getAmount());
        Person thirdLocalPerson = bank.getLocalPerson(passport);
        assertEquals(thirdLocalPerson.getAccount(accountId).getAmount(), remotePerson.getAccount(accountId).getAmount());
    }

    @Test
    public void changeLocalPersonCheckBank() throws RemoteException {
        String accountId = String.valueOf(random.nextInt());
        bank.createPerson(firstName, lastName, passport);
        Person firstLocalPerson = bank.getLocalPerson(passport);
        firstLocalPerson.createAccount(accountId);
        assertNull(bank.getAccount(passport + ":" + accountId));
        bank.createAccount(passport + ":" + accountId);
        Person secondLocalPerson = bank.getLocalPerson(passport);
        assertNotNull(secondLocalPerson.getAccount(accountId));
        secondLocalPerson.getAccount(accountId).setAmount(random.nextInt());
        assertNotEquals(bank.getAccount(passport + ":" + accountId).getAmount(),
                secondLocalPerson.getAccount(accountId).getAmount());
    }

    @Test
    public void changeLocalPersonCheckRemotePerson() throws RemoteException {
        String accountId = String.valueOf(random.nextInt());
        bank.createPerson(firstName, lastName, passport);
        Person firstLocalPerson = bank.getLocalPerson(passport);
        firstLocalPerson.createAccount(accountId);
        Person remotePerson = bank.getRemotePerson(passport);
        assertNull(remotePerson.getAccount(accountId));
        remotePerson.createAccount(accountId);
        Person secondLocalPerson = bank.getLocalPerson(passport);
        assertNotNull(secondLocalPerson.getAccount(accountId));
        secondLocalPerson.getAccount(accountId).setAmount(random.nextInt());
        assertNotEquals(remotePerson.getAccount(accountId).getAmount(),
                secondLocalPerson.getAccount(accountId).getAmount());
    }

    @Test
    public void changeRemoteAccountCheckBank() throws RemoteException {
        String accountId = String.valueOf(random.nextInt());
        Person remotePerson = bank.createPerson(firstName, lastName, passport);
        remotePerson.createAccount(accountId);
        remotePerson.getAccount(accountId).setAmount(random.nextInt());
        assertNotNull(bank.getAccount(passport + ":" + accountId));
        assertEquals(bank.getAccount(passport + ":" + accountId).getAmount(),
                remotePerson.getAccount(accountId).getAmount());
    }

    @Test
    public void changeBankCheckRemoteAccount() throws RemoteException {
        String accountId = String.valueOf(random.nextInt());
        Person remotePerson = bank.createPerson(firstName, lastName, passport);
        bank.createAccount(passport + ":" + accountId);
        bank.getAccount(passport + ":" + accountId).setAmount(random.nextInt());
        assertNotNull(remotePerson.getAccount(accountId));
        assertEquals(bank.getAccount(passport + ":" + accountId).getAmount(),
                remotePerson.getAccount(accountId).getAmount());
    }

    @Test
    public void multicoreTest() throws RemoteException {
        int tasks = 10;
        int threads = 10;
        Executor executor = Executors.newFixedThreadPool(threads);
        String accountId = String.valueOf(random.nextInt());
        Person person = bank.createPerson(firstName, lastName, passport);
        person.createAccount(accountId);
        Account account = person.getAccount(accountId);
        CountDownLatch countDownLatch = new CountDownLatch(tasks);
        Runnable task = () -> {
            try {
                account.addAmount(10);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();
        };
        for (int i = 0; i < tasks; i++) {
            executor.execute(task);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException ignored) {
        } finally {
            assertEquals( 10*tasks, account.getAmount());
        }
    }
}