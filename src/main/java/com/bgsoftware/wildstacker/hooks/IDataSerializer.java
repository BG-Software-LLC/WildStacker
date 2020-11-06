package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;

public interface IDataSerializer {

    void saveEntity(StackedEntity stackedEntity);

    void loadEntity(StackedEntity stackedEntity);

    void saveItem(StackedItem stackedItem);

    void loadItem(StackedItem stackedItem);

}
