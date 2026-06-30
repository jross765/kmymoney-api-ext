package org.kmymoney.apiext.trxmgr;

import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

interface IFTransactionMerger {

	public void merge(KMMTrxID survivorID, KMMTrxID dierID) throws MergePlausiCheckException;

	public void merge(KMyMoneyWritableTransaction survivor, KMyMoneyWritableTransaction dier) throws MergePlausiCheckException;

}