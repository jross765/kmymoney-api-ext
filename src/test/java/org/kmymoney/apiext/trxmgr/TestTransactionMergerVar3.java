package org.kmymoney.apiext.trxmgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyTransactionImpl;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.apiext.ConstTest;
import org.kmymoney.base.basetypes.simple.KMMSpltID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

import junit.framework.JUnit4TestAdapter;

public class TestTransactionMergerVar3 {

	private static final KMMTrxID TRX_1_ID = new KMMTrxID("T000000000000000001");
	
	// ---

	private static final KMMTrxID TRX_BANK_1_ID  = new KMMTrxID("T000000000000015084");
	private static final KMMTrxID TRX_BANK_2_ID  = new KMMTrxID("T000000000000015085");
	private static final KMMTrxID TRX_STOCK_1_ID = new KMMTrxID("T000000000000015209");
	private static final KMMTrxID TRX_STOCK_2_ID = new KMMTrxID("T000000000000015210");

	// ---

//	private static final String TRX_BANK_1_STR  = """
//			<TRANSACTION id="T000000000000015084" postdate="2026-06-10" memo="Zins/Dividende" entrydate="2026-06-24" commodity="EUR">
//				<SPLITS>
//					<SPLIT id="S0001" payee="" reconciledate="" action="" reconcileflag="2" value="4127/100" shares="4127/100" price="0/1" memo="Zins/Dividende" account="A000060" number="" bankid="A000060-2026-06-10-7f8c8d0-1"/>
//				</SPLITS>
//			</TRANSACTION>
//			""";
//	private static final String TRX_BANK_2_STR  = """
//			<TRANSACTION id="T000000000000015085" postdate="2026-06-15" memo="Zins/Dividende" entrydate="2026-06-24" commodity="EUR">
//				<SPLITS>
//					<SPLIT id="S0001" payee="" reconciledate="" action="" reconcileflag="2" value="1629/100" shares="1629/100" price="0/1" memo="Zins/Dividende" account="A000060" number="" bankid="A000060-2026-06-15-2ef6fa0-1"/>
//				</SPLITS>
//			</TRANSACTION>
//			""";
//	private static final String TRX_STOCK_1_STR = """
//			<TRANSACTION id="T000000000000015209" postdate="2026-06-10" memo="" entrydate="2026-06-30" commodity="EUR">
//				<SPLITS>
//					<SPLIT id="S0001" payee="" reconciledate="" action="" reconcileflag="0" value="4127/100" shares="4127/100" price="0/1" memo="" account="A000060" number="" bankid=""/>
//					<SPLIT id="S0002" payee="" reconciledate="" action="Dividend" reconcileflag="0" value="0/1" shares="0/1" price="0/1" memo="" account="A000939" number="" bankid=""/>
//					<SPLIT id="S0003" payee="" reconciledate="" action="" reconcileflag="0" value="831/100" shares="831/100" price="1/1" memo="" account="A000324" number="" bankid=""/>
//					<SPLIT id="S0004" payee="" reconciledate="" action="" reconcileflag="0" value="111/20" shares="111/20" price="1/1" memo="" account="A000323" number="" bankid=""/>
//					<SPLIT id="S0005" payee="" reconciledate="" action="" reconcileflag="0" value="3/10" shares="3/10" price="1/1" memo="" account="A000321" number="" bankid=""/>
//					<SPLIT id="S0006" payee="" reconciledate="" action="" reconcileflag="0" value="-5543/100" shares="-5543/100" price="1/1" memo="" account="A000316" number="" bankid=""/>
//				</SPLITS>
//			</TRANSACTION>
//			""";
//	private static final String TRX_STOCK_2_STR = """
//			<TRANSACTION id="T000000000000015210" postdate="2026-06-15" memo="" entrydate="2026-06-30" commodity="EUR">
//				<SPLITS>
//					<SPLIT id="S0001" payee="" reconciledate="" action="" reconcileflag="0" value="1629/100" shares="1629/100" price="0/1" memo="" account="A000060" number="" bankid=""/>
//					<SPLIT id="S0002" payee="" reconciledate="" action="Dividend" reconcileflag="0" value="0/1" shares="0/1" price="0/1" memo="" account="A001002" number="" bankid=""/>
//					<SPLIT id="S0003" payee="" reconciledate="" action="" reconcileflag="0" value="82/25" shares="82/25" price="1/1" memo="" account="A000324" number="" bankid=""/>
//					<SPLIT id="S0004" payee="" reconciledate="" action="" reconcileflag="0" value="219/100" shares="219/100" price="1/1" memo="" account="A000323" number="" bankid=""/>
//					<SPLIT id="S0005" payee="" reconciledate="" action="" reconcileflag="0" value="3/25" shares="3/25" price="1/1" memo="" account="A000321" number="" bankid=""/>
//					<SPLIT id="S0006" payee="" reconciledate="" action="" reconcileflag="0" value="-547/25" shares="-547/25" price="1/1" memo="" account="A000316" number="" bankid=""/>
//					</SPLITS>
//			</TRANSACTION>
//			""";

