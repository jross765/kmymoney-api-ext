# Technical Aspects
The test file has been generated with 
KMyMoney V. 5.2.1.

When you change the test.xml file, please save it in *uncompressed* XML format 
(by convention, compressed 
KMyMoney
files have the extension 
"kmy").

# Testing Aspects
Please be careful when making changes on the file: All JUnit test cases of this module heavily rely on it, and you might break things.

# Comparison to Other Modules' Test Files
This test file currently is *identical* to the one of module "API".

This is no coincidence, of course, because until 
V. 0.8, 
we had both modules' JUnit test cases run on one single test data file -- *the* test data file.

However, for organizational reasons, we now 
(i.e, V. 0.8-RESTRUCT and onwards) 
have a separate, redundant copy for this module. Therefore, the two files will very likely not stay identical. Please expect them to divert from one another in the course of the releases to come.
