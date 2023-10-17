/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aluguerservidores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;

/**
 *
 * @author quim
 */
public class AccountsMap {

    private HashMap<String, Account> accounts;

    public AccountsMap() {
        this.accounts = new HashMap<>();
    }

    public synchronized boolean isAccountEmail(String email) {
        return accounts.containsKey(email);
    }

    public synchronized void addAccount(Account account) {
        accounts.put(account.getEmail(), account);
    }

    public synchronized Account getAccount(String s) {
        return this.accounts.get(s);
    }

    public synchronized boolean isValidPassword(String email, String password) {
        return accounts.get(email).getPassword().equals(password);
    }
}
