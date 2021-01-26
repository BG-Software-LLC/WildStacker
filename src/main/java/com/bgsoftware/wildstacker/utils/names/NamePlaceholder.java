package com.bgsoftware.wildstacker.utils.names;

import java.util.function.Function;

public final class NamePlaceholder<T> {

    private final String placeholder;
    private final Function<T, String> parser;

    public NamePlaceholder(String placeholder, Function<T, String> parser){
        this.placeholder = placeholder;
        this.parser = parser;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public Function<T, String> getParser() {
        return parser;
    }

}
