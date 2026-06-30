package org.kmymoney.apiext.trxmgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.apiext.ConstTest;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

import junit.framework.JUnit4TestAdapter;

public class TestTransactionMergerVar3 {

	private static final KMMTrxID TRX_1_ID = new KMMTrxID("T000000000000000001");

	// -----------------------------------------------------------------

	private KMyMoneyWritableFile kmmFile = null;
	private KMyMoneyTransaction trx = null;

	private TransactionMergerVar3 mrgMgr = null;
	
	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestTransactionMergerVar3.class);
	}

	@Before
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL kmmFileURL = classLoader.getResource(Const.KMM_FILENAME);
		// System.err.println("KMyMoney test file resource: '" + kmmFileURL + "'");
		URL kmmFileURL = null;
		File kmmFileRaw = null;
		try {
			kmmFileURL = classLoader.getResource(ConstTest.KMM_FILENAME);
			kmmFileRaw = new File(kmmFileURL.getFile());
		} catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			return;
		}

		try {
			kmmFile = new KMyMoneyWritableFileImpl(kmmFileRaw);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney file");
			exc.printStackTrace();
		}
		
		mrgMgr = new TransactionMergerVar3(kmmFile);
	}

	// -----------------------------------------------------------------

	@Test
	public void test01_1() throws Exception {
		trx = kmmFile.getTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx);

		// System.err.println("'" + TransactionMergerVar3.getDierKMMContainerString(trx, false) + "'");
		assertEquals("""
				<!DOCTYPE MATCH><CONTAINER><TRANSACTION id="T000000000000000001" commodity="EUR" entrydate="2023-11-03" postdate="2023-01-01" memo="">    <SPLITS>        <SPLIT account="A000004" action="" bankid="" id="S0001" memo="" number="" payee="P000004" price="1/1" reconciledate="" reconcileflag="0" shares="10000/1" value="10000/1"/>        <SPLIT account="A000053" action="" bankid="" id="S0002" memo="" number="" payee="P000004" price="1/1" reconciledate="" reconcileflag="0" shares="-10000/1" value="-10000/1"/>    </SPLITS></TRANSACTION></CONTAINER>""", 
				TransactionMergerVar3.getDierKMMContainerString(trx, false));

		// System.err.println("'" + TransactionMergerVar3.getDierKMMContainerString(trx, true) + "'");
		assertEquals("""
				<!DOCTYPE MATCH><CONTAINER><TRANSACTION id="" commodity="EUR" entrydate="2023-11-03" postdate="2023-01-01" memo="">    <SPLITS>        <SPLIT account="A000004" action="" bankid="" id="S0001" memo="" number="" payee="P000004" price="1/1" reconciledate="" reconcileflag="0" shares="10000/1" value="10000/1"/>        <SPLIT account="A000053" action="" bankid="" id="S0002" memo="" number="" payee="P000004" price="1/1" reconciledate="" reconcileflag="0" shares="-10000/1" value="-10000/1"/>    </SPLITS></TRANSACTION></CONTAINER>""", 
				TransactionMergerVar3.getDierKMMContainerString(trx, true));
	}

	@Test
	public void test01_2() throws Exception {
		trx = kmmFile.getTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx);

		// System.err.println("'" + TransactionMergerVar3.getDierKMMContainerEscapedString(trx, false) + "'");
		assertEquals("&amp;#60;!DOCTYPE MATCH&gt;&#10;&amp;#60;CONTAINER&gt;&#10;&amp;#60;TRANSACTION id=&quot;T000000000000000001&quot; commodity=&quot;EUR&quot; entrydate=&quot;2023-11-03&quot; postdate=&quot;2023-01-01&quot; memo=&quot;&quot;&gt;&#10;    &amp;#60;SPLITS&gt;&#10;        &amp;#60;SPLIT account=&quot;A000004&quot; action=&quot;&quot; bankid=&quot;&quot; id=&quot;S0001&quot; memo=&quot;&quot; number=&quot;&quot; payee=&quot;P000004&quot; price=&quot;1/1&quot; reconciledate=&quot;&quot; reconcileflag=&quot;0&quot; shares=&quot;10000/1&quot; value=&quot;10000/1&quot;/&gt;&#10;        &amp;#60;SPLIT account=&quot;A000053&quot; action=&quot;&quot; bankid=&quot;&quot; id=&quot;S0002&quot; memo=&quot;&quot; number=&quot;&quot; payee=&quot;P000004&quot; price=&quot;1/1&quot; reconciledate=&quot;&quot; reconcileflag=&quot;0&quot; shares=&quot;-10000/1&quot; value=&quot;-10000/1&quot;/&gt;&#10;    &amp;#60;/SPLITS&gt;&#10;&amp;#60;/TRANSACTION&gt;&#10;&amp;#60;/CONTAINER&gt;&#10;", 
				TransactionMergerVar3.getDierKMMContainerEscapedString(trx, false));
		
		// System.err.println("'" + TransactionMergerVar3.getDierKMMContainerEscapedString(trx, false) + "'");
		assertEquals("&amp;#60;!DOCTYPE MATCH&gt;&#10;&amp;#60;CONTAINER&gt;&#10;&amp;#60;TRANSACTION id=&quot;&quot; commodity=&quot;EUR&quot; entrydate=&quot;2023-11-03&quot; postdate=&quot;2023-01-01&quot; memo=&quot;&quot;&gt;&#10;    &amp;#60;SPLITS&gt;&#10;        &amp;#60;SPLIT account=&quot;A000004&quot; action=&quot;&quot; bankid=&quot;&quot; id=&quot;S0001&quot; memo=&quot;&quot; number=&quot;&quot; payee=&quot;P000004&quot; price=&quot;1/1&quot; reconciledate=&quot;&quot; reconcileflag=&quot;0&quot; shares=&quot;10000/1&quot; value=&quot;10000/1&quot;/&gt;&#10;        &amp;#60;SPLIT account=&quot;A000053&quot; action=&quot;&quot; bankid=&quot;&quot; id=&quot;S0002&quot; memo=&quot;&quot; number=&quot;&quot; payee=&quot;P000004&quot; price=&quot;1/1&quot; reconciledate=&quot;&quot; reconcileflag=&quot;0&quot; shares=&quot;-10000/1&quot; value=&quot;-10000/1&quot;/&gt;&#10;    &amp;#60;/SPLITS&gt;&#10;&amp;#60;/TRANSACTION&gt;&#10;&amp;#60;/CONTAINER&gt;&#10;", 
				TransactionMergerVar3.getDierKMMContainerEscapedString(trx, true));
	}

}
