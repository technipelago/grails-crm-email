/*
 * Copyright (c) 2014 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.crm.email

import grails.events.Listener

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

/**
 * Methods for sending emails and working with email templates.
 */
class CrmEmailService {

    static transactional = false

    private static final String TOKEN_PREFIX = 'sendmail_'

    String prepareForSendMail(HttpServletRequest request, Map params) {
        HttpSession session = request.getSession(false)
        if (session == null) {
            throw new IllegalStateException("No HTTP session")
        }

        String token = Math.abs(params.hashCode()).toString()

        session.setAttribute(TOKEN_PREFIX + token, params)

        return token
    }

    Map getSendMailConfiguration(HttpServletRequest request, String token) {
        HttpSession session = request.getSession(false)
        if (session == null) {
            throw new IllegalStateException("No HTTP session")
        }

        def obj = session.getAttribute(TOKEN_PREFIX + token)
        if (obj == null) {
            return null
        }

        if (!(obj instanceof Map)) {
            log.error("Email token [$token] references an illegal type: ${obj.getClass().getName()}")
            throw new IllegalArgumentException("Email token [$token] references an illegal type")
        }

        return obj
    }

    boolean removeSendMailConfiguration(HttpServletRequest request, String token) {
        HttpSession session = request.getSession(false)
        if (session == null) {
            return false
        }

        def obj = session.getAttribute(TOKEN_PREFIX + token)
        if (obj != null) {
            session.removeAttribute(TOKEN_PREFIX + token)
            return true
        }

        return false
    }


    @Listener(namespace = 'crm', topic = 'sendMail')
    def sendMailListener(Map data) {
        sendMail {
            if (data.body && data.html) {
                multipart true
            }
            to data.to
            from data.from
            if (data.cc) {
                cc data.cc
            }
            if (data.bcc) {
                bcc data.bcc
            }
            if (data.subject) {
                subject data.subject
            }
            if (data.body) {
                body data.body
            }
            if (data.html) {
                html data.html
            }
        }
        event(for: 'crm', topic: 'mailSent', data: data)
    }
}
