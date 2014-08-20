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
package org.beamproject.common.carrier;

import org.beamproject.common.Participant;

/**
 * A {@link ClientCarrier} is used to connect to clients (mobile devices,
 * desktop clients, etc.) with a server and vice versa in order to provide
 * access to the recipient's account, notify all devices about new messages,
 * deliver messages bidirectional, etc..
 *
 * @see Carrier
 * @see ClientCarrierModel
 */
public interface ClientCarrier extends Carrier<ClientCarrierModel> {

    /**
     * Binds the given {@link Participant} to the given {@code topic}. Messages,
     * whose recipient is the given {@link Participant}, are then being
     * published to this {@code topic}.
     * <p>
     * The topic has to be known by all clients (mobile devices, desktop client,
     * etc.) of this {@link Participant} in order to subscribe to it. If the
     * server is the recipient, it has also to know the given topic.
     * <p>
     * If the participant is already bound to a topic, that binding will be
     * overwritten.
     *
     * @param participant The participant to bind.
     * @param topic The topic to bind.
     */
    public void bindParticipantToTopic(Participant participant, String topic);

    /**
     * Unbinds the given {@link Participant} from a possibly bound topic.
     *
     * @param participant The participant to unbind.
     */
    public void unbindParticipant(Participant participant);
}
