package org.csc.phynixx.spring.integration.model;

import org.csc.phynixx.spring.integration.model.ItemData;
import org.csc.phynixx.spring.integration.model.ItemService;
import org.junit.Assert;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Created by zf4iks2 on 26.08.14.
 */
@Named
public class TestScenario {

    @Inject
    ItemService itemService;


    @Transactional
    public void writeAndRead() {
        this.itemService.createItem("Test");

        final List<ItemData> items = this.itemService.findAllItems();
        Assert.assertEquals(1, items.size());
    }


    @Transactional
    public List<ItemData> findItems() {
        return this.itemService.findAllItems();
    }


    /**
     * creates one Items an throw an Exception
     * throw Exception to provoke rollback
     */
    @Transactional
    public void  throwException() {

        this.itemService.createItem("Test");
        throw new IllegalStateException("Do Rollback");
    }
}
