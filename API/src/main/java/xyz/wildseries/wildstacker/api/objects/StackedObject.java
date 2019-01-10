package xyz.wildseries.wildstacker.api.objects;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public interface StackedObject<T> {

    int getStackAmount();

    void setStackAmount(int stackAmount, boolean updateName);

    void remove();

    void updateName();

    T tryStack();

    boolean canStackInto(StackedObject stackedObject);

    boolean tryStackInto(StackedObject stackedObject);

    boolean tryUnstack(int amount);

    boolean isSimilar(StackedObject stackedObject);

}
