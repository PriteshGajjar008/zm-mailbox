/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2012, 2013, 2014, 2016 Synacor, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Set current volume.
 * <br />
 * Notes: Each SetCurrentVolumeRequest can set only one current volume type.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_SET_CURRENT_VOLUME_REQUEST)
public final class SetCurrentVolumeRequest {

    /**
     * @zm-api-field-description ID
     */
    @XmlAttribute(name=AdminConstants.A_ID, required=true)
    private final short id;

    /**
     * @zm-api-field-tag volume-type
     * @zm-api-field-description Volume type: 1 (primary message), 2 (secondary message) or 10 (index)
     */
    @XmlAttribute(name=AdminConstants.A_VOLUME_TYPE, required=true)
    private final short type;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private SetCurrentVolumeRequest() {
        this((short) -1, (short) -1);
    }

    public SetCurrentVolumeRequest(short id, short type) {
        this.id = id;
        this.type = type;
    }

    public short getType() {
        return type;
    }

    public short getId() {
        return id;
    }
}
