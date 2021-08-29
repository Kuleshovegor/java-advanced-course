package info.kgeorgiy.ja.kuleshov.statistics;

public interface TextStatistic extends Statistic<String> {
    int getMaximumLength();

    String getMaximumLengthText();

    int getMinimumLength();

    String getMinimumLengthText();

    double getAverageLength();
}
