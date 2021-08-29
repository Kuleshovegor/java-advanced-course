package info.kgeorgiy.ja.kuleshov.statistics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class TextStatistics {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("bad argument");
            return;
        }
        Locale textLocale = new Locale(args[0]);
        if (args[1].equals("en") || !args[1].equals("ru")) {
            System.err.println("bad out locale");
        }
        Locale outLocale = new Locale(args[1]);
        if (!Files.exists(Paths.get(args[2]))){
            System.err.println("file not found");
        }
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(args[2]))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            SentenceStatistic sentenceStatistic = new SentenceStatistic(textLocale);
            sentenceStatistic.parse(sb.toString());
            WordStatistic wordStatistic = new WordStatistic(textLocale);
            wordStatistic.parse(sb.toString());
            NumberStatistic numberStatistic = new NumberStatistic(textLocale);
            numberStatistic.parse(sb.toString());
            CurrencyStatistic currencyStatistic = new CurrencyStatistic(textLocale);
            currencyStatistic.parse(sb.toString());
            DateStatistic dateStatistic = new DateStatistic(textLocale);
            dateStatistic.parse(sb.toString());
            ResourceBundle bundle = ResourceBundle.getBundle("StatisticBundle", outLocale);
            NumberFormat numberFormat = NumberFormat.getNumberInstance(outLocale);
            String general = MessageFormat.format("{0}" + System.lineSeparator() +
                    "{1}: {2}" + System.lineSeparator() +
                    "{3}: {4}" + System.lineSeparator() +
                    "{5}: {6}" + System.lineSeparator() +
                    "{7}: {8}" + System.lineSeparator() +
                    "{9}: {10}" + System.lineSeparator(),
                    bundle.getString("general statistic"),
                    bundle.getString("Number of sentences"), numberFormat.format(sentenceStatistic.getOccurrencesNumber()),
                    bundle.getString("Number of words"), numberFormat.format(wordStatistic.getOccurrencesNumber()),
                    bundle.getString("Number of numbers"), numberFormat.format(numberStatistic.getOccurrencesNumber()),
                    bundle.getString("Number of money"), numberFormat.format(currencyStatistic.getOccurrencesNumber()),
                    bundle.getString("Number of dates"), numberFormat.format(dateStatistic.getOccurrencesNumber()));
            StringBuilder response = new StringBuilder();
            response.append(general).append(System.lineSeparator());
            response.append(sentenceStatistic.print(outLocale)).append(System.lineSeparator());
            response.append(wordStatistic.print(outLocale)).append(System.lineSeparator());
            response.append(numberStatistic.print(outLocale)).append(System.lineSeparator());
            response.append(currencyStatistic.print(outLocale)).append(System.lineSeparator());
            response.append(dateStatistic.print(outLocale)).append(System.lineSeparator());
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(args[3]))) {
                writer.write(response.toString());
            }catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
