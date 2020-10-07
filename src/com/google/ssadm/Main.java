package com.google.ssadm;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Main {
    private static final ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1);

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

    public static void main(String[] args) throws InterruptedException {

        String[][] lines = Arrays.stream(testBlock.trim().split("\n"))
                .map(string -> string.trim().split("\s+"))
                .toArray(String[][] :: new);




        try {
            long startTime = System.nanoTime();
            for(int i = 0; i < 100; i++)
                printAverageWaitingTime(lines);
            long stopTime = System.nanoTime();

            long startTimeSecond = System.nanoTime();
            for(int i = 0; i < 100; i++)
                printAverageWaitingTimeConcurrent(lines);
            long stopTimeSecond = System.nanoTime();

            long startTimeThird = System.nanoTime();
            for(int i = 0; i < 100; i++)
                printAWTC2(lines);
            long stopTimeThird = System.nanoTime();

            System.out.println("Elapsed time single thread method:  " + (stopTime - startTime)/100 + "\n");
            System.out.println("Elapsed time multiple executors and threads method: " + (stopTimeSecond - startTimeSecond)/100 + "\n");
            System.out.println("Elapsed time single executor and multiple threads method " + (stopTimeThird - startTimeThird)/100 + "\n");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            es.shutdown();
            es.awaitTermination(Long.MAX_VALUE , TimeUnit.NANOSECONDS);
        }


    }

    private static void printAWTC2(String[][] lines) throws InterruptedException{
        for (int i = 1; i <= Integer.parseInt(lines[0][0]); i++){
            List<Callable<String>> callableList = new ArrayList<>();
            if (lines[i][0].equals("D")){
                String requestDate = lines[i][4];
                LocalDate dateFrom;
                LocalDate dateTo = null;
                if (requestDate.length() <= 10) {
                    dateFrom = LocalDate.parse(requestDate , dtf);
                } else {
                    String[] dates = requestDate.split("-");
                    dateFrom = LocalDate.parse(dates[0], dtf);
                    dateTo = LocalDate.parse(dates[1] , dtf);
                }
                LocalDate finalDateTo = dateTo;
                int finalI = i;

                Callable<String> callableTask = () -> {
                    String resultString;
                    AtomicInteger result = new AtomicInteger(0);
                    AtomicInteger count = new AtomicInteger(0);

                    for (int j = 1 ; j < finalI ; j++) {
                        checkLine(lines , lines[finalI] , dateFrom , finalDateTo, result , count , j);
                    }
                    switch (count.get()) {
                        case 0 -> resultString = "-";
                        case 1 -> resultString = String.valueOf(result.get());
                        default -> resultString = String.valueOf(result.get()/count.get());
                    }
                    return resultString;
                };
                callableList.add(callableTask);
            }
            List<Future<String>> futures = es.invokeAll(callableList);
            for(Future<String> f : futures){
                try {
                    System.out.println(f.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void checkLine(String[][] lines , String[] dLine ,
                                  LocalDate dateFrom , LocalDate finalDateTo , AtomicInteger result ,
                                  AtomicInteger count , int j) {

        if (lines[j][0].equals("C") && (lines[j][1].startsWith(dLine[1]) || dLine[1].equals("*")) &&
                (lines[j][2].startsWith(dLine[2]) || dLine[2].equals("*"))) {
            if (lines[j][3].equals(dLine[3])) {
                if (finalDateTo == null) {
                    if (LocalDate.parse(lines[j][4] , dtf).isEqual(dateFrom)) {
                        result.addAndGet(Integer.parseInt(lines[j][5]));
                        count.incrementAndGet();
                    }
                } else {
                    if (LocalDate.parse(lines[j][4] , dtf).isAfter(dateFrom) &&
                            LocalDate.parse(lines[j][4] , dtf).isBefore(finalDateTo)) {
                        result.addAndGet(Integer.parseInt(lines[j][5]));
                        count.incrementAndGet();
                    }
                }
            }
        }
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
                    if (lines[j][0].equals("C") && (lines[j][1].startsWith(service) || service.equals("*")) &&
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
                switch (count) {
                    case 0 -> System.out.println("-");
                    case 1 -> System.out.println(averageWaitingTime);
                    default -> System.out.println(averageWaitingTime/count);
                }
            }
        }
    }

    private static void printAverageWaitingTimeConcurrent(String[][] lines) throws InterruptedException {
        LocalDate dateFrom;
        LocalDate dateTo = null;
        for (int i = 1 ; i <= Integer.parseInt(lines[0][0]) ; i++) {
            if (lines[i][0].equals("D")) {
                String requestDate = lines[i][4];
                if (requestDate.length() <= 10) {
                    dateFrom = LocalDate.parse(requestDate , dtf);
                } else {
                    String[] dates = requestDate.split("-");
                    dateFrom = LocalDate.parse(dates[0] , dtf);
                    dateTo = LocalDate.parse(dates[1] , dtf);
                }
                AtomicInteger counter = new AtomicInteger(0);
                AtomicInteger awt = new AtomicInteger(0);
                LocalDate finalDateTo = dateTo;
                LocalDate finalDateFrom = dateFrom;

                // the wrong decision to create its own executor for each D line is too expensive
                // and becomes at least a little justified with a large number of lines
                // don't do that !!!

                ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

                int finalI = i;
                IntStream.range(0 , i).<Runnable>mapToObj(j ->
                        () -> checkLine(lines , lines[finalI], finalDateFrom , finalDateTo , awt , counter , j))
                        .forEach(executor :: submit);

                executor.shutdown();
                executor.awaitTermination(Long.MAX_VALUE , TimeUnit.NANOSECONDS);

                switch (counter.get()) {
                    case 0 -> System.out.println("-");
                    case 1 -> System.out.println(awt.get());
                    default -> System.out.println(awt.get() / counter.get());
                }
            }
        }
    }



}
