package com.bgsoftware.wildstacker.utils.names;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public final class NameBuilder<T> {

    private NamePart<T> namePart;

    public NameBuilder(String pattern, NamePlaceholder<T>... placeholders){
        String[] words = pattern.split(" ");

        StringBuilder stringBuilder = new StringBuilder();

        for(String word : words){
            stringBuilder.append(" ");
            stringBuilder = handleWord(word, stringBuilder, placeholders);
        }

        if(namePart == null)
            namePart = new StaticPart<>(stringBuilder.toString());
        else
            namePart.addPart(new StaticPart<>(stringBuilder.toString()));
    }

    public String build(T argument){
        return namePart.build(argument);
    }

    private StringBuilder handleWord(String word, StringBuilder stringBuilder, NamePlaceholder<T>... placeholders){
        Optional<NamePlaceholder<T>> placeholder = Arrays.stream(placeholders)
                .filter(_placeholder -> word.contains(_placeholder.getPlaceholder()))
                .findFirst();

        if(placeholder.isPresent()){
            String rawPlaceholder = placeholder.get().getPlaceholder();
            int placeholderIndex = word.indexOf(rawPlaceholder);

            // Add string before the placeholder
            if(placeholderIndex > 0){
                stringBuilder.append(word, 0, placeholderIndex);
                if(namePart == null)
                    namePart = new StaticPart<>(stringBuilder.length() != 0 ? stringBuilder.substring(1) : stringBuilder.toString());
                else
                    namePart.addPart(new StaticPart<>(stringBuilder.toString()));
            }

            if(namePart == null)
                namePart = new PlaceholderPart<>(placeholder.get().getParser());
            else
                namePart.addPart(new PlaceholderPart<>(placeholder.get().getParser()));

            stringBuilder = new StringBuilder();

            // Add string after the placeholder
            if(placeholderIndex + rawPlaceholder.length() < word.length()){
                stringBuilder = handleWord(word.substring(placeholderIndex + rawPlaceholder.length()), stringBuilder, placeholders);
            }
        }
        else{
            stringBuilder.append(word);
        }

        return stringBuilder;
    }

    private static abstract class NamePart<T> {

        protected NamePart<T> nextPart;

        abstract String build(T argument);

        void addPart(NamePart<T> nextPart){
            if(this.nextPart == null) {
                this.nextPart = nextPart;
            }
            else{
                this.nextPart.addPart(nextPart);
            }
        }

    }

    private static final class StaticPart<T> extends NamePart<T> {

        private final String value;

        StaticPart(String value){
            this.value = value;
        }

        @Override
        String build(T argument) {
            return nextPart == null ? value : value + nextPart.build(argument);
        }

    }

    private static final class PlaceholderPart<T> extends NamePart<T> {

        private final Function<T, String> parser;

        PlaceholderPart(Function<T, String> parser){
            this.parser = parser;
        }

        @Override
        String build(T argument) {
            String value = parser.apply(argument);
            return nextPart == null ? value : value + nextPart.build(argument);
        }
    }

}
