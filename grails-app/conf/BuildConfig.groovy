grails.project.work.dir = "target"
grails.project.target.level = 1.6

grails.project.repos.default = "crm"

grails.project.dependency.resolution = {
    inherits("global") {}
    log "warn"
    legacyResolve false
    repositories {
        grailsHome()
        mavenRepo "http://labs.technipelago.se/repo/crm-releases-local/"
        grailsCentral()
        mavenCentral()
    }
    dependencies {
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
    }

    plugins {
        build(":tomcat:$grailsVersion",
                ":release:2.2.1",
                ":rest-client-builder:1.0.3") {
            export = false
        }
        test(":spock:0.7") {
            export = false
            exclude "spock-grails-support"
        }
        test(":greenmail:1.3.4") {
            export = false
        }
        compile "grails.crm:crm-core:latest.integration"
        compile "grails.crm:crm-content:latest.integration"

        compile ":mail:1.0.6"
    }
}
