package asciidraw

class DrawingSuite extends munit.FunSuite:
  test("horizontal line points"):
    assertEquals(Drawing.linePoints(1, 1, 4, 1), Some(List((1, 1), (2, 1), (3, 1), (4, 1))))

  test("vertical line points"):
    assertEquals(Drawing.linePoints(2, 0, 2, 2), Some(List((2, 0), (2, 1), (2, 2))))

  test("line points in reverse order"):
    assertEquals(Drawing.linePoints(4, 1, 1, 1), Some(List((1, 1), (2, 1), (3, 1), (4, 1))))

  test("single point line"):
    assertEquals(Drawing.linePoints(3, 3, 3, 3), Some(List((3, 3))))

  test("diagonal line is not supported"):
    assertEquals(Drawing.linePoints(0, 0, 3, 3), None)

  test("rect points form an outline"):
    val points = Drawing.rectPoints(1, 1, 3, 3).toSet
    val expected = Set((1, 1), (2, 1), (3, 1), (1, 2), (3, 2), (1, 3), (2, 3), (3, 3))
    assertEquals(points, expected)

  test("rect points have no duplicates"):
    val points = Drawing.rectPoints(0, 0, 2, 2)
    assertEquals(points.size, points.toSet.size)

  test("1x1 rect is a single point"):
    assertEquals(Drawing.rectPoints(2, 3, 1, 1), List((2, 3)))

  test("fill zone stops at borders"):
    val canvas = Canvas(5, 3, Map.empty).withPixels(List((2, 0), (2, 1), (2, 2)), '#')
    assertEquals(Drawing.fillZone(canvas, 0, 0), Set((0, 0), (1, 0), (0, 1), (1, 1), (0, 2), (1, 2)))

  test("fill zone on a whole empty canvas"):
    val canvas = Canvas(3, 2, Map.empty)
    assertEquals(Drawing.fillZone(canvas, 1, 1).size, 6)

  test("fill zone starting on a drawn char spreads on that char"):
    val canvas = Canvas(3, 1, Map.empty).withPixels(List((0, 0), (1, 0)), '#')
    assertEquals(Drawing.fillZone(canvas, 0, 0), Set((0, 0), (1, 0)))