	// from test data file 'test_mrg_3.kmy':
	private static final String MERGED_TRX_1_STR = """
			<TRANSACTION id="T000000000000015209" postdate="2026-06-10" memo="wulryvmdigjrxumdocyw" entrydate="2026-06-30" commodity="EUR">
				<SPLITS>
					<SPLIT id="S0001" payee="" reconciledate="" action="" reconcileflag="1" value="4127/100" shares="4127/100" price="0/1" memo="fdjfduajqqyanmjzlkyl&#10;Zins/Dividende" account="A000060" number="" bankid="A000060-2026-06-10-7f8c8d0-1">
						<KEYVALUEPAIRS>
							<PAIR key="kmm-match-split" value="S0001"/>
							<PAIR key="kmm-matched-tx" value="&#10;&amp;#60;!DOCTYPE MATCH&gt;&#10;&amp;#60;CONTAINER&gt;&#10;&amp;#60;TRANSACTION id=&quot;&quot; postdate=&quot;2026-06-10&quot; memo=&quot;Zins/Dividende XY1RM2AMEQG7&quot; entrydate=&quot;2026-06-24&quot; commodity=&quot;EUR&quot;&gt;&#10;&amp;#60;SPLITS&gt;&#10;&amp;#60;SPLIT id=&quot;S0001&quot; payee=&quot;&quot; reconciledate=&quot;&quot; action=&quot;&quot; reconcileflag=&quot;2&quot; value=&quot;4127/100&quot; shares=&quot;4127/100&quot; price=&quot;0/1&quot; memo=&quot;Zins/Dividende&quot; account=&quot;A000060&quot; number=&quot;&quot; bankid=&quot;A000060-2026-06-10-7f8c8d0-1&quot;/&gt;&#10;&amp;#60;/SPLITS&gt;&#10;&amp;#60;/TRANSACTION&gt;&#10;&amp;#60;/CONTAINER&gt;"/>
							<PAIR key="kmm-orig-memo" value="fdjfduajqqyanmjzlkyl"/>
							<PAIR key="kmm-orig-not-reconciled" value="yes"/>
						</KEYVALUEPAIRS>
					</SPLIT>
					<SPLIT id="S0002" payee="" reconciledate="" action="Dividend" reconcileflag="0" value="0/1" shares="0/1" price="0/1" memo="fdjfduajqqyanmjzlkyl&#10;Zins/Dividende" account="A000939" number="" bankid=""/>
					<SPLIT id="S0003" payee="" reconciledate="" action="" reconcileflag="0" value="831/100" shares="831/100" price="1/1" memo="fdjfduajqqyanmjzlkyl" account="A000324" number="" bankid=""/>
					<SPLIT id="S0004" payee="" reconciledate="" action="" reconcileflag="0" value="111/20" shares="111/20" price="1/1" memo="fdjfduajqqyanmjzlkyl" account="A000323" number="" bankid=""/>
					<SPLIT id="S0005" payee="" reconciledate="" action="" reconcileflag="0" value="3/10" shares="3/10" price="1/1" memo="fdjfduajqqyanmjzlkyl" account="A000321" number="" bankid=""/>
					<SPLIT id="S0006" payee="" reconciledate="" action="" reconcileflag="0" value="-5543/100" shares="-5543/100" price="1/1" memo="fdjfduajqqyanmjzlkyl" account="A000316" number="" bankid=""/>
				</SPLITS>
			</TRANSACTION>
			""";

	private static final String MERGED_TRX_1_MATCHED_TRX_STR = """
&#10;&amp;#60;!DOCTYPE MATCH&gt;&#10;&amp;#60;CONTAINER&gt;&#10;&amp;#60;TRANSACTION id=&quot;&quot; postdate=&quot;2026-06-10&quot; memo=&quot;Zins/Dividende&quot; entrydate=&quot;2026-06-24&quot; commodity=&quot;EUR&quot;&gt;&#10;&amp;#60;SPLITS&gt;&#10;&amp;#60;SPLIT id=&quot;S0001&quot; payee=&quot;&quot; reconciledate=&quot;&quot; action=&quot;&quot; reconcileflag=&quot;2&quot; value=&quot;4127/100&quot; shares=&quot;4127/100&quot; price=&quot;0/1&quot; memo=&quot;Zins/Dividende XY1RM2AMEQG7&quot; account=&quot;A000060&quot; number=&quot;&quot; bankid=&quot;A000060-2026-06-10-7f8c8d0-1&quot;/&gt;&#10;&amp;#60;/SPLITS&gt;&#10;&amp;#60;/TRANSACTION&gt;&#10;&amp;#60;/CONTAINER&gt;""";

