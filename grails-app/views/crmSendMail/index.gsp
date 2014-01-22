<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="crmSendMail.index.title"/></title>
    <r:script>
        var CRM = {
            selectEmailTemplate: function (name, callback) {
                $.get("${createLink(controller: 'crmSendMail', action: 'template')}", {token: "${token}", name: name}, function(data) {
                    callback(data);
                });
            }
        };

        $(document).ready(function() {
            $("#template-selector a").click(function(ev) {
                ev.preventDefault();
                CRM.selectEmailTemplate($(this).data('name'), function(text) {
                    $("#body").val(text);
                });
            });
        });
    </r:script>
</head>

<body>

<g:form>

    <g:hiddenField name="token" value="${token}"/>

    <div class="row-fluid">
        <div class="span9">

            <crm:header title="crmSendMail.index.title" subtitle="${reference}"/>

            <div class="row-fluid">

                <div class="control-group">
                    <label class="control-label"><g:message code="crmSendMail.from.label"/></label>

                    <div class="controls">
                        <g:select name="from" from="${senders}" value="${config.from}" class="span11"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label"><g:message code="crmSendMail.to.label"/></label>

                    <div class="controls">
                        <g:textField name="to" value="${config.to}" autocomplete="off" autofocus="" class="span11"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label"><g:message code="crmSendMail.subject.label"/></label>

                    <div class="controls">
                        <g:textField name="subject" value="${config.subject}" autocomplete="off" autofocus=""
                                     class="span11"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label"><g:message code="crmSendMail.body.label"/></label>

                    <div class="controls">
                        <g:textArea name="body" value="${config.body}" rows="8" class="span11"/>
                    </div>
                </div>
            </div>

            <div class="form-actions">
                <crm:button action="index" label="crmSendMail.button.send.label" visual="success"
                            icon="icon-envelope icon-white"/>
                <crm:button action="cancel" label="crmSendMail.button.cancel.label" icon="icon-remove"/>
            </div>

        </div>

        <div class="span3">

            <ul id="template-selector" class="nav nav-list">
                <li class="nav-header"><g:message code="crmSendMail.templates.label"/></li>
                <g:each in="${templates}" var="template">
                    <li>
                        <a href="#" data-name="${template.value.encodeAsHTML()}"
                           title="${template.description?.encodeAsHTML()}">
                            ${template.label.encodeAsHTML()}
                        </a>
                    </li>
                </g:each>
            </ul>
        </div>
    </div>

</g:form>

</body>
</html>
