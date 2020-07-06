package com.bgsoftware.wildstacker.utils.pair;

public final class MultiPair<X, Y, Z> {

    private final X x;
    private final Y y;
    private final Z z;

    public MultiPair(X x, Y y, Z z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public X getX() {
        return x;
    }

    public Y getY() {
        return y;
    }

    public Z getZ() {
        return z;
    }

    @Override
    public String toString() {
        return "MultiPair{" + x + ", " + y + ", " + z + '}';
    }

}
