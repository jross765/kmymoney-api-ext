package org.kmymoney.apiext.trxmgr;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.apache.commons.numbers.fraction.BigFraction;
import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.apiext.ConstTest;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;

import junit.framework.JUnit4TestAdapter;

public class TestTransactionSplitFilter_BF {
	
	public static final KMMQualifSpltID TRXSPLT_1_ID = new KMMQualifSpltID("T000000000000000017", "S0001");
	public static final KMMQualifSpltID TRXSPLT_2_ID = new KMMQualifSpltID("T000000000000000017", "S0003");
//	public static final KMMSpltID TRXSPLT_3_ID = new KMMSpltID("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

    private static final KMMAcctID ACCT_1_ID = TestTransactionFilter.ACCT_1_ID;
    private static final KMMAcctID ACCT_2_ID = TestTransactionFilter.ACCT_2_ID;
    private static final KMMAcctID ACCT_7_ID = TestTransactionFilter.ACCT_7_ID;
    private static final KMMAcctID ACCT_8_ID = TestTransactionFilter.ACCT_8_ID;

	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;
	private TransactionSplitFilter_BF flt = null;
	private KMyMoneyTransactionSplit splt = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestTransactionSplitFilter_BF.class);
	}

	@Before
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL kmmFileURL = classLoader.getResource(Const.KMM_FILENAME);
		// System.err.println("KMyMoney test file resource: '" + kmmFileURL + "'");
		URL kmmFileURL = null;
		File kmmFileRaw = null;
		try {
			kmmFileURL = classLoader.getResource(ConstTest.KMM_FILENAME);
			kmmFileRaw = new File(kmmFileURL.getFile());
		} catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			return;
		}

		try {
			kmmFile = new KMyMoneyFileImpl(kmmFileRaw);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney file");
			exc.printStackTrace();
		}
	}

	// -----------------------------------------------------------------

	@Test
	public void test01() throws Exception {
		flt = new TransactionSplitFilter_BF();
		flt.acctID.set(ACCT_1_ID);
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_1_ID);
		
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.acctID.set(ACCT_2_ID);
		assertEquals(false, flt.matchesCriteria(splt));
	}

	@Test
	public void test02_1() throws Exception {
		flt = new TransactionSplitFilter_BF();
		flt.acctID.set(ACCT_1_ID);
		flt.valueFrom = BigFraction.of(-196550, 100);
		flt.valueTo = BigFraction.of(-196550, 100);
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_1_ID);
		
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.valueFrom = BigFraction.of(-196551, 100);
		flt.valueTo = BigFraction.of(-196551, 100);
		assertEquals(false, flt.matchesCriteria(splt));
		
		flt.valueFrom = BigFraction.of(-196650, 100);
		flt.valueTo = BigFraction.of(-196450, 100);
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.valueFrom = BigFraction.of(-196450, 100);
		flt.valueTo = BigFraction.of(-196650, 100);
		assertEquals(false, flt.matchesCriteria(splt));
	}

	@Test
	public void test02_2() throws Exception {
		flt = new TransactionSplitFilter_BF();
		flt.acctID.set(ACCT_7_ID);
		flt.valueFrom = BigFraction.of(-1955);
		flt.valueTo = BigFraction.of(1955);
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_2_ID);
		
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.valueFrom = BigFraction.of(195501, 100);
		flt.valueTo = BigFraction.of(195501, 100);
		assertEquals(false, flt.matchesCriteria(splt));
		
		flt.valueFrom = BigFraction.of(195400, 100);
		flt.valueTo = BigFraction.of(195600, 100);
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.valueFrom = BigFraction.of(195500, 100);
		flt.valueTo = BigFraction.of(195300, 100);
		assertEquals(false, flt.matchesCriteria(splt));

		// CAUTION: No tolerance here, as opposed to FP variant
		flt.valueFrom = BigFraction.of(195499, 100);
		flt.valueTo = BigFraction.of(19549999, 10000);
		assertEquals(false, flt.matchesCriteria(splt));

		// CAUTION: No tolerance here, as opposed to FP variant
		flt.valueFrom = BigFraction.of(19550001, 10000);
		flt.valueTo = BigFraction.of(1956);
		assertEquals(false, flt.matchesCriteria(splt));
	}

	@Test
	public void test03_1() throws Exception {
		flt = new TransactionSplitFilter_BF();
		flt.acctID.set(ACCT_1_ID);
		flt.sharesFrom = BigFraction.of(-196550, 100);
		flt.sharesTo = BigFraction.of(-196550, 100);
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_1_ID);
		
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.sharesFrom = BigFraction.of(-196551, 100);
		flt.sharesTo = BigFraction.of(-196551, 100);
		assertEquals(false, flt.matchesCriteria(splt));
		
		flt.sharesFrom = BigFraction.of(-196650, 100);
		flt.sharesTo = BigFraction.of(-196450, 100);
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.sharesFrom = BigFraction.of(-196450, 100);
		flt.sharesTo = BigFraction.of(-196550, 100);
		assertEquals(false, flt.matchesCriteria(splt));
	}

	@Test
	public void test03_2() throws Exception {
		flt = new TransactionSplitFilter_BF();
		flt.acctID.set(ACCT_7_ID);
		flt.sharesFrom = BigFraction.of(17);
		flt.sharesTo = BigFraction.of(17);
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_2_ID);
		
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.sharesFrom = BigFraction.of(17);
		flt.sharesTo = BigFraction.of(1701, 100);
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.sharesFrom = BigFraction.of(1699, 100);
		flt.sharesTo = BigFraction.of(17);
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.sharesFrom = BigFraction.of(17);
		flt.sharesTo = BigFraction.of(1699, 100);
		assertEquals(false, flt.matchesCriteria(splt));
		
		// CAUTION: No tolerance here, as opposed to FP variant
		flt.sharesFrom = BigFraction.of(1699, 100);
		flt.sharesTo = BigFraction.of(169999, 10000);
		assertEquals(false, flt.matchesCriteria(splt));
		
		// CAUTION: No tolerance here, as opposed to FP variant
		flt.sharesFrom = BigFraction.of(170001, 10000);
		flt.sharesTo = BigFraction.of(18);
		assertEquals(false, flt.matchesCriteria(splt));
	}
	
	@Test
	public void test04() throws Exception {
		flt = new TransactionSplitFilter_BF();
		flt.acctID.set(ACCT_7_ID);
		flt.action = KMyMoneyTransactionSplit.Action.BUY_SHARES;
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_2_ID);
		
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.action = KMyMoneyTransactionSplit.Action.SELL_SHARES;
		assertEquals(false, flt.matchesCriteria(splt));
	}

	@Test
	public void test05() throws Exception {
		flt = new TransactionSplitFilter_BF();
		flt.acctID.set(ACCT_7_ID);
		flt.memoPart = ""; // sic, the TRANSACTION's description is set, not the SPLIT's one
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_2_ID);
		
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.memoPart = "Poop";
		assertEquals(false, flt.matchesCriteria(splt));
	}
}
