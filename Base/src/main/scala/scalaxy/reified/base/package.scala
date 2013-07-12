package scalaxy.reified

import scala.language.experimental.macros
import scala.language.implicitConversions

import scala.reflect._
import scala.reflect.macros.Context
import scala.reflect.runtime
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe.TypeTag

package object base {
  def reify[A: TypeTag](v: A): ReifiedValue[A] = macro impl.reifyImpl[A]
}

