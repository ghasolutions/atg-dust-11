            Apache License
       Version 2.0, January 2004
     http://www.apache.org/licenses/



================================================
================================================
ATG Dust for ATG 11.1.0


This is my rework of ATG Dust to bring it up to date with ATG 11.1.0.  I have also made some convenience modifications to help my efforts in setting up tests.

My thanks to the original team that brought ATG Dust to the community.  My most common need for ATG Dust has been testing repository manipulation, a task common on ATG projects.  ATG Dust is perfect for mocking large amounts of repository data or complex repository structures.

The original open source ATG Dust project can be found at http://atgdust.sourceforge.net


=================================================
=================================================
Usage

This project is build using maven and serves to generate the artifacts (jar file) required to include in your project either as another maven dependency or standalone jar in case you are using ATG's ANT structure.

The project does depend upon ATG classes being accessible for maven compilation.  See the ${atg.path} property in the pom file.

To build the project:


mvn clean install


This project includes a few sample tests to help you get started.  For more expansive tests you should refer to the original ATG Dust project.



=================================================
=================================================
Log4J

Its easy to forget but its very nice to run tests within Eclipse and have output logged in the eclipse console.  To do this just make sure you have a log4j.xml added to your /resources directory.  There are a few in the source of the DUST framework you can seek out and re-use.  

See src/test/resources/log4j.xml



=================================================
=================================================
Testing with Repositories



What are the database options?

Sometimes you need to write a series of unit tests that interact with a repository or bunch of repository items.  In cases like these you can find mocks to be time consuming an impractical.  In that case you can leverage ATG Dust to quickly build a repository full of items.  These aren't mocks but real objects to write your tests against.  Since they are repository items you need a database to back them.

ATG Dust supports three modes of database connectivity.  These modes are:


   * A test running against your development MySql repository
   

   * A test within the context of the Maven test phase against an Apache Derby database.  Derby uses the filesystem to store the database data.  If its not cleaned up (a Maven clean step is recommended) then your next test runs may have issues as they will interact with data put in from the last test run.
   

   * A test against an in-memory HyperSQL Database which is destroyed when the test phase complete
   

The best option for the lightweight testing I do is the HyperSQL Database.  Its fast and has shown no problems during test runs.  ATG must have thought the same because the default ATG Dust configuration layer replaces FakeXADataSource with HSQLDBDataSource.  The HSQLDBDataSource is a self-initializing datasource.

ATG Dust provides a nice repository extension called the atg.adapter.gsa.InitializingGSA.  This repository class allows your test framework to initialize the target database with data prior to repository startup.  Upon repository shutdown the design of this class allows for the database tables to be destroyed.  

I note the last feature does not seem to function with the Derby database setup.  When Nucleus begins its shutdown procedure it invokes the doStopService method of the repository which is supposed to connect to the database and drop all the tables corresponding to that repository.  In my tests with Derby, when Nucleus begins to shutdown the connection pool can no longer be obtained and errors ensue.  I hate errors.

A reasonable (and working) approach to testing a Derby-based repository is to use your test framework 'tearDown' method to invoke your test repository's dropTables() method. 

But then you're using HSQLDB and not Derby right?...


=================================================
Using a 'Mock' Repository

Lets say you need to test some code that manipulates repository items.  Using Mockito it would likely require a significant amount of setup code just to mock the repository items you want to test against.  It would be far more convenient to be able to define a definition file of items and load those into a test repository.  

There are two ways to accomplish this using ATG Dust.  The first mode is one where you test against a single Repository definition file.  This would happen in the case where your project defines a new repository.  This could also happen as a decision during test writing where you decide to create a sample repository from scratch that matches specific item definitions.

For this mode you can use GSATestUtils to initialize a repository against a single configuration that you provide

See com.mycompany.BasicRepositoryTest for an example


The second method involves leveraging several config layers to extend an existing repository definition and write tests against the extensions.  This is described below.


=================================================
Testing extensions to an ATG Repository

Its pretty common to extend existing ATG repositories.  Lets say you extend the profile repository and wanted to write tests against your extensions.  You can use NucleusTestUtils in combination with GSATestUtils to include the installed ATG module configuration with your own project configuration.