	// -----------------------------------------------------------------

	private KMyMoneyWritableFile kmmFileStd = null;
	private KMyMoneyWritableFile kmmFileMrg = null;
	
	private KMyMoneyTransaction trx = null;

	private TransactionMergerVar3 mrgMgr = null;
	
	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestTransactionMergerVar3.class);
	}

	@Before
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL kmmFileURL = classLoader.getResource(Const.KMM_FILENAME);
		// System.err.println("KMyMoney test file resource: '" + kmmFileURL + "'");
		URL kmmFileURL = null;
		File kmmFileRaw = null;
		
		// ---
		
		try {
			kmmFileURL = classLoader.getResource(ConstTest.KMM_FILENAME);
			kmmFileRaw = new File(kmmFileURL.getFile());
		} catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			return;
		}

		try {
			kmmFileStd = new KMyMoneyWritableFileImpl(kmmFileRaw);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney file");
			exc.printStackTrace();
		}
		
		// ---
		
		try {
			kmmFileURL = classLoader.getResource(ConstTest.KMM_MRG_FILENAME);
			kmmFileRaw = new File(kmmFileURL.getFile());
		} catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			return;
		}

		try {
			kmmFileMrg = new KMyMoneyWritableFileImpl(kmmFileRaw);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney file");
			exc.printStackTrace();
		}
		
		// ---
		
		mrgMgr = new TransactionMergerVar3(kmmFileMrg);
	}

	// -----------------------------------------------------------------

	@Test
	public void test01_1() throws Exception {
		trx = kmmFileStd.getTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx);

		// System.err.println("'" + TransactionMergerVar3.getDierKMMContainerString(trx, false) + "'");
		assertEquals("""
				<!DOCTYPE MATCH><CONTAINER><TRANSACTION id="T000000000000000001" commodity="EUR" entrydate="2023-11-03" postdate="2023-01-01" memo="">    <SPLITS>        <SPLIT account="A000004" action="" bankid="" id="S0001" memo="" number="" payee="P000004" price="1/1" reconciledate="" reconcileflag="0" shares="10000/1" value="10000/1"/>        <SPLIT account="A000053" action="" bankid="" id="S0002" memo="" number="" payee="P000004" price="1/1" reconciledate="" reconcileflag="0" shares="-10000/1" value="-10000/1"/>    </SPLITS></TRANSACTION></CONTAINER>""", 
				TransactionMergerVar3.getDierKMMContainerString(trx, false));

		// System.err.println("'" + TransactionMergerVar3.getDierKMMContainerString(trx, true) + "'");
		assertEquals("""
				<!DOCTYPE MATCH><CONTAINER><TRANSACTION id="" commodity="EUR" entrydate="2023-11-03" postdate="2023-01-01" memo="">    <SPLITS>        <SPLIT account="A000004" action="" bankid="" id="S0001" memo="" number="" payee="P000004" price="1/1" reconciledate="" reconcileflag="0" shares="10000/1" value="10000/1"/>        <SPLIT account="A000053" action="" bankid="" id="S0002" memo="" number="" payee="P000004" price="1/1" reconciledate="" reconcileflag="0" shares="-10000/1" value="-10000/1"/>    </SPLITS></TRANSACTION></CONTAINER>""", 
				TransactionMergerVar3.getDierKMMContainerString(trx, true));
	}

	@Test
	public void test01_2() throws Exception {
		trx = kmmFileStd.getTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx);

		// System.err.println("'" + TransactionMergerVar3.getDierKMMContainerEscapedString(trx, false) + "'");
		assertEquals("&#10;&amp;#60;!DOCTYPE MATCH&gt;&#10;&amp;#60;CONTAINER&gt;&#10;&amp;#60;TRANSACTION id=&quot;T000000000000000001&quot; commodity=&quot;EUR&quot; entrydate=&quot;2023-11-03&quot; postdate=&quot;2023-01-01&quot; memo=&quot;&quot;&gt;&#10;    &amp;#60;SPLITS&gt;&#10;        &amp;#60;SPLIT account=&quot;A000004&quot; action=&quot;&quot; bankid=&quot;&quot; id=&quot;S0001&quot; memo=&quot;&quot; number=&quot;&quot; payee=&quot;P000004&quot; price=&quot;1/1&quot; reconciledate=&quot;&quot; reconcileflag=&quot;0&quot; shares=&quot;10000/1&quot; value=&quot;10000/1&quot;/&gt;&#10;        &amp;#60;SPLIT account=&quot;A000053&quot; action=&quot;&quot; bankid=&quot;&quot; id=&quot;S0002&quot; memo=&quot;&quot; number=&quot;&quot; payee=&quot;P000004&quot; price=&quot;1/1&quot; reconciledate=&quot;&quot; reconcileflag=&quot;0&quot; shares=&quot;-10000/1&quot; value=&quot;-10000/1&quot;/&gt;&#10;    &amp;#60;/SPLITS&gt;&#10;&amp;#60;/TRANSACTION&gt;&#10;&amp;#60;/CONTAINER&gt;", 
				TransactionMergerVar3.getDierKMMContainerEscapedString(trx, false));
		
		// System.err.println("'" + TransactionMergerVar3.getDierKMMContainerEscapedString(trx, false) + "'");
		assertEquals("&#10;&amp;#60;!DOCTYPE MATCH&gt;&#10;&amp;#60;CONTAINER&gt;&#10;&amp;#60;TRANSACTION id=&quot;&quot; commodity=&quot;EUR&quot; entrydate=&quot;2023-11-03&quot; postdate=&quot;2023-01-01&quot; memo=&quot;&quot;&gt;&#10;    &amp;#60;SPLITS&gt;&#10;        &amp;#60;SPLIT account=&quot;A000004&quot; action=&quot;&quot; bankid=&quot;&quot; id=&quot;S0001&quot; memo=&quot;&quot; number=&quot;&quot; payee=&quot;P000004&quot; price=&quot;1/1&quot; reconciledate=&quot;&quot; reconcileflag=&quot;0&quot; shares=&quot;10000/1&quot; value=&quot;10000/1&quot;/&gt;&#10;        &amp;#60;SPLIT account=&quot;A000053&quot; action=&quot;&quot; bankid=&quot;&quot; id=&quot;S0002&quot; memo=&quot;&quot; number=&quot;&quot; payee=&quot;P000004&quot; price=&quot;1/1&quot; reconciledate=&quot;&quot; reconcileflag=&quot;0&quot; shares=&quot;-10000/1&quot; value=&quot;-10000/1&quot;/&gt;&#10;    &amp;#60;/SPLITS&gt;&#10;&amp;#60;/TRANSACTION&gt;&#10;&amp;#60;/CONTAINER&gt;", 
				TransactionMergerVar3.getDierKMMContainerEscapedString(trx, true));
	}

	@Test
	public void test02() throws Exception {
		KMyMoneyWritableTransaction bankTrx1 = kmmFileMrg.getWritableTransactionByID(TRX_BANK_1_ID);
		assertNotEquals(null, bankTrx1);
		
		KMyMoneyWritableTransaction stockTrx1 = kmmFileMrg.getWritableTransactionByID(TRX_STOCK_1_ID);
		assertNotEquals(null, stockTrx1);
		
		KMyMoneyTransactionImpl refMrgdTrx1 = KMyMoneyTransactionImpl.fromXMLString(MERGED_TRX_1_STR, kmmFileMrg);
		assertNotEquals(null, refMrgdTrx1);
		
		// ---

//		System.err.println("tt1");
//		KMyMoneyTransactionImpl bankTrx1RO  = KMyMoneyTransactionImpl.fromXMLString(TRX_BANK_1_STR, kmmFile1);
//		System.err.println("tt1: " + bankTrx1RO);
//		for ( KMyMoneyTransactionSplit splt : bankTrx1RO.getSplits() ) {
//			System.err.println(" - " + splt);
//		}
//		System.err.println("tt2");
//		KMyMoneyTransactionImpl stockTrx1RO = KMyMoneyTransactionImpl.fromXMLString(TRX_STOCK_1_STR, kmmFile1);
//		
//		System.err.println("tt3");
//		KMyMoneyWritableTransactionImpl bankTrx1RW  = new KMyMoneyWritableTransactionImpl(bankTrx1RO);
//		System.err.println("tt4");
//		KMyMoneyWritableTransactionImpl stockTrx1RW = new KMyMoneyWritableTransactionImpl(stockTrx1RO);
		
		// ---

		mrgMgr.setZDierTrxBankSpltID( getBankSplit(bankTrx1).getQualifID() );
		mrgMgr.setZSurvTrxBankSpltBeforeID( getBankSplit(stockTrx1).getQualifID() );
		mrgMgr.merge( stockTrx1, bankTrx1 );
		
		assertEquals(refMrgdTrx1.getSplitsCount(), stockTrx1.getSplitsCount());

		test02_compareSplits(stockTrx1, refMrgdTrx1);

		// ::TODO
//		KMyMoneyTransactionSplit splt = refMrgdTrx1.getSplitByID(new KMMSpltID("S0001"));
//		System.err.println("zz: " + splt.getUserDefinedAttribute(TransactionMergerVar3.KMM_MATCHED_TX));
//		assertTrue( matchedTrxSoftEquals(
//						splt.getUserDefinedAttribute(TransactionMergerVar3.KMM_MATCHED_TX),
//						getBankSplit(stockTrx1).getUserDefinedAttribute(TransactionMergerVar3.KMM_MATCHED_TX) ) );
		
		// ::TODO
		// Naive one-to-one-comparison of the XML strings will not do,
		// similary to the above matched-string.
//		assertEquals(refMrgdTrx1.toXMLString(), stockTrx1.toXMLString());
	}

	private void test02_compareSplits(KMyMoneyWritableTransaction stockTrx1, KMyMoneyTransactionImpl refMrgdTrx1) {
		// Split 1
		KMMSpltID spltID = new KMMSpltID("S0001");
		KMyMoneyTransactionSplit refSplt = refMrgdTrx1.getSplitByID(spltID);
		KMyMoneyTransactionSplit actSplt = stockTrx1.getSplitByID(spltID);
		test02_compareSplits_baseAttrs(refSplt, actSplt);
		
		// Special thing. add. attributes of split no. 1 
		test02_compareSplilts_addAttrs(refSplt, actSplt);
		
		// Split 2
		spltID = new KMMSpltID("S0002");
		refSplt = refMrgdTrx1.getSplitByID(spltID);
		actSplt = stockTrx1.getSplitByID(spltID);
		test02_compareSplits_baseAttrs(refSplt, actSplt);
		
		// Split 3
		spltID = new KMMSpltID("S0003");
		refSplt = refMrgdTrx1.getSplitByID(spltID);
		actSplt = stockTrx1.getSplitByID(spltID);
		test02_compareSplits_baseAttrs(refSplt, actSplt);
		
		// Split 4
		spltID = new KMMSpltID("S0004");
		refSplt = refMrgdTrx1.getSplitByID(spltID);
		actSplt = stockTrx1.getSplitByID(spltID);
		test02_compareSplits_baseAttrs(refSplt, actSplt);
	
		// Split 5
		spltID = new KMMSpltID("S0005");
		refSplt = refMrgdTrx1.getSplitByID(spltID);
		actSplt = stockTrx1.getSplitByID(spltID);
		test02_compareSplits_baseAttrs(refSplt, actSplt);
		
		// Split 6
		spltID = new KMMSpltID("S0006");
		refSplt = refMrgdTrx1.getSplitByID(spltID);
		actSplt = stockTrx1.getSplitByID(spltID);
		test02_compareSplits_baseAttrs(refSplt, actSplt);
	}

	private void test02_compareSplits_baseAttrs(KMyMoneyTransactionSplit refSplt, KMyMoneyTransactionSplit actSplt) {
		assertEquals(refSplt.getAccountID(), actSplt.getAccountID());
		assertEquals(refSplt.getPayeeID(), actSplt.getPayee());
		assertEquals(refSplt.getSharesRat(), actSplt.getSharesRat());
		assertEquals(refSplt.getValueRat(), actSplt.getValueRat());
		// ::TODO
		// assertEquals(refSplt.getPriceRat(), actSplt.getPriceRat());
		// ::TODO
		// assertEquals(refSplt.getMemo(), stockSplt.getMemo());
		assertEquals(refSplt.getTagIDs().size(), actSplt.getTagIDs().size());
	}

	private void test02_compareSplilts_addAttrs(KMyMoneyTransactionSplit refSplt, KMyMoneyTransactionSplit actSplt) {
		assertEquals("S0001", actSplt.getID().toString());
		assertEquals("S0001", getBankSplit(actSplt.getTransaction()).getID().toString());
		
		assertEquals( refSplt.getUserDefinedAttribute(TransactionMergerVar3.KMM_MATCH_SPLIT),
					  actSplt.getUserDefinedAttribute(TransactionMergerVar3.KMM_MATCH_SPLIT) );

		// ::TODO
//		assertEquals( refSplt.getUserDefinedAttribute(TransactionMergerVar3.KMM_ORIG_PAYEE),
//					  actSplt.getUserDefinedAttribute(TransactionMergerVar3.KMM_ORIG_PAYEE) );

		// ::TODO
//		assertEquals( refSplt.getUserDefinedAttribute(TransactionMergerVar3.KMM_ORIG_NOT_RECONCILED),
//				  	  actSplt.getUserDefinedAttribute(TransactionMergerVar3.KMM_ORIG_NOT_RECONCILED) );

		// ::TODO
//		assertEquals( refSplt.getUserDefinedAttribute(TransactionMergerVar3.KMM_ORIG_MEMO),
//				  	  actSplt.getUserDefinedAttribute(TransactionMergerVar3.KMM_ORIG_MEMO) );

		// Special case: Matched-transaction string
		// This naive approach will not work:
//		assertEquals( refSplt.getUserDefinedAttribute(TransactionMergerVar3.KMM_MATCHED_TX),
//			  	  	  actSplt.getUserDefinedAttribute(TransactionMergerVar3.KMM_MATCHED_TX) );
		// Instead:
		assertTrue( matchedTrxSoftEquals(
				MERGED_TRX_1_MATCHED_TRX_STR,
				actSplt.getUserDefinedAttribute(TransactionMergerVar3.KMM_MATCHED_TX) ) );
	}
	
	// ---------------------------------------------------------------

	private KMyMoneyTransactionSplit getBankSplit(KMyMoneyTransaction trx) {
		for ( KMyMoneyTransactionSplit splt : trx.getSplits() ) {
			if ( splt.getAccount().getType() == KMyMoneyAccount.Type.CHECKING ) {
				return splt;
			}
		}
		
		return null;
	}
	
	// Instead: Hard
