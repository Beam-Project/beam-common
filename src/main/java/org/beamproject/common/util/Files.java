/*
 * Copyright (C) 2013, 2014 beamproject.org
 *
 * This file is part of beam-client.
 *
 * beam-client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beam-client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beamproject.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Files {

    private final static String PROPERTIES_COMMENT = " Beam Configuration File\n"
            + " DO NOT MODIFY MANUALLY\n"
            + " Project website: https://www.beamproject.org/\n";

    public Properties loadConfigIfAvailable(String path) {
        Properties properties = new Properties();
        File configFile = new File(path);
        InputStream stream = null;

        if (configFile.exists() && configFile.canRead()) {
            try {
                stream = new FileInputStream(configFile);
                properties.load(stream);
            } catch (IOException ex) {
                Logger.getLogger(Files.class.getName()).log(Level.WARNING, "Could not read the config file properly: {0}", ex.getMessage());
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Files.class.getName()).log(Level.WARNING, "Could not close the config file stream properly: {0}", ex.getMessage());
                    }
                }
            }
        }

        return properties;
    }

    /**
     * Stores the given {@link Properties} as text file at the given path. If no
     * file exits, a new one will be created. If already a file exits, it will
     * be overwritten.
     *
     * @param properties The properties to write.
     * @param path The path where the file has to be write at.
     * @throws IllegalArgumentException If the arguments are null or invalid
     * (e.g. an illegal path).
     */
    public void storeProperies(Properties properties, String path) {
        Exceptions.verifyArgumentsNotNull(properties, path);
        Exceptions.verifyArgumentsNotEmpty(path);

        OutputStream output = null;

        try {
            output = new FileOutputStream(path);
            properties.store(output, PROPERTIES_COMMENT);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not write the properties file properly: " + ex.getMessage());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ex) {
                    Logger.getLogger(Files.class.getName()).log(Level.WARNING, "Could not write the properties file properly: {0}", ex.getMessage());
                }
            }
        }
    }
}
