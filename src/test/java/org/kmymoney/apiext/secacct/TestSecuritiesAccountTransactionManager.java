package org.kmymoney.apiext.secacct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.apiext.ConstTest;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;
import org.kmymoney.base.tuples.AcctIDAmountPair;

import junit.framework.JUnit4TestAdapter;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class TestSecuritiesAccountTransactionManager {

	private static KMMAcctID STOCK_ACCT_ID  = new KMMAcctID("A000063");
	private static KMMAcctID INCOME_ACCT_ID = new KMMAcctID("A000070"); // only for dividend, not for
																		// buy/sell
	private static List<AcctIDAmountPair> EXPENSES_ACCT_AMT_LIST = new ArrayList<AcctIDAmountPair>(); // only for dividend,
																									  // not for buy/sell
	private static KMMAcctID OFFSET_ACCT_ID = new KMMAcctID("A000004");

	private static FixedPointNumber NOF_STOCKS = new FixedPointNumber(15); // only for buy/sell, not for dividend
	private static FixedPointNumber STOCK_PRC  = new FixedPointNumber("23080/100"); // only for buy/sell, not for dividend
	private static FixedPointNumber DIV_GROSS  = new FixedPointNumber("11223/100"); // only for dividend, not for buy/sell

	private static LocalDate DATE_POSTED = LocalDate.of(2024, 3, 1);
	private static String DESCR = "Dividend payment";

	// ----------------------------

	private static KMMAcctID STOCK_BUY_EXP_ACCT_1_ID = new KMMAcctID( "A000073" ); // Bankprovision

	FixedPointNumber STOCK_BUY_EXP_1 = new FixedPointNumber("945/100");
	
	// ----------------------------

	private static KMMAcctID DIVIDEND_EXP_ACCT_1_ID = new KMMAcctID( "A000067" ); // Kapitalertragsteuer
	private static KMMAcctID DIVIDEND_EXP_ACCT_2_ID = new KMMAcctID( "A000027" ); // Soli

	FixedPointNumber DIVIDEND_EXP_1 = DIV_GROSS.copy().multiply(new FixedPointNumber("25/100"));
	FixedPointNumber DIVIDEND_EXP_2 = STOCK_BUY_EXP_1.copy().multiply(new FixedPointNumber("55/100"));
	
	// -----------------------------------------------------------------

	private KMyMoneyWritableFileImpl gcshInFile = null;
	private KMyMoneyFileImpl gcshOutFile = null;

	private KMMTrxID newTrxID = null;

	// https://stackoverflow.com/questions/11884141/deleting-file-and-directory-in-junit
	@SuppressWarnings("exports")
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestSecuritiesAccountTransactionManager.class);
	}

	@Before
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL gcshFileURL = classLoader.getResource(Const.KMM_FILENAME);
		// System.err.println("KMyMoney test file resource: '" + gcshFileURL + "'");
		InputStream gcshInFileStream = null;
		try {
			gcshInFileStream = classLoader.getResourceAsStream(ConstTest.KMM_FILENAME_IN);
		} catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			return;
		}

		try {
			gcshInFile = new KMyMoneyWritableFileImpl(gcshInFileStream);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney in-file");
			exc.printStackTrace();
		}
		
		// ---
		
		newTrxID = new KMMTrxID();
	}

	@Test
	public void test01() throws Exception {
		test01_initExpAccts();

		KMyMoneyWritableTransaction trx = 
				SecuritiesAccountTransactionManager
					.genBuyStockTrx(gcshInFile, 
									STOCK_ACCT_ID, EXPENSES_ACCT_AMT_LIST, OFFSET_ACCT_ID,
									NOF_STOCKS, STOCK_PRC, 
									DATE_POSTED, DESCR);
		assertNotEquals(null, trx);
		newTrxID.set(trx.getID());

		// ----------------------------
		// Now, check whether the generated object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
							// and the KMyMoney file writer does not like that.
		gcshInFile.writeFile(outFile);

		test01_check_persisted(outFile);
	}

	private void test01_check_persisted(File outFile) throws Exception {
		gcshOutFile = new KMyMoneyFileImpl(outFile);

		KMyMoneyTransaction trx = gcshOutFile.getTransactionByID(newTrxID);
		assertNotEquals(null, trx);

		assertEquals(DATE_POSTED, trx.getDatePosted());
		assertEquals(0, trx.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(3, trx.getSplits().size());
		assertEquals(true, trx.getMemo().startsWith("Generated by SecuritiesAccountTransactionManager"));

		// ---

		KMyMoneyTransactionSplit splt1 = null;
		for ( KMyMoneyTransactionSplit splt : trx.getSplits() ) {
			if ( splt.getAccountID().equals(new KMMComplAcctID(STOCK_ACCT_ID)) ) {
				splt1 = splt;
				break;
			}
		}
		assertNotEquals(null, splt1);
		
		KMyMoneyTransactionSplit splt2 = null;
		for ( KMyMoneyTransactionSplit splt : trx.getSplits() ) {
			if ( splt.getAccountID().equals(new KMMComplAcctID(OFFSET_ACCT_ID)) ) {
				splt2 = splt;
				break;
			}
		}
		assertNotEquals(null, splt2);
		
		KMyMoneyTransactionSplit splt3 = null;
		for ( KMyMoneyTransactionSplit splt : trx.getSplits() ) {
			if ( splt.getAccountID().equals(new KMMComplAcctID(STOCK_BUY_EXP_ACCT_1_ID)) ) {
				splt3 = splt;
				break;
			}
		}
		assertNotEquals(null, splt3);
		
		// ---

		FixedPointNumber amtNet   = NOF_STOCKS.copy().multiply(STOCK_PRC);
		FixedPointNumber amtGross = amtNet.copy();
		for ( AcctIDAmountPair elt : EXPENSES_ACCT_AMT_LIST ) {
		    amtGross.add(elt.amount());
		}
		
		assertEquals(STOCK_ACCT_ID, splt1.getAccountID().getStdID());
		assertEquals(KMyMoneyTransactionSplit.Action.BUY_SHARES, splt1.getAction());
		assertEquals(NOF_STOCKS.doubleValue(), splt1.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(amtNet.doubleValue(), splt1.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("", splt1.getMemo());

		assertEquals(OFFSET_ACCT_ID, splt2.getAccountID().getStdID());
		assertEquals(null, splt2.getAction());
		assertEquals(amtGross.copy().negate().doubleValue(), splt2.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(amtGross.copy().negate().doubleValue(), splt2.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(DESCR, splt2.getMemo());

		assertEquals(STOCK_BUY_EXP_ACCT_1_ID, splt3.getAccountID().getStdID());
		assertEquals(null, splt3.getAction());
		assertEquals(STOCK_BUY_EXP_1.doubleValue(), splt3.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(STOCK_BUY_EXP_1.doubleValue(), splt3.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("", splt3.getMemo());
	}

	@Test
	public void test02() throws Exception {
		test02_initExpAccts();

		KMyMoneyWritableTransaction trx = 
				SecuritiesAccountTransactionManager
					.genDividDistribTrx(gcshInFile, 
									STOCK_ACCT_ID, INCOME_ACCT_ID, EXPENSES_ACCT_AMT_LIST, OFFSET_ACCT_ID,
									KMyMoneyTransactionSplit.Action.DIVIDEND, DIV_GROSS, 
									DATE_POSTED, DESCR);
		assertNotEquals(null, trx);
		newTrxID.set(trx.getID());

		// ----------------------------
		// Now, check whether the generated object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
							// and the KMyMoney file writer does not like that.
		gcshInFile.writeFile(outFile);

		test02_check_persisted(outFile);
	}

	private void test02_check_persisted(File outFile) throws Exception {
		gcshOutFile = new KMyMoneyFileImpl(outFile);

		KMyMoneyTransaction trx = gcshOutFile.getTransactionByID(newTrxID);
		assertNotEquals(null, trx);

		assertEquals(DATE_POSTED, trx.getDatePosted());
		assertEquals(0, trx.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(5, trx.getSplits().size());
		assertEquals(true, trx.getMemo().startsWith("Generated by SecuritiesAccountTransactionManager"));

		// ---

		KMyMoneyTransactionSplit splt1 = null;
		for ( KMyMoneyTransactionSplit splt : trx.getSplits() ) {
			if ( splt.getAccountID().equals(new KMMComplAcctID(STOCK_ACCT_ID)) ) {
				splt1 = splt;
				break;
			}
		}
		assertNotEquals(null, splt1);
		
		KMyMoneyTransactionSplit splt2 = null;
		for ( KMyMoneyTransactionSplit splt : trx.getSplits() ) {
			if ( splt.getAccountID().equals(new KMMComplAcctID(OFFSET_ACCT_ID)) ) {
				splt2 = splt;
				break;
			}
		}
		assertNotEquals(null, splt2);
		
		KMyMoneyTransactionSplit splt3 = null;
		for ( KMyMoneyTransactionSplit splt : trx.getSplits() ) {
			if ( splt.getAccountID().equals(new KMMComplAcctID(INCOME_ACCT_ID)) ) {
				splt3 = splt;
				break;
			}
		}
		assertNotEquals(null, splt3);
		
		KMyMoneyTransactionSplit splt4 = null;
		for ( KMyMoneyTransactionSplit splt : trx.getSplits() ) {
			if ( splt.getAccountID().equals(new KMMComplAcctID(DIVIDEND_EXP_ACCT_1_ID)) ) {
				splt4 = splt;
				break;
			}
		}
		assertNotEquals(null, splt4);
		
		KMyMoneyTransactionSplit splt5 = null;
		for ( KMyMoneyTransactionSplit splt : trx.getSplits() ) {
			if ( splt.getAccountID().equals(new KMMComplAcctID(DIVIDEND_EXP_ACCT_2_ID)) ) {
				splt5 = splt;
				break;
			}
		}
		assertNotEquals(null, splt5);
		
		// ---

    	FixedPointNumber expensesSum = new FixedPointNumber();
    	for ( AcctIDAmountPair elt : EXPENSES_ACCT_AMT_LIST ) {
    	    expensesSum.add(elt.amount());
    	}
    	FixedPointNumber divNet = DIV_GROSS.copy().subtract(expensesSum);
		
		assertEquals(STOCK_ACCT_ID, splt1.getAccountID().getStdID());
		assertEquals(KMyMoneyTransactionSplit.Action.DIVIDEND, splt1.getAction());
		assertEquals(0.0, splt1.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0.0, splt1.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("", splt1.getMemo());

		assertEquals(OFFSET_ACCT_ID, splt2.getAccountID().getStdID());
		assertEquals(null, splt2.getAction());
		assertEquals(divNet.doubleValue(), splt2.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(divNet.doubleValue(), splt2.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(DESCR, splt2.getMemo());

		assertEquals(INCOME_ACCT_ID, splt3.getAccountID().getStdID());
		assertEquals(null, splt3.getAction());
		assertEquals(DIV_GROSS.copy().negate().doubleValue(), splt3.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(DIV_GROSS.copy().negate().doubleValue(), splt3.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("", splt3.getMemo());

		assertEquals(DIVIDEND_EXP_ACCT_1_ID, splt4.getAccountID().getStdID());
		assertEquals(null, splt4.getAction());
		assertEquals(DIVIDEND_EXP_1.doubleValue(), splt4.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(DIVIDEND_EXP_1.doubleValue(), splt4.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("", splt4.getMemo());

		assertEquals(DIVIDEND_EXP_ACCT_2_ID, splt5.getAccountID().getStdID());
		assertEquals(null, splt5.getAction());
		assertEquals(DIVIDEND_EXP_2.doubleValue(), splt5.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(DIVIDEND_EXP_2.doubleValue(), splt5.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("", splt5.getMemo());
	}

	// ---------------------------------------------------------------
	
	// 
	private void test01_initExpAccts() {
		EXPENSES_ACCT_AMT_LIST.clear();
		
		AcctIDAmountPair acctAmtPr1 = new AcctIDAmountPair(STOCK_BUY_EXP_ACCT_1_ID, STOCK_BUY_EXP_1);
		EXPENSES_ACCT_AMT_LIST.add(acctAmtPr1);
	}

	// Example for a dividend payment in Germany (domestic share).
	// If we had a foreign share (e.g. US), we would have to add a 
	// third entry to the list: "Auslaend. Quellensteuer" (that 
	// account is not in the test file yet).
	private void test02_initExpAccts() {
		EXPENSES_ACCT_AMT_LIST.clear();
		
		AcctIDAmountPair acctAmtPr1 = new AcctIDAmountPair(DIVIDEND_EXP_ACCT_1_ID, DIVIDEND_EXP_1);
		EXPENSES_ACCT_AMT_LIST.add(acctAmtPr1);
		
		AcctIDAmountPair acctAmtPr2 = new AcctIDAmountPair(DIVIDEND_EXP_ACCT_2_ID, DIVIDEND_EXP_2);
		EXPENSES_ACCT_AMT_LIST.add(acctAmtPr2);
	}

}
