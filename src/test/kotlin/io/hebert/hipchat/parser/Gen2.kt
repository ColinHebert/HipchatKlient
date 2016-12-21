package io.hebert.hipchat.parser

import java.util.concurrent.ThreadLocalRandom

typealias Gen2<T> = () -> T

fun choose(min: Int, max: Int): Gen2<Int> = { ThreadLocalRandom.current().nextInt(min, max) }
fun choose(min: Long, max: Long): Gen2<Long> = { ThreadLocalRandom.current().nextLong(min, max) }

fun <T> oneOf(values: List<T>): Gen2<T> = { values[ThreadLocalRandom.current().nextInt(values.size)] }
fun <T> oneOf(vararg generators: Gen2<T>): Gen2<T> = oneOf(generators.toList()).invoke()
fun <T> oneOf(values: Array<T>): Gen2<T> = oneOf(values.toList())
inline fun <reified E : Enum<E>> oneOf(): Gen2<E> = oneOf(enumValues<E>())

/**
 * Generates the next printable ASCII character.
 *
 * Characters are between 32 (space) and 126 (~)
 */
private fun ThreadLocalRandom.nextPrintableChar(): Char {
    return nextInt(32, 127).toChar()
}

fun ThreadLocalRandom.nextPrintableString(length: Int): String {
    return (0..length - 1).map { nextPrintableChar() }.joinToString("")
}

fun string(): Gen2<String> = { ThreadLocalRandom.current().nextPrintableString(ThreadLocalRandom.current().nextInt(100)) }
fun int(): Gen2<Int> = ThreadLocalRandom.current()::nextInt
fun long(): Gen2<Long> = ThreadLocalRandom.current()::nextLong
fun boolean(): Gen2<Boolean> = ThreadLocalRandom.current()::nextBoolean
fun double(): Gen2<Double> = ThreadLocalRandom.current()::nextDouble
fun float(): Gen2<Float> = ThreadLocalRandom.current()::nextFloat
fun <T> list(gen: Gen2<T>): Gen2<List<T>> = { (0..ThreadLocalRandom.current().nextInt(100)).map { gen.invoke() } }
fun <T> set(gen: Gen2<T>): Gen2<Set<T>> = { list(gen).invoke().toSet() }
fun <T> nullable(gen: Gen2<T>): Gen2<T?> = oneOf({ null }, gen)
