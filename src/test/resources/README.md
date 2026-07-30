# Technical Aspects
The test files have been generated with 
KMyMoney V. 5.2.2.

When you change one of them, please save it in *compressed* XML format (as opposed to module "API (Core)").

# Testing Aspects
Please be careful when making changes on the file: All JUnit test cases of this module heavily depend on it, and you might break things.

## The "Standard" Test File
The file `test.kmy` is the "standard" test file that is used in (almost) all test cases.

## The Merger Test Files
The files `test_mrg_xyz.kmy` are written especially for testing the transaction mergers in package "TrxMgr". Currently, only variant 3 of the merger is tested with them (as it is the most complicated one of them), but the other two will follow.

For more details, cf. file `test_mrg.ods`.

# Comparison to Other Modules' Test Files
This test file *originated* from the one of module "API (Core)", but it is *not identical* to it.

Main differences:

* **Format**: This module's file is compressed 
  (as usual with KMyMoney).
* **Content**: A few things added, a few things changed, all specific to this module's test cases. The rest is identical.

This is no coincidence, of course, because until 
V. 0.8, 
we had both modules' JUnit test cases run on one single test data file -- *the* test data file.

However, for organizational reasons, we now 
(i.e, V. 0.8-RESTRUCT and onwards) 
have a separate, redundant copy for this module. Therefore, the two files will very likely not stay identical. Please expect them to divert from one another in the course of the releases to come.

