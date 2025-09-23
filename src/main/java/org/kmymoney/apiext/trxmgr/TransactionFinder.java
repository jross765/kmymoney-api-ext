package org.kmymoney.apiext.trxmgr;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.apiext.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionFinder {
	
    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionFinder.class);
    
    // ---------------------------------------------------------------
    
	private KMyMoneyWritableFile kmmFile = null;
	
    // ---------------------------------------------------------------
	
	public TransactionFinder(KMyMoneyWritableFile kmmFile) {
		if ( kmmFile == null ) {
			throw new IllegalArgumentException("null kmymoney-file object given");
		}
		
		this.kmmFile = kmmFile;
	}
    
    // ---------------------------------------------------------------
	
	// ::TODO
	// - Have results writable?
    
	public ArrayList<KMyMoneyTransaction> find(TransactionFilter flt, 
			                                  boolean withSplits,
			                                  TransactionFilter.SplitLogic splitLogic) {
		if ( flt == null ) {
			throw new IllegalArgumentException("null transaction-filter given");
		}
		
		LOGGER.debug("find: Searching for Transactions matching filter: " + flt.toString());
		ArrayList<KMyMoneyTransaction> result = new ArrayList<KMyMoneyTransaction>();
		
		Collection<? extends KMyMoneyTransaction> candList = null;
		if ( flt.isDatePostedFromSet() ||
			 flt.isDatePostedToSet() ) {
			LocalDate fromDate = null;
			LocalDate toDate = null;
			
			if ( flt.isDatePostedFromSet() )
				fromDate = flt.datePostedFrom;
			else
				fromDate = Const.TRX_SUPER_EARLY_DATE;
			
			if ( flt.isDatePostedToSet() )
				toDate = flt.datePostedTo;
			else
				toDate = Const.TRX_SUPER_LATE_DATE;
			
			candList = kmmFile.getTransactions(fromDate, toDate);
		} else {
			candList = kmmFile.getTransactions();
		}
		
		for ( KMyMoneyTransaction trx : candList ) {
			if ( flt.matchesCriteria(trx, withSplits, splitLogic) ) {
				result.add(trx);
			}
		}
		
		LOGGER.debug("find: Found " + result.size() + " Transactions matching filter");
		return result;
	}

}
