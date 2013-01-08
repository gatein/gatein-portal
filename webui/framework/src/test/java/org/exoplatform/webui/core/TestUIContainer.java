package org.exoplatform.webui.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class TestUIContainer extends TestCase {
    public void testHasChildren() {
        UIContainer container = new UIContainer();
        assertNotNull(container.getChildren());
        assertFalse(container.hasChildren());

        container = createUIContainer(5);
        assertNotNull(container.getChildren());
        assertTrue(container.hasChildren());
    }

    public void testConcurrentModification() {
        UIContainer container = createUIContainer(5);

        List<String> list = new ArrayList<String>();
        for (UIComponent c : container.getChildren()) {
            list.add(c.getId());
            if (c.getId().equals("c2")) {
                assertNotNull(container.removeChildById("c4"));
            }
        }
        assertEquals(5, list.size());
        assertTrue(list.contains("c4"));

        String[] remain = { "c1", "c2", "c3", "c5" };
        assertChildren(remain, container);

        // Initialize a children list then set it to the container from outside
        List<UIComponent> children = new ArrayList<UIComponent>();
        children.add(new MockUIComponent("c1"));
        children.add(new MockUIComponent("c2"));
        children.add(new MockUIComponent("c3"));
        container = new UIContainer();
        container.setChildren(children);

        list.clear();
        for (UIComponent c : container.getChildren()) {
            list.add(c.getId());
            if (c.getId().equals("c2")) {
                container.addChild(new MockUIComponent("c_foo"));
            }
        }
        assertEquals(3, list.size());
        assertFalse(list.contains("c_foo"));

        String[] remain1 = { "c1", "c2", "c3", "c_foo" };
        assertChildren(remain1, container);
    }

    public void testMultiThreading() throws InterruptedException {
        int number = 10;
        final UIContainer container = createUIContainer(number);
        assertEquals(number, container.getChildren().size());

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                container.addChild(new MockUIComponent("c_foo"));
            }
        });

        Iterator<UIComponent> iterator = container.getChildren().iterator();
        List<String> list = new ArrayList<String>();
        boolean active = true;
        while (iterator.hasNext()) {
            if (active) {
                t.start();
                active = false;
            }
            // Just loop through the list to do something
            list.add(iterator.next().getId());
            Thread.sleep(5);
        }
        t.join();
        assertEquals(number, list.size());
        assertFalse(list.contains("c_foo"));

        assertEquals(number + 1, container.getChildren().size());
        assertNotNull(container.getChildById("c_foo"));
    }

    private void assertChildren(String[] ids, UIContainer container) {
        assertEquals(ids.length, container.getChildren().size());
        for (String id : ids) {
            assertNotNull(container.getChildById(id));
        }
    }

    private UIContainer createUIContainer(int number) {
        UIContainer container = new UIContainer();
        for (int i = 1; i <= number; i++) {
            container.addChild(new MockUIComponent("c" + i));
        }
        return container;
    }
}
