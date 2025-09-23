package org.kmymoney.apiext.secacct;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.tuples.AcctIDAmountPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * Collection of simplified, high-level access functions to a KMyMoney file for
 * managing securities accounts (brokerage accounts).
 * <br>
 * These methods are sort of "macros" for the low-level access functions
 * in the "API" module.
 */
public class SecuritiesAccountTransactionManager {
    
    public enum Type {
    	BUY_STOCK,
    	DIVIDEND,
    	DISTRIBUTION,
    	STOCK_SPLIT
    }
    
    public enum StockSplitVar {
    	FACTOR,
    	NOF_ADD_SHARES
    }
    
    // ---------------------------------------------------------------
    
    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(SecuritiesAccountTransactionManager.class);
    
    // ----------------------------

    // ::TODO These numbers should be extracted into a config. file. 
    // ::MAGIC
    private static FixedPointNumber SPLIT_FACTOR_MIN = new FixedPointNumber("1/20"); 
    	// anything below that value is technically OK,
        // but unplausible and thus forbidden.
    private static FixedPointNumber SPLIT_FACTOR_MAX = new FixedPointNumber("20");
    	// accordingly
    
    // Notes: 
    //  - It is common to specify stock (reverse) splits by a factor (e.g., 2 for a 2-for-1 split,
    //    or 1/4 for 1-for-4 reverse split). So why use the number of add. shares? Because that
    //    is how GnuCash (cf. the sister project) handles things, as opposed to KMyMoney, both 
    //    on the data and the GUI level, and given that we want to have both projects as symmetrical 
    //    as possible, we copy that logic here, so that the user can choose between both methods.
    //    Besides, the author has witnessed cases where the bank's statements provide wrong 
    //    values for the factor (yes, a bank's software also has bugs), whereas the number of add. 
    //    shares is practically always correct, given the usual bank-internal processes which
    //    the author happens to know a thing or two about.
    //  - As opposed to the factor above, a plausible range for the (abs.) number of additional 
    //    (to be subtracted) shares cannot as generally be specified. 
    //    E.g., European/US stocks tend to be priced above 1 EUR/USD, else they are considered penny 
    //    stocks (both literally and figuratively) and thus deemed uninvestable for the average Joe, 
    //    whereas in Singapore, e.g., it is deemed absolutely normal for a stock to be priced by just 
    //    a few cents or even less. Conversely, it is not uncommon for Japanese stocks to be priced
    //    very highly by European/US standards. Thus, the number of shares in a typical retail portfolio 
    //    will vary accordingly.
    //    Moreover, we of course know absolutely nothing about the entity/the individual that/who 
    //    will use this lib. A "regular" individual investor might have, say, 100 to 500 or so shares of 
    //    a European/US stock in his/her portfolio (and possibly 50-times that number of shares of a 
    //    Singaporean stock, and maybe just one single share of a Japanese stock), whereas a wealthy 
    //    individual might have 100-times as much or even more (never mind institutional investors, but 
    //    these entities will very probably use different software...)
    //    ==> ::TODO These numbers *must* be extracted into a config. file ASAP, whereas the above 
    //    factor *should* (but in fact can wait a little). 
    // ::MAGIC
    private static FixedPointNumber SPLIT_NOF_ADD_SHARES_MIN = new FixedPointNumber("1");
    private static FixedPointNumber SPLIT_NOF_ADD_SHARES_MAX = new FixedPointNumber("99999");

    // ---------------------------------------------------------------
    
    /**
     * Generates a transaction that buys a given number of stocks  
     * for a specific security's stock account at a given price, 
     * and generates additional splits for taxes/fees
     * (simple variant).
     * 
     * @param kmmFile KMyMoney file
     * @param stockAcctID ID the the stock account
     * @param taxFeeAcctID ID of the expenses account for the taxes/fees
     * @param offsetAcctID ID of the offsetting account
     * (the account that the gross amount will be debited to).
     * @param nofStocks no. of stocks bought
     * @param stockPrc stock price (net)
     * @param taxesFees taxes/fees
     * @param postDate post date for transaction
     * @param descr description of the transaction
     * @return a newly generated, modifiable transaction object
     * 
     * @see #genBuyStockTrx(KMyMoneyWritableFileImpl, KMMAcctID, Collection, KMMAcctID, FixedPointNumber, FixedPointNumber, LocalDate, String)
     */
    public static KMyMoneyWritableTransaction genBuyStockTrx(
    		final KMyMoneyWritableFileImpl kmmFile,
    		final KMMAcctID stockAcctID,
    		final KMMAcctID taxFeeAcctID,
    		final KMMAcctID offsetAcctID,
    		final FixedPointNumber nofStocks,
    		final FixedPointNumber stockPrc,
    		final FixedPointNumber taxesFees,
    		final LocalDate postDate,
    		final String descr) {
    	Collection<AcctIDAmountPair> expensesAcctAmtList = new ArrayList<AcctIDAmountPair>();
	
    	AcctIDAmountPair newPair = new AcctIDAmountPair(taxFeeAcctID, taxesFees);
    	expensesAcctAmtList.add(newPair);
	
    	return genBuyStockTrx(kmmFile, 
    				stockAcctID, expensesAcctAmtList, offsetAcctID, 
    				nofStocks, stockPrc, 
    				postDate, descr);	
    }
    
