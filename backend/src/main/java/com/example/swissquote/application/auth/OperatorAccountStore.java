package com.example.swissquote.application.auth;

import com.example.swissquote.domain.auth.OperatorAccount;

import java.util.Optional;

public interface OperatorAccountStore {

    Optional<OperatorAccount> findByProviderAndSubjectHash(String provider, String providerSubjectHash);

    OperatorAccount save(OperatorAccount operatorAccount);
}
