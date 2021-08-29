package info.kgeorgiy.ja.kuleshov.statistics;

import java.util.ListResourceBundle;

public class StatisticBundle_en extends ListResourceBundle {
    private static final Object[][] CONTENTS = {
            {"Number of numbers", "Number of numbers"},
            {"Number of money", "Number of money"},
            {"Number of sentences", "Number of sentences"},
            {"Number of words", "Number of words"},
            {"Number of dates", "Number of dates"},
            {"different", "different"},
            {"statistic", "statstics.Statistic"},
            {"for number", "for number"},
            {"for money", "for money"},
            {"for date", "for date"},
            {"for words", "for words"},
            {"for sentences", "for sentences"},
            {"maximumLength", "maximum length"},
            {"minimumLength", "minimum length"},
            {"averageLength", "average length"},
            {"minValue", "minimum value"},
            {"maxValue", "maximum value"},
            {"averageValue", "average value"}
    };

    @Override
    protected Object[][] getContents() {
        return CONTENTS;
    }
}
