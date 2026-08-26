plugins {
    id("io.github.qigao.simpledsl.java")
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
