package org.gatein.api.composition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.gatein.api.application.Application;
import org.gatein.api.application.ApplicationImpl;
import org.gatein.api.composition.BareContainer;
import org.gatein.api.composition.Container;
import org.gatein.api.composition.ContainerBuilder;
import org.gatein.api.composition.ContainerBuilderImpl;
import org.gatein.api.composition.PageBuilder;
import org.gatein.api.composition.PageBuilderImpl;
import org.gatein.api.page.Page;
import org.gatein.api.security.Permission;
import org.junit.Test;

/**
 * Set of tests that shows what's the expected data structure after some usage scenarios.
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class PageBuilderImplTest {

    /**
     * The simplest scenario ever: shows what are the required information to compose a page, as well as adds
     * a single application (which is actually not required, but an empty page isn't that useful, is it?)
     */
    @Test
    public void testSimplestScenario() {
        PageBuilder pageBuilder = new PageBuilderImpl();
        Application gadgetCalculator = new ApplicationImpl("gadgetCalculator");
            Page page = pageBuilder
                    .child(gadgetCalculator)
                    .siteName("classic") // to make it even simpler, we could assume that "classic" is the default, no?
                    .siteType("portal") // same here: should "portal" be the default? or better explicit than implicit?
                    .name("awesome")
                    .build(); // finishes the page
        assertEquals("should have 1 application", 1, page.getChildren().size());
        Application application = (Application) page.getChildren().get(0);
        assertEquals("application should be gadgetCalculator", "gadgetCalculator", application.getApplicationName());
    }

    /**
     * Same as the simple scenario, but with two rows
     */
    @Test
    public void testSimplestScenarioWithTwoChildren() {
        PageBuilder pageBuilder = new PageBuilderImpl();
        Application gadgetCalculator = new ApplicationImpl("gadgetCalculator");
        Application gadgetRss = new ApplicationImpl("gadgetRss");
            Page page = pageBuilder
                    .child(gadgetCalculator)
                    .child(gadgetRss)
                    .siteName("classic") // to make it even simpler, we could assume that "classic" is the default, no?
                    .siteType("portal") // same here: should "portal" be the default? or better explicit than implicit?
                    .name("awesome")
                    .build(); // finishes the page
        assertEquals("should have 2 child container", 2, page.getChildren().size());
        Application firstApplication = (Application) page.getChildren().get(0);
        Application secondApplication = (Application) page.getChildren().get(1);
        assertEquals("first application should gadgetCalculator", "gadgetCalculator", firstApplication.getApplicationName());
        assertEquals("second application should be gadgetRss", "gadgetRss", secondApplication.getApplicationName());
    }

    /**
     * Demonstrates a scenario where a layout has a single row, with a single application. The end result is equivalent
     * to the testSimplestScenario, but you can use it in debug mode to see how the data structure looks like.
     */
    @Test
    public void testRowApplicationScenario() {
        PageBuilder pageBuilder = new PageBuilderImpl();
        Application gadgetCalculator = new ApplicationImpl("gadgetCalculator");
        Page page = pageBuilder

                        .newRowsBuilder() // new single row on the page
                            .child(gadgetCalculator) // application on the row
                        .buildToTopBuilder() // finishes the layout
                        .siteName("classic")
                        .siteType("portal")
                        .name("awesome")
                    .build(); // finishes the page

        assertEquals("page should have 1 child", 1, page.getChildren().size());
        assertTrue("page's first child should be a Container", page.getChildren().get(0) instanceof Container);
        Container rows = (Container) page.getChildren().get(0);
        assertEquals("rows container should have 1 child", 1, rows.getChildren().size());
        assertTrue("rows container's first child should be an Application", rows.getChildren().get(0) instanceof Application);
        Application application = (Application) rows.getChildren().get(0);
        assertEquals("application should be named", "gadgetCalculator", application.getApplicationName());
    }


    /**
     * buildToTopBuilder() calls buildToParentBuilder() internally.
     */
    @Test
    public void testAutoClose() {
        PageBuilder pageBuilder = new PageBuilderImpl();
        Application app1 = new ApplicationImpl("app1");
        Application app2 = new ApplicationImpl("app2");
        Application app3 = new ApplicationImpl("app3");
        Page page = pageBuilder
                        .child(app1)
                        .newColumnsBuilder()
                            .child(app2)
                            .newRowsBuilder()
                                .child(app3)
                        .buildToTopBuilder() // closes rows and columns in proper order
                        .siteName("classic")
                        .siteType("portal")
                        .name("awesome")
                    .build(); // finishes the page

        assertEquals("page should have 2 children", 2, page.getChildren().size());
        Application foundApp1 = (Application) page.getChildren().get(0);
        assertEquals("app1 name expected", "app1", foundApp1.getApplicationName());
        assertTrue("page's second child should be a Container", page.getChildren().get(1) instanceof Container);
        Container columns = (Container) page.getChildren().get(1);
        assertEquals("columns container should have 2 children", 2, columns.getChildren().size());
        Application foundApp2 = (Application) columns.getChildren().get(0);
        assertEquals("app2 name expected", "app2", foundApp2.getApplicationName());

        assertTrue("columns container's second child should be a Container", columns.getChildren().get(1) instanceof Container);
        Container rows = (Container) columns.getChildren().get(1);
        assertEquals("rows container should have 1 child", 1, rows.getChildren().size());
        Application foundApp3 = (Application) rows.getChildren().get(0);
        assertEquals("app3 name expected", "app3", foundApp3.getApplicationName());
    }

    /**
     * A more complex scenario, which probably won't be the common use case, but demonstrates that the API itself
     * is ready to handle such cases.
     */
    @Test
    public void testComplexReferenceScenario() {
        PageBuilder pageBuilder = new PageBuilderImpl();
        Application gadgetCalculator = new ApplicationImpl("gadgetCalculator");
        Application gadgetRss = new ApplicationImpl("gadgetRss");
        Application wsrpCompanyNews = new ApplicationImpl("wsrpCompanyNews");
        Application portletUsefulLinks = new ApplicationImpl("portletUsefulLinks");

        Permission accessPermission = Permission.everyone();

        Permission moveAppsPermissions = Permission.everyone();

        Permission moveContainersPermissions = Permission.everyone();

        Page page = pageBuilder
                .newColumnsBuilder() // top-level column row, with 3 cells
                    .child(gadgetRss) // an application in the first column, about 33% of the total width of the screen
                    .newColumnsBuilder() // a new column set inside the second column, 33% of the width of the screen
                        .child(gadgetCalculator) // 50% of the second column, ie, ~16% of the screen size
                        .child(wsrpCompanyNews) // same as above
                    .buildToParentBuilder() // finishes the second column

                    .newColumnsBuilder() // third column, the remaining 33%
                        .child(portletUsefulLinks) // about 1/4 of the remaining 33%
                        .child(portletUsefulLinks) // same as above
                        .child(gadgetCalculator) // same as above
                        .child(gadgetRss) // same as above
                    .buildToParentBuilder() // finishes the third column

                .buildToTopBuilder() // finishes the layout
                .siteName("classic")
                .siteType("portal")
                .name("awesome")
                .displayName("Awesome page")
                .showMaxWindow(false)
                .accessPermission(accessPermission)
                .editPermission(Permission.everyone())
                .moveAppsPermission(moveAppsPermissions)
                .moveContainersPermission(moveContainersPermissions)
            .build(); // finishes the page

        assertEquals("page should have 1 child", 1, page.getChildren().size());
        assertTrue("page's first child should be a Container", page.getChildren().get(0) instanceof Container);
        Container columns = (Container) page.getChildren().get(0);

        assertEquals("Columns container should have 3 children containers", 3, columns.getChildren().size());
        Application firstRowApp = (Application) columns.getChildren().get(0);
        Container secondRow = (Container) columns.getChildren().get(1);
        Container thirdRow = (Container) columns.getChildren().get(2);

        assertEquals("first row app should be gadgetRss", "gadgetRss", firstRowApp.getApplicationName());

        assertEquals("second row should have 2 child container", 2, secondRow.getChildren().size());
        Application secondRowFirstApp = (Application) secondRow.getChildren().get(0);
        Application secondRowSecondApp = (Application) secondRow.getChildren().get(1);
        assertEquals("second row first app should be gadgetCalculator", "gadgetCalculator", secondRowFirstApp.getApplicationName());
        assertEquals("second row second app should be wsrpCompanyNews", "wsrpCompanyNews", secondRowSecondApp.getApplicationName());

        assertEquals("third row should have 4 child container", 4, thirdRow.getChildren().size());
        Application thirdRowFirstApp = (Application) thirdRow.getChildren().get(0);
        Application thirdRowSecondApp = (Application) thirdRow.getChildren().get(1);
        Application thirdRowThirdApp = (Application) thirdRow.getChildren().get(2);
        Application thirdRowFourthApp = (Application) thirdRow.getChildren().get(3);
        assertEquals("third row first app should be portletUsefulLinks", "portletUsefulLinks", thirdRowFirstApp.getApplicationName());
        assertEquals("third row second app should be portletUsefulLinks", "portletUsefulLinks", thirdRowSecondApp.getApplicationName());
        assertEquals("third row third app should be gadgetCalculator", "gadgetCalculator", thirdRowThirdApp.getApplicationName());
        assertEquals("third row fourth app should be gadgetRss", "gadgetRss", thirdRowFourthApp.getApplicationName());

    }

    @Test
    public void testPageDefaultPermissions() {
        PageBuilder pageBuilder = new PageBuilderImpl();
        Page page = pageBuilder
                    .siteName("classic")
                    .siteType("portal")
                    .name("awesome")
                    .build();
        assertEquals("Should have default access permissions defined in API", BareContainer.DEFAULT_ACCESS_PERMISSION, page.getAccessPermission());
        assertEquals("Should have default edit permissions defined in API", Page.DEFAULT_EDIT_PERMISSION, page.getEditPermission());
        assertEquals("Should have default moveAppsPermissions defined in API", BareContainer.DEFAULT_MOVE_APPS_PERMISSION, page.getMoveAppsPermission());
        assertEquals("Should have default moveContainersPermissions defined in API", BareContainer.DEFAULT_MOVE_CONTAINERS_PERMISSION, page.getMoveContainersPermission());
    }

    @Test
    public void testPageCustomPermissions() {
        PageBuilder pageBuilder = new PageBuilderImpl();
        Permission access = Permission.any("access");
        Permission edit = Permission.any("edit");
        Permission moveApps = Permission.any("moveApps");
        Permission moveContainers = Permission.any("moveContainers");
        Page page = pageBuilder
                    .accessPermission(access)
                    .editPermission(edit)
                    .moveAppsPermission(moveApps)
                    .moveContainersPermission(moveContainers)
                    .siteName("classic")
                    .siteType("portal")
                    .name("awesome")
                    .build();
        assertEquals("Should have custom access permissions defined in API", access, page.getAccessPermission());
        assertEquals("Should have custom edit permissions defined in API", edit, page.getEditPermission());
        assertEquals("Should have custom moveAppsPermissions defined in API", moveApps, page.getMoveAppsPermission());
        assertEquals("Should have custom moveContainersPermissions defined in API", moveContainers, page.getMoveContainersPermission());
    }


    @Test
    public void testContainerDefaultPermissions() {
        ContainerBuilder<PageBuilder> containerBuilder = new ContainerBuilderImpl<PageBuilder>(null);
        Container container = containerBuilder
                    .build();
        assertEquals("Should have default access permissions defined in API", BareContainer.DEFAULT_ACCESS_PERMISSION, container.getAccessPermission());
        assertEquals("Should have default moveAppsPermissions defined in API", BareContainer.DEFAULT_MOVE_APPS_PERMISSION, container.getMoveAppsPermission());
        assertEquals("Should have default moveContainersPermissions defined in API", BareContainer.DEFAULT_MOVE_CONTAINERS_PERMISSION, container.getMoveContainersPermission());
    }

    @Test
    public void testContainerCustomPermissions() {
        ContainerBuilder<PageBuilder> containerBuilder = new ContainerBuilderImpl<PageBuilder>(null);
        Permission access = Permission.any("access");
        Permission moveApps = Permission.any("moveApps");
        Permission moveContainers = Permission.any("moveContainers");
        Container container = containerBuilder
                    .accessPermission(access)
                    .moveAppsPermission(moveApps)
                    .moveContainersPermission(moveContainers)
                    .build();
        assertEquals("Should have custom access permissions defined in API", access, container.getAccessPermission());
        assertEquals("Should have custom moveAppsPermissions defined in API", moveApps, container.getMoveAppsPermission());
        assertEquals("Should have custom moveContainersPermissions defined in API", moveContainers, container.getMoveContainersPermission());
    }

}
