package scalaxy.reified

import scala.reflect.runtime.universe
import scala.reflect.runtime.currentMirror
import scala.reflect.runtime.universe._

import scalaxy.reified.internal.Optimizer
import scalaxy.reified.internal.Optimizer.newInlineAnnotation
import scalaxy.reified.internal.CapturesFlattener
import scalaxy.reified.internal.CompilerUtils
import scalaxy.reified.internal.CommonExtractors._
import scalaxy.reified.internal.CommonScalaNames._
import scalaxy.reified.internal.Utils
import scalaxy.reified.internal.Utils._
import scala.tools.reflect.ToolBox

import scala.reflect.NameTransformer.encode

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

import scalaxy.generic.AllTreeSimplifiers

/**
 * Reified value wrapper.
 */
private[reified] trait HasReified[A] {
  private[reified] def reifiedValue: Reified[A]
  def valueTag: TypeTag[A]
  override def toString = s"${getClass.getSimpleName}(${reifiedValue.value}, ${reifiedValue.expr})"
}

/**
 * Reified value which can be created by {@link scalaxy.reified.reified}.
 * This object retains the runtime value passed to {@link scalaxy.reified.reified} as well as its
 * compile-time AST.
 */
final class Reified[A: TypeTag](
  /**
   * Original value passed to {@link scalaxy.reified.reified}
   */
  valueGetter: => A,
  /**
   * AST of the value.
   */
  exprGetter: => Expr[A])
    extends HasReified[A] {

  lazy val value = valueGetter
  lazy val expr = exprGetter
  // {
  //   val x = exprGetter
  //   println("EXPR[" + valueTag.tpe + "]: " + x)
  //   x
  // }

  override def reifiedValue = this
  override def valueTag = typeTag[A]

  /**
   * Compile the AST.
   * @param toolbox toolbox used to perform the compilation. By default, using a
   *     toolbox configured with all stable optimization flags available.
   */
  def compile(toolbox: ToolBox[universe.type] = optimisingToolbox): () => A = {

    object Compile extends AllTreeSimplifiers {
      override val global = toolbox.u
      import global._

      var ast: Tree = flatExpr.tree

      ast = simplifyGenericTree(toolbox.typecheck(ast, pt = valueTag.tpe))
      // println("SIMPLIFIED AST: " + ast)

      // TODO(ochafik): Drop this!!!
      ast = toolbox.untypecheck(ast)
      // println("RESET AST: " + ast)

      // TODO(ochafik): Do the symbol reattribution/surgery needed to drop the untypecheck.
      def reinline(tree: Tree) = tree match {
        case DefDef(mods, name, tparams, vparamss, tpt, rhs) =>
          DefDef(
            mods.mapAnnotations(list => newInlineAnnotation :: list),
            name, tparams, vparamss, tpt, rhs)
        case _ =>
          tree
      }
      ast = ast match {
        case Block(stats, expr) =>
          Block(stats.map(reinline _), expr)
        case _ =>
          ast
      }
      // println("REINLINED AST: " + ast)
      val result = toolbox.compile(ast)
    }
    // println("AST: " + ast)
    
    val result = Compile.result

    () => result().asInstanceOf[A]
  }

  def flatExpr: Expr[A] = {
    val result = new CapturesFlattener(expr.tree).flatten
    // result collect {
    //   case t if isHasReifiedValueFreeTerm(t.symbol) =>
    //     sys.error("RETAINED FREE TERM: " + t + " : " + t.symbol)
    // }
    newExpr[A](result)
  }
}