    /**
     * Generates a transaction that buys a given number of stocks
     * for a specific security's stock account at a given price, 
     * and generates additional splits for taxes/fees
     * (general variant).
     * 
     * @param kmmFile KMyMoney file
     * @param stockAcctID ID the the stock account
     * @param expensesAcctAmtList list of pairs (acctID/amount)
     * that represents all taxes / fees for this transaction
     * (the account-IDs being the IDs of the according expenses
     * accounts)  
     * @param offsetAcctID ID of the offsetting account
     * (the account that the gross amount will be debited to).
     * @param nofStocks no. of stocks bought
     * @param stockPrc stock price (net)
     * @param postDate post date for transaction
     * @param descr description of the transaction
     * @return a newly generated, modifiable transaction object
     * 
     * @see #genBuyStockTrx(KMyMoneyWritableFileImpl, KMMAcctID, KMMAcctID, KMMAcctID, FixedPointNumber, FixedPointNumber, FixedPointNumber, LocalDate, String)
     */
    public static KMyMoneyWritableTransaction genBuyStockTrx(
    		final KMyMoneyWritableFileImpl kmmFile,
    		final KMMAcctID stockAcctID,
    		final Collection<AcctIDAmountPair> expensesAcctAmtList,
    		final KMMAcctID offsetAcctID,
    		final FixedPointNumber nofStocks,
    		final FixedPointNumber stockPrc,
    		final LocalDate postDate,
    		final String descr) {
    	if ( kmmFile == null ) {
    		throw new IllegalArgumentException("null KMyMoney file given");
    	}
		
    	if ( stockAcctID == null ||
    		 offsetAcctID == null ) {
    		throw new IllegalArgumentException("null account ID given");
    	}
	
    	if ( ! ( stockAcctID.isSet()  ) ||
    		 ! ( offsetAcctID.isSet() ) ) {
    		throw new IllegalArgumentException("unset account ID given");
    	}
		
    	if ( expensesAcctAmtList == null ) {
    		throw new IllegalArgumentException("null expenses account list given");
    	}
			
    	if ( expensesAcctAmtList.isEmpty() ) {
    		throw new IllegalArgumentException("empty expenses account list given");
    	}
			
    	for ( AcctIDAmountPair elt : expensesAcctAmtList ) {
    		if ( ! elt.isNotNull() ) {
    			throw new IllegalArgumentException("null expenses account list element given");
    		}
    		if ( ! elt.isSet() ) {
    			throw new IllegalArgumentException("unset expenses account list element given");
    		}
    	}

    	if ( nofStocks == null ||
    		 stockPrc == null ) {
    		throw new IllegalArgumentException("null amount given");
    	}
		
    	if ( nofStocks.doubleValue() <= 0.0 ) {
    		throw new IllegalArgumentException("number of stocks <= 0.0 given");
    	}
			
    	if ( stockPrc.doubleValue() <= 0.0 ) {
    		throw new IllegalArgumentException("stock price <= 0.0 given");
    	}
	
    	for ( AcctIDAmountPair elt : expensesAcctAmtList ) {
    		if ( elt.amount().doubleValue() <= 0.0 ) {
    			throw new IllegalArgumentException("expense <= 0.0 given");
    		}
    	}

    	LOGGER.debug("genBuyStockTrx: Account 1 name (stock):      '" + kmmFile.getAccountByID(stockAcctID).getQualifiedName() + "'");
    	int counter = 1;
    	for ( AcctIDAmountPair elt : expensesAcctAmtList ) {
    		LOGGER.debug("genBuyStockTrx: Account 2." + counter + " name (expenses): '" + kmmFile.getAccountByID(elt.accountID()).getQualifiedName() + "'");
    		counter++;
    	}
    	LOGGER.debug("genBuyStockTrx: Account 3 name (offsetting): '" + kmmFile.getAccountByID(offsetAcctID).getQualifiedName() + "'");

    	// ---
    	// Check account types
    	KMyMoneyAccount stockAcct  = kmmFile.getAccountByID(stockAcctID);
    	if ( stockAcct.getType() != KMyMoneyAccount.Type.STOCK ) {
    		throw new IllegalArgumentException("Account with ID " + stockAcctID + " is not of type " + KMyMoneyAccount.Type.STOCK);
    	}

    	for ( AcctIDAmountPair elt : expensesAcctAmtList ) {
    		KMyMoneyAccount expensesAcct = kmmFile.getAccountByID(elt.accountID());
    		if ( expensesAcct.getType() != KMyMoneyAccount.Type.EXPENSE ) {
    			throw new IllegalArgumentException("Account with ID " + elt.accountID() + " is not of type " + KMyMoneyAccount.Type.EXPENSE);
    		}
    	}
	
    	KMyMoneyAccount offsetAcct = kmmFile.getAccountByID(offsetAcctID);
    	if ( offsetAcct.getType() != KMyMoneyAccount.Type.CHECKING ) {
    		throw new IllegalArgumentException("Account with ID " + offsetAcctID + " is not of type " + KMyMoneyAccount.Type.CHECKING);
    	}

    	// ---

    	FixedPointNumber amtNet   = nofStocks.copy().multiply(stockPrc);
    	LOGGER.debug("genBuyStockTrx: Net amount: " + amtNet);

    	FixedPointNumber amtGross = amtNet.copy();
    	for ( AcctIDAmountPair elt : expensesAcctAmtList ) {
    		amtGross.add(elt.amount());
    	}
    	LOGGER.debug("genBuyStockTrx: Gross amount: " + amtGross);

    	// ---

    	KMyMoneyWritableTransaction trx = kmmFile.createWritableTransaction();
    	// Does not work like that: The description/memo on transaction
    	// level is purely internal:
    	// trx.setDescription(description);
    	trx.setMemo("Generated by SecuritiesAccountTransactionManager, " + LocalDateTime.now());

    	// ---

    	KMyMoneyWritableTransactionSplit splt1 = trx.createWritableSplit(offsetAcct);
    	splt1.setValue(amtGross.copy().negate());
    	splt1.setShares(amtGross.copy().negate());
    	// splt3.setPrice("1/1"); // completely optional
    	// This is what we actually want (cf. above):
    	splt1.setMemo(descr); // sic, only here
    	LOGGER.debug("genBuyStockTrx: Split 1 to write: " + splt1.toString());

    	// ---
	
    	KMyMoneyWritableTransactionSplit splt2 = trx.createWritableSplit(stockAcct);
    	splt2.setValue(amtNet);
    	splt2.setShares(nofStocks);
    	splt2.setPrice(stockPrc); // optional (sic), but advisable
    	splt2.setAction(KMyMoneyTransactionSplit.Action.BUY_SHARES);
    	LOGGER.debug("genBuyStockTrx: Split 2 to write: " + splt2.toString());

    	// ---

    	counter = 1;
    	for ( AcctIDAmountPair elt : expensesAcctAmtList ) {
    		KMyMoneyAccount expensesAcct = kmmFile.getAccountByID(elt.accountID());
    		KMyMoneyWritableTransactionSplit splt3 = trx.createWritableSplit(expensesAcct);
    		splt3.setValue(elt.amount());
    		splt3.setShares(elt.amount());
    		// splt3.setPrice("1/1"); // completely optional
    		LOGGER.debug("genBuyStockTrx: Split 3." + counter + " to write: " + splt3.toString());
    		counter++;
    	}

    	// ---

    	trx.setDatePosted(postDate);
    	trx.setDateEntered(LocalDate.now());

    	// ---

    	LOGGER.info("genBuyStockTrx: Generated new Transaction: " + trx.getID());
    	return trx;
    }
    
