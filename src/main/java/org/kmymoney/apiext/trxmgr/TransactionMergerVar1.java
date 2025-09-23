package org.kmymoney.apiext.trxmgr;

import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.base.basetypes.simple.KMMTrxID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionMergerVar1 extends TransactionMergerBase
								   implements IFTransactionMerger 
{
    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionMergerVar1.class);
    
    // ---------------------------------------------------------------
	
	public TransactionMergerVar1(KMyMoneyWritableFile kmmFile) {
		super(kmmFile);
		setVar(Var.VAR_1);
	}
    
    // ---------------------------------------------------------------
    
	public void merge(KMMTrxID survivorID, KMMTrxID dierID) throws MergePlausiCheckException {
		KMyMoneyTransaction survivor = kmmFile.getTransactionByID(survivorID);
		KMyMoneyWritableTransaction dier = kmmFile.getWritableTransactionByID(dierID);
		merge(survivor, dier);
	}

	public void merge(KMyMoneyTransaction survivor, KMyMoneyWritableTransaction dier) throws MergePlausiCheckException {
		// 1) Perform plausi checks
		if ( ! plausiCheck(survivor, dier) ) {
			LOGGER.error("merge: survivor-dier-pair did not pass plausi check: " + survivor.getID() + "/" + dier.getID());
			throw new MergePlausiCheckException();
		}
		
		// 2) If OK, remove dier
		KMMTrxID dierID = dier.getID();
		kmmFile.removeTransaction(dier);
		LOGGER.info("merge: Transaction " + dierID + " (dier) removed");
	}

}
