package org.kmymoney.apiext.trxmgr;

import java.util.ArrayList;
import java.util.Collection;

import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionSplitFinder {
	
    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionSplitFinder.class);
    
    // ---------------------------------------------------------------
    
	private KMyMoneyWritableFile kmmFile = null;
	
    // ---------------------------------------------------------------
	
	public TransactionSplitFinder(KMyMoneyWritableFile kmmFile) {
		if ( kmmFile == null ) {
			throw new IllegalArgumentException("null KMyMoney-file object given");
		}
		
		this.kmmFile = kmmFile;
	}
    
    // ---------------------------------------------------------------
	
	// ::TODO
	// - Have results writable?
    
	public ArrayList<KMyMoneyTransactionSplit> find(TransactionSplitFilter flt) {
		if ( flt == null ) {
			throw new IllegalArgumentException("null transaction-split-filter given");
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
