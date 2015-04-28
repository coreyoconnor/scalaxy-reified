package scalaxy

import scala.language.experimental.macros
import scala.language.implicitConversions

import scala.reflect._
import scala.reflect.macros.blackbox.Context
import scala.reflect.runtime
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe.{ typeTag, TypeTag }

import scalaxy.reified.internal

/**
 * Scalaxy/Reified: the reify method in this package captures it's compile-time argument's AST,
 * allowing / preserving values captured outside its expression.
 */
package object reified {

  /**
   * Reify a value (including functions), preserving the original value and keeping track of the
   * values it captures from the scope of its expression.
   * This allows for runtime processing of the value's AST (being able to capture external values makes this method more flexible than Universe.reify).
   * Compile-time error are raised when an external reference cannot be captured safely (vars and
   * lazy vals are not considered safe, for instance).
   * Captured values are inlined in the reified value's AST with a conversion function,
   * which can be customized (by default, it handles constants, arrays, immutable collections,
   * tuples and options).
   */
  implicit def reified[A: TypeTag](v: A): Reified[A] =
    macro scalaxy.reified.internal.reifiedImpl[A]

  /**
   * Wrapper that provides Function1-like methods to a reified Function1 value.
   */
  implicit class ReifiedFunction1[T1: TypeTag, R: TypeTag](
    override val reifiedValue: Reified[T1 => R])
      extends HasReified[T1 => R] {

    override def valueTag = typeTag[T1 => R]

    /**
     * Apply the body of this function to the argument.
     *  @return   the result of function application.
     */
    def apply(a: T1): R = reifiedValue.value(a)

    /**
     * Composes two instances of ReifiedFunction1 in a new ReifiedFunction1, with this
     * reified function applied last.
     *
     *  @tparam   A   the type to which function `g` can be applied
     *  @param    g   a reified function A => T1
     *  @return       a new reified function `f` such that `f(x) == apply(g(x))`
     */
    @annotation.unspecialized
    def compose[A: TypeTag](g: ReifiedFunction1[A, T1]): ReifiedFunction1[A, R] = {
      val f = this
      reified((c: A) => f(g(c)))
      // internal.reifiedWithDifferentRuntimeValue[A => R](
      //   // f.compose(g),
      //   // f.value.compose(g.value),
      //   (c: A) => f(g(c)),
      //   f.reifiedValue.value.compose(g.reifiedValue.value)
      // )
    }

    /**
     * Composes two instances of ReifiedFunction1 in a new ReifiedFunction1, with this
     * reified function applied first.
     *
     *  @tparam   A   the result type of function `g`
     *  @param    g   a reified function R => A
     *  @return       a new reified function `f` such that `f(x) == g(apply(x))`
     */
    @annotation.unspecialized
    def andThen[A: TypeTag](g: ReifiedFunction1[R, A]): ReifiedFunction1[T1, A] = {
      val f = this
      reified((a: T1) => g(f(a)))
      // internal.reifiedWithDifferentRuntimeValue[T1 => A](
      //   // f.andThen(g),
      //   // f.value.andThen(g.value),
      //   (a: T1) => g(f(a)),
      //   f.reifiedValue.value.andThen(g.reifiedValue.value)
      // )
    }
  }

  /**
   * Wrapper that provides Function2-like methods to a reified Function2 value.
   */
  implicit class ReifiedFunction2[T1: TypeTag, T2: TypeTag, R: TypeTag](
    override val reifiedValue: Reified[Function2[T1, T2, R]])
      extends HasReified[Function2[T1, T2, R]] {

    override def valueTag = typeTag[Function2[T1, T2, R]]

    def apply(v1: T1, v2: T2): R = reifiedValue.value(v1, v2)

    /**
     * Creates a curried version of this reified function.
     *
     *  @return   a reified function `f` such that `f(x1)(x2) == apply(x1, x2)`
     */
    /*
    // TODO fix this:
    def curried: ReifiedFunction1[T1, ReifiedFunction1[T2, R]] = {
      val f = this
      internal.reifiedWithDifferentRuntimeValue[T1 => ReifiedFunction1[T2, R]](
        (v1: T1) => {
          internal.reifyMacro((v2: T2) => f(v1, v2))
        },
        f.curried
      )
    }
    */

    /**
     * Creates a tupled version of this reified function: instead of 2 arguments,
     *  it accepts a single [[scala.Tuple2]] argument.
     *
     *  @return   a reified function `f` such that `f((x1, x2)) == f(Tuple2(x1, x2)) == apply(x1, x2)`
     */
    def tupled: ReifiedFunction1[(T1, T2), R] = {
      val f = this
      reified((p: (T1, T2)) => {
        val (v1, v2) = p
        f(v1, v2)
      })
      // internal.reifiedWithDifferentRuntimeValue[((T1, T2)) => R](
      //   // f.value.tupled,
      //   (p: (T1, T2)) => {
      //     val (v1, v2) = p
      //     f(v1, v2)
      //   },
      //   f.reifiedValue.value.tupled
      // )
    }
  }

  /**
   * Implicitly extract reified value from its wrappers (such as ReifiedFunction1, ReifiedFunction2).
   */
  implicit def hasReifiedValueToReifiedValue[A: TypeTag](r: HasReified[A]): Reified[A] =
    macro scalaxy.reified.internal.hasReifiedValueToReifiedValueImpl[A]

  /**
   * Implicitly convert reified value to their original non-reified value.
   */
  implicit def hasReifiedValueToValue[A: TypeTag](r: HasReified[A]): A =
    macro scalaxy.reified.internal.hasReifiedValueToValueImpl[A]
}

