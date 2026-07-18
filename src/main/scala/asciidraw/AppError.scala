package asciidraw

sealed trait AppError:
  def message: String

object AppError:
  case class UnknownCommand(name: String) extends AppError:
    def message: String = s"commande inconnue: $name"

  case class InvalidArgumentCount(command: String) extends AppError:
    def message: String = s"nombre d'arguments invalide pour $command"

  case class InvalidNumber(value: String) extends AppError:
    def message: String = s"nombre invalide: $value"

  case class InvalidDimensions(width: Int, height: Int) extends AppError:
    def message: String = s"dimensions invalides: $width $height"

  case class OutOfBounds(x: Int, y: Int) extends AppError:
    def message: String = s"coordonnées ($x, $y) en dehors du canevas"

  case object DiagonalLine extends AppError:
    def message: String = "seules les lignes horizontales et verticales sont supportées"

  case object NoCanvas extends AppError:
    def message: String = "aucun canevas n'a été créé"
