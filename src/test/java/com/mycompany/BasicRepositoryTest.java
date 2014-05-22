package com.mycompany;


import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import atg.adapter.gsa.GSARepository;
import atg.adapter.gsa.GSATestUtils;
import atg.dtm.TransactionDemarcation;
import atg.nucleus.Nucleus;
import atg.nucleus.NucleusTestUtils;
import atg.nucleus.servlet.NucleusServlet;
import atg.repository.MutableRepository;
import atg.repository.MutableRepositoryItem;
import atg.repository.Repository;
import atg.repository.RepositoryItem;
import atg.test.util.DBUtils;
import atg.test.util.FileUtil;
import static org.junit.Assert.assertNotNull;

/**
 * <p>This test illustrates how to create a repository from a definition file 
 * contained completely within the project's test directory.  This is useful for
 * tests on repositories created within the project or tests that only need to
 * operate on specific repository definitions.
 * 
 * <p>If you need a test that tests the extension of an existing ATG repository
 * see ExtendedRepositoryTest
 * 
 * @author dbrandt
 *
 */
public class BasicRepositoryTest {
	
	private static Logger mLogger = Logger.getLogger(BasicRepositoryTest.class);
	
	protected static Nucleus mNucleus = null;
	protected static DBUtils mDatabaseUtil = null;
	
	protected static final String REPOSITORY_PATH = "mycompany/repository/BasicRepository";
	
	
	/**
	 * **********************************************************
	 * SOME SETUP
	 * **********************************************************
	 */
	
	/**
	 * <p>using BeforeClass allows us to set up expensive operations that are required by all tests
	 * @throws Exception
	 */
    @BeforeClass
    public static void setUp() throws Exception {
    	
    	mLogger.info("setUp()");
    	
        // setup the repository
        File configpath = new File("target/test-classes/generated-config".replace("/", File.separator));

        // Specify the path to our repository definition file.
        final String[] definitionFiles = { "mycompany/repository/basicRepository.xml" };
        mLogger.info(" definitionFile[0]=" + definitionFiles[0]);
        
        // Specify the path to our repository data import file.
        final String[] importFiles = { "mycompany/repository/basicRepository-import.xml" };
        mLogger.info(" importFile[0]=" + importFiles[0]);

        // There are a couple ways to get our repository definition file into the 
        // configuration layer that will be automatically generated during this test
        //
        // 1. If we include the definition file in the src/test/resources then the 
        //    GSATestUtils will pick it up and copy it.  This is handy if we just 
        //    need to verify certain code works against some repository definition.
        //    However it makes for some messy config layers within the test directory.
        //    Best to stick with a convention and see #2 below
        //
        // 2. We could copy it directly from some location within our filesystem.
        //    This may be the more desired mechanism as it allows us to copy the 
        //    repository definitions checked into our actual codeline to our test
        //    facility.  This provides a way to ensure changes to repository 
        //    definitions do not break functionality.
        //
        
        // Copy all properties and definition files to the previously configured configpath
        //
        FileUtil.copyDirectory("src/test/resources/com/mycompany/data/config", 
        		                configpath.getPath(), 
        		                Arrays.asList(new String[] { ".svn" }));

        // Use the DBUtils utility class to get JDBC properties for an in memory
        // HSQL DB called "testdb".
        Properties props = DBUtils.getHSQLDBInMemoryDBConnection("testdb");

        // Start up our database
        mDatabaseUtil = new DBUtils(props.getProperty("URL"),
        		                    props.getProperty("driver"),
        		                    props.getProperty("user"),
        		                    props.getProperty("password"));

        // Setup our testing configpath
        // Disabled logging (last argument to false) to get rid of double logging statements
        //
        GSATestUtils.getGSATestUtils().initializeMinimalConfigpath(configpath, 
        		                                                   REPOSITORY_PATH, 
        		                                                   definitionFiles, 
        		                                                   props, 
        		                                                   importFiles);
        
        // Start Nucleus
        mNucleus = NucleusTestUtils.startNucleus(configpath.getAbsolutePath());
    }
    
    
	/**
	 * **********************************************************
	 * TEARDOWN
	 * **********************************************************
	 */

    @AfterClass
    public static void tearDown() throws Exception {
    	
    	mLogger.info("tearDown()");
    	
    	try {
        	mNucleus.stopService();
        	mDatabaseUtil.shutdown();
    	} catch (Exception ex) {
    		mLogger.error("Error tearing down test ", ex);
    	}
    }
    
	/**
	 * **********************************************************
	 * TESTS
	 * **********************************************************
	 */
	
    @Test
	public void testSetup() throws Exception {
    	
    	mLogger.info("testSetup()");
    	
        Repository r = (Repository) mNucleus.resolveName(REPOSITORY_PATH);
        assertNotNull(r);
        
    }
    
    @Test
    public void testImport() throws Exception {
    	
    	mLogger.info("testImport()");
    	
        Repository r = (Repository) mNucleus.resolveName(REPOSITORY_PATH);
        
        RepositoryItem item = r.getItem("simple001", "simpleItem");
        assertNotNull(item);
    }
    
    @Test
    public void testRepositoryAction() throws Exception {
    	
    	mLogger.info("testRepositoryAction()");
    	
        TransactionDemarcation td = new TransactionDemarcation();
        MutableRepository r = (MutableRepository) mNucleus.resolveName(REPOSITORY_PATH);
        
        boolean rollback = true;

        try {
          // Start a new transaction
          td.begin(((GSARepository) r).getTransactionManager());
          // Create the item
          MutableRepositoryItem item = r.createItem("simpleItem");
          item.setPropertyValue("name", "simpleName");
          // Persist to the repository
          r.addItem(item);
          // Try to get it back from the repository
          String id = item.getRepositoryId();
          RepositoryItem item2 = r.getItem(id, "simpleItem");
          assertNotNull(" We did not get back the item just created from the repository.", item2);
          rollback = false;
        }
        finally {
          // End the transaction, rollback on error
          if (td != null)
            td.end(rollback);
        }
    }
    
}
