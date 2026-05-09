package org.kmymoney.apiext.trxmgr;

import java.util.ArrayList;
import java.util.Collection;

import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionSplitFinder {
	
    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionSplitFinder.class);
    
    // ---------------------------------------------------------------
    
	private KMyMoneyFile kmmFile = null;
	
    // ---------------------------------------------------------------
	
	public TransactionSplitFinder(KMyMoneyFile kmmFile) {
		if ( kmmFile == null ) {
			throw new IllegalArgumentException("argument <kmmFile> is null");
		}
		
		this.kmmFile = kmmFile;
	}
    
    // ---------------------------------------------------------------
	
	// ::TODO
	// - Have results writable?
    
	public ArrayList<KMyMoneyTransactionSplit> find(TransactionSplitFilter_BF flt) {
		if ( flt == null ) {
			throw new IllegalArgumentException("argument <flt> is null");
		}
		
		LOGGER.debug("find: Searching for Transaction-Splits matching filter: " + flt.toString());
		ArrayList<KMyMoneyTransactionSplit> result = new ArrayList<KMyMoneyTransactionSplit>();
		
		Collection<KMyMoneyTransactionSplit> candList = kmmFile.getTransactionSplits();
		
		for ( KMyMoneyTransactionSplit splt : candList ) {
			if ( flt.matchesCriteria(splt) ) {
				result.add(splt);
			}
		}

		LOGGER.debug("find: Found " + result.size() + " Transaction-Splits matching filter");
		return result;
	}

}
