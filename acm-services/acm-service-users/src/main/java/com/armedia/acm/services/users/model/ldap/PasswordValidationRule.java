package com.armedia.acm.services.users.model.ldap;

/**
 * Created by sharmilee.sivakumaran on 6/12/17.
 */
public interface PasswordValidationRule {
    String RunValidationAndGetMessage(String username,String password);
}