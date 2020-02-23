package jdksymlink.effect

import cats._
import cats.implicits._
import cats.effect._

import jdksymlink.core.data.YesOrNo

/**
 * @author Kevin Lee
 * @since 2020-01-22
 */
trait EffectConstructor[F[_]] {
  def effect[A](a: => A): F[A]
  def pureEffect[A](a: A): F[A]
  def unit: F[Unit]
}

object EffectConstructor {

  def apply[F[_] : EffectConstructor]: EffectConstructor[F] = implicitly[EffectConstructor[F]]

  implicit val ioEffectConstructor: EffectConstructor[IO] = new EffectConstructor[IO] {

    override def effect[A](a: => A): IO[A] = IO(a)

    override def pureEffect[A](a: A): IO[A] = IO.pure(a)

    override def unit: IO[Unit] = IO.unit
  }

  implicit val idSideEffectConstructor: EffectConstructor[Id] = new EffectConstructor[Id] {

    override def effect[A](a: => A): Id[A] = a

    override def pureEffect[A](a: A): Id[A] = a

    override def unit: Id[Unit] = ()
  }

}

trait ConsoleEffect[F[_]] {
  def readLn: F[String]

  def putStrLn(value: String): F[Unit]

  def readYesOrNo(prompt: String): F[YesOrNo]
}

object ConsoleEffect {
  def apply[F[_]: ConsoleEffect]: ConsoleEffect[F] = implicitly[ConsoleEffect[F]]

  implicit val ioConsoleEffect: ConsoleEffect[IO] = new ConsoleEffectF[IO]

  final class ConsoleEffectF[F[_] : EffectConstructor : Monad] extends ConsoleEffect[F] {
    override def readLn: F[String] =
      EffectConstructor[F].effect(scala.io.StdIn.readLine)

    override def putStrLn(value: String): F[Unit] =
      EffectConstructor[F].effect(println(value))

    override def readYesOrNo(prompt: String): F[YesOrNo] = for {
      _ <- putStrLn(prompt)
      answer <- readLn
      yesOrN <-  answer match {
        case "y" | "Y" =>
          EffectConstructor[F].effect(YesOrNo.yes)
        case "n" | "N" =>
          EffectConstructor[F].effect(YesOrNo.no)
        case _ =>
          readYesOrNo(prompt)
      }
    } yield yesOrN

  }
}

trait Effectful[F[_]] {

  implicit protected def EF: EffectConstructor[F]

  def effect[A](a: => A): F[A] = EF.effect(a)

  def pureEffect[A](a: A): F[A] = EF.pureEffect(a)

  def effectUnit: F[Unit] = EF.unit

}

trait ConsoleEffectful[F[_]] {

  implicit protected def CF: ConsoleEffect[F]

  def readLn: F[String] = CF.readLn

  def putStrLn(value: String): F[Unit] = CF.putStrLn(value)

  def readYesOrNo(prompt: String): F[YesOrNo] = CF.readYesOrNo(prompt)

}