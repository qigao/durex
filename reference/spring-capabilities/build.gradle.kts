plugins {
    id("io.github.qigao.simpledsl.build")
}

simpledsl {
    springService()
    persistence {
        jpa()
        jdbc()
        jooq()
    }
    redis()
}
