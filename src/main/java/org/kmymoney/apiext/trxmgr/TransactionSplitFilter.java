package org.kmymoney.apiext.trxmgr;

import java.math.BigDecimal;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyTransactionSplitImpl;
import org.kmymoney.apiext.Const;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMPyeID;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class TransactionSplitFilter {

	// ::TODO
//	public enum DebitCredit {
//		DEBIT,
//		CREDIT,
//		UNDEFINED
//	}
	
	// ---------------------------------------------------------------

	public KMyMoneyTransactionSplit.Action     action;
	public KMyMoneyTransactionSplit.ReconState reconState;
	
	public KMMAcctID            acctID;
	public KMMPyeID             pyeID;
	
	public KMyMoneyAccount.Type acctType;
	
	public FixedPointNumber valueFrom;
	public FixedPointNumber valueTo;
	public boolean          valueAbs;
	
	public FixedPointNumber sharesFrom;
	public FixedPointNumber sharesTo;
	public boolean          sharesAbs;
	
	public String memoPart;
	
	// ---------------------------------------------------------------
	
	public TransactionSplitFilter() {
		init();
		reset();
	}

	// ---------------------------------------------------------------
	
	private void init() {
		action = null;
		reconState = null;
		
		acctID = new KMMAcctID();
		pyeID  = new KMMPyeID();
		
		acctType = null;
		
		valueFrom = new FixedPointNumber(BigDecimal.valueOf(Const.UNSET_VALUE));
		valueTo   = new FixedPointNumber(BigDecimal.valueOf(Const.UNSET_VALUE));
		valueAbs  = false;
		
		sharesFrom = new FixedPointNumber(BigDecimal.valueOf(Const.UNSET_VALUE));
		sharesTo   = new FixedPointNumber(BigDecimal.valueOf(Const.UNSET_VALUE));
		sharesAbs  = false;
		
		memoPart = "";
	}
	
	public void reset() {
		action = null;
		reconState  = null;

		acctID.reset();
		pyeID.reset();

		acctType = null;

		valueFrom = new FixedPointNumber(BigDecimal.valueOf(Const.UNSET_VALUE));
		valueTo   = new FixedPointNumber(BigDecimal.valueOf(Const.UNSET_VALUE));
		valueAbs  = false;

		sharesFrom = new FixedPointNumber(BigDecimal.valueOf(Const.UNSET_VALUE));
		sharesTo   = new FixedPointNumber(BigDecimal.valueOf(Const.UNSET_VALUE));
		sharesAbs  = false;
		
		memoPart = "";
	}
	
	// ---------------------------------------------------------------
	
	public boolean matchesCriteria(final KMyMoneyTransactionSplit splt) {
		
		if ( splt == null ) {
			throw new IllegalArgumentException("null transaction-split given");
		}
		
		// ---
		
		if ( action != null ) {
			// Important pre-check first:
			// (alternatively: call getAction() directly and catch MappingException)
			String actionStr = ((KMyMoneyTransactionSplitImpl) splt).getActionStr();
			if ( actionStr.trim().equals("") ) {
				return false;
			}

			// Core check
			if ( splt.getAction() != action ) {
				return false;
			}
		}
		
		if ( reconState != null ) {
			// Important pre-check first:
			// (alternatively: call getReconState() directly and catch MappingException)
			int reconStateInt = ((KMyMoneyTransactionSplitImpl) splt).getReconStateInt();
			if ( reconStateInt == -1 ) { // ::MAGIC, cf. impl of method getReconStateInt()
				return false;
			}

			// Core check
			if ( splt.getReconState() != reconState ) {
				return false;
			}
		}
		
		// ---
		
		if ( acctID.isSet() ) {
			if ( splt.getAccountID() != null ) { // *not* important, as opposed to pyeID
				if ( ! splt.getAccountID().toString().equals(acctID.toString()) ) { // important: toString()
					return false;
				}
			}
		}
		
		if ( pyeID.isSet() ) {
			if ( splt.getPayeeID() != null ) { // important, as opposed to acctID
				if ( ! splt.getPayeeID().toString().equals(pyeID.toString()) ) { // toString() *optional* here
					return false;
				}
			}
		}
		
		// ---
		
		if ( acctType != null ) {
			if ( splt.getAccount().getType() != acctType ) {
				return false;
			}
		}
		
		// ---
		
		if ( valueFrom.getBigDecimal().doubleValue() != Const.UNSET_VALUE ) {
			FixedPointNumber val = splt.getValue();
			if ( valueAbs && 
				 val.isNegative() ) {
				val.negate();
			}
			
			if ( val.isLessThan(valueFrom, Const.DIFF_TOLERANCE_VALUE ) ) {
				return false;
			}
		}
		
		if ( valueTo.getBigDecimal().doubleValue() != Const.UNSET_VALUE ) {
			FixedPointNumber val = splt.getValue();
			if ( valueAbs && 
				 val.isNegative() ) {
				val.negate();
			}
			
			if ( val.isGreaterThan(valueTo, Const.DIFF_TOLERANCE_VALUE ) ) {
				return false;
			}
		}
		
		// ---
		
		if ( sharesFrom.getBigDecimal().doubleValue() != Const.UNSET_VALUE ) {
			FixedPointNumber shr = splt.getShares();
			if ( sharesAbs && 
				 shr.isNegative() ) {
				shr.negate();
			}
			
			if ( shr.isLessThan(sharesFrom, Const.DIFF_TOLERANCE_VALUE ) ) {
				return false;
			}
		}
		
		if ( sharesTo.getBigDecimal().doubleValue() != Const.UNSET_VALUE ) {
			FixedPointNumber shr = splt.getShares();
			if ( sharesAbs && 
				 shr.isNegative() ) {
				shr.negate();
			}
			
			if ( shr.isGreaterThan(sharesTo, Const.DIFF_TOLERANCE_VALUE ) ) {
				return false;
			}
		}
		
		// ---
		
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
				"recon-state=" + reconState + ", " +

				     "acctID=" + acctID + ", " +
				      "pyeID=" + pyeID + ", " +
				     
	               "acctType=" + acctType + ", " +

				  "valueFrom=" + valueFrom + ( valueFrom.getBigDecimal().doubleValue() == Const.UNSET_VALUE ? " (unset)" : "" ) + ", " +
	                "valueTo=" + valueTo   + ( valueTo  .getBigDecimal().doubleValue() == Const.UNSET_VALUE ? " (unset)" : "" ) + ", " +
	               "valueAbs=" + valueAbs + ", " +

			     "sharesFrom=" + sharesFrom + ( sharesFrom.getBigDecimal().doubleValue() == Const.UNSET_VALUE ? " (unset)" : "" ) + ", " + 
	               "sharesTo=" + sharesTo   + ( sharesTo  .getBigDecimal().doubleValue() == Const.UNSET_VALUE ? " (unset)" : "" ) + ", " +
	              "sharesAbs=" + sharesAbs + ", " +

			       "memoPart='" + memoPart + "']";
	}

}
