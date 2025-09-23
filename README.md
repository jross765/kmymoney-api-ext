# Notes on the Module "API Extensions"

This module provides simplified, high-level access functions to a 
KMyMoney 
file via the "API" module (sort of "macros") for specialized, complex tasks.

## Sub-Modules
Currently, the module consists of two sub-modules:

* "SecAcct"
* "TrxMgr"

### SecAcct
This sub-module contains classes that provide a simplified, high-level interface for...

* generating and maintaining stock accounts,
* generating buy- and dividend/distribution transactions in a securities account (brokerage account).

### TrxMgr
This sub-module contains classes that help to...

* find transaction and splits by setting filter criteria,
* merge stock account transcations,
* generally manipulate transactions in a more convenient way than by using the pure API.

## Major Changes
### V. 0.7 &rarr; 0.8
None (not in this module).

### V. 0.6 &rarr; 0.7
* Added sub-module TrxMgr.
  * New: `Transaction(Split)Filter`
  * New: `TransactionFinder`
  * New: `TransactionManager`, `TransactionMergerXYZ` (the latter in two variants)

* Extended sub-module SecAcct:
  * `SecuritiesAccountTransactionManager`: new type "distribution" (as opposed to "dividend"). 
    As opposed to the sister project, this effectively *does not* lead to any difference in the generated transaction: 
    One of the splits generated will have another split action, as intended,
    but it won't make any difference.

### V. 0.5 &rarr; 0.6
* Sub-module SecAcct:
  * Added support for stock splits / reverse splits.
  * Added helper class that filters out inactive stock accounts.
  * Added `WritableSecuritiesAccountManager` (analogous to separation in module "API").

### V. 0.4 &rarr; 0.5
Created module.

## Planned
* Sub-module SecAcct: 
	* More variants of buy/sell/dividend/etc. transactions, including wrappers which you provide account names to instead of account IDs.
	* Possibly new class for high-level consistency checks of existing transactions, e.g.: All dividends of domestic shares are actually posted to the domestic dividend account.

* New sub-module for accounting-macros, such as closing the books.

    **Note**: Will have to re-think this, because, altough equity accounts
    (in the accounting sense of the word, not as a badly-chosen synonym for 
    stock accounts) do exist in KMyMoney, they do not seem to be used 
    in completely the same way as in GnuCash (it's a *personal finance* software,
    after all...) -- "closing the books", altough technically possible, might be 
    counter-productive here.

* New sub-module for management of securities and currencies (esp. bulk quote import).

## Known Issues
(None)

