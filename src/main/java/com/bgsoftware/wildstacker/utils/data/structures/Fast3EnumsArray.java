package com.bgsoftware.wildstacker.utils.data.structures;

import sun.misc.SharedSecrets;

import java.util.Arrays;
import java.util.List;

public final class Fast3EnumsArray<E extends Enum<E>, T extends Enum<T>, S extends Enum<S>> {

    private final FastEnumArray<E> firstKeyArray;
    private final FastEnumArray<T> secondKeyArray;
    private final FastEnumArray<S> thirdKeyArray;
    private final CombinedValue[] combinedKeyArray;

    private boolean containsAll = false;
    private int size = 0;

    public Fast3EnumsArray(Class<E> firstKeyType, Class<T> secondKeyType, Class<S> thirdKeyType){
        int firstKeyTypeLength = SharedSecrets.getJavaLangAccess().getEnumConstantsShared(firstKeyType).length;
        int secondKeyTypeLength = SharedSecrets.getJavaLangAccess().getEnumConstantsShared(secondKeyType).length;
        int thirdKeyTypeLength = SharedSecrets.getJavaLangAccess().getEnumConstantsShared(thirdKeyType).length;

        this.firstKeyArray = new FastEnumArray<>(firstKeyTypeLength, firstKeyType);
        this.secondKeyArray = new FastEnumArray<>(secondKeyTypeLength, secondKeyType);
        this.thirdKeyArray = new FastEnumArray<>(thirdKeyTypeLength, thirdKeyType);

        this.combinedKeyArray = new CombinedValue[firstKeyTypeLength];
        Arrays.fill(combinedKeyArray, new CombinedValue());
    }

    public static <E extends Enum<E>, T extends Enum<T>, S extends Enum<S>> Fast3EnumsArray<E, T, S> fromList(
            List<String> arr, Class<E> firstType, Class<T> secondType, Class<S> thirdType){
        Fast3EnumsArray<E, T, S> fast2EnumsArray = new Fast3EnumsArray<>(firstType, secondType, thirdType);
        arr.forEach(line -> {
            if (line.equalsIgnoreCase("ALL")) {
                fast2EnumsArray.containsAll = true;
                fast2EnumsArray.size++;
            }
            else {
                String[] sections = line.split(":");
                E first = FastEnumUtils.getEnum(firstType, sections[0]);

                if (sections.length == 2) {
                    if (first != null) {
                        T second = FastEnumUtils.getEnum(secondType, sections[1]);
                        if (second != null) {
                            fast2EnumsArray.addFirst(first, second);
                        } else {
                            S third = FastEnumUtils.getEnum(thirdType, sections[1]);
                            if (third != null)
                                fast2EnumsArray.addSecond(first, third);
                            else
                                fast2EnumsArray.size++;
                        }
                    }
                } else if (first != null) {
                    fast2EnumsArray.addFirst(first);
                } else {
                    T second = FastEnumUtils.getEnum(secondType, sections[0]);
                    if (second != null) {
                        fast2EnumsArray.addSecond(second);
                    } else {
                        S third = FastEnumUtils.getEnum(thirdType, sections[0]);
                        if (third != null)
                            fast2EnumsArray.addThird(third);
                        else
                            fast2EnumsArray.size++;
                    }
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

    public boolean addThird(S s) {
        boolean containedBefore = thirdKeyArray.add(s);
        if(!containedBefore)
            size++;
        return containedBefore;
    }

    public boolean addFirst(E e, T t) {
        boolean containedBefore = combinedKeyArray[e.ordinal()].first != -1;

        if(!containedBefore) {
            combinedKeyArray[e.ordinal()].first = (byte) t.ordinal();
            size++;
        }

        return containedBefore;
    }

    public boolean addSecond(E e, S s) {
        boolean containedBefore = combinedKeyArray[e.ordinal()].second != -1;

        if(!containedBefore) {
            combinedKeyArray[e.ordinal()].second = (byte) s.ordinal();
            size++;
        }

        return containedBefore;
    }

    public boolean containsFirst(E e, T t){
        return containsAll || firstKeyArray.contains(e) || secondKeyArray.contains(t) || combinedKeyArray[e.ordinal()].first == t.ordinal();
    }


    public boolean containsSecond(E e, S s){
        return containsAll || firstKeyArray.contains(e) || thirdKeyArray.contains(s) || combinedKeyArray[e.ordinal()].second == s.ordinal();
    }

    public int size() {
        return size;
    }

    private static final class CombinedValue{

        private byte first = -1, second = -1;

    }

}
