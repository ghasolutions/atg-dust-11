package com.mycompany;


import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import javax.servlet.ServletException;

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
             mNucleus = NucleusTestUtils.startNucleusWithModules(new String[] { "DAS","DPS"},
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
	
    @Test
	public void testSetup() throws Exception {
    	
    	mLogger.info("testSetup()");
        
    }
    
}
