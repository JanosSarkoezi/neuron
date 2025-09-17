package com.example.sandbox.lotto;

import com.example.sandbox.validator.Either;

public interface Processor {
    Either<String, LottoZiehung> processLine(String line);
    int getMaxLottoNumber();
}
