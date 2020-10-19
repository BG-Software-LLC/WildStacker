package com.bgsoftware.wildstacker.utils.data.structures;

import java.util.Arrays;
import java.util.List;

public final class Fast2EnumsArray<E extends Enum<E>, T extends Enum<T>> {

    private final FastEnumArray<E> firstKeyArray;
    private final FastEnumArray<T> secondKeyArray;
    private final byte[][] combinedKeyArray;

    private boolean containsAll = false;
    private int size = 0;

    public Fast2EnumsArray(Class<E> firstKeyType, Class<T> secondKeyType){
        int firstKeyTypeLength = FastEnumUtils.getEnumValues(firstKeyType).length;
        int secondKeyTypeLength = FastEnumUtils.getEnumValues(secondKeyType).length;

        this.firstKeyArray = new FastEnumArray<>(firstKeyTypeLength, firstKeyType);
        this.secondKeyArray = new FastEnumArray<>(secondKeyTypeLength, secondKeyType);

        this.combinedKeyArray = new byte[firstKeyTypeLength][0];
    }

    public static <E extends Enum<E>, T extends Enum<T>> Fast2EnumsArray<E, T> fromList(
            List<String> arr, Class<E> firstType, Class<T> secondType){
        Fast2EnumsArray<E, T> fast2EnumsArray = new Fast2EnumsArray<>(firstType, secondType);
        if(arr != null) {
            arr.forEach(line -> {
                if (line.equalsIgnoreCase("ALL")) {
                    fast2EnumsArray.containsAll = true;
                } else {
                    String[] sections = line.split(":");
                    E first = FastEnumUtils.getEnum(firstType, sections[0]);

                    if (sections.length == 2) {
                        T second = FastEnumUtils.getEnum(secondType, sections[1]);
                        if (first != null && second != null)
                            fast2EnumsArray.add(first, second);
                        else
                            fast2EnumsArray.size++;
                    } else if (first != null) {
                        fast2EnumsArray.addFirst(first);
                    } else {
                        T second = FastEnumUtils.getEnum(secondType, sections[0]);
                        if (second != null)
                            fast2EnumsArray.addSecond(second);
                        else
                            fast2EnumsArray.size++;
                    }
                }
            });
        }
        return fast2EnumsArray;
    }

    public boolean addFirst(E e) {
        boolean containedBefore = firstKeyArray.add(e);
        if(!containedBefore)
            size++;
        return containedBefore;
    }

    public boolean addSecond(T t) {
        boolean containedBefore = secondKeyArray.add(t);
        if(!containedBefore)
            size++;
        return containedBefore;
    }

    public boolean add(E e, T t) {
        byte[] combinedKeyArray = this.combinedKeyArray[e.ordinal()];
        boolean containedBefore = contains(e, t);
        if(!containedBefore) {
            this.combinedKeyArray[e.ordinal()] = combinedKeyArray = Arrays.copyOf(combinedKeyArray, combinedKeyArray.length + 1);
            combinedKeyArray[combinedKeyArray.length - 1] = (byte) t.ordinal();
            size++;
        }
        return containedBefore;
    }

    public boolean contains(T t){
        return containsAll || secondKeyArray.contains(t);
    }

    public boolean contains(E e, T t){
        if(containsAll || firstKeyArray.contains(e) || contains(t))
            return true;

        byte[] combinedKeyArray = this.combinedKeyArray[e.ordinal()];

        for (byte b : combinedKeyArray) {
            if (b == t.ordinal())
                return true;
        }

        return false;
    }

    public byte[][] combinedKeyArray(){
        return combinedKeyArray;
    }

    public int size() {
        return size;
    }

}