    // ---------------------------------------------------------------
    
    /**
     * Generates a transaction for a dividend or distribution
     * from a specific security's stock account, and generates additional 
     * splits for taxes/fees.
     * (simple variant).
     * 
     * @param kmmFile KMyMoney file
     * @param stockAcctID ID of the stock account
     * @param incomeAcctID ID of the income account
     * @param taxFeeAcctID ID of the expenses account for the taxes/fees
     * @param offsetAcctID ID of the offsetting account (the one that
     * the net amount will be credited to)
     * @param spltAct action type of the split that will point to the 
     * stock account (dividend or distribution)
     * @param divDistrGross gross dividend / distribution
     * @param taxesFees taxes/fees
     * @param postDate post date of the transaction
     * @param descr description of the transaction
     * @return a newly generated, modifiable transaction object
     */
    public static KMyMoneyWritableTransaction genDividDistribTrx(
    		final KMyMoneyWritableFileImpl kmmFile,
    		final KMMAcctID stockAcctID,
    		final KMMAcctID incomeAcctID,
    		final KMMAcctID taxFeeAcctID,
    		final KMMAcctID offsetAcctID,
    	    final KMyMoneyTransactionSplit.Action spltAct,
    		final FixedPointNumber divDistrGross,
    		final FixedPointNumber taxesFees,
    		final LocalDate postDate,
    		final String descr) {
    	Collection<AcctIDAmountPair> expensesAcctAmtList = new ArrayList<AcctIDAmountPair>();
	
    	AcctIDAmountPair newPair = new AcctIDAmountPair(taxFeeAcctID, taxesFees);
    	expensesAcctAmtList.add(newPair);

    	return genDividDistribTrx(kmmFile,
    				stockAcctID, incomeAcctID, expensesAcctAmtList, offsetAcctID, 
    				spltAct, divDistrGross,
    				postDate, descr);
    }
    
