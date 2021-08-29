package info.kgeorgiy.ja.kuleshov.statistics;

import java.text.BreakIterator;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.ResourceBundle;

public class NumberStatistic extends AbstractStatistic<Double> implements Statistic<Double> {
    private double sum;

    public NumberStatistic(Locale locale) {
        super(locale);
    }

    @Override
    public void parse(String text) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        BreakIterator breakIterator = BreakIterator.getWordInstance(locale);
        breakIterator.setText(text);
        breakIterator.first();
        int start = breakIterator.first();
        for (int end = breakIterator.next();
             end != BreakIterator.DONE;
             start = end, end = breakIterator.next()) {
            String target = text.substring(start,end).trim();
            Number num;
            try {
                num = numberFormat.parse(target);
            } catch (ParseException ignored) {
                continue;
            }
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

    public String print(Locale printLocal) {
        final ResourceBundle bundle = ResourceBundle.getBundle("StatisticBundle", printLocal);
        NumberFormat numberFormat = NumberFormat.getNumberInstance(printLocal);
        return MessageFormat.format( bundle.getString("statistic") + " " + bundle.getString("for number") + System.lineSeparator() +
                "{0}: {1} ({2} {3})" + System.lineSeparator() +
                "{4}: {5}" + System.lineSeparator() +
                "{6}: {7}" + System.lineSeparator() +
                "{8}: {9}" + System.lineSeparator()
                ,
                bundle.getString("Number of numbers"), numberFormat.format(getOccurrencesNumber()),
                bundle.getString("different"), numberFormat.format(getUniqOccurrencesNumber()),
                bundle.getString("minValue"), numberFormat.format(getMinimumValue()),
                bundle.getString("maxValue"), numberFormat.format(getMaximumValue()),
                bundle.getString("averageValue"), numberFormat.format(getAverageValue()));
    }

    public double getAverageValue() {
        return sum / occurrencesNumber;
    }

}
