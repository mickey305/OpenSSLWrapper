package com.mickey305.openssl.wrapper.scalafx.entity;

/**
 * Created by K.Misaki on 2017/08/12.
 *
 */
public class Config {
    public String publicKeyPath;
    public String privateKeyPath;

    public Config() {
        this("", "");
    }

    public Config(String publicKeyPath, String privateKeyPath) {
        this.setPublicKeyPath(publicKeyPath);
        this.setPrivateKeyPath(privateKeyPath);
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
}
