package info.kgeorgiy.ja.kuleshov.statistics;

import java.text.BreakIterator;
import java.text.Collator;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class AbstractTextStatistic extends AbstractStatistic<String> implements TextStatistic {
    private int maximumLength;
    private int minimumLength;
    private String maximumLengthText;
    private String minimumLengthText;
    private int sum;
    private final BreakIterator breakIterator;
    private final Collator collator;

    public AbstractTextStatistic(Locale locale, BreakIterator breakIterator) {
        super(locale);
        this.breakIterator = breakIterator;
        collator = Collator.getInstance(locale);
        collator.setStrength(Collator.IDENTICAL);
        minimumLength = Integer.MAX_VALUE;
    }

    @Override
    public void parse(String text) {
        breakIterator.setText(text);
        breakIterator.first();
        int start = breakIterator.first();
        for (int end = breakIterator.next();
             end != BreakIterator.DONE;
             start = end, end = breakIterator.next()) {
            String target = text.substring(start,end).trim();
            if (target.isEmpty()){
                continue;
            }
            occurrencesNumber++;
            uniq.add(target);
            sum += target.length();
            if (target.length() > maximumLength) {
                maximumLength = target.length();
                maximumLengthText = target;
            }
            if (target.length() < minimumLength) {
                minimumLength = target.length();
                minimumLengthText = target;
            }
            if (maxValue == null || collator.compare(target, maxValue) > 0) {
                maxValue = target;
            }
            if (minValue == null || collator.compare(target, minValue) < 0) {
                minValue = target;
            }
        }
    }

    @Override
    public int getMaximumLength() {
        return maximumLength;
    }

    @Override
    public int getMinimumLength() {
        return minimumLength;
    }

    @Override
    public String getMaximumLengthText() {
        return maximumLengthText;
    }

    @Override
    public String getMinimumLengthText() {
        return minimumLengthText;
    }

    @Override
    public double getAverageLength() {
        return ((double)sum)/occurrencesNumber;
    }

    public String print(Locale printLocal, String str) {
        final ResourceBundle bundle = ResourceBundle.getBundle("StatisticBundle", printLocal);
        NumberFormat numberFormat = NumberFormat.getNumberInstance(printLocal);
        return MessageFormat.format( bundle.getString("statistic") + " " + bundle.getString("for " + str) + System.lineSeparator() +
                        "{0}: {1} ({2} {3})" + System.lineSeparator() +
                        "{4}: {5}" + System.lineSeparator() +
                        "{6}: {7}" + System.lineSeparator() +
                        "{8}: {9} (\"{10}\")" + System.lineSeparator() +
                        "{11}: {12} (\"{13}\")" + System.lineSeparator() +
                        "{14}: {15}" + System.lineSeparator()
                ,
                bundle.getString("Number of " + str), numberFormat.format(getOccurrencesNumber()),
                bundle.getString("different"), numberFormat.format(getUniqOccurrencesNumber()),
                bundle.getString("minValue"), getMinimumValue(),
                bundle.getString("maxValue"), getMaximumValue(),
                bundle.getString("maximumLength"), getMaximumLength(), getMaximumLengthText(),
                bundle.getString("minimumLength"), getMinimumLength(), getMinimumLengthText(),
                bundle.getString("averageLength"), numberFormat.format(getAverageLength()));
    }
}
