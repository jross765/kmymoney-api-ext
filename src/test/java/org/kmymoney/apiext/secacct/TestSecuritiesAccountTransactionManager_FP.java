package org.kmymoney.apiext.secacct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
import org.kmymoney.apispec.write.KMyMoneyWritableStockBuySellTransaction;
import org.kmymoney.apispec.write.KMyMoneyWritableStockDividendTransaction;
import org.kmymoney.apispec.write.KMyMoneyWritableStockSplitTransaction;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;
import org.kmymoney.base.tuples.AcctIDAmountFPPair;

import junit.framework.JUnit4TestAdapter;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class TestSecuritiesAccountTransactionManager_FP {

	private static KMMAcctID STOCK_ACCT_ID  = new KMMAcctID("A000063");
	private static KMMAcctID INCOME_ACCT_ID = new KMMAcctID("A000070"); // only for dividend, not for
																		// buy/sell
	private static List<AcctIDAmountFPPair> EXPENSES_ACCT_AMT_LIST = new ArrayList<AcctIDAmountFPPair>(); // only for dividend,
																									         // not for buy/sell
	private static KMMAcctID OFFSET_ACCT_ID = new KMMAcctID("A000004");
	
	// ---

	private static FixedPointNumber BUY_NOF_STOCKS  = new FixedPointNumber(15);
	private static FixedPointNumber BUY_STOCK_PRC   = new FixedPointNumber("23080/100");
	private static FixedPointNumber BUY_NET_PRC     = BUY_STOCK_PRC.copy().multiply(BUY_NOF_STOCKS);
	private static FixedPointNumber BUY_EXP_1       = new FixedPointNumber("945/100");
	private static FixedPointNumber BUY_GROSS_PRC   = BUY_NET_PRC.copy().add(BUY_EXP_1);
	// .
	private static LocalDate        BUY_DATE_POSTED = LocalDate.of(2024, 3, 1);
	private static String           BUY_DESCR       = "Buying stocks";

	// ---

	private static FixedPointNumber DIV_GROSS       = new FixedPointNumber("11223/100");
	private static FixedPointNumber DIV_EXP_1       = DIV_GROSS.copy().multiply(new FixedPointNumber("25/100"));
	private static FixedPointNumber DIV_EXP_2       = BUY_EXP_1.copy().multiply(new FixedPointNumber("55/100"));
	private static FixedPointNumber DIV_FEETAX      = DIV_EXP_1.copy().add(DIV_EXP_2);
	private static FixedPointNumber DIV_NET         = DIV_GROSS.copy().subtract(DIV_FEETAX);
	
	private static LocalDate        DIV_DATE_POSTED = LocalDate.of(2024, 3, 1);
	private static String           DIV_DESCR       = "Dividend payment";

	// ---

	private static FixedPointNumber SPLT_NOF_SHR_BEFORE = new FixedPointNumber("15");
	private static FixedPointNumber SPLT_NOF_SHR_AFTER  = new FixedPointNumber("45");
	private static FixedPointNumber SPLT_FACTOR         = new FixedPointNumber("3");
	private static FixedPointNumber SPLT_NOF_ADD        = SPLT_NOF_SHR_AFTER.copy().subtract(SPLT_NOF_SHR_BEFORE);
	// .
	private static LocalDate        SPLT_DATE_POSTED    = LocalDate.of(2026, 3, 1);
	private static String           SPLT_DESCR          = "Stock split";

	// ----------------------------

	private static KMMAcctID BUY_EXP_ACCT_1_ID = new KMMAcctID( "A000073" ); // Bankprovision

	// ----------------------------

	private static KMMAcctID DIV_EXP_ACCT_1_ID = new KMMAcctID( "A000067" ); // Kapitalertragsteuer
	private static KMMAcctID DIV_EXP_ACCT_2_ID = new KMMAcctID( "A000027" ); // Soli

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
		return new JUnit4TestAdapter(TestSecuritiesAccountTransactionManager_FP.class);
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

		KMyMoneyWritableStockBuySellTransaction trx = 
				SecuritiesAccountTransactionManager_FP
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

		test01_check_persisted_hl(outFile);
		test01_check_persisted_ml(outFile);
	}

	// High-level checks 
	private void test01_check_persisted_hl(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);

		KMyMoneyTransaction genTrx = kmmOutFile.getTransactionByID(newTrxID);
		assertNotEquals(null, genTrx);

		KMyMoneyStockBuyTransaction specTrxRO = new KMyMoneyStockBuyTransactionImpl((KMyMoneyTransactionImpl) genTrx);
		assertNotEquals(null, specTrxRO);
		assertEquals(newTrxID, specTrxRO.getID());

		// ---

		KMyMoneyTransactionSplit splt1 = specTrxRO.getStockAccountSplit();
		assertNotEquals(null, splt1);
		assertEquals(STOCK_ACCT_ID, splt1.getAccountID());
		
		KMyMoneyTransactionSplit splt2 = specTrxRO.getOffsettingAccountSplit();
		assertNotEquals(null, splt1);
		assertEquals(OFFSET_ACCT_ID, splt2.getAccountID());
		
		assertNotEquals(null, specTrxRO.getExpensesSplits());
		assertEquals(1, specTrxRO.getExpensesSplits().size());

		// ---

		assertEquals(BUY_NOF_STOCKS, specTrxRO.getNofShares());
		assertEquals(BUY_STOCK_PRC,  specTrxRO.getPricePerShare());
		assertEquals(BUY_NET_PRC,    specTrxRO.getNetPrice());
		assertEquals(BUY_EXP_1,      specTrxRO.getFeesTaxes());
		assertEquals(BUY_EXP_1,      specTrxRO.getFeeTax(BUY_EXP_ACCT_1_ID));
		assertEquals(BUY_GROSS_PRC,  specTrxRO.getGrossPrice());
	}

	// Mid-level checks (i.e., "manually") 
	private void test01_check_persisted_ml(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);

		KMyMoneyTransaction genTrx = kmmOutFile.getTransactionByID(newTrxID);
		assertNotEquals(null, genTrx);

		assertEquals(BUY_DATE_POSTED, genTrx.getDatePosted());
		// .
		assertEquals(0.0, genTrx.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		// .
		assertEquals(3, genTrx.getSplits().size());
		assertEquals(true, genTrx.getMemo().startsWith("Generated by SecuritiesAccountTransactionManager"));

		// ---

		KMyMoneyTransactionSplit splt1 = null;
		for ( KMyMoneyTransactionSplit splt : genTrx.getSplits() ) {
			if ( splt.getAccountID().equals(STOCK_ACCT_ID) ) {
				splt1 = splt;
				break;
			}
		}
		assertNotEquals(null, splt1);
		
		KMyMoneyTransactionSplit splt2 = null;
		for ( KMyMoneyTransactionSplit splt : genTrx.getSplits() ) {
			if ( splt.getAccountID().equals(OFFSET_ACCT_ID) ) {
				splt2 = splt;
				break;
			}
		}
		assertNotEquals(null, splt2);
		
		KMyMoneyTransactionSplit splt3 = null;
		for ( KMyMoneyTransactionSplit splt : genTrx.getSplits() ) {
			if ( splt.getAccountID().equals(BUY_EXP_ACCT_1_ID) ) {
				splt3 = splt;
				break;
			}
		}
		assertNotEquals(null, splt3);
		
		// ---

		FixedPointNumber feeTaxFP = FixedPointNumber.ZERO.copy();
		for ( AcctIDAmountFPPair elt : EXPENSES_ACCT_AMT_LIST ) {
		    feeTaxFP.add(elt.amount()); // mutable
		}
    	FixedPointNumber prcGrossFP = BUY_NET_PRC.copy().add(feeTaxFP);
		
		assertEquals(BUY_EXP_1, feeTaxFP);
		assertEquals(BUY_GROSS_PRC, prcGrossFP);
		
		assertEquals(STOCK_ACCT_ID, splt1.getAccountID());
		assertEquals(KMyMoneyTransactionSplit.Action.BUY_SHARES, splt1.getAction());
		assertEquals(BUY_NOF_STOCKS, splt1.getShares());
		assertEquals(BUY_NET_PRC, splt1.getValue());
		assertEquals("", splt1.getMemo());

		assertEquals(OFFSET_ACCT_ID, splt2.getAccountID());
		assertEquals(null, splt2.getAction());
		assertEquals(BUY_GROSS_PRC.copy().negate(), splt2.getShares());
		assertEquals(BUY_GROSS_PRC.copy().negate(), splt2.getValue());
		assertEquals(BUY_DESCR, splt2.getMemo());

		assertEquals(BUY_EXP_ACCT_1_ID, splt3.getAccountID());
		assertEquals(null, splt3.getAction());
		assertEquals(BUY_EXP_1, splt3.getShares());
		assertEquals(BUY_EXP_1, splt3.getValue());
		assertEquals("", splt3.getMemo());
	}

	@Test
	public void test02() throws Exception {
		test02_initExpAccts();

		KMyMoneyWritableStockDividendTransaction trx = 
				SecuritiesAccountTransactionManager_FP
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

		test02_check_persisted_hl(outFile);
		test02_check_persisted_ml(outFile);
	}

	// High-level checks
	private void test02_check_persisted_hl(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);

		KMyMoneyTransaction genTrx = kmmOutFile.getTransactionByID(newTrxID);
		assertNotEquals(null, genTrx);

		KMyMoneyStockDividendTransaction specTrxRO = new KMyMoneyStockDividendTransactionImpl((KMyMoneyTransactionImpl) genTrx);
		assertNotEquals(null, specTrxRO);
		assertEquals(newTrxID, specTrxRO.getID());

		// ---

		KMyMoneyTransactionSplit splt1 = specTrxRO.getStockAccountSplit();
		assertNotEquals(null, splt1);
		assertEquals(STOCK_ACCT_ID, splt1.getAccountID());
		
		KMyMoneyTransactionSplit splt2 = specTrxRO.getOffsettingAccountSplit();
		assertNotEquals(null, splt1);
		assertEquals(OFFSET_ACCT_ID, splt2.getAccountID());
		
		assertNotEquals(null, specTrxRO.getExpensesSplits());
		assertEquals(2, specTrxRO.getExpensesSplits().size());

		// ---

		assertEquals(DIV_GROSS,  specTrxRO.getGrossDividend());
		assertEquals(DIV_FEETAX, specTrxRO.getFeesTaxes());
		assertEquals(DIV_EXP_1,  specTrxRO.getFeeTax(DIV_EXP_ACCT_1_ID));
		assertEquals(DIV_EXP_2,  specTrxRO.getFeeTax(DIV_EXP_ACCT_2_ID));
		assertEquals(DIV_NET,    specTrxRO.getNetDividend());
	}

	// Mid-level checks (i.e., "manually") 
	private void test02_check_persisted_ml(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);

		KMyMoneyTransaction genTrx = kmmOutFile.getTransactionByID(newTrxID);
		assertNotEquals(null, genTrx);

		assertEquals(DIV_DATE_POSTED, genTrx.getDatePosted());
		// .
		assertEquals(0.0, genTrx.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		// .
		assertEquals(5, genTrx.getSplits().size());
		assertEquals(true, genTrx.getMemo().startsWith("Generated by SecuritiesAccountTransactionManager"));

		// ---

		KMyMoneyTransactionSplit splt1 = null;
		for ( KMyMoneyTransactionSplit splt : genTrx.getSplits() ) {
			if ( splt.getAccountID().equals(STOCK_ACCT_ID) ) {
				splt1 = splt;
				break;
			}
		}
		assertNotEquals(null, splt1);
		
		KMyMoneyTransactionSplit splt2 = null;
		for ( KMyMoneyTransactionSplit splt : genTrx.getSplits() ) {
			if ( splt.getAccountID().equals(OFFSET_ACCT_ID) ) {
				splt2 = splt;
				break;
			}
		}
		assertNotEquals(null, splt2);
		
		KMyMoneyTransactionSplit splt3 = null;
		for ( KMyMoneyTransactionSplit splt : genTrx.getSplits() ) {
			if ( splt.getAccountID().equals(INCOME_ACCT_ID) ) {
				splt3 = splt;
				break;
			}
		}
		assertNotEquals(null, splt3);
		
		KMyMoneyTransactionSplit splt4 = null;
		for ( KMyMoneyTransactionSplit splt : genTrx.getSplits() ) {
			if ( splt.getAccountID().equals(DIV_EXP_ACCT_1_ID) ) {
				splt4 = splt;
				break;
			}
		}
		assertNotEquals(null, splt4);
		
		KMyMoneyTransactionSplit splt5 = null;
		for ( KMyMoneyTransactionSplit splt : genTrx.getSplits() ) {
			if ( splt.getAccountID().equals(DIV_EXP_ACCT_2_ID) ) {
				splt5 = splt;
				break;
			}
		}
		assertNotEquals(null, splt5);
		
		// ---

    	FixedPointNumber feeTaxFP = FixedPointNumber.ZERO.copy();
    	for ( AcctIDAmountFPPair elt : EXPENSES_ACCT_AMT_LIST ) {
    	    feeTaxFP.add(elt.amount()); // mutable
    	}
    	FixedPointNumber divNetFP = DIV_GROSS.copy().subtract(feeTaxFP);
		
		assertEquals(DIV_FEETAX, feeTaxFP);
		assertEquals(DIV_NET, divNetFP);
		
		assertEquals(STOCK_ACCT_ID, splt1.getAccountID());
		assertEquals(KMyMoneyTransactionSplit.Action.DIVIDEND, splt1.getAction());
		assertEquals(0.0, splt1.getShares().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0.0, splt1.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals("", splt1.getMemo());

		assertEquals(OFFSET_ACCT_ID, splt2.getAccountID());
		assertEquals(null, splt2.getAction());
		assertEquals(DIV_NET, splt2.getShares());
		assertEquals(DIV_NET, splt2.getValue());
		assertEquals(DIV_DESCR, splt2.getMemo());

		assertEquals(INCOME_ACCT_ID, splt3.getAccountID());
		assertEquals(null, splt3.getAction());
		assertEquals(DIV_GROSS.copy().negate(), splt3.getShares());
		assertEquals(DIV_GROSS.copy().negate(), splt3.getValue());
		assertEquals("", splt3.getMemo());

		assertEquals(DIV_EXP_ACCT_1_ID, splt4.getAccountID());
		assertEquals(null, splt4.getAction());
		assertEquals(DIV_EXP_1, splt4.getShares());
		assertEquals(DIV_EXP_1, splt4.getValue());
		assertEquals("", splt4.getMemo());

		assertEquals(DIV_EXP_ACCT_2_ID, splt5.getAccountID());
		assertEquals(null, splt5.getAction());
		assertEquals(DIV_EXP_2, splt5.getShares());
		assertEquals(DIV_EXP_2, splt5.getValue());
		assertEquals("", splt5.getMemo());
	}

	@Test
	public void test03_1() throws Exception {
		test03_initExpAccts();

		KMyMoneyAccount stockAcct = kmmInFile.getAccountByID(STOCK_ACCT_ID);
		assertEquals(SPLT_NOF_SHR_BEFORE, stockAcct.getBalance());
		
		KMyMoneyWritableStockSplitTransaction trx = 
				SecuritiesAccountTransactionManager_FP
					.genStockSplitTrx(kmmInFile, 
									STOCK_ACCT_ID,
									SecuritiesAccountTransactionManager_FP.StockSplitVar.FACTOR, SPLT_FACTOR, 
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
		assertEquals(SPLT_NOF_SHR_BEFORE, stockAcct.getBalance());
		
		KMyMoneyWritableStockSplitTransaction trx = 
				SecuritiesAccountTransactionManager_FP
					.genStockSplitTrx(kmmInFile, 
									STOCK_ACCT_ID,
									SecuritiesAccountTransactionManager_FP.StockSplitVar.NOF_ADD_SHARES, SPLT_NOF_ADD, 
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
		assertEquals(STOCK_ACCT_ID, splt1.getAccountID());
		
		// ---

		assertEquals(SPLT_NOF_SHR_BEFORE, specTrxRO.getNofSharesBeforeSplit());
		assertEquals(SPLT_NOF_SHR_AFTER,  specTrxRO.getNofSharesAfterSplit());
		assertEquals(SPLT_NOF_ADD,        specTrxRO.getNofAddShares());
		assertEquals(SPLT_FACTOR,         specTrxRO.getSplitFactor());
	}

	// Mid-level checks (i.e., "manually") 
	private void test03_check_persisted_ml(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);

		KMyMoneyTransaction genTrx = kmmOutFile.getTransactionByID(newTrxID);
		assertNotEquals(null, genTrx);

		assertEquals(SPLT_DATE_POSTED, genTrx.getDatePosted());
		// .
		assertEquals(0.0, genTrx.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		// .
		assertEquals(1, genTrx.getSplits().size());
		assertEquals(true, genTrx.getMemo().startsWith("Generated by SecuritiesAccountTransactionManager"));

		// ---

		KMyMoneyTransactionSplit splt1 = genTrx.getSplits().get(0);
		assertNotEquals(null, splt1);
		assertEquals(STOCK_ACCT_ID, splt1.getAccountID());
		
		// ---

		assertEquals(STOCK_ACCT_ID, splt1.getAccountID());
		assertEquals(KMyMoneyTransactionSplit.Action.SPLIT_SHARES, splt1.getAction());
		assertEquals(SPLT_FACTOR, splt1.getShares());
		assertEquals(SPLT_NOF_SHR_AFTER, splt1.getAccount().getBalance());
		assertEquals(0.0, splt1.getValue().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(SPLT_DESCR, splt1.getMemo());
	}

	// ---------------------------------------------------------------
	
	// 
	private void test01_initExpAccts() {
		EXPENSES_ACCT_AMT_LIST.clear();
		
		AcctIDAmountFPPair acctAmtPr1FP = new AcctIDAmountFPPair(BUY_EXP_ACCT_1_ID, BUY_EXP_1);
		EXPENSES_ACCT_AMT_LIST.add(acctAmtPr1FP);
	}

	// Example for a dividend payment in Germany (domestic share).
	// If we had a foreign share (e.g. US), we would have to add a 
	// third entry to the list: "Auslaend. Quellensteuer" (that 
	// account is not in the test file yet).
	private void test02_initExpAccts() {
		EXPENSES_ACCT_AMT_LIST.clear();
		
		AcctIDAmountFPPair acctAmtPr1FP = new AcctIDAmountFPPair(DIV_EXP_ACCT_1_ID, DIV_EXP_1);
		EXPENSES_ACCT_AMT_LIST.add(acctAmtPr1FP);
		
		AcctIDAmountFPPair acctAmtPr2FP = new AcctIDAmountFPPair(DIV_EXP_ACCT_2_ID, DIV_EXP_2);
		EXPENSES_ACCT_AMT_LIST.add(acctAmtPr2FP);
	}

	private void test03_initExpAccts() {
		EXPENSES_ACCT_AMT_LIST.clear();
	}

}
