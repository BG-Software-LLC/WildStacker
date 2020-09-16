package com.bgsoftware.wildstacker.utils.data;

public final class DataSerializer {

    private static final char COLOR_CHAR = '§';

    private DataSerializer() { }

    public static String serializeData(String str){
        str = ensureNotNull(str);

        if(str.isEmpty())
            return str;

        StringBuilder result = new StringBuilder();

        for(char ch : str.toCharArray()) {
//            if(isBetween(ch, 'a', 'f') || isBetween(ch, 'A', 'F') || isBetween(ch, '0', '9'))
//                result.append(COLOR_CHAR);
            result.append(COLOR_CHAR).append(ch);
        }

        // Marks end of data segment
        result.append(COLOR_CHAR).append("|").append(COLOR_CHAR).append("r");

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
            else if(curr != COLOR_CHAR) {
                result.append(curr);
            }
        }

        return hasEndTag ? result.toString() : "";
    }

    public static String stripData(String str){
        str = ensureNotNull(str);
        String[] sections = str.split(COLOR_CHAR + "\\|" + COLOR_CHAR + "r");
        return sections[sections.length - 1];
    }

    private static String ensureNotNull(String str){
        return str == null ? "" : str;
    }

    private static boolean isBetween(char ch, char min, char max){
        return ch >= min && ch <= max;
    }

}
