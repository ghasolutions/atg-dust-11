package com.mycompany;


import java.io.File;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import atg.adapter.gsa.GSATestUtils;
import atg.nucleus.Nucleus;
import atg.nucleus.NucleusTestUtils;
import atg.repository.Repository;
import atg.repository.RepositoryItem;
import atg.test.util.DBUtils;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;

/**
 * <p>This test illustrates how to spin up a repository using definitions from
 * within the ATG install base plus definitions supplied by the project.
 * 
 * @author dbrandt
 *
 */
public class ExtendedRepositoryTest {
	
	private static Logger mLogger = Logger.getLogger(ExtendedRepositoryTest.class);
	
	protected static Nucleus mNucleus = null;
	protected static DBUtils mDatabaseUtil = null;
	
	// The ATG namespace name of the repository we are testing
	//
	protected static final String REPOSITORY_PATH = "atg/userprofiling/ProfileAdapterRepository";
	
	
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
    	
        try {
        	
            // identify our additional config path to read the repository definition
        	// from.  In this case it is the actual config path for our applcation 
        	// project located at /config
        	//
            File projectConfigPath = new File("target/config".replace("/", File.separator));
            
            // create our test repository properties file and point it to our test import
            // file location so we can load the repository on startup with test data.
            // The confention in GSATestUtils is to look at for a config directory 
            // underneath resources/${class-package-path}/{class-name}/config/
            //
            // If we weren't dependent upon test data then we could skip this step
            //
            GSATestUtils testUtils = GSATestUtils.getGSATestUtils();
            testUtils.createRepositoryPropertiesFile(ExtendedRepositoryTest.class, 
            		                                 REPOSITORY_PATH, 
            		                                 new String[] {"atg/userprofiling/userProfile-import.xml"}, 
            		                                 null);
            
            // Start up our nucleus with the declared ATG modules and then apply
            // our additional config path at the end
            //
            mNucleus = NucleusTestUtils.startNucleusWithModules(new String[] { "DAS","DPS"},
            		                                            projectConfigPath,
            		                                            ExtendedRepositoryTest.class,
            		                                            REPOSITORY_PATH);

        } catch (ServletException e) {
            fail(e.getMessage());
        }
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
    	} catch (Exception ex) {
    		mLogger.error("Error tearing down test ", ex);
    	}
    }
    
	/**
	 * **********************************************************
	 * TESTS
	 * **********************************************************
	 */
	
    /*
     * Test that we got a repository up and started.  Then test that our
     * property addition to the repository was picked up.
     */
    @Test
	public void testSetup() throws Exception {
    	
    	mLogger.info("testSetup()");
        Repository r = (Repository) mNucleus.resolveName(REPOSITORY_PATH);
        
        RepositoryItem item = r.getItem("user00001", "user");
        assertNotNull(item);
        
        // make sure our repository extension was picked up
        String userToken = (String) item.getPropertyValue("token");
        assertNotNull(userToken);
    }
    
}
