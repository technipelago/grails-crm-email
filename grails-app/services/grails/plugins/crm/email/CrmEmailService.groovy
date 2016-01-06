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

        def config = getSendMailConfiguration(request, token)
        if (config?.attachments) {
            deleteAttachment(config.attachments, null)
        }

        def obj = session.getAttribute(TOKEN_PREFIX + token)
        if (obj != null) {
            session.removeAttribute(TOKEN_PREFIX + token)
            return true
        }

        return false
    }

    /*
     * Cleanup temporary files.
     */
    boolean deleteAttachment(final List<EmailAttachment> attachments, final String name) {
        boolean removed = false
        Iterator<EmailAttachment> itor = attachments.iterator()
        while (itor.hasNext()) {
            EmailAttachment a = itor.next()
            if (name == null || name == a.name) {
                a.cleanup()
                itor.remove()
                removed = true
            }
        }
        removed
    }

    def sendMailBySpec(Map data) {
        def inlines = data.inlines ?: data.inline
        def attachments = data.attachments ?: data.attachment

        sendMail {
            if (attachments || inlines || (data.body && data.html)) {
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
            if (data.replyTo) {
                replyTo data.replyTo
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
            if(inlines) {
                if(inlines instanceof Map) {
                    inlines = [inline]
                }
                for (a in inlines) {
                    def tmp = new ByteArrayOutputStream()
                    a.withInputStream { is -> tmp << is }
                    inline a.name, a.contentType, tmp.toByteArray()
                }
            }
            if(attachments) {
                if(attachments instanceof Map) {
                    attachments = [attachments]
                }
                for (a in attachments) {
                    def tmp = new ByteArrayOutputStream()
                    a.withInputStream { is -> tmp << is }
                    attachBytes a.name, a.contentType, tmp.toByteArray()
                }
            }
        }
        event(for: 'crm', topic: 'mailSent', data: data)
    }
}