    /**
     * Generates a transaction for a dividend or distribution
     * from a specific security's stock account, and generates additional 
     * splits for taxes/fees.
     * (general variant).
     * 
     * @param kmmFile KMyMoney file
     * @param stockAcctID ID of the stock account
     * @param incomeAcctID ID of the income account
     * @param expensesAcctAmtList list of pairs (acctID/amount) 
     * that represents all taxes / fees for this transaction
     * (the account-IDs being the IDs of the according expenses
     * accounts)  
     * @param offsetAcctID ID of the offsetting account (the one that
     * the net amount will be credited to)
     * @param spltAct action type of the split that will point to the 
     * stock account (dividend or distribution)
     * @param divDistrGross gross dividend / distribution
     * @param postDate post date of the transaction
     * @param descr description of the transaction
     * @return a newly generated, modifiable transaction object
     */
    public static KMyMoneyWritableTransaction genDividDistribTrx(
    		final KMyMoneyWritableFileImpl kmmFile,
    		final KMMAcctID stockAcctID,
    		final KMMAcctID incomeAcctID,
    		final Collection<AcctIDAmountPair> expensesAcctAmtList,
    		final KMMAcctID offsetAcctID,
    	    final KMyMoneyTransactionSplit.Action spltAct,
    		final FixedPointNumber divDistrGross,
    		final LocalDate postDate,
    		final String descr) {
    	if ( kmmFile == null ) {
    		throw new IllegalArgumentException("null KMyMoney file given");
    	}

    	if ( stockAcctID == null ||
    		 incomeAcctID == null ||
    		 offsetAcctID == null ) {
    		throw new IllegalArgumentException("null account ID given");
    	}

    	if ( ! ( stockAcctID.isSet() ) ||
    		 ! ( incomeAcctID.isSet() ) ||
    		 ! ( offsetAcctID.isSet() ) ) {
    		throw new IllegalArgumentException("unset account ID given");
    	}

    	if ( expensesAcctAmtList == null ) {
    		throw new IllegalArgumentException("null expenses account list given");
    	}

    	// CAUTION: Yes, this actually happens in real life, e.g. with specifics 
    	// of German tax law (Freibetrag, Kapitalausschuettung).
    	// ==> The following check is commented out on purpose.
//    	if ( expensesAcctAmtList.isEmpty() ) {
//    	    throw new IllegalArgumentException("empty expenses account list given");
//    	}
    			
    	for ( AcctIDAmountPair elt : expensesAcctAmtList ) {
    		if ( ! elt.isNotNull() ) {
    			throw new IllegalArgumentException("null expenses account list element given");
    		}
    		if ( ! elt.isSet() ) {
    			throw new IllegalArgumentException("unset expenses account list element given");
    		}
    	}

    	if ( divDistrGross == null ) {
    		throw new IllegalArgumentException("null gross dividend/distribution given");
    	}

	// CAUTION: The following two: In fact, this can happen
	// (negative booking after cancellation / Stornobuchung)
//	if ( divGross.doubleValue() <= 0.0 ) {
//	    throw new IllegalArgumentException("gross dividend <= 0.0 given");
//	}
//				
//	for ( AcctIDAmountPair elt : expensesAcctAmtList ) {
//	    if ( elt.amount().doubleValue() <= 0.0 ) {
//		throw new IllegalArgumentException("expense <= 0.0 given");
//	    }
//	}

    	LOGGER.debug("genDividDistribTrx: Account 1 name (stock):      '" + kmmFile.getAccountByID(stockAcctID).getQualifiedName() + "'");
    	LOGGER.debug("genDividDistribTrx: Account 2 name (income):     '" + kmmFile.getAccountByID(incomeAcctID).getQualifiedName() + "'");
    	int counter = 1;
    	for ( AcctIDAmountPair elt : expensesAcctAmtList ) {
    		LOGGER.debug("genDividDistribTrx: Account 3." + counter + " name (expenses): '" + kmmFile.getAccountByID(elt.accountID()).getQualifiedName() + "'");
    		counter++;
    	}
    	LOGGER.debug("genDividDistribTrx: Account 4 name (offsetting): '" + kmmFile.getAccountByID(offsetAcctID).getQualifiedName() + "'");

    	// ---
    	// Check account types
    	KMyMoneyAccount stockAcct  = kmmFile.getAccountByID(stockAcctID);
    	if ( stockAcct.getType() != KMyMoneyAccount.Type.STOCK ) {
    		throw new IllegalArgumentException("Account with ID " + stockAcctID + " is not of type " + KMyMoneyAccount.Type.STOCK);
    	}

    	KMyMoneyAccount incomeAcct = kmmFile.getAccountByID(incomeAcctID);
    	if ( incomeAcct.getType() != KMyMoneyAccount.Type.INCOME ) {
    		throw new IllegalArgumentException("Account with ID " + incomeAcct + " is not of type " + KMyMoneyAccount.Type.INCOME);
    	}

    	for ( AcctIDAmountPair elt : expensesAcctAmtList ) {
    		KMyMoneyAccount expensesAcct = kmmFile.getAccountByID(elt.accountID());
    		if ( expensesAcct.getType() != KMyMoneyAccount.Type.EXPENSE ) {
    			throw new IllegalArgumentException("Account with ID " + elt.accountID() + " is not of type " + KMyMoneyAccount.Type.EXPENSE);
    		}
    	}
	
    	KMyMoneyAccount offsetAcct = kmmFile.getAccountByID(offsetAcctID);
    	if ( offsetAcct.getType() != KMyMoneyAccount.Type.CHECKING ) {
    		throw new IllegalArgumentException("Account with ID " + offsetAcctID + " is not of type " + KMyMoneyAccount.Type.CHECKING);
    	}

    	// ---

    	FixedPointNumber expensesSum = new FixedPointNumber();
    	for ( AcctIDAmountPair elt : expensesAcctAmtList ) {
    		expensesSum.add(elt.amount());
    	}
    	LOGGER.debug("genDividDistribTrx: Sum of all expenses: " + expensesSum);

    	FixedPointNumber divDistrNet = divDistrGross.copy().subtract(expensesSum);
    	LOGGER.debug("genDividDistribTrx: Net dividend: " + divDistrNet);

    	// ---

    	KMyMoneyWritableTransaction trx = kmmFile.createWritableTransaction();
    	// Does not work like that: The description/memo on transaction
    	// level is purely internal:
    	// trx.setDescription(descr);
    	// Instead:
    	trx.setMemo("Generated by SecuritiesAccountTransactionManager, " + LocalDateTime.now());

    	// ---
	
    	KMyMoneyWritableTransactionSplit splt1 = trx.createWritableSplit(stockAcct);
    	splt1.setValue(new FixedPointNumber());
    	splt1.setShares(new FixedPointNumber());
    	splt1.setAction(KMyMoneyTransactionSplit.Action.DIVIDEND);
    	// splt1.setPrice("1/1"); // completely optional
    	LOGGER.debug("genDividDistribTrx: Split 1 to write: " + splt1.toString());

    	// ---

    	KMyMoneyWritableTransactionSplit splt2 = trx.createWritableSplit(offsetAcct);
    	splt2.setValue(divDistrNet);
    	splt2.setShares(divDistrNet);
    	// splt2.setPrice("1/1"); // completely optional
    	// This is what we actually want (cf. above):
    	splt2.setMemo(descr); // sic, only here
    	LOGGER.debug("genDividDistribTrx: Split 2 to write: " + splt2.toString());

    	// ---

    	KMyMoneyWritableTransactionSplit splt3 = trx.createWritableSplit(incomeAcct);
    	splt3.setValue(divDistrGross.copy().negate());
    	splt3.setShares(divDistrGross.copy().negate());
    	// splt3.setPrice("1/1"); // completely optional
    	LOGGER.debug("genDividDistribTrx: Split 3 to write: " + splt3.toString());

    	// ---

    	counter = 1;
    	for ( AcctIDAmountPair elt : expensesAcctAmtList ) {
    		KMyMoneyAccount expensesAcct = kmmFile.getAccountByID(elt.accountID());
    		KMyMoneyWritableTransactionSplit splt4 = trx.createWritableSplit(expensesAcct);
    		splt4.setValue(elt.amount());
    		splt4.setShares(elt.amount());
    		// splt4.setPrice("1/1"); // completely optional
    		LOGGER.debug("genDividDistribTrx: Split 4." + counter + " to write: " + splt4.toString());
    		counter++;
    	}

    	// ---

    	trx.setDatePosted(postDate);
    	trx.setDateEntered(LocalDate.now());

    	// ---

    	LOGGER.info("genDividDistribTrx: Generated new Transaction: " + trx.getID());
    	return trx;
    }

