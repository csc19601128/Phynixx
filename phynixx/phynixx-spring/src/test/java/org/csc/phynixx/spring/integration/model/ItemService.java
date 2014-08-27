package org.csc.phynixx.spring.integration.model;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;


@Named
public class ItemService {

    @Inject
    ItemDataDao itemDAO;


    /**
     * use the current thread for Thread information
     *
     * @return
     */
    public void createItem(final String value) {

        final ItemData data = new ItemData();
        data.setValue(value);
        data.setThreadName(Thread.currentThread().getName());
        this.itemDAO.save(data);
    }

    public List<ItemData> findAllItems() {
        return this.itemDAO.findAllInItems();
    }


}
