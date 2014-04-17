/*
 * Copyright (C) 2013, 2014 beamproject.org
 *
 * This file is part of beam-common.
 *
 * beam-common is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beam-common is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beamproject.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class allows to store {@link ConfigBase}s to files.
 */
public class ConfigWriter {

    /**
     * Stores the given {@link ConfigBase}. If the folder or the file is not
     * existing a the time of invocation, they will be created.
     *
     * @param config The config to store.
     * @param folderPath The folder path, where the file should be stored. This
     * has to end with a '/'.
     * @param filename The filename of the config file.
     * @throws IllegalArgumentException If at least one argument is null or if
     * the Strings are empty.
     * @see ConfigBase
     */
    public void writeConfig(ConfigBase config, String folderPath, String filename) {
        Exceptions.verifyArgumentsNotNull(config);
        Exceptions.verifyArgumentsNotEmpty(folderPath, filename);

        File folder = new File(folderPath);
        File file = new File(filename);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        try (FileOutputStream stream = new FileOutputStream(file)) {
            config.store(stream, null);
        } catch (IOException ex) {
            throw new ConfigException("Could not store the configuration: " + ex.getMessage());
        }
    }
}
