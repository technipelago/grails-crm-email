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

import grails.converters.JSON

import javax.servlet.http.HttpServletResponse

import grails.plugins.crm.core.TenantUtils

/**
 * Send email controller.
 */
class CrmSendMailController {

    static allowedMethods = [send: 'POST', attach: 'POST', upload: 'POST']

    def crmCoreService
    def crmContentService
    def crmEmailService

    def index() {
        def token = params.token ?: params.id
        def config = crmEmailService.getSendMailConfiguration(request, token)
        if (!config) {
            log.error("Email configuration [$token] not found")
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        def reference = config.reference ? crmCoreService.getReference(config.reference) : null
        [token    : token, config: config, ref: config.reference, reference: reference,
         selection: config.selection, referer: config.referer,
         senders  : config.senders, templates: config.templates, attachments: config.attachments, files: config.files] +
                config.subMap(['from', 'to', 'cc', 'bcc', 'subject', 'body'])

    }

    def send() {
        def token = params.token ?: params.id
        def config = crmEmailService.getSendMailConfiguration(request, token)
        if (!config) {
            log.error("Email configuration [$token] not found")
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
        def namespace = config.namespace ?: 'crm'
        def topic = config.topic ?: 'sendMail'
        def eventData = [:]
        eventData.putAll(config)
        eventData.putAll(params.subMap(['from', 'to', 'subject', 'body']))
        try {
            event(for: namespace, topic: topic, data: eventData, fork: false)
        } finally {
            crmEmailService.removeSendMailConfiguration(request, token)
        }
        flash.success = message(code: config.sentMessage ?: 'crmSendMail.sent.message', args: [params.to ?: 'recipients'])
        redirect uri: (config.referer - request.contextPath)
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
            while (e.getCause() != null) {
                e = e.getCause()
                result = e.getMessage()
                if (result) {
                    break
                }
            }
        }

        render result.toString()
    }

    /**
     * Attach existing CRM content.
     *
     * @param r
     * @return
     */
    def attach(Long r) {
        def token = params.token ?: params.id
        def config = crmEmailService.getSendMailConfiguration(request, token)
        if (!config) {
            log.error("Email configuration [$token] not found")
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        if (!config.attachments) {
            config.attachments = []
        }

        def tenant = TenantUtils.tenant
        def crmResourceRef = crmContentService.getResourceRef(r)
        if (!crmResourceRef) {
            log.error("Resource [$r] not found in tenant [$tenant]")
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
        if (crmResourceRef.tenantId != tenant) {
            log.error("Illegal access to resource [$r] in tenant [$tenant]")
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }
        config.attachments << new EmailAttachment(crmResourceRef)

        render config.attachments as JSON
    }

    /**
     * Upload a client file.
     *
     * @return
     */
    def upload() {
        def token = params.token ?: params.id
        def config = crmEmailService.getSendMailConfiguration(request, token)
        if (!config) {
            log.error("Email configuration [$token] not found")
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        if (!config.attachments) {
            config.attachments = []
        }

        def file = params.file

        if (file && file.isAvailable() && !file.isEmpty()) {
            def tempFile = File.createTempFile("crm", "att")
            tempFile.deleteOnExit()
            file.transferTo(tempFile)
            config.attachments << new EmailAttachment(tempFile, file.getOriginalFilename(), file.getContentType(), true)
        }

        render config.attachments as JSON
    }

    def attachments() {
        def token = params.token ?: params.id
        def config = crmEmailService.getSendMailConfiguration(request, token)
        if (!config) {
            log.error("Email configuration [$token] not found")
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
        render template: 'attachments', model: [list: config.attachments ?: []]
    }

    /**
     * Delete attachments in an email configuration.
     *
     * @param id configuration token
     * @param name attachment name or null to delete all attachments
     * @return JSON Array with remaining attachments
     */
    def delete(String id, String name) {
        def config = crmEmailService.getSendMailConfiguration(request, id)
        if (!config) {
            log.error("Email configuration [$id] not found")
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        crmEmailService.deleteAttachment(config.attachments, name)

        render config.attachments as JSON
    }
}