    // ---------------------------------------------------------------
    
    public static KMyMoneyWritableTransaction genStockSplitTrx(
    		final KMyMoneyWritableFileImpl kmmFile,
    		final KMMAcctID stockAcctID,
    		final StockSplitVar var,
    		final FixedPointNumber factorOfNofAddShares,
    		final LocalDate postDate,
    		final String descr) {
    	if ( var == StockSplitVar.FACTOR ) {
    		return genStockSplitTrx_factor(kmmFile, 
    									   stockAcctID, factorOfNofAddShares, 
    									   postDate, descr);
    	} else if ( var == StockSplitVar.NOF_ADD_SHARES ) {
    		return genStockSplitTrx_nofShares(kmmFile,
    										  stockAcctID, factorOfNofAddShares,
    										  postDate, descr);
    	}

    	return null; // Compiler happy
    }
    
    /**
     * 
     * @param kmmFile
     * @param stockAcctID
     * @param factor E.g., the number 3.0 for a 3-for-1 split (a threefold increase of the number of shares), 
     * or the number 1/3 (0.333...) for a 1-for-3 reverse stock-split (the number of shares is decreased to a third).
     * 
     * <em>Caution:</em> The wording is not standardized, at least not internationally: 
     * In english-speaking countries, people tend to say "3-for-1" ("3 new shares for 1 old share") 
     * when they mean a threefold-increase of the stocks, whereas in Germany, e.g., it tends
     * to be the other way round, i.e. "Aktiensplit 1:4" ("eine alte zu 4 neuen Aktien") is a 
     * "4-for-1" split).
     * 
     * Also, please be aware that KMyMoney uses the former logic internally, but the latter 
     * logic on the GUI (i.e., a 2-for-1 split (factor 2) is saved as "2/1" in the KMyMoney
     * file, but the GUI will show "1/2").
     * @param postDate
     * @param descr
     * @return a new share-(reverse-)split transaction
     * 
     * @see #genStockSplitTrx_nofShares(KMyMoneyWritableFileImpl, KMMAcctID, FixedPointNumber, LocalDate, String)
     * @see #genStockSplitTrx(KMyMoneyWritableFileImpl, KMMAcctID, StockSplitVar, FixedPointNumber, LocalDate, String)
     */
    public static KMyMoneyWritableTransaction genStockSplitTrx_factor(
    		final KMyMoneyWritableFileImpl kmmFile,
    		final KMMAcctID stockAcctID,
    		final FixedPointNumber factor,
    		final LocalDate postDate,
    		final String descr) {
    	if ( kmmFile == null ) {
    		throw new IllegalArgumentException("null KMyMoney file given");
    	}
		
    	if ( stockAcctID == null  ) {
    		throw new IllegalArgumentException("null stock account ID given");
    	}
	
    	if ( ! ( stockAcctID.isSet() ) ) {
    		throw new IllegalArgumentException("unset stock account ID given");
    	}
		
    	if ( factor == null ) {
    		throw new IllegalArgumentException("null factor given");
    	}

    	if ( factor.isNegative() ) {
    		throw new IllegalArgumentException("negative factor given");
    	}

    	if ( factor.getBigDecimal().equals(BigDecimal.ZERO) ) {
    		throw new IllegalArgumentException("zero-value factor given");
    	}

    	// ::TODO: Reconsider: Should we really reject the input and throw an exception 
    	// (which is kind of overly strict), or shouldn't we rather just issue a warning?
    	if ( factor.isLessThan(SPLIT_FACTOR_MIN) ) {
    		throw new IllegalArgumentException("unplausible factor given (smaller than " + SPLIT_FACTOR_MIN + ")");
    	}

    	// ::TODO: cf. above
    	if ( factor.isGreaterThan(SPLIT_FACTOR_MAX) ) {
    		throw new IllegalArgumentException("unplausible factor given (greater than " + SPLIT_FACTOR_MAX + ")");
    	}

    	// ---
    	// Check account type
    	KMyMoneyAccount stockAcct  = kmmFile.getAccountByID(stockAcctID);
    	if ( stockAcct == null ) {
    		throw new IllegalStateException("Could not find account with that ID");
    	}

    	LOGGER.debug("genStockSplitTrx_factor: Stock account name: '" + stockAcct.getQualifiedName() + "'");
    	if ( stockAcct.getType() != KMyMoneyAccount.Type.STOCK ) {
    		throw new IllegalArgumentException("Account with ID " + stockAcctID + " is not of type " + KMyMoneyAccount.Type.STOCK);
    	}

    	// ---
    	
    	FixedPointNumber nofSharesOld = stockAcct.getBalance();
    	LOGGER.debug("genStockSplitTrx_factor: Old no. of shares: " + nofSharesOld);
    	if ( nofSharesOld.equals(BigDecimal.ZERO) ) {
    		throw new IllegalStateException("No. of old shares is zero. Cannot carry out a split.");
    	}
    	FixedPointNumber nofSharesNew = nofSharesOld.copy().multiply(factor);
    	LOGGER.debug("genStockSplitTrx_factor: New no. of shares: " + nofSharesNew);
    	FixedPointNumber nofAddShares = nofSharesNew.copy().subtract(nofSharesOld);
    	LOGGER.debug("genStockSplitTrx_factor: No. of add. shares: " + nofAddShares);
    	
    	// ---

    	KMyMoneyWritableTransaction trx = kmmFile.createWritableTransaction();
    	// Does not work like that: The description/memo on transaction
    	// level is purely internal:
    	// trx.setDescription(descr);
    	// Instead:
    	trx.setMemo("Generated by SecuritiesAccountTransactionManager, " + LocalDateTime.now());

    	// ---
    	// CAUTION: One single split
	
    	KMyMoneyWritableTransactionSplit splt = trx.createWritableSplit(stockAcct);
    	splt.setValue(new FixedPointNumber());
    	splt.setShares(new FixedPointNumber(factor));
		// splt.setPrice("1/1"); // completely optional
    	splt.setAction(KMyMoneyTransactionSplit.Action.SPLIT_SHARES);
    	splt.setMemo(descr);
    	LOGGER.debug("genStockSplitTrx_factor: Split 1 to write: " + splt.toString());

    	// ---

    	trx.setDatePosted(postDate);
    	trx.setDateEntered(LocalDate.now());

    	// ---

    	LOGGER.info("genStockSplitTrx_factor: Generated new Transaction: " + trx.getID());
    	return trx;
    }
    
