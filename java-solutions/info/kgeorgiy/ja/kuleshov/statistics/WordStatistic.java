package info.kgeorgiy.ja.kuleshov.statistics;

import java.text.BreakIterator;
import java.util.Locale;

public class WordStatistic extends AbstractTextStatistic implements TextStatistic {
    public WordStatistic(Locale locale) {
        super(locale, BreakIterator.getWordInstance(locale));
    }

    public String print(Locale printLocal) {
        return super.print(printLocal, "words");
    }
}
