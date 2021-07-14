package org.crsh.auth;

public interface AuthInfo {

  boolean isSuccessful();

  AuthInfo UNSUCCESSFUL = () -> false;

  AuthInfo SUCCESSFUL = () -> true;
}
