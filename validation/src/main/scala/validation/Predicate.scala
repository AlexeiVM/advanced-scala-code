package validation

object predicate {
  import cats.Semigroup
  import cats.data.Validated
  import cats.syntax.semigroup._ // For |+|
  import cats.syntax.monoidal._ // For |@|

  sealed trait Predicate[E,A] {
    import cats.data.Validated._ // For Valid and Invalid

    def and(that: Predicate[E,A]): Predicate[E,A] =
      And(this, that)

    def or(that: Predicate[E,A]): Predicate[E,A] =
      Or(this, that)

    def apply(a: A)(implicit s: Semigroup[E]): Validated[E,A] =
      this match {
        case Pure(f) => f(a)
        case And(l, r) =>
          (l(a) |@| r(a)) map { (_, _) => a }
        case Or(l, r) =>
          l(a) match {
            case Valid(a1)   => Valid(a)
            case Invalid(e1) =>
              r(a) match {
                case Valid(a2)   => Valid(a)
                case Invalid(e2) => Invalid(e1 |+| e2)
              }
          }
      }
  }
  final case class And[E,A](left: Predicate[E,A], right: Predicate[E,A]) extends Predicate[E,A]
  final case class Or[E,A](left: Predicate[E,A], right: Predicate[E,A]) extends Predicate[E,A]
  final case class Pure[E,A](f: A => Validated[E,A]) extends Predicate[E,A]
}