package com.example.sandbox.lottery.lotto;

import com.example.sandbox.functional.Either;

public interface Processor {
    Either<String, LottoZiehung> processLine(String line);
    int getMaxLottoNumber();
}
