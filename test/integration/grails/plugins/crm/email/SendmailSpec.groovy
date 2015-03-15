package grails.plugins.crm.email

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession

/**
 * Test mail configuration.
 */
class SendmailSpec extends grails.test.spock.IntegrationSpec {

    def crmEmailService
    def greenMail

    def "send mail"() {
        given:
        def request = new MockHttpServletRequest()
        request.setSession(new MockHttpSession())
        when:
        def token = crmEmailService.prepareForSendMail(request, [
                tenant : 1L,
                user   : [username: "test", name: "Test User", email: "test@test.com"],
                referer: "http://localhost:8080/crmEmail/test/42",
                senders: ["foo@test.com", "bar@test.com"],
                from   : "info@test.com",
                to     : "Test User <test@test.com>",
                subject: "Integration Test",
                body   : "This is just a test, please ignore."
        ])

        then:
        token != null

        when:
        def config = crmEmailService.getSendMailConfiguration(request, token)

        then:
        config != null
        config.tenant == 1L
        config.subject == "Integration Test"

        when:
        crmEmailService.sendMailBySpec(config)

        then:
        greenMail.getReceivedMessages().length == 1

        when:
        def message = greenMail.getReceivedMessages()[0]

        then:
        message.subject == "Integration Test"

        when:
        crmEmailService.removeSendMailConfiguration(request, token)
        greenMail.deleteAllMessages()

        then:
        crmEmailService.getSendMailConfiguration(request, token) == null
    }
}
