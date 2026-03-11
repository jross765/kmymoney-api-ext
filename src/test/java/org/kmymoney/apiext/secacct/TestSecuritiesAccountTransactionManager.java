package org.kmymoney.apiext.secacct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.numbers.fraction.BigFraction;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.KMyMoneyTransactionImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.apiext.ConstTest;
import org.kmymoney.apispec.read.KMyMoneyStockBuyTransaction;
import org.kmymoney.apispec.read.KMyMoneyStockDividendTransaction;
import org.kmymoney.apispec.read.KMyMoneyStockSplitTransaction;
import org.kmymoney.apispec.read.impl.KMyMoneyStockBuyTransactionImpl;
import org.kmymoney.apispec.read.impl.KMyMoneyStockDividendTransactionImpl;
import org.kmymoney.apispec.read.impl.KMyMoneyStockSplitTransactionImpl;
import org.kmymoney.apispec.write.KMyMoneyWritableStockBuyTransaction;
import org.kmymoney.apispec.write.KMyMoneyWritableStockDividendTransaction;
import org.kmymoney.apispec.write.KMyMoneyWritableStockSplitTransaction;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;
import org.kmymoney.base.tuples.AcctIDAmountFPPair;

import junit.framework.JUnit4TestAdapter;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class TestSecuritiesAccountTransactionManager {

	private static KMMAcctID STOCK_ACCT_ID  = new KMMAcctID("A000063");
	private static KMMAcctID INCOME_ACCT_ID = new KMMAcctID("A000070"); // only for dividend, not for
																		// buy/sell
	private static List<AcctIDAmountFPPair> EXPENSES_ACCT_AMT_LIST = new ArrayList<AcctIDAmountFPPair>(); // only for dividend,
																									  // not for buy/sell
	private static KMMAcctID OFFSET_ACCT_ID = new KMMAcctID("A000004");
	
	// ---

	private static FixedPointNumber BUY_NOF_STOCKS  = new FixedPointNumber(15);
	private static FixedPointNumber BUY_STOCK_PRC   = new FixedPointNumber("23080/100");
	private static LocalDate        BUY_DATE_POSTED = LocalDate.of(2024, 3, 1);
	private static String           BUY_DESCR       = "Buying stocks";

	// ---

	private static FixedPointNumber DIV_GROSS       = new FixedPointNumber("11223/100");
	private static LocalDate        DIV_DATE_POSTED = LocalDate.of(2024, 3, 1);
	private static String           DIV_DESCR       = "Dividend payment";

	// ---

	private static FixedPointNumber SPLT_NOF_SHR_BEFORE_FP = new FixedPointNumber("15");
	private static FixedPointNumber SPLT_NOF_SHR_AFTER_FP  = new FixedPointNumber("45");
	private static FixedPointNumber SPLT_FACTOR_FP         = new FixedPointNumber("3");
	private static FixedPointNumber SPLT_NOF_ADD_FP        = SPLT_NOF_SHR_AFTER_FP.copy().subtract(SPLT_NOF_SHR_BEFORE_FP);
	// .
	private static BigFraction      SPLT_NOF_SHR_BEFORE_BF = BigFraction.of(15);
	private static BigFraction      SPLT_NOF_SHR_AFTER_BF  = BigFraction.of(45);
	private static BigFraction      SPLT_FACTOR_BF         = BigFraction.of(3);
	private static BigFraction      SPLT_NOF_ADD_BF        = SPLT_NOF_SHR_AFTER_BF.subtract(SPLT_NOF_SHR_BEFORE_BF);
	// .
	private static LocalDate        SPLT_DATE_POSTED       = LocalDate.of(2026, 3, 1);
	private static String           SPLT_DESCR             = "Stock split";

	// ----------------------------

	private static KMMAcctID STOCK_BUY_EXP_ACCT_1_ID = new KMMAcctID( "A000073" ); // Bankprovision

	FixedPointNumber STOCK_BUY_EXP_1 = new FixedPointNumber("945/100");
	
	// ----------------------------

	private static KMMAcctID DIVIDEND_EXP_ACCT_1_ID = new KMMAcctID( "A000067" ); // Kapitalertragsteuer
	private static KMMAcctID DIVIDEND_EXP_ACCT_2_ID = new KMMAcctID( "A000027" ); // Soli

	FixedPointNumber DIVIDEND_EXP_1 = DIV_GROSS.copy().multiply(new FixedPointNumber("25/100"));
	FixedPointNumber DIVIDEND_EXP_2 = STOCK_BUY_EXP_1.copy().multiply(new FixedPointNumber("55/100"));
	
	// -----------------------------------------------------------------

	private KMyMoneyWritableFileImpl kmmInFile = null;
	private KMyMoneyFileImpl kmmOutFile = null;

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
		// URL kmmFileURL = classLoader.getResource(Const.KMM_FILENAME);
		// System.err.println("KMyMoney test file resource: '" + kmmFileURL + "'");
		URL kmmInFileURL = null;
		File kmmInFileRaw = null;
		try {
			kmmInFileURL = classLoader.getResource(ConstTest.KMM_FILENAME);
			kmmInFileRaw = new File(kmmInFileURL.getFile());
		} catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			return;
		}

		try {
			kmmInFile = new KMyMoneyWritableFileImpl(kmmInFileRaw);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney in-file");
			exc.printStackTrace();
		}
		
		// ---
		
		newTrxID = new KMMTrxID();
	}

	// -----------------------------------------------------------------

	@Test
	public void test01() throws Exception {
		test01_initExpAccts();

		KMyMoneyWritableStockBuyTransaction trx = 
				SecuritiesAccountTransactionManager
					.genBuyStockTrx(kmmInFile, 
									STOCK_ACCT_ID, EXPENSES_ACCT_AMT_LIST, OFFSET_ACCT_ID,
									BUY_NOF_STOCKS, BUY_STOCK_PRC, 
									BUY_DATE_POSTED, BUY_DESCR);
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
		kmmInFile.writeFile(outFile);

		test01_check_persisted(outFile);
	}

	private void test01_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);

		KMyMoneyTransaction genTrx = kmmOutFile.getTransactionByID(newTrxID);
		assertNotEquals(null, genTrx);

		KMyMoneyStockBuyTransaction specTrxRO = new KMyMoneyStockBuyTransactionImpl((KMyMoneyTransactionImpl) genTrx);
		assertNotEquals(null, specTrxRO);
		assertEquals(newTrxID, specTrxRO.getID());

		assertEquals(BUY_DATE_POSTED, specTrxRO.getDatePosted());
		// .
		assertEquals(0.0, specTrxRO.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0.0, specTrxRO.getBalanceRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0, specTrxRO.getBalanceRat().getNumerator().longValue());
		assertEquals(1, specTrxRO.getBalanceRat().getDenominator().longValue());
		// .
		assertEquals(3, specTrxRO.getSplits().size());
		assertEquals(true, specTrxRO.getMemo().startsWith("Generated by SecuritiesAccountTransactionManager"));

		// ---

		KMyMoneyTransactionSplit splt1 = null;
		for ( KMyMoneyTransactionSplit splt : specTrxRO.getSplits() ) {
			if ( splt.getAccountID().equals(new KMMComplAcctID(STOCK_ACCT_ID)) ) {
				splt1 = splt;
				break;
			}
		}
		assertNotEquals(null, splt1);
		
		KMyMoneyTransactionSplit splt2 = null;
		for ( KMyMoneyTransactionSplit splt : specTrxRO.getSplits() ) {
			if ( splt.getAccountID().equals(new KMMComplAcctID(OFFSET_ACCT_ID)) ) {
				splt2 = splt;
				break;
			}
		}
		assertNotEquals(null, splt2);
		
		KMyMoneyTransactionSplit splt3 = null;
		for ( KMyMoneyTransactionSplit splt : specTrxRO.getSplits() ) {
			if ( splt.getAccountID().equals(new KMMComplAcctID(STOCK_BUY_EXP_ACCT_1_ID)) ) {
				splt3 = splt;
				break;
			}
		}
		assertNotEquals(null, splt3);
		
		// ---

		FixedPointNumber amtNet   = BUY_NOF_STOCKS.copy().multiply(BUY_STOCK_PRC);
		FixedPointNumber amtGross = amtNet.copy();
		for ( AcctIDAmountFPPair elt : EXPENSES_ACCT_AMT_LIST ) {
		    amtGross.add(elt.amount());
		}
		
		assertEquals(STOCK_ACCT_ID, splt1.getAccountID().getStdID());
		assertEquals(KMyMoneyTransactionSplit.Action.BUY_SHARES, splt1.getAction());
		// .
		assertEquals(BUY_NOF_STOCKS.doubleValue(), splt1.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(BUY_NOF_STOCKS.doubleValue(), splt1.getSharesRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(BUY_NOF_STOCKS.longValue(), splt1.getSharesRat().getNumerator().longValue());
		assertEquals(1, splt1.getSharesRat().getDenominator().longValue());
		// .
		assertEquals(amtNet.doubleValue(), splt1.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(amtNet.doubleValue(), splt1.getValueRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(3462, splt1.getValueRat().getNumerator().longValue());
		assertEquals(1, splt1.getValueRat().getDenominator().longValue());
		// .
		assertEquals("", splt1.getMemo());

		assertEquals(OFFSET_ACCT_ID, splt2.getAccountID().getStdID());
		assertEquals(null, splt2.getAction());
		// .
		assertEquals(amtGross.copy().negate().doubleValue(), splt2.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(amtGross.copy().negate().doubleValue(), splt2.getSharesRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(-69429, splt2.getSharesRat().getNumerator().longValue());
		assertEquals(20, splt2.getSharesRat().getDenominator().longValue());
		// .
		assertEquals(amtGross.copy().negate().doubleValue(), splt2.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(amtGross.copy().negate().doubleValue(), splt2.getValueRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(-69429, splt2.getValueRat().getNumerator().longValue());
		assertEquals(20, splt2.getValueRat().getDenominator().longValue());
		// .
		assertEquals(BUY_DESCR, splt2.getMemo());

		assertEquals(STOCK_BUY_EXP_ACCT_1_ID, splt3.getAccountID().getStdID());
		assertEquals(null, splt3.getAction());
		// .
		assertEquals(STOCK_BUY_EXP_1.doubleValue(), splt3.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(STOCK_BUY_EXP_1.doubleValue(), splt3.getSharesRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(189, splt3.getSharesRat().getNumerator().longValue());
		assertEquals(20, splt3.getSharesRat().getDenominator().longValue());
		// .
		assertEquals(STOCK_BUY_EXP_1.doubleValue(), splt3.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(STOCK_BUY_EXP_1.doubleValue(), splt3.getValueRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(189, splt3.getValueRat().getNumerator().longValue());
		assertEquals(20, splt3.getValueRat().getDenominator().longValue());
		// .
		assertEquals("", splt3.getMemo());
	}

	@Test
	public void test02() throws Exception {
		test02_initExpAccts();

		KMyMoneyWritableStockDividendTransaction trx = 
				SecuritiesAccountTransactionManager
					.genDividDistribTrx(kmmInFile, 
									STOCK_ACCT_ID, INCOME_ACCT_ID, EXPENSES_ACCT_AMT_LIST, OFFSET_ACCT_ID,
									KMyMoneyTransactionSplit.Action.DIVIDEND, DIV_GROSS, 
									DIV_DATE_POSTED, DIV_DESCR);
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
		kmmInFile.writeFile(outFile);

		test02_check_persisted(outFile);
	}

	private void test02_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);

		KMyMoneyTransaction genTrx = kmmOutFile.getTransactionByID(newTrxID);
		assertNotEquals(null, genTrx);

		KMyMoneyStockDividendTransaction specTrxRO = new KMyMoneyStockDividendTransactionImpl((KMyMoneyTransactionImpl) genTrx);
		assertNotEquals(null, specTrxRO);
		assertEquals(newTrxID, specTrxRO.getID());

		assertEquals(DIV_DATE_POSTED, specTrxRO.getDatePosted());
		// .
		assertEquals(0.0, specTrxRO.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0.0, specTrxRO.getBalanceRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0, specTrxRO.getBalanceRat().getNumerator().longValue());
		assertEquals(1, specTrxRO.getBalanceRat().getDenominator().longValue());
		// .
		assertEquals(5, specTrxRO.getSplits().size());
		assertEquals(true, specTrxRO.getMemo().startsWith("Generated by SecuritiesAccountTransactionManager"));

		// ---

		KMyMoneyTransactionSplit splt1 = null;
		for ( KMyMoneyTransactionSplit splt : specTrxRO.getSplits() ) {
			if ( splt.getAccountID().equals(new KMMComplAcctID(STOCK_ACCT_ID)) ) {
				splt1 = splt;
				break;
			}
		}
		assertNotEquals(null, splt1);
		
		KMyMoneyTransactionSplit splt2 = null;
		for ( KMyMoneyTransactionSplit splt : specTrxRO.getSplits() ) {
			if ( splt.getAccountID().equals(new KMMComplAcctID(OFFSET_ACCT_ID)) ) {
				splt2 = splt;
				break;
			}
		}
		assertNotEquals(null, splt2);
		
		KMyMoneyTransactionSplit splt3 = null;
		for ( KMyMoneyTransactionSplit splt : specTrxRO.getSplits() ) {
			if ( splt.getAccountID().getStdID().equals(INCOME_ACCT_ID) ) {
				splt3 = splt;
				break;
			}
		}
		assertNotEquals(null, splt3);
		
		KMyMoneyTransactionSplit splt4 = null;
		for ( KMyMoneyTransactionSplit splt : specTrxRO.getSplits() ) {
			if ( splt.getAccountID().getStdID().equals(DIVIDEND_EXP_ACCT_1_ID) ) {
				splt4 = splt;
				break;
			}
		}
		assertNotEquals(null, splt4);
		
		KMyMoneyTransactionSplit splt5 = null;
		for ( KMyMoneyTransactionSplit splt : specTrxRO.getSplits() ) {
			if ( splt.getAccountID().getStdID().equals(DIVIDEND_EXP_ACCT_2_ID) ) {
				splt5 = splt;
				break;
			}
		}
		assertNotEquals(null, splt5);
		
		// ---

    	FixedPointNumber expensesSum = new FixedPointNumber();
    	for ( AcctIDAmountFPPair elt : EXPENSES_ACCT_AMT_LIST ) {
    	    expensesSum.add(elt.amount());
    	}
    	FixedPointNumber divNet = DIV_GROSS.copy().subtract(expensesSum);
		
		assertEquals(STOCK_ACCT_ID, splt1.getAccountID().getStdID());
		assertEquals(KMyMoneyTransactionSplit.Action.DIVIDEND, splt1.getAction());
		// .
		assertEquals(0.0, splt1.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0.0, splt1.getSharesRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0, splt1.getSharesRat().getNumerator().longValue());
		assertEquals(1, splt1.getSharesRat().getDenominator().longValue());
		// .
		assertEquals(0.0, splt1.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0.0, splt1.getValueRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0, splt1.getValueRat().getNumerator().longValue());
		assertEquals(1, splt1.getValueRat().getDenominator().longValue());
		// .
		assertEquals("", splt1.getMemo());

		assertEquals(OFFSET_ACCT_ID, splt2.getAccountID().getStdID());
		assertEquals(null, splt2.getAction());
		// .
		assertEquals(divNet.doubleValue(), splt2.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(divNet.doubleValue(), splt2.getSharesRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(3159, splt2.getSharesRat().getNumerator().longValue());
		assertEquals(40, splt2.getSharesRat().getDenominator().longValue());
		// .
		assertEquals(divNet.doubleValue(), splt2.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(divNet.doubleValue(), splt2.getValueRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(3159, splt2.getValueRat().getNumerator().longValue());
		assertEquals(40, splt2.getValueRat().getDenominator().longValue());
		// .
		assertEquals(DIV_DESCR, splt2.getMemo());

		assertEquals(INCOME_ACCT_ID, splt3.getAccountID().getStdID());
		assertEquals(null, splt3.getAction());
		// .
		assertEquals(DIV_GROSS.copy().negate().doubleValue(), splt3.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(DIV_GROSS.copy().negate().doubleValue(), splt3.getSharesRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(-11223, splt3.getSharesRat().getNumerator().longValue());
		assertEquals(100, splt3.getSharesRat().getDenominator().longValue());
		// .
		assertEquals(DIV_GROSS.copy().negate().doubleValue(), splt3.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(DIV_GROSS.copy().negate().doubleValue(), splt3.getValueRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(-11223, splt3.getValueRat().getNumerator().longValue());
		assertEquals(100, splt3.getValueRat().getDenominator().longValue());
		// .
		assertEquals("", splt3.getMemo());

		assertEquals(DIVIDEND_EXP_ACCT_1_ID, splt4.getAccountID().getStdID());
		assertEquals(null, splt4.getAction());
		// .
		assertEquals(DIVIDEND_EXP_1.doubleValue(), splt4.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(DIVIDEND_EXP_1.doubleValue(), splt4.getSharesRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(11223, splt4.getSharesRat().getNumerator().longValue());
		assertEquals(400, splt4.getSharesRat().getDenominator().longValue());
		// .
		assertEquals(DIVIDEND_EXP_1.doubleValue(), splt4.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(DIVIDEND_EXP_1.doubleValue(), splt4.getValueRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(11223, splt4.getValueRat().getNumerator().longValue());
		assertEquals(400, splt4.getValueRat().getDenominator().longValue());
		// .
		assertEquals("", splt4.getMemo());

		assertEquals(DIVIDEND_EXP_ACCT_2_ID, splt5.getAccountID().getStdID());
		assertEquals(null, splt5.getAction());
		// .
		assertEquals(DIVIDEND_EXP_2.doubleValue(), splt5.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(DIVIDEND_EXP_2.doubleValue(), splt5.getSharesRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(2079, splt5.getSharesRat().getNumerator().longValue());
		assertEquals(400, splt5.getSharesRat().getDenominator().longValue());
		// .
		assertEquals(DIVIDEND_EXP_2.doubleValue(), splt5.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(DIVIDEND_EXP_2.doubleValue(), splt5.getValueRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(2079, splt5.getValueRat().getNumerator().longValue());
		assertEquals(400, splt5.getValueRat().getDenominator().longValue());
		// .
		assertEquals("", splt5.getMemo());
	}

	@Test
	public void test03_1() throws Exception {
		test03_initExpAccts();

		KMyMoneyAccount stockAcct = kmmInFile.getAccountByID(STOCK_ACCT_ID);
		assertEquals(SPLT_NOF_SHR_BEFORE_FP, stockAcct.getBalance());
		assertEquals(SPLT_NOF_SHR_BEFORE_BF, stockAcct.getBalanceRat());
		
		KMyMoneyWritableStockSplitTransaction trx = 
				SecuritiesAccountTransactionManager
					.genStockSplitTrx(kmmInFile, 
									STOCK_ACCT_ID,
									SecuritiesAccountTransactionManager.StockSplitVar.FACTOR, SPLT_FACTOR_FP, 
									SPLT_DATE_POSTED, SPLT_DESCR);
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
		kmmInFile.writeFile(outFile);

		test03_check_persisted_hl(outFile);
		test03_check_persisted_ml(outFile);
	}

	@Test
	public void test03_2() throws Exception {
		test03_initExpAccts();

		KMyMoneyAccount stockAcct = kmmInFile.getAccountByID(STOCK_ACCT_ID);
		assertEquals(SPLT_NOF_SHR_BEFORE_FP, stockAcct.getBalance());
		assertEquals(SPLT_NOF_SHR_BEFORE_BF, stockAcct.getBalanceRat());
		
		KMyMoneyWritableStockSplitTransaction trx = 
				SecuritiesAccountTransactionManager
					.genStockSplitTrx(kmmInFile, 
									STOCK_ACCT_ID,
									SecuritiesAccountTransactionManager.StockSplitVar.NOF_ADD_SHARES, SPLT_NOF_ADD_FP, 
									SPLT_DATE_POSTED, SPLT_DESCR);
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
		kmmInFile.writeFile(outFile);

		test03_check_persisted_hl(outFile);
		test03_check_persisted_ml(outFile);
	}
	
	// High-level checks 
	private void test03_check_persisted_hl(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);

		KMyMoneyTransaction genTrx = kmmOutFile.getTransactionByID(newTrxID);
		assertNotEquals(null, genTrx);

		KMyMoneyStockSplitTransaction specTrxRO = new KMyMoneyStockSplitTransactionImpl((KMyMoneyTransactionImpl) genTrx);
		assertNotEquals(null, specTrxRO);
		assertEquals(newTrxID, specTrxRO.getID());

		// ---

		KMyMoneyTransactionSplit splt1 = specTrxRO.getSplit();
		assertNotEquals(null, splt1);
		assertEquals(STOCK_ACCT_ID, splt1.getAccountID().getStdID());
		
		// ---

		assertEquals(SPLT_NOF_SHR_BEFORE_FP, specTrxRO.getNofSharesBeforeSplit());
		assertEquals(SPLT_NOF_SHR_BEFORE_BF, specTrxRO.getNofSharesBeforeSplitRat());
		assertEquals(SPLT_NOF_SHR_AFTER_FP,  specTrxRO.getNofSharesAfterSplit());
		assertEquals(SPLT_NOF_SHR_AFTER_BF,  specTrxRO.getNofSharesAfterSplitRat());
		// .
		assertEquals(SPLT_NOF_ADD_FP,        specTrxRO.getNofAddShares());
		assertEquals(SPLT_NOF_ADD_BF,        specTrxRO.getNofAddSharesRat());
		// .
		assertEquals(SPLT_FACTOR_FP,         specTrxRO.getSplitFactor());
		assertEquals(SPLT_FACTOR_BF,         specTrxRO.getSplitFactorRat());
	}

	// Mid-level checks (i.e., "manually") 
	private void test03_check_persisted_ml(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);

		KMyMoneyTransaction genTrx = kmmOutFile.getTransactionByID(newTrxID);
		assertNotEquals(null, genTrx);

		KMyMoneyTransaction specTrxRO = new KMyMoneyTransactionImpl((KMyMoneyTransactionImpl) genTrx);
		assertNotEquals(null, specTrxRO);
		assertEquals(newTrxID, specTrxRO.getID());

		assertEquals(SPLT_DATE_POSTED, specTrxRO.getDatePosted());
		// .
		assertEquals(0.0, specTrxRO.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0.0, specTrxRO.getBalanceRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0, specTrxRO.getBalanceRat().getNumerator().longValue());
		assertEquals(1, specTrxRO.getBalanceRat().getDenominator().longValue());
		// .
		assertEquals(1, specTrxRO.getSplits().size());
		assertEquals(true, specTrxRO.getMemo().startsWith("Generated by SecuritiesAccountTransactionManager"));

		// ---

		KMyMoneyTransactionSplit splt1 = specTrxRO.getSplits().get(0);
		assertNotEquals(null, splt1);
		assertEquals(STOCK_ACCT_ID, splt1.getAccountID().getStdID());
		
		// ---

		assertEquals(STOCK_ACCT_ID, splt1.getAccountID().getStdID());
		assertEquals(KMyMoneyTransactionSplit.Action.SPLIT_SHARES, splt1.getAction());
		// .
		assertEquals(SPLT_FACTOR_FP, splt1.getShares());
		assertEquals(SPLT_FACTOR_BF, splt1.getSharesRat());
		assertEquals(SPLT_NOF_SHR_AFTER_FP, splt1.getAccount().getBalance());
		assertEquals(SPLT_NOF_SHR_AFTER_BF, splt1.getAccount().getBalanceRat());
		// .
		assertEquals(0.0, splt1.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0.0, splt1.getValueRat().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0, splt1.getValueRat().getNumerator().longValue());
		assertEquals(1, splt1.getValueRat().getDenominator().longValue());
		// .
		assertEquals(SPLT_DESCR, splt1.getMemo());
	}

	// ---------------------------------------------------------------
	
	// 
	private void test01_initExpAccts() {
		EXPENSES_ACCT_AMT_LIST.clear();
		
		AcctIDAmountFPPair acctAmtPr1 = new AcctIDAmountFPPair(STOCK_BUY_EXP_ACCT_1_ID, STOCK_BUY_EXP_1);
		EXPENSES_ACCT_AMT_LIST.add(acctAmtPr1);
	}

	// Example for a dividend payment in Germany (domestic share).
	// If we had a foreign share (e.g. US), we would have to add a 
	// third entry to the list: "Auslaend. Quellensteuer" (that 
	// account is not in the test file yet).
	private void test02_initExpAccts() {
		EXPENSES_ACCT_AMT_LIST.clear();
		
		AcctIDAmountFPPair acctAmtPr1 = new AcctIDAmountFPPair(DIVIDEND_EXP_ACCT_1_ID, DIVIDEND_EXP_1);
		EXPENSES_ACCT_AMT_LIST.add(acctAmtPr1);
		
		AcctIDAmountFPPair acctAmtPr2 = new AcctIDAmountFPPair(DIVIDEND_EXP_ACCT_2_ID, DIVIDEND_EXP_2);
		EXPENSES_ACCT_AMT_LIST.add(acctAmtPr2);
	}

	private void test03_initExpAccts() {
		EXPENSES_ACCT_AMT_LIST.clear();
	}

}
