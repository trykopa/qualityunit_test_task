package com.google.ssadm;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Main {

    private final static String testBlock = """
            7
            C 1.1 8.15.1 P 15.10.2012 83
            C 1 10.1 P 01.12.2012 65
            C 1.1 5.5.1 P 01.11.2012 117
            D 1.1 8 P 01.01.2012-01.12.2012 
            C 3 10.2 N 02.10.2012 100
            D 1 * P 8.10.2012-20.11.2012
            D 3 10 P 01.12.2012
            """;

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d.MM.yyyy");

    public static void main(String[] args) {

        String[][] lines = Arrays.stream(testBlock.trim().split("\n"))
                .map(string -> string.trim().split("\s+"))
                .toArray(String[][] :: new);

        printAverageWaitingTime(lines);
    }

    private static void printAverageWaitingTime(String[][] lines) {
        LocalDate dateFrom;
        LocalDate dateTo = null;
        for (int i = 1; i <= Integer.parseInt(lines[0][0]); i++) {
            if (lines[i][0].equals("D")) {
                int averageWaitingTime = 0;
                int count = 0;
                String service = lines[i][1];
                String question = lines[i][2];
                String responseType = lines[i][3];
                String requestDate = lines[i][4];
                if (requestDate.length() <= 10) {
                    dateFrom = LocalDate.parse(requestDate , dtf);
                } else {
                    String[] dates = requestDate.split("-");
                    dateFrom = LocalDate.parse(dates[0], dtf);
                    dateTo = LocalDate.parse(dates[1] , dtf);
                }
                for (int j = 1; j < i; j++) {
                    if (lines[j][0].equals("C")) {
                        if ((lines[j][1].startsWith(service) || service.equals("*")) &&
                                (lines[j][2].startsWith(question) || question.equals("*"))) {
                            if (lines[j][3].equals(responseType)) {
                                if (dateTo == null) {
                                    if (LocalDate.parse(lines[j][4] , dtf).isEqual(dateFrom)) {
                                        averageWaitingTime += Integer.parseInt(lines[j][5]);
                                        count++;
                                    }
                                } else {
                                    if (LocalDate.parse(lines[j][4] , dtf).isAfter(dateFrom) &&
                                            LocalDate.parse(lines[j][4] , dtf).isBefore(dateTo)) {
                                        averageWaitingTime += Integer.parseInt(lines[j][5]);
                                        count++;
                                    }
                                }
                            }
                        }
                    }
                }
                switch (count) {
                    case 0 -> System.out.println("-");
                    case 1 -> System.out.println(averageWaitingTime);
                    default -> System.out.println(averageWaitingTime/count);
                }
            }
        }
    }

}
