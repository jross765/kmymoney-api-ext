module kmymoney.apiext {

	requires static org.slf4j;
	requires java.desktop;
	
	// ----------------------------

	requires transitive schnorxoborx.schnorxolib;
	
	requires transitive kmymoney.base;
	requires transitive kmymoney.api;
	requires transitive kmymoney.apispec;
	requires org.apache.commons.text;

	// ----------------------------

	exports org.kmymoney.apiext.secacct;
	exports org.kmymoney.apiext.trxmgr;

}
