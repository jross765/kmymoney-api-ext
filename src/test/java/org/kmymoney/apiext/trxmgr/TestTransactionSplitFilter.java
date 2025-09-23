package org.kmymoney.apiext.trxmgr;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.apiext.ConstTest;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;

import junit.framework.JUnit4TestAdapter;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class TestTransactionSplitFilter {
	
	public static final KMMQualifSpltID TRXSPLT_1_ID = new KMMQualifSpltID("T000000000000000017", "S0001");
	public static final KMMQualifSpltID TRXSPLT_2_ID = new KMMQualifSpltID("T000000000000000017", "S0003");
//	public static final KMMSpltID TRXSPLT_3_ID = new KMMSpltID("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

    private static final KMMAcctID ACCT_1_ID = TestTransactionFilter.ACCT_1_ID;
    private static final KMMAcctID ACCT_2_ID = TestTransactionFilter.ACCT_2_ID;
    private static final KMMAcctID ACCT_7_ID = TestTransactionFilter.ACCT_7_ID;
    private static final KMMAcctID ACCT_8_ID = TestTransactionFilter.ACCT_8_ID;

	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;
	private TransactionSplitFilter flt = null;
	private KMyMoneyTransactionSplit splt = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestTransactionSplitFilter.class);
	}

	@Before
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL kmmFileURL = classLoader.getResource(Const.GCSH_FILENAME);
		// System.err.println("KMyMoney test file resource: '" + kmmFileURL + "'");
		InputStream kmmFileStream = null;
		try {
			kmmFileStream = classLoader.getResourceAsStream(ConstTest.KMM_FILENAME);
		} catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			return;
		}

		try {
			kmmFile = new KMyMoneyFileImpl(kmmFileStream);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney file");
			exc.printStackTrace();
		}
	}

	// -----------------------------------------------------------------

	@Test
	public void test01() throws Exception {
		flt = new TransactionSplitFilter();
		flt.acctID.set(ACCT_1_ID);
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_1_ID);
		
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.acctID.set(ACCT_2_ID);
		assertEquals(false, flt.matchesCriteria(splt));
	}

	@Test
	public void test02_1() throws Exception {
		flt = new TransactionSplitFilter();
		flt.acctID.set(ACCT_1_ID);
		flt.valueFrom = new FixedPointNumber("-1965.50");
		flt.valueTo = new FixedPointNumber("-1965.50");
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_1_ID);
		
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.valueFrom = new FixedPointNumber("-1965.51");
		flt.valueTo = new FixedPointNumber("-1965.51");
		assertEquals(false, flt.matchesCriteria(splt));
		
		flt.valueFrom = new FixedPointNumber("-1966.50");
		flt.valueTo = new FixedPointNumber("-1964.50");
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.valueFrom = new FixedPointNumber("-1964.50");
		flt.valueTo = new FixedPointNumber("-1966.50");
		assertEquals(false, flt.matchesCriteria(splt));
	}

	@Test
	public void test02_2() throws Exception {
		flt = new TransactionSplitFilter();
		flt.acctID.set(ACCT_7_ID);
		flt.valueFrom = new FixedPointNumber("-1955.00");
		flt.valueTo = new FixedPointNumber("1955.00");
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_2_ID);
		
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.valueFrom = new FixedPointNumber("1955.01");
		flt.valueTo = new FixedPointNumber("1955.01");
		assertEquals(false, flt.matchesCriteria(splt));
		
		flt.valueFrom = new FixedPointNumber("1954.00");
		flt.valueTo = new FixedPointNumber("1956.00");
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.valueFrom = new FixedPointNumber("1955.00");
		flt.valueTo = new FixedPointNumber("1953.00");
		assertEquals(false, flt.matchesCriteria(splt));

		// CAUTION: tolerance
		flt.valueFrom = new FixedPointNumber("1954.99");
		flt.valueTo = new FixedPointNumber("1954.9999");
		assertEquals(true, flt.matchesCriteria(splt)); // sic

		// CAUTION: tolerance
		flt.valueFrom = new FixedPointNumber("1955.0001");
		flt.valueTo = new FixedPointNumber("1956");
		assertEquals(true, flt.matchesCriteria(splt)); // sic
	}

	@Test
	public void test03_1() throws Exception {
		flt = new TransactionSplitFilter();
		flt.acctID.set(ACCT_1_ID);
		flt.sharesFrom = new FixedPointNumber("-1965.50");
		flt.sharesTo = new FixedPointNumber("-1965.50");
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_1_ID);
		
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.sharesFrom = new FixedPointNumber("-1965.51");
		flt.sharesTo = new FixedPointNumber("-1965.51");
		assertEquals(false, flt.matchesCriteria(splt));
		
		flt.sharesFrom = new FixedPointNumber("-1966.50");
		flt.sharesTo = new FixedPointNumber("-1964.50");
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.sharesFrom = new FixedPointNumber("-1964.50");
		flt.sharesTo = new FixedPointNumber("-1965.50");
		assertEquals(false, flt.matchesCriteria(splt));
	}

	@Test
	public void test03_2() throws Exception {
		flt = new TransactionSplitFilter();
		flt.acctID.set(ACCT_7_ID);
		flt.sharesFrom = new FixedPointNumber("17.0000");
		flt.sharesTo = new FixedPointNumber("17.0000");
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_2_ID);
		
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.sharesFrom = new FixedPointNumber("17.00");
		flt.sharesTo = new FixedPointNumber("17.01");
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.sharesFrom = new FixedPointNumber("16.99");
		flt.sharesTo = new FixedPointNumber("17.00");
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.sharesFrom = new FixedPointNumber("17.00");
		flt.sharesTo = new FixedPointNumber("16.99");
		assertEquals(false, flt.matchesCriteria(splt));
		
		// CAUTION: tolerance
		flt.sharesFrom = new FixedPointNumber("16.99");
		flt.sharesTo = new FixedPointNumber("16.9999");
		assertEquals(true, flt.matchesCriteria(splt)); // sic
		
		// CAUTION: tolerance
		flt.sharesFrom = new FixedPointNumber("17.0001");
		flt.sharesTo = new FixedPointNumber("18");
		assertEquals(true, flt.matchesCriteria(splt)); // sic
	}
	
	@Test
	public void test04() throws Exception {
		flt = new TransactionSplitFilter();
		flt.acctID.set(ACCT_7_ID);
		flt.action = KMyMoneyTransactionSplit.Action.BUY_SHARES;
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_2_ID);
		
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.action = KMyMoneyTransactionSplit.Action.SELL_SHARES;
		assertEquals(false, flt.matchesCriteria(splt));
	}

	@Test
	public void test05() throws Exception {
		flt = new TransactionSplitFilter();
		flt.acctID.set(ACCT_7_ID);
		flt.memoPart = ""; // sic, the TRANSACTION's description is set, not the SPLIT's one
		splt = kmmFile.getTransactionSplitByID(TRXSPLT_2_ID);
		
		assertEquals(true, flt.matchesCriteria(splt));
		
		flt.memoPart = "Poop";
		assertEquals(false, flt.matchesCriteria(splt));
	}
}
