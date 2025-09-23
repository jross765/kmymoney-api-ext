package org.kmymoney.apiext.secacct;

import java.util.ArrayList;
import java.util.List;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class SecuritiesAccountManager {

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(SecuritiesAccountManager.class);
    
    // ---------------------------------------------------------------
    
    private KMyMoneyAccount invstAcct = null;
    
    // ---------------------------------------------------------------
    
    public SecuritiesAccountManager() {
    }
    
    public SecuritiesAccountManager(KMyMoneyFile kmmFile, KMMAcctID acctID) {
    	if ( acctID == null ) {
    		throw new IllegalArgumentException("null account ID given");
    	}
    	
    	if ( ! acctID.isSet() ) {
    		throw new IllegalArgumentException("unset account ID given");
    	}
    	
    	invstAcct = kmmFile.getAccountByID(acctID);
    	if ( invstAcct.getType() != KMyMoneyAccount.Type.INVESTMENT ) {
    		invstAcct = null;
    		throw new IllegalArgumentException("account is not of type '" + KMyMoneyAccount.Type.INVESTMENT + "'");
    	}
    }
    
    public SecuritiesAccountManager(KMyMoneyAccount acct) {
    	setInvstAcct(acct);
    }
    
    // ---------------------------------------------------------------
    
    public KMyMoneyAccount getInvstAcct() {
		return invstAcct;
	}

	public void setInvstAcct(KMyMoneyAccount acct) {
    	if ( acct == null ) {
    		throw new IllegalArgumentException("null account given");
    	}
    	
    	if ( acct.getType() != KMyMoneyAccount.Type.INVESTMENT ) {
    		throw new IllegalArgumentException("account is not of type '" + KMyMoneyAccount.Type.INVESTMENT + "'");
    	}

		this.invstAcct = acct;
	}

    // ---------------------------------------------------------------
    
    public List<KMyMoneyAccount> getShareAccts() {
    	return invstAcct.getChildren();
    }
    
	public ArrayList<KMyMoneyAccount> getActiveShareAccts() {
    	ArrayList<KMyMoneyAccount> result = new ArrayList<KMyMoneyAccount>();
    	
    	for ( KMyMoneyAccount acct : getShareAccts() ) {
    		if ( acct.getBalance().isGreaterThan(new FixedPointNumber()) ) {
    			result.add(acct);
    		}
    	}
    	
    	return result;
    }
    
}
