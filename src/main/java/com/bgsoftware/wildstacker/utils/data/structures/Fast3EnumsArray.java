package com.bgsoftware.wildstacker.utils.data.structures;

import java.util.List;

public final class Fast3EnumsArray<E extends Enum<E>, T extends Enum<T>, S extends Enum<S>> {

    private final Fast2EnumsArray<E, T> firstKeyArray;
    private final Fast2EnumsArray<E, S> secondKeyArray;

    private boolean containsAll = false;
    private int size = 0;

    public Fast3EnumsArray(Class<E> firstKeyType, Class<T> secondKeyType, Class<S> thirdKeyType){
        this.firstKeyArray = new Fast2EnumsArray<>(firstKeyType, secondKeyType);
        this.secondKeyArray = new Fast2EnumsArray<>(firstKeyType, thirdKeyType);
    }

    public static <E extends Enum<E>, T extends Enum<T>, S extends Enum<S>> Fast3EnumsArray<E, T, S> fromList(
            List<String> arr, Class<E> firstType, Class<T> secondType, Class<S> thirdType){
        Fast3EnumsArray<E, T, S> fast2EnumsArray = new Fast3EnumsArray<>(firstType, secondType, thirdType);
        if(arr != null) {
            arr.forEach(line -> {
                if (line.equalsIgnoreCase("ALL")) {
                    fast2EnumsArray.containsAll = true;
                    fast2EnumsArray.size++;
                } else {
                    String[] sections = line.split(":");
                    E first = FastEnumUtils.getEnum(firstType, sections[0]);

                    if (sections.length == 2) {
                        if (first != null) {
                            T second = FastEnumUtils.getEnum(secondType, sections[1]);
                            boolean added = false;
                            if (second != null) {
                                fast2EnumsArray.addFirst(first, second);
                                added = true;
                            }
                            S third = FastEnumUtils.getEnum(thirdType, sections[1]);
                            if (third != null) {
                                fast2EnumsArray.addSecond(first, third);
                                added = true;
                            }
                            if(!added)
                                fast2EnumsArray.size++;
                        }
                    } else if (first != null) {
                        fast2EnumsArray.addFirst(first);
                    } else {
                        T second = FastEnumUtils.getEnum(secondType, sections[0]);
                        boolean added = false;
                        if (second != null) {
                            fast2EnumsArray.addSecond(second);
                            added = true;
                        }
                        S third = FastEnumUtils.getEnum(thirdType, sections[0]);
                        if (third != null) {
                            fast2EnumsArray.addThird(third);
                            added = true;
                        }
                        if(!added)
                            fast2EnumsArray.size++;
                    }
                }
            });
        }
        return fast2EnumsArray;
    }

    public void addFirst(E e) {
        boolean containedBefore = firstKeyArray.addFirst(e);
        if(!containedBefore)
            size++;
    }

    public void addSecond(T t) {
        boolean containedBefore = firstKeyArray.addSecond(t);
        if(!containedBefore)
            size++;
    }

    public void addThird(S s) {
        boolean containedBefore = secondKeyArray.addSecond(s);
        if(!containedBefore)
            size++;
    }

    public boolean addFirst(E e, T t) {
        boolean containedBefore = firstKeyArray.add(e, t);

        if(!containedBefore)
            size++;

        return containedBefore;
    }

    public boolean addSecond(E e, S s) {
        boolean containedBefore = secondKeyArray.add(e, s);

        if(!containedBefore)
            size++;

        return containedBefore;
    }

    public boolean containsFirst(E e, T t){
        return containsAll || firstKeyArray.contains(e, t);
    }


    public boolean containsSecond(E e, S s){
        return containsAll || secondKeyArray.contains(e, s);
    }

    public int size() {
        return size;
    }

}