See com.mycompany.ExtendedRepositoryTest for an example





=================================================
=================================================
Nucleus components in Unit Tests



=================================================
Getting Nucleus to Start Components

This is pretty easy with Dust.  You can simply use one of the following methods and pass your component name.  Nucleus will start your component up as the initial service.  It will also start any dependent components.

   // Start Nucleus against a config path

   mNucleus = NucleusTestUtils.startNucleus(configpath.getAbsolutePath());


   // Start Nucleus against existing module configurations and any project configs plus test specific configs

   mNucleus = NucleusTestUtils.startNucleusWithModules(new String[] { "DAS","DPS"},
                                                       projectConfigPath,
                                                       MyComponentTest.class,
                                                       "/some/atg/Component");




=================================================
=================================================
Test Configuration Naming Conventions

Rant warning...

When writing tests for ATG you will inevitably need to define configuration files (*.properties) to write your tests against.  I tend to prefer atomic testing and the thought of sharing a single configuration layer for all the tests frustrates me.  I would much prefer and encourage each test to provide its own discreet set of configuration files.  This may not always be practical in which case I would encourage package level configuration.

When defining test cases it would be incredibly helpful to stick with a convention.  Otherwise you will end up with the confusing configuration you see within the original ATG Dust examples.  Consider that only a few tests resulted in this file structure:

./resources/atg/adapter/gsa/rep1.xml
./resources/atg/adapter/gsa/rep2.xml
./resources/config/.DS_Store
./resources/config/GettingStarted/songs-data.xml
./resources/config/GettingStarted/songs.xml
./resources/config/GettingStarted/SongsRepository.properties
./resources/config/test/SimpleComponentGlobalScope.properties
./resources/config/test/SimpleComponentRequestScope.properties
./resources/config/test/SimpleDroplet.properties
./resources/config/test/simpleRepository.xml
./resources/env.properties
./resources/log4j.xml
./resources/SimpleFormHandlerTest/config/atg/test/SimpleFormHandler.properties
./resources/sql/dynamusic.sql
./resources/test/data/NucleusResolutionTest/config/GlobalComponent.properties
./resources/test/data/NucleusResolutionTest/config/RequestComponent.properties
./resources/test/data/NucleusResolutionTest/config/SessionComponent.properties
./resources/test/data/NucleusResolutionTest/config/WindowComponent.properties
./resources/test/data/test.DerbyDataSourceTest
./resources/test/data/test.DerbyDataSourceTest/.DS_Store
./resources/test/data/test.DerbyDataSourceTest/GLOBAL.properties
./resources/test/data/test.DerbyDataSourceTest/Nucleus.properties
./resources/test/data/test.IdGeneratorTest
./resources/test/data/test.IdGeneratorTest/atg/dynamo/service/IdGenerator.properties
./resources/test/data/test.IdGeneratorTest/atg/dynamo/service/idspaces.xml
./resources/test/data/test.ProfileFormHandlerTest
./resources/test/data/test.ProfileFormHandlerTest/atg/scenario/ScenarioClusterManager.properties
./resources/test/data/test.ProfileFormHandlerTest/atg/scenario/ScenarioManager.properties
./resources/test/data/test.ProfileFormHandlerTest/ConfigurableConfigCreationFilter.properties


The above is from the ATG Dust sample test configurations.  There are 5 separate configuration roots used above:


   * /resources

   * /resources/config

   * /resources/config/test

   * /resources/SimpleFormHandlerTest

   * /resources/test/data


I'm pretty sure the team that put this together was more focused on providing examples of various tests than enforcing a particular convention.

To ATG Dust's credit some classes like GSATestUtils and NucleusTestUtils attempt to provide conventions for the location of your test configuration files.  Note that ATG Dust does not strictly enforce conventions and they probably shouldn't.  It should be up to each implementation team to determine how they want to organize.


Any test where you need to provide ATG configuration files should follow some sort of convention.  My preferred convention is borrowed from a particular example within ATG Dust.  The convention is as follows:


${basedir}/test/resources/com/mycompany/data/TestSomething/config

where

${basedir}/test/resources  - the path to your resources directory for the tests

com/mycompany              -  the package name for your test class

data                       -  a distinct directory named 'data'.  Every package would have a 'data' directory

