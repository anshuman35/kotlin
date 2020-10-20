fun test() = 1

fun box(): String {
    val l = ArrayList<Long>()
    l.add(1)
    l.add(-1)
    l.<!INAPPLICABLE_CANDIDATE!>add<!>(1.inv())
    l.<!INAPPLICABLE_CANDIDATE!>add<!>(1.unaryMinus())
    l.<!INAPPLICABLE_CANDIDATE!>add<!>(test()) //frontend error: type mismatch
    return if (l[0] is Long) "OK" else "fail: ${l[0]}"
}
