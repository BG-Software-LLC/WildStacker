package com.bgsoftware.wildstacker.utils.data.structures;

import sun.misc.SharedSecrets;

import java.util.Arrays;
import java.util.List;

public final class Fast2EnumsArray<E extends Enum<E>, T extends Enum<T>> {

    private final FastEnumArray<E> firstKeyArray;
    private final FastEnumArray<T> secondKeyArray;
    private final byte[] combinedKeyArray;

    private boolean containsAll = false;
    private int size = 0;

    public Fast2EnumsArray(Class<E> firstKeyType, Class<T> secondKeyType){
        int firstKeyTypeLength = SharedSecrets.getJavaLangAccess().getEnumConstantsShared(firstKeyType).length;
        int secondKeyTypeLength = SharedSecrets.getJavaLangAccess().getEnumConstantsShared(secondKeyType).length;

        this.firstKeyArray = new FastEnumArray<>(firstKeyTypeLength, firstKeyType);
        this.secondKeyArray = new FastEnumArray<>(secondKeyTypeLength, secondKeyType);

        this.combinedKeyArray = new byte[firstKeyTypeLength];
        Arrays.fill(combinedKeyArray, (byte) -1);
    }

    public static <E extends Enum<E>, T extends Enum<T>> Fast2EnumsArray<E, T> fromList(
            List<String> arr, Class<E> firstType, Class<T> secondType){
        Fast2EnumsArray<E, T> fast2EnumsArray = new Fast2EnumsArray<>(firstType, secondType);
        arr.forEach(line -> {
            if(line.equalsIgnoreCase("ALL")){
                fast2EnumsArray.containsAll = true;
            }
            else {
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
        boolean containedBefore = combinedKeyArray[e.ordinal()] != -1;

        if(!containedBefore) {
            combinedKeyArray[e.ordinal()] = (byte) t.ordinal();
            size++;
        }

        return containedBefore;
    }

    public boolean contains(T t){
        return containsAll || secondKeyArray.contains(t);
    }

    public boolean contains(E e, T t){
        return containsAll || firstKeyArray.contains(e) || contains(t) || combinedKeyArray[e.ordinal()] == t.ordinal();
    }

    public int size() {
        return size;
    }

}
