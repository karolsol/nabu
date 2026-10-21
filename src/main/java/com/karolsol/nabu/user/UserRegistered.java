package com.karolsol.nabu.user;

import java.util.UUID;

public record UserRegistered(UUID userId, String email) {
}
