package org.kmymoney.apiext.trxmgr;

import java.time.LocalDate;

import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;

import xyz.schnorxoborx.base.dateutils.DateHelpers;
import xyz.schnorxoborx.base.dateutils.LocalDateHelpers;

public class TransactionFilter {
	
	public enum SplitLogic {
		AND, // split criteria have to apply to every single split
		OR   // it's enough if split criteria apply to one or just a few splits
	}
	
	// ---------------------------------------------------------------
	// Transaction Level

	// ::TODO -- not supported yet
	// public KMyMoneyTransaction.Type type;
	
	public LocalDate datePostedFrom;
	public LocalDate datePostedTo;
	
	public int nofSpltFrom;
	public int nofSpltTo;

	public String memoPart;
	
	// ----------------------------
	// Split Level

	public TransactionSplitFilter spltFilt;
	
	// ---------------------------------------------------------------
	
	public TransactionFilter() {
		init();
		reset();
	}

	// ---------------------------------------------------------------
	
	private void init() {
		// type = null;

		try {
			datePostedFrom = LocalDateHelpers.parseLocalDate(LocalDateHelpers.DATE_UNSET, DateHelpers.DATE_FORMAT_1);
			datePostedTo = LocalDateHelpers.parseLocalDate(LocalDateHelpers.DATE_UNSET, DateHelpers.DATE_FORMAT_1);
		} catch (Exception e) {
			// pro forma, de facto unreachable
			e.printStackTrace();
		}
		
		nofSpltFrom = 0;
		nofSpltTo = 0;

		memoPart = "";
		
		// ---
		
		spltFilt = new TransactionSplitFilter();
	}
	
	public void reset() {
		// type = null;
		
		try {
			datePostedFrom = LocalDateHelpers.parseLocalDate(LocalDateHelpers.DATE_UNSET, DateHelpers.DATE_FORMAT_1);
			datePostedTo = LocalDateHelpers.parseLocalDate(LocalDateHelpers.DATE_UNSET, DateHelpers.DATE_FORMAT_1);
		} catch (Exception e) {
			// pro forma, de facto unreachable
			e.printStackTrace();
		}
		
		nofSpltFrom = 0;
		nofSpltTo = 0;

		memoPart = "";
		
		// ---
		
		spltFilt.reset();
	}
	
	// ---------------------------------------------------------------
	
	public boolean matchesCriteria(final KMyMoneyTransaction trx,
			                       final boolean withSplits,
			                       final SplitLogic splitLogic) {
		
		if ( trx == null ) {
			throw new IllegalArgumentException("argument <trx> is null");
		}
		
		// 1) Transaction Level
//		if ( type != null ) {
//			if ( trx.gettype() != type) {
//				return false;
//			}
//		}

		if ( isDatePostedFromSet() ) {
			if ( trx.getDatePosted().isBefore(datePostedFrom) ) {
				return false;
			}
		}
		
		if ( isDatePostedToSet() ) {
			if ( trx.getDatePosted().isAfter(datePostedTo) ) {
				return false;
			}
		}
			
		if ( nofSpltFrom != 0 ) {
			if ( trx.getSplits().size() < nofSpltFrom ) {
				return false;
			}
		}
		
		if ( nofSpltTo != 0 ) {
			if ( trx.getSplits().size() > nofSpltTo ) {
				return false;
			}
		}
		
		if ( ! memoPart.trim().equals("") ) {
			if ( ! trx.getMemo().contains(memoPart.trim()) ) {
				return false;
			}
		}
		
		// 2) Split Level
		if ( withSplits ) {
			if ( ! splitsMatchCriteria(trx, splitLogic) ) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean splitsMatchCriteria(final KMyMoneyTransaction trx,
										final SplitLogic splitLogic) {
		if ( spltFilt == null ) {
			throw new IllegalStateException("split-filter is null");
		}
		
		if ( splitLogic == SplitLogic.AND ) {
			for ( KMyMoneyTransactionSplit splt : trx.getSplits() ) {
				if ( ! spltFilt.matchesCriteria(splt) ) {
					return false;
				}
			}
			return true;
		} else if ( splitLogic == SplitLogic.OR ) {
			boolean oneMatch = false;
			for ( KMyMoneyTransactionSplit splt : trx.getSplits() ) {
				if ( spltFilt.matchesCriteria(splt) ) {
					oneMatch = true;
				}
			}
			if ( ! oneMatch )
				return false;
			else
				return true;
		} // splitLogic
		
		return true; // Compiler happy
	}
	
	// -----------------------------------------------------
	// helpers

	public boolean isDatePostedFromSet() {
		try {
			if ( datePostedFrom.equals( LocalDateHelpers.parseLocalDate(LocalDateHelpers.DATE_UNSET, DateHelpers.DATE_FORMAT_1) ) )
				return false;
			else			
				return true;
		} catch (Exception e) {
			// pro forma, de facto unreachable
			e.printStackTrace();
		}
		
		return true; // Compiler happy
	}

	public boolean isDatePostedToSet() {
		try {
			if ( datePostedTo.equals( LocalDateHelpers.parseLocalDate(LocalDateHelpers.DATE_UNSET, DateHelpers.DATE_FORMAT_1) ) )
				return false;
			else			
				return true;
		} catch (Exception e) {
			// pro forma, de facto unreachable
			e.printStackTrace();
		}
		
		return true; // Compiler happy
	}
	
	// ---------------------------------------------------------------

	@Override
	public String toString() {
		return "TransactionFilter [" + 
	              "datePostedFrom=" + datePostedFrom + ", " +
				    "datePostedTo=" + datePostedTo + ", " +
	                 "nofSpltFrom=" + nofSpltFrom + ", " +
				       "nofSpltTo=" + nofSpltTo + ", " +
	                   "memoPart='" + memoPart + "', " +
				        "spltFilt=" + spltFilt + "]";
	}

}
