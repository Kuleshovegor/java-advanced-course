package info.kgeorgiy.ja.kuleshov.statistics;

import java.util.ListResourceBundle;

public class StatisticBundle_ru extends ListResourceBundle {
    private static final Object[][] CONTENTS = {
            {"Number of numbers", "Число чисел"},
            {"Number of money", "Число сумм денег"},
            {"Number of sentences", "Число предложений"},
            {"Number of words", "Число слов"},
            {"Number of dates", "Количество дат"},
            {"different", "различных"},
            {"statistic", "Статистика"},
            {"for number", "для чисел"},
            {"for money", "для денег"},
            {"for date", "для дат"},
            {"for words", "для слов"},
            {"for sentences", "для предложений"},
            {"maximumLength", "максимальная длина"},
            {"minimumLength", "минимальная длина"},
            {"averageLength", "средняя длина"},
            {"minValue", "минимальное значение"},
            {"maxValue", "максимальное значение"},
            {"averageValue", "среднее значение"}
    };

    @Override
    protected Object[][] getContents() {
        return CONTENTS;
    }
}
