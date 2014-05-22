package com.mycompany;


import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SimpleTest {
	
	private static Logger log = Logger.getLogger(SimpleTest.class);
	
	/**
	 * **********************************************************
	 * SOME SETUP
	 * **********************************************************
	 */
    @Before
    public void setUp() throws Exception {
    	log.info("Setup");
    }
	
    @Test
	public void genericTest() throws Exception {
		log.info("Test");
		
		boolean myBoolean = Boolean.TRUE.booleanValue();
		assertTrue(myBoolean);
	}

}
