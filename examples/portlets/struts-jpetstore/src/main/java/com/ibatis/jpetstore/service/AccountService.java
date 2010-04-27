package com.ibatis.jpetstore.service;

import com.ibatis.dao.client.DaoManager;
import com.ibatis.jpetstore.domain.Account;
import com.ibatis.jpetstore.persistence.iface.AccountDao;
import com.ibatis.jpetstore.persistence.DaoConfig;

public class AccountService {

  private AccountDao accountDao;

  public AccountService() {
    DaoManager daoMgr = DaoConfig.getDaoManager();
    this.accountDao = (AccountDao) daoMgr.getDao(AccountDao.class);
  }

  public AccountService(AccountDao accountDao) {
    this.accountDao = accountDao;
  }

  public Account getAccount(String username) {
    return accountDao.getAccount(username);
  }

  public Account getAccount(String username, String password) {
    return accountDao.getAccount(username, password);
  }

  public void insertAccount(Account account) {
    accountDao.insertAccount(account);
  }

  public void updateAccount(Account account) {
    accountDao.updateAccount(account);
  }

}
