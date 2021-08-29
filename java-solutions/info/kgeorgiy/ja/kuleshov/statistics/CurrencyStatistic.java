package info.kgeorgiy.ja.kuleshov.statistics;

import java.text.BreakIterator;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.ResourceBundle;

public class CurrencyStatistic extends AbstractStatistic<Double> implements Statistic<Double> {
    private double sum;

    public CurrencyStatistic(Locale locale) {
        super(locale);
    }

    @Override
    public void parse(String text) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
        BreakIterator breakIterator = BreakIterator.getWordInstance(locale);
        breakIterator.setText(text);
        breakIterator.first();
        int start = breakIterator.first();
        for (int end = breakIterator.next();
             end != BreakIterator.DONE;
             start = end, end = breakIterator.next()) {
            String target = text.substring(start).trim();
            Number num;
            try {
                num = numberFormat.parse(target);
            } catch (ParseException ignored) {
                continue;
            }
            end = breakIterator.next();
            double d = num.doubleValue();
            uniq.add(d);
            if (maxValue == null) {
                maxValue = d;
            }
            if (minValue == null) {
                minValue = d;
            }
            maxValue = Double.max(d, maxValue);
            minValue = Double.min(d, minValue);
            sum += d;
            occurrencesNumber++;
        }
    }

    public double getAverageValue() {
        return sum / occurrencesNumber;
    }

    public String print(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("StatisticBundle", locale);
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
        return MessageFormat.format( bundle.getString("statistic") + " " + bundle.getString("for money") + System.lineSeparator() +
                        "{0}: {1} ({2} {3})" + System.lineSeparator() +
                        "{4}: {5}" + System.lineSeparator() +
                        "{6}: {7}" + System.lineSeparator() +
                        "{8}: {9}" + System.lineSeparator()
                ,
                bundle.getString("Number of money"), numberFormat.format(getOccurrencesNumber()),
                bundle.getString("different"), numberFormat.format(getUniqOccurrencesNumber()),
                bundle.getString("minValue"), currencyFormat.format(getMinimumValue()),
                bundle.getString("maxValue"), currencyFormat.format(getMaximumValue()),
                bundle.getString("averageValue"), currencyFormat.format(getAverageValue()));
    }
}
