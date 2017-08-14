package com.mickey305.openssl.wrapper.scalafx.entity;

/**
 * Created by K.Misaki on 2017/08/12.
 *
 */
public class Config {
    public String publicKeyPath;
    public String privateKeyPath;
    public boolean executionEncryptionMode;

    public Config() {
        this("", "", true);
    }

    public Config(String publicKeyPath, String privateKeyPath, boolean executionEncryptionMode) {
        this.setPublicKeyPath(publicKeyPath);
        this.setPrivateKeyPath(privateKeyPath);
        this.setExecutionEncryptionMode(executionEncryptionMode);
    }

    public String getPublicKeyPath() {
        return publicKeyPath;
    }

    public void setPublicKeyPath(String publicKeyPath) {
        this.publicKeyPath = publicKeyPath;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public boolean isExecutionEncryptionMode() {
        return executionEncryptionMode;
    }

    public void setExecutionEncryptionMode(boolean executionEncryptionMode) {
        this.executionEncryptionMode = executionEncryptionMode;
    }
}
