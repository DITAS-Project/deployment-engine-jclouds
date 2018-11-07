package com.atos.deployment.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public class Utils {

    public static <T> T retry(Predicate<T> checker, long timeout, long period, Getter<T> getter) throws TimeoutException, InterruptedException {
        long now = System.currentTimeMillis();
        long limit = now + timeout;
        T last = getter.get();
        boolean compliant = checker.test(last);
        while (!compliant && now < limit) {
            Thread.sleep(period);
            now = System.currentTimeMillis();
            last = getter.get();
            compliant = checker.test(getter.get());
        }

        if (!compliant) {
            throw new TimeoutException();
        }

        return last;
    }

    public static int executeProcess(List<String> command, String dir) throws IOException, InterruptedException {
        return executeProcess(new ProcessBuilder().command(command).directory(new File(dir)));
    }

    public static int executeProcess(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process process = processBuilder.inheritIO().start();
        return process.waitFor();
    }
}
