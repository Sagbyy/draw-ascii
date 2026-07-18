package asciidraw

import scala.annotation.tailrec

object Drawing:
  def linePoints(x1: Int, y1: Int, x2: Int, y2: Int): Option[List[(Int, Int)]] =
    if x1 == x2 then Some(range(y1, y2).map(y => (x1, y)))
    else if y1 == y2 then Some(range(x1, x2).map(x => (x, y1)))
    else None

  def rectPoints(x: Int, y: Int, width: Int, height: Int): List[(Int, Int)] =
    val horizontal = range(x, x + width - 1).flatMap(px => List((px, y), (px, y + height - 1)))
    val vertical = range(y, y + height - 1).flatMap(py => List((x, py), (x + width - 1, py)))
    (horizontal ++ vertical).distinct

  def fillZone(canvas: Canvas, x: Int, y: Int): Set[(Int, Int)] =
    explore(canvas, canvas.charAt(x, y), List((x, y)), Set.empty)

  @tailrec
  private def explore(
      canvas: Canvas,
      target: Char,
      frontier: List[(Int, Int)],
      visited: Set[(Int, Int)]
  ): Set[(Int, Int)] =
    frontier match
      case Nil => visited
      case (x, y) :: rest =>
        if visited.contains((x, y)) || !canvas.contains(x, y) || canvas.charAt(x, y) != target then
          explore(canvas, target, rest, visited)
        else explore(canvas, target, neighbors(x, y) ++ rest, visited + ((x, y)))

  private def neighbors(x: Int, y: Int): List[(Int, Int)] =
    List((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1))

  private def range(from: Int, to: Int): List[Int] =
    ((from min to) to (from max to)).toList
