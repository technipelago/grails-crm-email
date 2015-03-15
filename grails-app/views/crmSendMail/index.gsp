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
            },
            refreshFiles: function() {
                $("#files").load("${createLink(controller: 'crmSendMail', action: 'attachments', id: token)}", function() {
                    $("#attachments .crm-delete").click(function(ev) {
                        ev.preventDefault();
                        var $a = $(this).closest('a');
                        var name = $a.data("crm-name");
                        $.post("${createLink(action: 'delete')}", {id: "${token}", name: name}, function(data) {
                            $a.remove();
                        });
                    });
                });
            }
        };

        $(document).ready(function() {
            $("#template-selector a").click(function(ev) {
                ev.preventDefault();
                var title = $(this).text().trim();
                CRM.selectEmailTemplate($(this).data('name'), function(text) {
                    $("#body").val(text);
                    var subjectField = $('#subject');
                    if(! subjectField.val().trim()) {
                        subjectField.val(title);
                    }
                });
            });

            $("a.crm-file").click(function(ev) {
                var id = $(this).data("crm-id");
                var formData = new FormData();
                formData.append('r', id);
                $.ajax({
                    url: "${createLink(controller: 'crmSendMail', action: 'attach', id: token)}",
                    type: "POST",
                    data: formData,
                    dataType: 'json',
                    //Options to tell jQuery not to process data or worry about content-type.
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function(data, status, xhr) {
                        CRM.refreshFiles();
                        $('#attachModal').modal('hide');
                    }
                });
            });

            $("#attachModal form").submit(function(ev) {
                ev.preventDefault();
                var formData = new FormData($(this)[0]);
                $.ajax({
                    url: "${createLink(controller: 'crmSendMail', action: 'upload', id: token)}",
                    type: "POST",
                    data: formData,
                    dataType: 'json',
                    //Options to tell jQuery not to process data or worry about content-type.
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function(data, status, xhr) {
                        CRM.refreshFiles();
                        $('#attachModal').modal('hide');
                    }
                });
            });

            CRM.refreshFiles();
        });
    </r:script>
    <style type="text/css">
    #attachments .btn {
        margin-bottom: 5px;
    }
    </style>
</head>

<body>

<g:form action="send">

    <g:hiddenField name="token" value="${token}"/>

    <div class="row-fluid">
        <div class="span9">

            <crm:header title="crmSendMail.index.title" subtitle="${reference}"/>

            <div class="row-fluid">

                <div class="control-group">
                    <label class="control-label"><g:message code="crmSendMail.from.label"/></label>

                    <div class="controls">
                        <g:select name="from" from="${senders}" value="${from}" class="span11"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label"><g:message code="crmSendMail.to.label"/></label>
                    <g:if test="${selection}">
                        <div class="controls">
                            &laquo; Flera mottagare &raquo;
                        </div>
                    </g:if>
                    <g:else>
                        <div class="controls">
                            <g:textField name="to" value="${to}" autocomplete="off" autofocus="" class="span11"/>
                        </div>
                    </g:else>
                </div>

                <g:if test="${config.cc != null}">
                    <div class="control-group">
                        <label class="control-label"><g:message code="crmSendMail.cc.label"/></label>

                        <div class="controls">
                            <g:textField name="cc" value="${cc}" autocomplete="off" autofocus="" class="span11"/>
                        </div>
                    </div>
                </g:if>

                <g:if test="${config.bcc != null}">
                    <div class="control-group">
                        <label class="control-label"><g:message code="crmSendMail.bcc.label"/></label>

                        <div class="controls">
                            <g:textField name="bcc" value="${bcc}" autocomplete="off" autofocus=""
                                         class="span11"/>
                        </div>
                    </div>
                </g:if>

                <div class="control-group">
                    <label class="control-label"><g:message code="crmSendMail.subject.label"/></label>

                    <div class="controls">
                        <g:textField name="subject" value="${subject}" autocomplete="off" autofocus=""
                                     class="span11"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label"><g:message code="crmSendMail.body.label"/></label>

                    <div class="controls">
                        <g:textArea name="body" value="${body}" rows="10" class="span11"/>
                    </div>
                </div>
            </div>

            <div id="attachments" class="control-group">
                <label class="control-label"><g:message code="crmSendMail.attachments.label"/></label>

                <div class="controls">
                    <span id="files">
                        <tmpl:attachments list="${attachments}"/>
                    </span>

                    <div class="btn-group">
                        <a class="btn btn-info dropdown-toggle" data-toggle="dropdown" href="#">
                            <i class="icon icon-folder-open icon-white"></i>
                            <g:message code="crmSendMail.button.attachment.select.label" default="Select"/>
                            <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu">
                            <g:each in="${files}" var="file">
                                <li>
                                    <a href="#" data-crm-id="${file.id}" class="crm-file">
                                        ${file.title.encodeAsHTML()}
                                    </a>
                                </li>
                            </g:each>
                            <li>
                                <a href="#attachModal" data-toggle="modal">
                                    <g:message code="crmSendMail.attachment.upload.label" default="Upload..."/>
                                </a>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>

            <div class="form-actions">
                <crm:button action="send" label="crmSendMail.button.send.label" visual="success"
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

<div id="attachModal" class="modal hide fade" tabindex="-1" role="dialog"
     aria-labelledby="attachModalLabel" aria-hidden="true">
    <g:uploadForm action="attach" id="${token}">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>

            <h3 id="attachModalLabel">
                <g:message code="crmSendMail.attach.title"/>
            </h3>
        </div>

        <div class="modal-body row-fluid">
            <p>
                <g:message code="crmSendMail.attach.help"/>
            </p>

            <input type="file" name="file"/>

        </div>

        <div class="modal-footer">
            <button type="submit" class="btn btn-success"><g:message
                    code="crmSendMail.button.attachment.upload.label"/></button>
            <button type="button" class="btn" data-dismiss="modal" aria-hidden="true"><g:message
                    code="crmSendMail.button.cancel.label"/></button>
        </div>
    </g:uploadForm>
</div>

</body>
</html>
