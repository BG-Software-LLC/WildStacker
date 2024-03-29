package com.bgsoftware.wildstacker.utils;

import java.util.concurrent.ThreadLocalRandom;

public final class Random {

    public static int nextChance(double chance, int bound) {
        int result = 0;

        for (int i = 0; i < bound; i++)
            result += ThreadLocalRandom.current().nextDouble() < (chance / 100) ? 1 : 0;

        return result;
    }

    public static int nextInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    public static int nextInt(int min, int max, int amount, double exp) {
        min *= amount;
        max *= amount;

        double power = Math.pow(nextFloat(), exp);
        double bound = ((double) (max - min + 1)) * power;
        int rounded = Math.max(1, (int) Math.round(bound));

        return nextInt(rounded) + min;
    }

    public static int nextInt(int min, int max, int amount) {
        min *= amount;
        max *= amount;

        if (amount < 10) {
            return nextInt(max - min + 1) + min;
        } else {
            int avg = getAverage(min, max);
            return ensureRange(min, max, (int) Math.round(nextGaussian() * getSD(max, avg) + avg));
        }
    }

    private static double nextGaussian() {
        return ThreadLocalRandom.current().nextGaussian();
    }

    private static float nextFloat() {
        return ThreadLocalRandom.current().nextFloat();
    }

    private static int getAverage(int min, int max) {
        return (max + min) / 2;
    }

    private static int getSD(int max, int avg) {
        return (max - avg) / 3;
    }

    private static int ensureRange(int min, int max, int num) {
        return Math.min(max, Math.max(min, num));
    }

}
