package com.karolsol.nabu.user.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record UserPreferences(
    @Column(name = "preferred_language", nullable = false, length = 10) String language,
    @Column(name = "time_zone", nullable = false, length = 50) String timeZone) {}
