package scala.virtualization.lms
package common

import internal._
import org.scala_lang.virtualized.SourceContext
import org.scala_lang.virtualized.EmbeddedControls

/**
 * This trait automatically lifts any concrete instance to a representation.
 */
trait LiftAll extends Base {
  protected implicit def __unit[T:Manifest](x: T) = unit(x)
}

/**
 * The Base trait defines the type constructor Rep, which is the higher-kinded type that allows for other DSL types to be
 * polymorphically embedded.
 *
 * @since 0.1 
 */
trait Base extends EmbeddedControls {
  type API <: Base
  type Rep[+T]

  protected def unit[T:Manifest](x: T): Rep[T]

  // always lift Unit and Null (for now)
  implicit def unitToRepUnit(x: Unit) = unit(x)
  implicit def nullToRepNull(x: Null) = unit(x)
}

/**
 * This trait sets the representation to be based on AST Expression nodes.
 *
 * @since 0.1
 */
trait BaseExp extends Base with Expressions with Blocks with Transforming {
  type Rep[+T] = Exp[T]

  protected def unit[T:Manifest](x: T) = Const(x)
}

trait BlockExp extends BaseExp with Blocks


trait EffectExp extends BaseExp with Effects {

  def mapOver(t: Transformer, u: Summary) = { // TODO: move to effects class?
    u.copy(mayRead = t.onlySyms(u.mayRead), mstRead = t.onlySyms(u.mstRead),
      mayWrite = t.onlySyms(u.mayWrite), mstWrite = t.onlySyms(u.mstWrite))
  }

  override def mirrorDef[A:Manifest](e: Def[A], f: Transformer)(implicit pos: SourceContext): Def[A] = e match {
    case Reflect(x, u, es) => Reflect(mirrorDef(x,f), mapOver(f,u), f(es))
    case Reify(x, u, es) => Reify(f(x), mapOver(f,u), f(es))
    case _ => super.mirrorDef(e,f)
  }

  override def mirror[A:Manifest](e: Def[A], f: Transformer)(implicit pos: SourceContext): Exp[A] = e match {
    case Reflect(x, u, es) => reflectMirrored(mirrorDef(e,f).asInstanceOf[Reflect[A]])
    case Reify(x, u, es) => Reify(f(x), mapOver(f,u), f(es))
    case _ => super.mirror(e,f)
  }
    
}

trait BaseFatExp extends BaseExp with FatExpressions with FatTransforming


// The traits below provide an interface to codegen so that clients do
// not need to depend on internal._

trait ScalaGenBase extends ScalaCodegen

trait ScalaGenEffect extends ScalaNestedCodegen with ScalaGenBase

trait ScalaGenFat extends ScalaFatCodegen with ScalaGenBase


trait CLikeGenBase extends CLikeCodegen
trait CLikeGenEffect extends CLikeNestedCodegen with CLikeGenBase
trait CLikeGenFat extends CLikeFatCodegen with CLikeGenBase

trait GPUGenBase extends GPUCodegen
trait GPUGenEffect extends GPUGenBase with CLikeNestedCodegen
trait GPUGenFat extends GPUGenBase with CLikeFatCodegen

trait CudaGenBase extends CudaCodegen
trait CudaGenEffect extends CudaNestedCodegen with CudaGenBase
trait CudaGenFat extends CudaFatCodegen with CudaGenBase

trait OpenCLGenBase extends OpenCLCodegen
trait OpenCLGenEffect extends OpenCLNestedCodegen with OpenCLGenBase
trait OpenCLGenFat extends OpenCLFatCodegen with OpenCLGenBase

trait CGenBase extends CCodegen
trait CGenEffect extends CNestedCodegen with CGenBase
trait CGenFat extends CFatCodegen with CGenBase