    /**
     * 
     * @param kmmFile
     * @param stockAcctID
     * @param nofAddShares The number of additional shares to be added to the stock account.
     * E.g., when you have 100 shares and you add 200 more, then you have 300 shares, 
     * i.e. the number has increased by a factor of 3 (a 3-for-1 split).
     * Likewise, if you have 100 shares and you take away 75 of them (neg. no. of add. shares),
     * then you have 25 shares left, i.e. the number of shares as decreased by a factor
     * of 1/4 (1-for-4). 
     * 
     * Also, please be aware that GnuCash does not use the factor-logic, neither internally
     * nor on the GUI, but instead only shows and stores the number of additional shares.
     * @param postDate
     * @param descr
     * @return a new share-(reverse-)split transaction
     * 
     * @see #genStockSplitTrx_factor(KMyMoneyWritableFileImpl, KMMAcctID, FixedPointNumber, LocalDate, String)
     * @see #genStockSplitTrx(KMyMoneyWritableFileImpl, KMMAcctID, StockSplitVar, FixedPointNumber, LocalDate, String)
     */
    public static KMyMoneyWritableTransaction genStockSplitTrx_nofShares(
    	    final KMyMoneyWritableFileImpl kmmFile,
    	    final KMMAcctID stockAcctID,
    	    final FixedPointNumber nofAddShares, // use neg. number in case of reverse stock-split
    	    final LocalDate postDate,
    	    final String descr) {
    	if ( kmmFile == null ) {
    		throw new IllegalArgumentException("null KMyMoney file given");
    	}
		
    	if ( stockAcctID == null  ) {
    		throw new IllegalArgumentException("null stock account ID given");
    	}
	
    	if ( ! ( stockAcctID.isSet() ) ) {
    		throw new IllegalArgumentException("unset stock account ID given");
    	}
		
    	if ( nofAddShares == null ) {
    		throw new IllegalArgumentException("null no. of add. shares given");
    	}

    	// CAUTION: Neg. no. of add. shares is allowed!
//    	if ( nofAddShares.isNegative() ) {
//    		throw new IllegalArgumentException("negative no. of add. shares given");
//    	}

    	if ( nofAddShares.getBigDecimal().equals(BigDecimal.ZERO) ) {
    		throw new IllegalArgumentException("zero-value no. of add. shares given");
    	}

    	FixedPointNumber nofAddSharesAbs = new FixedPointNumber( nofAddShares.copy().abs() );
    	
    	// ::TODO: Reconsider: Should we really reject the input and throw an exception 
    	// (which is kind of overly strict), or shouldn't we rather just issue a warning?
    	if ( nofAddSharesAbs.isLessThan(SPLIT_NOF_ADD_SHARES_MIN) ) {
    		throw new IllegalArgumentException("unplausible no. of add. shares given (abs. smaller than " + SPLIT_NOF_ADD_SHARES_MIN + ")");
    	}

    	// ::TODO: Cf. above
    	if ( nofAddSharesAbs.isGreaterThan(SPLIT_NOF_ADD_SHARES_MAX) ) {
    		throw new IllegalArgumentException("unplausible no. of add. shares (abs. greater than " + SPLIT_NOF_ADD_SHARES_MAX + ")");
    	}

    	// CAUTION: Yes, it actually *is* possible that the no. of add. shares
    	// is not an integer: If the old no. of shares is non-int as well (and yes,
    	// that can actually be the case, not just theoretically, but in practice!)
//    	// Check if no. of add. shares is integer
//    	// https://stackoverflow.com/questions/1078953/check-if-bigdecimal-is-an-integer-in-java
//    	if ( nofAddShares.stripTrailingZeros().scale() <= 0 ) {
//    		throw new IllegalArgumentException("no. of add. shares given is not integer value");
//    	}

    	// ---
    	// Check account type
    	KMyMoneyAccount stockAcct  = kmmFile.getAccountByID(stockAcctID);
    	if ( stockAcct == null ) {
    		throw new IllegalStateException("Could not find account with that ID");
    	}

    	LOGGER.debug("genStockSplitTrx_nofShares: Stock account name: '" + stockAcct.getQualifiedName() + "'");
    	if ( stockAcct.getType() != KMyMoneyAccount.Type.STOCK ) {
    		throw new IllegalArgumentException("Account with ID " + stockAcctID + " is not of type " + KMyMoneyAccount.Type.STOCK);
    	}

    	// ---
    	
    	FixedPointNumber nofSharesOld = stockAcct.getBalance();
    	LOGGER.debug("genStockSplitTrx_nofShares: Old no. of shares: " + nofSharesOld);
    	if ( nofSharesOld.equals(BigDecimal.ZERO) ) {
    		throw new IllegalStateException("No. of old shares is zero. Cannot carry out a split.");
    	}
    	FixedPointNumber nofSharesNew = nofSharesOld.copy().add(nofAddShares);
    	LOGGER.debug("genStockSplitTrx_nofShares: New no. of shares: " + nofSharesNew);
    	FixedPointNumber factor = nofSharesNew.copy().divide(nofSharesOld);
    	LOGGER.debug("genStockSplitTrx_nofShares: Factor: " + factor);
    	
    	// ---
    	
    	return genStockSplitTrx_factor(kmmFile, 
    								   stockAcctID, factor, postDate, 
    								   descr);
    }
    
}
