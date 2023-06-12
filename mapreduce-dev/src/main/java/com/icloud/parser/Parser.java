package com.icloud.parser;

public interface Parser<INPUT, OUTPUT> {

    OUTPUT parse(INPUT input);

}
