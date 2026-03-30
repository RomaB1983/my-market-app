package ru.yandex.practicum.payment.util;

import java.math.BigDecimal;
import java.util.Random;

public class BalanceGenerator {

    private static final Random random = new Random();

    public static BigDecimal get() {
        double mean =25.0;    // среднее: 25 000 руб.
        double stdDev = 15.0; // стандартное отклонение

        double balance;
        do {
            balance = mean + random.nextGaussian() * stdDev;
        } while (balance < 0.0); // исключаем отрицательные значения

        return BigDecimal.valueOf(Math.round(balance * 100) / 100); // округление до копеек
    }
}
