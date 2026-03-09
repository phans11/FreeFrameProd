package de.isolveproblems.freeframe.api;

public interface ZeroDowntimeMigrationService {
    MigrationPreview preview();

    MigrationPreview apply();
}
