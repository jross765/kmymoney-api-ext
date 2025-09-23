package org.kmymoney.apiext.trxmgr;

import java.math.BigDecimal;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.apiext.Const;
import org.kmymoney.base.basetypes.simple.KMMAcctID;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class TransactionSplitFilter {

	// ::TODO
//	public enum DebitCredit {
//		DEBIT,
//		CREDIT,
//		UNDEFINED
//	}
	
	// ---------------------------------------------------------------

	public KMyMoneyTransactionSplit.Action      action;
	// ::TODO -- not supported yet
	// public KMyMoneyTransactionSplit.ReconStatus reconStatus;
	
	public KMMAcctID            acctID;
	public KMyMoneyAccount.Type acctType;
	
	public FixedPointNumber valueFrom;
	public FixedPointNumber valueTo;
	
	public FixedPointNumber sharesFrom;
	public FixedPointNumber sharesTo;
	
	public String memoPart;
	
	// ---------------------------------------------------------------
	
	public TransactionSplitFilter() {
		init();
		reset();
	}

	// ---------------------------------------------------------------
	
	private void init() {
		action = null;
		// reconStatus = null;
		
		acctID = new KMMAcctID();
		acctType = null;
		
		valueFrom = new FixedPointNumber(BigDecimal.valueOf(Const.UNSET_VALUE));
		valueTo = new FixedPointNumber(BigDecimal.valueOf(Const.UNSET_VALUE));
		
		sharesFrom = new FixedPointNumber(BigDecimal.valueOf(Const.UNSET_VALUE));
		sharesTo = new FixedPointNumber(BigDecimal.valueOf(Const.UNSET_VALUE));
		
		memoPart = "";
	}
	
	public void reset() {
		action = null;
		// reconStatus = null;
		
		acctID.reset();
		acctType = null;
		
		valueFrom = new FixedPointNumber(BigDecimal.valueOf(Const.UNSET_VALUE));
		valueTo = new FixedPointNumber(BigDecimal.valueOf(Const.UNSET_VALUE));
		
		sharesFrom = new FixedPointNumber(BigDecimal.valueOf(Const.UNSET_VALUE));
		sharesTo = new FixedPointNumber(BigDecimal.valueOf(Const.UNSET_VALUE));
		
		memoPart = "";
	}
	
	// ---------------------------------------------------------------
	
	public boolean matchesCriteria(final KMyMoneyTransactionSplit splt) {
		
		if ( splt == null ) {
			throw new IllegalArgumentException("null transaction-split given");
		}
		
		if ( action != null ) {
			if ( splt.getAction() != action ) {
				return false;
			}
		}
		
//		if ( reconStatus != null ) {
//			if ( ! splt.getReconStatus().getID().equals(acctID) ) {
//				return false;
//			}
//		}
		
		if ( acctID.isSet() ) {
			if ( ! splt.getAccount().getID().toString().equals(acctID.toString()) ) { // important: toString()
				return false;
			}
		}
		
		if ( acctType != null ) {
			if ( splt.getAccount().getType() != acctType ) {
				return false;
			}
		}
		
		if ( valueFrom.getBigDecimal().doubleValue() != Const.UNSET_VALUE ) {
			if ( splt.getValue().isLessThan(valueFrom, Const.DIFF_TOLERANCE_VALUE ) ) {
				return false;
			}
		}
		
		if ( valueTo.getBigDecimal().doubleValue() != Const.UNSET_VALUE ) {
			if ( splt.getValue().isGreaterThan(valueTo, Const.DIFF_TOLERANCE_VALUE ) ) {
				return false;
			}
		}
		
		if ( sharesFrom.getBigDecimal().doubleValue() != Const.UNSET_VALUE ) {
			if ( splt.getShares().isLessThan(sharesFrom, Const.DIFF_TOLERANCE_VALUE ) ) {
				return false;
			}
		}
		
		if ( sharesTo.getBigDecimal().doubleValue() != Const.UNSET_VALUE ) {
			if ( splt.getShares().isGreaterThan(sharesTo, Const.DIFF_TOLERANCE_VALUE ) ) {
				return false;
			}
		}
		
		if ( ! memoPart.trim().equals("") ) {
			if ( ! splt.getMemo().contains(memoPart.trim()) ) {
				return false;
			}
		}
		
		return true;
	}
	
	// ---------------------------------------------------------------
	
	@Override
	public String toString() {
		return "TransactionSplitFilter [" + 
	                 "action=" + action + ", " +
				     "acctID=" + acctID + ", " +
	               "acctType=" + acctType + ", " +
				  "valueFrom=" + valueFrom + ", " +
	                "valueTo=" + valueTo + ", " +
			     "sharesFrom=" + sharesFrom + ", " + 
	               "sharesTo=" + sharesTo + ", " +
			       "memoPart='" + memoPart + "']";
	}

}
