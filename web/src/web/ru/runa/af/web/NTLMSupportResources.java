/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.af.web;

import ru.runa.commons.ResourceCommons;

/**
 * Created on 10.11.2005
 *
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class NTLMSupportResources extends ResourceCommons {
    private static final String BUNDLE_NAME = "ntlm_support";

    private static final String DOMAIN_NAME = "domain";

    private static final String NTLM_SUPPORTED = "ntlm_supported";

    private NTLMSupportResources() {
        super(BUNDLE_NAME);
    }

    public static boolean isNTLMSupported() {
        return Boolean.TRUE.toString().equalsIgnoreCase(readProperty(NTLM_SUPPORTED, BUNDLE_NAME));
    }

    public static String getDomainName() {
        return readProperty(DOMAIN_NAME, BUNDLE_NAME);
    }
}
