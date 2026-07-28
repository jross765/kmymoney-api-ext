# Major Changes

## V. 0.9 &rarr; 0.10
Followed the deprecation of `FixedPointNumber` in the modules
"(Core) API", V. 0.10 and 
"Specialized Entitites", V. 0.4: 

* Deprecated everything that is `FixedPointNumber`-related (cf. previous release).

* Partially changed implementations so that `BigFraction` is used internally instead of `FixedPointNumber`.

* New functionality in package TrxMgr.

* Usual maintenance: Fixed small bugs, small improvements, low-level code-cleaning.

In more detail:

* Package SecAcct: Small improvements.

* Package TrxMgr: 
  * `SecuritiesAccountTransactionManager_[BF|FP]`:
     * Added method `genSellStockTrx()` (both variants).
     * Used newly-introduced types `KMyMoney(Writable)Stock[Buy|Sell]Transaction`.
     * A number of small improvements.

  * Added third variant of transaction merger (the "KMyMoney-way"):
    * `TransactionMergerVar3`: New
    
      Works (more or less, but not exactly) as defined by the regular 
      KMyMoney workflow; every transaction merge of this variant has to be 
      confirmed later by the user (using the standard GUI).

## V. 0.8 &rarr; 0.9
Adapted to module "Base", V. 0.9 and "API Specialized Entities", V. 0.3.

In more detail:

* Package SecAcct:
  * `SecuritiesAccountManager`: 
     * Added more variants of method `getShareAccounts()`
     * Changed logic of method `getActiveShareAccounts()`
       (now additianally checks for account being hidden).
  * `SecuritiesAccountTransactionManager`: Changed interface:
    Transaction-generating methods now return the according specialized
    entities from module "API Specialized Entities", 
    as you would expect.

* Package TrxMgr:
  
  Nothing

## V. 0.7 &rarr; 0.8
(TODO)

## V. 0.6 &rarr; 0.7
* Added package TrxMgr.
  * New: `Transaction(Split)Filter`
  * New: `TransactionFinder`
  * New: `TransactionManager`, `TransactionMergerXYZ` (the latter in two variants)

* Extended package SecAcct:
  * `SecuritiesAccountTransactionManager`: new type "distribution" (as opposed to "dividend"). 
    As opposed to the sister project, this effectively *does not* lead to any difference in the generated transaction: 
    One of the splits generated will have another split action, as intended,
    but it won't make any difference.

## V. 0.5 &rarr; 0.6
* Package SecAcct:
  * Added support for stock splits / reverse splits.
  * Added helper class that filters out inactive stock accounts.
  * Added `WritableSecuritiesAccountManager` (analogous to separation in module "API").

## V. 0.4 &rarr; 0.5
Created module.
