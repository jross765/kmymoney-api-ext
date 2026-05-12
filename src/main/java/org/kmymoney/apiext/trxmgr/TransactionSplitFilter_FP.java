package org.kmymoney.apiext.trxmgr;

import java.math.BigDecimal;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyTransactionSplitImpl;
import org.kmymoney.apiext.Const;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMPyeID;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

@Deprecated
public class TransactionSplitFilter_FP {

	// a bit bulky, I admit...
	static final FixedPointNumber UNSET_VALUE = new FixedPointNumber(BigDecimal.valueOf(Const.UNSET_VALUE));

	// ---------------------------------------------------------------

	public KMyMoneyTransactionSplit.Action     action;
	public KMyMoneyTransactionSplit.ReconState reconState;
	
	public KMMAcctID        acctID;
	public KMMPyeID         pyeID;
	
	public KMyMoneyAccount.Type acctType;
	
	public FixedPointNumber valueFrom;
	public FixedPointNumber valueTo;
	public boolean          valueAbs;
	
	public FixedPointNumber sharesFrom;
	public FixedPointNumber sharesTo;
	public boolean          sharesAbs;
	
	public String memoPart;
	
	// ---------------------------------------------------------------
	
	@Deprecated
	public TransactionSplitFilter_FP() {
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
	
	@Deprecated
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
	
	@Deprecated
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
			FixedPointNumber val = splt.getValue();
			if ( valueAbs && 
				 val.compareTo(FixedPointNumber.ZERO) < 0 ) {
				val.negate(); // mutable
			}
			
			if ( val.isLessThan(valueFrom, Const.DIFF_TOLERANCE_VALUE ) ) {
				return false;
			}
		}
		
		if ( ! valueTo.equals(UNSET_VALUE) ) {
			FixedPointNumber val = splt.getValue();
			if ( valueAbs && 
				 val.compareTo(FixedPointNumber.ZERO) < 0 ) {
				val.negate(); // mutable
			}
			
			if ( val.isGreaterThan(valueTo, Const.DIFF_TOLERANCE_VALUE ) ) {
				return false;
			}
		}
		
		// ---
		
		if ( ! sharesFrom.equals(UNSET_VALUE) ) {
			FixedPointNumber shr = splt.getShares();
			if ( sharesAbs && 
				 shr.compareTo(FixedPointNumber.ZERO) < 0 ) {
				shr.negate(); // mutable
			}
			
			if ( shr.isLessThan(sharesFrom, Const.DIFF_TOLERANCE_VALUE ) ) {
				return false;
			}
		}
		
		if ( ! sharesTo.equals(UNSET_VALUE) ) {
			FixedPointNumber shr = splt.getShares();
			if ( sharesAbs && 
				 shr.compareTo(FixedPointNumber.ZERO) < 0 ) {
				shr.negate(); // mutable
			}
			
			if ( shr.isGreaterThan(sharesTo, Const.DIFF_TOLERANCE_VALUE ) ) {
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
	@Deprecated
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
