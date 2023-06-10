package com.icloud.parser;

public interface Parser<INPUT, OUTPUT> {
    OUTPUT parse(String value);

    OUTPUT parse(INPUT INPUT);
}
