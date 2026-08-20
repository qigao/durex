plugins {
    id("durex.spring-service")
}

durex {
    persistence {
        jpa()
        jdbc()
        jooq()
    }
    redis()
}
