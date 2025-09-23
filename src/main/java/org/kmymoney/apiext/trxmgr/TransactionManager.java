package org.kmymoney.apiext.trxmgr;

import java.util.ArrayList;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.apiext.Const;
import org.kmymoney.base.basetypes.simple.KMMTrxID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class TransactionManager {
	
    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManager.class);
    
    // ---------------------------------------------------------------
    
	private KMyMoneyWritableFile kmmFile = null;
	
    // ---------------------------------------------------------------
	
	public TransactionManager(KMyMoneyWritableFile kmmFile) {
		this.kmmFile = kmmFile;
	}
    
    // ---------------------------------------------------------------
    
	public boolean isSane(KMMTrxID trxID) {
		KMyMoneyTransaction trx = kmmFile.getTransactionByID(trxID);
		return isSane(trx);
	}

	public boolean isSane(KMyMoneyTransaction trx) {
		if ( trx.getSplits().size() == 0 )
			return false;
		
		FixedPointNumber sum = new FixedPointNumber();
		for ( KMyMoneyTransactionSplit splt : trx.getSplits() ) {
			sum.add(splt.getValue());
		}
		
		if ( sum.abs().doubleValue() > Const.DIFF_TOLERANCE_VALUE ) {
			LOGGER.warn("isSane: abs. value of sum greater than tolerance: " + sum);
			return false;
		}
		
		return true;
	}
	
	public boolean hasSplitBoundToAccounttType(KMyMoneyTransaction trx, KMyMoneyAccount.Type acctType) {
		for ( KMyMoneyTransactionSplit splt : trx.getSplits() ) {
			if ( splt.getAccount().getType() == acctType )
				return true;
		}
		
		return false;
	}

	public ArrayList<KMyMoneyTransactionSplit> getSplitsBoundToAccounttType(KMyMoneyTransaction trx, KMyMoneyAccount.Type acctType) {
		ArrayList<KMyMoneyTransactionSplit> result = new ArrayList<KMyMoneyTransactionSplit>();
		
		for ( KMyMoneyTransactionSplit splt : trx.getSplits() ) {
			if ( splt.getAccount().getType() == acctType )
				result.add(splt);
		}
		
		return result;
	}
}
