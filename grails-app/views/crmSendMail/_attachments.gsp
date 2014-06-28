<g:each in="${list}" var="file">
    <a class="btn btn-info" href="#" data-crm-name="${file.name.encodeAsHTML()}">
        ${file.name.encodeAsHTML()}
        <span class="crm-delete">×</span>
    </a>
</g:each>