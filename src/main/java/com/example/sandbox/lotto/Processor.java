package com.example.sandbox.lotto;

import com.example.sandbox.triade.Either;

public interface Processor {
    Either<String, LottoZiehung> processLine(String line);
    int getMaxLottoNumber();
}