TestSomething              -  the name of your test class

config                     -  a directory named 'config'



This is a convention put forth within ATG Dust and using certain methods the right way will result in this convention being utilized.  This may not be practical for all cases.  As a general guideline test configurations should attempt to keep the test-specific configurations confined to within the package name for the test.  Just be thoughtful about where these are ending up and think ahead to what you want your resources directory to look like as more tests are added to the system.

My use of Dust on my current project has been fairly lightweight.  Usually I use it to set up a repository so I can test manager classes.  Once I start getting away from the manager classes I rely upon Mockito.  I find this particular config convention to work out well as there are only a few tests I need to set up ATG configurations for.






=================================================
=================================================
Some Test APIs in ATG Dust



=================================================
GSATestUtils

This class provides utilities for manipulating test repositories.  Its key method is the 'initializeMinimalConfigPath' and its purpose is to dynamically create the minimum configuration layer with properties files that supports and actual ATG repository.  This can be a bit confusing as it hides the generated configuration from you.  The utility of this is to reduce the amount of configuration you need to set up to get a repository running for test purposes.  Here is some helpful information about this method.

I'm  not a fan of these multi-argument methods because they get confusing quick.  Here is the laundry list of arguments, what they mean and a visualization of the output.

  public  void initializeMinimalConfigpath(File pRoot,
                                           String pRepositoryPath, 
                                           String[] pDefinitionFiles,
                                           Properties pJDBCProperties, 
                                           String pCreateSQLAbsolutePath, 
                                           String pDropSQLAbsolutePath, 
                                           String[] pImportFile, 
                                           boolean pLogging,
                                           String pFakeXADataSourceComponentName, 
                                           String pJTDataSourceComponentName) throws IOException, Exception {
...

}


pRoot - This is the root location for the configuration layer.  Sample tests indicate a convention of target/test-classes/config as the location where GSATestUtils generates configuration files to support the repository.

pRepositoryPath - The Nucleus path to the repository.  For example /test/SimpleRepository. The method actually creates your repository file in the location relative to pRoot.

pDefinitionFiles - The array of Nucleus paths for the repository definition files.  If you specify a name of a file within /src/test/resources then GSATestUtils will copy that file to pRoot.  Otherwise you must provide code to copy the file to pRoot.

pJDBCProperties - The properties to have GSATestUtils set for the JDBC connection in the generated FakeXADataSource.properties file

pCreateSQLAbsolutePath - The location of SQL create statements for the repository.  This is used by the InitializingGSA class when starting the repository to create initial tables.  This can be null and if it is null the InitializingGSA class will create the SQL based upon the repository definition.

pDropSQLAbsolutePath -   The location of SQL drop statements for the repository.  This is used by the InitializingGSA class when calling the repository to drop tables.  This can be null and if it is null the InitializingGSA class will create the SQL based upon the repository definition.

pImportFile - The array of Nucleus paths to the import files for the repository.  If you specify a name of a file within /src/test/resources then GSATestUtils will copy that file to pRoot.  Otherwise you must provide code to copy the file to pRoot.  This is used by the InitializingGSA class when initializing the repository.

pLogging - Whether the screen logger is enabled or not

pFakeXADataSourceComponentName - The name of the FakeXADataSource.  If null is passed it uses the default 'FakeXADataSource' and creates the properties file with this name.

pJTDataSourceComponentName - The name of the JTDataSource.  If null is passed it uses the default 'JTDataSource' and creates the properties file with this name.


When initializeMinimalConfigpathCompletes it generates the following files:

./atg/dynamo/server/SQLRepositoryEventServer.properties
./atg/dynamo/service/ClientLockManager.properties
./atg/dynamo/service/IdGenerator.properties
./atg/dynamo/service/idspaces.xml
./atg/dynamo/service/jdbc/FakeXADataSource.properties
./atg/dynamo/service/jdbc/JTDataSource.properties
./atg/dynamo/service/logging/ScreenLog.properties
./atg/dynamo/service/xml/XMLToolsFactory.properties
./atg/dynamo/transaction/TransactionDemarcationLogging.properties
./atg/dynamo/transaction/TransactionManager.properties
./atg/dynamo/transaction/UserTransaction.properties
./com/mycompany/BasicRepository.properties
./com/mycompany/basicRepository.xml
./GLOBAL.properties
./Nucleus.properties


