package vaultWeb.services.auth;

import vaultWeb.models.User;

public record LoginResult(User user, String accessToken) {}
