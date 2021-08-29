package info.kgeorgiy.ja.kuleshov.statistics;

import java.text.BreakIterator;
import java.util.Locale;

public class SentenceStatistic extends AbstractTextStatistic implements TextStatistic {
    public SentenceStatistic(Locale locale) {
        super(locale, BreakIterator.getSentenceInstance(locale));
    }

    public String print(Locale printLocal) {
        return super.print(printLocal, "sentences");
    }
}
