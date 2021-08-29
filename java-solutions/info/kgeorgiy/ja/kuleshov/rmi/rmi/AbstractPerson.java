package info.kgeorgiy.ja.kuleshov.rmi.rmi;

import java.io.Serializable;

public abstract class AbstractPerson implements Person, Serializable {
    protected String firstName;
    protected String lastName;
    protected String passportNumber;

    public AbstractPerson(String firstName, String lastName, String passportNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passportNumber = passportNumber;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getPassportNumber() {
        return passportNumber;
    }

    @Override
    public String toString() {
        return "Person{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", passportNumber='" + passportNumber + '\'' +
                '}';
    }
}
