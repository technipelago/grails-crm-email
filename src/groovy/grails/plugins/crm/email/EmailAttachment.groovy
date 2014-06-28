package grails.plugins.crm.email

import org.springframework.web.multipart.commons.CommonsMultipartFile
import grails.plugins.crm.content.CrmResourceRef

/**
 * Holder of email attachment.
 */
class EmailAttachment implements Serializable {
    private CommonsMultipartFile multipartFile
    private String resourceId
    private File file
    private boolean delete

    String name
    String contentType
    long size

    EmailAttachment(CommonsMultipartFile cmf) {
        this.multipartFile = cmf
        this.name = cmf.originalFilename
        this.contentType = cmf.contentType
        this.size = cmf.size
    }

    EmailAttachment(CrmResourceRef crmResourceRef) {
        this.resourceId = crmResourceRef.ident()
        this.name = crmResourceRef.title
        final Map metadata = crmResourceRef.metadata
        this.contentType = metadata.contentType
        this.size = metadata.bytes
    }

    EmailAttachment(File file, String name, String contentType, boolean delete) {
        this.file = file
        this.name = name ?: file.name
        this.contentType = contentType ?: "application/octet-stream"
        this.size = file.length()
        this.delete = delete
    }

    def withInputStream(Closure work) {
        if(multipartFile) {
            InputStream inputStream = multipartFile.getInputStream()
            try {
                return work(inputStream)
            } finally {
                inputStream.close()
            }
        } else if(resourceId) {
            def ref = CrmResourceRef.get(resourceId)
            if(ref) {
                return ref.withInputStream(work)
            }
        } else if(file) {
            try {
                return file.withInputStream(work)
            } finally {
                cleanup()
            }

        }
        throw new IllegalStateException("No resource attached")
    }

    void cleanup() {
        if(file && delete) {
            file.delete()
            file = null
        }
    }
}
