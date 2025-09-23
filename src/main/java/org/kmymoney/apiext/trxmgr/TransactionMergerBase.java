package org.kmymoney.apiext.trxmgr;

import java.util.ArrayList;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.apiext.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.dateutils.JulianDate;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public abstract class TransactionMergerBase {
	
	public enum Var {
		VAR_1,
		VAR_2
	}
	
    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionMergerBase.class);
    
    // ---------------------------------------------------------------
    
	protected KMyMoneyWritableFile kmmFile = null;
	private   Var                 var      = null;
	
    // ---------------------------------------------------------------
	
	public TransactionMergerBase(KMyMoneyWritableFile kmmFile) {
		this.kmmFile = kmmFile;
	}
    
    // ---------------------------------------------------------------
	
	public Var getVar() {
		return var;
	}
	
	public void setVar(Var var) {
		this.var = var;
	}
	
    // ---------------------------------------------------------------
	
	public boolean plausiCheck(KMyMoneyTransaction survivor, KMyMoneyTransaction dier) {
		// Level 1:
		double survDateFromJul = 0.0;
		double dierDateToJul   = 0.0;
		try {
			survDateFromJul = JulianDate.toJulian(survivor.getDatePosted());
			dierDateToJul   = JulianDate.toJulian(dier.getDatePosted());
		} catch ( Exception exc ) {
			// pro forma
			exc.printStackTrace();
		}
		
		if ( Math.abs( survDateFromJul - dierDateToJul ) > Const.DIFF_TOLERANCE_DAYS ) {
			LOGGER.warn("plausiCheck: Survivor- and dier-transaction do not have the same post-date");
			LOGGER.debug("plausiCheck: Survivor-date: " + survivor.getDatePosted());
			LOGGER.debug("plausiCheck: Dier-date: " + dier.getDatePosted());
			return false;
		}

		TransactionManager trxMgr = new TransactionManager(kmmFile);
		if ( ! trxMgr.isSane(survivor) ) {
			LOGGER.warn("plausiCheck: Survivor-transaction is not sane");
			return false;
		}
		
		if ( ! trxMgr.isSane(dier) ) {
			LOGGER.warn("plausiCheck: Dier-transaction is not sane");
			return false;
		}
		
		if ( ! ( trxMgr.hasSplitBoundToAccounttType(survivor, KMyMoneyAccount.Type.CHECKING) &&
			     trxMgr.hasSplitBoundToAccounttType(dier, KMyMoneyAccount.Type.CHECKING) 
			     ||
			     trxMgr.hasSplitBoundToAccounttType(survivor, KMyMoneyAccount.Type.CASH) &&
			     trxMgr.hasSplitBoundToAccounttType(dier, KMyMoneyAccount.Type.CASH)
			     ||
			     trxMgr.hasSplitBoundToAccounttType(survivor, KMyMoneyAccount.Type.STOCK) &&
			     trxMgr.hasSplitBoundToAccounttType(dier, KMyMoneyAccount.Type.STOCK) 
			   ) ) {
			LOGGER.warn("plausiCheck: One or both transactions has/have no split belonging to bank/cash/stock account");
			return false;
		}
		
		// Level 2:
		// Splits belong to the same accounts -- per account type
		ArrayList<KMyMoneyTransactionSplit> spltListSurvBank = trxMgr.getSplitsBoundToAccounttType(dier, KMyMoneyAccount.Type.CHECKING);
		ArrayList<KMyMoneyTransactionSplit> spltListDierBank = trxMgr.getSplitsBoundToAccounttType(dier, KMyMoneyAccount.Type.CHECKING);
		if ( trxMgr.hasSplitBoundToAccounttType(survivor, KMyMoneyAccount.Type.CHECKING) &&
			 trxMgr.hasSplitBoundToAccounttType(dier, KMyMoneyAccount.Type.CHECKING) ) {
			spltListSurvBank = trxMgr.getSplitsBoundToAccounttType(dier, KMyMoneyAccount.Type.CHECKING);
			spltListDierBank = trxMgr.getSplitsBoundToAccounttType(dier, KMyMoneyAccount.Type.CHECKING);
			
			for ( KMyMoneyTransactionSplit spltSurv : spltListSurvBank ) {
				boolean accountInBothLists = false;
				
				for ( KMyMoneyTransactionSplit spltDier : spltListDierBank ) {
					if ( spltSurv.getAccount().getID().equals(spltDier.getAccount().getID() ) ) {
						accountInBothLists = true;
					}
				}
				
				if ( ! accountInBothLists ) {
					LOGGER.warn("plausiCheck: Survivor-split " + spltSurv.getID() + " has no according dier-split sibling (bank accounts)");
					return false;
				}
			}
		} 

		// sic, no else-if!
		ArrayList<KMyMoneyTransactionSplit> spltListSurvCash = trxMgr.getSplitsBoundToAccounttType(dier, KMyMoneyAccount.Type.CASH);
		ArrayList<KMyMoneyTransactionSplit> spltListDierCash = trxMgr.getSplitsBoundToAccounttType(dier, KMyMoneyAccount.Type.CASH);
		if ( trxMgr.hasSplitBoundToAccounttType(survivor, KMyMoneyAccount.Type.CASH) &&
			 trxMgr.hasSplitBoundToAccounttType(dier, KMyMoneyAccount.Type.CASH) ) {
			spltListSurvCash = trxMgr.getSplitsBoundToAccounttType(dier, KMyMoneyAccount.Type.CASH);
			spltListDierCash = trxMgr.getSplitsBoundToAccounttType(dier, KMyMoneyAccount.Type.CASH);
			
			for ( KMyMoneyTransactionSplit spltSurv : spltListSurvCash ) {
				boolean accountInBothLists = false;
				
				for ( KMyMoneyTransactionSplit spltDier : spltListDierCash ) {
					if ( spltSurv.getAccount().getID().equals(spltDier.getAccount().getID() ) ) {
						accountInBothLists = true;
					}
				}
				
				if ( ! accountInBothLists ) {
					LOGGER.warn("plausiCheck: Survivor-split " + spltSurv.getID() + " has no according dier-split sibling (cash accounts)");
					return false;
				}
			}
		}
		
		// sic, no else-if!
		ArrayList<KMyMoneyTransactionSplit> spltListSurvStock = trxMgr.getSplitsBoundToAccounttType(dier, KMyMoneyAccount.Type.STOCK);
		ArrayList<KMyMoneyTransactionSplit> spltListDierStock = trxMgr.getSplitsBoundToAccounttType(dier, KMyMoneyAccount.Type.STOCK);
		if ( trxMgr.hasSplitBoundToAccounttType(survivor, KMyMoneyAccount.Type.STOCK) &&
			 trxMgr.hasSplitBoundToAccounttType(dier, KMyMoneyAccount.Type.STOCK) ) {
			spltListSurvStock = trxMgr.getSplitsBoundToAccounttType(dier, KMyMoneyAccount.Type.STOCK);
			spltListDierStock = trxMgr.getSplitsBoundToAccounttType(dier, KMyMoneyAccount.Type.STOCK);
			
			for ( KMyMoneyTransactionSplit spltSurv : spltListSurvStock ) {
				boolean accountInBothLists = false;
				
				for ( KMyMoneyTransactionSplit spltDier : spltListDierStock ) {
					if ( spltSurv.getAccount().getID().equals(spltDier.getAccount().getID() ) ) {
						accountInBothLists = true;
					}
				}
				
				if ( ! accountInBothLists ) {
					LOGGER.warn("plausiCheck: Survivor-split " + spltSurv.getID() + " has no according dier-split sibling (stock accounts)");
					return false;
				}
			}
		}
		
		// Level 3:
		// Split values are identical
		FixedPointNumber sumSurv = new FixedPointNumber();
		for ( KMyMoneyTransactionSplit elt : spltListSurvBank ) {
			sumSurv = sumSurv.add(elt.getValue());
		}
		
		FixedPointNumber sumDier = new FixedPointNumber();
		for ( KMyMoneyTransactionSplit elt : spltListDierBank ) {
			sumDier = sumDier.add(elt.getValue());
		}
		
		if ( Math.abs( sumSurv.getBigDecimal().doubleValue() - 
				       sumDier.getBigDecimal().doubleValue() ) > Const.DIFF_TOLERANCE_VALUE ) {
			LOGGER.warn("plausiCheck: Split-sums over survivor- and dier-splits are unequal (bank accounts)");
			LOGGER.debug("plausiCheck: sumSurv: " + sumSurv.getBigDecimal());
			LOGGER.debug("plausiCheck: sumDier: " + sumDier.getBigDecimal());
			return false;
		}
		
		// ---
		
		sumSurv = new FixedPointNumber();
		for ( KMyMoneyTransactionSplit elt : spltListSurvCash ) {
			sumSurv = sumSurv.add(elt.getValue());
		}
		
		sumDier = new FixedPointNumber();
		for ( KMyMoneyTransactionSplit elt : spltListDierCash ) {
			sumDier = sumDier.add(elt.getValue());
		}
		
		if ( Math.abs( sumSurv.getBigDecimal().doubleValue() - 
				       sumDier.getBigDecimal().doubleValue() ) > Const.DIFF_TOLERANCE_VALUE ) {
			LOGGER.warn("plausiCheck: Split-sums over survivor- and dier-splits are unequal (cash accounts)");
			LOGGER.debug("plausiCheck: sumSurv: " + sumSurv.getBigDecimal());
			LOGGER.debug("plausiCheck: sumDier: " + sumDier.getBigDecimal());
			return false;
		}
		
		// ---
		
		sumSurv = new FixedPointNumber();
		for ( KMyMoneyTransactionSplit elt : spltListSurvStock ) {
			sumSurv = sumSurv.add(elt.getValue());
		}
		
		sumDier = new FixedPointNumber();
		for ( KMyMoneyTransactionSplit elt : spltListDierStock ) {
			sumDier = sumDier.add(elt.getValue());
		}
		
		if ( Math.abs( sumSurv.getBigDecimal().doubleValue() - 
				       sumDier.getBigDecimal().doubleValue() ) > Const.DIFF_TOLERANCE_VALUE ) {
			LOGGER.warn("plausiCheck: Split-sums over survivor- and dier-splits are unequal (stock accounts)");
			LOGGER.debug("plausiCheck: sumSurv: " + sumSurv.getBigDecimal());
			LOGGER.debug("plausiCheck: sumDier: " + sumDier.getBigDecimal());
			return false;
		}
		
		return true;
	}
    
}
