package info.kgeorgiy.ja.kuleshov.rmi.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;

public abstract class AbstractAccount implements Account, Serializable {
    protected final String id;
    protected long amount;

    public AbstractAccount(final String id) {
        this.id = id;
        amount = 0;
    }

    public AbstractAccount(final Account account) throws RemoteException {
        this.id = account.getId();
        this.amount = account.getAmount();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized long getAmount() {
        return amount;
    }

    @Override
    public synchronized void setAmount(final long amount) {
        this.amount = amount;
    }

    @Override
    public synchronized void addAmount(long diff) {
        this.amount += diff;
    }

    @Override
    public String toString() {
        return "LocalAccount{" +
                "id='" + id + '\'' +
                ", amount=" + amount +
                '}';
    }
}
