package com.atos.deployment.infrastructure.utils;

import org.jclouds.cloudsigma2.CloudSigma2Api;
import org.jclouds.cloudsigma2.domain.DriveInfo;
import org.jclouds.cloudsigma2.domain.DriveStatus;

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

}
