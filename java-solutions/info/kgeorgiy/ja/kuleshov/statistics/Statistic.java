package info.kgeorgiy.ja.kuleshov.statistics;

import java.util.Locale;

public interface Statistic<T> {
    void parse(String text);

    int getOccurrencesNumber();

    int getUniqOccurrencesNumber();

    T getMinimumValue();

    T getMaximumValue();

    String print(Locale locale);
}
