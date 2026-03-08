package de.isolveproblems.freeframe.api;

import java.io.File;

public interface BackupService {
    File createBackup();

    boolean restoreBackup(String fileName);

    String runDoctor();
}
