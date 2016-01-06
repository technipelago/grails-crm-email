package grails.plugins.crm.email

import com.icegreen.greenmail.util.GreenMailUtil
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession

/**
 * Test mail configuration.
 */
class SendmailSpec extends grails.test.spock.IntegrationSpec {

    def grailsApplication
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

    def "test inline images"() {
        given:
        def request = new MockHttpServletRequest()
        request.setSession(new MockHttpSession())
        def image = grailsApplication.mainContext.getResource("images/test-image.png")

        when:
        def token = crmEmailService.prepareForSendMail(request, [
                tenant : 1L,
                user   : [username: "test", name: "Test User", email: "test@test.com"],
                referer: "http://localhost:8080/crmEmail/test/42",
                senders: ["foo@test.com", "bar@test.com"],
                from   : "info@test.com",
                to     : "Test User <test@test.com>",
                subject: "Inline Image Test",
                body   : 'This is just a test, look at the nice inlined image <img src="cid:foo" alt="Test"/>',
                inline : [[name: 'foo', contentType: 'image/png', withInputStream: { Closure cl -> cl(image.getInputStream()) }]]
        ])

        then:
        token != null

        when:
        def config = crmEmailService.getSendMailConfiguration(request, token)
        crmEmailService.sendMailBySpec(config)

        then:
        greenMail.getReceivedMessages().length == 1
        def msg = greenMail.getReceivedMessages()[0]
        msg.getSubject() == "Inline Image Test"
        GreenMailUtil.hasNonTextAttachments(msg)
        containsImage(GreenMailUtil.getBody(msg), 'foo')

        cleanup:
        crmEmailService.removeSendMailConfiguration(request, token)
        greenMail.deleteAllMessages()
    }

    private boolean containsImage(String body, String name) {
        if (!body.contains('Content-Type: image/png')) {
            return false
        }
        if (!body.contains('Content-Disposition: inline')) {
            eturn false
        }
        if (!body.contains('Content-ID: <' + name + '>')) {
            return false
        }
        return true
    }
}
