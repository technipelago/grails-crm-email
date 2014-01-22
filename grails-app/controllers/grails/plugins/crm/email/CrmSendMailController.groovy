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

import javax.servlet.http.HttpServletResponse

/**
 * Send email controller.
 */
class CrmSendMailController {

    def crmCoreService
    def crmEmailService

    def index() {
        def token = params.token ?: params.id
        def config = crmEmailService.getSendMailConfiguration(request, token)
        if (!config) {
            log.error("Email configuration [$token] not found")
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        if (request.post) {
            def namespace = config.namespace ?: 'crm'
            def topic = config.topic ?: 'sendMail'
            def eventData = [:]
            eventData.putAll(config)
            eventData.putAll(params.subMap(['from', 'to', 'subject', 'body']))
            try {
                event(for: namespace, topic: topic, data: eventData)
            } finally {
                crmEmailService.removeSendMailConfiguration(request, token)
            }
            flash.success = message(code: config.sentMessage ?: 'crmSendMail.sent.message', args: [params.to])
            redirect uri: (config.referer - request.contextPath)
        } else {
            def reference = config.reference ? crmCoreService.getReference(config.reference) : null
            [token: token, config: config, ref: config.reference, reference: reference, referer: config.referer,
                    senders: config.senders, templates: config.templates]
        }
    }

    def cancel() {
        def token = params.token ?: params.id
        def config = crmEmailService.getSendMailConfiguration(request, token)
        if (!config) {
            log.error("Email configuration [$token] not found")
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
        crmEmailService.removeSendMailConfiguration(request, token)
        redirect uri: (config.referer - request.contextPath)
    }

    def template(String name) {
        def token = params.token ?: params.id
        def config = crmEmailService.getSendMailConfiguration(request, token)
        if (!config) {
            log.error("Email configuration [$token] not found")
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
        def namespace = config.templateNamespace ?: 'crm'
        def topic = config.templateTopic ?: 'parseTemplate'
        def eventData = [:]
        eventData.putAll(config)
        eventData.put('template', name)

        def result
        try {
            result = event(for: namespace, topic: topic, data: eventData, fork: false)?.value
        } catch (Throwable e) {
            log.error(e)
            while(e.getCause() != null) {
                e = e.getCause()
                result = e.getMessage()
                if(result) {
                    break
                }
            }
        }

        render result.toString()
    }
}
