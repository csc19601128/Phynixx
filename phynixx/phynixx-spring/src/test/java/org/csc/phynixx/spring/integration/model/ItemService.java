package org.csc.phynixx.spring.integration.model;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void createItem(final String value) {

        final ItemData data = new ItemData();
        data.setValue(value);
        data.setThreadName(Thread.currentThread().getName());
        this.itemDAO.save(data);
    }


    @Transactional
    public List<ItemData> findAllItems() {
        return this.itemDAO.findAllInItems();
    }


}
