package kz.aruzhan.care_steps.domain.model

enum class ParentRelationship(val apiValue: String) {
    MOM("mom"),
    DAD("dad"),
    GUARDIAN("guardian"),
    OTHER("other")
}