package info.kgeorgiy.ja.kuleshov.statistics;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public abstract class AbstractStatistic<T> implements Statistic<T> {
    protected final Locale locale;
    protected T minValue;
    protected T maxValue;
    protected Set<T> uniq;
    protected int occurrencesNumber;

    public AbstractStatistic(Locale locale) {
        this.locale = locale;
        uniq = new HashSet<>();
        occurrencesNumber = 0;
    }

    public T getMinimumValue() {
        return minValue;
    }

    public T getMaximumValue() {
        return maxValue;
    }

    public int getOccurrencesNumber() {
        return occurrencesNumber;
    }

    public int getUniqOccurrencesNumber() {
        return uniq.size();
    }
}
