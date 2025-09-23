package org.kmymoney.apiext.trxmgr;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.apiext.ConstTest;
import org.kmymoney.apiext.trxmgr.TransactionFilter.SplitLogic;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

import junit.framework.JUnit4TestAdapter;

public class TestTransactionFilter {
	
	public static final KMMTrxID TRX_1_ID = new KMMTrxID("T000000000000000017");
	// public static final KMMTrxID TRX_2_ID = new KMMTrxID("xxx");

    public static final KMMAcctID ACCT_1_ID = new KMMAcctID("A000004"); // Anlagen:Barvermögen:Giro RaiBa
    public static final KMMAcctID ACCT_2_ID = new KMMAcctID("A000062"); // Anlagen:Finanzanlagen:Depot RaiBa
    public static final KMMAcctID ACCT_7_ID = new KMMAcctID("A000064"); // Anlagen:Finanzanlagen:Depot RaiBa:DE0007100000 Mercedes-Benz
    public static final KMMAcctID ACCT_8_ID = new KMMAcctID("A000063"); // Anlagen:Finanzanlagen:Depot RaiBa:DE0007164600 SAP

	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;
	private TransactionFilter flt = null;
	private KMyMoneyTransaction trx = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestTransactionFilter.class);
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
		flt = new TransactionFilter();
		flt.spltFilt.acctID.set(ACCT_1_ID);
		trx = kmmFile.getTransactionByID(TRX_1_ID);
		
		assertEquals(true, flt.matchesCriteria(trx, false, SplitLogic.OR)); // AND works as well, because of 2nd arg.
		assertEquals(false, flt.matchesCriteria(trx, true, SplitLogic.AND));
		assertEquals(true, flt.matchesCriteria(trx, true, SplitLogic.OR));
		
		flt.spltFilt.acctID.set(ACCT_2_ID);
		assertEquals(true, flt.matchesCriteria(trx, false, SplitLogic.OR)); // sic, splits not checked, thus acct-ID not checked
		assertEquals(false, flt.matchesCriteria(trx, true, SplitLogic.AND));
		assertEquals(false, flt.matchesCriteria(trx, true, SplitLogic.OR));
	}

	@Test
	public void test02() throws Exception {
		flt = new TransactionFilter();
		flt.spltFilt.acctID.set(ACCT_1_ID);
		flt.datePostedFrom = LocalDate.of(2023, 10, 27);
		flt.datePostedTo = LocalDate.of(2023, 10, 27);
		trx = kmmFile.getTransactionByID(TRX_1_ID);
		
		assertEquals(true, flt.matchesCriteria(trx, true, SplitLogic.OR));
		
		flt.datePostedFrom = LocalDate.of(2023, 9, 20);
		flt.datePostedTo = LocalDate.of(2023, 10, 27);
		assertEquals(true, flt.matchesCriteria(trx, true, SplitLogic.OR));
		
		flt.datePostedFrom = LocalDate.of(2023, 10, 27);
		flt.datePostedTo = LocalDate.of(2023, 10, 30);
		assertEquals(true, flt.matchesCriteria(trx, true, SplitLogic.OR));
		
		flt.datePostedFrom = LocalDate.of(2023, 10, 30);
		flt.datePostedTo = LocalDate.of(2023, 10, 27);
		assertEquals(false, flt.matchesCriteria(trx, true, SplitLogic.OR));
	}

	/* Doesn't work like that in KMM, as opposed to sister project
	@Test
	public void test03() throws Exception {
		flt = new TransactionFilter();
		flt.spltFilt.acctID.set(ACCT_1_ID);
		flt.memoPart = "MBG";
		trx = kmmFile.getTransactionByID(TRX_1_ID);
		
		assertEquals(true, flt.matchesCriteria(trx, true, SplitLogic.OR));
		
		flt.memoPart = "SAP";
		assertEquals(false, flt.matchesCriteria(trx, true, SplitLogic.OR));
	}
	*/

	@Test
	public void test04() throws Exception {
		flt = new TransactionFilter();
		flt.spltFilt.acctID.set(ACCT_1_ID);
		flt.nofSpltFrom = 1;
		flt.nofSpltTo = 10;
		trx = kmmFile.getTransactionByID(TRX_1_ID);
		
		assertEquals(true, flt.matchesCriteria(trx, true, SplitLogic.OR));
		
		flt.nofSpltFrom = 10;
		flt.nofSpltTo = 1;
		assertEquals(false, flt.matchesCriteria(trx, true, SplitLogic.OR));
		
		flt.nofSpltFrom = 3;
		flt.nofSpltTo = 3;
		assertEquals(true, flt.matchesCriteria(trx, true, SplitLogic.OR));
	}
}
