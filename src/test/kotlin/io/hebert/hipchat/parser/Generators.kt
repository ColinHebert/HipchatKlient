package io.hebert.hipchat.parser

import java.util.concurrent.ThreadLocalRandom

typealias Generator<T> = () -> T

fun choose(min: Int, max: Int): Generator<Int> = { ThreadLocalRandom.current().nextInt(min, max) }
fun choose(min: Long, max: Long): Generator<Long> = { ThreadLocalRandom.current().nextLong(min, max) }

fun <T> oneOf(values: List<T>): Generator<T> = { values[ThreadLocalRandom.current().nextInt(values.size)] }
fun <T> oneOf(vararg generators: Generator<T>): Generator<T> = oneOf(generators.toList()).invoke()
fun <T> oneOf(values: Array<T>): Generator<T> = oneOf(values.toList())
inline fun <reified E : Enum<E>> oneOf(): Generator<E> = oneOf(enumValues<E>())

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

fun string(): Generator<String> = { ThreadLocalRandom.current().nextPrintableString(ThreadLocalRandom.current().nextInt(100)) }
fun int(): Generator<Int> = ThreadLocalRandom.current()::nextInt
fun long(): Generator<Long> = ThreadLocalRandom.current()::nextLong
fun boolean(): Generator<Boolean> = ThreadLocalRandom.current()::nextBoolean
fun double(): Generator<Double> = ThreadLocalRandom.current()::nextDouble
fun float(): Generator<Float> = ThreadLocalRandom.current()::nextFloat
fun <T> list(gen: Generator<T>): Generator<List<T>> = { (0..ThreadLocalRandom.current().nextInt(100)).map { gen.invoke() } }
fun <T> set(gen: Generator<T>): Generator<Set<T>> = { list(gen).invoke().toSet() }
fun <T> nullable(gen: Generator<T>): Generator<T?> = oneOf({ null }, gen)
