package fr.iolabs.leaf.authentication.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.Objects;

public class PrivateToken {

    private String name;
    private LocalDate created;
    private LocalDate expiration;
    private String accountId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String secretKey;

    public PrivateToken(String name, String accountId, LocalDate expiration, String secretKey) {
        this.name = name;
        this.accountId = accountId;
        this.expiration = expiration;
        this.secretKey = secretKey;
    }

    public PrivateToken() {
        this(null, null,null,null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateToken that = (PrivateToken) o;
        return Objects.equals(secretKey, that.secretKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(secretKey);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public LocalDate getCreated() {
        return created;
    }

    public void setCreated(LocalDate created) {
        this.created = created;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public LocalDate getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDate expiration) {
        this.expiration = expiration;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
