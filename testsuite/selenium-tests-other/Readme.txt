
An alternative selenium testsuite.



== Features ==

- API for simple programmatic test composition
- includes ready made per-test ant build - allowing individual tests to prepare and deploy any server-side dependency they might need but are unavailable
- no support for 'macro' .html files
- more control at every step of execution allowing for a more robust while still efficient tests



== Usage ==

To run against an already running GateIn instance use:

  mvn integration-test -Pselenium



To start an already built GateIn JBoss (avaialbe in packaging/pkg/target/jboss) before starting tests use:

  mvn integration-test -Pselenium -DcontainerId=jboss5x



== Using per-test ant builds ==

This functionality is very useful for integration testing where individual tests need to pull in custom dependencies for deployment to an already running GateIn instance for example.

When a test class extends AbstractSingleTestWithAnt it inherits functionality that allows for test-specific build.xml file discovery and execution. Discovery is based on a naming convention - it looks for a file called TEST_CLASS_NAME-build.xml (where TEST_CLASS_NAME is the name of the test class), and it expects to find it in test class' package.

A test making use of this functionality can easily trigger a complete discovery and execution cycle by calling one single method.

(For an example see Test_GTNPORTAL_1257_SeamSessionOutlivesTheGateInSession.java which deploys JBoss Portlet Bridge's Seam booking example)



== Known issues ==

Using maven-ant-tasks-2.1.1 from ant builds doesn't work with Maven 3.




