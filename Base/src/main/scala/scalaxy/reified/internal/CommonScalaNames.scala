package scalaxy.reified.internal

import scala.language.implicitConversions

import scala.reflect._
import scala.reflect.runtime.universe._
import scala.reflect.NameTransformer.encode

private[reified] object CommonScalaNames {
  import definitions._

  class N(val s: String) {
    def unapply(n: Name): Boolean = n.toString == s
    def apply() = TermName(s)
  }
  object N {
    def apply(s: String) = new N(s)
    def unapply(n: Name): Option[String] = Option(n).map(_.toString)
  }
  implicit def N2TermName(n: N) = n()

  val addAssignName = N(NameTransformer.encode("+="))
  val toArrayName = N("toArray")
  val toListName = N("toList")
  val toSeqName = N("toSeq")
  val toSetName = N("toSet")
  val toIndexedSeqName = N("toIndexedSeq")
  val toVectorName = N("toVector")
  val toMapName = N("toMap")
  val resultName = N("result")
  val scalaName = N("scala")
  val ArrayName = N("Array")
  val intWrapperName = N("intWrapper")
  val longWrapperName = N("longWrapper")
  val tabulateName = N("tabulate")
  val toName = N("to")
  val byName = N("by")
  val withFilterName = N("withFilter")
  val untilName = N("until")
  val isEmptyName = N("isEmpty")
  val sumName = N("sum")
  val productName = N("product")
  val minName = N("min")
  val maxName = N("max")
  val headName = N("head")
  val tailName = N("tail")
  val foreachName = N("foreach")
  val foldLeftName = N("foldLeft")
  val foldRightName = N("foldRight")
  val zipWithIndexName = N("zipWithIndex")
  val zipName = N("zip")
  val reverseName = N("reverse")
  val reduceLeftName = N("reduceLeft")
  val reduceRightName = N("reduceRight")
  val scanLeftName = N("scanLeft")
  val scanRightName = N("scanRight")
  val mapName = N("map")
  val collectName = N("collect")
  val canBuildFromName = N("canBuildFrom")
  val filterName = N("filter")
  val filterNotName = N("filterNot")
  val takeWhileName = N("takeWhile")
  val dropWhileName = N("dropWhile")
  val countName = N("count")
  val lengthName = N("length")
  val forallName = N("forall")
  val existsName = N("exists")
  val findName = N("find")
  val updateName = N("update")
  val toSizeTName = N("toSizeT")
  val toLongName = N("toLong")
  val toIntName = N("toInt")
  val toShortName = N("toShort")
  val toByteName = N("toByte")
  val toCharName = N("toChar")
  val toDoubleName = N("toDouble")
  val toFloatName = N("toFloat")
  val mathName = N("math")
  val packageName = N("package")
  val applyName = N("apply")
  val thisName = N("this")
  val superName = N("super")

  def C(name: String) = rootMirror.staticClass(name)
  def M(name: String) = rootMirror.staticModule(name)
  def P(name: String) = rootMirror.staticPackage(name)

  lazy val ScalaReflectPackage = P("scala.reflect")
  lazy val ScalaCollectionPackage = P("scala.collection")
  lazy val ScalaMathPackage = M("scala.math.package")
  lazy val ScalaMathPackageClass =
    ScalaMathPackage.moduleClass //.asModule.moduleClass
  lazy val ScalaMathCommonClass = C("scala.MathCommon")

  lazy val PredefModule = M("scala.Predef")

  lazy val SeqModule = M("scala.collection.Seq")
  lazy val SeqClass = C("scala.collection.Seq")
  lazy val SetModule = M("scala.collection.Set")
  lazy val SetClass = C("scala.collection.Set")
  lazy val VectorClass = C("scala.collection.Set")
  lazy val ListClass = C("scala.List")
  lazy val ImmutableListClass = C("scala.collection.immutable.List")
  lazy val NonEmptyListClass = C("scala.collection.immutable.$colon$colon")
  lazy val IndexedSeqModule = M("scala.collection.IndexedSeq")
  lazy val IndexedSeqClass = C("scala.collection.IndexedSeq")
  lazy val OptionModule = M("scala.Option")
  lazy val OptionClass = C("scala.Option")
  lazy val SomeModule = M("scala.Some")
  lazy val NoneModule = M("scala.None")
  lazy val StringOpsClass = C("scala.collection.immutable.StringOps")
  lazy val ArrayOpsClass = C("scala.collection.mutable.ArrayOps")

  lazy val VectorBuilderClass = C("scala.collection.immutable.VectorBuilder")
  lazy val ListBufferClass = C("scala.collection.mutable.ListBuffer")
  lazy val ArrayBufferClass = C("scala.collection.mutable.ArrayBuffer")
  lazy val WrappedArrayBuilderClass = C("scala.collection.mutable.WrappedArrayBuilder")
  lazy val RefArrayBuilderClass = C("scala.collection.mutable.ArrayBuilder.ofRef")
  lazy val RefArrayOpsClass = C("scala.collection.mutable.ArrayOps.ofRef")
  lazy val SetBuilderClass = C("scala.collection.mutable.SetBuilder")
}
