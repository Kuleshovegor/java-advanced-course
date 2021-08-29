package info.kgeorgiy.ja.kuleshov.statistics;

import java.text.*;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

public class DateStatistic extends AbstractStatistic<Date> implements Statistic<Date> {
    private long sum;
    public DateStatistic(Locale locale) {
        super(locale);
    }

    @Override
    public void parse(String text) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);
        BreakIterator breakIterator = BreakIterator.getWordInstance(locale);
        breakIterator.setText(text);
        breakIterator.first();
        int start = breakIterator.first();
        for (int end = breakIterator.next();
             end != BreakIterator.DONE;
             start = end, end = breakIterator.next()) {
            String target = text.substring(start).trim();
            Date date;
            try {
                date = dateFormat.parse(target);
            } catch (ParseException ignored) {
                continue;
            }
            end = breakIterator.next();
            uniq.add(date);
            if (maxValue == null || date.compareTo(maxValue) > 0) {
                maxValue = date;
            }
            if (minValue == null || date.compareTo(minValue) < 0) {
                minValue = date;
            }
            sum += date.getTime();
            occurrencesNumber++;
        }
    }

    public double getAverageValue() {
        return (double)sum / occurrencesNumber;
    }

    public String print(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("StatisticBundle", locale);
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);
        return MessageFormat.format( bundle.getString("statistic") + " " + bundle.getString("for date") + System.lineSeparator() +
                        "{0}: {1} ({2} {3})" + System.lineSeparator() +
                        "{4}: {5}" + System.lineSeparator() +
                        "{6}: {7}" + System.lineSeparator() +
                        "{8}: {9}" + System.lineSeparator()
                ,
                bundle.getString("Number of dates"), numberFormat.format(getOccurrencesNumber()),
                bundle.getString("different"), numberFormat.format(getUniqOccurrencesNumber()),
                bundle.getString("minValue"), dateFormat.format(getMinimumValue()),
                bundle.getString("maxValue"), dateFormat.format(getMaximumValue()),
                bundle.getString("averageValue"), dateFormat.format((long)getAverageValue()));
    }
}
