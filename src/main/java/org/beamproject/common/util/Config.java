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

import java.util.HashMap;
import java.util.Properties;

/**
 * This class abstracts form {@link Properties} to enable a strongly typed
 * interface.
 *
 * @param <T> An enum which has to be used with an instance of Config.
 */
public class Config<T extends Enum<T>> {

    protected final HashMap<String, Object> map = new HashMap<>();

    protected Config() {
    }

    /**
     * Instantiates a new {@link Config}, filled with the given properties.
     *
     * @param properties The properties to use.
     * @throws IllegalArgumentException If the argument is null.
     */
    public Config(Properties properties) {
        Exceptions.verifyArgumentsNotNull(properties);

        fillPropertiesToMap(properties);
    }

    private void fillPropertiesToMap(Properties properties) {
        for (String key : properties.stringPropertyNames()) {
            byte[] decodedValue = Base58.decode(properties.getProperty(key));
            map.put(key, decodedValue);
        }
    }

    public void set(T key, String value) {
        map.put(key.toString(), value.getBytes());
    }

    public String getAsString(T key) {
        return new String((byte[]) map.get(key.toString()));
    }

    public void set(T key, byte[] value) {
        map.put(key.toString(), value);
    }

    public byte[] getAsBytes(T key) {
        return (byte[]) map.get(key.toString());
    }

    protected byte[] getByStringAsBytes(String key) {
        return (byte[]) map.get(key);
    }

    public boolean contains(T key) {
        return map.containsKey(key.toString());
    }

    /**
     * Copies the stored values, encoded as {@link Base58} strings, to a new
     * {@link Properties} instance.
     *
     * @return The created and filled instance.
     */
    public Properties copyToProperties() {
        Properties properties = new Properties();

        for (String key : map.keySet()) {
            properties.setProperty(key, Base58.encode(getByStringAsBytes(key)));
        }

        return properties;
    }

}