//	private KMMQualifSpltID getBankSplit(KMyMoneyTransaction trx) {
//		if ( trx.getID().equals(TRX_BANK_1_ID) )
//			return trx.getSplitByID(new KMMSpltID("S0001")).getQualifID();
//		else if ( trx.getID().equals(TRX_STOCK_1_ID) )
//			return trx.getSplitByID(new KMMSpltID("S0001")).getQualifID();
//		
//		return null; // Compiler happy
//	}


//	private void getSplitDataSurvivor(KMyMoneyTransaction survTrx) {
//		for ( KMyMoneyTransactionSplit splt : survTrx.getSplits() ) {
//			if ( splt.getAccount().getType() == KMyMoneyAccount.Type.STOCK ) {
//				stockAcct = splt.getAccount();
//			} else if ( splt.getAccount().getType() == KMyMoneyAccount.Type.CHECKING ) {
//				bankAcctSpltSurvBefore = splt;
//			}
//		}
//
//		System.out.println("Stock account (survivor/stock trx): " + stockAcct);
//		System.out.println("Bank transaction splt (survivor/stock trx): " + bankAcctSpltSurvBefore);
//	}

	// This helper:function and its sub-helpers:
	// I intentionally chose not to do a "real" parsing, but instead,
	// to decompose the string manually.
	// Reasons:
	//  - I do not really understand all details of the (de)composisiton
	//    (cf. "magic" in implementation)
	//  - Even if I understood all details: This goes beyong JAXB,
	//    and I have not (and don't need) a parser for this string.
	//    In fact, I do not really want to have anything to do with
	//    it beyond just generating it.
	private boolean matchedTrxSoftEquals(String str1, String str2) {
		// 1) Prefix
		String prefixStr1 = str1.replaceAll("TRANSACTION id=.*$", "");
		// System.err.println("prefix-str1: '" + prefixStr1 + "'");
		String prefixStr2 = str2.replaceAll("TRANSACTION id=.*$", "");
		if ( ! prefixStr1.equals(prefixStr2) ) {
			System.err.println("matchedTrxSoftEquals: Error 1");
			return false;
		}
		
		// 2) Suffix
		String suffixStr1 = str1.replaceAll("^.*/TRANSACTION", "");
		// System.err.println("suffix-str1: '" + suffixStr1 + "'");
		String suffixStr2 = str2.replaceAll("^.*/TRANSACTION", "");
		if ( ! suffixStr1.equals(suffixStr2) ) {
			System.err.println("matchedTrxSoftEquals: Error 2");
			return false;
		}
		
		// 2) Core
		int pos1 = str1.indexOf("TRANSACTION id=");
		int pos2 = str1.indexOf("/TRANSACTION");
		String coreStr1 = str1.substring(pos1, pos2 + "/TRANSACTION".length());

		pos1 = str2.indexOf("TRANSACTION id=");
		pos2 = str2.indexOf("/TRANSACTION");
		String coreStr2 = str2.substring(pos1, pos2 + "/TRANSACTION".length());

//		System.err.println("core-str1: '" + coreStr1 + "'");
//		System.err.println("core-str2: '" + coreStr2 + "'");
		if ( ! matchedTrxSoftEqualsCore(coreStr1, coreStr2) ) {
			System.err.println("matchedTrxSoftEquals: Error 3: ");
			System.err.println("  '" + coreStr1 + "'");
			System.err.println("  '" + coreStr2 + "'");
			return false;
		}

		return true;
	}
	
	private boolean matchedTrxSoftEqualsCore(String coreStr1, String coreStr2) {
		int pos1 = 0;
		int pos2 = coreStr1.indexOf("SPLITS");
		String trxStr1 = coreStr1.substring(pos1, pos2);
		trxStr1 = trxStr1.replaceAll("^TRANSACTION ", "");
		trxStr1 = trxStr1.replaceAll("\\s+", " "); // normalize: replace multiple white space with one single space
		trxStr1 = trxStr1.replaceAll("; ", "|");
		// trxStr1 = trxStr1.replaceAll(";&#10;&amp;#60;$", "");
		trxStr1 = trxStr1.replaceAll(";&gt;.*$", "");
		
		pos1 = 0;
		pos2 = coreStr2.indexOf("SPLITS");
		String trxStr2 = coreStr2.substring(pos1, pos2);
		trxStr2 = trxStr2.replaceAll("^TRANSACTION ", "");
		trxStr2 = trxStr2.replaceAll("\\s+", " "); // cf. above
		trxStr2 = trxStr2.replaceAll("; ", "|");
		// trxStr2 = trxStr2.replaceAll(";&#10;&amp;#60;$", "");
		trxStr2 = trxStr2.replaceAll(";&gt;.*$", "");
		
//		System.err.println("trx-str1: '" + trxStr1 + "'");
//		System.err.println("trx-str2: '" + trxStr2 + "'");
		
		// 1) Transaction w/o splits
		if ( ! matchedTrxSoftEqualsCoreTrx(trxStr1, trxStr2) ) {
			System.err.println("matchedTrxSoftEqualsCore: Error 1");
			System.err.println("  '" + trxStr1 + "'");
			System.err.println("  '" + trxStr2 + "'");
			return false;
		}
		
		pos1 = coreStr1.indexOf("SPLITS");
		pos2 = coreStr1.indexOf("/SPLITS");
		String splitsStr1 = coreStr1.substring(pos1, pos2 + "/SPLITS".length());
		
		// 1) Splits (plural)
		pos1 = coreStr2.indexOf("SPLITS");
		pos2 = coreStr2.indexOf("/SPLITS");
		String splitsStr2 = coreStr2.substring(pos1, pos2 + "/SPLITS".length());
		
//		System.err.println("splits-str1: '" + splitsStr1 + "'");
//		System.err.println("splits-str2: '" + splitsStr2 + "'");

		if ( ! matchedTrxSoftEqualsCoreMultSplt(splitsStr1, splitsStr2) ) {
			System.err.println("matchedTrxSoftEqualsCore: Error 1");
			System.err.println("  '" + splitsStr1 + "'");
			System.err.println("  '" + splitsStr2 + "'");
			return false;
		}
		
		return true;
	}
	
	// Decompose multi-split-string into several single-split strings
	// and check each of them individually.
	// Notice that currently, this is overkill: The test cases and -data are 
	// such that the multi-split-string effectively contains just one
	// single string anyway.
	private boolean matchedTrxSoftEqualsCoreTrx(String str1, String str2) {
		String[] arr1 = str1.split("\\|");
		String[] arr2 = str2.split("\\|");
		
		if ( arr1.length != arr2.length ) {
			System.err.println("matchedTrxSoftEqualsCoreTrx: Error 1: " + arr1.length + "/" + arr2.length);
			return false;
		}
		
		Arrays.sort(arr1);
		Arrays.sort(arr2);
		
		for ( int i = 0; i < arr1.length; i++ ) {
			if ( arr1[i].endsWith("&quot") )
				arr1[i] = arr1[i] + ";";
			if ( arr2[i].endsWith("&quot") )
				arr2[i] = arr2[i] + ";";
			
			if ( ! arr1[i].equals( arr2[i] ) ) {
				System.err.println("matchedTrxSoftEqualsCoreTrx: Error 2/arr[" + i + "]: '" + arr1[i] + "'/'" + arr2[i] + "'");
				System.err.println("-----");
				printArr("arr1", arr1);
				System.err.println("-----");
				printArr("arr2", arr2);
				return false;
			}
		}
		
		return true;
	}

	private boolean matchedTrxSoftEqualsCoreMultSplt(String str1, String str2) {
		str1 = str1.replaceAll("^SPLITS&gt;&#10;", "");
		str1 = str1.replaceAll("/SPLITS.*$", "");
		str2 = str2.replaceAll("^SPLITS&gt;&#10;", "");
		str2 = str2.replaceAll("/SPLITS.*$", "");
		
		String restStr1 = str1;
		String restStr2 = str2;
		while ( restStr1.length() > 0 &&
				restStr2.length() > 0 )
		{
//			System.err.println("--- iter ---");
//			System.err.println("rest1: '" + restStr1 + "'");
//			System.err.println("rest2: '" + restStr2 + "'");
//			System.err.println("------");
			int pos1Str1 = restStr1.indexOf("SPLIT");
			int pos2Str1 = restStr1.indexOf("/&gt;", pos1Str1);
			int pos1Str2 = restStr2.indexOf("SPLIT");
			int pos2Str2 = restStr2.indexOf("/&gt;", pos1Str2);
			
			if ( pos1Str1 >= 0 && pos2Str1 >= 0 &&
				 pos2Str1 >= 0 && pos2Str2 >= 0 )
			{
				String spltStr1 = restStr1.substring(pos1Str1, pos2Str1);
				spltStr1 = spltStr1.replaceAll("^SPLIT ", "");
				spltStr1 = spltStr1.replaceAll("\\s+", " "); // normalize: replace multiple white space with one single space
				spltStr1 = spltStr1.replaceAll("; ", "|");

				String spltStr2 = restStr2.substring(pos1Str2, pos2Str2);
				spltStr2 = spltStr2.replaceAll("^SPLIT ", "");
				spltStr2 = spltStr2.replaceAll("\\s+", " "); // normalize: replace multiple white space with one single space
				spltStr2 = spltStr2.replaceAll("; ", "|");

//				System.err.println("splt-str1: '" + spltStr1 + "'");
//				System.err.println("splt-str2: '" + spltStr2 + "'");
				
				if ( ! matchedTrxSoftEqualsCoreOneSplt(spltStr1, spltStr2) ) {
					System.err.println("matchedTrxSoftEqualsCoreMultSplt: Error 1:");
					System.err.println("  '" + spltStr1 + "'");
					System.err.println("  '" + spltStr2 + "'");
					return false;
				}
				
				restStr1 = restStr1.substring(pos2Str1 + "/&gt;".length());
				restStr2 = restStr2.substring(pos2Str2 + "/&gt;".length());
			}
			else
			{
				if ( pos1Str1 < 0 || pos2Str1 < 0 )
					restStr1 = "";
				
				if ( pos1Str2 < 0 || pos2Str2 < 0 )
					restStr2 = "";
			}
		} // while
		
		return true;
	}

	private boolean matchedTrxSoftEqualsCoreOneSplt(String str1, String str2) {
		String[] arr1 = str1.split("\\|");
		String[] arr2 = str2.split("\\|");
		
		if ( arr1.length != arr2.length ) {
			System.err.println("matchedTrxSoftEqualsCoreOneSplt: Error 1: " + arr1.length + "/" + arr2.length);
			return false;
		}
		
		Arrays.sort(arr1);
		Arrays.sort(arr2);
		
		for ( int i = 0; i < arr1.length; i++ ) {
			if ( arr1[i].endsWith("&quot") )
				arr1[i] = arr1[i] + ";";
			if ( arr2[i].endsWith("&quot") )
				arr2[i] = arr2[i] + ";";
			
			if ( ! arr1[i].equals( arr2[i] ) ) {
				System.err.println("matchedTrxSoftEqualsCoreOneSplt: Error 2/arr[" + i + "]: '" + arr1[i] + "'/'" + arr2[i] + "'");
				System.err.println("-----");
				printArr("arr1", arr1);
				System.err.println("-----");
				printArr("arr2", arr2);
				return false;
			}
		}
		
		return true;
	}

	private void printArr(String arrName, String[] arr) {
		for ( int i = 0; i < arr.length; i++ ) {
			System.err.println(arrName + "[" + i + "]: '" + arr[i] + "'"); 
		}
	}

}
