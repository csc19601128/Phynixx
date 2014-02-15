package org.csc.phynixx.common.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by christoph on 09.02.14.
 */
public class UnorderedMany2ManyAssociationTest {
    @Test
    public void testAssociate() throws Exception {

        UnorderedMany2ManyAssociation<String, Long> assoc = new UnorderedMany2ManyAssociation<String, Long>();
        assoc.associate("A", 1l);
        Assert.assertEquals(1, assoc.getX("A").size());
        Assert.assertEquals(Long.valueOf(1l), assoc.getX("A").iterator().next());


        Assert.assertEquals(1, assoc.getY(1l).size());
        Assert.assertEquals("A", assoc.getY(1l).iterator().next());

    }

    @Test
    public void testAssociation1() throws Exception {
        UnorderedMany2ManyAssociation<String, Long> assoc = new UnorderedMany2ManyAssociation<String, Long>();
        assoc.associate("A", 1l);
        assoc.associate("A", 2l);
        Assert.assertEquals(2, assoc.getX("A").size());
        Assert.assertTrue(assoc.getX("A").contains(1l));
        Assert.assertTrue(assoc.getX("A").contains(2l));

        Assert.assertEquals(1, assoc.getY(1l).size());
        Assert.assertEquals("A", assoc.getY(1l).iterator().next());

        Assert.assertEquals(1, assoc.getY(2l).size());
        Assert.assertEquals("A", assoc.getY(2l).iterator().next());
    }

    @Test
    public void testAssociation2() throws Exception {
        UnorderedMany2ManyAssociation<String, Long> assoc = new UnorderedMany2ManyAssociation<String, Long>();
        assoc.associate("A", 1l);
        assoc.associate("B", 1l);
        Assert.assertEquals(2, assoc.getY(1l).size());
        Assert.assertTrue(assoc.getY(1l).contains("A"));
        Assert.assertTrue(assoc.getY(1l).contains("B"));

        Assert.assertEquals(1, assoc.getX("A").size());
        Assert.assertEquals(Long.valueOf(1l), assoc.getX("A").iterator().next());


        Assert.assertEquals(1, assoc.getX("B").size());
        Assert.assertEquals(Long.valueOf(1l), assoc.getX("B").iterator().next());
    }

    public void testMultipleAssoc() throws Exception {
        UnorderedMany2ManyAssociation<String, Long> assoc = new UnorderedMany2ManyAssociation<String, Long>();
        assoc.associate("A", 1l);
        assoc.associate("A", 1l);
        Assert.assertEquals(1, assoc.getX("A").size());
        Assert.assertEquals(Long.valueOf(1l), assoc.getX("A").iterator().next());
    }

    @Test
    public void testGetX() throws Exception {

    }

    @Test
    public void remove() {
        UnorderedMany2ManyAssociation<String, Long> assoc = new UnorderedMany2ManyAssociation<String, Long>();

        assoc.removeY(1l);
        Assert.assertEquals(0, assoc.getY(1l).size());

        assoc.removeX("A");
        Assert.assertEquals(0, assoc.getX("A").size());
    }

    @Test
    public void testRemoveX() throws Exception {

        UnorderedMany2ManyAssociation<String, Long> assoc = new UnorderedMany2ManyAssociation<String, Long>();
        assoc.associate("A", 1l);
        assoc.associate("B", 1l);
        assoc.associate("A", 2l);
        assoc.associate("B", 2l);

        assoc.removeX("A");

        Assert.assertEquals(0, assoc.getX("A").size());
        Assert.assertEquals(2, assoc.getX("B").size());
        Assert.assertEquals(1, assoc.getY(1l).size());
        Assert.assertEquals(1, assoc.getY(2l).size());

    }


    @Test
    public void testRemoveY() throws Exception {

        UnorderedMany2ManyAssociation<String, Long> assoc = new UnorderedMany2ManyAssociation<String, Long>();
        assoc.associate("A", 1l);
        assoc.associate("B", 1l);
        assoc.associate("A", 2l);
        assoc.associate("B", 2l);

        assoc.removeY(1l);

        Assert.assertEquals(1, assoc.getX("A").size());
        Assert.assertEquals(1, assoc.getX("B").size());
        Assert.assertEquals(0, assoc.getY(1l).size());
        Assert.assertEquals(2, assoc.getY(2l).size());

    }

    @Test
    public void testDisassociate() throws Exception {
        UnorderedMany2ManyAssociation<String, Long> assoc = new UnorderedMany2ManyAssociation<String, Long>();
        assoc.associate("A", 1l);
        assoc.associate("B", 1l);
        assoc.associate("A", 2l);
        assoc.associate("B", 2l);

        assoc.disassociate("A", 2l);

        Assert.assertEquals(1, assoc.getX("A").size());
        Assert.assertEquals(2, assoc.getX("B").size());
        Assert.assertEquals(2, assoc.getY(1l).size());
        Assert.assertEquals(1, assoc.getY(2l).size());

    }
}
