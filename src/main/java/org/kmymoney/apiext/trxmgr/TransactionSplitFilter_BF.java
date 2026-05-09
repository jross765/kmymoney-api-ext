package org.kmymoney.apiext.trxmgr;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyTransactionSplitImpl;
import org.kmymoney.apiext.Const;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMPyeID;

public class TransactionSplitFilter_BF {
	
	private static final BigFraction UNSET_VALUE = Const.UNSET_VALUE_BF;

	// ---------------------------------------------------------------

	public KMyMoneyTransactionSplit.Action     action;
	public KMyMoneyTransactionSplit.ReconState reconState;
	
	public KMMAcctID        acctID;
	public KMMPyeID         pyeID;
	
	public KMyMoneyAccount.Type acctType;
	
	public BigFraction      valueFrom;
	public BigFraction      valueTo;
	public boolean          valueAbs;
	
	public BigFraction      sharesFrom;
	public BigFraction      sharesTo;
	public boolean          sharesAbs;
	
	public String memoPart;
	
	// ---------------------------------------------------------------
	
	public TransactionSplitFilter_BF() {
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
		
		valueFrom = UNSET_VALUE;
		valueTo   = UNSET_VALUE;
		valueAbs  = false;
		
		sharesFrom = UNSET_VALUE;
		sharesTo   = UNSET_VALUE;
		sharesAbs  = false;
		
		memoPart = "";
	}
	
	public void reset() {
		action = null;
		reconState = null;

		acctID.reset();
		pyeID.reset();

		acctType = null;

		valueFrom = UNSET_VALUE;
		valueTo   = UNSET_VALUE;
		valueAbs  = false;

		sharesFrom = UNSET_VALUE;
		sharesTo   = UNSET_VALUE;
		sharesAbs  = false;
		
		memoPart = "";
	}
	
	// ---------------------------------------------------------------
	
	public boolean matchesCriteria(final KMyMoneyTransactionSplit splt) {
		
		if ( splt == null ) {
			throw new IllegalArgumentException("argument <splt> is null");
		}
		
		// ---
		
		if ( action != null ) {
			// Pre-check first:
			// (alternatively: call getAction() directly and catch MappingException)
			// (Not nearly as important as in sister module, though, because here, 
			// the values are standardized.)
			String actionStr = ((KMyMoneyTransactionSplitImpl) splt).getActionStr();
			if ( actionStr == null ) {
				return false;
			}

			if ( actionStr.isBlank() ) {
				return false;
			}

			// Core check
			if ( splt.getAction() != action ) {
				return false;
			}
		}
		
		if ( reconState != null ) {
			// Pre-check first:
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
		
		if ( ! valueFrom.equals(UNSET_VALUE) ) {
			BigFraction val = splt.getValueRat();
			if ( valueAbs && 
				 val.compareTo(BigFraction.ZERO) < 0 ) {
				val = val.negate(); // immutable
			}
			
			// CAUTION: Will not work due to bug in BigFraction.compareTo()
			// if ( val.compareTo(valueFrom) < 0 ) {
			// Instead:
			if ( valueFrom.subtract(val).compareTo(BigFraction.ZERO) > 0 ) {
				return false;
			}
		}
		
		if ( ! valueTo.equals(UNSET_VALUE) ) {
			BigFraction val = splt.getValueRat();
			if ( valueAbs && 
				 val.compareTo(BigFraction.ZERO) < 0 ) {
				val = val.negate(); // immutable
			}
			
			// CAUTION: Will not work due to bug in BigFraction.compareTo()
			// if ( val.compareTo(valueTo) > 0 ) {
			// Instead:
			if ( valueTo.subtract(val).compareTo(BigFraction.ZERO) < 0 ) {
				return false;
			}
		}
		
		// ---
		
		if ( ! sharesFrom.equals(UNSET_VALUE) ) {
			BigFraction qty = splt.getSharesRat();
			if ( sharesAbs && 
				 qty.compareTo(BigFraction.ZERO) < 0 ) {
				qty = qty.negate(); // immutable
			}
			
			// CAUTION: Will not work due to bug in BigFraction.compareTo()
			// if ( qty.compareTo(quantityFrom) < 0 ) {
			// Instead:
			if ( sharesFrom.subtract(qty).compareTo(BigFraction.ZERO) > 0 ) {
				return false;
			}
		}
		
		if ( ! sharesTo.equals(UNSET_VALUE) ) {
			BigFraction qty = splt.getSharesRat();
			if ( sharesAbs && 
				 qty.compareTo(BigFraction.ZERO) < 0 ) {
				qty = qty.negate(); // immutable
			}
			
			// CAUTION: Will not work due to bug in BigFraction.compareTo()
			// if ( qty.compareTo(quantityTo) > 0 ) {
			// Instead:
			if ( sharesTo.subtract(qty).compareTo(BigFraction.ZERO) < 0 ) {
				return false;
			}
		}
		
		// ---
		
		if ( ! memoPart.isBlank() ) {
			if ( splt.getMemo() != null ) {
				if ( ! splt.getMemo().toLowerCase().contains(memoPart.trim().toLowerCase()) ) {
					return false;
				}
			} else {
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

				  "valueFrom=" + valueFrom + ( valueFrom.bigDecimalValue().doubleValue() == Const.UNSET_VALUE ? " (unset)" : "" ) + ", " +
	                "valueTo=" + valueTo   + ( valueTo  .bigDecimalValue().doubleValue() == Const.UNSET_VALUE ? " (unset)" : "" ) + ", " +
	               "valueAbs=" + valueAbs + ", " +

			     "sharesFrom=" + sharesFrom + ( sharesFrom.bigDecimalValue().doubleValue() == Const.UNSET_VALUE ? " (unset)" : "" ) + ", " + 
	               "sharesTo=" + sharesTo   + ( sharesTo  .bigDecimalValue().doubleValue() == Const.UNSET_VALUE ? " (unset)" : "" ) + ", " +
	              "sharesAbs=" + sharesAbs + ", " +

			       "memoPart='" + memoPart + "']";
	}

}
