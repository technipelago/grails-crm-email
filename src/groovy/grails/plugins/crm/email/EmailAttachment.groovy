package grails.plugins.crm.email

import org.springframework.web.multipart.commons.CommonsMultipartFile
import grails.plugins.crm.content.CrmResourceRef

/**
 * Created by goran on 2014-06-28.
 */
class EmailAttachment implements Serializable {
    private CommonsMultipartFile multipartFile
    private String resourceId

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
        }
        throw new IllegalStateException("No resource attached")
    }
}
