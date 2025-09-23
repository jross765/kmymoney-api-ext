package org.kmymoney.apiext.trxmgr;

import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionMergerVar2 extends TransactionMergerBase
								   implements IFTransactionMerger 
{
    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionMergerVar2.class);
    
    // ---------------------------------------------------------------
    
	// CAUTION: 
	// In this merger variant, the survivor trx's bank split will be deleted
	// and vice versa.
	// ==> The zdTrxBankSpltID (ZD Splt), part of the dier trx, will die, 
	//     but a copy of it will survive as part of the survivor trx, 
	//     called ZS Splt/after.
	// Analogously, the dierBankTrxSplt (ZS Splt/before), part of the survivor trx, 
	// will die / be replaced by the above-mentioned copy ZS Splt/after.
	// 
	// Visualization of Typical Example:
	// ---------------------------------
	//
	// Dier Trx                            Survivor Trx
	// +-- XD Splt                         +-- XS Splt (to stock account)
	// |                                   +-- YS.a Splt (to expenses account)
	// |                                   +-- YS.b Splt (to expenses account)
	// |                                   +-- YS.c Splt (to income account)
	// +-- ZD Splt (to bank acct)          +-- ZS Splt/before (to bank account)
	//     ^                               |   ^
	//     +-- will be copied to           |   +-- Will be replaced by ZS Splt/after
	//         survivor trx -------------> +-- ZS Splt/after (to bank account)
	//                                         ^
	//                                         +-- Copy of ZD Splt
    //
	// Let that sink in for a moment before you review the code in this class.

	private KMyMoneyWritableTransaction survTrx = null;
	private KMyMoneyTransactionSplit zDierTrxBankSplt = null; // "ZS Split/before"
	private KMyMoneyTransactionSplit zSurvTrxBankSpltBefore = null; // "ZS Split/before"
	
	private KMMQualifSpltID zDierTrxBankSpltID = null;       // cf. above
	private KMMQualifSpltID zSurvTrxBankSpltBeforeID = null; // dto.

    // ---------------------------------------------------------------
	
	public TransactionMergerVar2(KMyMoneyWritableFile kmmFile) {
		super(kmmFile);
		setVar(Var.VAR_2);
	}
    
    // ---------------------------------------------------------------
	
	public KMMQualifSpltID getZDierTrxBankSpltID() {
		return zDierTrxBankSpltID;
	}
    
	public void setZDierTrxBankSpltID(KMMQualifSpltID spltID) {
		this.zDierTrxBankSpltID = spltID;
		
		zDierTrxBankSplt = kmmFile.getTransactionSplitByID(spltID);
	}
	
	// ---
    
	public KMMQualifSpltID getZSurvTrxBankSpltBeforeID() {
		return zSurvTrxBankSpltBeforeID;
	}
    
	public void setZSurvTrxBankSpltBeforeID(KMMQualifSpltID spltID) {
		this.zSurvTrxBankSpltBeforeID = spltID;
		
		zSurvTrxBankSpltBefore = kmmFile.getTransactionSplitByID(spltID);
	}
    
	// ---
    
	public KMyMoneyWritableTransaction getSurvTrx() {
		return survTrx;
	}
    
	public void setSurvTrx(KMyMoneyWritableTransaction trx) {
		this.survTrx = trx;
	}
    
    // ---------------------------------------------------------------
    
	public void merge(KMMTrxID survivorID, KMMTrxID dierID) throws MergePlausiCheckException {
		KMyMoneyTransaction survivor = kmmFile.getTransactionByID(survivorID);
		KMyMoneyWritableTransaction dier = kmmFile.getWritableTransactionByID(dierID);
		merge(survivor, dier);
	}

	public void merge(KMyMoneyTransaction survivor, KMyMoneyWritableTransaction dier) throws MergePlausiCheckException {
		if ( zDierTrxBankSpltID == null ) {
			throw new IllegalStateException("Z dier Trx bank Split ID is null");
		}
		
		if ( zSurvTrxBankSpltBeforeID == null ) {
			throw new IllegalStateException("Z survivor Trx bank Split (before) ID is null");
		}
		
		if ( survTrx == null ) {
			throw new IllegalStateException("Survivor Trx is null");
		}
		
		if ( ! zDierTrxBankSpltID.isSet() ) {
			throw new IllegalStateException("Z dier Trx bank Split ID is not set");
		}
		
		if ( ! zSurvTrxBankSpltBeforeID.isSet() ) {
			throw new IllegalStateException("Z survivor Trx bank Split (before) ID is not set");
		}
		
		if ( ! survTrx.getID().isSet() ) {
			throw new IllegalStateException("New bank Trx's ID is not set");
		}
		
		if ( zDierTrxBankSpltID.equals(zSurvTrxBankSpltBeforeID) ) {
			throw new IllegalStateException("IDs of Z dier Trx bank Split and Z survivor Trx bank Split (before) are identical");
		}
		
		// ---
		
		// 1) Perform plausi checks
		if ( ! plausiCheck(survivor, dier) ) {
			LOGGER.error("merge: survivor-dier-pair did not pass plausi check: " + survivor.getID() + "/" + dier.getID());
			throw new MergePlausiCheckException();
		}

		KMyMoneyWritableTransactionSplit zSurvBankTrxSpltAfter = copyBankTrxSplt();
		LOGGER.info("merge: Transaction Split " + zDierTrxBankSpltID + " copied to new Splt " + zSurvBankTrxSpltAfter.getID());
		
		KMyMoneyWritableTransactionSplit zSurvBankTrxSpltBefore = kmmFile.getWritableTransactionSplitByID(zSurvTrxBankSpltBeforeID);
		survTrx.remove(zSurvBankTrxSpltBefore);
		LOGGER.info("merge: Removed Transaction Split " + zSurvTrxBankSpltBeforeID);

		KMMID dierID = dier.getID();
		kmmFile.removeTransaction(dier);
		LOGGER.info("merge: Transaction " + dierID + " (dier) removed");
	}

    // ---------------------------------------------------------------
	
	private KMyMoneyWritableTransactionSplit copyBankTrxSplt() {
		KMyMoneyWritableTransactionSplit copy = survTrx.createWritableSplit(zDierTrxBankSplt.getAccount());

		if ( zDierTrxBankSplt.getAction() != null )
			copy.setAction(zDierTrxBankSplt.getAction());
		copy.setAccountID(zSurvTrxBankSpltBefore.getAccountID());
		copy.setValue(zDierTrxBankSplt.getValue().negate());
		copy.setShares(zDierTrxBankSplt.getShares().negate());
		copy.setMemo(zDierTrxBankSplt.getMemo());
		
		// User-defined attributes
		// ::TODO
//		for ( String attrKey : zdTrxBankSplt.getUserDefinedAttributeKeys() ) {
//			newBankTrxSplt.addUserDefinedAttribute( zdTrxBankSplt.getUserDefinedAttribute(attrKey) );
//		}
		
		return copy;
	}
}
