package com.bgsoftware.wildstacker.utils.data;

public final class DataSerializer {

    private static final char COLOR_CHAR = '§';
    private static final char NUMBERS_SPACER = '~';

    private DataSerializer() { }

    public static String serializeData(String str){
        str = ensureNotNull(str);

        if(str.isEmpty())
            return str;

        StringBuilder result = new StringBuilder();

        for(char ch : str.toCharArray()) {
            if(isNumber(ch))
                result.append(COLOR_CHAR).append(NUMBERS_SPACER);
            result.append(COLOR_CHAR).append(ch);
        }

        // Marks end of data segment
        result.append(COLOR_CHAR).append("|").append(COLOR_CHAR).append("f");

        return result.toString();
    }

    public static String deserializeData(String str){
        str = ensureNotNull(str);

        if(str.isEmpty())
            return str;

        StringBuilder result = new StringBuilder();
        boolean hasEndTag = false;

        for(int i = 0; i < str.length() && !hasEndTag; i++) {
            char curr = str.charAt(i);

            if(curr == '|' && i > 0 && str.charAt(i - 1) == COLOR_CHAR) {
                hasEndTag = true;
            }
            else if(curr != COLOR_CHAR && curr != NUMBERS_SPACER) {
                result.append(curr);
            }
        }

        return hasEndTag ? result.toString() : "";
    }

    public static String stripData(String str){
        str = ensureNotNull(str);
        String[] sections = str.split(COLOR_CHAR + "\\|");
        return sections[sections.length - 1];
    }

    private static String ensureNotNull(String str){
        return str == null ? "" : str;
    }

    private static boolean isNumber(char ch){
        return ch >= '0' && ch <= '9';
    }

}