GSATestUtils can also clean up after itself by invoking the cleanup method.




=================================================
NucleusTestUtils

NucleusTestUtils allows you to provide a configuration directory (or list of directories) along with the name of a single component to start up.  

It allows you to dynamically write properties files to a target test location.  This saves you from having to set up external properties files and provides the benefit of being able to 'see' all the configuration settings within your test code.

This class has the added benefit of letting you start up Nucleus using modules in the ATG install location.  In this manner you can test extensions to existing ATG components.


When it starts up Nucleus it builds a configpath using a few variables passed into the method:

  public static Nucleus startNucleusWithModules(String[] pModules, 
                                        File pConfigPath
                                        Class pClassRelativeTo, 
                                        String pBaseConfigDirectory,
                                        String pInitialService) 


pModules - this is the list of ATG Modules to add to the configpath.  This allows you to test extensions to existing ATG components.

pConfigPath - this is an additional config path to have Nucleus include upon startup.  When you use this you are likely to reference the project's config directory.

pClassRelativeTo - this is the class whose location is the base directory from which your test configpath is constructed

pBaseConfigDirectory - this is the name of the configuration directory that sits beneath <path-to-relative-class>/data/.  For example <path-to-relative-class>/data/<pBaseConfigDirectory>.  If this is set to null then 'config' is used as the value.

pInitialService - this is the initial service started by Nucleus.  This is inserted in place of the usual InitialServiceName which ensures Nucleus only starts up your component and its dependencies.


When startNucleusWithModules is used the constructed config path is



   * ATG module path per the pModules parameter
   * project specific config directory like target/config.  This may be passed in as null
   * target/classes/atg/nucleus/data/config
   * target/test-classes/<path-to-relative-class>/data/<pBaseConfigDirectory>


The significance of target/classes/atg/nucleus/data/config is that this directory contains configuration settings specific to the test framework.  One significant configuration is the use of hsqldb data source in place of FakeXADataSource which results in the startup of an in memory database.

For testing conventions callers should pass either 'null' or 'pClassRelativeTo.getSimpleName()' as the pBaseConfigDirectory.  This results in structure like the below



|- src
   |- main
      |- java
         |- com
            |- mycompany
             |- SomeManager.java
   |- test
      |- java
         |- com
            |- mycompany
             |- SomeManagerTest.java
             |- AnotherTest.java
             |- AndAnotherTest.java
      |- resources
         |- com
            |- mycompany
             |- data
               |- AnotherTest
                 |- config
                   |- atg
                     |- userprofiling
                       |- userProfile.xml



=================================================
DropletInvoker

DropletInvoker is made obsolete by Mockito in my opinion.  Its meant to keep track of the oparams rendered and such.  Droplets are just servlets with a typical service(HttpServletRequest, HttpServletResponse) method that can easily and more thoroughly be tested using Mockito mock objects. 



=================================================
ATGDustCase

This is an extension of junit.framework.TestCase that is meant to provide utility when setting up and tearing down ATG tests.  It provides a lot of configuration file management than necessary.  I am not a fan of it.  I prefer to use tests that do not extend from anything but Object.  I prefer tests that explicitly deal with their own configuration setup.  In this manner I can always see exactly how the test is setting itself up.  It doesn't take much code to define where your test configurations come from. See my BasicRepositoryTest and ExtendedRepositoryTest for examples.



=================================================
=================================================
A Final Note about ATG Tests
  
ATG Dust is very useful in specific areas.  I like it for creating repository items which saves you a lot of mock object headache.  In addition your repository tests can leverage your project definition files which provides the added benefit of tests that cover the actual configuration checked into your source tree.  If someone changes the repository definition your tests break.  Which is the kind of coverage you want.

ATG Dust can help you write tests against your pipeline configuration as well.

Before applying ATG Dust I encourage you to review mainstream approaches like Mockito.  This will reduce the tech specialization your team requires and it will provide familiar structures to those who have worked with software outside the realm of ATG.  That said there are conditions (described above) where ATG Dust is very handy to have.





