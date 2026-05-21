# Notes on the Module "API Extensions"

## What Does It Do?

This module provides simplified, high-level access functions to a 
KMyMoney 
file via the modules "API (Core)" and "API Specialized Entities".
They constitute sort of "macros" for specialized, complex tasks.

## Packages
Currently, the module consists of two packages:

* "SecAcct"
* "TrxMgr"

### SecAcct
This package contains classes that provide a simplified, high-level interface for...

* generating and maintaining stock accounts,
* generating buy- and dividend/distribution transactions in a securities account (brokerage account).

### TrxMgr
This package contains classes that help to...

* find transaction and splits by setting filter criteria,
* merge stock account transcations,
* generally manipulate transactions in a more convenient way than by using the pure API.

## What is This Repo's Relationship with the Other Repos?

* This is a module-level repository which is part of a multi-module project, i.e. it has a parent and several siblings. 

  [Parent](https://github.com/jross765/JKMyMoneyLibNTools.git)

* Under normal circumstances, you cannot compile it on its own (at least not without further preparation), but instead, you should clone it together with the other repos and use the parent repo's build-script.

* This repository contains no history before V. 0.8 (cf. notes in parent repo).

## Major Changes

Cf. document "[Major Changes](https://github.com/jross765/JKMyMoneyLibNTools/kmymoney-api-ext/major_changes.md)".

## Planned
* Package SecAcct: 
	* More variants of buy/sell/dividend/etc. transactions, including wrappers which you provide account names to instead of account IDs.
	* Possibly new class for high-level consistency checks of existing transactions, e.g.: All dividends of domestic shares are actually posted to the domestic dividend account.

* Package TrxMgr: 
   * Add new variant for merging transactions: the one that is actually used by KMyMoney.

* New package for accounting-macros, such as closing the books.

    **Note**: Will have to re-evaluate this, because, altough equity accounts
    (in the accounting sense of the word, not as a badly-chosen synonym for 
    stock accounts) do exist in KMyMoney, they do not seem to be used 
    in completely the same way as in GnuCash (it's a *personal finance* software,
    after all...) -- "closing the books", altough technically possible, might be 
    counter-productive here.

* New package for management of securities and currencies (esp. bulk quote import).

## Known Issues

### Package SecAcct
* The specialized entities are built-up "manually", as the according entities' design and 
  implementation (module "API Specialized Entities") assumes that the transaction passed 
  to the contructor is already built -- at least to the point that the validation test passes.

### Package TrxMgr
(Nothing)

