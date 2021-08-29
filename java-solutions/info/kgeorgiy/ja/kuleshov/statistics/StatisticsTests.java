package info.kgeorgiy.ja.kuleshov.statistics;

import org.junit.Test;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatisticsTests {
    private Date date = new Date(64);
    private String strDate = DateFormat.getDateInstance(DateFormat.FULL, new Locale("ru_ru")).format(date);
    private final String textRu = "Если вы хотите, чтобы в Excel числа определенных типов воспринимались как текст, используйте вместо числового формата текстовый." +
            " Например, при использовании номеров кредитных карт за " + NumberFormat.getCurrencyInstance(new Locale("ru_ru")).format(12) + " или других числового кода, содержащих не менее 16 цифр или " + strDate +" , необходимо использовать текстовый формат." +
            " Это происходит потому Excel что точность не может быть больше 15 цифр, и она округлит все числа после 15-й цифры до нуля, что, вероятно, вас не захотнет.";
    private final String[] sentences = new String[]{"Если вы хотите, чтобы в Excel числа определенных типов воспринимались как текст, используйте вместо числового формата текстовый." ,
            "Например, при использовании номеров кредитных карт за " + NumberFormat.getCurrencyInstance(new Locale("ru_ru")).format(12) + " или других числового кода, содержащих не менее 16 цифр или "+ strDate +" , необходимо использовать текстовый формат." ,
            "Это происходит потому Excel что точность не может быть больше 15 цифр, и она округлит все числа после 15-й цифры до нуля, что, вероятно, вас не захотнет."};


    @Test
    public void testSentence() {
        SentenceStatistic sentenceStatistic = new SentenceStatistic(new Locale("ru_ru"));
        sentenceStatistic.parse(textRu);
        assertEquals(sentenceStatistic.getOccurrencesNumber(), sentences.length);
        assertEquals(sentenceStatistic.getMaximumLength(), sentences[1].length());
        assertEquals(sentenceStatistic.getMaximumLengthText(), sentences[1]);
        assertEquals(sentenceStatistic.getMinimumLength(), sentences[0].length());
        assertEquals(sentenceStatistic.getMinimumLengthText(), sentences[0]);
        assertEquals(sentenceStatistic.getAverageLength(), ((double)textRu.length() - sentences.length + 1) / sentences.length);
        assertEquals(sentenceStatistic.getUniqOccurrencesNumber(), 3);
        assertEquals(sentenceStatistic.getMaximumValue(), sentences[2]);
        assertEquals(sentenceStatistic.getMinimumValue(), sentences[0]);
    }

    @Test
    public void testWord() {
        WordStatistic wordStatistic = new WordStatistic(new Locale("ru_ru"));
        wordStatistic.parse(textRu);
        assertEquals(wordStatistic.occurrencesNumber, 87);
        assertEquals(wordStatistic.getMaximumValue(), "чтобы");
        assertEquals(wordStatistic.getMaximumLength(), 14);
        assertEquals(wordStatistic.getMaximumLengthText(), "воспринимались");
    }

    @Test
    public void testNumber() {
        NumberStatistic numberStatistic = new NumberStatistic(new Locale("ru"));
        numberStatistic.parse(textRu);
        assertEquals(numberStatistic.getOccurrencesNumber(), 6);
        assertEquals(numberStatistic.getUniqOccurrencesNumber(), 5);
    }

    @Test
    public void testCurrency() {
        CurrencyStatistic currencyStatistic = new CurrencyStatistic(new Locale("ru_ru"));
        currencyStatistic.parse(textRu);
        assertEquals(currencyStatistic.getOccurrencesNumber(), 1);
        assertEquals(currencyStatistic.getAverageValue(), 12.0);
        assertEquals(currencyStatistic.getMinimumValue(), 12);
    }

    @Test
    public void testData() {
        DateStatistic dateStatistic = new DateStatistic(new Locale("ru_ru"));
        dateStatistic.parse(textRu);
        assertEquals(dateStatistic.getOccurrencesNumber(), 1);
        assertEquals(dateStatistic.getUniqOccurrencesNumber(), 1);
    }
}
