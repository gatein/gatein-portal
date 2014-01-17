package org.exoplatform.portal.webui.page;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:jpkroehling@redhat.com">Juraci Paixão Kröhling</a>
 */
public class UIPageActionListenerTest {

    private static final long SECOND = 1000; // 1000 milliseconds
    private static final long MINUTE = SECOND * 60;
    private static final long HOUR = MINUTE * 60;
    private static final long DAY = HOUR * 24;
    private static final long WEEK = DAY * 7;

    @Test
    public void testAllowWhenBadDataIsPresented() {
        long now = System.currentTimeMillis();
        long startPublishingDate = -1L;
        long endPublishingDate = -1L;
        boolean restrictOutsidePublicationWindow = true;
        assertFalse(UIPageActionListener.ChangeNodeActionListener.shouldRestrictAccess(restrictOutsidePublicationWindow, now, startPublishingDate, endPublishingDate));
    }

    @Test
    public void testAllowUnrestrictedPagesInsidePublishingWindow() {
        long now = System.currentTimeMillis();
        long startPublishingDate = now - DAY;
        long endPublishingDate = now + DAY;
        boolean restrictOutsidePublicationWindow = true;
        assertFalse(UIPageActionListener.ChangeNodeActionListener.shouldRestrictAccess(restrictOutsidePublicationWindow, now, startPublishingDate, endPublishingDate));
    }

    @Test
    public void testAllowRestrictedPagesInsidePublishingWindow() {
        long now = System.currentTimeMillis();
        long startPublishingDate = now - DAY;
        long endPublishingDate = now + DAY;
        boolean restrictOutsidePublicationWindow = false;
        assertFalse(UIPageActionListener.ChangeNodeActionListener.shouldRestrictAccess(restrictOutsidePublicationWindow, now, startPublishingDate, endPublishingDate));
    }

    @Test
    public void testBlockRestrictedPagesBeforeStartDate() {
        long now = System.currentTimeMillis();
        long startPublishingDate = now + DAY;
        long endPublishingDate = now + WEEK;
        boolean restrictOutsidePublicationWindow = true;
        assertTrue(UIPageActionListener.ChangeNodeActionListener.shouldRestrictAccess(restrictOutsidePublicationWindow, now, startPublishingDate, endPublishingDate));
    }

    @Test
    public void testAllowUnrestrictedPagesBeforeStartDate() {
        long now = System.currentTimeMillis();
        long startPublishingDate = now + DAY;
        long endPublishingDate = now + WEEK;
        boolean restrictOutsidePublicationWindow = false;
        assertFalse(UIPageActionListener.ChangeNodeActionListener.shouldRestrictAccess(restrictOutsidePublicationWindow, now, startPublishingDate, endPublishingDate));
    }

    @Test
    public void testBlockRestrictedPagesAfterEndDate() {
        long now = System.currentTimeMillis();
        long startPublishingDate = now - WEEK;
        long endPublishingDate = now - DAY;
        boolean restrictOutsidePublicationWindow = true;
        assertTrue(UIPageActionListener.ChangeNodeActionListener.shouldRestrictAccess(restrictOutsidePublicationWindow, now, startPublishingDate, endPublishingDate));
    }

    @Test
    public void testAllowUnrestrictedPagesAfterEndDate() {
        long now = System.currentTimeMillis();
        long startPublishingDate = now - WEEK;
        long endPublishingDate = now - DAY;
        boolean restrictOutsidePublicationWindow = false;
        assertFalse(UIPageActionListener.ChangeNodeActionListener.shouldRestrictAccess(restrictOutsidePublicationWindow, now, startPublishingDate, endPublishingDate));
    }
}
