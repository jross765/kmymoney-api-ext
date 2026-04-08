package org.kmymoney.apiext.trxmgr;

import java.time.LocalDate;

import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;

import xyz.schnorxoborx.base.dateutils.LocalDateHelpers;

public class TransactionFilter {
	
	public enum SplitLogic {
		AND, // split criteria have to apply to every single split
		OR   // it's enough if split criteria apply to one or just a few splits
	}
	
	// ---------------------------------------------------------------

	public static final LocalDate DATE_UNSET     = LocalDateHelpers.DATE_UNSET;
	public static final int       NOF_SPLT_UNSET = 0;
	
	// ---------------------------------------------------------------
	// Transaction Level

	// ::TODO -- not supported yet
	// public KMyMoneyTransaction.Type type;
	
	public LocalDate datePostedFrom;
	public LocalDate datePostedTo;
	
	public LocalDate dateEnteredFrom;
	public LocalDate dateEnteredTo;
	
	public int nofSpltFrom;
	public int nofSpltTo;

	public String memoPart;
	
	// ----------------------------
	// Split Level

	public TransactionSplitFilter_FP spltFilt;
	
	// ---------------------------------------------------------------
	
	public TransactionFilter() {
		init();
		reset();
	}

	// ---------------------------------------------------------------
	
	private void init() {
		// type = null;

		datePostedFrom  = DATE_UNSET;
		datePostedTo    = DATE_UNSET;
		
		dateEnteredFrom = DATE_UNSET;
		dateEnteredTo   = DATE_UNSET;
		
		nofSpltFrom = NOF_SPLT_UNSET;
		nofSpltTo   = NOF_SPLT_UNSET;

		memoPart = "";
		
		// ---
		
		spltFilt = new TransactionSplitFilter_FP();
	}
	
	public void reset() {
		// type = null;
		
		datePostedFrom  = DATE_UNSET;
		datePostedTo    = DATE_UNSET;
		
		dateEnteredFrom = DATE_UNSET;
		dateEnteredTo   = DATE_UNSET;
		
		nofSpltFrom = NOF_SPLT_UNSET;
		nofSpltTo   = NOF_SPLT_UNSET;

		memoPart = "";
		
		// ---
		
		spltFilt.reset();
	}
	
	// ---------------------------------------------------------------
	
	public boolean matchesCriteria(final KMyMoneyTransaction trx,
            					   final boolean withSplits,
            					   final SplitLogic splitLogic) {
		return matchesCriteria(trx, true, withSplits, splitLogic);
	}

	public boolean matchesCriteria(final KMyMoneyTransaction trx,
								   final boolean datePostedAlreadyFiltered,
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

		// ---
		
		if ( ! datePostedAlreadyFiltered ) {
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
		}
		
		// ---
			
		if ( isDateEnteredFromSet() ) {
			if ( trx.getDateEntered().isBefore(dateEnteredFrom) ) {
				return false;
			}
		}
		
		if ( isDateEnteredToSet() ) {
			if ( trx.getDateEntered().isAfter(dateEnteredTo) ) {
				return false;
			}
		}
			
		// ---
		
		if ( nofSpltFrom != NOF_SPLT_UNSET ) {
			if ( trx.getSplitsCount() < nofSpltFrom ) {
				return false;
			}
		}
		
		if ( nofSpltTo != NOF_SPLT_UNSET ) {
			if ( trx.getSplitsCount() > nofSpltTo ) {
				return false;
			}
		}
		
		// ---
		
		if ( ! memoPart.isBlank() ) {
			if ( trx.getMemo() != null ) {
				if ( ! trx.getMemo().toLowerCase().contains(memoPart.trim().toLowerCase()) ) {
					return false;
				}
			} else {
				return false;
			}
		}
		
		// ---------
		
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
	
	// ---------------------------------------------------------------
	// helpers

	public boolean isDatePostedFromSet() {
		if ( datePostedFrom.equals(DATE_UNSET) )
			return false;
		else			
			return true;
	}

	public boolean isDatePostedToSet() {
		if ( datePostedTo.equals(DATE_UNSET) )
			return false;
		else			
			return true;
	}
	
	// ----------------------------

	public boolean isDateEnteredFromSet() {
		if ( dateEnteredFrom.equals(DATE_UNSET) )
			return false;
		else			
			return true;
	}

	public boolean isDateEnteredToSet() {
		if ( dateEnteredTo.equals(DATE_UNSET) )
			return false;
		else			
			return true;
	}
	
	// ---------------------------------------------------------------

	@Override
	public String toString() {
		return "TransactionFilter [" + 
	              "datePostedFrom=" + datePostedFrom  + ( isDatePostedFromSet()  ? "" : " (unset)" ) + ", " +
				    "datePostedTo=" + datePostedTo    + ( isDatePostedToSet()    ? "" : " (unset)" ) + ", " +
	              
                 "dateEnteredFrom=" + dateEnteredFrom + ( isDateEnteredFromSet() ? "" : " (unset)" ) + ", " +
                   "dateEnteredTo=" + dateEnteredTo   + ( isDateEnteredToSet()   ? "" : " (unset)" ) + ", " +

	                 "nofSpltFrom=" + nofSpltFrom + ( nofSpltFrom == NOF_SPLT_UNSET ? " (unset)" : "" ) + ", " + 
				       "nofSpltTo=" + nofSpltTo   + ( nofSpltTo   == NOF_SPLT_UNSET ? " (unset)" : "" ) + ", " +
	                 
	                   "memoPart='" + memoPart + "', " +

				        "spltFilt=" + spltFilt + "]";
	}

}
