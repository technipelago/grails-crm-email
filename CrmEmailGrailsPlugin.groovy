/*
 * Copyright (c) 2015 Goran Ehrsson.
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

class CrmEmailGrailsPlugin {
    def groupId = ""
    def version = "2.4.2"
    def grailsVersion = "2.2 > *"
    def dependsOn = [:]
    def loadAfter = ['crmCore']
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "web-app/images/test-image.png"
    ]
    def title = "GR8 CRM Email Composition Plugin"
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''\
        This plugin provides services, controllers and views for sending email in a GR8 CRM application.
        '''
    def documentation = "http://gr8crm.github.io/plugins/crm-email/"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "github", url: "https://github.com/technipelago/grails-crm-email/issues"]
    def scm = [url: "https://github.com/technipelago/grails-crm-email"]
}
